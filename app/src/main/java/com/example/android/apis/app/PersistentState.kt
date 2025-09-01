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

package com.example.android.apis.app

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import androidx.core.content.edit

/**
 * Simple example of using persistent preferences to retain a screen's state.
 *
 * This can be used as an alternative to the normal **onSaveInstanceState()** mechanism, if you
 * wish the state to persist even after an activity is finished.
 *
 * Note that using this approach requires more care, since you are sharing
 * the persistent state potentially across multiple instances of the activity.
 * In particular, if you allow a new instance of the activity to be launched
 * directly on top of the existing instance, the state can get out of sync
 * because the new instance is resumed before the old one is paused.
 *
 * For any persistent state that is not simplistic, a content
 * provider is often a better choice.
 *
 * In this example we are currently saving and restoring the state of the
 * top text editor, but not of the bottom text editor.  You can see the difference
 * by editing the two text fields, then going back from the activity and
 * starting it again.
 *
 * Demo App/Activity/Save Restore State
 *
 * Source files:
 *
 * src/com.example.android.apis/app/PersistentState.java The Save/Restore Screen implementation
 * /res/any/layout/save_restore_state.xml Defines contents of the screen
 */
class PersistentState : AppCompatActivity() {
    /**
     * [EditText] whose text we save in our `SharedPreferences` under the key "text"
     */
    private var mSaved: EditText? = null

    /**
     * Initialization of the Activity after it is first created.  Here we use
     * [setContentView()][androidx.appcompat.app.AppCompatActivity.setContentView]
     * to set up the Activity's content to our layout file R.layout.save_restore_state,
     * set the text of the [TextView] with ID  R.id.msg to the string R.string.persistent_msg,
     * and initialize our [EditText] field [mSaved] by finding the view with ID R.id.saved
     * (we will persist the text it contains in our `SharedPreferences` file).
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState)

        // See assets/res/any/layout/save_restore_state.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.save_restore_state)

        // Set message to be appropriate for this screen.
        (findViewById<View>(R.id.msg) as TextView)
            .setText(R.string.persistent_msg)

        // Retrieve the EditText widget whose state we will save.
        mSaved = findViewById(R.id.saved)
    }

    /**
     * Upon being resumed we can retrieve the current state.  This allows us
     * to update the state if it was changed at any time while paused.
     *
     * First we call through to the super's implementation of `onResume`, then we fetch a shared
     * preferences Object with the mode MODE_PRIVATE (0) and save in our `SharedPreferences`
     * variable `val prefs`. Then we use `prefs` to retrieve the which might have been stored in
     * the SharedPreferences under the key "text" (defaulting to null) and initialize our
     * [String] variable  `val restoredText` to it. If there was a [String] stored there, we set
     * the text of our [EditText] field [mSaved] to `restoredText`, setting the type of the text
     * to [TextView.BufferType.EDITABLE] (an editable text field). Then we fetch the [Int] saved in
     * `prefs` under the key "selection-start" to initialize our [Int] variable `val selectionStart`,
     * and the [Int] saved under the key "selection-end" to initialize our [Int] variable
     * `val selectionEnd` (defaulting to -1 for both). If both of these are not -1, then we set the
     * selection of `mSaved` to them.
     */
    override fun onResume() {
        super.onResume()

        val prefs = getPreferences(0)
        val restoredText = prefs.getString("text", null)
        if (restoredText != null) {
            mSaved!!.setText(restoredText, TextView.BufferType.EDITABLE)

            val selectionStart = prefs.getInt("selection-start", -1)
            val selectionEnd = prefs.getInt("selection-end", -1)
            if (selectionStart != -1 && selectionEnd != -1) {
                mSaved!!.setSelection(selectionStart, selectionEnd)
            }
        }
    }

    /**
     * Any time we are paused we need to save away the current state, so it
     * will be restored correctly when we are resumed.
     *
     * First we call through to our super's implementation of `onPause`. Then we fetch an `Editor`
     * for a shared preferences Object with the mode MODE_PRIVATE (0) and store a reference to
     * it in our `SharedPreferences.Editor` variable `val editor`. We use `editor` to save a
     * [String] value under the key "text" in the preferences editor containing the text we fetch
     * from our [EditText] field  [mSaved]. We store the selection start of `mSaved` as an [Int]
     * under the key "selection-start" and the selection end under the key "selection-end". Finally
     * we commit our preferences changes back from `editor` to the SharedPreferences object it is
     * editing. This atomically performs the requested modifications, replacing whatever is currently
     * in the SharedPreferences.
     */
    override fun onPause() {
        super.onPause()

        getPreferences(0).edit {
            putString("text", mSaved!!.text.toString())
            putInt("selection-start", mSaved!!.selectionStart)
            putInt("selection-end", mSaved!!.selectionEnd)
        }
    }
}
