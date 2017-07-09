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

import com.gworks.richtext.Constants;
import com.gworks.richtext.tags.AttributedTag;
import com.gworks.richtext.tags.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class RichTextManager {

    private static final String TAG = "@RichTextManager";
    private EditText editText;
    private SparseArray<Tag> prototypes;
    private Map<Object, Tag> appliedSpans;

    public RichTextManager(EditText editText) {
        this.editText = editText;
        prototypes = new SparseArray<>();
        appliedSpans = new HashMap<>();
        editText.addTextChangedListener(textWatcher);
    }

    public void registerMarkup(int markupType, Tag markup) {
        prototypes.put(markupType, markup);
    }

    public <T> void apply(int markupType, T value) {
        apply(createMarkup(markupType, value), editText.getSelectionStart(), editText.getSelectionEnd());
    }

    public void apply(Tag markup, int from, int to) {
        markup.apply(editText.getText(), from, to,
                from == to ? Spannable.SPAN_MARK_MARK : Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        appliedSpans.put(markup.getSpan(), markup);
    }

    public void remove(int markupType) {
        remove(markupType, editText.getSelectionStart(), editText.getSelectionEnd());
    }

    public void remove(int markupType, int from, int to) {
        remove(prototypes.get(markupType).getClass(), from, to);
    }

    public void removeAll() {
        removeAll(editText.getSelectionStart(), editText.getSelectionEnd());
    }

    public void removeAll(int from, int to) {
        remove(Object.class, from, to);
    }

    private void remove(Class<?> clazz, int from, int to) {
        Object[] markups = editText.getText().getSpans(from, to, clazz);
        for (Object span : markups)
            remove(appliedSpans.get(span), from, to);
    }

    private void remove(Tag markup, int from, int to) {
        if (markup != null) {
            Spannable text = editText.getText();
            int start = text.getSpanStart(markup);
            int end = text.getSpanEnd(markup);
            remove(markup);
            if (markup.isSplittable()) {
                if (start < from)
                    apply(markup, start, from);
                if (end > to)
                    apply(createMarkup(markup.getType(), null), to, end);
            }
        }
    }

    private void remove(Tag markup) {
        markup.remove(editText.getText());
        appliedSpans.remove(markup.getSpan());
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

    private boolean isApplied(Class<? extends Tag> markupClass, int from, int to) {
        return editText.getText().getSpans(from, to, markupClass).length > 0;
    }

    public List<Tag> getAppliedMarkups() {
        return getAppliedMarkups(editText.getSelectionStart(), editText.getSelectionEnd());
    }

    public List<Tag> getAppliedMarkups(int from, int to) {
        ArrayList<Tag> markups = new ArrayList<>();
        Object[] spans = editText.getText().getSpans(from, to, Object.class);
        for (Object span : spans) {
            if (appliedSpans.containsKey(span))
                markups.add(appliedSpans.get(span));
        }
        return markups;
    }

    public List<Object> getSpansStartingAt(int index) {
        List<Object> openSpans = new ArrayList<>();
        Spanned text = editText.getText();
        Object[] spans = text.getSpans(index, text.length(), Object.class);
        for (Object span : spans) {
            if (appliedSpans.containsKey(span))
                openSpans.add(span);
        }
        return openSpans;
    }

    public List<Object> getSpansEndingAt(int index) {
        List<Object> closedSpans = new ArrayList<>();
        Spanned text = editText.getText();
        Object[] spans = text.getSpans(0, index, Object.class);
        for (Object span : spans) {
            if (appliedSpans.containsKey(span))
                closedSpans.add(span);
        }
        return closedSpans;
    }

    public String getPlainText() {
        return editText.getText().toString();
    }

    public String getHtml() {

        Spanned text = editText.getText();
        StringBuilder html = new StringBuilder(text.length());
        int processed = 0;
        int end = text.length();
        List<Object> openSpans = new LinkedList<>();

        while (processed < end) {
            // Get the next span transition.
            int transitionIndex = text.nextSpanTransition(processed, end, null);

            // Iterate all ending spans at the transition index.
            List<Object> endingSpans = getSpansEndingAt(transitionIndex);
            for (Object endingSpan : endingSpans) {
                ListIterator<Object> iterator = openSpans.listIterator(openSpans.size());
                while (iterator.hasPrevious()) {
                    Object openSpan = iterator.previous();
                    // If an ending span has a matching open span, consider it as a closing
                    // span and remove the open span, and proceed to next ending span.
                    if (openSpan == endingSpan) {
                        iterator.remove();
                        Tag markup = appliedSpans.get(endingSpan);
                        html.append(Constants._LT + markup.getTag() + Constants.GT);
                        break;
                    }
                }
            }

            List<Object> startingSpans = getSpansStartingAt(transitionIndex);
            for (Object startingSpan : startingSpans) {
                if (appliedSpans.containsKey(startingSpan)) {
                    Tag markup = appliedSpans.get(startingSpan);
                    html.append(Constants.LT + markup.getTag() + Constants.GT);
                    // Consider a starting span as an opening span.
                    openSpans.add(startingSpan);
                }
            }
            html.append(text.subSequence(processed, transitionIndex));
            processed = transitionIndex;
        }
        if (openSpans.isEmpty())
            return html.toString();
        else throw new IllegalStateException("Spans are not well formed");
    }

    public <V> void onMarkupMenuClicked(int markupType, @Nullable V value) {

        Tag prototype = prototypes.get(markupType);
        if (prototype == null)
            throw new IllegalArgumentException("The given markupType " + markupType + " is not registered");

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        boolean toggled = false;
        for (Tag existing : getAppliedMarkups()) {
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

    private <V> Tag createMarkup(int markupType, V value) {
        return prototypes.get(markupType);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        private List<Tag> markupMarks;
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
                for (Tag markup : markupMarks) {
                    int spanStart = s.getSpanStart(markup.getSpan());
                    s.removeSpan(markup);
                    s.setSpan(markup, spanStart, replacedLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                markupMarks = null;
            }
        }
    };

}