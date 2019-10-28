/*
 * Copyright (C) 2009 The Android Open Source Project
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

import androidx.appcompat.app.AppCompatActivity

import com.example.android.apis.R

/**
 * ReorderOnLaunch is the first of a sequence of four Activities: ReorderTwo, ReorderThree, and
 * ReorderFour follow.  A button on the fourth will use the Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
 * flag to bring the second of the activities to the front of the history stack. After that,
 * proceeding back through the history should begin with the newly front most second reorder
 * activity, then the fourth, the third, and finally the first.
 */
class ReorderTwo : AppCompatActivity() {

    /**
     * Called when the "Go to the third" Button (R.id.reorder_launch_three) is clicked.
     * We create an Intent to launch the ReorderThree Activity and start that Activity.
     *
     * Parameter: View of the Button that was clicked
     */
    private val mClickListener = OnClickListener {
        startActivity(Intent(this@ReorderTwo, ReorderThree::class.java))
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.reorder_two. Then we
     * locate the "Go to the third" Button (R.id.reorder_launch_three) and set its OnClickListener
     * to OnClickListener mClickListener.
     *
     * @param savedState always null since onSaveInstanceState is not overridden
     */
    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        setContentView(R.layout.reorder_two)

        val twoButton = findViewById<Button>(R.id.reorder_launch_three)
        twoButton.setOnClickListener(mClickListener)
    }
}