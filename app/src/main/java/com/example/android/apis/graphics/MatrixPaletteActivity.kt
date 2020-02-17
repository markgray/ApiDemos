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

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * This sample shows how to implement a Matrix Palette, used to rock a column back and forth.
 */
class MatrixPaletteActivity : AppCompatActivity() {
    /**
     * `GLSurfaceView` we create for `MatrixPaletteRenderer` to render to.
     */
    private var mGLSurfaceView: GLSurfaceView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we create a new `GLSurfaceView` and use it to initialize our field
     * `GLSurfaceView mGLSurfaceView`, set its renderer to a new instance of `MatrixPaletteRenderer`
     * and finally set our content view to `GLSurfaceView mGLSurfaceView`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGLSurfaceView = GLSurfaceView(this)
        mGLSurfaceView!!.setRenderer(MatrixPaletteRenderer(this))
        setContentView(mGLSurfaceView)
    }

    /**
     * Called after [.onRestoreInstanceState], [.onRestart], or [.onPause], for
     * your activity to start interacting with the user. First we call through to our super's
     * implementation of `onResume`, then we call the `onResume` method of our field
     * `GLSurfaceView mGLSurfaceView`.
     */
    override fun onResume() { // Ideally a game should implement onResume() and onPause()
// to take appropriate action when the activity looses focus
        super.onResume()
        mGLSurfaceView!!.onResume()
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. The counterpart to [.onResume]. First we call through to our
     * super's  implementation of `onPause`, then we call the `onPause` method of our
     * field `GLSurfaceView mGLSurfaceView`.
     */
    override fun onPause() { // Ideally a game should implement onResume() and onPause()
// to take appropriate action when the activity looses focus
        super.onPause()
        mGLSurfaceView!!.onPause()
    }
}