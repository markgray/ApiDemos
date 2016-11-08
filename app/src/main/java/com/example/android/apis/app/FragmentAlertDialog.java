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

package com.example.android.apis.app;

import com.example.android.apis.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
/**
 * Demonstrates how to show an AlertDialog that is managed by a Fragment. Uses DialogFragment
 * as the base class and overrides onCreateDialog in which it builds the AlertDialog using an
 * AlertDialog.Builder
 */
public class FragmentAlertDialog extends Activity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.fragment_dialog. Then we
     * find the <code>View tv</code> in the layout (R.id.text) and set its text to the String
     * R.string.example_alert_dialogfragment:
     *
     *     "Example of displaying an alert dialog with a DialogFragment"
     *
     * Finally we locate the <code>Button button</code> R.id.show ("Show") and set its OnClickListener
     * to an anonymous class which calls our method <code>showDialog()</code> to show our AlertDialog.
     *
     * @param savedInstanceState we do not override onSaveInstanceState so do not use this
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_dialog);

        View tv = findViewById(R.id.text);
        ((TextView)tv).setText(R.string.example_alert_dialogfragment);

        // Watch for button clicks.
        Button button = (Button)findViewById(R.id.show);
        button.setOnClickListener(new OnClickListener() {
            /**
             * Called when the Button R.id.show ("Show") is clicked. We simply call our method
             * showDialog()
             *
             * @param v View of Button that was clicked
             */
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }

    /**
     * Create and show a MyAlertDialogFragment DialogFragment. We create a new instance of
     * MyAlertDialogFragment by calling its method newInstance with the resource id for the
     * nonsense String R.string.alert_dialog_two_buttons_title, and then invoke the method
     * DialogFragment.show to show it.
     */
    void showDialog() {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(R.string.alert_dialog_two_buttons_title);
        newFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * OnClickListener Callback for when the positive Button of the MyAlertDialogFragment
     */
    public void doPositiveClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Positive click!");
    }

    /**
     * OnClickListener Callback for when the negative Button of the MyAlertDialogFragment
     */
    public void doNegativeClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Negative click!");
    }

    /**
     * Minimalist DialogFragment
     */
    public static class MyAlertDialogFragment extends DialogFragment {

        /**
         * Factory method to create a new instance of MyAlertDialogFragment and set its arguments.
         * First we create a new instance <code>MyAlertDialogFragment frag</code>, then we create
         * a <code>Bundle args</code>, add our parameter <code>int title</code> to it under the
         * key "title", and then set the argmuments of <code>frag</code> to our <code>Bundle args</code>.
         * Finally we return <code>MyAlertDialogFragment frag</code> to the caller.
         *
         * @param title resource id for a String to use as the DialogFragment's title
         *
         * @return New instance of MyAlertDialogFragment with its arguments set to
         */
        public static MyAlertDialogFragment newInstance(int title) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }
        
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");
            
            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.alert_dialog_icon)
                    .setTitle(title)
                    .setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((FragmentAlertDialog)getActivity()).doPositiveClick();
                            }
                        }
                    )
                    .setNegativeButton(R.string.alert_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((FragmentAlertDialog)getActivity()).doNegativeClick();
                            }
                        }
                    )
                    .create();
        }
    }

}
