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

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Shows a rotating triangle using a bitmap made from raw/robot.png as the texture. It uses a
 * GLSurfaceView.Renderer that uses the Android-specific android.opengl.GLESXXX static OpenGL ES
 * APIs. The static APIs expose more of the OpenGL ES features than the
 * javax.microedition.khronos.opengles APIs, and also provide a programming model that is closer
 * to the C OpenGL ES APIs, which may make it easier to reuse code and documentation written for
 * the C OpenGL ES APIs. It uses android:theme="@style/ThemeCurrentDialog" in AndroidManifest.xml
 * so the SurfaceView is shrunk to appear in a dialog size window.
 */
class TriangleActivity : AppCompatActivity() {
    /**
     * The [GLSurfaceView] we use for drawing.
     */
    private var mGLView: GLSurfaceView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Next we initialize our [GLSurfaceView] field [mGLView] with a new instance
     * of [GLSurfaceView], and set its renderer to a new instance of [StaticTriangleRenderer].
     * Finally we set our content view to [mGLView]. Note that since our entry in the AndroidManifest
     * uses android:theme="@style/ThemeCurrentDialog" we appear in a dialog sized window above the
     * main activity which spawned us.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGLView = GLSurfaceView(this)
        mGLView!!.setRenderer(StaticTriangleRenderer(this))
        setContentView(mGLView)
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of `onPause`
     * then we pass the call on to the `onPause` method of our [GLSurfaceView] field [mGLView].
     */
    override fun onPause() {
        super.onPause()
        mGLView!!.onPause()
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for our activity to start
     * interacting with the user. First we call through to our super's implementation of `onResume`
     * then we pass the call on to the `onResume` method of our [GLSurfaceView] field [mGLView].
     */
    override fun onResume() {
        super.onResume()
        mGLView!!.onResume()
    }
}