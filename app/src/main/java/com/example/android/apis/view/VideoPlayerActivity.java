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
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SeekBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.apis.R;

/**
 * This activity demonstrates how to use system UI flags to implement
 * a video player style of UI (where the navigation bar should be hidden
 * when the user isn't interacting with the screen to achieve full screen
 * video playback). Uses system UI flags to transition in and out of modes
 * where the entire screen can be filled with content (at the expense of
 * no user interaction).
 */
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class VideoPlayerActivity extends Activity
        implements OnQueryTextListener, ActionBar.TabListener {

    /**
     * Implementation of a view for displaying full-screen video playback,
     * using system UI flags to transition in and out of modes where the entire
     * screen can be filled with content (at the expense of no user interaction).
     */
    public static class Content extends android.support.v7.widget.AppCompatImageView implements
            View.OnSystemUiVisibilityChangeListener, View.OnClickListener,
            ActionBar.OnMenuVisibilityListener {
        /**
         * {@code Activity} of the containing activity that is passed to our {@code init} method.
         * ("this" when called from the {@code onCreate} method of {@code VideoPlayerActivity}.
         */
        Activity mActivity;
        /**
         * {@code TextView} in the {@code VideoPlayerActivity} layout file R.layout.video_player
         * with the id R.layout.video_player (the text reads "A title goes here"), we use it just
         * to switch its visibility to VISIBLE or INVISIBLE in our {@code setNavVisibility} method.
         */
        TextView mTitleView;
        /**
         * {@code Button} in the {@code VideoPlayerActivity} layout file R.layout.video_player
         * with the id R.id.play we use it to switch its text to "Play" of "Pause" in our
         * {@code setPlayPaused} method, and to make it VISIBLE or INVISIBLE in our
         * {@code setNavVisibility} method.
         */
        Button mPlayButton;
        /**
         * {@code SeekBar} in the {@code VideoPlayerActivity} layout file R.layout.video_player
         * with the id R.id.seekbar, we use it just to switch its visibility to VISIBLE or INVISIBLE
         * in our {@code setNavVisibility} method.
         */
        SeekBar mSeekView;
        /**
         * Flag to indicate whether we have added "this" as an {@code OnMenuVisibilityListener} to
         * the action bar (which we do in our {@code onAttachedToWindow} override). If it is true
         * we call {@code removeOnMenuVisibilityListener} in our {@code onDetachedFromWindow}
         * override to remove us.
         */
        boolean mAddedMenuListener;
        /**
         * Flag to indicate that the menus are currently open, it is set in {@code onMenuVisibilityChanged}
         * to its parameter, and if it is true our {@code setNavVisibility} will not schedule
         * {@code Runnable mNavHider} to auto hide our navigation UI.
         */
        boolean mMenusOpen;
        /**
         * Paused flag, when true navigation UI is displayed.
         */
        boolean mPaused;
        /**
         * Unused
         */
        @SuppressWarnings("unused")
        boolean mNavVisible;
        /**
         * Last system UI visibility mask, received by {@code onSystemUiVisibilityChange} override.
         */
        int mLastSystemUiVis;

        /**
         * {@code Runnable} which makes system UI visibility go away after 3000ms when play is
         * resumed, its running is scheduled in {@code setNavVisibility} method
         */
        Runnable mNavHider = new Runnable() {
            @Override
            public void run() {
                setNavVisibility(false);
            }
        };

        /**
         * Perform inflation from XML. First we call our super's constructor, then we register this
         * as a {@code OnSystemUiVisibilityChangeListener}, and a {@code OnClickListener}.
         *
         * @param context The Context the view is running in, through which it can
         *                access the current theme, resources, etc.
         * @param attrs   The attributes of the XML tag that is inflating the view.
         */
        public Content(Context context, AttributeSet attrs) {
            super(context, attrs);
            setOnSystemUiVisibilityChangeListener(this);
            setOnClickListener(this);
        }

        /**
         * Called by our containing {@code Activity} to initialize our fields with information about
         * the state of the video player that we will interact with. We save our parameter
         * {@code Activity activity} in our field {@code Activity mActivity}, our parameter
         * {@code TextView title} in our field {@code TextView mTitleView}, our parameter
         * {@code Button playButton} in our field {@code Button mPlayButton}, and our parameter
         * {@code SeekBar seek} in our field {@code SeekBar mSeekView}. We set the {@code OnClickListener}
         * of {@code mPlayButton} to "this", and call our {@code setPlayPaused(true)} to initialize
         * our UI to the paused state.
         *
         * @param activity   {@code Activity} we use to fetch the action bar
         * @param title      {@code TextView} containing the title we are playing
         * @param playButton {@code Button} that toggles between play and paused states
         * @param seek       {@code SeekBar} in the layout file we are contained in
         */
        public void init(Activity activity, TextView title, Button playButton, SeekBar seek) {
            // This called by the containing activity to supply the surrounding
            // state of the video player that it will interact with.
            mActivity = activity;
            mTitleView = title;
            mPlayButton = playButton;
            mSeekView = seek;
            mPlayButton.setOnClickListener(this);
            setPlayPaused(true);
        }

        /**
         * This is called when the view is attached to a window. First we call our super's implementation
         * of {@code onAttachedToWindow}. Then if our field {@code Activity mActivity} is not null we
         * set our flag {@code mAddedMenuListener} to true, use {@code mActivity} to get a reference
         * to the action bar in order to register "this" as an {@code OnMenuVisibilityListener}.
         */
        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (mActivity != null) {
                mAddedMenuListener = true;
                //noinspection ConstantConditions
                mActivity.getActionBar().addOnMenuVisibilityListener(this);
            }
        }

        /**
         * This is called when the view is detached from a window. First we call our super's implementation
         * of {@code onDetachedFromWindow}. Then if our flag {@code mAddedMenuListener} is true, we
         * use {@code mActivity} to get a reference to the action bar in order to remove "this" as an
         * {@code OnMenuVisibilityListener}.
         */
        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (mAddedMenuListener) {
                //noinspection ConstantConditions
                mActivity.getActionBar().removeOnMenuVisibilityListener(this);
            }
        }

        /**
         * Called when the status bar changes visibility. We initialize {@code int diff} to the bits
         * that have changed by bitwise exclusive or'ing our parameter {@code visibility} with our
         * field {@code mLastSystemUiVis}, then set {@code mLastSystemUiVis} to {@code visibility}.
         * If the bit that changed is SYSTEM_UI_FLAG_HIDE_NAVIGATION, and the new value of the bit
         * in {@code visibility} is equal to 0, we call our method {@code setNavVisibility(true)}
         * in order to make our navigation UI visible, and to schedule {@code Runnable mNavHider} to
         * run in 3000ms to make it invisible.
         *
         * @param visibility current system UI visibility mask, Bitwise-or of the bit flags
         *                   SYSTEM_UI_FLAG_LOW_PROFILE, SYSTEM_UI_FLAG_HIDE_NAVIGATION, and
         *                   SYSTEM_UI_FLAG_FULLSCREEN.
         */
        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            // Detect when we go out of nav-hidden mode, to clear our state
            // back to having the full UI chrome up.  Only do this when
            // the state is changing and nav is no longer hidden.
            int diff = mLastSystemUiVis ^ visibility;
            mLastSystemUiVis = visibility;
            if ((diff & SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0
                    && (visibility & SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                setNavVisibility(true);
            }
        }

        /**
         * Called when the window containing has changed its visibility (between GONE, INVISIBLE, and
         * VISIBLE). First we call our super's implementation of {@code onWindowVisibilityChanged} then
         * we call our method {@code setPlayPaused(true)} in order to pause play (when we become visible
         * or invisible, play is paused).
         *
         * @param visibility The new visibility of the window.
         */
        @Override
        protected void onWindowVisibilityChanged(int visibility) {
            super.onWindowVisibilityChanged(visibility);

            // When we become visible or invisible, play is paused.
            setPlayPaused(true);
        }

        /**
         * Called when a {@code View} we are registered as an {@code OnClickListener} for it clicked.
         * If the {@code View v} that was clicked is {@code Button mPlayButton} we call our method
         * {@code setPlayPaused(!mPaused)} to toggle our play/pause state, otherwise we call our
         * method {@code setNavVisibility(true)} to make the navigation visible.
         *
         * @param v View that was clicked
         */
        @Override
        public void onClick(View v) {
            if (v == mPlayButton) {
                // Clicking on the play/pause button toggles its state.
                setPlayPaused(!mPaused);
            } else {
                // Clicking elsewhere makes the navigation visible.
                setNavVisibility(true);
            }
        }

        /**
         * Called when an action bar menu is shown or hidden. We save our parameter {@code isVisible}
         * in our field {@code mMenusOpen}, then call our method {@code setNavVisibility(true)} to
         * make the navigation visible.
         *
         * @param isVisible True if an action bar menu is now visible, false if no action bar
         *                  menus are visible.
         */
        @Override
        public void onMenuVisibilityChanged(boolean isVisible) {
            mMenusOpen = isVisible;
            setNavVisibility(true);
        }

        /**
         * Called to change state to paused if its parameter {@code paused} is true, or to play if
         * it is false. First we save {@code paused} in our field {@code mPaused}. If {@code mPaused}
         * is true we set the text of our field {@code Button mPlayButton} to the string with the
         * resource id R.string.play ("Play"), if false we set it to the string with the resource id
         * R.string.pause ("Pause"). We call our method {@code setKeepScreenOn(!paused)} to keep our
         * screen on if we are now in play state, or to allow it to go off if we are now in paused
         * state. Finally we call our method {@code setNavVisibility(true)} to make the navigation
         * visible.
         *
         * @param paused if true move to the paused state, if false move to the play state
         */
        void setPlayPaused(boolean paused) {
            mPaused = paused;
            mPlayButton.setText(paused ? R.string.play : R.string.pause);
            setKeepScreenOn(!paused);
            setNavVisibility(true);
        }

        /**
         * Called to make our navigation visible if its parameter {@code visible} is true, or to hide
         * it if it is false. We initialize our variable {@code int newVis} by or'ing together the
         * following bit flags:
         * <ul>
         * <li>
         * SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN - View would like its window to be laid out as if it
         * has requested SYSTEM_UI_FLAG_FULLSCREEN, even if it currently hasn't. This allows it
         * to avoid artifacts when switching in and out of that mode, at the expense that some
         * of its user interface may be covered by screen decorations when they are shown.
         * </li>
         * <li>
         * SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION - View would like its window to be laid out
         * as if it has requested SYSTEM_UI_FLAG_HIDE_NAVIGATION, even if it currently hasn't.
         * This allows it to avoid artifacts when switching in and out of that mode, at the
         * expense that some of its user interface may be covered by screen decorations when
         * they are shown.
         * </li>
         * <li>
         * SYSTEM_UI_FLAG_LAYOUT_STABLE - When using other layout flags, we would like a
         * stable view of the content insets given to fitSystemWindows(Rect). This means that
         * the insets seen there will always represent the worst case that the application
         * can expect as a continuous state.
         * </li>
         * </ul>
         * Then is {@code visible} is false (we want to hide the navigation) we or {@code newVis}
         * with the following bit flags:
         * <ul>
         * <li>
         * SYSTEM_UI_FLAG_LOW_PROFILE - View requests the system UI to enter an unobtrusive
         * "low profile" mode. In low profile mode, the status bar and/or navigation icons
         * may dim.
         * </li>
         * <li>
         * SYSTEM_UI_FLAG_FULLSCREEN - View requests to go into the normal fullscreen mode
         * so that its content can take over the screen while still allowing the user to
         * interact with the application.
         * </li>
         * <li>
         * SYSTEM_UI_FLAG_HIDE_NAVIGATION - View requests that the system navigation be
         * temporarily hidden.
         * </li>
         * </ul>
         * Then if {@code visible} is true, we initialize {@code Handler h} with a handler associated
         * with the thread running the View. If {@code h} is not null, we remove all scheduled runs of
         * {@code Runnable mNavHider} from its queue. If {@code mMenusOpen} is false (no menus are
         * open), and {@code mPaused} is false (we are in play state) we use {@code h} to schedule
         * {@code Runnable mNavHider} to run in 3000ms to hide the navigation again.
         * <p>
         * We now set the system UI visibility to {@code newVis}, and set the visibility of the views
         * {@code mTitleView}, {@code mPlayButton}, and {@code mSeekView} to VISIBLE if our parameter
         * {@code visible} is true, or the INVISIBLE if it is false.
         *
         * @param visible if true we make our navigation visible, if false we hide the navigation.
         */
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        void setNavVisibility(boolean visible) {
            int newVis = SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | SYSTEM_UI_FLAG_LAYOUT_STABLE;
            if (!visible) {
                newVis |= SYSTEM_UI_FLAG_LOW_PROFILE
                        | SYSTEM_UI_FLAG_FULLSCREEN
                        | SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }

            // If we are now visible, schedule a timer for us to go invisible.
            if (visible) {
                Handler h = getHandler();
                if (h != null) {
                    h.removeCallbacks(mNavHider);
                    if (!mMenusOpen && !mPaused) {
                        // If the menus are open or play is paused, we will not auto-hide.
                        h.postDelayed(mNavHider, 3000);
                    }
                }
            }

            // Set the new desired visibility.
            setSystemUiVisibility(newVis);
            mTitleView.setVisibility(visible ? VISIBLE : INVISIBLE);
            mPlayButton.setVisibility(visible ? VISIBLE : INVISIBLE);
            mSeekView.setVisibility(visible ? VISIBLE : INVISIBLE);
        }
    }

    /**
     * Our {@code Content} instance.
     */
    Content mContent;

    /**
     * Our constructor.
     */
    public VideoPlayerActivity() {
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we request the window feature FEATURE_ACTION_BAR_OVERLAY (requests an
     * Action Bar that overlays window content), and then we set our content view to our layout file
     * R.layout.video_player. We initialize our field {@code Content mContent} by finding the view
     * with the id R.id.content, then call its {@code init} method with "this" as the activity, the
     * {@code TextView} with id R.id.title, the {@code Button} with id R.id.play, and the {@code SeekBar}
     * with id R.id.seekbar. We initialize {@code ActionBar bar} by retrieving a reference to this
     * activity's ActionBar, then add 3 tabs to it with the text "Tab 1", "Tab 2", and "Tab 3" and
     * setting their {@code TabListener} each to this.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setContentView(R.layout.video_player);

        mContent = (Content) findViewById(R.id.content);
        mContent.init(this, (TextView) findViewById(R.id.title),
                (Button) findViewById(R.id.play),
                (SeekBar) findViewById(R.id.seekbar));

        ActionBar bar = getActionBar();
        //noinspection ConstantConditions
        bar.addTab(bar.newTab().setText("Tab 1").setTabListener(this));
        bar.addTab(bar.newTab().setText("Tab 2").setTabListener(this));
        bar.addTab(bar.newTab().setText("Tab 3").setTabListener(this));
    }

    /**
     * Initialize the contents of the Activity's standard options menu. We initialize our variable
     * {@code MenuInflater inflater} with a menu inflater in this context and use it to inflate our
     * menu file R.menu.content_actions into our parameter {@code Menu menu}. We initialize our
     * variable {@code SearchView searchView} by finding the menu item with id R.id.action_search
     * in {@code menu} and retrieving its action view. We then set the {@code OnQueryTextListener}
     * of {@code searchView} to "this". We initialize our variable {@code MenuItem actionItem} by
     * finding the menu item in {@code menu} with id R.id.menu_item_share_action_provider_action_bar,
     * then initialize {@code ShareActionProvider actionProvider} by retrieving its action provider.
     * We then set the share history file name of {@code actionProvider} to the file DEFAULT_SHARE_HISTORY_FILE_NAME
     * ("share_history.xml"). We create {@code Intent shareIntent} with the action ACTION_SEND, set its
     * type to "image/&#42;". We create {@code Uri uri} to reference the file "shared.png", and put
     * it as an extra to {@code shareIntent} under the key EXTRA_STREAM. We then set {@code shareIntent}
     * as the share intent of {@code actionProvider} and return true to the caller so that the menu
     * will be displayed.
     *
     * @param menu The options menu in which you place your items.     *
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
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

    /**
     * Called when the main window associated with the activity has been attached to the window
     * manager. We just call our super's implementation of {@code onAttachedToWindow}.
     */
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    /**
     * Called after {@code onRestoreInstanceState}, {@code onRestart}, or {@code onPause}, for our
     * activity to start interacting with the user. We just call our super's implementation of
     * {@code onResume}.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * This method is referenced in the menu in the attribute android:onClick="onSort".
     */
    public void onSort(MenuItem item) {
    }

    /**
     * This hook is called whenever an item in your options menu is selected. We switch on the item
     * id of our parameter {@code MenuItem item}:
     * <ul>
     * <li>
     * R.id.show_tabs "Show Tabs" - We retrieve the action bar for our activity and set its
     * navigation mode to NAVIGATION_MODE_TABS (Tab navigation mode. Instead of static title
     * text this mode presents a series of tabs for navigation within the activity). We then
     * set the item to checked, and return true to the caller to indicate we have consumed
     * the event.
     * </li>
     * <li>
     * R.id.hide_tabs "Hide Tabs" - We retrieve the action bar for our activity and set its
     * navigation mode to NAVIGATION_MODE_STANDARD (Standard navigation mode. Consists of
     * either a logo or icon and title text with an optional subtitle. Clicking any of these
     * elements will dispatch onOptionsItemSelected to the host Activity with a MenuItem
     * with item ID android.R.id.home). We then set the item to checked, and return true to
     * the caller to indicate we have consumed the event.
     * </li>
     * </ul>
     * If the item id is not one of two above, we return false to the caller to allow normal menu
     * processing to proceed.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     */
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

    /**
     * Called when the query text is changed by the user. We just return true to the caller to indicate
     * that we have handled the event.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }

    /**
     * Called when the user submits the query. We toast a string formed by concatenating the string
     * "Searching for: " with our parameter {@code String query} followed by the string "...", then
     * return true to the caller to indicate that we have handled the event.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(this, "Searching for: " + query + "...", Toast.LENGTH_SHORT).show();
        return true;
    }

    /**
     * Called when a tab enters the selected state. We ignore.
     *
     * @param tab The tab that was selected
     * @param ft  A {@code FragmentTransaction} for queuing fragment operations to execute
     *            during a tab switch. The previous tab's unselect and this tab's select will be
     *            executed in a single transaction. This FragmentTransaction does not support
     *            being added to the back stack.
     */
    @Override
    public void onTabSelected(@SuppressWarnings("deprecation") Tab tab, FragmentTransaction ft) {
    }

    /**
     * Called when a tab exits the selected state. We ignore.
     *
     * @param tab The tab that was unselected
     * @param ft  A {@code FragmentTransaction} for queuing fragment operations to execute
     *            during a tab switch. This tab's unselect and the newly selected tab's select
     *            will be executed in a single transaction. This FragmentTransaction does not
     *            support being added to the back stack.
     */
    @Override
    public void onTabUnselected(@SuppressWarnings("deprecation") Tab tab, FragmentTransaction ft) {
    }

    /**
     * Called when a tab that is already selected is chosen again by the user. We ignore.
     *
     * @param tab The tab that was reselected.
     * @param ft  A {@link FragmentTransaction} for queuing fragment operations to execute
     *            once this method returns. This FragmentTransaction does not support
     *            being added to the back stack.
     */
    @Override
    public void onTabReselected(@SuppressWarnings("deprecation") Tab tab, FragmentTransaction ft) {
    }
}
