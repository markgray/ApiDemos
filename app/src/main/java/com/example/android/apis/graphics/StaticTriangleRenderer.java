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

package com.example.android.apis.graphics;

import static android.opengl.GLES10.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.SystemClock;

import com.example.android.apis.R;

/**
 * A GLSurfaceView.Renderer that uses the Android-specific android.opengl.GLESXXX
 * static OpenGL ES APIs. The static APIs expose more of the OpenGL ES features than
 * the javax.microedition.khronos.opengles APIs, and also provide a programming model
 * that is closer to the C OpenGL ES APIs, which may make it easier to reuse code and
 * documentation written for the C OpenGL ES APIs.
 */
@SuppressWarnings("WeakerAccess")
public class StaticTriangleRenderer implements GLSurfaceView.Renderer {

    /**
     * Context used to retrieve resources, set in {@code init} method from one of its parameters. The
     * method {@code init} is called from both our {@code StaticTriangleRenderer} constructors and is
     * "this" {@code TriangleActivity} when called from the {@code onCreate} override of the
     * {@code TriangleActivity} demo, and "this" {@code CompressedTextureActivity} when called from
     * the {@code onCreate} override of the {@code CompressedTextureActivity} demo.
     */
    private Context mContext;
    /**
     * Set to a new instance of {@code Triangle} in our {@code init} method, whose {@code draw} method
     * is called to draw the triangular shape we are animating.
     */
    private Triangle mTriangle;
    /**
     * The texture ID of the texture we are using, it is bound to the texturing target GL_TEXTURE_2D,
     * and all code uses GL_TEXTURE_2D to reference it.
     */
    private int mTextureID;
    /**
     * {@code TextureLoader} whose {@code load} method is called to load the texture image we are to
     * use, it is set to one of the parameters to our method {@code init}. It is our own default
     * {@code RobotTextureLoader} as the {@code TextureLoader} when we are used by the activity
     * {@code TriangleActivity}, and depending on the compile time switch TEST_CREATE_TEXTURE either
     * {@code CompressedTextureLoader}, or {@code SyntheticCompressedTextureLoader} when we are used
     * by the activity {@code CompressedTextureActivity}
     */
    private TextureLoader mTextureLoader;

    /**
     * Our constructor needs to be provided with an implementation of this. We call its {@code load}
     * in our {@code onSurfaceCreated} callback to load the appropriate texture into our OpenGL
     * context.
     */
    public interface TextureLoader {
        /**
         * Load a texture into the currently bound OpenGL texture.
         *
         * @param gl OpenGL interface UNUSED
         */
        void load(GL10 gl);
    }

    /**
     * Constructor that uses our own default {@code RobotTextureLoader} as the {@code TextureLoader},
     * (which loads R.raw.robot image as our texture). It is used in {@code TriangleActivity}. We
     * simply call our method {@code init} with our parameter {@code Context context} and a new
     * instance of {@code RobotTextureLoader} to use as its {@code CompressedTextureActivity}.
     *
     * @param context Context to use for resources, "this" {@code TriangleActivity} when called from
     *                the {@code onCreate} override of the {@code TriangleActivity} demo
     */
    public StaticTriangleRenderer(Context context) {
        init(context, new RobotTextureLoader());
    }

    /**
     * Constructor that uses {@code TextureLoader loader} as its {@code TextureLoader}. It is used
     * in {@code CompressedTextureActivity}. We simply call our method {@code init} with our parameter
     * {@code Context context} to use for the context and {@code TextureLoader loader} to use as the
     * {@code TextureLoader}.
     *
     * @param context Context to use for resources, "this" {@code CompressedTextureActivity} when
     *                called from the {@code onCreate} override of the {@code CompressedTextureActivity}
     *                demo.
     * @param loader  Class implementing the {@code TextureLoader} interface
     */
    public StaticTriangleRenderer(Context context, TextureLoader loader) {
        init(context, loader);
    }

    private void init(Context context, TextureLoader loader) {
        mContext = context;
        mTriangle = new Triangle();
        mTextureLoader = loader;
    }

