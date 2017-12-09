package com.gworks.richtext.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import com.gworks.richtext.tags.Markup;
import com.gworks.richtext.util.RichTextManager;

/**
 * Created by Godwin Lewis on 12/9/2017.
 */

public class RichEditText extends AppCompatEditText{

    private RichTextManager manager;

    public RichEditText(Context context) {
        super(context);
        init();
    }

    public RichEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RichEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        manager = new RichTextManager(this);
    }

    public void registerMarkup(int id, Markup markup){
        manager.registerMarkup(id,markup);
    }

    public void onMarkupClicked(int id){
        manager.onMarkupMenuClicked(id,null);
    }
}
