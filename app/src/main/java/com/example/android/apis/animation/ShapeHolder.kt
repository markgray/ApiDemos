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

import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.drawable.ShapeDrawable

/**
 * A data structure that holds a Shape and various properties that can be used to define
 * how the shape is drawn.
 */
class ShapeHolder
/**
 * Constructor which initializes a ShapeHolder instance's Shape shape with a ShapeDrawable s
 *
 * @param shape ShapeDrawable that the ShapeHolder will contain
 */
(
        /**
         * The `ShapeDrawable` object we are holding.
         */
        var shape: ShapeDrawable?) {
    /**
     * Our x coordinate.
     */
    var x = 0f
    /**
     * Our y coordinate.
     */
    var y = 0f
    /**
     * Color of the `ShapeDrawable` object we are holding.
     */
    var color: Int = 0
        set(value) {
            shape!!.paint.color = value
            field = value
        }
    /**
     * `RadialGradient` of the `ShapeDrawable` object we are holding.
     */
    var gradient: RadialGradient? = null
    /**
     * Alpha of the `ShapeDrawable` object we are holding.
     */
    private var alpha = 1f
    /**
     * `Paint` of the `ShapeDrawable` object we are holding.
     */
    var paint: Paint? = null

    /**
     * The width of the Shape contained in the ShapeHolder
     */
    var width: Float
        get() = shape!!.shape.width
        set(width) {
            val s = shape!!.shape
            s.resize(width, s.height)
        }

    /**
     * The height of the Shape contained in the ShapeHolder
     */
    var height: Float
        get() = shape!!.shape.height
        set(height) {
            val s = shape!!.shape
            s.resize(s.width, height)
        }

    /**
     * Set the alpha value of the ShapeHolder and the Shape it contains
     *
     * @param alpha alpha value to use
     */
    fun setAlpha(alpha: Float) {
        this.alpha = alpha
        shape!!.alpha = (alpha * 255f + .5f).toInt()
    }
}
