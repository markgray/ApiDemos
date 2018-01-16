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
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.example.android.apis.R;
import com.example.android.apis.graphics.TouchPaint;

/**
 * This activity demonstrates how to use the system UI flags to
 * implement an immersive game.
 */
public class GameActivity extends Activity {

    /**
     * Implementation of a view for the game, filling the entire screen.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class Content extends TouchPaint.PaintView implements
            View.OnSystemUiVisibilityChangeListener, View.OnClickListener {

        /**
         * Set by our containing Activity to "this" by calling our {@code init} method, but never
         * used.
         */
        Activity mActivity;
        /**
         * Set by our containing Activity to the {@code Button} in its layout with resource id
         * R.id.play by calling our {@code init} method, we set its {@code OnClickListener} to this
         * and toggle our paused state when the user clicks on it.
         */
        Button mPlayButton;
        /**
         * Flag indicating whether we are paused of not. When true the navigation UI elements are
         * displayed and the text of {@code Button mPlayButton} is set to "Play". When false the
         * navigation UI elements are invisible and the text of {@code Button mPlayButton} is set to
         * "Pause".
         */
        boolean mPaused;
        /**
         * The last system UI visibility mask passed to our {@code onSystemUiVisibilityChange} callback.
         */
        int mLastSystemUiVis;
        /**
         * Flag indicating that the system UI state should be updated during the next game loop
         */
        boolean mUpdateSystemUi;

        /**
         * {@code Runnable} which is used to fade the current finger painting.
         */
        Runnable mFader = new Runnable() {
            /**
             * First we call our method {@code fade} to fade the current finger painting a bit, then
             * if our {@code mUpdateSystemUi} flag is true we call our {@code updateNavVisibility} to
             * make the system UI invisible if we are running. Then if we are not paused we reschedule
             * ourselves to run again in 33ms.
             */
            @Override
            public void run() {
                fade();
                if (mUpdateSystemUi) {
                    updateNavVisibility();
                }
                if (!mPaused) {
                    getHandler().postDelayed(mFader, 1000 / 30);
                }
            }
        };

        /**
         * Our constructor which is called when we are inflated from an xml layout file. First we
         * call our super's constructor. Then we register "this" as a listener to receive callbacks
         * when the visibility of the system bar changes.
         *
         * @param context The Context the view is running in, through which it can
         *                access the current theme, resources, etc.
         * @param attrs   The attributes of the XML tag that is inflating the view.
         */
        public Content(Context context, AttributeSet attrs) {
            super(context, attrs);
            setOnSystemUiVisibilityChangeListener(this);
        }

        /**
         * This is called by the containing activity to supply the surrounding state of the game
         * that it will interact with. We save our parameter {@code Activity activity} in our field
         * {@code Activity mActivity}, and our parameter {@code Button playButton} in our field
         * {@code Button mPlayButton}, set the {@code OnClickListener} of {@code mPlayButton} to
         * "this" and call our method {@code setGamePaused(true)} to place our game in the "paused"
         * mode.
         *
         * @param activity   {@code Activity} of the containing class
         * @param playButton {@code Button} which toggles visibility modes
         */
        public void init(Activity activity, Button playButton) {
            // This called by the containing activity to supply the surrounding
            // state of the game that it will interact with.
            mActivity = activity;
            mPlayButton = playButton;
            mPlayButton.setOnClickListener(this);
            setGamePaused(true);
        }

