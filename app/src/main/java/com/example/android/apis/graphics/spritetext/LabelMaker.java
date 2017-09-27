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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.opengl.GLUtils;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

/**
 * An OpenGL text label maker.
 * <p>
 * OpenGL labels are implemented by creating a Bitmap, drawing all the labels
 * into the Bitmap, converting the Bitmap into an Alpha texture, and drawing
 * portions of the texture using glDrawTexiOES.
 * <p>
 * The benefits of this approach are that the labels are drawn using the high
 * quality anti-aliased font rasterizer, full character set support, and all the
 * text labels are stored on a single texture, which makes it faster to use.
 * <p>
 * The drawbacks are that you can only have as many labels as will fit onto one
 * texture, and you have to recreate the whole texture if any label text
 * changes.
 */
@SuppressWarnings({"WeakerAccess", "FieldCanBeLocal"})
public class LabelMaker {

    /**
     * Width of text, rounded up to power of 2, set to the {@code strikeWidth} parameter to our
     * constructor.
     */
    private int mStrikeWidth;
    /**
     * Height of text, rounded up to power of 2, set to the {@code strikeHeight} parameter to our
     * constructor.
     */
    private int mStrikeHeight;
    /**
     * true if we want a full color backing store (4444), otherwise we generate a grey L8 backing
     * store. Set to the {@code boolean fullColor} parameter of our constructor, always true in our
     * case.
     */
    private boolean mFullColor;
    /**
     * {@code Bitmap} we create our labels in, and when done creating the labels we upload it as
     * GL_TEXTURE_2D for drawing the labels (and then recycle it).
     */
    private Bitmap mBitmap;
    /**
     * {@code Canvas} we use to draw into {@code Bitmap mBitmap}.
     */
    private Canvas mCanvas;
    /**
     * We create this as a black paint, with a style of FILL but never actually use it.
     */
    private Paint mClearPaint;

    /**
     * Texture name of our label texture.
     */
    private int mTextureID;

    @SuppressWarnings("unused")
    private float mTexelWidth;  // Convert texel to U
    @SuppressWarnings("unused")
    private float mTexelHeight; // Convert texel to V
    /**
     * {@code u} (x) coordinate to use when adding next label to our texture.
     */
    private int mU;
    /**
     * {@code v} (y) coordinate to use when adding next label to our texture.
     */
    private int mV;
    /**
     * Height of the current line of labels.
     */
    private int mLineHeight;
    /**
     * List of the {@code Label} objects in our texture. A {@code Label} instance contains information
     * about the location and size of the label's text in the texture, as well as the cropping
     * parameters to use to draw only that {@code Label}.
     */
    private ArrayList<Label> mLabels = new ArrayList<>();

    /**
     * Constant used to set our field {@code mState} to indicate that we are just starting the
     * creation of our {@code Label} texture and there are no resources that need to be freed if
     * our {@code GLSurface} is destroyed.
     */
    private static final int STATE_NEW = 0;
    /**
     * Constant used to set our field {@code mState} to indicate that our {@code initialize} method
     * has been called, and we are ready to begin adding labels. We have acquired a texture name
     * for our field {@code mTextureID}, bound it to GL_TEXTURE_2D and configured it so there is
     * a texture which needs to be freed if our {@code GLSurface} is destroyed.
     */
    private static final int STATE_INITIALIZED = 1;
    /**
     * Constant used to set our field {@code mState} to indicate that our {@code beginAdding} method
     * has been called, and we are ready to add a label (or an additional label). {@code initialize}
     * was called before us, and we have allocated a {@code Bitmap} for our field {@code Bitmap mBitmap}
     * so there is some needed if our {@code GLSurface} is destroyed.
     */
    private static final int STATE_ADDING = 2;
    /**
     * Constant used to set our field {@code mState} to indicate that our {@code beginDrawing} method
     * has been called and we are in the process of drawing the various {@code Label} objects located
     * in our texture.
     */
    private static final int STATE_DRAWING = 3;
    /**
     * State that our {@code LabelMaker} instance is in, one of the above constants. It is used by
     * our method {@code checkState} to make sure that a state change is "legal", and also by our
     * method {@code shutdown} to make sure that our texture is deleted when our surface has been
     * destroyed (a texture will only have been allocated if {@code mState>STATE_NEW}).
     */
    private int mState;

