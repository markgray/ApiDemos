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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;

/**
 * Custom {@code Dialog} to pick colors, used only by {@code FingerPaint}
 */
@SuppressWarnings("WeakerAccess")
public class ColorPickerDialog extends Dialog {

    /**
     * This interface defines the method {@code colorChanged} which we will call when the user has
     * chosen a new color
     */
    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    /**
     * Set in constructor of {@code ColorPickerDialog}, and called in {@code ColorPickerView} when
     * the user has selected a color. Do not confuse with {@code ColorPickerView.mListener} which
     * is an anonymous class which calls {@code ColorPickerDialog.mListener.colorChanged}
     */
    private OnColorChangedListener mListener;
    /**
     * The current color being used by {@code FingerPaint}, passed to us as a constructor argument.
     */
    private int mInitialColor;

    /**
     * Constructor for our {@code ColorPickerDialog} instance. First we call through to our super's
     * constructor, then we save our parameter {@code OnColorChangedListener listener} in our field
     * {@code OnColorChangedListener mListener}, and {@code int initialColor} in our field
     * {@code int mInitialColor}.
     *
     * @param context      {@code Context} to use for resources, in our case "this" when called from
     *                     {@code FingerPaint}'s {@code onOptionsItemSelected} override
     * @param listener     {@code OnColorChangedListener} whose {@code colorChanged} method we should
     *                     call when user has selected a color, in our case "this" when called from
     *                     {@code FingerPaint}'s {@code onOptionsItemSelected} override
     * @param initialColor initial color currently being used by {@code FingerPaint}
     */
    public ColorPickerDialog(Context context, OnColorChangedListener listener, int initialColor) {
        super(context);

        mListener = listener;
        mInitialColor = initialColor;
    }

    /**
     * Called when the {@code Dialog} is starting. First we call through to our super's implementation
     * of {@code onCreate}. Next we create an anonymous class {@code OnColorChangedListener l} which
     * will call the {@code OnColorChangedListener.colorChanged} method in {@code FingerPaint} when
     * its own {@code colorChanged} method is called, then {@code dismiss} the {@code ColorPickerDialog}.
     * It then sets the content view of our dialog to a new instance of {@code ColorPickerView} created
     * using the {@code Context} this dialog is running in, {@code OnColorChangedListener l}, and our
     * field {@code int mInitialColor}. Finally we set our title to "Pick a Color".
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnColorChangedListener l = new OnColorChangedListener() {
            public void colorChanged(int color) {
                mListener.colorChanged(color);
                dismiss();
            }
        };

        setContentView(new ColorPickerView(getContext(), l, mInitialColor));
        setTitle("Pick a Color");
    }

    /**
     * Custom View that draws a color wheel that the user can choose a color from.
     */
    private static class ColorPickerView extends View {
        /**
         * {@code Paint} with a {@code SweepGradient} shader used to draw a color spectrum circular
         * oval. Any user finger movement reported to our {@code onTouchEvent} callback that is found
         * to be located on that oval selects the color underneath the finger to be the new color for
         * {@code Paint mCenterPaint}.
         */
        private Paint mPaint;
        /**
         * Paint used to draw the "select color and dismiss" circle at the center of the color wheel.
         * It is set to the current color, either the one we are constructed with, or the one the user
         * has selected on the color wheel.
         */
        private Paint mCenterPaint;
        /**
         * Colors used to construct the {@code SweepGradient} that is used as the shader for
         * {@code Paint mPaint}, which is used to draw the color wheel.
         */
        private final int[] mColors;
        /**
         * The {@code OnColorChangedListener} we were constructed with, its {@code colorChanged} method
         * will be called once the user the has clicked "select color and dismiss" circle at the center
         * of the color wheel. Do not confuse with {@code ColorPickerDialog.mListener}.
         */
        private OnColorChangedListener mListener;

        /**
         * Constructor for our {@code ColorPickerView} instance. First we call through to our super's
         * constructor, then we save our parameter {@code OnColorChangedListener l} in our field
         * {@code OnColorChangedListener mListener}. We initialize our field {@code int[] mColors}
         * with an array of colors which will describe a color wheel spectrum when we create the
         * {@code SweepGradient} for {@code Shader s}. We allocate an instance of {@code Paint} with
         * the anti-alias flag set to initialize our field {@code Paint mPaint}, set its Shader to
         * {@code Shader s}, set the style to STROKE, and set the stroke width to 32.  We allocate
         * an instance of {@code Paint} with the anti-alias flag set to initialize our field
         * {@code Paint mCenterPaint}, set its color to our parameter {@code color}, and set the
         * stroke width to 5.
         *
         * @param c     {@code Context} to use for resources, in our case the results of a call in
         *              {@code onCreate} of {@code ColorPickerView} to the method {@code getContext}
         *              to fetch the Context the Dialog is running in.
         * @param l     The {@code OnColorChangedListener} whose {@code colorChanged} method we are
         *              to call when done.
         * @param color Color currently being used in {@code FingerPaint}
         */
        ColorPickerView(Context c, OnColorChangedListener l, int color) {
            super(c);
            mListener = l;
            mColors = new int[]{
                    0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF,
                    0xFF00FF00, 0xFFFFFF00, 0xFFFF0000
            };
            Shader s = new SweepGradient(0, 0, mColors, null);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setShader(s);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(32);

            mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCenterPaint.setColor(color);
            mCenterPaint.setStrokeWidth(5);
        }

