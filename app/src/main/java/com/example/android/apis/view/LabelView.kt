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
package com.example.android.apis.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

import com.example.android.apis.R
import androidx.core.content.withStyledAttributes

/**
 * Example of how to write a custom subclass of View. LabelView
 * is used to draw simple text views. Note that it does not handle
 * styled text or right-to-left writing systems. Used by the
 * CustomView1.kt demo in its layout file layout/custom_view_1.xml
 */
@Suppress("MemberVisibilityCanBePrivate")
class LabelView : View {
    /**
     * [Paint] we use to draw our text with
     */
    private var mTextPaint: Paint? = null

    /**
     * Text we draw on our label
     */
    private var mText: String? = null

    /**
     * The maximum distance above the baseline based on the current typeface and text size of
     * [Paint] field [mTextPaint] (a negative number).
     */
    private var mAscent = 0

    /**
     * Constructor. This version is only needed if you will be instantiating the object manually
     * (not from a layout XML file). We call our super's constructor, then we call our method
     * [initLabelView].
     *
     * @param context context the view is running in
     */
    constructor(context: Context?) : super(context) {
        initLabelView()
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called when a view is
     * being constructed from an XML file, supplying attributes that were specified in the XML file.
     * Our attributes are defined in values/attrs.xml. First we call our super's constructor, then
     * we call our method [initLabelView].
     *
     * Next we retrieve styled attribute information in this [Context]'s theme to initialize
     * [TypedArray] `val a`, specifying R.styleable.LabelView as the attributes to retrieve.
     * R.styleable.LabelView is defined in a declare-styleable element and declares the following
     * attributes:
     *
     *  * name="text" format="string" R.styleable.LabelView_text
     *
     *  * name="textColor" format="color" R.styleable.LabelView_textColor
     *
     *  * name="textSize" format="dimension R.styleable.LabelView_textSize
     *
     * Having obtained our custom attributes in `a`, we proceed to extract them:
     *
     *  * We set [CharSequence] `val s` to the string at index R.styleable.LabelView_text in `a`,
     *  and if it is not null we call our method [setText] to set our text to `s`
     *
     *  * We call our method [setTextColor] to set the color of our text to the color at index
     *  R.styleable.LabelView_textColor in `a`, defaulting to black if the xml did not specify it.
     *
     *  * We set [Int] `val textSize` to the pixel version of the dimension in `a` at index
     *  R.styleable.LabelView_textSize, defaulting to 0 if the xml did not specify it. If
     *  `textSize` is greater than 0, we call our method [setTextSize] to set the size of our text.
     *
     * Finally we recycle `a`.
     *
     * @param context The [Context] the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initLabelView()
        context.withStyledAttributes(set = attrs, attrs = R.styleable.LabelView) {
            val s: CharSequence? = getString(R.styleable.LabelView_text)
            if (s != null) {
                setText(s.toString())
            }

            // Retrieve the color(s) to be used for this view and apply them.
            // Note, if you only care about supporting a single color, that you
            // can instead call a.getColor() and pass that to setTextColor().
            setTextColor(
                color = getColor(
                    /* index = */ R.styleable.LabelView_textColor,
                    /* defValue = */ -0x1000000
                )
            )
            val textSize: Int = getDimensionPixelOffset(
                /* index = */ R.styleable.LabelView_textSize,
                /* defValue = */ 0
            )
            if (textSize > 0) {
                setTextSize(textSize)
            }
        }
    }

    /**
     * Called to initialize this instance of [LabelView]. We allocate a new [Paint] instance for
     * [Paint] field [mTextPaint], set its antialias flag, set its text size to 16 times the logical
     * density of our display, and set its color to black. Finally We set the padding of our [View]
     * to 3 pixels on all four sides.
     */
    private fun initLabelView() {
        mTextPaint = Paint()
        mTextPaint!!.isAntiAlias = true
        // Must manually scale the desired text size to match screen density
        mTextPaint!!.textSize = 16 * resources.displayMetrics.density
        mTextPaint!!.color = -0x1000000
        setPadding(/* left = */ 3, /* top = */ 3, /* right = */ 3, /* bottom = */ 3)
    }

    /**
     * Sets the text to display in this label. We save our [String] parameter [text] in our field
     * [mText], call [requestLayout] to schedule a layout pass of the view tree, and call
     * [invalidate] to invalidate the whole view so that [onDraw] will be called at some point in
     * the future.
     *
     * @param text The text to display. This will be drawn as one line.
     */
    fun setText(text: String?) {
        mText = text
        requestLayout()
        invalidate()
    }

