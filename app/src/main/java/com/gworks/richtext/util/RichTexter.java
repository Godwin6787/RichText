package com.gworks.richtext.util;

import android.support.annotation.Nullable;
import android.text.Spanned;
import android.util.SparseArray;
import android.widget.TextView;

import com.gworks.richtext.tags.Markup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Created by durgadass on 6/1/18.
 */

public class RichTexter {

    private static final String TAG = "@RichEditTexter";

    // The text view which acts as rich text view.
    private TextView textView;

    //Mapping between the index and its span transitions in the text.
    private SparseArray<SpanTransition> spanTransitions;

    public RichTexter(TextView textView) {
        this.textView = textView;
        spanTransitions = new SparseArray<>();
    }

    public TextView getRichTextView(){
        return textView;
    }

    public boolean isApplied(Class<? extends Markup> markupClass, int from, int to) {
        CharSequence text = textView.getText();
        return text instanceof Spanned && ((Spanned)text).getSpans(from, to, markupClass).length > 0;
    }

    public boolean isApplied(Markup markup) {
        CharSequence text = textView.getText();
        return text instanceof Spanned && ((Spanned)text).getSpanStart(markup) >= 0;
    }

    public boolean isApplied(Markup markup, int from, int to) {
        CharSequence text = textView.getText();
        if (text instanceof Spanned) {
            Spanned s = (Spanned) text;
            int start = s.getSpanStart(markup);
            int end = s.getSpanEnd(markup);
            return (start > 0 && start >= from && start < to) &&
                    (end > 0 && end > from && end <= to);
        }
        return false;
    }

    /**
     * Returns all the markups applied in the current selection if any.
     */
    public List<Markup> getAppliedMarkups() {
        return getAppliedMarkups(0, textView.getText().length());
    }

    /**
     * Returns all the markups applied strictly inside the given range [from, to).
     *
     * @param from from inclusive
     * @param to to exclusive
     */
    public List<Markup> getAppliedMarkups(int from, int to) {

        ArrayList<Markup> result = new ArrayList<>();
        Set<Markup> startedMarkups = new HashSet<>(); // To keep track of the started markups.

        for (int i = from; i < to; i++) {

            List<Markup> startingMarkups = spansStartingAt(i);
            if (startingMarkups != null)
                startedMarkups.addAll(startingMarkups);

            List<Markup> endingMarkups = spansEndingAt(i);
            if (endingMarkups != null) {
                for (Markup endingMarkup : endingMarkups) {
                    // Only markups started in the given range are added to the result.
                    if (startedMarkups.contains(endingMarkup)) {
                        result.add(endingMarkup);
                        startedMarkups.remove(endingMarkup);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the markups starting at the given index.
     *
     * @param index start index
     * @return unmodifiable list of markups
     */
    public List<Markup> getSpansStartingAt(int index) {
        List<Markup> spans = spansStartingAt(index);
        return spans == null ? Collections.<Markup>emptyList() : Collections.unmodifiableList(spans);
    }

    /**
     * Returns the markups ending at the given index.
     *
     * @param index end index
     * @return unmodifiable list of markups
     */
    public List<Markup> getSpansEndingAt(int index) {
        List<Markup> spans = spansEndingAt(index);
        return spans == null ? Collections.<Markup>emptyList() : Collections.unmodifiableList(spans);
    }

    @Nullable
    private List<Markup> spansStartingAt(int index) {
        SpanTransition transition = spanTransitions.get(index);
        return (transition != null) ? transition.startingSpans : null;
    }

    @Nullable
    private List<Markup> spansEndingAt(int index) {
        SpanTransition transition = spanTransitions.get(index);
        return (transition != null) ? transition.endingSpans : null;
    }

    /**
     * Returns the rich text in the text view as plain text (i.e. String).
     */
    public String getPlainText() {
        return textView.getText().toString();
    }

    /**
     * Returns the html equivalent of the rich text in the text view.
     */
    public String getHtml() {
        return getHtml(null);
    }

    /**
     * Returns the html equivalent of the rich text in the text view.
     *
     * @param unknownMarkupHandler the handler to handle the unknown markups.
     */
    public String getHtml(MarkupConverter.UnknownMarkupHandler unknownMarkupHandler) {

        CharSequence cs = textView.getText();
        if (!(cs instanceof Spanned))
            return cs.toString();

        Spanned text = (Spanned) cs;
        StringBuilder html = new StringBuilder(text.length());
        HtmlConverter htmlConverter = new HtmlConverter(unknownMarkupHandler);
        List<Markup> openSpans = new LinkedList<>();

        int processed = 0;
        int end = text.length();
        while (processed < end) {
            // Get the next span transition.
            int transitionIndex = text.nextSpanTransition(processed, end, null);
            if (transitionIndex > processed)
                html.append(text.subSequence(processed, transitionIndex));

            List<Markup> startingSpans = spansStartingAt(transitionIndex);
            if(startingSpans != null) {
                for (Markup startingSpan : startingSpans) {
                    startingSpan.convert(html, htmlConverter, true);
                    // Consider a starting span as an opening span.
                    openSpans.add(startingSpan);
                }
            }

            // Iterate all ending spans at the transition index.
            List<Markup> endingSpans = spansEndingAt(transitionIndex);
            if(endingSpans != null) {
                for (Markup endingSpan : endingSpans) {
                    ListIterator<Markup> iterator = openSpans.listIterator(openSpans.size());
                    while (iterator.hasPrevious()) {
                        Markup openSpan = iterator.previous();
                        // If an ending span has a matching open span, consider it as a closing
                        // span and remove the open span, and proceed to next ending span.
                        if (openSpan == endingSpan) {
                            iterator.remove();
                            endingSpan.convert(html, htmlConverter, false);
                            break;
                        }
                    }
                }
            }

            processed = transitionIndex;
        }
        if (openSpans.isEmpty())
            return html.toString();
        //TODO Will we really reach this?
        else throw new IllegalStateException("Spans are not well formed");
    }

    /*default*/ void removeFromSpanTransitions(Markup markup, int from, int to) {

        SpanTransition transitionFrom = spanTransitions.get(from);
        if (transitionFrom != null)
            transitionFrom.startingSpans.remove(markup);

        SpanTransition transitionTo = spanTransitions.get(to);
        if (transitionTo != null)
            transitionTo.endingSpans.remove(markup);
    }

    /*default*/ void addToSpanTransitions(Markup markup, int from, int to) {

        SpanTransition transitionFrom = spanTransitions.get(from);
        if (transitionFrom == null)
            spanTransitions.put(from, transitionFrom = new SpanTransition());
        transitionFrom.startingSpans.add(markup);

        SpanTransition transitionTo = spanTransitions.get(to);
        if (transitionTo == null)
            spanTransitions.put(to, transitionTo = new SpanTransition());
        transitionTo.endingSpans.add(markup);
    }

    /**
     * Class representing a span transition in the text at a given index.
     */
    /*default*/ static class SpanTransition {

        //spans starting at this span transition.
        List<Markup> startingSpans;

        //spans ending at this span transition.
        List<Markup> endingSpans;

        SpanTransition() {
            startingSpans = new LinkedList<>();
            endingSpans = new LinkedList<>();
        }

    }

}
