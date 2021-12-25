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
import android.opengl.GLES10
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.opengl.GLUtils
import android.os.SystemClock
import com.example.android.apis.R
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * A [GLSurfaceView.Renderer] that uses the Android-specific android.opengl.GLESXXX
 * static OpenGL ES APIs. The static APIs expose more of the OpenGL ES features than
 * the javax.microedition.khronos.opengles APIs, and also provide a programming model
 * that is closer to the C OpenGL ES APIs, which may make it easier to reuse code and
 * documentation written for the C OpenGL ES APIs. Used in CompressedTextureActivity.kt,
 * and TriangleActivity.kt
 */
class StaticTriangleRenderer : GLSurfaceView.Renderer {
    /**
     * Context used to retrieve resources, set in [init] method from one of its parameters. The
     * method [init] is called from both our [StaticTriangleRenderer] constructors and is
     * "this" [TriangleActivity] when called from the `onCreate` override of the
     * [TriangleActivity] demo, and "this" [CompressedTextureActivity] when called from
     * the `onCreate` override of the [CompressedTextureActivity] demo.
     */
    private var mContext: Context? = null

    /**
     * Set to a new instance of [Triangle] in our [init] method, whose [Triangle.draw] method
     * is called to draw the triangular shape we are animating.
     */
    private var mTriangle: Triangle? = null

    /**
     * The texture ID of the texture we are using, it is bound to the texturing target GL_TEXTURE_2D,
     * and all code uses GL_TEXTURE_2D to reference it.
     */
    private var mTextureID = 0

    /**
     * [TextureLoader] whose [TextureLoader.load] method is called to load the texture image we are
     * to use, it is set to one of the parameters to our method [init]. It is our own default
     * [RobotTextureLoader] as the [TextureLoader] when we are used by the activity [TriangleActivity],
     * and depending on the compile time switch TEST_CREATE_TEXTURE either `CompressedTextureLoader`,
     * or `SyntheticCompressedTextureLoader` when we are used by the activity [CompressedTextureActivity]
     */
    private var mTextureLoader: TextureLoader? = null

    /**
     * Our constructor needs to be provided with an implementation of this. We call its `load` in
     * our [onSurfaceCreated] callback to load the appropriate texture into our OpenGL context.
     */
    interface TextureLoader {
        /**
         * Load a texture into the currently bound OpenGL texture.
         *
         * @param gl OpenGL interface UNUSED
         */
        fun load(gl: GL10?)
    }

    /**
     * Constructor that uses our own default [RobotTextureLoader] as the [TextureLoader],
     * (which loads R.raw.robot image as our texture). It is used in [TriangleActivity]. We
     * simply call our method [init] with our [Context] parameter [context] and a new
     * instance of [RobotTextureLoader] to use as its [TextureLoader].
     *
     * @param context Context to use for resources, "this" [TriangleActivity] when called from
     * the `onCreate` override of the [TriangleActivity] demo
     */
    constructor(context: Context) {
        init(context, RobotTextureLoader())
    }

    /**
     * Constructor that uses [TextureLoader] parameter [loader] as its [TextureLoader]. It is used
     * in [CompressedTextureActivity]. We simply call our method [init] with our [Context] parameter
     * [context] to use for the context and [TextureLoader] parameter [loader] to use as the
     * [TextureLoader].
     *
     * @param context [Context] to use for resources, "this" [CompressedTextureActivity] when
     * called from the `onCreate` override of the [CompressedTextureActivity] demo.
     * @param loader  Class implementing the [TextureLoader] interface
     */
    constructor(context: Context, loader: TextureLoader) {
        init(context, loader)
    }

    /**
     * Called to initialize our [Context] field [mContext], [Triangle] field [mTriangle], and
     * [TextureLoader] field [mTextureLoader].
     *
     * @param context the [Context] to use to initialize our [Context] field [mContext]
     * @param loader  the [TextureLoader] to use to for our [TextureLoader] field [mTextureLoader]
     */
    private fun init(context: Context, loader: TextureLoader) {
        mContext = context
        mTriangle = Triangle()
        mTextureLoader = loader
    }

