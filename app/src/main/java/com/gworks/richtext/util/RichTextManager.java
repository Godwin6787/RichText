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

import android.text.Editable;
import android.text.Spanned;
import android.widget.EditText;
import com.gworks.richtext.markups.Markup;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;



public class RichTextManager {

    private EditText editText;
    private List<Markup> appliedMarkups;

    public RichTextManager(EditText editText){

        this.editText = editText;
        appliedMarkups = new ArrayList<>();
    }

    public void apply(Class<? extends Markup> markupClass, Object mValue){

        Markup m = createMarkup(markupClass,mValue);
        setSpan(m,true);
        appliedMarkups.add(m);
    }

    public void toggleMarkup(Class<? extends Markup> markupClass, Object mValue){

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        Markup[] spans = editText.getText().getSpans(start,end,markupClass);

        if(spans.length > 0){
            Markup theSpan = spans[0];
            if(appliedMarkups.contains(theSpan))
                removeMarkup(theSpan,start,end);
            else
                apply(markupClass,mValue);
        }

    }

    public boolean isApplied(Class<? extends Markup> markupClass){

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        return editText.getText().getSpans(start,end,markupClass).length > 0;

    }

    public List<Markup> getAppliedMarkups(){
        return appliedMarkups;
    }

    private void removeMarkup(Markup what, int from, int to){
        Editable text = editText.getText();
        int start = text.getSpanStart(what);
        int end = text.getSpanEnd(what);

        text.removeSpan(what);

        if(start < from)
            text.setSpan(what,start,from,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if(end > to)
            text.setSpan(what,to,end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        appliedMarkups.remove(what);
    }

    private void setSpan(Markup spanObject,boolean apply){

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        Editable text =  editText.getText();
        if(apply)
        text.setSpan(spanObject,start,end,Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        else
            removeMarkup(spanObject,start,end);
    }

    private <T extends Markup> Markup createMarkup(Class<T> markupClass, Object mValue){

        T markup;
        Constructor<T> constructor;
        try {
            if (mValue == null) {
                constructor = markupClass.getConstructor();
                markup = constructor.newInstance((Object[]) null);
            }
            else{
                constructor = markupClass.getConstructor(mValue.getClass());
                markup = constructor.newInstance(mValue);
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }

        return markup;
    }
}
