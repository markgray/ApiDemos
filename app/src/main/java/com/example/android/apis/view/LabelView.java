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

package com.example.android.apis.view;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.android.apis.R;


/**
 * Example of how to write a custom subclass of View. LabelView
 * is used to draw simple text views. Note that it does not handle
 * styled text or right-to-left writing systems.
 */
public class LabelView extends View {
    /**
     * {@code Paint} we use to draw our text with
     */
    private Paint mTextPaint;
    /**
     * Text we draw on our label
     */
    private String mText;
    /**
     * The maximum distance above the baseline based on the current typeface and text size of
     * {@code Paint mTextPaint} (a negative number).
     */
    private int mAscent;

    /**
     * Constructor. This version is only needed if you will be instantiating the object manually
     * (not from a layout XML file). We call our super's constructor, then we call our method
     * {@code initLabelView}.
     *
     * @param context context the view is running in
     */
    public LabelView(Context context) {
        super(context);
        initLabelView();
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called when a view is
     * being constructed from an XML file, supplying attributes that were specified in the XML file.
     * Our attributes are defined in values/attrs.xml. First we call our super's constructor, then
     * we call our method {@code initLabelView}.
     * <p>
     * Next we retrieve styled attribute information in this Context's theme to initialize
     * {@code TypedArray a}, specifying R.styleable.LabelView as the attributes to retrieve.
     * R.styleable.LabelView is defined in a declare-styleable element and declares the following
     * attributes:
     * <ul>
     * <li>
     * name="text" format="string" R.styleable.LabelView_text
     * </li>
     * <li>
     * name="textColor" format="color" R.styleable.LabelView_textColor
     * </li>
     * <li>
     * name="textSize" format="dimension R.styleable.LabelView_textSize
     * </li>
     * </ul>
     * Having obtained our custom attributes in {@code a}, we proceed to extract them:
     * <ul>
     * <li>
     * We set {@code CharSequence s} to the string at index R.styleable.LabelView_text in
     * {@code a}, and if it is not null we call our method {@code setText} to set our text
     * to {@code s}
     * </li>
     * <li>
     * We call our method {@code setTextColor} to set the color of our text to the color at
     * index R.styleable.LabelView_textColor in {@code a}, defaulting to black if the xml
     * did not specify it.
     * </li>
     * <li>
     * We set {@code int textSize} to the pixel version of the dimension in {@code a} at index
     * R.styleable.LabelView_textSize, defaulting to 0 if the xml did not specify it. If
     * {@code textSize} is greater than 0, we call our method {@code setTextSize} to set the
     * size of our text.
     * </li>
     * </ul>
     * Finally we recycle {@code a}.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public LabelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLabelView();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LabelView);

        CharSequence s = a.getString(R.styleable.LabelView_text);
        if (s != null) {
            setText(s.toString());
        }

        // Retrieve the color(s) to be used for this view and apply them.
        // Note, if you only care about supporting a single color, that you
        // can instead call a.getColor() and pass that to setTextColor().
        setTextColor(a.getColor(R.styleable.LabelView_textColor, 0xFF000000));

        int textSize = a.getDimensionPixelOffset(R.styleable.LabelView_textSize, 0);
        if (textSize > 0) {
            setTextSize(textSize);
        }

