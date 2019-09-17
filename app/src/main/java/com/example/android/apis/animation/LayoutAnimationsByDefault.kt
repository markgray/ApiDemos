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

package com.example.android.apis.animation

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import com.example.android.apis.R
import kotlin.math.min

/**
 * This application demonstrates how to use the animateLayoutChanges="true" attribute in XML
 * to automate transition animations as items are removed from or added to a container.
 */
class LayoutAnimationsByDefault : Activity() {

    /**
     * Counter we use as the label of the button we add, then increment for the next time.
     */
    private var numButtons = 1

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to the layout layout_animations_by_default. We initialize our variable
     * `GridLayout gridContainer` by finding the view with id R.id.gridContainer, and initialize our
     * variable `Button addButton` by finding the view with id R.id.addNewButton ("Add Button").
     * We set the `OnClickListener` of `addButton` to an anonymous class whose `onClick`
     * override creates a new button, sets its text to the string value of `numButtons` (post
     * incrementing `numButtons`), sets its `OnClickListener` to an anonymous class which
     * will remove the button from `gridContainer` when the button is clicked. The `onClick`
     * override of `addButton` then adds the new button to `gridContainer` at the location 0
     * for the first button then at position 1 for all the following buttons. The GridView attribute
     * android:animateLayoutChanges="true" causes a default LayoutTransition object to be set
     * on the ViewGroup container and default animations will run when layout changes occur (both when
     * adding and removing a button).
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_animations_by_default)

        val gridContainer = findViewById<GridLayout>(R.id.gridContainer)

        val addButton = findViewById<Button>(R.id.addNewButton)
        addButton.setOnClickListener {
            val newButton = Button(this@LayoutAnimationsByDefault)
            newButton.text = numButtons++.toString()
            newButton.setOnClickListener { v ->
                /**
                 * Called when `Button newButton` is clicked. We call the `removeView`
                 * method of `GridLayout gridContainer` to remove our view from it.
                 *
                 * @param v `View` that was clicked.
                 */
                /**
                 * Called when `Button newButton` is clicked. We call the `removeView`
                 * method of `GridLayout gridContainer` to remove our view from it.
                 *
                 * @param v `View` that was clicked.
                 */
                gridContainer.removeView(v)
            }
            gridContainer.addView(newButton, min(1, gridContainer.childCount))
        }
    }

}