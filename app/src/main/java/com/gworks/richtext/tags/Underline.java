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

import android.text.style.UnderlineSpan;

import com.gworks.richtext.util.MarkupConverter;

/**
 * Created by Godwin Lewis on 5/9/2017.
 */

public class Underline extends StyleMarkup {

    public Underline() {
        super(new UnderlineSpan());
    }

    @Override
    public void convert(StringBuilder sb, MarkupConverter converter, boolean begin) {
        converter.convertMarkup(sb, this, begin);
    }

}
