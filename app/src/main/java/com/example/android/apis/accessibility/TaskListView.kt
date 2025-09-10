/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.android.apis.accessibility

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.ListView


/**
 * Acts as a go-between for all AccessibilityEvents sent from items in the ListView, providing the
 * option of sending more context to an AccessibilityService by adding more AccessibilityRecords to
 * an event.
 * Perform inflation from XML. We just call our super's constructor.
 *
 * @param context      The Context the view is running in, through which it can
 * access the current theme, resources, etc.
 * @param attributeSet The attributes of the XML tag that is inflating the view.
 */
class TaskListView(context: Context, attributeSet: AttributeSet) : ListView(context, attributeSet) {

    /**
     * This method will fire whenever a child event wants to send an AccessibilityEvent. As a
     * result, it's a great place to add more AccessibilityRecords, if you want. In this case,
     * the code is grabbing the position of the item in the list, and assuming that to be the
     * priority for the task.
     *
     * We initialize our variable `AccessibilityEvent record` by retrieving a cached instance
     * if available or a new instance if not. We then pass `record` to our super's method
     * `onInitializeAccessibilityEvent` which will initialize it with information about the
     * View which is the event source. We retrieve the tag of our parameter `View child` to
     * initialize our variable `int priority` then append its string value to the string
     * "Priority: " to initialize `String priorityStr`. We set the content description of
     * `record` to `priorityStr` and append `record` to our parameter `event`.
     * Finally we return true so that the event will be sent.
     *
     * @param child The child which requests sending the event.
     * @param event The event to be sent.
     * @return True if the event should be sent.
     * RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     */
    override fun onRequestSendAccessibilityEvent(child: View, event: AccessibilityEvent): Boolean {
        // Add a record for ourselves as well.
        @Suppress("DEPRECATION") // Object pooling has been discontinued. Calling this function now will have no effect.
        val record = AccessibilityEvent.obtain()
        super.onInitializeAccessibilityEvent(record)

        val priority = child.tag as Int
        val priorityStr = "Priority: $priority"
        record.contentDescription = priorityStr

        event.appendRecord(record)
        return true
    }
}

