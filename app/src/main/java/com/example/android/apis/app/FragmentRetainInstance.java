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
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

/**
 * Work fragment/thread calls setRetainInstance(true) in its onCreate callback,
 * causing it and the thread it is running to be retained when the device configuration
 * changes. Shows how you can use a Fragment to easily propagate state (such as
 * threads) across activity instances when an activity needs to be restarted. This is
 * a lot easier than using the raw Activity.onRetainNonConfigurationInstance() API.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentRetainInstance extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then if <b>savedInstanceState</b> is null (first time init) we use a handle to the
     * FragmentManager for interacting with fragments associated with this activity to create a
     * <b>FragmentTransaction</b> which we use to add a new instance of <b>UiFragment</b> fragment
     * to the activity state using android.R.id.content (the ViewGroup of the entire  content area
     * of the Activity) as the container the fragment is to be placed in, and finally we commit this
     * FragmentTransaction.
     *
     * @param savedInstanceState the Framework uses this when the Activity is being recreated after
     *                           an orientation change but we only use it as a flag since the first
     *                           time onCreate is called it will be null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // First time init, create the UI.
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, new UiFragment())
                    .commit();
        }
    }

    /**
     * This is a fragment showing UI that will be updated from work done
     * in the retained fragment.
     */
    public static class UiFragment extends Fragment {
        RetainedFragment mWorkFragment; // Reference to the retained background fragment doing the work

        /**
         * Called to have the fragment instantiate its user interface view. First we inflate our
         * layout file R.layout.fragment_retain_instance into <b>View v</b>. Then we locate the
         * <b>Button button</b> in <b>v</b> with ID R.id.restart ("RESTART") and set its OnClickListener
         * to an anonymous class which will call <b>mWorkFragment.restart()</b> when the Button is
         * clicked, and finally we return <b>v</b> to the caller.
         *
         * @param inflater           The LayoutInflater object that can be used to inflate any views
         *                           in the fragment,
         * @param container          If non-null, this is the parent view that the fragment's UI
         *                           should be attached to.  The fragment should not add the view
         *                           itself, but this can be used to generate the LayoutParams of
         *                           the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed from a
         *                           previous saved state as given here.
         * @return Return the View for the fragment's UI, or null.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_retain_instance, container, false);

            // Watch for button clicks.
            Button button = (Button) v.findViewById(R.id.restart);
            button.setOnClickListener(new OnClickListener() {
                /**
                 * When the "RESTART" Button is clicked we simply call the method <b>mWorkFragment.restart()</b>
                 * to start the "work" back to the beginning again.
                 *
                 * @param v View of the Button that was clicked.
                 */
                @Override
                public void onClick(View v) {
                    mWorkFragment.restart();
                }
            });

            return v;
        }

        /**
         * Called when the fragment's activity has been created and this
         * fragment's view hierarchy instantiated.  It can be used to do final
         * initialization once these pieces are in place, such as retrieving
         * views or restoring state.  It is also useful for fragments that use
         * {@link #setRetainInstance(boolean)} to retain their instance,
         * as this callback tells the fragment when it is fully associated with
         * the new activity instance.  This is called after {@link #onCreateView}
         * and before {@link #onViewStateRestored(Bundle)}.
         *
         * First we call through to our super's implementation of onActivityCreated, then we fetch a
         * handle to the FragmentManager for interacting with fragments associated with this fragment's
         * activity into {@code FragmentManager fm}. We use <b>fm</b> to search for a fragment with
         * the tag "work" (the tag we use for our retained work fragment) and save the reference in
         * our field {@code RetainedFragment mWorkFragment}. If the FragmentManager failed to find
         * this fragment (findFragmentByTag returned null) we create a new {@code RetainedFragment}
         * for {@code RetainedFragment mWorkFragment}, and set its target fragment to this with a
         * request code of 0 (this establishes a caller/called relationship which the called Fragment
         * can use to send results back using a direct call to getTargetFragment().onActivityResult.)
         * Then we use {@code FragmentManager fm} to create a {@code FragmentTransaction} which we
         * use to add {@code mWorkFragment} with the tag "work", and we then commit the
         * FragmentTransaction.
         *
         * @param savedInstanceState If the fragment is being re-created from
         *                           a previous saved state, this is the state.
         */
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            FragmentManager fm = getFragmentManager();

            // Check to see if we have retained the worker fragment.
            mWorkFragment = (RetainedFragment) fm.findFragmentByTag("work");

            // If not retained (or first time running), we need to create it.
            if (mWorkFragment == null) {
                mWorkFragment = new RetainedFragment();
                // Tell it who it is working with.
                mWorkFragment.setTargetFragment(this, 0);
                fm.beginTransaction().add(mWorkFragment, "work").commit();
            }
        }

    }

    /**
     * This is the Fragment implementation that will be retained across
     * activity instances.  It represents some ongoing work, here a thread
     * we have that sits around incrementing a progress indicator.
     */
    public static class RetainedFragment extends Fragment {
        ProgressBar mProgressBar; // ProgressBar we are incrementing
        int mPosition; // Current position of the ProgressBar 0-500 (maximum is set in layout xml)

        /**
         * Flag to indicate UI thread is ready for us to run. It is set to true and mThread.notify()
         * is called in our {@code onActivityCreated} callback, and we block on {@code wait()} until
         * this occurs. It is set back to false in {@code onDetach} to prevent us from accessing
         * activity state until we are reattached and in {@code onDestroy} in order to prompt us to
         * check the value of {@code mQuiting} to see if we need to exit by returning.
         */
        boolean mReady = false;

        /**
         * Flag to indicate we are being destroyed and need to end the thread by returning, it is
         * set to true in our {@code onDestroy} callback.
         */
        boolean mQuiting = false;

        /**
         * This is the thread that will do our work.  It sits in a loop running
         * the progress up until it has reached the top, then stops and waits.
         */
        final Thread mThread = new Thread() {
            /**
             * When this Thread is start()'ed it loops executing two synchronized blocks in a row.
             * <p>
             * The first block will wait() until the UI is ready for it (checking to see if it needs
             * to exit when the mQuiting flag is set to true, and it will also wait when the max
             * setting of the ProgressBar is reached. If the UI is ready and we have not yet reached
             * the max setting of the ProgressBar we increment the position of the ProgressBar
             * ({@code mPosition}), update the value of the max setting of the ProgressBar ({@code max})
             * and set the current progress of the ProgressBar to {@code mPosition}.
             * <p>
             * The second block just wait()'s for 50 milliseconds before continuing the loop.
             */
            @Override
            public void run() {
                // We'll figure the real value out later.
                int max = 10000;

                // This thread runs almost forever.
                while (true) {

                    // Update our shared state with the UI.
                    synchronized (this) {
                        // Our thread is stopped if the UI is not ready
                        // or it has completed its work.
                        while (!mReady || mPosition >= max) {
                            if (mQuiting) {
                                return;
                            }
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        // Now update the progress.  Note it is important that
                        // we touch the progress bar with the lock held, so it
                        // doesn't disappear on us.
                        mPosition++;
                        max = mProgressBar.getMax();
                        mProgressBar.setProgress(mPosition);
                    }

                    // Normally we would be doing some work, but put a kludge
                    // here to pretend like we are.
                    synchronized (this) {
                        try {
                            wait(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        /**
         * Fragment initialization. First we call through to our super's implementation of onCreate,
         * then we call {@code setRetainInstance(true)} to specify that this fragment instance is to
         * be retained across Activity re-creation (such as from a configuration change).
         *
         * Finally we start our worker thread {@code Thread mThread} running.
         *
         * @param savedInstanceState we do not override onSaveInstanceState to we do not use
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Tell the framework to try to keep this fragment around
            // during a configuration change.
            setRetainInstance(true);

            // Start up the worker thread.
            mThread.start();
        }

        /**
         * This is called when the Fragment's Activity is ready to go, after its content view has
         * been installed; it is called both after the initial fragment creation and after the
         * fragment is re-attached to a new activity.
         *
         * First we call through to our super's implementation of onActivityCreated, then we use
         * {@code getTargetFragment} to retrieve the UIFragment instance which was set as our
         * target fragment using {@code mWorkFragment.setTargetFragment(this, 0)}, and use that
         * reference to retrieve the root view for that fragment's layout (the one returned by
         * onCreateView(LayoutInflater, ViewGroup, Bundle)), which we search in order to find
         * the {@code ProgressBar} with the ID R.id.progress_horizontal and we save a reference to
         * this ProgressBar in our field {@code ProgressBar mProgressBar}. Then in a block
         * synchronized on our worker thread {@code mThread} we set our field {@code boolean mReady}
         * to true and notify the worker thread that it should stop waiting and run for a bit.
         *
         * @param savedInstanceState we do not override onSaveInstanceState to we do not use
         */
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // Retrieve the progress bar from the target's view hierarchy.
            //noinspection ConstantConditions
            mProgressBar = (ProgressBar) getTargetFragment()
                    .getView()
                    .findViewById(R.id.progress_horizontal);

            // We are ready for our thread to go.
            synchronized (mThread) {
                mReady = true;
                mThread.notify();
            }
        }

        /**
         * This is called when the fragment is going away.  It is NOT called
         * when the fragment is being propagated between activity instances.
         */
        @Override
        public void onDestroy() {
            // Make the thread go away.
            synchronized (mThread) {
                mReady = false;
                mQuiting = true;
                mThread.notify();
            }

            super.onDestroy();
        }

        /**
         * This is called right before the fragment is detached from its
         * current activity instance.
         */
        @Override
        public void onDetach() {
            // This fragment is being detached from its activity.  We need
            // to make sure its thread is not going to touch any activity
            // state after returning from this function.
            synchronized (mThread) {
                mProgressBar = null;
                mReady = false;
                mThread.notify();
            }

            super.onDetach();
        }

        /**
         * API for our UI to restart the progress thread.
         */
        public void restart() {
            synchronized (mThread) {
                mPosition = 0;
                mThread.notify();
            }
        }
    }
}
