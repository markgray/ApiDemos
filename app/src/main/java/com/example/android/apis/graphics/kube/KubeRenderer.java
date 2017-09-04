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

package com.example.android.apis.graphics.kube;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.R.attr.width;

/**
 * Example of how to use OpenGL|ES in a custom view. This is the {@code GLSurfaceView.Renderer}
 * implementation which is set as the renderer of our {@code GLSurfaceView} in the {@code onCreate}
 * override of {@code Kube}.
 */
@SuppressWarnings("WeakerAccess")
class KubeRenderer implements GLSurfaceView.Renderer {

    /**
     * The {@code GLWorld} instance for our rubic cube, it is created in the method {@code Kube.makeGLWorld}
     * and used as an argument to our constructor when the {@code Kube.onCreate} initializes its field
     * {@code KubeRenderer mRenderer} with the instance of {@code KubeRenderer} it uses as the renderer
     * for its field {@code GLSurfaceView mView}.
     */
    private GLWorld mWorld;
    /**
     * The class that implements our interface {@code AnimationCallback}, it is set by one of the
     * arguments to our constructor (in our case it is "this" when {@code Kube.onCreate} initializes
     * its field {@code KubeRenderer mRenderer} with the instance of {@code KubeRenderer} it uses as
     * the renderer for its field {@code GLSurfaceView mView}. We call its method {@code animate}
     * from our {@code onDrawFrame} method when we want the openGL buffers to be updated to the next
     * step of the animation before we call {@code mWorld.draw} to draw the next frame.
     */
    private AnimationCallback mCallback;
    /**
     * Angle to rotate the entire rubic cube before drawing it.
     */
    private float mAngle;

    /**
     * Interface for the callback we call to update the openGL buffers before we call {@code GLWorld.draw}
     * to draw them. It is implemented by {@code Kube} which uses "this" when calling our constructor
     * which sets our field {@code AnimationCallback mCallback} to it, and our {@code onDrawFrame}
     * override calls {@code mCallback.animate}.
     */
    public interface AnimationCallback {
        void animate();
    }

    /**
     * Our constructor, we merely save our argument {@code GLWorld world} in our field {@code mWorld}
     * and our argument {@code AnimationCallback callback} in our field {@code mCallback}.
     *
     * @param world    {@code GLWorld} object containing the model of the rubic cube, we use it for
     *                 its {@code draw} method
     * @param callback Callback we use to update the openGL buffers before instructing {@code mWorld}
     *                 to draw itself.
     */
    public KubeRenderer(GLWorld world, AnimationCallback callback) {
        mWorld = world;
        mCallback = callback;
    }

    /**
     * Called to draw the current frame. First we make sure our field {@code AnimationCallback mCallback}
     * is not null before calling its method {@code animate} to update the openGL buffers for the next
     * frame to be drawn. Next we set the clear color to Gray, and clear both the color buffer and the
     * depth buffer. Then we set the matrix mode to GL_MODELVIEW, load the identity matrix, translate
     * it to (0, 0, 3.0f), set the scale factors for all three axes to 0.5, rotate it by {@code mAngle}
     * around the y axis, and rotate it by {@code mAngle*0.25} around the x axis. We set the current
     * color to a darker shade of Gray, enable the client-side capability GL_VERTEX_ARRAY and also
     * GL_COLOR_ARRAY (the vertex buffer and color buffer are enabled for writing and used for rendering
     * when {@code glDrawElements} is called). We enable server-side GL capability GL_CULL_FACE (cull
     * polygons based on their winding in window coordinates), set the shade model to GL_SMOOTH
     * (causes the computed colors of vertices to be interpolated as the primitive is rasterized),
     * and enable the server-side GL capability GL_DEPTH_TEST (do depth comparisons and update the
     * depth buffer).
     * <p>
     * Finally we call the method {@code mWorld.draw} to issue the final openGL commands to draw the
     * rubic cube.
     *
     * @param gl the GL interface.
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        if (mCallback != null) {
            mCallback.animate();
        }

        /*
         * Usually, the first thing one might want to do is to clear
         * the screen. The most efficient way of doing this is to use
         * glClear(). However we must make sure to set the scissor
         * correctly first. The scissor is always specified in window
         * coordinates: Obsolete comment - we do now use the GL_SCISSOR_TEST.
         */

        gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        /*
         * Now we're ready to draw some 3D object
         */

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(0, 0, -3.0f);
        gl.glScalef(0.5f, 0.5f, 0.5f);
        gl.glRotatef(mAngle, 0, 1, 0);
        gl.glRotatef(mAngle * 0.25f, 1, 0, 0);

        gl.glColor4f(0.7f, 0.7f, 0.7f, 1.0f);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);

        mWorld.draw(gl);
    }

    /**
     * Called when the surface changed size.
     *
     * @param gl     the GL interface.
     * @param width  new width of our {@code GLSurfaceView}
     * @param height new height of our {@code GLSurfaceView}
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);

        /*
         * Set our projection matrix. This doesn't have to be done
         * each time we draw, but usually a new projection needs to be set
         * when the viewport is resized.
         */

        float ratio = (float) width / height;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 2, 12);

        /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER);
        gl.glActiveTexture(GL10.GL_TEXTURE0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Nothing special, don't have any textures we need to recreate.
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }

    public float getAngle() {
        return mAngle;
    }
}
