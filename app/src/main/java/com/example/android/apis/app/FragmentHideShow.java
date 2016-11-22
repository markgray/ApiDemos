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
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Demonstration of hiding and showing fragments while keeping fragment state using
 * FragmentTransaction.hide(Fragment), and FragmentTransaction.show(Fragment).
 * The Fragment's are created in the layout using the <fragment> xml element.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentHideShow extends Activity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.fragment_hide_show. We
     * fetch a handle to the FragmentManager for interacting with fragments associated with this
     * activity and save it in <b>FragmentManager fm</b>. Then we use <b>fm</b> to locate the two
     * fragments in our layout: R.id.fragment1 and R.id.fragment2 and pass these Fragment references
     * to our method addShowHideListener in order to configure the OnClickListener's for the Button's
     * R.id.frag1hide and R.id.frag2hide so that clicking those Button's will toggle whether the
     * two Fragment's are shown or hidden.
     *
     * @param savedInstanceState we do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_hide_show);

        // The content view embeds two fragments; now retrieve them and attach
        // their "hide" button.
        FragmentManager fm = getFragmentManager();
        addShowHideListener(R.id.frag1hide, fm.findFragmentById(R.id.fragment1));
        addShowHideListener(R.id.frag2hide, fm.findFragmentById(R.id.fragment2));
    }

    /**
     * Adds an OnClickListener to the Button whose resource id is <b>buttonId</b> which creates a
     * FragmentTransaction to toggle whether the <b>Fragment fragment</b> is shown or hidden. First
     * we locate the <b>Button button</b> in our layout whose id is <b>buttonId</b>, then we set the
     * OnClickListener of <b>button</b> to an anonymous class which will create a FragmentTransaction
     * which will toggle whether the Fragment is shown or hidden.
     *
     * @param buttonId Resource id for the Button to add the OnClickListener for
     * @param fragment Fragment whose show/hide will toggled
     */
    void addShowHideListener(int buttonId, final Fragment fragment) {
        final Button button = (Button) findViewById(buttonId);
        button.setOnClickListener(new OnClickListener() {
            /**
             * Called when <b>Button button</b> is clicked. First we create a <b>FragmentTransaction ft</b>
             * by using the FragmentManager for interacting with fragments associated with this activity
             * to start a series of edit operations on the Fragments associated with this FragmentManager.
             * If the <b>Fragment fragment</b> is hidden we use <b>ft</b> to show the Fragment, and update
             * the text of <b>Button button</b> to the String R.string.hide ("Hide"). Otherwise we use
             * <b>ft</b> to hide the Fragment, and update the text of <b>Button button</b> to the String
             * R.string.show ("Show"). Finally we schedule a commit of <b>FragmentTransaction ft</b>.
             *
             * @param v View of Button that was clicked
             */
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                if (fragment.isHidden()) {
                    ft.show(fragment);
                    button.setText(R.string.hide);
                } else {
                    ft.hide(fragment);
                    button.setText(R.string.show);
                }
                ft.commit();
            }
        });
    }

    /**
     * This is an example Fragment which saves the state of the EditText in its layout file using
     * the callback OnSaveInstanceState, and is added to our main layout using a fragment element.
     */
    public static class FirstFragment extends Fragment {
        TextView mTextView; // EditText R.id.msg in our layout file (R.layout.labeled_text_edit)

        /**
         * Called to have the fragment instantiate its user interface view. First we use
         * <b>LayoutInflater inflater</b> to inflate our layout file R.layout.labeled_text_edit
         * into <b>View v</b>. Then we locate the label TextView R.id.msg in our layout file and
         * save a reference to it in <b>View tv</b>. We use <b>tv</b> to set the text in the
         * label <b>TextView</b> to the String R.string.text_for_fragment_to_save ("The fragment
         * saves and restores this text."). Then we initialize our field <b>TextView mTextView</b>
         * by finding the EditText in our layout R.id.saved. Then we check to see if our parameter
         * <b>savedInstanceState</b> is not null (we are being restarted) and if so we retrieve the
         * String we stored in our callback onSaveInstanceState under the key "text" and use it to
         * set the text of <b>mTextView</b>. Finally we return <b>View v</b> to our caller.
         *
         * @param inflater           The LayoutInflater object that can be used to inflate
         *                           any views in the fragment,
         * @param container          If non-null, this is the parent view that the fragment's
         *                           UI should be attached to.  The fragment should not add the view itself,
         *                           but this can be used to generate the LayoutParams of the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         *                           from a previous saved state as given here.
         *
         * @return Return the View for the fragment's UI.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.labeled_text_edit, container, false);
            View tv = v.findViewById(R.id.msg);
            ((TextView) tv).setText(R.string.text_for_fragment_to_save);

            // Retrieve the text editor, and restore the last saved state if needed.
            mTextView = (TextView) v.findViewById(R.id.saved);
            if (savedInstanceState != null) {
                mTextView.setText(savedInstanceState.getCharSequence("text"));
            }
            return v;
        }

        /**
         * Called to ask the fragment to save its current dynamic state, so it can later be
         * reconstructed in a new instance of its process is restarted.  If a new instance
         * of the fragment later needs to be created, the data you place in the Bundle here
         * will be available in the Bundle given to {@link #onCreate(Bundle)},
         * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, and
         * {@link #onActivityCreated(Bundle)}.
         * <p>
         * First we call through to our super's implementation of onSaveInstanceState. Then we
         * retrieve the text in our layout's EditText R.id.saved (a reference to it was used to
         * initialize our field <b>TextView mTextView</b>) and store it in our parameter
         * <b>Bundle outState</b> under the key "text".
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
     * This is an example Fragment which relies on the TextView to save and restore the state of the
     * EditText in its layout file, and is added to our main layout using a fragment element.
     */
    public static class SecondFragment extends Fragment {

        /**
         * Called to have the fragment instantiate its user interface view. First we use our parameter
         * <b>LayoutInflater inflater</b> to inflate our layout file R.layout.labeled_text_edit into
         * <b>View v</b>. Then we locate the TextView R.id.msg in <b>v</b> and save a reference to
         * it in <b>View tv</b>. We use <b>tv</b> to set the text of the TextView to the String
         * R.string.textview_text_to_save ("The TextView saves and restores this text."). We locate
         * the EditText in our layout R.id.saved and call its method <b>setSaveEnabled(true)</b>
         * which enables the saving of the EditText's state (that is, whether its onSaveInstanceState()
         * method will be called). Note that even if freezing is enabled, the view still must have an
         * id assigned to it (via setId(int)) for its state to be saved. Finally we return our inflated
         * and configured layour <b>View v</b> to the caller.
         *
         * @param inflater           The LayoutInflater object that can be used to inflate
         *                           any views in the fragment,
         * @param container          If non-null, this is the parent view that the fragment's
         *                           UI should be attached to.  The fragment should not add the view itself,
         *                           but this can be used to generate the LayoutParams of the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         *                           from a previous saved state as given here.
         *
         * @return Return the View for the fragment's UI.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.labeled_text_edit, container, false);
            View tv = v.findViewById(R.id.msg);
            ((TextView) tv).setText(R.string.textview_text_to_save);

            // Retrieve the text editor and tell it to save and restore its state.
            // Note that you will often set this in the layout XML, but since
            // we are sharing our layout with the other fragment we will customize
            // it here.
            v.findViewById(R.id.saved).setSaveEnabled(true);
            return v;
        }
    }
}
