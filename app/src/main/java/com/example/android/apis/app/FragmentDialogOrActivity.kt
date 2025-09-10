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
 * Shows how to show the same [DialogFragment] embedded in the activity layout, and as a dialog.
 * RequiresApi(Build.VERSION_CODES.HONEYCOMB)
 */
@Suppress("MemberVisibilityCanBePrivate")
class FragmentDialogOrActivity : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.fragment_dialog_or_activity.
     * Next we check to see if our [Bundle] parameter [savedInstanceState] is *null*, which means
     * this is our first time being called, so we need to embed our `Fragment` in our layout ourselves
     * rather than rely on the system to recreate it. If *null* then we initialize our
     * `FragmentTransaction` variable `val ft` by using the support `FragmentManager` for interacting
     * with fragments associated with this activity to start a series of edit operations on the
     * Fragments associated with this `FragmentManager`. We create an instance of [DialogFragment]
     * to initialize our variable `val newFragment`, use `ft` to add `newFragment` as the contents
     * of the `FrameLayout` with ID R.id.embedded inside our content view, and then commit `ft`.
     * Having taken care of our embedded `Fragment` we initialize our [Button] variable `val button`
     * by finding the view with ID R.id.show_dialog ("Show") and set its `OnClickListener` to a
     * lambda which will call our method [showDialog] when the [Button] is clicked.
     *
     * @param savedInstanceState if the activity is being recreated after an orientation change this
     * will contain information for the `FragmentManager` to use, otherwise it is *null*. We use
     * this to decide whether it is the first time that `onCreate` has been called (it will be *null*)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_dialog_or_activity)

        if (savedInstanceState == null) {
            // First-time init; create fragment to embed in activity.

            val ft = supportFragmentManager.beginTransaction()
            val newFragment = MyDialogFragment.newInstance()
            ft.add(R.id.embedded, newFragment)
            ft.commit()

        }

        // Watch for button clicks.
        val button = findViewById<Button>(R.id.show_dialog)
        /**
         * Called when a view has been clicked. We simply call our method **showDialog()**.
         *
         * Parameter: View of the Button that was clicked
         */
        button.setOnClickListener {
            showDialog()
        }
    }

    /**
     * Create the fragment and show it as a dialog. We initialize our [MyDialogFragment] variable
     * `val newFragment` with the new instance returned by [MyDialogFragment.newInstance]. We then
     * call the `show` method of `newFragment` to have it use the support `FragmentManager` to show
     * the dialog fragment using the tag "dialog".
     */
    internal fun showDialog() {
        val newFragment = MyDialogFragment.newInstance()
        newFragment.show(supportFragmentManager, "dialog")
    }

    /**
     * Simple [DialogFragment] which only displays a [String] in a [TextView]
     */
    class MyDialogFragment : DialogFragment() {

        /**
         * Called to have the fragment instantiate its user interface view. First we use our
         * [LayoutInflater] parameter [inflater] to inflate our layout file R.layout.hello_world
         * into the [View] variable `val v`. Then we initialize our [View] variable `val tv` by
         * finding the view with ID R.id.text in `v`, and set the text in this [TextView] to the
         * [String] with ID R.string.my_dialog_fragment_label:
         *
         *     This is an instance of MyDialogFragment
         *
         * Finally we return `v` to the caller.
         *
         * @param inflater The [LayoutInflater] object that can be used to inflate any views.
         * @param container If non-null, this is the parent view that the fragment's UI should be
         * attached to. The fragment should not add the view itself, but this can be used to generate
         * the `LayoutParams` of the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         * from a previous saved state as given here.
         * @return Return the View for the fragment's UI
         */
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val v = inflater.inflate(R.layout.hello_world, container, false)
            val tv = v.findViewById<View>(R.id.text)
            (tv as TextView).setText(R.string.my_dialog_fragment_label)
            return v
        }

        /**
         * Our static factory method.
         */
        companion object {
            /**
             * Simply creates and returns a new instance of **MyDialogFragment**
             *
             * @return new instance of **MyDialogFragment**
             */
            internal fun newInstance(): MyDialogFragment {
                return MyDialogFragment()
            }
        }
    }

}
