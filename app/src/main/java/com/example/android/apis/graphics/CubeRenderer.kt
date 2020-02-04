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
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Render a pair of tumbling cubes.
 */
class CubeRenderer(
        /**
         * Flag to use a translucent background (glClearColor(0,0,0,0)
         */
        private val mTranslucentBackground : Boolean
) : GLSurfaceView.Renderer {
    /**
     * an instance of a vertex shaded cube
     */
    private val mCube : Cube = Cube()
    /**
     * ever increasing angle that is used to rotate the two cubes
     */
    private var mAngle = 0f

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
     * @param gl the GL interface. Use **instanceof** to test if the interface supports
     * GL11 or higher interfaces.
     */
    override fun onDrawFrame(gl: GL10) { /*
         * Usually, the first thing one might want to do is to clear
         * the screen. The most efficient way of doing this is to use
         * glClear().
         */
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        /*
         * Now we're ready to draw some 3D objects
         */gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glLoadIdentity()
        gl.glTranslatef(0f, 0f, -3.0f)
        gl.glRotatef(mAngle, 0f, 1f, 0f)
        gl.glRotatef(mAngle * 0.25f, 1f, 0f, 0f)
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY)
        mCube.draw(gl)
        gl.glRotatef(mAngle * 2.0f, 0f, 1f, 1f)
        gl.glTranslatef(0.5f, 0.5f, 0.5f)
        mCube.draw(gl)
        mAngle += 1.2f
    }

    /**
     * Called after the surface is created and whenever the OpenGL ES surface size changes. First
     * we calculate the "ratio" of the width to the height, set the target matrix stack to
     * GL_PROJECTION to specify that subsequent matrix operations apply to the projection matrix
     * stack, load the identity matrix into the projection matrix, and finally multiply the current
     * matrix by a perspective matrix with the coordinates for the left and right vertical clipping
     * planes set to -ratio and +ratio respectively, the coordinates for the bottom and top
     * horizontal clipping planes set to -1 and +1 respectively, and the distances to the near and
     * far depth clipping planes set to 1 and 10 respectively.
     *
     * @param gl     the GL interface. Use **instanceof** to test if the interface supports
     * GL11 or higher interfaces.
     * @param width  width of the surface in pixels
     * @param height height of the surface in pixels
     */
    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        gl.glViewport(0, 0, width, height)
        /*
          * Set our projection matrix. This doesn't have to be done
          * each time we draw, but usually a new projection needs to
          * be set when the viewport is resized.
          */
        val ratio = width.toFloat() / height
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()
        gl.glFrustumf(-ratio, ratio, -1f, 1f, 1f, 10f)
    }

    /**
     * Called when the surface is created or recreated. Part of the GLSurfaceView.Renderer interface.
     *
     *
     * Called when the rendering thread starts and whenever the EGL context is lost. The EGL context
     * will typically be lost when the Android device awakes after going to sleep.
     *
     *
     * Since this method is called at the beginning of rendering, as well as every time the EGL
     * context is lost, this method is a convenient place to put code to create resources that
     * need to be created when the rendering starts, and that need to be recreated when the EGL
     * context is lost. Textures are an example of a resource that you might want to create here.
     *
     *
     * Note that when the EGL context is lost, all OpenGL resources associated with that context
     * will be automatically deleted. You do not need to call the corresponding "glDelete" methods
     * such as glDeleteTextures to manually delete these lost resources.
     *
     *
     * First to improve performance we disable the GL_DITHER GL capability (dithers color components
     * or indices before they are written to the color buffer). Next we specify implementation
     * specific hint GL_PERSPECTIVE_CORRECTION_HINT (Indicates the quality of color and texture
     * coordinate interpolation) with the mode GL_FASTEST (The most efficient option should be
     * chosen). If this instance was created with useTranslucentBackground true we specify the clear
     * value for the color buffers to be all 0, and if false we specify the clear value for the
     * color buffers to be all 1. Next we enable the GL capability GL_CULL_FACE (cull polygons based
     * on their winding in window coordinates), set the shade model to GL_SMOOTH (smooth shading),
     * and finally enable the GL capability GL_DEPTH_TEST (do depth comparisons and update the depth
     * buffer).
     *
     * @param gl     the GL interface. Use **instanceof** to test if the interface supports
     * GL11 or higher interfaces.
     * @param config the EGLConfig of the created surface. Can be used to create matching pbuffers.
     */
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) { /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER)
        /*
         * Some one-time OpenGL initialization can be made here
         * probably based on features of this particular context
         */gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST)
        if (mTranslucentBackground) {
            gl.glClearColor(0f, 0f, 0f, 0f)
        } else {
            gl.glClearColor(1f, 1f, 1f, 1f)
        }
        gl.glEnable(GL10.GL_CULL_FACE)
        gl.glShadeModel(GL10.GL_SMOOTH)
        gl.glEnable(GL10.GL_DEPTH_TEST)
    }

}