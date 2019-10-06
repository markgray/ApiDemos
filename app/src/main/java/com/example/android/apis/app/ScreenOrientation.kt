/*
 * Copyright (C) 2010 The Android Open Source Project
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
import android.annotation.TargetApi
import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener

import com.example.android.apis.R

/**
 * Lets the user choose the screen orientation programmatically -- some orientations are only
 * available for v9+, and some only for v18+ but froyo ignores them rather than crashes.
 * Very nice example of spinner layout and use.
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class ScreenOrientation : Activity() {
    lateinit var mOrientation: Spinner // Spinner in layout used for choosing orientation

    var mCurrentOrientation: Int = -2 //

    var mNewOrientation: Int = -2

    lateinit var mCurrentTextView: TextView
    lateinit var mRequestedTextView: TextView

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.screen_orientation. We
     * locate the Spinner in the layout (R.id.orientation) and save a reference to it in our field
     * mOrientation. We create `ArrayAdapter<CharSequence>` adapter using the xml defined String array
     * R.array.screen_orientations, and using for the item layout the system defined layout file
     * android.R.layout.simple_spinner_item. Then we set the layout resource to create the drop down
     * views to R.layout.simple_spinner_dropdown_item (the list displays the items using a different
     * layout from the single item layout specified in the constructor). We set the SpinnerAdapter
     * used to provide the data which backs the Spinner mOrientation to **adapter**, and
     * finally we set the OnItemSelectedListener of **mOrientation** to an anonymous class
     * which calls setRequestedOrientation with the appropriate orientation constant for the item
     * selected in the Spinner.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not called
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_orientation)

        mCurrentTextView = findViewById(R.id.current_orientation)
        mRequestedTextView = findViewById(R.id.requested_orientation)
        val applyButton = findViewById<Button>(R.id.apply_button)
        applyButton.setOnClickListener { v -> applyNewOrientation(v) }
        mOrientation = findViewById(R.id.orientation)
        val adapter = ArrayAdapter.createFromResource(
                this, R.array.screen_orientations, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mOrientation.adapter = adapter
        mOrientation.onItemSelectedListener = object : OnItemSelectedListener {
            /**
             * Callback method to be invoked when an item in this view has been selected.
             * We simply call setRequestedOrientation to set the desired orientation of
             * the activity to the type requested in the Spinner.
             *
             * @param parent The AdapterView where the selection happened
             * @param view The view within the AdapterView that was clicked
             * @param position The position of the view in the adapter
             * @param id The row id of the item that is selected
             */
            override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                selectNewOrientation(position)
            }

            /**
             * Callback method to be invoked when the selection disappears from this view.
             * The selection can disappear for instance when touch is activated or when the
             * adapter becomes empty. We simply call setRequestedOrientation to set the
             * desired orientation of the activity to SCREEN_ORIENTATION_UNSPECIFIED.
             *
             * @param parent The AdapterView that now contains no selected item.
             */
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectNewOrientation(0) // SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun selectNewOrientation(position: Int) {
        mCurrentOrientation = requestedOrientation
        mCurrentTextView.text = "Current Orientation:\n ${orientationDesciption(mCurrentOrientation)}"
        mNewOrientation = mOrientationValues[position]
        mRequestedTextView.text = "Requested Orientation:\n ${orientationDesciption(mNewOrientation)}"
    }

    @Suppress("UNUSED_PARAMETER")
    fun applyNewOrientation(view: View) {
        requestedOrientation = mNewOrientation
    }

    fun orientationDesciption(orient: Int) : String {
        for(i in mOrientationValues.indices) {
            if(mOrientationValues[i] == orient) return mOrientationDescriptions[i]
        }
        return "Unknown orientation: $orient"
    }

    companion object {

        // Orientation spinner choices
        // This list must match the list found in samples/ApiDemos/res/values/arrays.xml
        val mOrientationValues = intArrayOf(
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_USER,
                ActivityInfo.SCREEN_ORIENTATION_BEHIND,
                ActivityInfo.SCREEN_ORIENTATION_SENSOR,
                ActivityInfo.SCREEN_ORIENTATION_NOSENSOR,
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR,
                ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_FULL_USER,
                ActivityInfo.SCREEN_ORIENTATION_LOCKED
        )
        val mOrientationDescriptions = arrayOf(
                "SCREEN_ORIENTATION_UNSPECIFIED",
                "SCREEN_ORIENTATION_LANDSCAPE",
                "SCREEN_ORIENTATION_PORTRAIT",
                "SCREEN_ORIENTATION_USER",
                "SCREEN_ORIENTATION_BEHIND",
                "SCREEN_ORIENTATION_SENSOR",
                "SCREEN_ORIENTATION_NOSENSOR",
                "SCREEN_ORIENTATION_SENSOR_LANDSCAPE",
                "SCREEN_ORIENTATION_SENSOR_PORTRAIT",
                "SCREEN_ORIENTATION_REVERSE_LANDSCAPE",
                "SCREEN_ORIENTATION_REVERSE_PORTRAIT",
                "SCREEN_ORIENTATION_FULL_SENSOR",
                "SCREEN_ORIENTATION_USER_LANDSCAPE",
                "SCREEN_ORIENTATION_USER_PORTRAIT",
                "SCREEN_ORIENTATION_FULL_USER",
                "SCREEN_ORIENTATION_LOCKED"
        )
    }
}
