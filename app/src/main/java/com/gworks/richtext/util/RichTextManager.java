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

import com.gworks.richtext.tags.AttributedTag;
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
    private EditText editText;
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

    public void apply(Markup markup, int from, int to) {
        applyInternal(markup, from, to);
    }

    private void applyInternal(Markup markup, int from, int to) {
        markup.apply(editText.getText(), from, to,
                from == to ? Spannable.SPAN_MARK_MARK : Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpanTransition transitionFrom = spanTransitions.get(from);
        if (transitionFrom == null)
            spanTransitions.put(from, transitionFrom = new SpanTransition());
        transitionFrom.startingSpans.add(markup);

        SpanTransition transitionTo = spanTransitions.get(to);
        if (transitionTo == null)
            spanTransitions.put(to, transitionTo = new SpanTransition());
        transitionTo.endingSpans.add(markup);

    }

    public void remove(int markupType) {
        remove(markupType, editText.getSelectionStart(), editText.getSelectionEnd());
    }

    public void remove(int markupType, int from, int to) {
        for (Markup appliedMarkup : getAppliedMarkups(from, to))
            if (appliedMarkup.getType() == markupType)
                remove(appliedMarkup, from, to);
    }

    public void removeAll() {
        removeAll(editText.getSelectionStart(), editText.getSelectionEnd());
    }

    public void removeAll(int from, int to) {
        for (Markup appliedMarkup : getAppliedMarkups(from, to))
            remove(appliedMarkup, from, to);
    }

    private void remove(Markup markup, int from, int to) {
        if (markup != null) {
            Spannable text = editText.getText();
            int start = text.getSpanStart(markup);
            int end = text.getSpanEnd(markup);
            removeInternal(markup, start, end);

            if (markup.isSplittable()) {
                if (start < from)
                    applyInternal(markup, start, from);
                if (end > to)
                    applyInternal(createMarkup(markup.getType(), null), to, end);
            }
        }
    }

    private void removeInternal(Markup markup, int from, int to) {
        markup.remove(editText.getText());
        spanTransitions.get(from).startingSpans.remove(markup);
        spanTransitions.get(to).endingSpans.remove(markup);
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

    public List<Markup> getAppliedMarkups() {
        return getAppliedMarkups(editText.getSelectionStart(), editText.getSelectionEnd());
    }

    public List<Markup> getAppliedMarkups(int from, int to) {
        ArrayList<Markup> markups = new ArrayList<>();
        Set<Markup> startedTags = new HashSet<>();
        for (int i = from; i < to; i++) {
            List<Markup> startingTags = getSpansStartingAt(i);
            startedTags.addAll(startingTags);
            List<Markup> endingTags = getSpansEndingAt(i);
            for (Markup endingTag : endingTags)
                if (startedTags.contains(endingTag)) {
                    markups.add(endingTag);
                    startedTags.remove(endingTag);
                }
        }
        return markups;
    }

    public List<Markup> getSpansStartingAt(int index) {
        return Collections.unmodifiableList(spansStartingAt(index));
    }

    public List<Markup> getSpansEndingAt(int index) {
        return Collections.unmodifiableList(spansEndingAt(index));
    }

    private List<Markup> spansStartingAt(int index) {
        return spanTransitions.get(index).startingSpans;
    }

    private List<Markup> spansEndingAt(int index) {
        return spanTransitions.get(index).endingSpans;
    }

    public String getPlainText() {
        return editText.getText().toString();
    }

    public String getHtml() {
        return getHtml(null);
    }

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

    public <V> void onMarkupMenuClicked(int markupType, @Nullable V value) {

        Markup prototype = prototypes.get(markupType);
        if (prototype == null)
            throw new IllegalArgumentException("The given markupType " + markupType + " is not registered");

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        boolean toggled = false;
        for (Markup existing : getAppliedMarkups()) {
            if (!prototype.canExistWith(existing.getType())) {
                remove(existing, start, end);
                if (existing.getType() == prototype.getType())
                    // If it can not exist with itself toggle.
                    toggled = true;
            }
        }
        if (prototype instanceof AttributedTag || !toggled)
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

    private static class SpanTransition {
        List<Markup> startingSpans;
        List<Markup> endingSpans;

        SpanTransition() {
            startingSpans = new LinkedList<>();
            endingSpans = new LinkedList<>();
        }

    }
}