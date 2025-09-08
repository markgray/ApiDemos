/*
 * Copyright (C) 2017 The Android Open Source Project
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

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.android.apis.R

/**
 * Demonstrates the display of overlay windows, i.e. windows that are drawn on top of other apps.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
class OverlayWindowActivity : AppCompatActivity() {
    /**
     * [TextView] which we use as our overlay view, saved so we can `removeView` it.
     */
    private var mOverlayView: View? = null

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file R.layout.overlay_window. We initialize
     * [Button] variable `var button` by finding the view with id R.id.show_overlay ("Show Overlay")
     * and set its `OnClickListener` to [mShowOverlayListener], then set `button` by finding
     * the view with id R.id.hide_overlay ("Hide Overlay") and set its `OnClickListener` to
     * [mHideOverlayListener].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.overlay_window)
        var button = findViewById<Button>(R.id.show_overlay)
        button.setOnClickListener(mShowOverlayListener)
        button = findViewById(R.id.hide_overlay)
        button.setOnClickListener(mHideOverlayListener)
    }

    /**
     * `OnClickListener` for the button with id R.id.show_overlay ("Show Overlay"), its
     * `onClick` override calls our `drawOverlay` method to show the overlay if we
     * have permission to draw overlays, otherwise it launches the settings app to ask the user
     * for those permissions.
     *
     * If the `canDrawOverlays` method of [Settings] returns true (the user has granted our activity
     * permission to draw on top of other apps), we call our method [drawOverlay] to draw our overlay
     * window. Otherwise we need to ask the user, so we initialize [Intent] variable `val intent`
     * with a new instance with the settings action ACTION_MANAGE_OVERLAY_PERMISSION (Show screen
     * for controlling which apps can draw on top of other app) with an Uri parsed from the string
     * "package:" and our app's package name (it resolves as: "package:com.example.android.apis").
     * We then launch the activity of `intent` using our [ActivityResultLauncher] field
     * [requestOverlayPermissionLauncher].
     */
    private val mShowOverlayListener: View.OnClickListener = View.OnClickListener {
        if (Settings.canDrawOverlays(this@OverlayWindowActivity)) {
            drawOverlay()
        } else {
            // Need to ask the user's permission first. We'll redirect them to Settings.
            val intent = Intent(
                /* action = */ Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                /* uri = */ "package:$packageName".toUri()
            )
            requestOverlayPermissionLauncher.launch(intent)
        }
    }

    /**
     * This is the [ActivityResultLauncher] that we use to have the settings activity ask the user
     * to grant our activity permission to draw on top of other apps.
     */
    private val requestOverlayPermissionLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            /* contract = */ ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                if (Settings.canDrawOverlays(/* context = */ this)) {
                    drawOverlay()
                }
            }
        }

    /**
     * `OnClickListener` for the button with id R.id.hide_overlay ("Hide Overlay"), in its
     * `onClick` override if [mOverlayView] is not null it initializes [WindowManager] variable
     * `val wm` by retrieving the window manager for showing custom windows, then calls its
     * `removeView` method to remove [View] field [mOverlayView] and sets [mOverlayView] to null.
     */
    private val mHideOverlayListener: View.OnClickListener = View.OnClickListener {
        if (mOverlayView != null) {
            val wm = windowManager
            wm.removeView(mOverlayView)
            mOverlayView = null
        }
    }

    /**
     * Called to draw an overlay window. If our [View] field [mOverlayView] is not null there is
     * already an overlay being shown so we return having done nothing. Otherwise we initialize
     * [TextView] variable `val textView` with a new instance, set its text to the string:
     * "I'm an overlay", set its background color to WHITE, set its text color to BLACK, and set
     * its padding to 10 pixels on every side. We initialize [WindowManager] variable `val wm` by
     * retrieving the window manager for showing custom windows, and initialize `LayoutParams`
     * variable `val params` with a new instance. If the SDK version of the software currently
     * running on this hardware device is greater than or equal to "O" (v26) we set the `type`
     * field of `params` to TYPE_APPLICATION_OVERLAY (Window type: Application overlay windows are
     * displayed above all activity windows (types between `FIRST_APPLICATION_WINDOW` and
     * `LAST_APPLICATION_WINDOW`) but below critical system windows like the status bar or IME),
     * otherwise we set it to TYPE_PHONE (Window type: phone.  These are non-application windows
     * providing user interaction with the phone (in particular incoming calls). These windows are
     * normally placed above all applications, but behind the status bar). We then set the `flags`
     * field of `params` to the bitwise or of FLAG_NOT_FOCUSABLE (this window won't ever get key
     * input focus, so the user can not send key or other button events to it), FLAG_NOT_TOUCH_MODAL
     * (even when this window is focusable allow any pointer events outside of the window to be sent
     * to the windows behind it), and FLAG_WATCH_OUTSIDE_TOUCH (if you have set FLAG_NOT_TOUCH_MODAL,
     * you can set this flag to receive a single special MotionEvent with the action ACTION_OUTSIDE
     * for touches that occur outside of your window). We set the `format` field of `params` to
     * TRANSPARENT (system chooses a format that supports transparency (at least 1 alpha bit)). Set
     * both its `width` and `height` fields to WRAP_CONTENT. We set the `gravity` field of `params`
     * to the bitwise or of TOP and RIGHT (snap to the upper right corner of the screen), and set
     * its `x` and `y` fields both to 10 (position relative to upper right corner). We then use the
     * `addView` method of `wm` to add `textView` to our window using `params` as the LayoutParams
     * to assign to the view. Finally we save `textView` in our field [mOverlayView].
     */
    @SuppressLint("SetTextI18n", "RtlHardcoded")
    private fun drawOverlay() {
        if (mOverlayView != null) {
            // Already shown.
            return
        }
        val textView = TextView(this)
        textView.text = "I'm an overlay"
        textView.setBackgroundColor(Color.WHITE)
        textView.setTextColor(Color.BLACK)
        textView.setPadding(10, 10, 10, 10)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40.0F)
        val wm: WindowManager = windowManager
        val params = WindowManager.LayoutParams()
        @SuppressLint("ObsoleteSdkInt")
        params.type = if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION") // Needed for SDK older than "O"
            WindowManager.LayoutParams.TYPE_PHONE
        }
        params.flags = (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
        params.format = PixelFormat.TRANSPARENT
        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        // Snap to the upper right corner of the screen.
        params.gravity = Gravity.TOP or Gravity.RIGHT
        // Set position relative to upper right corner.
        params.x = 10
        params.y = 10
        wm.addView(textView, params)
        mOverlayView = textView
    }
}