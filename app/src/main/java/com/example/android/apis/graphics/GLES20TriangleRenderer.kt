/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import com.example.android.apis.R
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Draws a textured rotating triangle using OpenGL ES 2.0 -- used in GLES20Activity.kt
 *
 * @property mContext [Context] we were constructed with, used to retrieve resources.
 * (See our init block for our constructor details)
 */
internal class GLES20TriangleRenderer(
    private val mContext: Context
) : GLSurfaceView.Renderer {

    /**
     * Vertex data for the three vertices of our triangle.
     */
    private val mTriangleVerticesData = floatArrayOf( // X, Y, Z, U, V
        -1.0f, -0.5f, 0f,         // X
        -0.5f, 0.0f, 1.0f,        // Y
        -0.5f, 0f, 1.5f,          // Z
        -0.0f, 0.0f, 1.118034f, // U
        0f, 0.5f, 1.618034f     // V
    )

    /**
     * [FloatBuffer] that we load [mTriangleVerticesData] into then use in a call to
     * `glVertexAttribPointer` to define an array of generic vertex attribute data both for
     * [maPositionHandle] (the location of the attribute variable "aPosition" in our compiled
     * program object [mProgram]) and [maTextureHandle] (the location of the attribute
     * variable "aTextureCoord" in our compiled program object [mProgram])
     */
    @Suppress("JoinDeclarationAndAssignment")
    private val mTriangleVertices: FloatBuffer

    /**
     * Source code for the GL_VERTEX_SHADER part of our shader program [mProgram] (shader that
     * is intended to run on the programmable vertex processor). The statements meanings:
     *
     *  * uniform mat4 uMVPMatrix; # A `uniform` is a global openGL Shading Language variable
     *  declared with the "uniform" storage qualifier. These act as parameters that the user of a
     *  shader program can pass to that program. They are stored in a program object. Uniforms
     *  are so named because they do not change from one execution of a shader program to the next
     *  within a particular rendering call. This makes them unlike shader stage inputs and outputs,
     *  which are often different for each invocation of a program stage. A `mat4` is a
     *  4x4 matrix, and `uMVPMatrix` is the name of the uniform variable which is located
     *  using the method `glGetUniformLocation`, its location assigned to the field
     *  `muMVPMatrixHandle` and changed using the method `glUniformMatrix4fv` in our
     *  `onDrawFrame` method. It is used to feed the Model View Projection matrix
     *  `mMVPMatrix` which rotates the triangle a little bit every frame.
     *
     *  * attribute vec4 aPosition; # An `attribute` is used to feed data from the vertex
     *  array object, with the index into that object for the vertices being fed it set by the
     *  method `glVertexAttribPointer`, a `vec4` is a 4-component float vector,
     *  `aPosition` is the name of the attribute, and its location is located and assigned
     *  to the field `maPositionHandle` using the method `glGetAttribLocation`. It
     *  is used to feed the (x,y,z) coordinates to the shader program.
     *
     *  * attribute vec2 aTextureCoord; # Like `aPosition` but a 2-component float vector,
     *  with the location assigned to the field `maTextureHandle`. It is used to feed
     *  the (u,v) texture coordinates to the shader program.
     *
     *  * varying vec2 vTextureCoord; # A `varying` variable provides an interface between
     *  Vertex and Fragment Shader. Vertex Shaders compute values per vertex and fragment shaders
     *  compute values per fragment. If you define a varying variable in a vertex shader, its
     *  value will be interpolated (perspective-correct) over the primitive being rendered and
     *  you can access the interpolated value in the fragment shader. We use it simply to pass
     *  the value of `aTextureCoord` for this vertex to the fragment shader.
     *
     *  * void main() { # Each shader's entry point is at its `main` function where we
     *  process any input variables and output the results in its output variables.
     *
     *  * gl_Position = uMVPMatrix * aPosition; # `gl_Position` is a built-in variable for
     *  the clip-space output position of the current vertex, and is intended for writing the
     *  homogeneous vertex position. It can be written at any time during vertex shader execution.
     *  This value will be used by primitive assembly, clipping, culling, and other fixed
     *  functionality operations, if present, that operate on primitives after vertex processing
     *  has occurred. Its value is undefined after the vertex processing stage if the vertex
     *  shader executable does not write gl_Position. We calculate it by multiplying the (x,y,z)
     *  coordinates of the vertex fed us in `aPosition` by the Model View Projection matrix
     *  `uMVPMatrix` which rotates the vertex to the current position.
     *
     *  * vTextureCoord = aTextureCoord; # We merely pass on the (u,v) texture coordinates of this
     *  vertex fed us in `aTextureCoord` to the fragment shader using `vTextureCoord`.
     *
     *  * } # That's all folks!
     */
    private val mVertexShader = "uniform mat4 uMVPMatrix;\n" +
        "attribute vec4 aPosition;\n" +
        "attribute vec2 aTextureCoord;\n" +
        "varying vec2 vTextureCoord;\n" +
        "void main() {\n" +
        "  gl_Position = uMVPMatrix * aPosition;\n" +
        "  vTextureCoord = aTextureCoord;\n" +
        "}\n"

    /**
     * Source code for the GL_FRAGMENT_SHADER part of our shader program [mProgram] (shader that
     * is intended to run on the programmable fragment processor). The statements meanings:
     *
     *  * precision mediump float; # Specifies the use of medium precision for `float` calculations.
     *
     *  * varying vec2 vTextureCoord; # Storage for the vertex shader to use to pass us the (u,v)
     *  texture coordinates for the vertex.
     *
     *  * uniform sampler2D sTexture; # A `uniform` is a global openGL Shading Language variable
     *  declared with the "uniform" storage qualifier. These act as parameters that the user of a
     *  shader program can pass to that program. They are stored in a program object. Uniforms
     *  are so named because they do not change from one execution of a shader program to the next
     *  within a particular rendering call. This makes them unlike shader stage inputs and outputs,
     *  which are often different for each invocation of a program stage. A `sampler2D` is
     *  a floating point sampler for a 2 dimensional texture. The only place where you can use a
     *  sampler is in one of the openGL Shader Language standard library's texture lookup functions.
     *  These functions access the texture referred to by the sampler. They take a texture coordinate
     *  as parameters. The name `sTexture` is never used by the java program, so the sampler2D
     *  simply accesses the texture bound to the GL_TEXTURE_2D of the default texture unit GL_TEXTURE0.
     *
     *  * void main() { # As in the vertex shader program
     *
     *  * gl_FragColor = texture2D(sTexture, vTextureCoord); # `gl_FragColor` is a built-in output
     *  variable for setting the `vec4` fragment color. `texture2D` looks up the color
     *  for the coordinates given by `vTextureCoord` using the sampler `sTexture` (which
     *  just our texture we bound to GL_TEXTURE_2D for the default texture unit GL_TEXTURE0.
     *
     *  * } # That's all folks!
     */
    private val mFragmentShader = "precision mediump float;\n" +
        "varying vec2 vTextureCoord;\n" +
        "uniform sampler2D sTexture;\n" +
        "void main() {\n" +
        "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
        "}\n"

    /**
     * Model View Projection Matrix is the multiplication of the Projection Matrix times the View
     * matrix times the Model Matrix, and transforms a vertex from model coordinates to Homogeneous
     * coordinates. We pass this matrix to the vertex shader using `uniform mat4 uMVPMatrix`
     * which we locate using the method `glGetUniformLocation` (saving the location in our
     * variable `muMVPMatrixHandle`) and set using the method `glUniformMatrix4fv`.
     */
    private val mMVPMatrix = FloatArray(16)

    /**
     * Model matrix, translates model coordinates to world coordinates. We set it to a rotation matrix
     * for the current angle of rotation using the method `setRotateM`. It is used to calculate
     * our Model View Projection Matrix [mMVPMatrix] (along with our view matrix [mVMatrix] and
     * projection matrix [mProjMatrix]). It is set every frame in our method [onDrawFrame] to an
     * angle calculated from the system time since boot.
     */
    private val mMMatrix = FloatArray(16)

    /**
     * View matrix, translates world coordinates to camera coordinates. We set it using the method
     * `setLookAtM` to have an (x,y,z) eye point coordinate of (0,0,-5), an (x,y,z) center at
     * (0,0,0), and an "up vector" of (0,1,0). It is set once in our method [onSurfaceCreated].
     */
    private val mVMatrix = FloatArray(16)

    /**
     * Projection matrix, translates camera coordinates to Homogeneous Space (perspective is included).
     * We set it using the method `frustumM` to have  (mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
     * a left clipping plane of the negative of the aspect ration, a right clipping plane of the
     * aspect ration, bottom clipping plane of -1, top clipping plane of 1, near clipping plane of 3,
     * and a far clipping plane of 7. It is set once in our method [onSurfaceChanged].
     */
    private val mProjMatrix = FloatArray(16)

    /**
     * Compiled and linked Program object which contains an executable that will run its GL_VERTEX_SHADER
     * shader object on the vertex processor and run its GL_FRAGMENT_SHADER object on the fragment
     * processor. Its code comes from [String] field [mVertexShader] and [String] field [mFragmentShader],
     * and it is compiled and linked by our method [createProgram] using GLES20 utility methods.
     */
    private var mProgram = 0

    /**
     * Texture name we use for our texture, generated using `glGenTextures`, and bound to
     * GL_TEXTURE_2D in [onSurfaceCreated] in order to upload our png resource, then again in
     * [onDrawFrame] in order to draw using it.
     */
    private var mTextureID = 0

    /**
     * The location of the GL vector shader program's uniform variable "uMVPMatrix" located using
     * `glGetUniformLocation` in our method [onSurfaceCreated] and used to upload our
     * Model View Projection Matrix [mMVPMatrix] to the GL shader program in [onDrawFrame]
     * using the method `glUniformMatrix4fv`
     */
    private var muMVPMatrixHandle = 0

    /**
     * The location of the GL vector shader program's `attribute vec4 aPosition` located using
     * `glGetAttribLocation` in [onSurfaceCreated] and used to initialize `aPosition` to access
     * the (x,y,z) coordinates of each vertex as they are processed from the VBO using
     * `glVertexAttribPointer` in [onDrawFrame]
     */
    private var maPositionHandle = 0

    /**
     * The location of the GL vector shader program's `attribute vec2 aTextureCoord` located
     * using `glGetAttribLocation` in [onSurfaceCreated] and used to initialize
     * `aTextureCoord` to access the (u,v) coordinates of each vertex as they are processed
     * from the VBO using `glVertexAttribPointer` in [onDrawFrame]
     */
    private var maTextureHandle = 0

    /**
     * Called to draw the current frame. First we set the clear color to a dark blue, and clear both
     * the depth buffer and the color buffer. Then we install our program object with the handle
     * [mProgram] as part of current rendering state, and afterwards call our method [checkGlError]
     * which will throw a [RuntimeException] if any openGL error occurred. We select GL_TEXTURE0 to
     * be the active texture unit, and bind our texture named [mTextureID] to the texture target
     * GL_TEXTURE_2D (While a texture is bound, GL operations on the target to which it is bound
     * affect the bound texture, and queries of the target to which it is bound return state from
     * the bound texture. If texture mapping is active on the target to which a texture is bound,
     * the bound texture is used. In effect, the texture targets become aliases for the textures
     * currently bound to them, and the texture name zero refers to the default textures that were
     * bound to them at initialization).
     *
     * We position [mTriangleVertices] to the offset to the location of the (x,y,z) coordinates
     * TRIANGLE_VERTICES_DATA_POS_OFFSET (0), and then use the method `glVertexAttribPointer`
     * to initialize the vertex shader program's `attribute vec4 aPosition` (whose location we
     * have stored in [maPositionHandle]) by defining its array of generic vertex attribute data.
     * This array has 3 components (x,y,z), is of type GL_FLOAT, does not need to be normalized, has
     * a stride of TRIANGLE_VERTICES_DATA_STRIDE_BYTES (20), and draws its data from our [FloatBuffer]
     * field [mTriangleVertices]. We then call our method [checkGlError] which will throw a
     * [RuntimeException] if any openGL error occurred.
     *
     * Next we position [mTriangleVertices] to the offset to the location of the (u,v) texture
     * coordinates TRIANGLE_VERTICES_DATA_UV_OFFSET (3), enable the vertex attribute array given by
     * the handle [maPositionHandle] using the method `glEnableVertexAttribArray` (If enabled, the
     * values in the generic vertex attribute array will be accessed and used for rendering when
     * calls are made to vertex array commands such as `glDrawArrays` or `glDrawElements`). And
     * again we call our method [checkGlError] which will throw a [RuntimeException] if any openGL
     * error occurred.
     *
     * We now use the method `glVertexAttribPointer` to initialize the vertex shader program's
     * `attribute vec2 aTextureCoord` (whose location we have stored in [maTextureHandle]) by defining
     * its array of generic vertex attribute data. This array has 2 components (u,v), is of type
     * GL_FLOAT, does not need to be normalized, has a stride of TRIANGLE_VERTICES_DATA_STRIDE_BYTES
     * (20), and draws its data from our [FloatBuffer] field [mTriangleVertices]. We then call
     * our method [checkGlError] which will throw a [RuntimeException] if any openGL error occurred.
     *
     * We enable the vertex attribute array given by the handle [maTextureHandle] using the
     * method `glEnableVertexAttribArray` (as above for [maPositionHandle]), and call our method
     * [checkGlError] which will throw a [RuntimeException] if any openGL error occurred.
     *
     * We fetch the milliseconds since boot modulo 4000 to initialize [Long] variable `val time` and
     * use it to calculate [Float] variable `val angle` (an angle in degrees which goes from 0 to 360
     * in those 4000 milliseconds and repeats). We set our model matrix [mMMatrix] to a rotation
     * matrix of `angle` around the z axis, set our model view projection matrix [mMVPMatrix] to the
     * view matrix [mVMatrix] times [mMMatrix] and then set [mMVPMatrix] to the projection matrix
     * [mProjMatrix] times that value of [mMVPMatrix].
     *
     * We next use the method `glUniformMatrix4fv` to specify the value of the uniform variable
     * `uniform mat4 uMVPMatrix` for the current program object whose location we have stored
     * in [mMVPMatrix] specifying that 1 matrix is to be modified, no need to transpose, the values
     * are to come from [mMVPMatrix], with an offset of 0.
     *
     * We now use the method `glDrawArrays` to request that GL_TRIANGLES be drawn starting from
     * vertex 0, with 3 indices to be rendered, and call our method [checkGlError] which will throw
     * a [RuntimeException] if any openGL error occurred.
     *
     * @param glUnused The [GL10] interface, but we don't use it
     */
    override fun onDrawFrame(glUnused: GL10) {
        /**
         * Ignore the passed-in GL10 interface, and use the GLES20
         * class's static methods instead.
         */
        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(mProgram)
        checkGlError("glUseProgram")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID)
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET)
        GLES20.glVertexAttribPointer(
            maPositionHandle, 3, GLES20.GL_FLOAT, false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices
        )
        checkGlError("glVertexAttribPointer maPosition")
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET)
        GLES20.glEnableVertexAttribArray(maPositionHandle)
        checkGlError("glEnableVertexAttribArray maPositionHandle")
        GLES20.glVertexAttribPointer(
            maTextureHandle, 2, GLES20.GL_FLOAT, false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices
        )
        checkGlError("glVertexAttribPointer maTextureHandle")
        GLES20.glEnableVertexAttribArray(maTextureHandle)
        checkGlError("glEnableVertexAttribArray maTextureHandle")
        val time = SystemClock.uptimeMillis() % 4000L
        val angle = 0.090f * time.toInt()
        Matrix.setRotateM(
            /* rm = */ mMMatrix,
            /* rmOffset = */ 0,
            /* a = */ angle,
            /* x = */ 0f, /* y = */ 0f, /* z = */ 1.0f
        )
        Matrix.multiplyMM(
            /* result = */ mMVPMatrix,/* resultOffset = */ 0,
            /* lhs = */ mVMatrix,/* lhsOffset = */ 0,
            /* rhs = */ mMMatrix, /* rhsOffset = */ 0
        )
        Matrix.multiplyMM(
            /* result = */ mMVPMatrix, /* resultOffset = */ 0,
            /* lhs = */ mProjMatrix, /* lhsOffset = */ 0,
            /* rhs = */ mMVPMatrix, /* rhsOffset = */ 0
        )
        GLES20.glUniformMatrix4fv(
            /* location = */ muMVPMatrixHandle,
            /* count = */ 1,
            /* transpose = */ false,
            /* value = */ mMVPMatrix,
            /* offset = */ 0
        )
        GLES20.glDrawArrays(/* mode = */ GLES20.GL_TRIANGLES, /* first = */ 0, /* count = */ 3)
        checkGlError("glDrawArrays")
    }

    /**
     * Called when the surface changed size. Called after the surface is created and whenever the
     * OpenGL ES surface size changes. Typically you will set your viewport here. If your camera is
     * fixed then you could also set your projection matrix here.
     *
     * We set our viewport to have the lower left corner at (0,0), a width of [width], and
     * a height of [height]. We calculate the [Float] aspect ration `val ratio` to be the value
     * `width/height`, and then use the method `frustumM` to define the projection matrix
     * [mProjMatrix] to have the left clipping plane at `-ratio`, the right clipping plane
     * at `ratio`, the bottom clipping plane at -1, the top clipping plane at 1, the near clipping
     * plane at 3, and the far clipping plane at 7.
     *
     * @param glUnused The [GL10] interface, but we don't use it
     * @param width    width of the new surface
     * @param height   height of the new surface
     */
    override fun onSurfaceChanged(glUnused: GL10, width: Int, height: Int) {
        /**
         * Ignore the passed-in GL10 interface, and use the GLES20
         * class's static methods instead.
         */
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        Matrix.frustumM(
            /* m = */ mProjMatrix, /* offset = */ 0,
            /* left = */ -ratio, /* right = */ ratio,
            /* bottom = */ -1f, /* top = */ 1f,
            /* near = */ 3f, /* far = */ 7f
        )
    }

    /**
     * Called when the surface is created or recreated. Called when the rendering thread starts and
     * whenever the EGL context is lost. The EGL context will typically be lost when the Android
     * device awakes after going to sleep. Since this method is called at the beginning of rendering,
     * as well as every time the EGL context is lost, this method is a convenient place to put code
     * to create resources that need to be created when the rendering starts, and that need to be
     * recreated when the EGL context is lost. Textures are an example of a resource that you might
     * want to create here.
     *
     * First we call our method [createProgram] to compile and link our two shader programs:
     * the vertex shader [mVertexShader] and the fragment shader [mFragmentShader], saving
     * the handle to the program object it has created in our [Int] field [mProgram]. A zero return
     * indicates it failed to create it, so we just return if this happens.
     *
     * We initialize our field [maPositionHandle] by locating the `attribute vec4 aPosition`
     * within the program object [mProgram] using the method `glGetAttribLocation`, and
     * throw a [RuntimeException] if we failed to locate it.
     *
     * We initialize our field [maTextureHandle] by locating the `attribute vec2 aTextureCoord`
     * within the program object [mProgram] using the method `glGetAttribLocation`, and
     * throw a [RuntimeException] if we failed to locate it.
     *
     * We initialize our field [muMVPMatrixHandle] by locating the `uniform mat4 uMVPMatrix`
     * within the program object [mProgram] using the method `glGetUniformLocation`, and
     * throw a [RuntimeException] if we failed to locate it.
     *
     * Now we create our texture. We use `glGenTextures` to generate a texture name which we
     * save to our field [mTextureID]. We bind it to the target GL_TEXTURE_2D. Then we proceed
     * to configure GL_TEXTURE_2D, setting its texture parameter GL_TEXTURE_MIN_FILTER to GL_NEAREST
     * (the texture minifying function is used whenever the pixel being textured maps to an area
     * greater than one texture element, GL_NEAREST Returns the value of the texture element that is
     * nearest (in Manhattan distance) to the center of the pixel being textured), and its texture
     * parameter GL_TEXTURE_MAG_FILTER to GL_LINEAR (The texture magnification function is used when
     * the pixel being textured maps to an area less than or equal to one texture element, GL_LINEAR
     * Returns the weighted average of the four texture elements that are closest to the center of
     * the pixel being textured). We set the texture parameters GL_TEXTURE_WRAP_S, and GL_TEXTURE_WRAP_T
     * both to GL_REPEAT (causes the integer part of the coordinate to be ignored; the GL uses only
     * the fractional part, thereby creating a repeating pattern).
     *
     * Next we open [InputStream] `val inputStream` to read the raw resource robot.png, declare
     * [Bitmap] `val bitmap` and wrapped in a *try* block intended to catch [IOException] we decode
     * `inputStream` into `bitmap`. We then use the method `texImage2D` to read `bitmap` into
     * GL_TEXTURE_2D, and recycle `bitmap`.
     *
     * Finally we initialize our view matrix [mVMatrix] with a viewing transformation which has
     * the eye point (x,y,z) coordinates of (0,0,-5), the center of the view at (0,0,0), and the up
     * vector of (0,1,0).
     *
     * @param glUnused The [GL10] interface, but we don't use it
     * @param config   the EGLConfig of the created surface. UNUSED
     */
    override fun onSurfaceCreated(glUnused: GL10, config: EGLConfig) {
        /**
         * Ignore the passed-in GL10 interface, and use the GLES20
         * class's static methods instead.
         */
        mProgram = createProgram(mVertexShader, mFragmentShader)
        if (mProgram == 0) {
            return
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
        checkGlError("glGetAttribLocation aPosition")
        if (maPositionHandle == -1) {
            throw RuntimeException("Could not get attrib location for aPosition")
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord")
        checkGlError("glGetAttribLocation aTextureCoord")
        if (maTextureHandle == -1) {
            throw RuntimeException("Could not get attrib location for aTextureCoord")
        }
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        checkGlError("glGetUniformLocation uMVPMatrix")
        if (muMVPMatrixHandle == -1) {
            throw RuntimeException("Could not get attrib location for uMVPMatrix")
        }
        /**
         * Create our texture. This has to be done each time the
         * surface is created.
         */
        val textures = IntArray(size = 1)
        GLES20.glGenTextures(/* n = */ 1, /* textures = */ textures, /* offset = */ 0)
        mTextureID = textures[0]
        GLES20.glBindTexture(/* target = */ GLES20.GL_TEXTURE_2D, /* texture = */ mTextureID)
        GLES20.glTexParameterf(
            /* target = */ GLES20.GL_TEXTURE_2D,
            /* pname = */ GLES20.GL_TEXTURE_MIN_FILTER,
            /* param = */ GLES20.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            /* target = */ GLES20.GL_TEXTURE_2D,
            /* pname = */ GLES20.GL_TEXTURE_MAG_FILTER,
            /* param = */ GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            /* target = */ GLES20.GL_TEXTURE_2D,
            /* pname = */ GLES20.GL_TEXTURE_WRAP_S,
            /* param = */ GLES20.GL_REPEAT
        )
        GLES20.glTexParameteri(
            /* target = */ GLES20.GL_TEXTURE_2D,
            /* pname = */ GLES20.GL_TEXTURE_WRAP_T,
            /* param = */ GLES20.GL_REPEAT
        )
        val inputStream: InputStream = mContext.resources.openRawResource(R.raw.robot)

        @Suppress("JoinDeclarationAndAssignment")
        val bitmap: Bitmap
        bitmap = try {
            BitmapFactory.decodeStream(inputStream)
        } finally {
            try {
                inputStream.close()
            } catch (_: IOException) { // Ignore.
            }
        }
        GLUtils.texImage2D(
            /* target = */ GLES20.GL_TEXTURE_2D,
            /* level = */ 0,
            /* bitmap = */ bitmap,
            /* border = */ 0
        )
        bitmap.recycle()
        Matrix.setLookAtM(
            /* rm = */ mVMatrix, /* rmOffset = */ 0,
            /* eyeX = */ 0f, /* eyeY = */ 0f, /* eyeZ = */ -5f,
            /* centerX = */ 0f, /* centerY = */ 0f, /* centerZ = */ 0f,
            /* upX = */ 0f, /* upY = */ 1.0f, /* upZ = */ 0.0f
        )
    }

    /**
     * Compiles shader source code into a shader object. First we call `glCreateShader` to
     * create an empty shader of type [shaderType] and save the [Int] which we can use to
     * reference it in `var shader`. If `shader` is non-zero we call the method `glShaderSource`
     * to replace the source code in the shader object with the source code contained in our
     * [String] parameter [source]. We then call `glCompileShader` to compile the source code
     * strings that have been stored in the shader object `shader`. We allocate 1 int for the
     * array `val compiled`, then call `glGetShaderiv` to retrieve the compile status of `shader`
     * into `compiled`. A zero returned indicates the compile failed so we log it, delete `shader`
     * and set it to 0. In any case we return the value of `shader` to the caller.
     *
     * @param shaderType type of shader, either GL_VERTEX_SHADER or GL_FRAGMENT_SHADER
     * @param source     string containing source code of shader
     * @return compiled shader object
     */
    private fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES20.glCreateShader(shaderType)
        if (shader != 0) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader $shaderType:")
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader))
                GLES20.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }

    /**
     * Compiles and links the shader source code passed it into a program which can be referenced
     * using the [Int] that it returns. First we call our method [loadShader] to compile our [String]
     * parameter [vertexSource] into a vertex shader whose reference number we save to the variable
     * `val vertexShader`. If the result was 0, we return 0 to the caller, otherwise we call our
     * method [loadShader] to compile our [String] parameter [fragmentSource] into a fragment
     * shader whose reference number we save to the variable `val pixelShader`. If the result
     * was 0, we return 0 to the caller.
     *
     * We create an empty program object by calling `glCreateProgram` saving the reference [Int]
     * returned in `var program`. If the result was non-zero, we call `glAttachShader` to
     * attach the shader object `vertexShader` to the program object `program`. We call
     * our method [checkGlError] to throw a [RuntimeException] if an error occurred. We call
     * `glAttachShader` to attach the shader object `pixelShader` to the program object
     * `program`. We call our method [checkGlError] to throw a [RuntimeException] if an
     * error occurred.
     *
     * We next call `glLinkProgram` to link the program object specified by `program`
     * (A shader object of type GL_VERTEX_SHADER attached to `program` is used to create an
     * executable that will run on the programmable vertex processor. A shader object of type
     * GL_FRAGMENT_SHADER attached to `program` is used to create an executable that will run
     * on the programmable fragment processor).
     *
     * Next we call `glGetProgramiv` to retrieve the link status into [Int] array `val linkStatus`,
     * and if the result is not GL_TRUE we log the error, delete `program` and set it to 0.
     * In either case we return the value of `program` to the caller.
     *
     * @param vertexSource   source code string for the vertex shader program
     * @param fragmentSource source code string for the fragment shader program
     * @return an int which can be used to access the compiled and linked program object
     */
    @Suppress("SameParameterValue")
    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        val pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            return 0
        }
        var program = GLES20.glCreateProgram()
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader)
            checkGlError("glAttachShader")
            GLES20.glAttachShader(program, pixelShader)
            checkGlError("glAttachShader")
            GLES20.glLinkProgram(program)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(
                /* program = */ program,
                /* pname = */ GLES20.GL_LINK_STATUS,
                /* params = */ linkStatus,
                /* offset = */ 0
            )
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ")
                Log.e(TAG, GLES20.glGetProgramInfoLog(program))
                GLES20.glDeleteProgram(/* program = */ program)
                program = 0
            }
        }
        return program
    }

    /**
     * Fetches the next error in the error queue, and if it is not GL_NO_ERROR, logs the error and
     * throws a [RuntimeException].
     *
     * @param op string indicating which operation is checking for an error
     */
    private fun checkGlError(op: String) {
        var error: Int
        while (GLES20.glGetError().also { error = it } != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "$op: glError $error")
            throw RuntimeException("$op: glError $error")
        }
    }

    companion object {
        /**
         * Size in bytes of a float value.
         */
        private const val FLOAT_SIZE_BYTES = 4

        /**
         * Stride in bytes for triangle vertex data. (3 float (x,y,z) coordinates
         * and 2 texture coordinates).
         */
        private const val TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES

        /**
         * Offset for the vertex (x,y,z) coordinates.
         */
        private const val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0

        /**
         * Offset for the texture coordinates.
         */
        private const val TRIANGLE_VERTICES_DATA_UV_OFFSET = 3

        /**
         * TAG used for logging
         */
        private const val TAG = "GLES20TriangleRenderer"
    }

    /**
     * Init block of our constructor. We initialize our `FloatBuffer` field `mTriangleVertices`
     * by allocating enough bytes on the native heap to contain our `mTriangleVerticesData`,
     * in native byte order, and creating a view of this as a `FloatBuffer`. We next proceed
     * to fill `mTriangleVertices` with the contents of `mTriangleVerticesData`, and
     * then rewind it to its beginning.
     */
    init {
        mTriangleVertices = ByteBuffer.allocateDirect(
            mTriangleVerticesData.size * FLOAT_SIZE_BYTES
        ).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTriangleVertices.put(mTriangleVerticesData).position(0)
    }
}