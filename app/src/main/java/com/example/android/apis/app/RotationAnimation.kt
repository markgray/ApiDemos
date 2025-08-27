/*
 * Copyright (C) 2013 The Android Open Source Project
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
// TODO: Replace deprecated use of LayoutParams.FLAG_FULLSCREEN with WindowInsetsController.hide

package com.example.android.apis.app

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.CheckBox
import android.widget.RadioGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Allows you to choose the animation that occurs when the screen is rotated:
 * either ROTATION_ANIMATION_ROTATE, ROTATION_ANIMATION_CROSSFADE,
 * ROTATION_ANIMATION_JUMPCUT, or ROTATION_ANIMATION_SEAMLESS.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
class RotationAnimation : AppCompatActivity() {

    /**
     * Current rotationAnimation to use when setting WindowManager.LayoutParams.rotationAnimation
     */
    private var mRotationAnimation = LayoutParams.ROTATION_ANIMATION_ROTATE

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.rotation_animation. We then
     * call our method `setRotationAnimation` to set the window attributes value of
     * WindowManager.LayoutParams.rotationAnimation to our field `mRotationAnimation`. Next
     * we locate the CheckBox R.id.windowFullscreen ("FULLSCREEN") and set its OnCheckChangedListener
     * to an anonymous class which calls our method setFullscreen when the CheckBox changes state.
     * Finally we locate the RadioGroup R.id.rotation_radio_group and set its OnCheckedChangeListener
     * to an anonymous class which chooses the value to set mRotationAnimation to based on the radio
     * button which has been checked:
     *  - R.id.rotate LayoutParams.ROTATION_ANIMATION_ROTATE, specifies that this
     * window will visually rotate in or out following a rotation.
     *  - R.id.crossfade LayoutParams.ROTATION_ANIMATION_CROSSFADE, specifies that
     * this window will fade in or out following a rotation
     *  - R.id.jumpcut LayoutParams.ROTATION_ANIMATION_JUMPCUT, specifies that this
     * window will immediately disappear or appear following a rotation.
     *  - R.id.seamless LayoutParams.ROTATION_ANIMATION_SEAMLESS, specifies seamless
     * rotation mode, works like JUMPCUT but will fall back to CROSSFADE if
     * rotation can't be applied without pausing the screen.
     *
     * and then calls our method setRotationAnimation to change the window attributes using our
     * our field `mRotationAnimation` for the WindowManager.LayoutParams.rotationAnimation.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rotation_animation)

        setRotationAnimation(mRotationAnimation)

        (findViewById<View>(R.id.windowFullscreen) as CheckBox)
            .setOnCheckedChangeListener { buttonView, isChecked ->
                /**
                 * Called when the checked state of our R.id.windowFullscreen compound button
                 * changed. We simply call our method setFullscreen with the isChecked parameter
                 * passed us.
                 *
                 * @param buttonView The compound button view whose state has changed.
                 * @param isChecked The new checked state of buttonView.
                 */
                setFullscreen(isChecked)
            }

        (findViewById<View>(R.id.rotation_radio_group) as RadioGroup)
            .setOnCheckedChangeListener { group, checkedId ->
                /**
                 * Called when the checked radio button has changed. When the selection is
                 * cleared, checkedId is -1. We switch on checkedId and set mRotationAnimation
                 * to the appropriate value:
                 *  - R.id.rotate LayoutParams.ROTATION_ANIMATION_ROTATE, specifies that this
                 * window will visually rotate in or out following a rotation.
                 *  - R.id.crossfade LayoutParams.ROTATION_ANIMATION_CROSSFADE, specifies that
                 * this window will fade in or out following a rotation
                 *  - R.id.jumpcut LayoutParams.ROTATION_ANIMATION_JUMPCUT, specifies that this
                 * window will immediately disappear or appear following a rotation.
                 *  - R.id.seamless LayoutParams.ROTATION_ANIMATION_SEAMLESS, specifies seamless
                 * rotation mode, works like JUMPCUT but will fall back to CROSSFADE if
                 * rotation can't be applied without pausing the screen.
                 *
                 * and then we call our method setRotationAnimation to change the window
                 * attributes using this value for WindowManager.LayoutParams.rotationAnimation
                 *
                 * @param group the group in which the checked radio button has changed
                 * @param checkedId the unique identifier of the newly checked radio button
                 */
                mRotationAnimation = when (checkedId) {
                    R.id.rotate -> LayoutParams.ROTATION_ANIMATION_ROTATE
                    R.id.crossfade -> LayoutParams.ROTATION_ANIMATION_CROSSFADE
                    R.id.jumpcut -> LayoutParams.ROTATION_ANIMATION_JUMPCUT
                    R.id.seamless -> LayoutParams.ROTATION_ANIMATION_SEAMLESS
                    else -> LayoutParams.ROTATION_ANIMATION_ROTATE
                }
                setRotationAnimation(mRotationAnimation)
            }
    }

    /**
     * Called when the FULLSCREEN CheckBox changes state. We set or clear the Window Attributes
     * WindowManager.LayoutParams.FLAG_FULLSCREEN bit according to the CheckBox state.
     *
     * @param on if true set the FLAG_FULLSCREEN bit or the window attributes, if false clear it.
     */
    private fun setFullscreen(on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or LayoutParams.FLAG_FULLSCREEN
        } else {
            winParams.flags = winParams.flags and LayoutParams.FLAG_FULLSCREEN.inv()
        }
        win.attributes = winParams
    }

    /**
     * Set the window attributes value of WindowManager.LayoutParams.rotationAnimation to our
     * parameter. We initialize `Window win` to the current [android.view.Window]
     * for the activity, initialize `WindowManager.LayoutParams winParams` to the current window
     * attributes associated with `win`, set the `rotationAnimation` field of
     * `winParams` to our field `rotationAnimation`, then call the `setAttributes`
     * method of `win` to set its window attributes to `winParams`.
     *
     * @param rotationAnimation WindowManager.LayoutParams.rotationAnimation to use
     */
    private fun setRotationAnimation(rotationAnimation: Int) {
        val win = window
        val winParams = win.attributes
        winParams.rotationAnimation = rotationAnimation
        win.attributes = winParams
    }
}
