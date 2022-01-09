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
package com.example.android.apis.view

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This activity demonstrates two different ways in which views can be made more secure to
 * touch spoofing attacks by leveraging framework features.
 *
 * The activity presents 3 buttons that ostensibly perform a risky security critical
 * function. Under ordinary circumstances, the user would never click on these buttons
 * or would at least think long and hard about it. However, a carefully crafted toast can
 * overlay the contents of the activity in such a way as to make the user believe the buttons
 * are innocuous.  Since the toast cannot receive input, the touches are passed down to the
 * activity potentially yielding an effect other than what the user intended.
 *
 * To simulate the spoofing risk, this activity pops up a specially crafted overlay as
 * a toast laid out so as to cover the buttons and part of the descriptive text.
 * For the purposes of this demonstration, pretend that the overlay was actually popped
 * up by a malicious application published by the International Cabal of Evil Penguins.
 *
 * The 3 buttons are set up as follows:
 *
 *  1. The "unsecured button" does not apply any touch filtering of any kind.
 *  When the toast appears, this button remains clickable as usual which creates an
 *  opportunity for spoofing to occur.
 *
 *  2. The "built-in secured button" leverages the android:filterTouchesWhenObscured view
 *  attribute to ask the framework to filter out touches when the window is obscured.
 *  When the toast appears, the button does not receive the touch and appears to be inoperable.
 *
 *  3. The "custom secured button" adds a touch listener to the button which intercepts the
 *  touch event and checks whether the window is obscured.  If so, it warns the user and
 *  drops the touch event.  This example is intended to demonstrate how a view can
 *  perform its own filtering and provide additional feedback by examining the `MotionEvent`
 *  flags to determine whether the window is obscured. Here we use a touch listener but
 *  a custom view subclass could perform the filtering by overriding
 *  `View.onFilterTouchEventForSecurity(MotionEvent)`.
 *
 * Refer to the comments on [View] for more information about view security.
 */
