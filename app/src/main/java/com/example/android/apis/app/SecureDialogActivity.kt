/*
 * Copyright (C) 2012 The Android Open Source Project
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

import com.example.android.apis.R

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button

/**
 * Secure Dialog Activity
 *
 * This activity demonstrates how to create a dialog whose window is backed by
 * a secure surface using [WindowManager.LayoutParams.FLAG_SECURE].
 * Because the surface is secure, its contents cannot be captured in screenshots
 * and will not be visible on non-secure displays even when mirrored.
 *
 * Here are a few things you can do to experiment with secure surfaces and
 * observe their behavior:
 *  - Try taking a screenshot. Either the system will prevent you from taking
 * a screenshot altogether or the screenshot should not contain the contents
 * of the secure surface.
 *  - Try mirroring the secure surface onto a non-secure display such as an
 * "Overlay Display" created using the "Simulate secondary displays" option in
 * the "Developer options" section of the Settings application. The non-secure
 * secondary display should not show the contents of the secure surface.
 *  - Try mirroring the secure surface onto a secure display such as an HDMI
 * display with HDCP enabled. The contents of the secure surface should appear
 * on the display.
 */
class SecureDialogActivity : Activity(), View.OnClickListener {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.secure_dialog_activity.
     * Finally we locate the "Show secure dialog" Button (R.id.show) and set its `OnClickListener`
     * to "this" (we implement [View.OnClickListener] so our onClick method will be called when
     * the Button is clicked).
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState)

        // See assets/res/any/layout/secure_dialog_activity.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.secure_dialog_activity)

        // Handle click events on the button to show the dialog.
        val button = findViewById<Button>(R.id.show)
        button.setOnClickListener(this)
    }

    /**
     * Called when the button to show the dialog is clicked. First we create the [AlertDialog]
     * `val dialog` using an instance of [AlertDialog.Builder] to set its positive button to
     * read "OK", and its message to read "I am a secure dialog!", then we set the FLAG_SECURE flag
     * of the `dialog` window which causes the system to treat the content of the window as secure,
     * preventing it from appearing in screenshots or from being viewed on non-secure displays.
     * Finally we call show to show the dialog.
     *
     * @param v View of the "Show secure dialog" Button (R.id.show)
     */
    override fun onClick(v: View) {
        // Create a dialog.
        val dialog = AlertDialog.Builder(this)
                .setPositiveButton(android.R.string.ok, null)
                .setMessage(R.string.secure_dialog_dialog_text)
                .create()

        // Make the dialog secure.  This must be done at the time the dialog is
        // created.  It cannot be changed after the dialog has been shown.
        dialog.window!!.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE)

        // Show the dialog.
        dialog.show()
    }
}
