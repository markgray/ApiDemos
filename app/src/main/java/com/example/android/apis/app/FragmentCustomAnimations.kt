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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.android.apis.R

/**
 * Demonstrates the use of custom animations in a `FragmentTransaction` when
 * pushing and popping a stack.
 *
 * Uses `FragmentTransaction.setCustomAnimations` to cause animations to be used
 * when replacing one fragment with the next. The "POP" button does the same thing
 * as the back button by calling `onBackPressed`, it takes you back through the numbered
 * fragments on the stack after you "Push" them, again using the same animation.
 * [onSaveInstanceState] saves the [mStackLevel] field in an int "level" which is used when
 * the [FragmentActivity] is recreated to remember the stack level, [mStackLevel] is then used
 * to set the int argument "num" passed to the new fragment when it is created.
 * RequiresApi(Build.VERSION_CODES.LOLLIPOP)
 */
@Suppress("MemberVisibilityCanBePrivate")
@SuppressLint("DefaultLocale")
class FragmentCustomAnimations : FragmentActivity() {
    /**
     * Stack level for the next fragment
     */
    internal var mStackLevel = 1

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.fragment_stack. Next we
     * initialize our [Button] variable `var button` to the [View] in our layout with id
     * R.id.new_fragment ("Push"), and set its `onClickListener` to a lambda which calls our method
     * [addFragmentToStack] when the [Button] is clicked. We set `button` to the [View] with the ID
     * R.id.delete_fragment ("Pop") and set its `OnClickListener` to a lambda which invokes the method
     * [onBackPressed] when clicked and "finishes" the [Fragment] thereby returning to the [Fragment]
     * behind it. If our parameter [savedInstanceState] is null this is the first time we have been
     * created so we create a new instance of [CountingFragment] with the initial stack level of
     * ([mStackLevel]) of 1. Then we initialize our `FragmentTransaction` variable `ft` by using the
     * support `FragmentManager` to `begin()` a series of Fragment transactions. We add `newFragment`
     * using the view id of the `FrameLayout` in our layout file with ID R.id.simple_fragment, and
     * then commit `FragmentTransaction ft`. If [savedInstanceState] is not null on the other hand
     * we are being recreated after an orientation change, so we retrieve the value for our [Int]
     * field [mStackLevel] from [savedInstanceState] which was stored under the key "level" by our
     * callback [onSaveInstanceState].
     *
     * @param savedInstanceState if *null* it is first time, otherwise will contain the value of
     * [mStackLevel] to use stored under the key "level"
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_stack)

        // Watch for button clicks.
        var button = findViewById<Button>(R.id.new_fragment)
        button.setOnClickListener { addFragmentToStack() }

        button = findViewById(R.id.delete_fragment)
        button.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        if (savedInstanceState == null) {
            // Do first time initialization -- add initial fragment.
            val newFragment = CountingFragment.newInstance(mStackLevel)
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
     * First we call through to our super's implementation of `onSaveInstanceState`, then we insert
     * the value of [mStackLevel] into the mapping of our [Bundle] parameter [outState] using the
     * key "level".
     *
     * @param outState Bundle in which to place your saved state.
     */
    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("level", mStackLevel)
    }

    /**
     * This method adds a new fragment to the stack when the R.id.new_fragment ("Push") Button is
     * clicked. First we increment the stack level [mStackLevel], then we create a new
     * instance of [CountingFragment] with this level to initialize our [Fragment] variable
     * `newFragment`. Then we initialize our `FragmentTransaction` variable `ft` by using the
     * support fragment manager to begin a series of fragment transactions. The first transaction
     * sets specific animation resources to run for the fragments that are entering and
     * exiting in this transaction:
     *
     *  - R.animator.fragment_slide_left_enter is used as a new fragment is entering (pushed)
     *  - R.animator.fragment_slide_left_exit when a fragment is exiting (being replaced)
     *  - R.animator.fragment_slide_right_enter when a fragment is returning from a stack pop
     *  - R.animator.fragment_slide_right_exit when a fragment is leaving because it was popped.
     *
     * The next transaction replaces the old fragment with `newFragment`, followed by one adding
     * this transaction to the back stack, and finally we schedule a commit of the transaction.
     */
    internal fun addFragmentToStack() {
        mStackLevel++

        // Instantiate a new fragment.
        val newFragment = CountingFragment.newInstance(mStackLevel)

        // Add the fragment to the activity, pushing this transaction
        // on to the back stack.
        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(
            R.animator.fragment_slide_left_enter,
            R.animator.fragment_slide_left_exit,
            R.animator.fragment_slide_right_enter,
            R.animator.fragment_slide_right_exit
        )
        ft.replace(R.id.simple_fragment, newFragment)
        ft.addToBackStack(null)
        ft.commit()
    }

    /**
     * Simple Fragment which displays only the instance number in its UI.
     */
    class CountingFragment : Fragment() {
        /**
         * Instance number of *this* [Fragment]
         */
        internal var mNum: Int = 0

        /**
         * When creating, retrieve this instance's number from its arguments. First we call through
         * to our super's implementation of `onCreate`, then if we have arguments we set our [Int]
         * field [mNum] to the value stored in the arguments under the key "num", otherwise we set
         * it to 1.
         *
         * @param savedInstanceState we do not use anything in here
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            mNum = if (arguments != null) requireArguments().getInt("num") else 1
        }

        /**
         * The Fragment's UI is just a simple text view showing its instance number. First we inflate
         * our layout R.layout.hello_world into our [View] variable `val v`, set our [View] variable
         * `val tv` to the [TextView] in our layout with id R.id.text, set the text of `tv` to the
         * formatted String containing the value of this instance's instance number [mNum], set the
         * background of `tv`, and finally return `v` to the caller.
         *
         * @param inflater The [LayoutInflater] object that can be used to inflate
         * any views in the fragment,
         * @param container If non-null, this is the parent view that the fragment's
         * UI should be attached to, used to generate the LayoutParams of the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         * from a previous saved state as given here.
         *
         * @return Return the View for the fragment's UI
         */
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val v = inflater.inflate(R.layout.hello_world, container, false)
            val tv = v.findViewById<View>(R.id.text)
            (tv as TextView).text = String.format("%s%d", getString(R.string.fragment_number), mNum)
            tv.background = ResourcesCompat.getDrawable(
                resources,
                android.R.drawable.gallery_thumb,
                null
            )
            return v
        }

        /**
         * Our static factory method.
         */
        companion object {

            /**
             * Create a new instance of CountingFragment, providing "num" as an argument. First we create
             * a new instance of **CountingFragment f**, then we create a **Bundle args**,
             * add our parameter num to the mapping of **args** using the key "num" and set the
             * arguments of **CountingFragment f** to **args**. Finally we return
             * **CountingFragment f** to our caller.
             *
             * @param num instance number to assign to this Fragment
             * @return a new instance of CountingFragment
             */
            internal fun newInstance(num: Int): CountingFragment {
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
