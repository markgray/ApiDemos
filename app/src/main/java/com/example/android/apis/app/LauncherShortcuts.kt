/*
 * Copyright (C) 2008 The Android Open Source Project
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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This Activity actually handles two stages of a launcher shortcut's life cycle.
 *
 *  1. Your application offers to provide shortcuts to the launcher.  When
 *  the user installs a shortcut, an activity within your application
 *  generates the actual shortcut and returns it to the launcher, where it
 *  is shown to the user as an icon.
 *
 *  2. Any time the user clicks on an installed shortcut, an intent is sent.
 *  Typically this would then be handled as necessary by an activity within
 *  your application.
 *
 * We handle stage 1 (creating a shortcut) by simply sending back the information (in the form
 * of an [android.content.Intent] that the launcher will use to create the shortcut.
 *
 * You can also implement this in an interactive way, by having your activity actually present
 * UI for the user to select the specific nature of the shortcut, such as a contact, picture, URL,
 * media item, or action.
 *
 * We handle stage 2 (responding to a shortcut) in this sample by simply displaying the contents
 * of the incoming [android.content.Intent].
 *
 * In a real application, you would probably use the shortcut intent to display specific content
 * or start a particular operation.
 */
@Suppress("DEPRECATION") // TODO: use ShortcutManager for SDK >= 25
class LauncherShortcuts : AppCompatActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. We initialize our [Intent] variable `val intent` to the intent that started this
     * activity and initialize our [String] variable `val action` to the general action to be
     * performed by `intent`. It `action` is [Intent.ACTION_CREATE_SHORTCUT] we call our method
     * [setupShortcut] to set up the shortcut [Intent] and set it as our activity result, then call
     * the [finish] method to end our activity. If `action` is not [Intent.ACTION_CREATE_SHORTCUT]
     * we set our content view to our layout file R.layout.launcher_shortcuts, then initialize our
     * [TextView] variable `val intentInfo` by finding the view with the ID R.id.txt_shortcut_intent,
     * initialize our [String] variable `var info` to the string value of `intent`, initialize our
     * [String] variable `val extra` to the extra stored in `intent` under the key EXTRA_KEY (if any)
     * and if `extra` is not *null* we append it to the end of `info`. Finally we set the text of
     * `intentInfo` to `info`.
     */
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Resolve the intent

        val intent = intent
        val action = intent.action

        // If the intent is a request to create a shortcut, we'll do that and exit

        if (Intent.ACTION_CREATE_SHORTCUT == action) {
            setupShortcut()
            finish()
            return
        }

        // If we weren't launched with a CREATE_SHORTCUT intent, simply put up an informative
        // display.

        // Inflate our UI from its XML layout description.

        setContentView(R.layout.launcher_shortcuts)

        // Provide a lightweight view of the Intent that launched us

        val intentInfo = findViewById<TextView>(R.id.txt_shortcut_intent)
        var info = intent.toString()
        val extra = intent.getStringExtra(EXTRA_KEY)
        if (extra != null) {
            info = "$info $extra"
        }
        intentInfo.text = info
    }

    /**
     * This function creates a shortcut and returns it to the caller.  There are actually two
     * intents that you will send back.
     *
     * The first intent serves as a container for the shortcut and is returned to the launcher by
     * setResult().  This intent must contain three fields:
     *  * [android.content.Intent.EXTRA_SHORTCUT_INTENT] The shortcut intent.
     *  * [android.content.Intent.EXTRA_SHORTCUT_NAME] The text that will be displayed with
     * the shortcut.
     *  * [android.content.Intent.EXTRA_SHORTCUT_ICON] The shortcut's icon, if provided as a
     * bitmap, *or* [android.content.Intent.EXTRA_SHORTCUT_ICON_RESOURCE] if provided as
     * a drawable resource.
     *
     * If you use a simple drawable resource, note that you must wrapper it using
     * [android.content.Intent.ShortcutIconResource], as shown below.  This is required so
     * that the launcher can access resources that are stored in your application's .apk file.  If
     * you return a bitmap, such as a thumbnail, you can simply put the bitmap into the extras
     * bundle using [android.content.Intent.EXTRA_SHORTCUT_ICON].
     *
     * The shortcut intent can be any intent that you wish the launcher to send, when the user
     * clicks on the shortcut.  Typically this will be [android.content.Intent.ACTION_VIEW]
     * with an appropriate Uri for your content, but any Intent will work here as long as it
     * triggers the desired action within your Activity.
     */
    private fun setupShortcut() {
        // First, set up the shortcut intent.  For this example, we simply create an intent that
        // will bring us directly back to this activity.  A more typical implementation would use a
        // data Uri in order to display a more specific result, or a custom action in order to
        // launch a specific operation.

        val shortcutIntent = Intent(Intent.ACTION_MAIN)
        shortcutIntent.setClassName(this, this.javaClass.name)
        shortcutIntent.putExtra(EXTRA_KEY, "ApiDemos Provided This Shortcut")

        // Then, set up the container intent (the response to the caller)

        val intent = Intent()
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.shortcut_name))
        val iconResource = Intent.ShortcutIconResource.fromContext(
            this, R.drawable.app_sample_code
        )
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)

        // Now, return the result to the launcher

        setResult(RESULT_OK, intent)
    }

    /**
     * Our static constant.
     */
    companion object {
        /**
         * The extra key used for the storing of an extra in the [Intent] that launches us.
         */
        private const val EXTRA_KEY = "com.example.android.apis.app.LauncherShortcuts"
    }
}
