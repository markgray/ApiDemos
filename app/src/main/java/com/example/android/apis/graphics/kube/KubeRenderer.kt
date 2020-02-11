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
package com.example.android.apis.graphics.kube

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Example of how to use OpenGL|ES in a custom view. This is the [GLSurfaceView.Renderer]
 * implementation which is set as the renderer of our [GLSurfaceView] in the `onCreate`
 * override of [Kube].
 */
internal class KubeRenderer
/**
 * Our constructor, we merely save our argument `GLWorld world` in our field `mWorld`
 * and our argument `AnimationCallback callback` in our field `mCallback`.
 *
 * Parameter: world    `GLWorld` object containing the model of the rubic cube, we use it for
 * its `draw` method
 * Parameter: callback Callback we use to update the openGL buffers before instructing `mWorld`
 * to draw itself.
 */(
        /**
         * The `GLWorld` instance for our rubic cube, it is created in the method `Kube.makeGLWorld`
         * and used as an argument to our constructor when the `Kube.onCreate` initializes its field
         * `KubeRenderer mRenderer` with the instance of `KubeRenderer` it uses as the renderer
         * for its field `GLSurfaceView mView`.
         */
        private val mWorld: GLWorld,
        /**
         * The class that implements our interface `AnimationCallback`, it is set by one of the
         * arguments to our constructor (in our case it is "this" when `Kube.onCreate` initializes
         * its field `KubeRenderer mRenderer` with the instance of `KubeRenderer` it uses as
         * the renderer for its field `GLSurfaceView mView`. We call its method `animate`
         * from our `onDrawFrame` method when we want the openGL buffers to be updated to the next
         * step of the animation before we call `mWorld.draw` to draw the next frame.
         */
        private val mCallback: AnimationCallback?) : GLSurfaceView.Renderer {
    /**
     * Getter method for our field `float mAngle`.
     *
     * @return current value of our field `float mAngle`
     */
    /**
     * Setter method for our field `float mAngle`.
     *
     * Parameter: angle angle in degrees to set `mAngle` to
     */
    /**
     * Angle in degrees to rotate the entire rubic cube before drawing it.
     */
    var angle = 0f

    /**
     * Interface for the callback we call to update the openGL buffers before we call `GLWorld.draw`
     * to draw them. It is implemented by `Kube` which uses "this" when calling our constructor
     * which sets our field `AnimationCallback mCallback` to it, and our `onDrawFrame`
     * override calls `mCallback.animate`.
     */
    interface AnimationCallback {
        fun animate()
    }

    /**
     * Called to draw the current frame. First we make sure our field `AnimationCallback mCallback`
     * is not null before calling its method `animate` to update the openGL buffers for the next
     * frame to be drawn. Next we set the clear color to Gray, and clear both the color buffer and the
     * depth buffer. Then we set the matrix mode to GL_MODELVIEW, load the identity matrix, translate
     * it to (0, 0, 3.0f), set the scale factors for all three axes to 0.5, rotate it by `mAngle`
     * around the y axis, and rotate it by `mAngle*0.25` around the x axis. We set the current
     * color to a darker shade of Gray, enable the client-side capability GL_VERTEX_ARRAY and also
     * GL_COLOR_ARRAY (the vertex buffer and color buffer are enabled for writing and used for rendering
     * when `glDrawElements` is called). We enable server-side GL capability GL_CULL_FACE (cull
     * polygons based on their winding in window coordinates), set the shade model to GL_SMOOTH
     * (causes the computed colors of vertices to be interpolated as the primitive is rasterized),
     * and enable the server-side GL capability GL_DEPTH_TEST (do depth comparisons and update the
     * depth buffer).
     *
     *
     * Finally we call the method `mWorld.draw` to issue the final openGL commands to draw the
     * rubic cube.
     *
     * @param gl the GL interface.
     */
    override fun onDrawFrame(gl: GL10) {
        mCallback?.animate()
        /*
         * Usually, the first thing one might want to do is to clear
         * the screen. The most efficient way of doing this is to use
         * glClear(). However we must make sure to set the scissor
         * correctly first. The scissor is always specified in window
         * coordinates: Obsolete comment - we do now use the GL_SCISSOR_TEST.
         */gl.glClearColor(0.5f, 0.5f, 0.5f, 1f)
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        /*
         * Now we're ready to draw some 3D object
         */gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glLoadIdentity()
        gl.glTranslatef(0f, 0f, -3.0f)
        gl.glScalef(0.5f, 0.5f, 0.5f)
        gl.glRotatef(angle, 0f, 1f, 0f)
        gl.glRotatef(angle * 0.25f, 1f, 0f, 0f)
        gl.glColor4f(0.7f, 0.7f, 0.7f, 1.0f)
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY)
        gl.glEnable(GL10.GL_CULL_FACE)
        gl.glShadeModel(GL10.GL_SMOOTH)
        gl.glEnable(GL10.GL_DEPTH_TEST)
        mWorld.draw(gl)
    }

    /**
     * Called when the surface changed size. First we set the viewport to have its lower left corner
     * at (0,0), a width of `width` and a height of `height`. Next we calculate the
     * aspect ratio `float ratio` to be `width/height`, specify GL_PROJECTION to be the
     * current matrix (the matrix used to create your viewing volume), load it with the identity
     * matrix, set coordinates for the left and right vertical clipping planes to `-ratio`
     * and `+ratio`, set the bottom clipping plane to -1, the top clipping plane to +2, the
     * near clipping plane to 2, and the far clipping plane to 12. We disable the server-side GL
     * capability GL_DITHER (do not dither color components or indices before they are written to the
     * color buffer), and finally set the active texture to GL_TEXTURE0 (selects which texture unit
     * subsequent texture state calls will affect - we do not use textures so?).
     *
     * @param gl     the GL interface.
     * @param width  new width of our `GLSurfaceView`
     * @param height new height of our `GLSurfaceView`
     */
    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        gl.glViewport(0, 0, width, height)
        /*
         * Set our projection matrix. This doesn't have to be done
         * each time we draw, but usually a new projection needs to be set
         * when the viewport is resized.
         */
        val ratio = width.toFloat() / height
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()
        gl.glFrustumf(-ratio, ratio, -1f, 1f, 2f, 12f)
        /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */gl.glDisable(GL10.GL_DITHER)
        gl.glActiveTexture(GL10.GL_TEXTURE0)
    }

    /**
     * Called when the surface is created or recreated. We do nothing.
     *
     * @param gl     the GL interface. Use `instanceof` to
     * test if the interface supports GL11 or higher interfaces.
     * @param config the EGLConfig of the created surface. Can be used
     * to create matching pbuffers.
     */
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) { // Nothing special, don't have any textures we need to recreate.
    }

}