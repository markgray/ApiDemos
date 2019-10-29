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
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Lets the user choose the screen orientation programmatically -- some orientations are only
 * available for v9+, and some only for v18+ but froyo ignores them rather than crashes.
 * Very nice example of spinner layout and use.
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class ScreenOrientation : AppCompatActivity() {
    /**
     * [Spinner] in layout used for choosing orientation
     */
    lateinit var mOrientation: Spinner

    /**
     * The current orientation.
     */
    var mCurrentOrientation: Int = -2

    /**
     * Orientation chosen by the [mOrientation] orientation [Spinner]
     */
    var mNewOrientation: Int = -2

    /**
     * [TextView] displaying information about the current orientation.
     */
    lateinit var mCurrentTextView: TextView
    /**
     * [TextView] displaying information about the requested orientation.
     */
    lateinit var mRequestedTextView: TextView

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.screen_orientation.
     * We initialize our [TextView] field [mCurrentTextView] by finding the view in our layout file
     * with id R.id.current_orientation, and our [TextView] field [mRequestedTextView] by finding
     * the view in our layout file with id R.id.requested_orientation. We initialize our [Button]
     * variable `val applyButton` by finding the view with id R.id.apply_button, and set its
     * `OnClickListener` to a lambda which calls our method [applyNewOrientation] to apply the
     * orientation requested by the [Spinner] item that was last selected. We locate the [Spinner]
     * in the layout (R.id.orientation) and save a reference to it in our field [mOrientation]. We
     * create a `ArrayAdapter<CharSequence>` variable `val adapter` using the xml defined String array
     * R.array.screen_orientations, and using for the item layout the system defined layout file
     * android.R.layout.simple_spinner_item. Then we set the layout resource to create the drop down
     * views to R.layout.simple_spinner_dropdown_item (the list displays the items using a different
     * layout from the single item layout specified in the constructor). We set the SpinnerAdapter
     * used to provide the data which backs the Spinner mOrientation to **adapter**, and finally we
     * set the `OnItemSelectedListener` of [mOrientation] to an anonymous class which calls our method
     * [selectNewOrientation] with the position of the item that was selected in the Spinner.
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
             * We simply call our method [selectNewOrientation] to set the desired
             * orientation of the activity to the type requested in the Spinner.
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
             * adapter becomes empty. We simply call our method [selectNewOrientation] to set
             * the desired orientation of the activity to SCREEN_ORIENTATION_UNSPECIFIED.
             *
             * @param parent The AdapterView that now contains no selected item.
             */
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectNewOrientation(0) // SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    /**
     * Called when a new orientation has been selected by the [Spinner]. We set our field
     * [mCurrentOrientation] to the devices current orientation setting, and then set the
     * text of our [TextView] field [mCurrentTextView] to the description of that setting
     * that is returned by our [orientationDescription] method. We then set our field
     * [mNewOrientation] to the orientation constant for the item in position [position],
     * and set the text of our [TextView] field [mRequestedTextView] to the description of
     * that setting that is returned by our [orientationDescription] method.
     *
     * @param position Position of the item that was selected in the [Spinner]
     */
    @SuppressLint("SetTextI18n")
    fun selectNewOrientation(position: Int) {
        mCurrentOrientation = requestedOrientation
        mCurrentTextView.text = orientationDescription(mCurrentOrientation)
        mNewOrientation = mOrientationValues[position]
        mRequestedTextView.text = orientationDescription(mNewOrientation)
    }

    /**
     * Called to apply the orientation requested by the [Spinner]. We set the text of our [TextView]
     * field [mCurrentTextView] to the description of the setting chosen in our field [mNewOrientation]
     * that is returned by our [orientationDescription] method, then change the orientation of this
     * activity to that orientation.
     *
     * @param view Unused (I originally intended this to be an `OnClickListener`)
     */
    @Suppress("UNUSED_PARAMETER")
    fun applyNewOrientation(view: View) {
        mCurrentTextView.text = orientationDescription(mNewOrientation)
        requestedOrientation = mNewOrientation
    }

    /**
     * Returns a description of the orientation constant passed us. We search through all of the
     * orientation constants in our [mOrientationValues] array until we find our parameter [orient],
     * at which point we return the [String] in our [mOrientationDescriptions] array which has the
     * same index. If we do not find it we return the string "Unknown orientation".
     *
     * @param orient the orientation constant
     * @return the [String] in our [mOrientationDescriptions] field describing [orient].
     */
    fun orientationDescription(orient: Int): String {
        for (i in mOrientationValues.indices) {
            if (mOrientationValues[i] == orient) return mOrientationDescriptions[i]
        }
        return "Unknown orientation: $orient"
    }

    companion object {

        /**
         * Orientation spinner choices This list must match the string array found in
         * ApiDemos/res/values/arrays.xml with the ID R.array.screen_orientations
         */
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
        /**
         * Descriptions of the orientation constants in [mOrientationValues].
         */
        val mOrientationDescriptions = arrayOf(
                "SCREEN_ORIENTATION_UNSPECIFIED\nNo preference specified: let the system decide the best orientation. This will either be the orientation selected by the activity below, or the user's preferred orientation if this activity is the bottom of a task. If the user explicitly turned off sensor based orientation through settings sensor based device rotation will be ignored. If not by default sensor based orientation will be taken into account and the orientation will changed based on how the user rotates the device",
                "SCREEN_ORIENTATION_LANDSCAPE\nWould like to have the screen in a landscape orientation: that is, with the display wider than it is tall, ignoring sensor data",
                "SCREEN_ORIENTATION_PORTRAIT\nWould like to have the screen in a portrait orientation: that is, with the display taller than it is wide, ignoring sensor data",
                "SCREEN_ORIENTATION_USER\nUse the user's current preferred orientation of the handset",
                "SCREEN_ORIENTATION_BEHIND\nKeep the screen in the same orientation as whatever is behind this activity",
                "SCREEN_ORIENTATION_SENSOR\nOrientation is determined by a physical orientation sensor: the display will rotate based on how the user moves the device. Ignores user's setting to turn off sensor-based rotation",
                "SCREEN_ORIENTATION_NOSENSOR\nAlways ignore orientation determined by orientation sensor: the display will not rotate when the user moves the device.",
                "SCREEN_ORIENTATION_SENSOR_LANDSCAPE\nWould like to have the screen in landscape orientation, but can use the sensor to change which direction the screen is facing",
                "SCREEN_ORIENTATION_SENSOR_PORTRAIT\nWould like to have the screen in portrait orientation, but can use the sensor to change which direction the screen is facing",
                "SCREEN_ORIENTATION_REVERSE_LANDSCAPE\nWould like to have the screen in landscape orientation, turned in the opposite direction from normal landscape",
                "SCREEN_ORIENTATION_REVERSE_PORTRAIT\nWould like to have the screen in portrait orientation, turned in the opposite direction from normal portrait.",
                "SCREEN_ORIENTATION_FULL_SENSOR\nOrientation is determined by a physical orientation sensor: the display will rotate based on how the user moves the device. This allows any of the 4 possible rotations, regardless of what the device will normally do (for example some devices won't normally use 180 degree rotation)",
                "SCREEN_ORIENTATION_USER_LANDSCAPE\nWould like to have the screen in landscape orientation, but if the user has enabled sensor-based rotation then we can use the sensor to change which direction the screen is facing",
                "SCREEN_ORIENTATION_USER_PORTRAIT\nWould like to have the screen in portrait orientation, but if the user has enabled sensor-based rotation then we can use the sensor to change which direction the screen is facing",
                "SCREEN_ORIENTATION_FULL_USER\nRespect the user's sensor-based rotation preference, but if sensor-based rotation is enabled then allow the screen to rotate in all 4 possible directions regardless of what the device will normally do (for example some devices won't normally use 180 degree rotation)",
                "SCREEN_ORIENTATION_LOCKED\nScreen is locked to its current rotation, whatever that is"
        )
    }
}
