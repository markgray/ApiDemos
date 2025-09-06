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
package com.example.android.apis.view

import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import android.widget.FrameLayout
import androidx.core.graphics.drawable.toDrawable

/**
 * While it is used in layout/simple_list_item_checkable_1.xml that file is not used by any demo.
 */
class CheckableFrameLayout : FrameLayout, Checkable {
    private var mChecked = false

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    override fun setChecked(checked: Boolean) {
        mChecked = checked
        background = if (checked) (-0xffff60).toDrawable() else null
    }

    override fun isChecked(): Boolean {
        return mChecked
    }

    override fun toggle() {
        isChecked = !mChecked
    }
}