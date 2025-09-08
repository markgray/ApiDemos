/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.android.apis.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.PointerIcon
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.createBitmap

/**
 * Constructs a [PointerIcon] using kotlin code in its [onResolvePointerIcon] override (needs mouse)
 */
@RequiresApi(api = Build.VERSION_CODES.N)
class LivePointerIconButton : AppCompatButton {
    @JvmOverloads
    constructor(
        context: Context?,
        attrs: AttributeSet? = null
    ) : super(context!!, attrs)

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context!!, attrs, defStyleAttr)

    override fun onResolvePointerIcon(event: MotionEvent, pointerIndex: Int): PointerIcon {
        val cursorSize: Int = height
        val bitmap: Bitmap = createBitmap(width = cursorSize, height = cursorSize)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.setARGB(/* a = */ 255, /* r = */ 255, /* g = */ 255, /* b = */ 255)
        paint.style = Paint.Style.STROKE
        val strokeWidth = 4
        paint.strokeWidth = strokeWidth.toFloat()

        // Draw a large circle filling the bitmap.
        val outerCenterX: Int = cursorSize / 2
        val outerCenterY: Int = cursorSize / 2
        val outerRadius: Int = cursorSize / 2 - strokeWidth
        canvas.drawCircle(
            /* cx = */ outerCenterX.toFloat(),
            /* cy = */ outerCenterY.toFloat(),
            /* radius = */ outerRadius.toFloat(),
            /* paint = */ paint
        )

        // Compute relative offset of the mouse pointer from the view center.
        // It should be between -0.5 and 0.5.
        val relativeX: Float = event.getX(pointerIndex) / width - 0.5f
        val relativeY: Float = event.getY(pointerIndex) / height - 0.5f

        // Draw a smaller circle inside the large circle, offset towards the center of the view.
        val innerCenterX: Int = (cursorSize * (1 - relativeX) / 2).toInt()
        val innerCenterY: Int = (cursorSize * (1 - relativeY) / 2).toInt()
        val innerRadius: Int = cursorSize / 6
        if (event.action == MotionEvent.ACTION_MOVE) {
            // Fill the inner circle if the mouse button is down.
            paint.style = Paint.Style.FILL
        }
        canvas.drawCircle(
            /* cx = */ innerCenterX.toFloat(),
            /* cy = */ innerCenterY.toFloat(),
            /* radius = */ innerRadius.toFloat(),
            /* paint = */ paint
        )
        val hotSpotX: Int = bitmap.width / 2
        val hotSpotY: Int = bitmap.height / 2
        return PointerIcon.create(
            /* bitmap = */ bitmap,
            /* hotSpotX = */ hotSpotX.toFloat(),
            /* hotSpotY = */ hotSpotY.toFloat()
        )
    }
}