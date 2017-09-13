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

package com.example.android.apis.graphics;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.SystemClock;

import com.example.android.apis.R;

/**
 * This renderer is part of the {@code MatrixPaletteActivity} sample which shows how to implement a
 * Matrix Palette, which is used to rock a column back and forth.
 */
@SuppressWarnings("WeakerAccess")
public class MatrixPaletteRenderer implements GLSurfaceView.Renderer {
    /**
     * Context used to retrieve resources, set in our constructor ("this" when called from the
     * {@code onCreate} method of {@code MatrixPaletteActivity}).
     */
    private Context mContext;
    /**
     * {@code Grid} containing our column vertices. It is created and configured by the method
     * {@code generateWeightedGrid} with all the data needed for drawing by the method {@code draw}
     * loaded into GPU vertex buffer objects.
     */
    private Grid mGrid;
    /**
     * Texture name that we use for the texture we use for GL_TEXTURE_2D, it is created and uploaded
     * to the GPU in the method {@code onSurfaceCreated} from the raw resource robot.png
     */
    private int mTextureID;

    /**
     * A grid is a topologically rectangular array of vertices. This grid class is customized for
     * the vertex data required for this example. The vertex and index data are held in VBO objects
     * because on most GPUs VBO objects are the fastest way of rendering static vertex and index
     * data.
     */
    private static class Grid {
        // Size of vertex data elements in bytes:
        /**
         * Number of bytes in a {@code float} value
         */
        final static int FLOAT_SIZE = 4;
        /**
         * Number of bytes in a {@code char} value
         */
        final static int CHAR_SIZE = 2;

        // Vertex structure:
        // float x, y, z;
        // float u, v;
        // float weight0, weight1;
        // byte palette0, palette1, pad0, pad1;

        /**
         * Total number of bytes for a complete vertex entry
         */
        final static int VERTEX_SIZE = 8 * FLOAT_SIZE;
        /**
         * Offset to the texture coordinates of a vertex entry
         */
        final static int VERTEX_TEXTURE_BUFFER_INDEX_OFFSET = 3;
        /**
         * Offset to the palette matrix weight part of the vertex entry
         */
        final static int VERTEX_WEIGHT_BUFFER_INDEX_OFFSET = 5;
        /**
         * Offset to the palette matrix indices in a vertex entry
         */
        final static int VERTEX_PALETTE_INDEX_OFFSET = 7 * FLOAT_SIZE;

        /**
         * Buffer object name for the GPU VBO we use for our vertex buffer
         */
        private int mVertexBufferObjectId;
        /**
         * Buffer object name for the GPU VBO we use for our the GL_ELEMENT_ARRAY_BUFFER indices into
         * our vertex buffer. Consists of the indices of the vertex buffer elements that are to be
         * used for the GL_TRIANGLES triangles that will be drawn by {@code glDrawElements}.
         */
        private int mElementBufferObjectId;

        // These buffers are used to hold the vertex and index data while
        // constructing the grid. Once createBufferObjects() is called
        // the buffers are nulled out to save memory.

        /**
         * {@code ByteBuffer} pointer for the vertex data, allows the vertex data buffer to be
         * accessed as {@code byte} values when "putting" the {@code byte} values for the palette
         * matrix indices.
         */
        private ByteBuffer mVertexByteBuffer;
        /**
         * {@code FloatBuffer} pointer for the vertex data, allows the vertex data to be accessed as
         * {@code float} values when "putting" {@code float} values into the vertex data buffer.
         */
        private FloatBuffer mVertexBuffer;
        /**
         * {@code CharBuffer} we build the indices of the GL_TRIANGLES triangles of our column,
         * which we later upload to the GL_ELEMENT_ARRAY_BUFFER GPU VBO for drawing .
         */
        private CharBuffer mIndexBuffer;

        /**
         * Width of our {@code Grid} in number of vertices, set in our constructor.
         */
        private int mW;
        /**
         * Height of our {@code Grid} in number of vertices, set in our constructor.
         */
        private int mH;
        /**
         * Number of indices in our {@code Grid} and {@code char} values in {@code mIndexBuffer},
         * used in the call to {@code glDrawElements}.
         */
        private int mIndexCount;

