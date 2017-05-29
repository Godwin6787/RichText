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

/**
 * Created by Godwin Lewis on 5/29/2017.
 */

public class MarkupValue<T> {

    private Class<T> valueClass;
    private Object value;

    public MarkupValue(Class<T> valueClass, T value) {
        this.valueClass = valueClass;
        this.value = value;
    }

    public Class<T> getValueClass() {
        return valueClass;
    }

    public Object getValue() {
        return value;
    }
}
