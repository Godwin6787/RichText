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

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.style.StyleSpan;

import com.gworks.richtext.Constants;


public class Italic extends StyleSpan implements Tag {

    public static final int ID = 2;

    public Italic() {
        super(Typeface.ITALIC);
    }

    @NonNull
    @Override
    public String getTag() {
        return Constants.TAG_ITALIC;
    }

    @Override
    public int getType() {
        return ID;
    }

    @Override
    public boolean canExistWith(int markupId) {
        return getType() != markupId;
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
    public Object getSpan() {
        return this;
    }

    @Override
    public boolean isSplittable() {
        return true;
    }
}
