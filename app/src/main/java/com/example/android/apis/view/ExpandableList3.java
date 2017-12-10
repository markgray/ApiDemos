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

package com.example.android.apis.view;

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.widget.ExpandableListAdapter;
import android.widget.SimpleExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates expandable lists backed by a Simple Map-based adapter, which is created using
 * SimpleExpandableListAdapter
 */
public class ExpandableList3 extends ExpandableListActivity {
    /**
     * Key in the {@code Map} objects for the "name" of the group or the child
     */
    private static final String NAME = "NAME";
    /**
     * Key in the {@code Map} objects for the "is even" data of the group or the child
     */
    private static final String IS_EVEN = "IS_EVEN";

    /**
     * {@code SimpleExpandableListAdapter} created from a list of maps, and a list of list of maps,
     * it is used as the {@code ExpandableListAdapter} of our {@code ExpandableListActivity}
     */
    @SuppressWarnings("FieldCanBeLocal")
    private ExpandableListAdapter mAdapter;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Then we declare {@code List<Map<String, String>> groupData} to be a list of
     * maps that we will use as our "groupData" when creating {@code mAdapter}, and then we declare
     * {@code List<List<Map<String, String>>> childData} to be a list of list of maps that we will
     * use as our "childData" (each entry in the outer List corresponds to a group (index by group
     * position), each entry in the inner List corresponds to a child within the group (index by child
     * position), and the Map corresponds to the data for a child).
     * <p>
     * Now we loop over {@code int i} for the 20 groups we are to create, first creating a new instance
     * of {@code HashMap<>} for {@code Map<String, String> curGroupMap}. We add {@code curGroupMap} to
     * the list {@code groupData}, then we put the string "Group " with the index {@code i} appended to
     * it in {@code curGroupMap} under the key NAME, and store under the key IS_EVEN the string
     * "This group is even" if {@code (i % 2 == 0)} otherwise we store the string "This group is odd".
     * <p>
     * We create the list {@code List<Map<String, String>> children}, then loop over {@code int j} for
     * the 15 children we will create for each group, first creating a new instance of {@code HashMap<>}
     * for {@code Map<String, String> curChildMap}. We add {@code curChildMap} to the list {@code children},
     * then we put the string "Child " with the index {@code j} appended to it in {@code curChildMap}
     * under the key NAME, and store under the key IS_EVEN the string "This child is even" if
     * {@code (j % 2 == 0)} otherwise we store the string "This child is odd". After adding all of the
     * children for the current group to {@code children}, we ad {@code children} to {@code childData}
     * and loop for the next group.
     * <p>
     * Now that we have initialized our {@code groupData} list of maps, and our {@code childData} list
     * of list of maps we use them to initialize our field {@code ExpandableListAdapter mAdapter} with
     * a new instance of {@code SimpleExpandableListAdapter} which uses:
     * <ul>
     * <li>
     * this - as the context where the ExpandableListView associated with this adapter is running
     * </li>
     * <li>
     * {@code groupData} - A List of Maps. Each entry in the List corresponds to one group in
     * the list. The Maps contain the data for each group.
     * </li>
     * <li>
     * android.R.layout.simple_expandable_list_item_2 - The {@code groupLayout}, a resource
     * identifier of a view layout that defines the views for a group.
     * </li>
     * <li>
     * {@code String[]{NAME, IS_EVEN}} - The {@code groupFrom} - A list of keys that will be
     * fetched from the Map associated with each group.
     * </li>
     * <li>
     * {@code int[]{android.R.id.text1, android.R.id.text2}} - The {@code groupTo} - The group
     * views that should display column in the "groupFrom" parameter.
     * </li>
     * <li>
     * {@code childData} - The {@code childData} - A List of List of Maps. Each entry in the
     * outer List corresponds to a group (index by group position), each entry in the inner
     * List corresponds to a child within the group (index by child position), and the Map
     * corresponds to the data for a child (index by values in the childFrom array).
     * </li>
     * <li>
     * android.R.layout.simple_expandable_list_item_2 - The {@code childLayout} - resource
     * identifier of a view layout that defines the views for a child.
     * </li>
     * <li>
     * {@code String[]{NAME, IS_EVEN}} - The {@code childFrom} - A list of keys that will be
     * fetched from the Map associated with each child.
     * </li>
     * <li>
     * {@code int[]{android.R.id.text1, android.R.id.text2}} - The {@code childTo} - The child
     * views that should display the columns in the "childFrom" parameter.
     * </li>
     * </ul>
     * Finally we set {@code mAdapter} as our list adapter.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<Map<String, String>> groupData = new ArrayList<>();
        List<List<Map<String, String>>> childData = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Map<String, String> curGroupMap = new HashMap<>();
            groupData.add(curGroupMap);
            curGroupMap.put(NAME, "Group " + i);
            curGroupMap.put(IS_EVEN, (i % 2 == 0) ? "This group is even" : "This group is odd");

            List<Map<String, String>> children = new ArrayList<>();
            for (int j = 0; j < 15; j++) {
                Map<String, String> curChildMap = new HashMap<>();
                children.add(curChildMap);
                curChildMap.put(NAME, "Child " + j);
                curChildMap.put(IS_EVEN, (j % 2 == 0) ? "This child is even" : "This child is odd");
            }
            childData.add(children);
        }

        // Set up our adapter
        mAdapter = new SimpleExpandableListAdapter(
                this,
                groupData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{NAME, IS_EVEN},
                new int[]{android.R.id.text1, android.R.id.text2},
                childData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{NAME, IS_EVEN},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        setListAdapter(mAdapter);
    }

}
