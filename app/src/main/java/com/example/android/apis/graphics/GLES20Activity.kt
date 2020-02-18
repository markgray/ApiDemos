/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ConfigurationInfo
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * This sample shows how to check for OpenGL ES 2.0 support at runtime, and then use either
 * OpenGL ES 1.0 or OpenGL ES 2.0, as appropriate. Since even froyo uses 2.0 this is not all
 * that important it seems. Does show a simple example of using Vertex shaders, and Fragment
 * shaders (Pixel shaders).
 */
class GLES20Activity : AppCompatActivity() {
    /**
     * [GLSurfaceView] we use for drawing. Its renderer is either [GLES20TriangleRenderer]
     * for OpenGL ES 2.0, or [TriangleRenderer] for OpenGL ES 1.0.
     */
    private var mGLSurfaceView: GLSurfaceView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we initialize our [GLSurfaceView] field [mGLSurfaceView] with a new
     * instance of [GLSurfaceView]. We call our method [detectOpenGLES20] to determine
     * whether our device supports OpenGL ES 2.0 or above, and if it does we inform the default
     * EGLContextFactory and default EGLConfigChooser to pick EGLContext client version 2, and then
     * we set the renderer of [GLSurfaceView] field [mGLSurfaceView] to a new instance of
     * [GLES20TriangleRenderer].
     *
     * If our device does not support OpenGL ES 2.0 or above we set the renderer of
     * [GLSurfaceView] field [mGLSurfaceView] to a new instance of [TriangleRenderer].
     *
     * In either case we then set our content view to [mGLSurfaceView].
     *
     * @param savedInstanceState We do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGLSurfaceView = GLSurfaceView(this)
        if (detectOpenGLES20()) {
            /**
             * Tell the surface view we want to create an OpenGL ES 2.0-compatible
             * context, and set an OpenGL ES 2.0-compatible renderer.
             */
            mGLSurfaceView!!.setEGLContextClientVersion(2)
            mGLSurfaceView!!.setRenderer(GLES20TriangleRenderer(this))
        } else {
            /**
             * Set an OpenGL ES 1.x-compatible renderer. In a real application
             * this renderer might approximate the same output as the 2.0 renderer.
             */
            mGLSurfaceView!!.setRenderer(TriangleRenderer(this))
        }
        setContentView(mGLSurfaceView)
    }

    /**
     * Checks to see if the device supports OpenGL ES 2.0 or above. First we fetch a handle to the
     * ACTIVITY_SERVICE system level service to initialize [ActivityManager] variable `val am` (the
     * [ActivityManager] allows you to interact with the overall activities running in the system).
     * We use `am` to get the device configuration attributes into [ConfigurationInfo] variable
     * `val info`. We then return *true* if the field `info.reqGlEsVersion` is greater than or equal
     * to 0x20000 (OpenGL ES 2.0 or above is used by the application).
     *
     * @return *true* if the device supports OpenGL ES 2.0 or above, *false* otherwise
     */
    private fun detectOpenGLES20(): Boolean {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info: ConfigurationInfo = am.deviceConfigurationInfo
        return info.reqGlEsVersion >= 0x20000
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for our activity to start
     * interacting with the user. First we call through to our super's implementation of `onResume`,
     * then we pass the call on to the `onResume` method of [GLSurfaceView] field [mGLSurfaceView].
     */
    override fun onResume() {
        /**
         * Ideally a game should implement onResume() and onPause()
         * to take appropriate action when the activity looses focus
         */
        super.onResume()
        mGLSurfaceView!!.onResume()
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. The counterpart to [onResume]. First we call through to our
     * super's implementation of `onPause`, then we pass the call on to the `onPause`
     * method of [GLSurfaceView] field [mGLSurfaceView].
     */
    override fun onPause() {
        /**
         * Ideally a game should implement onResume() and onPause()
         * to take appropriate action when the activity looses focus
         */
        super.onPause()
        mGLSurfaceView!!.onPause()
    }
}