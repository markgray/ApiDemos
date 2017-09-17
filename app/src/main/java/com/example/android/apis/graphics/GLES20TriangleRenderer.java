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

package com.example.android.apis.graphics;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.example.android.apis.R;

/**
 * Draws a textured rotating triangle using OpenGL ES 2.0
 */
@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
class GLES20TriangleRenderer implements GLSurfaceView.Renderer {
    /**
     * Size in bytes of a float value.
     */
    private static final int FLOAT_SIZE_BYTES = 4;
    /**
     * Stride in bytes for triangle vertex data. (3 float (x,y,z) coordinates and 2 texture coordinates).
     */
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    /**
     * Offset for the vertex (x,y,z) coordinates.
     */
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    /**
     * Offset for the texture coordinates.
     */
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    /**
     * Vertex data for the three vertices of our triangle.
     */
    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -0.5f, 0, -0.5f, 0.0f,
            1.0f, -0.5f, 0, 1.5f, -0.0f,
            0.0f, 1.11803399f, 0, 0.5f, 1.61803399f};

    /**
     * {@code FloatBuffer} that we load {@code mTriangleVerticesData} into then use in a call to
     * {@code glVertexAttribPointer} to define an array of generic vertex attribute data both for
     * {@code maPositionHandle} (the location of the attribute variable "aPosition" in our compiled
     * program object {@code mProgram}) and {@code maTextureHandle} (the location of the attribute
     * variable "aTextureCoord" in our compiled program object {@code mProgram})
     */
    private FloatBuffer mTriangleVertices;

    /**
     * Source code for the GL_VERTEX_SHADER part of our shader program {@code mProgram} (shader that
     * is intended to run on the programmable vertex processor). The statements meanings:
     * <ul>
     * <li>
     * uniform mat4 uMVPMatrix; # A {@code uniform} is a global openGL Shading Language variable
     * declared with the "uniform" storage qualifier. These act as parameters that the user of a
     * shader program can pass to that program. They are stored in a program object. Uniforms
     * are so named because they do not change from one execution of a shader program to the next
     * within a particular rendering call. This makes them unlike shader stage inputs and outputs,
     * which are often different for each invocation of a program stage. A {@code mat4} is a
     * 4x4 matrix, and {@code uMVPMatrix} is the name of the uniform variable which is located
     * using the method {@code glGetUniformLocation}, its location assigned to the field
     * {@code muMVPMatrixHandle} and changed using the method {@code glUniformMatrix4fv} in our
     * {@code onDrawFrame} method. It is used to feed the Model View Projection matrix
     * {@code mMVPMatrix} which rotates the triangle a little bit every frame.
     * </li>
     * <li>
     * attribute vec4 aPosition; # An {@code attribute} is used to feed data from the vertex
     * array object, with the index into that object for the vertices being fed it set by the
     * method {@code glVertexAttribPointer}, a {@code vec4} is a 4-component float vector,
     * {@code aPosition} is the name of the attribute, and its location is located and assigned
     * to the field {@code maPositionHandle} using the method {@code glGetAttribLocation}. It
     * is used to feed the (x,y,z) coordinates to the shader program.
     * </li>
     * <li>
     * attribute vec2 aTextureCoord; # Like {@code aPosition} but a 2-component float vector,
     * with the location assigned to the field {@code maTextureHandle}. It is used to feed
     * the (u,v) texture coordinates to the shader program.
     * </li>
     * <li>
     * varying vec2 vTextureCoord; # A {@code varying} variable provides an interface between
     * Vertex and Fragment Shader. Vertex Shaders compute values per vertex and fragment shaders
     * compute values per fragment. If you define a varying variable in a vertex shader, its
     * value will be interpolated (perspective-correct) over the primitive being rendered and
     * you can access the interpolated value in the fragment shader. We use it simply to pass
     * the value of {@code aTextureCoord} for this vertex to the fragment shader.
     * </li>
     * <li>
     * void main() { # Each shader's entry point is at its {@code main} function where we
     * process any input variables and output the results in its output variables.
     * </li>
     * <li>
     * gl_Position = uMVPMatrix * aPosition; # {@code gl_Position} is a built-in variable for
     * the clip-space output position of the current vertex, and is intended for writing the
     * homogeneous vertex position. It can be written at any time during vertex shader execution.
     * This value will be used by primitive assembly, clipping, culling, and other fixed
     * functionality operations, if present, that operate on primitives after vertex processing
     * has occurred. Its value is undefined after the vertex processing stage if the vertex
     * shader executable does not write gl_Position. We calculate it by multiplying the (x,y,z)
     * coordinates of the vertex fed us in {@code aPosition} by the Model View Projection matrix
     * {@code uMVPMatrix} which rotates the vertex to the current position.
     * </li>
     * <li>
     * vTextureCoord = aTextureCoord; # We merely pass on the (u,v) texture coordinates of this
     * vertex fed us in {@code aTextureCoord} to the fragment shader using {@code vTextureCoord}.
     * </li>
     * <li>
     * } # That's all folks!
     * </li>
     * </ul>
     */
    private final String mVertexShader =
            "uniform mat4 uMVPMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec2 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = aTextureCoord;\n" +
                    "}\n";

    /**
     * Source code for the GL_FRAGMENT_SHADER part of our shader program {@code mProgram} (shader that
     * is intended to run on the programmable fragment processor). The statements meanings:
     * <ul>
     * <li>
     * precision mediump float; # Specifies the use of medium precision for {@code float} calculations.
     * </li>
     * <li>
     * varying vec2 vTextureCoord; # Storage for the vertex shader to use to pass us the (u,v) texture
     * coordinates for the vertex.
     * </li>
     * <li>
     * uniform sampler2D sTexture; # A {@code uniform} is a global openGL Shading Language variable
     * declared with the "uniform" storage qualifier. These act as parameters that the user of a
     * shader program can pass to that program. They are stored in a program object. Uniforms
     * are so named because they do not change from one execution of a shader program to the next
     * within a particular rendering call. This makes them unlike shader stage inputs and outputs,
     * which are often different for each invocation of a program stage. A {@code sampler2D} is
     * a floating point sampler for a 2 dimensional texture. The only place where you can use a
     * sampler is in one of the openGL Shader Language standard library's texture lookup functions.
     * These functions access the texture referred to by the sampler. They take a texture coordinate
     * as parameters. The name {@code sTexture} is never used by the java program, so the sampler2D
     * simply accesses the texture bound to the GL_TEXTURE_2D of the default texture unit GL_TEXTURE0.
     * </li>
     * <li>
     * void main() { # As in the vertex shader program
     * </li>
     * <li>
     * gl_FragColor = texture2D(sTexture, vTextureCoord); # {@code gl_FragColor} is a built-in output
     * variable for setting the {@code vec4} fragment color. {@code texture2D} looks up the color
     * for the coordinates given by {@code vTextureCoord} using the sampler {@code sTexture} (which
     * just our texture we bound to GL_TEXTURE_2D for the default texture unit GL_TEXTURE0.
     * </li>
     * <li>
     * } # That's all folks!
     * </li>
     * </ul>
     */
    private final String mFragmentShader =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    /**
     * Model View Projection Matrix is the multiplication of the Projection Matrix times the View
     * matrix times the Model Matrix, and transforms a vertex from model coordinates to Homogeneous
     * coordinates. We pass this matrix to the vertex shader using {@code uniform mat4 uMVPMatrix}
     * which we locate using the method {@code glGetUniformLocation} (saving the location in our
     * variable {@code muMVPMatrixHandle}) and set using the method {@code glUniformMatrix4fv}.
     */
    private float[] mMVPMatrix = new float[16];
    /**
     * Model matrix, translates model coordinates to world coordinates. We set it to a rotation matrix
     * for the current angle of rotation using the method {@code setRotateM}. It is used to calculate
     * our Model View Projection Matrix {@code mMVPMatrix} (along with our view matrix {@code mVMatrix}
     * and projection matrix {@code mProjMatrix}). It is set every frame in our method {@code onDrawFrame}
     * to an angle calculated from the system time since boot.
     */
    private float[] mMMatrix = new float[16];
    /**
     * View matrix, translates world coordinates to camera coordinates. We set it using the method
     * {@code setLookAtM} to have an (x,y,z) eye point coordinate of (0,0,-5), an (x,y,z) center at
     * (0,0,0), and an "up vector" of (0,1,0). It is set once in our method {@code onSurfaceCreated}.
     */
    private float[] mVMatrix = new float[16];
    /**
     * Projection matrix, translates camera coordinates to Homogeneous Space (perspective is included).
     * We set it using the method {@code frustumM} to have  (mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
     * a left clipping plane of the negative of the aspect ration, a right clipping plane of the
     * aspect ration, bottom clipping plane of -1, top clipping plane of 1, near clipping plane of 3,
     * and a far clipping plane of 7. It is set once in our method {@code onSurfaceChanged}.
     */
    private float[] mProjMatrix = new float[16];

    /**
     * Compiled and linked Program object which contains an executable that will run its GL_VERTEX_SHADER
     * shader object on the vertex processor and run its GL_FRAGMENT_SHADER object on the fragment processor.
     * Its code comes from {@code String mVertexShader} and {@code String mFragmentShader}, and it is
     * compiled and linked by our method {@code createProgram} using GLES20 utility methods.
     */
    private int mProgram;
    /**
     * Texture name we use for our texture, generated using {@code glGenTextures}, and bound to
     * GL_TEXTURE_2D in {@code onSurfaceCreated} in order to upload our png resource, then again in
     * {@code onDrawFrame} in order to draw using it.
     */
    private int mTextureID;
    /**
     * The location of the GL vector shader program's uniform variable "uMVPMatrix" located using
     * {@code glGetUniformLocation} in our method {@code onSurfaceCreated} and used to upload our
     * Model View Projection Matrix {@code mMVPMatrix} to the GL shader program in {@code onDrawFrame}
     * using the method {@code glUniformMatrix4fv}
     */
    private int muMVPMatrixHandle;
    /**
     * The location of the GL vector shader program's {@code attribute vec4 aPosition} located using
     * {@code glGetAttribLocation} in {@code onSurfaceCreated} and used to initialize {@code aPosition}
     * to access the (x,y,z) coordinates of each vertex as they are processed from the VBO using
     * {@code glVertexAttribPointer} in {@code onDrawFrame}
     */
    private int maPositionHandle;
    /**
     * The location of the GL vector shader program's {@code attribute vec2 aTextureCoord} located
     * using {@code glGetAttribLocation} in {@code onSurfaceCreated} and used to initialize
     * {@code aTextureCoord} to access the (u,v) coordinates of each vertex as they are processed
     * from the VBO using {@code glVertexAttribPointer} in {@code onDrawFrame}
     */
    private int maTextureHandle;

    /**
     * {@code Context} we were constructed with, used to retrieve resources.
     */
    private Context mContext;
    /**
     * TAG used for logging
     */
    private static String TAG = "GLES20TriangleRenderer";

    /**
     * Our constructor. First we save our parameter {@code Context context} in our field
     * {@code Context mContext}, Then we initialize our field {@code FloatBuffer mTriangleVertices}
     * by allocating enough bytes on the native heap to contain our {@code mTriangleVerticesData},
     * in native byte order, and creating a view of this as a {@code FloatBuffer}. We next proceed
     * to fill {@code mTriangleVertices} with the contents of {@code mTriangleVerticesData}, and
     * then rewind it to its beginning.
     *
     * @param context {@code Context} to use to retrieve resources, "this" when called from the
     *                {@code onCreate} method of {@code GLES20Activity}.
     */
    public GLES20TriangleRenderer(Context context) {
        mContext = context;
        mTriangleVertices = ByteBuffer.allocateDirect(mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);
    }

    /**
     * Called to draw the current frame. First we set the clear color to a dark blue, and clear both
     * the depth buffer and the color buffer. Then we install our program object with the handle
     * {@code mProgram} as part of current rendering state, and afterwards call our method
     * {@code checkGlError} which will throw a RuntimeException if any openGL error occurred. We
     * select GL_TEXTURE0 to be the active texture unit, and bind our texture named {@code mTextureID}
     * to the texture target GL_TEXTURE_2D (While a texture is bound, GL operations on the target to
     * which it is bound affect the bound texture, and queries of the target to which it is bound
     * return state from the bound texture. If texture mapping is active on the target to which a
     * texture is bound, the bound texture is used. In effect, the texture targets become aliases
     * for the textures currently bound to them, and the texture name zero refers to the default
     * textures that were bound to them at initialization).
     *
     * We position {@code mTriangleVertices} to the offset to the location of the (x,y,z) coordinates
     * TRIANGLE_VERTICES_DATA_POS_OFFSET (0), and then use the method {@code glVertexAttribPointer}
     * to initialize the vertex shader program's {@code attribute vec4 aPosition} (whose location we
     * have stored in {@code maPositionHandle}) by defining its array of generic vertex attribute data.
     * This array has 3 components (x,y.z), is of type GL_FLOAT, does not need to be normalized, has
     * a stride of TRIANGLE_VERTICES_DATA_STRIDE_BYTES (20), and draws its data from our field
     * {@code FloatBuffer mTriangleVertices}. We then call our method {@code checkGlError} which will
     * throw a RuntimeException if any openGL error occurred. TODO: rephrase this garbage
     *
     * Next we position {@code mTriangleVertices} to the offset to the location of the (u,v) texture
     * coordinates TRIANGLE_VERTICES_DATA_UV_OFFSET (3), enable the vertex attribute array given by
     * the handle {@code maPositionHandle} using the method {@code glEnableVertexAttribArray} (If
     * enabled, the values in the generic vertex attribute array will be accessed and used for
     * rendering when calls are made to vertex array commands such as glDrawArrays or glDrawElements).
     * And again we call our method {@code checkGlError} which will throw a RuntimeException if any
     * openGL error occurred.
     *
     * We now use the method {@code glVertexAttribPointer} to initialize the vertex shader program's
     * {@code attribute vec2 aTextureCoord} (whose location we have stored in {@code maTextureHandle})
     * by defining its array of generic vertex attribute data. This array has 2 components (u,v), is
     * of type GL_FLOAT, does not need to be normalized, has a stride of TRIANGLE_VERTICES_DATA_STRIDE_BYTES
     * (20), and draws its data from our field {@code FloatBuffer mTriangleVertices}. We then call
     * our method {@code checkGlError} which will throw a RuntimeException if any openGL error occurred.
     *
     * We enable the vertex attribute array given by the handle {@code maTextureHandle} using the
     * method {@code glEnableVertexAttribArray} (as above for {@code maPositionHandle}), and call
     * our method {@code checkGlError} which will throw a RuntimeException if any openGL error
     * occurred.
     *
     * We fetch the milliseconds since boot modulo 4000 to initialize {@code long time} and use it to
     * calculate {@code float angle} (an angle in degrees which goes from 0 to 360 in those 4000
     * milliseconds and repeats). We set our model matrix {@code mMMatrix} to a rotation matrix of
     * {@code angle} around the z axis, set our model view projection matrix {@code mMVPMatrix} to
     * the view matrix {@code mVMatrix} times {@code mMMatrix} and then set {@code mMVPMatrix} to
     * the projection matrix {@code mProjMatrix} times that value of {@code mMVPMatrix}.
     *
     * We next use the method {@code glUniformMatrix4fv} to specify the value of the uniform variable
     * {@code uniform mat4 uMVPMatrix} for the current program object whose location we have stored
     * in {@code muMVPMatrixHandle} specifying that 1 matrix is to be modified, no need to transpose,
     * the values are to come from {@code mMVPMatrix}, with an offset of 0.
     *
     * We now use the method {@code glDrawArrays} to request that GL_TRIANGLES be drawn starting from
     * vertex 0, with 3 indices to be rendered, and call our method {@code checkGlError} which will
     * throw a RuntimeException if any openGL error occurred.
     *
     * @param glUnused The GL interface, but we don't use it
     */
    @Override
    public void onDrawFrame(GL10 glUnused) {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");

        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");

        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);
        Matrix.setRotateM(mMMatrix, 0, angle, 0, 0, 1.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        checkGlError("glDrawArrays");
    }

    /**
     * Called when the surface changed size. Called after the surface is created and whenever the
     * OpenGL ES surface size changes. Typically you will set your viewport here. If your camera is
     * fixed then you could also set your projection matrix here.
     *
     * We set our viewport to have the lower left corner at (0,0), a width of {@code width}, and
     * a height of {@code height}. We calculate the aspect ration {@code float ratio} to be the value
     * {@code width/height}, and then use the method {@code frustumM} to define the projection matrix
     * {@code mProjMatrix} to have the left clipping plane at {@code -ratio}, the right clipping plane
     * at {@code ratio}, the bottom clipping plane at -1, the top clipping plane at 1, the near clipping
     * plane at 3, and the far clipping plane at 7.
     *
     * @param glUnused The GL interface, but we don't use it
     * @param width width of the new surface
     * @param height height of the new surface
     */
    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
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
     * First we call our method {@code createProgram} to compile and link our two shader programs:
     * the vertex shader {@code mVertexShader} and the fragment shader {@code mFragmentShader}, saving
     * the handle to the program object it has created in our field {@code int mProgram}. A zero return
     * indicates it failed to create it, so we just return.
     *
     * We initialize our field {@code maPositionHandle} by locating the {@code attribute vec4 aPosition}
     * within the program object {@code mProgram} using the method {@code glGetAttribLocation}, and
     * throw a RuntimeException if we failed to locate it.
     *
     * We initialize our field {@code maTextureHandle} by locating the {@code attribute vec2 aTextureCoord}
     * within the program object {@code mProgram} using the method {@code glGetAttribLocation}, and
     * throw a RuntimeException if we failed to locate it.
     *
     * We initialize our field {@code muMVPMatrixHandle} by locating the {@code uniform mat4 uMVPMatrix}
     * within the program object {@code mProgram} using the method {@code glGetUniformLocation}, and
     * throw a RuntimeException if we failed to locate it.
     *
     * Now we create our texture. We use {@code glGenTextures} to generate a texture name which we
     * save to our field {@code mTextureID}. We bind it to the target GL_TEXTURE_2D. 
     *
     * @param glUnused The GL interface, but we don't use it
     * @param config the EGLConfig of the created surface. UNUSED
     */
    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        mProgram = createProgram(mVertexShader, mFragmentShader);
        if (mProgram == 0) {
            return;
        }

        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }

        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }

        /*
         * Create our texture. This has to be done each time the
         * surface is created.
         */

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        InputStream is = mContext.getResources().openRawResource(R.raw.robot);
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Ignore.
            }
        }

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    private void checkGlError(String op) {
        int error;
        //noinspection LoopStatementThatDoesntLoop
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

}