    /**
     * Sets the text size for this label. We set the text size of [Paint] field [mTextPaint] to our
     * [Int] parameter [size], call [requestLayout] to schedule a layout pass of the view tree, and
     * call [invalidate] to invalidate the whole view so that [onDraw] will be called at some point
     * in the future.
     *
     * @param size Font size
     */
    fun setTextSize(size: Int) {
        // This text size has been pre-scaled by the getDimensionPixelOffset method
        mTextPaint!!.textSize = size.toFloat()
        requestLayout()
        invalidate()
    }

    /**
     * Sets the text color for this label. We set the color of [Paint] field [mTextPaint] to our
     * [Int] parameter [color], and call [invalidate] to invalidate the whole view so that [onDraw]
     * will be called at some point in the future.
     *
     * @param color ARGB color value to use to draw text
     */
    fun setTextColor(color: Int) {
        mTextPaint!!.color = color
        invalidate()
    }

    /**
     * Measure the view and its content to determine the measured width and the measured height.
     * This method is invoked by [measure] and should be overridden by subclasses to provide
     * accurate and efficient measurement of their contents.
     *
     * We call our methods [measureWidth] and [measureHeight] to determine the width and height we
     * want given the space requirements imposed by our parent in our parameters [widthMeasureSpec]
     * and [heightMeasureSpec] respectively, and then pass the values they calculate to the method
     * [setMeasuredDimension] to store the measured width and measured height.
     *
     * @param widthMeasureSpec  horizontal space requirements as imposed by the parent. The
     * requirements are encoded with [MeasureSpec].
     * @param heightMeasureSpec vertical space requirements as imposed by the parent. The
     * requirements are encoded with [MeasureSpec].
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            /* measuredWidth = */ measureWidth(measureSpec = widthMeasureSpec),
            /* measuredHeight = */ measureHeight(measureSpec = heightMeasureSpec)
        )
    }

    /**
     * Determines the width of this view. We declare [Int] `var result`, and then initialize
     * [Int] `val specMode` with the mode of our parameter [measureSpec] (one of UNSPECIFIED,
     * AT_MOST or EXACTLY), and initialize `val specSize` with the size in pixels defined by the
     * [measureSpec] measure specification. If `specMode` is EXACTLY (we were told how big to be)
     * we set `result` to `specSize`. Otherwise we set `result` to the total width of our text
     * field [mText] when drawn using [Paint] field [mTextPaint], plus the left padding and the
     * right padding of this view. If `specMode` is AT_MOST we then set `result` to the minimum
     * of `result` and `specSize` (if it were UNSPECIFIED we leave `result` as is.
     *
     * Finally we return `result` to the caller.
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private fun measureWidth(measureSpec: Int): Int {
        var result: Int
        val specMode: Int = MeasureSpec.getMode(measureSpec)
        val specSize: Int = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize
        } else {
            // Measure the text
            result = mTextPaint!!.measureText(mText).toInt() + paddingLeft + paddingRight
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = result.coerceAtMost(maximumValue = specSize)
            }
        }
        return result
    }

    /**
     * Determines the height of this view. We declare [Int] `var result`, and then initialize
     * `val specMode` with the mode of our parameter [measureSpec] (one of UNSPECIFIED, AT_MOST
     * or EXACTLY), and initialize [Int] `val specSize` with the size in pixels defined by the
     * [measureSpec] measure specification. If `specMode` is EXACTLY (we were told how big to be)
     * we set `result` to `specSize`. Otherwise we set `result` to the total width of our text
     * field [mText] when drawn using [Paint] field [mTextPaint], plus the left padding and the
     * right padding of this view. If `specMode` is AT_MOST we then set `result` to the minimum
     * of `result` and `specSize` (if it were UNSPECIFIED we leave `result` as is).
     *
     * Finally we return `result` to the caller.
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private fun measureHeight(measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        mAscent = mTextPaint!!.ascent().toInt()
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = ((-mAscent + mTextPaint!!.descent()).toInt() + paddingTop
                + paddingBottom)
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = result.coerceAtMost(specSize)
            }
        }
        return result
    }

    /**
     * Render the text. First we call our super's implementation of `onDraw`, then we instruct
     * our [Canvas] parameter [canvas] to draw our text in [String] field [mText], with x starting
     * at our left padding value (3), y starting at [mAscent] below our top padding, and using
     * [Paint] field [mTextPaint] as the [Paint].
     *
     * @param canvas the [Canvas] on which the background will be drawn
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawText(
            /* text = */ mText!!,
            /* x = */ paddingLeft.toFloat(),
            /* y = */ paddingTop - mAscent.toFloat(),
            /* paint = */ mTextPaint!!
        )
    }
}