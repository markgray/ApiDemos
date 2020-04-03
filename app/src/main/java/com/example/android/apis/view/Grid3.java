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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

import java.util.List;

/**
 * This demo illustrates the use of CHOICE_MODE_MULTIPLE_MODAL, a.k.a. selection mode on GridView.
 * Implements multi-selection mode on GridView - hard to select by touch though
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Grid3 extends AppCompatActivity {
    /**
     * Our layout's {@code GridView}, with ID R.id.myGrid.
     */
    GridView mGrid;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.grid_1. Next we
     * call our method {@code loadApps} to load our field {@code List<ResolveInfo> mApps} with
     * {@code ResolveInfo} objects for accessing information about all of the apps that the
     * {@code PackageManager} knows about. We initialize {@code GridView mGrid} by finding the view
     * with ID R.id.myGrid, set its adapter to a new instance of {@code AppsAdapter}, set its choice
     * mode to CHOICE_MODE_MULTIPLE_MODAL, and set its {@code MultiChoiceModeListener} to a new
     * instance of our class {@code MultiChoiceModeListener}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_1);

        loadApps();

        mGrid = findViewById(R.id.myGrid);
        mGrid.setAdapter(new AppsAdapter());
        mGrid.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        mGrid.setMultiChoiceModeListener(new MultiChoiceModeListener());
    }

    /**
     * List of {@code ResolveInfo} objects for all activities that can be executed for an intent
     * with the action MAIN, and category LAUNCHER loaded from the {@code PackageManager} by our
     * method {@code loadApps}.
     */
    private List<ResolveInfo> mApps;

    /**
     * Loads {@code List<ResolveInfo> mApps} with a list of all activities that can be performed for
     * an intent with the action MAIN, and category LAUNCHER loaded using the {@code PackageManager}.
     * First we create {@code Intent mainIntent} with the action ACTION_MAIN, and add the category
     * CATEGORY_LAUNCHER. Then we retrieve a {@code PackageManager} instance and use it to retrieve
     * all activities that can be performed for intent {@code mainIntent} to initialize {@code mApps}.
     */
    private void loadApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        mApps = getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    /**
     * Our adapter which fetches app icons from the {@code ResolveInfo} for each of the applications
     * in our {@code List<ResolveInfo> mApps}.
     */
    @SuppressWarnings({"WeakerAccess", "FieldCanBeLocal"})
    public class AppsAdapter extends BaseAdapter {
        /**
         * Logical density of the display
         */
        private float dp2px;
        /**
         * Width of an icon in pixels (45*dp2px)
         */
        private int w;
        /**
         * Height of an icon in pixels (45*dp2px)
         */
        private int h;

        /**
         * Our constructor. First we initialize our field {@code dp2px} with the logical density of
         * our display, then we use it to scale 50dp to pixels to initialize both {@code w} and
         * {@code h}.
         */
        public AppsAdapter() {
            dp2px = getResources().getDisplayMetrics().density;
            w = (int) (50 * dp2px);
            h = (int) (50 * dp2px);
        }

        /**
         * Get a View that displays the data at the specified position in the data set. First we
         * declare {@code CheckableLayout l} and {@code ImageView i}. Then if our parameter
         * {@code convertView} is null we create a new instance of {@code ImageView} for {@code i},
         * set its scale type to FIT_CENTER and set its layout parameters to {@code w} pixels wide
         * by {@code h} pixels high (these are both 50dp scaled by the logical density of our
         * display). We create a new instance for {@code CheckableLayout l}, and set its layout
         * parameters to WRAP_CONTENT for both width and height. We then add the view {@code i} to
         * {@code l}. If {@code convertView} is not null we set {@code l} to it after casting it to
         * an {@code CheckableLayout}, and set {@code i} to the child of {@code l} at position 0.
         * We initialize {@code ResolveInfo info} with the data in {@code mApps} at position
         * {@code position} and set {@code i} to a drawable of the icon associated with {@code info}
         * that we retrieve by using that {@code ResolveInfo} to call back a {@code PackageManager}
         * instance to load the icon from the application. Finally we return {@code l} to the caller.
         *
         * @param position    The position of the item within the adapter's data set whose view we want.
         * @param convertView The old view to reuse, if possible.
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CheckableLayout l;
            ImageView i;

            if (convertView == null) {
                i = new ImageView(Grid3.this);
                i.setScaleType(ImageView.ScaleType.FIT_CENTER);
                i.setLayoutParams(new ViewGroup.LayoutParams(w, h));
                l = new CheckableLayout(Grid3.this);
                l.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.WRAP_CONTENT,
                        GridView.LayoutParams.WRAP_CONTENT));
                l.addView(i);
            } else {
                l = (CheckableLayout) convertView;
                i = (ImageView) l.getChildAt(0);
            }

            ResolveInfo info = mApps.get(position);
            i.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));

            return l;
        }

        /**
         * How many items are in the data set represented by this Adapter. We return the size of
         * {@code List<ResolveInfo> mApps},
         *
         * @return Count of items.
         */
        @Override
        public final int getCount() {
            return mApps.size();
        }

        /**
         * Get the data item associated with the specified position in the data set. We return the
         * data at position {@code position} in {@code List<ResolveInfo> mApps}.
         *
         * @param position Position of the item whose data we want within the adapter's data set.
         * @return The data at the specified position.
         */
        @Override
        public final Object getItem(int position) {
            return mApps.get(position);
        }

        /**
         * Get the row id associated with the specified position in the list. Our row id is the same
         * as our parameter {@code position}, so we just return that.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public final long getItemId(int position) {
            return position;
        }
    }

    /**
     * View group which holds our {@code ImageView}, and allows it to be checkable.
     */
    @SuppressWarnings("InnerClassMayBeStatic")
    public class CheckableLayout extends FrameLayout implements Checkable {
        /**
         * Flag to indicate whether we are checked or not.
         */
        private boolean mChecked;

        /**
         * Our constructor, we just call our super's constructor.
         *
         * @param context {@code Context} to use to access resources
         */
        public CheckableLayout(Context context) {
            super(context);
        }

        /**
         * Change the checked state of the view. We save our parameter {@code checked} in our field
         * {@code mChecked}, and set our background to a slightly translucent blue (0x770000ff) if
         * we are checked, or to null if we are not checked.
         *
         * @param checked The new checked state
         */
        @Override
        public void setChecked(boolean checked) {
            mChecked = checked;
            //noinspection deprecation
            setBackgroundDrawable(checked ?
                    getResources().getDrawable(R.drawable.blue)
                    : null);
        }

        /**
         * Returns the current checked state of the view, which is the value of our field
         * {@code mChecked}.
         *
         * @return The current checked state of the view
         */
        @Override
        public boolean isChecked() {
            return mChecked;
        }

        /**
         * Change the checked state of the view to the inverse of its current state. We just call
         * our method {@code setChecked} with the negated value of our field {@code mChecked}.
         */
        @Override
        public void toggle() {
            setChecked(!mChecked);
        }

    }

    /**
     * Our custom {@code GridView.MultiChoiceModeListener}, customized to just display the number
     * of items selected in the action mode.
     */
    public class MultiChoiceModeListener implements GridView.MultiChoiceModeListener {

        /**
         * Called when action mode is first created. We set the title of our parameter
         * {@code ActionMode mode} to the string "Select Items", and the subtitle to the string
         * "One item selected", then return true to the caller.
         *
         * @param mode ActionMode being created
         * @param menu Menu used to populate action buttons
         * @return true if the action mode should be created, false if entering this mode should
         * be aborted.
         */
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("Select Items");
            mode.setSubtitle("One item selected");
            return true;
        }

        /**
         * Called to refresh an action mode's action menu whenever it is invalidated. We just return
         * true to the caller.
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
         * Called to report a user click on an action button. We just return true to the caller.
         *
         * @param mode The current ActionMode
         * @param item The item that was clicked
         * @return true if this callback handled the event, false if the standard MenuItem
         * invocation should continue.
         */
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
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
         * Called when an item is checked or unchecked during selection mode. We initialize our
         * variable {@code selectCount} with the number of items currently selected in our field
         * {@code GridView mGrid}, then switch on it:
         * <ul>
         * <li>
         * 1: we set the subtitle of {@code ActionMode mode} to the string "One item selected"
         * then break
         * </li>
         * <li>
         * default: we set the subtitle of {@code ActionMode mode} to the string formed by
         * prepending the string " items selected" with the string value of {@code selectCount},
         * then break.
         * </li>
         * </ul>
         *
         * @param mode     The {@code ActionMode} providing the selection mode
         * @param position Adapter position of the item that was checked or unchecked
         * @param id       Adapter ID of the item that was checked or unchecked
         * @param checked  true if the item is now checked, false if the item is now unchecked.
         */
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            int selectCount = mGrid.getCheckedItemCount();
            //noinspection SwitchStatementWithTooFewBranches
            switch (selectCount) {
                case 1:
                    mode.setSubtitle("One item selected");
                    break;
                default:
                    mode.setSubtitle("" + selectCount + " items selected");
                    break;
            }
        }

    }
}
