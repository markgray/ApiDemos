/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.view;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.example.android.apis.R;

import java.util.ArrayList;

/**
 * Demonstrates the using a list view in transcript mode. The bottom of the layout
 * is an EditText, clicking on the EditText view .add()'s the text to the ArrayAdapter
 * filling the ListView above it.
 */
public class List12 extends ListActivity implements OnClickListener, OnKeyListener {
    /**
     * {@code EditText} in our layout that the user can type in, and add to our {@code ListView}
     */
    private EditText mUserText;
    /**
     * {@code ArrayAdapter} which feeds our {@code ListView}
     */
    private ArrayAdapter<String> mAdapter;
    /**
     * Our database which feeds {@code ArrayAdapter<String> mAdapter}
     */
    private ArrayList<String> mStrings = new ArrayList<>();

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.list_12. Next we
     * initialize our field {@code ArrayAdapter<String> mAdapter} with an an {@code ArrayAdapter}
     * constructed to display our list {@code mStrings} using the layout file android.R.layout.simple_list_item_1
     * to display each item, and we set our list adapter to it. We initialize our field {@code mUserText}
     * by finding the view with ID R.id.userText, and then set both its {@code OnClickListener} and
     * {@code OnKeyListener} to "this".
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_12);

        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mStrings);

        setListAdapter(mAdapter);

        mUserText = (EditText) findViewById(R.id.userText);

        mUserText.setOnClickListener(this);
        mUserText.setOnKeyListener(this);
    }

    /**
     * Called when the view {@code mUserText} has been clicked. We just call our method {@code sendText}
     * to add the text in {@code mUserText} to the adapter {@code mAdapter}.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        sendText();
    }

    /**
     * Convenience method to add the text in {@code mUserText} to the adapter {@code mAdapter}. First
     * we initialize {@code String text} by fetching the string value of the text in {@code mUserText}.
     * Then we call the {@code add} method of {@code mAdapter} to add the text to our {@code ListView}.
     * Finally we set the text of {@code mUserText} to null.
     */
    private void sendText() {
        String text = mUserText.getText().toString();
        mAdapter.add(text);
        mUserText.setText(null);
    }

    /**
     * Called when a hardware key is dispatched to a view. If the action of {@code KeyEvent event}
     * is ACTION_DOWN we switch on the value of the {@code keyCode}:
     * <ul>
     * <li>KEYCODE_DPAD_CENTER - we fall through to the KEYCODE_ENTER case</li>
     * <li>
     * KEYCODE_ENTER - we call our method {@code sendText} and return true to the caller thereby
     * consuming the event.
     * </li>
     * </ul>
     * If the action of {@code KeyEvent event} is not ACTION_DOWN, or a different character, we return
     * false to the caller.
     *
     * @param v       The view the key has been dispatched to.
     * @param keyCode The code for the physical key that was pressed
     * @param event   The KeyEvent object containing full information about the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    sendText();
                    return true;
            }
        }
        return false;
    }
}
