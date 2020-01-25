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

import android.opengl.ETC1Util;
import android.opengl.GLES10;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.opengles.GL10;

/**
 * Demonstrate how to use ETC1 format compressed textures.
 * This sample can be recompiled to use either resource-based
 * textures (compressed offline using the etc1tool), or
 * textures created on the fly by compressing images.
 */
public class CompressedTextureActivity extends AppCompatActivity {
    /**
     * TAG used for logging
     */
    private final static String TAG = "C...dTextureAct...";
    /**
     * Choose between creating a compressed texture on the fly or
     * loading a compressed texture from a resource.
     */
    private final static boolean TEST_CREATE_TEXTURE = false;
    /**
     * When creating a compressed texture on the fly, choose
     * whether or not to use the i/o stream APIs.
     */
    private final static boolean USE_STREAM_IO = false;

    /**
     * {@code GLSurfaceView} used to display our demo
     */
    private GLSurfaceView mGLView;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we initialize our field {@code GLSurfaceView mGLView} with a new instance
     * of {@code GLSurfaceView} using "this" activity as the context for resources. Then we call
     * {@code setEGLConfigChooser} to install a config chooser which will choose a config as close
     * to 16-bit RGB as possible with the value false so that there is no depth buffer. We declare
     * {@code StaticTriangleRenderer.TextureLoader loader}, and if value of the compile time switch
     * TEST_CREATE_TEXTURE is true we set it to an instance of {@code SyntheticCompressedTextureLoader},
     * and if false we set it to an instance of {@code CompressedTextureLoader}. We set the renderer
     * of {@code mGLView} to a new instance of {@code StaticTriangleRenderer} using "this"
     * {@code CompressedTextureActivity} activity for context, and {@code loader} as the
     * {@code TextureLoader} to use, and finally we set our content view to {@code mGLView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLView = new GLSurfaceView(this);
        mGLView.setEGLConfigChooser(false);
        StaticTriangleRenderer.TextureLoader loader;
        if (TEST_CREATE_TEXTURE) {
            loader = new SyntheticCompressedTextureLoader();
        } else {
            loader = new CompressedTextureLoader();
        }
        mGLView.setRenderer(new StaticTriangleRenderer(this, loader));
        setContentView(mGLView);
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed.  The counterpart to
     * {@link #onResume}. First we call through to our super's implementation of
     * {@code onPause}, then we inform the {@code GLSurfaceView mGLView} view that
     * the activity is paused.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or
     * {@link #onPause}, for your activity to start interacting with the user.
     * First we call through to our super's implementation of {@code onResume},
     * then we inform the {@code GLSurfaceView mGLView} view that the activity
     * is resumed (calling this method will recreate the OpenGL display and resume
     * the rendering thread.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }

    /**
     * Demonstrate how to load a compressed texture from an APK resource.
     */
    private class CompressedTextureLoader implements StaticTriangleRenderer.TextureLoader {
        /**
         * Called to load the compressed texture, it is called in the {@code onSurfaceCreated}
         * override of {@code StaticTriangleRenderer} if TEST_CREATE_TEXTURE is false. First we
         * open a data stream {@code InputStream input} for reading the raw resource R.raw.androids
         * (raw/androids.pkm), then we use the method {@code ETC1Util.loadTexture} to load the ETC1
         * texture contained in that file. (The rest is just boilerplate to catch exceptions that
         * might occur.)
         *
         * @param gl the GL interface. Use <code>instanceof</code> to test if the interface supports
         *           GL11 or higher interfaces. UNUSED.
         */
        @Override
        public void load(GL10 gl) {
            Log.w(TAG, "ETC1 texture support: " + ETC1Util.isETC1Supported());
            InputStream input = getResources().openRawResource(R.raw.androids);
            //noinspection TryFinallyCanBeTryWithResources
            try {
                ETC1Util.loadTexture(GLES10.GL_TEXTURE_2D, 0, 0,
                        GLES10.GL_RGB, GLES10.GL_UNSIGNED_SHORT_5_6_5, input);
            } catch (IOException e) {
                Log.w(TAG, "Could not load texture: " + e);
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    // ignore exception thrown from close.
                }
            }
        }
    }

    /**
     * Demonstrate how to create a compressed texture on the fly.
     */
    private class SyntheticCompressedTextureLoader implements StaticTriangleRenderer.TextureLoader {
        /**
         * Called to create and load the compressed texture, it is called in the {@code onSurfaceCreated}
         * override of {@code StaticTriangleRenderer} if TEST_CREATE_TEXTURE is true. We declare the
         * constants {@code width} and {@code height} to be 128, then we call our method {@code createImage}
         * to create a {@code ByteBuffer} holding a 128x128 colored pattern to use as our texture and
         * save that in {@code Buffer image}. We use the method {@code ETC1Util.compressTexture} to
         * create {@code ETC1Util.ETC1Texture etc1Texture} from {@code Buffer image}. If the compile
         * time flag USE_STREAM_IO is true we write {@code etc1Texture} to a {@code ByteArrayOutputStream},
         * convert that {@code ByteArrayOutputStream} to a {@code byte[]} array, and open a
         * {@code ByteArrayInputStream bis} from that array which we then use in a call to the method
         * {@code ETC1Util.loadTexture} to load the texture to the active openGL context. If USE_STREAM_IO
         * is false we simply call the method {@code ETC1Util.loadTexture} to directly load the texture
         * to the active openGL context.
         *
         * @param gl the GL interface. Use <code>instanceof</code> to test if the interface supports
         *           GL11 or higher interfaces. UNUSED.
         */
        @Override
        public void load(GL10 gl) {
            final int width = 128;
            final int height = 128;
            Buffer image = createImage(width, height);
            ETC1Util.ETC1Texture etc1Texture = ETC1Util.compressTexture(image, width, height, 3, 3 * width);
            if (USE_STREAM_IO) {
                // Test the ETC1Util APIs for reading and writing compressed textures to I/O streams.
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ETC1Util.writeTexture(etc1Texture, bos);
                    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                    ETC1Util.loadTexture(GLES10.GL_TEXTURE_2D, 0, 0,
                            GLES10.GL_RGB, GLES10.GL_UNSIGNED_SHORT_5_6_5, bis);
                } catch (IOException e) {
                    Log.w(TAG, "Could not load texture: " + e);
                }
            } else {
                ETC1Util.loadTexture(GLES10.GL_TEXTURE_2D, 0, 0,
                        GLES10.GL_RGB, GLES10.GL_UNSIGNED_SHORT_5_6_5, etc1Texture);
            }
        }

        /**
         * Fills a {@code ByteBuffer} with a width X height colored image to use as a texture. First
         * we calculate {@code int stride} to be 3*width (three bytes per pixel RGB), then we allocate
         * {@code ByteBuffer image} to be a height*stride {@code ByteBuffer} using native byte order.
         * Then we loop through every row in {@code image}, calculating a "munching squares" pattern
         * for each pixel and appending them to {@code image}. When done we reset the position of
         * {@code image} to the beginning and return it to the caller.
         *
         * @param width width of the image to create
         * @param height height of the image to create
         * @return {@code Buffer} (actually a {@code ByteBuffer}) containing an image to be used as a
         * texture.
         */
        @SuppressWarnings("SameParameterValue")
        private Buffer createImage(int width, int height) {
            int stride = 3 * width;
            ByteBuffer image = ByteBuffer.allocateDirect(height * stride)
                    .order(ByteOrder.nativeOrder());

            // Fill with a pretty "munching squares" pattern:
            for (int t = 0; t < height; t++) {
                byte red = (byte) (255 - 2 * t);
                byte green = (byte) (2 * t);
                byte blue = 0;
                for (int x = 0; x < width; x++) {
                    int y = x ^ t;
                    image.position(stride * y + x * 3);
                    image.put(red);
                    image.put(green);
                    image.put(blue);
                }
            }
            image.position(0);
            return image;
        }
    }
}
