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
package com.example.android.apis.graphics

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Wrapper activity demonstrating the use of [GLSurfaceView], a view
 * that uses OpenGL drawing into a dedicated surface.
 *
 * Shows: How to redraw in response to user input. Draws a cube, and allows the user to rotate it
 * using their finger.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
class TouchRotateActivity : AppCompatActivity() {
    /**
     * The [GLSurfaceView] (a [TouchSurfaceView] in our case) that we use for drawing
     */
    private var mGLSurfaceView: GLSurfaceView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we initialize our [GLSurfaceView] field [mGLSurfaceView] with a new instance
     * of [TouchSurfaceView], give it focus, and allow it to receive focus while in touch mode.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**
         * Create our Preview view and set it as the content of our Activity
         */
        mGLSurfaceView = TouchSurfaceView(this)
        setContentView(mGLSurfaceView)
        mGLSurfaceView!!.requestFocus()
        mGLSurfaceView!!.isFocusableInTouchMode = true
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for our activity to start
     * interacting with the user. First we call through to our super's implementation of `onResume`,
     * then we pass the call on to the `onResume` method of our [GLSurfaceView] field [mGLSurfaceView].
     */
    override fun onResume() {
        /**
         * Ideally a game should implement onResume() and onPause()
         * to take appropriate action when the activity looses focus
         */
        super.onResume()
        mGLSurfaceView!!.onResume()
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of `onPause`,
     * then we pass the call on to the `onPause` method of our field [mGLSurfaceView].
     */
    override fun onPause() {
        /**
         * Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
         */
        super.onPause()
        mGLSurfaceView!!.onPause()
    }

    /**
     * Adds a simple rotation control to a [GLSurfaceView] which contains a [Cube].
     */
    private class TouchSurfaceView(context: Context?) : GLSurfaceView(context) {
        /**
         * Renderer for our [GLSurfaceView].
         */
        private val mRenderer: CubeRenderer

        /**
         * Last x coordinate of a [MotionEvent] received by our [onTouchEvent] override. It
         * is set by all types of [MotionEvent] but then only used when the touch event is of the
         * ACTION_MOVE type when it is used along with the new x coordinate of the [MotionEvent]
         * to calculate a new angle to rotate for [CubeRenderer] field [mRenderer]
         */
        private var mPreviousX = 0f

        /**
         * Last y coordinate of a [MotionEvent] received by our [onTouchEvent] override. It
         * is set by all types of [MotionEvent] but then only used when the touch event is of the
         * ACTION_MOVE type when it is used along with the new y coordinate of the [MotionEvent]
         * to calculate a new angle to rotate for [CubeRenderer] field [mRenderer]
         */
        private var mPreviousY = 0f

        /**
         * We implement this method to handle trackball motion events. We call our [updateAngles]
         * method with the X and Y coordinate of the first pointer index of our [MotionEvent] parameter
         * [e] and the scale factor for a trackball [MotionEvent] TRACKBALL_SCALE_FACTOR. The
         * [updateAngles] method will modify the `mAngleX` and `mAngleY` fields of our [CubeRenderer]
         * field [mRenderer] and call the method [requestRender] to request that our renderer render
         * a new frame. Finally we return *true* to the caller.
         *
         * @param e The motion event.
         * @return *true* if the event was handled, false otherwise. We always return *true*
         */
        override fun onTrackballEvent(e: MotionEvent): Boolean {
            updateAngles(e.x, e.y, TRACKBALL_SCALE_FACTOR)
            return true
        }

        /**
         * We implement this method to handle touch screen motion events. We initialize `val action`
         * with the masked off action of our [MotionEvent] parameter [e], then branch on the value
         * of `action`:
         *
         *  * ACTION_MOVE: A change has happened during a press gesture (between ACTION_DOWN and
         *  ACTION_UP), we call our method [updateAngles] with the difference between  the X and Y
         *  coordinates of the first pointer index of our [MotionEvent] parameter [e] and the
         *  previous values stored in our fields [mPreviousX] and [mPreviousY] along with the scale
         *  factor for a ACTION_MOVE touch movement TOUCH_SCALE_FACTOR. The [updateAngles] method
         *  will modify the `mAngleX` and `mAngleY` fields of our [CubeRenderer] field [mRenderer]
         *  and call the method [requestRender] to request that our renderer render a new frame.
         *
         *  * ACTION_DOWN: A pressed gesture has started, if the source of [MotionEvent] parameter
         *  [e] is SOURCE_MOUSE (input source is a mouse pointing device) we call the method
         *  [requestPointerCapture] to request pointer capture mode (When the window has pointer
         *  capture, the mouse pointer icon will disappear and will not change its position. Further
         *  mouse events will be dispatched with the source [InputDevice.SOURCE_MOUSE_RELATIVE], and
         *  relative position changes will be available through the methods [MotionEvent.getX] and
         *  [MotionEvent.getY].) If the source is not SOURCE_MOUSE we call the [releasePointerCapture]
         *  method to release pointer capture (if the window does not have pointer capture, this
         *  call will do nothing).
         *
         * For all [MotionEvent] types we save the value of the X coordinate of [e] in
         * [mPreviousX] and the value the Y coordinate in [mPreviousY], and return *true*
         * to the caller to consume the event.
         *
         * @param e The motion event.
         * @return *true* if the event was handled, *false* otherwise.
         */
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(e: MotionEvent): Boolean {
            val action = e.actionMasked
            if (action == MotionEvent.ACTION_MOVE) {
                updateAngles(
                    dx = e.x - mPreviousX,
                    dy = e.y - mPreviousY,
                    scaleFactor = TOUCH_SCALE_FACTOR
                )
            } else if (action == MotionEvent.ACTION_DOWN) {
                if (e.isFromSource(InputDevice.SOURCE_MOUSE)) {
                    requestPointerCapture()
                } else {
                    releasePointerCapture()
                }
            }
            mPreviousX = e.x
            mPreviousY = e.y
            return true
        }

        /**
         * Implement this method to handle captured pointer events. If the masked off action of our
         * [MotionEvent] parameter [e] is ACTION_DOWN we call the [releasePointerCapture]
         * method to release pointer capture, otherwise we call our method [updateAngles] with
         * the X and Y coordinates of [e] along with the scale factor for a ACTION_MOVE touch
         * movement TOUCH_SCALE_FACTOR. The [updateAngles] method will modify the `mAngleX`
         * and `mAngleY` fields of our [CubeRenderer] field [mRenderer] and call the method
         * [requestRender] to request that our renderer render a new frame. Finally we return
         * *true* to the caller to consume the event.
         *
         * @param e The captured pointer event.
         * @return *true* if the event was handled, *false* otherwise.
         */
        override fun onCapturedPointerEvent(e: MotionEvent): Boolean {
            if (e.actionMasked == MotionEvent.ACTION_DOWN) {
                releasePointerCapture()
            } else {
                updateAngles(dx = e.x, dy = e.y, scaleFactor = TOUCH_SCALE_FACTOR)
            }
            return true
        }

        /**
         * Called when a key down event has occurred. We call the [releasePointerCapture] method
         * to release pointer capture and return the value returned by our super's implementation of
         * `onKeyDown` to the caller.
         *
         * @param keyCode The value in `event.getKeyCode()`.
         * @param event Description of the key event.
         * @return If you handled the event, return *true*. If you want to allow
         * the event to be handled by the next receiver, return *false*.
         */
        override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
            /**
             * Release pointer capture on any key press.
             */
            releasePointerCapture()
            return super.onKeyDown(keyCode, event)
        }

        /**
         * Convenience function to multiply the X and Y angles calculated from a [MotionEvent] by a
         * scale factor and to use these values to update the `mAngleX` and `mAngleY` fields of our
         * [CubeRenderer] field [mRenderer] and then to call the [requestRender] method to request
         * that if render a frame with the new angles.
         *
         * @param dx unscaled angle in degrees for rotation around the y axis
         * @param dy unscaled angle in degrees for rotation around the x axis
         * @param scaleFactor scale factor to multiply [dx] and [dy] by before updating angles
         */
        private fun updateAngles(dx: Float, dy: Float, scaleFactor: Float) {
            if (dx != 0f && dy != 0f) {
                mRenderer.mAngleX += dx * scaleFactor
                mRenderer.mAngleY += dy * scaleFactor
                requestRender()
            }
        }

        /**
         * Render a [Cube], rotating the model view matrix according to the values of our fields
         * [mAngleX] and [mAngleY] before asking our [Cube] field [mCube] to draw itself.
         */
        private class CubeRenderer : Renderer {
            /**
             * [Cube] instance we have draw itself in our [GLSurfaceView].
             */
            private val mCube: Cube = Cube()

            /**
             * Angle in degrees to rotate our model view matrix around the y axis before having our
             * [Cube] field [mCube] draw itself
             */
            var mAngleX = 0f

            /**
             * Angle in degrees to rotate our model view matrix around the x axis before having our
             * [Cube] field [mCube] draw itself
             */
            var mAngleY = 0f

            /**
             * Called to draw the current frame. First we clear the color buffer and the depth buffer.
             * Then we set the current matrix mode to GL_MODELVIEW, load it with the identity matrix,
             * and translate it by -3.0 along the z axis. We next rotate the matrix by [mAngleX]
             * degrees around the y axis and [mAngleY] degrees around the x axis. We enable the client
             * side capability GL_VERTEX_ARRAY (the vertex array is enabled for writing and used
             * during rendering), and the client side capability GL_COLOR_ARRAY (the color array is
             * enabled for writing and used during rendering). Finally we instruct our [Cube] field
             * [mCube] to draw itself.
             *
             * @param gl the GL interface.
             */
            override fun onDrawFrame(gl: GL10) {
                /**
                 * Usually, the first thing one might want to do is to clear
                 * the screen. The most efficient way of doing this is to use
                 * glClear().
                 */
                gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
                /**
                 * Now we're ready to draw some 3D objects
                 */
                gl.glMatrixMode(GL10.GL_MODELVIEW)
                gl.glLoadIdentity()
                gl.glTranslatef(0f, 0f, -3.0f)
                gl.glRotatef(mAngleX, 0f, 1f, 0f)
                gl.glRotatef(mAngleY, 1f, 0f, 0f)
                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
                gl.glEnableClientState(GL10.GL_COLOR_ARRAY)
                mCube.draw(gl)
            }

            /**
             * Called when the surface changed size. Called after the surface is created and whenever
             * the OpenGL ES surface size changes. We set our view port to have its lower left corner
             * at (0,0), a width of [width] and a height of [height]. We calculate the [Float] aspect
             * ratio `val ratio` to be `width/height`, set the current matrix mode to GL_PROJECTION,
             * load it with the identity matrix, and multiply the current matrix by a perspective
             * matrix with the left clipping plane at `-ratio`, the right clipping plane at `ratio`,
             * the bottom clipping plane at -1, the top clipping plane at 1, the near clipping plane
             * at 1 and the far clipping plane at 10.
             *
             * @param gl     the GL interface.
             * @param width  new width of the surface view
             * @param height new height of the surface view
             */
            override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
                gl.glViewport(0, 0, width, height)
                /**
                 * Set our projection matrix. This doesn't have to be done
                 * each time we draw, but usually a new projection needs to
                 * be set when the viewport is resized.
                 */
                val ratio = width.toFloat() / height
                gl.glMatrixMode(GL10.GL_PROJECTION)
                gl.glLoadIdentity()
                gl.glFrustumf(
                    /* left = */ -ratio,
                    /* right = */ ratio,
                    /* bottom = */ -1f,
                    /* top = */ 1f,
                    /* zNear = */ 1f,
                    /* zFar = */ 10f
                )
            }

            /**
             * Called when the surface is created or recreated. Called when the rendering thread
             * starts and whenever the EGL context is lost. The EGL context will typically be lost
             * when the Android device awakes after going to sleep.
             *
             * First we disable the server side capability GL_DITHER (so the server will not dither
             * color components or indices before they are written to the color buffer). We set the
             * implementation specific hint GL_PERSPECTIVE_CORRECTION_HINT to GL_FASTEST (results in
             * simple linear interpolation of colors and/or texture coordinates). We set the clear
             * color to white, enable the server side capability GL_CULL_FACE (cull polygons based
             * on their winding in window coordinates), set the shade model to GL_SMOOTH (causes the
             * computed colors of vertices to be interpolated as the primitive is rasterized, typically
             * assigning different colors to each resulting pixel fragment), and finally enable the
             * server side capability GL_DEPTH_TEST (do depth comparisons and update the depth buffer).
             *
             * @param gl     the GL interface.
             * @param config the EGLConfig of the created surface. Unused
             */
            override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
                /**
                 * By default, OpenGL enables features that improve quality
                 * but reduce performance. One might want to tweak that
                 * especially on software renderer.
                 */
                gl.glDisable(/* cap = */ GL10.GL_DITHER)
                /**
                 * Some one-time OpenGL initialization can be made here
                 * probably based on features of this particular context
                 */
                gl.glHint(
                    /* target = */ GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                    /* mode = */ GL10.GL_FASTEST
                )
                gl.glClearColor(/* red = */ 1f, /* green = */ 1f, /* blue = */ 1f, /* alpha = */ 1f)
                gl.glEnable(/* cap = */ GL10.GL_CULL_FACE)
                gl.glShadeModel(/* mode = */ GL10.GL_SMOOTH)
                gl.glEnable(/* cap = */ GL10.GL_DEPTH_TEST)
            }

        }

        companion object {
            /**
             * Scale factor for a ACTION_MOVE touch movement. Scales a dx or dy movement to an angle
             * to rotate the `Cube`.
             */
            private const val TOUCH_SCALE_FACTOR = 180.0f / 320

            /**
             * Scale factor for a trackball `MotionEvent`. Scales a x or y movement of the trackball
             * to an angle to rotate the `Cube`.
             */
            private const val TRACKBALL_SCALE_FACTOR = 36.0f
        }

        /**
         * The init block of our constructor.We initialize our `CubeRenderer` field `mRenderer` with
         * a new instance of `CubeRenderer`. We set our renderer to `mRenderer` and set our render
         * mode to RENDERMODE_WHEN_DIRTY.
         */
        init {
            mRenderer = CubeRenderer()
            setRenderer(mRenderer)
            renderMode = RENDERMODE_WHEN_DIRTY
        }
    }
}