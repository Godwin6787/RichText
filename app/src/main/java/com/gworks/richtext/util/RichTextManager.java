/*
 * Copyright 2017 Godwin Lewis
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.gworks.richtext.util;

import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.widget.EditText;

import com.gworks.richtext.tags.AttributedMarkup;
import com.gworks.richtext.tags.Markup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class RichTextManager {

    private static final String TAG = "@RichTextManager";

    // The text view which acts as rich text view.
    private EditText editText;

    //Mapping between the index and its span transitions in the text.
    private SparseArray<SpanTransition> spanTransitions;
    private SparseArray<Markup> prototypes;

    public RichTextManager(EditText editText) {
        this.editText = editText;
        spanTransitions = new SparseArray<>();
        prototypes = new SparseArray<>();
        editText.addTextChangedListener(textWatcher);
    }

    public void registerMarkup(int markupType, Markup markup) {
        prototypes.put(markupType, markup);
    }

    public <T> void apply(int markupType, T value) {
        applyInternal(createMarkup(markupType, value), editText.getSelectionStart(), editText.getSelectionEnd());
    }

    /**
     * Applies the given markup in the given range.
     *
     * @param markup
     * @param from
     * @param to
     */
    public void apply(Markup markup, int from, int to) {
        applyInternal(markup, from, to);
    }

    /**
     * Applies the given markup in the given range.
     *
     * @param markup
     * @param from
     * @param to
     */
    private void applyInternal(Markup markup, int from, int to) {
        markup.apply(editText.getText(), from, to,
                from == to ? Spannable.SPAN_MARK_MARK : Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        addToSpanTransitions(markup, from, to);
    }

    public void remove(int markupType) {
        remove(markupType, editText.getSelectionStart(), editText.getSelectionEnd());
    }

    public void remove(int markupType, int from, int to) {
        for (Markup appliedMarkup : getAppliedMarkups(from, to))
            if (appliedMarkup.getType() == markupType)
                removeInternal(appliedMarkup, from, to);
    }

    /**
     * Removes all the markups from the current selection if any.
     */
    public void removeAll() {
        removeAll(editText.getSelectionStart(), editText.getSelectionEnd());
    }

    /**
     * Removes all the markups from the given range.
     *
     * @param from
     * @param to
     */
    public void removeAll(int from, int to) {
        for (Markup appliedMarkup : getAppliedMarkups(from, to))
            removeInternal(appliedMarkup, from, to);
    }

    /**
     * Removes the given markup from the given range. If the markup spans outside the
     * given range the markup is retained in the outer region if the markup is splittable.
     * Otherwise the markup is removed entirely.
     *
     * @param markup
     * @param from
     * @param to
     */
    private void removeInternal(Markup markup, int from, int to) {
        if (markup != null) {
            Spannable text = editText.getText();
            int start = text.getSpanStart(markup);
            int end = text.getSpanEnd(markup);

            // If the markup is really applied in the text.
            if (start >= 0) {

                markup.remove(editText.getText());
                removeFromSpanTransitions(markup, from, to);

                //If the markup is splittable apply in the outer region.
                if (markup.isSplittable()) {
                    boolean reused = false;
                    if (start < from) {
                        //The removed markup is reused here.
                        applyInternal(markup, start, from);
                        reused = true;
                    }
                    if (end > to) {
                        Object value = markup instanceof AttributedMarkup ? ((AttributedMarkup) markup).getValue() : null;
                        //If not reused above reuse here.
                        applyInternal(reused ? createMarkup(markup.getType(), value) : markup, to, end);
                    }
                }
            }
        }
    }

    private void removeFromSpanTransitions(Markup markup, int from, int to) {

        SpanTransition transitionFrom = spanTransitions.get(from);
        if (transitionFrom != null)
            transitionFrom.startingSpans.remove(markup);

        SpanTransition transitionTo = spanTransitions.get(to);
        if (transitionTo != null)
            transitionTo.endingSpans.remove(markup);

//        List<Markup> startingSpans = spansStartingAt(from);
//        if (startingSpans != null)
//            startingSpans.remove(markup);
//
//        List<Markup> endingSpans = spansEndingAt(to);
//        if (endingSpans != null)
//            endingSpans.remove(markup);

    }

    private void addToSpanTransitions(Markup markup, int from, int to) {

        SpanTransition transitionFrom = spanTransitions.get(from);
        if (transitionFrom == null)
            spanTransitions.put(from, transitionFrom = new SpanTransition());
        transitionFrom.startingSpans.add(markup);

        SpanTransition transitionTo = spanTransitions.get(to);
        if (transitionTo == null)
            spanTransitions.put(to, transitionTo = new SpanTransition());
        transitionTo.endingSpans.add(markup);

    }

    public <T> void update(int markupType, T value) {
        remove(markupType);
        apply(markupType, value);
    }

    public boolean isApplied(int markupType) {
        return isApplied(prototypes.get(markupType).getClass(), editText.getSelectionStart(), editText.getSelectionEnd());
    }

    public boolean isApplied(int markupType, int from, int to) {
        return isApplied(prototypes.get(markupType).getClass(), from, to);
    }

    private boolean isApplied(Class<? extends Markup> markupClass, int from, int to) {
        return editText.getText().getSpans(from, to, markupClass).length > 0;
    }

    private boolean isApplied(Markup markup) {
        return editText.getText().getSpanStart(markup) >= 0;
    }

    private boolean isApplied(Markup markup, int from, int to) {
        int start = editText.getText().getSpanStart(markup);
        int end = editText.getText().getSpanEnd(markup);
        return (start > 0 && start >= from && start < to) &&
                (end > 0 && end > from && end <= to);
    }

    /**
     * Returns all the markups applied in the current selection if any.
     *
     * @return
     */
    public List<Markup> getAppliedMarkups() {
        return getAppliedMarkups(editText.getSelectionStart(), editText.getSelectionEnd() + 1);
    }

    /**
     * Returns all the markups applied strictly inside the given range [from, to).
     *
     * @param from from inclusive
     * @param to to exclusive
     * @return
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
     * @param index
     * @return unmodifiable list of markups
     */
    public List<Markup> getSpansStartingAt(int index) {
        List<Markup> spans = spansStartingAt(index);
        return spans == null ? Collections.<Markup>emptyList() : Collections.unmodifiableList(spans);
    }

    /**
     * Returns the markups ending at the given index.
     *
     * @param index
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
     *
     * @return
     */
    public String getPlainText() {
        return editText.getText().toString();
    }

    /**
     * Returns the html equivalent of the rich text in the text view.
     *
     * @return
     */
    public String getHtml() {
        return getHtml(null);
    }

    /**
     * Returns the html equivalent of the rich text in the text view.
     *
     * @param unknownMarkupHandler the handler to handle the unknown markups.
     * @return
     */
    public String getHtml(MarkupConverter.UnknownMarkupHandler unknownMarkupHandler) {

        Spanned text = editText.getText();
        StringBuilder html = new StringBuilder(text.length());
        HtmlConverter htmlConverter = new HtmlConverter(unknownMarkupHandler);
        int processed = 0;
        int end = text.length();
        List<Markup> openSpans = new LinkedList<>();

        while (processed < end) {
            // Get the next span transition.
            int transitionIndex = text.nextSpanTransition(processed, end, null);
            if (transitionIndex > processed)
                html.append(text.subSequence(processed, transitionIndex));

            List<Markup> startingSpans = spansStartingAt(transitionIndex);
            for (Markup startingSpan : startingSpans) {
                startingSpan.convert(html, htmlConverter, true);
                // Consider a starting span as an opening span.
                openSpans.add(startingSpan);
            }

            // Iterate all ending spans at the transition index.
            List<Markup> endingSpans = spansEndingAt(transitionIndex);
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

            processed = transitionIndex;
        }
        if (openSpans.isEmpty())
            return html.toString();
        else throw new IllegalStateException("Spans are not well formed");
    }

    /**
     * Call this when a markup menu item is clicked. This method takes care of toggling the
     * markup, splitting the markup, updating the markup, etc.
     * @param markupType
     * @param value
     * @param <V>
     */
    public <V> void onMarkupMenuClicked(int markupType, @Nullable V value) {

        Markup prototype = prototypes.get(markupType);
        if (prototype == null)
            throw new IllegalArgumentException("The given markupType " + markupType + " is not registered");

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        boolean toggled = false;
        for (Markup existing : getAppliedMarkups()) {
            if (!prototype.canExistWith(existing.getType())) {
                removeInternal(existing, start, end);
                if (existing.getType() == prototype.getType())
                    // If it can not exist with itself toggle.
                    toggled = true;
            }
        }
        // Attributed markups are updated (reapplied) hence always .
        if (prototype instanceof AttributedMarkup || !toggled)
            apply(markupType, value);
    }

    private <V> Markup createMarkup(int markupType, V value) {
        return prototypes.get(markupType);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        private List<Markup> markupMarks;
        private int replacedLength;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            //TODO need to remove the spans in text to be removed

            if (count == 0) {
                //Only 0 -length markups need to be replaced
                markupMarks = getAppliedMarkups(start, start + count);
                replacedLength = after;
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //TODO need to handle the spans in newly added text
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (markupMarks != null) {
                for (Markup markup : markupMarks) {
                    int spanStart = markup.getSpanStart(s);
                    s.removeSpan(markup);
                    s.setSpan(markup, spanStart, replacedLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                markupMarks = null;
            }
        }
    };

    /**
     * Class representing a span transition in the text at a given index.
     */
    private static class SpanTransition {

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