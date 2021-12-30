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
@file:Suppress("DEPRECATION")
// TODO: replace deprecated OnSystemUiVisibilityChangeListener with OnApplyWindowInsetsListener
// TODO: replace SYSTEM_UI_FLAG_* with WindowInsetsController

package com.example.android.apis.view

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.View.OnSystemUiVisibilityChangeListener
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import com.example.android.apis.graphics.TouchPaint.PaintView

/**
 * This activity demonstrates how to use the system UI flags to
 * implement an immersive game.
 */
@Suppress("MemberVisibilityCanBePrivate", "RedundantOverride")
class GameActivity : AppCompatActivity() {
    /**
     * Implementation of a view for the game, filling the entire screen.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class Content(context: Context?, attrs: AttributeSet?) :
            PaintView(context, attrs),
            OnSystemUiVisibilityChangeListener,
            View.OnClickListener
    {
        /**
         * Set by our containing Activity to "this" by calling our [init] method, but never
         * used.
         */
        var mActivity: Activity? = null

        /**
         * Set by our containing Activity to the [Button] in its layout with resource id
         * R.id.play by calling our [init] method, we set its `OnClickListener` to this
         * and toggle our paused state when the user clicks on it.
         */
        var mPlayButton: Button? = null

        /**
         * Flag indicating whether we are paused of not. When true the navigation UI elements are
         * displayed and the text of [Button] field [mPlayButton] is set to "Play". When false the
         * navigation UI elements are invisible and the text of [Button] field [mPlayButton] is set
         * to "Pause".
         */
        var mPaused = false

        /**
         * The last system UI visibility mask passed to our [onSystemUiVisibilityChange] callback.
         */
        var mLastSystemUiVis = 0

        /**
         * Flag indicating that the system UI state should be updated during the next game loop
         */
        var mUpdateSystemUi = false

        /**
         * [Runnable] which is used to fade the current finger painting.
         */
        var mFader: Runnable = object : Runnable {
            /**
             * First we call our method [fade] to fade the current finger painting a bit, then
             * if our [mUpdateSystemUi] flag is true we call our [updateNavVisibility] method to
             * make the system UI invisible if we are running. Then if we are not paused we reschedule
             * ourselves to run again in 33ms.
             */
            override fun run() {
                fade()
                if (mUpdateSystemUi) {
                    updateNavVisibility()
                }
                if (!mPaused) {
                    handler.postDelayed(this, 1000 / 30.toLong())
                }
            }
        }

        /**
         * This is called by the containing activity to supply the surrounding state of the game
         * that it will interact with. We save our [Activity] parameter [activity] in our field
         * [mActivity], and our [Button] parameter [playButton] in our field [mPlayButton], set
         * the `OnClickListener` of [mPlayButton] to "this" and call our method [setGamePaused] with
         * true as the argument to place our game in the "paused" mode.
         *
         * @param activity   [Activity] of the containing class
         * @param playButton [Button] which toggles visibility modes
         */
        fun init(activity: Activity?, playButton: Button?) {
            // This called by the containing activity to supply the surrounding
            // state of the game that it will interact with.
            mActivity = activity
            mPlayButton = playButton
            mPlayButton!!.setOnClickListener(this)
            setGamePaused(true)
        }

        /**
         * Called when the status bar changes visibility. We initialize our [Int] variable `val diff`
         * by xor'ing [mLastSystemUiVis] (the previous visibility mask) with our parameter [visibility]
         * (the new visibility mask) isolating the bits that have changed state. We then set
         * [mLastSystemUiVis] to [visibility]. If our field [mPaused] is false (we are "running")
         * and the bit that changed was SYSTEM_UI_FLAG_HIDE_NAVIGATION, and the new value in
         * [visibility] is 0 (system UI navigation has become visible), we set our field
         * [mUpdateSystemUi] to true so that we will update our system UI state during the next
         * game loop.
         *
         * @param visibility Bitwise-or of flags SYSTEM_UI_FLAG_LOW_PROFILE, SYSTEM_UI_FLAG_HIDE_NAVIGATION,
         * and SYSTEM_UI_FLAG_FULLSCREEN. This tells you the global state of these UI
         * visibility flags, not what your app is currently applying.
         */
        override fun onSystemUiVisibilityChange(visibility: Int) {
            // Detect when we go out of nav-hidden mode, to reset back to having
            // it hidden; our game wants those elements to stay hidden as long
            // as it is being played and stay shown when paused.
            val diff = mLastSystemUiVis xor visibility
            mLastSystemUiVis = visibility
            if (!mPaused && diff and SYSTEM_UI_FLAG_HIDE_NAVIGATION != 0 && visibility and SYSTEM_UI_FLAG_HIDE_NAVIGATION == 0) {
                // We are running and the system UI navigation has become
                // shown...  we want it to remain hidden, so update our system
                // UI state at the next game loop.
                mUpdateSystemUi = true
            }
        }

