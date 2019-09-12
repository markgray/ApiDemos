package com.example.android.apis.accessibility

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView

import com.example.android.apis.R

/**
 * Adds Accessibility information to individual child views of rows in the list.
 */
class TaskAdapter
/**
 * Our constructor. First we call our super's constructor, then we save our parameters
 * `Context context` in our field `Context mContext`, `String[] labels`
 * in our field `String[] mLabels` and `boolean[] checkboxes` in our field
 * `boolean[] mCheckboxes`.
 *
 * @param context    `Context` to use to access resources
 * @param labels     labels to use for our checkboxes
 * @param checkboxes initial state of our checkboxes
 */
(context: Context, labels: Array<String>, checkboxes: BooleanArray) : BaseAdapter() {
    /**
     * Labels to use for our checkboxes, set by our constructor
     */
    private var mLabels: Array<String>? = null
    /**
     * Current state of our checkboxes (true if checked, false if unchecked)
     */
    private var mCheckboxes: BooleanArray? = null
    /**
     * `Context` passed to our constructor ("this" in the `onCreate` method of
     * `TaskListActivity`
     */
    private var mContext: Context? = null

    init {
        mContext = context
        mLabels = labels
        mCheckboxes = checkboxes
    }

    /**
     * How many items are in the data set represented by this Adapter. We just return the length of
     * our field `String[] mLabels`.
     *
     * @return Count of items.
     */
    override fun getCount(): Int {
        return mLabels!!.size
    }

    /**
     * Get a View that displays the data at the specified position in the data set. Expands the views
     * for individual list entries, and sets content descriptions for use by the TaskBackAccessibilityService.
     *
     *
     * If our parameter `View convertView` is null we initialize `LayoutInflater inflater`
     * with the LayoutInflater from context `mContext` and use it to inflate the layout file
     * R.layout.tasklist_row into a view to use to set `convertView`. We initialize our variable
     * `CheckBox checkbox` by finding the view in `convertView` with id R.id.tasklist_finished
     * and set its checked state to the value stored in `mCheckboxes[position]`. We initialize
     * `TextView label` by finding the view in `convertView` with id R.id.tasklist_label
     * and set its text to the contents of `mLabels[position]`. We initialize our variable
     * `String contentDescription` to the string formed by concatenating the string R.string.task_name
     * ("Task") to a space followed by the contents of `mLabels[position]`, and use it to set the
     * content description of `label`. We then set the tag of `convertView` to `position`
     * and return `convertView` to the caller.
     *
     * @param position    The position of the item within the adapter's data set whose view we want.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertViewLocal = convertView
        if (convertViewLocal == null) {
            val inflater = LayoutInflater.from(mContext)
            convertViewLocal = inflater.inflate(R.layout.tasklist_row, parent, false)
        }

        val checkbox = convertViewLocal!!.findViewById<CheckBox>(R.id.tasklist_finished)
        checkbox.isChecked = mCheckboxes!![position]

        val label = convertViewLocal.findViewById<TextView>(R.id.tasklist_label)
        label.text = mLabels!![position]

        val contentDescription = StringBuilder()
                .append(mContext!!.getString(R.string.task_name))
                .append(' ')
                .append(mLabels!![position]).toString()
        label.contentDescription = contentDescription

        convertViewLocal.tag = position

        return convertViewLocal
    }

    /**
     * Get the data item associated with the specified position in the data set. We return the contents
     * of `mLabels[position]` to the caller.
     *
     * @param position Position of the item whose data we want
     * @return The data at the specified position.
     */
    override fun getItem(position: Int): Any {
        return mLabels!![position]
    }

    /**
     * Get the row id associated with the specified position in the list. We just return our parameter
     * `position` to the caller.
     *
     * @param position The position of the item within the data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
