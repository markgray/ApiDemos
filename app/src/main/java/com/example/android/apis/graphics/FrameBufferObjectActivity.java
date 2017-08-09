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
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.os.SystemClock;

/**
 * Demonstrate the Frame Buffer Object OpenGL ES extension. This sample renders a scene into an
 * offscreen frame buffer, and then uses the resulting image as a texture to render an onscreen scene.
 */
public class FrameBufferObjectActivity extends Activity {
    /**
     * {@code GLSurfaceView} containing our demo, its {@code GLSurfaceView.Renderer} is our class
     * {@code Renderer} and it is our entire content view.
     */
    private GLSurfaceView mGLSurfaceView;

    /**
     * {@code GLSurfaceView.Renderer} which draws our demo, it consists of two rotating {@code Cube}
     * objects used as the texture for a rotating {@code Triangle}
     */
    private class Renderer implements GLSurfaceView.Renderer {
        /**
         * Flag indicating whether the current GL context supports the extension GL_OES_framebuffer_object
         */
        private boolean mContextSupportsFrameBufferObject;
        /**
         * Texture ID of the texture we use for our demo, it is bound to GL_TEXTURE_2D in our method
         * {@code drawOnscreen} which is called by our override of {@code onDrawFrame}.
         */
        private int mTargetTexture;
        /**
         * Framebuffer object name we use to draw our offscreen texture to, it is bound to
         * GL11ExtensionPack.GL_FRAMEBUFFER_OES in {@code onDrawFrame}
         */
        private int mFramebuffer;
        /**
         * Width of the Framebuffer object, it is used in our method {@code createTargetTexture} to
         * specify the width of the two-dimensional texture image for the GL_TEXTURE_2D target (which
         * is bound to our {@code mTargetTexture}, as well as by our method {@code createFrameBuffer}
         * to specify the width of the renderbuffer object's image that is bound to GL_RENDERBUFFER_OES
         * (which is our {@code mFramebuffer}).
         */
        private int mFramebufferWidth = 256;
        /**
         * Height of the Framebuffer object, it is used in our method {@code createTargetTexture} to
         * specify the height of the two-dimensional texture image for the GL_TEXTURE_2D target (which
         * is bound to our {@code mTargetTexture}, as well as by our method {@code createFrameBuffer}
         * to specify the height of the renderbuffer object's image that is bound to GL_RENDERBUFFER_OES
         * (which is our {@code mFramebuffer}).
         */
        private int mFramebufferHeight = 256;
        /**
         * Width of our {@code SurfaceView} which is set in our {@code onSurfaceChanged} callback
         */
        private int mSurfaceWidth;
        /**
         * Height of our {@code SurfaceView} which is set in our {@code onSurfaceChanged} callback
         */
        private int mSurfaceHeight;

        /**
         * {@code Triangle} instance which we draw in our method {@code drawOnscreen} every time our
         * callback {@code onDrawFrame} is called, it is rotated by a function of the system time
         * by rotating the {@code GLSurfaceView} using {@code glRotatef}, and the texture is supplied
         * by {@code mTargetTexture} (which consists of our off screen frame buffer which has two
         * rotating {@code Cube} objects being drawn into it).
         */
        private Triangle mTriangle;
        /**
         * {@code Cube} instance that we use twice to produce the texture used by our {@code Triangle}
         * instance (the second rotated around the (y,z) axis by twice the angle the first is rotated
         * by, and translated by (0.5, 0.5, 0.5))
         */
        private Cube mCube;
        /**
         * Angle used to draw the two {@code Cube} objects we use as our texture, it is advanced by
         * 1.2 degrees every frame.
         */
        private float mAngle;
        /**
         * Setting this to true will change the behavior  of this sample. It
         * will suppress the normally onscreen rendering, and it will cause the
         * rendering that would normally be done to the offscreen FBO
         * be rendered onscreen instead. This can be helpful in debugging the
         * rendering algorithm.
         */
        private static final boolean DEBUG_RENDER_OFFSCREEN_ONSCREEN = false;

