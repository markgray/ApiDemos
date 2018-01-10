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

package com.example.android.apis.view;

import com.example.android.apis.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.RadioGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.LinearLayout;

/**
 * Demonstrates RadioGroup, including adding a RadioButton to an xml defined
 * RadioGroup programmatically (the R.id.snack is created in the file values/ids.xml),
 * retrieving the R.id.* of the RadioButton selected, clearing all buttons, and the
 * value View.NO_ID (=-1) returned when no RadioButton is selected.
 */
@SuppressLint("SetTextI18n")
public class RadioGroup1 extends Activity implements RadioGroup.OnCheckedChangeListener,
        View.OnClickListener {

    /**
     * {@code TextView} that we use to display the unique identifier of the currently selected radio
     * button (along with the text: "You have selected:")
     */
    private TextView mChoice;
    /**
     * {@code RadioGroup} in our layout file with ID R.id.menu
     */
    private RadioGroup mRadioGroup;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.radio_group_1. We
     * initialize our field {@code RadioGroup mRadioGroup} by finding the view with the ID R.id.menu.
     * <p>
     * We create a new instance to initialize our variable {@code RadioButton newRadioButton}, set its
     * text to the string with the resource ID R.string.radio_group_snack ("Snack"), set its ID to
     * the value with resource ID R.id.snack (this is defined in the file values/ids.xml using the
     * resources element {@code <item type="id" name="snack"/>}). We then create a new instance to
     * initialize our variable {@code LinearLayout.LayoutParams layoutParams} specifying WRAP_CONTENT
     * for both width and height, and add the view {@code newRadioButton} to {@code mRadioGroup} using
     * it for the layout parameters. We initialize our variable {@code String selection} by fetching
     * the string with ID R.string.radio_group_selection ("You have selected:"). We set "this" as
     * the {@code OnCheckedChangeListener} of {@code mRadioGroup}, initialize our field {@code TextView mChoice}
     * by finding the view with ID R.id.choice and set its text to the string created by concatenating
     * {@code selection} with the unique id of the selected radio button {@code mRadioGroup} (the xml
     * starts with android:checkedButton pointing to the android:id="@id/lunch" ("Lunch"), the unique
     * id is an arbitrary {@code int}). Finally we initialize our variable {@code Button clearButton}
     * and set its {@code OnClickListener} to "this".
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radio_group_1);

        mRadioGroup = (RadioGroup) findViewById(R.id.menu);

        // test adding a radio button programmatically
        RadioButton newRadioButton = new RadioButton(this);
        newRadioButton.setText(R.string.radio_group_snack);
        newRadioButton.setId(R.id.snack);
        LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT);
        mRadioGroup.addView(newRadioButton, 0, layoutParams);

        // test listening to checked change events
        String selection = getString(R.string.radio_group_selection);
        mRadioGroup.setOnCheckedChangeListener(this);
        mChoice = (TextView) findViewById(R.id.choice);
        mChoice.setText(selection + mRadioGroup.getCheckedRadioButtonId());

        // test clearing the selection
        Button clearButton = (Button) findViewById(R.id.clear);
        clearButton.setOnClickListener(this);
    }

    /**
     * Called when the checked radio button has changed. When the selection is cleared, checkedId is -1.
     * First we initialize our variable {@code String selection} by retrieving the string with the
     * resource ID R.string.radio_group_selection ("You have selected:"), then we initialize our
     * variable {@code String none} by retrieving the string with the resource ID R.string.radio_group_none
     * ("(none)"). Finally we set the text of {@code TextView mChoice} to the string formed by concatenating
     * {@code selection} with {@code none} if our parameter {@code checkedId} is equal to View.NO_ID (-1)
     * or to the string value of {@code checkedId} otherwise.
     *
     * @param group     the group in which the checked radio button has changed
     * @param checkedId the unique identifier of the newly checked radio button
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        String selection = getString(R.string.radio_group_selection);
        String none = getString(R.string.radio_group_none);
        mChoice.setText(selection +
                (checkedId == View.NO_ID ? none : checkedId));
    }

    /**
     * Called when the button with ID R.id.clear ("Clear") is clicked. We just call the {@code clearCheck}
     * method of {@code RadioGroup mRadioGroup} to clear the selection. When the selection is cleared, no
     * radio button in this group is selected and {@code getCheckedRadioButtonId()} returns null.
     *
     * @param v View that was clicked
     */
    @Override
    public void onClick(View v) {
        mRadioGroup.clearCheck();
    }
}