    /**
     * Create a label maker. For maximum compatibility with various OpenGL ES implementations, the
     * strike width and height must be powers of two, We want the strike width to be at least as
     * wide as the widest window. First we initialize our field {@code boolean mFullColor} to our
     * parameter {@code boolean fullColor}, {@code int mStrikeWidth} to {@code int strikeWidth}, and
     * {@code int mStrikeHeight} to {@code int strikeHeight}. We configure 3 fields which are never
     * used: {@code mTexelWidth}. {@code mTexelHeight}, and {@code mPaint}. Finally we set our field
     * {@code int mState} to STATE_NEW (in this state we do not yet have a texture that will need to
     * be freed if our surface is destroyed, but we are ready to begin building our label texture).
     *
     * @param fullColor    true if we want a full color backing store (4444),
     *                     otherwise we generate a grey L8 backing store.
     * @param strikeWidth  width of strike
     * @param strikeHeight height of strike
     */
    public LabelMaker(boolean fullColor, int strikeWidth, int strikeHeight) {
        mFullColor = fullColor;
        mStrikeWidth = strikeWidth;
        mStrikeHeight = strikeHeight;
        mTexelWidth = (float) (1.0 / mStrikeWidth); // UNUSED
        mTexelHeight = (float) (1.0 / mStrikeHeight); // UNUSED
        mClearPaint = new Paint();
        mClearPaint.setARGB(0, 0, 0, 0);
        mClearPaint.setStyle(Style.FILL);
        mState = STATE_NEW;
    }

    /**
     * Call to initialize the class. Call whenever the surface has been created. First we set our
     * field {@code int mState} to STATE_INITIALIZED (in this state we have generated a texture name,
     * bound that texture to GL_TEXTURE_2D and configured it to our liking, but no image data has
     * been uploaded yet). Next we generate a texture name and save it in our field {@code int mTextureID}.
     * <p>
     * We bind the texture {@code mTextureID} to the target GL_TEXTURE_2D (GL_TEXTURE_2D becomes an
     * alias for our texture), and set both the texture parameters GL_TEXTURE_MIN_FILTER and
     * GL_TEXTURE_MAG_FILTER of GL_TEXTURE_2D to GL_NEAREST (uses the value of the texture element
     * that is nearest (in Manhattan distance) to the center of the pixel being textured when the
     * pixel being textured maps to an area greater than one texture element, as well as when the
     * the pixel being textured maps to an area less than or equal to one texture element). We set
     * the texture parameters GL_TEXTURE_WRAP_S and GL_TEXTURE_WRAP_T of GL_TEXTURE_2D both to
     * GL_CLAMP_TO_EDGE (when the fragment being textured is larger than the texture, the texture
     * elements at the edges will be used for the rest of the fragment).
     * <p>
     * Finally we set the texture environment parameter GL_TEXTURE_ENV_MODE of the texture environment
     * GL_TEXTURE_ENV to GL_REPLACE (the texture will replace whatever was in the fragment).
     *
     * @param gl the gl interface
     */
    public void initialize(GL10 gl) {
        mState = STATE_INITIALIZED;

        int[] textures = new int[1];
        gl.glGenTextures(1, textures, 0);
        mTextureID = textures[0];

        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);

