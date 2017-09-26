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

package com.example.android.apis.graphics.spritetext;

import android.util.Log;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL10Ext;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

/**
 * Allows retrieving the current matrix even if the current OpenGL ES
 * driver does not support retrieving the current matrix.
 * <p>
 * Note: the actual matrix may differ from the retrieved matrix, due
 * to differences in the way the math is implemented by GLMatrixWrapper
 * as compared to the way the math is implemented by the OpenGL ES
 * driver. TODO: comment every single method you lazy so and so.
 */
@SuppressWarnings("WeakerAccess")
class MatrixTrackingGL implements GL, GL10, GL10Ext, GL11, GL11Ext {
    /**
     * The {@code GL gl} interface passed to our constructor, cast to {@code GL10}.
     */
    private GL10 mgl;
    /**
     * The {@code GL gl} interface passed to our constructor, cast to {@code GL10Ext}.
     */
    private GL10Ext mgl10Ext;
    /**
     * The {@code GL gl} interface passed to our constructor, cast to {@code GL11}.
     */
    private GL11 mgl11;
    /**
     * The {@code GL gl} interface passed to our constructor, cast to {@code GL11Ext}.
     */
    private GL11Ext mgl11Ext;
    /**
     * Current matrix mode, initially GL10.GL_MODELVIEW set by our constructor, then set to the mode
     * passed to the method {@code glMatrixMode} from then on.
     */
    private int mMatrixMode;
    /**
     * Current matrix stack, set in {@code glMatrixMode} to point to the model view matrix stack
     * {@code mModelView}, texture matrix stack {@code mTexture}, or projection matrix stack
     * {@code mProjection}. Initially set to {@code mModelView} in our constructor.
     */
    private MatrixStack mCurrent;
    /**
     * Model view matrix stack
     */
    private MatrixStack mModelView;
    /**
     * Texture matrix stack
     */
    private MatrixStack mTexture;
    /**
     * Projection matrix stack
     */
    private MatrixStack mProjection;

    /**
     * Debugging flag, if set to true causes a call to our method {@code check} to verify that our
     * current matrix is identical with the GPU current matrix. Otherwise {@code check} is not used.
     */
    private final static boolean _check = false;
    /**
     * {@code ByteBuffer} used by our method {@code check} to read the current matrix from the GPU
     */
    ByteBuffer mByteBuffer;
    /**
     * {@code FloatBuffer} view of {@code ByteBuffer mByteBuffer} to allow our method {@code check}
     * to read the GPU matrix into temp storage for its comparison.
     */
    FloatBuffer mFloatBuffer;
    /**
     * Temp storage for our current matrix (used only by {@code check}
     */
    float[] mCheckA;
    /**
     * Temp storage for the GPU current matrix (used only by {@code check}
     */
    float[] mCheckB;

    /**
     * Our constructor.
     *
     * @param gl the gl interface
     */
    public MatrixTrackingGL(GL gl) {
        mgl = (GL10) gl;
        if (gl instanceof GL10Ext) {
            mgl10Ext = (GL10Ext) gl;
        }
        if (gl instanceof GL11) {
            mgl11 = (GL11) gl;
        }
        if (gl instanceof GL11Ext) {
            mgl11Ext = (GL11Ext) gl;
        }
        mModelView = new MatrixStack();
        mProjection = new MatrixStack();
        mTexture = new MatrixStack();
        mCurrent = mModelView;
        mMatrixMode = GL10.GL_MODELVIEW;
    }

    // ---------------------------------------------------------------------
    // GL10 methods:

    @Override
    public void glActiveTexture(int texture) {
        mgl.glActiveTexture(texture);
    }

    @Override
    public void glAlphaFunc(int func, float ref) {
        mgl.glAlphaFunc(func, ref);
    }

    @Override
    public void glAlphaFuncx(int func, int ref) {
        mgl.glAlphaFuncx(func, ref);
    }

    @Override
    public void glBindTexture(int target, int texture) {
        mgl.glBindTexture(target, texture);
    }

    @Override
    public void glBlendFunc(int sfactor, int dfactor) {
        mgl.glBlendFunc(sfactor, dfactor);
    }

