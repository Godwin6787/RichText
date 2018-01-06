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

/**
 * Created by Godwin Lewis on 5/11/2017.
 */

/**
 * Represents an attributed markup or markup with parameters. It could be single
 * param (like the url in Link markup) or a set of params (like style params in Font markup).
 * Often set of params are wrapped in a single class and used as generic param <code>ATTR</code>
 * while implementing this interface and single param need not be wrapped.
 * @param <ATTR>
 */
public abstract class AttributedMarkup<ATTR> extends Markup {

   /**
    * Returns the attributes of this {@link AttributedMarkup}.
    * @return
    */
   public abstract ATTR getAttributes();

}
