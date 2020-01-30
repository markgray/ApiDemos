/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.opengl.GLSurfaceView
import android.view.SurfaceView
import android.opengl.GLU
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11ExtensionPack

/**
 * Demonstrate the Frame Buffer Object OpenGL ES extension. This sample renders a scene into an
 * offscreen frame buffer, and then uses the resulting image as a texture to render an onscreen scene.
 */
class FrameBufferObjectActivity : AppCompatActivity() {
    /**
     * [GLSurfaceView] containing our demo, its [GLSurfaceView.Renderer] is our class
     * [Renderer] and it is our entire content view.
     */
    private var mGLSurfaceView: GLSurfaceView? = null

    /**
     * [GLSurfaceView.Renderer] which draws our demo, it consists of two rotating [Cube]
     * objects used as the texture for a rotating [Triangle] object.
     */
    private inner class Renderer : GLSurfaceView.Renderer {
        /**
         * Flag indicating whether the current GL context supports the GL_OES_framebuffer_object
         * extension
         */
        private var mContextSupportsFrameBufferObject = false
        /**
         * Texture ID of the texture we use for our demo, it is bound to GL_TEXTURE_2D in our method
         * [drawOnscreen] which is called by our override of [onDrawFrame].
         */
        private var mTargetTexture = 0
        /**
         * Framebuffer object name we use to draw our offscreen texture to, it is bound to
         * [GL11ExtensionPack.GL_FRAMEBUFFER_OES] in our [onDrawFrame] override
         */
        private var mFramebuffer = 0
        /**
         * Width of the Framebuffer object, it is used in our method [createTargetTexture] to
         * specify the width of the two-dimensional texture image for the GL_TEXTURE_2D target
         * (which is bound to our [mTargetTexture], as well as by our method [createFrameBuffer]
         * to specify the width of the renderbuffer object's image that is bound to GL_RENDERBUFFER_OES
         * (which is our [mFramebuffer]).
         */
        private val mFramebufferWidth = 256
        /**
         * Height of the Framebuffer object, it is used in our method [createTargetTexture] to
         * specify the height of the two-dimensional texture image for the GL_TEXTURE_2D target
         * (which is bound to our [mTargetTexture], as well as by our method [createFrameBuffer]
         * to specify the height of the renderbuffer object's image that is bound to GL_RENDERBUFFER_OES
         * (which is our [mFramebuffer]).
         */
        private val mFramebufferHeight = 256
        /**
         * Width of our [SurfaceView] which is set in our [onSurfaceChanged] callback
         */
        private var mSurfaceWidth = 0
        /**
         * Height of our [SurfaceView] which is set in our [onSurfaceChanged] callback
         */
        private var mSurfaceHeight = 0
        /**
         * [Triangle] instance which we draw in our method [drawOnscreen] every time our
         * callback [onDrawFrame] is called, it is rotated by a function of the system time
         * by rotating the [GLSurfaceView] using `glRotatef`, and the texture is supplied
         * by [mTargetTexture] (which consists of our off screen frame buffer which has two
         * rotating [Cube] objects being drawn into it).
         */
        private var mTriangle: Triangle? = null
        /**
         * [Cube] instance that we use twice to produce the texture used by our [Triangle] instance
         * (the second rotated around the (y,z) axis by twice the angle the first is rotated by, and
         * translated by (0.5, 0.5, 0.5))
         */
        private var mCube: Cube? = null
        /**
         * Angle used to draw the two [Cube] objects we use as our texture, it is advanced by
         * 1.2 degrees every frame.
         */
        private var mAngle = 0f

        /**
         * Called to draw the current frame. First we call our method [checkGLError] to catch
         * any errors that may have occurred. Then if [mContextSupportsFrameBufferObject] is
         * *true* (the current context supports frame buffer objects) we cast our [GL10] parameter
         * [gl] to a [GL11ExtensionPack] to set variable `val gl11ep` and if we are debugging we
         * immediately call our method [drawOffscreenImage] which will draw the two rotating [Cube]
         * objects that we use as our texture directly to the [GLSurfaceView] window system provided
         * framebuffer, if we are not debugging we bind our frame buffer object name [mFramebuffer]
         * to the target GL_FRAMEBUFFER_OES, then call our method [drawOffscreenImage], unbind the
         * target GL_FRAMEBUFFER_OES, and call our method [drawOnscreen]. If the current context
         * doesn't support frame buffer objects we set our clear color to red, and clear the color
         * buffer and depth buffer with it.
         *
         * @param gl the [GL] interface.
         */
        override fun onDrawFrame(gl: GL10) {
            checkGLError(gl)
            if (mContextSupportsFrameBufferObject) {
                val gl11ep = gl as GL11ExtensionPack
                @Suppress("ConstantConditionIf")
                if (Companion.DEBUG_RENDER_OFFSCREEN_ONSCREEN) {
                    drawOffscreenImage(gl, mSurfaceWidth, mSurfaceHeight)
                } else {
                    gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, mFramebuffer)
                    drawOffscreenImage(gl, mFramebufferWidth, mFramebufferHeight)
                    gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0)
                    drawOnscreen(gl, mSurfaceWidth, mSurfaceHeight)
                }
            } else {
                /**
                 * Current context doesn't support frame buffer objects.
                 * Indicate this by drawing a red background.
                 */
                gl.glClearColor(1f, 0f, 0f, 0f)
                gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
            }
        }

        /**
         * Called when the surface changed size. Called after the surface is created and whenever
         * the OpenGL ES surface size changes. First we call our method `checkGLError` to
         * catch any error that may have occurred, then we save `width` in our field
         * `mSurfaceWidth` and height in our field `mSurfaceHeight` then set the viewport
         * to have the lower left corner at (0,0) and a width of `width` and a height of
         * `height`.
         *
         * @param gl     the GL interface.
         * @param width  width of surface
         * @param height height of surface
         */
        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            checkGLError(gl)
            mSurfaceWidth = width
            mSurfaceHeight = height
            gl.glViewport(0, 0, width, height)
        }

        /**
         * Called when the surface is created or recreated. Called when the rendering thread starts
         * and whenever the EGL context is lost. The EGL context will typically be lost when the
         * Android device awakes after going to sleep. First we check to see if the current context
         * supports the capability GL_OES_framebuffer_object (has frame buffer objects) and set our
         * field `mContextSupportsFrameBufferObject` to true if it does. Then if it does support
         * frame buffer objects, we call our method `createTargetTexture` and initialize our
         * field `mTargetTexture` with the texture name bound to GL_TEXTURE_2D and configured
         * appropriately which it we returns. We call our method `createFrameBuffer` and
         * initialize our field `mFramebuffer` with the framebuffer object name which has our
         * `mTargetTexture` attached to it and which is configured appropriately which it
         * returns. Finally we initialize our field `Cube mCube` with a new instance of
         * `Cube`, and our field `Triangle mTriangle` with a new instance of
         * `Triangle`.
         *
         * @param gl     the GL interface.
         * @param config the EGLConfig of the created surface. Unused.
         */
        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            mContextSupportsFrameBufferObject = checkIfContextSupportsFrameBufferObject(gl)
            if (mContextSupportsFrameBufferObject) {
                mTargetTexture = createTargetTexture(gl, mFramebufferWidth, mFramebufferHeight)
                mFramebuffer = createFrameBuffer(gl, mFramebufferWidth, mFramebufferHeight, mTargetTexture)
                mCube = Cube()
                mTriangle = Triangle()
            }
        }

        /**
         * Called from our implementation of `onDrawFrame` to draw our triangle. First we set
         * the viewport to have the lower left corner at (0,0) and a width of `width` and a
         * height of `height`. Then we calculate the aspect ratio `float ratio` to be
         * `width/height`, then we set the GL_PROJECTION matrix to be the current matrix, load
         * it with the identity matrix and call `glFrustumf` to multiply the matrix by the
         * perspective matrix with the left clipping plane at `-ratio`, the right clipping
         * plane at `+ratio`, the bottom clipping plane at -1, the top clipping plane at 1,
         * the near clipping plane at 3 and the far clipping plane at 7.
         *
         *
         * We set the clear color to the color blue, and clear the color buffer and the depth buffer.
         * Then we bind our `mTargetTexture` to the target texture GL_TEXTURE_2D.
         *
         *
         * We set the GL_TEXTURE_ENV_MODE texture environment parameter of the texture environment
         * GL_TEXTURE_ENV to the texture function GL_REPLACE (the texture replaces the current color).
         *
         *
         * Next we set the GL_MODELVIEW matrix to be the current matrix, load it with the identity
         * matrix and call `GLU.gluLookAt` to specify the viewing transformation to have the
         * eye point at (0,0,-5), the center reference point at (0,0,0) and the up vector (0,1,0).
         *
         *
         * We enable the client-side capability GL_VERTEX_ARRAY and GL_TEXTURE_COORD_ARRAY then select
         * GL_TEXTURE0 to be the active texture unit.
         *
         *
         * We calculate `float angle` using the current milliseconds since boot and rotate the
         * GL_MODELVIEW matrix by `angle` around the vector (0,0,1). Then we instruct our
         * `Triangle mTriangle` to draw itself.
         *
         *
         * To clean up we unbind the texture GL_TEXTURE_2D, and disable the client-side capabilities
         * GL_VERTEX_ARRAY and GL_TEXTURE_COORD_ARRAY.
         *
         * @param gl     the GL interface.
         * @param width  width of our surface view `mSurfaceWidth` in our case.
         * @param height height of our surface view `mSurfaceHeight` in our case.
         */
        private fun drawOnscreen(gl: GL10, width: Int, height: Int) {
            gl.glViewport(0, 0, width, height)
            val ratio = width.toFloat() / height
            gl.glMatrixMode(GL10.GL_PROJECTION)
            gl.glLoadIdentity()
            gl.glFrustumf(-ratio, ratio, -1f, 1f, 3f, 7f)
            gl.glClearColor(0f, 0f, 1f, 0f)
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
            gl.glBindTexture(GL10.GL_TEXTURE_2D, mTargetTexture)
            gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE.toFloat())
            gl.glMatrixMode(GL10.GL_MODELVIEW)
            gl.glLoadIdentity()
            GLU.gluLookAt(gl, 0f, 0f, -5f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
            gl.glActiveTexture(GL10.GL_TEXTURE0)
            val time = SystemClock.uptimeMillis() % 4000L
            val angle = 0.090f * time.toInt()
            gl.glRotatef(angle, 0f, 0f, 1.0f)
            mTriangle!!.draw(gl)
            // Restore default state so the other renderer is not affected.
            gl.glBindTexture(GL10.GL_TEXTURE_2D, 0)
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
        }

        /**
         * Called from our `onDrawFrame` implementation, this Draws our `Cube mCube`
         * twice to the currently selected framebuffer (either the window system provided default
         * framebuffer it we are debugging our texture, or the offscreen `mFramebuffer` which
         * is bound to the target GL_FRAMEBUFFER_OES). First we set the viewport to have the lower
         * left corner at (0,0) and a width of `width` and a height of `height`. Then we
         * calculate the aspect ratio `float ratio` to be `width/height`, then we set
         * the GL_PROJECTION matrix to be the current matrix, load it with the identity matrix and
         * call `glFrustumf` to multiply the matrix by the perspective matrix with the left
         * clipping plane at `-ratio`, the right clipping plane at `+ratio`, the bottom
         * clipping plane at -1, the top clipping plane at 1, the near clipping plane at 1 and the
         * far clipping plane at 10.
         *
         *
         * We enable the server-side GL capabilities GL_CULL_FACE (cull polygons based on their
         * winding in window coordinates) and GL_DEPTH_TEST (do depth comparisons and update the
         * depth buffer). We set the clear color to (0, 0.5, 1.0) (a light blue) and clear the color
         * buffer and the depth buffer. Next we set the GL_MODELVIEW matrix to be the current matrix,
         * load it with the identity matrix, translate it to (0, 0, -3), rotate it by `mAngle`
         * around the vector (0, 1, 0), and rotate it by `mAngle*0.25` around the vector (1,0,0).
         *
         *
         * We enable the client-side capability GL_VERTEX_ARRAY (the vertex array is enabled for
         * writing and used during rendering) and GL_COLOR_ARRAY (the color array is enabled for
         * writing and used during rendering), and then we instruct our field `Cube mCube` to
         * draw itself. We then rotate the GL_MODELVIEW matrix by `mAngle*2.0` around the
         * vector (0, 1, 1), translate it to (0.5f, 0.5f, 0.5f) and instruct our field `Cube mCube`
         * to draw itself again.
         *
         *
         * We increment `mAngle` by 1.2 degrees for the next time we are executed, disable the
         * server-side GL capabilities GL_CULL_FACE and GL_DEPTH_TEST, and disable the client-side
         * capabilities GL_VERTEX_ARRAY, and GL_COLOR_ARRAY so the other renderer is not affected..
         *
         * @param gl     the GL interface.
         * @param width  width of our texture
         * @param height height of our texture
         */
        private fun drawOffscreenImage(gl: GL10, width: Int, height: Int) {
            gl.glViewport(0, 0, width, height)
            val ratio = width.toFloat() / height
            gl.glMatrixMode(GL10.GL_PROJECTION)
            gl.glLoadIdentity()
            gl.glFrustumf(-ratio, ratio, -1f, 1f, 1f, 10f)
            gl.glEnable(GL10.GL_CULL_FACE)
            gl.glEnable(GL10.GL_DEPTH_TEST)
            gl.glClearColor(0f, 0.5f, 1f, 0f)
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
            gl.glMatrixMode(GL10.GL_MODELVIEW)
            gl.glLoadIdentity()
            gl.glTranslatef(0f, 0f, -3.0f)
            gl.glRotatef(mAngle, 0f, 1f, 0f)
            gl.glRotatef(mAngle * 0.25f, 1f, 0f, 0f)
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY)
            mCube!!.draw(gl)
            gl.glRotatef(mAngle * 2.0f, 0f, 1f, 1f)
            gl.glTranslatef(0.5f, 0.5f, 0.5f)
            mCube!!.draw(gl)
            mAngle += 1.2f
            // Restore default state so the other renderer is not affected.
            gl.glDisable(GL10.GL_CULL_FACE)
            gl.glDisable(GL10.GL_DEPTH_TEST)
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
            gl.glDisableClientState(GL10.GL_COLOR_ARRAY)
        }

        /**
         * Generates a texture name, binds it to GL_TEXTURE_2D, configures it, then returns the
         * texture name to the caller. Called from our implementation of `onSurfaceCreated` it
         * is used to initialize the field `int mTargetTexture`. We allocate `int texture`
         * and `int[] textures`, call `glGenTextures` to generate 1 texture name in
         * `textures` which we then use to initialize `texture`. We then bind `texture`
         * to GL_TEXTURE_2D, call `glTexImage2D` to specify a two-dimensional texture image for
         * GL_TEXTURE_2D with a level-of-detail number of Level 0 (the base image level), an internal
         * format of GL_RGBA, `width` for its width, `height` for its height, 0 for its
         * border (must be 0 according to docs), GL_RGBA as the format of the texel data, GL_UNSIGNED_BYTE
         * as the data type of the texel data, and 0 to point to the beginning of the data in memory.
         *
         *
         * New we set texture parameter GL_TEXTURE_MIN_FILTER for the target GL_TEXTURE_2D to GL_NEAREST
         * (the value of the texture element that is nearest (in Manhattan distance) to the center of
         * the pixel being textured is used whenever the pixel being textured maps to an area greater
         * than one texture element).
         *
         *
         * We set texture parameter GL_TEXTURE_MAG_FILTER for the target GL_TEXTURE_2D to GL_LINEAR
         * (the weighted average of the four texture elements that are closest to the center of the
         * pixel being textured is used when the pixel being textured maps to an area less than or
         * equal to one texture element).
         *
         *
         * We call `glTexParameterx` to set texture parameter GL_TEXTURE_WRAP_S for the target
         * GL_TEXTURE_2D to GL_REPEAT (causes the integer part of the s coordinate to be ignored;
         * the GL uses only the fractional part, thereby creating a repeating pattern).
         *
         *
         * We call `glTexParameterx` to set texture parameter GL_TEXTURE_WRAP_T for the target
         * GL_TEXTURE_2D to GL_REPEAT (causes the integer part of the t coordinate to be ignored;
         * the GL uses only the fractional part, thereby creating a repeating pattern).
         *
         *
         * Finally we return `texture` to the caller.
         *
         * @param gl     the GL interface.
         * @param width  width of our texture
         * @param height height of our texture
         * @return a texture name bound to GL_TEXTURE_2D and configured as we wish it
         */
        @Suppress("SameParameterValue")
        private fun createTargetTexture(gl: GL10, width: Int, height: Int): Int {
            val texture: Int
            val textures = IntArray(1)
            gl.glGenTextures(1, textures, 0)
            texture = textures[0]
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texture)
            gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, width, height, 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, null)
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat())
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT)
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT)
            return texture
        }

        /**
         * Generates a framebuffer object name, binds it to GL_FRAMEBUFFER_OES, configures it to our
         * wishing, attaches the texture image `targetTextureId` to that framebuffer object and
         * returns the framebuffer object name to the caller. Used to initialize our field
         * `mFramebuffer`.
         *
         *
         * First we cast our argument `GL10 gl` to `GL11ExtensionPack gl11ep`, then we
         * allocate `int framebuffer` and `int[] framebuffers`, fill `framebuffers`
         * with 1 framebuffer object name which we use to set `framebuffer`. We then bind
         * `framebuffer` to GL_FRAMEBUFFER_OES.
         *
         *
         * Next we allocate `int depthbuffer` and `int[] renderbuffers`, fill
         * `renderbuffers` with 1 renderbuffer object name which we use to set `depthbuffer`.
         * We then bind `depthbuffer` to the target GL_RENDERBUFFER_OES, establish data storage,
         * format and dimensions of the renderbuffer object's image to use GL_DEPTH_COMPONENT16 as its
         * internal format, a width of `width`, and a height of `height`. We next attach
         * the renderbuffer object `depthbuffer` to the target GL_FRAMEBUFFER_OES, to the
         * attachment point GL_DEPTH_ATTACHMENT_OES, specifying the renderbuffer target GL_RENDERBUFFER_OES.
         *
         *
         * We attach the texture image `targetTextureId` to the framebuffer object GL_FRAMEBUFFER_OES
         * using the attachment point GL_COLOR_ATTACHMENT0_OES, texture target GL_TEXTURE_2D, and 0
         * as the mipmap level of the texture image to be attached, which must be 0.
         *
         *
         * We now retrieve the framebuffer completeness status of the framebuffer object GL_FRAMEBUFFER_OES
         * to set `int status`, and if it is not GL_FRAMEBUFFER_COMPLETE_OES we throw a runtime
         * exception. Otherwise we unbind the framebuffer GL_FRAMEBUFFER_OES and return `framebuffer`
         * to the caller.
         *
         * @param gl              the GL interface.
         * @param width           width of our framebuffer
         * @param height          height of our framebuffer
         * @param targetTextureId a texture name bound to GL_TEXTURE_2D, `int mTargetTexture`
         * in our case.
         * @return a framebuffer object name bound to GL_RENDERBUFFER_OES and configured as we wish it.
         */
        @Suppress("SameParameterValue")
        private fun createFrameBuffer(gl: GL10, width: Int, height: Int, targetTextureId: Int): Int {
            val gl11ep = gl as GL11ExtensionPack
            val framebuffer: Int
            val framebuffers = IntArray(1)
            gl11ep.glGenFramebuffersOES(1, framebuffers, 0)
            framebuffer = framebuffers[0]
            gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, framebuffer)
            val depthbuffer: Int
            val renderbuffers = IntArray(1)
            gl11ep.glGenRenderbuffersOES(1, renderbuffers, 0)
            depthbuffer = renderbuffers[0]
            gl11ep.glBindRenderbufferOES(GL11ExtensionPack.GL_RENDERBUFFER_OES, depthbuffer)
            gl11ep.glRenderbufferStorageOES(GL11ExtensionPack.GL_RENDERBUFFER_OES,
                    GL11ExtensionPack.GL_DEPTH_COMPONENT16, width, height)
            gl11ep.glFramebufferRenderbufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES,
                    GL11ExtensionPack.GL_DEPTH_ATTACHMENT_OES,
                    GL11ExtensionPack.GL_RENDERBUFFER_OES, depthbuffer)
            gl11ep.glFramebufferTexture2DOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES,
                    GL11ExtensionPack.GL_COLOR_ATTACHMENT0_OES, GL10.GL_TEXTURE_2D,
                    targetTextureId, 0)
            val status = gl11ep.glCheckFramebufferStatusOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES)
            if (status != GL11ExtensionPack.GL_FRAMEBUFFER_COMPLETE_OES) {
                throw RuntimeException("Framebuffer is not complete: " +
                        Integer.toHexString(status))
            }
            gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0)
            return framebuffer
        }

        /**
         * Convenience function that calls our method `checkIfContextSupportsExtension` with the
         * string GL_OES_framebuffer_object.
         *
         * @param gl the GL interface.
         * @return true if the context supports the extension GL_OES_framebuffer_object
         */
        private fun checkIfContextSupportsFrameBufferObject(gl: GL10): Boolean {
            return checkIfContextSupportsExtension(gl, "GL_OES_framebuffer_object")
        }

        /**
         * This is not the fastest way to check for an extension, but fine if we are only checking
         * for a few extensions each time a context is created. We append spaces to the beginning
         * and end of the GL_EXTENSIONS string describing the current GL connection (the space
         * separated list of supported extensions to GL), and we return the results of searching
         * that string for our argument `String extension` (true if found).
         *
         * @param gl        the GL interface.
         * @param extension extension to check for
         * @return true if the extension is present in the current context.
         */
        @Suppress("SameParameterValue")
        private fun checkIfContextSupportsExtension(gl: GL10, extension: String): Boolean {
            val extensions = " " + gl.glGetString(GL10.GL_EXTENSIONS) + " "
            // The extensions string is padded with spaces between extensions, but not
// necessarily at the beginning or end. For simplicity, add spaces at the
// beginning and end of the extensions string and the extension string.
// This means we can avoid special-case checks for the first or last
// extension, as well as avoid special-case checks when an extension name
// is the same as the first part of another extension name.
            return extensions.contains(" $extension ")
        }

    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Then we initialize our field `GLSurfaceView mGLSurfaceView` with a new
     * instance of `GLSurfaceView`, set its renderer to a new instance of `Renderer` and
     * set our content view to `GLSurfaceView mGLSurfaceView`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create our surface view and set it as the content of our
