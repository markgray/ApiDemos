/*
 * Copyright (C) 2010 The Android Open Source Project
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

@file:Suppress("unused", "ReplaceNotNullAssertionWithElvisReturn")

package com.example.android.apis.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.Keyframe
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This application demonstrates how to use LayoutTransition to automate transition animations
 * as items are hidden or shown in a container. Pressing the "Show Buttons" button while the
 * "Custom Animations" CheckBox is checked causes a crash which blanks out the system wallpaper.
 * (Sometimes? This may be fixed as of Android Q -- at least it doesn't seem to happen anymore).
 * RequiresApi(Build.VERSION_CODES.HONEYCOMB)
 */
class LayoutAnimationsHideShow : AppCompatActivity() {
    /**
     * `LinearLayout` into which we place the buttons we are hiding or showing.
     */
    internal var container: ViewGroup? = null

    /**
     * `LayoutTransition` used by our `ViewGroup container`, either a default one or the
     * custom one created when we check the "Custom Animations" CheckBox.
     */
    private var mTransitioner: LayoutTransition? = null

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our Layout file layout_animations_hideshow. We initialize our
     * variable `CheckBox hideGoneCB` by finding the view with id R.id.hideGoneCB ("Hide (GONE)").
     * We initialize our field `ViewGroup container` with a new instance and set its layout params
     * to have both width and height set to MATCH_PARENT. We then loop over `int i` from 1 to 4
     * creating a new instance for `Button newButton`, setting its text to the string value of
     * `i`, adding it to `container` and setting its `OnClickListener` to an anonymous
     * class whose `onClick` override sets the visibility of the button to GONE if `hideGoneCB`
     * is checked or to INVISIBLE if it is not checked. When done adding the 4 buttons to `container`
     * we call our method `resetTransition` which creates a new instance of `LayoutTransition`
     * (the default LayoutTransition) for `LayoutTransition mTransitioner` and sets it to be the
     * LayoutTransition used by `container`. We then initialize `ViewGroup parent` by finding
     * the view with id R.id.parent and add `container` to it. We initialize `Button addButton`
     * by finding the view with id R.id.addNewButton ("Show Buttons") and set its `OnClickListener`
     * to an anonymous class whose `onClick` override loops through all the children in the view group
     * `container` setting their visibility to VISIBLE. We initialize `CheckBox customAnimCB`
     * by finding the view with id R.id.customAnimCB ("Custom Animations") and set its OnCheckedChangeListener
     * to either create a  custom LayoutTransition mTransitioner if the CheckBox is checked, or reset it
     * to the default animation if unchecked.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_animations_hideshow)

        val hideGoneCB = findViewById<CheckBox>(R.id.hideGoneCB)

        container = LinearLayout(this)
        container!!.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Add a slew of buttons to the container. We won't add any more buttons at runtime, but
        // will just show/hide the buttons we've already created
        for (i in 0..3) {
            val newButton = Button(this)
            newButton.text = i.toString()
            container!!.addView(newButton)
            newButton.setOnClickListener { v ->
                /**
                 * If the hideGoneCB is checked, the visibility of the Button clicked is
                 * set to GONE, if not it is set to INVISIBLE.
                 *
                 * @param v View which was clicked
                 */
                v.visibility = if (hideGoneCB.isChecked) View.GONE else View.INVISIBLE
            }
        }

        resetTransition()

        val parent = findViewById<ViewGroup>(R.id.parent)
        parent.addView(container)

        val addButton = findViewById<Button>(R.id.addNewButton)
        addButton.setOnClickListener {
            for (i in 0 until container!!.childCount) {
                val view = container!!.getChildAt(i)
                view.visibility = View.VISIBLE
            }
        }

        val customAnimCB = findViewById<CheckBox>(R.id.customAnimCB)
        customAnimCB.setOnCheckedChangeListener { buttonView, isChecked ->
            /**
             * This callback either configures the LayoutTransition mTransitioner to perform
             * a custom layout change animation, or resets it to a new LayoutTransition by
             * calling resetTransition. The custom animation is created by first setting the
             * stagger (delay between animations) of the CHANGE_APPEARING and CHANGE_DISAPPEARING
             * layout transitions to 30 milliseconds. It then calls setupCustomAnimations() which
             * puts together a rather complex combination of animations which it then sets in the
             * LayoutTransition mTransitioner (see setupCustomAnimations) The duration of the
             * custom animations if 500 milliseconds, while the default animations is set to
             * 300 milliseconds.
             *
             * @param `buttonView` CheckBox customAnimCB whose state has changed
             * @param `isChecked` whether the CheckBox has changed to "custom" (true) or been cleared
             */
            val duration: Long = if (isChecked) {
                mTransitioner!!.setStagger(LayoutTransition.CHANGE_APPEARING, 30)
                mTransitioner!!.setStagger(LayoutTransition.CHANGE_DISAPPEARING, 30)
                setupCustomAnimations()
                500
            } else {
                resetTransition()
                300
            }
            mTransitioner!!.setDuration(duration)
        }
    }

    /**
     * Create a new default LayoutTransition and set the LinearLayout container to use
     * this LayoutTransition
     */
    private fun resetTransition() {
        mTransitioner = LayoutTransition()
        container!!.layoutTransition = mTransitioner
    }

    /**
     * This method creates complex CHANGE_APPEARING, CHANGE_DISAPPEARING, APPEARING, and
     * DISAPPEARING animations and configures the LayoutTransition mTransitioner to use them.
     *
     * For the CHANGE_APPEARING (Changing while Adding) part of the animation it defines property
     * value holders to animate property "left" from 0 to 1, "top" from 0 to 1, "right" from 0 to 1,
     * "bottom" from 0 to 1, "scaleX" from 1f to 0f to 1f, "scaleY" from 1f to 0f to 1f. It then
     * creates an ObjectAnimator changeIn for these properties, sets its duration to use the
     * same duration of the current LayoutTransition mTransitioner and sets changeIn to be the
     * CHANGE_APPEARING animation of mTransitioner. It adds an AnimatorListenerAdapter to changeIn
     * which overrides onAnimationEnd and scales the Button added to full size. This animation has
     * has the appearance of a card flipping right to left from the back side to the front side.
     * You can see this animation in action by clicking the SHOW BUTTONS Button after deleting
     * Button's with the "Hide (GONE)" CheckBox checked.
     *
     * For the CHANGE_DISAPPEARING (Changing while Removing) part of the animation it defines an
     * additional PropertyValueHolder for "rotation" constructed of three KeyFrame's (kf0 - a
     * starting value of the rotation of 0f lasting 0f, kf1 - a rotation of 360f degrees lasting
     * .9999f of the frame, and kf2 - an ending rotation of 0f degrees. It combines these in the
     * "rotation" property value holder pvhRotation, then combines the "left", "top", "right", and
     * "bottom" PropertyValuesHolder's used for the CHANGE_APPEARING animation to create the
     * ObjectAnimator changeOut, sets the duration of changeOut to be the same as the current
     * LayoutTransition mTransitioner, and sets changeOut to be the CHANGE_DISAPPEARING animation
     * of mTransitioner. It adds an AnimatorListenerAdapter to changeOut which overrides
     * onAnimationEnd to set the rotation of the Button to 0f degrees. It has the effect of rotating
     * the Button's to the right of the Button removed clockwise when the "Hide (GONE)" CheckBox
     * is checked while moving them into their new positions.
     *
     * For the APPEARING (Adding) part of the animation it creates a simple "rotationY"
     * ObjectAnimator animIn which rotates the Button from 90f degrees to 0f degrees, sets
     * the duration of animIn to be the same as the current LayoutTransition mTransitioner, and
     * sets animIn to be the APPEARING animation of mTransitioner. It add an AnimatorListenerAdapter
     * to animIn which overrides onAnimationEnd to set the rotation of the Button to 0f degrees.
     * It has the effect of rotating the appearing Button's about the y axis when the SHOW BUTTONS
     * button is pressed (after removing a Button or two), starting from sticking directly out of
     * the plane of the View, to flat.
     *
     * For the DISAPPEARING (Removing) part of the animation it creates a simple "rotationX"
     * ObjectAnimator animOut which rotates the Button from 0f degrees (flat) to 90f degrees
     * (sticking out of the plane), sets the duration of animOut to be the same as the current
     * LayoutTransition mTransitioner, and sets animOut to be the DISAPPEARING animation of
     * mTransitioner. It add an AnimatorListenerAdapter to animOut which overrides onAnimationEnd
     * to set the rotation of the Button to 0f degrees. It has the effect of rotating the
     * disappearing Button about the x axis when the Button is clicked starting from flat to
     * sticking vertically out of the plane.
     */
    @SuppressLint("ObjectAnimatorBinding")
    private fun setupCustomAnimations() {
        // Changing while Adding
        val pvhLeft = PropertyValuesHolder.ofInt("left", 0, 1)
        val pvhTop = PropertyValuesHolder.ofInt("top", 0, 1)
        val pvhRight = PropertyValuesHolder.ofInt("right", 0, 1)
        val pvhBottom = PropertyValuesHolder.ofInt("bottom", 0, 1)
        val pvhScaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 0f, 1f)
        val pvhScaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 0f, 1f)
        val changeIn = ObjectAnimator.ofPropertyValuesHolder(
            this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhScaleX, pvhScaleY
        )
            .setDuration(mTransitioner!!.getDuration(LayoutTransition.CHANGE_APPEARING))
        mTransitioner!!.setAnimator(LayoutTransition.CHANGE_APPEARING, changeIn)
        changeIn.addListener(object : AnimatorListenerAdapter() {
            /**
             * Notifies the end of the animation. We initialize `View view` with the target of
             * our parameter `Animator anim` then set both the x and y scaling factor of
             * `view` to 1f.
             *
             * @param anim The animation which reached its end.
             */
            override fun onAnimationEnd(anim: Animator) {
                val view = (anim as ObjectAnimator).target as View?
                view!!.scaleX = 1f
                view.scaleY = 1f
            }
        })

        // Changing while Removing
        val kf0 = Keyframe.ofFloat(0f, 0f)
        val kf1 = Keyframe.ofFloat(.9999f, 360f)
        val kf2 = Keyframe.ofFloat(1f, 0f)
        val pvhRotation = PropertyValuesHolder
            .ofKeyframe("rotation", kf0, kf1, kf2)
        val changeOut = ObjectAnimator.ofPropertyValuesHolder(
            this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhRotation
        ).setDuration(mTransitioner!!.getDuration(LayoutTransition.CHANGE_DISAPPEARING))
        mTransitioner!!.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, changeOut)
        changeOut.addListener(object : AnimatorListenerAdapter() {
            /**
             * Notifies the end of the animation. We initialize `View view` with the target of
             * our parameter `Animator anim` and set its rotation to 0.
             *
             * @param anim The animation which reached its end.
             */
            override fun onAnimationEnd(anim: Animator) {
                val view = (anim as ObjectAnimator).target as View?
                view!!.rotation = 0f
            }
        })

        // Adding
        val animIn = ObjectAnimator.ofFloat(
            null, "rotationY", 90f, 0f
        )
            .setDuration(mTransitioner!!.getDuration(LayoutTransition.APPEARING))
        mTransitioner!!.setAnimator(LayoutTransition.APPEARING, animIn)
        animIn.addListener(object : AnimatorListenerAdapter() {
            /**
             * Notifies the end of the animation. We initialize `View view` with the target of
             * our parameter `Animator anim` and set its rotation around the vertical axis to 0.
             *
             * @param anim The animation which reached its end.
             */
            override fun onAnimationEnd(anim: Animator) {
                val view = (anim as ObjectAnimator).target as View?
                view!!.rotationY = 0f
            }
        })

        // Removing
        val animOut = ObjectAnimator.ofFloat(
            null, "rotationX", 0f, 90f
        )
            .setDuration(mTransitioner!!.getDuration(LayoutTransition.DISAPPEARING))
        mTransitioner!!.setAnimator(LayoutTransition.DISAPPEARING, animOut)
        animOut.addListener(object : AnimatorListenerAdapter() {
            /**
             * Notifies the end of the animation. We initialize `View view` with the target of
             * our parameter `Animator anim` and set its rotation around the horizontal axis to 0.
             *
             * @param anim The animation which reached its end.
             */
            override fun onAnimationEnd(anim: Animator) {
                val view = (anim as ObjectAnimator).target as View?
                view!!.rotationX = 0f
            }
        })
    }

    // The following are here to silence an error warning.
    @Suppress("unused", "UNUSED_PARAMETER")
    fun setLeft(duh: Int) {
        throw RuntimeException("I should not be called")
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    fun setTop(duh: Int) {
        throw RuntimeException("I should not be called")
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    fun setRight(duh: Int) {
        throw RuntimeException("I should not be called")
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    fun setBottom(duh: Int) {
        throw RuntimeException("I should not be called")
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    fun setScaleX(duh: Float) {
        throw RuntimeException("I should not be called")
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    fun setScaleY(duh: Float) {
        throw RuntimeException("I should not be called")
    }

}