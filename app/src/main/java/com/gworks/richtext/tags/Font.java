package com.gworks.richtext.tags;

import android.text.Spannable;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;

import com.gworks.richtext.util.MarkupConverter;

/**
 * Created by durgadass on 15/7/17.
 */

public class Font implements Markup {

    private TypefaceSpan typefaceSpan;
    private AbsoluteSizeSpan sizeSpan;
    private ForegroundColorSpan colorSpan;

    public Font(Attributes attributes){
        typefaceSpan = new TypefaceSpan(attributes.typeface);
        sizeSpan = new AbsoluteSizeSpan(attributes.size, true);
        colorSpan = new ForegroundColorSpan(attributes.color);
    }

    @Override
    public void convert(StringBuilder sb, MarkupConverter converter, boolean begin) {
        converter.convertMarkup(sb, this, begin);
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public boolean canExistWith(int anotherType) {
        return false;
    }

    @Override
    public void apply(Spannable text, int from, int to, int flags) {
        text.setSpan(typefaceSpan, from, to, flags);
        text.setSpan(sizeSpan, from, to, flags);
        text.setSpan(colorSpan, from, to, flags);
    }

    @Override
    public void remove(Spannable text) {
        text.removeSpan(typefaceSpan);
        text.removeSpan(sizeSpan);
        text.removeSpan(colorSpan);
    }

    @Override
    public int getSpanStart(Spanned text) {
        return text.getSpanStart(typefaceSpan);
    }

    @Override
    public int getSpanEnd(Spanned text) {
        return text.getSpanEnd(typefaceSpan);
    }

    @Override
    public boolean isSplittable() {
        return true;
    }

    public static class Attributes {
        public final int color;
        public final int size;
        public final String typeface;

        public Attributes(String typeface, int size, int color) {
            this.typeface = typeface;
            this.size = size;
            this.color = color;
        }

        public Attributes() {
            this(null, -1, 0);
        }
    }
}