    /**
     * Called when the surface is created or recreated. Called when the rendering thread starts and
     * whenever the EGL context is lost. The EGL context will typically be lost when the Android
     * device awakes after going to sleep.
     * <p>
     * First we disable dithering, then we use the method {@code glHint} to set the implementation
     * specific hint GL_PERSPECTIVE_CORRECTION_HINT to GL_FASTEST. We set the red, green, blue, and
     * alpha values used when the color buffers are cleared to (0.5,0.5,0.5,1.0) (GRAY), set the
     * shade model to GL_SMOOTH (causes the computed colors of vertices to be interpolated as the
     * primitive is rasterized, typically assigning different colors to each resulting pixel), we
     * enable GL_DEPTH_TEST (the depth value from the fragment being rendered is compared to the
     * depth value from the matching sample currently in the framebuffer and if it fails it is
     * discarded), and we enable GL_TEXTURE_2D (Images in our texture all are 2-dimensional. They
     * have width and height, but no depth).
     * <p>
     * Now we create our texture. First we generate 1 texture name in our array {@code int[] textures},
     * and we store the texture name created in our field {@code int mTextureID}, then we bind that
     * texture to the texturing target GL_TEXTURE_2D (While a texture is bound, GL operations on the
     * target to which it is bound affect the bound texture, and queries of the target to which it
     * is bound return state from the bound texture. In effect, the texture targets become aliases
     * for the textures currently bound to them, and the texture name zero refers to the default
     * textures that were bound to them at initialization.)
     * <p>
     * Now we set the texture parameters for GL_TEXTURE_2D:
     * <ul>
     * <li>
     * GL_TEXTURE_MIN_FILTER set to GL_NEAREST (The texture minifying function used whenever
     * the level-of-detail function used when sampling from the texture determines that the
     * texture should be minified: GL_NEAREST Returns the value of the texture element that
     * is nearest (in Manhattan distance) to the specified texture coordinates.)
     * </li>
     * <li>
     * GL_TEXTURE_MAG_FILTER set to GL_LINEAR (The texture magnification function is used
     * whenever the level-of-detail function used when sampling from the texture determines
     * that the texture should be magnified: GL_LINEAR Returns the weighted average of the
     * texture elements that are closest to the specified texture coordinates.)
     * </li>
     * <li>
     * GL_TEXTURE_WRAP_S set to GL_CLAMP_TO_EDGE (Sets the wrap parameter for texture coordinate s to
     * GL_CLAMP_TO_EDGE: causes s coordinates to be clamped to the range [1/2N,1−1/2N], where N is
     * the size of the texture in the direction of clamping)
     * </li>
     * <li>
     * GL_TEXTURE_WRAP_T set to GL_CLAMP_TO_EDGE (Sets the wrap parameter for texture coordinate t to
     * GL_CLAMP_TO_EDGE: see the discussion under GL_TEXTURE_WRAP_S)
     * </li>
     * </ul>
     * Then we set the target texture environment GL_TEXTURE_ENV texture environment parameter
     * GL_TEXTURE_ENV_MODE to the texture function GL_REPLACE. Finally we call the {@code load}
     * method of our field {@code TextureLoader mTextureLoader} to load the texture image.
     *
     * @param gl     the GL interface. Use <code>instanceof</code> to
     *               test if the interface supports GL11 or higher interfaces.
     * @param config the EGLConfig of the created surface. Can be used
     *               to create matching pbuffers.
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        glDisable(GL_DITHER);

        /*
         * Some one-time OpenGL initialization can be made here
         * probably based on features of this particular context
         */
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_FASTEST);

        glClearColor(.5f, .5f, .5f, 1);
        glShadeModel(GL_SMOOTH);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);

        /*
         * Create our texture. This has to be done each time the
         * surface is created.
         */

        int[] textures = new int[1];
        glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        glBindTexture(GL_TEXTURE_2D, mTextureID);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
        mTextureLoader.load(gl);
    }

    /**
     * Called to draw the current frame. First we disable dithering, then we set the target texture
     * environment GL_TEXTURE_ENV texture environment parameter GL_TEXTURE_ENV_MODE to the texture
     * function GL_MODULATE, and then we clear the screen by calling {@code glClear} to clear both
     * the color buffer (GL_COLOR_BUFFER_BIT) and depth buffer (GL_DEPTH_BUFFER_BIT).
     * <p>
     * Now we are ready to draw some 3D objects. We set the matrix stack GL_MODELVIEW to be the target
     * for subsequent matrix operations (The modelview matrix defines how your objects are transformed
     * (meaning translation, rotation and scaling) in your world coordinate frame), and load it with
     * the identity matrix. We call the utility function {@code GLU.gluLookAt} to:
     * <ul>
     * <li>specify the position of the eye point to be (0,0,5)</li>
     * <li>specify the position of the reference point to be (0f, 0f, 0f)</li>
     * <li>specify the direction of the up vector to be (0f, 1.0f, 0.0f)</li>
     * </ul>
     * This creates a viewing matrix derived from the eye point, the reference point indicating the
     * center of the scene, and the UP vector.
     * <p>
     * We then enable the client-side capability GL_VERTEX_ARRAY (the vertex array is enabled for
     * writing and used during rendering when glArrayElement, glDrawArrays, glDrawElements,
     * glDrawRangeElements glMultiDrawArrays, or glMultiDrawElements is called), and the capability
     * GL_TEXTURE_COORD_ARRAY (the texture coordinate array is enabled for writing and used during
     * rendering when glArrayElement, glDrawArrays, glDrawElements, glDrawRangeElements
     * glMultiDrawArrays, or glMultiDrawElements is called).
     * <p>
     * Now we specify that the GL_TEXTURE0 texture unit is active, and bind the texture ID stored in
     * our field {@code mTextureID} to GL_TEXTURE_2D. We set the texture parameters for GL_TEXTURE_2D
     * GL_TEXTURE_WRAP_S and GL_TEXTURE_WRAP_T both to GL_REPEAT (when the coordinate falls outside
     * (0..1) the integer part of the coordinate will be ignored and a repeating pattern is formed).
     * <p>
     * Now we calculate the {@code float angle} we want to use to rotate our triangle (using an
     * arbitrary function of the system uptime), and rotate our modelview matrix by that angle.
     * Finally we instruct our {@code Triangle mTriangle} to draw itself.
     *
     * @param gl the GL interface. Use <code>instanceof</code> to
     *           test if the interface supports GL11 or higher interfaces.
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        glDisable(GL_DITHER);

        glTexEnvx(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

        /*
         * Usually, the first thing one might want to do is to clear
         * the screen. The most efficient way of doing this is to use
         * glClear().
         */

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        /*
         * Now we're ready to draw some 3D objects
         */

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        GLU.gluLookAt(gl, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mTextureID);
        glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);

        glRotatef(angle, 0, 0, 1.0f);

        mTriangle.draw(gl);
    }

    /**
     * Called when the surface changed size. Called after the surface is created and whenever the
     * OpenGL ES surface size changes.
     * <p>
     * First we calculate the ratio of {@code w/h} which we will use to specify the coordinates for
     * the left and right vertical clipping planes. We set the matrix stack GL_PROJECTION to be the
     * for subsequent matrix operations (the projection matrix defines the properties of the camera
     * that views the objects in the world coordinate frame. Here you typically set the zoom factor,
     * aspect ratio and the near and far clipping planes), and load it with the identity matrix. Then
     * we call the method {@code glFrustumf} to multiply the current matrix by a perspective matrix
     * built using {@code -ratio} as the left vertical clipping plane, {@code ratio} as the right
     * vertical clipping plane, {@code -1} for the bottom clipping plane, {@code 1} for the top
     * clipping plane, {@code 3} for the near clipping plane, and {@code 7} for the far clipping
     * plane. ((left, bottom, -near) and (right, top, -near) specify the points on the near clipping
     * plane that are mapped to the lower left and upper right corners of the window, assuming that
     * the eye is located at (0, 0, 0). -far specifies the location of the far clipping plane.)
     *
     * @param gl the GL interface. Use <code>instanceof</code> to
     *           test if the interface supports GL11 or higher interfaces.
     * @param w  new width of surface
     * @param h  new height of surface
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        glViewport(0, 0, w, h);

        /*
        * Set our projection matrix. This doesn't have to be done
        * each time we draw, but usually a new projection needs to
        * be set when the viewport is resized.
        */

        float ratio = (float) w / h;
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glFrustumf(-ratio, ratio, -1, 1, 3, 7);
    }

    /**
     * Default {@code TextureLoader} class we use when we are used by the activity {@code TriangleActivity},
     * it consists of the png image R.raw.robot stored in our resources.
     */
    private class RobotTextureLoader implements TextureLoader {
        /**
         * Loads the png image R.raw.robot as the two-dimensional texture image to be used in the
         * current context into GL_TEXTURE_2D. First we open a data stream {@code InputStream is}
         * for reading the raw resource png image R.raw.robot. We declare {@code Bitmap bitmap} and
         * read and decode {@code is} into it. We use the utility method {@code GLUtils.texImage2D}
         * to set specify {@code bitmap} as the two-dimensional texture image used by GL_TEXTURE_2D,
         * and finally free the native object associated with {@code bitmap}.
         *
         * @param gl OpenGL interface UNUSED
         */
        @Override
        public void load(GL10 gl) {
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

            GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
        }
    }

    /**
     * Class used to draw a triangle.
     */
    @SuppressWarnings("WeakerAccess")
    static class Triangle {
        /**
         * Number of vertices.
         */
        private final static int VERTS = 3;

        /**
         * {@code FloatBuffer} loaded with the vertex coordinates of a unit-sided equilateral
         * triangle centered on the origin. It is used in our {@code draw} method in a call to
         * {@code glVertexPointer} in order to specify the location and data format of the array
         * of vertex coordinates to use when rendering.
         */
        private FloatBuffer mFVertexBuffer;
        /**
         * {@code FloatBuffer} loaded with the x,y coordinates of a unit-sided equilateral triangle
         * centered on the origin. It is used in our {@code draw} method in a call to the method
         * {@code glTexCoordPointer} in order to specify the location and data format of an array
         * of texture coordinates to use when rendering.
         */
        private FloatBuffer mTexBuffer;
        /**
         * {@code ShortBuffer} loaded with the three indices 0, 1, and 2. It is used in our
         * {@code draw} method in the call to {@code glDrawElements} to specify the indices
         * which it uses to construct a sequence of geometric primitives that are drawn using
         * the vertices loaded from {@code mFVertexBuffer} and texture vertices loaded from
         * {@code mTexBuffer}
         */
        private ShortBuffer mIndexBuffer;

        /**
         * Constructor for our class, it allocates and initializes the content of the three fields
         * we use to draw a triangle: {@code FloatBuffer mFVertexBuffer}, {@code FloatBuffer mTexBuffer},
         * and {@code ShortBuffer mIndexBuffer}.
         * <p>
         * First: we allocate a direct byte buffer on the native heap for {@code ByteBuffer vbb}, and
         * set its byte order to native order. We use {@code vbb} to create a view of this byte
         * buffer as a float buffer which we store in the field {@code FloatBuffer mFVertexBuffer}.
         * <p>
         * Second: we allocate a direct byte buffer on the native heap for {@code ByteBuffer tbb}, and
         * set its byte order to native order. We use {@code tbb} to create a view of this byte
         * buffer as a float buffer which we store in the field {@code FloatBuffer mTexBuffer}.
         * <p>
         * Third: we allocate a direct byte buffer on the native heap for {@code ByteBuffer ibb}, and
         * set its byte order to native order. We use {@code ibb} to create a view of this byte
         * buffer as a short buffer which we store in the field {@code ShortBuffer mIndexBuffer}.
         * <p>
         * In {@code float[] coords} we declare the coordinates of a unit-sided equilateral triangle
         * centered on the origin.
         * <p>
         * We load each of the three, three dimensional points in {@code float[] coords} into
         * {@code FloatBuffer mFVertexBuffer} (each coordinate scaled by 2.0 for some reason).
         * We load the x and y coordinates of each of the three points in {@code float[] coords} into
         * {@code FloatBuffer mTexBuffer} (each coordinate scaled by 2.0 and offset by 0.5 for some
         * reason). And we load the three indices 0, 1, and 2 into {@code ShortBuffer mIndexBuffer}.
         * <p>
         * Finally we set each of the buffers {@code mFVertexBuffer}, {@code mTexBuffer} and
         * {@code mIndexBuffer} position to 0, so that the {@code gl*Pointer()} functions can read
         * them from the beginning.
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
         * Called when we are meant to draw our triangle. First we specify the orientation of front
         * facing polygons to be GL_CCW (counter clockwise). Then we call {@code glVertexPointer} to
         * specify the location and data format of the array of vertex coordinates to use when
         * rendering (3 coordinates per vertex, GL_FLOAT as the data type of each coordinate, 0 as
         * the stride between vertices (no extra data contained between vertices), and
         * {@code FloatBuffer mFVertexBuffer} as the pointer to the first coordinate of the first
         * vertex in the array.
         * <p>
         * Next we enable the GL_TEXTURE_2D server-side GL capability (If enabled and no fragment
         * shader is active, two-dimensional texturing is performed (unless three-dimensional or
         * cube-mapped texturing is also enabled), and we use {@code glTexCoordPointer} to define an
         * array of texture coordinates with 2 coordinates per array element, GL_FLOAT as the data
         * type, 0 as the byte offset between consecutive texture coordinate sets. and
         * {@code FloatBuffer mTexBuffer} as the pointer to the first coordinate of the first
         * vertex in the array.
         * <p>
         * Finally we call {@code glDrawElements} to render primitives from our array data, using
         * the primitive type GL_TRIANGLE_STRIP, VERTS (3) elements to be rendered, GL_UNSIGNED_SHORT
         * as the type of the values in our indices buffer, and {@code ShortBuffer mIndexBuffer} as
         * the pointer to the location where the indices are stored.
         *
         * @param gl OpenGL interface UNUSED
         */
        @SuppressWarnings("UnusedParameters")
        public void draw(GL10 gl) {
            glFrontFace(GL_CCW);
            glVertexPointer(3, GL_FLOAT, 0, mFVertexBuffer);
            glEnable(GL_TEXTURE_2D);
            glTexCoordPointer(2, GL_FLOAT, 0, mTexBuffer);
            glDrawElements(GL_TRIANGLE_STRIP, VERTS, GL_UNSIGNED_SHORT, mIndexBuffer);
        }
    }
}
