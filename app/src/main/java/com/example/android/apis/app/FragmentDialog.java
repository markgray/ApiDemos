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
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Example of displaying dialogs with a DialogFragment. Press the show button
 * at the bottom to see the first dialog; pressing successive show buttons on
 * the dialogs will display other dialog styles as a stack, with back going to
 * the previous dialog. The various styles are: STYLE_NO_TITLE; STYLE_NO_FRAME;
 * STYLE_NO_INPUT (this window can't receive input, so you will need to press
 * the bottom show button); STYLE_NORMAL with dark fullscreen theme; STYLE_NORMAL
 * with light theme; STYLE_NO_TITLE with light theme; STYLE_NO_FRAME with light
 * theme; STYLE_NORMAL with light fullscreen theme; and STYLE_NORMAL.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentDialog extends Activity {
    int mStackLevel = 0; // Level used to choose style of dialog (and stack level)

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.fragment_dialog. Next we
     * locate the TextView in our layout (R.id.text) and set its text to the instructions for this
     * demo (R.string.dialog_fragment_example_instructions):
     *
     *     Example of displaying dialogs with a DialogFragment.
     *     Press the show button below to see the first dialog;
     *     pressing successive show buttons will display other
     *     dialog styles as a stack, with dismissing or back
     *     going to the previous dialog.
     *
     * We locate Button button in our layout (R.id.show "SHOW") and set its OnClickListener to an
     * anonymous class which calls our method showDialog() when the Button is clicked. Finally if
     * savedInstanceState is not null (we are being recreated after an orientation change or other
     * reason) we retrieve the value of our field mStackLevel that our callback onSaveInstanceState
     * saved using the key "level".
     *
     * @param savedInstanceState if Activity has been recreated after an orientation change this will
     *                           have the value for mStackLevel saved by onSaveInstanceState using
     *                           the key "level"
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_dialog);

        View tv = findViewById(R.id.text);
        ((TextView) tv).setText(R.string.dialog_fragment_example_instructions);

        // Watch for button clicks.
        Button button = (Button) findViewById(R.id.show);
        button.setOnClickListener(new OnClickListener() {
            /**
             * Called when a view has been clicked. We simply call our method showDialog() when
             * the "SHOW" Button (R.id.show) is clicked.
             *
             * @param v View of Button that was clicked
             */
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        if (savedInstanceState != null) {
            mStackLevel = savedInstanceState.getInt("level");
        }
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in {@link #onCreate} or
     * {@link #onRestoreInstanceState} (the {@link Bundle} populated by this method
     * will be passed to both).
     *
     * First we call through to our super's implementation of onCreate, then we store the value of
     * our field mStackLevel in the mapping of our parameter Bundle outState under the key "level".
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("level", mStackLevel);
    }

    /**
     * Creates and show's a new instance of MyDialogFragment using the next style and theme. First we
     * increment the value of our field mStackLevel and if it is greater than 8 we set it back to 1.
     * Then we use the FragmentManager for interacting with fragments associated with this activity
     * to begin a FragmentTransaction ft, and then we add this transaction to the back stack. We
     * search the FragmentManager for an existing Fragment that is already using Tag "dialog" and if
     * there is one we use FragmentTransaction ft to remove it. Next we create a new instance of
     * MyDialogFragment with the style and theme corresponding to the value of mStackLevel,
     * (DialogFragment newFragment) and finally we show this DialogFragment.
     */
    void showDialog() {
        mStackLevel++;
        if (mStackLevel > 8) mStackLevel = 1;

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }

        // Create and show the dialog.
        DialogFragment newFragment = MyDialogFragment.newInstance(mStackLevel);
        newFragment.show(ft, "dialog");
    }


    static String getNameForNum(int num) {
        switch (num) {
            case 1:
                return "STYLE_NO_TITLE";
            case 2:
                return "STYLE_NO_FRAME";
            case 3:
                return "STYLE_NO_INPUT (this window can't receive input, so "
                        + "you will need to press the bottom show button)";
            case 4:
                return "STYLE_NORMAL with dark fullscreen theme";
            case 5:
                return "STYLE_NORMAL with light theme";
            case 6:
                return "STYLE_NO_TITLE with light theme";
            case 7:
                return "STYLE_NO_FRAME with light theme";
            case 8:
                return "STYLE_NORMAL with light fullscreen theme";
        }
        return "STYLE_NORMAL";
    }

    public static class MyDialogFragment extends DialogFragment {
        int mNum;

        /**
         * Create a new instance of MyDialogFragment, providing "num"
         * as an argument.
         */
        static MyDialogFragment newInstance(int num) {
            MyDialogFragment f = new MyDialogFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            f.setArguments(args);

            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments().getInt("num");

            // Pick a style based on the num.
            int style = DialogFragment.STYLE_NORMAL, theme = 0;
            switch (mNum) {
                case 1:
                    style = DialogFragment.STYLE_NO_TITLE;
                    break;
                case 2:
                    style = DialogFragment.STYLE_NO_FRAME;
                    break;
                case 3:
                    style = DialogFragment.STYLE_NO_INPUT;
                    break;
                case 4:
                    style = DialogFragment.STYLE_NORMAL;
                    break;
                case 5:
                    style = DialogFragment.STYLE_NORMAL;
                    break;
                case 6:
                    style = DialogFragment.STYLE_NO_TITLE;
                    break;
                case 7:
                    style = DialogFragment.STYLE_NO_FRAME;
                    break;
                case 8:
                    style = DialogFragment.STYLE_NORMAL;
                    break;
            }
            switch (mNum) {
                case 4:
                    theme = android.R.style.Theme_Holo;
                    break;
                case 5:
                    theme = android.R.style.Theme_Holo_Light_Dialog;
                    break;
                case 6:
                    theme = android.R.style.Theme_Holo_Light;
                    break;
                case 7:
                    theme = android.R.style.Theme_Holo_Light_Panel;
                    break;
                case 8:
                    theme = android.R.style.Theme_Holo_Light;
                    break;
            }
            setStyle(style, theme);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_dialog, container, false);
            View tv = v.findViewById(R.id.text);
            String dialogLabel = getString(R.string.dialog_number) + mNum + ": using style "
                    + getNameForNum(mNum);
            ((TextView) tv).setText(dialogLabel);

            // Watch for button clicks.
            Button button = (Button) v.findViewById(R.id.show);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // When button is clicked, call up to owning activity.
                    ((FragmentDialog) getActivity()).showDialog();
                }
            });

            return v;
        }
    }

}
