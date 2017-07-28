package com.gworks.richtext.util;

import com.gworks.richtext.tags.Bold;
import com.gworks.richtext.tags.Italic;
import com.gworks.richtext.tags.Link;
import com.gworks.richtext.tags.Markup;

/**
 * Created by durgadass on 15/7/17.
 */

public class MarkupConverter {

    private final UnknownMarkupHandler unknownMarkupHandler;

    public MarkupConverter(UnknownMarkupHandler unknownMarkupHandler){
        this.unknownMarkupHandler = unknownMarkupHandler;
    }

    public boolean convertMarkup(StringBuilder sb, Bold boldMarkup, boolean begin){
        return false;
    }

    public boolean convertMarkup(StringBuilder sb, Italic italicMarkup, boolean begin){
        return false;
    }

    public boolean convertMarkup(StringBuilder sb, Link linkMarkup, boolean begin){
        return false;
    }

    public final boolean convertMarkup(StringBuilder sb, Markup markup, boolean begin){
        if (unknownMarkupHandler != null)
            return unknownMarkupHandler.handleMarkup(sb, markup, begin);
        return false;
    }

    public interface UnknownMarkupHandler{
        boolean handleMarkup(StringBuilder sb, Markup markup, boolean begin);
    }
}
