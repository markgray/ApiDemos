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

package com.example.android.apis.accessibility;

import com.example.android.apis.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;


/**
 * Acts as a go-between for all AccessibilityEvents sent from items in the ListView, providing the
 * option of sending more context to an AccessibilityService by adding more AccessibilityRecords to
 * an event.
 */
public class TaskListView extends ListView {

    /**
     * Perform inflation from XML. We just call our super's constructor.
     *
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param attributeSet The attributes of the XML tag that is inflating the view.
     */
    public TaskListView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /**
     * This method will fire whenever a child event wants to send an AccessibilityEvent. As a
     * result, it's a great place to add more AccessibilityRecords, if you want. In this case,
     * the code is grabbing the position of the item in the list, and assuming that to be the
     * priority for the task.
     *
     *
     *
     * @param child The child which requests sending the event.
     * @param event The event to be sent.
     * @return True if the event should be sent.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onRequestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        // Add a record for ourselves as well.
        AccessibilityEvent record = AccessibilityEvent.obtain();
        super.onInitializeAccessibilityEvent(record);

        int priority = (Integer) child.getTag();
        String priorityStr = "Priority: " + priority;
        record.setContentDescription(priorityStr);

        event.appendRecord(record);
        return true;
    }
}

/**
 * Adds Accessibility information to individual child views of rows in the list.
 */
@SuppressWarnings("WeakerAccess")
final class TaskAdapter extends BaseAdapter {

    private String[] mLabels = null;
    private boolean[] mCheckboxes = null;
    private Context mContext = null;

    public TaskAdapter(Context context, String[] labels, boolean[] checkboxes) {
        super();
        mContext = context;
        mLabels = labels;
        mCheckboxes = checkboxes;
    }

    @Override
    public int getCount() {
        return mLabels.length;
    }

    /**
     * Expands the views for individual list entries, and sets content descriptions for use by the
     * TaskBackAccessibilityService.
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.tasklist_row, parent, false);
        }

        CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.tasklist_finished);
        checkbox.setChecked(mCheckboxes[position]);

        TextView label = (TextView) (convertView.findViewById(R.id.tasklist_label));
        label.setText(mLabels[position]);

        String contentDescription = new StringBuilder()
                .append(mContext.getString(R.string.task_name))
                .append(' ')
                .append(mLabels[position]).toString();
        label.setContentDescription(contentDescription);

        convertView.setTag(position);

        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return mLabels[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