        /**
         * Called to draw the current frame. First we call our method {@code checkGLError} to catch
         * any errors that may have occurred. Then if {@code mContextSupportsFrameBufferObject} (the
         * current context supports frame buffer objects) we cast our parameter {@code GL10 gl} to
         * {@code GL11ExtensionPack gl11ep} and if we are debugging we immediately call our method
         * {@code drawOffscreenImage} which will draw the two rotating {@code Cube} that we use as
         * our texture directly to the {@code GLSurfaceView} window system provided framebuffer,
         * if we are not debugging we bind our frame buffer object name {@code mFramebuffer} to the
         * target GL_FRAMEBUFFER_OES, then call our method {@code drawOffscreenImage}, unbind the
         * target GL_FRAMEBUFFER_OES, and call our method {@code drawOnscreen}. If the current
         * context doesn't support frame buffer objects we set our clear color to red, and clear the
         * color buffer and depth buffer with it.
         *
         * @param gl the GL interface.
         */
        @Override
        public void onDrawFrame(GL10 gl) {
            checkGLError(gl);
            if (mContextSupportsFrameBufferObject) {
                GL11ExtensionPack gl11ep = (GL11ExtensionPack) gl;
                if (DEBUG_RENDER_OFFSCREEN_ONSCREEN) {
                    drawOffscreenImage(gl, mSurfaceWidth, mSurfaceHeight);
                } else {
                    gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, mFramebuffer);
                    drawOffscreenImage(gl, mFramebufferWidth, mFramebufferHeight);
                    gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);
                    drawOnscreen(gl, mSurfaceWidth, mSurfaceHeight);
                }
            } else {
                // Current context doesn't support frame buffer objects.
                // Indicate this by drawing a red background.
                gl.glClearColor(1, 0, 0, 0);
                gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            }
        }

        /**
         * Called when the surface changed size. Called after the surface is created and whenever
         * the OpenGL ES surface size changes. First we call our method {@code checkGLError} to
         * catch any error that may have occurred, then we save {@code width} in our field
         * {@code mSurfaceWidth} and height in our field {@code mSurfaceHeight} then set the viewport
         * to have the lower left corner at (0,0) and a width of {@code width} and a height of
         * {@code height}.
         *
         * @param gl     the GL interface.
         * @param width  width of surface
         * @param height height of surface
         */
        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            checkGLError(gl);
            mSurfaceWidth = width;
            mSurfaceHeight = height;
            gl.glViewport(0, 0, width, height);
        }

        /**
         * Called when the surface is created or recreated. Called when the rendering thread starts
         * and whenever the EGL context is lost. The EGL context will typically be lost when the
         * Android device awakes after going to sleep. First we check to see if the current context
         * supports the capability GL_OES_framebuffer_object (has frame buffer objects) and set our
         * field {@code mContextSupportsFrameBufferObject} to true if it does. Then if it does support
         * frame buffer objects, we call our method {@code createTargetTexture} and initialize our
         * field {@code mTargetTexture} with the texture name bound to GL_TEXTURE_2D and configured
         * appropriately which it we returns. We call our method {@code createFrameBuffer} and
         * initialize our field {@code mFramebuffer} with the framebuffer object name which has our
         * {@code mTargetTexture} attached to it and which is configured appropriately which it
         * returns. Finally we initialize our field {@code Cube mCube} with a new instance of
         * {@code Cube}, and our field {@code Triangle mTriangle} with a new instance of
         * {@code Triangle}.
         *
         * @param gl     the GL interface.
         * @param config the EGLConfig of the created surface. Unused.
         */
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mContextSupportsFrameBufferObject = checkIfContextSupportsFrameBufferObject(gl);
            if (mContextSupportsFrameBufferObject) {
                mTargetTexture = createTargetTexture(gl, mFramebufferWidth, mFramebufferHeight);
                mFramebuffer = createFrameBuffer(gl, mFramebufferWidth, mFramebufferHeight, mTargetTexture);

                mCube = new Cube();
                mTriangle = new Triangle();
            }
        }

        /**
         * Called from our implementation of {@code onDrawFrame} to draw our triangle. First we set
         * the viewport to have the lower left corner at (0,0) and a width of {@code width} and a
         * height of {@code height}. Then we calculate the aspect ratio {@code float ratio} to be
         * {@code width/height}, then we set the GL_PROJECTION matrix to be the current matrix, load
         * it with the identity matrix and call {@code glFrustumf} to multiply the matrix by the
         * perspective matrix with the left clipping plane at {@code -ratio}, the right clipping
         * plane at {@code +ratio}, the bottom clipping plane at -1, the top clipping plane at 1,
         * the near clipping plane at 3 and the far clipping plane at 7.
         * <p>
         * We set the clear color to the color blue, and clear the color buffer and the depth buffer.
         * Then we bind our {@code mTargetTexture} to the target texture GL_TEXTURE_2D.
         * <p>
         * We set the GL_TEXTURE_ENV_MODE texture environment parameter of the texture environment
         * GL_TEXTURE_ENV to the texture function GL_REPLACE (the texture replaces the current color).
         * <p>
         * Next we set the GL_MODELVIEW matrix to be the current matrix, load it with the identity
         * matrix and call {@code GLU.gluLookAt} to specify the viewing transformation to have the
         * eye point at (0,0,-5), the center reference point at (0,0,0) and the up vector (0,1,0).
         * <p>
         * We enable the client-side capability GL_VERTEX_ARRAY and GL_TEXTURE_COORD_ARRAY then select
         * GL_TEXTURE0 to be the active texture unit.
         * <p>
         * We calculate {@code float angle} using the current milliseconds since boot and rotate the
         * GL_MODELVIEW matrix by {@code angle} around the vector (0,0,1). Then we instruct our
         * {@code Triangle mTriangle} to draw itself.
         * <p>
         * To clean up we unbind the texture GL_TEXTURE_2D, and disable the client-side capabilities
         * GL_VERTEX_ARRAY and GL_TEXTURE_COORD_ARRAY.
         *
         * @param gl     the GL interface.
         * @param width  width of our surface view {@code mSurfaceWidth} in our case.
         * @param height height of our surface view {@code mSurfaceHeight} in our case.
         */
        private void drawOnscreen(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);
            float ratio = (float) width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);

            gl.glClearColor(0, 0, 1, 0);
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glBindTexture(GL10.GL_TEXTURE_2D, mTargetTexture);

            gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);

            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();

            GLU.gluLookAt(gl, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

            gl.glActiveTexture(GL10.GL_TEXTURE0);

            long time = SystemClock.uptimeMillis() % 4000L;
            float angle = 0.090f * ((int) time);

            gl.glRotatef(angle, 0, 0, 1.0f);

            mTriangle.draw(gl);

            // Restore default state so the other renderer is not affected.

            gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        }

        /**
         * Called from our {@code onDrawFrame} implementation, this Draws our {@code Cube mCube}
         * twice to the currently selected framebuffer (either the window system provided default
         * framebuffer it we are debugging our texture, or the offscreen {@code mFramebuffer} which
         * is bound to the target GL_FRAMEBUFFER_OES). First we set the viewport to have the lower
         * left corner at (0,0) and a width of {@code width} and a height of {@code height}. Then we
         * calculate the aspect ratio {@code float ratio} to be {@code width/height}, then we set
         * the GL_PROJECTION matrix to be the current matrix, load it with the identity matrix and
         * call {@code glFrustumf} to multiply the matrix by the perspective matrix with the left
         * clipping plane at {@code -ratio}, the right clipping plane at {@code +ratio}, the bottom
         * clipping plane at -1, the top clipping plane at 1, the near clipping plane at 1 and the
         * far clipping plane at 10.
         * <p>
         * We enable the server-side GL capabilities GL_CULL_FACE (cull polygons based on their
         * winding in window coordinates) and GL_DEPTH_TEST (do depth comparisons and update the
         * depth buffer). We set the clear color to (0, 0.5, 1.0) (a light blue) and clear the color
         * buffer and the depth buffer. Next we set the GL_MODELVIEW matrix to be the current matrix,
         * load it with the identity matrix, translate it to (0, 0, -3), rotate it by {@code mAngle}
         * around the vector (0, 1, 0), and rotate it by {@code mAngle*0.25} around the vector (1,0,0).
         * <p>
         * We enable the client-side capability GL_VERTEX_ARRAY (the vertex array is enabled for
         * writing and used during rendering) and GL_COLOR_ARRAY (the color array is enabled for
         * writing and used during rendering), and then we instruct our field {@code Cube mCube} to
         * draw itself. We then rotate the GL_MODELVIEW matrix by {@code mAngle*2.0} around the
         * vector (0, 1, 1), translate it to (0.5f, 0.5f, 0.5f) and instruct our field {@code Cube mCube}
         * to draw itself again.
         * <p>
         * We increment {@code mAngle} by 1.2 degrees for the next time we are executed, disable the
         * server-side GL capabilities GL_CULL_FACE and GL_DEPTH_TEST, and disable the client-side
         * capabilities GL_VERTEX_ARRAY, and GL_COLOR_ARRAY so the other renderer is not affected..
         *
         * @param gl     the GL interface.
         * @param width  width of our texture
         * @param height height of our texture
         */
        private void drawOffscreenImage(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);
            float ratio = (float) width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);

            gl.glEnable(GL10.GL_CULL_FACE);
            gl.glEnable(GL10.GL_DEPTH_TEST);

            gl.glClearColor(0, 0.5f, 1, 0);
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glTranslatef(0, 0, -3.0f);
            gl.glRotatef(mAngle, 0, 1, 0);
            gl.glRotatef(mAngle * 0.25f, 1, 0, 0);

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

            mCube.draw(gl);

            gl.glRotatef(mAngle * 2.0f, 0, 1, 1);
            gl.glTranslatef(0.5f, 0.5f, 0.5f);

            mCube.draw(gl);

            mAngle += 1.2f;

            // Restore default state so the other renderer is not affected.

            gl.glDisable(GL10.GL_CULL_FACE);
            gl.glDisable(GL10.GL_DEPTH_TEST);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        }

        /**
         * Generates a texture name, binds it to GL_TEXTURE_2D, configures it, then returns the
         * texture name to the caller. Called from our implementation of {@code onSurfaceCreated} it
         * is used to initialize the field {@code int mTargetTexture}. We allocate {@code int texture}
         * and {@code int[] textures}, call {@code glGenTextures} to generate 1 texture name in
         * {@code textures} which we then use to initialize {@code texture}. We then bind {@code texture}
         * to GL_TEXTURE_2D, call {@code glTexImage2D} to specify a two-dimensional texture image for
         * GL_TEXTURE_2D with a level-of-detail number of Level 0 (the base image level), an internal
         * format of GL_RGBA, {@code width} for its width, {@code height} for its height, 0 for its
         * border (must be 0 according to docs), GL_RGBA as the format of the texel data, GL_UNSIGNED_BYTE
         * as the data type of the texel data, and 0 to point to the beginning of the data in memory.
         * <p>
         * New we set texture parameter GL_TEXTURE_MIN_FILTER for the target GL_TEXTURE_2D to GL_NEAREST
         * (the value of the texture element that is nearest (in Manhattan distance) to the center of
         * the pixel being textured is used whenever the pixel being textured maps to an area greater
         * than one texture element).
         * <p>
         * We set texture parameter GL_TEXTURE_MAG_FILTER for the target GL_TEXTURE_2D to GL_LINEAR
         * (the weighted average of the four texture elements that are closest to the center of the
         * pixel being textured is used when the pixel being textured maps to an area less than or
         * equal to one texture element).
         * <p>
         * We call {@code glTexParameterx} to set texture parameter GL_TEXTURE_WRAP_S for the target
         * GL_TEXTURE_2D to GL_REPEAT (causes the integer part of the s coordinate to be ignored;
         * the GL uses only the fractional part, thereby creating a repeating pattern).
         * <p>
         * We call {@code glTexParameterx} to set texture parameter GL_TEXTURE_WRAP_T for the target
         * GL_TEXTURE_2D to GL_REPEAT (causes the integer part of the t coordinate to be ignored;
         * the GL uses only the fractional part, thereby creating a repeating pattern).
         * <p>
         * Finally we return {@code texture} to the caller.
         *
         * @param gl     the GL interface.
         * @param width  width of our texture
         * @param height height of our texture
         * @return a texture name bound to GL_TEXTURE_2D and configured as we wish it
         */
        private int createTargetTexture(GL10 gl, int width, int height) {
            int texture;
            int[] textures = new int[1];
            gl.glGenTextures(1, textures, 0);
            texture = textures[0];

            gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
            gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, width, height, 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, null);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
            return texture;
        }

        /**
         * Generates a framebuffer object name, binds it to GL_FRAMEBUFFER_OES, configures it to our
         * wishing, attaches the texture image {@code targetTextureId} to that framebuffer object and
         * returns the framebuffer object name to the caller. Used to initialize our field
         * {@code mFramebuffer}.
         * <p>
         * First we cast our argument {@code GL10 gl} to {@code GL11ExtensionPack gl11ep}, then we
         * allocate {@code int framebuffer} and {@code int[] framebuffers}, fill {@code framebuffers}
         * with 1 framebuffer object name which we use to set {@code framebuffer}. We then bind
         * {@code framebuffer} to GL_FRAMEBUFFER_OES.
         * <p>
         * Next we allocate {@code int depthbuffer} and {@code int[] renderbuffers}, fill
         * {@code renderbuffers} with 1 renderbuffer object name which we use to set {@code depthbuffer}.
         * We then bind {@code depthbuffer} to the target GL_RENDERBUFFER_OES, establish data storage,
         * format and dimensions of the renderbuffer object's image to use GL_DEPTH_COMPONENT16 as its
         * internal format, a width of {@code width}, and a height of {@code height}. We next attach
         * the renderbuffer object {@code depthbuffer} to the target GL_FRAMEBUFFER_OES, to the
         * attachment point GL_DEPTH_ATTACHMENT_OES, specifying the renderbuffer target GL_RENDERBUFFER_OES.
         * <p>
         * We attach the texture image {@code targetTextureId} to the framebuffer object GL_FRAMEBUFFER_OES
         * using the attachment point GL_COLOR_ATTACHMENT0_OES, texture target GL_TEXTURE_2D, and 0
         * as the mipmap level of the texture image to be attached, which must be 0.
         * <p>
         * We now retrieve the framebuffer completeness status of the framebuffer object GL_FRAMEBUFFER_OES
         * to set {@code int status}, and if it is not GL_FRAMEBUFFER_COMPLETE_OES we throw a runtime
         * exception. Otherwise we unbind the framebuffer GL_FRAMEBUFFER_OES and return {@code framebuffer}
         * to the caller.
         *
         * @param gl              the GL interface.
         * @param width           width of our framebuffer
         * @param height          height of our framebuffer
         * @param targetTextureId a texture name bound to GL_TEXTURE_2D, {@code int mTargetTexture}
         *                        in our case.
         * @return a framebuffer object name bound to GL_RENDERBUFFER_OES and configured as we wish it.
         */
        private int createFrameBuffer(GL10 gl, int width, int height, int targetTextureId) {
            GL11ExtensionPack gl11ep = (GL11ExtensionPack) gl;
            int framebuffer;
            int[] framebuffers = new int[1];
            gl11ep.glGenFramebuffersOES(1, framebuffers, 0);
            framebuffer = framebuffers[0];
            gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, framebuffer);

            int depthbuffer;
            int[] renderbuffers = new int[1];
            gl11ep.glGenRenderbuffersOES(1, renderbuffers, 0);
            depthbuffer = renderbuffers[0];

            gl11ep.glBindRenderbufferOES(GL11ExtensionPack.GL_RENDERBUFFER_OES, depthbuffer);
            gl11ep.glRenderbufferStorageOES(GL11ExtensionPack.GL_RENDERBUFFER_OES,
                    GL11ExtensionPack.GL_DEPTH_COMPONENT16, width, height);
            gl11ep.glFramebufferRenderbufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES,
                    GL11ExtensionPack.GL_DEPTH_ATTACHMENT_OES,
                    GL11ExtensionPack.GL_RENDERBUFFER_OES, depthbuffer);

            gl11ep.glFramebufferTexture2DOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES,
                    GL11ExtensionPack.GL_COLOR_ATTACHMENT0_OES, GL10.GL_TEXTURE_2D,
                    targetTextureId, 0);
            int status = gl11ep.glCheckFramebufferStatusOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES);
            if (status != GL11ExtensionPack.GL_FRAMEBUFFER_COMPLETE_OES) {
                throw new RuntimeException("Framebuffer is not complete: " +
                        Integer.toHexString(status));
            }
            gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);
            return framebuffer;
        }

        /**
         * Convenience function that calls our method {@code checkIfContextSupportsExtension} with the
         * string GL_OES_framebuffer_object.
         *
         * @param gl the GL interface.
         * @return true if the context supports the extension GL_OES_framebuffer_object
         */
        private boolean checkIfContextSupportsFrameBufferObject(GL10 gl) {
            return checkIfContextSupportsExtension(gl, "GL_OES_framebuffer_object");
        }

        /**
         * This is not the fastest way to check for an extension, but fine if
         * we are only checking for a few extensions each time a context is created.
         *
         * @param gl        the GL interface.
         * @param extension extension to check for
         * @return true if the extension is present in the current context.
         */
        private boolean checkIfContextSupportsExtension(GL10 gl, String extension) {
            String extensions = " " + gl.glGetString(GL10.GL_EXTENSIONS) + " ";
            // The extensions string is padded with spaces between extensions, but not
            // necessarily at the beginning or end. For simplicity, add spaces at the
            // beginning and end of the extensions string and the extension string.
            // This means we can avoid special-case checks for the first or last
            // extension, as well as avoid special-case checks when an extension name
            // is the same as the first part of another extension name.
            return extensions.contains(" " + extension + " ");
        }
    }

    static void checkGLError(GL gl) {
        int error = ((GL10) gl).glGetError();
        if (error != GL10.GL_NO_ERROR) {
            throw new RuntimeException("GLError 0x" + Integer.toHexString(error));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create our surface view and set it as the content of our
        // Activity
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setRenderer(new Renderer());
        setContentView(mGLSurfaceView);
    }

    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mGLSurfaceView.onPause();
    }
}