        /**
         * Our constructor. First we check that our arguments are within the bounds dictated by the
         * use of {@code char} size index entries, and throw an IllegalArgumentException if they are
         * too large or negative. Then we initialize our field {@code mW} with our argument {@code w}
         * (the width in vertices of our {@code Grid}) and {@code mH} with our argument {@code h}
         * (the height in vertices of our {@code Grid}). We calculate the total number of vertices
         * required {@code int size} to be {@code w*h}, and initialize {@code ByteBuffer mVertexByteBuffer}
         * with a {@code ByteBuffer} on the native heap that is large enough to hold that many vertices
         * and is in native byte order. We initialize {@code FloatBuffer mVertexBuffer} to be a
         * {@code FloatBuffer} view of {@code ByteBuffer mVertexByteBuffer}.
         * <p>
         * Next we calculate the number of index entries {@code mIndexCount} required to divide our
         * {@code Grid} into triangles for {@code glDrawElements}. This is the quantity 6*(mW-1)(mH-1).
         * This calculation is based on the fact that the number of quadrilaterals in each direction
         * ({@code quadW} and {@code quadW}) is one less than the number of vertices in that direction,
         * the total number of quadrilaterals is {@code quadW*quadH}, there are 2 triangles needed for
         * each quadrilateral, and 3 vertices for each triangle hence 6 indices required for each of
         * the quadrilaterals.
         * <p>
         * We use this count of required indices to allocate enough bytes on the native heap in native
         * byte order, which we use to initialize our field {@code CharBuffer mIndexBuffer} by viewing
         * that {@code ByteBuffer} as a {@code CharBuffer}.
         * <p>
         * Our next step is to initialize the triangle list mesh that {@code mIndexBuffer} needs to
         * divide our {@code Grid} into triangles. To do this we loop from the "bottom" quadrilateral
         * to the "top" using the index {@code y}, and in an inner loop we loop from the "left" to the
         * "right" using the index {@code x}. Then for each of these quadrilaterals we calculate the
         * vertex index values for the quadrilateral:
         * <ul>
         * <li>{@code a} (lower left corner) (y * mW + x)</li>
         * <li>{@code b} (lower right corner) (y * mW + x + 1)</li>
         * <li>{@code c} (upper left corner) ((y + 1) * mW + x)</li>
         * <li>{@code d} (upper right corner) ((y + 1) * mW + x + 1)</li>
         * </ul>
         * We now use these 4 index values to define the two triangles required to produce the current
         * quadrilateral: (a, c, b) lower left to upper left to lower right (normal faces away from us),
         * and (b, c, d) lower right to upper left to upper right (normal faces away from us). These
         * six index values are added in order to our field {@code CharBuffer mIndexBuffer}.
         *
         * @param w width of our {@code Grid} in vertices
         * @param h height of our {@code Grid} in vertices
         */
        @SuppressWarnings("WeakerAccess")
        public Grid(int w, int h) {
            if (w < 0 || w >= 65536) {
                throw new IllegalArgumentException("w");
            }
            if (h < 0 || h >= 65536) {
                throw new IllegalArgumentException("h");
            }
            if (w * h >= 65536) {
                throw new IllegalArgumentException("w * h >= 65536");
            }

            mW = w;
            mH = h;
            int size = w * h;

            mVertexByteBuffer = ByteBuffer.allocateDirect(VERTEX_SIZE * size).order(ByteOrder.nativeOrder());
            mVertexBuffer = mVertexByteBuffer.asFloatBuffer();

            int quadW = mW - 1;
            int quadH = mH - 1;
            int quadCount = quadW * quadH;
            int indexCount = quadCount * 6;
            mIndexCount = indexCount;
            mIndexBuffer = ByteBuffer.allocateDirect(CHAR_SIZE * indexCount).order(ByteOrder.nativeOrder()).asCharBuffer();

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

            {
                int i = 0;
                for (int y = 0; y < quadH; y++) {
                    for (int x = 0; x < quadW; x++) {
                        char a = (char) (y * mW + x);
                        char b = (char) (y * mW + x + 1);
                        char c = (char) ((y + 1) * mW + x);
                        char d = (char) ((y + 1) * mW + x + 1);

                        mIndexBuffer.put(i++, a);
                        mIndexBuffer.put(i++, c);
                        mIndexBuffer.put(i++, b);

                        mIndexBuffer.put(i++, b);
                        mIndexBuffer.put(i++, c);
                        mIndexBuffer.put(i++, d);
                    }
                }
            }

        }

