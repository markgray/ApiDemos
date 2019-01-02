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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Wrapper activity demonstrating the use of {@link GLSurfaceView}, a view
 * that uses OpenGL drawing into a dedicated surface.
 *
 * Shows:
 * + How to redraw in response to user input. Draws a cube, and allows the user to rotate it using
 * their finger.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class TouchRotateActivity extends Activity {

    /**
     * The {@code GLSurfaceView} ({@code TouchSurfaceView}) we use for drawing
     */
    private GLSurfaceView mGLSurfaceView;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we initialize our field {@code GLSurfaceView mGLSurfaceView} with a new
     * instance of {@code TouchSurfaceView}, give it focus, and allow it to receive focus while in
     * touch mode.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create our Preview view and set it as the content of our
        // Activity
        mGLSurfaceView = new TouchSurfaceView(this);
        setContentView(mGLSurfaceView);
        mGLSurfaceView.requestFocus();
        mGLSurfaceView.setFocusableInTouchMode(true);
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or {@link #onPause}, for
     * your activity to start interacting with the user. First we call through to our super's
     * implementation of {@code onResume}, then we pass the call on to the {@code onResume} method
     * of our field {@code mGLSurfaceView}.
     */
    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mGLSurfaceView.onResume();
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of {@code onPause},
     * then we pass the call on to the {@code onPause} method of our field {@code mGLSurfaceView}.
     */
    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mGLSurfaceView.onPause();
    }

    /**
     * Adds a simple rotation control to a {@code GLSurfaceView} which contains a {@code Cube}.
     */
    private static class TouchSurfaceView extends GLSurfaceView {

        /**
         * Scale factor for a ACTION_MOVE touch movement. Scales a dx or dy movement to an angle to
         * rotate the {@code Cube}.
         */
        private static final float TOUCH_SCALE_FACTOR = 180.0f / 320;
        /**
         * Scale factor for a trackball {@code MotionEvent}. Scales a x or y movement of the trackball
         * to an angle to rotate the {@code Cube}.
         */
        private static final float TRACKBALL_SCALE_FACTOR = 36.0f;
        /**
         * Renderer for our {@code GLSurfaceView}.
         */
        private CubeRenderer mRenderer;
        /**
         * Last x coordinate of a {@code MotionEvent} received by our {@code onTouchEvent} override. It
         * is set by all types of {@code MotionEvent} but then only used when the touch event is of the
         * ACTION_MOVE type when it is used along with the new x coordinate of the {@code MotionEvent}
         * to calculate a new angle to rotate for {@code CubeRenderer mRenderer}
         */
        private float mPreviousX;
        /**
         * Last y coordinate of a {@code MotionEvent} received by our {@code onTouchEvent} override. It
         * is set by all types of {@code MotionEvent} but then only used when the touch event is of the
         * ACTION_MOVE type when it is used along with the new y coordinate of the {@code MotionEvent}
         * to calculate a new angle to rotate for {@code CubeRenderer mRenderer}
         */
        private float mPreviousY;

        /**
         * Our constructor. First we call through to our super's constructor, then we initialize our field
         * {@code CubeRenderer mRenderer} with a new instance of {@code CubeRenderer}. We set our renderer
         * to {@code mRenderer} and set our render mode to RENDERMODE_WHEN_DIRTY.
         *
         * @param context {@code Context} to use for resources, "this" when called from the {@code onCreate}
         *                method of the {@code TouchRotateActivity} activity.
         */
        TouchSurfaceView(Context context) {
            super(context);
            mRenderer = new CubeRenderer();
            setRenderer(mRenderer);
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }

        /**
         * We implement this method to handle trackball motion events. We call our method {@code updateAngles}
         * with the X and Y coordinate of the first pointer index of our parameter {@code MotionEvent e}
         * and the scale factor for a trackball {@code MotionEvent} TRACKBALL_SCALE_FACTOR. The
         * {@code updateAngles} method will modify the {@code mAngleX} and {@code mAngleY} fields of our
         * {@code CubeRenderer mRenderer} and call the method {@code requestRender} to request that
         * our renderer render a new frame. Finally we return true to the caller.
         *
         * @param e The motion event.
         * @return True if the event was handled, false otherwise. We always return true
         */
        @Override
        public boolean onTrackballEvent(MotionEvent e) {
            updateAngles(e.getX(), e.getY(), TRACKBALL_SCALE_FACTOR);
            return true;
        }

        /**
         * We implement this method to handle touch screen motion events. We initialize {@code action}
         * with the masked of action of our parameter {@code MotionEvent e}, then branch on the value
         * of {@code action}:
         * <ul>
         *     <li>
         *         ACTION_MOVE: A change has happened during a press gesture (between ACTION_DOWN and
         *         ACTION_UP), we call our method {@code updateAngles} with the difference between
         *         the X and Y coordinates of the first pointer index of our parameter {@code MotionEvent e}
         *         and the previous values stored in our fields {@code mPreviousX} and {@code mPreviousY}
         *         along with the scale factor for a ACTION_MOVE touch movement TOUCH_SCALE_FACTOR. The
         *         {@code updateAngles} method will modify the {@code mAngleX} and {@code mAngleY} fields
         *         of our {@code CubeRenderer mRenderer} and call the method {@code requestRender} to
         *         request that our renderer render a new frame.
         *     </li>
         *     <li>
         *         ACTION_DOWN: A pressed gesture has started, if the source of {@code MotionEvent e}
         *         is SOURCE_MOUSE (input source is a mouse pointing device) we call the method
         *         {@code requestPointerCapture} to request pointer capture mode (When the window has
         *         pointer capture, the mouse pointer icon will disappear and will not change its position.
         *         Further mouse will be dispatched with the source {@link InputDevice#SOURCE_MOUSE_RELATIVE},
         *         and relative position changes will be available through {@link MotionEvent#getX} and
         *         {@link MotionEvent#getY}.) If the source is not SOURCE_MOUSE we call the {@code releasePointerCapture}
         *         method to release pointer capture (if the window does not have pointer capture, this
         *         call will do nothing).
         *     </li>
         * </ul>
         * For all {@code MotionEvent} types we save the value of the X coordinate of {@code e} in
         * {@code mPreviousX} and the value the Y coordinate in {@code mPreviousY}, and return true
         * to the caller to consume the event.
         *
         * @param e The motion event.
         * @return True if the event was handled, false otherwise.
         */
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent e) {
            final int action = e.getActionMasked();
            if (action == MotionEvent.ACTION_MOVE) {
                updateAngles(e.getX() - mPreviousX, e.getY() - mPreviousY, TOUCH_SCALE_FACTOR);
            } else if (action == MotionEvent.ACTION_DOWN) {
                if (e.isFromSource(InputDevice.SOURCE_MOUSE)) {
                    requestPointerCapture();
                } else {
                    releasePointerCapture();
                }
            }
            mPreviousX = e.getX();
            mPreviousY = e.getY();
            return true;
        }

        /**
         * Implement this method to handle captured pointer events If the masked off action of our
         * parameter {@code MotionEvent e} is ACTION_DOWN we call the {@code releasePointerCapture}
         * method to release pointer capture, otherwise we call our method {@code updateAngles} with
         * the X and Y coordinates of {@code e} along with the scale factor for a ACTION_MOVE touch
         * movement TOUCH_SCALE_FACTOR. The {@code updateAngles} method will modify the {@code mAngleX}
         * and {@code mAngleY} fields of our {@code CubeRenderer mRenderer} and call the method
         * {@code requestRender} to request that our renderer render a new frame. Finally we return
         * true to the caller to consume the event.
         *
         * @param e The captured pointer event.
         * @return True if the event was handled, false otherwise.
         * @see #requestPointerCapture()
         */
        @Override
        public boolean onCapturedPointerEvent(MotionEvent e) {
            if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
                releasePointerCapture();
            } else {
                updateAngles(e.getX(), e.getY(), TOUCH_SCALE_FACTOR);
            }
            return true;
        }

        /**
         * Called when a key down event has occurred. We call the {@code releasePointerCapture} method
         * to release pointer capture and return the value returned by our super's implementation of
         * {@code onKeyDown} to the caller.
         *
         * @param keyCode The value in event.getKeyCode().
         * @param event Description of the key event.
         * @return If you handled the event, return true.  If you want to allow
         *         the event to be handled by the next receiver, return false.
         */
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            // Release pointer capture on any key press.
            releasePointerCapture();
            return super.onKeyDown(keyCode, event);
        }

        private void updateAngles(float dx, float dy, float scaleFactor) {
            if (dx != 0 && dy != 0) {
                mRenderer.mAngleX += dx * scaleFactor;
                mRenderer.mAngleY += dy * scaleFactor;
                requestRender();
            }
        }

        /**
         * Render a {@code Cube}, rotating the model view matrix according to the values of our fields
         * {@code mAngleX} and {@code mAngleY} before asking our {@code Cube mCube} to draw itself.
         */
        @SuppressWarnings("WeakerAccess")
        private static class CubeRenderer implements GLSurfaceView.Renderer {
            /**
             * {@code Cube} instance we have draw itself in our {@code GLSurfaceView}.
             */
            private Cube mCube;
            /**
             * Angle in degrees to rotate our model view matrix around the y axis before having our
             * {@code Cube mCube} draw itself
             */
            public float mAngleX;
            /**
             * Angle in degrees to rotate our model view matrix around the x axis before having our
             * {@code Cube mCube} draw itself
             */
            public float mAngleY;

            /**
             * Our constructor. We simply initialize our field {@code Cube mCube} with a new instance of
             * {@code Cube}.
             */
            CubeRenderer() {
                mCube = new Cube();
            }

            /**
             * Called to draw the current frame. First we clear the color buffer and the depth buffer.
             * Then we set the current matrix mode to GL_MODELVIEW, load it with the identity matrix, and
             * translate it by -3.0 along the z axis. We next rotate the matrix by {@code mAngleX} degrees
             * around the y axis and {@code mAngleY} degrees around the x axis. We enable the client side
             * capability GL_VERTEX_ARRAY (the vertex array is enabled for writing and used during rendering),
             * and the client side capability GL_COLOR_ARRAY (the color array is enabled for writing and
             * used during rendering). Finally we instruct our {@code Cube mCube} to draw itself.
             *
             * @param gl the GL interface.
             */
            @Override
            public void onDrawFrame(GL10 gl) {
                /*
                 * Usually, the first thing one might want to do is to clear
                 * the screen. The most efficient way of doing this is to use
                 * glClear().
                 */

                gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

                /*
                 * Now we're ready to draw some 3D objects
                 */

                gl.glMatrixMode(GL10.GL_MODELVIEW);
                gl.glLoadIdentity();
                gl.glTranslatef(0, 0, -3.0f);
                gl.glRotatef(mAngleX, 0, 1, 0);
                gl.glRotatef(mAngleY, 1, 0, 0);

                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

                mCube.draw(gl);
            }

            /**
             * Called when the surface changed size. Called after the surface is created and whenever
             * the OpenGL ES surface size changes. We set our view port to have its lower left corner at
             * (0,0), a width of {@code width} and a height of {@code height}. We calculate the aspect
             * ratio {@code float ratio} to be {@code width/height}, set the current matrix mode to
             * GL_PROJECTION, load it with the identity matrix, and multiply the current matrix by a
             * perspective matrix with the left clipping plane at {@code -ratio}, the right clipping
             * plane at {@code ratio}, the bottom clipping plane at -1, the top clipping plane at 1, the
             * near clipping plane at 1 and the far clipping plane at 10.
             *
             * @param gl     the GL interface.
             * @param width  new width of the surface view
             * @param height new height of the surface view
             */
            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                gl.glViewport(0, 0, width, height);

                /*
                * Set our projection matrix. This doesn't have to be done
                * each time we draw, but usually a new projection needs to
                * be set when the viewport is resized.
                */

                float ratio = (float) width / height;
                gl.glMatrixMode(GL10.GL_PROJECTION);
                gl.glLoadIdentity();
                gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
            }

            /**
             * Called when the surface is created or recreated. Called when the rendering thread starts
             * and whenever the EGL context is lost. The EGL context will typically be lost when the
             * Android device awakes after going to sleep.
             * <p>
             * First we disable the server side capability GL_DITHER (so the server will not dither color
             * components or indices before they are written to the color buffer). We set the implementation
             * specific hint GL_PERSPECTIVE_CORRECTION_HINT to GL_FASTEST (results in simple linear
             * interpolation of colors and/or texture coordinates). We set the clear color to white, enable
             * the server side capability GL_CULL_FACE (cull polygons based on their winding in window
             * coordinates), set the shade model to GL_SMOOTH (causes the computed colors of vertices to
             * be interpolated as the primitive is rasterized, typically assigning different colors to
             * each resulting pixel fragment), and finally enable the server side capability GL_DEPTH_TEST
             * (do depth comparisons and update the depth buffer).
             *
             * @param gl     the GL interface.
             * @param config the EGLConfig of the created surface. Unused
             */
            @Override
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

                gl.glClearColor(1, 1, 1, 1);
                gl.glEnable(GL10.GL_CULL_FACE);
                gl.glShadeModel(GL10.GL_SMOOTH);
                gl.glEnable(GL10.GL_DEPTH_TEST);
            }
        }
    }
}
