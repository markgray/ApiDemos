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
import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.example.android.apis.R


@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
/**
 * Demonstrates how to show an [AlertDialog] that is managed by a `Fragment`. Uses [DialogFragment]
 * as the base class and overrides [onCreateDialog] in which it builds the [AlertDialog] using an
 * [AlertDialog.Builder]
 */
class FragmentAlertDialog : FragmentActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.fragment_dialog. Then we
     * find the **View tv** in the layout (R.id.text) and set its text to the String
     * R.string.example_alert_dialogfragment:
     *
     * "Example of displaying an alert dialog with a DialogFragment"
     *
     * Finally we locate the **Button button** R.id.show ("Show") and set its OnClickListener
     * to an anonymous class which calls our method **showDialog()** to show our AlertDialog.
     *
     * @param savedInstanceState we do not override onSaveInstanceState so do not use this
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_dialog)

        val tv = findViewById<View>(R.id.text)
        (tv as TextView).setText(R.string.example_alert_dialogfragment)

        // Watch for button clicks.
        val button = findViewById<Button>(R.id.show)
        /**
         * Called when the Button R.id.show ("Show") is clicked. We simply call our method
         * showDialog()
         *
         * Parameter: View of Button that was clicked
         */
        button.setOnClickListener {
            showDialog()
        }
    }

    /**
     * Create and show a MyAlertDialogFragment DialogFragment. We create a new instance of
     * MyAlertDialogFragment by calling its method newInstance with the resource id for the
     * nonsense String R.string.alert_dialog_two_buttons_title, and then invoke the method
     * DialogFragment.show to show it.
     */
    internal fun showDialog() {
        val newFragment = MyAlertDialogFragment.newInstance(R.string.alert_dialog_two_buttons_title)
        newFragment.show(supportFragmentManager, "dialog")
    }

    /**
     * OnClickListener Callback for when the positive Button of the MyAlertDialogFragment
     */
    fun doPositiveClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Positive click!")
    }

    /**
     * OnClickListener Callback for when the negative Button of the MyAlertDialogFragment
     */
    fun doNegativeClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Negative click!")
    }

    /**
     * Minimalist DialogFragment
     */
    class MyAlertDialogFragment : DialogFragment() {

        /**
         * Override to build your own custom Dialog container.  This is typically
         * used to show an AlertDialog instead of a generic Dialog; when doing so,
         * [.onCreateView] does not need
         * to be implemented since the AlertDialog takes care of its own content.
         *
         *
         * This method will be called after [.onCreate] and
         * before [.onCreateView].  The
         * default implementation simply instantiates and returns a [Dialog]
         * class.
         *
         *
         * *Note: DialogFragment needs to use the [ Dialog.setOnCancelListener][Dialog.setOnCancelListener] and [ Dialog.setOnDismissListener][Dialog.setOnDismissListener] callbacks.  You must not set them yourself.*
         * To find out about these events, override [.onCancel]
         * and [.onDismiss].
         *
         * First we retrieve the **int title** String resource id from our arguments, then
         * we use an AlertDialog.Builder to create a Dialog instance with the icon set to the
         * resource R.drawable.alert_dialog_icon, the title set to **title**, the positive
         * Button labeled using the String R.string.alert_dialog_ok ("OK") with an anonymous class
         * DialogInterface.OnClickListener which calls our method doPositiveClick, and the negative
         * Button labeled using the String R.string.alert_dialog_cancel ("Cancel") with an anonymous
         * class DialogInterface.OnClickListener which calls our method doNegativeClick -- which
         * we return to the caller.
         *
         * @param savedInstanceState we do not override onSaveInstanceState so do not use this
         *
         * @return Return a new Dialog instance to be displayed by the Fragment.
         */
        @Suppress("UNUSED_ANONYMOUS_PARAMETER")
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val title = arguments!!.getInt("title")

            return AlertDialog.Builder(activity)
                    .setIcon(R.drawable.alert_dialog_icon)
                    .setTitle(title)
                    .setPositiveButton(R.string.alert_dialog_ok
                    ) { dialog, whichButton -> (activity as FragmentAlertDialog).doPositiveClick() }
                    .setNegativeButton(R.string.alert_dialog_cancel
                    ) { dialog, whichButton -> (activity as FragmentAlertDialog).doNegativeClick() }
                    .create()
        }

        companion object {

            /**
             * Factory method to create a new instance of MyAlertDialogFragment and set its arguments.
             * First we create a new instance **MyAlertDialogFragment frag**, then we create
             * a **Bundle args**, add our parameter **int title** to it under the
             * key "title", and then set the argmuments of **frag** to our **Bundle args**.
             * Finally we return **MyAlertDialogFragment frag** to the caller.
             *
             * @param title resource id for a String to use as the DialogFragment's title
             *
             * @return New instance of MyAlertDialogFragment with its arguments set to
             */
            fun newInstance(title: Int): MyAlertDialogFragment {
                val frag = MyAlertDialogFragment()
                val args = Bundle()
                args.putInt("title", title)
                frag.arguments = args
                return frag
            }
        }
    }

}
