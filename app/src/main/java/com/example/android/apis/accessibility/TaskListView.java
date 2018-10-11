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
     * @param context      The Context the view is running in, through which it can
     *                     access the current theme, resources, etc.
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
     * <p>
     * We initialize our variable {@code AccessibilityEvent record} by retrieving a cached instance
     * if available or a new instance if not. We then pass {@code record} to our super's method
     * {@code onInitializeAccessibilityEvent} which will initialize it with information about the
     * View which is the event source. We retrieve the tag of our parameter {@code View child} to
     * initialize our variable {@code int priority} then append its string value to the string
     * "Priority: " to initialize {@code String priorityStr}. We set the content description of
     * {@code record} to {@code priorityStr} and append {@code record} to our parameter {@code event}.
     * Finally we return true so that the event will be sent.
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
    /**
     * Labels to use for our checkboxes, set by our constructor
     */
    @SuppressWarnings("UnusedAssignment")
    private String[] mLabels = null;
    /**
     * Current state of our checkboxes (true if checked, false if unchecked)
     */
    @SuppressWarnings("UnusedAssignment")
    private boolean[] mCheckboxes = null;
    /**
     * {@code Context} passed to our constructor ("this" in the {@code onCreate} method of
     * {@code TaskListActivity}
     */
    @SuppressWarnings("UnusedAssignment")
    private Context mContext = null;

    /**
     * Our constructor. First we call our super's constructor, then we save our parameters
     * {@code Context context} in our field {@code Context mContext}, {@code String[] labels}
     * in our field {@code String[] mLabels} and {@code boolean[] checkboxes} in our field
     * {@code boolean[] mCheckboxes}.
     *
     * @param context    {@code Context} to use to access resources
     * @param labels     labels to use for our checkboxes
     * @param checkboxes initial state of our checkboxes
     */
    public TaskAdapter(Context context, String[] labels, boolean[] checkboxes) {
        super();
        mContext = context;
        mLabels = labels;
        mCheckboxes = checkboxes;
    }

    /**
     * How many items are in the data set represented by this Adapter. We just return the length of
     * our field {@code String[] mLabels}.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return mLabels.length;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. Expands the views
     * for individual list entries, and sets content descriptions for use by the TaskBackAccessibilityService.
     * <p>
     * If our parameter {@code View convertView} is null we initialize {@code LayoutInflater inflater}
     * with the LayoutInflater from context {@code mContext} and use it to inflate the layout file
     * R.layout.tasklist_row into a view to use to set {@code convertView}. We initialize our variable
     * {@code CheckBox checkbox} by finding the view in {@code convertView} with id R.id.tasklist_finished
     * and set its checked state to the value stored in {@code mCheckboxes[position]}. We initialize
     * {@code TextView label} by finding the view in {@code convertView} with id R.id.tasklist_label
     * and set its text to the contents of {@code mLabels[position]}. We initialize our variable
     * {@code String contentDescription} to the string formed by concatenating the string R.string.task_name
     * ("Task") to a space followed by the contents of {@code mLabels[position]}, and use it to set the
     * content description of {@code label}. We then set the tag of {@code convertView} to {@code position}
     * and return {@code convertView} to the caller.
     *
     * @param position    The position of the item within the adapter's data set whose view we want.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.tasklist_row, parent, false);
        }

        CheckBox checkbox = convertView.findViewById(R.id.tasklist_finished);
        checkbox.setChecked(mCheckboxes[position]);

        TextView label = convertView.findViewById(R.id.tasklist_label);
        label.setText(mLabels[position]);

        String contentDescription = new StringBuilder()
                .append(mContext.getString(R.string.task_name))
                .append(' ')
                .append(mLabels[position]).toString();
        label.setContentDescription(contentDescription);

        convertView.setTag(position);

        return convertView;
    }

    /**
     * Get the data item associated with the specified position in the data set. We return the contents
     * of {@code mLabels[position]} to the caller.
     *
     * @param position Position of the item whose data we want
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return mLabels[position];
    }

    /**
     * Get the row id associated with the specified position in the list. We just return our parameter
     * {@code position} to the caller.
     *
     * @param position The position of the item within the data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }
}
