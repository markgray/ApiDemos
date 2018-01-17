/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.apis.R;

/**
 * This activity demonstrates some of the available ways to reduce the size or visual contrast of
 * the system decor, in order to better focus the user's attention or use available screen real
 * estate on the task at hand. Uses CheckBox'es to set or unset the various flags passed to
 * View.setSystemUiVisibility for the IV extends ImageView which serves as the background in the
 * FrameLayout holding it and the CheckBox'es which overlay it.
 */
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.KITKAT)
public class SystemUIModes extends Activity
        implements OnQueryTextListener, ActionBar.TabListener {

    /**
     * {@code ImageView} which is used as the background of our window.
     */
    public static class IV extends android.support.v7.widget.AppCompatImageView
            implements View.OnSystemUiVisibilityChangeListener {
        /**
         * {@code SystemUIModes} activity containing us. We use it to access its methods in
         * several places.
         */
        private SystemUIModes mActivity;
        /**
         * {@code ActionMode} which the user can select to be displayed using a checkbox.
         */
        private ActionMode mActionMode;

        /**
         * Our constructor. We just call our super's constructor. UNUSED
         *
         * @param context The Context the view is running in, through which it can access the current
         *                theme, resources, etc.
         */
        public IV(Context context) {
            super(context);
        }

        /**
         * Constructor which is called when our view is being inflated from an xml file. We just call
         * our super's constructor.
         *
         * @param context The Context the view is running in, through which it can access the current
         *                theme, resources, etc.
         * @param attrs The attributes of the XML tag that is inflating the view.
         */
        public IV(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        /**
         * Setter for our field {@code SystemUIModes mActivity}. We register "this" as a
         * {@code OnSystemUiVisibilityChangeListener}, then save our parameter {@code SystemUIModes act}
         * in our field {@code SystemUIModes mActivity}.
         *
         * @param act {@code SystemUIModes} instance which is containing us.
         */
        public void setActivity(SystemUIModes act) {
            setOnSystemUiVisibilityChangeListener(this);
            mActivity = act;
        }

        /**
         * This is called during layout when the size of this view has changed. We just call the
         * {@code refreshSizes} method of our containing activity {@code SystemUIModes mActivity}.
         *
         * @param w Current width of this view.
         * @param h Current height of this view.
         * @param oldw Old width of this view.
         * @param oldh Old height of this view.
         */
        @Override
        public void onSizeChanged(int w, int h, int oldw, int oldh) {
            mActivity.refreshSizes();
        }

        /**
         * Called when the status bar changes visibility. We call the {@code updateCheckControls} and
         * {@code refreshSizes} methods of our containing activity {@code SystemUIModes mActivity}.
         *
         * @param visibility  Bitwise-or of flags SYSTEM_UI_FLAG_LOW_PROFILE, SYSTEM_UI_FLAG_HIDE_NAVIGATION
         *                    SYSTEM_UI_FLAG_FULLSCREEN.
         */
        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            mActivity.updateCheckControls();
            mActivity.refreshSizes();
        }

        /**
         * {@code ActionMode.Callback} for the {@code ActionMode} which the user can choose to
         * display using a checkbox.
         */
        private class MyActionModeCallback implements ActionMode.Callback {
            /**
             * Called when action mode is first created. The menu supplied will be used to generate
             * action buttons for the action mode. First we set the title of our {@code ActionMode mode}
             * to the string "My Action Mode!", set its subtitle to null, and "hint" that the title
             * is not optional. Then we add to our parameter {@code Menu menu} a menu item with the
             * title "Sort By Size", whose icon we set to android.R.drawable.ic_menu_sort_by_size,
             * and a menu item with the title "Sort By Alpha", whose icon we set to
             * android.R.drawable.ic_menu_sort_alphabetically. Finally we return true to the caller
             * to indicate that the action mode should be created.
             *
             * @param mode ActionMode being created
             * @param menu Menu used to populate action buttons
             * @return true if the action mode should be created, false if entering this
             *              mode should be aborted.
             */
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.setTitle("My Action Mode!");
                mode.setSubtitle(null);
                mode.setTitleOptionalHint(false);
                menu.add("Sort By Size").setIcon(android.R.drawable.ic_menu_sort_by_size);
                menu.add("Sort By Alpha").setIcon(android.R.drawable.ic_menu_sort_alphabetically);
                return true;
            }

            /**
             * Called to refresh an action mode's action menu whenever it is invalidated. We ignore.
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
             * Called to report a user click on an action button. We return true to indicate that we
             * consumed the event.
             *
             * @param mode The current ActionMode
             * @param item The item that was clicked
             * @return true if this callback handled the event, false if the standard MenuItem
             *          invocation should continue.
             */
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return true;
            }

            /**
             * Called when an action mode is about to be exited and destroyed. We set our field
             * {@code ActionMode mActionMode} to null, and call the {@code clearActionMode} method
             * of our field {@code SystemUIModes mActivity} to un-check the checkbox with the id
             * R.id.windowActionMode.
             *
             * @param mode The current ActionMode being destroyed
             */
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mActionMode = null;
                mActivity.clearActionMode();
            }
        }

        /**
         * Called when the checkbox with the id R.id.windowActionMode is checked to create a new
         * {@code MyActionModeCallback} and use it to start an action mode using that
         * {@code ActionMode.Callback} to control the lifecycle of the action mode.
         */
        public void startActionMode() {
            if (mActionMode == null) {
                ActionMode.Callback cb = new MyActionModeCallback();
                mActionMode = startActionMode(cb);
            }
        }

        /**
         * Called when the checkbox with the id R.id.windowActionMode is un-checked. If our field
         * {@code ActionMode mActionMode} is not null, we call its {@code finish} method to finish
         * and close this action mode. The action mode's ActionMode.Callback will have its
         * onDestroyActionMode(ActionMode) method called.
         */
        public void stopActionMode() {
            if (mActionMode != null) {
                mActionMode.finish();
            }
        }
    }

    /**
     * Called when the {@code CheckBox} with the id R.id.windowFullscreen is checked or unchecked.
     * We initialize our variable {@code Window win} with the current Window for our activity,
     * initialize {@code WindowManager.LayoutParams winParams} with the current window attributes
     * associated with {@code win}, and initialize {@code int bits} with the Window flag FLAG_FULLSCREEN
     * (hide all screen decorations (such as the status bar) while this window is displayed). If our
     * parameter {@code boolean on} is true we set the FLAG_FULLSCREEN bit in the {@code flags} field
     * of {@code winParams}, if it is false we clear that bit. Finally we use {@code winParams} to
     * set the window attributes of {@code win}.
     *
     * @param on if true go to full screen mode, if false leave full screen mode.
     */
    private void setFullscreen(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        if (on) {
            winParams.flags |=  bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    /**
     * Called when the {@code CheckBox} with the id R.id.windowOverscan is checked or unchecked to
     * switch in or out of overscan mode. We initialize our variable {@code Window win} with the
     * current Window for our activity, initialize {@code WindowManager.LayoutParams winParams} with
     * the current window attributes associated with {@code win}, and initialize {@code int bits}
     * with the Window flag FLAG_LAYOUT_IN_OVERSCAN (allow window contents to extend into the screen's
     * overscan area, if there is one) If our parameter {@code boolean on} is true we set the
     * FLAG_LAYOUT_IN_OVERSCAN bit in the {@code flags} field of {@code winParams}, if it is false
     * we clear that bit. Finally we use {@code winParams} to set the window attributes of {@code win}.
     *
     * @param on true to allow window contents to extend in to the screen's overscan area, false to
     *           disable this.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void setOverscan(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN;
        if (on) {
            winParams.flags |=  bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    /**
     * Called when the {@code CheckBox} with the id R.id.windowTranslucentStatus is checked or
     * unchecked to enable or disable translucent status bar. We initialize our variable
     * {@code Window win} with the current Window for our activity, initialize
     * {@code WindowManager.LayoutParams winParams} with the current window attributes associated
     * with {@code win}, and initialize {@code int bits} with the Window flag FLAG_TRANSLUCENT_STATUS
     * (request a translucent status bar with minimal system-provided background protection) If our
     * parameter {@code boolean on} is true we set the FLAG_TRANSLUCENT_STATUS bit in the
     * {@code flags} field of {@code winParams}, if it is false we clear that bit. Finally we use
     * {@code winParams} to set the window attributes of {@code win}.
     *
     * @param on true to request a translucent status bar with minimal system-provided background
     *           protection, false to disable this.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |=  bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    /**
     * Called when the {@code CheckBox} with the id R.id.windowTranslucentNav is checked or unchecked
     * to enable or disable translucent navigation bar. We initialize our variable {@code Window win}
     * with the current Window for our activity, initialize {@code WindowManager.LayoutParams winParams}
     * with the current window attributes associated with {@code win}, and initialize {@code int bits}
     * with the Window flag FLAG_TRANSLUCENT_NAVIGATION (request a translucent navigation bar with
     * minimal system-provided background protection) If our parameter {@code boolean on} is true we
     * set the FLAG_TRANSLUCENT_NAVIGATION bit in the {@code flags} field of {@code winParams}, if it
     * is false we clear that bit. Finally we use {@code winParams} to set the window attributes of
     * {@code win}.
     *
     * @param on true to request a translucent navigation bar with minimal system-provided background
     *          protection, false to disable this.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setTranslucentNavigation(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        if (on) {
            winParams.flags |=  bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    /**
     * Fetches the width and height of the display in pixels and returns a string describing them.
     * We initialize our variable {@code DisplayMetrics dm} with the current display metrics that
     * are in effect. Then we return a formatted string of the {@code widthPixels} field of {@code dm}
     * (absolute width of the available display size in pixels) and the {@code heightPixels} field
     * of {@code dm} (absolute height of the available display size in pixels), formatted using the
     * format "DisplayMetrics = (%d x %d)".
     *
     * @return String describing the width and height of the display in pixels.
     */
    @SuppressLint("DefaultLocale")
    private String getDisplaySize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return String.format("DisplayMetrics = (%d x %d)", dm.widthPixels, dm.heightPixels);
    }

    /**
     * Retrieves the left, top, right, and bottom positions of {@code IV mImage} and returns a string
     * formatted using the format "View = (%d,%d - %d,%d)".
     *
     * @return string displaying the top left and bottom right coordinates of our {@code IV mImage}
     * view.
     */
    @SuppressLint("DefaultLocale")
    private String getViewSize() {
        return String.format("View = (%d,%d - %d,%d)",
                mImage.getLeft(), mImage.getTop(),
                mImage.getRight(), mImage.getBottom());
    }

    /**
     * Called from the {@code onSizeChanged} and {@code onSystemUiVisibilityChange} callbacks of our
     * embedded {@code IV mImage} in order to display the new display metrics values in our
     * {@code TextView mMetricsText}. We just set the text of {@code mMetricsText} to the string
     * formed by concatenating the value returned by our method {@code getDisplaySize} to the string
     * " " followed by the value returned by our method {@code getViewSize}.
     */
    @SuppressLint("SetTextI18n")
    void refreshSizes() {
        mMetricsText.setText(getDisplaySize() + " " + getViewSize());
    }

    /**
     * UNUSED
     */
    @SuppressWarnings("unused")
    static int TOAST_LENGTH = 500;
    /**
     * Our embedded {@code IV} instance.
     */
    IV mImage;
    /**
     * References to the 8 {@code CheckBox} at the top of the view, they are all managed by the
     * {@code OnCheckedChangeListener checkChangeListener} (the state of the checkboxes are read
     * by the method {@code updateSystemUi} and the related flag in {@code int[] mCheckFlags} is
     * set if the {@code CheckBox} is checked, and the resulting mask is used to update the system
     * UI using the method {@code setSystemUiVisibility}).
     */
    CheckBox[] mCheckControls = new CheckBox[8];
    /**
     * System UI Flags controlled by the 8 checkboxes in {@code CheckBox[] mCheckControls}
     */
    int[] mCheckFlags = new int[] { View.SYSTEM_UI_FLAG_LOW_PROFILE,
            View.SYSTEM_UI_FLAG_FULLSCREEN, View.SYSTEM_UI_FLAG_HIDE_NAVIGATION,
            View.SYSTEM_UI_FLAG_IMMERSIVE, View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY,
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE, View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN,
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    };
    /**
     * {@code TextView} with id R.id.metricsText which our method {@code refreshSizes} uses to display
     * the display and view metrics whenever they have changed.
     */
    TextView mMetricsText;

    /**
     * Our constructor.
     */
    public SystemUIModes() {
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.system_ui_modes.
     * We initialize our field {@code IV mImage} by finding the view with id R.id.image, and call its
     * {@code setActivity} to have it set its {@code SystemUIModes mActivity} field to "this". We
     * initialize {@code CompoundButton.OnCheckedChangeListener checkChangeListener} with an anonymous
     * class which calls our method {@code updateSystemUi} whenever one of the checkboxes it listens
     * to changes state. We initialize the 8 entries in {@code CheckBox[] mCheckControls} by finding
     * the views with the following ids:
     * <ul>
     *     <li>
     *         R.id.modeLowProfile - "LOW_PROFILE" controls flag SYSTEM_UI_FLAG_LOW_PROFILE
     *         View requests the system UI to enter an unobtrusive "low profile" mode.
     *     </li>
     *     <li>
     *         R.id.modeFullscreen - "FULLSCRN" controls flag SYSTEM_UI_FLAG_FULLSCREEN
     *         View requests to go into the normal fullscreen mode so that its content can take over
     *         the screen.
     *     </li>
     *     <li>
     *         R.id.modeHideNavigation - "HIDE_NAV" controls flag SYSTEM_UI_FLAG_HIDE_NAVIGATION
     *         View request that the system navigation be temporarily hidden.
     *     </li>
     *     <li>
     *         R.id.modeImmersive - "IMMERSIVE" controls flag SYSTEM_UI_FLAG_IMMERSIVE
     *         View would like to remain interactive when hiding the navigation bar with
     *         SYSTEM_UI_FLAG_HIDE_NAVIGATION. If this flag is not set, SYSTEM_UI_FLAG_HIDE_NAVIGATION
     *         will be force cleared by the system on any user interaction.
     *     </li>
     *     <li>
     *         R.id.modeImmersiveSticky - "IMM_STICKY" controls flag SYSTEM_UI_FLAG_IMMERSIVE_STICKY
     *         View would like to remain interactive when hiding the status bar with SYSTEM_UI_FLAG_FULLSCREEN
     *         and/or hiding the navigation bar with SYSTEM_UI_FLAG_HIDE_NAVIGATION. Use this flag to
     *         create an immersive experience while also hiding the system bars. If this flag is not set,
     *         SYSTEM_UI_FLAG_HIDE_NAVIGATION will be force cleared by the system on any user interaction,
     *         and SYSTEM_UI_FLAG_FULLSCREEN will be force-cleared by the system if the user swipes from
     *         the top of the screen.
     *     </li>
     *     <li>
     *         R.id.layoutStable - "STABLE" controls flag SYSTEM_UI_FLAG_LAYOUT_STABLE
     *         When using other layout flags, we would like a stable view of the content insets given
     *         to fitSystemWindows(Rect). This means that the insets seen there will always represent
     *         the worst case that the application can expect as a continuous state.
     *     </li>
     *     <li>
     *         R.id.layoutFullscreen - "FULLSCRN" controls flag SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
     *         View would like its window to be laid out as if it has requested SYSTEM_UI_FLAG_FULLSCREEN,
     *         even if it currently hasn't. This allows it to avoid artifacts when switching in and out
     *         of that mode, at the expense that some of its user interface may be covered by screen
     *         decorations when they are shown.
     *     </li>
     *     <li>
     *         R.id.layoutHideNavigation - "HIDE_NAV" controls flag SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
     *         View would like its window to be laid out as if it has requested SYSTEM_UI_FLAG_HIDE_NAVIGATION,
     *         even if it currently hasn't. This allows it to avoid artifacts when switching in and out
     *         of that mode, at the expense that some of its user interface may be covered by screen
     *         decorations when they are shown.
     *     </li>
     * </ul>
     * Next we loop through all 8 of the checkboxes in {@code mCheckControls} and set their
     * {@code OnCheckedChangeListener} to {@code checkChangeListener}.
     *
     * We now find the 6 remaining checkboxes in our layout under the "Window" label (grouped this
     * way because they each set or clear their flags using the layout parameters of the current
     * Window of the activity instead of using {@code setSystemUiVisibility}) and set their
     * {@code OnCheckedChangeListener} to anonymous classes as follows:
     * <ul>
     *     <li>
     *         R.id.windowFullscreen - "FULLSCRN" calls our method {@code setFullscreen} with the
     *         value of its parameter {@code isChecked} which sets or clears the
     *         WindowManager.LayoutParams.FLAG_FULLSCREEN bit of our activities window.
     *     </li>
     *     <li>
     *         R.id.windowOverscan - "OVERSCAN" calls our method {@code setOverscan} with the
     *         value of its parameter {@code isChecked} which sets or clears the
     *         WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN bit of our activities window.
     *     </li>
     *     <li>
     *         R.id.windowTranslucentStatus - "TRANS_STATUS" calls our method {@code setTranslucentStatus}
     *         with the value of its parameter {@code isChecked} which sets or clears the
     *         WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS bit of our activities window.
     *     </li>
     *     <li>
     *         R.id.windowTranslucentNav - "TRANS_NAV" calls our method {@code setTranslucentNavigation}
     *         with the value of its parameter {@code isChecked} which sets or clears the
     *         WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION bit of our activities window.
     *     </li>
     *     <li>
     *         R.id.windowHideActionBar - "No ActionBar" if its parameter {@code isChecked} is true
     *         it retrieves a reference to this activity's ActionBar and calls its {@code hide}
     *         method, if false it retrieves a reference to this activity's ActionBar and calls its
     *         {@code show} method.
     *     </li>
     *     <li>
     *         R.id.windowActionMode - "Action Mode" if its parameter {@code isChecked} is true
     *         if calls the {@code startActionMode} method of {@code IV mImage}, if it is false it
     *         calls the {@code stopActionMode} method.
     *     </li>
     * </ul>
     * Finally we initialize our field {@code TextView mMetricsText} by finding the view with id
     * R.id.metricsText.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.system_ui_modes);
        mImage = (IV) findViewById(R.id.image);
        mImage.setActivity(this);

        CompoundButton.OnCheckedChangeListener checkChangeListener
                = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSystemUi();
            }
        };
        mCheckControls[0] = (CheckBox) findViewById(R.id.modeLowProfile);
        mCheckControls[1] = (CheckBox) findViewById(R.id.modeFullscreen);
        mCheckControls[2] = (CheckBox) findViewById(R.id.modeHideNavigation);
        mCheckControls[3] = (CheckBox) findViewById(R.id.modeImmersive);
        mCheckControls[4] = (CheckBox) findViewById(R.id.modeImmersiveSticky);
        mCheckControls[5] = (CheckBox) findViewById(R.id.layoutStable);
        mCheckControls[6] = (CheckBox) findViewById(R.id.layoutFullscreen);
        mCheckControls[7] = (CheckBox) findViewById(R.id.layoutHideNavigation);
        //noinspection ForLoopReplaceableByForEach
        for (int i=0; i<mCheckControls.length; i++) {
            mCheckControls[i].setOnCheckedChangeListener(checkChangeListener);
        }

        ((CheckBox) findViewById(R.id.windowFullscreen)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setFullscreen(isChecked);
                    }
                }
        );
        ((CheckBox) findViewById(R.id.windowOverscan)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setOverscan(isChecked);
                    }
                }
        );
        ((CheckBox) findViewById(R.id.windowTranslucentStatus)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setTranslucentStatus(isChecked);
                    }
                }
        );
        ((CheckBox) findViewById(R.id.windowTranslucentNav)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setTranslucentNavigation(isChecked);
                    }
                }
        );
        ((CheckBox) findViewById(R.id.windowHideActionBar)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            //noinspection ConstantConditions
                            getActionBar().hide();
                        } else {
                            //noinspection ConstantConditions
                            getActionBar().show();
                        }
                    }
                }
        );
        ((CheckBox) findViewById(R.id.windowActionMode)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mImage.startActionMode();
                        } else {
                            mImage.stopActionMode();
                        }
                    }
                }
        );
        mMetricsText = (TextView) findViewById(R.id.metricsText);
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     *
     * @param menu The options menu in which to place our items.
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.content_actions, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);

        // Set file with share history to the provider and set the share intent.
        MenuItem actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
        ShareActionProvider actionProvider = (ShareActionProvider) actionItem.getActionProvider();
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        // Note that you can set/change the intent any time,
        // say when the user has selected an image.
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri uri = Uri.fromFile(getFileStreamPath("shared.png"));
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        actionProvider.setShareIntent(shareIntent);
        return true;
    }

    @Override
    public void onAttachedToWindow() {
        updateCheckControls();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onSort(MenuItem item) {
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(this, "Searching for: " + query + "...", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_tabs:
                //noinspection ConstantConditions,deprecation
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                item.setChecked(true);
                return true;
            case R.id.hide_tabs:
                //noinspection ConstantConditions,deprecation
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                item.setChecked(true);
                return true;
        }
        return false;
    }

    @Override
    public void onTabSelected(@SuppressWarnings("deprecation") Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabUnselected(@SuppressWarnings("deprecation") Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(@SuppressWarnings("deprecation") Tab tab, FragmentTransaction ft) {
    }

    public void updateCheckControls() {
        int visibility = mImage.getSystemUiVisibility();
        for (int i=0; i<mCheckControls.length; i++) {
            mCheckControls[i].setChecked((visibility&mCheckFlags[i]) != 0);
        }
    }

    public void updateSystemUi() {
        int visibility = 0;
        for (int i=0; i<mCheckControls.length; i++) {
            if (mCheckControls[i].isChecked()) {
                visibility |= mCheckFlags[i];
            }
        }
        mImage.setSystemUiVisibility(visibility);
    }

    public void clearActionMode() {
        ((CheckBox) findViewById(R.id.windowActionMode)).setChecked(false);
    }
}