class SecureView : AppCompatActivity() {
    /**
     * Number of times that the user has been tricked into clicking an insecure button, used to
     * select which humorous terrifying message to pop up.
     */
    private var mClickCount = 0

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.secure_view. We
     * initialize our [Button] variable `val toastButton` by finding the view in our layout with ID
     * R.id.secure_view_toast_button ("Pop toast") and set its `OnClickListener` to an a lambda
     * which calls our method [showOverlay] to pop up a toast obscuring our UI when the button is
     * clicked. We initialize our [Button] variable `val unsecureButton` by finding the view with
     * ID R.id.secure_view_unsecure_button ("Don't click! It'll cost you!") and call our method
     * [setClickedAction] with it to set its `OnClickListener` to an a lambda which pops up an alert
     * dialog to terrify the user. We initialize our [Button] variable `val builtinSecureButton` by
     * finding the view in our layout with the ID R.id.secure_view_builtin_secure_button ("Don't
     * click! It'll cost you!") and call our method [setClickedAction] with it to do the same thing.
     * We initialize our [Button] variable `val customSecureButton` by finding the view with ID
     * R.id.secure_view_custom_secure_button ("Don't click! It'll cost you!") and also call our
     * method `setClickedAction` with it, followed by calling our method [setTouchFilter] with it
     * to add an `OnTouchListener` which prevents clicks from reaching the button if the
     * button is obscured.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.secure_view)
        val toastButton = findViewById<Button>(R.id.secure_view_toast_button)
        toastButton.setOnClickListener { showOverlay() }
        val unsecureButton = findViewById<Button>(R.id.secure_view_unsecure_button)
        setClickedAction(unsecureButton)
        val builtinSecureButton = findViewById<Button>(R.id.secure_view_builtin_secure_button)
        setClickedAction(builtinSecureButton)
        val customSecureButton = findViewById<Button>(R.id.secure_view_custom_secure_button)
        setClickedAction(customSecureButton)
        setTouchFilter(customSecureButton)
    }

    /**
     * Generates a toast view with a special layout that will position itself right on top of this
     * view's interesting widgets. First we fetch the shared `LayoutInflater` for our main window
     * and use it to inflate the layout file R.layout.secure_view_overlay into our [SecureViewOverlay]
     * variable `val overlay`. We call the `setActivityToSpoof` method of `overlay` to save a
     * reference to "this" in its [SecureView] field `mActivity`. Next we initialize our [Toast]
     * variable `val toast` with a new instance, set its gravity to FILL, with (0,0) as its offset,
     * set is view to `overlay`, and show it.
     */
    private fun showOverlay() {
        // Generate a toast view with a special layout that will position itself right
        // on top of this view's interesting widgets.  Sneaky huh?
        @SuppressLint("InflateParams")
        val overlay = layoutInflater
            .inflate(R.layout.secure_view_overlay, null) as SecureViewOverlay
        overlay.setActivityToSpoof(this)
        val toast = Toast(applicationContext)
        toast.setGravity(Gravity.FILL, 0, 0)
        @Suppress("DEPRECATION") // This is still OK if we are foreground.
        toast.view = overlay
        toast.show()
    }

    /**
     * Sets the `OnClickListener` of its [Button] parameter [button] to an a lambda which pops up
     * an [AlertDialog] with a random "terrifying" message when the [Button] is clicked.
     *
     * @param button [Button] whose `OnClickListener` we are to set
     */
    private fun setClickedAction(button: Button) {
        button.setOnClickListener {
            val messages = resources.getStringArray(R.array.secure_view_clicked)
            val message = messages[mClickCount++ % messages.size]
            AlertDialog.Builder(this@SecureView)
                .setTitle(R.string.secure_view_action_dialog_title)
                .setMessage(message)
                .setNeutralButton(resources.getString(
                    R.string.secure_view_action_dialog_dismiss), null)
                .show()
        }
    }

    /**
     * Sets the `OnTouchListener` of its [Button] parameter [button] to an anonymous class whose
     * `onTouch` override prevents touches of [button] from reaching its `OnClickListener` if the
     * button is obscured (and pops up an [AlertDialog] to inform the user about what it did).
     *
     * @param button [Button] whose `OnTouchListener` we are to set
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchFilter(button: Button) {
        button.setOnTouchListener(object : OnTouchListener {
            /**
             * Called when a touch event is dispatched to a view. This allows listeners to get a
             * chance to respond before the target view. We fetch the flags from our [MotionEvent]
             * parameter [event], mask off the FLAG_WINDOW_IS_OBSCURED bit (if 1 it indicates that
             * the window that received this motion event is partly or wholly obscured by another
             * visible window above it), and if it is not zero we check to see if the action of
             * [event] is ACTION_UP in which case we build and show an [AlertDialog] with the title
             * resource ID R.string.secure_view_caught_dialog_title ("Saved!"), the message resource
             * ID R.string.secure_view_caught_dialog_message ("Careful! There appears to be another
             * window partly obscuring this window... Something unutterably HORRIBLE might have
             * happened."), set the text of its neutral button to the string with ID
             * R.string.secure_view_caught_dialog_dismiss ("Phew!"), then show the dialog. We then
             * return true to the caller to prevent the button from processing the touch.
             *
             * If the window is not obscured we return false to the caller, allowing the click to
             * pass through to the button's `OnClickListener`.
             *
             * @param v The view the touch event has been dispatched to.
             * @param event The [MotionEvent] object containing full information about the event.
             * @return True if the listener has consumed the event, false otherwise.
             */
            @TargetApi(Build.VERSION_CODES.GINGERBREAD)
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if ((event.flags and MotionEvent.FLAG_WINDOW_IS_OBSCURED) != 0) {
                    if (event.action == MotionEvent.ACTION_UP) {
                        AlertDialog.Builder(this@SecureView)
                            .setTitle(R.string.secure_view_caught_dialog_title)
                            .setMessage(R.string.secure_view_caught_dialog_message)
                            .setNeutralButton(resources.getString(
                                R.string.secure_view_caught_dialog_dismiss), null)
                            .show()
                    }
                    // Return true to prevent the button from processing the touch.
                    return true
                }
                return false
            }
        })
    }
}