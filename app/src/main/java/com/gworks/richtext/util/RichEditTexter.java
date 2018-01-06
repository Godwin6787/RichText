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
import android.widget.EditText;

import com.gworks.richtext.tags.AttributedMarkup;
import com.gworks.richtext.tags.Markup;

import java.util.List;

public class RichEditTexter extends RichTexter {

    private static final String TAG = "@RichEditTexter";

    public RichEditTexter(EditText editText) {
        super(editText);
        editText.addTextChangedListener(textWatcher);
    }

    @Override
    public EditText getRichTextView() {
        return (EditText) super.getRichTextView();
    }

    public void apply(Class<? extends Markup> markupType, Object value) {
        EditText editText = getRichTextView();
        applyInternal(createMarkup(markupType, value), editText.getSelectionStart(), editText.getSelectionEnd());
    }

    public void apply(Markup markup) {
        EditText editText = getRichTextView();
        applyInternal(markup, editText.getSelectionStart(), editText.getSelectionEnd());
    }

    /**
     * Applies the given markup in the given range.
     *
     * @param markup markup to apply
     * @param from inclusive
     * @param to exclusive
     */
    public void apply(Markup markup, int from, int to) {
        applyInternal(markup, from, to);
    }

    /**
     * Applies the given markup in the given range.
     *
     * @param markup markup to apply
     * @param from inclusive
     * @param to exclusive
     */
    private void applyInternal(Markup markup, int from, int to) {
        EditText editText = getRichTextView();
        markup.applyInternal(editText.getText(), from, to,
                from == to ? Spannable.SPAN_MARK_MARK : Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        addToSpanTransitions(markup, from, to);
    }

    public void remove(Class<? extends Markup> markupType) {
        EditText editText = getRichTextView();
        remove(markupType, editText.getSelectionStart(), editText.getSelectionEnd());
    }

    public void remove(Class<? extends Markup> markupType, int from, int to) {
        for (Markup appliedMarkup : getAppliedMarkups(from, to))
            if (appliedMarkup.getClass() == markupType)
                removeInternal(appliedMarkup, from, to);
    }

    /**
     * Removes all the markups from the current selection if any.
     */
    public void removeAll() {
        EditText editText = getRichTextView();
        removeAll(editText.getSelectionStart(), editText.getSelectionEnd());
    }

    /**
     * Removes all the markups from the given range.
     *
     * @param from inclusive
     * @param to exclusive
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
     * @param markup markup to remove
     * @param from inclusive
     * @param to exclusive
     */
    private void removeInternal(Markup markup, int from, int to) {
        if (markup != null) {
            EditText editText = getRichTextView();
            Spannable text = editText.getText();
            int start = markup.getSpanStart(text);
            int end = markup.getSpanEnd(text);

            // If the markup is really applied in the text.
            if (start >= 0) {

                //First remove from the old range and reapply if splittable.
                removeFromSpanTransitions(markup, start, end);
                markup.removeInternal(text);

                //If the markup is splittable apply in the outer region.
                if (markup.isSplittable()) {
                    boolean reused = false;
                    if (start < from) {
                        //The removed markup is reused here.
                        applyInternal(markup, start, from);
                        reused = true;
                    }
                    if (end > to) {
                        Object value = markup instanceof AttributedMarkup ? ((AttributedMarkup) markup).getAttributes() : null;
                        //If not reused above reuse here.
                        applyInternal(reused ? createMarkup(markup.getClass(), value) : markup, to, end);
                    }
                }
            }
        }
    }

    public void update(Class<? extends Markup> markupType, Object value) {
        remove(markupType);
        apply(markupType, value);
    }

    /**
     * Call this when a markup menu item is clicked. This method takes care of toggling the
     * markup, splitting the markup, updating the markup, etc.
     *
     * @param markupType
     * @param value
     */
    public void onMarkupMenuClicked(Class<? extends Markup> markupType, @Nullable Object value) {
        EditText editText = getRichTextView();
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        boolean toggled = false;
        for (Markup existing : getAppliedMarkups(start, end)) {
            if (!existing.canExistWith(markupType)) {
                removeInternal(existing, start, end);
                if (existing.getClass() == markupType)
                    // If it can not exist with itself toggle.
                    toggled = true;
            }
        }
        // Attributed markups are updated (reapplied) hence always applied.
        if (AttributedMarkup.class.isAssignableFrom(markupType) || !toggled)
            apply(markupType, value);
    }

    private Markup createMarkup(Class<? extends Markup> markupType, Object value) {
        try {
            //TODO Add reflection code to create an instance for attributed markups.
            return markupType.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
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

}