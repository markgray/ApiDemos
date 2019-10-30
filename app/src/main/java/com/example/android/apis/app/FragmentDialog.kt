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

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.example.android.apis.R

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
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class FragmentDialog : AppCompatActivity() {
    internal var mStackLevel = 0 // Level used to choose style of dialog (and stack level)

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.fragment_dialog. Next we
     * locate the TextView in our layout (R.id.text) and set its text to the instructions for this
     * demo (R.string.dialog_fragment_example_instructions):
     *
     *
     * Example of displaying dialogs with a DialogFragment.
     * Press the show button below to see the first dialog;
     * pressing successive show buttons will display other
     * dialog styles as a stack, with dismissing or back
     * going to the previous dialog.
     *
     *
     * We locate Button button in our layout (R.id.show "SHOW") and set its OnClickListener to an
     * anonymous class which calls our method showDialog() when the Button is clicked. Finally if
     * savedInstanceState is not null (we are being recreated after an orientation change or other
     * reason) we retrieve the value of our field mStackLevel that our callback onSaveInstanceState
     * saved using the key "level".
     *
     * @param savedInstanceState if Activity has been recreated after an orientation change this will
     * have the value for mStackLevel saved by onSaveInstanceState using
     * the key "level"
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_dialog)

        val tv = findViewById<View>(R.id.text)
        (tv as TextView).setText(R.string.dialog_fragment_example_instructions)

        // Watch for button clicks.
        val button = findViewById<Button>(R.id.show)
        /**
         * Called when a view has been clicked. We simply call our method showDialog() when
         * the "SHOW" Button (R.id.show) is clicked.
         *
         * Parameter: View of Button that was clicked
         */
        button.setOnClickListener {
            showDialog()
        }

        if (savedInstanceState != null) {
            mStackLevel = savedInstanceState.getInt("level")
        }
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in [.onCreate] or
     * [.onRestoreInstanceState] (the [Bundle] populated by this method
     * will be passed to both).
     *
     *
     * First we call through to our super's implementation of onCreate, then we store the value of
     * our field mStackLevel in the mapping of our parameter Bundle outState under the key "level".
     *
     * @param outState Bundle in which to place your saved state.
     */
    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("level", mStackLevel)
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
    internal fun showDialog() {
        mStackLevel++
        if (mStackLevel > 8) mStackLevel = 1

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        val ft = supportFragmentManager.beginTransaction()
        ft.addToBackStack(null)
        val prev = supportFragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }

        // Create and show the dialog.
        val newFragment = MyDialogFragment.newInstance(mStackLevel)
        newFragment.show(ft, "dialog")
    }

    /**
     * This DialogFragment displays a dialog with its style and theme configured by its arguments
     * when created, using 8 styles and five themes.
     */
    class MyDialogFragment : DialogFragment() {
        internal var mNum: Int = 0 // Which combination of styles and themes are being used for the dialog

        /**
         * Called to do initial creation of a fragment. First we call through to our super's
         * implementation of onCreate, then we fetch the value of our field **int mNum** which
         * is contained in our arguments under the key "num". Next we set our variables
         * **int style** and **int theme** based on the value of **mNum**, defaulting to
         * **STYLE_NORMAL** for **style** and 0 for **theme** (causes the system to pick
         * an appropriate theme (based on the style)). Finally we use **style** and **theme**
         * to set the attributes for our dialog.
         *
         * @param savedInstanceState we do not override onSaveInstanceState so do not use
         */
        @Suppress("DEPRECATION")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            mNum = arguments!!.getInt("num")

            // Pick a style based on the num.
            var style = STYLE_NORMAL
            var theme = 0
            when (mNum) {
                1 -> style = STYLE_NO_TITLE
                2 -> style = STYLE_NO_FRAME
                3 -> style = STYLE_NO_INPUT
                4 -> style = STYLE_NORMAL
                5 -> style = STYLE_NORMAL
                6 -> style = STYLE_NO_TITLE
                7 -> style = STYLE_NO_FRAME
                8 -> style = STYLE_NORMAL
            }
            when (mNum) {
                4 -> theme = android.R.style.Theme_Holo
                5 -> theme = android.R.style.Theme_Holo_Light_Dialog
                6 -> theme = android.R.style.Theme_Holo_Light
                7 -> theme = android.R.style.Theme_Holo_Light_Panel
                8 -> theme = android.R.style.Theme_Holo_Light
            }
            setStyle(style, theme)
        }

        /**
         * Called to have the fragment instantiate its user interface view. First we inflate our
         * layout file R.layout.fragment_dialog into **View v**, locate the **TextView** in
         * our layout R.id.text and set its text to a **String dialogLabel** which we create to
         * describe the style and theme we are using for the dialog. Next we locate the R.id.show
         * ("Show") **Button button** in our layout and set its OnClickListener to an anonymous
         * class which will call our method **showDialog()** to show the next styled and themed
         * **MyDialogFragment** that follows this one. Finally we return **View v** to our
         * caller.
         *
         * @param inflater           The LayoutInflater object that can be used to inflate
         * any views in the fragment,
         * @param container          If non-null, this is the parent view that the fragment's
         * UI should be attached to.  The fragment should not add the view itself,
         * but this can be used to generate the LayoutParams of the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         * from a previous saved state as given here.
         * @return Return the View for the fragment's UI.
         */
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val v = inflater.inflate(R.layout.fragment_dialog, container, false)
            val tv = v.findViewById<View>(R.id.text)
            val dialogLabel = getString(R.string.dialog_number) + mNum + ": using style " + getNameForNum(mNum)
            (tv as TextView).text = dialogLabel

            // Watch for button clicks.
            val button = v.findViewById<Button>(R.id.show)
            /**
             * Called when the R.id.show ("Show") **Button** is clicked. We simply call our
             * method **showDialog()** which will show the **MyDialogFragment** which
             * follows this one.
             *
             * Parameter: View of Button that was clicked
             */
            button.setOnClickListener {
                // When button is clicked, call up to owning activity.

                (activity as FragmentDialog).showDialog()
            }

            return v
        }

        companion object {

            /**
             * Create a new instance of MyDialogFragment, providing "num" as an argument. First we create
             * a new instance of MyDialogFragment f, then we create a **Bundle args** and add
             * our parameter num to its mapping using "num" as the key, and set **args** as the
             * arguments for **f**. Finally we return **f** to the caller.
             *
             * @param num number of the style and theme combination to use
             * @return a MyDialogFragment with arguments set to include **num** stored using the
             * key "num"
             */
            internal fun newInstance(num: Int): MyDialogFragment {
                val f = MyDialogFragment()

                // Supply num input as an argument.
                val args = Bundle()
                args.putInt("num", num)
                f.arguments = args

                return f
            }
        }
    }

    companion object {

        /**
         * This method converts the value of mStackLevel currently being used to a String describing the
         * the style and theme to display to the user. We simply use a switch statement to choose the
         * String based on the value of num passed us 1-8, returning "STYLE_NORMAL" if outside those
         * values.
         *
         * @param num mStackLevel for current style and theme
         * @return String describing style and theme
         */
        internal fun getNameForNum(num: Int): String {
            when (num) {
                1 -> return "STYLE_NO_TITLE"
                2 -> return "STYLE_NO_FRAME"
                3 -> return "STYLE_NO_INPUT (this window can't receive input, so " + "you will need to press the bottom show button)"
                4 -> return "STYLE_NORMAL with dark fullscreen theme"
                5 -> return "STYLE_NORMAL with light theme"
                6 -> return "STYLE_NO_TITLE with light theme"
                7 -> return "STYLE_NO_FRAME with light theme"
                8 -> return "STYLE_NORMAL with light fullscreen theme"
            }
            return "STYLE_NORMAL"
        }
    }

}
