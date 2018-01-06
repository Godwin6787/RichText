package com.gworks.richtext.tags;

import android.text.Spannable;

/**
 * Created by durgadass on 6/1/18.
 */

public abstract class StyleMarkup extends Markup {

    private Object styleSpan;

    public StyleMarkup(Object styleSpan) {
        this.styleSpan = styleSpan;
    }

    @Override
    public boolean canExistWith(Class<? extends Markup> anotherType) {
        return anotherType != getClass();
    }

    @Override
    public void apply(Spannable text, int from, int to, int flags) {
        text.setSpan(styleSpan, from, to, flags);
    }

    @Override
    public void remove(Spannable text) {
        text.removeSpan(styleSpan);
    }

    @Override
    public boolean isSplittable() {
        return true;
    }
}
