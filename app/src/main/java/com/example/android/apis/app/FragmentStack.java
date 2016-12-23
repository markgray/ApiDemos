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
 * Shows how to push and pop fragments using the system backstack.
 * FragmentTransaction.addToBackStack() adds the fragement to the backstack, and
 * getFragmentManager().popBackStack() (or simply pressing "back" button) goes back
 * one fragment, using a fancy animation for push and pop. (Not really visible on
 * Nexus 6 Marshmallow, but striking on Excite 10.)
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class FragmentStack extends Activity {
    int mStackLevel = 1; // stack level of next {@code CountingFragment} to add to back stack

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.fragment_stack. Next we
     * find the {@code Button} R.id.new_fragment ("PUSH") and set its {@code OnClickListener} to an
     * anonymous class which calls our method {@code addFragmentToStack()} when the Button is
     * clicked, and we locate the {@code Button} R.id.delete_fragment ("POP") and set its
     * {@code OnClickListener} to an anonymous class which uses the FragmentManager for interacting
     * with fragments associated with this activity Pop the top state off the back stack. This
     * function is asynchronous -- it enqueues the request to pop, but the action will not be
     * performed until the application returns to its event loop.
     * <p>
     * Then if our parameter {@code Bundle savedInstanceState} is null we need to do first time
     * initialization, so we create {@code Fragment newFragment} by calling {@code CountingFragment}'s
     * {@code newInstance} method, use the FragmentManager for interacting with fragments associated
     * with this activity to start {@code FragmentTransaction ft} which we use to add the fragment
     * {@code newFragment} to the activity state and commit the FragmentTransaction.
     * <p>
     * If our parameter {@code Bundle savedInstanceState} is not null, we are being recreated, so
     * we retrieve the value of our field {@code int mStackLevel} (which was saved by our override of
     * onSaveInstanceState using the key "level") from {@code savedInstanceState}.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     *                           down then this Bundle contains the data it most recently supplied
     *                           in {@link #onSaveInstanceState}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_stack);

        // Watch for button clicks.
        Button button = (Button) findViewById(R.id.new_fragment);
        button.setOnClickListener(new OnClickListener() {
            /**
             * When the "PUSH" Button is clicked we simply call our method {@code addFragmentToStack()}
             * which will create a new instance of {@code CountingFragment}, replace the current
             * Fragment with it and add the whole {@code FragmentTransaction} used to do this to the
             * back stack.
             *
             * @param v View of Button that was clicked
             */
            @Override
            public void onClick(View v) {
                addFragmentToStack();
            }
        });
        button = (Button) findViewById(R.id.delete_fragment);
        button.setOnClickListener(new OnClickListener() {
            /**
             * When the "POP" Button is clicked we simply use he FragmentManager for interacting
             * with fragments associated with this activity to Pop the top state off the back stack.
             *
             * @param v View of Button that was clicked.
             */
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
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
     * Called to retrieve per-instance state from an activity before being killed so that the state
     * can be restored in {@link #onCreate} or {@link #onRestoreInstanceState} (the {@link Bundle}
     * populated by this method will be passed to both).
     * <p>
     * First we call through to our super's implementation of {@code onSaveInstanceState}, then we
     * insert the value of our field {@code int mStackLevel} into the mapping of our parameter
     * {@code Bundle outState}, using the key "level".
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("level", mStackLevel);
    }

    /**
     * Create a new instance of {@code CountingFragment} and use it to replace the current Fragment
     * while adding the whole {@code FragmentTransaction} used to do this to the back stack so that
     * it may later be reversed by calling {@code FragmentManager.popBackStack()}. First we increment
     * our field {@code int mStackLevel} (stack level of next {@code CountingFragment} to add to back
     * stack), then we create a new instance of {@code CountingFragment} {@code Fragment newFragment}
     * using {@code mStackLevel} as its level. We use the FragmentManager for interacting with
     * fragments associated with this activity to start a {@code FragmentTransaction ft} which we use
     * to replace the current fragment occupying view R.id.simple_fragment with {@code newFragment},
     * specify a transition of TRANSIT_FRAGMENT_OPEN for the transaction, add the transaction to the
     * back stack and finally commit the {@code FragmentTransaction}.
     */
    void addFragmentToStack() {
        mStackLevel++;

        // Instantiate a new fragment.
        Fragment newFragment = CountingFragment.newInstance(mStackLevel);

        // Add the fragment to the activity, pushing this transaction
        // on to the back stack.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.simple_fragment, newFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * This is a minimalist Fragment whose only UI consists of a {@code TextView} displaying the
     * formatted stack level number passed to its factory method {@code newInstance}.
     */
    public static class CountingFragment extends Fragment {
        int mNum;

        /**
         * Create a new instance of CountingFragment, providing "num" as an argument. First we create
         * a new instance of {@code CountingFragment f}, then we create a {@code Bundle args} and
         * then we insert our parameter {@code int num} into the mapping of this Bundle under the key
         * "num", we set the arguments of {@code CountingFragment f} to {@code args} and return the
         * configured {@code CountingFragment} instance {@code f} to the caller.
         *
         * @param num Stack level number to pass as argument to new instance of
         *            {@code CountingFragment} we create.
         * @return New instance of {@code CountingFragment} with the argument
         * Bundle containing our parameter {@code int num} stored using the key
         * "num"
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
         * to our super's implementation of onCreate, then if arguments have been passed us using
         * {@code setArguments} we retrieve the int stored in the argument {@code Bundle} under the
         * key "num" to set our field {@code int mNum}, and if no arguments were passed we default
         * to 1.
         *
         * @param savedInstanceState since we do not override onSaveInstanceState we do not use
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt("num") : 1;
        }

        /**
         * Called to have the fragment instantiate its user interface view. The Fragment's UI is
         * just a simple text view showing its instance number.
         * <p>
         * First we use our parameter {@code LayoutInflater inflater} to inflate our layout file
         * R.layout.hello_world into {@code View v}, locate the {@code TextView} R.id.text in {@code v}
         * to set {@code View tv}, set the text of {@code tv} to a formatted String containing our
         * field {@code int mNum} (the stack level we represent), and set the background of {@code tv}
         * to the {@code Drawable} android.R.drawable.gallery_thumb. Finally we return {@code View v}
         * to our caller.
         *
         * @param inflater           The LayoutInflater object that can be used to inflate
         *                           any views in the fragment,
         * @param container          If non-null, this is the parent view that the fragment's
         *                           UI should be attached to.  The fragment should not add the view itself,
         *                           but this can be used to generate the LayoutParams of the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         *                           from a previous saved state as given here.
         * @return Return the View for the fragment's UI, or null.
         */
        @SuppressWarnings("deprecation")
        @SuppressLint("DefaultLocale")
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.hello_world, container, false);
            View tv = v.findViewById(R.id.text);
            ((TextView) tv).setText(String.format("%s%d", getString(R.string.fragment_num), mNum));
            tv.setBackground(getResources().getDrawable(android.R.drawable.gallery_thumb));
            return v;
        }
    }

}
