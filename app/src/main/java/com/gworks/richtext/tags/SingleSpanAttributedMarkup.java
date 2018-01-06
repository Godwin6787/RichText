package com.gworks.richtext.tags;

import android.text.Spannable;

/**
 * Created by durgadass on 6/1/18.
 */

public abstract class SingleSpanAttributedMarkup<ATTR> extends BaseAttributedMarkup<ATTR> {

    private final Object span;

    public SingleSpanAttributedMarkup(Object span, ATTR attributes) {
        super(attributes);
        this.span = span;
    }

    @Override
    public void apply(Spannable text, int from, int to, int flags) {
        text.setSpan(span, from, to, flags);
    }

    @Override
    public void remove(Spannable text) {
        text.removeSpan(span);
    }

}