        /**
         * Called when the status bar changes visibility. We initialize our variable {@code int diff}
         * by xor'ing {@code mLastSystemUiVis} (the previous visibility mask) with our parameter
         * {@code int visibility} (the new visibility mask) isolating the bits that have changed state.
         * We then set {@code mLastSystemUiVis} to {@code visibility}. If our field {@code mPaused} is
         * false (we are "running") and the bit that changed was SYSTEM_UI_FLAG_HIDE_NAVIGATION, and the
         * new value in {@code visibility} is 0 (system UI navigation has become visible), we set our field
         * {@code mUpdateSystemUi} to true so that we will update our system UI state during the next
         * game loop.
         *
         * @param visibility Bitwise-or of flags SYSTEM_UI_FLAG_LOW_PROFILE, SYSTEM_UI_FLAG_HIDE_NAVIGATION,
         *                   and SYSTEM_UI_FLAG_FULLSCREEN. This tells you the global state of these UI
         *                   visibility flags, not what your app is currently applying.
         */
        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            // Detect when we go out of nav-hidden mode, to reset back to having
            // it hidden; our game wants those elements to stay hidden as long
            // as it is being played and stay shown when paused.
            int diff = mLastSystemUiVis ^ visibility;
            mLastSystemUiVis = visibility;
            if (!mPaused && (diff & SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0
                    && (visibility & SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                // We are running and the system UI navigation has become
                // shown...  we want it to remain hidden, so update our system
                // UI state at the next game loop.
                mUpdateSystemUi = true;
            }
        }

        /**
         * Called when the containing window has changed its visibility (between GONE, INVISIBLE,
         * and VISIBLE). First we call our super's implementation of {@code onWindowVisibilityChanged},
         * then we call our method {@code setGamePaused(true)} to pause our game.
         *
         * @param visibility The new visibility of the window.
         */
        @Override
        protected void onWindowVisibilityChanged(int visibility) {
            super.onWindowVisibilityChanged(visibility);

            // When we become visible or invisible, play is paused.
            setGamePaused(true);
        }

        /**
         * Called when the window containing this view gains or loses focus. First we call our super's
         * implementation of {@code onWindowFocusChanged}, then we check if our parameter {@code hasWindowFocus}
         * is false (our window has lost focus) and if so we "could" pause the game if we wanted to.
         *
         * @param hasWindowFocus True if the window containing this view now has focus, false otherwise.
         */
        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus);

