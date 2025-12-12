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

package com.example.android.apis.graphics

import android.opengl.ETC1Util
import android.opengl.GLES10
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import com.example.android.apis.graphics.StaticTriangleRenderer.TextureLoader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.opengles.GL10

/**
 * Demonstrate how to use ETC1 format compressed textures.
 * This sample can be recompiled to use either resource-based
 * textures (compressed offline using the etc1tool), or
 * textures created on the fly by compressing images.
 */
class CompressedTextureActivity : AppCompatActivity() {
    /**
     * [GLSurfaceView] used to display our demo
     */
    private var mGLView: GLSurfaceView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we initialize our [GLSurfaceView] field [mGLView] with a new instance
     * of [GLSurfaceView] using "this" activity as the context for resources. Then we call
     * the [GLSurfaceView.setEGLConfigChooser] method of [mGLView] to install a config chooser which
     * will choose a config as close to 16-bit RGB as possible with the value *false* so that there
     * is no depth buffer. We declare [StaticTriangleRenderer.TextureLoader] variable `vak loader`,
     * and if the value of the compile time switch TEST_CREATE_TEXTURE is *true* we set it to an
     * instance of [SyntheticCompressedTextureLoader], and if *false* we set it to an instance of
     * [CompressedTextureLoader]. We set the renderer of [mGLView] to a new instance of
     * [StaticTriangleRenderer] using *this* [CompressedTextureActivity] activity for context, and
     * `loader` as the [TextureLoader] to use, and finally we set our content view to [mGLView].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGLView = GLSurfaceView(this)
        mGLView!!.setEGLConfigChooser(false)
        val loader: TextureLoader = if (TEST_CREATE_TEXTURE) {
            SyntheticCompressedTextureLoader()
        } else {
            CompressedTextureLoader()
        }
        mGLView!!.setRenderer(StaticTriangleRenderer(this, loader))
        setContentView(mGLView)
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed.  The counterpart to
     * [onResume]. First we call through to our super's implementation of
     * `onPause`, then we inform the [GLSurfaceView] field [mGLView] view that
     * the activity is paused.
     */
    override fun onPause() {
        super.onPause()
        mGLView!!.onPause()
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or
     * [onPause], for your activity to start interacting with the user.
     * First we call through to our super's implementation of `onResume`,
     * then we inform the [GLSurfaceView] field [mGLView] view that the activity
     * is resumed (calling this method will recreate the OpenGL display and resume
     * the rendering thread.
     */
    override fun onResume() {
        super.onResume()
        mGLView!!.onResume()
    }

    /**
     * Demonstrate how to load a compressed texture from an APK resource.
     */
    private inner class CompressedTextureLoader : TextureLoader {
        /**
         * Called to load the compressed texture, it is called in the `onSurfaceCreated`
         * override of [StaticTriangleRenderer] if TEST_CREATE_TEXTURE is false. First we
         * open a [InputStream] data stream `val input` for reading the raw resource R.raw.androids
         * (raw/androids.pkm), then we use the method [ETC1Util.loadTexture] to load the ETC1
         * texture contained in that file. (The rest is just boilerplate to catch exceptions that
         * might occur.)
         *
         * @param gl the GL interface. Use `instanceof` to test if the interface supports
         * GL11 or higher interfaces. UNUSED.
         */
        override fun load(gl: GL10?) {
            Log.w(TAG, "ETC1 texture support: " + ETC1Util.isETC1Supported())
            val input: InputStream = resources.openRawResource(R.raw.androids)
            try {
                ETC1Util.loadTexture(
                    GLES10.GL_TEXTURE_2D,
                    0,
                    0,
                    GLES10.GL_RGB,
                    GLES10.GL_UNSIGNED_SHORT_5_6_5,
                    input
                )
            } catch (e: IOException) {
                Log.w(TAG, "Could not load texture: $e")
            } finally {
                try {
                    input.close()
                } catch (_: IOException) { // ignore exception thrown from close.
                }
            }
        }
    }

    /**
     * Demonstrate how to create a compressed texture on the fly.
     */
    private class SyntheticCompressedTextureLoader : TextureLoader {
        /**
         * Called to create and load the compressed texture, it is called in the `onSurfaceCreated`
         * override of [StaticTriangleRenderer] if TEST_CREATE_TEXTURE is *true*. We declare the
         * constants `val width` and `val height` to be 128, then we call our method [createImage]
         * to create a [ByteBuffer] holding a 128x128 colored pattern to use as our texture and
         * save that in [Buffer] variable `val image`. We use the method [ETC1Util.compressTexture]
         * to create [ETC1Util.ETC1Texture] `val etc1Texture` from `image`. If the compile
         * time flag USE_STREAM_IO is *true* we write `etc1Texture` to a [ByteArrayOutputStream]
         * variable `val bos`, convert that [ByteArrayOutputStream] to a [Byte] array, and open a
         * [ByteArrayInputStream] `val bis` from that array which we then use in a call to the method
         * [ETC1Util.loadTexture] to load the texture to the active openGL context. If USE_STREAM_IO
         * is *false* we simply call the method [ETC1Util.loadTexture] to directly load the texture
         * to the active openGL context.
         *
         * @param gl the GL interface. Use `instanceof` to test if the interface supports
         * GL11 or higher interfaces. UNUSED.
         */
        override fun load(gl: GL10?) {
            val width = 128
            val height = 128
            val image = createImage(width, height)
            val etc1Texture = ETC1Util.compressTexture(image, width, height, 3, 3 * width)
            if (USE_STREAM_IO) { // Test the ETC1Util APIs for reading and writing compressed textures to I/O streams.
                try {
                    val bos = ByteArrayOutputStream()
                    ETC1Util.writeTexture(etc1Texture, bos)
                    val bis = ByteArrayInputStream(bos.toByteArray())
                    ETC1Util.loadTexture(
                        GLES10.GL_TEXTURE_2D,
                        0,
                        0,
                        GLES10.GL_RGB,
                        GLES10.GL_UNSIGNED_SHORT_5_6_5,
                        bis
                    )
                } catch (e: IOException) {
                    Log.w(TAG, "Could not load texture: $e")
                }
            } else {
                ETC1Util.loadTexture(
                    GLES10.GL_TEXTURE_2D,
                    0,
                    0,
                    GLES10.GL_RGB,
                    GLES10.GL_UNSIGNED_SHORT_5_6_5,
                    etc1Texture
                )
            }
        }

        /**
         * Fills a [ByteBuffer] with a width by height colored image to use as a texture. First
         * we calculate `val stride` to be 3*width (three bytes per pixel RGB), then we allocate
         * [ByteBuffer] variable `val image` to be a height times stride [ByteBuffer] using native
         * byte order. Then we loop through every row in `image`, calculating a "munching squares"
         * pattern for each pixel and appending them to `image`. When done we reset the position of
         * `image` to the beginning and return it to the caller.
         *
         * @param width width of the image to create
         * @param height height of the image to create
         * @return [Buffer] (actually a [ByteBuffer]) containing an image to be used as a
         * texture.
         */
        @Suppress("SameParameterValue")
        private fun createImage(width: Int, height: Int): Buffer {
            val stride = 3 * width
            val image = ByteBuffer.allocateDirect(height * stride)
                .order(ByteOrder.nativeOrder())
            // Fill with a pretty "munching squares" pattern:
            for (t in 0 until height) {
                val red = (255 - 2 * t).toByte()
                val green = (2 * t).toByte()
                val blue: Byte = 0
                for (x in 0 until width) {
                    val y = x xor t
                    image.position(stride * y + x * 3)
                    image.put(red)
                    image.put(green)
                    image.put(blue)
                }
            }
            image.position(0)
            return image
        }
    }

    companion object {
        /**
         * TAG used for logging
         */
        private const val TAG = "C...dTextureAct..."

        /**
         * Choose between creating a compressed texture on the fly or
         * loading a compressed texture from a resource.
         */
        private const val TEST_CREATE_TEXTURE = false

        /**
         * When creating a compressed texture on the fly, choose
         * whether or not to use the i/o stream APIs.
         */
        private const val USE_STREAM_IO = false
    }
}