    /**
     * Called when the surface is created or recreated. Called when the rendering thread starts and
     * whenever the EGL context is lost. The EGL context will typically be lost when the Android
     * device awakes after going to sleep.
     *
     * First we disable dithering, then we use the method `glHint` to set the implementation
     * specific hint GL_PERSPECTIVE_CORRECTION_HINT to GL_FASTEST. We set the red, green, blue, and
     * alpha values used when the color buffers are cleared to (0.5,0.5,0.5,1.0) (GRAY), set the
     * shade model to GL_SMOOTH (causes the computed colors of vertices to be interpolated as the
     * primitive is rasterized, typically assigning different colors to each resulting pixel), we
     * enable GL_DEPTH_TEST (the depth value from the fragment being rendered is compared to the
     * depth value from the matching sample currently in the framebuffer and if it fails it is
     * discarded), and we enable GL_TEXTURE_2D (Images in our texture all are 2-dimensional. They
     * have width and height, but no depth).
     *
     * Now we create our texture. First we generate 1 texture name in our [IntArray] `val textures`,
     * and we store the texture name created in our [Int] field [mTextureID], then we bind that
     * texture to the texturing target GL_TEXTURE_2D (While a texture is bound, GL operations on the
     * target to which it is bound affect the bound texture, and queries of the target to which it
     * is bound return state from the bound texture. In effect, the texture targets become aliases
     * for the textures currently bound to them, and the texture name zero refers to the default
     * textures that were bound to them at initialization.)
     *
     * Now we set the texture parameters for GL_TEXTURE_2D:
     *
     *  * GL_TEXTURE_MIN_FILTER set to GL_NEAREST (The texture minifying function used whenever
     *  the level-of-detail function used when sampling from the texture determines that the
     *  texture should be minified: GL_NEAREST Returns the value of the texture element that
     *  is nearest (in Manhattan distance) to the specified texture coordinates.)
     *
     *  * GL_TEXTURE_MAG_FILTER set to GL_LINEAR (The texture magnification function is used
     *  whenever the level-of-detail function used when sampling from the texture determines
     *  that the texture should be magnified: GL_LINEAR Returns the weighted average of the
     *  texture elements that are closest to the specified texture coordinates.)
     *
     *  * GL_TEXTURE_WRAP_S set to GL_CLAMP_TO_EDGE (Sets the wrap parameter for texture coordinate s to
     *  GL_CLAMP_TO_EDGE: causes s coordinates to be clamped to the range [1/2N,1−1/2N], where N is
     *  the size of the texture in the direction of clamping)
     *
     *  * GL_TEXTURE_WRAP_T set to GL_CLAMP_TO_EDGE (Sets the wrap parameter for texture coordinate t to
     *  GL_CLAMP_TO_EDGE: see the discussion under GL_TEXTURE_WRAP_S)
     *
     * Then we set the target texture environment GL_TEXTURE_ENV texture environment parameter
     * GL_TEXTURE_ENV_MODE to the texture function GL_REPLACE. Finally we call the `load`
     * method of our [TextureLoader] field [mTextureLoader] to load the texture image.
     *
     * @param gl     the GL interface.
     * @param config the EGLConfig of the created surface.
     */
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        /**
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        GLES10.glDisable(GLES10.GL_DITHER)
        /**
         * Some one-time OpenGL initialization can be made here
         * probably based on features of this particular context
         */
        GLES10.glHint(GLES10.GL_PERSPECTIVE_CORRECTION_HINT, GLES10.GL_FASTEST)
        GLES10.glClearColor(.5f, .5f, .5f, 1f)
        GLES10.glShadeModel(GLES10.GL_SMOOTH)
        GLES10.glEnable(GLES10.GL_DEPTH_TEST)
        GLES10.glEnable(GLES10.GL_TEXTURE_2D)
        /**
         * Create our texture. This has to be done each time the
         * surface is created.
         */
        val textures = IntArray(1)
        GLES10.glGenTextures(1, textures, 0)
        mTextureID = textures[0]
        GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, mTextureID)
        GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MIN_FILTER, GLES10.GL_NEAREST.toFloat())
        GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MAG_FILTER, GLES10.GL_LINEAR.toFloat())
        GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_S, GLES10.GL_CLAMP_TO_EDGE.toFloat())
        GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_T, GLES10.GL_CLAMP_TO_EDGE.toFloat())
        GLES10.glTexEnvf(GLES10.GL_TEXTURE_ENV, GLES10.GL_TEXTURE_ENV_MODE, GLES10.GL_REPLACE.toFloat())
        mTextureLoader!!.load(gl)
    }

    /**
     * Called to draw the current frame. First we disable dithering, then we set the target texture
     * environment GL_TEXTURE_ENV texture environment parameter GL_TEXTURE_ENV_MODE to the texture
     * function GL_MODULATE, and then we clear the screen by calling `glClear` to clear both
     * the color buffer (GL_COLOR_BUFFER_BIT) and depth buffer (GL_DEPTH_BUFFER_BIT).
     *
     * Now we are ready to draw some 3D objects. We set the matrix stack GL_MODELVIEW to be the target
     * for subsequent matrix operations (The modelview matrix defines how your objects are transformed
     * (meaning translation, rotation and scaling) in your world coordinate frame), and load it with
     * the identity matrix. We call the utility function `GLU.gluLookAt` to:
     *
     *  * specify the position of the eye point to be (0,0,5)
     *  * specify the position of the reference point to be (0f, 0f, 0f)
     *  * specify the direction of the up vector to be (0f, 1.0f, 0.0f)
     *
     * This creates a viewing matrix derived from the eye point, the reference point indicating the
     * center of the scene, and the UP vector.
     *
     * We then enable the client-side capability GL_VERTEX_ARRAY (the vertex array is enabled for
     * writing and used during rendering when glArrayElement, glDrawArrays, glDrawElements,
     * glDrawRangeElements glMultiDrawArrays, or glMultiDrawElements is called), and the capability
     * GL_TEXTURE_COORD_ARRAY (the texture coordinate array is enabled for writing and used during
     * rendering when glArrayElement, glDrawArrays, glDrawElements, glDrawRangeElements
     * glMultiDrawArrays, or glMultiDrawElements is called).
     *
     * Now we specify that the GL_TEXTURE0 texture unit is active, and bind the texture ID stored in
     * our field [mTextureID] to GL_TEXTURE_2D. We set the texture parameters for GL_TEXTURE_2D
     * GL_TEXTURE_WRAP_S and GL_TEXTURE_WRAP_T both to GL_REPEAT (when the coordinate falls outside
     * (0..1) the integer part of the coordinate will be ignored and a repeating pattern is formed).
     *
     * Now we calculate the [Float] `val angle` we want to use to rotate our triangle (using an
     * arbitrary function of the system uptime), and rotate our modelview matrix by that angle.
     * Finally we instruct our [Triangle] field [mTriangle] to draw itself.
     *
     * @param gl the GL interface.
     */
    override fun onDrawFrame(gl: GL10) {
        /**
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        GLES10.glDisable(GLES10.GL_DITHER)
        GLES10.glTexEnvx(GLES10.GL_TEXTURE_ENV, GLES10.GL_TEXTURE_ENV_MODE, GLES10.GL_MODULATE)
        /**
         * Usually, the first thing one might want to do is to clear
         * the screen. The most efficient way of doing this is to use
         * glClear().
         */
        GLES10.glClear(GLES10.GL_COLOR_BUFFER_BIT or GLES10.GL_DEPTH_BUFFER_BIT)
        /**
         * Now we're ready to draw some 3D objects
         */
        GLES10.glMatrixMode(GLES10.GL_MODELVIEW)
        GLES10.glLoadIdentity()
        GLU.gluLookAt(gl,
            0f, 0f, -5f,
            0f, 0f, 0f,
            0f, 1.0f, 0.0f
        )
        GLES10.glEnableClientState(GLES10.GL_VERTEX_ARRAY)
        GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY)
        GLES10.glActiveTexture(GLES10.GL_TEXTURE0)
        GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, mTextureID)
        GLES10.glTexParameterx(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_S, GLES10.GL_REPEAT)
        GLES10.glTexParameterx(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_T, GLES10.GL_REPEAT)
        val time = SystemClock.uptimeMillis() % 4000L
        val angle = 0.090f * time.toInt()
        GLES10.glRotatef(angle, 0f, 0f, 1.0f)
        mTriangle!!.draw(gl)
    }

    /**
     * Called when the surface changed size. Called after the surface is created and whenever the
     * OpenGL ES surface size changes.
     *
     * First we calculate the `val ratio` of `w/h` which we will use to specify the coordinates for
     * the left and right vertical clipping planes. We set the matrix stack GL_PROJECTION to be the
     * for subsequent matrix operations (the projection matrix defines the properties of the camera
     * that views the objects in the world coordinate frame. Here you typically set the zoom factor,
     * aspect ratio and the near and far clipping planes), and load it with the identity matrix.
     * Then we call the method `glFrustumf` to multiply the current matrix by a perspective matrix
     * built using `-ratio` as the left vertical clipping plane, `ratio` as the right vertical
     * clipping plane, `-1` for the bottom clipping plane, `1` for the top clipping plane, `3` for
     * the near clipping plane, and `7` for the far clipping plane. ((left, bottom, -near) and
     * (right, top, -near) specify the points on the near clipping plane that are mapped to the
     * lower left and upper right corners of the window, assuming that the eye is located at
     * (0, 0, 0). `-far` specifies the location of the far clipping plane.)
     *
     * @param gl the GL interface.
     * @param w  new width of surface
     * @param h  new height of surface
     */
    override fun onSurfaceChanged(gl: GL10, w: Int, h: Int) {
        GLES10.glViewport(0, 0, w, h)
        /**
         * Set our projection matrix. This doesn't have to be done
         * each time we draw, but usually a new projection needs to
         * be set when the viewport is resized.
         */
        val ratio = w.toFloat() / h
        GLES10.glMatrixMode(GLES10.GL_PROJECTION)
        GLES10.glLoadIdentity()
        GLES10.glFrustumf(-ratio, ratio, -1f, 1f, 3f, 7f)
    }

    /**
     * Default [TextureLoader] class we use when we are used by the activity [TriangleActivity],
     * it consists of the png image R.raw.robot stored in our resources.
     */
    private inner class RobotTextureLoader : TextureLoader {
        /**
         * Loads the png image R.raw.robot as the two-dimensional texture image to be used in the
         * current context into GL_TEXTURE_2D. First we open a [InputStream] `val inputStream`
         * for reading the raw resource png image R.raw.robot. We declare [Bitmap] `val bitmap` and
         * read and decode `inputStream` into it. We use the utility method [GLUtils.texImage2D]
         * to set specify `bitmap` as the two-dimensional texture image used by GL_TEXTURE_2D,
         * and finally free the native object associated with `bitmap`.
         *
         * @param gl OpenGL interface UNUSED
         */
        override fun load(gl: GL10?) {
            val inputStream: InputStream = mContext!!.resources.openRawResource(R.raw.robot)

            @Suppress("JoinDeclarationAndAssignment")
            val bitmap: Bitmap
            bitmap = try {
                BitmapFactory.decodeStream(inputStream)
            } finally {
                try {
                    inputStream.close()
                } catch (e: IOException) { // Ignore.
                }
            }
            GLUtils.texImage2D(GLES10.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
        }
    }

    /**
     * Class used to draw a triangle.
     */
    internal class Triangle {
        /**
         * [FloatBuffer] loaded with the vertex coordinates of a unit-sided equilateral
         * triangle centered on the origin. It is used in our [draw] method in a call to
         * `glVertexPointer` in order to specify the location and data format of the array
         * of vertex coordinates to use when rendering.
         */
        private val mFVertexBuffer: FloatBuffer

        /**
         * [FloatBuffer] loaded with the x,y coordinates of a unit-sided equilateral triangle
         * centered on the origin. It is used in our [draw] method in a call to the method
         * `glTexCoordPointer` in order to specify the location and data format of an array
         * of texture coordinates to use when rendering.
         */
        private val mTexBuffer: FloatBuffer

        /**
         * [ShortBuffer] loaded with the three indices 0, 1, and 2. It is used in our [draw] method
         * in the call to `glDrawElements` to specify the indices which it uses to construct a
         * sequence of geometric primitives that are drawn using the vertices loaded from
         * [mFVertexBuffer] and texture vertices loaded from [mTexBuffer]
         */
        private val mIndexBuffer: ShortBuffer

        /**
         * Called when we are meant to draw our triangle. First we specify the orientation of front
         * facing polygons to be GL_CCW (counter clockwise). Then we call `glVertexPointer` to
         * specify the location and data format of the array of vertex coordinates to use when
         * rendering (3 coordinates per vertex, GL_FLOAT as the data type of each coordinate, 0 as
         * the stride between vertices (no extra data contained between vertices), and [FloatBuffer]
         * field [mFVertexBuffer] as the pointer to the first coordinate of the first vertex in the
         * array.
         *
         * Next we enable the GL_TEXTURE_2D server-side GL capability (If enabled and no fragment
         * shader is active, two-dimensional texturing is performed (unless three-dimensional or
         * cube-mapped texturing is also enabled), and we use `glTexCoordPointer` to define an
         * array of texture coordinates with 2 coordinates per array element, GL_FLOAT as the data
         * type, 0 as the byte offset between consecutive texture coordinate sets. and [FloatBuffer]
         * field [mTexBuffer] as the pointer to the first coordinate of the first vertex in the array.
         *
         * Finally we call `glDrawElements` to render primitives from our array data, using the
         * primitive type GL_TRIANGLE_STRIP, VERTS (3) elements to be rendered, GL_UNSIGNED_SHORT
         * as the type of the values in our indices buffer, and [ShortBuffer] field [mIndexBuffer]
         * as the pointer to the location where the indices are stored.
         *
         * @param gl OpenGL interface UNUSED
         */
        @Suppress("UNUSED_PARAMETER")
        fun draw(gl: GL10?) {
            GLES10.glFrontFace(GLES10.GL_CCW)
            GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, mFVertexBuffer)
            GLES10.glEnable(GLES10.GL_TEXTURE_2D)
            GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, mTexBuffer)
            GLES10.glDrawElements(
                GLES10.GL_TRIANGLE_STRIP,
                VERTS,
                GLES10.GL_UNSIGNED_SHORT,
                mIndexBuffer
            )
        }

        companion object {
            /**
             * Number of vertices.
             */
            private const val VERTS = 3
        }

        /**
         * Init block for our Constructor it allocates and initializes the content of the three
         * fields we use to draw a triangle: `FloatBuffer` field ` mFVertexBuffer`, `FloatBuffer`
         * field `mTexBuffer`, and `ShortBuffer` field `mIndexBuffer`.
         *
         * First: we allocate a direct byte buffer on the native heap for `ByteBuffer` `val vbb`,
         * and set its byte order to native order. We use `vbb` to create a view of this byte
         * buffer as a float buffer which we store in the `FloatBuffer` field `mFVertexBuffer`.
         *
         * Second: we allocate a direct byte buffer on the native heap for `ByteBuffer` `val tbb`,
         * and set its byte order to native order. We use `tbb` to create a view of this byte
         * buffer as a float buffer which we store in the `FloatBuffer` field `mTexBuffer`.
         *
         * Third: we allocate a direct byte buffer on the native heap for `ByteBuffer` `val ibb`,
         * and set its byte order to native order. We use `ibb` to create a view of this byte
         * buffer as a short buffer which we store in the `ShortBuffer` field `mIndexBuffer`.
         *
         * In `FloatArray` `val coords` we declare the coordinates of a unit-sided equilateral
         * triangle centered on the origin.
         *
         * We load each of the three, three dimensional points in `coords` into `FloatBuffer` field
         * `mFVertexBuffer` (each coordinate scaled by 2.0 for some reason). We load the x and y
         * coordinates of each of the three points in `coords` into `FloatBuffer` field `mTexBuffer`
         * (each coordinate scaled by 2.0 and offset by 0.5 for some reason). And we load the three
         * indices 0, 1, and 2 into `ShortBuffer` fuked `mIndexBuffer`.
         *
         * Finally we set the position of each of the buffers `mFVertexBuffer`, `mTexBuffer` and
         * `mIndexBuffer` to 0, so that the `gl*Pointer()` functions can read them from the
         * beginning.
         */
        init {
            /**
             * Buffers to be passed to gl*Pointer() functions
             * must be direct, i.e., they must be placed on the
             * native heap where the garbage collector cannot
             * move them.
             *
             * Buffers with multi-byte data types (e.g., short, int, float)
             * must have their byte order set to native order
             */
            val vbb = ByteBuffer.allocateDirect(VERTS * 3 * 4)
            vbb.order(ByteOrder.nativeOrder())
            mFVertexBuffer = vbb.asFloatBuffer()
            val tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4)
            tbb.order(ByteOrder.nativeOrder())
            mTexBuffer = tbb.asFloatBuffer()
            val ibb = ByteBuffer.allocateDirect(VERTS * 2)
            ibb.order(ByteOrder.nativeOrder())
            mIndexBuffer = ibb.asShortBuffer()
            /**
             * A unit-sided equilateral triangle centered on the origin.
             */
            val coords = floatArrayOf( // X, Y, Z
                -0.5f, -0.25f, 0f,
                0.5f, -0.25f, 0f,
                0.0f, 0.559016994f, 0f)
            for (i in 0 until VERTS) {
                for (j in 0..2) {
                    mFVertexBuffer.put(coords[i * 3 + j] * 2.0f)
                }
            }
            for (i in 0 until VERTS) {
                for (j in 0..1) {
                    mTexBuffer.put(coords[i * 3 + j] * 2.0f + 0.5f)
                }
            }
            for (i in 0 until VERTS) {
                mIndexBuffer.put(i.toShort())
            }
            mFVertexBuffer.position(0)
            mTexBuffer.position(0)
            mIndexBuffer.position(0)
        }
    }
}