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

package com.example.android.apis.app
// TODO: Replace suppresssed deprecation warnings with modern solution.

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.android.apis.R

/**
 * Work fragment/thread calls `setRetainInstance(true)` in its [onCreate] callback,
 * causing it and the thread it is running to be retained when the device configuration
 * changes. Shows how you can use a [Fragment] to easily propagate state (such as
 * threads) across activity instances when an activity needs to be restarted. This is
 * a lot easier than using the raw `Activity.onRetainNonConfigurationInstance()` API.
 */
@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class FragmentRetainInstance : FragmentActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then if [savedInstanceState] is *null* (first time init) we use a handle to the
     * support `FragmentManager` for interacting with fragments associated with this activity to
     * create a `FragmentTransaction` which we use to add a new instance of our [UiFragment]
     * fragment to the activity state using android.R.id.content (the [ViewGroup] of the entire
     * content area of the Activity) as the container the fragment is to be placed in, and finally
     * we commit this `FragmentTransaction`.
     *
     * @param savedInstanceState the Framework uses this when the Activity is being recreated after
     * an orientation change but we only use it as a flag, since the first time `onCreate` is called
     * it will be *null*
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // First time init, create the UI.
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(android.R.id.content, UiFragment())
                    .commit()
        }
    }

    /**
     * This is a fragment showing UI that will be updated from work done
     * in the retained fragment.
     */
    class UiFragment : Fragment() {
        /**
         * Reference to the retained background fragment doing the work
         */
        internal var mWorkFragment: RetainedFragment? = null

        /**
         * Called to have the fragment instantiate its user interface view. First we inflate our
         * layout file R.layout.fragment_retain_instance into our [View] variable `val v`. Then we
         * initialize our [Button] variable `val button` by finding the view in `v` with the ID
         * R.id.restart ("RESTART") and set its `OnClickListener` to a lambda which will call
         * the `restart()` method of [mWorkFragment] when the [Button] is clicked, and finally we
         * return `v` to the caller.
         *
         * @param inflater           The LayoutInflater object that can be used to inflate any views
         * in the fragment,
         * @param container          If non-null, this is the parent view that the fragment's UI
         * should be attached to.  The fragment should not add the view
         * itself, but this can be used to generate the LayoutParams of
         * the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed from a
         * previous saved state as given here.
         * @return Return the View for the fragment's UI, or null.
         */
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val v = inflater.inflate(R.layout.fragment_retain_instance, container, false)

            // Watch for button clicks.
            val button = v.findViewById<Button>(R.id.restart)
            /**
             * When the "RESTART" Button is clicked we simply call the `restart()` method of
             * [mWorkFragment] to start the "work" back to the beginning again.
             *
             * Parameter: View of the Button that was clicked.
             */
            button.setOnClickListener {
                mWorkFragment!!.restart()
            }

            return v
        }

        /**
         * Called when all saved state has been restored into the view hierarchy of the fragment.
         * First we call through to our super's implementation of `onViewStateRestored`, then we
         * fetch a handle to the support `FragmentManager` for interacting with fragments associated
         * with this fragment's activity to initialize our `FragmentManager` variable `val fm`. We
         * use `fm` to search for a fragment with the tag "work" (the tag we use for our retained
         * work fragment) and save the reference in our [RetainedFragment] field [mWorkFragment].
         * If the `FragmentManager` failed to find this fragment (`findFragmentByTag` returned
         * *null*) we create a new instance of [RetainedFragment] to initialize [mWorkFragment],
         * and set its target fragment to this with a request code of 0 (this establishes a
         * caller/called relationship which the called [Fragment] can use to send results back
         * using a direct call to `getTargetFragment().onActivityResult`). Then we use `fm` to
         * create a `FragmentTransaction` which we use to add [mWorkFragment] with the tag "work",
         * and we then commit this `FragmentTransaction`.
         *
         * @param savedInstanceState If the fragment is being re-created from a previous saved
         * state, this is the state.
         */
        override fun onViewStateRestored(savedInstanceState: Bundle?) {
            super.onViewStateRestored(savedInstanceState)

            val fm = requireActivity().supportFragmentManager

            // Check to see if we have retained the worker fragment.
            mWorkFragment = fm.findFragmentByTag("work") as RetainedFragment?

            // If not retained (or first time running), we need to create it.
            if (mWorkFragment == null) {
                mWorkFragment = RetainedFragment()
                // Tell it who it is working with.
                @Suppress("DEPRECATION")
                mWorkFragment!!.setTargetFragment(this, 0)
                fm.beginTransaction().add(mWorkFragment!!, "work").commit()
            }
        }
    }

    /**
     * This is the [Fragment] implementation that will be retained across
     * activity instances. It represents some ongoing work, here a thread
     * we have that sits around incrementing a progress indicator.
     */
    class RetainedFragment : Fragment() {
        /**
         * [ProgressBar] we are incrementing
         */
        internal var mProgressBar: ProgressBar? = null
        /**
         * Current position of the ProgressBar 0-500 (maximum is set in layout xml)
         */
        internal var mPosition: Int = 0

        /**
         * Flag to indicate UI thread is ready for us to run. It is set to *true* and the `notify`
         * method of [mThread] is called in our [onActivityCreated] callback, and we block on
         * `wait()` until this occurs. It is set back to *false in [onDetach] to prevent us from
         * accessing activity state until we are reattached and in [onDestroy] in order to prompt
         * us to check the value of [mQuiting] to see if we need to exit by returning.
         */
        internal var mReady = false

        /**
         * Flag to indicate we are being destroyed and need to have the thread end itself by
         * returning, it is set to *true* in our [onDestroy] callback.
         */
        internal var mQuiting = false

        /**
         * This is the thread that will do our work.  It sits in a loop running
         * the progress up until it has reached the top, then stops and waits.
         */
        internal val mThread: Thread = object : Thread() {
            /**
             * When this Thread is `start`()'ed it loops executing two synchronized blocks in a row.
             * The first block will `wait()` until the UI is ready for it (checking to see if it needs
             * to exit when the [mQuiting] flag is set to *true*, and it will also wait when the max
             * setting of the [ProgressBar] is reached. If the UI is ready and we have not yet
             * reached the max setting of the [ProgressBar] we increment the position of the
             * [ProgressBar] (our field [mPosition]), update the value of the max setting of the
             * [ProgressBar] (our variable `max`) and set the current progress of the [ProgressBar]
             * to [mPosition]. The second synchronized block just `wait()`'s for 50 milliseconds
             * before continuing the loop.
             */
            override fun run() {
                // We'll figure the real value out later.
                var max = 10000

                // This thread runs almost forever.
                while (true) {

                    // Update our shared state with the UI.
                    synchronized(this) {
                        // Our thread is stopped if the UI is not ready
                        // or it has completed its work.
                        while (!mReady || mPosition >= max) {
                            if (mQuiting) {
                                return
                            }
                            try {
                                (this as Object).wait()
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }

                        }

                        // Now update the progress.  Note it is important that
                        // we touch the progress bar with the lock held, so it
                        // doesn't disappear on us.
                        mPosition++
                        max = mProgressBar!!.max
                        mProgressBar!!.progress = mPosition
                    }

                    // Normally we would be doing some work, but put a kludge
                    // here to pretend like we are.
                    synchronized(this) {
                        try {
                            (this as Object).wait(50)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                    }
                }
            }
        }

        /**
         * Fragment initialization. First we call through to our super's implementation of `onCreate`,
         * then we call `setRetainInstance(true)` to specify that this fragment instance is to be
         * retained across Activity re-creation (such as from a configuration change). Finally we
         * start our worker [Thread] in our field [mThread] running.
         *
         * @param savedInstanceState we do not override onSaveInstanceState to we do not use
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Tell the framework to try to keep this fragment around
            // during a configuration change.
            @Suppress("DEPRECATION")
            retainInstance = true

            // Start up the worker thread.
            mThread.start()
        }

        /**
         * Called when all saved state has been restored into the view hierarchy of the fragment.
         * This is called after [onViewCreated] and before [onStart].
         *
         * First we call through to our super's implementation of `onViewStateRestored`, then we use
         * the `getTargetFragment` method to retrieve the `UIFragment` instance which was set as our
         * target fragment using `mWorkFragment.setTargetFragment(this, 0)`, and use that
         * reference to retrieve the root view for that fragment's layout (the one returned by
         * `onCreateView(LayoutInflater, ViewGroup, Bundle)`), which we search in order to find
         * the [ProgressBar] with the ID R.id.progress_horizontal and we save a reference to
         * this [ProgressBar] in our field [mProgressBar]. Then in a block synchronized on our
         * worker thread [mThread] we set our [Boolean] field [mReady] to *true* and notify the
         * worker thread that it should stop waiting and run for a bit.
         *
         * @param savedInstanceState we do not override onSaveInstanceState to we do not use
         */
        override fun onViewStateRestored(savedInstanceState: Bundle?) {
            super.onViewStateRestored(savedInstanceState)

            // Retrieve the progress bar from the target's view hierarchy.
            @Suppress("DEPRECATION")
            mProgressBar = targetFragment!!
                .requireView()
                .findViewById(R.id.progress_horizontal)

            // We are ready for our thread to go.
            synchronized(mThread) {
                mReady = true
                (mThread as Object).notify()
            }
        }

        /**
         * This is called when the fragment is going away.  It is NOT called when the fragment is
         * being propagated between activity instances.
         *
         * In a block synchronized on our worker [Thread] field [mThread] we set our [Boolean] field
         * [mReady] to *false*, and our [Boolean] field [mQuiting] to *true*. Then we wake up our
         * worker thread [mThread] if it is currently waiting for us to release our lock on [mThread]
         * either by attempting to synchronize on "this" or by using the method `wait` inside of a
         * synchronized block of its own. The worker thread will not start running again until we
         * exit the synchronized block, allowing it to either become the owner of the lock or regain
         * ownership respectively. (If the worker thread is not currently waiting for the lock the
         * notify() is not necessary (or harmful) because eventually the worker thread will acquire
         * the lock in a synchronized block and check our flag fields anyway). Finally we call our
         * super's implementation of `onDestroy`.
         */
        override fun onDestroy() {
            // Make the thread go away.
            synchronized(mThread) {
                mReady = false
                mQuiting = true
                (mThread as Object).notify()
            }

            super.onDestroy()
        }

        /**
         * This is called right before the fragment is detached from its current activity instance.
         * Wrapped in a synchronized block we throw away our reference to the [ProgressBar] we had
         * in our [mProgressBar] field, set our [Boolean] field [mReady] to *false* and notify our
         * worker thread that it should wait until [mReady] is set to *true* before touching our
         * [mProgressBar] again. Finally we exit the synchronized block and call through to our
         * super's implementation of `onDetach()`.
         */
        override fun onDetach() {
            // This fragment is being detached from its activity.  We need
            // to make sure its thread is not going to touch any activity
            // state after returning from this function.
            synchronized(mThread) {
                mProgressBar = null
                mReady = false
                (mThread as Object).notify()
            }

            super.onDetach()
        }

        /**
         * API for our UI to restart the progress thread. This is called when the R.id.restart
         * ("Restart") [Button] is clicked. Wrapped in a block synchronized on our field [mThread]
         * we set the value of our field [mPosition] to 0, and then notify any thread waiting to
         * obtain the lock on [mThread].
         */
        fun restart() {
            synchronized(mThread) {
                mPosition = 0
                (mThread as Object).notify()
            }
        }
    }
}
