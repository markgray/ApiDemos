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
 * Used by the [AnimateDrawables] demo which:
 *
 * Shows how to use the [Animation] api (in this case `TranslateAnimation`) in order to move a jpg
 * around a [Canvas]. Uses AnimateDrawable which extends ProxyDrawable (A neat way to package the
 * methods required when extending Drawable, overriding only draw in AnimateDrawable)
 */
@Suppress("MemberVisibilityCanBePrivate")
class AnimateDrawable : ProxyDrawable {
    /**
     * [Animation] we are to use to move our [Drawable] around the [Canvas] passed to our
     * [draw] method.
     */
    var mAnimation: Animation? = null
    /**
     * [Transformation] which we modify for each animation step then use to modify the matrix of the
     * [Canvas] passed to our [draw] method.
     */
    private val mTransformation = Transformation()

    /**
     * Unused constructor which simply passes its [Drawable] parameter [target] to our super's
     * constructor.
     *
     * @param target [Drawable] we are to proxy for
     */
    @Suppress("unused")
    constructor(target: Drawable?) : super(target)

    /**
     * We pass our [Drawable] parameter [target] to our super's constructor, then save our
     * [Animation] parameter [animation] in our [Animation] field [mAnimation].
     *
     * @param target    `Drawable` we are to proxy for
     * @param animation `Animation` we are to apply to our proxy `Drawable`
     */
    constructor(target: Drawable?, animation: Animation?) : super(target) {
        this.mAnimation = animation
    }

    /**
     * Indicates whether the [Animation] in field [mAnimation] has started or not. If [mAnimation]
     * is not *null*, we pass the call through to it. Unused.
     *
     * @return *true* if the animation has started, false otherwise
     */
    @Suppress("unused")
    fun hasStarted(): Boolean {
        return mAnimation != null && mAnimation!!.hasStarted()
    }

    /**
     * Indicates whether the [Animation] in field [mAnimation] has  ended or not. If [mAnimation]
     * is not *null*, we pass the call through to it. Unused.
     *
     * @return *true* if the animation has ended, false otherwise
     */
    @Suppress("unused")
    fun hasEnded(): Boolean {
        return mAnimation == null || mAnimation!!.hasEnded()
    }

    /**
     * Called when we are required to draw our drawable into our [Canvas] parameter [canvas].
     * First we call our super to retrieve the [Drawable] `val dr` that we (and it) were constructed
     * with and if `dr` is not *null* we:
     *  * Save the current matrix and clip onto a private stack, saving the value to pass to
     *  `restoreToCount()` to balance this `save()` in [Int] variable `val sc`.
     *  * Copy a reference to our [Animation] field  [mAnimation] to [Animation] `val anim` (Why?)
     *  * If `anim` is not null, we get the transformation to apply for the current animation
     *  time in milliseconds to our [Transformation] field [mTransformation], and pre-concatenate
     *  the 3x3 Matrix representing the transformation to apply to the coordinates of the object
     *  being animated to the current matrix of [Canvas] parameter [canvas].
     *  * We instruct our [Drawable] variable `dr` to draw itself
     *  * Finally we restore the state of the [Canvas] parameter [canvas] to the one we saved in `sc`.
     *
     * @param canvas The [Canvas] to draw into
     */
    override fun draw(canvas: Canvas) {
        val dr = mProxy
        if (dr != null) {
            val sc = canvas.save()
            val anim = mAnimation
            if (anim != null) {
                anim.getTransformation(
                        AnimationUtils.currentAnimationTimeMillis(),
                        mTransformation
                )
                canvas.concat(mTransformation.matrix)
            }
            dr.draw(canvas)
            canvas.restoreToCount(sc)
        }
    }
}