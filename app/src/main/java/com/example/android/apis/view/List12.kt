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

package com.example.android.apis.view

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates the using a list view in transcript mode. The bottom of the layout is an
 * [EditText], clicking on the [EditText] view .add()'s the text to the [ArrayAdapter]
 * filling the `ListView` above it.
 */
class List12 : AppCompatActivity(), View.OnClickListener, View.OnKeyListener {
    /**
     * [EditText] in our layout that the user can type in, and add to our `ListView`
     */
    private var mUserText: EditText? = null

    /**
     * [ArrayAdapter] which feeds our `ListView`
     */
    private var mAdapter: ArrayAdapter<String>? = null

    /**
     * Our database which feeds `ArrayAdapter<String>` field [mAdapter]
     */
    private val mStrings = ArrayList<String>()

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.list_12, and initialize
     * our [ListView] variable `val list` by finding the view with ID `R.id.list`. Next we
     * initialize our `ArrayAdapter<String>` field [mAdapter] with an an [ArrayAdapter] constructed
     * to display our list [mStrings] using the layout file android.R.layout.simple_list_item_1
     * to display each item, and we set the adapter of `list` to it. We initialize our field [mUserText]
     * by finding the view with ID `R.id.userText`, and then set both its [View.OnClickListener] and
     * [View.OnKeyListener] to "this".
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_12)
        val list: ListView = findViewById(R.id.list)
        mAdapter = ArrayAdapter(
            /* context = */ this,
            /* resource = */ android.R.layout.simple_list_item_1,
            /* objects = */ mStrings
        )
        list.adapter = mAdapter
        mUserText = findViewById(R.id.userText)
        mUserText!!.setOnClickListener(this)
        mUserText!!.setOnKeyListener(this)
    }

    /**
     * Called when the view [mUserText] has been clicked. We just call our method [sendText]
     * to add the text in [mUserText] to the adapter [mAdapter].
     *
     * @param v The view that was clicked.
     */
    override fun onClick(v: View) {
        sendText()
    }

    /**
     * Convenience method to add the text in [mUserText] to the adapter [mAdapter]. First
     * we initialize [String] variable `val text` by fetching the string value of the text in
     * [mUserText]. Then we call the `add` method of [mAdapter] to add the text to our `ListView`.
     * Finally we set the text of [mUserText] to the empty string.
     */
    private fun sendText() {
        val text = mUserText!!.text.toString()
        mAdapter!!.add(text)
        mUserText!!.setText("")
    }

    /**
     * Called when a hardware key is dispatched to a view. If the action of [KeyEvent] parameter
     * [event] is ACTION_DOWN we switch on the value of the [keyCode]:
     *
     *  * KEYCODE_DPAD_CENTER - we fall through to the KEYCODE_ENTER case
     *
     *  * KEYCODE_ENTER - we call our method [sendText] and return true to the caller thereby
     *  consuming the event.
     *
     * If the action of [KeyEvent] parameter [event] is not ACTION_DOWN, or a different character,
     * we return false to the caller.
     *
     * @param v       The view the key has been dispatched to.
     * @param keyCode The code for the physical key that was pressed
     * @param event   The KeyEvent object containing full information about the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    sendText()
                    return true
                }
            }
        }
        return false
    }
}
