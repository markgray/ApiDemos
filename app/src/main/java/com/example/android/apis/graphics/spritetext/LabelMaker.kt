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

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.opengl.GLUtils
import androidx.core.graphics.createBitmap
import com.example.android.apis.graphics.spritetext.LabelMaker.Companion.STATE_ADDING
import com.example.android.apis.graphics.spritetext.LabelMaker.Companion.STATE_DRAWING
import com.example.android.apis.graphics.spritetext.LabelMaker.Companion.STATE_INITIALIZED
import com.example.android.apis.graphics.spritetext.LabelMaker.Companion.STATE_NEW
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11
import javax.microedition.khronos.opengles.GL11Ext
import kotlin.math.ceil

/**
 * An OpenGL text label maker.
 *
 * OpenGL labels are implemented by creating a [Bitmap], drawing all the labels
 * into the [Bitmap], converting the [Bitmap] into an Alpha texture, and drawing
 * portions of the texture using `glDrawTexiOES`.
 *
 * The benefits of this approach are that the labels are drawn using the high
 * quality anti-aliased font rasterizer, full character set support, and all the
 * text labels are stored on a single texture, which makes it faster to use.
 *
 * The drawbacks are that you can only have as many labels as will fit onto one
 * texture, and you have to recreate the whole texture if any label text changes.
 */