        // Use Nearest for performance.
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
    }

    /**
     * Called when the surface we were labeling has been destroyed and a new surface is being created
     * so that the the label texture used by this instance of {@code LabelMaker} can be deleted. To
     * do this if we have already passed to a state where a texture name has been allocated by the
     * hardware ({@code mState>STATE_NEW}) we must delete our texture {@code int mTextureID} and
     * move our state field {@code int mState} to the state STATE_NEW (ready to start building a new
     * label texture).
     *
     * @param gl the gl interface
     */
    public void shutdown(GL10 gl) {
        if (gl != null) {
            if (mState > STATE_NEW) {
                int[] textures = new int[1];
                textures[0] = mTextureID;
                gl.glDeleteTextures(1, textures, 0);
                mState = STATE_NEW;
            }
        }
    }

    /**
     * Call before adding labels, and after calling {@code initialize}. Clears out any existing labels.
     * First we call {@code checkState} to make sure we are currently in STATE_INITIALIZED, and if so
     * changing to state STATE_ADDING. Next we clear our current list of {@code Label} objects contained
     * in {@code ArrayList<LabelMaker.Label> mLabels}. We reset the texture coordinates for our next
     * label {@code int mU} and {@code int mV}, and set the height of the current line {@code int mLineHeight}
     * to 0. We set {@code Bitmap.Config config} to ARGB_4444 if our field {@code boolean mFullColor}
     * is true, or to ALPHA_8 if it is false. Then we initialize our field {@code Bitmap mBitmap} with
     * a new instance of {@code Bitmap} using {@code config} which is {@code mStrikeWidth} pixels by
     * {@code mStrikeHeight} pixels. We initialize {@code Canvas mCanvas} with a canvas which will use
     * {@code mBitmap} to draw to, then set the entire {@code mBitmap} to black.
     *
     * @param gl the gl interface UNUSED
     */
    @SuppressWarnings("UnusedParameters")
    public void beginAdding(GL10 gl) {
        checkState(STATE_INITIALIZED, STATE_ADDING);
        mLabels.clear();
        mU = 0;
        mV = 0;
        mLineHeight = 0;
        Bitmap.Config config = mFullColor ?
                Bitmap.Config.ARGB_4444 : Bitmap.Config.ALPHA_8;
        mBitmap = Bitmap.createBitmap(mStrikeWidth, mStrikeHeight, config);
        mCanvas = new Canvas(mBitmap);
        mBitmap.eraseColor(0);
    }

    /**
     * Call to add a label, convenience function to call the full argument {@code add} with a null
     * {@code Drawable background} and {@code winWidth} and {@code minHeight} both equal to 0. We
     * simply supply a null for {@code Drawable background} and pass the call on.
     *
     * @param gl        the gl interface
     * @param text      the text of the label
     * @param textPaint the paint of the label
     * @return the id of the label, used to measure and draw the label
     */
    public int add(GL10 gl, String text, Paint textPaint) {
        return add(gl, null, text, textPaint);
    }

    /**
     * Call to add a label, convenience function to call the full argument {@code add} with
     * {@code winWidth} and {@code minHeight} both equal to 0. We simply supply 0 for both
     * {@code int minWidth} and {@code int minHeight} and pass the call on.
     *
     * @param gl         the gl interface
     * @param background background {@code Drawable} to use
     * @param text       the text of the label
     * @param textPaint  the paint of the label
     * @return the id of the label, used to measure and draw the label
     */
    public int add(GL10 gl, Drawable background, String text, Paint textPaint) {
        return add(gl, background, text, textPaint, 0, 0);
    }

    /**
     * Call to add a label UNUSED
     *
     * @param gl         the gl interface
     * @param background background {@code Drawable} to use
     * @param minWidth   minimum width of label
     * @param minHeight  minimum height of label
     * @return the id of the label, used to measure and draw the label
     */
    public int add(GL10 gl, Drawable background, int minWidth, int minHeight) {
        return add(gl, background, null, null, minWidth, minHeight);
    }

    /**
     * Call to add a label. First we call our method {@code checkState} to make sure we are in the
     * state STATE_ADDING (our method {@code beginAdding} has been called). Next we determine if
     * we have a {@code background} to draw {@code background != null}) saving the result to
     * {@code boolean drawBackground}, and determine if we have text to draw ({@code text != null}
     * and {@code textPaint != null}) saving the result to {@code boolean drawText}.
     * <p>
     * We allocate a new instance for {@code Rect padding} and if we have a background that needs to
     * be drawn we fetch the padding insets for {@code background} to {@code padding}, set the input
     * parameter {@code minWidth} to the max of {@code minWidth} and the minimum width of the drawable
     * {@code background}, and set the input parameter {@code minHeight} to the max of {@code minHeight}
     * and the minimum height of the drawable {@code background}.
     * <p>
     * Next we set {@code int ascent}, {@code int descent} and {@code int measuredTextWidth} all to
     * 0, and if we have text that needs drawing we set {@code int ascent} to the ceiling value of
     * the highest ascent above the baseline for the current typeface and text size of the parameter
     * {@code Paint textPaint}, set {@code int descent} to the ceiling value of the lowest descent
     * below the baseline for the current typeface and text size of the parameter {@code Paint textPaint},
     * and set {@code {@code measuredTextWidth}} to the ceiling value of the length of {@code text}.
     * <p>
     * We now perform a bunch of boring calculations to determine where on the {@code Canvas mCanvas}
     * we are to draw our background and/or text, and if we have a background we draw the background
     * drawable at that position, and if we have text we draw the text at that position.
     * <p>
     * Having done so, we update our field {@code mU} to point to the next u (x) coordinate we can
     * use, {@code mV} to point to the next v (y) coordinate we can use, and {@code mLineHeight} to
     * the (possibly new) height of our current line.
     * <p>
     * Finally we add a new instance of {@code Label} to {@code ArrayList<Label> mLabels} with the
     * information that will be needed to locate, crop and draw the label we just drew, and return
     * the index of this {@code Label} object to the caller.
     *
     * @param gl         the gl interface UNUSED
     * @param background background {@code Drawable} to use
     * @param text       the text of the label
     * @param textPaint  the paint of the label
     * @param minWidth   minimum width of label
     * @param minHeight  minimum height of label
     * @return index of the {@code Label} in {@code ArrayList<Label> mLabels}, the {@code Label}
     * object will be used to locate, measure, crop and draw the label.
     */
    @SuppressWarnings("UnusedParameters")
    public int add(GL10 gl, Drawable background, String text, Paint textPaint, int minWidth, int minHeight) {
        checkState(STATE_ADDING, STATE_ADDING);
        boolean drawBackground = background != null;
        boolean drawText = (text != null) && (textPaint != null);

        Rect padding = new Rect();
        if (drawBackground) {
            background.getPadding(padding);
            minWidth = Math.max(minWidth, background.getMinimumWidth());
            minHeight = Math.max(minHeight, background.getMinimumHeight());
        }

        int ascent = 0;
        int descent = 0;
        int measuredTextWidth = 0;
        if (drawText) {
            // Paint.ascent is negative, so negate it.
            ascent = (int) Math.ceil(-textPaint.ascent());
            descent = (int) Math.ceil(textPaint.descent());
            measuredTextWidth = (int) Math.ceil(textPaint.measureText(text));
        }
        int textHeight = ascent + descent;
        int textWidth = Math.min(mStrikeWidth, measuredTextWidth);

        int padHeight = padding.top + padding.bottom;
        int padWidth = padding.left + padding.right;
        int height = Math.max(minHeight, textHeight + padHeight);
        int width = Math.max(minWidth, textWidth + padWidth);
        int effectiveTextHeight = height - padHeight;
        int effectiveTextWidth = width - padWidth;

        int centerOffsetHeight = (effectiveTextHeight - textHeight) / 2;
        int centerOffsetWidth = (effectiveTextWidth - textWidth) / 2;

        // Make changes to the local variables, only commit them
        // to the member variables after we've decided not to throw
        // any exceptions.

        int u = mU;
        int v = mV;
        int lineHeight = mLineHeight;

        if (width > mStrikeWidth) {
            width = mStrikeWidth;
        }

        // Is there room for this string on the current line?
        if (u + width > mStrikeWidth) {
            // No room, go to the next line:
            u = 0;
            v += lineHeight;
            lineHeight = 0;
        }
        lineHeight = Math.max(lineHeight, height);
        if (v + lineHeight > mStrikeHeight) {
            throw new IllegalArgumentException("Out of texture space.");
        }

        @SuppressWarnings("unused")
        int u2 = u + width;
        int vBase = v + ascent;
        @SuppressWarnings("unused")
        int v2 = v + height;

        if (drawBackground) {
            background.setBounds(u, v, u + width, v + height);
            background.draw(mCanvas);
        }

        if (drawText) {
            mCanvas.drawText(text,
                    u + padding.left + centerOffsetWidth,
                    vBase + padding.top + centerOffsetHeight,
                    textPaint);
        }

        // We know there's enough space, so update the member variables
        mU = u + width;
        mV = v;
        mLineHeight = lineHeight;
        mLabels.add(new Label(width, height, ascent, u, v + height, width, -height));
        return mLabels.size() - 1;
    }

    /**
     * Call to end adding labels. Must be called before drawing starts. First we call our method
     * {@code checkState} to verify that we are in the STATE_ADDING state, and if so transition back
     * to the STATE_INITIALIZED state. Next we bind our texture name {@code mTextureID} to the
     * GL_TEXTURE_2D target, upload our {@code Bitmap mBitmap} to the GPU, recycle our {@code mBitmap}
     * and null our both {@code mBitmap} and {@code mCanvas} so they can be garbage collected.
     *
     * @param gl the gl interface
     */
    public void endAdding(GL10 gl) {
        checkState(STATE_ADDING, STATE_INITIALIZED);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0);
        // Reclaim storage used by bitmap and canvas.
        mBitmap.recycle();
        mBitmap = null;
        mCanvas = null;
    }

    /**
     * Get the width in pixels of a given label. Convenience getter for the {@code width} field of
     * the {@code Label} at index value {@code labelID} in our {@code ArrayList<Label> mLabels}.
     *
     * @param labelID index of label
     * @return the width in pixels
     */
    public float getWidth(int labelID) {
        return mLabels.get(labelID).width;
    }

    /**
     * Get the height in pixels of a given label. Convenience getter for the {@code height} field of
     * the {@code Label} at index value {@code labelID} in our {@code ArrayList<Label> mLabels}.
     *
     * @param labelID index of label
     * @return the height in pixels
     */
    public float getHeight(int labelID) {
        return mLabels.get(labelID).height;
    }

    /**
     * Get the baseline of a given label. That's how many pixels from the top of the label to the
     * text baseline. (This is equivalent to the negative of the label's paint's ascent.) UNUSED
     *
     * @param labelID index of label
     * @return the baseline in pixels.
     */
    @SuppressWarnings("unused")
    public float getBaseline(int labelID) {
        return mLabels.get(labelID).baseline;
    }

    /**
     * Begin drawing labels. Sets the OpenGL state for rapid drawing. First we call our method
     * {@code checkState} to verify that we are in STATE_INITIALIZED state and if so to transition
     * to STATE_DRAWING state. Next we bind our texture name {@code mTextureID} to the target
     * GL_TEXTURE_2D, set the shade model to GL_FLAT and enable the server side capability GL_BLEND
     * (blend the computed fragment color values with the values in the color buffers). We call the
     * method {@code glBlendFunc} to set the source blending function to GL_SRC_ALPHA, and the
     * destination blending function to GL_ONE_MINUS_SRC_ALPHA (modifies the incoming color by its
     * associated alpha value and modifies the destination color by one minus the incoming alpha
     * value. The sum of these two colors is then written back into the framebuffer.) We then call
     * the method {@code glColor4x} to set the primitive’s opacity to 1.0 in GLfixed format.
     * <p>
     * We set the current matrix to the projection matrix GL_PROJECTION, push the current projection
     * matrix to its stack, load GL_PROJECTION with the identity matrix, and multiply it with the
     * orthographic matrix that has the left clipping plane at 0, the right clipping plane at
     * {@code viewWidth}, the bottom clipping plane at 0, the top clipping plane at {@code viewHeight},
     * the near clipping plane at 0.0, and the far clipping plane at 1.0
     * <p>
     * We then set the current matrix to the model view matrix GL_MODELVIEW, push the current model
     * view matrix to its stack, load GL_MODELVIEW with the identity matrix, and multiply it by a
     * translation matrix which moves both x and y coordinates by 0.375 in order to promote consistent
     * rasterization.
     *
     * @param gl         the gl interface
     * @param viewWidth  view width
     * @param viewHeight view height
     */
    public void beginDrawing(GL10 gl, float viewWidth, float viewHeight) {
        checkState(STATE_INITIALIZED, STATE_DRAWING);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
        gl.glShadeModel(GL10.GL_FLAT);

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrthof(0.0f, viewWidth, 0.0f, viewHeight, 0.0f, 1.0f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        // Magic offsets to promote consistent rasterization.
        gl.glTranslatef(0.375f, 0.375f, 0.0f);
    }

    /**
     * Draw a given label at a given x,y position, expressed in pixels, with the lower-left-hand
     * corner of the view being (0,0). First we call our method {@code checkState} to make sure we
     * are in the STATE_DRAWING state. We fetch the {@code Label} object for the label we are to
     * draw to {@code Label label}, enable the server side capability GL_TEXTURE_2D, and set the
     * cropping rectangle of GL_TEXTURE_2D to the contents of the {@code label.mCrop} field. Then
     * we call glDrawTexiOES to draw the cropped area of the texture at {@code (x,y,z)} using the
     * width and height specified by the {@code label.width} field, and the {@code label.height}
     * field.
     *
     * @param gl      the gl interface
     * @param x       x coordinate to draw at
     * @param y       y coordinate to draw at
     * @param labelID index of {@code Label} in the list {@code ArrayList<Label> mLabels} to draw
     */
    public void draw(GL10 gl, float x, float y, int labelID) {
        checkState(STATE_DRAWING, STATE_DRAWING);
        Label label = mLabels.get(labelID);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        ((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES, label.mCrop, 0);
        ((GL11Ext) gl).glDrawTexiOES((int) x, (int) y, 0, (int) label.width, (int) label.height);
    }

    /**
     * Ends the drawing and restores the OpenGL state. First we call our method {@code checkState} to
     * make sure we are in the STATE_DRAWING state and if so to transition to the STATE_INITIALIZED
     * state. We disable the server side capability GL_BLEND, set the current matrix to the projection
     * matrix GL_PROJECTION and pop the old matrix off of its stake, and then set the current matrix
     * to the model view matrix GL_MODELVIEW and pop the old matrix off of its stake.
     *
     * @param gl the gl interface
     */
    public void endDrawing(GL10 gl) {
        checkState(STATE_DRAWING, STATE_INITIALIZED);
        gl.glDisable(GL10.GL_BLEND);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glPopMatrix();
    }

    /**
     * Throws an IllegalArgumentException if we are not currently in the state {@code oldState}, and
     * if we are in that state transitions to {@code newState}.
     *
     * @param oldState state we need to be in to use the method we are called from
     * @param newState state to transition to iff we were in {@code oldState}
     */
    private void checkState(int oldState, int newState) {
        if (mState != oldState) {
            throw new IllegalArgumentException("Can't call this method now.");
        }
        mState = newState;
    }

    /**
     * Class that contains the information needed to crop and draw a label contained in the current
     * GL_TEXTURE_2D label texture.
     */
    private static class Label {

        /**
         * width of the label in pixels
         */
        public float width;
        /**
         * height of the label in pixels
         */
        public float height;
        /**
         * Unused, but set to the ascent value of the font and font size of the paint used to draw
         */
        public float baseline;
        /**
         * Defines the location and size of the label in the texture, it is used to crop the texture
         * so that only this label is used for drawing.
         */
        public int[] mCrop;

        /**
         * Our constructor. We simply initialize our fields using our input parameters.
         *
         * @param width    width of our label
         * @param height   height of our label
         * @param baseLine baseline of our label
         * @param cropU    u coordinate of left side of label in texture
         * @param cropV    v coordinate of top of texture
         * @param cropW    width of label in texture
         * @param cropH    height of the crop region, the negative value in our case specifies that the
         *                 region lies below the coordinate (u,v) in the texture image.
         */
        public Label(float width, float height, float baseLine, int cropU, int cropV, int cropW, int cropH) {
            this.width = width;
            this.height = height;
            this.baseline = baseLine;
            int[] crop = new int[4];
            crop[0] = cropU;
            crop[1] = cropV;
            crop[2] = cropW;
            crop[3] = cropH;
            mCrop = crop;
        }
    }
}
