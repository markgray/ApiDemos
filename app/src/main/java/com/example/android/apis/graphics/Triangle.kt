package com.example.android.apis.graphics

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.opengles.GL10

/**
 * Draws a triangle, used both by `TriangleRenderer` (which is used by `GLES20Activity`) and
 * `FrameBufferObjectActivity`.
 */
class Triangle {
    /**
     * Vertex Buffer loaded with (x,y,z) coordinates of our triangle
     */
    private val mFVertexBuffer: FloatBuffer
    /**
     * Texture buffer loaded with (x,y,z) coordinates of our triangle offset by 0.5 so that
     * the texture is centered.
     */
    private val mTexBuffer: FloatBuffer
    /**
     * Indices of the triangle (0,1,2) in counter clockwise order so that the normal points towards us
     */
    private val mIndexBuffer: ShortBuffer

    /**
     * Draws our triangle. First we specify the orientation of front-facing polygons to be GL_CCW
     * (selects counterclockwise polygons as front-facing). We next define `mFVertexBuffer` to
     * be our array of vertex data, with 3 coordinates per vertex, using GL_FLOAT as its data type,
     * and a stride of 0. We enable the server-side GL capability GL_TEXTURE_2D (two-dimensional
     * texturing is performed), then define `mTexBuffer` to be our array of texture coordinates,
     * with 2 coordinates per point, using GL_FLOAT as its data type, and a stride of 0.
     *
     *
     * Finally we call `glDrawElements` to render primitives from array data specifying
     * `ShortBuffer mIndexBuffer` as our indices array, GL_UNSIGNED_SHORT as its data type,
     * and GL_TRIANGLE_STRIP as the type of primitive to render (Every group of 3 adjacent vertices
     * forms a triangle - we have only one triangle).
     *
     * @param gl the GL interface.
     */
    fun draw(gl: GL10) {
        gl.glFrontFace(GL10.GL_CCW)
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer)
        gl.glEnable(GL10.GL_TEXTURE_2D)
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer)
        gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, VERTS, GL10.GL_UNSIGNED_SHORT, mIndexBuffer)
    }

    companion object {
        /**
         * Number of vertices in our triangle
         */
        private const val VERTS = 3
    }

    /**
     * Constructs our `Triangle` instance by allocating and initializing our fields. First we
     * allocate `ByteBuffer vbb` on the native heap, set its byte order to native byte order
     * and initialize `FloatBuffer mFVertexBuffer` with a view of `vbb` as a float buffer.
     * We allocate `ByteBuffer tbb` on the native heap, set its byte order to native byte order
     * and initialize `FloatBuffer mTexBuffer` with a view of `tbb` as a float buffer.
     * We allocate `ByteBuffer ibb` on the native heap, set its byte order to native byte order
     * and initialize `ShortBuffer mIndexBuffer` with a view of `tbb` as a short buffer.
     *
     *
     * We define the contents of `float[] coords` to be the coordinates of a unit-sided
     * equilateral triangle centered on the origin. Then we load the 9 entries in `coords[]`
     * multiplied by 2.0 into `FloatBuffer mFVertexBuffer`, and the (x,y) values only
     * multiplied by 2.0 and offset by 0.5 into `FloatBuffer mTexBuffer`. We load
     * `ShortBuffer mIndexBuffer` with the three indices 0, 1, and 2.
     *
     *
     * Finally we position `mFVertexBuffer`, `mTexBuffer`, and `mIndexBuffer` to
     * their beginning entry ready for use.
     */
    init { // Buffers to be passed to gl*Pointer() functions
// must be direct, i.e., they must be placed on the
// native heap where the garbage collector cannot
// move them.
//
// Buffers with multi-byte data types (e.g., short, int, float)
// must have their byte order set to native order
        val vbb = ByteBuffer.allocateDirect(VERTS * 3 * 4)
        vbb.order(ByteOrder.nativeOrder())
        mFVertexBuffer = vbb.asFloatBuffer()
        val tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4)
        tbb.order(ByteOrder.nativeOrder())
        mTexBuffer = tbb.asFloatBuffer()
        val ibb = ByteBuffer.allocateDirect(VERTS * 2)
        ibb.order(ByteOrder.nativeOrder())
        mIndexBuffer = ibb.asShortBuffer()
        // A unit-sided equilateral triangle centered on the origin.
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