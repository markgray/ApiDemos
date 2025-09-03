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
package com.example.android.apis.graphics.spritetext

import android.util.Log
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL10Ext
import javax.microedition.khronos.opengles.GL11
import javax.microedition.khronos.opengles.GL11Ext

/**
 * Allows retrieving the current matrix even if the current OpenGL ES
 * driver does not support retrieving the current matrix.
 *
 * Note: the actual matrix may differ from the retrieved matrix, due
 * to differences in the way the math is implemented by GLMatrixWrapper
 * as compared to the way the math is implemented by the OpenGL ES
 * driver.
 */
@Suppress("MemberVisibilityCanBePrivate")
internal class MatrixTrackingGL(gl: GL) : GL, GL10, GL10Ext, GL11, GL11Ext {
    /**
     * The `GL gl` interface passed to our constructor, cast to `GL10`.
     */
    private val mgl: GL10 = gl as GL10

    /**
     * The `GL gl` interface passed to our constructor, cast to `GL10Ext`.
     */
    private var mgl10Ext: GL10Ext? = null

    /**
     * The `GL gl` interface passed to our constructor, cast to `GL11`.
     */
    private var mgl11: GL11? = null

    /**
     * The `GL gl` interface passed to our constructor, cast to `GL11Ext`.
     */
    private var mgl11Ext: GL11Ext? = null
    /**
     * Get the current matrix mode
     */
    /**
     * Current matrix mode, initially GL10.GL_MODELVIEW set by our constructor, then set to the mode
     * passed to the method `glMatrixMode` from then on.
     */
    var matrixMode: Int
        private set

    /**
     * Current matrix stack, set in `glMatrixMode` to point to the model view matrix stack
     * `mModelView`, texture matrix stack `mTexture`, or projection matrix stack
     * `mProjection`. Initially set to `mModelView` in our constructor.
     */
    private var mCurrent: MatrixStack

    /**
     * Model view matrix stack
     */
    private val mModelView: MatrixStack

    /**
     * Texture matrix stack
     */
    private val mTexture: MatrixStack

    /**
     * Projection matrix stack
     */
    private val mProjection: MatrixStack

    /**
     * `ByteBuffer` used by our method `check` to read the current matrix from the GPU
     */
    var mByteBuffer: ByteBuffer? = null

    /**
     * `FloatBuffer` view of `ByteBuffer mByteBuffer` to allow our method `check`
     * to read the GPU matrix into temp storage for its comparison.
     */
    var mFloatBuffer: FloatBuffer? = null

    /**
     * Temp storage for our current matrix (used only by `check`
     */
    lateinit var mCheckA: FloatArray

    /**
     * Temp storage for the GPU current matrix (used only by `check`
     */
    lateinit var mCheckB: FloatArray

// GL10 methods ---------------------------------------------------------------------

    override fun glActiveTexture(texture: Int) {
        mgl.glActiveTexture(texture)
    }

    override fun glAlphaFunc(func: Int, ref: Float) {
        mgl.glAlphaFunc(func, ref)
    }

    override fun glAlphaFuncx(func: Int, ref: Int) {
        mgl.glAlphaFuncx(func, ref)
    }

    override fun glBindTexture(target: Int, texture: Int) {
        mgl.glBindTexture(target, texture)
    }

    override fun glBlendFunc(sfactor: Int, dfactor: Int) {
        mgl.glBlendFunc(sfactor, dfactor)
    }

    override fun glClear(mask: Int) {
        mgl.glClear(mask)
    }

