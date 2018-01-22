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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import com.example.android.apis.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Demonstrates making a view VISIBLE, INVISIBLE and GONE. Three buttons control
 * the visibility of a TextView which is sandwiched between two other TextView's
 */
public class Visibility1 extends Activity {
    /**
     * {@code View} whose visibility is changed by the buttons between VISIBLE INVISIBLE or GONE
     */
    private View mVictim;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.visibility_1. We
     * initialize our field {@code View mVictim} by finding the view with the id R.id.victim (a
     * {@code TextView} with the text "View B" that lies between two other {@code TextView}). We
     * initialize our variables {@code Button visibleButton} by finding the view with id R.id.vis
     * ("Vis"), {@code Button invisibleButton} by finding the view with id R.id.invis ("Invis"), and
     * {@code Button goneButton} by finding the view with id R.id.gone ("Gone"). We set the
     * {@code OnClickListener} of {@code visibleButton} to {@code OnClickListener mVisibleListener}
     * (an anonymous class which sets the visibility of {@code View mVictim} to VISIBLE), the
     * {@code OnClickListener} of {@code invisibleButton} to {@code OnClickListener mInvisibleListener}
     * (an anonymous class which sets the visibility of {@code View mVictim} to INVISIBLE), and the
     * {@code OnClickListener} of {@code goneButton} to {@code OnClickListener mGoneListener}
     * (an anonymous class which sets the visibility of {@code View mVictim} to GONE).
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visibility_1);

        // Find the view whose visibility will change
        mVictim = findViewById(R.id.victim);

        // Find our buttons
        Button visibleButton = (Button) findViewById(R.id.vis);
        Button invisibleButton = (Button) findViewById(R.id.invis);
        Button goneButton = (Button) findViewById(R.id.gone);

        // Wire each button to a click listener
        visibleButton.setOnClickListener(mVisibleListener);
        invisibleButton.setOnClickListener(mInvisibleListener);
        goneButton.setOnClickListener(mGoneListener);
    }

    /**
     * An anonymous class which sets the visibility of {@code View mVictim} to VISIBLE when its view
     * is clicked.
     */
    OnClickListener mVisibleListener = new OnClickListener() {
        /**
         * Called when the {@code Button} with id R.id.vis ("Vis") is clicked, we just set the
         * visibility of {@code View mVictim} to VISIBLE.
         *
         * @param v View that was clicked.
         */
        @Override
        public void onClick(View v) {
            mVictim.setVisibility(View.VISIBLE);
        }
    };

    /**
     * An anonymous class which sets the visibility of {@code View mVictim} to INVISIBLE when its view
     * is clicked.
     */
    OnClickListener mInvisibleListener = new OnClickListener() {
        /**
         * Called when the {@code Button} with id R.id.invis ("Invis") is clicked, we just set the
         * visibility of {@code View mVictim} to INVISIBLE.
         *
         * @param v View that was clicked.
         */
        @Override
        public void onClick(View v) {
            mVictim.setVisibility(View.INVISIBLE);
        }
    };

    /**
     * An anonymous class which sets the visibility of {@code View mVictim} to GONE when its view
     * is clicked.
     */
    OnClickListener mGoneListener = new OnClickListener() {
        /**
         * Called when the {@code Button} with id R.id.gone ("Gone") is clicked, we just set the
         * visibility of {@code View mVictim} to GONE.
         *
         * @param v View that was clicked.
         */
        @Override
        public void onClick(View v) {
            mVictim.setVisibility(View.GONE);
        }
    };
}
