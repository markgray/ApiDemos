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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent

/**
 * Extends [ViewGroup] to mirror any single [View] added to it in the four corners of its
 * [Canvas]. Actually a disabled test of the [Picture] class.
 */
class PictureLayout : ViewGroup {
    /**
     *
     */
    private val mPicture = Picture()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun addView(child: View) {
        check(childCount <= 1) { "PictureLayout can host only one direct child" }
        super.addView(child)
    }

    override fun addView(child: View, index: Int) {
        check(childCount <= 1) { "PictureLayout can host only one direct child" }
        super.addView(child, index)
    }

    override fun addView(child: View, params: LayoutParams) {
        check(childCount <= 1) { "PictureLayout can host only one direct child" }
        super.addView(child, params)
    }

    override fun addView(child: View, index: Int, params: LayoutParams) {
        check(childCount <= 1) { "PictureLayout can host only one direct child" }
        super.addView(child, index, params)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val count = childCount
        var maxHeight = 0
        var maxWidth = 0
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
            }
        }
        maxWidth += paddingLeft + paddingRight
        maxHeight += paddingTop + paddingBottom
        val drawable = background
        if (drawable != null) {
            maxHeight = maxHeight.coerceAtLeast(drawable.minimumHeight)
            maxWidth = maxWidth.coerceAtLeast(drawable.minimumWidth)
        }
        setMeasuredDimension(View.resolveSize(maxWidth, widthMeasureSpec),
                View.resolveSize(maxHeight, heightMeasureSpec))
    }

    private fun drawPict(canvas: Canvas, x: Int, y: Int, w: Int, h: Int,
                         sx: Float, sy: Float) {
        canvas.save()
        canvas.translate(x.toFloat(), y.toFloat())
        canvas.clipRect(0, 0, w, h)
        canvas.scale(0.5f, 0.5f)
        canvas.scale(sx, sy, w.toFloat(), h.toFloat())
        canvas.drawPicture(mPicture)
        canvas.restore()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(mPicture.beginRecording(width, height))
        mPicture.endRecording()
        val x = width / 2
        val y = height / 2
        @Suppress("ConstantConditionIf")
        if (false) {
            canvas.drawPicture(mPicture)
        } else {
            drawPict(canvas, 0, 0, x, y, 1f, 1f)
            drawPict(canvas, x, 0, x, y, -1f, 1f)
            drawPict(canvas, 0, y, x, y, 1f, -1f)
            drawPict(canvas, x, y, x, y, -1f, -1f)
        }
    }

    override fun invalidateChildInParent(location: IntArray, dirty: Rect): ViewParent {
        location[0] = left
        location[1] = top
        dirty[0, 0, width] = height
        return parent
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = super.getChildCount()
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                val childLeft = paddingLeft
                val childTop = paddingTop
                child.layout(childLeft, childTop,
                        childLeft + child.measuredWidth,
                        childTop + child.measuredHeight)
            }
        }
    }
}