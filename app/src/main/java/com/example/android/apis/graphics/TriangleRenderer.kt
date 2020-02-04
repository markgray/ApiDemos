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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.opengl.GLUtils
import android.os.SystemClock
import com.example.android.apis.R
import java.io.IOException
import java.io.InputStream
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Draws a `Triangle` using OpenGL ES 1.x-compatible renderer.
 */
class TriangleRenderer(
        /**
         * `Context` passed to our constructor, we use it to access resources, "this" when called
         * from the `onCreate` method the the activity `GLES20Activity`.
         */
        private val mContext: Context
) : GLSurfaceView.Renderer {
    /**
     * Our [Triangle] instance. We use it only to ask it to `draw` itself.
     */
    private val mTriangle: Triangle = Triangle()
    /**
     * Texture name for the texture we use. It is bound to `GL_TEXTURE_2D`, configured and
     * loaded from the raw resource robot.png in our override of [onSurfaceCreated].
     */
    private var mTextureID = 0

    /**
     * Called when the surface is created or recreated. Called when the rendering thread starts and
     * whenever the EGL context is lost. The EGL context will typically be lost when the Android
     * device awakes after going to sleep.
     *
     * First we disable the server side capability GL_DITHER (color components and indices will not
     * be dithered before they are written to the color buffer), then we specify the implementation
     * specific hint GL_PERSPECTIVE_CORRECTION_HINT to GL_FASTEST (the quality of color, texture
     * coordinate, and fog coordinate interpolation should use the fastest method, probably a simple
     * linear interpolation of colors and/or texture coordinates). We set the clear color to gray,
     * set the shade model to GL_SMOOTH (causes the computed colors of vertices to be interpolated as
     * the primitive is rasterized, typically assigning different colors to each resulting pixel
     * fragment), enable the server side capability GL_DEPTH_TEST (each Fragment's output depth value
     * will be tested against the depth of the sample being written to. If the test fails, the fragment
     * is discarded. If the test passes, the depth buffer will be updated with the new fragment's output
     * depth), and enable the server side capability GL_TEXTURE_2D (If enabled and no fragment shader
     * is active, two-dimensional texturing is performed).
     *
     * Next we create our texture. we call the [GL10.glGenTextures] method of [gl] to generate a
     * texture name which we store in our field [mTextureID]. We bind [mTextureID] to the target
     * GL_TEXTURE_2D (While a texture is bound, GL operations on the target to which it is bound
     * affect the bound texture, and queries of the target to which it is bound return state from
     * the bound texture. If texture mapping is active on the target to which a texture is bound,
     * the bound texture is used. In effect, the texture targets become aliases for the textures
     * currently bound to them, and the texture name zero refers to the default textures that were
     * bound to them at initialization).
     *
     * We now proceed to configure our texture. We use the [GL10.glTexParameterf] method of [gl] to
     * set the texture parameter GL_TEXTURE_MIN_FILTER to GL_NEAREST (The texture minifying function
     * is used whenever the pixel being textured maps to an area greater than one texture element.
     * GL_NEAREST Returns the value of the texture element that is nearest (in Manhattan distance)
     * to the center of the pixel being textured). We set the texture parameter GL_TEXTURE_MAG_FILTER
     * to GL_LINEAR (The texture magnification function is used when the pixel being textured maps to
     * an area less than or equal to one texture element. GL_LINEAR Returns the weighted average of
     * the four texture elements that are closest to the center of the pixel being textured). We set
     * the texture parameters GL_TEXTURE_WRAP_S, and GL_TEXTURE_WRAP_T to GL_CLAMP_TO_EDGE (causes
     * texture coordinates to be clamped to the range [1/(2N), 1-1/(2N)] where N is the size of the
     * texture in the direction of clamping ie. the color of the edge colors will be repeated when
     * the drawing reaches the edge of the texture, rather than repeating the texture).
     *
     * We next set the GL_TEXTURE_ENV_MODE texture environment parameter of the target texture
     * environment GL_TEXTURE_ENV to GL_REPLACE (the texture color will replace the color already
     * present).
     *
     * We open an [InputStream] for `val inputStream` to read our raw resource file robot.png,
     * allocate a [Bitmap] for `val bitmap` and wrapped in a try we decode `inputStream` into
     * `bitmap`. We then use the [GLUtils.texImage2D] method to use `bitmap` as the image for
     * the target GL_TEXTURE_2D. Then we recycle `bitmap`.
     *
     * @param gl     the GL interface.
     * @param config the EGLConfig of the created surface.
     */
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER)
        /*
         * Some one-time OpenGL initialization can be made here
         * probably based on features of this particular context
         */
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST)
        gl.glClearColor(.5f, .5f, .5f, 1f)
        gl.glShadeModel(GL10.GL_SMOOTH)
        gl.glEnable(GL10.GL_DEPTH_TEST)
        gl.glEnable(GL10.GL_TEXTURE_2D)
        /*
         * Create our texture. This has to be done each time the
         * surface is created.
         */
        val textures = IntArray(1)
        gl.glGenTextures(1, textures, 0)
        mTextureID = textures[0]
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID)
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat())
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE.toFloat())
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE.toFloat())
        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE.toFloat())
        val inputStream: InputStream = mContext.resources.openRawResource(R.raw.robot)
        val bitmap: Bitmap
        bitmap = try {
            BitmapFactory.decodeStream(inputStream)
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) { // Ignore.
            }
        }
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
    }

    /**
     * Called to draw the current frame. First we disable the server side capability GL_DITHER (Do not
     * dither color components or indices before they are written to the color buffer). Then we set
     * the GL_TEXTURE_ENV_MODE texture environment parameter for the texture environment GL_TEXTURE_ENV
     * to GL_MODULATE (color from the texture image will be merged with the existing color on the
     * surface of the polygon by multiplying the color components together). We next clear both the
     * color buffer and the depth buffer.
     *
     * We set the matrix mode to GL_MODELVIEW (subsequent matrix operations will apply to the model
     * view matrix stack), and load it with the identify matrix. We define a viewing transformation
     * with the eye at (0,0,-5), the center at (0,0,0), and an up vector of (0,1,0).
     *
     * We enable the client side capability GL_VERTEX_ARRAY (the vertex array is enabled for writing
     * and used during rendering), and the client side capability GL_TEXTURE_COORD_ARRAY (the texture
     * coordinate array is enabled for writing and used during rendering). We select GL_TEXTURE0 to
     * be the active texture unit (selects which texture unit subsequent texture state calls will
     * affect), and bind our texture name [mTextureID] to the texture target GL_TEXTURE_2D (While a
     * texture is bound, GL operations on the target to which it is bound affect the bound texture,
     * and queries of the target to which it is bound return state from the bound texture. If texture
     * mapping is active on the target to which a texture is bound, the bound texture is used. In
     * effect, the texture targets become aliases for the textures currently bound to them, and the
     * texture name zero refers to the default textures that were bound to them at initialization).
     *
     * We set both the texture parameters GL_TEXTURE_WRAP_S and GL_TEXTURE_WRAP_T of GL_TEXTURE_2D
     * to GL_REPEAT (causes the integer part of the texture coordinate to be ignored; the GL uses
     * only the fractional part, thereby creating a repeating pattern).
     *
     * We next compute an [Float] `val angle` based on the current system time since boot modulo 4000
     * which creates an angle which goes from 0 to 360 degrees every 4 seconds, and we use it to
     * rotate our model matrix around the z axis. We then instruct our [Triangle] field [mTriangle]
     * to draw itself.
     *
     * @param gl the GL interface.
     */
    override fun onDrawFrame(gl: GL10) { /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER)
        gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE)
        /*
         * Usually, the first thing one might want to do is to clear
         * the screen. The most efficient way of doing this is to use
         * glClear().
         */gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        /*
         * Now we're ready to draw some 3D objects
         */gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glLoadIdentity()
        GLU.gluLookAt(gl, 0f, 0f, -5f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
        gl.glActiveTexture(GL10.GL_TEXTURE0)
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID)
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT)
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT)
        val time = SystemClock.uptimeMillis() % 4000L
        val angle = 0.090f * time.toInt()
        gl.glRotatef(angle, 0f, 0f, 1.0f)
        mTriangle.draw(gl)
    }

    /**
     * Called when the surface changes size. Called after the surface is created and whenever the
     * OpenGL ES surface size changes. We set the view port to have its lower left hand corner at
     * (0,0) and to have a width of [w] and a height of [h]. We calculate the aspect ratio [Float]
     * `val ratio` to be `w/h`, set the matrix mode to GL_PROJECTION (subsequent matrix operations
     * will apply to the projection matrix stack), load it with the identity matrix, then set its
     * left clipping plane to `-ratio`, its right clipping plane to `ratio`, its bottom clipping
     * plane to -1, its top clipping plane to 1, its near clipping plane to 3, and its far clipping
     * plane to 7.
     *
     * @param gl the GL interface.
     * @param w  width of new surface
     * @param h  height of new surface
     */
    override fun onSurfaceChanged(gl: GL10, w: Int, h: Int) {
        gl.glViewport(0, 0, w, h)
        /*
        * Set our projection matrix. This doesn't have to be done
        * each time we draw, but usually a new projection needs to
        * be set when the viewport is resized.
        */
        val ratio = w.toFloat() / h
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()
        gl.glFrustumf(-ratio, ratio, -1f, 1f, 3f, 7f)
    }

}