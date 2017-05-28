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

import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.gworks.richtext.markups.AttributedMarkup;
import com.gworks.richtext.markups.Markup;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Godwin Lewis on 5/9/2017.
 */

public class HtmlUtility {

    public static final String TAG = "@HtmlUtility";
    private final String TAG_START = "<";
    private final String TAG_START_CLOSED = "</";
    private final String TAG_END = ">";
    private Map<Class<?>, SpanTransformer> transformers;

    public HtmlUtility() {
        transformers = new HashMap<>();
    }

    public void registerTransformer(Class<?> spanClass, @NonNull SpanTransformer spanTransformer) {
        transformers.put(spanClass, spanTransformer);
    }

    public void registerTransformers(Map<Class<?>, SpanTransformer> transformers) {
        this.transformers = transformers;
    }

    public String toHtml(Spanned text) {

        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        Object[] spans = sb.getSpans(0, text.length(), Object.class);
        SpanTransformer transformer;

        for (Object span : spans) {
            transformer = transformers.get(span.getClass());
            if (transformer != null)
                transformer.transformSpan(span, sb);

            else {
                if (span instanceof AttributedMarkup)
                    convertAttributedMarkup((AttributedMarkup) span, sb);
                else if (span instanceof Markup)
                    convertMarkup((Markup) span, sb);
            }
        }
        return sb.toString();
    }

    private void convertMarkup(Markup span, SpannableStringBuilder sb) {

        int start = sb.getSpanStart(span);
        String tag = span.getTag();
        sb.insert(start, TAG_START + tag + TAG_END);
        sb.insert(sb.getSpanEnd(span), TAG_START_CLOSED + tag + TAG_END);
    }

    private void convertAttributedMarkup(AttributedMarkup span, SpannableStringBuilder sb) {

        int start = sb.getSpanStart(span);
        Map<String, String> attrMap = span.getAttributes();

        String tag = span.getTag();
        StringBuilder builder = new StringBuilder();
        builder.append(TAG_START).append(tag);
        for (Map.Entry<String, String> attribute : attrMap.entrySet()) {

            builder.append(" ").append(attribute.getKey()).append("=")
                    .append(" \"")
                    .append(attribute.getValue())
                    .append("\" ");
        }
        builder.append(TAG_END);
        sb.insert(start, builder.toString());
        sb.insert(sb.getSpanEnd(span), TAG_START_CLOSED + tag + TAG_END);
    }
}