// Activity
        mGLSurfaceView = GLSurfaceView(this)
        mGLSurfaceView!!.setRenderer(Renderer())
        setContentView(mGLSurfaceView)
    }

    /**
     * Called after [.onRestoreInstanceState], [.onRestart], or [.onPause], for
     * your activity to start interacting with the user. First we call through to our super's
     * implementation of `onResume`, then we pass the call on to the `onResume` method
     * of our field `GLSurfaceView mGLSurfaceView`.
     */
    override fun onResume() { // Ideally a game should implement onResume() and onPause()
// to take appropriate action when the activity looses focus
        super.onResume()
        mGLSurfaceView!!.onResume()
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. The counterpart to [.onResume]. First we call through to our super's
     * implementation of `onPause`, then we pass the call on to the `onPause` method
     * of our field `GLSurfaceView mGLSurfaceView`.
     */
    override fun onPause() { // Ideally a game should implement onResume() and onPause()
// to take appropriate action when the activity looses focus
        super.onPause()
        mGLSurfaceView!!.onPause()
    }

    companion object {
        /**
         * Setting this to true will change the behavior  of this sample. It
         * will suppress the normally onscreen rendering, and it will cause the
         * rendering that would normally be done to the offscreen FBO
         * be rendered onscreen instead. This can be helpful in debugging the
         * rendering algorithm.
         */
        private const val DEBUG_RENDER_OFFSCREEN_ONSCREEN = false

        /**
         * We call `glGetError` to fetch error information to `int error` and if the result
         * is not GL_NO_ERROR we through a RuntimeException.
         *
         * @param gl the GL interface.
         */
        fun checkGLError(gl: GL) {
            val error = (gl as GL10).glGetError()
            if (error != GL10.GL_NO_ERROR) {
                throw RuntimeException("GLError 0x" + Integer.toHexString(error))
            }
        }
    }
}