        /**
         * Flag indicating whether the finger's last ACTION_DOWN {@code MotionEvent} was in the center
         * circle used to "select the current color" and is used to keep track of the finger movement
         * while waiting for an ACTION_UP also in the center circle to indicate the selection is
         * complete. This allows the user to move his finger out of the center circle if he changes
         * his mind and wishes to continue the search for a color.
         */
        private boolean mTrackingCenter;
        /**
         * Flag indicating that a halo should be drawn around the center circle, it is set to the
         * value {@code boolean inCenter} (a flag indicating the location is in the center circle)
         * when an ACTION_DOWN event occurs, and set to false if an ACTION_UP event occurs outside
         * the center circle (the finger has moved on)
         */
        private boolean mHighlightCenter;

        /**
         * x coordinate of the center of the center circle
         */
        private static final int CENTER_X = 100;
        /**
         * y coordinate of the center of the center circle
         */
        private static final int CENTER_Y = 100;
        /**
         * radius of the center circle
         */
        private static final int CENTER_RADIUS = 32;

        /**
         * constant pi value used for circle calculations when a color on the wheel is being selected
         */
        private static final float PI = 3.1415926f;

        /**
         * We implement this to do our drawing. First we calculate the radius {@code float r} of the
         * color spectrum circle, move the {@code Canvas canvas} to the center of our view as given
         * by (CENTER_X, CENTER_X), draw our color spectrum circle using {@code Paint mPaint}, and
         * draw the center "select and dismiss" circle using {@code Paint mCenterPaint}. Then if our
         * flag {@code boolean mTrackingCenter} is true (the last ACTION_DOWN event was in the center
         * circle), we first save the current color of {@code Paint mCenterPaint} in {@code int c},
         * then we set its style to STROKE, and if the {@code boolean mHighlightCenter} flag is true
         * (which it is after an ACTION_DOWN in the center circle, until an ACTION_MOVE outside the
         * circle) we set the alpha to the max 0xFF, otherwise we set it to 0x80 (this has the effect
         * of lightening the "halo" if the finger moves outside the center). We then draw the center
         * circle "halo" using {@code Paint mCenterPaint}. Finally we reset the style of {@code mCenterPaint}
         * to FILL, and the color to the saved color {@code c}.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @SuppressLint("DrawAllocation")
        @Override
        protected void onDraw(Canvas canvas) {
            float r = CENTER_X - mPaint.getStrokeWidth() * 0.5f;

            canvas.translate(CENTER_X, CENTER_X);

            canvas.drawOval(new RectF(-r, -r, r, r), mPaint);
            canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);

            if (mTrackingCenter) {
                int c = mCenterPaint.getColor();
                mCenterPaint.setStyle(Paint.Style.STROKE);

                if (mHighlightCenter) {
                    mCenterPaint.setAlpha(0xFF);
                } else {
                    mCenterPaint.setAlpha(0x80);
                }
                canvas.drawCircle(0, 0, CENTER_RADIUS + mCenterPaint.getStrokeWidth(), mCenterPaint);

                mCenterPaint.setStyle(Paint.Style.FILL);
                mCenterPaint.setColor(c);
            }
        }

        /**
         * Measure the view and its content to determine the measured width and the
         * measured height. This method is invoked by {@link #measure(int, int)} and
         * should be overridden by subclasses to provide accurate and efficient
         * measurement of their contents.
         * <p>
         * We simply call {@code setMeasuredDimension} with a width of twice {@code CENTER_X}, and
         * a height of twice {@code CENTER_Y}/
         *
         * @param widthMeasureSpec  horizontal space requirements as imposed by the parent.
         *                          The requirements are encoded with
         *                          {@link android.view.View.MeasureSpec}.
         * @param heightMeasureSpec vertical space requirements as imposed by the parent.
         *                          The requirements are encoded with
         *                          {@link android.view.View.MeasureSpec}.
         */
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(CENTER_X * 2, CENTER_Y * 2);
        }

