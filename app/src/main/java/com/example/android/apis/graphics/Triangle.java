package com.example.android.apis.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Draws a triangle, used both by {@code TriangleRenderer} and {@code FrameBufferObjectActivity}.
 */
public class Triangle {
    /**
     * Number of vertices in our triangle
     */
    private final static int VERTS = 3;

    /**
     * Vertex Buffer loaded with (x,y,z) coordinates of our triangle
     */
    private FloatBuffer mFVertexBuffer;
    /**
     * Texture buffer loaded with (x,y,z) coordinates of our triangle offset by 0.5 so that
     * the texture is centered.
     */
    private FloatBuffer mTexBuffer;
    /**
     * Indices of the triangle (0,1,2) in counter clockwise order so that the normal points towards us
     */
    private ShortBuffer mIndexBuffer;

    /**
     * Constructs our {@code Triangle} instance by allocating and initializing our fields. First we
     * allocate {@code ByteBuffer vbb} on the native heap, set its byte order to native byte order
     * and initialize {@code FloatBuffer mFVertexBuffer} with a view of {@code vbb} as a float buffer.
     * We allocate {@code ByteBuffer tbb} on the native heap, set its byte order to native byte order
     * and initialize {@code FloatBuffer mTexBuffer} with a view of {@code tbb} as a float buffer.
     * We allocate {@code ByteBuffer ibb} on the native heap, set its byte order to native byte order
     * and initialize {@code ShortBuffer mIndexBuffer} with a view of {@code tbb} as a short buffer.
     * <p>
     * We define the contents of {@code float[] coords} to be the coordinates of a unit-sided
     * equilateral triangle centered on the origin. Then we load the 9 entries in {@code coords[]}
     * multiplied by 2.0 into {@code FloatBuffer mFVertexBuffer}, and the (x,y) values only
     * multiplied by 2.0 and offset by 0.5 into {@code FloatBuffer mTexBuffer}. We load
     * {@code ShortBuffer mIndexBuffer} with the three indices 0, 1, and 2.
     * <p>
     * Finally we position {@code mFVertexBuffer}, {@code mTexBuffer}, and {@code mIndexBuffer} to
     * their beginning entry ready for use.
     */
    public Triangle() {

        // Buffers to be passed to gl*Pointer() functions
        // must be direct, i.e., they must be placed on the
        // native heap where the garbage collector cannot
        // move them.
        //
        // Buffers with multi-byte data types (e.g., short, int, float)
        // must have their byte order set to native order

        ByteBuffer vbb = ByteBuffer.allocateDirect(VERTS * 3 * 4);
        vbb.order(ByteOrder.nativeOrder());
        mFVertexBuffer = vbb.asFloatBuffer();

        ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
        tbb.order(ByteOrder.nativeOrder());
        mTexBuffer = tbb.asFloatBuffer();

        ByteBuffer ibb = ByteBuffer.allocateDirect(VERTS * 2);
        ibb.order(ByteOrder.nativeOrder());
        mIndexBuffer = ibb.asShortBuffer();

        // A unit-sided equilateral triangle centered on the origin.
        float[] coords = {
                // X, Y, Z
                -0.5f, -0.25f, 0,
                0.5f, -0.25f, 0,
                0.0f, 0.559016994f, 0
        };

        for (int i = 0; i < VERTS; i++) {
            for (int j = 0; j < 3; j++) {
                mFVertexBuffer.put(coords[i * 3 + j] * 2.0f);
            }
        }

        for (int i = 0; i < VERTS; i++) {
            for (int j = 0; j < 2; j++) {
                mTexBuffer.put(coords[i * 3 + j] * 2.0f + 0.5f);
            }
        }

        for (int i = 0; i < VERTS; i++) {
            mIndexBuffer.put((short) i);
        }

        mFVertexBuffer.position(0);
        mTexBuffer.position(0);
        mIndexBuffer.position(0);
    }

    /**
     * Draws our triangle. First we specify the orientation of front-facing polygons to be GL_CCW
     * (selects counterclockwise polygons as front-facing). We next define {@code mFVertexBuffer} to
     * be our array of vertex data, with 3 coordinates per vertex, using GL_FLOAT as its data type,
     * and a stride of 0. We enable the server-side GL capability GL_TEXTURE_2D (two-dimensional
     * texturing is performed), then define {@code mTexBuffer} to be our array of texture coordinates,
     * with 2 coordinates per point, using GL_FLOAT as its data type, and a stride of 0.
     * <p>
     * Finally we call {@code glDrawElements} to render primitives from array data specifying
     * {@code ShortBuffer mIndexBuffer} as our indices array, GL_UNSIGNED_SHORT as its data type,
     * and GL_TRIANGLE_STRIP as the type of primitive to render (Every group of 3 adjacent vertices
     * forms a triangle - we have only one triangle).
     *
     * @param gl the GL interface.
     */
    public void draw(GL10 gl) {
        gl.glFrontFace(GL10.GL_CCW);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, VERTS, GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
    }
}
