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
@file:Suppress(
    "DEPRECATION",
    "unused",
    "ReplaceNotNullAssertionWithElvisReturn"
) // TODO: Replace lots of deprectated stuff in this guy.
package com.example.android.apis.app

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import com.example.android.apis.app.AlertDialogSamples.Companion.mProgressHandler

/**
 * Example of how to use an [android.app.AlertDialog]. AlertDialogSamples
 *
 * This demonstrates the different ways the AlertDialog can be used.
 *
 * Source files:
 *  - src/com.example.android.apis/app/AlertDialogSamples.java The Alert Dialog Samples implementation
 *  - /res/any/layout/alert_dialog.xml Defines contents of the screen
 *  RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
 */
@Suppress("MemberVisibilityCanBePrivate", "UNUSED_ANONYMOUS_PARAMETER")
class AlertDialogSamples : AppCompatActivity() {
    /**
     * ProgressDialog used for DIALOG_PROGRESS_SPINNER
     */
    private var mProgressSpinnerDialog: ProgressDialog? = null

    /**
     * ProgressDialog used for DIALOG_PROGRESS
     */
    private var mProgressDialog: ProgressDialog? = null

    /**
     * Count used by [mProgressHandler] for the two [ProgressDialog]'s
     */
    private var mProgress: Int = 0

    /**
     * Root View used for inflation, initialize by finding android.R.id.content View in [onCreate]
     */
    internal lateinit var mRoot: ViewGroup

