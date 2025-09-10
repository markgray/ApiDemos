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

package com.example.android.apis.app

import android.os.Bundle
import android.view.View.OnClickListener
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Uses `setTheme(theme)` followed by `recreate()` to cycle through three different Holo themes
 * (original version did not initialize the field that the current theme is kept in, causing
 * it to crash on Material Light default devices.)
 */
@Suppress("MemberVisibilityCanBePrivate")
class ActivityRecreate : AppCompatActivity() {
    /**
     * Theme in use for this instance of the Activity
     */
    internal var mCurTheme: Int = 0

    /**
     * [OnClickListener] used for the "Recreate" [Button] (R.id.recreate) in our layout.
     * Called when the view has been clicked. We simply call Activity.recreate() to cause this
     * Activity to be recreated with a new instance. This results in essentially the same flow
     * as when the Activity is created due to a configuration change -- the current instance
     * will go through its lifecycle to onDestroy() and a new instance then created after it.
     *
     * Parameter: v View of the Button that was clicked
     */
    private val mRecreateListener = OnClickListener {
        recreate()
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Then if [savedInstanceState] is not *null* we retrieve the [mCurTheme] from that
     * [Bundle] (stored under the key "theme"), and switch on its current value changing the
     * [mCurTheme] from Theme_Holo_Light to Theme_Holo_Dialog to Theme_Holo and back to
     * Theme_Holo_Light. If [savedInstanceState] is null (first time run) we set [mCurTheme] to
     * Theme_Holo_Light. Next we set the base theme for this context to [mCurTheme], then set the
     * content view to our layout file R.layout.activity_recreate. Finally we locate the "Recreate"
     * [Button] (R.id.recreate) and set its [OnClickListener] to our [OnClickListener] field
     * [mRecreateListener] so that when the [Button] is clicked the Activity will be recreated.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState].
     * ***Note: Otherwise it is null.***
     */
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            mCurTheme = savedInstanceState.getInt("theme")

            // Switch to a new theme different from last theme.
            mCurTheme = when (mCurTheme) {
                android.R.style.Theme_Holo_Light -> android.R.style.Theme_Holo_Dialog
                android.R.style.Theme_Holo_Dialog -> android.R.style.Theme_Holo
                else -> android.R.style.Theme_Holo_Light
            }

        } else {
            mCurTheme = android.R.style.Theme_Holo_Light
        }

        setTheme(mCurTheme)
        setContentView(R.layout.activity_recreate)

        // Watch for button clicks.
        val button = findViewById<Button>(R.id.recreate)
        button.setOnClickListener(mRecreateListener)
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in [onCreate] or
     * [onRestoreInstanceState] (the [Bundle] populated by this method
     * will be passed to both).
     *
     * First we call through to our super's implementation of `onSaveInstanceState`, then we insert
     * the current value of [mCurTheme] using the key "theme" in the [Bundle] parameter
     * [savedInstanceState] passed us. It will be used in our [onCreate] method when the Activity
     * is recreated.
     *
     * @param savedInstanceState Bundle in which to place your saved state.
     */
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putInt("theme", mCurTheme)
    }
}
