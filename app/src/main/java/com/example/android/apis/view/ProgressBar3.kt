/*
 * Copyright (C) 2007 The Android Open Source Project
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
@file:Suppress("DEPRECATION")

package com.example.android.apis.view

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates the use of progress dialogs. Uses Activity#onCreateDialog
 * Activity#showDialog to ensure the dialogs will be properly saved and
 * restored. Buttons on the main Layout allow you to choose between one
 * which sets the title using dialog.setTitle("Indeterminate") and one which
 * has no title. (Direct use of showDialog is deprecated, use a DialogFragment
 * instead).
 */
class ProgressBar3 : AppCompatActivity() {
    /**
     * [ProgressDialog] which sets the title to "Indeterminate" UNUSED
     */
    @Suppress("unused")
    var mDialog1: ProgressDialog? = null

    /**
     * [ProgressDialog] with no title UNUSED
     */
    @Suppress("unused")
    var mDialog2: ProgressDialog? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.progressbar_3. We
     * initialize our [Button] variable `var button` by finding the view with ID R.id.showIndeterminate
     * ("Show Indeterminate") and set its `OnClickListener` to a lambda which calls [showDialog] with
     * the ID DIALOG1_KEY. We then set `button` by finding the view with ID R.id.showIndeterminateNoTitle
     * ("Show Indeterminate No Title") and set its `OnClickListener` to an a lambda which calls
     * [showDialog] with the ID DIALOG2_KEY.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.progressbar_3)
        var button = findViewById<Button>(R.id.showIndeterminate)
        button.setOnClickListener { showDialog(DIALOG1_KEY) }
        button = findViewById(R.id.showIndeterminateNoTitle)
        button.setOnClickListener { showDialog(DIALOG2_KEY) }
    }

    /**
     * Callback for creating dialogs that are managed (saved and restored) for you by the activity.
     * We switch on the value of our [Int] parameter [id]:
     *
     *  * DIALOG1_KEY - we create a new instance to initialize our [ProgressDialog] variable
     *  `val dialog`, set its title to the string "Indeterminate", set its message to the string
     *  "Please wait while loading...", set it to be indeterminate, and set it to be cancelable.
     *  Finally we return `dialog` to the caller.
     *
     *  * DIALOG2_KEY - we create a new instance to initialize our [ProgressDialog] variable
     *  `val dialog`, set its message to the string "Please wait while loading...", set it to be
     *  indeterminate, and set it to be cancelable. Finally we return `dialog` to the caller.
     *
     * For any other [id] we return null.
     *
     * @param id The id of the dialog.
     * @return The dialog. If you return null, the dialog will not be created.
     */
    @Deprecated("Deprecated in Java")
    override fun onCreateDialog(id: Int): Dialog? {
        when (id) {
            DIALOG1_KEY -> {
                val dialog = ProgressDialog(this)
                dialog.setTitle("Indeterminate")
                dialog.setMessage("Please wait while loading...")
                dialog.isIndeterminate = true
                dialog.setCancelable(true)
                return dialog
            }

            DIALOG2_KEY -> {
                val dialog = ProgressDialog(this)
                dialog.setMessage("Please wait while loading...")
                dialog.isIndeterminate = true
                dialog.setCancelable(true)
                return dialog
            }
        }
        return null
    }

    companion object {
        /**
         * ID of the [ProgressDialog] in field [mDialog1]
         */
        private const val DIALOG1_KEY = 0

        /**
         * ID of the [ProgressDialog] in field [mDialog2]
         */
        private const val DIALOG2_KEY = 1
    }
}