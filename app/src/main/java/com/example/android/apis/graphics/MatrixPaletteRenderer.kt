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
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.CharBuffer
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11
import javax.microedition.khronos.opengles.GL11Ext
import kotlin.math.cos
import kotlin.math.sin

/**
 * This renderer is part of the [MatrixPaletteActivity] sample which shows how to implement a
 * Matrix Palette, which is used to rock a column back and forth.
 */
class MatrixPaletteRenderer
/**
 * Our constructor, we simply save our parameter in our [Context] field [mContext].
 *
 * Parameter: [mContext] the [Context] to use to retrieve resources, this when called from the
 * `onCreate` method of the [MatrixPaletteActivity] `Activity`.
 */
(
        /**
         * Context used to retrieve resources, set in our constructor ("this" when called from the
         * `onCreate` method of `MatrixPaletteActivity`).
         */
        private val mContext: Context

) : GLSurfaceView.Renderer {
    /**
     * [Grid] containing our column vertices. It is created and configured by our method
     * [generateWeightedGrid] with all the data needed for drawing by tbe [Grid.draw] method
     * loaded into GPU vertex buffer objects.
     */
    private var mGrid: Grid? = null
    /**
     * Texture name that we use for the texture we use for GL_TEXTURE_2D, it is created and uploaded
     * to the GPU in the method [onSurfaceCreated] from the raw resource robot.png
     */
    private var mTextureID = 0

    /**
     * A grid is a topologically rectangular array of vertices. This grid class is customized for
     * the vertex data required for this example. The vertex and index data are held in VBO objects
     * because on most GPUs VBO objects are the fastest way of rendering static vertex and index
     * data.
     *
     * @param w width of our [Grid] in vertices
     * @param h height of our [Grid] in vertices
     */
    private class Grid(w: Int, h: Int) {
        /**
         * Buffer object name for the GPU VBO we use for our vertex buffer
         */
        private var mVertexBufferObjectId = 0
        /**
         * Buffer object name for the GPU VBO we use for our GL_ELEMENT_ARRAY_BUFFER indices into
         * our vertex buffer. Consists of the indices of the vertex buffer elements that are to be
         * used for the GL_TRIANGLES triangles that will be drawn by `glDrawElements`.
         */
        private var mElementBufferObjectId = 0

        /**
         * The following buffers are used to hold the vertex and index data while constructing the
         * grid. Once createBufferObjects() is called the buffers are nulled out to save memory.
         */

        /**
         * [ByteBuffer] pointer for the vertex data, allows the vertex data buffer to be accessed
         * as [Byte] values when "putting" the byte values for the palette matrix indices.
         */
        private var mVertexByteBuffer: ByteBuffer?
        /**
         * [FloatBuffer] pointer for the vertex data, allows the vertex data to be accessed as
         * [Float] values when "putting" float values into the vertex data buffer.
         */
        private var mVertexBuffer: FloatBuffer?
        /**
         * [CharBuffer] in which we build the indices of the GL_TRIANGLES triangles of our column,
         * which we later upload to the GL_ELEMENT_ARRAY_BUFFER GPU VBO for drawing .
         */
        private var mIndexBuffer: CharBuffer?
        /**
         * Width of our [Grid] in number of vertices, set in our constructor.
         */
        private val mW: Int
        /**
         * Height of our [Grid] in number of vertices, set in our constructor.
         */
        private val mH: Int
        /**
         * Number of indices in our [Grid] and [Char] values in [mIndexBuffer],
         * used in the call to `glDrawElements`.
         */
        private val mIndexCount: Int

        /**
         * Sets the values of a specific vertex in [FloatBuffer] field [mVertexBuffer] (alias for
         * the [Byte] values in [ByteBuffer] field [mVertexByteBuffer]). After making sure our input
         * values are within range and throwing [IllegalArgumentException] if they are not, we
         * calculate the index value `val index` for the vertex in question, which is `(mW*j + i)`
         * (the number of vertices in a row times the row number (y index), plus the column number
         * (x index). We use `index` to calculate the correct offset into [FloatBuffer] field
         * [mVertexBuffer] (which is `index*VERTEX_SIZE/FLOAT_SIZE`) and position [mVertexBuffer] to
         * this offset. We then [FloatBuffer.put] our seven [Float] parameters `x, y, z, u, v, w0, w1`
         * into [mVertexBuffer]. We then position [ByteBuffer] field [mVertexByteBuffer] to the
         * correct position for our two byte parameters `p0, p1` of our vertex (which is
         * `index*VERTEX_SIZE + VERTEX_PALETTE_INDEX_OFFSET`) and put them into the buffer in
         * order.
         *
         * @param i  [Int] x index of the vertex to set
         * @param j  [Int] y index of the vertex to set
         * @param x  [Float] x coordinate of the vertex
         * @param y  [Float] y coordinate of the vertex
         * @param z  [Float] z coordinate of the vertex
         * @param u  [Float] x coordinate of the texture to use
         * @param v  [Float] y coordinate of the texture to use
         * @param w0 [Float] weight of palette matrix 0
         * @param w1 [Float] weight of palette matrix 1
         * @param p0 [Int] index of palette matrix that w0 refers to (always 0)
         * @param p1 [Int] index of palette matrix that w1 refers to (always 1)
         */
        operator fun set(i: Int, j: Int,
                         x: Float, y: Float, z: Float,
                         u: Float, v: Float,
                         w0: Float, w1: Float,
                         p0: Int, p1: Int) {
            require(!(i < 0 || i >= mW)) { "i" }
            require(!(j < 0 || j >= mH)) { "j" }
            require(w0 + w1 == 1.0f) { "Weights must add up to 1.0f" }
            val index = mW * j + i
            mVertexBuffer!!.position(index * VERTEX_SIZE / FLOAT_SIZE)
            mVertexBuffer!!.put(x)
            mVertexBuffer!!.put(y)
            mVertexBuffer!!.put(z)
            mVertexBuffer!!.put(u)
            mVertexBuffer!!.put(v)
            mVertexBuffer!!.put(w0)
            mVertexBuffer!!.put(w1)
            mVertexByteBuffer!!.position(index * VERTEX_SIZE + VERTEX_PALETTE_INDEX_OFFSET)
            mVertexByteBuffer!!.put(p0.toByte())
            mVertexByteBuffer!!.put(p1.toByte())
        }

        /**
         * Uploads our two data buffers: [ByteBuffer] field [mVertexByteBuffer] (our vertex buffer)
         * and [CharBuffer] field [mIndexBuffer] (our index buffer) to the proper openGL GPU VBOs.
         * First we allocate 2 ints for [Int] array `val vboIds`, cast our [GL] argument [gl] to
         * initialize our variable [GL11] `val gl11`, and use `gl11` to generate 2 buffer object
         * names in `vboIds`. We initialize our fields [mVertexBufferObjectId] and
         * [mElementBufferObjectId] with these two buffer object names.
         *
         * We bind [mVertexBufferObjectId] to the target GL_ARRAY_BUFFER (The GL_ARRAY_BUFFER
         * target for buffer objects represents the intent to use that buffer object for vertex
         * attribute data), then we rewind [mVertexByteBuffer] and then we create and initialize
         * the GL_ARRAY_BUFFER buffer object's data store using [mVertexByteBuffer], giving
         * openGL the hint GL_STATIC_DRAW (The user will be writing data to the buffer, but the user
         * will not read it, and the user will set the data only once).
         *
         * We bind [mElementBufferObjectId] to the target GL_ELEMENT_ARRAY_BUFFER (the
         * GL_ELEMENT_ARRAY_BUFFER contains the indices of each element in the GL_ARRAY_BUFFER
         * buffer), then we rewind [mIndexBuffer] and create and initialize the GL_ELEMENT_ARRAY_BUFFER
         * buffer object's data store using [mIndexBuffer], giving openGL the hint GL_STATIC_DRAW
         * (as above).
         *
         * In order to save memory we now null out [mVertexBuffer], [mVertexByteBuffer], and
         * [mIndexBuffer] so their storage can be garbage collected.
         *
         * @param gl the [GL] interface.
         */
        fun createBufferObjects(gl: GL) {
            /**
             * Generate a the vertex and element buffer IDs
             */
            val vboIds = IntArray(2)
            val gl11 = gl as GL11
            gl11.glGenBuffers(2, vboIds, 0)
            mVertexBufferObjectId = vboIds[0]
            mElementBufferObjectId = vboIds[1]

            /**
             * Upload the vertex data
             */
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectId)
            mVertexByteBuffer!!.position(0)
            gl11.glBufferData(
                    GL11.GL_ARRAY_BUFFER,
                    mVertexByteBuffer!!.capacity(),
                    mVertexByteBuffer,
                    GL11.GL_STATIC_DRAW
            )
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectId)
            mIndexBuffer!!.position(0)
            gl11.glBufferData(
                    GL11.GL_ELEMENT_ARRAY_BUFFER,
                    mIndexBuffer!!.capacity() * CHAR_SIZE,
                    mIndexBuffer,
                    GL11.GL_STATIC_DRAW
            )

            /**
             * We don't need the in-memory data any more
             */
            mVertexBuffer = null
            mVertexByteBuffer = null
            mIndexBuffer = null
        }

        /**
         * Called from [onDrawFrame] to issue the commands for the openGL GPU to draw our VBOs
         * to the [GLSurfaceView]. First we cast our [GL] parameter [gl] to initialize [GL11]
         * variable `val gl11` and cast our [GL] parameter [gl] to initialize [GL11Ext] variable
         * `val gl11Ext`. We enable the client side capability GL_VERTEX_ARRAY (the vertex array
         * is enabled for writing and used during rendering), and bind our buffer object
         * [mVertexBufferObjectId] to the target GL_ARRAY_BUFFER (registers our intent to use
         * that buffer object for vertex attribute data). We next specify the location and data
         * format of our array of vertex coordinates to use when rendering to have 3 coordinates
         * per vertex, to be in GL_FLOAT data type, have a stride of VERTEX_SIZE (32), and initial
         * pointer of 0. We then specify the location and data format of an array of texture
         * coordinates to use when rendering to have 2 coordinates per array element, to be in
         * GL_FLOAT data type, have a stride of VERTEX_SIZE (32), and have an initial pointer of
         * VERTEX_TEXTURE_BUFFER_INDEX_OFFSET*FLOAT_SIZE (ie. 3*4=12 in our case).
         *
         * We enable the client side capability GL_MATRIX_INDEX_ARRAY_OES (the palette matrix index
         * array is enabled for writing and used for rendering), and the client side capability
         * GL_WEIGHT_ARRAY_OES (the palette matrix weight array is enabled for writing and used for
         * rendering). Then we specify the palette matrix weight array pointer to have 2 entries, of
         * type GL_FLOAT, with a stride of VERTEX_SIZE (32), and an initial pointer of the quantity
         * VERTEX_WEIGHT_BUFFER_INDEX_OFFSET*FLOAT_SIZE (ie. 5*4=20 in our case). We specify the
         * palette matrix index pointer to have 2 entries, of type GL_UNSIGNED_BYTE, with a stride
         * of VERTEX_SIZE (32 in our case), and an initial pointer of VERTEX_PALETTE_INDEX_OFFSET
         * (28 in our case). These two are used to describe the weights and matrix indices used to
         * blend corresponding matrices for a given vertex.
         *
         * Now we bind our buffer name [mElementBufferObjectId] to the GL_ELEMENT_ARRAY_BUFFER
         * (the GL_ELEMENT_ARRAY_BUFFER contains the indices of each element in the GL_ARRAY_BUFFER
         * buffer), and we call the method `glDrawElements` to render [mIndexCount] GL_TRIANGLES
         * primitives, with values of our indices being of the type GL_UNSIGNED_SHORT, and an
         * initial pointer of 0.
         *
         * Having done our drawing, we now disable the client side capability GL_VERTEX_ARRAY,
         * GL_MATRIX_INDEX_ARRAY_OES, and GL_WEIGHT_ARRAY_OES. Then reset our binding of
         * GL_ARRAY_BUFFER and GL_ELEMENT_ARRAY_BUFFER to 0.
         *
         * @param gl the [GL] interface.
         */
        fun draw(gl: GL10) {
            val gl11 = gl as GL11
            val gl11Ext = gl as GL11Ext
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectId)
            gl11.glVertexPointer(3, GL10.GL_FLOAT, VERTEX_SIZE, 0)
            gl11.glTexCoordPointer(2, GL10.GL_FLOAT, VERTEX_SIZE, VERTEX_TEXTURE_BUFFER_INDEX_OFFSET * FLOAT_SIZE)
            gl.glEnableClientState(GL11Ext.GL_MATRIX_INDEX_ARRAY_OES)
            gl.glEnableClientState(GL11Ext.GL_WEIGHT_ARRAY_OES)
            gl11Ext.glWeightPointerOES(2, GL10.GL_FLOAT, VERTEX_SIZE, VERTEX_WEIGHT_BUFFER_INDEX_OFFSET * FLOAT_SIZE)
            gl11Ext.glMatrixIndexPointerOES(2, GL10.GL_UNSIGNED_BYTE, VERTEX_SIZE, VERTEX_PALETTE_INDEX_OFFSET)
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectId)
            gl11.glDrawElements(GL10.GL_TRIANGLES, mIndexCount, GL10.GL_UNSIGNED_SHORT, 0)
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
            gl.glDisableClientState(GL11Ext.GL_MATRIX_INDEX_ARRAY_OES)
            gl.glDisableClientState(GL11Ext.GL_WEIGHT_ARRAY_OES)
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0)
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0)
        }

        companion object {

            /**
             * Number of bytes in a `float` value
             */
            const val FLOAT_SIZE = 4
            /**
             * Number of bytes in a `char` value
             */
            const val CHAR_SIZE = 2

            /**
             * Vertex structure:
             *
             *  * [Float] x, y, z;
             *  * [Float] u, v;
             *  * [Float] weight0, weight1;
             *  * [Byte] palette0, palette1, pad0, pad1;
             */

            /**
             * Total number of bytes for a complete vertex entry
             */
            const val VERTEX_SIZE = 8 * FLOAT_SIZE
            /**
             * Offset to the texture coordinates of a vertex entry
             */
            const val VERTEX_TEXTURE_BUFFER_INDEX_OFFSET = 3
            /**
             * Offset to the palette matrix weight part of the vertex entry
             */
            const val VERTEX_WEIGHT_BUFFER_INDEX_OFFSET = 5
            /**
             * Offset to the palette matrix indices in a vertex entry
             */
            const val VERTEX_PALETTE_INDEX_OFFSET = 7 * FLOAT_SIZE
        }

        /**
         * The init block of our constructor. First we check that our arguments are within the bounds
         * dictated by the use of `Char` size index entries, and throw an IllegalArgumentException if
         * they are too large or negative. Then we initialize our field `mW` with our argument `w`
         * (the width in vertices of our `Grid`) and `mH` with our argument `h` (the height in
         * vertices of our `Grid`). We calculate the total number of vertices required to initialize
         * `Int` variable `val size` to be `w*h`, and initialize `ByteBuffer` field `mVertexByteBuffer`
         * with a `ByteBuffer` on the native heap that is large enough to hold that many vertices
         * and is in native byte order. We initialize `FloatBuffer` field `mVertexBuffer` to be a
         * `FloatBuffer` view of `mVertexByteBuffer`.
         *
         * Next we calculate the number of index entries `mIndexCount` required to divide our
         * `Grid` into triangles for `glDrawElements`. This is the quantity 6*(mW-1)(mH-1).
         * This calculation is based on the fact that the number of quadrilaterals in each direction
         * (`quadW` and `quadW`) is one less than the number of vertices in that direction,
         * the total number of quadrilaterals is `quadW*quadH`, there are 2 triangles needed for
         * each quadrilateral, and 3 vertices for each triangle hence 6 indices required for each
         * of the quadrilaterals.
         *
         * We use this count of required indices to allocate enough bytes on the native heap in native
         * byte order, which we use to initialize our `CharBuffer` field `mIndexBuffer` by viewing
         * that `ByteBuffer` as a `CharBuffer`.
         *
         * Our next step is to initialize the triangle list mesh that `mIndexBuffer` needs to
         * divide our `Grid` into triangles. To do this we loop from the "bottom" quadrilateral
         * to the "top" using the index `y`, and in an inner loop we loop from the "left" to the
         * "right" using the index `x`. Then for each of these quadrilaterals we calculate the
         * vertex index values for the quadrilateral:
         *
         *  * `a` (lower left corner) (y * mW + x)
         *  * `b` (lower right corner) (y * mW + x + 1)
         *  * `c` (upper left corner) ((y + 1) * mW + x)
         *  * `d` (upper right corner) ((y + 1) * mW + x + 1)
         *
         * We now use these 4 index values to define the two triangles required to produce the current
         * quadrilateral: (a, c, b) lower left to upper left to lower right (normal faces away from us),
         * and (b, c, d) lower right to upper left to upper right (normal faces away from us). These
         * six index values are added in order to our field `CharBuffer mIndexBuffer`.
         *
         * Parameter: w width of our `Grid` in vertices
         * Parameter: h height of our `Grid` in vertices
         */
        init {
            require(!(w < 0 || w >= 65536)) { "w" }
            require(!(h < 0 || h >= 65536)) { "h" }
            require(w * h < 65536) { "w * h >= 65536" }
            mW = w
            mH = h
            val size = w * h
            mVertexByteBuffer = ByteBuffer.allocateDirect(
                    VERTEX_SIZE * size
            ).order(ByteOrder.nativeOrder())
            mVertexBuffer = mVertexByteBuffer!!.asFloatBuffer()
            val quadW = mW - 1
            val quadH = mH - 1
            val quadCount = quadW * quadH
            val indexCount = quadCount * 6
            mIndexCount = indexCount
            mIndexBuffer = ByteBuffer.allocateDirect(CHAR_SIZE * indexCount).order(ByteOrder.nativeOrder()).asCharBuffer()
            /*
             * Initialize triangle list mesh. (Original comment got this diagram wrong (I think?))
             * the author confused View coordinates which start at 0 at the top and and increase down
             * the screen, with openGL coordinates which start with 0 at the bottom and increase up
             * the screen.
             *
             *     [w]-----[w + 1] ...
             *      | \      |
             *      |   \    |
             *      |     \  |
             *     [0]-----[  1] ...
             *      |        |
             *
             *     [c]------[d] ...
             *      | \      |
             *      |   \    |
             *      |     \  |
             *     [a]------[b] ...
             *      |       |
             */
            var i = 0
            for (y in 0 until quadH) {
                for (x in 0 until quadW) {
                    val a = (y * mW + x).toChar()
                    val b = (y * mW + x + 1).toChar()
                    val c = ((y + 1) * mW + x).toChar()
                    val d = ((y + 1) * mW + x + 1).toChar()
                    mIndexBuffer!!.put(i++, a)
                    mIndexBuffer!!.put(i++, c)
                    mIndexBuffer!!.put(i++, b)
                    mIndexBuffer!!.put(i++, b)
                    mIndexBuffer!!.put(i++, c)
                    mIndexBuffer!!.put(i++, d)
                }
            }
        }
    }

    /**
     * Called when the surface is created or recreated. Called when the rendering thread starts and
     * whenever the EGL context is lost. The EGL context will typically be lost when the Android
     * device awakes after going to sleep.
     *
     * First we disable the server-side GL capability GL_DITHER (color components or indices will not
     * be dithered before they are written to the color buffer). We specify the implementation-specific
     * hint GL_PERSPECTIVE_CORRECTION_HINT to be GL_FASTEST (Indicates the quality of color, texture
     * coordinate, and fog coordinate interpolation to be a simple linear interpolation of colors
     * and/or texture coordinates). We set the clear color to gray, the shade model to GL_SMOOTH
     * (causes the computed colors of vertices to be interpolated as the primitive is rasterized,
     * typically assigning different colors to each resulting pixel fragment). We enable the server
     * side GL capability GL_DEPTH_TEST (do depth comparisons and update the depth buffer) and the
     * server-side GL capability GL_TEXTURE_2D (if no fragment shader is active, two-dimensional
     * texturing is performed). Next we allocate [Int] array `val textures` to hold one int and ask
     * GL to generate one texture name in it, and we set our field [mTextureID] to that texture name,
     * then bind that named texture to the texturing target GL_TEXTURE_2D (while a texture is bound,
     * GL operations on the target to which it is bound affect the bound texture, and queries of the
     * target to which it is bound return state from the bound texture. If texture mapping is active
     * on the target to which a texture is bound, the bound texture is used. In effect, the texture
     * targets become aliases for the textures currently bound to them, and the texture name zero
     * refers to the default textures that were bound to them at initialization).
     *
     * Next we set texture parameter GL_TEXTURE_MIN_FILTER for GL_TEXTURE_2D to be GL_NEAREST (The
     * texture minifying function is used whenever the pixel being textured maps to an area greater
     * than one texture element. GL_NEAREST Returns the value of the texture element that is nearest
     * (in Manhattan distance) to the center of the pixel being textured). We set texture parameter
     * GL_TEXTURE_MAG_FILTER for GL_TEXTURE_2D to be GL_LINEAR (The GL_TEXTURE_MAG_FILTER function
     * is used when the pixel being textured maps to an area less than or equal to one texture
     * element. GL_LINEAR Returns the weighted average of the four texture elements that are closest
     * to the center of the pixel being textured).
     *
     * We set texture parameter GL_TEXTURE_WRAP_S for GL_TEXTURE_2D to be GL_CLAMP_TO_EDGE (Sets the
     * wrap parameter for texture coordinate *s* to GL_CLAMP_TO_EDGE which causes *s* coordinates to
     * be clamped to the range [1/2N, 1-1/2N] where N is the size of the texture in the direction of
     * clamping), and we set texture parameter GL_TEXTURE_WRAP_T for GL_TEXTURE_2D to be GL_CLAMP_TO_EDGE
     * as well. This has textures stop at the last pixel when you fall off the edge in either direction.
     *
     * We next set texture environment parameter GL_TEXTURE_ENV_MODE of GL_TEXTURE_ENV to GL_REPLACE
     * (causes the texture to replace whatever pixels were present).
     *
     * We open our raw resource file robot.png for reading by [InputStream] `val inputStream` and
     * decode the png into [Bitmap] `val bitmap` (with the code wrapped in an appropriate try block).
     * We then specify `bitmap` as a two-dimensional texture image for GL_TEXTURE_2D, and recycle
     * `bitmap`.
     *
     * Finally we initialize our [Grid] field [mGrid] with the [Grid] generated by our method
     * [generateWeightedGrid].
     *
     * @param gl     the [GL] interface.
     * @param config the [EGLConfig] of the created surface.
     */
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        /**
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER)
        /**
         * Some one-time OpenGL initialization can be made here
         * probably based on features of this particular context
         */
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST)
        gl.glClearColor(.5f, .5f, .5f, 1f)
        gl.glShadeModel(GL10.GL_SMOOTH)
        gl.glEnable(GL10.GL_DEPTH_TEST)
        gl.glEnable(GL10.GL_TEXTURE_2D)
        /**
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
        mGrid = generateWeightedGrid(gl)
    }

    /**
     * Called to draw the current frame. First we disable the server-side GL capability GL_DITHER.
     * Then we set texture environment parameter GL_TEXTURE_ENV_MODE of the GL_TEXTURE_ENV texture
     * environment to the GL_MODULATE texture function (multiplies color components). Next we clear
     * the color buffer and depth buffer, enable the server-side GL capability GL_DEPTH_TEST, and
     * the server-side GL capability GL_CULL_FACE.
     *
     * Now we are ready to draw, so we set the matrix mode to GL_MODELVIEW and load it with the
     * identity matrix, and then we define a viewing transformation with the position of the eye
     * point at (0,0,-5), the position of the reference point at (0,0,0), and the direction of the
     * up vector (0,1,0).
     *
     * We enable the client-side capability GL_VERTEX_ARRAY (the vertex array is enabled for writing
     * and used during rendering), and the client-side capability GL_TEXTURE_COORD_ARRAY (the texture
     * coordinate array is enabled for writing and used during rendering).
     *
     * We select active texture unit GL_TEXTURE0, and bind [mTextureID] to the texturing target
     * GL_TEXTURE_2D. We set the texture parameter GL_TEXTURE_WRAP_S of GL_TEXTURE_2D to GL_REPEAT
     * (Sets the wrap parameter for texture coordinate *s* to ignore the integer part of the *s*
     * coordinate and use only the fractional part, thereby creating a repeating pattern), and
     * GL_TEXTURE_WRAP_T to GL_REPEAT as well.
     *
     * We set [Long] variable `val time` to the number of milliseconds since boot modulo 4000, set
     * the [Double] variable `val animationUnit` to `time/4000` in order to calculate a value for
     * the [Float] variable `val unitAngle` which we multiply by 135.0 to get the current value for
     * the [Float] variable `val angle` which we will later use to build and apply a rotation matrix
     * to the GL_MATRIX_PALETTE_OES matrix (vertices will transformed by the matrix before they are
     * rendered, a "skinning effect". Skinning allows organic shapes (such as humans) to deform nicely
     * around joints as they bend. Without skinning, joints have a rigid appearance that is more
     * similar to a mechanical joint like you would see in a robot.)
     *
     * We enable the server side capability GL_MATRIX_PALETTE_OES (When this extension is utilized,
     * the enabled units transform each vertex by the modelview matrices specified by the vertices'
     * respective indices. These results are subsequently scaled by the weights of the respective
     * units and then summed to create the eye-space vertex i.e. it warps the vertex location before
     * they are rendered to the eye-space). We set the current matrix mode to GL_MATRIX_PALETTE_OES
     * (GL_MATRIX_PALETTE_OES matrix stack is the target for subsequent matrix operations), set the
     * current matrix palette to 0, and load the current palette matrix from the modelview matrix.
     * We then multiply the matrix by a rotation matrix of `angle` around the z axis.
     *
     * Next we set the current matrix palette to 1, and load the current palette matrix from the
     * modelview matrix. Each vertex contains a weight for the two palette matrices, with the vertices
     * at the bottom of the column giving less weight to palette 0 (the rotated model view) with the
     * rest given to palette 1 so palette 1 (the un-rotated matrix) has more influence at the bottom
     * and palette 0 (the rotated matrix) has more influence at the top causing the bottom to be
     * glued in place and the top to sway with the `angle` of rotation.
     *
     * Now that we have set up our texture and palette configuration we call the [Grid.draw] method
     * of [mGrid] to draw the vertices of [mGrid]. And finally we disable the server-side capability
     * GL_MATRIX_PALETTE_OES.
     *
     * @param gl the [GL10] interface.
     */
    override fun onDrawFrame(gl: GL10) {
        /**
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER)
        gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE)
        /**
         * Usually, the first thing one might want to do is to clear
         * the screen. The most efficient way of doing this is to use
         * glClear().
         */gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        gl.glEnable(GL10.GL_DEPTH_TEST)
        gl.glEnable(GL10.GL_CULL_FACE)
        /**
         * Now we're ready to draw some 3D objects
         */
        gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glLoadIdentity()
        GLU.gluLookAt(gl,
                0f, 0f, -5f,
                0f, 0f, 0f,
                0f, 1.0f, 0.0f
        )
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
        gl.glActiveTexture(GL10.GL_TEXTURE0)
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID)
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT)
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT)
        val time = SystemClock.uptimeMillis() % 4000L
        /**
         * Rock back and forth
         */
        val animationUnit = time.toDouble() / 4000
        val unitAngle = cos(animationUnit * 2 * Math.PI).toFloat()
        val angle = unitAngle * 135f
        gl.glEnable(GL11Ext.GL_MATRIX_PALETTE_OES)
        gl.glMatrixMode(GL11Ext.GL_MATRIX_PALETTE_OES)
        val gl11Ext = gl as GL11Ext
        /**
         * matrix 0: no transformation
         */
        gl11Ext.glCurrentPaletteMatrixOES(0)
        gl11Ext.glLoadPaletteFromModelViewMatrixOES()
        /**
         * matrix 1: rotate by "angle" NOTE: This comment is wrong(?), matrix 0 is rotated!
         */
        gl.glRotatef(angle, 0f, 0f, 1.0f)
        gl11Ext.glCurrentPaletteMatrixOES(1)
        gl11Ext.glLoadPaletteFromModelViewMatrixOES()
        mGrid!!.draw(gl)
        gl.glDisable(GL11Ext.GL_MATRIX_PALETTE_OES)
    }

    /**
     * Called when the surface changed size. Called after the surface is created and whenever the
     * OpenGL ES surface size changes. First we set the viewport to have the lower left corner at
     * (0,0), a width of [w] and a height of [h]. Then we calculate the aspect ratio `val ratio`
     * to be `w/h`, set the matrix mode to GL_PROJECTION (subsequent matrix operations will be
     * applied to the projection matrix stack), load it with the identity matrix, and apply a
     * perspective projection to that with the left and right vertical clipping planes at `-ratio`,
     * and `+ratio`, the bottom clipping plane at -1, the top clipping plane at +1, the near
     * clipping plane at 3 and the far clipping plane at 7.
     *
     * @param gl the [GL10] interface.
     * @param w  new width of the surface
     * @param h  new height of the surface
     */
    override fun onSurfaceChanged(gl: GL10, w: Int, h: Int) {
        gl.glViewport(0, 0, w, h)

        /**
         * Set our projection matrix. This doesn't have to be done
         * each time we draw, but usually a new projection needs to
         * be set when the viewport is resized.
         */
        val ratio = w.toFloat() / h
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()
        gl.glFrustumf(-ratio, ratio, -1f, 1f, 3f, 7f)
    }

    /**
     * Creates and configures a [Grid] instance of our swaying column. First we define the
     * constants used to size our [Grid]: `val uSteps` (number of "steps" in the *u* texture
     * coordinate space) and `val vSteps` (number of "steps" in the *v* texture coordinate space).
     * Then we initialize the `val radius` of our column to be 0.25 and the `val height` to be 2.
     *
     * Next we initialize [Grid] `val grid` to be a [Grid] allocated and indexed for the texture
     * space size required for our `uSteps` by `vSteps` column.
     *
     * Now we loop with the index `j` covering all of the *v* coordinates, and the inner loop with
     * the index `i` covering all of the *u* coordinates. We calculate the current `val angle`
     * around the circumference column we are to use to locate our vertex by dividing `2*PI` by
     * `uSteps` and multiplying this by the index `i` (the inner loop goes around the column). Then
     * we calculate the `x` coordinate of the vertex to be the `radius` times the cosine of
     * `angle`, the `y` coordinate of the vertex to be the `height` of the column
     * times the index `j` divided by the quantity `vSteps` minus 0.5 (the outer loop
     * goes from the bottom of the tower to the top). We calculate the `z` coordinate of the
     * vertex to be the `radius` times the sine of `angle`. The texture coordinate `val u`
     * is -4.0 time the `i` index divided by `uSteps` (the 2 dimensional texture wraps
     * around the tower), and the texture coordinate `val v` is -4.0 time the `j` index
     * divided by `vSteps` (the 2 dimensional texture runs up and down the tower).
     *
     * Now comes the fun part where we calculate the weights of our two palette matrices. `val w0`
     * (the weight of palette matrix 0) is assigned the value of the index `j` divided by the
     * number of `vSteps` (it runs from a weight of 0 at the bottom of the tower (no effect),
     * to 1.0 at the top of the tower (maximum effect), and `val w1` is just `1-w0` (it runs
     * from a weight of 1.0 at the bottom of the tower (maximum effect) to a weight of 0 at the top
     * of the tower (no effect). The last statement of our double loop calls the method `grid.set`
     * to store in the space reserved for the index `(i,j)` the location of the vertex (x,y,z),
     * its texture coordinate assignment (u,v), the weights of the two palette matrices `w0`
     * and `w1` and the indices of these matrices 0 and 1 (in kotlin this is done using matrix
     * notation -- cute hey?).
     *
     * When our loops have finished building our tower [Grid] `grid` we call the
     * [Grid.createBufferObjects] method of `grid.` to load the information contained in its
     * [Grid.mVertexBuffer] and [Grid.mIndexBuffer] into the openGL buffer object data store.
     *
     * And finally we return `grid` to our caller (the `onSurfaceCreated` method, which
     * stores it in [Grid] field [mGrid]).
     *
     * @param gl the [GL] interface.
     * @return A [Grid] ready to be drawn, with (x,y,z) vertex coordinates, (u,v) texture
     * coordinates, weights and indices of the two palette matrices used all loaded into the openGL
     * vertex buffer.
     */
    private fun generateWeightedGrid(gl: GL): Grid {
        val uSteps = 20
        val vSteps = 20
        val radius = 0.25f
        val height = 2.0f
        val grid = Grid(uSteps + 1, vSteps + 1)
        for (j in 0..vSteps) {
            for (i in 0..uSteps) {
                val angle = Math.PI * 2 * i / uSteps
                val x = radius * cos(angle).toFloat()
                val y = height * (j.toFloat() / vSteps - 0.5f)
                val z = radius * sin(angle).toFloat()
                val u = -4.0f * i.toFloat() / uSteps
                val v = -4.0f * j.toFloat() / vSteps
                val w0 = j.toFloat() / vSteps
                val w1 = 1.0f - w0
                grid[i, j, x, y, z, u, v, w0, w1, 0] = 1
            }
        }
        grid.createBufferObjects(gl)
        return grid
    }

}