    /**
     * Callback for creating dialogs that are managed (saved and restored) for you by the activity.
     * We switch based on the id passed us which is the [Int] parameter that the `onClickListener`
     * for the [Button] clicked in our layout gave when calling `showDialog`. In each when branch
     * an [AlertDialog.Builder] is used to build a [Dialog] which it returns to the caller. If the
     * [id] is not among the ones defined below (DIALOG_*) we return null. See each case statement
     * for comments about what is done.
     *
     * @param id The id of the dialog.
     * @return The dialog. If you return null, the dialog will not be created.
     */
    @Deprecated("Deprecated in Java")
    override fun onCreateDialog(id: Int): Dialog? {
        when (id) {
            DIALOG_YES_NO_MESSAGE // OK Cancel dialog with a message
                -> return AlertDialog.Builder(this@AlertDialogSamples)
                .setTitle(R.string.alert_dialog_two_buttons_title)
                .setPositiveButton(R.string.alert_dialog_ok) { dialog, whichButton -> /*OK*/ }
                .setNegativeButton(R.string.alert_dialog_cancel) { dialog, whichButton -> /*Cancel*/ }
                .create()

            DIALOG_YES_NO_LONG_MESSAGE // OK Cancel dialog with a long message (Note .setMessage and .setNeutralButton)
                -> return AlertDialog.Builder(this@AlertDialogSamples)
                .setTitle(R.string.alert_dialog_two_buttons_msg)
                .setMessage(R.string.alert_dialog_two_buttons2_msg)
                .setPositiveButton(R.string.alert_dialog_ok) { dialog, whichButton -> /*OK*/ }
                .setNeutralButton(R.string.alert_dialog_something) { dialog, whichButton -> /*Something*/ }
                .setNegativeButton(R.string.alert_dialog_cancel) { dialog, whichButton -> /*Cancel*/ }
                .create()

            DIALOG_YES_NO_ULTRA_LONG_MESSAGE // OK Cancel dialog with ultra long message
                -> return AlertDialog.Builder(this@AlertDialogSamples)
                .setTitle(R.string.alert_dialog_two_buttons_msg)
                .setMessage(R.string.alert_dialog_two_buttons2ultra_msg)
                .setPositiveButton(R.string.alert_dialog_ok) { dialog, whichButton -> /*OK*/ }
                .setNeutralButton(R.string.alert_dialog_something) { dialog, whichButton -> /*Something*/ }
                .setNegativeButton(R.string.alert_dialog_cancel) { dialog, whichButton -> /*Cancel*/ }
                .create()

            DIALOG_LIST // List dialog
                -> return AlertDialog.Builder(this@AlertDialogSamples)
                .setTitle(R.string.select_dialog)
                .setItems(R.array.select_dialog_items) { dialog, which ->
                    /* User clicked so do some stuff */
                    val items = resources.getStringArray(R.array.select_dialog_items)
                    AlertDialog.Builder(this@AlertDialogSamples)
                        .setMessage("You selected: " + which + " , " + items[which])
                        .show()
                }
                .create()

            DIALOG_PROGRESS // Progress bar dialog
                -> {
                mProgressDialog = ProgressDialog(this@AlertDialogSamples)
                mProgressDialog!!.setTitle(R.string.select_dialog)
                mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                mProgressDialog!!.max = MAX_PROGRESS
                mProgressDialog!!.setButton(
                    DialogInterface.BUTTON_POSITIVE,
                    getText(R.string.alert_dialog_hide)
                ) { dialog, whichButton -> /*Yes*/ }
                mProgressDialog!!.setButton(
                    DialogInterface.BUTTON_NEGATIVE,
                    getText(R.string.alert_dialog_cancel)
                ) { dialog, whichButton -> /*No*/ }
                return mProgressDialog
            }

            DIALOG_PROGRESS_SPINNER // Progress spinner dialog
                -> {
                mProgressSpinnerDialog = ProgressDialog(this@AlertDialogSamples)
                mProgressSpinnerDialog!!.setTitle(R.string.select_dialog)
                mProgressSpinnerDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                return mProgressSpinnerDialog
            }

            DIALOG_SINGLE_CHOICE // Single choice list
                -> return AlertDialog.Builder(this@AlertDialogSamples)
                .setTitle(R.string.alert_dialog_single_choice)
                .setSingleChoiceItems(
                    R.array.select_dialog_items2,
                    0
                ) { dialog, whichButton -> /*radio button clicked*/ }
                .setPositiveButton(R.string.alert_dialog_ok) { dialog, whichButton -> /*Yes*/ }
                .setNegativeButton(R.string.alert_dialog_cancel) { dialog, whichButton -> /*No*/ }
                .create()

            DIALOG_MULTIPLE_CHOICE // Repeat alarm
                -> return AlertDialog.Builder(this@AlertDialogSamples)
                .setTitle(R.string.alert_dialog_multi_choice)
                .setMultiChoiceItems(
                    R.array.select_dialog_items3,
                    booleanArrayOf(false, true, false, true, false, false, false)
                ) { dialog, whichButton, isChecked -> /*check box clicked*/ }
                .setPositiveButton(
                    R.string.alert_dialog_ok
                ) { dialog, whichButton -> /*Yes*/ }
                .setNegativeButton(
                    R.string.alert_dialog_cancel
                ) { dialog, whichButton -> /*No*/ }
                .create()

            DIALOG_MULTIPLE_CHOICE_CURSOR // Send Call to VoiceMail
                -> {
                val projection = arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.SEND_TO_VOICEMAIL
                )
                val cursor = managedQuery(
                    ContactsContract.Contacts.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null
                )
                return AlertDialog.Builder(this@AlertDialogSamples)
                    .setTitle(R.string.alert_dialog_multi_choice_cursor)
                    .setMultiChoiceItems(
                        cursor,
                        ContactsContract.Contacts.SEND_TO_VOICEMAIL,
                        ContactsContract.Contacts.DISPLAY_NAME
                    ) { dialog, whichButton, isChecked ->
                        Toast.makeText(
                            this@AlertDialogSamples,
                            "Readonly Demo Only - Data will not be updated",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .create()
            }

            DIALOG_TEXT_ENTRY // Text Entry dialog
                -> {
                // This example shows how to add a custom layout to an AlertDialog
                val factory = LayoutInflater.from(this)
                val textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, mRoot, false)
                return AlertDialog.Builder(this@AlertDialogSamples)
                    .setTitle(R.string.alert_dialog_text_entry)
                    .setView(textEntryView)
                    .setPositiveButton(R.string.alert_dialog_ok) { dialog, whichButton -> /*OK*/ }
                    .setNegativeButton(R.string.alert_dialog_cancel) { dialog, whichButton -> /*cancel*/ }
                    .create()
            }

            DIALOG_YES_NO_OLD_SCHOOL_MESSAGE // OK Cancel dialog with traditional theme
                -> return AlertDialog.Builder(
                this@AlertDialogSamples,
                AlertDialog.THEME_TRADITIONAL
            )
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.alert_dialog_two_buttons_title)
                .setPositiveButton(R.string.alert_dialog_ok) { dialog, whichButton -> /*OK*/ }
                .setNegativeButton(R.string.alert_dialog_cancel) { dialog, whichButton -> /*Cancel*/ }
                .create()

            DIALOG_YES_NO_HOLO_LIGHT_MESSAGE // OK Cancel dialog with Holo Light theme
                -> return AlertDialog.Builder(this@AlertDialogSamples, AlertDialog.THEME_HOLO_LIGHT)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.alert_dialog_two_buttons_title)
                .setPositiveButton(R.string.alert_dialog_ok) { dialog, whichButton -> /*Ok*/ }
                .setNegativeButton(R.string.alert_dialog_cancel) { dialog, whichButton -> /*cancel*/ }
                .create()