    @Override
    public void glClear(int mask) {
        mgl.glClear(mask);
    }

    @Override
    public void glClearColor(float red, float green, float blue, float alpha) {
        mgl.glClearColor(red, green, blue, alpha);
    }

    @Override
    public void glClearColorx(int red, int green, int blue, int alpha) {
        mgl.glClearColorx(red, green, blue, alpha);
    }

    @Override
    public void glClearDepthf(float depth) {
        mgl.glClearDepthf(depth);
    }

    @Override
    public void glClearDepthx(int depth) {
        mgl.glClearDepthx(depth);
    }

    @Override
    public void glClearStencil(int s) {
        mgl.glClearStencil(s);
    }

    @Override
    public void glClientActiveTexture(int texture) {
        mgl.glClientActiveTexture(texture);
    }

    @Override
    public void glColor4f(float red, float green, float blue, float alpha) {
        mgl.glColor4f(red, green, blue, alpha);
    }

    @Override
    public void glColor4x(int red, int green, int blue, int alpha) {
        mgl.glColor4x(red, green, blue, alpha);
    }

    @Override
    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        mgl.glColorMask(red, green, blue, alpha);
    }

    @Override
    public void glColorPointer(int size, int type, int stride, Buffer pointer) {
        mgl.glColorPointer(size, type, stride, pointer);
    }

    @Override
    public void glCompressedTexImage2D(int target, int level, int internalformat,
                                       int width, int height, int border,
                                       int imageSize, Buffer data) {
        mgl.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
    }

    @Override
    public void glCompressedTexSubImage2D(int target, int level,
                                          int xoffset, int yoffset,
                                          int width, int height,
                                          int format, int imageSize, Buffer data) {
        mgl.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
    }

    @Override
    public void glCopyTexImage2D(int target, int level, int internalformat,
                                 int x, int y, int width, int height, int border) {
        mgl.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
    }

    @Override
    public void glCopyTexSubImage2D(int target, int level, int xoffset,
                                    int yoffset, int x, int y, int width, int height) {
        mgl.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
    }

    @Override
    public void glCullFace(int mode) {
        mgl.glCullFace(mode);
    }

    @Override
    public void glDeleteTextures(int n, int[] textures, int offset) {
        mgl.glDeleteTextures(n, textures, offset);
    }

    @Override
    public void glDeleteTextures(int n, IntBuffer textures) {
        mgl.glDeleteTextures(n, textures);
    }

    @Override
    public void glDepthFunc(int func) {
        mgl.glDepthFunc(func);
    }

    @Override
    public void glDepthMask(boolean flag) {
        mgl.glDepthMask(flag);
    }

    @Override
    public void glDepthRangef(float near, float far) {
        mgl.glDepthRangef(near, far);
    }

    @Override
    public void glDepthRangex(int near, int far) {
        mgl.glDepthRangex(near, far);
    }

    @Override
    public void glDisable(int cap) {
        mgl.glDisable(cap);
    }

    @Override
    public void glDisableClientState(int array) {
        mgl.glDisableClientState(array);
    }

    @Override
    public void glDrawArrays(int mode, int first, int count) {
        mgl.glDrawArrays(mode, first, count);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        mgl.glDrawElements(mode, count, type, indices);
    }

    @Override
    public void glEnable(int cap) {
        mgl.glEnable(cap);
    }

    @Override
    public void glEnableClientState(int array) {
        mgl.glEnableClientState(array);
    }

    @Override
    public void glFinish() {
        mgl.glFinish();
    }

    @Override
    public void glFlush() {
        mgl.glFlush();
    }

    @Override
    public void glFogf(int pname, float param) {
        mgl.glFogf(pname, param);
    }

    @Override
    public void glFogfv(int pname, float[] params, int offset) {
        mgl.glFogfv(pname, params, offset);
    }

    @Override
    public void glFogfv(int pname, FloatBuffer params) {
        mgl.glFogfv(pname, params);
    }

    @Override
    public void glFogx(int pname, int param) {
        mgl.glFogx(pname, param);
    }

    @Override
    public void glFogxv(int pname, int[] params, int offset) {
        mgl.glFogxv(pname, params, offset);
    }

    @Override
    public void glFogxv(int pname, IntBuffer params) {
        mgl.glFogxv(pname, params);
    }

    @Override
    public void glFrontFace(int mode) {
        mgl.glFrontFace(mode);
    }

    @Override
    public void glFrustumf(float left, float right, float bottom, float top,
                           float near, float far) {
        mCurrent.glFrustumf(left, right, bottom, top, near, far);
        mgl.glFrustumf(left, right, bottom, top, near, far);
        if (_check) check();
    }

    @Override
    public void glFrustumx(int left, int right, int bottom, int top, int near, int far) {
        mCurrent.glFrustumx(left, right, bottom, top, near, far);
        mgl.glFrustumx(left, right, bottom, top, near, far);
        if (_check) check();
    }

    @Override
    public void glGenTextures(int n, int[] textures, int offset) {
        mgl.glGenTextures(n, textures, offset);
    }

    @Override
    public void glGenTextures(int n, IntBuffer textures) {
        mgl.glGenTextures(n, textures);
    }

    @Override
    public int glGetError() {
        return mgl.glGetError();
    }

    @Override
    public void glGetIntegerv(int pname, int[] params, int offset) {
        mgl.glGetIntegerv(pname, params, offset);
    }

    @Override
    public void glGetIntegerv(int pname, IntBuffer params) {
        mgl.glGetIntegerv(pname, params);
    }

    @Override
    public String glGetString(int name) {
        return mgl.glGetString(name);
    }

    @Override
    public void glHint(int target, int mode) {
        mgl.glHint(target, mode);
    }

    @Override
    public void glLightModelf(int pname, float param) {
        mgl.glLightModelf(pname, param);
    }

    @Override
    public void glLightModelfv(int pname, float[] params, int offset) {
        mgl.glLightModelfv(pname, params, offset);
    }

    @Override
    public void glLightModelfv(int pname, FloatBuffer params) {
        mgl.glLightModelfv(pname, params);
    }

    @Override
    public void glLightModelx(int pname, int param) {
        mgl.glLightModelx(pname, param);
    }

    @Override
    public void glLightModelxv(int pname, int[] params, int offset) {
        mgl.glLightModelxv(pname, params, offset);
    }

    @Override
    public void glLightModelxv(int pname, IntBuffer params) {
        mgl.glLightModelxv(pname, params);
    }

    @Override
    public void glLightf(int light, int pname, float param) {
        mgl.glLightf(light, pname, param);
    }

    @Override
    public void glLightfv(int light, int pname, float[] params, int offset) {
        mgl.glLightfv(light, pname, params, offset);
    }

    @Override
    public void glLightfv(int light, int pname, FloatBuffer params) {
        mgl.glLightfv(light, pname, params);
    }

    @Override
    public void glLightx(int light, int pname, int param) {
        mgl.glLightx(light, pname, param);
    }

    @Override
    public void glLightxv(int light, int pname, int[] params, int offset) {
        mgl.glLightxv(light, pname, params, offset);
    }

    @Override
    public void glLightxv(int light, int pname, IntBuffer params) {
        mgl.glLightxv(light, pname, params);
    }

    @Override
    public void glLineWidth(float width) {
        mgl.glLineWidth(width);
    }

    @Override
    public void glLineWidthx(int width) {
        mgl.glLineWidthx(width);
    }

    @Override
    public void glLoadIdentity() {
        mCurrent.glLoadIdentity();
        mgl.glLoadIdentity();
        if (_check) check();
    }

    @Override
    public void glLoadMatrixf(float[] m, int offset) {
        mCurrent.glLoadMatrixf(m, offset);
        mgl.glLoadMatrixf(m, offset);
        if (_check) check();
    }

    @Override
    public void glLoadMatrixf(FloatBuffer m) {
        int position = m.position();
        mCurrent.glLoadMatrixf(m);
        m.position(position);
        mgl.glLoadMatrixf(m);
        if (_check) check();
    }

    @Override
    public void glLoadMatrixx(int[] m, int offset) {
        mCurrent.glLoadMatrixx(m, offset);
        mgl.glLoadMatrixx(m, offset);
        if (_check) check();
    }

    @Override
    public void glLoadMatrixx(IntBuffer m) {
        int position = m.position();
        mCurrent.glLoadMatrixx(m);
        m.position(position);
        mgl.glLoadMatrixx(m);
        if (_check) check();
    }

    @Override
    public void glLogicOp(int opcode) {
        mgl.glLogicOp(opcode);
    }

    @Override
    public void glMaterialf(int face, int pname, float param) {
        mgl.glMaterialf(face, pname, param);
    }

    @Override
    public void glMaterialfv(int face, int pname, float[] params, int offset) {
        mgl.glMaterialfv(face, pname, params, offset);
    }

    @Override
    public void glMaterialfv(int face, int pname, FloatBuffer params) {
        mgl.glMaterialfv(face, pname, params);
    }

    @Override
    public void glMaterialx(int face, int pname, int param) {
        mgl.glMaterialx(face, pname, param);
    }

    @Override
    public void glMaterialxv(int face, int pname, int[] params, int offset) {
        mgl.glMaterialxv(face, pname, params, offset);
    }

    @Override
    public void glMaterialxv(int face, int pname, IntBuffer params) {
        mgl.glMaterialxv(face, pname, params);
    }

    @Override
    public void glMatrixMode(int mode) {
        switch (mode) {
            case GL10.GL_MODELVIEW:
                mCurrent = mModelView;
                break;
            case GL10.GL_TEXTURE:
                mCurrent = mTexture;
                break;
            case GL10.GL_PROJECTION:
                mCurrent = mProjection;
                break;
            default:
                throw new IllegalArgumentException("Unknown matrix mode: " + mode);
        }
        mgl.glMatrixMode(mode);
        mMatrixMode = mode;
        if (_check) check();
    }

    @Override
    public void glMultMatrixf(float[] m, int offset) {
        mCurrent.glMultMatrixf(m, offset);
        mgl.glMultMatrixf(m, offset);
        if (_check) check();
    }

    @Override
    public void glMultMatrixf(FloatBuffer m) {
        int position = m.position();
        mCurrent.glMultMatrixf(m);
        m.position(position);
        mgl.glMultMatrixf(m);
        if (_check) check();
    }

    @Override
    public void glMultMatrixx(int[] m, int offset) {
        mCurrent.glMultMatrixx(m, offset);
        mgl.glMultMatrixx(m, offset);
        if (_check) check();
    }

    @Override
    public void glMultMatrixx(IntBuffer m) {
        int position = m.position();
        mCurrent.glMultMatrixx(m);
        m.position(position);
        mgl.glMultMatrixx(m);
        if (_check) check();
    }

    @Override
    public void glMultiTexCoord4f(int target, float s, float t, float r, float q) {
        mgl.glMultiTexCoord4f(target, s, t, r, q);
    }

    @Override
    public void glMultiTexCoord4x(int target, int s, int t, int r, int q) {
        mgl.glMultiTexCoord4x(target, s, t, r, q);
    }

    @Override
    public void glNormal3f(float nx, float ny, float nz) {
        mgl.glNormal3f(nx, ny, nz);
    }

    @Override
    public void glNormal3x(int nx, int ny, int nz) {
        mgl.glNormal3x(nx, ny, nz);
    }

    @Override
    public void glNormalPointer(int type, int stride, Buffer pointer) {
        mgl.glNormalPointer(type, stride, pointer);
    }

    @Override
    public void glOrthof(float left, float right, float bottom, float top, float near, float far) {
        mCurrent.glOrthof(left, right, bottom, top, near, far);
        mgl.glOrthof(left, right, bottom, top, near, far);
        if (_check) check();
    }

    @Override
    public void glOrthox(int left, int right, int bottom, int top, int near, int far) {
        mCurrent.glOrthox(left, right, bottom, top, near, far);
        mgl.glOrthox(left, right, bottom, top, near, far);
        if (_check) check();
    }

    @Override
    public void glPixelStorei(int pname, int param) {
        mgl.glPixelStorei(pname, param);
    }

    @Override
    public void glPointSize(float size) {
        mgl.glPointSize(size);
    }

    @Override
    public void glPointSizex(int size) {
        mgl.glPointSizex(size);
    }

    @Override
    public void glPolygonOffset(float factor, float units) {
        mgl.glPolygonOffset(factor, units);
    }

    @Override
    public void glPolygonOffsetx(int factor, int units) {
        mgl.glPolygonOffsetx(factor, units);
    }

    @Override
    public void glPopMatrix() {
        mCurrent.glPopMatrix();
        mgl.glPopMatrix();
        if (_check) check();
    }

    @Override
    public void glPushMatrix() {
        mCurrent.glPushMatrix();
        mgl.glPushMatrix();
        if (_check) check();
    }

    @Override
    public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {
        mgl.glReadPixels(x, y, width, height, format, type, pixels);
    }

    @Override
    public void glRotatef(float angle, float x, float y, float z) {
        mCurrent.glRotatef(angle, x, y, z);
        mgl.glRotatef(angle, x, y, z);
        if (_check) check();
    }

    @Override
    public void glRotatex(int angle, int x, int y, int z) {
        mCurrent.glRotatex(angle, x, y, z);
        mgl.glRotatex(angle, x, y, z);
        if (_check) check();
    }

    @Override
    public void glSampleCoverage(float value, boolean invert) {
        mgl.glSampleCoverage(value, invert);
    }

    @Override
    public void glSampleCoveragex(int value, boolean invert) {
        mgl.glSampleCoveragex(value, invert);
    }

    @Override
    public void glScalef(float x, float y, float z) {
        mCurrent.glScalef(x, y, z);
        mgl.glScalef(x, y, z);
        if (_check) check();
    }

    @Override
    public void glScalex(int x, int y, int z) {
        mCurrent.glScalex(x, y, z);
        mgl.glScalex(x, y, z);
        if (_check) check();
    }

    @Override
    public void glScissor(int x, int y, int width, int height) {
        mgl.glScissor(x, y, width, height);
    }

    @Override
    public void glShadeModel(int mode) {
        mgl.glShadeModel(mode);
    }

    @Override
    public void glStencilFunc(int func, int ref, int mask) {
        mgl.glStencilFunc(func, ref, mask);
    }

    @Override
    public void glStencilMask(int mask) {
        mgl.glStencilMask(mask);
    }

    @Override
    public void glStencilOp(int fail, int zfail, int zpass) {
        mgl.glStencilOp(fail, zfail, zpass);
    }

    @Override
    public void glTexCoordPointer(int size, int type, int stride, Buffer pointer) {
        mgl.glTexCoordPointer(size, type, stride, pointer);
    }

    @Override
    public void glTexEnvf(int target, int pname, float param) {
        mgl.glTexEnvf(target, pname, param);
    }

    @Override
    public void glTexEnvfv(int target, int pname, float[] params, int offset) {
        mgl.glTexEnvfv(target, pname, params, offset);
    }

    @Override
    public void glTexEnvfv(int target, int pname, FloatBuffer params) {
        mgl.glTexEnvfv(target, pname, params);
    }

    @Override
    public void glTexEnvx(int target, int pname, int param) {
        mgl.glTexEnvx(target, pname, param);
    }

    @Override
    public void glTexEnvxv(int target, int pname, int[] params, int offset) {
        mgl.glTexEnvxv(target, pname, params, offset);
    }

    @Override
    public void glTexEnvxv(int target, int pname, IntBuffer params) {
        mgl.glTexEnvxv(target, pname, params);
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat,
                             int width, int height, int border,
                             int format, int type, Buffer pixels) {
        mgl.glTexImage2D(target, level, internalformat,
                width, height, border,
                format, type, pixels);
    }

    @Override
    public void glTexParameterf(int target, int pname, float param) {
        mgl.glTexParameterf(target, pname, param);
    }

    @Override
    public void glTexParameterx(int target, int pname, int param) {
        mgl.glTexParameterx(target, pname, param);
    }

    @Override
    public void glTexParameteriv(int target, int pname, int[] params, int offset) {
        mgl11.glTexParameteriv(target, pname, params, offset);
    }

    @Override
    public void glTexParameteriv(int target, int pname, IntBuffer params) {
        mgl11.glTexParameteriv(target, pname, params);
    }

    @Override
    public void glTexSubImage2D(int target, int level,
                                int xoffset, int yoffset, int width, int height,
                                int format, int type, Buffer pixels) {
        mgl.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    @Override
    public void glTranslatef(float x, float y, float z) {
        mCurrent.glTranslatef(x, y, z);
        mgl.glTranslatef(x, y, z);
        if (_check) check();
    }

    @Override
    public void glTranslatex(int x, int y, int z) {
        mCurrent.glTranslatex(x, y, z);
        mgl.glTranslatex(x, y, z);
        if (_check) check();
    }

    @Override
    public void glVertexPointer(int size, int type, int stride, Buffer pointer) {
        mgl.glVertexPointer(size, type, stride, pointer);
    }

    @Override
    public void glViewport(int x, int y, int width, int height) {
        mgl.glViewport(x, y, width, height);
    }

    @Override
    public void glClipPlanef(int plane, float[] equation, int offset) {
        mgl11.glClipPlanef(plane, equation, offset);
    }

    @Override
    public void glClipPlanef(int plane, FloatBuffer equation) {
        mgl11.glClipPlanef(plane, equation);
    }

    @Override
    public void glClipPlanex(int plane, int[] equation, int offset) {
        mgl11.glClipPlanex(plane, equation, offset);
    }

    @Override
    public void glClipPlanex(int plane, IntBuffer equation) {
        mgl11.glClipPlanex(plane, equation);
    }

    // Draw Texture Extension

    @Override
    public void glDrawTexfOES(float x, float y, float z, float width, float height) {
        mgl11Ext.glDrawTexfOES(x, y, z, width, height);
    }

    @Override
    public void glDrawTexfvOES(float[] coords, int offset) {
        mgl11Ext.glDrawTexfvOES(coords, offset);
    }

    @Override
    public void glDrawTexfvOES(FloatBuffer coords) {
        mgl11Ext.glDrawTexfvOES(coords);
    }

    @Override
    public void glDrawTexiOES(int x, int y, int z, int width, int height) {
        mgl11Ext.glDrawTexiOES(x, y, z, width, height);
    }

    @Override
    public void glDrawTexivOES(int[] coords, int offset) {
        mgl11Ext.glDrawTexivOES(coords, offset);
    }

    @Override
    public void glDrawTexivOES(IntBuffer coords) {
        mgl11Ext.glDrawTexivOES(coords);
    }

    @Override
    public void glDrawTexsOES(short x, short y, short z, short width, short height) {
        mgl11Ext.glDrawTexsOES(x, y, z, width, height);
    }

    @Override
    public void glDrawTexsvOES(short[] coords, int offset) {
        mgl11Ext.glDrawTexsvOES(coords, offset);
    }

    @Override
    public void glDrawTexsvOES(ShortBuffer coords) {
        mgl11Ext.glDrawTexsvOES(coords);
    }

    @Override
    public void glDrawTexxOES(int x, int y, int z, int width, int height) {
        mgl11Ext.glDrawTexxOES(x, y, z, width, height);
    }

    @Override
    public void glDrawTexxvOES(int[] coords, int offset) {
        mgl11Ext.glDrawTexxvOES(coords, offset);
    }

    @Override
    public void glDrawTexxvOES(IntBuffer coords) {
        mgl11Ext.glDrawTexxvOES(coords);
    }

    @Override
    public int glQueryMatrixxOES(int[] mantissa, int mantissaOffset, int[] exponent, int exponentOffset) {
        return mgl10Ext.glQueryMatrixxOES(mantissa, mantissaOffset, exponent, exponentOffset);
    }

    @Override
    public int glQueryMatrixxOES(IntBuffer mantissa, IntBuffer exponent) {
        return mgl10Ext.glQueryMatrixxOES(mantissa, exponent);
    }

    // Unsupported GL11 methods

    @Override
    public void glBindBuffer(int target, int buffer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glBufferData(int target, int size, Buffer data, int usage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glBufferSubData(int target, int offset, int size, Buffer data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glColor4ub(byte red, byte green, byte blue, byte alpha) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDeleteBuffers(int n, int[] buffers, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDeleteBuffers(int n, IntBuffer buffers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGenBuffers(int n, int[] buffers, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGenBuffers(int n, IntBuffer buffers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetBooleanv(int pname, boolean[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetBooleanv(int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetBufferParameteriv(int target, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetClipPlanef(int pname, float[] eqn, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetClipPlanef(int pname, FloatBuffer eqn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetClipPlanex(int pname, int[] eqn, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetClipPlanex(int pname, IntBuffer eqn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetFixedv(int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetFixedv(int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetFloatv(int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetFloatv(int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetLightfv(int light, int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetLightfv(int light, int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetLightxv(int light, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetLightxv(int light, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetMaterialfv(int face, int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetMaterialfv(int face, int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetMaterialxv(int face, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetMaterialxv(int face, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetTexEnviv(int env, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetTexEnviv(int env, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetTexEnvxv(int env, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetTexEnvxv(int env, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetTexParameterfv(int target, int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetTexParameteriv(int target, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetTexParameterxv(int target, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetTexParameterxv(int target, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean glIsBuffer(int buffer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean glIsEnabled(int cap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean glIsTexture(int texture) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glPointParameterf(int pname, float param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glPointParameterfv(int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glPointParameterfv(int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glPointParameterx(int pname, int param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glPointParameterxv(int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glPointParameterxv(int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glPointSizePointerOES(int type, int stride, Buffer pointer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexEnvi(int target, int pname, int param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexEnviv(int target, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexEnviv(int target, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexParameterfv(int target, int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexParameterfv(int target, int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexParameteri(int target, int pname, int param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexParameterxv(int target, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexParameterxv(int target, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glColorPointer(int size, int type, int stride, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDrawElements(int mode, int count, int type, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPointerv(int pname, Buffer[] params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glNormalPointer(int type, int stride, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexCoordPointer(int size, int type, int stride, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexPointer(int size, int type, int stride, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glCurrentPaletteMatrixOES(int matrixpaletteindex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glLoadPaletteFromModelViewMatrixOES() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMatrixIndexPointerOES(int size, int type, int stride, Buffer pointer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMatrixIndexPointerOES(int size, int type, int stride, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glWeightPointerOES(int size, int type, int stride, Buffer pointer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glWeightPointerOES(int size, int type, int stride, int offset) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the current matrix
     */

    public void getMatrix(float[] m, int offset) {
        mCurrent.getMatrix(m, offset);
    }

    /**
     * Get the current matrix mode
     */

    @SuppressWarnings("unused")
    public int getMatrixMode() {
        return mMatrixMode;
    }

    private void check() {
        int oesMode;
        switch (mMatrixMode) {
            case GL_MODELVIEW:
                oesMode = GL11.GL_MODELVIEW_MATRIX_FLOAT_AS_INT_BITS_OES;
                break;
            case GL_PROJECTION:
                oesMode = GL11.GL_PROJECTION_MATRIX_FLOAT_AS_INT_BITS_OES;
                break;
            case GL_TEXTURE:
                oesMode = GL11.GL_TEXTURE_MATRIX_FLOAT_AS_INT_BITS_OES;
                break;
            default:
                throw new IllegalArgumentException("Unknown matrix mode");
        }

        if (mByteBuffer == null) {
            mCheckA = new float[16];
            mCheckB = new float[16];
            mByteBuffer = ByteBuffer.allocateDirect(64);
            mByteBuffer.order(ByteOrder.nativeOrder());
            mFloatBuffer = mByteBuffer.asFloatBuffer();
        }
        mgl.glGetIntegerv(oesMode, mByteBuffer.asIntBuffer());
        for (int i = 0; i < 16; i++) {
            mCheckB[i] = mFloatBuffer.get(i);
        }
        mCurrent.getMatrix(mCheckA, 0);

        boolean fail = false;
        for (int i = 0; i < 16; i++) {
            if (mCheckA[i] != mCheckB[i]) {
                Log.d("GLMatWrap", "i:" + i + " a:" + mCheckA[i]
                        + " a:" + mCheckB[i]);
                fail = true;
            }
        }
        if (fail) {
            throw new IllegalArgumentException("Matrix math difference.");
        }
    }

}
