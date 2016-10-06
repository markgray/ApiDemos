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

package com.example.android.apis.graphics;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;

import static android.R.attr.width;

/**
 * Render a pair of tumbling cubes.
 */
public class CubeRenderer implements GLSurfaceView.Renderer {
//  private static String TAG = "CubeRenderer";
    private boolean mTranslucentBackground; // Flag to use a translucent background (glClearColor(0,0,0,0)
    private Cube mCube; // an instance of a vertex shaded cube.
    private float mAngle; // ever increasing angle that is used to rotate the two cubes

    /**
     * Constructor which is used to initialize our fields. After saving the value of the parameter
     * boolean useTranslucentBackground to our field boolean mTranslucentBackground, we create
     * a new instance of Cube and save it in our field Cube mCube.
     *
     * @param useTranslucentBackground use a translucent background (glClearColor(0,0,0,0)
     */
    public CubeRenderer(boolean useTranslucentBackground) {
        mTranslucentBackground = useTranslucentBackground;
        mCube = new Cube();
    }

    /**
     * Called to draw the current frame. First we clear buffers to preset values using glClear and
     * the mask GL_COLOR_BUFFER_BIT (the buffers currently enabled for color writing) or'ed with
     * GL_DEPTH_BUFFER_BIT (the depth buffer). Next we replace the current matrix with the identity
     * matrix, translate the current matrix using a translation matrix using the xyz coordinates
     * (0, 0, -3), rotate the current matrix using the current value of mAngle about the vector
     * (0, 1, 0) (y axis rotation), rotate the current matrix using the value 0.25*mAngle about
     * the vector (1, 0, 0) (x axis rotation). Next we enable the client-side capabilities
     * GL_VERTEX_ARRAY (the vertex array is enabled for writing and used during rendering) and
     * GL_COLOR_ARRAY (the color array is enabled for writing and used during rendering). Then we
     * instruct our instance of Cube mCube to draw itself. Having drawn the first Cube, we now
     * rotate the current matrix using the value 2.0*mAngle about the vector (0, 1, 1), translate
     * the matrix using the xyz coordinates (0.5f, 0.5f, 0.5f), and instruct our Cube mCube to
     * draw itself again using this matrix. Finally we increment mAngle by 1.2 degrees.
     *
     * @param gl the GL interface. Use <code>instanceof</code> to test if the interface supports
     *           GL11 or higher interfaces.
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
        gl.glRotatef(mAngle,        0, 1, 0);
        gl.glRotatef(mAngle*0.25f,  1, 0, 0);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        mCube.draw(gl);

        gl.glRotatef(mAngle*2.0f, 0, 1, 1);
        gl.glTranslatef(0.5f, 0.5f, 0.5f);

        mCube.draw(gl);

        mAngle += 1.2f;
    }

    /**
     * Called after the surface is created and whenever the OpenGL ES surface size changes.
     *
     * @param gl the GL interface. Use <code>instanceof</code> to test if the interface supports
     *           GL11 or higher interfaces.
     * @param width width of the surface in pixels
     * @param height height of the surface in pixels
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
         gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                 GL10.GL_FASTEST);

         if (mTranslucentBackground) {
             gl.glClearColor(0,0,0,0);
         } else {
             gl.glClearColor(1,1,1,1);
         }
         gl.glEnable(GL10.GL_CULL_FACE);
         gl.glShadeModel(GL10.GL_SMOOTH);
         gl.glEnable(GL10.GL_DEPTH_TEST);
    }
}
