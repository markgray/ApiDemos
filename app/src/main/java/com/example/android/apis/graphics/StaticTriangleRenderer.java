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
     *
     * First we disable dithering, then we use the method {@code glHint} to set the implementation
     * specific hint GL_PERSPECTIVE_CORRECTION_HINT to GL_FASTEST. We set the red, green, blue, and
     * alpha values used when the color buffers are cleared to (0.5,0.5,0.5,1.0) (GRAY), set the
     * shade model to GL_SMOOTH (causes the computed colors of vertices to be interpolated as the
     * primitive is rasterized, typically assigning different colors to each resulting pixel), we
     * enable GL_DEPTH_TEST (the depth value from the fragment being rendered is compared to the
     * depth value from the matching sample currently in the framebuffer and if it fails it is
     * discarded), and we enable GL_TEXTURE_2D (Images in our texture all are 2-dimensional. They
     * have width and height, but no depth).
     *
     * Now we create our texture.
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

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,
                GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D,
                GL_TEXTURE_MAG_FILTER,
                GL_LINEAR);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,
                GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,
                GL_CLAMP_TO_EDGE);

        glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE,
                GL_REPLACE);
        mTextureLoader.load(gl);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        glDisable(GL_DITHER);

        glTexEnvx(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE,
                GL_MODULATE);

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
        glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,
                GL_REPEAT);
        glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,
                GL_REPEAT);

        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);

        glRotatef(angle, 0, 0, 1.0f);

        mTriangle.draw(gl);
    }

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

    private Context mContext;
    private Triangle mTriangle;
    private int mTextureID;
    private TextureLoader mTextureLoader;

    private class RobotTextureLoader implements TextureLoader {
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

    @SuppressWarnings("WeakerAccess")
    static class Triangle {
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

        @SuppressWarnings("UnusedParameters")
        public void draw(GL10 gl) {
            glFrontFace(GL_CCW);
            glVertexPointer(3, GL_FLOAT, 0, mFVertexBuffer);
            glEnable(GL_TEXTURE_2D);
            glTexCoordPointer(2, GL_FLOAT, 0, mTexBuffer);
            glDrawElements(GL_TRIANGLE_STRIP, VERTS,
                    GL_UNSIGNED_SHORT, mIndexBuffer);
        }

        private final static int VERTS = 3;

        private FloatBuffer mFVertexBuffer;
        private FloatBuffer mTexBuffer;
        private ShortBuffer mIndexBuffer;
    }
}
