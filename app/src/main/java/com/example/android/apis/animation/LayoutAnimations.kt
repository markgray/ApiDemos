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

package com.example.android.apis.animation

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.animation.*
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import com.example.android.apis.R
import kotlin.math.min

/**
 * This application demonstrates how to use LayoutTransition to automate transition animations
 * as items are removed from or added to a container.
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class LayoutAnimations : Activity() {
    /**
     * Counter of the number of buttons that have been added, used as the button label.
     */
    private var numButtons = 1
    /**
     * `FixedGridLayout` into which we place our buttons.
     */
    internal var container: ViewGroup? = null
    /**
     * Default `Animator` returned for `LayoutTransition.APPEARING` by the `getAnimator`
     * method of `LayoutTransition`, it is the animation that runs on those items that are appearing
     * in the container
     */
    internal lateinit var defaultAppearingAnim: Animator
    /**
     * Default `Animator` returned for `LayoutTransition.DISAPPEARING` by the `getAnimator`
     * method of `LayoutTransition`, it is the animation that runs on those items that are disappearing
     * from the container
     */
    internal lateinit var defaultDisappearingAnim: Animator
    /**
     * Default `Animator` returned for `LayoutTransition.CHANGE_APPEARING` by the `getAnimator`
     * method of `LayoutTransition`, it is the animation that runs on those items that are changing
     * due to a new item appearing in the container.
     */
    internal lateinit var defaultChangingAppearingAnim: Animator
    /**
     * Default `Animator` returned for `LayoutTransition.CHANGE_DISAPPEARING` by the `getAnimator`
     * method of `LayoutTransition`, it is the animation that runs on those items that are changing
     * due to a new item disappearing from the container.
     */
    internal lateinit var defaultChangingDisappearingAnim: Animator
    /**
     * Our custom `Animator` for the `LayoutTransition.APPEARING` animation, it is the
     * animation that runs on those items that are appearing in the container.
     */
    internal lateinit var customAppearingAnim: Animator
    /**
     * Our custom `Animator` for the `LayoutTransition.DISAPPEARING` animation, it is the
     * animation that runs on those items that are disappearing from the container
     */
    internal lateinit var customDisappearingAnim: Animator
    /**
     * Our custom `Animator` for the `LayoutTransition.CHANGE_APPEARING` animation, it is the
     * animation that runs on those items that are changing due to a new item appearing in the container.
     */
    internal lateinit var customChangingAppearingAnim: Animator
    /**
     * Our custom `Animator` for the `LayoutTransition.CHANGE_DISAPPEARING` animation, it is the
     * animation that runs on those items that are changing due to a new item disappearing from
     * the container.
     */
    internal lateinit var customChangingDisappearingAnim: Animator
    /**
     * Just a copy of `defaultAppearingAnim` made for no apparent reason.
     */
    internal lateinit var currentAppearingAnim: Animator
    /**
     * Just a copy of `defaultDisappearingAnim` made for no apparent reason.
     */
    internal lateinit var currentDisappearingAnim: Animator
    /**
     * Just a copy of `defaultChangingAppearingAnim` made for no apparent reason.
     */
    internal lateinit var currentChangingAppearingAnim: Animator
    /**
     * Just a copy of `defaultChangingDisappearingAnim` made for no apparent reason.
     */
    internal lateinit var currentChangingDisappearingAnim: Animator
    /**
     * Logical screen density of our display.
     */
    @Suppress("PropertyName")
    internal var SCREEN_DENSITY: Float = 0.toFloat()

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * the we set the content view to our layout file R.layout.layout_animations. We then initialize
     * our field SCREEN_DENSITY with the logical screen density of our display. We initialize our
     * field `ViewGroup container` with a new instance of `FixedGridLayout`, and
     * configure its cell height, and cell width scaled for SCREEN_DENSITY. We create LayoutTransition
     * transitioner with default animations, set it as the LayoutTransition for container, and squirrels
     * away the default animations for later use. It then calls the method createCustomAnimations to
     * create custom animations using its argument transitioner only to fetch the default value of the
     * duration of the animations (see createCustomAnimations). setupTransition() is used to
     * switch LayoutTransition transitioner between the default and custom animations for the four
     * different animations used in a layout transition based on the state of the CheckBox's and is
     * only called when the state of one of the 5 CheckBox's changes (the CheckBox for choosing
     * custom instead of default animations, as well as the 4 CheckBox's selecting "In", "Out",
     * "Changing-In" and "Changing-Out" animations or disabling them if un-checked."
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_animations)
        SCREEN_DENSITY = resources.displayMetrics.density

        container = FixedGridLayout(this)
        container!!.clipChildren = false
        (container as FixedGridLayout).setCellHeight((90 * SCREEN_DENSITY).toInt())
        (container as FixedGridLayout).setCellWidth((100 * SCREEN_DENSITY).toInt())
        val transitioner = LayoutTransition()
        container!!.layoutTransition = transitioner
        defaultAppearingAnim = transitioner.getAnimator(LayoutTransition.APPEARING)
        defaultDisappearingAnim = transitioner.getAnimator(LayoutTransition.DISAPPEARING)
        defaultChangingAppearingAnim = transitioner.getAnimator(LayoutTransition.CHANGE_APPEARING)
        defaultChangingDisappearingAnim = transitioner.getAnimator(LayoutTransition.CHANGE_DISAPPEARING)
        createCustomAnimations(transitioner)
        currentAppearingAnim = defaultAppearingAnim
        currentDisappearingAnim = defaultDisappearingAnim
        currentChangingAppearingAnim = defaultChangingAppearingAnim
        currentChangingDisappearingAnim = defaultChangingDisappearingAnim

        val parent = findViewById<ViewGroup>(R.id.parent)
        parent.addView(container)
        parent.clipChildren = false
        val addButton = findViewById<Button>(R.id.addNewButton)
        addButton.setOnClickListener {
            val newButton = Button(this@LayoutAnimations)
            newButton.text = numButtons++.toString()
            newButton.setOnClickListener { v ->
                container!!.removeView(v)
            }
            container!!.addView(newButton, min(1, container!!.childCount))
        }

        val customAnimCB = findViewById<CheckBox>(R.id.customAnimCB)
        customAnimCB.setOnCheckedChangeListener { buttonView, isChecked ->
            /**
             * Called when the checked state of the "Custom Animations" CheckBox has changed,
             * it just calls setupTransition to change the LayoutTransition transitioner as
             * needed.
             *
             * @param buttonView The compound button view whose state has changed.
             * @param isChecked  The new checked state of buttonView.
             */
            Log.i(TAG, "Custom Checkbox")
            setupTransition(transitioner)
        }

        // Check for disabled animations
        val appearingCB = findViewById<CheckBox>(R.id.appearingCB)
        appearingCB.setOnCheckedChangeListener { buttonView, isChecked ->
            /**
             * Called when the checked state of the "In" (appearing) CheckBox has changed,
             * it just calls setupTransition to change the LayoutTransition transitioner as
             * needed.
             *
             * @param buttonView The compound button view whose state has changed.
             * @param isChecked  The new checked state of buttonView.
             */
            Log.i(TAG, "Appearing Checkbox")
            setupTransition(transitioner)
        }
        val disappearingCB = findViewById<CheckBox>(R.id.disappearingCB)
        disappearingCB.setOnCheckedChangeListener { buttonView, isChecked ->
            /**
             * Called when the checked state of the "Out" (disappearing) CheckBox has changed,
             * it just calls setupTransition to change the LayoutTransition transitioner as
             * needed.
             *
             * @param buttonView The compound button view whose state has changed.
             * @param isChecked  The new checked state of buttonView.
             */
            Log.i(TAG, "Disappearing Checkbox")
            setupTransition(transitioner)
        }
        val changingAppearingCB = findViewById<CheckBox>(R.id.changingAppearingCB)
        changingAppearingCB.setOnCheckedChangeListener { buttonView, isChecked ->
            /**
             * Called when the checked state of the "Changing In" (CHANGING_APPEARING) CheckBox
             * has changed, it just calls setupTransition to change the LayoutTransition
             * transitioner as needed.
             *
             * @param buttonView The compound button view whose state has changed.
             * @param isChecked  The new checked state of buttonView.
             */
            Log.i(TAG, "Changing Appearing Checkbox")
            setupTransition(transitioner)
        }
        val changingDisappearingCB = findViewById<CheckBox>(R.id.changingDisappearingCB)
        changingDisappearingCB.setOnCheckedChangeListener { buttonView, isChecked ->
            /**
             * Called when the checked state of the "Changing Out" (CHANGING_DISAPPEARING) CheckBox
             * has changed, it just calls setupTransition to change the LayoutTransition
             * transitioner as needed.
             *
             * @param buttonView The compound button view whose state has changed.
             * @param isChecked  The new checked state of buttonView.
             */
            Log.i(TAG, "Changing Disappearing Checkbox")
            setupTransition(transitioner)
        }
    }

    /**
     * This method sets the Animations used for the four different animation types used in a
     * layout transition (APPEARING, DISAPPEARING, CHANGE_APPEARING, and CHANGE_DISAPPEARING)
     * to be either the default animations, or the custom animations (created by the method
     * createCustomAnimations) based on the state of the checkboxes at the moment. It is called
     * only when one of the five checkboxes changes state. It first finds each of the checkboxes,
     * then sets each of the four animation types by choosing animations (default or custom)
     * using a double ternary operator which decides the animation to be used based on the
     * isChecked() state of the relevant CheckBox's for that animation.
     *
     * @param transition LayoutTransition to be modified
     */
    private fun setupTransition(transition: LayoutTransition) {
        val customAnimCB = findViewById<CheckBox>(R.id.customAnimCB)
        val appearingCB = findViewById<CheckBox>(R.id.appearingCB)
        val disappearingCB = findViewById<CheckBox>(R.id.disappearingCB)
        val changingAppearingCB = findViewById<CheckBox>(R.id.changingAppearingCB)
        val changingDisappearingCB = findViewById<CheckBox>(R.id.changingDisappearingCB)
        transition.setAnimator(LayoutTransition.APPEARING, if (appearingCB.isChecked)
            if (customAnimCB.isChecked) customAppearingAnim else defaultAppearingAnim
        else
            null)
        transition.setAnimator(LayoutTransition.DISAPPEARING, if (disappearingCB.isChecked)
            if (customAnimCB.isChecked) customDisappearingAnim else defaultDisappearingAnim
        else
            null)
        transition.setAnimator(LayoutTransition.CHANGE_APPEARING, if (changingAppearingCB.isChecked)
            if (customAnimCB.isChecked)
                customChangingAppearingAnim
            else
                defaultChangingAppearingAnim
        else
            null)
        transition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING,
                if (changingDisappearingCB.isChecked)
                    if (customAnimCB.isChecked)
                        customChangingDisappearingAnim
                    else
                        defaultChangingDisappearingAnim
                else
                    null)
    }

    /**
     * This method creates the four different custom animations which can be selected to be used
     * by when the appropriate CheckBox's are "checked"
     *
     * For the CHANGE_APPEARING (Changing while Adding) part of the animation it defines property
     * value holders to animate property "left" from 0 to 1, "top" from 0 to 1, "right" from 0 to 1,
     * "bottom" from 0 to 1, "scaleX" from 1f to 0f to 1f, "scaleY" from 1f to 0f to 1f. It then
     * creates an ObjectAnimator customChangingAppearingAnim for these properties, sets its duration
     * to use the same duration of the current LayoutTransition transition and sets
     * customChangingAppearingAnim to be the CHANGE_APPEARING animation of transition. It adds an
     * AnimatorListenerAdapter to customChangingAppearingAnim which overrides onAnimationEnd and
     * scales the Button added to full size. This animation has has the appearance of a card
     * flipping right to left from the back side to the front side. You can see this animation in
     * action by clicking the ADD BUTTON Button when both the "Custom Animations" and "In"
     * CheckBox are checked.
     *
     * For the CHANGE_DISAPPEARING (Changing while Removing) part of the animation it defines an
     * additional PropertyValueHolder for "rotation" constructed of three KeyFrame's (kf0 - a
     * starting value of the rotation of 0f lasting 0f, kf1 - a rotation of 360f degrees lasting
     * .9999f of the frame, and kf2 - an ending rotation of 0f degrees. It combines these in the
     * "rotation" property value holder pvhRotation, then combines the "left", "top", "right", and
     * "bottom" PropertyValuesHolder's used for the CHANGE_APPEARING animation to create the
     * ObjectAnimator customChangingDisappearingAnim, sets the duration of
     * customChangingDisappearingAnim to be the same as the current LayoutTransition transition,
     * and sets customChangingDisappearingAnim to be the CHANGE_DISAPPEARING animation of
     * transition. It adds an AnimatorListenerAdapter to customChangingDisappearingAnim which
     * overrides onAnimationEnd to set the rotation of the Button to 0f degrees. It has the
     * effect of rotating the Button's to the right of the Button removed clockwise while moving
     * them into their new positions when both the "Custom Animations" and "Out" CheckBox are checked.
     *
     * For the APPEARING (Adding) part of the animation it creates a simple "rotationY"
     * ObjectAnimator customAppearingAnim which rotates the Button from 90f degrees to 0f degrees,
     * sets the duration of customAppearingAnim to be the same as the current LayoutTransition
     * transition, and sets customAppearingAnim to be the APPEARING animation of transition. It
     * adds an AnimatorListenerAdapter to customAppearingAnim which overrides onAnimationEnd to
     * set the rotation of the Button to 0f degrees. It has the effect of rotating the appearing
     * Button's about the y axis when the ADD BUTTON button is pressed, starting from sticking
     * directly out of the plane of the View, to flat when both the "Custom Animations" and
     * "Changing In" CheckBox are checked.
     *
     * For the DISAPPEARING (Removing) part of the animation it creates a simple "rotationX"
     * ObjectAnimator customDisappearingAnim which rotates the Button from 0f degrees (flat) to
     * 90f degrees (sticking out of the plane), sets the duration of customDisappearingAnim to
     * be the same as the current LayoutTransition transition, and sets customDisappearingAnim
     * to be the DISAPPEARING animation of transition. It add an AnimatorListenerAdapter to
     * customDisappearingAnim which overrides onAnimationEnd to set the rotation of the Button
     * to 0f degrees. It has the effect of rotating the disappearing Button about the x axis
     * when the Button is clicked starting from flat to sticking vertically out of the plane
     * when both the "Custom Animations" and "Changing Out" CheckBox are checked.
     *
     * @param transition LayoutTransition which is to be modified
     */
    @SuppressLint("ObjectAnimatorBinding")
    private fun createCustomAnimations(transition: LayoutTransition) {
        // Changing while Adding
        val pvhLeft = PropertyValuesHolder.ofInt("left", 0, 1)
        val pvhTop = PropertyValuesHolder.ofInt("top", 0, 1)
        val pvhRight = PropertyValuesHolder.ofInt("right", 0, 1)
        val pvhBottom = PropertyValuesHolder.ofInt("bottom", 0, 1)
        val pvhScaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 0f, 1f)
        val pvhScaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 0f, 1f)
        customChangingAppearingAnim = ObjectAnimator.ofPropertyValuesHolder(
                this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhScaleX, pvhScaleY)
                .setDuration(transition.getDuration(LayoutTransition.CHANGE_APPEARING) * 100)
        customChangingAppearingAnim.addListener(object : AnimatorListenerAdapter() {
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
        val pvhRotation = PropertyValuesHolder.ofKeyframe("rotation", kf0, kf1, kf2)
        customChangingDisappearingAnim = ObjectAnimator.ofPropertyValuesHolder(
                this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhRotation)
                .setDuration(transition.getDuration(LayoutTransition.CHANGE_DISAPPEARING) * 100)
        customChangingDisappearingAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(anim: Animator) {
                val view = (anim as ObjectAnimator).target as View?
                view!!.rotation = 0f
            }
        })

        // Adding
        customAppearingAnim = ObjectAnimator.ofFloat(
                null, "rotationY", 90f, 0f)
                .setDuration(transition.getDuration(LayoutTransition.APPEARING) * 100)
        customAppearingAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(anim: Animator) {
                val view = (anim as ObjectAnimator).target as View?
                view!!.rotationY = 0f
            }
        })

        // Removing
        customDisappearingAnim = ObjectAnimator.ofFloat(
                null, "rotationX", 0f, 90f)
                .setDuration(transition.getDuration(LayoutTransition.DISAPPEARING) * 100)
        customDisappearingAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(anim: Animator) {
                val view = (anim as ObjectAnimator).target as View?
                view!!.rotationX = 0f
            }
        })

    }

    // These are added to silence error warning.
    @Suppress("unused", "UNUSED_PARAMETER")
    fun setLeft(left: Int) {
        throw RuntimeException("This should not be called")
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    fun setTop(top: Int) {
        throw RuntimeException("This should not be called")
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    fun setRight(right: Int) {
        throw RuntimeException("This should not be called")
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    fun setBottom(bottom: Int) {
        throw RuntimeException("This should not be called")
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    fun setScaleX(scaleX: Float) {
        throw RuntimeException("This should not be called")
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    fun setScaleY(scaleY: Float) {
        throw RuntimeException("This should not be called")
    }

    companion object {

        /**
         * TAG used for logging.
         */
        private const val TAG = "LayoutAnimations"
    }
}