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

import java.util.Map;

/**
 * Created by Godwin Lewis on 5/11/2017.
 */

public interface AttributedTag extends HtmlTag {

   @NonNull Map<String,String> getAttributes();
   String valueOf(String attribute);
}
