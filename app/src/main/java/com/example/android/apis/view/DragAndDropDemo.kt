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
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.view.View.OnDragListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Shows how to implement draggable views.
 * RequiresApi(Build.VERSION_CODES.HONEYCOMB)
 */
@SuppressLint("SetTextI18n")
class DragAndDropDemo : AppCompatActivity() {
    /**
     * Target [TextView] to display some messages in.
     */
    private var mResultText: TextView? = null

    /**
     * This [DraggableDot] is invisible until we receive an ACTION_DRAG_STARTED [DragEvent]
     * whereupon we set it to be visible.
     */
    private var mHiddenDot: DraggableDot? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.drag_layout. We
     * initialize [TextView] `val text` by finding the view with ID R.id.drag_text, then we locate
     * the views with ID R.id.drag_dot_1, R.id.drag_dot_2, and R.id.drag_dot_3 in turn and call
     * their method `setReportView(text)`. We initialize our [DraggableDot] field [mHiddenDot] by
     * finding the view with ID R.id.drag_dot_hidden, and call its method `setReportView(text)`
     * (`setReportView(text)` saves `text` in the instance's [TextView] field `mReportView` so
     * that it can write to it when it wants to). We initialize our [TextView] field [mResultText]
     * by finding the view with ID R.id.drag_result_text, and our variable [View] `val mainView` by
     * finding the view with ID R.id.drag_main. Finally we set the [OnDragListener] of `mainView`
     * to a lambda which writes info about the [DragEvent] it receives to [mResultText], as well as
     * monkeying with the visibility of [DraggableDot] field [mHiddenDot].
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drag_layout)
        val text = findViewById<TextView>(R.id.drag_text)
        var dot = findViewById<DraggableDot>(R.id.drag_dot_1)
        dot.setReportView(text)
        dot = findViewById(R.id.drag_dot_2)
        dot.setReportView(text)
        dot = findViewById(R.id.drag_dot_3)
        dot.setReportView(text)
        mHiddenDot = findViewById(R.id.drag_dot_hidden)
        mHiddenDot!!.setReportView(text)
        mResultText = findViewById(R.id.drag_result_text)
        val mainView = findViewById<View>(R.id.drag_main)
        mainView.setOnDragListener(OnDragListener { v: View, event: DragEvent ->
            /**
             * Called when a drag event is dispatched to a view. This allows listeners to get a
             * chance to override base [View] behavior. We retrieve the action from our [DragEvent]
             * parameter `event` to our variable `int action`, then switch on it as follows:
             *
             *  * ACTION_DRAG_STARTED - we set the text of `TextView mResultText` to the string
             *  "ACTION_DRAG_STARTED", set the visibility of `DraggableDot mHiddenDot` to
             *  visible, then return true so that we will get more drag events.
             *
             *  * ACTION_DRAG_ENTERED - we set the text of `TextView mResultText` to the string
             *  "ACTION_DRAG_ENTERED" and break
             *
             *  * ACTION_DRAG_EXITED - we set the text of `TextView mResultText` to the string
             *  "ACTION_DRAG_EXITED" and break
             *
             *  * ACTION_DROP - we set the text of `TextView mResultText` to the string
             *  "ACTION_DROP" and break
             *
             *  * ACTION_DRAG_ENDED - we set the visibility of `DraggableDot mHiddenDot`
             *  back to invisible, and fetch the result of our parameter `DragEvent event`
             *  to `boolean dropped` and if it is true we set the text of `TextView mResultText`
             *  to the string "Dropped", otherwise we set it to "No drop". Finally we break.
             *
             *  * default - we set the text of `TextView mResultText` to the string value
             *  of `action` and break
             *
             * If we have executed a break instead of a return above, we now return false.
             *
             * Parameter: v     The View that received the drag event.
             * Parameter: event The [DragEvent] object for the drag event.
             * @return `true` if the drag event was handled successfully, or `false`
             * if the drag event was not handled. We need to return true from ACTION_DRAG_STARTED
             * if we want to receive any more drag events.
             */
            val action: Int = event.action
            when (action) {
                DragEvent.ACTION_DRAG_STARTED -> {

                    // Bring up a fourth draggable dot on the fly. Note that it
                    // is properly notified about the ongoing drag, and lights up
                    // to indicate that it can handle the current content.
                    mResultText!!.text = "ACTION_DRAG_STARTED"
                    mHiddenDot!!.visibility = View.VISIBLE
                    return@OnDragListener true
                }

                DragEvent.ACTION_DRAG_ENTERED -> {
                    mResultText!!.text = "ACTION_DRAG_ENTERED"
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    mResultText!!.text = "ACTION_DRAG_EXITED"
                }

                DragEvent.ACTION_DROP -> {
                    mResultText!!.text = "ACTION_DROP"
                }

                DragEvent.ACTION_DRAG_ENDED -> {

                    // Hide the surprise again
                    mHiddenDot!!.visibility = View.INVISIBLE

                    // Report the drop/no-drop result to the user
                    val dropped: Boolean = event.result
                    mResultText!!.text = if (dropped) "Dropped!" else "No drop"
                }

                else -> {
                    mResultText!!.text = action.toString()
                }
            }
            false
        })
    }
}