        /**
         * We Implement this method to handle touch screen motion events. We first fetch {@code float x}
         * (the x coordinate) and {@code float y} (the y coordinate) from the {@code MotionEvent event}.
         * We set the flag {@code boolean inCenter} based on a geometric calculation to determine if
         * the event occurred inside the center "select and dismiss" circle. We then switch based on
         * the action of the {@code MotionEvent event}:
         * <ul>
         *     <li>
         *         ACTION_DOWN - we set our flag {@code boolean mTrackingCenter} to the value of
         *         {@code boolean inCenter}, and if the event occurred in the center circle we set
         *         {@code boolean mHighlightCenter} to true and invalidate our view so it will be
         *         redrawn (this time with a "halo") and we break. If it was not in the center circle
         *         we fall through to the code for the ACTION_MOVE case.
         *     </li>
         *     <li>
         *         ACTION_MOVE - if {@code boolean mTrackingCenter} is true (the last ACTION_DOWN occurred
         *         in the center circle) we check to see if {@code boolean mHighlightCenter} is not equal
         *         to {@code boolean inCenter} (the finger has moved in or out of the center circle) and if
         *         they are not we set {@code mHighlightCenter} to {@code inCenter} and invalidate the view
         *         so it will be redrawn with the new "halo" setting. If {@code mTrackingCenter} is false
         *         the last ACTION_DOWN was outside the center circle and the movement is intended to select
         *         a color from the color spectrum circle, so we determine the angle on the spectrum circle
         *         based on the x and y coordinate, convert that angle to a {@code float unit} between 0 and
         *         1, then set the color of {@code Paint mCenterPaint} to the color calculated by our method
         *         {@code interpColor} and invalidate our view causing it to be redrawn with the new color
         *         for the center circle.
         *     </li>
         *     <li>
         *         ACTION_UP - If our flag {@code boolean mTrackingCenter} is true (the last ACTION_DOWN
         *         occurred in the center circle) we check to see if the current event occurred still in
         *         the circle and if so we call our {@code OnColorChangedListener mListener} callback to
         *         select the current color and dismiss the dialog. If the finger is now outside the
         *         center circle (the user changed his mind), we set {@code boolean mTrackingCenter}
         *         to false (the center circle will be drawn without the "halo") and invalidate our view
         *         causing it to be redrawn.
         *     </li>
         * </ul>
         * Finally we return true to indicate that we have handled the {@code MotionEvent event}.
         *
         * @param event The motion event.
         * @return True if the event was handled, false otherwise.
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX() - CENTER_X;
            float y = event.getY() - CENTER_Y;
            boolean inCenter = java.lang.Math.hypot(x, y) <= CENTER_RADIUS;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTrackingCenter = inCenter;
                    if (inCenter) {
                        mHighlightCenter = true;
                        invalidate();
                        break;
                    }
                case MotionEvent.ACTION_MOVE:
                    if (mTrackingCenter) {
                        if (mHighlightCenter != inCenter) {
                            mHighlightCenter = inCenter;
                            invalidate();
                        }
                    } else {
                        float angle = (float) java.lang.Math.atan2(y, x);
                        // need to turn angle [-PI ... PI] into unit [0....1]
                        float unit = angle / (2 * PI);
                        if (unit < 0) {
                            unit += 1;
                        }
                        mCenterPaint.setColor(interpColor(mColors, unit));
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mTrackingCenter) {
                        if (inCenter) {
                            mListener.colorChanged(mCenterPaint.getColor());
                        }
                        mTrackingCenter = false;    // so we draw w/o halo
                        invalidate();
                    }
                    break;
            }
            return true;
        }

        /**
         * 
         *
         * @param x float value to round to a byte
         * @return rounded int version of {@code x}
         */
        private int floatToByte(float x) {
            //noinspection UnnecessaryLocalVariable
            int n = java.lang.Math.round(x);
            return n;
        }

        private int pinToByte(int n) {
            if (n < 0) {
                n = 0;
            } else if (n > 255) {
                n = 255;
            }
            return n;
        }

        private int ave(int s, int d, float p) {
            return s + java.lang.Math.round(p * (d - s));
        }

        private int interpColor(int colors[], float unit) {
            if (unit <= 0) {
                return colors[0];
            }
            if (unit >= 1) {
                return colors[colors.length - 1];
            }

            float p = unit * (colors.length - 1);
            int i = (int) p;
            p -= i;

            // now p is just the fractional part [0...1) and i is the index
            int c0 = colors[i];
            int c1 = colors[i + 1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0), Color.red(c1), p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0), Color.blue(c1), p);

            return Color.argb(a, r, g, b);
        }

        @SuppressWarnings("unused")
        private int rotateColor(int color, float rad) {
            float deg = rad * 180 / 3.1415927f;
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);

            ColorMatrix cm = new ColorMatrix();
            ColorMatrix tmp = new ColorMatrix();

            cm.setRGB2YUV();
            tmp.setRotate(0, deg);
            cm.postConcat(tmp);
            tmp.setYUV2RGB();
            cm.postConcat(tmp);

            final float[] a = cm.getArray();

            int ir = floatToByte(a[0] * r + a[1] * g + a[2] * b);
            int ig = floatToByte(a[5] * r + a[6] * g + a[7] * b);
            int ib = floatToByte(a[10] * r + a[11] * g + a[12] * b);

            return Color.argb(Color.alpha(color), pinToByte(ir),
                    pinToByte(ig), pinToByte(ib));
        }

    }
}
