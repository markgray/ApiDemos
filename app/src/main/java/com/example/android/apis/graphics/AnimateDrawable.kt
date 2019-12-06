/*
 * Copyright (C) 2008 The Android Open Source Project
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
package com.example.android.apis.graphics

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Transformation

/**
 * Used by the `AnimateDrawables` demo which:
 * Shows how to use the Animation api (in this case TranslateAnimation) in order to move a jpg
 * around a Canvas. Uses AnimateDrawable which extends ProxyDrawable (A neat way to package the
 * methods required when extending Drawable, overriding only draw in AnimateDrawable)
 */
class AnimateDrawable : ProxyDrawable {
    /**
     * A getter method for our field `Animation mAnimation`.
     *
     * @return the `Animation` instance reference we have saved in our field `Animation mAnimation`.
     */
    /**
     * A setter method for our field `Animation mAnimation`.
     *
     * Parameter: anim `Animation` instance reference to save in our field `Animation mAnimation`.
     */
    /**
     * Animation we are to use to move our `Drawable` around the `Canvas` passed to our
     * `draw` method.
     */
    var animation: Animation? = null
    /**
     * Transformation which we modify for each animation step then use to modify the matrix of the
     * `Canvas` passed to our `draw` method.
     */
    private val mTransformation = Transformation()

    /**
     * Unused constructor which simply passes its parameter `Drawable target` to our super's
     * constructor.
     *
     * @param target `Drawable` we are to proxy for
     */
    @Suppress("unused")
    constructor(target: Drawable?) : super(target)

    /**
     * We pass our parameter `Drawable target` to our super's constructor, then save our
     * parameter `Animation animation` in our field `Animation mAnimation`.
     *
     * @param target    `Drawable` we are to proxy for
     * @param animation `Animation` we are to apply to our proxy `Drawable`
     */
    constructor(target: Drawable?, animation: Animation?) : super(target) {
        this.animation = animation
    }

    /**
     * Indicates whether the `Animation mAnimation` has started or not. If `mAnimation`
     * is not null, we pass the call through to it. Unused.
     *
     * @return true if the animation has started, false otherwise
     */
    @Suppress("unused")
    fun hasStarted(): Boolean {
        return animation != null && animation!!.hasStarted()
    }

    /**
     * Indicates whether the `Animation mAnimation` has ended or not. If `mAnimation`
     * is not null, we pass the call through to it. Unused.
     *
     * @return true if the animation has ended, false otherwise
     */
    @Suppress("unused")
    fun hasEnded(): Boolean {
        return animation == null || animation!!.hasEnded()
    }

    /**
     * Called when we are required to draw our drawable into our parameter `Canvas canvas`.
     * First we call our super to retrieve the `Drawable dr` that we (and it) were constructed
     * with and if `dr` is not null we:
     *
     *  *
     * Save the current matrix and clip onto a private stack, saving the value to pass to
     * `restoreToCount()` to balance this `save()` in `int sc`.
     *
     *  *
     * Copy a reference to our field `Animation mAnimation` to `Animation anim` (Why?)
     *
     *  *
     * If `anim` is not null, we get the transformation to apply for the current animation
     * time in milliseconds to our field `Transformation mTransformation`, and pre-concatenate
     * the 3x3 Matrix representing the transformation to apply to the coordinates of the object
     * being animated to the current matrix of `Canvas canvas`.
     *
     *  *
     * We instruct our `Drawable dr` to draw itself
     *
     *  *
     * Finally we restore the state of the `Canvas canvas` to the one we saved in `sc`.
     *
     *
     *
     * @param canvas The canvas to draw into
     */
    override fun draw(canvas: Canvas) {
        val dr = proxy
        if (dr != null) {
            val sc = canvas.save()
            val anim = animation
            if (anim != null) {
                anim.getTransformation(
                        AnimationUtils.currentAnimationTimeMillis(),
                        mTransformation)
                canvas.concat(mTransformation.matrix)
            }
            dr.draw(canvas)
            canvas.restoreToCount(sc)
        }
    }
}