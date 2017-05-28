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

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.style.StyleSpan;



public class ItalicMarkup extends StyleSpan implements Markup {

    public static final int ID = 2;

    public ItalicMarkup(){
        super(Typeface.ITALIC);
    }

    @NonNull
    @Override
    public String getTag() {
        return "i";
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public boolean canExistWith(int markupId) {
        return getId() != markupId;
    }

}
