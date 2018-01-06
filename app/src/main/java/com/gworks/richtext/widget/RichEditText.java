package com.gworks.richtext.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import com.gworks.richtext.tags.Markup;
import com.gworks.richtext.util.RichEditTexter;

/**
 * Created by Godwin Lewis on 12/9/2017.
 */

public class RichEditText extends AppCompatEditText{

    private RichEditTexter manager;

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
        manager = new RichEditTexter(this);
    }

    public void onMarkupClicked(Class<? extends Markup> id){
        manager.onMarkupMenuClicked(id,null);
    }
}
