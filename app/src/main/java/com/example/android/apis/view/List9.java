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

import com.example.android.apis.R;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

        mDialogText = (TextView) inflate.inflate(R.layout.list_position, null);
        mDialogText.setVisibility(View.INVISIBLE);

        mHandler.post(new Runnable() {
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

    @Override
    protected void onResume() {
        super.onResume();
        mReady = true;
    }


    @Override
    protected void onPause() {
        super.onPause();
        removeWindow();
        mReady = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(mDialogText);
        mReady = false;
    }

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

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }


    private void removeWindow() {
        if (mShowing) {
            mShowing = false;
            mDialogText.setVisibility(View.INVISIBLE);
        }
    }

    private String[] mStrings = Cheeses.sCheeseStrings;
}
