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

package com.example.android.apis.animation;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R;

import android.view.View;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;

/**
 * This application demonstrates how to use the animateLayoutChanges="true" attribute in XML
 * to automate transition animations as items are removed from or added to a container.
 */
public class LayoutAnimationsByDefault extends Activity {

    /**
     * Counter we use as the label of the button we add, then increment for the next time.
     */
    private int numButtons = 1;

    /**
     * Called when the activity is starting. First we call our super's implementation of {@code onCreate},
     * then we set our content view to the layout layout_animations_by_default. We initialize our variable
     * {@code GridLayout gridContainer} by finding the view with id R.id.gridContainer, and initialize our
     * variable {@code Button addButton} by finding the view with id R.id.addNewButton ("Add Button").
     * We set the {@code OnClickListener} of {@code addButton} to an anonymous class whose {@code onClick}
     * override creates a new button, sets its text to the string value of {@code numButtons} (post
     * incrementing {@code numButtons}), sets its {@code OnClickListener} to an anonymous class which
     * will remove the button from {@code gridContainer} when the button is clicked. The {@code onClick}
     * override of {@code addButton} then adds the new button to {@code gridContainer} at the location 0
     * for the first button then at position 1 for all the following buttons. The GridView attribute
     * android:animateLayoutChanges="true" causes a default LayoutTransition object to be set
     * on the ViewGroup container and default animations will run when layout changes occur (both when
     * adding and removing a button).
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_animations_by_default);

        final GridLayout gridContainer = findViewById(R.id.gridContainer);

        Button addButton = findViewById(R.id.addNewButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when the "Add Button" button is clicked. First we initialize {@code Button newButton}
             * with a new instance, set its text to the string value of {@code numButtons} (post incrementing
             * {@code numButtons}), and set its {@code OnClickListener} to an anonymous class which removes
             * the button from {@code gridContainer} when it is clicked. We then add {@code newButton} to
             * {@code gridContainer} at location 0 for the first button then at position 1 for all the
             * following buttons.
             *
             * @param v {@code View} that was clicked.
             */
            @Override
            public void onClick(View v) {
                Button newButton = new Button(LayoutAnimationsByDefault.this);
                newButton.setText(String.valueOf(numButtons++));
                newButton.setOnClickListener(new View.OnClickListener() {
                    /**
                     * Called when {@code Button newButton} is clicked. We call the {@code removeView}
                     * method of {@code GridLayout gridContainer} to remove our view from it.
                     *
                     * @param v {@code View} that was clicked.
                     */
                    @Override
                    public void onClick(View v) {
                        gridContainer.removeView(v);
                    }
                });
                gridContainer.addView(newButton, Math.min(1, gridContainer.getChildCount()));
            }
        });
    }

}