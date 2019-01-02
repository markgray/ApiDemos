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
package com.example.android.apis.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.apis.R;

/**
 * Demonstrates the display of overlay windows, i.e. windows that are drawn on top of other apps.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class OverlayWindowActivity extends Activity {

    /**
     * Request code we use when we have settings app ask the user's permission to use overlays.
     */
    private static int MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1;
    /**
     * {@code TextView} which we used as our overlay view, saved so we can {@code removeView} it.
     */
    private View mOverlayView;

    /**
     * Called when the activity is starting. First we call our super's implementation of {@code onCreate},
     * then we set our content view to our layout file R.layout.overlay_window. We initialize
     * {@code Button button} by finding the view with id R.id.show_overlay ("Show Overlay") and set
     * its {@code OnClickListener} to {@code mShowOverlayListener}, then set {@code button} by finding
     * the view with id R.id.hide_overlay ("Hide Overlay") and set its {@code OnClickListener} to
     * {@code mHideOverlayListener}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.overlay_window);

        Button button = findViewById(R.id.show_overlay);
        button.setOnClickListener(mShowOverlayListener);
        button = findViewById(R.id.hide_overlay);
        button.setOnClickListener(mHideOverlayListener);
    }

    /**
     * {@code OnClickListener} for the button with id R.id.show_overlay ("Show Overlay"), its
     * {@code onClick} override calls our {@code drawOverlay} method to show the overlay if we
     * have permission to draw overlays, otherwise it launches the settings app to ask the user
     * for those permissions.
     */
    private OnClickListener mShowOverlayListener = new OnClickListener() {
        /**
         * Called when our view has been clicked. If the {@code canDrawOverlays} method of {@code Settings}
         * returns true (the user has granted our activity permission to draw on top of other apps), we
         * call our method {@code drawOverlay} to draw our overlay window. Otherwise we need to ask the
         * user, so we initialize {@code Intent intent} with a new instance with the settings action
         * ACTION_MANAGE_OVERLAY_PERMISSION (Show screen for controlling which apps can draw on top of
         * other app) with an Uri parsed from the string "package:" and our app's package name (it
         * resolves as: "package:com.example.android.apis"). We then start the activity of {@code intent}
         * for a result using MANAGE_OVERLAY_PERMISSION_REQUEST_CODE as the request code.
         *
         * @param view The view that was clicked.
         */
        @Override
        public void onClick(View view) {
            if (Settings.canDrawOverlays(OverlayWindowActivity.this)) {
                drawOverlay();
            } else {
                // Need to ask the user's permission first. We'll redirect them to Settings.
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            }
        }
    };

    /**
     * {@code OnClickListener} for the button with id R.id.hide_overlay ("Hide Overlay"), in its
     * {@code onClick} override if {@code mOverlayView} is not null it initializes {@code WindowManager wm}
     * by retrieving the window manager for showing custom windows, then calls its {@code removeView}
     * method to remove {@code View mOverlayView} and sets {@code mOverlayView} to null.
     */
    private OnClickListener mHideOverlayListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mOverlayView != null) {
                WindowManager wm = getWindowManager();
                wm.removeView(mOverlayView);
                mOverlayView = null;
            }
        }
    };

    /**
     * This is called after the user chooses whether they grant permission to the app to display
     * overlays or not. If {@code requestCode} is MANAGE_OVERLAY_PERMISSION_REQUEST_CODE we check
     * whether the user granted permission to draw overlays by calling the {@code canDrawOverlays}
     * method of {@code Settings} and if so we call our method {@code drawOverlay} to draw our
     * overlay window.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult(). UNUSED
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras"). UNUSED
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            // Check if the user granted permission to draw overlays.
            if (Settings.canDrawOverlays(this)) {
                drawOverlay();
            }
        }
    }

    /**
     * Called to draw an overlay window. If our field {@code View mOverlayView} there is already an
     * overlay being shown so we return having done nothing. Otherwise we initialize {@code TextView textView}
     * with a new instance, set its text to the string "I'm an overlay", set its background color to WHITE,
     * set its text color to BLACK, and set its padding to 10 pixels on every side. We initialize
     * {@code WindowManager wm} by retrieving the window manager for showing custom windows, and initialize
     * {@code LayoutParams params} with a new instance. If the SDK version of the software currently running
     * on this hardware device is greater than or equal to "O" (v26) we set the {@code type} field of
     * {@code params} to TYPE_APPLICATION_OVERLAY (Window type: Application overlay windows are displayed
     * above all activity windows (types between {@code FIRST_APPLICATION_WINDOW} and {@code LAST_APPLICATION_WINDOW})
     * but below critical system windows like the status bar or IME), otherwise we set it to TYPE_PHONE
     * (Window type: phone.  These are non-application windows providing user interaction with the phone
     * (in particular incoming calls). These windows are normally placed above all applications, but behind
     * the status bar). We then set the {@code flags} field of {@code params} to bitwise or of FLAG_NOT_FOCUSABLE
     * (this window won't ever get key input focus, so the user can not send key or other button events to it),
     * FLAG_NOT_TOUCH_MODAL (even when this window is focusable allow any pointer events outside of the window
     * to be sent to the windows behind it), and FLAG_WATCH_OUTSIDE_TOUCH (if you have set FLAG_NOT_TOUCH_MODAL,
     * you can set this flag to receive a single special MotionEvent with the action ACTION_OUTSIDE for
     * touches that occur outside of your window). We set the {@code format} field of {@code params} to
     * TRANSPARENT (system chooses a format that supports transparency (at least 1 alpha bit)). Set both its
     * {@code width} and {@code height} fields to WRAP_CONTENT. We set the {@code gravity} field of {@code params}
     * to the bitwise or of TOP and RIGHT (snap to the upper right corner of the screen), and set its
     * {@code x} and {@code y} fields both to 10 (position relative to upper right corner). We then use the
     * {@code addView} method of {@code wm} to add {@code textView} to our window using {@code params} as
     * the LayoutParams to assign to the view. Finally we save {@code textView} in our field {@code mOverlayView}.
     */
    @SuppressLint({"SetTextI18n", "RtlHardcoded"})
    private void drawOverlay() {
        if (mOverlayView != null) {
            // Already shown.
            return;
        }

        TextView textView = new TextView(this);
        textView.setText("I'm an overlay");
        textView.setBackgroundColor(Color.WHITE);
        textView.setTextColor(Color.BLACK);
        textView.setPadding(10, 10, 10, 10);

        WindowManager wm = getWindowManager();
        LayoutParams params = new LayoutParams();

        params.type = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ? LayoutParams.TYPE_APPLICATION_OVERLAY
                : LayoutParams.TYPE_PHONE;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        params.format = PixelFormat.TRANSPARENT;

        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.WRAP_CONTENT;
        // Snap to the upper right corner of the screen.
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        // Set position relative to upper right corner.
        params.x = 10;
        params.y = 10;

        wm.addView(textView, params);
        mOverlayView = textView;
    }
}
