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

package com.gworks.richtext.tags;

import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.URLSpan;

import com.gworks.richtext.Constants;
import com.gworks.richtext.util.MarkupConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Godwin Lewis on 5/11/2017.
 */

public class Link extends URLSpan implements AttributedTag<String> {

    public static final int ID = 4;
    private Map<String, String> attributes;

    public Link(String url) {
        super(url);
        attributes = new HashMap<>();
        attributes.put(Constants.ATTR_URL, url);
    }

    @Override
    public void convert(StringBuilder sb, MarkupConverter converter, boolean begin) {
        converter.convertMarkup(sb, this, begin);
    }

    @Override
    public int getType() {
        return ID;
    }

    @Override
    public boolean canExistWith(int anotherType) {
        return anotherType != getType();
    }

    @NonNull
    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public String valueOf(String attribute) {
        return attributes.get(attribute);
    }

    @Override
    public void apply(Spannable text, int from, int to, int flags) {
        text.setSpan(this, from, to, flags);
    }

    @Override
    public void remove(Spannable text) {
        text.removeSpan(this);
    }

    @Override
    public String getValue() {
        return attributes.get(Constants.ATTR_URL);
    }

    @Override
    public boolean isSplittable() {
        return false;
    }

    @Override
    public int getSpanStart(Spanned text) {
        return text.getSpanStart(this);
    }

    @Override
    public int getSpanEnd(Spanned text) {
        return text.getSpanEnd(this);
    }
}
