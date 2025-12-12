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
@file:Suppress("ReplaceNotNullAssertionWithElvisReturn")

package com.example.android.apis.graphics.spritetext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
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
 * Renderer for the [GLSurfaceView] used for the demo.
 */
class SpriteTextRenderer(

    /**
     * [Context] to use for accessing resources ("this" when called from the `onCreate` method
     * of the activity [SpriteTextActivity]).
     */
    private val mContext: Context

) : GLSurfaceView.Renderer {
    /**
     * Width of the [GLSurfaceView] we are rendering to, set using the `w` parameter
     * passed to our method [onSurfaceChanged].
     */
    private var mWidth = 0

    /**
     * Height of the [GLSurfaceView] we are rendering to, set using the `h` parameter
     * passed to our method [onSurfaceChanged].
     */
    private var mHeight = 0

    /**
     * Rotating [Triangle] instance that we render.
     */
    private val mTriangle: Triangle = Triangle()

    /**
     * Texture name we use for our texture image robot.png
     */
    private var mTextureID = 0

    /**
     * Frame counter we use to determine milliseconds per frame value (goes from 0 to SAMPLE_PERIOD_FRAMES)
     */
    private var mFrames = 0

    /**
     * Current milliseconds per frame value, recalculated every SAMPLE_PERIOD_FRAMES frames
     */
    private var mMsPerFrame = 0

    /**
     * Start time for current counting of frames used to calculate the value of [mMsPerFrame]
     */
    private var mStartTime: Long = 0

    /**
     * [LabelMaker] containing labels for the three vertices of our triangle "A", "B", and "C",
     * as well as the label "ms/f"
     */
    private var mLabels: LabelMaker? = null

    /**
     * [Paint] instance we use for our labels as well as the labels that [NumericSprite]
     * draws to display our millisecond per frame data.
     */
    private val mLabelPaint: Paint = Paint()

    /**
     * `Label` index pointing to the `Label` "A" in [LabelMaker] field [mLabels]
     */
    private var mLabelA = 0

    /**
     * `Label` index pointing to the `Label` "B" in [LabelMaker] field [mLabels]
     */
    private var mLabelB = 0

    /**
     * `Label` index pointing to the `Label` "C" in [LabelMaker] field [mLabels]
     */
    private var mLabelC = 0

    /**
     * `Label` index pointing to the `Label` "ms/f" in [LabelMaker] field [mLabels]
     */
    private var mLabelMsPF = 0

    /**
     * [Projector] we use to "project" our vertex labels to the correct position on our rotating
     * triangle.
     */
    private val mProjector: Projector = Projector()

    /**
     * [NumericSprite] instance we use to draw the digit labels to display our [mMsPerFrame]
     * (milliseconds per frame) data at the bottom of the `SurfaceView`.
     */
    private var mNumericSprite: NumericSprite? = null

    /**
     * Scratch array we use in our call to [Projector.project] to calculate the correct location
     * of our triangle vertex labels.
     */
    private val mScratch = FloatArray(8)

    /**
     * Called when the surface is created or recreated. Called when the rendering thread starts and
     * whenever the EGL context is lost. The EGL context will typically be lost when the Android
     * device awakes after going to sleep. First we disable the server side capability GL_DITHER
     * (color components and indices will not be dithered before they are written to the color buffer).
     *
     * Next we specify the implementation specific hint GL_FASTEST for the GL_PERSPECTIVE_CORRECTION_HINT
     * target (Indicates the quality of color, texture coordinate, and fog coordinate interpolation.
     * GL_FASTEST will result in simple linear interpolation of colors and/or texture coordinates).
     *
     * We set the clear color to gray, set the shade model to GL_SMOOTH (causes the computed colors
     * of vertices to be interpolated as the primitive is rasterized, typically assigning different
     * colors to each resulting pixel fragment), enable the server side capability GL_DEPTH_TEST
     * (do depth comparisons and update the depth buffer), and the server side capability GL_TEXTURE_2D
     * (If enabled and no fragment shader is active, two-dimensional texturing is performed).
     *
     * Next we request that a texture name be generated, and we save the name in our [Int] field
     * [mTextureID]. We then bind [mTextureID] to the target GL_TEXTURE_2D (GL_TEXTURE_2D becomes
     * an alias for [mTextureID] which becomes a two dimensional texture. While a texture is bound,
     * GL operations on the target to which it is bound affect the bound texture, and queries of
     * the target to which it is bound return state from the bound texture).
     *
     * We set the texture parameter GL_TEXTURE_MIN_FILTER of GL_TEXTURE_2D to GL_NEAREST (The texture
     * minifying function is used whenever the pixel being textured maps to an area greater than one
     * texture element. GL_NEAREST causes the value of the texture element that is nearest (in Manhattan
     * distance) to the center of the pixel being textured to be used). We set the texture parameter
     * GL_TEXTURE_MAG_FILTER of GL_TEXTURE_2D to GL_LINEAR (The texture magnification function is
     * used when the pixel being textured maps to an area less than or equal to one texture element.
     * GL_LINEAR causes the weighted average of the four texture elements that are closest to the
     * center of the pixel being textured to be used).
     *
     * We set the texture parameters GL_TEXTURE_WRAP_S and GL_TEXTURE_WRAP_T of GL_TEXTURE_2D both to
     * GL_CLAMP_TO_EDGE (causes the color of the pixel at the edges of the texture to be used when
     * the area being textured extends past the size of the texture). We set texture parameter
     * GL_TEXTURE_ENV_MODE of the texture environment target GL_TEXTURE_ENV to GL_REPLACE (causes
     * the colors of the texture to replace whatever colors were there before).
     *
     * We open [InputStream] `val inputStream` to read the contents of our raw resource robot.png,
     * declare [Bitmap] `val bitmap`, then decode `inputStream` into `bitmap`. We upload `bitmap`
     * to the texture target GL_TEXTURE_2D and recycle `bitmap`.
     *
     * If we already have a [LabelMaker] field [mLabels] in use (our surface has been recreated), we
     * call its `shutdown` method to have it delete its current texture, and if [mLabels] is *null*
     * we initialize it with a new instance of [LabelMaker]. We then instruct [mLabels] to begin
     * adding labels and add the four labels "A", "B", "C", and "ms/f" and saving the index number
     * returned in [mLabelA], [mLabelB], [mLabelC], and [mLabelMsPF] respectively. We then instruct
     * [mLabels] to end the adding of labels.
     *
     * Finally, if [NumericSprite] field [mNumericSprite] is not null (our surface has been recreated),
     * we instruct it to `shutdown`, otherwise we initialize [mNumericSprite] with a new instance of
     * [NumericSprite]. Then we instruct [mNumericSprite] to initialize.
     *
     * @param gl     the GL interface
     * @param config the EGLConfig of the created surface. UNUSED
     */
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) { /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER)
        /*
         * Some one-time OpenGL initialization can be made here
         * probably based on features of this particular context
         */gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST)
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
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_MIN_FILTER,
            GL10.GL_NEAREST.toFloat()
        )
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_WRAP_S,
            GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_WRAP_T,
            GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE.toFloat())
        val inputStream: InputStream = mContext.resources.openRawResource(R.raw.robot)

        @Suppress("JoinDeclarationAndAssignment")
        val bitmap: Bitmap
        bitmap = try {
            BitmapFactory.decodeStream(inputStream)
        } finally {
            try {
                inputStream.close()
            } catch (_: IOException) { // Ignore.
            }
        }
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
        if (mLabels != null) {
            mLabels!!.shutdown(gl)
        } else {
            mLabels = LabelMaker(true, 256, 64)
        }
        mLabels!!.initialize(gl)
        mLabels!!.beginAdding(gl)
        mLabelA = mLabels!!.add(gl, "A", mLabelPaint)
        mLabelB = mLabels!!.add(gl, "B", mLabelPaint)
        mLabelC = mLabels!!.add(gl, "C", mLabelPaint)
        mLabelMsPF = mLabels!!.add(gl, "ms/f", mLabelPaint)
        mLabels!!.endAdding(gl)
        if (mNumericSprite != null) {
            mNumericSprite!!.shutdown(gl)
        } else {
            mNumericSprite = NumericSprite()
        }
        mNumericSprite!!.initialize(gl, mLabelPaint)
    }

    /**
     * Called to draw the current frame. First we disable the server side capability GL_DITHER (color
     * components and indices will not be dithered before they are written to the color buffer). Then
     * we set texture parameter  GL_TEXTURE_ENV_MODE of the texture environment target GL_TEXTURE_ENV
     * to GL_MODULATE (causes the colors from the texture units to be multiplied). Next we clear both
     * the color buffer and the depth buffer.
     *
     * To do the drawing we make the model view matrix the current matrix, load it with the identity
     * matrix, then we create a viewing matrix derived from an eye point at (0,0,-2.5), a reference
     * point indicating the center of the scene at (0,0,0), and an UP vector or (0,1,0).
     *
     * We enable the client side capability GL_VERTEX_ARRAY (the vertex array is enabled for writing
     * and used during rendering), and the client side capability GL_TEXTURE_COORD_ARRAY (the texture
     * coordinate array is enabled for writing and used during rendering). We set the active texture
     * unit to GL_TEXTURE0, and bind our texture name [mTextureID] to the texture target GL_TEXTURE_2D.
     * We set the texture parameters GL_TEXTURE_WRAP_S and GL_TEXTURE_WRAP_T of GL_TEXTURE_2D both to
     * GL_REPEAT (causes the texture to be repeated when the area being textured is bigger than the
     * texture).
     *
     * We calculate `val angle` based on the system time since boot modulo 4000, multiplied by a
     * factor of 0.090 (angle goes from 0 degrees to 360 degrees every 4 seconds). We then rotate
     * our model view matrix by `angle` degrees around the z axis, and scale it by 2.0 in all
     * three directions (multiply the current matrix by a general scaling matrix using 2.0 for all
     * three scale factors). Then we instruct our [Triangle] field [mTriangle] to draw itself.
     *
     * To add our labels to the `SurfaceView` we instruct our [Projector] field [mProjector] to
     * load the current model view matrix, tell our [LabelMaker] field [mLabels] to begin drawing,
     * then call our method [drawLabel] to draw the three vertex labels [mLabelA], [mLabelB], and
     * [mLabelC]. We calculate [Float] `val msPFX` to be the x coordinate of our label [mLabelMsPF]
     * by subtracting the width of that label from the width [mWidth] of our surface view (with an
     * additional pixel for spacing), then instruct [mLabels] to draw our label [mLabelMsPF] at the
     * xy location `(msPFX,0)`. We then instruct [mLabels] to end its drawing state.
     *
     * Finally we call our method [drawMsPF] to display the milliseconds per frame data in front of
     * the [mLabelMsPF] label.
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
        GLU.gluLookAt(
            gl, 0.0f, 0.0f, -2.5f,
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f
        )
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
        gl.glActiveTexture(GL10.GL_TEXTURE0)
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID)
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT)
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT)
        val time = SystemClock.uptimeMillis() % 4000L
        val angle = 0.090f * time.toInt()
        gl.glRotatef(angle, 0f, 0f, 1.0f)
        gl.glScalef(2.0f, 2.0f, 2.0f)
        mTriangle.draw(gl)
        mProjector.getCurrentModelView(gl)
        mLabels!!.beginDrawing(gl, mWidth.toFloat(), mHeight.toFloat())
        drawLabel(gl, 0, mLabelA)
        drawLabel(gl, 1, mLabelB)
        drawLabel(gl, 2, mLabelC)
        val msPFX = mWidth - mLabels!!.getWidth(mLabelMsPF) - 1
        mLabels!!.draw(gl, msPFX, 0f, mLabelMsPF)
        mLabels!!.endDrawing(gl)
        drawMsPF(gl, msPFX)
    }

    /**
     * Draws the milliseconds per frame data on the `SurfaceView` using our [NumericSprite] field
     * [mNumericSprite] to draw the value using its per digit labels.
     *
     * First we fetch the system time since boot to [Long] `val time`, and if [mStartTime] is
     * 0 (our first time called) we also save it in [mStartTime]. We increment our field
     * [mFrames] and if we have waited for SAMPLE_PERIOD_FRAMES (12) since our last update,
     * we set [mFrames] to 0, calculate [Long] `val delta` as the number of milliseconds that
     * have passed between `time` and [mStartTime], set [mStartTime] to `time`, and set
     * [mMsPerFrame] to the number of milliseconds per frame we calculate by multiplying
     * `delta` times SAMPLE_FACTOR.
     *
     * Then if [mMsPerFrame] is greater than 0, we tell [mNumericSprite] to set its value
     * to [mMsPerFrame], retrieve the width need to display this value to [Float] `val numWidth`
     * and calculate the value of the x coordinate to begin our number display [Float] `val x` by
     * subtracting `numWidth` from our input parameter [rightMargin].
     *
     * Finally we instruct [mNumericSprite] to draw its value at (`x`, 0).
     *
     * @param gl          the GL interface
     * @param rightMargin x coordinate of the end of our milliseconds display (the beginning of the
     * "ms/f" label).
     */
    private fun drawMsPF(gl: GL10, rightMargin: Float) {
        val time = SystemClock.uptimeMillis()
        if (mStartTime == 0L) {
            mStartTime = time
        }
        if (mFrames++ == SAMPLE_PERIOD_FRAMES) {
            mFrames = 0
            val delta = time - mStartTime
            mStartTime = time
            mMsPerFrame = (delta * SAMPLE_FACTOR).toInt()
        }
        if (mMsPerFrame > 0) {
            mNumericSprite!!.setValue(mMsPerFrame)
            val numWidth = mNumericSprite!!.width()
            val x = rightMargin - numWidth
            mNumericSprite!!.draw(gl, x, 0f, mWidth.toFloat(), mHeight.toFloat())
        }
    }

    /**
     * Draws the vertex label requested in the proper position on the rotating triangle. First we
     * get the x and y coordinates of the [triangleVertex] vertex we are to label to the variables
     * `x` and `y`. We load our scratch vector with `x`, `y`, 0, and 1.0 for the w coordinate (to
     * indicate it is a point). Then we call our the [Projector.project] method of our [Projector]
     * field [mProjector] to translate the relative position of our vertex to the absolute position
     * in the rotating model view and retrieve the resulting x coordinate to `val sx`, and the y
     * coordinate to `val sy`. We get the height of our label for [Float] `val height`, and the
     * width of our label for [Float] `val width` and calculate the centered location `(tx,ty)` for
     * placing our label by subtracting half the width from `sx` and half the height from `sy`
     * respectively.
     *
     * Finally we instruct our [LabelMaker] field [mLabels] to draw the label with index [labelId]
     * at the location `(tx,ty)`.
     *
     * @param gl             the GL interface
     * @param triangleVertex the index number of the vertex, 0, 1, or 2.
     * @param labelId        the label index we are to draw.
     */
    private fun drawLabel(gl: GL10, triangleVertex: Int, labelId: Int) {
        val x = mTriangle.getX(triangleVertex)
        val y = mTriangle.getY(triangleVertex)
        mScratch[0] = x
        mScratch[1] = y
        mScratch[2] = 0.0f
        mScratch[3] = 1.0f
        mProjector.project(mScratch, 0, mScratch, 4)
        val sx = mScratch[4]
        val sy = mScratch[5]
        val height = mLabels!!.getHeight(labelId)
        val width = mLabels!!.getWidth(labelId)
        val tx = sx - width * 0.5f
        val ty = sy - height * 0.5f
        mLabels!!.draw(gl, tx, ty, labelId)
    }

    /**
     * Called when the surface changed size. Called after the surface is created and whenever the
     * OpenGL ES surface size changes. First we save our parameter [w] (the new width) in our field
     * [mWidth] and our parameter [h] (the new height) in our field [mHeight]. Then we set the
     * viewport of our surface to have the lower left hand corner at `(0,0)`, a width of [w] and a
     * height of [h]. We also inform our [Projector] field [mProjector] about the new surface size.
     *
     * Next we calculate the [Float] aspect ration `val ratio`, set the current matrix to GL_PROJECTION,
     * load it with the identity matrix, and multiply it by a perspective matrix with the left clipping
     * plane at `-ratio`, the right clipping plane at `ratio`, the bottom clipping plane at -1, the
     * top clipping plane at 1, the near clipping plane at 1, and the far clipping plane at 10.
     * Finally we instruct [Projector] field [mProjector] to fetch a copy of this projection matrix
     * for its use.
     *
     * @param gl the GL interface.
     * @param w  new width of the surface
     * @param h  hew height of the surface
     */
    override fun onSurfaceChanged(gl: GL10, w: Int, h: Int) {
        mWidth = w
        mHeight = h
        gl.glViewport(0, 0, w, h)
        mProjector.setCurrentView(0, 0, w, h)
        /*
        * Set our projection matrix. This doesn't have to be done
        * each time we draw, but usually a new projection needs to
        * be set when the viewport is resized.
        */
        val ratio = w.toFloat() / h
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()
        gl.glFrustumf(-ratio, ratio, -1f, 1f, 1f, 10f)
        mProjector.getCurrentProjection(gl)
    }

    companion object {
        /**
         * Number of frames to draw before updating the value of `mMsPerFrame`
         */
        private const val SAMPLE_PERIOD_FRAMES = 12

        /**
         * Factor to multiply elapsed time for drawing SAMPLE_PERIOD_FRAMES frames to calculate the
         * value of `mMsPerFrame`
         */
        private const val SAMPLE_FACTOR = 1.0f / SAMPLE_PERIOD_FRAMES
    }

    /**
     * The init block of our constructor. We set the text size of `Paint` field `mLabelPaint` to 32,
     * set its antialias flag, and set its color to black.
     */
    init {
        mLabelPaint.textSize = 32f
        mLabelPaint.isAntiAlias = true
        mLabelPaint.setARGB(0xff, 0x00, 0x00, 0x00)
    }
}

