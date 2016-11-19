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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.apis.R;

/**
 * Shows how to show the same DialogFragment embedded in the activity layout, and as a dialog.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentDialogOrActivity extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.fragment_dialog_or_activity.
     * Next we check to see if our parameter <b>Bundle savedInstanceState</b> is null, which means
     * this is our first time being called so we need to embed our Fragment in our layout ourselves
     * rather than rely on the system to recreate it. If null then we create <b>FragmentTransaction ft</b>
     * by using the FragmentManager for interacting with fragments associated with this activity to
     * start a series of edit operations on the Fragments associated with this FragmentManager. We
     * create an instance of <b>DialogFragment newFragment</b>, use <b>ft</b> to add <b>newFragment</b>
     * in the <b>FrameLayout</b> R.id.embedded inside our layout, and then commit <b>ft</b>. Having
     * taken care of our embedded Fragment we locate <b>Button button</b> R.id.show_dialog ("Show")
     * in our layout file and set its OnClickListener to an anonymous class which will call our method
     * <b>showDialog()</b> when the Button is clicked.
     *
     * @param savedInstanceState if the activity is being recreated after an orientation change this
     *                           will contain information for the FragmentManager to use, otherwise
     *                           it is null. We use this to decide whether it is the first time that
     *                           onCreate has been called (it will be null)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_dialog_or_activity);

        if (savedInstanceState == null) {
            // First-time init; create fragment to embed in activity.

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DialogFragment newFragment = MyDialogFragment.newInstance();
            ft.add(R.id.embedded, newFragment);
            ft.commit();

        }

        // Watch for button clicks.
        Button button = (Button) findViewById(R.id.show_dialog);
        button.setOnClickListener(new OnClickListener() {
            /**
             * Called when a view has been clicked. We simply call our method <b>showDialog()</b>.
             *
             * @param v View of the Button that was clicked
             */
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }

    /**
     * Create the fragment and show it as a dialog.
     */
    void showDialog() {
        DialogFragment newFragment = MyDialogFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * Simple <b>DialogFragment</b> which only displays a String in a <b>TextView</b>
     */
    public static class MyDialogFragment extends DialogFragment {
        /**
         * Simply creates and returns a new instance of <b>MyDialogFragment</b>
         *
         * @return new instance of <b>MyDialogFragment</b>
         */
        static MyDialogFragment newInstance() {
            return new MyDialogFragment();
        }

        /**
         * Called to have the fragment instantiate its user interface view. First we use our parameter
         * <b>LayoutInflater inflater</b> to inflate our layout file R.layout.hello_world into the
         * variable <b>View v</b>. Then we locate <b>View tv</b> R.id.text in <b>v</b>, and set the
         * text in this <b>TextView</b> to the String R.string.my_dialog_fragment_label:
         * <p>
         * <center>This is an instance of MyDialogFragment</center>
         * <p>
         * Finally we return <b>View v</b> to the caller.
         *
         * @param inflater           The LayoutInflater object that can be used to inflate
         *                           any views in the fragment,
         * @param container          If non-null, this is the parent view that the fragment's
         *                           UI should be attached to.  The fragment should not add the view itself,
         *                           but this can be used to generate the LayoutParams of the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         *                           from a previous saved state as given here.
         *
         * @return Return the View for the fragment's UI
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.hello_world, container, false);
            View tv = v.findViewById(R.id.text);
            ((TextView) tv).setText(R.string.my_dialog_fragment_label);
            return v;
        }
    }

}
