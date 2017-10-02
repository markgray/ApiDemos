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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

import static android.R.attr.width;

/**
 * How to redraw in response to user input. Draws a cube, and allows the user to rotate it using
 * their finger.
 */
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
}

/**
 * Adds a simple rotation control to a {@code GLSurfaceView} which contains a {@code Cube}.
 */
@SuppressWarnings("FieldCanBeLocal")
class TouchSurfaceView extends GLSurfaceView {

    /**
     * Scale factor for a ACTION_MOVE touch movement. Scales a dx or dy movement to an angle to
     * rotate the {@code Cube}.
     */
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    /**
     * Scale factor for a trackball {@code MotionEvent}. Scales a x or y movement of the trackball
     * to an angle to rotate the {@code Cube}.
     */
    private final float TRACKBALL_SCALE_FACTOR = 36.0f;
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
    public TouchSurfaceView(Context context) {
        super(context);
        mRenderer = new CubeRenderer();
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    /**
     * We implement this method to handle trackball motion events. The relative movement of the
     * trackball since the last event can be retrieved with {@code MotionEvent.getX()} and
     * {@code MotionEvent.getY()}. These are normalized so that a movement of 1 corresponds to the
     * user pressing one DPAD key (so they will often be fractional values, representing the more
     * fine-grained movement information available from a trackball).
     * <p>
     * We set the value of the {@code mAngleX} field of our {@code CubeRenderer mRenderer} to the
     * relative x movement since the last event scaled by TRACKBALL_SCALE_FACTOR plus the old
     * {@code mAngleX}, and we set the value of the {@code mAngleY} field of {@code mRenderer} to
     * the relative y movement since the last event scaled by TRACKBALL_SCALE_FACTOR plus the old
     * {@code mAngleY}. Then we call the method {@code requestRender} to request that our renderer
     * render a new frame, and return true to the caller.
     *
     * @param e The motion event.
     * @return True if the event was handled, false otherwise. We always return true
     */
    @Override
    public boolean onTrackballEvent(MotionEvent e) {
        mRenderer.mAngleX += e.getX() * TRACKBALL_SCALE_FACTOR;
        mRenderer.mAngleY += e.getY() * TRACKBALL_SCALE_FACTOR;
        requestRender();
        return true;
    }

    /**
     * We implement this method to handle touch screen motion events. We fetch the x coordinate of
     * the {@code MotionEvent e} to {@code float x}, and the y coordinate to {@code float y}. Then
     * we switch on the kind of action of {@code e} and if it is of type ACTION_MOVE we calculate
     * the change in x {@code float dx} by subtracting {@code mPreviousX} from {@code x}, and the
     * change in y {@code float dy} by subtracting {@code mPreviousY} from {@code y}. Next we set
     * the value of the {@code mAngleX} field of our {@code CubeRenderer mRenderer} to {@code dx}
     * scaled by TOUCH_SCALE_FACTOR plus the old {@code mAngleX}, and we set the value of the
     * {@code mAngleY} field of {@code mRenderer} to {@code dy} scaled by TOUCH_SCALE_FACTOR plus
     * the old {@code mAngleY}. Then we call the method {@code requestRender} to request that our
     * renderer render a new frame. For all {@code MotionEvent} types we save the value of {@code x}
     * in {@code mPreviousX} and the value of {@code y} in {@code mPreviousY}, and return true to
     * the caller.
     *
     * @param e The motion event.
     * @return True if the event was handled, false otherwise.
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = x - mPreviousX;
                float dy = y - mPreviousY;
                mRenderer.mAngleX += dx * TOUCH_SCALE_FACTOR;
                mRenderer.mAngleY += dy * TOUCH_SCALE_FACTOR;
                requestRender();
        }
        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    /**
     * Render a {@code Cube}, rotating the model view matrix according to the values of our fields
     * {@code mAngleX} and {@code mAngleY} before asking our {@code Cube mCube} to draw itself.
     */
    @SuppressWarnings("WeakerAccess")
    private class CubeRenderer implements GLSurfaceView.Renderer {

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
        public CubeRenderer() {
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
         * ration {@code float ratio} to be {@code width/height}, set the current matrix mode to
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
