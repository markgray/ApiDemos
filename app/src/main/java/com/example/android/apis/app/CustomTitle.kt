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

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R


/**
 * ### CustomTitle
 *
 * Used to be an example of how to use a custom title [android.view.Window.FEATURE_CUSTOM_TITLE].
 * It used to demonstrate how a custom title could be used, but when migrating to [AppCompatActivity]
 * I found out that new versions will always throw a runtime exception: "You cannot combine custom
 * titles with other title features", and there is no way to turn back the clock to the days when
 * title bars were used instead of [ActionBar]'s (apart from targeting Api 7.0 instead of the latest).
 * What I did instead is embed a CustomView in the Activity's [ActionBar]. Looks and acts the same,
 * but allows you to use all the neat features of [AppCompatActivity].
 *
 * Demo path: App/Title/Custom Title
 *
 * Source files:
 *  - src/com.example.android.apis/app/CustomTitle.java The Custom Title implementation
 *  - /res/any/layout/custom_title.xml Defines contents of the screen
 *  - /res/any/layout/custom_title_1.xml The layout file for the custom title we display.
 */
@Suppress("MemberVisibilityCanBePrivate")
class CustomTitle : AppCompatActivity() {

    /**
     * A handle to our Activity's [ActionBar].
     */
    lateinit var mActionBar: ActionBar

    /**
     * The custom title [View] that we insert as a CustomView in our [ActionBar] field [mActionBar]
     */
    var mCustomView: View? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation
     * of onCreate. Then we set our content view to our layout file R.layout.custom_title, and use
     * a `LayoutInflater` for our context to inflate the custom title layout with resource ID
     * R.layout.custom_title_1 into a [View] using the root [View] android.R.id.content for the
     * layout params without attaching to it, which we then use to initialize our [View] field
     * [mCustomView]. We initialize our [ActionBar] field [mActionBar] with a reference to this
     * activity's [ActionBar] retrieved by the support library version of `Activity.getActionBar`,
     * and disable the showing of the application home affordance in the action bar with a call to
     * the `setDisplayShowHomeEnabled` of [mActionBar], disable the showing of a logo, disable the
     * showing of a title, and enable the showing of a custom view. We then set the action bar into
     * custom navigation mode, supplying [mCustomView] as the [View] to use for custom navigation,
     * with its layout parameters both set to WRAP_CONTENT. Next we locate the input and output
     * View's of our layout:
     *
     *  - TextView leftText (R.id.left_text) the left TextView of our custom title
     *  - TextView rightText (R.id.right_text) the right TextView of our custom title
     *  - EditText leftTextEdit (R.id.left_text_edit) the EditText for entering a new left TextView
     * for our custom title
     *  - EditText rightTextEdit (R.id.right_text_edit) the EditText for entering a new right TextView
     * for our custom title
     *  - Button leftButton (R.id.left_text_button) the Button used to set "leftText" to the contents
     * of "leftTextEdit"
     *  - Button rightButton (R.id.right_text_button) the Button used to set "rightText" to the contents
     * of "rightTextEdit"
     *
     * Finally we set the `onClickListener` of "leftButton" to set the text of "leftText" to the
     * contents of "leftTextEdit" when pressed, and "rightButton" to set the text of "rightText"
     * to the contents of "rightTextEdit" when pressed.
     *
     * @param savedInstanceState always null since we not override onSaveInstanceState
     */
    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_title)

        mCustomView = layoutInflater.inflate(
            R.layout.custom_title_1,
            findViewById<View>(android.R.id.content) as ViewGroup, false
        )
        mActionBar = supportActionBar!!
        mActionBar.setDisplayShowHomeEnabled(false)
        mActionBar.setDisplayUseLogoEnabled(false)
        mActionBar.setDisplayShowTitleEnabled(false)
        mActionBar.setDisplayShowCustomEnabled(true)

        mActionBar.setCustomView(
            mCustomView,
            ActionBar.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        Log.i(TAG, "Before first call to findViewById")
        val leftText = mCustomView!!.findViewById<TextView>(R.id.left_text)
        val rightText = mCustomView!!.findViewById<TextView>(R.id.right_text)
        val leftTextEdit = findViewById<EditText>(R.id.left_text_edit)
        val rightTextEdit = findViewById<EditText>(R.id.right_text_edit)
        val leftButton = findViewById<Button>(R.id.left_text_button)
        val rightButton = findViewById<Button>(R.id.right_text_button)

        leftButton.setOnClickListener { leftText.text = leftTextEdit.text }
        rightButton.setOnClickListener { rightText.text = rightTextEdit.text }
    }

    /**
     * Our static constant
     */
    companion object {
        /**
         * TAG to use for logging.
         */
        const val TAG: String = "CustomTitle"
    }
}
