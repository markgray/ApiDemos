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
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.PointerIcon
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import com.example.android.apis.R

/**
 * Constructs a [PointerIcon] from a [BitmapDrawable] in its [onResolvePointerIcon] override
 */
@RequiresApi(api = Build.VERSION_CODES.N)
class StaticPointerIconButton : AppCompatButton {
    private var mCustomIcon: PointerIcon? = null

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
        if (mCustomIcon == null) {
            val d = AppCompatResources.getDrawable(context, R.drawable.smile)
            val bitmapDrawable = d as BitmapDrawable?
            val hotSpotX = d!!.intrinsicWidth / 2
            val hotSpotY = d.intrinsicHeight / 2
            mCustomIcon = PointerIcon.create(
                bitmapDrawable!!.bitmap,
                hotSpotX.toFloat(),
                hotSpotY.toFloat()
            )
        }
        return mCustomIcon!!
    }
}