        /**
         * Sets the values of a specific vertex in {@code FloatBuffer mVertexBuffer} (aka for byte
         * values: {@code ByteBuffer mVertexByteBuffer}). After making sure our input values are
         * within range and throwing IllegalArgumentException if they are not, we calculate the index
         * value {@code index} for the vertex in question, which is {@code (mW*j + i)} (the number of
         * vertices in a row times the row number (y index), plus the column number (x index). We use
         * {@code index} to calculate the correct offset into {@code FloatBuffer mVertexBuffer} (which
         * is {@code index*VERTEX_SIZE/FLOAT_SIZE}) and position {@code FloatBuffer mVertexBuffer}
         * to this offset. We then {@code put} the seven float parameters {@code x, y, z, u, v, w0, w1}
         * into {@code mVertexBuffer}. We then position {@code ByteBuffer mVertexByteBuffer} to the
         * correct position for our two byte parameters {@code p0, p1} of our vertex (which is
         * {@code index*VERTEX_SIZE + VERTEX_PALETTE_INDEX_OFFSET}) and put them into the buffer in
         * order.
         *
         * @param i  x index of the vertex to set
         * @param j  y index of the vertex to set
         * @param x  x coordinate of the vertex
         * @param y  y coordinate of the vertex
         * @param z  z coordinate of the vertex
         * @param u  x coordinate of the texture to use
         * @param v  y coordinate of the texture to use
         * @param w0 weight of palette matrix 0
         * @param w1 weight of palette matrix 1
         * @param p0 index of palette matrix that w0 refers to (always 0)
         * @param p1 index of palette matrix that w1 refers to (always 1)
         */
        public void set(int i, int j,
                        float x, float y, float z,
                        float u, float v,
                        float w0, float w1,
                        int p0, int p1) {

            if (i < 0 || i >= mW) {
                throw new IllegalArgumentException("i");
            }
            if (j < 0 || j >= mH) {
                throw new IllegalArgumentException("j");
            }

            if (w0 + w1 != 1.0f) {
                throw new IllegalArgumentException("Weights must add up to 1.0f");
            }

            int index = mW * j + i;

            mVertexBuffer.position(index * VERTEX_SIZE / FLOAT_SIZE);
            mVertexBuffer.put(x);
            mVertexBuffer.put(y);
            mVertexBuffer.put(z);
            mVertexBuffer.put(u);
            mVertexBuffer.put(v);
            mVertexBuffer.put(w0);
            mVertexBuffer.put(w1);

            mVertexByteBuffer.position(index * VERTEX_SIZE + VERTEX_PALETTE_INDEX_OFFSET);
            mVertexByteBuffer.put((byte) p0);
            mVertexByteBuffer.put((byte) p1);
        }

