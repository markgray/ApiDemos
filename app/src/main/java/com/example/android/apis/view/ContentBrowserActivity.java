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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;

import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBar.Tab;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;

import com.example.android.apis.R;

/**
 * This activity demonstrates how to use system UI flags to implement
 * a content browser style of UI (such as a book reader). Includes "Content",
 * and implementation of a view for displaying immersive content, using system
 * UI flags to transition in and out of modes where the user is focused on
 * that content. When the user clicks, it toggles the visibility of navigation
 * elements.
 */
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ContentBrowserActivity extends AppCompatActivity
        implements OnQueryTextListener, ActionBar.TabListener {

    /**
     * Implementation of a view for displaying immersive content, using system UI
     * flags to transition in and out of modes where the user is focused on that
     * content.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static class Content extends ScrollView
            implements View.OnSystemUiVisibilityChangeListener, View.OnClickListener {
        /**
         * {@code TextView} we use to display our "content" in (the string with the resource id
         * R.string.alert_dialog_two_buttons2ultra_msg)
         */
        TextView mText;
        /**
         * {@code TextView} that our containing activity uses to display a title, it is toggled between
         * VISIBLE and INVISIBLE to increase usable window space.
         */
        TextView mTitleView;
        /**
         * {@code SeekBar} that our containing activity uses to display and control the position of
         * our {@code ScrollView}, it is toggled between VISIBLE and INVISIBLE to increase usable
         * window space.
         */
        SeekBar mSeekView;
        /**
         * UNUSED
         */
        @SuppressWarnings("unused")
        boolean mNavVisible;
        /**
         * These are the visibility flags to be given to {@code setSystemUiVisibility(int)}, they are
         * modified by user choice in the menu. It starts out:
         * <p>
         * SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN:
         * View would like its window to be laid out as if it has requested SYSTEM_UI_FLAG_FULLSCREEN,
         * even if it currently hasn't. This allows it to avoid artifacts when switching in and out of
         * that mode, at the expense that some of its user interface may be covered by screen decorations
         * when they are shown.
         * <p>
         * SYSTEM_UI_FLAG_LAYOUT_STABLE:
         * When using other layout flags, we would like a stable view of the content insets given to
         * fitSystemWindows(Rect). This means that the insets seen there will always represent the
         * worst case that the application can expect as a continuous state. In the stock Android UI
         * this is the space for the system bar, nav bar, and status bar, but not more transient
         * elements such as an input method. The stable layout your UI sees is based on the system
         * UI modes you can switch to. That is, if you specify SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN then
         * you will get a stable layout for changes of the SYSTEM_UI_FLAG_FULLSCREEN mode
         */
        int mBaseSystemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_LAYOUT_STABLE;
        /**
         * Current global UI visibility flags received by our {@code onSystemUiVisibilityChange}
         * callback.
         */
        int mLastSystemUiVis;

        /**
         * {@code Runnable} that makes the navigation invisible after a delay of 2000ms. Used by our
         * {@code onWindowVisibilityChanged} callback in order to show our navigation elements briefly
         * before hiding them.
         */
        Runnable mNavHider = new Runnable() {
            /**
             * Calls our method {@code setNavVisibility(false)} to make the navigation views
             * invisible, this includes {@code TextView mTitleView}, and {@code SeekBar mSeekView}
             * as well as calling {@code setSystemUiVisibility} to set the system UI visibility to
             * the appropriate state.
             */
            @Override
            public void run() {
                setNavVisibility(false);
            }
        };

        /**
         * Our constructor which is called when we are inflated from an xml layout file. First we
         * call our super's constructor. We initialize our field {@code TextView mText} with a new
         * instance, set its text size to 16dp, set its text to the string with resource id
         * R.string.alert_dialog_two_buttons2ultra_msg (a very long bit of nonsense text), disable
         * its clickable state, set its {@code OnClickListener} to "this", make its text selectable
         * by the user, then add it our view using {@code LayoutParams} which specify a width of
         * MATCH_PARENT and a height of WRAP_CONTENT. Finally we register "this" as an
         * {@code OnSystemUiVisibilityChangeListener}.
         *
         * @param context The Context the view is running in, through which it can
         *                access the current theme, resources, etc.
         * @param attrs   The attributes of the XML tag that is inflating the view.
         */
        public Content(Context context, AttributeSet attrs) {
            super(context, attrs);

            mText = new TextView(context);
            mText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            mText.setText(context.getString(R.string.alert_dialog_two_buttons2ultra_msg));
            mText.setClickable(false);
            mText.setOnClickListener(this);
            mText.setTextIsSelectable(true);
            addView(mText, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            setOnSystemUiVisibilityChangeListener(this);
        }

        /**
         * Called by the containing activity to supply the surrounding state of the content browser
         * that it will interact with. We save our parameter {@code TextView title} in our field
         * {@code TextView mTitleView}, and {@code SeekBar seek} in {@code SeekBar mSeekView} then
         * call our method {@code setNavVisibility(true)} to make our navigation UI appropriately
         * visible.
         *
         * @param title {@code TextView} to use for our title in {@code TextView mTitleView}.
         * @param seek  {@code SeekBar} to use for our seekbar in {@code SeekBar mSeekView}.
         */
        public void init(TextView title, SeekBar seek) {
            // This called by the containing activity to supply the surrounding
            // state of the content browser that it will interact with.
            mTitleView = title;
            mSeekView = seek;
            setNavVisibility(true);
        }

        /**
         * Called when the status bar changes visibility because of a call to {@code setSystemUiVisibility(int)}.
         * We initialize our variable {@code int diff} by xor'ing {@code mLastSystemUiVis} (the previous
         * visibility mask) with our parameter {@code int visibility} (the new visibility mask) isolating
         * the bits that have changed state. We then set {@code mLastSystemUiVis} to {@code visibility}.
         * If the bit that changed was SYSTEM_UI_FLAG_LOW_PROFILE, and the new value in {@code visibility}
         * is 0 (we have left low profile mode), we call our method {@code setNavVisibility(true)} to
         * make our navigation UI visible.
         *
         * @param visibility Bitwise-or of flags SYSTEM_UI_FLAG_LOW_PROFILE, SYSTEM_UI_FLAG_HIDE_NAVIGATION
         *                   SYSTEM_UI_FLAG_FULLSCREEN.
         */
        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            // Detect when we go out of low-profile mode, to also go out
            // of full screen.  We only do this when the low profile mode
            // is changing from its last state, and turning off.
            int diff = mLastSystemUiVis ^ visibility;
            mLastSystemUiVis = visibility;
            if ((diff & SYSTEM_UI_FLAG_LOW_PROFILE) != 0
                    && (visibility & SYSTEM_UI_FLAG_LOW_PROFILE) == 0) {
                setNavVisibility(true);
            }
        }

        /**
         * Called when the containing window has changed its visibility (between GONE, INVISIBLE,
         * and VISIBLE). First we call our super's implementation of {@code onWindowVisibilityChanged},
         * then we call our method {@code setNavVisibility(true)} to make our navigation UI visible.
         * Finally we get a handler associated with the thread running our view and schedule the
         * {@code Runnable mNavHider} to run in 2000ms to make the navigation UI invisible.
         *
         * @param visibility The new visibility of the window.
         */
        @Override
        protected void onWindowVisibilityChanged(int visibility) {
            super.onWindowVisibilityChanged(visibility);

            // When we become visible, we show our navigation elements briefly
            // before hiding them.
            setNavVisibility(true);
            getHandler().postDelayed(mNavHider, 2000);
        }

        /**
         * This is called in response to an internal scroll in this view. We first call through to
         * our super's implementation of {@code onScrollChanged}, then we call our method
         * {@code setNavVisibility(false)} to hide the navigation elements.
         *
         * @param l    Current horizontal scroll origin.
         * @param t    Current vertical scroll origin.
         * @param oldl Previous horizontal scroll origin.
         * @param oldt Previous vertical scroll origin.
         */
        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            super.onScrollChanged(l, t, oldl, oldt);

            // When the user scrolls, we hide navigation elements.
            setNavVisibility(false);
        }

        /**
         * Called when our view has been clicked. When the user clicks, we toggle the visibility of
         * the navigation elements. We fetch the current system visibility flags to initialize our
         * variable {@code int curVis}. Then we call our method {@code setNavVisibility} with false
         * if the SYSTEM_UI_FLAG_LOW_PROFILE bit in {@code curVis} is not set and true if it is
         * set (thereby toggling the visibility of the navigation elements).
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            // When the user clicks, we toggle the visibility of navigation elements.
            int curVis = getSystemUiVisibility();
            setNavVisibility((curVis & SYSTEM_UI_FLAG_LOW_PROFILE) != 0);
        }

        /**
         * Convenience setter method for our field {@code mBaseSystemUiVisibility}, we just set
         * {@code mBaseSystemUiVisibility} to our parameter {@code int visibility}.
         *
         * @param visibility new value for {@code mBaseSystemUiVisibility}
         */
        void setBaseSystemUiVisibility(int visibility) {
            mBaseSystemUiVisibility = visibility;
        }

        /**
         * Set our navigation elements visible if our parameter {@code visible} is true, or invisible
         * if it is false. First we initialize our variable {@code int newVis} to our field
         * {@code mBaseSystemUiVisibility}. If our parameter {@code visible} is false we set the flags
         * SYSTEM_UI_FLAG_LOW_PROFILE and SYSTEM_UI_FLAG_FULLSCREEN in {@code newVis}. We initialize
         * {@code boolean changed} to true if {@code newVis} is the same as the last system UI that
         * was requested using {@code setSystemUiVisibility(int)} (is this logic inverted?). If
         * {@code changed} or {@code visible} is true, we initialize {@code Handler h} with a handler
         * associated with the thread running our View, and if the result is not null we remove any
         * scheduled {@code Runnable mNavHider} from the queue. We then call {@code setSystemUiVisibility}
         * to set the system UI visibility to {@code newVis}, and if {@code visible} is true we set
         * the visibility of both {@code TextView mTitleView} and {@code SeekBar mSeekView} to VISIBLE,
         * or to INVISIBLE if {@code visible} is false.
         *
         * @param visible true makes our navigation elements visible, false makes them invisible.
         */
        void setNavVisibility(boolean visible) {
            int newVis = mBaseSystemUiVisibility;
            if (!visible) {
                newVis |= SYSTEM_UI_FLAG_LOW_PROFILE | SYSTEM_UI_FLAG_FULLSCREEN;
            }
            final boolean changed = newVis == getSystemUiVisibility();

            // Un-schedule any pending event to hide navigation if we are
            // changing the visibility, or making the UI visible.
            if (changed || visible) {
                Handler h = getHandler();
                if (h != null) {
                    h.removeCallbacks(mNavHider);
                }
            }

            // Set the new desired visibility.
            setSystemUiVisibility(newVis);
            mTitleView.setVisibility(visible ? VISIBLE : INVISIBLE);
            mSeekView.setVisibility(visible ? VISIBLE : INVISIBLE);
        }
    }

    /**
     * {@code Content} instance we use.
     */
    Content mContent;

    /**
     * Our constructor
     */
    public ContentBrowserActivity() {
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we request the window feature FEATURE_ACTION_BAR_OVERLAY (requests an
     * Action Bar that overlays window content), then we set our content view to our layout file
     * R.layout.content_browser. We initialize our field {@code Content mContent} by finding the
     * view with id R.id.content, and call its {@code init} method passing it the view with the id
     * R.id.title for its title {@code TextView} and the view with the id R.id.seekbar for its
     * {@code SeekBar}. We initialize our variable {@code ActionBar bar} by retrieving a reference
     * to our activity's ActionBar, then create and add three tabs to it whose text we set to "Tab 1",
     * "Tab 2", and "Tab 3" and whose {@code TabListener} we set to "this".
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setContentView(R.layout.content_browser);
        mContent = findViewById(R.id.content);
        mContent.init(findViewById(R.id.title),
                findViewById(R.id.seekbar));

        ActionBar bar = getSupportActionBar();
        //noinspection ConstantConditions
        bar.addTab(bar.newTab().setText("Tab 1").setTabListener(this));
        bar.addTab(bar.newTab().setText("Tab 2").setTabListener(this));
        bar.addTab(bar.newTab().setText("Tab 3").setTabListener(this));
    }

    /**
     * Initialize the contents of the Activity's standard options menu. We initialize our variable
     * {@code MenuInflater inflater} with a {@code MenuInflater} for this context, then use it to
     * inflate the menu layout file R.menu.content_actions into our parameter {@code Menu menu}. We
     * initialize our variable {@code SearchView searchView} by finding the menu item with the id
     * R.id.action_search in {@code menu} and fetching the currently set action view for this menu
     * item. We then set the {@code OnQueryTextListener} of {@code searchView} to "this". We initialize
     * our variable {@code MenuItem actionItem} by finding the menu item in {@code menu} with the id
     * R.id.menu_item_share_action_provider_action_bar, and use it to initialize our variable
     * {@code ShareActionProvider actionProvider} by fetching its action provider. We set the file
     * name of the file for persisting the share history of {@code actionProvider} to the string
     * DEFAULT_SHARE_HISTORY_FILE_NAME ("share_history.xml"). We create {@code Intent shareIntent}
     * with the action ACTION_SEND, and set its type to "image/&#42;". We create {@code Uri uri} to
     * reference the file "shared.png", and add it as an extra to {@code shareIntent} with the key
     * EXTRA_STREAM. We then set the share intent of {@code actionProvider} to {@code shareIntent}
     * and return true to the caller.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed.
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
        ShareActionProvider actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(actionItem);
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
     * This method is used for the android:onClick method by two of the menu items in the menu. We
     * do nothing.
     *
     * @param item {@code MenuItem} that has been selected.
     */
    public void onSort(MenuItem item) {
    }

    /**
     * This hook is called whenever an item in your options menu is selected. We switch on the item
     * id of our parameter {@code MenuItem item}:
     * <ul>
     * <li>
     * R.id.show_tabs - we fetch a reference to our action bar and call its {@code setNavigationMode}
     * method with the argument NAVIGATION_MODE_TABS to have it display tabs. We then call the
     * {@code setChecked} method of {@code item} to make sure the {@code CheckBox} is checked and
     * return true to the caller to signal that we have consumed the event.
     * </li>
     * <li>
     * R.id.hide_tabs - we fetch a reference to our action bar and call its {@code setNavigationMode}
     * method with the argument NAVIGATION_MODE_STANDARD to have it hide the tabs. We then call the
     * {@code setChecked} method of {@code item} to make sure the {@code CheckBox} is checked and
     * return true to the caller to signal that we have consumed the event.
     * </li>
     * <li>
     * R.id.stable_layout - we toggle the checked state of {@code item}, then we call the
     * {@code setBaseSystemUiVisibility} of our field {@code Content mContent} with the bitmask
     * formed by or'ing SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN with SYSTEM_UI_FLAG_LAYOUT_STABLE if
     * the {@code item} is now checked, or the bit flag SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN if it
     * is not. Then we return true to the caller to signal that we have consumed the event.
     * </li>
     * </ul>
     * If the item selected is not one of the three above, we return false to the caller to allow
     * normal menu processing to proceed.
     *
     * @param item The menu item that was selected.
     * @return Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_tabs:
                // noinspection ConstantConditions
                getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                item.setChecked(true);
                return true;
            case R.id.hide_tabs:
                // noinspection ConstantConditions
                getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                item.setChecked(true);
                return true;
            case R.id.stable_layout:
                item.setChecked(!item.isChecked());
                mContent.setBaseSystemUiVisibility(item.isChecked()
                        ? View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        : View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                return true;
        }
        return false;
    }

    /**
     * Called when the query text is changed by the user. We just return true signaling that we have
     * consumed the event.
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
     * Called when the user submits the query. We construct and show a toast displaying the string
     * formed by concatenating the string "Searching for: " with our parameter {@code String query}
     * followed by the string "...", then we return true to the caller to signal that we have handled
     * the query.
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
     *            during a tab switch. The previous tab's un-select and this tab's select will be
     *            executed in a single transaction. This FragmentTransaction does not support
     *            being added to the back stack.
     */
    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
    }

    /**
     * Called when a tab exits the selected state. We ignore.
     *
     * @param tab The tab that was unselected
     * @param ft  A {@code FragmentTransaction} for queuing fragment operations to execute
     *            during a tab switch. This tab's un-select and the newly selected tab's select
     *            will be executed in a single transaction. This FragmentTransaction does not
     *            support being added to the back stack.
     */
    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
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
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }
}