        a.recycle();
    }

    /**
     * Called to initialize this instance of {@code LabelView}. We allocate a new {@code Paint}
     * instance for {@code Paint mTextPaint}, set its antialias flag, set its text size to 16 times
     * the logical density of our display, and set its color to black. Finally We set the padding of
     * our view to 3 pixels on all four sides.
     */
    private void initLabelView() {
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        // Must manually scale the desired text size to match screen density
        mTextPaint.setTextSize(16 * getResources().getDisplayMetrics().density);
        mTextPaint.setColor(0xFF000000);
        setPadding(3, 3, 3, 3);
    }

    /**
     * Sets the text to display in this label. We save our parameter {@code String text} in our field
     * {@code String mText}, call {@code requestLayout} to schedule a layout pass of the view tree,
     * and call {@code invalidate} to invalidate the whole view so that {@code onDraw} will be called
     * at some point in the future.
     *
     * @param text The text to display. This will be drawn as one line.
     */
    public void setText(String text) {
        mText = text;
        requestLayout();
        invalidate();
    }

    /**
     * Sets the text size for this label. We set the text size of {@code Paint mTextPaint} to our
     * parameter {@code int size}, call {@code requestLayout} to schedule a layout pass of the view
     * tree, and call {@code invalidate} to invalidate the whole view so that {@code onDraw} will be
     * called at some point in the future.
     *
     * @param size Font size
     */
    public void setTextSize(int size) {
        // This text size has been pre-scaled by the getDimensionPixelOffset method
        mTextPaint.setTextSize(size);
        requestLayout();
        invalidate();
    }

    /**
     * Sets the text color for this label. We set the color of {@code Paint mTextPaint} to our
     * parameter {@code int color}, and call {@code invalidate} to invalidate the whole view so that
     * {@code onDraw} will be called at some point in the future.
     *
     * @param color ARGB value for the text
     */
    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    /**
     * Measure the view and its content to determine the measured width and the
     * measured height. This method is invoked by {@link #measure(int, int)} and
     * should be overridden by subclasses to provide accurate and efficient
     * measurement of their contents.
     * <p>
     * We call our methods {@code measureWidth} and {@code measureHeight} to determine to determine
     * the width and height we want given the space requirements imposed by our parent in our
     * parameters {@code widthMeasureSpec} and {@code heightMeasureSpec} respectively, and then
     * pass the values they calculate to the method {@code setMeasuredDimension} to store the measured
     * width and measured height.
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
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    /**
     * Determines the width of this view. We declare {@code result}, and then initialize {@code specMode}
     * with the mode of our parameter {@code measureSpec} (one of UNSPECIFIED, AT_MOST or EXACTLY),
     * and initialize {@code specSize} with the size in pixels defined by the {@code measureSpec}
     * measure specification. If {@code specMode} is EXACTLY (we were told how big to be) we set
     * {@code result} to {@code specSize}. Otherwise we set {@code result} to the total width of our
     * text {@code mText} when drawn using {@code Paint mTextPaint}, plus the left padding and the
     * right padding of this view. If {@code specMode} is AT_MOST we then set {@code result} to the
     * minimum of {@code result} and {@code specSize} (if it were UNSPECIFIED we leave {@code result}
     * as is.
     * <p>
     * Finally we return {@code result} to the caller.
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = (int) mTextPaint.measureText(mText) + getPaddingLeft() + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    /**
     * Determines the height of this view. We declare {@code result}, and then initialize {@code specMode}
     * with the mode of our parameter {@code measureSpec} (one of UNSPECIFIED, AT_MOST or EXACTLY),
     * and initialize {@code specSize} with the size in pixels defined by the {@code measureSpec}
     * measure specification. If {@code specMode} is EXACTLY (we were told how big to be) we set
     * {@code result} to {@code specSize}. Otherwise we set {@code result} to the total width of our
     * text {@code mText} when drawn using {@code Paint mTextPaint}, plus the left padding and the
     * right padding of this view. If {@code specMode} is AT_MOST we then set {@code result} to the
     * minimum of {@code result} and {@code specSize} (if it were UNSPECIFIED we leave {@code result}
     * as is.
     * <p>
     * Finally we return {@code result} to the caller.
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        mAscent = (int) mTextPaint.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = (int) (-mAscent + mTextPaint.descent()) + getPaddingTop()
                    + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * Render the text. First we call our super's implementation of {@code onDraw}, then we instruct
     * our parameter {@code Canvas canvas} to draw our text {@code String mText}, with x starting at
     * our left padding value (3), y starting at {@code mAscent} below our top padding, and using
     * {@code Paint mTextPaint} as the {@code Paint}.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(mText, getPaddingLeft(), getPaddingTop() - mAscent, mTextPaint);
    }
}
