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

package com.gworks.richtext;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import com.gworks.richtext.tags.Bold;
import com.gworks.richtext.tags.Italic;
import com.gworks.richtext.widget.RichEditText;

public class MainActivity extends AppCompatActivity {

    RichEditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edt_layout);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        editText = findViewById(R.id.editText);
        editText.registerMarkup(Bold.ID,new Bold());
        editText.registerMarkup(Italic.ID,new Italic());
        LinearLayout layout = findViewById(R.id.buttonLayout);
        layout.addView(newMarkupButton("Bold",Bold.ID));
        layout.addView(newMarkupButton("Italics",Italic.ID));
    }

    private Button newMarkupButton(String label, int mId){

        Button b = new Button(this);
        b.setText(label);
        b.setTag(mId);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.onMarkupClicked((int)view.getTag());
            }
        });
        return b;
    }
}