        /**
         * Uploads our two data buffers {@code ByteBuffer mVertexByteBuffer} (our vertex buffer) and
         * {@code CharBuffer mIndexBuffer} (our index buffer) to the proper openGL GPU VBOs. First
         * we allocate 2 ints for {@code int[] vboIds}, cast our argument {@code GL gl} to set
         * {@code GL11 gl11}, and use {@code gl11} to generate 2 buffer object names in {@code vboIds}.
         * We initialize our fields {@code int mVertexBufferObjectId} and {@code int mElementBufferObjectId}
         * with these to buffer object names.
         * <p>
         * We bind {@code mVertexBufferObjectId} to the target GL_ARRAY_BUFFER (The GL_ARRAY_BUFFER
         * target for buffer objects represents the intent to use that buffer object for vertex
         * attribute data), then we rewind {@code mVertexByteBuffer} and then we create and initialize
         * the GL_ARRAY_BUFFER buffer object's data store using {@code mVertexByteBuffer}, giving
         * openGL the hint GL_STATIC_DRAW (The user will be writing data to the buffer, but the user
         * will not read it, and The user will set the data once).
         * <p>
         * We bind {@code mElementBufferObjectId} to the target GL_ELEMENT_ARRAY_BUFFER (the
         * GL_ELEMENT_ARRAY_BUFFER contains the indices of each element in the GL_ARRAY_BUFFER buffer),
         * then we rewind {@code mIndexBuffer} and create and initialize the GL_ELEMENT_ARRAY_BUFFER
         * buffer object's data store using {@code mIndexBuffer}, giving openGL the hint GL_STATIC_DRAW
         * (as above).
         * <p>
         * In order to save memory we now null out {@code mVertexBuffer}, {@code mVertexByteBuffer},
         * and {@code mIndexBuffer} so their storage can be garbage collected.
         *
         * @param gl the GL interface.
         */
        @SuppressWarnings("WeakerAccess")
        public void createBufferObjects(GL gl) {
            // Generate a the vertex and element buffer IDs
            int[] vboIds = new int[2];
            GL11 gl11 = (GL11) gl;
            gl11.glGenBuffers(2, vboIds, 0);
            mVertexBufferObjectId = vboIds[0];
            mElementBufferObjectId = vboIds[1];

            // Upload the vertex data
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectId);
            mVertexByteBuffer.position(0);
            gl11.glBufferData(GL11.GL_ARRAY_BUFFER, mVertexByteBuffer.capacity(), mVertexByteBuffer, GL11.GL_STATIC_DRAW);

            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectId);
            mIndexBuffer.position(0);
            gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, mIndexBuffer.capacity() * CHAR_SIZE, mIndexBuffer, GL11.GL_STATIC_DRAW);

            // We don't need the in-memory data any more
            mVertexBuffer = null;
            mVertexByteBuffer = null;
            mIndexBuffer = null;
        }

        /**
         * Called from {@code onDrawFrame} issue the commands for the openGL GPU to draw our VBOs to
         * the {@code GLSurfaceView}.
         *
         * @param gl the GL interface.
         */
        public void draw(GL10 gl) {
            GL11 gl11 = (GL11) gl;
            GL11Ext gl11Ext = (GL11Ext) gl;

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectId);
            gl11.glVertexPointer(3, GL10.GL_FLOAT, VERTEX_SIZE, 0);
            gl11.glTexCoordPointer(2, GL10.GL_FLOAT, VERTEX_SIZE, VERTEX_TEXTURE_BUFFER_INDEX_OFFSET * FLOAT_SIZE);

            gl.glEnableClientState(GL11Ext.GL_MATRIX_INDEX_ARRAY_OES);
            gl.glEnableClientState(GL11Ext.GL_WEIGHT_ARRAY_OES);

            gl11Ext.glWeightPointerOES(2, GL10.GL_FLOAT, VERTEX_SIZE, VERTEX_WEIGHT_BUFFER_INDEX_OFFSET * FLOAT_SIZE);
            gl11Ext.glMatrixIndexPointerOES(2, GL10.GL_UNSIGNED_BYTE, VERTEX_SIZE, VERTEX_PALETTE_INDEX_OFFSET);

            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectId);
            gl11.glDrawElements(GL10.GL_TRIANGLES, mIndexCount, GL10.GL_UNSIGNED_SHORT, 0);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL11Ext.GL_MATRIX_INDEX_ARRAY_OES);
            gl.glDisableClientState(GL11Ext.GL_WEIGHT_ARRAY_OES);
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public MatrixPaletteRenderer(Context context) {
        mContext = context;
    }

    /**
     * Called when the surface is created or recreated. Called when the rendering thread starts and
     * whenever the EGL context is lost. The EGL context will typically be lost when the Android
     * device awakes after going to sleep.
     * <p>
     * First we disable the server-side GL capability GL_DITHER (color components or indices will not
     * be dithered before they are written to the color buffer). We specify the implementation-specific
     * hint GL_PERSPECTIVE_CORRECTION_HINT to be GL_FASTEST (Indicates the quality of color, texture
     * coordinate, and fog coordinate interpolation to be a simple linear interpolation of colors
     * and/or texture coordinates). We set the clear color to gray, the shade model to GL_SMOOTH
     * (causes the computed colors of vertices to be interpolated as the primitive is rasterized,
     * typically assigning different colors to each resulting pixel fragment). We enable the server
     * side GL capability GL_DEPTH_TEST (do depth comparisons and update the depth buffer) and the
     * server-side GL capability GL_TEXTURE_2D (if no fragment shader is active, two-dimensional
     * texturing is performed). Next we allocate {@code int[] textures} and ask GL to generate 1
     * texture name in it, and we set our field {@code mTextureID} to that texture name, then bind
     * that named texture to the texturing target GL_TEXTURE_2D (while a texture is bound, GL
     * operations on the target to which it is bound affect the bound texture, and queries of the
     * target to which it is bound return state from the bound texture. If texture mapping is active
     * on the target to which a texture is bound, the bound texture is used. In effect, the texture
     * targets become aliases for the textures currently bound to them, and the texture name zero
     * refers to the default textures that were bound to them at initialization).
     * <p>
     * Next we set texture parameter GL_TEXTURE_MIN_FILTER for GL_TEXTURE_2D to be GL_NEAREST (The
     * texture minifying function is used whenever the pixel being textured maps to an area greater
     * than one texture element. GL_NEAREST Returns the value of the texture element that is nearest
     * (in Manhattan distance) to the center of the pixel being textured). We set texture parameter
     * GL_TEXTURE_MAG_FILTER for GL_TEXTURE_2D to be GL_LINEAR (The GL_TEXTURE_MAG_FILTER function
     * is used when the pixel being textured maps to an area less than or equal to one texture
     * element. GL_LINEAR Returns the weighted average of the four texture elements that are closest
     * to the center of the pixel being textured).
     * <p>
     * We set texture parameter GL_TEXTURE_WRAP_S for GL_TEXTURE_2D to be GL_CLAMP_TO_EDGE (Sets the
     * wrap parameter for texture coordinate s to GL_CLAMP_TO_EDGE which causes s coordinates to be
     * clamped to the range [1/2N, 1-1/2N] where N is the size of the texture in the direction of
     * clamping), and we set texture parameter GL_TEXTURE_WRAP_T for GL_TEXTURE_2D to be GL_CLAMP_TO_EDGE
     * as well. This has textures stop at the last pixel when you fall off the edge in either direction.
     * <p>
     * We next set texture environment parameter GL_TEXTURE_ENV_MODE of GL_TEXTURE_ENV to GL_REPLACE
     * (causes the texture to replace whatever pixels were present).
     * <p>
     * We open our raw resource file robot.png for reading by {@code InputStream is} and decode the
     * png into {@code Bitmap bitmap} (with the code wrapped in an appropriate try block). We then
     * specify {@code bitmap} as a two-dimensional texture image for GL_TEXTURE_2D, and recycle
     * {@code bitmap}.
     * <p>
     * Finally we initialize our field {@code Grid mGrid} with the {@code Grid} generated by our
     * method {@code generateWeightedGrid}.
     *
     * @param gl     the GL interface.
     * @param config the EGLConfig of the created surface.
     */
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER);

        /*
         * Some one-time OpenGL initialization can be made here
         * probably based on features of this particular context
         */
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

        gl.glClearColor(.5f, .5f, .5f, 1);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glEnable(GL10.GL_TEXTURE_2D);

        /*
         * Create our texture. This has to be done each time the
         * surface is created.
         */

        int[] textures = new int[1];
        gl.glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);

        InputStream is = mContext.getResources().openRawResource(R.raw.robot);
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Ignore.
            }
        }

        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

        mGrid = generateWeightedGrid(gl);
    }

    /**
     * Called to draw the current frame. First we disable the server-side GL capability GL_DITHER.
     * Then we set texture environment parameter GL_TEXTURE_ENV_MODE of the GL_TEXTURE_ENV texture
     * environment to the GL_MODULATE texture function (multiplies color components). Next we clear
     * the color buffer and depth buffer, enable the server-side GL capability GL_DEPTH_TEST, and
     * the server-side GL capability GL_CULL_FACE.
     * <p>
     * Now we are ready to draw, so we set the matrix mode to GL_MODELVIEW and load it with the
     * identity matrix, and then we define a viewing transformation with the position of the eye
     * point at (0,0,-5), the position of the reference point at (0,0,0), and the direction of the
     * up vector (0,1,0).
     * <p>
     * We enable the client-side capability GL_VERTEX_ARRAY (the vertex array is enabled for writing
     * and used during rendering), and the client-side capability GL_TEXTURE_COORD_ARRAY (the texture
     * coordinate array is enabled for writing and used during rendering).
     * <p>
     * We select active texture unit GL_TEXTURE0, and bind {@code mTextureID} to the texturing target
     * GL_TEXTURE_2D. We set the texture parameter GL_TEXTURE_WRAP_S of GL_TEXTURE_2D to GL_REPEAT
     * (Sets the wrap parameter for texture coordinate s to ignore the integer part of the s coordinate
     * and use only the fractional part, thereby creating a repeating pattern), and GL_TEXTURE_WRAP_T
     * to GL_REPEAT as well.
     * <p>
     * We set {@code long time} to the number of milliseconds since boot modulo 4000, set the variable
     * {@code double animationUnit} {@code time/4000} in order to calculate a value for the variable
     * {@code float unitAngle} which we multiply by 135.0 to get the current value for the variable
     * {@code float angle} which we will later use to build and apply a rotation matrix to the
     * GL_MATRIX_PALETTE_OES matrix (vertices will transformed by the matrix before they are rendered,
     * a "skinning effect")
     * <p>
     * We enable the server side capability GL_MATRIX_PALETTE_OES (When this extension is utilized,
     * the enabled units transform each vertex by the modelview matrices specified by the vertices'
     * respective indices. These results are subsequently scaled by the weights of the respective
     * units and then summed to create the eye-space vertex i.e. it warps the vertex location before
     * they are rendered to the eye-space). We set the current matrix mode to GL_MATRIX_PALETTE_OES
     * (GL_MATRIX_PALETTE_OES matrix stack is the target for subsequent matrix operations), set the
     * current matrix palette to 0, and load the current palette matrix from the modelview matrix.
     * We then multiply the matrix by a rotation matrix of {@code angle} around the z axis.
     * <p>
     * Next we set the current matrix palette to 1, and load the current palette matrix from the
     * modelview matrix. Each vertex contains a weight for the two palette matrices, with the vertices
     * at the bottom of the column giving less weight to palette 0 (the rotated model view) with the
     * rest given to palette 1 so palette 1 (the un-rotated matrix) has more influence at the bottom
     * and palette 0 (the rotated matrix) has more influence at the top causing the bottom to be
     * glued in place and the top to sway with the {@code angle} of rotation.
     * <p>
     * Now that we have set up our texture and palette configuration we call {@code mGrid.draw} to
     * draw the vertices of the {@code Grid mGrid}. And finally we disable the server-side capability
     * GL_MATRIX_PALETTE_OES.
     *
     * @param gl the GL interface.
     */
    public void onDrawFrame(GL10 gl) {
        /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER);

        gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);

        /*
         * Usually, the first thing one might want to do is to clear
         * the screen. The most efficient way of doing this is to use
         * glClear().
         */

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glEnable(GL10.GL_DEPTH_TEST);

        gl.glEnable(GL10.GL_CULL_FACE);

        /*
         * Now we're ready to draw some 3D objects
         */

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        GLU.gluLookAt(gl, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glActiveTexture(GL10.GL_TEXTURE0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);

        long time = SystemClock.uptimeMillis() % 4000L;

        // Rock back and forth
        double animationUnit = ((double) time) / 4000;
        float unitAngle = (float) Math.cos(animationUnit * 2 * Math.PI);
        float angle = unitAngle * 135f;

        gl.glEnable(GL11Ext.GL_MATRIX_PALETTE_OES);
        gl.glMatrixMode(GL11Ext.GL_MATRIX_PALETTE_OES);

        GL11Ext gl11Ext = (GL11Ext) gl;

        // matrix 0: no transformation
        gl11Ext.glCurrentPaletteMatrixOES(0);
        gl11Ext.glLoadPaletteFromModelViewMatrixOES();

        // matrix 1: rotate by "angle" NOTE: This comment is wrong, matrix 0 is rotated!
        gl.glRotatef(angle, 0, 0, 1.0f);

        gl11Ext.glCurrentPaletteMatrixOES(1);
        gl11Ext.glLoadPaletteFromModelViewMatrixOES();

        mGrid.draw(gl);

        gl.glDisable(GL11Ext.GL_MATRIX_PALETTE_OES);
    }

    /**
     * Called when the surface changed size. Called after the surface is created and whenever the
     * OpenGL ES surface size changes. First we set the viewport to have the lower left corner at
     * (0,0), a width of {@code width} and a height of {@code height}. Then we calculate the aspect
     * ratio {@code ratio} w/h, set the matrix mode to GL_PROJECTION (subsequent matrix operations
     * will be applied to the projection matrix stack), load it with the identity matrix, and apply
     * a perspective projection to that with the left and right vertical clipping planes at
     * {@code -ratio}, and {@code +ratio}, the bottom clipping plane at -1, the top clipping plane
     * at +1, the near clipping plane at 3 and the far clipping plane at 7.
     *
     * @param gl the GL interface.
     * @param w  new width of the surface
     * @param h  new height of the surface
     */
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        gl.glViewport(0, 0, w, h);

        /*
        * Set our projection matrix. This doesn't have to be done
        * each time we draw, but usually a new projection needs to
        * be set when the viewport is resized.
        */

        float ratio = (float) w / h;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);
    }

    /**
     * Creates and configures a {@code Grid} instance of our swaying column. First we define the
     * constants used to size our {@code Grid}: {@code uSteps} (number of "steps" in the u texture
     * coordinate space) and {@code vSteps} (number of "steps" in the v texture coordinate space).
     * Then we initialize the {@code radius} of our column to be 0.25 and the {@code height} to be 2.
     * <p>
     * Next we initialize {@code Grid grid} to be a {@code Grid} allocated and indexed for the texture
     * space size required for our {@code uSteps} by {@code vSteps} column.
     * <p>
     * Now we loop with the index {@code j} covering all of the v coordinates, and the inner loop with
     * the index {@code i} covering all of the u coordinates. We calculate the current {@code angle}
     * of the column we are to use to locate our vertex by dividing 2*pi by {@code uSteps} and
     * multiplying this by the index {@code i} (the inner loop goes around the column). Then we
     * calculate the {@code x} coordinate of the vertex to be the {@code radius} times the cosine of
     * {@code angle}, the {@code y} coordinate of the vertex to be the {@code height} of the column
     * times the index {@code j} divided by the quantity {@code vSteps} minus 0.5 (the outer loop
     * goes from the bottom of the tower to the top). We calculate the {@code z} coordinate of the
     * vertex to be the {@code radius} times the sine of {@code angle}. The texture coordinate {@code u}
     * is -4.0 time the {@code i} index divided by {@code uSteps} (the 2 dimensional texture wraps
     * around the tower), and the texture coordinate {@code v} is -4.0 time the {@code j} index
     * divided by {@code vSteps} (the 2 dimensional texture runs up and down the tower).
     * <p>
     * Now comes the fun part where we calculate the weights of our two palette matrices. {@code w0}
     * (the weight of palette matrix 0) is assigned the value of the index {@code j} divided by the
     * number of {@code vSteps} (it runs from a weight of 0 at the bottom of the tower (no effect),
     * to 1.0 at the top of the tower (maximum effect), and {@code w1} is just {@code 1-w0} (it runs
     * from a weight of 1.0 at the bottom of the tower (maximum effect) to a weight of 0 at the top
     * of the tower (no effect). The last statement of our double loop calls the method {@code grid.set}
     * to store in the space reserved for the index {@code (i,j)} the location of the vertex (x,y,z),
     * its texture coordinate assignment (u,v), the weights of the two palette matrices {@code w0}
     * and {@code w1} and the indices of these matrices 0 and 1.
     * <p>
     * When our loops have finished building our tower {@code Grid grid} we call the method
     * {@code grid.createBufferObjects} to load the information contained in its {@code mVertexBuffer}
     * and {@code mIndexBuffer} into the openGL buffer object data store.
     * <p>
     * And finally we return {@code grid} to our caller (the {@code onSurfaceCreated} method, which
     * stores it in {@code Grid mGrid}).
     *
     * @param gl the GL interface.
     * @return A {@code Grid} ready to be drawn, with (x,y,z) vertex coordinates, (u,v) texture
     * coordinates, weights and indices of the two palette matrices used all loaded into the openGL
     * vertex buffer.
     */
    private Grid generateWeightedGrid(GL gl) {
        final int uSteps = 20;
        final int vSteps = 20;

        float radius = 0.25f;
        float height = 2.0f;
        Grid grid = new Grid(uSteps + 1, vSteps + 1);

        for (int j = 0; j <= vSteps; j++) {
            for (int i = 0; i <= uSteps; i++) {
                double angle = Math.PI * 2 * i / uSteps;
                float x = radius * (float) Math.cos(angle);
                float y = height * ((float) j / vSteps - 0.5f);
                float z = radius * (float) Math.sin(angle);
                float u = -4.0f * (float) i / uSteps;
                float v = -4.0f * (float) j / vSteps;
                float w0 = (float) j / vSteps;
                float w1 = 1.0f - w0;
                grid.set(i, j, x, y, z, u, v, w0, w1, 0, 1);
            }
        }

        grid.createBufferObjects(gl);
        return grid;
    }
}
