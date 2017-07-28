package com.gworks.richtext.util;

import com.gworks.richtext.tags.Bold;
import com.gworks.richtext.tags.Italic;
import com.gworks.richtext.tags.Link;

/**
 * Created by durgadass on 15/7/17.
 */

public class HtmlConverter extends MarkupConverter {

    public static final String BOLD = "b";
    public static final String ITALIC = "i";
    public static final String UNDERLINE = "u";
    public static final String LINK = "a";
    public static final String H1 = "h1";
    public static final String H2 = "h2";
    public static final String H3 = "h3";
    public static final String H4 = "h4";

    public static final String ATTR_URL = "href";
    public static final String ATTR_SRC = "src";

    public static final String LT = "<";
    public static final String _LT = "</";
    public static final String GT = ">";
    public static final String _GT = "/>";

    public HtmlConverter(UnknownMarkupHandler unknownMarkupHandler) {
        super(unknownMarkupHandler);
    }

    @Override
    public boolean convertMarkup(StringBuilder sb, Bold boldMarkup, boolean begin) {
        sb.append(begin ? LT : _LT + BOLD + GT);
        return true;
    }

    @Override
    public boolean convertMarkup(StringBuilder sb, Italic italicMarkup, boolean begin) {
        sb.append(begin ? LT : _LT + ITALIC + GT);
        return true;
    }

    @Override
    public boolean convertMarkup(StringBuilder sb, Link linkMarkup, boolean begin) {
        sb.append(begin ? LT : _LT);
        sb.append(LINK);
        if (begin)
            sb.append(" " + ATTR_URL + "=" + linkMarkup.getURL());
        sb.append(GT);
        return true;
    }
}
