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

package com.gworks.richtext.markups;

import android.support.annotation.NonNull;
import android.text.style.URLSpan;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Godwin Lewis on 5/11/2017.
 */

public class LinkMarkup extends URLSpan implements AttributedMarkup {

    public static final String ATTR_URL = "url";
    public static final int ID = 4;
    private Map<String, String> attributes;

    public LinkMarkup(String url) {
        super(url);
        attributes = new HashMap<>();
        attributes.put(ATTR_URL,url);
    }

    @NonNull
    @Override
    public String getTag() {
        return "a";
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public boolean canExistWith(int markupId) {
        return markupId != getId();
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
}
