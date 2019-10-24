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
 * Example of how to use a custom title [android.view.Window.FEATURE_CUSTOM_TITLE].
 * This demonstrates how a custom title can be used.
 *
 * Demo path: App/Title/Custom Title
 *
 * Source files:
 *  - src/com.example.android.apis/app/CustomTitle.java The Custom Title implementation
 *  - /res/any/layout/custom_title.xml Defines contents of the screen
 */
@Suppress("MemberVisibilityCanBePrivate")
class CustomTitle : AppCompatActivity() {

    lateinit var mActionBar: ActionBar
    var mCustomView: View? = null
    /**
     * Called when the activity is starting. First we call through to our super's implementation
     * of onCreate. Then we request the window feature FEATURE_CUSTOM_TITLE, set our content view
     * to our layout file R.layout.custom_title, retrieve the current Window for the activity and
     * use it to set the value of the Window's FEATURE_CUSTOM_TITLE to the layout file we want it
     * to use: R.layout.custom_title_1. Next we locate the input and output View's of our layout:
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
     * Finally we set the onClickListener of "leftButton" to set the text of "leftText" to the
     * contents of "leftTextEdit" when pressed, and "rightButton" to set the text of "rightText"
     * to the contents of "rightTextEdit" when pressed.
     *
     * @param savedInstanceState always null since we not override onSaveInstanceState
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.custom_title)
        mCustomView = layoutInflater.inflate(R.layout.custom_title_1,
                findViewById<View>(android.R.id.content) as ViewGroup, false)
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
        val leftText = findViewById<TextView>(R.id.left_text)
        val rightText = findViewById<TextView>(R.id.right_text)
        val leftTextEdit = findViewById<EditText>(R.id.left_text_edit)
        val rightTextEdit = findViewById<EditText>(R.id.right_text_edit)
        val leftButton = findViewById<Button>(R.id.left_text_button)
        val rightButton = findViewById<Button>(R.id.right_text_button)

        leftButton.setOnClickListener { leftText.text = leftTextEdit.text }
        rightButton.setOnClickListener { rightText.text = rightTextEdit.text }
    }

    companion object {
        const val TAG: String = "CustomTitle"
    }
}
