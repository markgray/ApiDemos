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

package com.example.android.apis.app

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import com.example.android.apis.R


/**
 *
 * Example of using a custom animation when transitioning between activities.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class Animation : Activity() {

    /**
     * Called when the R.id.fade_animation ("Fade in") Button is clicked. We start the
     * Activity AlertDialogSamples, then call overridePendingTransition to specify an
     * explicit transition animation to perform: R.anim.fade to use for the incoming
     * Activity (an alpha animation from 0.0 to 1.0), and R.anim.hold for the outgoing
     * Activity (a do-nothing animation of fromDeltaX from 0.0 to 0.0 for the same time
     * duration as the incoming animation)
     *
     * Parameter: The [View] of the Button which was clicked
     */
    private val mFadeListener = OnClickListener{
        Log.i(TAG, "Starting fade-in animation...")
        // Request the next activity transition (here starting a new one).
        startActivity(Intent(this@Animation, AlertDialogSamples::class.java))
        // Supply a custom animation.  This one will just fade the new
        // activity on top.  Note that we need to also supply an animation
        // (here just doing nothing for the same amount of time) for the
        // old activity to prevent it from going away too soon.
        overridePendingTransition(R.anim.fade, R.anim.hold)
    }

    /**
     * Called when the R.id.zoom_animation ("Zoom in") Button is clicked. We start the
     * Activity AlertDialogSamples, then call overridePendingTransition to specify an
     * explicit transition animation to perform: R.anim.zoom_enter to use for the incoming
     * Activity (a scale animation from 2.0 to 1.0 around a centered pivot point), and
     * R.anim.zoom_exit for the outgoing Activity (a scale animation to scale from 1.0 to 0.5
     * around a centered pivot point, combined in a set with an alpha animation from 1.0 to
     * 0.0)
     *
     * Parameter: The [View] of the Button which was clicked
     */
    private val mZoomListener = OnClickListener {
        Log.i(TAG, "Starting zoom-in animation...")
        // Request the next activity transition (here starting a new one).
        startActivity(Intent(this@Animation, AlertDialogSamples::class.java))
        // This is a more complicated animation, involving transformations
        // on both this (exit) and the new (enter) activity.  Note how for
        // the duration of the animation we force the exiting activity
        // to be Z-ordered on top (even though it really isn't) to achieve
        // the effect we want.
        overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit)
    }

   /**
     * Called when the R.id.modern_fade_animation ("Modern fade in") Button is clicked. We
     * create ActivityOptions opts using R.anim.fade as the fade in animation (an alpha
     * animation from 0.0 to 1.0) and R.anim.hold as the fade out animation (a do-nothing
     * animation of fromDeltaX from 0.0 to 0.0 for the same time duration as the incoming
     * animation). Then we start the activity AlertDialogSamples using a Bundle created
     * from "opts" to tell how to animate the transition to the new Activity.
     *
     * Parameter: The [View] of the Button which was clicked
     */
    private val mModernFadeListener = OnClickListener {
        Log.i(TAG, "Starting modern-fade-in animation...")
        // Create the desired custom animation, involving transformations
        // on both this (exit) and the new (enter) activity.  Note how for
        // the duration of the animation we force the exiting activity
        // to be Z-ordered on top (even though it really isn't) to achieve
        // the effect we want.
        val opts = ActivityOptions.makeCustomAnimation(this@Animation,
                R.anim.fade, R.anim.hold)
        // Request the activity be started, using the custom animation options.
        startActivity(Intent(this@Animation, AlertDialogSamples::class.java), opts.toBundle())
    }

    /**
     * Called when the R.id.modern_zoom_animation ("Modern Zoom in") Button is clicked. We
     * create ActivityOptions opts using R.anim.zoom_enter as the animation to use for the
     * incoming Activity (a scale animation from 2.0 to 1.0 around a centered  pivot point),
     * and R.anim.zoom_exit for the outgoing Activity (a scale animation to scale from 1.0
     * to 0.5 around a centered pivot point combined in a set with an alpha animation from
     * 1.0 to 0.0) Then we start the activity AlertDialogSamples using a Bundle created
     * from "opts" to tell how to animate the transition to the new Activity.
     *
     * Parameter: The [View] of the Button which was clicked
     */
    private val mModernZoomListener = OnClickListener {
        Log.i(TAG, "Starting modern-zoom-in animation...")
        // Create a more complicated animation, involving transformations
        // on both this (exit) and the new (enter) activity.  Note how for
        // the duration of the animation we force the exiting activity
        // to be Z-ordered on top (even though it really isn't) to achieve
        // the effect we want.
        val opts = ActivityOptions.makeCustomAnimation(this@Animation,
                R.anim.zoom_enter, R.anim.zoom_enter)
        // Request the activity be started, using the custom animation options.
        startActivity(Intent(this@Animation, AlertDialogSamples::class.java), opts.toBundle())
    }

    /**
     * Called when the R.id.scale_up_animation ("SCALE UP") Button is clicked. We create
     * ActivityOptions opts using makeScaleUpAnimation, which specifies an animation which
     * scales up from the Button which was pressed. Then we start the activity
     * AlertDialogSamples using a Bundle created from "opts" to tell how to animate the
     * transition to the new Activity.
     *
     * Parameter: The [View] of the Button which was clicked
     */
    private val mScaleUpListener = OnClickListener { v ->

        Log.i(TAG, "Starting scale-up animation...")
        // Create a scale-up animation that originates at the button
        // being pressed.
        val opts = ActivityOptions.makeScaleUpAnimation(
                v, 0, 0, v.width, v.height)
        // Request the activity be started, using the custom animation options.
        startActivity(Intent(this@Animation, AlertDialogSamples::class.java), opts.toBundle())
    }

    /**
     * Called when the R.id.zoom_thumbnail_animation ("Thumbnail zoom") is clicked. We create
     * ActivityOptions opts using makeThumbnailScaleUpAnimation. To do this we call the method
     * v.setDrawingCacheEnabled(true) to enable the drawing cache for the Button that was pressed,
     * v.setPressed(false) to make sure the Button is not selected and v.refreshDrawableState()
     * to make sure the Button is redrawn. We set "Bitmap bm" to the bitmap in which the View
     * v's drawing is cached (Canvas c is unused), then we set ActivityOptions opts to an
     * ActivityOptions specifying an animation where a thumbnail ("bm") is scaled from the
     * position (0,0) to the new activity window that is being started by using the method
     * ActivityOptions.makeThumbnailScaleUpAnimation. Then we start the activity
     * AlertDialogSamples using a Bundle created from "opts" to tell how to animate the
     * transition to the new Activity. Finally we call v.setDrawingCacheEnabled(false) to
     * disable the drawing cache.
     *
     * Parameter: The [View] of the Button which was clicked
     */
    private val mZoomThumbnailListener = OnClickListener { v ->
        Log.i(TAG, "Starting thumbnail-zoom animation...")
        // Create a thumbnail animation.  We are going to build our thumbnail
        // just from the view that was pressed.  We make sure the view is
        // not selected, because by the time the animation starts we will
        // have finished with the selection of the tap.
        @Suppress("DEPRECATION")
        v.isDrawingCacheEnabled = true
        v.isPressed = false
        v.refreshDrawableState()
        @Suppress("DEPRECATION")
        val bm = v.drawingCache

        @Suppress("UNUSED_VARIABLE")
        val c = Canvas(bm)
        //c.drawARGB(255, 255, 0, 0);
        val opts = ActivityOptions.makeThumbnailScaleUpAnimation(
                v, bm, 0, 0)
        // Request the activity be started, using the custom animation options.
        startActivity(Intent(this@Animation, AlertDialogSamples::class.java), opts.toBundle())
        @Suppress("DEPRECATION")
        v.isDrawingCacheEnabled = false
    }

    /**
     * Called when the R.id.no_animation ("NO ANIMATION") Button is clicked. We use
     * overridePendingTransition after the startActivity to set the animation used to 0 in
     * and 0 out. I would argue that this should be outside the if/else statement.
     *
     * Parameter: The [View] of the Button which was clicked
     */
    private val mNoAnimationListener = OnClickListener {
        Log.i(TAG, "Starting no animation transition...")
        // Request the next activity transition (here starting a new one).
        startActivity(Intent(this@Animation, AlertDialogSamples::class.java))
        overridePendingTransition(0, 0)
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation
     * of onCreate, then we set our content view to our layout file R.layout.activity_animation.
     *
     * Now we set up the OnClickListener's for our Button's for all versions of android:
     *
     *  - R.id.fade_animation ("Fade in") is set to mFadeListener which uses the animation
     *  - R.anim.fade (an animation of alpha from 0.0 to 1.0) which it specifies using
     * overridePendingTransition after the startActivity for the Activity we are
     * transitioning to.
     *  - R.id.zoom_animation ("Zoom in") is set to mZoomListener which uses the animation
     *  - R.anim.zoom_enter for the new Activity (uses a scale animation to scale from
     * 2.0 to 1.0 around a centered pivot point), and R.anim.zoom_exit for the old
     * Activity (uses a scale animation to scale from 1.0 to 0.5 around a centered
     * pivot point, combined in a set with an alpha animation from 1.0 to 0.0) which
     * it specifies using overridePendingTransition after the startActivity for the
     * Activity we are transitioning to.
     *
     * The next 5 Button's are surrounded by an if/else statement. For devices with a version
     * android.os.Build.VERSION.SDK_INT less than android.os.Build.VERSION_CODES.JELLY_BEAN
     * 4 of them are disabled (the R.id.no_animation ("NO ANIMATION") Button should really
     * be outside the if/else). For JELLY_BEAN and above:
     *
     *  - R.id.modern_fade_animation ("Modern fade in") is set to mModernFadeListener which uses
     * an options bundle passed to startActivity containing the same animations used by
     *  - R.id.fade_animation above.
     *  - R.id.modern_zoom_animation ("Modern Zoom in") is set to  mModernZoomListener which uses
     * an options bundle passed to startActivity containing the same animations used by
     *  - R.id.zoom_animation above.
     *  - R.id.scale_up_animation ("SCALE UP") is set to mScaleUpListener which uses an options
     * bundle passed to startActivity that it creates using makeScaleUpAnimation
     *  - R.id.zoom_thumbnail_animation ("Thumbnail zoom") is set to mZoomThumbnailListener which
     * creates a zoom from the Button pressed to the new Activity using an options bundle
     * that it creates using makeThumbnailScaleUpAnimation
     *  - R.id.no_animation ("NO ANIMATION") uses overridePendingTransition after the startActivity
     * to set the animation used to 0 in and 0 out. Should be outside the if/else statement.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_animation)

        // Watch for button clicks.
        var button = findViewById<Button>(R.id.fade_animation)
        button.setOnClickListener(mFadeListener)
        button = findViewById(R.id.zoom_animation)
        button.setOnClickListener(mZoomListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            button = findViewById(R.id.modern_fade_animation)
            button.setOnClickListener(mModernFadeListener)
            button = findViewById(R.id.modern_zoom_animation)
            button.setOnClickListener(mModernZoomListener)
            button = findViewById(R.id.scale_up_animation)
            button.setOnClickListener(mScaleUpListener)
            button = findViewById(R.id.zoom_thumbnail_animation)
            button.setOnClickListener(mZoomThumbnailListener)
            button = findViewById(R.id.no_animation)
            button.setOnClickListener(mNoAnimationListener)
        } else {
            findViewById<View>(R.id.modern_fade_animation).isEnabled = false
            findViewById<View>(R.id.modern_zoom_animation).isEnabled = false
            findViewById<View>(R.id.scale_up_animation).isEnabled = false
            findViewById<View>(R.id.zoom_thumbnail_animation).isEnabled = false
        }
    }

    /**
     * Activities cannot draw during the period that their windows are animating in. In order
     * to know when it is safe to begin drawing they can override this method which will be
     * called when the entering animation has completed.
     */
    override fun onEnterAnimationComplete() {
        Log.i(TAG, "onEnterAnimationComplete")
    }

    companion object {
        private const val TAG = "Animation"
    }
}
