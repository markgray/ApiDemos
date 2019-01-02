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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Demonstration of hiding and showing fragments.
 */
@SuppressLint("SetTextI18n")
public class FragmentHideShow extends Activity {

    /**
     * Called when the activity is starting. First we call our super's implementation of {@code onCreate},
     * then we set our content view to our layout file R.layout.fragment_hide_show. We initialize
     * {@code FragmentManager fm} with the FragmentManager for interacting with fragments associated
     * with this activity, use it to find the fragment with id R.id.fragment1 in order to initialize
     * {@code Fragment fragment1} with it. We call our method {@code addShowHideListener} to add an
     * {@code OnClickListener} to the button with id R.id.frag1hide which will toggle the show/hide
     * state of {@code fragment1} when it is clicked and set the text of the button to "Hide" or
     * "Show" whichever is then appropriate. We initialize {@code Button button1} by finding the
     * view with id R.id.frag1hide and set its text to "Show" if the {@code isHidden} method of
     * {@code fragment1} returns true (fragment is hidden) or to "Hide" if it is false (fragment is
     * not hidden). We then use {@code fm} to find the fragment with id R.id.fragment2 in order to
     * initialize {@code Fragment fragment2} with it. We call our method {@code addShowHideListener}
     * to add an {@code OnClickListener} to the button with id R.id.frag2hide which will toggle the
     * show/hide state of {@code fragment2} when it is clicked and set the text of the button to "Hide"
     * or "Show" whichever is then appropriate. We initialize {@code Button button2} by finding the
     * view with id R.id.frag2hide and set its text to "Show" if the {@code isHidden} method of
     * {@code fragment2} returns true (fragment is hidden) or to "Hide" if it is false (fragment is
     * not hidden).
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_hide_show);

        // The content view embeds two fragments; now retrieve them and attach
        // their "hide" button.
        FragmentManager fm = getFragmentManager();
        Fragment fragment1 = fm.findFragmentById(R.id.fragment1);
        addShowHideListener(R.id.frag1hide, fragment1);
        final Button button1 = findViewById(R.id.frag1hide);
        button1.setText(fragment1.isHidden() ? "Show" : "Hide");

        Fragment fragment2 = fm.findFragmentById(R.id.fragment2);
        addShowHideListener(R.id.frag2hide, fragment2);
        final Button button2 = findViewById(R.id.frag2hide);
        button2.setText(fragment2.isHidden() ? "Show" : "Hide");
    }

    /**
     * Locates the button whose resource id is {@code int buttonId} and sets its {@code OnClickListener}
     * to an anonymous class which will toggle the hide/show state of {@code Fragment fragment}.
     *
     * @param buttonId resource id of the button we are to add our {@code OnClickListener} to
     * @param fragment {@code Fragment} whose hide/show state is to be toggled by the button.
     */
    void addShowHideListener(int buttonId, final Fragment fragment) {
        final Button button = findViewById(buttonId);
        button.setOnClickListener(new OnClickListener() {
            /**
             * Called when the {@code View} we are the {@code OnClickListener} for is clicked. We
             * initialize {@code FragmentTransaction ft} by using the the FragmentManager for interacting
             * with fragments associated with this activity to begin a transaction. We call the
             * {@code setCustomAnimations} of {@code ft} to specify the animation resources to run for
             * the fragments that are entering and exiting in this transaction to be
             * android.R.animator.fade_in and android.R.animator.fade_out (these are objectAnimator
             * objects which manipulate the alpha value of their views). If the {@code isHidden} method
             * of {@code fragment} returns true (the fragment is currently hidden) we call the {@code show}
             * method of {@code ft} to show the fragment and set the text of our {@code button} to "Hide".
             * If returns false (the fragment is currently shown) we call the {@code hide} method of
             * {@code ft} to hide the fragment and set the text of our {@code button} to "Show". Finally
             * we commit {@code FragmentTransaction ft}.
             *
             * @param v {@code View} that was clicked.
             */
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                if (fragment.isHidden()) {
                    ft.show(fragment);
                    button.setText("Hide");
                } else {
                    ft.hide(fragment);
                    button.setText("Show");
                }
                ft.commit();
            }
        });
    }

    /**
     * First fragment in our layout file layout/fragment_hide_show.xml, has id R.id.fragment1. It uses
     * an {@code onSaveInstanceState} override to save the text of the {@code EditText} in its layout
     * file, then restores the text in its {@code onCreateView} override.
     */
    public static class FirstFragment extends Fragment {
        /**
         * The {@code EditText} in our layout file with id R.id.saved whose contents we save in the
         * {@code Bundle} passed to our {@code onSaveInstanceState} override, then restore in our
         * {@code onCreateView} override.
         */
        TextView mTextView;

        /**
         * Called to have the fragment instantiate its user interface view. We use our parameter
         * {@code LayoutInflater inflater} to inflate our layout file R.layout.labeled_text_edit into
         * {@code View v} using our parameter {@code ViewGroup container} for the LayoutParams without
         * attaching to it. We initialize {@code View tv} by finding the view in {@code v} with id
         * R.id.msg, then set its text to the string "The fragment saves and restores this text."
         * We initialize our field {@code TextView mTextView} by finding the view in {@code v} with
         * id R.id.saved, then if our parameter {@code Bundle savedInstanceState} is not null we set
         * the text of {@code mTextView} to the string stored under the key "text" in {@code savedInstanceState}.
         * Finally we return {@code v} to the caller.
         *
         * @param inflater           The LayoutInflater object that can be used to inflate
         *                           any views in the fragment,
         * @param container          If non-null, this is the parent view that the fragment's
         *                           UI should be attached to. The fragment should not add the view itself,
         *                           but this can be used to generate the LayoutParams of the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         *                           from a previous saved state as given here.
         *
         * @return Return the View for the fragment's UI, or null.
         */
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.labeled_text_edit, container, false);
            View tv = v.findViewById(R.id.msg);
            ((TextView)tv).setText("The fragment saves and restores this text.");

            // Retrieve the text editor, and restore the last saved state if needed.
            mTextView = v.findViewById(R.id.saved);
            if (savedInstanceState != null) {
                mTextView.setText(savedInstanceState.getCharSequence("text"));
            }
            return v;
        }

        /**
         * Called to ask the fragment to save its current dynamic state, so it can later be reconstructed
         * in a new instance if its process is restarted. First we call our super's implementation of
         * {@code onSaveInstanceState} then we retrieve the text of our field {@code TextView mTextView}
         * and store that {@code CharSequence} in our parameter {@code Bundle outState} under the key
         * "text".
         *
         * @param outState Bundle in which to place your saved state.
         */
        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);

            // Remember the current text, to restore if we later restart.
            outState.putCharSequence("text", mTextView.getText());
        }
    }

    /**
     * Second fragment in our layout file layout/fragment_hide_show.xml, has id R.id.fragment2. It
     * calls the {@code setSaveEnabled(true)} method of the {@code EditText} in its layout to have it
     * save and restore its text.
     */
    public static class SecondFragment extends Fragment {

        /**
         * Called to have the fragment instantiate its user interface view. We use our parameter
         * {@code LayoutInflater inflater} to inflate our layout file R.layout.labeled_text_edit into
         * {@code View v} using our parameter {@code ViewGroup container} for the LayoutParams without
         * attaching to it. We initialize {@code View tv} by finding the view in {@code v} with id
         * R.id.msg, then set its text to the string "The TextView saves and restores this text."
         * We then find the view in {@code v} with id R.id.saved and call its {@code setSaveEnabled(true)}
         * method to allow it to save its state. Finally we return {@code v} to the caller.
         *
         * @param inflater           The LayoutInflater object that can be used to inflate
         *                           any views in the fragment,
         * @param container          If non-null, this is the parent view that the fragment's
         *                           UI should be attached to. The fragment should not add the view itself,
         *                           but this can be used to generate the LayoutParams of the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         *                           from a previous saved state as given here.
         *
         * @return Return the View for the fragment's UI, or null.
         */
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.labeled_text_edit, container, false);
            View tv = v.findViewById(R.id.msg);
            ((TextView)tv).setText("The TextView saves and restores this text.");

            // Retrieve the text editor and tell it to save and restore its state.
            // Note that you will often set this in the layout XML, but since
            // we are sharing our layout with the other fragment we will customize
            // it here.
            v.findViewById(R.id.saved).setSaveEnabled(true);
            return v;
        }
    }
}