            DIALOG_YES_NO_DEFAULT_LIGHT_MESSAGE // OK Cancel dialog with DeviceDefault Light theme
                -> return AlertDialog.Builder(
                this@AlertDialogSamples,
                AlertDialog.THEME_DEVICE_DEFAULT_LIGHT
            )
                .setTitle(R.string.alert_dialog_two_buttons_title)
                .setPositiveButton(R.string.alert_dialog_ok) { dialog, whichButton -> /*ok*/ }
                .setNegativeButton(R.string.alert_dialog_cancel) { dialog, whichButton -> /*cancel*/ }
                .create()

            DIALOG_YES_NO_DEFAULT_DARK_MESSAGE // OK Cancel dialog with DeviceDefault theme
                -> return AlertDialog.Builder(
                this@AlertDialogSamples,
                AlertDialog.THEME_DEVICE_DEFAULT_DARK
            )
                .setTitle(R.string.alert_dialog_two_buttons_title)
                .setPositiveButton(R.string.alert_dialog_ok) { dialog, whichButton -> /*ok*/ }
                .setNegativeButton(R.string.alert_dialog_cancel) { dialog, whichButton -> /*cancel*/ }
                .create()
        }
        return null
    }

    /**
     * Initialization of the Activity after it is first created.  Must at least
     * call [android.app.Activity.setContentView] to
     * describe what is to be displayed in the screen.
     */
    @SuppressLint("HandlerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.alert_dialog)
        mRoot =
            findViewById<View>(android.R.id.content) as ViewGroup // In order to have a rootView for inflate

        /* Display a text message with yes/no buttons and handle each message as well as the cancel action */
        val twoButtonsTitle =
            findViewById<View>(R.id.two_buttons) as Button // OK Cancel dialog with DeviceDefault theme
        twoButtonsTitle.setOnClickListener { showDialog(DIALOG_YES_NO_MESSAGE) }

        /* Display a long text message with yes/no buttons and handle each message as well as the cancel action */
        val twoButtons2Title =
            findViewById<View>(R.id.two_buttons2) as Button // OK Cancel dialog with a long message
        twoButtons2Title.setOnClickListener { showDialog(DIALOG_YES_NO_LONG_MESSAGE) }


        /* Display an ultra long text message with yes/no buttons and handle each message as well as the cancel action */
        val twoButtons2UltraTitle =
            findViewById<View>(R.id.two_buttons2ultra) as Button // OK Cancel dialog with ultra long message
        twoButtons2UltraTitle.setOnClickListener { showDialog(DIALOG_YES_NO_ULTRA_LONG_MESSAGE) }


        /* Display a list of items */
        val selectButton = findViewById<View>(R.id.select_button) as Button // List dialog
        selectButton.setOnClickListener { showDialog(DIALOG_LIST) }

        /* Display a custom progress bar */
        val progressButton =
            findViewById<View>(R.id.progress_button) as Button // Progress bar dialog
        progressButton.setOnClickListener {
            showDialog(DIALOG_PROGRESS)
            mProgress = 0
            mProgressDialog!!.progress = 0
            mProgressHandler!!.sendEmptyMessage(0)
        }

        /* Display a custom progress bar */
        val progressSpinnerButton =
            findViewById<View>(R.id.progress_spinner_button) as Button // Progress spinner dialog
        progressSpinnerButton.setOnClickListener { showDialog(DIALOG_PROGRESS_SPINNER) }

        /* Display a radio button group */
        val radioButton = findViewById<View>(R.id.radio_button) as Button // Single choice list
        radioButton.setOnClickListener { showDialog(DIALOG_SINGLE_CHOICE) }

        /* Display a list of checkboxes */
        val checkBox = findViewById<View>(R.id.checkbox_button) as Button // Repeat alarm
        checkBox.setOnClickListener { showDialog(DIALOG_MULTIPLE_CHOICE) }

        /* Display a list of checkboxes, backed by a cursor */
        val checkBox2 =
            findViewById<View>(R.id.checkbox_button2) as Button // Send Call to VoiceMail
        checkBox2.setOnClickListener { showDialog(DIALOG_MULTIPLE_CHOICE_CURSOR) }

        /* Display a text entry dialog */
        val textEntry = findViewById<View>(R.id.text_entry_button) as Button // Text Entry dialog
        textEntry.setOnClickListener { showDialog(DIALOG_TEXT_ENTRY) }

        /* Two points, in the traditional theme */
        val twoButtonsOldSchoolTitle =
            findViewById<View>(R.id.two_buttons_old_school) as Button // OK Cancel dialog with traditional theme
        twoButtonsOldSchoolTitle.setOnClickListener { showDialog(DIALOG_YES_NO_OLD_SCHOOL_MESSAGE) }

        /* Two points, in the light holographic theme */
        val twoButtonsHoloLightTitle =
            findViewById<View>(R.id.two_buttons_holo_light) as Button // OK Cancel dialog with Holo Light theme
        twoButtonsHoloLightTitle.setOnClickListener { showDialog(DIALOG_YES_NO_HOLO_LIGHT_MESSAGE) }

        /* Two points, in the light default theme */
        val twoButtonsDefaultLightTitle =
            findViewById<View>(R.id.two_buttons_default_light) as Button // OK Cancel dialog with DeviceDefault Light theme
        twoButtonsDefaultLightTitle.setOnClickListener {
            showDialog(
                DIALOG_YES_NO_DEFAULT_LIGHT_MESSAGE
            )
        }

        /* Two points, in the dark default theme */
        val twoButtonsDefaultDarkTitle =
            findViewById<View>(R.id.two_buttons_default_dark) as Button // OK Cancel dialog with DeviceDefault theme
        twoButtonsDefaultDarkTitle.setOnClickListener {
            showDialog(
                DIALOG_YES_NO_DEFAULT_DARK_MESSAGE
            )
        }

        /**
         * Handler for background Thread which advances the Progress Bar in the Progress bar dialog.
         */
        mProgressHandler = object : Handler(Looper.myLooper()!!) {
            /**
             * Subclasses must implement this to receive messages. We advance the counter whenever
             * we are called, whether by sendEmptyMessage(0) when the ProgressBar is started, or
             * by sendEmptyMessageDelayed(0, 100) which we call after incrementing the ProgressBar
             * to schedule the next increment for 100 milliseconds later
             *
             * @param msg Message sent us by sendEmptyMessage and sendEmptyMessageDelayed, the int
             * value given them is Bundle'd under the key "what".
             */
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (mProgress >= MAX_PROGRESS) {
                    mProgressDialog!!.dismiss()
                } else {
                    mProgress++
                    mProgressDialog!!.incrementProgressBy(1)
                    mProgressHandler!!.sendEmptyMessageDelayed(0, 100)
                }
            }
        }
    }

    /**
     * Activities cannot draw during the period that their windows are animating in. In order
     * to know when it is safe to begin drawing they can override this method which will be
     * called when the entering animation has completed. Called after the Activity has finished
     * onCreate and the content view has been rendered, but not called for the Dialog's.
     */
    override fun onEnterAnimationComplete() {
        Log.i(TAG, "onEnterAnimationComplete")
    }

    companion object {
        private const val TAG = "AlertDialogSamples" // TAG used for logging

        private const val DIALOG_YES_NO_MESSAGE = 1 // OK Cancel dialog with a message
        private const val DIALOG_YES_NO_LONG_MESSAGE = 2 // OK Cancel dialog with a long message
        private const val DIALOG_LIST = 3 // List dialog
        private const val DIALOG_PROGRESS = 4 // Progress bar dialog
        private const val DIALOG_SINGLE_CHOICE = 5 // Single choice list
        private const val DIALOG_MULTIPLE_CHOICE = 6 // Repeat alarm
        private const val DIALOG_TEXT_ENTRY = 7 // Text Entry dialog
        private const val DIALOG_MULTIPLE_CHOICE_CURSOR = 8 // Send Call to VoiceMail
        private const val DIALOG_YES_NO_ULTRA_LONG_MESSAGE =
            9 // OK Cancel dialog with ultra long message
        private const val DIALOG_YES_NO_OLD_SCHOOL_MESSAGE =
            10 // OK Cancel dialog with traditional theme
        private const val DIALOG_YES_NO_HOLO_LIGHT_MESSAGE =
            11 // OK Cancel dialog with Holo Light theme
        private const val DIALOG_YES_NO_DEFAULT_LIGHT_MESSAGE =
            12 // OK Cancel dialog with DeviceDefault Light theme
        private const val DIALOG_YES_NO_DEFAULT_DARK_MESSAGE =
            13 // OK Cancel dialog with DeviceDefault theme
        private const val DIALOG_PROGRESS_SPINNER = 14 // Progress spinner dialog

        private const val MAX_PROGRESS = 100 // Value used for ProgressDialog.setMax call
        private var mProgressHandler: Handler? =
            null // Handler which "moves" the ProgressDialog progress setting
    }
}
