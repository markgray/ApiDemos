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
package com.example.android.apis.view

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.ExpandableListActivity
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.*
import android.widget.ExpandableListView.ExpandableListContextMenuInfo
import com.example.android.apis.R

/**
 * Demonstrates expandable lists using a custom [ExpandableListAdapter] derived from
 * [BaseExpandableListAdapter]. The custom [BaseExpandableListAdapter] groups
 * different child lists under group names: "People Names", "Dog Names", "Cat Names",
 * and "Fish Names". When clicked the groups expand to show the child lists, any child
 * or group which is long-pressed will pop up a context menu with an "action button".
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class ExpandableList1 : ExpandableListActivity() {
    /**
     * The [ExpandableListAdapter] which serves as the [ListAdapter] for our activity.
     * (It is actually a [MyExpandableListAdapter] that is initialized in [onCreate]).
     */
    var mAdapter: ExpandableListAdapter? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. We initialize our [ExpandableListAdapter] field [mAdapter] with a new instance
     * of [MyExpandableListAdapter], and provide it as the adapter for the expandable list. We fetch
     * our activity's expandable list view widget and register it for a context menu to be shown
     * (this will set the [View.OnCreateContextMenuListener] of the [View] to "this").
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up our adapter
        mAdapter = MyExpandableListAdapter()
        setListAdapter(mAdapter)
        registerForContextMenu(expandableListView)
    }

    /**
     * Called when the context menu for this view is being built after the expandable list view
     * widget is long clicked. First we set the [ContextMenu] parameter [menu] header's title to
     * the string "Sample menu".
     *
     * Then we add a menu item with the title R.string.expandable_list_sample_action
     * ("Sample action").
     *
     * @param menu     The context menu that is being built
     * @param v        The view for which the context menu is being built
     * @param menuInfo Extra information about the item for which the context menu should be shown.
     * This information will vary depending on the class of v.
     */
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        menu.setHeaderTitle("Sample menu")
        menu.add(0, 0, 0, R.string.expandable_list_sample_action)
    }

    /**
     * This hook is called whenever an item in a context menu is selected. First we initialize our
     * [ExpandableListContextMenuInfo] variable `val info` to the extra information linked to the
     * [View] that added [MenuItem] parameter [item] to the menu. We initialize [String] `val title`
     * by casting the view for which the context menu is being displayed to a [TextView], fetching
     * its text, and converting it to a string. We initialize `val type` to the type of the position
     * contained within the packed position `info.packedPosition`, either PACKED_POSITION_TYPE_CHILD,
     * PACKED_POSITION_TYPE_GROUP, or PACKED_POSITION_TYPE_NULL. Then if `type` is
     * PACKED_POSITION_TYPE_CHILD we fetch the group position from the packed position
     * `info.packedPosition` to set our variable `val groupPos`, and the child position from it to
     * set `val childPos`. We then create and show a toast using a string made from concatenating
     * the `title` (which will be the child name) with the string ": Child ", the `childPos`, the
     * string " clicked in group ", followed by `groupPos`. If type is PACKED_POSITION_TYPE_GROUP we
     * fetch the group position from the packed position `info.packedPosition` to set our variable
     * `val groupPos`. We then create and show a toast using a string made from concatenating the
     * `title` (which will be the group name) with the string ": Group ", the `groupPos`, followed
     * by the string " clicked". In both cases we return true to the caller to consume the menu
     * selection here. If the `type` is not one of the two above we return false to the caller to
     * allow normal context menu processing to proceed.
     *
     * @param item The context menu item that was selected.
     * @return boolean Return false to allow normal context menu processing to
     * proceed, true to consume it here.
     */
    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as ExpandableListContextMenuInfo
        val title = (info.targetView as TextView).text.toString()
        val type = ExpandableListView.getPackedPositionType(info.packedPosition)
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            val groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition)
            val childPos = ExpandableListView.getPackedPositionChild(info.packedPosition)
            Toast.makeText(this, "$title: Child $childPos clicked in group $groupPos",
                    Toast.LENGTH_SHORT).show()
            return true
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            val groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition)
            Toast.makeText(this, "$title: Group $groupPos clicked", Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }

    /**
     * A simple adapter which is based on two hardcoded `String[]` arrays, one containing the
     * four group names, and the other containing `String[]` arrays for each of the groups.
     */
    inner class MyExpandableListAdapter : BaseExpandableListAdapter() {
        /**
         * Names of the four groups in our expandable list, children[ i ] contains the children
         * (String[]) for groups[ i ].
         */
        private val groups = arrayOf(
                "People Names",
                "Dog Names",
                "Cat Names",
                "Fish Names"
        )

        /**
         * `String[]` arrays containing the names of the children belong to each of the groups
         * in `String[] groups`, children[ i ] contains the children (String[]) for groups[ i ].
         */
        private val children = arrayOf(
                arrayOf("Arnold", "Barry", "Chuck", "David"),
                arrayOf("Ace", "Bandit", "Cha-Cha", "Deuce"),
                arrayOf("Fluffy", "Snuggles"),
                arrayOf("Goldy", "Bubbles")
        )

        /**
         * Gets the data associated with the given child within the given group.
         *
         * @param groupPosition the position of the group that the child resides in
         * @param childPosition the position of the child with respect to other children in the group
         * @return the data of the child
         */
        override fun getChild(groupPosition: Int, childPosition: Int): Any {
            return children[groupPosition][childPosition]
        }

        /**
         * Gets the ID for the given child within the given group. We simply return our parameter
         * [childPosition] to the caller.
         *
         * @param groupPosition the position of the group that contains the child
         * @param childPosition the position of the child within the group for which the ID is wanted
         * @return the ID associated with the child
         */
        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return childPosition.toLong()
        }

        /**
         * Gets the number of children in a specified group. We simply return the size of the
         * `String[]` array in [children] for the group [groupPosition].
         *
         * @param groupPosition the position of the group for which the children count should be returned
         * @return the children count in the specified group
         */
        override fun getChildrenCount(groupPosition: Int): Int {
            return children[groupPosition].size
        }

        // Layout parameters for the ExpandableListView
        // Center the text vertically
        // Set the text starting position
        // Set the text alignment

        /**
         * Creates, configures and returns a [TextView] that can be used to display a child in our
         * expandable list. First we create [AbsListView.LayoutParams] `val lp` with a width of
         * MATCH_PARENT, and a height of 64. We create a new instance for [TextView] `val textView`,
         * set its layout parameters to `lp`, set its gravity to CENTER_VERTICAL and LEFT, set
         * its padding to 36 (start), 0 (top), 0 (end), and 0 (bottom), set its text alignment to
         * TEXT_ALIGNMENT_VIEW_START, and set its text size to 18sp. Finally we return `textView`
         * to our caller.
         *
         * @return a [TextView] configured for use in our expandable list.
         */
        @get:SuppressLint("RtlHardcoded")
        val genericView: TextView
            get() {
                // Layout parameters for the ExpandableListView
                val lp = AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        64
                )
                val textView = TextView(this@ExpandableList1)
                textView.layoutParams = lp
                // Center the text vertically
                textView.gravity = Gravity.CENTER_VERTICAL or Gravity.LEFT
                // Set the text starting position
                textView.setPaddingRelative(36, 0, 0, 0)
                // Set the text alignment
                textView.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                return textView
            }

        /**
         * Gets a View that displays the data for the given child within the given group. First we
         * set [TextView] `val textView` to a text view created and configured by our method
         * `getGenericView` (kotlin prefers to consider it the [genericView] property), then we set
         * its text to the string that our override of [getChild] returns for group [groupPosition]
         * and child [childPosition]. Finally we return `textView` to the caller.
         *
         * @param groupPosition the position of the group that contains the child
         * @param childPosition the position of the child within the group
         * @param isLastChild   Whether the child is the last child within the group
         * @param convertView   the old view to reuse, if possible. We do not bother.
         * @param parent        the parent that this view will eventually be attached to
         * @return the View corresponding to the child at the specified position
         */
        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean,
                                  convertView: View?, parent: ViewGroup): View {
            val textView = genericView
            textView.text = getChild(groupPosition, childPosition).toString()
            return textView
        }

        /**
         * Gets the data associated with the given group. Simply returns the string contained in
         * the position [groupPosition] of [groups].
         *
         * @param groupPosition the position of the group
         * @return the string title of the specified group
         */
        override fun getGroup(groupPosition: Int): Any {
            return groups[groupPosition]
        }

        /**
         * Gets the number of groups. We simply return the size of our `String[]` array [groups].
         *
         * @return the number of groups
         */
        override fun getGroupCount(): Int {
            return groups.size
        }

        /**
         * Gets the ID for the group at the given position. We simply return the [groupPosition]
         * parameter to the caller.
         *
         * @param groupPosition the position of the group for which the ID is wanted
         * @return the ID associated with the group
         */
        override fun getGroupId(groupPosition: Int): Long {
            return groupPosition.toLong()
        }

        /**
         * Gets a [View] that displays the given group. First we set [TextView] `val textView` to a
         * text view created and configured by our method `getGenericView` (kotlin prefers to
         * consider it the [genericView] property), then we set its text to the string returned by
         * our override of [getGroup] returns. Finally we return `textView` to the caller.
         *
         * @param groupPosition the position of the group for which the View is returned
         * @param isExpanded    whether the group is expanded or collapsed
         * @param convertView   the old view to reuse, if possible. We do not bother.
         * @param parent        the parent that this view will eventually be attached to
         * @return the View corresponding to the group at the specified position
         */
        override fun getGroupView(
                groupPosition: Int,
                isExpanded: Boolean,
                convertView: View?,
                parent: ViewGroup
        ): View {
            val textView = genericView
            textView.text = getGroup(groupPosition).toString()
            return textView
        }

        /**
         * Whether the child at the specified position is selectable. We always return true.
         *
         * @param groupPosition the position of the group that contains the child
         * @param childPosition the position of the child within the group
         * @return whether the child is selectable. We always return true.
         */
        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
            return true
        }

        /**
         * Indicates whether the child and group IDs are stable across changes to the underlying data.
         * We always return true.
         *
         * @return whether or not the same ID always refers to the same object. We always return true.
         */
        override fun hasStableIds(): Boolean {
            return true
        }
    }
}