        /**
         * Called when the containing window has changed its visibility (between GONE, INVISIBLE,
         * and VISIBLE). First we call our super's implementation of `onWindowVisibilityChanged`,
         * then we call our method [setGamePaused] with true as the argument to pause our game.
         *
         * @param visibility The new visibility of the window.
         */
        override fun onWindowVisibilityChanged(visibility: Int) {
            super.onWindowVisibilityChanged(visibility)

            // When we become visible or invisible, play is paused.
            setGamePaused(true)
        }

        /**
         * Called when the window containing this view gains or loses focus. First we call our super's
         * implementation of `onWindowFocusChanged`, then we check if our parameter [hasWindowFocus]
         * is false (our window has lost focus) and if so we "could" pause the game if we wanted to.
         *
         * @param hasWindowFocus True if the window containing this view now has focus, false otherwise.
         */
        override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
            super.onWindowFocusChanged(hasWindowFocus)

            // When we become visible or invisible, play is paused.
            // Optional: pause game when window loses focus.  This will cause it to
            // pause, for example, when the notification shade is pulled down.
            @Suppress("ControlFlowWithEmptyBody")
            if (!hasWindowFocus) {
                //setGamePaused(true);
            }
        }

        /**
         * Called when our view has been clicked. If the view is our field [mPlayButton] we call our
         * method [setGamePaused] with the negation of [mPaused] toggling the state between play and
         * pause.
         *
         * @param v The view that was clicked.
         */
        override fun onClick(v: View) {
            if (v === mPlayButton) {
                // Clicking on the play/pause button toggles its state.
                setGamePaused(!mPaused)
            }
        }

        /**
         * Sets the state of the game to paused if its parameter is true, or play if its parameter
         * is false. We save our [Boolean] parameter [paused] in our field [mPaused], then if [paused]
         * is true we set the text of [Button] field [mPlayButton] to the string with the resource
         * id R.string.play ("Play"), or if false to the string with the resource id R.string.pause
         * ("Pause"). We call the method [setKeepScreenOn] with the inverse of [paused] to keep the
         * in pause mode. We call our method [updateNavVisibility] to hide or show the system
         * navigation UI depending on the new state of [mPaused] (hide it if it is false indicating
         * that we are "playing", show it if it is true indicating that we are paused). We initialize
         * `Handler` variable `val h` with a handler associated with the thread running our View, and
         * if it is not null we remove all scheduled [Runnable] field [mFader] in its queue, and if
         * we are not paused we run [mFader] then call the method [text] (in the [PaintView] class
         * of `TouchPaint`) to draw the string "Draw!" to our view.
         *
         * @param paused if true we go into paused state, false we go into play state.
         */
        fun setGamePaused(paused: Boolean) {
            mPaused = paused
            mPlayButton!!.setText(if (paused) R.string.play else R.string.pause)
            keepScreenOn = !paused
            updateNavVisibility()
            val h = handler
            if (h != null) {
                handler.removeCallbacks(mFader)
                if (!paused) {
                    mFader.run()
                    text("Draw!")
                }
            }
        }

        /**
         * Updates the system UI visibility based on the current state of `mPaused`. We start
         * by initializing our variable `var newVis` by or'ing the following flags together:
         *
         *  * SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN - our View would like its window to be laid out
         *  as if it has requested SYSTEM_UI_FLAG_FULLSCREEN, even if it currently hasn't.
         *  This allows it to avoid artifacts when switching in and out of that mode, at the
         *  expense that some of its user interface may be covered by screen decorations
         *  when they are shown.
         *
         *  * SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION - our View would like its window to be laid
         *  out as if it has requested SYSTEM_UI_FLAG_HIDE_NAVIGATION, even if it currently
         *  hasn't. This allows it to avoid artifacts when switching in and out of that mode,
         *  at the expense that some of its user interface may be covered by screen decorations
         *  when they are shown.
         *
         *  * SYSTEM_UI_FLAG_LAYOUT_STABLE - When using other layout flags, we would like a stable
         *  view of the content insets given to fitSystemWindows(Rect). This means that the
         *  insets seen there will always represent the worst case that the application can
         *  expect as a continuous state. In the stock Android UI this is the space for the
         *  system bar, nav bar, and status bar, but not more transient elements such as an
         *  input method.
         *
         * Then if our field [mPaused] is false (we are playing) we add the following bit flags:
         *
         *  * SYSTEM_UI_FLAG_LOW_PROFILE -  View requests the system UI to enter an unobtrusive
         *  "low profile" mode. In low profile mode, the status bar and/or navigation icons may dim.
         *
         *  * SYSTEM_UI_FLAG_FULLSCREEN - View requests that we go into the normal fullscreen
         *  mode so that our content can take over the screen while still allowing the user
         *  to interact with the application. Non-critical screen decorations (such as the
         *  status bar) will be hidden while the user is in the View's window.
         *
         *  * SYSTEM_UI_FLAG_HIDE_NAVIGATION -  View requests that the system navigation be
         *  temporarily hidden. On devices that draw essential navigation controls (Home, Back,
         *  and the like) on screen, SYSTEM_UI_FLAG_HIDE_NAVIGATION will cause those to disappear.
         *
         *  * SYSTEM_UI_FLAG_IMMERSIVE_STICKY - View would like to remain interactive when hiding
         *  the status bar with SYSTEM_UI_FLAG_FULLSCREEN and/or hiding the navigation bar with
         *  SYSTEM_UI_FLAG_HIDE_NAVIGATION. If this flag is not set, SYSTEM_UI_FLAG_HIDE_NAVIGATION
         *  will be force cleared by the system on any user interaction, and SYSTEM_UI_FLAG_FULLSCREEN
         *  will be force-cleared by the system if the user swipes from the top of the screen.
         *  When system bars are hidden in immersive mode, they can be revealed temporarily with
         *  system gestures, such as swiping from the top of the screen. These transient system
         *  bars will overlay app’s content, may have some degree of transparency, and will
         *  automatically hide after a short timeout. Since this flag is a modifier for
         *  SYSTEM_UI_FLAG_FULLSCREEN and SYSTEM_UI_FLAG_HIDE_NAVIGATION, it only has an effect
         *  when used in combination with one or both of those flags.
         *
         * We then call the method [setSystemUiVisibility] with the argument `newVis` to request
         * that the visibility of the status bar or other screen/window decorations be changed.
         * Finally we set our field [mUpdateSystemUi] to false.
         */
        @TargetApi(Build.VERSION_CODES.KITKAT)
        fun updateNavVisibility() {
            var newVis = (SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or SYSTEM_UI_FLAG_LAYOUT_STABLE)
            if (!mPaused) {
                newVis = newVis or (SYSTEM_UI_FLAG_LOW_PROFILE or SYSTEM_UI_FLAG_FULLSCREEN
                        or SYSTEM_UI_FLAG_HIDE_NAVIGATION or SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }

            // Set the new desired visibility.
            systemUiVisibility = newVis
            mUpdateSystemUi = false
        }

        /**
         * The init block of our constructor which is called when we are inflated from an xml layout
         * file. We register "this" as a listener to receive callbacks when the visibility of the
         * system bar changes.
         */
        init {
            setOnSystemUiVisibilityChangeListener(this)
        }
    }

    /**
     * Our instance of [Content].
     */
    var mContent: Content? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.game. We initialize
     * our [Content] field [mContent] by finding the view with id R.id.content, and use it to
     * call its `init` method with "this" as the [Activity] argument, and the [Button] with the id
     * R.id.play as the [Button] argument.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game)
        mContent = findViewById(R.id.content)
        mContent!!.init(this, findViewById(R.id.play))
    }

    /**
     * Called when the main window associated with the activity has been attached to the window
     * manager. We just call our super's implementation of `onAttachedToWindow`.
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. We call our super's implementation of `onPause` then call the
     * `setGamePaused` method of our field [mContent] with the argument true to pause the game while
     * our activity is paused.
     */
    override fun onPause() {
        super.onPause()

        // Pause game when its activity is paused.
        mContent!!.setGamePaused(true)
    }
}