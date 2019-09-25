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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.app.Activity
import android.os.Bundle
import android.view.View.OnClickListener
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.android.apis.R

/**
 * Dialog Activity
 *
 * This demonstrates the how to write an activity that looks like
 * a pop-up dialog.
 */
class DialogActivity : Activity() {

    /**
     * Called when the Button R.id.add ("ADD CONTENT") is clicked. First we locate the
     * LinearLayout layout (R.id.inner_content) in which we will add our content to, then
     * create ImageView iv and set drawable R.drawable.icon48x48_1 as the content of this
     * ImageView, set the ImageView's padding to 4 pixels on each side, and finally add this
     * ImageView as a child to LinearLayout layout.
     *
     * Parameter: `View` of [Button] which was clicked: (R.id.add "ADD CONTENT")
     */
    private val mAddContentListener = OnClickListener {
        val layout = findViewById<LinearLayout>(R.id.inner_content)
        val iv = ImageView(this@DialogActivity)

        @Suppress("DEPRECATION")
        iv.setImageDrawable(resources.getDrawable(R.drawable.icon48x48_1))
        iv.setPadding(4, 4, 4, 4)
        layout.addView(iv)
    }

    /**
     * Called when the Button R.id.remove ("REMOVE CONTENT") is clicked. First we locate the
     * LinearLayout layout (R.id.inner_content) which holds the icons which were added to our
     * content, determine the number of children in the group, and if there are more than 0 we
     * remove the last icon added (at getChildCount()-1 since the group position is zero based.
     *
     * Parameter: `View` of [Button] which was clicked: R.id.remove ("REMOVE CONTENT")
     */
    private val mRemoveContentListener = OnClickListener {
        val layout = findViewById<LinearLayout>(R.id.inner_content)
        val num = layout.childCount
        if (num > 0) {
            layout.removeViewAt(num - 1)
        }
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation
     * of onCreate. Then we request the window feature FEATURE_LEFT_ICON (the flag for having an
     * icon on the left side of the title bar). Then we set our content view to our layout file
     * R.layout.dialog_activity. Our entry in AndroidManifest set the theme to be used for our
     * layout using the attribute android:theme="@style/ThemeCurrentDialog", which is defined
     * using a style style derived from a theme that is appropriate and available given the Api
     * level: style/ThemeCurrentDialog is defined in each:
     *
     *  - values/styles.xml uses android:Theme.Dialog
     *  - values-v11/styles.xml uses android:Theme.Holo.Dialog
     *  - values-v19/styles.xml uses android:Theme.Material.Dialog
     *  - values-v20/styles.xml uses android:Theme.Material.Light.Dialog
     *
     * Then we try to set the title to "This is just a test" but nothing happens?
     *
     * Next we set the value for a drawable feature of this window (FEATURE_LEFT_ICON), from a
     * resource identifier (android.R.drawable.ic_dialog_alert). We find the Button R.id.add
     * ("ADD CONTENT") and set its OnClickListener to OnClickListener mAddContentListener (which
     * adds an icon to the LinearLayout R.id.inner_content) and find the Button R.id.remove
     * ("REMOVE CONTENT") and set its OnClickListener to OnClickListener mRemoveContentListener
     * (which removes the last icon added to the LinearLayout R.id.inner_content is there are any
     * remaining.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_LEFT_ICON)

        // See assets/res/any/layout/dialog_activity.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.dialog_activity)
        window.setTitle("This is just a test")

        window.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                android.R.drawable.ic_dialog_alert)

        var button = findViewById<Button>(R.id.add)
        button.setOnClickListener(mAddContentListener)
        button = findViewById(R.id.remove)
        button.setOnClickListener(mRemoveContentListener)
    }
}
