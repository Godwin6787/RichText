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

import android.text.Spannable;
import android.text.Spanned;

import com.gworks.richtext.util.MarkupConverter;

/**
 * Created by Godwin Lewis on 5/9/2017.
 */

public interface Markup {

    /**
     *
     * @param sb
     * @param converter
     * @param begin
     */
    void convert(StringBuilder sb, MarkupConverter converter, boolean begin);

    /**
     * Tells whether this markup can exist with the given markup type.
     * @param anotherType
     * @return
     */
    boolean canExistWith(Class<? extends Markup> anotherType);

    /**
     * Applies this markup to the given text in given range [from, to).
     * @param text
     * @param from from inclusive
     * @param to to exclusive
     * @param flags
     */
    void apply(Spannable text, int from ,int to, int flags);

    /**
     * Removes this markup from the given text.
     * @param text
     */
    void remove(Spannable text);

    /**
     * Returns the starting index of this markup in the given text. Returns -1 if not applied.
     * @param text
     * @return
     */
    int getSpanStart(Spanned text);

    /**
     * Returns the ending index of this markup in the given text. Returns -1 if not applied.
     * @param text
     * @return
     */
    int getSpanEnd(Spanned text);

    /**
     * Tells whether this markup type is splittable.
     * @return
     */
    boolean isSplittable();
}
