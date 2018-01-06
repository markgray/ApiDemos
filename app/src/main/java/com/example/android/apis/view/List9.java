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

import android.app.ListActivity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.apis.R;

/**
 * Another variation of the list of cheeses. In this case, we use
 * AbsListView.OnScrollListener AbsListView.OnItemScrollListener
 * to display the first letter of the visible range of cheeses. Uses an
 * Handler thread to remove the dialog displaying the letter after 3000ms
 * using postDelayed(mRemoveWindow, 3000); where mRemoveWindow is a pointer
 * to a class RemoveWindow which implements Runnable.
 */
public class List9 extends ListActivity implements ListView.OnScrollListener {
    /**
     * Class we use to call our method {@code removeWindow} to make our {@code TextView mDialogText}
     * invisible. It is scheduled to run 3000ms after a change to it makes it visible.
     */
    private final class RemoveWindow implements Runnable {
        @Override
        public void run() {
            removeWindow();
        }
    }

    /**
     * Instance of {@code RemoveWindow} we use to make our {@code TextView mDialogText} invisible.
     */
    private RemoveWindow mRemoveWindow = new RemoveWindow();
    /**
     * {@code Handler} we use to both create and add {@code TextView mDialogText} (see the code in
     * {@code onCreate} and to make it invisible again after a 3000ms delay (see the code in
     * {@code onScroll}, {@code RemoveWindow} and {@code removeWindow}).
     */
    Handler mHandler = new Handler();
    /**
     * Handle to the system level service WINDOW_SERVICE, we use it to call its {@code addView}
     * method to add {@code TextView mDialogText} and its {@code removeView} method to remove it
     * in our {@code onDestroy} callback.
     */
    private WindowManager mWindowManager;
    /**
     * Translucent overlay containing the first letter of the cheese at the top of our
     * {@code ListView}
     */
    private TextView mDialogText;
    /**
     * Flag indicating whether our {@code TextView mDialogText} is currently visible or not.
     */
    private boolean mShowing;
    /**
     * Flag indicating that our UI is ready to have {@code TextView mDialogText} displayed, it is
     * set to true by the {@code Runnable} scheduled to run in our {@code onCreate} callback, in our
     * {@code onResume} callback, and set to false in our {@code onPause} and {@code onDestroy}
     * callbacks. It is tested in our {@code onScroll} callback to skip doing anything if it is
     * false.
     */
    private boolean mReady;
    /**
     * The character currently being displayed in {@code TextView mDialogText}, it is set the the
     * first letter of the cheese at the top of our {@code ListView} in our {@code onScroll}
     * callback.
     */
    private char mPrevLetter = Character.MIN_VALUE;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Next we initialize {@code WindowManager mWindowManager} with a handle to
     * the system level service WINDOW_SERVICE. We set the adapter of our {@code ListView} to a new
     * instance of {@code ArrayAdapter} constructed to display our array {@code String[] mStrings}
     * using android.R.layout.simple_list_item_1 as the layout file. We fetch our {@code ListView}
     * and set its {@code OnScrollListener} to "this". We initialize {@code LayoutInflater inflate}
     * with a {@code LayoutInflater}, then use it to inflate the layout file with the resource ID
     * R.layout.list_position in order to initialize {@code TextView mDialogText}. We the set the
     * visibility of {@code mDialogText} to INVISIBLE. Finally we add an anonymous {@code Runnable}
     * class to the message queue of {@code Handler mHandler} which will use {@code mWindowManager}
     * to add the view {@code mDialogText} to our UI.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        // Use an existing ListAdapter that will map an array
        // of strings to TextViews
        setListAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, mStrings));

        getListView().setOnScrollListener(this);

        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //noinspection ConstantConditions
        mDialogText = (TextView) inflate.inflate(R.layout.list_position, getListView(), false);
        mDialogText.setVisibility(View.INVISIBLE);

        mHandler.post(new Runnable() {
            /**
             * When {@code mHandler} gets around to running us, we will set our flag {@code mReady}
             * to true, construct {@code LayoutParams lp} with a width and height of WRAP_CONTENT,
             * TYPE_APPLICATION (normal application window), with the flags FLAG_NOT_TOUCHABLE
             * (window can never receive touch events), or'ed with FLAG_NOT_FOCUSABLE (window won't
             * ever get key input focus), and using the format TRANSLUCENT (system chooses a format
             * that supports translucency (many alpha bits)). We then use {@code mWindowManager} to
             * add the view {@code mDialogText} with the layout parameters {@code lp}.
             */
            @Override
            public void run() {
                mReady = true;
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                mWindowManager.addView(mDialogText, lp);
            }
        });
    }

    /**
     * Called after {@code onRestoreInstanceState}, {@code onRestart}, or {@code onPause}, for our
     * activity to start interacting with the user. First we call our super's implementation of
     * {@code onResume}, then we set our flag {@code mReady} to true.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mReady = true;
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of
     * {@code onPause}, then we call our method {@code removeWindow} to make our {@code mDialogText}
     * INVISIBLE, and finally we set our flag {@code mReady} to false.
     */
    @Override
    protected void onPause() {
        super.onPause();
        removeWindow();
        mReady = false;
    }

    /**
     * Perform any final cleanup before our activity is destroyed. First we call through to our
     * super's implementation of {@code onDestroy}, then we use {@code mWindowManager} to remove
     * {@code mDialogText}, and finally we set our flag {@code mReady} to false.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(mDialogText);
        mReady = false;
    }

    /**
     * Callback method to be invoked when the list or grid has been scrolled. This will be called
     * after the scroll has completed. If our flag {@code mReady} is true we set our variable
     * {@code char firstLetter} to the first letter of {@code mStrings[firstVisibleItem]}. If our
     * flag {@code mShowing} is false AND {@code firstLetter} is not equal to {@code mPrevLetter},
     * we set our flag {@code mShowing} to true, and set the visibility of {@code mDialogText} to
     * VISIBLE. Then we set the text of {@code mDialogText} to the string value of {@code firstLetter},
     * remove all {@code mRemoveWindow} callbacks from the {@code mHandler} message queue, and
     * schedule {@code mRemoveWindow} to be run 3000ms from now. Finally we set {@code mPrevLetter}
     * to {@code firstLetter}. If {@code mReady} is false we return having done nothing.
     *
     * @param view             The view whose scroll state is being reported
     * @param firstVisibleItem the index of the first visible cell
     * @param visibleItemCount the number of visible cells
     * @param totalItemCount   the number of items in the list adaptor
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (mReady) {
            char firstLetter = mStrings[firstVisibleItem].charAt(0);

            if (!mShowing && firstLetter != mPrevLetter) {
                mShowing = true;
                mDialogText.setVisibility(View.VISIBLE);
            }

            mDialogText.setText(((Character) firstLetter).toString());
            mHandler.removeCallbacks(mRemoveWindow);
            mHandler.postDelayed(mRemoveWindow, 3000);
            mPrevLetter = firstLetter;
        }
    }

    /**
     * Callback method to be invoked while the list view or grid view is being scrolled. We ignore.
     *
     * @param view        The view whose scroll state is being reported
     * @param scrollState The current scroll state. Either SCROLL_STATE_TOUCH_SCROLL or SCROLL_STATE_IDLE.
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    /**
     * Convenience function to make {@code TextView mDialogText} INVISIBLE. If our flag {@code mShowing}
     * is true we set it to false and set the visibility of {@code TextView mDialogText} to INVISIBLE.
     * If {@code mShowing} is false we return having done nothing.
     */
    private void removeWindow() {
        if (mShowing) {
            mShowing = false;
            mDialogText.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Reference to {@code Cheeses.sCheeseStrings} that we use as our data array.
     */
    private String[] mStrings = Cheeses.sCheeseStrings;
}
