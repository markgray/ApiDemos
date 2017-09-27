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

package com.example.android.apis.graphics.spritetext;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Paint;

/**
 * Label class for the ten numeric labels 0, 1, 2, 3, 4, 5, 6, 7, 8, and 9.
 */
@SuppressWarnings("WeakerAccess")
public class NumericSprite {

    /**
     * Our {@code LabelMaker}
     */
    private LabelMaker mLabelMaker;
    /**
     * Numeric text we are to draw, initially the empty string it is set to the number of milliseconds
     * per frame using our method {@code setValue} by the {@code drawMsPF} method of {@code SpriteTextRenderer}
     * which is called every time its {@code onDrawFrame} method is called
     */
    private String mText;
    /**
     * Width of each of our ten numeric labels
     */
    private int[] mWidth = new int[10];
    /**
     * Label index for each of our ten numeric labels.
     */
    private int[] mLabelId = new int[10];
    /**
     * String of digits used to initialize our ten numeric labels with the character they should draw.
     */
    private final static String sStrike = "0123456789";

    /**
     * Our constructor, we simply initialize our field {@code String mText} to the empty string, and
     * set our field {@code LabelMaker mLabelMaker} to null.
     */
    public NumericSprite() {
        mText = "";
        mLabelMaker = null;
    }

    /**
     * Called from the {@code onSurfaceCreated} method of {@code SpriteTextRenderer} to initialize
     * this instance of {@code NumericSprite}. First we initialize {@code int height} to be the first
     * power of 2 number greater than the recommended line spacing of {@code Paint paint}. We set
     * {@code float interDigitGaps} to 9 pixels (the spacing between digits) and then initialize
     * {@code int width} to be the first power of 2 number greater than the sum of {@code interDigitGaps}
     * and the length of our string {@code String sStrike} in pixels when {@code paint} is used to
     * render it. We set our field {@code LabelMaker mLabelMaker} to a new instance of {@code LabelMaker}
     * specifying the use of full color, a width of {@code width} and a height of {@code height}. We
     * instruct {@code mLabelMaker} to initialize itself (transition to the STATE_INITIALIZED state),
     * then to transition to the state STATE_ADDING.
     * <p>
     * Then for each of the 10 digits in our {@code String sStrike} we extract a single digit at a
     * time and instruct {@code LabelMaker mLabelMaker} to add it to its labels saving the index
     * number of the label in our field {@code int[] mLabelId}, and we save the width of the label
     * created by {@code mLabelMaker} in our field {@code int[] mWidth}. When done adding the digits
     * to {@code mLabelMaker} we instruct it to transition from STATE_ADDING to STATE_INITIALIZED.
     *
     * @param gl    the gl interface
     * @param paint {@code Paint} to use, it comes from the field {@code Paint mLabelPaint} which is
     *              created and configured in the constructor of {@code Paint mLabelPaint}.
     */
    public void initialize(GL10 gl, Paint paint) {
        int height = roundUpPower2((int) paint.getFontSpacing());
        final float interDigitGaps = 9 * 1.0f;
        int width = roundUpPower2((int) (interDigitGaps + paint.measureText(sStrike)));
        mLabelMaker = new LabelMaker(true, width, height);
        mLabelMaker.initialize(gl);
        mLabelMaker.beginAdding(gl);
        for (int i = 0; i < 10; i++) {
            String digit = sStrike.substring(i, i + 1);
            mLabelId[i] = mLabelMaker.add(gl, digit, paint);
            mWidth[i] = (int) Math.ceil(mLabelMaker.getWidth(i));
        }
        mLabelMaker.endAdding(gl);
    }

    /**
     * Called from the {@code onSurfaceCreated} method of {@code SpriteTextRenderer} when an instance
     * of us already exists for a previous creation of the surface, in order to force us to create a
     * new instance for the new surface. We simply call the {@code shutdown} method of {@code mLabelMaker}
     * and set it to null.
     *
     * @param gl the gl interface
     */
    public void shutdown(GL10 gl) {
        mLabelMaker.shutdown(gl);
        mLabelMaker = null;
    }

    /**
     * Find the smallest power of two >= the input value. (Doesn't work for negative numbers.) The
     * shifting and or'ing result in a value with all bits to the right of the most significant 1 bit
     * are also 1's and when 1 is added to that you get a power of 2 value. The initial subtract is
     * done in case the number was already that value.
     *
     * @param x number to round up
     * @return our input parameter rounded up to the smallest power of two above it
     */
    private int roundUpPower2(int x) {
        x = x - 1;
        x = x | (x >> 1);
        x = x | (x >> 2);
        x = x | (x >> 4);
        x = x | (x >> 8);
        x = x | (x >> 16);
        return x + 1;
    }

    /**
     * Formats its parameter {@code int value} as text and saves it in our field {@code String mText}.
     * {@code mText} is then drawn using the individual digit labels when our {@code draw} method is
     * called.
     *
     * @param value int value we want to print to our surface
     */
    public void setValue(int value) {
        mText = format(value);
    }

    /**
     * Called to draw the contents of our field {@code String mText} to the {@code SurfaceView} using
     * the individual digit labels we have created in our {@code LabelMaker mLabelMaker}. First we
     * set {@code int length} to the number of characters in {@code mText} and instruct our instance
     * of {@code LabelMaker mLabelMaker} to begin drawing (transition from the state STATE_INITIALIZED
     * to the state STATE_DRAWING). Then for each of the characters in {@code mText} we calculate the
     * index of the label for that character and instruct {@code mLabelMaker} to draw that label at
     * the location (x,y). We then advance {@code x} by the width of the label just drawn and move to
     * the next character.
     * <p>
     * When done drawing we instruct {@code mLabelMaker} to end the drawing (transition from the state
     * STATE_DRAWING to the state STATE_INITIALIZED).
     *
     * @param gl         the gl interface
     * @param x          x coordinate to start drawing at
     * @param y          y coordinate to start drawing at
     * @param viewWidth  width of the {@code SurfaceView}
     * @param viewHeight height of the {@code SurfaceView}
     */
    public void draw(GL10 gl, float x, float y, float viewWidth, float viewHeight) {
        int length = mText.length();
        mLabelMaker.beginDrawing(gl, viewWidth, viewHeight);
        for (int i = 0; i < length; i++) {
            char c = mText.charAt(i);
            int digit = c - '0';
            mLabelMaker.draw(gl, x, y, mLabelId[digit]);
            x += mWidth[digit];
        }
        mLabelMaker.endDrawing(gl);
    }

    /**
     * Adds up the width of each of the labels needed to print the contents of {@code String mText}
     * and returns the result to the caller. Called from the {@code drawMsPF} method of the class
     * {@code SpriteTextRenderer} in order to calculate the correct starting x location before calling
     * our method {@code draw}.
     *
     * @return width in pixels of the labels needed to render the contents of {@code String mText}
     */
    public float width() {
        float width = 0.0f;
        int length = mText.length();
        for (int i = 0; i < length; i++) {
            char c = mText.charAt(i);
            width += mWidth[c - '0'];
        }
        return width;
    }

    /**
     * Returns its parameter's {@code String} representation.
     *
     * @param value int value to format into text string
     * @return {@code String} object representing our parameter {@code int value}
     */
    private String format(int value) {
        return Integer.toString(value);
    }
}
