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

import android.app.ListActivity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.android.apis.R

/**
 * Another variation of the list of cheeses. In this case, we use [AbsListView.OnScrollListener] to
 * display the first letter of the visible range of cheeses. Uses a [Handler] thread to remove the
 * dialog displaying the letter after 3000ms using `postDelayed(mRemoveWindow, 3000)` where
 * [mRemoveWindow] is a pointer to a [RemoveWindow] class which implements [Runnable].
 */
@Suppress("MemberVisibilityCanBePrivate")
class List9 : ListActivity(), AbsListView.OnScrollListener {
    /**
     * Class we use to call our method [removeWindow] to make our [TextView] field [mDialogText]
     * invisible. It is scheduled to run 3000ms after a change to it makes it visible.
     */
    private inner class RemoveWindow : Runnable {
        override fun run() {
            removeWindow()
        }
    }

    /**
     * Instance of [RemoveWindow] we use to make our [TextView] field [mDialogText] invisible.
     */
    private val mRemoveWindow = RemoveWindow()

    /**
     * [Handler] we use to both create and add [TextView] field [mDialogText] (see the code in
     * [onCreate]) and to make it invisible again after a 3000ms delay (see the code in
     * [onScroll], [RemoveWindow] and [removeWindow]).
     */
    var mHandler = Handler()

    /**
     * Handle to the system level service WINDOW_SERVICE, we use it to call its `addView`
     * method to add [TextView] field [mDialogText] and its [removeWindow] method to remove it
     * in our [onDestroy] callback.
     */
    private var mWindowManager: WindowManager? = null

    /**
     * Translucent overlay containing the first letter of the cheese at the top of our `ListView`
     */
    private var mDialogText: TextView? = null

    /**
     * Flag indicating whether our [TextView] field [mDialogText] is currently visible or not.
     */
    private var mShowing = false

    /**
     * Flag indicating that our UI is ready to have [TextView] field [mDialogText] displayed, it is
     * set to true by the [Runnable] scheduled to run in our [onCreate] callback, in our [onResume]
     * callback, and set to false in our [onPause] and [onDestroy] callbacks. It is tested in our
     * [onScroll] callback to skip doing anything if it is false.
     */
    private var mReady = false

    /**
     * The character currently being displayed in [TextView] field [mDialogText], it is set to the
     * first letter of the cheese at the top of our `ListView` in our [onScroll] callback.
     */
    private var mPrevLetter = Character.MIN_VALUE

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Next we initialize [WindowManager] field [mWindowManager] with a handle to
     * the system level service WINDOW_SERVICE. We set the adapter of our `ListView` to a new
     * instance of [ArrayAdapter] constructed to display our [String] array field [mStrings]
     * using android.R.layout.simple_list_item_1 as the layout file. We fetch our `ListView`
     * and set its `OnScrollListener` to "this". We initialize [LayoutInflater] variable
     * `val inflate` with a [LayoutInflater], then use it to inflate the layout file with the
     * resource ID R.layout.list_position in order to initialize [TextView] field [mDialogText].
     * We then set the visibility of [mDialogText] to INVISIBLE. Finally we add a [Runnable] lambda
     * to the message queue of [Handler] field [mHandler] which will use [mWindowManager] to add
     * the view [mDialogText] to our UI.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Use an existing ListAdapter that will map an array
        // of strings to TextViews
        listAdapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1, mStrings)
        listView.setOnScrollListener(this)
        val inflate = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mDialogText = inflate.inflate(R.layout.list_position, listView, false) as TextView
        mDialogText!!.visibility = View.INVISIBLE
        mHandler.post {
            mReady = true
            val lp = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT)
            mWindowManager!!.addView(mDialogText, lp)
        }
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for our activity to start
     * interacting with the user. First we call our super's implementation of `onResume`, then we
     * set our flag [mReady] to true.
     */
    override fun onResume() {
        super.onResume()
        mReady = true
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of `onPause`,
     * then we call our method [removeWindow] to make our [TextView] field [mDialogText] INVISIBLE,
     * and finally we set our flag [mReady] to false.
     */
    override fun onPause() {
        super.onPause()
        removeWindow()
        mReady = false
    }

    /**
     * Perform any final cleanup before our activity is destroyed. First we call through to our
     * super's implementation of `onDestroy`, then we use [mWindowManager] to remove [TextView]
     * field [mDialogText], and finally we set our flag [mReady] to false.
     */
    override fun onDestroy() {
        super.onDestroy()
        mWindowManager!!.removeView(mDialogText)
        mReady = false
    }

    /**
     * Callback method to be invoked when the list or grid has been scrolled. This will be called
     * after the scroll has completed. If our flag [mReady] is true we set our [Char] variable
     * `val firstLetter` to the first letter of the [firstVisibleItem] entry in [mStrings]. If our
     * flag [mShowing] is false AND `firstLetter` is not equal to [mPrevLetter], we set our flag
     * [mShowing] to true, and set the visibility of [mDialogText] to VISIBLE. Then we set the text
     * of [mDialogText] to the string value of `firstLetter`, remove all [mRemoveWindow] callbacks
     * from the [mHandler] message queue, and schedule [mRemoveWindow] to be run 3000ms from now.
     * Finally we set [mPrevLetter] to `firstLetter`. On the other hand if [mReady] is false we
     * return having done nothing.
     *
     * @param view             The view whose scroll state is being reported
     * @param firstVisibleItem the index of the first visible cell
     * @param visibleItemCount the number of visible cells
     * @param totalItemCount   the number of items in the list adaptor
     */
    override fun onScroll(view: AbsListView, firstVisibleItem: Int,
                          visibleItemCount: Int, totalItemCount: Int) {
        if (mReady) {
            val firstLetter = mStrings[firstVisibleItem][0]
            if (!mShowing && firstLetter != mPrevLetter) {
                mShowing = true
                mDialogText!!.visibility = View.VISIBLE
            }
            mDialogText!!.text = firstLetter.toString()
            mHandler.removeCallbacks(mRemoveWindow)
            mHandler.postDelayed(mRemoveWindow, 3000)
            mPrevLetter = firstLetter
        }
    }

    /**
     * Callback method to be invoked while the list view or grid view is being scrolled. We ignore.
     *
     * @param view        The view whose scroll state is being reported
     * @param scrollState The current scroll state. Either SCROLL_STATE_TOUCH_SCROLL or SCROLL_STATE_IDLE.
     */
    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}

    /**
     * Convenience function to make [TextView] field [mDialogText] INVISIBLE. If our flag [mShowing]
     * is true we set it to false and set the visibility of [TextView] field [mDialogText] to
     * INVISIBLE. If on the other hand [mShowing] is false we return having done nothing.
     */
    private fun removeWindow() {
        if (mShowing) {
            mShowing = false
            mDialogText!!.visibility = View.INVISIBLE
        }
    }

    /**
     * Reference to [Cheeses.sCheeseStrings] that we use as our data array.
     */
    private val mStrings = Cheeses.sCheeseStrings
}