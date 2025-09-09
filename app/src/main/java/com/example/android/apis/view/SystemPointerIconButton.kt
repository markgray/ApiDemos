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
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.PointerIcon
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton

/**
 * Loads a [PointerIcon] from one of the system pointer icons in its [onResolvePointerIcon] override
 * (Used by .view.PointerShapes -- Needs Mouse.)
 */
@RequiresApi(api = Build.VERSION_CODES.N)
class SystemPointerIconButton : AppCompatButton {
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
        val minX = width / 4
        val maxX = width - minX
        val minY = height / 4
        val maxY = height - minY
        val x = event.getX(pointerIndex)
        val y = event.getY(pointerIndex)

        @Suppress("JoinDeclarationAndAssignment")
        val type: Int
        type = if (x < minX && y < minY || x > maxX && y > maxY) {
            // Top/left or bottom/right corner.
            PointerIcon.TYPE_TOP_LEFT_DIAGONAL_DOUBLE_ARROW
        } else if (x < minX && y > maxY || x > maxX && y < minY) {
            // Top/right or bottom/left corner.
            PointerIcon.TYPE_TOP_RIGHT_DIAGONAL_DOUBLE_ARROW
        } else if (x < minX || x > maxX) {
            // Left or right edge.
            PointerIcon.TYPE_HORIZONTAL_DOUBLE_ARROW
        } else if (y < minY || y > maxY) {
            // Top or bottom edge edge.
            PointerIcon.TYPE_VERTICAL_DOUBLE_ARROW
        } else {
            // Everything else (the middle).
            PointerIcon.TYPE_ALL_SCROLL
        }
        return PointerIcon.getSystemIcon(context, type)
    }
}