            // When we become visible or invisible, play is paused.
            // Optional: pause game when window loses focus.  This will cause it to
            // pause, for example, when the notification shade is pulled down.
            //noinspection StatementWithEmptyBody
            if (!hasWindowFocus) {
                //setGamePaused(true);
            }
        }

        /**
         * Called when our view has been clicked. If the view is our field {@code mPlayButton} we
         * call our method {@code setGamePaused(!mPaused)} toggling the state between play and pause.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            if (v == mPlayButton) {
                // Clicking on the play/pause button toggles its state.
                setGamePaused(!mPaused);
            }
        }

        /**
         * Sets the state of the game to paused if its parameter is true, or play if its parameter is
         * false. We save our parameter {@code boolean paused} in our field {@code boolean mPaused},
         * then if {@code paused} is true we set the text of {@code Button mPlayButton} to the string
         * with the resource id R.string.play ("Play"), or if false to the string with the resource
         * id R.string.pause ("Pause"). We call the method {@code setKeepScreenOn} with the inverse
         * of {@code paused} to keep the screen on if were are playing, or not keep it on if we are
         * in pause mode. We call our method {@code updateNavVisibility} to hide or show the system
         * navigation UI depending on the new state of {@code mPaused} (hide if it is false indicating
         * that we are "playing", show it if it is true indicating that we are paused). We initialize
         * {@code Handler h} with a handler associated with the thread running our View, and if it
         * is not null we remove all scheduled {@code Runnable mFader} in its queue, and if we are
         * not paused we run {@code mFader} then call the method {@code TouchPaint.PaintView.text}
         * to draw the string "Draw!" to our view.
         *
         * @param paused if true we go into paused state, false we go into play state.
         */
        void setGamePaused(boolean paused) {
            mPaused = paused;
            mPlayButton.setText(paused ? R.string.play : R.string.pause);
            setKeepScreenOn(!paused);
            updateNavVisibility();
            Handler h = getHandler();
            if (h != null) {
                getHandler().removeCallbacks(mFader);
                if (!paused) {
                    mFader.run();
                    text("Draw!");
                }
            }
        }

        /**
         * Updates the system UI visibility based on the current state of {@code mPaused}. We start
         * by initializing our variable {@code int newVis} by or'ing the following flags together:
         * <ul>
         * <li>
         * SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN - our View would like its window to be laid out
         * as if it has requested SYSTEM_UI_FLAG_FULLSCREEN, even if it currently hasn't.
         * This allows it to avoid artifacts when switching in and out of that mode, at the
         * expense that some of its user interface may be covered by screen decorations
         * when they are shown.
         * </li>
         * <li>
         * SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION - our View would like its window to be laid
         * out as if it has requested SYSTEM_UI_FLAG_HIDE_NAVIGATION, even if it currently
         * hasn't. This allows it to avoid artifacts when switching in and out of that mode,
         * at the expense that some of its user interface may be covered by screen decorations
         * when they are shown.
         * </li>
         * <li>
         * SYSTEM_UI_FLAG_LAYOUT_STABLE - When using other layout flags, we would like a stable
         * view of the content insets given to fitSystemWindows(Rect). This means that the
         * insets seen there will always represent the worst case that the application can
         * expect as a continuous state. In the stock Android UI this is the space for the
         * system bar, nav bar, and status bar, but not more transient elements such as an
         * input method.
         * </li>
         * </ul>
         * Then if our field {@code mPaused} is false (we are playing) we add the following bit flags:
         * <ul>
         * <li>
         * SYSTEM_UI_FLAG_LOW_PROFILE -  View requests the system UI to enter an unobtrusive
         * "low profile" mode. In low profile mode, the status bar and/or navigation icons may dim.
         * </li>
         * <li>
         * SYSTEM_UI_FLAG_FULLSCREEN - View requests that we go into the normal fullscreen
         * mode so that our content can take over the screen while still allowing the user
         * to interact with the application. Non-critical screen decorations (such as the
         * status bar) will be hidden while the user is in the View's window.
         * </li>
         * <li>
         * SYSTEM_UI_FLAG_HIDE_NAVIGATION -  View requests that the system navigation be
         * temporarily hidden. On devices that draw essential navigation controls (Home, Back,
         * and the like) on screen, SYSTEM_UI_FLAG_HIDE_NAVIGATION will cause those to disappear.
         * </li>
         * <li>
         * SYSTEM_UI_FLAG_IMMERSIVE_STICKY - View would like to remain interactive when hiding
         * the status bar with SYSTEM_UI_FLAG_FULLSCREEN and/or hiding the navigation bar with
         * SYSTEM_UI_FLAG_HIDE_NAVIGATION. If this flag is not set, SYSTEM_UI_FLAG_HIDE_NAVIGATION
         * will be force cleared by the system on any user interaction, and SYSTEM_UI_FLAG_FULLSCREEN
         * will be force-cleared by the system if the user swipes from the top of the screen.
         * When system bars are hidden in immersive mode, they can be revealed temporarily with
         * system gestures, such as swiping from the top of the screen. These transient system
         * bars will overlay app’s content, may have some degree of transparency, and will
         * automatically hide after a short timeout. Since this flag is a modifier for
         * SYSTEM_UI_FLAG_FULLSCREEN and SYSTEM_UI_FLAG_HIDE_NAVIGATION, it only has an effect
         * when used in combination with one or both of those flags.
         * </li>
         * </ul>
         * We then call the method {@code setSystemUiVisibility} with the argument {@code newVis} to
         * request that the visibility of the status bar or other screen/window decorations be changed.
         * Finally we set our field {@code mUpdateSystemUi} to false.
         */
        @TargetApi(Build.VERSION_CODES.KITKAT)
        void updateNavVisibility() {
            int newVis = SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | SYSTEM_UI_FLAG_LAYOUT_STABLE;
            if (!mPaused) {
                newVis |= SYSTEM_UI_FLAG_LOW_PROFILE | SYSTEM_UI_FLAG_FULLSCREEN
                        | SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }

            // Set the new desired visibility.
            setSystemUiVisibility(newVis);
            mUpdateSystemUi = false;
        }
    }

    /**
     * Our instance of {@code Content}.
     */
    Content mContent;

    /**
     * Our constructor.
     */
    public GameActivity() {
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.game. We initialize
     * our field {@code Content mContent} by finding the view with id R.id.content, and use it to
     * call its {@code init} method with "this" as the {@code Activity} argument, and the {@code Button}
     * with the id R.id.play as the {@code Button} argument.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.game);
        mContent = (Content) findViewById(R.id.content);
        mContent.init(this, (Button) findViewById(R.id.play));
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
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. We call our super's implementation of {@code onPause} then call the
     * {@code setGamePaused(true)} of our field {@code mContent} to pause the game while our activity
     * is paused.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Pause game when its activity is paused.
        mContent.setGamePaused(true);
    }
}
