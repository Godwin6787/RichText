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
import android.text.Spanned;
import android.util.Log;
import android.widget.EditText;

import com.gworks.richtext.markups.AttributedMarkup;
import com.gworks.richtext.markups.Markup;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

public class RichTextManager {

    private static final String TAG = "@RichTextManager";
    private EditText editText;

    public RichTextManager(EditText editText) {
        this.editText = editText;
    }

    public <T extends Markup> void manageMarkup(Class<T> markupClass, @Nullable Object mValue) {

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        Markup actualMarkup = null;
        if (start != end)
            actualMarkup = newMarkup(markupClass, mValue);

        if (actualMarkup != null) {

            Markup[] spans = editText.getText().getSpans(start, end, Markup.class);
            if (spans.length == 0)
                setSpan(actualMarkup, true);

            else {
                if (actualMarkup instanceof AttributedMarkup)
                    manageAttributedMarkup((AttributedMarkup) actualMarkup, spans, start, end);
                else {
                    Markup existingMarkup;
                    for (int i = 0; i < spans.length; i++) {
                        existingMarkup = spans[i];
                        if (existingMarkup.getId() == actualMarkup.getId()) {
                            removeMarkup(existingMarkup, start, end);
                            break;
                        } else {
                            if (!actualMarkup.canExistWith(existingMarkup.getId()))
                                removeMarkup(existingMarkup, start, end);
                            if (i == spans.length - 1)
                                setSpan(actualMarkup, true);
                        }
                    }
                }
            }
        }
    }

    private void manageAttributedMarkup(AttributedMarkup actualMarkup, Markup[] spans, int start, int end) {

        Markup existingMarkup;
        for (int i = 0; i < spans.length; i++) {
            existingMarkup = spans[i];
            if (existingMarkup.getId() == actualMarkup.getId()) {
                removeMarkup(existingMarkup, start, end);
                setSpan(actualMarkup, true);
                break;
            } else {
                if (!actualMarkup.canExistWith(existingMarkup.getId()))
                    removeMarkup(existingMarkup, start, end);
                if (i == spans.length - 1)
                    setSpan(actualMarkup, true);
            }
        }
    }

    private <T extends Markup> Markup newMarkup(Class<T> markupClass, Object mValue) {

        Markup markup = null;
        try {
            if (hasSingleArgumentConstructor(markupClass)) {
                Constructor<T> ctor = markupClass.getConstructor(mValue.getClass());
                markup = ctor.newInstance(mValue);
            } else
                markup = markupClass.newInstance();
        } catch (Exception e) {
            Log.e(TAG, "Could not instantiate " + markupClass, e);
        }
        return markup;
    }

    private boolean hasSingleArgumentConstructor(Class<?> theClass) {

        Constructor<?>[] ctors = theClass.getConstructors();
        for (Constructor<?> constructor : ctors) {
            if (constructor.getParameterTypes().length == 1)
                return true;
        }
        return false;
    }

    public boolean isApplied(Class<? extends Markup> markupClass) {

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        return editText.getText().getSpans(start, end, markupClass).length > 0;

    }

    public List<Markup> getAppliedMarkups() {

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        Markup[] markups = editText.getText().getSpans(start, end, Markup.class);
        return Arrays.asList(markups);
    }

    private void removeMarkup(Markup what, int from, int to) {

        Editable text = editText.getText();
        int start = text.getSpanStart(what);
        int end = text.getSpanEnd(what);

        text.removeSpan(what);

        if (start < from)
            text.setSpan(what, start, from, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (end > to)
            text.setSpan(what, to, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void setSpan(Markup spanObject, boolean apply) {

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        Editable text = editText.getText();
        if (apply)
            text.setSpan(spanObject, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        else
            removeMarkup(spanObject, start, end);
    }
}
