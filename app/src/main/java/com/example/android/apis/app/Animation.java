/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.example.android.apis.app;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


/**
 * <p>Example of using a custom animation when transitioning between activities.</p>
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class Animation extends Activity {
    private static final String TAG = "Animation";

    /**
     * Called when the activity is starting. First we call through to our super's implementation
     * of onCreate, then we set our content view to our layout file R.layout.activity_animation.
     *
     * Now we set up the OnClickListener's for our Button's for all versions of android:
     *
     *     R.id.fade_animation ("Fade in") is set to mFadeListener which uses the animation
     *         R.anim.fade (an animation of alpha from 0.0 to 1.0) which it specifies using
     *         overridePendingTransition after the startActivity for the Activity we are
     *         transitioning to.
     *     R.id.zoom_animation ("Zoom in") is set to mZoomListener which uses the animation
     *         R.anim.zoom_enter for the new Activity (uses a scale animation to scale from
     *         2.0 to 1.0 around a centered pivot point), and R.anim.zoom_exit for the old
     *         Activity (uses a scale animation to scale from 1.0 to 0.5 around a centered
     *         pivot point, combined in a set with an alpha animation from 1.0 to 0.0) which
     *         it specifies using overridePendingTransition after the startActivity for the
     *         Activity we are transitioning to.
     *
     * The next 5 Button's are surrounded by an if/else statement. For devices with a version
     * android.os.Build.VERSION.SDK_INT less than android.os.Build.VERSION_CODES.JELLY_BEAN
     * 4 of them are disabled (the R.id.no_animation ("NO ANIMATION") Button should really
     * be outside the if/else). For JELLY_BEAN and above:
     *
     *     R.id.modern_fade_animation ("Modern fade in") is set to mModernFadeListener which uses
     *         an options bundle passed to startActivity containing the same animations used by
     *         R.id.fade_animation above.
     *     R.id.modern_zoom_animation ("Modern Zoom in") is set to  mModernZoomListener which uses
     *         an options bundle passed to startActivity containing the same animations used by
     *         R.id.zoom_animation above.
     *     R.id.scale_up_animation ("SCALE UP") is set to mScaleUpListener which uses an options
     *         bundle passed to startActivity that it creates using makeScaleUpAnimation
     *     R.id.zoom_thumbnail_animation ("Thumbnail zoom") is set to mZoomThumbnailListener which
     *         creates a zoom from the Button pressed to the new Activity using an options bundle
     *         that it creates using makeThumbnailScaleUpAnimation
     *     R.id.no_animation ("NO ANIMATION") uses overridePendingTransition after the startActivity
     *         to set the animation used to 0 in and 0 out. Should be outside the if/else statement.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_animation);

        // Watch for button clicks.
        Button button = (Button)findViewById(R.id.fade_animation);
        button.setOnClickListener(mFadeListener);
        button = (Button)findViewById(R.id.zoom_animation);
        button.setOnClickListener(mZoomListener);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            button = (Button)findViewById(R.id.modern_fade_animation);
            button.setOnClickListener(mModernFadeListener);
            button = (Button)findViewById(R.id.modern_zoom_animation);
            button.setOnClickListener(mModernZoomListener);
            button = (Button)findViewById(R.id.scale_up_animation);
            button.setOnClickListener(mScaleUpListener);
            button = (Button)findViewById(R.id.zoom_thumbnail_animation);
            button.setOnClickListener(mZoomThumbnailListener);
            button = (Button)findViewById(R.id.no_animation);
            button.setOnClickListener(mNoAnimationListener);
        } else {
            findViewById(R.id.modern_fade_animation).setEnabled(false);
            findViewById(R.id.modern_zoom_animation).setEnabled(false);
            findViewById(R.id.scale_up_animation).setEnabled(false);
            findViewById(R.id.zoom_thumbnail_animation).setEnabled(false);
        }
    }

    /**
     * Activities cannot draw during the period that their windows are animating in. In order
     * to know when it is safe to begin drawing they can override this method which will be
     * called when the entering animation has completed.
     */
    @Override
    public void onEnterAnimationComplete() {
        Log.i(TAG, "onEnterAnimationComplete");
    }

    private OnClickListener mFadeListener = new OnClickListener() {
        public void onClick(View v) {
            Log.i(TAG, "Starting fade-in animation...");
            // Request the next activity transition (here starting a new one).
            startActivity(new Intent(Animation.this, AlertDialogSamples.class));
            // Supply a custom animation.  This one will just fade the new
            // activity on top.  Note that we need to also supply an animation
            // (here just doing nothing for the same amount of time) for the
            // old activity to prevent it from going away too soon.
            overridePendingTransition(R.anim.fade, R.anim.hold);
        }
    };

    private OnClickListener mZoomListener = new OnClickListener() {
        public void onClick(View v) {
            Log.i(TAG, "Starting zoom-in animation...");
            // Request the next activity transition (here starting a new one).
            startActivity(new Intent(Animation.this, AlertDialogSamples.class));
            // This is a more complicated animation, involving transformations
            // on both this (exit) and the new (enter) activity.  Note how for
            // the duration of the animation we force the exiting activity
            // to be Z-ordered on top (even though it really isn't) to achieve
            // the effect we want.
            overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
        }
    };

    private OnClickListener mModernFadeListener = new OnClickListener() {
        public void onClick(View v) {
            Log.i(TAG, "Starting modern-fade-in animation...");
            // Create the desired custom animation, involving transformations
            // on both this (exit) and the new (enter) activity.  Note how for
            // the duration of the animation we force the exiting activity
            // to be Z-ordered on top (even though it really isn't) to achieve
            // the effect we want.
            ActivityOptions opts = ActivityOptions.makeCustomAnimation(Animation.this,
                    R.anim.fade, R.anim.hold);
            // Request the activity be started, using the custom animation options.
            startActivity(new Intent(Animation.this, AlertDialogSamples.class), opts.toBundle());
        }
    };

    private OnClickListener mModernZoomListener = new OnClickListener() {
        public void onClick(View v) {
            Log.i(TAG, "Starting modern-zoom-in animation...");
            // Create a more complicated animation, involving transformations
            // on both this (exit) and the new (enter) activity.  Note how for
            // the duration of the animation we force the exiting activity
            // to be Z-ordered on top (even though it really isn't) to achieve
            // the effect we want.
            ActivityOptions opts = ActivityOptions.makeCustomAnimation(Animation.this,
                    R.anim.zoom_enter, R.anim.zoom_enter);
            // Request the activity be started, using the custom animation options.
            startActivity(new Intent(Animation.this, AlertDialogSamples.class), opts.toBundle());
        }
    };

    private OnClickListener mScaleUpListener = new OnClickListener() {
        public void onClick(View v) {
            Log.i(TAG, "Starting scale-up animation...");
            // Create a scale-up animation that originates at the button
            // being pressed.
            ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(
                    v, 0, 0, v.getWidth(), v.getHeight());
            // Request the activity be started, using the custom animation options.
            startActivity(new Intent(Animation.this, AlertDialogSamples.class), opts.toBundle());
        }
    };

    private OnClickListener mZoomThumbnailListener = new OnClickListener() {
        public void onClick(View v) {
            Log.i(TAG, "Starting thumbnail-zoom animation...");
            // Create a thumbnail animation.  We are going to build our thumbnail
            // just from the view that was pressed.  We make sure the view is
            // not selected, because by the time the animation starts we will
            // have finished with the selection of the tap.
            v.setDrawingCacheEnabled(true);
            v.setPressed(false);
            v.refreshDrawableState();
            Bitmap bm = v.getDrawingCache();
            //noinspection unused
            Canvas c = new Canvas(bm);
            //c.drawARGB(255, 255, 0, 0);
            ActivityOptions opts = ActivityOptions.makeThumbnailScaleUpAnimation(
                    v, bm, 0, 0);
            // Request the activity be started, using the custom animation options.
            startActivity(new Intent(Animation.this, AlertDialogSamples.class), opts.toBundle());
            v.setDrawingCacheEnabled(false);
        }
    };

    private OnClickListener mNoAnimationListener = new OnClickListener() {
        public void onClick(View v) {
            Log.i(TAG, "Starting no animation transition...");
            // Request the next activity transition (here starting a new one).
            startActivity(new Intent(Animation.this, AlertDialogSamples.class));
            overridePendingTransition(0, 0);
        }
    };
}
