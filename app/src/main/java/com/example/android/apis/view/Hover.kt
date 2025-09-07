/*
 * Copyright (C) 2011 The Android Open Source Project
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
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates how to use [View.onHoverEvent], `ViewGroup.onInterceptHoverEvent`,
 * and [View.setOnHoverListener].
 *
 * This activity displays a few buttons and text fields and entices the user
 * to hover over them using a mouse or touch pad.  It displays feedback reporting
 * the position of the pointing device and the label of the view being hovered.
 *
 * A button changes from dark green to bright yellow when hovered.
 * This effect is achieved by using a state-list drawable to select among different
 * background shapes and colors based on the hover state of the button.
 *
 * A `View.OnHoverEventListener` is used to listen for hover events within the
 * container.  The container will re
 *
 * A [CheckBox] is used to control whether a special view, the Interceptor, will intercept
 * events before they are sent to its child (a button).  When the Interceptor
 * is intercepting events, the button will not change state as the pointer hovers
 * over it because the interceptor itself will grab the events.
 */
@SuppressLint("ObsoleteSdkInt")
@Suppress("UNUSED_ANONYMOUS_PARAMETER")
@RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class Hover : AppCompatActivity() {
    /**
     * [TextView] in our layout with ID R.id.message, it is used to display strings describing
     * the actions of the hover events received.
     */
    private var mMessageTextView: TextView? = null

    /**
     * [CheckBox] in our layout with ID R.id.intercept_checkbox, it is used to select whether
     * our [HoverInterceptorView] field [mInterceptor] intercepts hover events or not.
     */
    private var mInterceptCheckBox: CheckBox? = null

    /**
     * [HoverInterceptorView] in our layout with ID R.id.interceptor, it is a custom
     * `LinearLayout` whose sole purpose is to intercept hover events that would otherwise be
     * sent to its children if the [CheckBox] field [mInterceptCheckBox] is checked.
     */
    private var mInterceptor: HoverInterceptorView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.hover. We initialize
     * our [TextView] field [mMessageTextView] by finding the view with ID R.id.message, [CheckBox]
     * field [mInterceptCheckBox] by finding the view with ID R.id.intercept_checkbox, and
     * [HoverInterceptorView] field [mInterceptor] by finding the view with ID R.id.interceptor.
     *
     * We initialize our [View] variable `val container` by finding the view with ID R.id.container
     * and set its `OnHoverListener` to a lambda whose `onHover` method will set the text of
     * [TextView] field [mMessageTextView] to different strings depending on the action type of the
     * [MotionEvent] it receives.
     *
     * Finally we set the `OnCheckedChangeListener` of [CheckBox] field [mInterceptCheckBox] to
     * a lambda which calls the `setInterceptHover` method of [mInterceptor] with the new state of
     * the [CheckBox].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hover)
        mMessageTextView = findViewById(R.id.message)
        mInterceptCheckBox = findViewById(R.id.intercept_checkbox)
        mInterceptor = findViewById(R.id.interceptor)
        val container = findViewById<View>(R.id.container)
        container.setOnHoverListener { v: View, event: MotionEvent ->

            /**
             * Called when a hover event is dispatched to a view. This allows listeners to get a
             * chance to respond before the target view. We switch on the kind of action being
             * performed by the [MotionEvent] parameter [event]:
             *
             *  * ACTION_HOVER_ENTER - we set the text of [TextView] field [mMessageTextView] to a
             *  formatted string using the format R.string.hover_message_entered_at with the
             *  X and Y coordinates of [event] filling in the two float arguments, then break.
             *
             *  * ACTION_HOVER_MOVE - we set the text of [TextView] field [mMessageTextView] to a
             *  formatted string using the format R.string.hover_message_moved_at with the
             *  X and Y coordinates of [event] filling in the two float arguments, then break.
             *
             *  * ACTION_HOVER_EXIT - we set the text of [TextView] field [mMessageTextView] to a
             *  formatted string using the format R.string.hover_message_exited_at with the
             *  X and Y coordinates of [event] filling in the two float arguments, then break.
             *
             * Finally we return true to the caller.
             *
             * @param v The [View] the hover event has been dispatched to.
             * @param event The [MotionEvent] object containing full information about the event.
             * @return True if the listener has consumed the event, false otherwise.
             */
            when (event.action) {
                MotionEvent.ACTION_HOVER_ENTER ->
                    mMessageTextView!!.text = this@Hover.resources.getString(
                        /* id = */ R.string.hover_message_entered_at,
                        /* ...formatArgs = */ event.x, event.y
                    )

                MotionEvent.ACTION_HOVER_MOVE ->
                    mMessageTextView!!.text = this@Hover.resources.getString(
                        /* id = */ R.string.hover_message_moved_at,
                        /* ...formatArgs = */ event.x, event.y
                    )

                MotionEvent.ACTION_HOVER_EXIT ->
                    mMessageTextView!!.text = this@Hover.resources.getString(
                        /* id = */ R.string.hover_message_exited_at,
                        /* ...formatArgs = */ event.x, event.y
                    )
            }
            false
        }

        /**
         * Called when the checked state of a compound button has changed. We simply call the
         * `setInterceptHover` method of `HoverInterceptorView` field `mInterceptor` with
         * the new checked value `isChecked`.
         *
         *  buttonView: The compound button view whose state has changed.
         *  isChecked:  The new checked state of buttonView.
         */
        mInterceptCheckBox!!.setOnCheckedChangeListener { buttonView: CompoundButton,
                                                          isChecked: Boolean ->
            mInterceptor!!.setInterceptHover(isChecked)
        }
    }
}