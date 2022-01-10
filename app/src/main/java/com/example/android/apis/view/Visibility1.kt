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
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates making a view VISIBLE, INVISIBLE and GONE. Three buttons control
 * the visibility of a TextView which is sandwiched between two other TextView's
 */
class Visibility1 : AppCompatActivity() {
    /**
     * [View] whose visibility is changed by the buttons between VISIBLE INVISIBLE or GONE
     */
    private var mVictim: View? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.visibility_1. We
     * initialize our [View] field [mVictim] by finding the view with the id R.id.victim (a
     * `TextView` with the text "View B" that lies between two other `TextView`). We initialize our
     * [Button] variables `val visibleButton` by finding the view with id R.id.vis ("Vis"),
     * `val invisibleButton` by finding the view with id R.id.invis ("Invis"), and `val goneButton`
     * by finding the view with id R.id.gone ("Gone"). We set the `OnClickListener` of `visibleButton`
     * to the `OnClickListener` field [mVisibleListener] (a lambda which sets the visibility of
     * [View] field [mVictim] to VISIBLE), the `OnClickListener` of `invisibleButton` to `OnClickListener`
     * field [mInvisibleListener] (a lambda which sets the visibility of [View] field [mVictim] to
     * INVISIBLE), and the `OnClickListener` of `goneButton` to `OnClickListener` field
     * [mGoneListener] (an a lambda which sets the visibility of [View] field [mVictim] to GONE).
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.visibility_1)

        // Find the view whose visibility will change
        mVictim = findViewById(R.id.victim)

        // Find our buttons
        val visibleButton = findViewById<Button>(R.id.vis)
        val invisibleButton = findViewById<Button>(R.id.invis)
        val goneButton = findViewById<Button>(R.id.gone)

        // Wire each button to a click listener
        visibleButton.setOnClickListener(mVisibleListener)
        invisibleButton.setOnClickListener(mInvisibleListener)
        goneButton.setOnClickListener(mGoneListener)
    }

    /**
     * Called when the [Button] with id R.id.vis ("Vis") is clicked, we just set the
     * visibility of [View] field [mVictim] to VISIBLE.
     */
    private var mVisibleListener: View.OnClickListener = View.OnClickListener {
        mVictim!!.visibility = View.VISIBLE
    }

    /**
     * Called when the [Button] with id R.id.invis ("Invis") is clicked, we just set the
     * visibility of [View] field [mVictim] to INVISIBLE.
     */
    private var mInvisibleListener: View.OnClickListener = View.OnClickListener {
        mVictim!!.visibility = View.INVISIBLE
    }

    /**
     * Called when the [Button] with id R.id.gone ("Gone") is clicked, we just set the
     * visibility of [View] field [mVictim] to GONE.
     */
    private var mGoneListener: View.OnClickListener = View.OnClickListener {
        mVictim!!.visibility = View.GONE
    }
}