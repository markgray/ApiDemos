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
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
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
 * Demonstrates the use of custom animations in a FragmentTransaction when
 * pushing and popping a stack.
 *
 * Uses FragmentTransaction.setCustomAnimations to cause animations to be used
 * when replacing one fragment with the next. The "POP" button does the same thing
 * as the back button by calling onBackPressed, it takes you back through the numbered
 * fragments on the stack after you "Push" them, again using the same animation.
 * onSaveInstanceState saves the mStackLevel in an int "level" which is used when
 * the Activity is recreated to remember the stack level, mStackLevel is then used
 * to set the int argument "num" passed to the new fragment when it is created.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@SuppressLint("DefaultLocale")
public class FragmentCustomAnimations extends Activity {
    int mStackLevel = 1; // Stack level for the next fragment

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.fragment_stack. Next we
     * locate <b>Button button</b> in our layout with id R.id.new_fragment ("Push"), and set
     * its onClickListener to an anonymous class which calls our method addFragmentToStack when the
     * Button is clicked. We locate the <b>Button</b> R.id.delete_fragment ("Pop") and set its
     * OnClickListener to an anonymous class which invokes Activity.onBackPressed when clicked and
     * "finishes" the Fragment thereby returning to the Fragment behind it. If our parameter
     * savedInstanceState is null this is the first time we have been created so we create a new
     * instance of CountingFragment with the initial stack level of mStackLevel = 1. Then we create
     * a <b>FragmentTransaction ft</b> and begin a series of Fragment transactions using it
     * by calling <b>getFragmentManager().beginTransaction()</b>. We add <b>newFragment</b>
     * using the view id of the FrameLayout in our layout file: R.id.simple_fragment, and then commit
     * <b>FragmentTransaction ft</b>. If it is not null we are being recreated after an
     * orientation change so we retrieve the value for our field <b>int mStackLevel</b> which
     * was stored under the key "level" by our callback onSaveInstanceState.
     *
     * @param savedInstanceState if null it is first time, otherwise will contain the value of
     *                           mStackLevel to use under the key "level"
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_stack);

        // Watch for button clicks.
        Button button = (Button)findViewById(R.id.new_fragment);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragmentToStack();
            }
        });

        button = (Button) findViewById(R.id.delete_fragment);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (savedInstanceState == null) {
            // Do first time initialization -- add initial fragment.
            Fragment newFragment = CountingFragment.newInstance(mStackLevel);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.simple_fragment, newFragment).commit();
        } else {
            mStackLevel = savedInstanceState.getInt("level");
        }
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in {@link #onCreate} or
     * {@link #onRestoreInstanceState} (the {@link Bundle} populated by this method
     * will be passed to both).
     *
     * First we call through to our super's implementation of onSaveInstanceState, then we insert the
     * value of mStackLevel into the mapping of our parameter <b>Bundle outState</b> using the
     * key "level".
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("level", mStackLevel);
    }

    /**
     * This method adds a new fragment to the stack when the R.id.new_fragment ("Push") Button is
     * clicked. First we increment the stack level <b>mStackLevel</b>, then we create a new
     * instance of <b>CountingFragment</b> with this level: <b>Fragment newFragment</b>.
     * Then we begin a series of fragment transactions using <b>FragmentTransaction ft</b>
     * which is created by calling <b>getFragmentManager().beginTransaction()</b>. The first
     * transaction sets specific animation resources to run for the fragments that are entering and
     * exiting in this transaction:
     *
     *     R.animator.fragment_slide_left_enter is used as a new fragment is entering (pushed)
     *     R.animator.fragment_slide_left_exit when a fragment is exiting (being replaced)
     *     R.animator.fragment_slide_right_enter when a fragment is returning from a stack pop
     *     R.animator.fragment_slide_right_exit when a fragment is leaving because it was popped.
     *
     * The next transaction replaces the old fragment with <b>newFragment</b>, followed by
     * one adding this transaction to the back stack, and finally we schedule a commit of this
     * transaction.
     */
    void addFragmentToStack() {
        mStackLevel++;

        // Instantiate a new fragment.
        Fragment newFragment = CountingFragment.newInstance(mStackLevel);

        // Add the fragment to the activity, pushing this transaction
        // on to the back stack.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.animator.fragment_slide_left_enter,
                R.animator.fragment_slide_left_exit,
                R.animator.fragment_slide_right_enter,
                R.animator.fragment_slide_right_exit);
        ft.replace(R.id.simple_fragment, newFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * Simple Fragment which displays only the instance number in its UI.
     */
    public static class CountingFragment extends Fragment {
        int mNum; // Instance number of Fragment

        /**
         * Create a new instance of CountingFragment, providing "num" as an argument. First we create
         * a new instance of <b>CountingFragment f</b>, then we create a <b>Bundle args</b>,
         * add our parameter num to the mapping of <b>args</b> using the key "num" and set the
         * arguments of <b>CountingFragment f</b> to <b>args</b>. Finally we return
         * <b>CountingFragment f</b> to our caller.
         *
         * @param num instance number to assign to this Fragment
         * @return a new instance of CountingFragment
         */
        static CountingFragment newInstance(int num) {
            CountingFragment f = new CountingFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            f.setArguments(args);

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments. First we call through
         * to our super's implementation of onCreate, then if have arguments we set our field
         * <b>int mNum</b> to the value stored in the arguments under the key "num", otherwise
         * we set it to 1.
         *
         * @param savedInstanceState we do not use anything in here
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt("num") : 1;
        }

        /**
         * The Fragment's UI is just a simple text view showing its instance number. First we inflate
         * our layout R.layout.hello_world into <b>View v</b>, set <b>View tv</b> to the
         * <b>TextView</b> in our layout with id R.id.text, set the text of <b>tv</b> to
         * the formatted String containing the value of this instance's instance number <b>mNum</b>,
         * set the background of <b>tv</b>, and finally return <b>View v</b> to the
         * caller.
         *
         * @param inflater The LayoutInflater object that can be used to inflate
         * any views in the fragment,
         * @param container If non-null, this is the parent view that the fragment's
         * UI should be attached to, used to generate the LayoutParams of the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         * from a previous saved state as given here.
         *
         * @return Return the View for the fragment's UI
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.hello_world, container, false);
            View tv = v.findViewById(R.id.text);
            ((TextView)tv).setText(String.format("%s%d", getString(R.string.fragment_number), mNum));
            tv.setBackground(getResources().getDrawable(android.R.drawable.gallery_thumb, null));
            return v;
        }
    }

}
