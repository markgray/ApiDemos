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

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.example.android.apis.R
import com.example.android.apis.app.FragmentStack.CountingFragment.Companion.newInstance

/**
 * Shows how to push and pop fragments using the system backstack.
 * `FragmentTransaction.addToBackStack()` adds the fragment to the backstack, and
 * `getSupportFragmentManager().popBackStack()` (or simply pressing "back" button) goes back
 * one fragment, using a fancy animation for push and pop. (Not really visible on
 * Nexus 6 Marshmallow, but striking on Excite 10.)
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class FragmentStack : FragmentActivity() {
    /**
     * stack level of next [CountingFragment] to add to back stack
     */
    internal var mStackLevel = 1

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.fragment_stack. Next we
     * find the [Button] with ID R.id.new_fragment ("PUSH") to initialize our variable `var button`
     * and set its `OnClickListener` to a lambda which calls our method [addFragmentToStack] when
     * the [Button] is clicked, and then set `button` to the [Button] with ID R.id.delete_fragment
     * ("POP") and set its `OnClickListener` to a lambda which uses the support `FragmentManager`
     * for interacting with fragments associated with this activity Pop the top state off the back
     * stack. This function is asynchronous -- it enqueues the request to pop, but the action will
     * not be performed until the application returns to its event loop. Then if our [Bundle]
     * parameter [savedInstanceState] is *null* we need to do first time initialization, so we
     * initialize our [CountingFragment] variable `val newFragment` by calling [CountingFragment]'s
     * `newInstance` method, use the support FragmentManager for interacting with fragments
     * associated with this activity to begin a [FragmentTransaction] which we use to initialize our
     * variable `val ft`, and we then use `ft` add the fragment `newFragment` to the activity state
     * and commit the [FragmentTransaction]. If our [Bundle] parameter [savedInstanceState] is not
     * *null*, we are being recreated, so we retrieve the value for our [mStackLevel] field which
     * was saved by our override of [onSaveInstanceState] using the key "level" from
     * [savedInstanceState].
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this [Bundle] contains the data it most recently supplied in [onSaveInstanceState],
     * otherwise it is *null*.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_stack)

        // Watch for button clicks.
        var button = findViewById<Button>(R.id.new_fragment)
        /**
         * When the "PUSH" Button is clicked we simply call our method `addFragmentToStack()`
         * which will create a new instance of `CountingFragment`, replace the current
         * Fragment with it and add the whole [FragmentTransaction] used to do this to the
         * back stack.
         *
         * Parameter: View of Button that was clicked
         */
        button.setOnClickListener {
            addFragmentToStack()
        }
        button = findViewById(R.id.delete_fragment)
        /**
         * When the "POP" Button is clicked we simply use he FragmentManager for interacting
         * with fragments associated with this activity to Pop the top state off the back stack.
         *
         * Parameter: View of Button that was clicked.
         */
        button.setOnClickListener {
            supportFragmentManager.popBackStack()
        }

        if (savedInstanceState == null) {
            // Do first time initialization -- add initial fragment.
            val newFragment = newInstance(mStackLevel)
            val ft = supportFragmentManager.beginTransaction()
            ft.add(R.id.simple_fragment, newFragment).commit()
        } else {
            mStackLevel = savedInstanceState.getInt("level")
        }
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed so that the state
     * can be restored in [onCreate] or [onRestoreInstanceState] (the [Bundle] populated by this
     * method will be passed to both).
     *
     * First we call through to our super's implementation of `onSaveInstanceState`, then we
     * insert the value of our [Int] field [mStackLevel] into the mapping of our [Bundle] parameter
     * [outState], using the key "level".
     *
     * @param outState [Bundle] in which to place your saved state.
     */
    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("level", mStackLevel)
    }

    /**
     * Create a new instance of [CountingFragment] and use it to replace the current [Fragment]
     * while adding the whole [FragmentTransaction] used to do this to the back stack so that it
     * may later be reversed by calling the `popBackStack` method of the support `FragmentManager`.
     * First we increment our field [mStackLevel] (stack level of next [CountingFragment] to add to
     * the back stack), then we create a new instance of [CountingFragment] to initialize our
     * variable `val newFragment` using [mStackLevel] as its level. We use the support
     * `FragmentManager` for interacting with fragments associated with this activity to start a
     * [FragmentTransaction] to initialize our variable `val ft` which we then use to replace the
     * current fragment occupying view R.id.simple_fragment with `newFragment`, specify a transition
     * of TRANSIT_FRAGMENT_OPEN for the transaction, add the transaction to the back stack and
     * finally commit the [FragmentTransaction].
     */
    internal fun addFragmentToStack() {
        mStackLevel++

        // Instantiate a new fragment.
        val newFragment = newInstance(mStackLevel)

        // Add the fragment to the activity, pushing this transaction
        // on to the back stack.
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.simple_fragment, newFragment)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        ft.addToBackStack(null)
        ft.commit()
    }

    /**
     * This is a minimalist [Fragment] whose only UI consists of a [TextView] displaying the
     * formatted stack level number passed to its factory method [newInstance].
     */
    class CountingFragment : Fragment() {
        /**
         * The stack level number we are to display.
         */
        internal var mNum: Int = 0

        /**
         * When creating, retrieve this instance's number from its arguments. First we call through
         * to our super's implementation of `onCreate`, then if arguments have been passed us using
         * `setArguments` we retrieve the [Int] stored in the argument [Bundle] under the key "num"
         * to set our [Int] field [mNum], and if no arguments were passed we default to 1.
         *
         * @param savedInstanceState since we do not override onSaveInstanceState we do not use
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            mNum = if (arguments != null) arguments!!.getInt("num") else 1
        }

        /**
         * Called to have the fragment instantiate its user interface view. The Fragment's UI is
         * just a simple text view showing its instance number. First we use our [LayoutInflater]
         * parameter [inflater] to inflate our layout file R.layout.hello_world into a [View] to
         * initialize our varible `val v`, locate the [TextView] with ID R.id.text in `v`
         * to to initialize our  `val tv`, set the text of `tv` to a formatted String containing our
         * field [mNum] (the stack level we represent), and set the background of `tv` to the
         * system `Drawable` android.R.drawable.gallery_thumb. Finally we return `v` to our caller.
         *
         * @param inflater The LayoutInflater object that can be used to inflate  any views
         * @param container If non-null, this is the parent view that the fragment's UI will be
         * attached to. The fragment should not add the view itself, but this can be used to
         * generate the LayoutParams of the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         * from a previous saved state as given here.
         * @return Return the [View] for the fragment's UI, or null.
         */
        @SuppressLint("DefaultLocale")
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val v = inflater.inflate(R.layout.hello_world, container, false)
            val tv = v.findViewById<View>(R.id.text)
            (tv as TextView).text = String.format("%s%d", getString(R.string.fragment_num), mNum)
            @Suppress("DEPRECATION")
            tv.setBackground(
                ResourcesCompat.getDrawable(resources, android.R.drawable.gallery_thumb, null)
            )
            return v
        }

        /**
         * Our static method.
         */
        companion object {
            /**
             * Create a new instance of [CountingFragment], providing "num" as an argument. First
             * we create a new instance of [CountingFragment] to initialize our variable `val f`,
             * then we create a [Bundle] to initialize our variable `val args` and then we insert
             * our [Int] parameter [num] into the mapping of this [Bundle] under the key "num", we
             * set the arguments of `f` to `args` and return the configured [CountingFragment]
             * instance `f` to the caller.
             *
             * @param num Stack level number to pass as argument to new instance of
             * [CountingFragment] we create.
             * @return New instance of [CountingFragment] with the argument [Bundle] containing our
             * parameter [num] stored under the key "num"
             */
            fun newInstance(num: Int): CountingFragment {
                val f = CountingFragment()

                // Supply num input as an argument.
                val args = Bundle()
                args.putInt("num", num)
                f.arguments = args

                return f
            }
        }
    }

}
