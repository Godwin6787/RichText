package com.gworks.richtext.tags;

/**
 * Created by durgadass on 6/1/18.
 */

public abstract class BaseAttributedMarkup<ATTR> extends AttributedMarkup<ATTR> {

    private final ATTR attributes;

    public BaseAttributedMarkup(ATTR attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean canExistWith(Class<? extends Markup> anotherType) {
        return anotherType != getClass();
    }

    @Override
    public ATTR getAttributes() {
        return attributes;
    }
}