class LabelMaker(
    /**
     * *true* if we want a full color backing store (8888), otherwise we generate a grey L8
     * backing store. Set to the [Boolean] `fullColor` parameter of our constructor, always
     * *true* in our case.
     */
    private val mFullColor: Boolean,
    /**
     * Width of text, rounded up to power of 2, set to the `strikeWidth` parameter to our
     * constructor.
     */
    private val mStrikeWidth: Int,
    /**
     * Height of text, rounded up to power of 2, set to the `strikeHeight` parameter to our
     * constructor.
     */
    private val mStrikeHeight: Int
) {
    /**
     * [Bitmap] we create our labels in, and when done creating the labels we upload it as
     * GL_TEXTURE_2D for drawing the labels (and then recycle it).
     */
    private var mBitmap: Bitmap? = null

    /**
     * [Canvas] we use to draw into [Bitmap] field [mBitmap].
     */
    private var mCanvas: Canvas? = null

    /**
     * We create this as a black [Paint], alpha 0, with a style of FILL but never actually use it.
     */
    private val mClearPaint: Paint = Paint()

    /**
     * Texture name of our label texture.
     */
    private var mTextureID = 0

    /**
     * Convert texel to U
     */
    @Suppress("unused")
    private val mTexelWidth: Float = (1.0 / mStrikeWidth).toFloat()

    /**
     * Convert texel to V
     */
    @Suppress("unused")
    private val mTexelHeight: Float = (1.0 / mStrikeHeight).toFloat()

    /**
     * `u` (x) coordinate to use when adding next label to our texture.
     */
    private var mU = 0

    /**
     * `v` (y) coordinate to use when adding next label to our texture.
     */
    private var mV = 0

    /**
     * Height of the current line of labels.
     */
    private var mLineHeight = 0

    /**
     * List of the [Label] objects in our texture. A [Label] instance contains information
     * about the location and size of the label's text in the texture, as well as the cropping
     * parameters to use to draw only that [Label].
     */
    private val mLabels = ArrayList<Label>()

    /**
     * State that our [LabelMaker] instance is in, one of [STATE_NEW], [STATE_INITIALIZED],
     * [STATE_ADDING], or [STATE_DRAWING]. It is used by our method [checkState] to make sure
     * that a state change is "legal", and also by our method [shutdown] to make sure that our
     * texture is deleted when our surface has been destroyed (a texture will only have been
     * allocated if `mState>STATE_NEW`).
     */
    private var mState: Int

    /**
     * Called to initialize the class. Called whenever the surface has been created. First we set
     * our field [mState] to [STATE_INITIALIZED] (in this state we have generated a texture name,
     * bound that texture to GL_TEXTURE_2D and configured it to our liking, but no image data has
     * been uploaded yet). Next we generate a texture name and save it in our field [mTextureID]
     *
     * We bind the texture [mTextureID] to the target GL_TEXTURE_2D (GL_TEXTURE_2D becomes an
     * alias for our texture), and set both the texture parameters GL_TEXTURE_MIN_FILTER and
     * GL_TEXTURE_MAG_FILTER of GL_TEXTURE_2D to GL_NEAREST (uses the value of the texture element
     * that is nearest (in Manhattan distance) to the center of the pixel being textured when the
     * pixel being textured maps to an area greater than one texture element, as well as when the
     * the pixel being textured maps to an area less than or equal to one texture element). We set
     * the texture parameters GL_TEXTURE_WRAP_S and GL_TEXTURE_WRAP_T of GL_TEXTURE_2D both to
     * GL_CLAMP_TO_EDGE (when the fragment being textured is larger than the texture, the texture
     * elements at the edges will be used for the rest of the fragment).
     *
     * Finally we set the texture environment parameter GL_TEXTURE_ENV_MODE of the texture environment
     * GL_TEXTURE_ENV to GL_REPLACE (the texture will replace whatever was in the fragment).
     *
     * @param gl the [GL10] interface
     */
    fun initialize(gl: GL10) {
        mState = STATE_INITIALIZED
        val textures = IntArray(1)
        gl.glGenTextures(1, textures, 0)
        mTextureID = textures[0]
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID)
        // Use Nearest for performance.
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_MIN_FILTER,
            GL10.GL_NEAREST.toFloat()
        )
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_MAG_FILTER,
            GL10.GL_NEAREST.toFloat()
        )
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_WRAP_S,
            GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_WRAP_T,
            GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE.toFloat())
    }

    /**
     * Called when the surface we were labeling has been destroyed and a new surface is being created
     * so that the the label texture used by this instance of [LabelMaker] can be deleted. We need to
     * do this if we have already passed to a state where a texture name has been allocated by the
     * hardware (`mState>STATE_NEW`) we must delete our [mTextureID] texture  and move our state
     * field [mState] to the state [STATE_NEW] (ready to start building a new label texture).
     *
     * @param gl the [GL10] interface
     */
    fun shutdown(gl: GL10?) {
        if (gl != null) {
            if (mState > STATE_NEW) {
                val textures = IntArray(1)
                textures[0] = mTextureID
                gl.glDeleteTextures(1, textures, 0)
                mState = STATE_NEW
            }
        }
    }

    /**
     * Call before [add]'ing labels, and after calling [initialize]. Clears out any existing labels.
     * First we call [checkState] to make sure we are currently in [STATE_INITIALIZED], and if so
     * have it change to state [STATE_ADDING]. Next we clear our current list of [Label] objects
     * contained in our `ArrayList<LabelMaker.Label>` field [mLabels]. We reset the [Int] texture
     * coordinates for our next label [mU] and [mV], and set the [Int] height of the current line
     * [mLineHeight] to 0. We set [Bitmap.Config] variable `val config` to ARGB_8888 if our [Boolean]
     * field [mFullColor] is true, or to ALPHA_8 if it is false. Then we initialize our [Bitmap]
     * field [mBitmap] with a new instance of [Bitmap] using `config` which is [mStrikeWidth] pixels
     * by [mStrikeHeight] pixels. We initialize [Canvas] field [mCanvas] with a canvas which will
     * use [mBitmap] to draw to, then set the entire [mBitmap] to black.
     *
     * @param gl the [GL10] interface UNUSED
     */
    @Suppress("UNUSED_PARAMETER")
    fun beginAdding(gl: GL10?) {
        checkState(STATE_INITIALIZED, STATE_ADDING)
        mLabels.clear()
        mU = 0
        mV = 0
        mLineHeight = 0
        val config = if (mFullColor) Bitmap.Config.ARGB_8888 else Bitmap.Config.ALPHA_8
        mBitmap = createBitmap(width = mStrikeWidth, height = mStrikeHeight, config = config)
        mCanvas = Canvas(mBitmap!!)
        mBitmap!!.eraseColor(0)
    }

    /**
     * Call to add a label, convenience function to call the full argument `add` with a *null*
     * `Drawable` parameter `background` and `minWidth` and `minHeight` both equal to 0. We
     * simply supply a *null* for `Drawable` parameter `background` and pass the call on.
     *
     * @param gl        the [GL10] interface
     * @param text      the text of the label
     * @param textPaint the [Paint] of the label
     * @return the id of the label, used to measure and draw the label
     */
    fun add(gl: GL10?, text: String?, textPaint: Paint?): Int {
        return add(gl, null, text, textPaint)
    }

    /**
     * Call to add a label UNUSED
     *
     * @param gl         the [GL10] interface
     * @param background background [Drawable] to use
     * @param minWidth   minimum width of label
     * @param minHeight  minimum height of label
     * @return the id of the label, used to measure and draw the label
     */
    fun add(gl: GL10?, background: Drawable?, minWidth: Int, minHeight: Int): Int {
        return add(gl, background, null, null, minWidth, minHeight)
    }

    /**
     * Call to add a label. First we call our method [checkState] to make sure we are in the
     * state [STATE_ADDING] (our method [beginAdding] has been called). Next we determine if
     * we have a [background] to draw ([background]!= *null*) saving the result to [Boolean]
     * variable `val drawBackground`, and determine if we have text to draw ([text]!= null`
     * and [textPaint] != *null*) saving the result to [Boolean] variable `val drawText`.
     *
     * We allocate a new instance of [Rect] for `val padding` and if we have a background that needs
     * to be drawn we fetch the padding insets for [background] to `padding`, set the variable
     * `minWidthVar` to the max of `minWidthVar` and the minimum width of the drawable [background],
     * and set the variable `minHeightVar` to the max of `minHeightVar` and the minimum height of the
     * drawable [background].
     *
     * Next we set `var ascent`, `var descent` and `var measuredTextWidth` all to 0, and if we have
     * text that needs drawing we set `ascent` to the ceiling value of the highest ascent above the
     * baseline for the current typeface and text size of the [Paint] parameter [textPaint], set
     * `descent` to the ceiling value of the lowest descent below the baseline for the current
     * typeface and text size of the [Paint] parameter [textPaint], and set `measuredTextWidth` to
     * the ceiling value of the length of [text].
     *
     * We now perform a bunch of boring calculations to determine where on the [Canvas] field
     * [mCanvas] we are to draw our background and/or text, and if we have a background we draw
     * the background drawable at that position, and if we have text we draw the text at that
     * position.
     *
     * Having done so, we update our field [mU] to point to the next u (x) coordinate we can
     * use, [mV] to point to the next v (y) coordinate we can use, and [mLineHeight] to the
     * (possibly new) height of our current line.
     *
     * Finally we add a new instance of [Label] to `ArrayList<Label>` field [mLabels] with the
     * information that will be needed to locate, crop and draw the label we just created, and
     * return the index in [mLabels] of this [Label] object to the caller.
     *
     * @param gl         the [GL10] interface
     * @param background background [Drawable] to use
     * @param text       the text of the label
     * @param textPaint  the [Paint] of the label
     * @param minWidth   minimum width of label
     * @param minHeight  minimum height of label
     * @return index of the [Label] in `ArrayList<Label>` field [mLabels]. The [Label]
     * object will be used to locate, measure, crop and draw the label.
     */
    @Suppress("UNUSED_PARAMETER")
    @JvmOverloads
    fun add(
        gl: GL10?,
        background: Drawable?,
        text: String?,
        textPaint: Paint?,
        minWidth: Int = 0,
        minHeight: Int = 0
    ): Int {
        var minWidthVar = minWidth
        var minHeightVar = minHeight
        checkState(STATE_ADDING, STATE_ADDING)
        val drawBackground = background != null
        val drawText = text != null && textPaint != null
        val padding = Rect()
        if (drawBackground) {
            background.getPadding(padding)
            minWidthVar = minWidthVar.coerceAtLeast(background.minimumWidth)
            minHeightVar = minHeightVar.coerceAtLeast(background.minimumHeight)
        }
        var ascent = 0
        var descent = 0
        var measuredTextWidth = 0
        if (drawText) { // Paint.ascent is negative, so negate it.
            ascent = ceil(-textPaint.ascent().toDouble()).toInt()
            descent = ceil(textPaint.descent().toDouble()).toInt()
            measuredTextWidth = ceil(textPaint.measureText(text).toDouble()).toInt()
        }
        val textHeight = ascent + descent
        val textWidth = mStrikeWidth.coerceAtMost(measuredTextWidth)
        val padHeight = padding.top + padding.bottom
        val padWidth = padding.left + padding.right
        val height = minHeightVar.coerceAtLeast(textHeight + padHeight)
        var width = minWidthVar.coerceAtLeast(textWidth + padWidth)
        val effectiveTextHeight = height - padHeight
        val effectiveTextWidth = width - padWidth
        val centerOffsetHeight = (effectiveTextHeight - textHeight) / 2
        val centerOffsetWidth = (effectiveTextWidth - textWidth) / 2

        /**
         * Make changes to the local variables, only commit them to the member
         * variables after we've decided not to throw any exceptions.
         */
        var u = mU
        var v = mV
        var lineHeight = mLineHeight
        if (width > mStrikeWidth) {
            width = mStrikeWidth
        }
        /**
         * Is there room for this string on the current line?
         */
        if (u + width > mStrikeWidth) { // No room, go to the next line:
            u = 0
            v += lineHeight
            lineHeight = 0
        }
        lineHeight = lineHeight.coerceAtLeast(height)
        require(v + lineHeight <= mStrikeHeight) { "Out of texture space." }
        @Suppress("UNUSED_VARIABLE")
        val u2 = u + width
        val vBase = v + ascent

        @Suppress("UNUSED_VARIABLE")
        val v2 = v + height
        if (drawBackground) {
            background.setBounds(u, v, u + width, v + height)
            background.draw(mCanvas!!)
        }
        if (drawText) {
            mCanvas!!.drawText(
                text,
                u + padding.left + centerOffsetWidth.toFloat(),
                vBase + padding.top + centerOffsetHeight.toFloat(),
                textPaint
            )
        }
        /**
         * We know there's enough space, so update the member variables
         */
        mU = u + width
        mV = v
        mLineHeight = lineHeight
        mLabels.add(
            Label(
                width.toFloat(),
                height.toFloat(),
                ascent.toFloat(),
                u,
                v + height,
                width,
                -height
            )
        )
        return mLabels.size - 1
    }

    /**
     * Call to end adding labels. Must be called before drawing of the lables starts. First we call
     * our method [checkState] to verify that we are in the [STATE_ADDING] state, and if so have it
     * transition us back to the [STATE_INITIALIZED] state. Next we bind our texture name [mTextureID]
     * to the GL_TEXTURE_2D target, upload our [Bitmap] field [mBitmap] to the GPU, recycle [mBitmap]
     * and null both [mBitmap] and [mCanvas] so they can be garbage collected.
     *
     * @param gl the [GL10] interface
     */
    fun endAdding(gl: GL10) {
        checkState(STATE_ADDING, STATE_INITIALIZED)
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID)
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0)
        /**
         * Reclaim storage used by bitmap and canvas.
         */
        mBitmap!!.recycle()
        mBitmap = null
        mCanvas = null
    }

    /**
     * Get the width in pixels of a given label. Convenience getter for the `width` field of
     * the [Label] at index value [labelID] in our `ArrayList<Label>` field [mLabels].
     *
     * @param labelID index of label
     * @return the width in pixels
     */
    fun getWidth(labelID: Int): Float {
        return mLabels[labelID].width
    }

    /**
     * Get the height in pixels of a given label. Convenience getter for the `height` field of
     * the [Label] at index value [labelID] in our `ArrayList<Label>` field [mLabels].
     *
     * @param labelID index of label
     * @return the height in pixels
     */
    fun getHeight(labelID: Int): Float {
        return mLabels[labelID].height
    }

    /**
     * Get the baseline of a given label. That's how many pixels from the top of the label to the
     * text baseline. (This is equivalent to the negative of the label's paint's ascent.) UNUSED
     *
     * @param labelID index of label
     * @return the baseline in pixels.
     */
    @Suppress("unused")
    fun getBaseline(labelID: Int): Float {
        return mLabels[labelID].baseline
    }

    /**
     * Begin drawing labels. Sets the OpenGL state for rapid drawing. First we call our method
     * [checkState] to verify that we are in [STATE_INITIALIZED] state and if so to transition
     * to [STATE_DRAWING] state. Next we bind our texture name [mTextureID] to the target
     * GL_TEXTURE_2D, set the shade model to GL_FLAT and enable the server side capability GL_BLEND
     * (blend the computed fragment color values with the values in the color buffers). We call the
     * method `glBlendFunc` to set the source blending function to GL_SRC_ALPHA, and the
     * destination blending function to GL_ONE_MINUS_SRC_ALPHA (modifies the incoming color by its
     * associated alpha value and modifies the destination color by one minus the incoming alpha
     * value. The sum of these two colors is then written back into the framebuffer.) We then call
     * the method `glColor4x` to set the primitive’s opacity to 1.0 in GLfixed format.
     *
     * We set the current matrix to the projection matrix GL_PROJECTION, push the current projection
     * matrix to its stack, load GL_PROJECTION with the identity matrix, and multiply it with the
     * orthographic matrix that has the left clipping plane at 0, the right clipping plane at
     * [viewWidth], the bottom clipping plane at 0, the top clipping plane at [viewHeight], the
     * near clipping plane at 0.0, and the far clipping plane at 1.0
     *
     * We then set the current matrix to the model view matrix GL_MODELVIEW, push the current model
     * view matrix to its stack, load GL_MODELVIEW with the identity matrix, and multiply it by a
     * translation matrix which moves both x and y coordinates by 0.375 in order to promote consistent
     * rasterization.
     *
     * @param gl         the [GL10] interface
     * @param viewWidth  view width
     * @param viewHeight view height
     */
    fun beginDrawing(gl: GL10, viewWidth: Float, viewHeight: Float) {
        checkState(STATE_INITIALIZED, STATE_DRAWING)
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID)
        gl.glShadeModel(GL10.GL_FLAT)
        gl.glEnable(GL10.GL_BLEND)
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA)
        gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000)
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glPushMatrix()
        gl.glLoadIdentity()
        gl.glOrthof(0.0f, viewWidth, 0.0f, viewHeight, 0.0f, 1.0f)
        gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glPushMatrix()
        gl.glLoadIdentity()
        // Magic offsets to promote consistent rasterization.
        gl.glTranslatef(0.375f, 0.375f, 0.0f)
    }

    /**
     * Draw a given label at a given x,y position, expressed in pixels, with the lower-left-hand
     * corner of the view being (0,0). First we call our method [checkState] to make sure we
     * are in the [STATE_DRAWING] state. We fetch the [Label] object for the label we are to
     * draw to [Label] variable `val label`, enable the server side capability GL_TEXTURE_2D, and
     * set the cropping rectangle of GL_TEXTURE_2D to the contents of the `label.mCrop` field. Then
     * we call glDrawTexiOES to draw the cropped area of the texture at `(x,y,z)` using the
     * width and height specified by the `label.width` field, and the `label.height`
     * field.
     *
     * @param gl      the [GL10] interface
     * @param x       x coordinate to draw at
     * @param y       y coordinate to draw at
     * @param labelID index of `Label` in the list `ArrayList<Label> mLabels` to draw
     */
    fun draw(gl: GL10, x: Float, y: Float, labelID: Int) {
        checkState(STATE_DRAWING, STATE_DRAWING)
        val label = mLabels[labelID]
        gl.glEnable(GL10.GL_TEXTURE_2D)
        (gl as GL11).glTexParameteriv(
            GL10.GL_TEXTURE_2D,
            GL11Ext.GL_TEXTURE_CROP_RECT_OES,
            label.mCrop,
            0
        )
        (gl as GL11Ext).glDrawTexiOES(
            x.toInt(), y.toInt(), 0,
            label.width.toInt(), label.height.toInt()
        )
    }

    /**
     * Ends the drawing and restores the OpenGL state. First we call our method [checkState] to make
     * sure we are in the [STATE_DRAWING] state and if so to transition to the [STATE_INITIALIZED]
     * state. We disable the server side capability GL_BLEND, set the current matrix to the projection
     * matrix GL_PROJECTION and pop the old matrix off of its stake, and then set the current matrix
     * to the model view matrix GL_MODELVIEW and pop the old matrix off of its stack.
     *
     * @param gl the [GL10] interface
     */
    fun endDrawing(gl: GL10) {
        checkState(STATE_DRAWING, STATE_INITIALIZED)
        gl.glDisable(GL10.GL_BLEND)
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glPopMatrix()
        gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glPopMatrix()
    }

    /**
     * Throws an [IllegalArgumentException] if we are not currently in the state [oldState], and
     * if we are in that state transitions to [newState].
     *
     * @param oldState state we need to be in to use the method we are called from
     * @param newState state to transition to iff we were in `oldState`
     */
    private fun checkState(oldState: Int, newState: Int) {
        require(mState == oldState) { "Can't call this method now." }
        mState = newState
    }

    /**
     * Class that contains the information needed to crop and draw a label contained in the current
     * GL_TEXTURE_2D label texture.
     */
    private class Label(
        /**
         * width of the label in pixels
         */
        var width: Float,
        /**
         * height of the label in pixels
         */
        var height: Float,
        /**
         * Unused, but set to the ascent value of the font and font size of the paint used to draw
         */
        var baseline: Float, cropU: Int, cropV: Int, cropW: Int, cropH: Int
    ) {
        /**
         * Defines the location and size of the label in the texture, it is used to crop the texture
         * so that only this label is used for drawing.
         */
        var mCrop: IntArray

        /**
         * The init block of our constructor. We simply initialize our `mCrop` field to an `Int`
         * array containing the values of the constructor parameters:
         *
         *  * cropU    u coordinate of left side of label in texture
         *  * cropV    v coordinate of top of texture
         *  * cropW    width of label in texture
         *  * cropH    height of the crop region, the negative value in our case specifies that the
         *  region lies below the coordinate (u,v) in the texture image.
         */
        init {
            val crop = IntArray(4)
            crop[0] = cropU
            crop[1] = cropV
            crop[2] = cropW
            crop[3] = cropH
            mCrop = crop
        }
    }

    companion object {
        /**
         * Constant used to set our field [mState] to indicate that we are just starting the
         * creation of our [Label] texture and there are no resources that need to be freed if
         * our `GLSurface` is destroyed.
         */
        private const val STATE_NEW = 0

        /**
         * Constant used to set our field [mState] to indicate that our [initialize] method
         * has been called, and we are ready to begin adding labels. We have acquired a texture name
         * for our field [mTextureID], bound it to GL_TEXTURE_2D and configured it so there is
         * a texture which needs to be freed if our `GLSurface` is destroyed.
         */
        private const val STATE_INITIALIZED = 1

        /**
         * Constant used to set our field [mState] to indicate that our [beginAdding] method
         * has been called, and we are ready to add a label (or an additional label). [initialize]
         * was called before us, and we have allocated a [Bitmap] for our [Bitmap] field [mBitmap]
         * so there is some cleanup needed if our `GLSurface` is destroyed.
         */
        private const val STATE_ADDING = 2

        /**
         * Constant used to set our field [mState] to indicate that our [beginDrawing] method
         * has been called and we are in the process of drawing the various [Label] objects located
         * in our texture.
         */
        private const val STATE_DRAWING = 3
    }

    /**
     * The init block for our `LabelMaker` constructor. We just configure our `Paint` field
     * `mClearPaint` to be a black `Paint` with its alpha 0, with a style of FILL, and set our
     * state field `mState` to `STATE_NEW`.
     */
    init {
        mClearPaint.setARGB(0, 0, 0, 0)
        mClearPaint.style = Paint.Style.FILL
        mState = STATE_NEW
    }
}