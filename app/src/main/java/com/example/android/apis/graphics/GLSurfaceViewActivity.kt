/*
 * Copyright (C) 2007 The Android Open Source Project
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
 * Wrapper activity demonstrating the use of [GLSurfaceView], a view that uses OpenGL drawing
 * into a dedicated surface. Uses [CubeRenderer] to render a pair of tumbling cubes created and
 * drawn by Cube.java
 */
class GLSurfaceViewActivity : AppCompatActivity() {
    /**
     * [GLSurfaceView] we do our drawing to.
     */
    private var mGLSurfaceView: GLSurfaceView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. We initialize our [GLSurfaceView] field [mGLSurfaceView] with a new instance of
     * [GLSurfaceView], set its renderer to a new instance of [CubeRenderer] specifying that it not
     * use a translucent background, and set our content view to [mGLSurfaceView].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**
         * Create our Preview view and set it as the content of our Activity
         */
        mGLSurfaceView = GLSurfaceView(this)
        mGLSurfaceView!!.setRenderer(CubeRenderer(mTranslucentBackground = false))
        setContentView(mGLSurfaceView)
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for our activity to start
     * interacting with the user. First we call through to our super's implementation of `onResume`,
     * then we pass the call on to the `onResume` method of our [GLSurfaceView] field [mGLSurfaceView].
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
     * method of our [GLSurfaceView] field [mGLSurfaceView].
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