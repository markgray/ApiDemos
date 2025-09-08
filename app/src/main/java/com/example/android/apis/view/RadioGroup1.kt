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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates [RadioGroup], including adding a [RadioButton] to an xml defined
 * [RadioGroup] programmatically (the R.id.snack is created in the file values/ids.xml),
 * retrieving the R.id.* of the [RadioButton] selected, clearing all buttons, and the
 * value View.NO_ID (=-1) returned when no [RadioButton] is selected.
 */
@SuppressLint("SetTextI18n")
class RadioGroup1 : AppCompatActivity(), RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    /**
     * [TextView] that we use to display the unique identifier of the currently selected radio
     * button (along with the text: "You have selected:")
     */
    private var mChoice: TextView? = null

    /**
     * [RadioGroup] in our layout file with ID R.id.menu
     */
    private var mRadioGroup: RadioGroup? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.radio_group_1. We
     * initialize our [RadioGroup] field [mRadioGroup] by finding the view with the ID R.id.menu.
     *
     * We create a new instance to initialize our [RadioButton] variable `val newRadioButton`, set
     * its text to the string with the resource ID R.string.radio_group_snack ("Snack"), set its ID
     * to the value with resource ID R.id.snack (this is defined in the file values/ids.xml using
     * the resources element `<item type="id" name="snack"/>`). We then create a new instance to
     * initialize our [LinearLayout.LayoutParams] variable `val layoutParams` specifying WRAP_CONTENT
     * for both width and height, and add the view `newRadioButton` to [mRadioGroup] using it for the
     * layout parameters. We initialize our [String] variable `val selection` by fetching the string
     * with ID R.string.radio_group_selection ("You have selected:"). We set "this" as the
     * `OnCheckedChangeListener` of [mRadioGroup], initialize our [TextView] field [mChoice] by
     * finding the view with ID R.id.choice and set its text to the string created by concatenating
     * `selection` with the unique id of the selected radio button of [mRadioGroup] (the xml starts
     * with android:checkedButton pointing to the android:id="@id/lunch" ("Lunch"), the unique id is
     * an arbitrary [Int]). Finally we initialize our [Button] variable `val clearButton` and set its
     * `OnClickListener` to "this".
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.radio_group_1)
        mRadioGroup = findViewById(R.id.menu)

        // test adding a radio button programmatically
        val newRadioButton = RadioButton(this)
        newRadioButton.setText(R.string.radio_group_snack)
        newRadioButton.id = R.id.snack
        val layoutParams: LinearLayout.LayoutParams = RadioGroup.LayoutParams(
            /* w = */ RadioGroup.LayoutParams.WRAP_CONTENT,
            /* h = */ RadioGroup.LayoutParams.WRAP_CONTENT
        )
        mRadioGroup!!.addView(
            /* child = */ newRadioButton,
            /* index = */ 0,
            /* params = */ layoutParams
        )

        // test listening to checked change events
        val selection: String = getString(R.string.radio_group_selection)
        mRadioGroup!!.setOnCheckedChangeListener(this)
        mChoice = findViewById(R.id.choice)
        mChoice!!.text = selection + mRadioGroup!!.checkedRadioButtonId

        // test clearing the selection
        val clearButton = findViewById<Button>(R.id.clear)
        clearButton.setOnClickListener(this)
    }

    /**
     * Called when the checked radio button has changed. When the selection is cleared, [checkedId]
     * is -1. First we initialize our [String] variable `val selection` by retrieving the string with
     * the resource ID R.string.radio_group_selection ("You have selected:"), then we initialize our
     * [String] variable `val none` by retrieving the string with the resource ID
     * R.string.radio_group_none ("(none)"). Finally we set the text of [TextView] field [mChoice]
     * to the string formed by concatenating `selection` with `none` if our parameter `checkedId`
     * is equal to View.NO_ID (-1) or to the string value of `checkedId` otherwise.
     *
     * @param group     the group in which the checked radio button has changed
     * @param checkedId the unique identifier of the newly checked radio button
     */
    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        val selection: String = getString(R.string.radio_group_selection)
        val none: String = getString(R.string.radio_group_none)
        mChoice!!.text = selection +
            if (checkedId == View.NO_ID) none else checkedId
    }

    /**
     * Called when the button with ID R.id.clear ("Clear") is clicked. We just call the `clearCheck`
     * method of [RadioGroup] field [mRadioGroup] to clear the selection. When the selection is
     * cleared, no radio button in this group is selected and `getCheckedRadioButtonId()` returns
     * null.
     *
     * @param v [View] that was clicked
     */
    override fun onClick(v: View) {
        mRadioGroup!!.clearCheck()
    }
}