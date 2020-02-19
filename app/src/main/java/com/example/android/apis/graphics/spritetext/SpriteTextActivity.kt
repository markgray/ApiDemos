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
package com.example.android.apis.graphics.spritetext

import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import javax.microedition.khronos.opengles.GL

/**
 * Shows how to add text to a [GLSurfaceView] using OpenGL labels. OpenGL labels are implemented by
 * creating a [Bitmap], drawing all the labels into the [Bitmap], converting the [Bitmap] into an
 * Alpha texture, and drawing portions of the texture using `glDrawTexiOES`. The benefits of this
 * approach are that the labels are drawn using the high quality anti-aliased font rasterizer, full
 * character set support, and all the text labels are stored on a single texture, which makes it
 * faster to use. The drawbacks are that you can only have as many labels as will fit onto one
 * texture, and you have to recreate the whole texture if any label text changes. Characters are
 * too small on lollipop, okay on froyo.
 */
class SpriteTextActivity : AppCompatActivity() {
    /**
     * Our [GLSurfaceView], created in our [onCreate] method.
     */
    private var mGLSurfaceView: GLSurfaceView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we initialize our [GLSurfaceView] field [mGLSurfaceView] with a new instance
     * of [GLSurfaceView]. Next we set the [GLSurfaceView.GLWrapper] of [mGLSurfaceView] to an lambda
     * which returns a new instance of [MatrixTrackingGL] "wrapping" the [GL] it is passed when its
     * `wrap` method is called ([MatrixTrackingGL] implements the various [GL] variants adding code
     * to track changes to the [GL] matrices, and to retrieve their contents). Then we set the
     * renderer of [mGLSurfaceView] to a new instance of [SpriteTextRenderer], and set our content
     * view to [mGLSurfaceView].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGLSurfaceView = GLSurfaceView(this)
        mGLSurfaceView!!.setGLWrapper { gl: GL ->
            /**
             * Wraps a gl interface in another [GL] interface, in our case a new instance of
             * [MatrixTrackingGL].
             *
             * @param gl a GL interface that is to be wrapped.
             * @return a new instance of [MatrixTrackingGL] that wraps the input argument.
             */
            MatrixTrackingGL(gl)
        }
        mGLSurfaceView!!.setRenderer(SpriteTextRenderer(this))
        setContentView(mGLSurfaceView)
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. The counterpart to [onResume]. First we call through to our
     * super's implementation of `onPause`, then we call the [GLSurfaceView.onPause] method of our
     * [GLSurfaceView] field [mGLSurfaceView].
     */
    override fun onPause() {
        super.onPause()
        mGLSurfaceView!!.onPause()
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for our activity to start
     * interacting with the user. First we call through to our super's implementation of `onResume`,
     * then we call the [GLSurfaceView.onResume] method of our [GLSurfaceView] field [mGLSurfaceView].
     */
    override fun onResume() {
        super.onResume()
        mGLSurfaceView!!.onResume()
    }
}