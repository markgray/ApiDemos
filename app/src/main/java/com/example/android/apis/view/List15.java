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
package com.example.android.apis.view;

import com.example.android.apis.R;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * This demo illustrates the use of CHOICE_MODE_MULTIPLE_MODAL, a.k.a. selection mode on ListView.
 * It switches into selection mode on long press.
 */
public class List15 extends ListActivity {
    /**
     * Reference to the array that we use as our database.
     */
    private String[] mStrings = Cheeses.sCheeseStrings;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. We initialize our variable {@code ListView lv} by fetching a reference to
     * our {@code ListView}, set its choice mode to {@code CHOICE_MODE_MULTIPLE_MODAL}, and set its
     * {@code MultiChoiceModeListener} to a new instance of {@code ModeCallback}. Finally we set our
     * list adapter to a new instance of {@code ArrayAdapter} constructed to display our array
     * {@code mStrings} using the layout android.R.layout.simple_list_item_checked.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView lv = getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(new ModeCallback());
        setListAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_checked, mStrings));
    }

    /**
     * Called when activity start-up is complete (after {@code onStart} and {@code onRestoreInstanceState}
     * have been called). First we call through to our super's implementation of {@code onPostCreate},
     * then we fetch a reference to this activity's ActionBar and set its subtitle to the string:
     * "Long press to start selection".
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //noinspection ConstantConditions
        getActionBar().setSubtitle("Long press to start selection");
    }

    /**
     * Our custom {@code MultiChoiceModeListener}, becomes active when the {@code ListView} is long
     * clicked. It then inflates an action mode R.menu.list_select_menu which remains up as long as
     * at least one item is selected, or until the selected items are "shared"
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class ModeCallback implements ListView.MultiChoiceModeListener {
        /**
         * Called when action mode is first created. The menu supplied will be used to generate
         * action buttons for the action mode. First we initialize {@code MenuInflater inflater} by
         * setting it to the value returned by {@code getMenuInflater}, then we use it to inflate
         * our menu layout file R.menu.list_select_menu into our parameter {@code Menu menu}. We set
         * the title of our parameter {@code ActionMode mode} to the string "Select Items", then
         * call our method {@code setSubtitle} to set the subtitle of {@code mode} to a string
         * showing the number of items currently selected, or to null if no items are selected
         * anymore. Finally we return true to the caller so that the action mode will be created.
         *
         * @param mode ActionMode being created
         * @param menu Menu used to populate action buttons
         * @return true if the action mode should be created
         */
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.list_select_menu, menu);
            mode.setTitle("Select Items");
            setSubtitle(mode);
            return true;
        }

        /**
         * Called to refresh an action mode's action menu whenever it is invalidated. We always
         * return true to the caller.
         *
         * @param mode ActionMode being prepared
         * @param menu Menu used to populate action buttons
         * @return true if the menu or action mode was updated, false otherwise.
         */
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        /**
         * Called to report a user click on an action button. We switch on the item ID of our
         * parameter {@code MenuItem item}:
         * <ul>
         * <li>
         * R.id.share - we toast a message saying that the number of checked items have
         * been "shared", then call the {@code finish} method of our parameter
         * {@code ActionMode mode} to finish and close the action mode. Finally we break.
         * </li>
         * <li>
         * default - we toast a message stating that the item with the title of our parameter
         * {@code MenuItem item} has been clicked, then we break
         * </li>
         * </ul>
         * In all cases we return true to the caller signifying that we have handled the event.
         *
         * @param mode The current ActionMode
         * @param item The item that was clicked
         * @return true if this callback handled the event, false if the standard MenuItem
         * invocation should continue.
         */
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.share:
                    Toast.makeText(List15.this, "Shared " + getListView().getCheckedItemCount() +
                            " items", Toast.LENGTH_SHORT).show();
                    mode.finish();
                    break;
                default:
                    Toast.makeText(List15.this, "Clicked " + item.getTitle(),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }

        /**
         * Called when an action mode is about to be exited and destroyed. We ignore it.
         *
         * @param mode The current ActionMode being destroyed
         */
        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }

        /**
         * Called when an item is checked or unchecked during selection mode. We call our method
         * {@code setSubtitle} to set the subtitle of {@code mode} to a string showing the number of
         * items currently selected, or to null if no items are selected anymore.
         *
         * @param mode     The {@code ActionMode} providing the selection mode
         * @param position Adapter position of the item that was checked or unchecked
         * @param id       Adapter ID of the item that was checked or unchecked
         * @param checked  true if the item is now checked, false if the item is now unchecked.
         */
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            setSubtitle(mode);
        }

        /**
         * Sets the subtitle of our parameter {@code ActionMode mode} to a string showing the number
         * of items currently selected, or to null if no items are selected anymore. We initialize
         * our variable {@code int checkedCount} with the number of checked items in our
         * {@code ListView}, then switch on it:
         * <ul>
         * <li>
         * 0 - we set the subtitle of {@code ActionMode mode} to null and break.
         * </li>
         * <li>
         * 1 - we set the subtitle of {@code ActionMode mode} to the string "One item selected" and break.
         * </li>
         * <li>
         * default - we set the subtitle of {@code ActionMode mode} to the string value of {@code checkedCount}
         * concatenated to the string " items selected" and break.
         * </li>
         * </ul>
         *
         * @param mode The {@code ActionMode} providing the selection mode
         */
        private void setSubtitle(ActionMode mode) {
            final int checkedCount = getListView().getCheckedItemCount();
            switch (checkedCount) {
                case 0:
                    mode.setSubtitle(null);
                    break;
                case 1:
                    mode.setSubtitle("One item selected");
                    break;
                default:
                    mode.setSubtitle("" + checkedCount + " items selected");
                    break;
            }
        }
    }
}
