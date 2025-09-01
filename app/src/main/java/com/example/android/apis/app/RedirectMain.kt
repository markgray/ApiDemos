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

import android.content.Intent
import android.os.Bundle
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

import androidx.appcompat.app.AppCompatActivity

import com.example.android.apis.R
import androidx.core.content.edit

/**
 * Launched by a push of a Button in the Activity RedirectEnter we will immediately start
 * RedirectGetter if there is no data stored in "RedirectData" yet, if there is data we
 * will display it and give the user the options to either "Clear and Exit" back to
 * RedirectEnter, or "New Text" which restarts RedirectGetter. RedirectMain uses the
 * request codes INIT_TEXT_REQUEST, and NEW_TEXT_REQUEST that it sends in the Intent to
 * RedirectGetter to determine what to do if the result code was RESULT_CANCELED,
 * either finish() back to RedirectEnter, or just display the old text.
 */
class RedirectMain : AppCompatActivity() {

    private var mTextPref: String? =
        null // String stored in shared preferences file by RedirectGetter

    /**
     * Called when the "Clear and exit" Button is clicked. First we open the shared preference
     * file "RedirectData" into SharedPreferences preferences, then we create an editor for
     * **preferences**, use it to mark the key "text" for removal, and  commit the
     * change back from the editor to the preferences file. Finally we finish() this Activity
     * and return to RedirectEnter.
     *
     * Parameter: View of the Button that was clicked
     */
    private val mClearListener = OnClickListener {
        // Erase the preferences and exit!
        val preferences = getSharedPreferences("RedirectData", 0)
        preferences.edit { remove("text") }
        finish()
    }

    /**
     * OnClickListener for the Button "New text" (R.id.newView). We create an Intent to start the
     * Activity [RedirectGetter], then call the [ActivityResultLauncher.launch] method of our field
     * [newTextGetterLauncher] to launch that activity.
     *
     * Parameter: View of the Button that was clicked
     */
    private val mNewListener = OnClickListener {
        // Retrieve new text preferences.
        val intent = Intent(this@RedirectMain, RedirectGetter::class.java)
        newTextGetterLauncher.launch(intent)
    }

    /**
     * This is the [ActivityResultLauncher] that is used to launch [RedirectGetter] when the user
     * clicks the "New text" [Button]. In the lambda of the [ActivityResultLauncher] constructor the
     * [ActivityResult.getResultCode] (kotlin `resultCode` property) value of the [ActivityResult]
     * that [RedirectGetter] returns is just checked to see if the user canceled the change in which
     * case we just ignore it, otherwise we call our [loadPrefs] method to reload the text that was
     * saved by [RedirectGetter] in our shared preferences file.
     */
    private val newTextGetterLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            // In this case we are just changing the text, so if it was
            // cancelled then we can leave things as-is.
            if (result.resultCode != RESULT_CANCELED) {
                loadPrefs()
            }

        }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.redirect_main. Next we
     * locate the "Clear and exit" Button (R.id.clear) and set its OnClickListener to OnClickListener
     * mClearListener. We then locate the "New text" Button (R.id.newView) and set its OnClickListener
     * to OnClickListener mNewListener. We call our method loadPrefs() to read the "text" entry from
     * our shared preference file, and if was there it returns true and we are done. If it returns
     * false there was nothing stored under "text" in the shared preference file so we create an
     * Intent intent to launch [RedirectGetter] and call the [ActivityResultLauncher.launch] method
     * of our field [initTextRequestLauncher] using that Intent with to launch the activity.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not called
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.redirect_main)

        // Watch for button clicks.
        val clearButton = findViewById<Button>(R.id.clear)
        clearButton.setOnClickListener(mClearListener)
        val newButton = findViewById<Button>(R.id.newView)
        newButton.setOnClickListener(mNewListener)

        // Retrieve the current text preference.  If there is no text
        // preference set, we need to get it from the user by invoking the
        // activity that retrieves it.  To do this cleanly, we will
        // temporarily hide our own activity so it is not displayed until the
        // result is returned.
        if (!loadPrefs()) {
            val intent = Intent(this, RedirectGetter::class.java)
            initTextRequestLauncher.launch(intent)
        }
    }

    /**
     * This is the [ActivityResultLauncher] that is used to launch [RedirectGetter] in our [onCreate]
     * override when our [loadPrefs] method returns `false` indicating that ous shared preferences
     * file has no text stored. In the lambda of the [ActivityResultLauncher] constructor the
     * [ActivityResult.getResultCode] (kotlin `resultCode` property) value of the [ActivityResult]
     * that [RedirectGetter] returns is checked to see if the user canceled the activity in which
     * case we are cancelled as well so we call [finish] to exit, otherwise we call our [loadPrefs]
     * method to reload the text that was saved by [RedirectGetter] in our shared preferences file.
     */
    private val initTextRequestLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->

            // If the request was cancelled, then we are cancelled as well.
            if (result.resultCode == RESULT_CANCELED) {
                finish()

                // Otherwise, there now should be text...  reload the prefs,
                // and show our UI.  (Optionally we could verify that the text
                // is now set and exit if it isn't.)
            } else {
                loadPrefs()
            }
        }

    /**
     * Reads the shared preference file "RedirectData" looking for a String stored under the key
     * "text", and if it was there it updates the TextView text (R.id.text) in our layout to
     * display what it found and returns true to the caller. If no "text" was found in the shared
     * preference file it returns false.
     *
     * @return `true` if there was text stored under "text" in the shared preference file, false otherwise
     */
    private fun loadPrefs(): Boolean {
        // Retrieve the current redirect values.
        // NOTE: because this preference is shared between multiple
        // activities, you must be careful about when you read or write
        // it in order to keep from stepping on yourself.
        val preferences = getSharedPreferences("RedirectData", 0)

        mTextPref = preferences.getString("text", null)
        if (mTextPref != null) {
            val text = findViewById<TextView>(R.id.text)
            text.text = mTextPref
            return true
        }

        return false
    }

}
