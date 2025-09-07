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

import android.os.Bundle
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.SimpleExpandableListAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates expandable lists backed by a Simple Map-based adapter, which is created using
 * [SimpleExpandableListAdapter]
 */
class ExpandableList3 : AppCompatActivity() {

    /**
     * The [ExpandableListView] in our layout file with ID `R.id.list`
     */
    private lateinit var expandableList: ExpandableListView

    /**
     * [SimpleExpandableListAdapter] created from a list of maps, and a list of list of maps,
     * it is used as the [ExpandableListAdapter] of our [ExpandableListView]
     */
    private var mAdapter: ExpandableListAdapter? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Then we declare `List<Map<String, String>>` `val groupData` to be a list of
     * maps that we will use as our "groupData" when creating [mAdapter], and then we declare
     * `List<List<Map<String, String>>>` `val childData` to be a list of list of maps that we will
     * use as our "childData" (each entry in the outer List corresponds to a group (index by group
     * position), each entry in the inner List corresponds to a child within the group (index by
     * child position), and the Map corresponds to the data for a child).
     *
     * Now we loop over [Int] `i` for the 20 groups we are to create, first creating a new instance
     * of `HashMap<>` for `Map<String, String> curGroupMap`. We add `curGroupMap` to the list
     * `groupData`, then we put the string "Group " with the index `i` appended to it in
     * `curGroupMap` under the key NAME, and store under the key IS_EVEN the string "This group is
     * even" if `(i % 2 == 0)` otherwise we store the string "This group is odd".
     *
     * We create the list `List<Map<String, String>>` `val children`, then loop over [Int] `j` for
     * the 15 children we will create for each group, first creating a new instance of `HashMap<>`
     * for `Map<String, String> curChildMap`. We add `curChildMap` to the list `children`, then we
     * put the string "Child " with the index `j` appended to it in `curChildMap` under the key
     * NAME, and store under the key IS_EVEN the string "This child is even" if `(j % 2 == 0)`
     * otherwise we store the string "This child is odd". After adding all of the children for the
     * current group to `children`, we add `children` to `childData` and loop for the next group.
     *
     * Now that we have initialized our `groupData` list of maps, and our `childData` list
     * of list of maps we use them to initialize our [ExpandableListAdapter] field [mAdapter]
     * with a new instance of [SimpleExpandableListAdapter] which uses:
     *
     *  * this - as the context where the ExpandableListView associated with this adapter is running
     *
     *  * `groupData` - A List of Maps. Each entry in the List corresponds to one group in
     *  the list. The Maps contain the data for each group.
     *
     *  * android.R.layout.simple_expandable_list_item_2 - The `groupLayout`, a resource
     *  identifier of a view layout that defines the views for a group.
     *
     *  * `String[]{NAME, IS_EVEN}` - The `groupFrom` - A list of keys that will be
     *  fetched from the Map associated with each group.
     *
     *  * `int[]{android.R.id.text1, android.R.id.text2}` - The `groupTo` - The group
     *  views that should display column in the "groupFrom" parameter.
     *
     *  * `childData` - The `childData` - A List of List of Maps. Each entry in the
     *  outer List corresponds to a group (index by group position), each entry in the inner
     *  List corresponds to a child within the group (index by child position), and the Map
     *  corresponds to the data for a child (index by values in the childFrom array).
     *
     *  * android.R.layout.simple_expandable_list_item_2 - The `childLayout` - resource
     *  identifier of a view layout that defines the views for a child.
     *
     *  * `String[]{NAME, IS_EVEN}` - The `childFrom` - A list of keys that will be
     *  fetched from the Map associated with each child.
     *
     *  * `int[]{android.R.id.text1, android.R.id.text2}` - The `childTo` - The child
     *  views that should display the columns in the "childFrom" parameter.
     *
     * Finally we set [mAdapter] as our list adapter.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.expandable_list1)
        expandableList = findViewById(R.id.list)

        val groupData: MutableList<Map<String, String?>> = ArrayList()
        val childData: MutableList<List<Map<String, String?>>> = ArrayList()
        for (i in 0..19) {
            val curGroupMap: MutableMap<String, String?> = HashMap()
            groupData.add(curGroupMap)
            curGroupMap[NAME] = "Group $i"
            curGroupMap[IS_EVEN] = if (i % 2 == 0) "This group is even" else "This group is odd"
            val children: MutableList<Map<String, String?>> = ArrayList()
            for (j in 0..14) {
                val curChildMap: MutableMap<String, String?> = HashMap()
                children.add(curChildMap)
                curChildMap[NAME] = "Child $j"
                curChildMap[IS_EVEN] = if (j % 2 == 0) "This child is even" else "This child is odd"
            }
            childData.add(children)
        }

        // Set up our adapter
        mAdapter = SimpleExpandableListAdapter(
            /* context = */ this,
            /* groupData = */ groupData,
            /* groupLayout = */ android.R.layout.simple_expandable_list_item_2,
            /* groupFrom = */ arrayOf(NAME, IS_EVEN),
            /* groupTo = */ intArrayOf(android.R.id.text1, android.R.id.text2),
            /* childData = */ childData,
            /* childLayout = */ android.R.layout.simple_expandable_list_item_2,
            /* childFrom = */ arrayOf(NAME, IS_EVEN),
            /* childTo = */ intArrayOf(android.R.id.text1, android.R.id.text2)
        )
        expandableList.setAdapter(mAdapter)
    }

    companion object {
        /**
         * Key in the `Map` objects for the "name" of the group or the child
         */
        private const val NAME = "NAME"

        /**
         * Key in the `Map` objects for the "is even" data of the group or the child
         */
        private const val IS_EVEN = "IS_EVEN"
    }
}