/**
 * Class that draws a triangle to the surface view when required to do so.
 */
internal class Triangle {
    /**
     * Native heap [FloatBuffer] we use to hold our vertex coordinates in.
     */
    private val mFVertexBuffer: FloatBuffer

    /**
     * Native heap [FloatBuffer] we use to hold our texture coordinates in.
     */
    private val mTexBuffer: FloatBuffer

    /**
     * Native heap [ShortBuffer] we use to hold our indices in.
     */
    private val mIndexBuffer: ShortBuffer

    /**
     * Called from the `onDrawFrame` method of [SpriteTextRenderer] to draw our triangle. First we
     * select counterclockwise polygons as front-facing. Next we specify [mFVertexBuffer] to be the
     * location of our vertex data, with 3 coordinates per vertex, GL_FLOAT as the data type, and 0
     * as the stride. We enable the server side capability GL_TEXTURE_2D, and specify [mTexBuffer]
     * to be the location of our texture coordinates, with 2 coordinates per array element, GL_FLOAT
     * as the data type, and 0 as the stride.
     *
     * Finally we instruct openGL to render primitives from array data, using GL_TRIANGLE_STRIP as
     * the primitive type, VERTS (3) as the number of elements to be rendered, [mIndexBuffer] as
     * the location of the indices, and GL_UNSIGNED_SHORT as the type of values it contains.
     *
     * @param gl the GL interface
     */
    fun draw(gl: GL10) {
        gl.glFrontFace(GL10.GL_CCW)
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer)
        gl.glEnable(GL10.GL_TEXTURE_2D)
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer)
        gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, VERTS, GL10.GL_UNSIGNED_SHORT, mIndexBuffer)
    }

    /**
     * Getter for the x coordinate of a particular vertex.
     *
     * @param vertex vertex number, 0, 1, or 2.
     * @return the x coordinate of vertex number `vertex`
     */
    fun getX(vertex: Int): Float {
        return sCoords[3 * vertex]
    }

    /**
     * Getter for the y coordinate of a particular vertex.
     *
     * @param vertex vertex number, 0, 1, or 2.
     * @return the y coordinate of vertex number `vertex`
     */
    fun getY(vertex: Int): Float {
        return sCoords[3 * vertex + 1]
    }

    companion object {
        /**
         * number of vertices of our object.
         */
        private const val VERTS = 3

        /**
         * (x,y,z) coordinates for a unit-sided equilateral triangle centered on the origin.
         */
        private val sCoords: FloatArray = floatArrayOf( // X, Y, Z
            -0.5f, -0.25f, 0f,
            0.5f, -0.25f, 0f,
            0.0f, 0.559017f, 0f
        )
    }

    /**
     * The init block of Our constructor. First we allocate `ByteBuffer` variable `val vbb` on the
     * native heap, with enough space to hold our `FloatArray` field `sCoords` array of vertex
     * coordinates, we set its byte order to native byte order, and initialize our `FloatBuffer`
     * field `mFVertexBuffer` with a view of this byte buffer as a float buffer. We allocate `ByteBuffer`
     * variable `val tbb` on the native heap, with enough space to hold our two dimensional texture
     * vertex coordinates, we set its byte order to native byte order, and initialize our `FloatBuffer`
     * field `mTexBuffer` with a view of this byte buffer as a float buffer. We allocate `ByteBuffer`
     * variable `val ibb` on the native heap, with enough space to hold our indices, we set its byte
     * order to native byte order, and initialize our `ShortBuffer` field `mIndexBuffer` with a view
     * of this byte buffer as a short buffer.
     *
     * Next we loop through the 3 vertices, each with 3 coordinates and add the coordinate values from
     * `FloatArray` field `sCoords` to `mFVertexBuffer`. For the texture coordinates we loop through
     * the 3 vertices, each with 2 coordinates, scaling the (x,y) coordinates of `sCoords` by
     * 2.0 and offsetting them by 0.5 before storing them in `mTexBuffer`. For our index buffer
     * `mIndexBuffer` we simply add the three indices 0, 1, 2.
     *
     * Finally we rewind our three buffers `mFVertexBuffer`, `mTexBuffer`, and `mIndexBuffer` so they
     * will be ready for use.
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
        for (i in 0 until VERTS) {
            for (j in 0..2) {
                mFVertexBuffer.put(sCoords[i * 3 + j])
            }
        }
        for (i in 0 until VERTS) {
            for (j in 0..1) {
                mTexBuffer.put(sCoords[i * 3 + j] * 2.0f + 0.5f)
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