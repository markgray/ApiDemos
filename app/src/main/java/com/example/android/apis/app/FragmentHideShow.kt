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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.android.apis.R
import java.lang.RuntimeException

/**
 * Demonstration of hiding and showing fragments.
 */
@Suppress("MemberVisibilityCanBePrivate")
@SuppressLint("SetTextI18n")
class FragmentHideShow : FragmentActivity() {

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file R.layout.fragment_hide_show. We initialize
     * our [FragmentManager] variable `val fm` with the support [FragmentManager] for interacting
     * with fragments associated with this activity, and declare our [Fragment] variables
     * `val fragment1` and `val fragment2`. If our [Bundle] parameter [savedInstanceState] is not
     * *null* we are being recreated after an orientation change and our fragments will have been
     * recreated by the system so we initialize `fragment1` by using `fm` to find the fragment with
     * ID R.id.fragment1, and `fragment2` by using `fm` to find the fragment with ID R.id.fragment2.
     * If [savedInstanceState] is *null* this is the first time we have started so we use `fm` to
     * begin a [FragmentTransaction] to initialize our [FragmentTransaction] variable `val ft`, set
     * `fragment1` to a new instance of [FirstFragment], use `ft` to add `fragment1` to the View in
     * our content view with ID R.id.fragment1, and set `fragment2` to a new instance of
     * [SecondFragment], and use `ft` to add `fragment2` to the View in our content view with ID
     * R.id.fragment2. We then commit the `ft` [FragmentTransaction]. We call our method
     * [addShowHideListener] to add an `OnClickListener` to the button with id R.id.frag1hide which
     * will toggle the show/hide state of `fragment1` when it is clicked and set the text of the
     * button to "Hide" or "Show" whichever is then appropriate. We initialize our [Button] variable
     * `val button1` by finding the view with id R.id.frag1hide and set its text to "Show" if the
     * `isHidden` method of `fragment1` returns true (fragment is hidden) or to "Hide" if it is
     * false (fragment is not hidden). We then [addShowHideListener] to add an `OnClickListener` to
     * the button with id R.id.frag2hide which will toggle the show/hide state of `fragment2` when
     * it is clicked and set the text of the button to "Hide" or "Show" whichever is then appropriate.
     * We initialize our [Button] variable `val button2` by finding the view with id R.id.frag2hide
     * and set its text to "Show" if the `isHidden` method of `fragment2` returns true (fragment is
     * hidden) or to "Hide" if it is false (fragment is not hidden).
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_hide_show)

        val fm: FragmentManager = supportFragmentManager
        val fragment1: Fragment?
        val fragment2: Fragment?
        if (savedInstanceState != null) {
            fragment1 = fm.findFragmentById(R.id.fragment1)
                ?: throw RuntimeException("fragment1 is null")
            fragment2 = fm.findFragmentById(R.id.fragment2)
                ?: throw RuntimeException("fragment2 is null")
        } else {
            val ft: FragmentTransaction = fm.beginTransaction()
            fragment1 = FirstFragment()
            ft.add(R.id.fragment1, fragment1)
            fragment2 = SecondFragment()
            ft.add(R.id.fragment2, fragment2)
            ft.commit()
        }

        // The content view now embeds our two fragments, now we attach their "hide" button.
        addShowHideListener(R.id.frag1hide, fragment1)
        val button1 = findViewById<Button>(R.id.frag1hide)
        button1.text = if (fragment1.isHidden) "Show" else "Hide"

        addShowHideListener(R.id.frag2hide, fragment2)
        val button2 = findViewById<Button>(R.id.frag2hide)
        button2.text = if (fragment2.isHidden) "Show" else "Hide"
    }

    /**
     * Locates the button whose resource id is given by our [Int] parameter [buttonId] and sets its
     * `OnClickListener` to an a lambda which will toggle the hide/show state of our [Fragment]
     * parameter [fragment].
     *
     * @param buttonId resource id of the button we are to add our `OnClickListener` to
     * @param fragment `Fragment` whose hide/show state is to be toggled by the button.
     */
    internal fun addShowHideListener(buttonId: Int, fragment: Fragment) {
        val button = findViewById<Button>(buttonId)
        /**
         * Called when the `View` we are the `OnClickListener` for is clicked. We initialize our
         * [FragmentTransaction] variable `val ft` by using the the support [FragmentManager] for
         * interacting with fragments associated with this activity to begin a transaction. We call
         * the `setCustomAnimations` method of `ft` to specify the animation resources to run for
         * the fragments that are entering and exiting in this transaction to be
         * android.R.animator.fade_in and android.R.animator.fade_out (these are `objectAnimator`
         * objects which manipulate the alpha value of their views). If the `isHidden` method
         * of [fragment] returns true (the fragment is currently hidden) we call the `show`
         * method of `ft` to show the fragment and set the text of our `button` to "Hide".
         * If it returns false (the fragment is currently shown) we call the `hide` method of
         * `ft` to hide the fragment and set the text of our `button` to "Show". Finally
         * we commit [FragmentTransaction] `ft`.
         *
         * Parameter: `View` that was clicked.
         */
        button.setOnClickListener {
            val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
            ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
            if (fragment.isHidden) {
                ft.show(fragment)
                button.text = "Hide"
            } else {
                ft.hide(fragment)
                button.text = "Show"
            }
            ft.commit()
        }
    }

    /**
     * First fragment in our content view, it is added to the `FrameLayout` in our layout file
     * layout/fragment_hide_show.xml, with the ID R.id.fragment1. It uses an `onSaveInstanceState`
     * override to save the text of the `EditText` in its layout file, then restores the text in
     * its `onViewCreated` override.
     */
    class FirstFragment : Fragment() {
        /**
         * The `EditText` in our layout file with id R.id.saved whose contents we save in the
         * `Bundle` passed to our `onSaveInstanceState` override, then restore in our
         * `onViewCreated` override.
         */
        internal lateinit var mTextView: TextView

        /**
         * Called to have the fragment instantiate its user interface view. We use our [LayoutInflater]
         * parameter [inflater] to inflate our layout file R.layout.labeled_text_edit using our
         * [ViewGroup] parameter [container] for its layout parameters without attaching to it, and
         * return the inflated [View] to the caller.
         *
         * @param inflater The LayoutInflater object that can be used to inflate any views.
         * @param container If non-null, this is the parent view that the fragment's UI will be
         * attached to. The fragment should not add the view itself, but this can be used to generate
         * the LayoutParams of the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         * from a previous saved state as given here.
         *
         * @return Return the View for the fragment's UI, or null.
         */
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.labeled_text_edit, container, false)
        }

        /**
         * Called immediately after [onCreateView]  has returned, but before any saved state has been
         * restored in to the view. This gives subclasses a chance to initialize themselves once they
         * know their view hierarchy has been completely created. The fragment's view hierarchy is
         * not however attached to its parent at this point.
         *
         * First we call our super's implementation of `onViewCreated`, then we initialize our [View]
         * variable `val tv` by finding the view in our [View] parameter [view] with the ID R.id.msg
         * and set its text to: "The fragment saves and restores this text." We then initialize our
         * [TextView] field [mTextView] by finding the view in our [View] parameter [view] with the
         * ID R.id.saved, and if our [Bundle] parameter [savedInstanceState] is not *null* we set the
         * text of [mTextView] to the [String] stored in [savedInstanceState] under the key "text"
         * by our [onSaveInstanceState] override.
         *
         * @param view               The View returned by [onCreateView].
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         */
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val tv = view.findViewById<View>(R.id.msg)
            (tv as TextView).text = "The fragment saves and restores this text."

            // Retrieve the text editor, and restore the last saved state if needed.
            mTextView = view.findViewById(R.id.saved)
            if (savedInstanceState != null) {
                mTextView.text = savedInstanceState.getCharSequence("text")
            }
        }

        /**
         * Called to ask the fragment to save its current dynamic state, so it can later be reconstructed
         * in a new instance if its process is restarted. First we call our super's implementation of
         * `onSaveInstanceState` then we retrieve the text of our [TextView] field [mTextView] and
         * store that `CharSequence` in our [Bundle] parameter [outState] under the key "text".
         *
         * @param outState Bundle in which to place your saved state.
         */
        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)

            // Remember the current text, to restore if we later restart.
            outState.putCharSequence("text", mTextView.text)
        }
    }

    /**
     * Second fragment in our content view, it is added to the `FrameLayout` in our layout file
     * layout/fragment_hide_show.xml, with the ID R.id.fragment2. It calls the `setSaveEnabled(true)`
     * method of the `EditText` in its layout to have it save and restore its text.
     */
    class SecondFragment : Fragment() {

        /**
         * Called to have the fragment instantiate its user interface view. We use our [LayoutInflater]
         * parameter [inflater] to inflate our layout file R.layout.labeled_text_edit using our
         * [ViewGroup] parameter [container] for its layout parameters without attaching to it, and
         * return the inflated [View] to the caller.
         *
         * @param inflater The LayoutInflater object that can be used to inflate any views.
         * @param container If non-null, this is the parent view that the fragment's UI will be
         * attached to. The fragment should not add the view itself, but this can be used to generate
         * the LayoutParams of the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         * from a previous saved state as given here.
         *
         * @return Return the View for the fragment's UI, or null.
         */
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(
                R.layout.labeled_text_edit,
                container,
                false
            )
        }

        /**
         * Called immediately after [onCreateView]  has returned, but before any saved state has been
         * restored in to the view. This gives subclasses a chance to initialize themselves once they
         * know their view hierarchy has been completely created. The fragment's view hierarchy is
         * not however attached to its parent at this point.
         *
         * First we call our super's implementation of `onViewCreated`, then we initialize our [View]
         * variable `val tv` by finding the view in our [View] parameter [view] with the ID R.id.msg
         * and set its text to: "The fragment saves and restores this text." We then find the [View]
         * in our [View] parameter [view] with the ID R.id.saved and set its `isSaveEnabled` attribute
         * to *true* so that it will save and restore its state.
         *
         * @param view               The View returned by [onCreateView].
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         */
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val tv = view.findViewById<View>(R.id.msg)
            (tv as TextView).text = "The TextView saves and restores this text."

            // Retrieve the text editor and tell it to save and restore its state.
            // Note that you will often set this in the layout XML, but since
            // we are sharing our layout with the other fragment we will customize
            // it here.
            view.findViewById<View>(R.id.saved).isSaveEnabled = true
        }
    }
}
