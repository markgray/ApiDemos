/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import com.example.android.apis.graphics.CubeRenderer

/**
 * Secure Window Activity
 *
 * This activity demonstrates how to create a [SurfaceView] backed by a secure surface using
 * [SurfaceView.setSecure]. Because the surface is secure, its contents cannot be captured in
 * screenshots and will not be visible on non-secure displays even when mirrored.
 *
 * Here are a few things you can do to experiment with secure surfaces and
 * observe their behavior:
 *  - Try taking a screenshot. Either the system will prevent you from taking
 * a screenshot altogether or the screenshot should not contain the contents
 * of the secure surface.
 *  - Try mirroring the secure surface onto a non-secure display such as an
 * "Overlay Display" created using the "Simulate secondary displays" option in
 * the "Developer options" section of the Settings application.  The non-secure
 * secondary display should not show the contents of the secure surface.
 *  - Try mirroring the secure surface onto a secure display such as an
 * HDMI display with HDCP enabled.  The contents of the secure surface should appear
 * RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
 */
class SecureSurfaceViewActivity : AppCompatActivity() {
    /**
     * GLSurfaceView in our layout
     */
    private var mSurfaceView: GLSurfaceView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.secure_surface_view_activity.
     * We initialize our [GLSurfaceView] field [mSurfaceView] by locating the [GLSurfaceView] in our
     * layout (R.id.surface_view) and set the renderer associated with this view to a new instance
     * of [CubeRenderer] (two rotating cubes) which also starts the thread that will call the renderer,
     * which in turn causes the rendering to start. Finally we set the surface view to be secure.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not called
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState)

        // See assets/res/any/layout/secure_surface_view_activity.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.secure_surface_view_activity)

        // Set up the surface view.
        // We use a GLSurfaceView in this demonstration but ordinary
        // SurfaceViews also support the same secure surface functionality.
        mSurfaceView = findViewById(R.id.surface_view)
        mSurfaceView!!.setRenderer(CubeRenderer(false))

        // Make the surface view secure.  This must be done at the time the surface view
        // is created before the surface view's containing window is attached to
        // the window manager which happens after onCreate returns.
        // It cannot be changed later.
        mSurfaceView!!.setSecure(true)
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for your activity to start
     * interacting with the user. First we call through to our super's implementation of `onResume`,
     * then we call the `onResume` callback of our [GLSurfaceView] field [mSurfaceView].
     */
    override fun onResume() {
        // Be sure to call the super class.
        super.onResume()

        // Resume rendering.
        mSurfaceView!!.onResume()
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. The counterpart to [onResume]. First we call through to our super's
     * implementation of `onPause`, then we call the `onPause` callback of our [GLSurfaceView] field
     * [mSurfaceView].
     */
    override fun onPause() {
        // Be sure to call the super class.
        super.onPause()

        // Pause rendering.
        mSurfaceView!!.onPause()
    }
}
