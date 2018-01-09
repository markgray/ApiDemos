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

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Demonstrates the use of progress dialogs. Uses Activity#onCreateDialog
 * Activity#showDialog to ensure the dialogs will be properly saved and
 * restored. Buttons on the main Layout allow you to choose between one
 * which sets the title using dialog.setTitle("Indeterminate") and one which
 * has no title. (Direct use of showDialog is deprecated, use a DialogFragment
 * instead).
 */
@SuppressWarnings({"unused", "deprecation"})
public class ProgressBar3 extends Activity {
    /**
     * {@code ProgressDialog} which sets the title to "Indeterminate" UNUSED
     */
    ProgressDialog mDialog1;
    /**
     * {@code ProgressDialog} with no title UNUSED
     */
    ProgressDialog mDialog2;

    /**
     * ID of the {@code ProgressDialog mDialog1}
     */
    private static final int DIALOG1_KEY = 0;
    /**
     * ID of the {@code ProgressDialog mDialog2}
     */
    private static final int DIALOG2_KEY = 1;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.progressbar_3.
     * We initialize our variable {@code Button button} by finding the view with ID R.id.showIndeterminate
     * ("Show Indeterminate") and set its {@code OnClickListener} to an anonymous class which calls
     * {@code showDialog} with the ID DIALOG1_KEY. We then set {@code button} by finding the view with
     * ID R.id.showIndeterminateNoTitle ("Show Indeterminate No Title") and set its {@code OnClickListener}
     * to an anonymous class which calls {@code showDialog} with the ID DIALOG2_KEY.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progressbar_3);

        Button button = (Button) findViewById(R.id.showIndeterminate);
        button.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when the button with ID R.id.showIndeterminate ("Show Indeterminate") is clicked
             * we simply call the method {@code showDialog} using the ID DIALOG1_KEY. Our {@code onCreateDialog}
             * override will take care of constructing the appropriate {@code Dialog}.
             *
             * @param v View that was clicked
             */
            @Override
            public void onClick(View v) {
                showDialog(DIALOG1_KEY);
            }
        });

        button = (Button) findViewById(R.id.showIndeterminateNoTitle);
        button.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when the button with ID R.id.showIndeterminateNoTitle ("Show Indeterminate No Title") is clicked
             * we simply call the method {@code showDialog} using the ID DIALOG2_KEY. Our {@code onCreateDialog}
             * override will take care of constructing the appropriate {@code Dialog}.
             *
             * @param v View that was clicked
             */
            @Override
            public void onClick(View v) {
                showDialog(DIALOG2_KEY);
            }
        });
    }

    /**
     * Callback for creating dialogs that are managed (saved and restored) for you by the activity.
     * We switch on the value of our parameter {@code int id}:
     * <ul>
     * <li>
     * DIALOG1_KEY - we create a new instance to initialize our variable {@code ProgressDialog dialog},
     * set its title to the string "Indeterminate", set its message to the string "Please wait while loading...",
     * set it to be indeterminate, and set it to be cancelable. Finally we return {@code dialog} to the
     * caller.
     * </li>
     * <li>
     * DIALOG2_KEY - we create a new instance to initialize our variable {@code ProgressDialog dialog},
     * set its message to the string "Please wait while loading...", set it to be indeterminate, and
     * set it to be cancelable. Finally we return {@code dialog} to the caller.
     * </li>
     * </ul>
     * For any other {@code id} we return null.
     *
     * @param id The id of the dialog.
     * @return The dialog.  If you return null, the dialog will not be created.
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG1_KEY: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle("Indeterminate");
                dialog.setMessage("Please wait while loading...");
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            case DIALOG2_KEY: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage("Please wait while loading...");
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
        }
        return null;
    }
}
