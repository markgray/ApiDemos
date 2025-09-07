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

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import com.example.android.apis.R

/**
 * Part of the [Hover] demo activity.
 *
 * The Interceptor view is a simple subclass of LinearLayout whose sole purpose
 * is to override [.onInterceptHoverEvent].  When the checkbox in the
 * hover activity is checked, the interceptor view will intercept hover events.
 *
 * When this view intercepts hover events, its children will not receive
 * hover events.  This can be useful in some cases when implementing a custom
 * view group that would like to prevent its children from being hovered
 * under certain situations.  Usually such custom views will be much more
 * interesting and complex than our little Interceptor example here.
 *
 * @param context The Context the view is running in, through which it can access the current
 * theme, resources, etc.
 * @param attrs   The attributes of the XML tag that is inflating the view.
 */
class HoverInterceptorView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    /**
     * Flag to indicate whether we should intercept hover events or not (true we intercept them,
     * false we let them through to our children).
     */
    private var mInterceptHover = false

    /**
     * We implement this method to intercept hover events before they are handled by child views. If
     * our [Boolean] field [mInterceptHover] is true, we return true to our caller, thereby
     * intercepting the hover event. Otherwise we return the value returned by our super's
     * implementation of `onInterceptHoverEvent`.
     *
     * @param event The motion event that describes the hover.
     * @return True if the view group would like to intercept the hover event
     * and prevent its children from receiving it.
     */
    override fun onInterceptHoverEvent(event: MotionEvent): Boolean {
        return if (mInterceptHover) {
            true
        } else super.onInterceptHoverEvent(event)
    }

    /**
     * We implement this method to handle hover events. We initialize our [TextView] variable
     * `val textView` by finding the view with ID R.id.intercept_message (it is the child view
     * we use to display status messages). If our field [mInterceptHover] is true and the
     * [MotionEvent] parameter [event] is not a ACTION_HOVER_EXIT event, we set the text of
     * `textView` to the string given by the resource ID R.string.hover_intercept_message_intercepted
     * ("Intercepted hover event instead of sending it to the button.  Om nom nom!") and return true
     * to the caller. Otherwise we set the text of `textView` to the string with ID
     * R.string.hover_intercept_message_initial ("Try hovering over the button.") and return the
     * value returned by our super's implementation of `onHoverEvent` to the caller.
     *
     * @param event The motion event that describes the hover.
     * @return True if the view handled the hover event.
     */
    override fun onHoverEvent(event: MotionEvent): Boolean {
        val textView = findViewById<TextView>(R.id.intercept_message)
        if (mInterceptHover && event.action != MotionEvent.ACTION_HOVER_EXIT) {
            textView.text = resources.getString(R.string.hover_intercept_message_intercepted)
            return true
        }
        textView.text = resources.getString(R.string.hover_intercept_message_initial)
        return super.onHoverEvent(event)
    }

    /**
     * Setter for our [Boolean] field [mInterceptHover].
     *
     * @param intercept Value to set [Boolean] field [mInterceptHover] to
     */
    fun setInterceptHover(intercept: Boolean) {
        mInterceptHover = intercept
    }
}