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

package com.example.android.apis.app;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import com.example.android.apis.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Simple example of using persistent preferences to retain a screen's state.
 * <p>This can be used as an alternative to the normal
 * <code>onSaveInstanceState()</code> mechanism, if you
 * wish the state to persist even after an activity is finished.</p>
 *
 * <p>Note that using this approach requires more care, since you are sharing
 * the persistent state potentially across multiple instances of the activity.
 * In particular, if you allow a new instance of the activity to be launched
 * directly on top of the existing instance, the state can get out of sync
 * because the new instance is resumed before the old one is paused.</p>
 *
 * <p>For any persistent state that is not simplistic, a content
 * provider is often a better choice.</p>
 *
 * <p>In this example we are currently saving and restoring the state of the
 * top text editor, but not of the bottom text editor.  You can see the difference
 * by editing the two text fields, then going back from the activity and
 * starting it again.</p>
 *
 * <h4>Demo</h4>
 * App/Activity/Save &amp; Restore State
 *
 * <h4>Source files</h4>
 * <table class="LinkTable">
 *         <tr>
 *             <td class="LinkColumn">src/com.example.android.apis/app/PersistentState.java</td>
 *             <td class="DescrColumn">The Save/Restore Screen implementation</td>
 *         </tr>
 *         <tr>
 *             <td class="LinkColumn">/res/any/layout/save_restore_state.xml</td>
 *             <td class="DescrColumn">Defines contents of the screen</td>
 *         </tr>
 * </table>
 *
 */
public class PersistentState extends Activity {
    /**
     * Initialization of the Activity after it is first created.  Here we use
     * {@link android.app.Activity#setContentView setContentView()} to set up
     * the Activity's content to our layout file R.layout.save_restore_state,
     * set the TextView R.id.msg to the string R.string.persistent_msg, and
     * retrieve the EditText widget (R.id.saved) whose state we will persistent
     * and save a reference to it in our field EditText mSaved.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

        // See assets/res/any/layout/save_restore_state.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.save_restore_state);

        // Set message to be appropriate for this screen.
        ((TextView)findViewById(R.id.msg)).setText(R.string.persistent_msg);

        // Retrieve the EditText widget whose state we will save.
        mSaved = (EditText)findViewById(R.id.saved);
    }

    /**
     * Upon being resumed we can retrieve the current state.  This allows us
     * to update the state if it was changed at any time while paused.
     *
     * First we call through to the super's implementation of onResume, then we fetch a shared
     * preferences Object with the mode MODE_PRIVATE (0) and save in in SharedPreferences prefs.
     * Then we use "prefs" to retrieve the String restoredText which might have been stored in
     * the SharedPreferences under the key "text" (defaulting to null). If there was a String
     * stored there, we set the text of the EditText mSaved to restoredText setting its type
     * to TextView.BufferType.EDITABLE (an editable text field). Then we fetch the int saved in
     * "prefs" under the key "selection-start" to int selectionStart, and the int saved under
     * the key "selection-end" to int selectionEnd (defaulting to -1 for both). If both of these
     * are not -1, then we set the selection of "mSaved" to them.
     */
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getPreferences(0); 
        String restoredText = prefs.getString("text", null);
        if (restoredText != null) {
            mSaved.setText(restoredText, TextView.BufferType.EDITABLE);

            int selectionStart = prefs.getInt("selection-start", -1);
            int selectionEnd = prefs.getInt("selection-end", -1);
            if (selectionStart != -1 && selectionEnd != -1) {
                mSaved.setSelection(selectionStart, selectionEnd);
            }
        }
    }

    /**
     * Any time we are paused we need to save away the current state, so it
     * will be restored correctly when we are resumed.
     */
    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = getPreferences(0).edit();
        editor.putString("text", mSaved.getText().toString());
        editor.putInt("selection-start", mSaved.getSelectionStart());
        editor.putInt("selection-end", mSaved.getSelectionEnd());
        editor.commit();
    }

    private EditText mSaved;
}