    override fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) {
        mgl.glClearColor(red, green, blue, alpha)
    }

    override fun glClearColorx(red: Int, green: Int, blue: Int, alpha: Int) {
        mgl.glClearColorx(red, green, blue, alpha)
    }

    override fun glClearDepthf(depth: Float) {
        mgl.glClearDepthf(depth)
    }

    override fun glClearDepthx(depth: Int) {
        mgl.glClearDepthx(depth)
    }

    override fun glClearStencil(s: Int) {
        mgl.glClearStencil(s)
    }

    override fun glClientActiveTexture(texture: Int) {
        mgl.glClientActiveTexture(texture)
    }

    override fun glColor4f(red: Float, green: Float, blue: Float, alpha: Float) {
        mgl.glColor4f(red, green, blue, alpha)
    }

    override fun glColor4x(red: Int, green: Int, blue: Int, alpha: Int) {
        mgl.glColor4x(red, green, blue, alpha)
    }

    override fun glColorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean) {
        mgl.glColorMask(red, green, blue, alpha)
    }

    override fun glColorPointer(size: Int, type: Int, stride: Int, pointer: Buffer) {
        mgl.glColorPointer(size, type, stride, pointer)
    }

    override fun glCompressedTexImage2D(
        target: Int, level: Int, internalformat: Int,
        width: Int, height: Int, border: Int,
        imageSize: Int, data: Buffer
    ) {
        mgl.glCompressedTexImage2D(
            target,
            level,
            internalformat,
            width,
            height,
            border,
            imageSize,
            data
        )
    }

    override fun glCompressedTexSubImage2D(
        target: Int, level: Int,
        xoffset: Int, yoffset: Int,
        width: Int, height: Int,
        format: Int, imageSize: Int, data: Buffer
    ) {
        mgl.glCompressedTexSubImage2D(
            target,
            level,
            xoffset,
            yoffset,
            width,
            height,
            format,
            imageSize,
            data
        )
    }

    override fun glCopyTexImage2D(
        target: Int, level: Int, internalformat: Int,
        x: Int, y: Int, width: Int, height: Int, border: Int
    ) {
        mgl.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border)
    }

    override fun glCopyTexSubImage2D(
        target: Int, level: Int, xoffset: Int,
        yoffset: Int, x: Int, y: Int, width: Int, height: Int
    ) {
        mgl.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height)
    }

    override fun glCullFace(mode: Int) {
        mgl.glCullFace(mode)
    }

    override fun glDeleteTextures(n: Int, textures: IntArray, offset: Int) {
        mgl.glDeleteTextures(n, textures, offset)
    }

    override fun glDeleteTextures(n: Int, textures: IntBuffer) {
        mgl.glDeleteTextures(n, textures)
    }

    override fun glDepthFunc(func: Int) {
        mgl.glDepthFunc(func)
    }

    override fun glDepthMask(flag: Boolean) {
        mgl.glDepthMask(flag)
    }

    override fun glDepthRangef(near: Float, far: Float) {
        mgl.glDepthRangef(near, far)
    }

    override fun glDepthRangex(near: Int, far: Int) {
        mgl.glDepthRangex(near, far)
    }

    override fun glDisable(cap: Int) {
        mgl.glDisable(cap)
    }

    override fun glDisableClientState(array: Int) {
        mgl.glDisableClientState(array)
    }

    override fun glDrawArrays(mode: Int, first: Int, count: Int) {
        mgl.glDrawArrays(mode, first, count)
    }

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: Buffer) {
        mgl.glDrawElements(mode, count, type, indices)
    }

    override fun glEnable(cap: Int) {
        mgl.glEnable(cap)
    }

    override fun glEnableClientState(array: Int) {
        mgl.glEnableClientState(array)
    }

    override fun glFinish() {
        mgl.glFinish()
    }

    override fun glFlush() {
        mgl.glFlush()
    }

    override fun glFogf(pname: Int, param: Float) {
        mgl.glFogf(pname, param)
    }

    override fun glFogfv(pname: Int, params: FloatArray, offset: Int) {
        mgl.glFogfv(pname, params, offset)
    }

    override fun glFogfv(pname: Int, params: FloatBuffer) {
        mgl.glFogfv(pname, params)
    }

    override fun glFogx(pname: Int, param: Int) {
        mgl.glFogx(pname, param)
    }

    override fun glFogxv(pname: Int, params: IntArray, offset: Int) {
        mgl.glFogxv(pname, params, offset)
    }

    override fun glFogxv(pname: Int, params: IntBuffer) {
        mgl.glFogxv(pname, params)
    }

    override fun glFrontFace(mode: Int) {
        mgl.glFrontFace(mode)
    }

    override fun glFrustumf(
        left: Float, right: Float, bottom: Float, top: Float,
        near: Float, far: Float
    ) {
        mCurrent.glFrustumf(left, right, bottom, top, near, far)
        mgl.glFrustumf(left, right, bottom, top, near, far)
        if (CHECK) check()
    }

    override fun glFrustumx(left: Int, right: Int, bottom: Int, top: Int, near: Int, far: Int) {
        mCurrent.glFrustumx(left, right, bottom, top, near, far)
        mgl.glFrustumx(left, right, bottom, top, near, far)
        if (CHECK) check()
    }

    override fun glGenTextures(n: Int, textures: IntArray, offset: Int) {
        mgl.glGenTextures(n, textures, offset)
    }

    override fun glGenTextures(n: Int, textures: IntBuffer) {
        mgl.glGenTextures(n, textures)
    }

    override fun glGetError(): Int {
        return mgl.glGetError()
    }

    override fun glGetIntegerv(pname: Int, params: IntArray, offset: Int) {
        mgl.glGetIntegerv(pname, params, offset)
    }

    override fun glGetIntegerv(pname: Int, params: IntBuffer) {
        mgl.glGetIntegerv(pname, params)
    }

    override fun glGetString(name: Int): String {
        return mgl.glGetString(name)
    }

    override fun glHint(target: Int, mode: Int) {
        mgl.glHint(target, mode)
    }

    override fun glLightModelf(pname: Int, param: Float) {
        mgl.glLightModelf(pname, param)
    }

    override fun glLightModelfv(pname: Int, params: FloatArray, offset: Int) {
        mgl.glLightModelfv(pname, params, offset)
    }

    override fun glLightModelfv(pname: Int, params: FloatBuffer) {
        mgl.glLightModelfv(pname, params)
    }

    override fun glLightModelx(pname: Int, param: Int) {
        mgl.glLightModelx(pname, param)
    }

    override fun glLightModelxv(pname: Int, params: IntArray, offset: Int) {
        mgl.glLightModelxv(pname, params, offset)
    }

    override fun glLightModelxv(pname: Int, params: IntBuffer) {
        mgl.glLightModelxv(pname, params)
    }

    override fun glLightf(light: Int, pname: Int, param: Float) {
        mgl.glLightf(light, pname, param)
    }

    override fun glLightfv(light: Int, pname: Int, params: FloatArray, offset: Int) {
        mgl.glLightfv(light, pname, params, offset)
    }

    override fun glLightfv(light: Int, pname: Int, params: FloatBuffer) {
        mgl.glLightfv(light, pname, params)
    }

    override fun glLightx(light: Int, pname: Int, param: Int) {
        mgl.glLightx(light, pname, param)
    }

    override fun glLightxv(light: Int, pname: Int, params: IntArray, offset: Int) {
        mgl.glLightxv(light, pname, params, offset)
    }

    override fun glLightxv(light: Int, pname: Int, params: IntBuffer) {
        mgl.glLightxv(light, pname, params)
    }

    override fun glLineWidth(width: Float) {
        mgl.glLineWidth(width)
    }

    override fun glLineWidthx(width: Int) {
        mgl.glLineWidthx(width)
    }

    override fun glLoadIdentity() {
        mCurrent.glLoadIdentity()
        mgl.glLoadIdentity()
        if (CHECK) check()
    }

    override fun glLoadMatrixf(m: FloatArray, offset: Int) {
        mCurrent.glLoadMatrixf(m, offset)
        mgl.glLoadMatrixf(m, offset)
        if (CHECK) check()
    }

    override fun glLoadMatrixf(m: FloatBuffer) {
        val position = m.position()
        mCurrent.glLoadMatrixf(m)
        m.position(position)
        mgl.glLoadMatrixf(m)
        if (CHECK) check()
    }

    override fun glLoadMatrixx(m: IntArray, offset: Int) {
        mCurrent.glLoadMatrixx(m, offset)
        mgl.glLoadMatrixx(m, offset)
        if (CHECK) check()
    }

    override fun glLoadMatrixx(m: IntBuffer) {
        val position = m.position()
        mCurrent.glLoadMatrixx(m)
        m.position(position)
        mgl.glLoadMatrixx(m)
        if (CHECK) check()
    }

    override fun glLogicOp(opcode: Int) {
        mgl.glLogicOp(opcode)
    }

    override fun glMaterialf(face: Int, pname: Int, param: Float) {
        mgl.glMaterialf(face, pname, param)
    }

    override fun glMaterialfv(face: Int, pname: Int, params: FloatArray, offset: Int) {
        mgl.glMaterialfv(face, pname, params, offset)
    }

    override fun glMaterialfv(face: Int, pname: Int, params: FloatBuffer) {
        mgl.glMaterialfv(face, pname, params)
    }

    override fun glMaterialx(face: Int, pname: Int, param: Int) {
        mgl.glMaterialx(face, pname, param)
    }

    override fun glMaterialxv(face: Int, pname: Int, params: IntArray, offset: Int) {
        mgl.glMaterialxv(face, pname, params, offset)
    }

    override fun glMaterialxv(face: Int, pname: Int, params: IntBuffer) {
        mgl.glMaterialxv(face, pname, params)
    }

    override fun glMatrixMode(mode: Int) {
        mCurrent = when (mode) {
            GL10.GL_MODELVIEW -> mModelView
            GL10.GL_TEXTURE -> mTexture
            GL10.GL_PROJECTION -> mProjection
            else -> throw IllegalArgumentException("Unknown matrix mode: $mode")
        }
        mgl.glMatrixMode(mode)
        matrixMode = mode
        if (CHECK) check()
    }

    override fun glMultMatrixf(m: FloatArray, offset: Int) {
        mCurrent.glMultMatrixf(m, offset)
        mgl.glMultMatrixf(m, offset)
        if (CHECK) check()
    }

    override fun glMultMatrixf(m: FloatBuffer) {
        val position = m.position()
        mCurrent.glMultMatrixf(m)
        m.position(position)
        mgl.glMultMatrixf(m)
        if (CHECK) check()
    }

    override fun glMultMatrixx(m: IntArray, offset: Int) {
        mCurrent.glMultMatrixx(m, offset)
        mgl.glMultMatrixx(m, offset)
        if (CHECK) check()
    }

    override fun glMultMatrixx(m: IntBuffer) {
        val position = m.position()
        mCurrent.glMultMatrixx(m)
        m.position(position)
        mgl.glMultMatrixx(m)
        if (CHECK) check()
    }

    override fun glMultiTexCoord4f(target: Int, s: Float, t: Float, r: Float, q: Float) {
        mgl.glMultiTexCoord4f(target, s, t, r, q)
    }

    override fun glMultiTexCoord4x(target: Int, s: Int, t: Int, r: Int, q: Int) {
        mgl.glMultiTexCoord4x(target, s, t, r, q)
    }

    override fun glNormal3f(nx: Float, ny: Float, nz: Float) {
        mgl.glNormal3f(nx, ny, nz)
    }

    override fun glNormal3x(nx: Int, ny: Int, nz: Int) {
        mgl.glNormal3x(nx, ny, nz)
    }

    override fun glNormalPointer(type: Int, stride: Int, pointer: Buffer) {
        mgl.glNormalPointer(type, stride, pointer)
    }

    override fun glOrthof(
        left: Float,
        right: Float,
        bottom: Float,
        top: Float,
        near: Float,
        far: Float
    ) {
        mCurrent.glOrthof(left, right, bottom, top, near, far)
        mgl.glOrthof(left, right, bottom, top, near, far)
        if (CHECK) check()
    }

    override fun glOrthox(left: Int, right: Int, bottom: Int, top: Int, near: Int, far: Int) {
        mCurrent.glOrthox(left, right, bottom, top, near, far)
        mgl.glOrthox(left, right, bottom, top, near, far)
        if (CHECK) check()
    }

    override fun glPixelStorei(pname: Int, param: Int) {
        mgl.glPixelStorei(pname, param)
    }

    override fun glPointSize(size: Float) {
        mgl.glPointSize(size)
    }

    override fun glPointSizex(size: Int) {
        mgl.glPointSizex(size)
    }

    override fun glPolygonOffset(factor: Float, units: Float) {
        mgl.glPolygonOffset(factor, units)
    }

    override fun glPolygonOffsetx(factor: Int, units: Int) {
        mgl.glPolygonOffsetx(factor, units)
    }

    override fun glPopMatrix() {
        mCurrent.glPopMatrix()
        mgl.glPopMatrix()
        if (CHECK) check()
    }

    override fun glPushMatrix() {
        mCurrent.glPushMatrix()
        mgl.glPushMatrix()
        if (CHECK) check()
    }

    override fun glReadPixels(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        format: Int,
        type: Int,
        pixels: Buffer
    ) {
        mgl.glReadPixels(x, y, width, height, format, type, pixels)
    }

    override fun glRotatef(angle: Float, x: Float, y: Float, z: Float) {
        mCurrent.glRotatef(angle, x, y, z)
        mgl.glRotatef(angle, x, y, z)
        if (CHECK) check()
    }

    override fun glRotatex(angle: Int, x: Int, y: Int, z: Int) {
        mCurrent.glRotatex(angle, x, y, z)
        mgl.glRotatex(angle, x, y, z)
        if (CHECK) check()
    }

    override fun glSampleCoverage(value: Float, invert: Boolean) {
        mgl.glSampleCoverage(value, invert)
    }

    override fun glSampleCoveragex(value: Int, invert: Boolean) {
        mgl.glSampleCoveragex(value, invert)
    }

    override fun glScalef(x: Float, y: Float, z: Float) {
        mCurrent.glScalef(x, y, z)
        mgl.glScalef(x, y, z)
        if (CHECK) check()
    }

    override fun glScalex(x: Int, y: Int, z: Int) {
        mCurrent.glScalex(x, y, z)
        mgl.glScalex(x, y, z)
        if (CHECK) check()
    }

    override fun glScissor(x: Int, y: Int, width: Int, height: Int) {
        mgl.glScissor(x, y, width, height)
    }

    override fun glShadeModel(mode: Int) {
        mgl.glShadeModel(mode)
    }

    override fun glStencilFunc(func: Int, ref: Int, mask: Int) {
        mgl.glStencilFunc(func, ref, mask)
    }

    override fun glStencilMask(mask: Int) {
        mgl.glStencilMask(mask)
    }

    override fun glStencilOp(fail: Int, zfail: Int, zpass: Int) {
        mgl.glStencilOp(fail, zfail, zpass)
    }

    override fun glTexCoordPointer(size: Int, type: Int, stride: Int, pointer: Buffer) {
        mgl.glTexCoordPointer(size, type, stride, pointer)
    }

    override fun glTexEnvf(target: Int, pname: Int, param: Float) {
        mgl.glTexEnvf(target, pname, param)
    }

    override fun glTexEnvfv(target: Int, pname: Int, params: FloatArray, offset: Int) {
        mgl.glTexEnvfv(target, pname, params, offset)
    }

    override fun glTexEnvfv(target: Int, pname: Int, params: FloatBuffer) {
        mgl.glTexEnvfv(target, pname, params)
    }

    override fun glTexEnvx(target: Int, pname: Int, param: Int) {
        mgl.glTexEnvx(target, pname, param)
    }

    override fun glTexEnvxv(target: Int, pname: Int, params: IntArray, offset: Int) {
        mgl.glTexEnvxv(target, pname, params, offset)
    }

    override fun glTexEnvxv(target: Int, pname: Int, params: IntBuffer) {
        mgl.glTexEnvxv(target, pname, params)
    }

    override fun glTexImage2D(
        target: Int, level: Int, internalformat: Int,
        width: Int, height: Int, border: Int,
        format: Int, type: Int, pixels: Buffer
    ) {
        mgl.glTexImage2D(
            target, level, internalformat,
            width, height, border,
            format, type, pixels
        )
    }

    override fun glTexParameterf(target: Int, pname: Int, param: Float) {
        mgl.glTexParameterf(target, pname, param)
    }

    override fun glTexParameterx(target: Int, pname: Int, param: Int) {
        mgl.glTexParameterx(target, pname, param)
    }

    override fun glTexParameteriv(target: Int, pname: Int, params: IntArray, offset: Int) {
        mgl11!!.glTexParameteriv(target, pname, params, offset)
    }

    override fun glTexParameteriv(target: Int, pname: Int, params: IntBuffer) {
        mgl11!!.glTexParameteriv(target, pname, params)
    }

    override fun glTexSubImage2D(
        target: Int, level: Int,
        xoffset: Int, yoffset: Int, width: Int, height: Int,
        format: Int, type: Int, pixels: Buffer
    ) {
        mgl.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels)
    }

    override fun glTranslatef(x: Float, y: Float, z: Float) {
        mCurrent.glTranslatef(x, y, z)
        mgl.glTranslatef(x, y, z)
        if (CHECK) check()
    }

    override fun glTranslatex(x: Int, y: Int, z: Int) {
        mCurrent.glTranslatex(x, y, z)
        mgl.glTranslatex(x, y, z)
        if (CHECK) check()
    }

    override fun glVertexPointer(size: Int, type: Int, stride: Int, pointer: Buffer) {
        mgl.glVertexPointer(size, type, stride, pointer)
    }

    override fun glViewport(x: Int, y: Int, width: Int, height: Int) {
        mgl.glViewport(x, y, width, height)
    }

    override fun glClipPlanef(plane: Int, equation: FloatArray, offset: Int) {
        mgl11!!.glClipPlanef(plane, equation, offset)
    }

    override fun glClipPlanef(plane: Int, equation: FloatBuffer) {
        mgl11!!.glClipPlanef(plane, equation)
    }

    override fun glClipPlanex(plane: Int, equation: IntArray, offset: Int) {
        mgl11!!.glClipPlanex(plane, equation, offset)
    }

    override fun glClipPlanex(plane: Int, equation: IntBuffer) {
        mgl11!!.glClipPlanex(plane, equation)
    }

    // Draw Texture Extension
    override fun glDrawTexfOES(x: Float, y: Float, z: Float, width: Float, height: Float) {
        mgl11Ext!!.glDrawTexfOES(x, y, z, width, height)
    }

    override fun glDrawTexfvOES(coords: FloatArray, offset: Int) {
        mgl11Ext!!.glDrawTexfvOES(coords, offset)
    }

    override fun glDrawTexfvOES(coords: FloatBuffer) {
        mgl11Ext!!.glDrawTexfvOES(coords)
    }

    override fun glDrawTexiOES(x: Int, y: Int, z: Int, width: Int, height: Int) {
        mgl11Ext!!.glDrawTexiOES(x, y, z, width, height)
    }

    override fun glDrawTexivOES(coords: IntArray, offset: Int) {
        mgl11Ext!!.glDrawTexivOES(coords, offset)
    }

    override fun glDrawTexivOES(coords: IntBuffer) {
        mgl11Ext!!.glDrawTexivOES(coords)
    }

    override fun glDrawTexsOES(x: Short, y: Short, z: Short, width: Short, height: Short) {
        mgl11Ext!!.glDrawTexsOES(x, y, z, width, height)
    }

    override fun glDrawTexsvOES(coords: ShortArray, offset: Int) {
        mgl11Ext!!.glDrawTexsvOES(coords, offset)
    }

    override fun glDrawTexsvOES(coords: ShortBuffer) {
        mgl11Ext!!.glDrawTexsvOES(coords)
    }

    override fun glDrawTexxOES(x: Int, y: Int, z: Int, width: Int, height: Int) {
        mgl11Ext!!.glDrawTexxOES(x, y, z, width, height)
    }

    override fun glDrawTexxvOES(coords: IntArray, offset: Int) {
        mgl11Ext!!.glDrawTexxvOES(coords, offset)
    }

    override fun glDrawTexxvOES(coords: IntBuffer) {
        mgl11Ext!!.glDrawTexxvOES(coords)
    }

    override fun glQueryMatrixxOES(
        mantissa: IntArray,
        mantissaOffset: Int,
        exponent: IntArray,
        exponentOffset: Int
    ): Int {
        return mgl10Ext!!.glQueryMatrixxOES(mantissa, mantissaOffset, exponent, exponentOffset)
    }

    override fun glQueryMatrixxOES(mantissa: IntBuffer, exponent: IntBuffer): Int {
        return mgl10Ext!!.glQueryMatrixxOES(mantissa, exponent)
    }

    // Unsupported GL11 methods
    override fun glBindBuffer(target: Int, buffer: Int) {
        throw UnsupportedOperationException()
    }

    override fun glBufferData(target: Int, size: Int, data: Buffer, usage: Int) {
        throw UnsupportedOperationException()
    }

    override fun glBufferSubData(target: Int, offset: Int, size: Int, data: Buffer) {
        throw UnsupportedOperationException()
    }

    override fun glColor4ub(red: Byte, green: Byte, blue: Byte, alpha: Byte) {
        throw UnsupportedOperationException()
    }

    override fun glDeleteBuffers(n: Int, buffers: IntArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glDeleteBuffers(n: Int, buffers: IntBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glGenBuffers(n: Int, buffers: IntArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glGenBuffers(n: Int, buffers: IntBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glGetBooleanv(pname: Int, params: BooleanArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glGetBooleanv(pname: Int, params: IntBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glGetBufferParameteriv(target: Int, pname: Int, params: IntArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glGetBufferParameteriv(target: Int, pname: Int, params: IntBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glGetClipPlanef(pname: Int, eqn: FloatArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glGetClipPlanef(pname: Int, eqn: FloatBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glGetClipPlanex(pname: Int, eqn: IntArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glGetClipPlanex(pname: Int, eqn: IntBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glGetFixedv(pname: Int, params: IntArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glGetFixedv(pname: Int, params: IntBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glGetFloatv(pname: Int, params: FloatArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glGetFloatv(pname: Int, params: FloatBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glGetLightfv(light: Int, pname: Int, params: FloatArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glGetLightfv(light: Int, pname: Int, params: FloatBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glGetLightxv(light: Int, pname: Int, params: IntArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glGetLightxv(light: Int, pname: Int, params: IntBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glGetMaterialfv(face: Int, pname: Int, params: FloatArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glGetMaterialfv(face: Int, pname: Int, params: FloatBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glGetMaterialxv(face: Int, pname: Int, params: IntArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glGetMaterialxv(face: Int, pname: Int, params: IntBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glGetTexEnviv(env: Int, pname: Int, params: IntArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glGetTexEnviv(env: Int, pname: Int, params: IntBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glGetTexEnvxv(env: Int, pname: Int, params: IntArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glGetTexEnvxv(env: Int, pname: Int, params: IntBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glGetTexParameterfv(target: Int, pname: Int, params: FloatArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glGetTexParameterfv(target: Int, pname: Int, params: FloatBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glGetTexParameteriv(target: Int, pname: Int, params: IntArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glGetTexParameteriv(target: Int, pname: Int, params: IntBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glGetTexParameterxv(target: Int, pname: Int, params: IntArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glGetTexParameterxv(target: Int, pname: Int, params: IntBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glIsBuffer(buffer: Int): Boolean {
        throw UnsupportedOperationException()
    }

    override fun glIsEnabled(cap: Int): Boolean {
        throw UnsupportedOperationException()
    }

    override fun glIsTexture(texture: Int): Boolean {
        throw UnsupportedOperationException()
    }

    override fun glPointParameterf(pname: Int, param: Float) {
        throw UnsupportedOperationException()
    }

    override fun glPointParameterfv(pname: Int, params: FloatArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glPointParameterfv(pname: Int, params: FloatBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glPointParameterx(pname: Int, param: Int) {
        throw UnsupportedOperationException()
    }

    override fun glPointParameterxv(pname: Int, params: IntArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glPointParameterxv(pname: Int, params: IntBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glPointSizePointerOES(type: Int, stride: Int, pointer: Buffer) {
        throw UnsupportedOperationException()
    }

    override fun glTexEnvi(target: Int, pname: Int, param: Int) {
        throw UnsupportedOperationException()
    }

    override fun glTexEnviv(target: Int, pname: Int, params: IntArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glTexEnviv(target: Int, pname: Int, params: IntBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glTexParameterfv(target: Int, pname: Int, params: FloatArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glTexParameterfv(target: Int, pname: Int, params: FloatBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glTexParameteri(target: Int, pname: Int, param: Int) {
        throw UnsupportedOperationException()
    }

    override fun glTexParameterxv(target: Int, pname: Int, params: IntArray, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glTexParameterxv(target: Int, pname: Int, params: IntBuffer) {
        throw UnsupportedOperationException()
    }

    override fun glColorPointer(size: Int, type: Int, stride: Int, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glDrawElements(mode: Int, count: Int, type: Int, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glGetPointerv(pname: Int, params: Array<Buffer>) {
        throw UnsupportedOperationException()
    }

    override fun glNormalPointer(type: Int, stride: Int, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glTexCoordPointer(size: Int, type: Int, stride: Int, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glVertexPointer(size: Int, type: Int, stride: Int, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glCurrentPaletteMatrixOES(matrixpaletteindex: Int) {
        throw UnsupportedOperationException()
    }

    override fun glLoadPaletteFromModelViewMatrixOES() {
        throw UnsupportedOperationException()
    }

    override fun glMatrixIndexPointerOES(size: Int, type: Int, stride: Int, pointer: Buffer) {
        throw UnsupportedOperationException()
    }

    override fun glMatrixIndexPointerOES(size: Int, type: Int, stride: Int, offset: Int) {
        throw UnsupportedOperationException()
    }

    override fun glWeightPointerOES(size: Int, type: Int, stride: Int, pointer: Buffer) {
        throw UnsupportedOperationException()
    }

    override fun glWeightPointerOES(size: Int, type: Int, stride: Int, offset: Int) {
        throw UnsupportedOperationException()
    }

    /**
     * Get the current matrix
     */
    fun getMatrix(m: FloatArray?, offset: Int) {
        mCurrent.getMatrix(m, offset)
    }

    private fun check() {
        val oesMode: Int = when (matrixMode) {
            GL10.GL_MODELVIEW -> GL11.GL_MODELVIEW_MATRIX_FLOAT_AS_INT_BITS_OES
            GL10.GL_PROJECTION -> GL11.GL_PROJECTION_MATRIX_FLOAT_AS_INT_BITS_OES
            GL10.GL_TEXTURE -> GL11.GL_TEXTURE_MATRIX_FLOAT_AS_INT_BITS_OES
            else -> throw IllegalArgumentException("Unknown matrix mode")
        }
        if (mByteBuffer == null) {
            mCheckA = FloatArray(16)
            mCheckB = FloatArray(16)
            mByteBuffer = ByteBuffer.allocateDirect(64)
            mByteBuffer!!.order(ByteOrder.nativeOrder())
            mFloatBuffer = mByteBuffer!!.asFloatBuffer()
        }
        mgl.glGetIntegerv(oesMode, mByteBuffer!!.asIntBuffer())
        for (i in 0..15) {
            mCheckB[i] = mFloatBuffer!![i]
        }
        mCurrent.getMatrix(mCheckA, 0)
        var fail = false
        for (i in 0..15) {
            if (mCheckA[i] != mCheckB[i]) {
                Log.d(
                    "GLMatWrap", "i:" + i + " a:" + mCheckA[i]
                        + " a:" + mCheckB[i]
                )
                fail = true
            }
        }
        require(!fail) { "Matrix math difference." }
    }

    companion object {
        /**
         * Debugging flag, if set to true causes a call to our method `check` to verify that our
         * current matrix is identical with the GPU current matrix. Otherwise `check` is not used.
         */
        private const val CHECK = false
    }

    /**
     * Our constructor. First we cast our parameter `GL gl` to `GL10 mgl`, if `gl`
     * is an instance of `GL10Ext` we cast it to `GL10Ext mgl10Ext`, if `gl` is an
     * instance of `G11` we cast it to `GL11 mgl11`, and if  if `gl` is an instance
     * of `G11Ext` we cast it to `GL11Ext mgl11Ext`. Next we allocate storage for our
     * three matrix stacks `MatrixStack mModelView`, `MatrixStack mProjection` and
     * `MatrixStack mTexture`. We set `MatrixStack mCurrent` to point to `mModelView`,
     * and set our matrix mode `int mMatrixMode` to GL_MODELVIEW.
     *
     * Parameter: gl the gl interface
     */
    init {
        if (gl is GL10Ext) {
            mgl10Ext = gl
        }
        if (gl is GL11) {
            mgl11 = gl
        }
        if (gl is GL11Ext) {
            mgl11Ext = gl
        }
        mModelView = MatrixStack()
        mProjection = MatrixStack()
        mTexture = MatrixStack()
        mCurrent = mModelView
        matrixMode = GL10.GL_MODELVIEW
    }
}