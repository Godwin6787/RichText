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

import com.gworks.richtext.tags.AttributedTag;
import com.gworks.richtext.tags.HtmlTag;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RichTextManager {

    private static final String TAG = "@RichTextManager";
    private EditText editText;

    public RichTextManager(EditText editText) {
        this.editText = editText;
    }

    public <T extends HtmlTag> void manageMarkup(Class<T> markupClass, @Nullable AttributeValue mValue) {

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        HtmlTag actualHtmlTag = null;
        if (start != end)
            actualHtmlTag = newMarkup(markupClass, mValue);

        if (actualHtmlTag != null) {

            HtmlTag[] spans = editText.getText().getSpans(start, end, HtmlTag.class);
            if (spans.length == 0)
                setSpan(actualHtmlTag, true);

            else {
                if (actualHtmlTag instanceof AttributedTag)
                    manageAttributedMarkup((AttributedTag) actualHtmlTag, spans, start, end);
                else {
                    HtmlTag existingHtmlTag;
                    for (int i = 0; i < spans.length; i++) {
                        existingHtmlTag = spans[i];
                        if (existingHtmlTag.getId() == actualHtmlTag.getId()) {
                            removeMarkup(existingHtmlTag, start, end);
                            break;
                        } else {
                            if (!actualHtmlTag.canExistWith(existingHtmlTag.getId()))
                                removeMarkup(existingHtmlTag, start, end);
                            if (i == spans.length - 1)
                                setSpan(actualHtmlTag, true);
                        }
                    }
                }
            }
        }
    }

    private void manageAttributedMarkup(AttributedTag actualMarkup, HtmlTag[] spans, int start, int end) {

        HtmlTag existingHtmlTag;
        Map<String, String> attribs;
        for (int i = 0; i < spans.length; i++) {
            existingHtmlTag = spans[i];
            if (existingHtmlTag.getId() == actualMarkup.getId()) {
                attribs = actualMarkup.getAttributes();
                if (!hasNullAttributes(attribs))
                    setSpan(actualMarkup, true);
                removeMarkup(existingHtmlTag, start, end);
                break;
            } else {
                if (!actualMarkup.canExistWith(existingHtmlTag.getId()))
                    removeMarkup(existingHtmlTag, start, end);
                if (i == spans.length - 1)
                    setSpan(actualMarkup, true);
            }
        }
    }

    private boolean hasNullAttributes(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue() == null)
                return true;
        }
        return false;
    }

    private <T extends HtmlTag> HtmlTag newMarkup(Class<T> markupClass, AttributeValue mValue) {

        HtmlTag htmlTag = null;
        try {
            if (AttributedTag.class.isAssignableFrom(markupClass)) {
                if (mValue != null && mValue.getClass() != null) {
                    Constructor<T> ctor = markupClass.getConstructor(mValue.getValueClass());
                    htmlTag = ctor.newInstance(mValue.getValue());
                } else
                    Log.e(TAG, "AttributedMarkups cannot have a null ValueClass !");
            } else
                htmlTag = markupClass.newInstance();
        } catch (Exception e) {
            Log.e(TAG, "Could not instantiate " + markupClass, e);
        }
        return htmlTag;
    }

    private boolean hasSingleArgumentConstructor(Class<?> theClass) {

        Constructor<?>[] ctors = theClass.getConstructors();
        for (Constructor<?> constructor : ctors) {
            if (constructor.getParameterTypes().length == 1)
                return true;
        }
        return false;
    }

    public boolean isApplied(Class<? extends HtmlTag> markupClass) {

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        return editText.getText().getSpans(start, end, markupClass).length > 0;

    }

    public List<HtmlTag> getAppliedMarkups() {

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        HtmlTag[] htmlTags = editText.getText().getSpans(start, end, HtmlTag.class);
        return Arrays.asList(htmlTags);
    }

    private void removeMarkup(HtmlTag what, int from, int to) {

        Editable text = editText.getText();
        int start = text.getSpanStart(what);
        int end = text.getSpanEnd(what);

        text.removeSpan(what);

        if (start < from)
            text.setSpan(what, start, from, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (end > to)
            text.setSpan(what, to, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void setSpan(HtmlTag spanObject, boolean exclusiveInclusive) {

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        Editable text = editText.getText();
            text.setSpan(spanObject, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
    }
}
