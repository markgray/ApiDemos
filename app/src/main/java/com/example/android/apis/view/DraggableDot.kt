/*
 * Copyright (C) 2010 The Android Open Source Project
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
@file:Suppress("ReplaceNotNullAssertionWithElvisReturn")

package com.example.android.apis.view

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.SystemClock
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import com.example.android.apis.R

/**
 * Used by [DragAndDropDemo] to draw the dots which the user can drag.
 * (See our init block for the details of our construction.)
 *
 * @param context The [Context] the view is running in, through which it can access the current
 * theme, resources, etc.
 * @param attrs The attributes of the XML tag that is inflating the view.
 * RequiresApi(Build.VERSION_CODES.HONEYCOMB)
 */
@SuppressLint("SetTextI18n")
class DraggableDot(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    /**
     * Flag used to indicate that a drag has started. Set to true when we receive a ACTION_DRAG_STARTED
     * event, and false when we receive a ACTION_DRAG_ENDED event. It is used in our [onDraw]
     * method to decide whether to light up as a potential target.
     */
    private var mDragInProgress = false

    /**
     * Flag used to indicate that the dot being dragged is over our [DraggableDot] instance.
     * Set to true when we receive a ACTION_DRAG_ENTERED event, false when we receive either an
     * ACTION_DRAG_EXITED, or a ACTION_DRAG_ENDED event. It is used in our [onDraw] method to
     * decide whether to "light" our [DraggableDot] with a green (false) or white (true) circle.
     */
    private var mHovering = false

    /**
     * Flag used to indicate that we accept drops of the [DraggableDot] being dragged. If is set to
     * true when we receive a ACTION_DRAG_STARTED event, and never set to false again so it may be
     * unnecessary.
     */
    private var mAcceptsDrag = false

    /**
     * [TextView] we are to append the drag's textual conversion to if it is dropped on us.
     */
    private var mReportView: TextView? = null

    /**
     * [Paint] used to draw our Red dot with.
     */
    private val mPaint: Paint

    /**
     * [TextPaint] used to draw the "legend" in the center of our dot. The "legend" comes from
     * the dot:legend attribute as set in the layout file for this instance of [DraggableDot].
     */
    private val mLegendPaint: TextPaint

    /**
     * [Paint] used to draw the green or white circle around our dot when a drag is in progress
     * if we are a potential target to be dropped on.
     */
    private val mGlow: Paint

    /**
     * Radius of our dot, set by the dot:radius="64dp" attribute in our layout.
     */
    private var mRadius = 0

    /**
     * The type of ANR (application not responding) that our dot should produce, set by the
     * dot:anr="thumbnail", and dot:anr="drop" attributes in our layout. The "dot:anr" attribute
     * is defined in values/attrs.xml using a "declare-styleable" element with the value of
     * "thumbnail" 1, and the value of "drop" 2. The "thumbnail" ANR causes a call to our method
     * [sleepSixSeconds] when a dot with that attribute is long clicked, the "drop" ANR causes a
     * call to [sleepSixSeconds] when another dot is "dropped" on a dot with that attribute.
     */
    private var mAnrType = 0

    /**
     * The text to display in the center of our dot, set by the dot:legend attribute in our layout.
     */
    private var mLegend: CharSequence?

    /**
     * Sleeps for 6 seconds in an attempt to generate an ANR (application not responding). We
     * initialize our [Long] variable `val start` with the milliseconds of non-sleep uptime since
     * boot, then we call [Thread.sleep] to sleep for 1000 milliseconds as long as the milliseconds
     * of non-sleep uptime since boot is less than `start` plus 6000 (ie. we hang for 6-7 seconds).
     */
    fun sleepSixSeconds() {
        // hang forever; good for producing ANRs
        val start = SystemClock.uptimeMillis()
        do {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                Log.i(TAG, e.localizedMessage!!)
            }
        } while (SystemClock.uptimeMillis() < start + 6000)
    }

    /**
     * Shadow builder that can ANR if desired
     * Constructs a shadow image builder based on a View which can optionally force an ANR.
     * First we call our super's constructor, then we save our [Boolean] parameter in our
     * field [mDoAnr].
     *
     * @param view   A View. Any View in scope can be used.
     * @param mDoAnr Flag to indicate whether we should for a ANR when we are long clicked.
     */
    internal inner class ANRShadowBuilder(
        view: View?,
        private var mDoAnr: Boolean
    ) : DragShadowBuilder(view) {

        /**
         * Draws the shadow image. First if our flag [mDoAnr] is true we call our method
         * [sleepSixSeconds] to try to force an ANR, then we call our super's implementation
         * of [onDrawShadow].
         *
         * @param canvas A [Canvas] object in which to draw the shadow image.
         */
        override fun onDrawShadow(canvas: Canvas) {
            if (mDoAnr) {
                sleepSixSeconds()
            }
            super.onDrawShadow(canvas)
        }

    }

    /**
     * A setter method for [TextView] field [mReportView], just sets our field [mReportView]
     * to its argument.
     *
     * @param view TextView that we want to set `TextView mReportView` to
     */
    fun setReportView(view: TextView?) {
        mReportView = view
    }

    /**
     * We implement this to draw our [View] when required to do so. First we initialize our [Float]
     * variable `var wf` with the width of our [View], and `var hf` with our height. We calculate
     * the center X `val cx` and center Y `val cy` to be half of the `wf` and `hf` respectively. We
     * then subtract the left and right padding from `wf` and the top and bottom padding from `hf`.
     * We calculate `var rad` (our radius) to be half of the smaller of `wf` and `hf` then instruct
     * [Canvas] parameter [canvas] to draw a circle at `(cy,cy)` with a radius of `rad` using [Paint]
     * field [mPaint] as the paint.
     *
     * Then if our field [mLegend] is not null, and has 1 or more characters in it we instruct
     * [canvas] to draw the text in [mLegend] with the center of the text with `cx` as the X
     * coordinate and `cy` with half of the line spacing of [mLegendPaint] added to it as the
     * Y coordinate, and using [TextPaint] field [mLegendPaint] as the paint.
     *
     * Next we check whether we are in the middle of a drag ([mDragInProgress] is true) and we
     * are configured to accept drops ([mAcceptsDrag] is true). If so we want to "light up as
     * a potential target". To do this we loop for NUM_GLOW_STEPS (10) times setting `var color`
     * to WHITE_STEP if the drag is over us ([mHovering] is true) or GREEN_STEP if not. We now
     * multiply `color` or'ed with ALPHA_STEP by the index count of our loop (10 down to 1) and
     * set the color of [Paint] field [mGlow] to it. We instruct [canvas] to draw a circle centered
     * at `(cx,cy)` of `rad` radius using [mGlow] as the paint, subtract 0.5 from `rad` and have
     * [canvas] draw another circle. We subtract another 0.5 from `rad` and continue the loop.
     *
     * @param canvas the [Canvas] on which the background will be drawn
     */
    override fun onDraw(canvas: Canvas) {
        var wf = width.toFloat()
        var hf = height.toFloat()
        val cx: Float = wf / 2
        val cy: Float = hf / 2
        wf -= paddingLeft + paddingRight.toFloat()
        hf -= paddingTop + paddingBottom.toFloat()
        var rad: Float = if (wf < hf) wf / 2 else hf / 2
        canvas.drawCircle(/* cx = */ cx, /* cy = */ cy, /* radius = */ rad, /* paint = */ mPaint)
        if (mLegend != null && mLegend!!.isNotEmpty()) {
            canvas.drawText(
                /* text = */ mLegend!!,
                /* start = */ 0,
                /* end = */ mLegend!!.length,
                /* x = */ cx,
                /* y = */ cy + mLegendPaint.fontSpacing / 2,
                /* paint = */ mLegendPaint
            )
        }

        // if we're in the middle of a drag, light up as a potential target
        if (mDragInProgress && mAcceptsDrag) {
            for (i in NUM_GLOW_STEPS downTo 1) {
                var color: Int = if (mHovering) WHITE_STEP else GREEN_STEP
                color = i * (color or ALPHA_STEP)
                mGlow.color = color
                canvas.drawCircle(
                    /* cx = */ cx,
                    /* cy = */ cy,
                    /* radius = */ rad,
                    /* paint = */ mGlow
                )
                rad -= 0.5f
                canvas.drawCircle(
                    /* cx = */ cx,
                    /* cy = */ cy,
                    /* radius = */ rad,
                    /* paint = */ mGlow
                )
                rad -= 0.5f
            }
        }
    }

    /**
     * Measure the view and its content to determine the measured width and the measured height.
     * We set `val totalDiameter` to twice the value of our field [mRadius] plus our left padding
     * and right padding, then use `totalDiameter` as both the X and Y arguments to
     * [setMeasuredDimension].
     *
     * @param widthSpec  horizontal space requirements as imposed by the parent.
     * The requirements are encoded with
     * [MeasureSpec].
     * @param heightSpec vertical space requirements as imposed by the parent.
     * The requirements are encoded with
     * [MeasureSpec].
     */
    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val totalDiameter: Int = 2 * mRadius + paddingLeft + paddingRight
        setMeasuredDimension(
            /* measuredWidth = */ totalDiameter,
            /* measuredHeight = */ totalDiameter
        )
    }

    /**
     * Handles drag events sent by the system following a call to [startDrag]. We initialize
     * our [Boolean] variable `var result` to false, then switch based on the action of our
     * [DragEvent] parameter [event]:
     *
     *  * ACTION_DRAG_STARTED - We set our [Boolean] field [mDragInProgress] to true, and
     *  set our [Boolean] field [mAcceptsDrag] and `result` to true. Finally we call
     *  [invalidate] to schedule a redraw so we can "light up as a potential target".
     *  Then we break.
     *
     *  * ACTION_DRAG_ENDED - If [mAcceptsDrag] is true we call [invalidate] to schedule a redraw
     *  so we can "light up as a potential target", then we set our field [mDragInProgress] to true
     *  and [mHovering] to true. Then we break.
     *
     *  * ACTION_DRAG_LOCATION - We set `result` to [mAcceptsDrag] and break.
     *
     *  * ACTION_DROP - If we are configured to ANR when dropped on we call our method
     *  [sleepSixSeconds] to force an ANR (nasty!), then we call our method [processDrop]
     *  to process the drop, set `result` to true and break.
     *
     *  * ACTION_DRAG_ENTERED - We set our field [mHovering] to true, call [invalidate]
     *  to schedule a redraw so we can "light up as a WHITE hovered over target", and break.
     *
     *  * ACTION_DRAG_EXITED - We set our field [mHovering] to false, call [invalidate]
     *  to schedule a redraw so we can "light up as a GREEN potential target", and break.
     *
     *  * default - We set `result` to [mAcceptsDrag] and break.
     *
     * Finally we return `result` to the caller.
     *
     * @param event The [DragEvent] sent by the system.
     * @return true if the method was successful, otherwise false. The method should return true in
     * response to an action type of ACTION_DRAG_STARTED to receive drag events for the current
     * operation. The method should also return true in response to an action type of ACTION_DROP if
     * it consumed the drop, or false if it didn't.
     */
    override fun onDragEvent(event: DragEvent): Boolean {
        var result = false
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {

                // claim to accept any dragged content
                Log.i(TAG, "Drag started, event=$event")
                // cache whether we accept the drag to return for LOCATION events
                mDragInProgress = true
                result = true
                @Suppress("KotlinConstantConditions")
                mAcceptsDrag = result
                // Redraw in the new visual state since we are a potential drop target
                invalidate()
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                Log.i(TAG, "Drag ended.")
                if (mAcceptsDrag) {
                    invalidate()
                }
                mDragInProgress = false
                mHovering = false
            }

            DragEvent.ACTION_DRAG_LOCATION -> {

                // we returned true to DRAG_STARTED, so return true here
                Log.i(TAG, "... seeing drag locations ...")
                result = mAcceptsDrag
            }

            DragEvent.ACTION_DROP -> {
                Log.i(TAG, "Got a drop! dot=$this event=$event")
                if (mAnrType == ANR_DROP) {
                    sleepSixSeconds()
                }
                processDrop(event)
                result = true
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                Log.i(TAG, "Entered dot @ $this")
                mHovering = true
                invalidate()
            }

            DragEvent.ACTION_DRAG_EXITED -> {
                Log.i(TAG, "Exited dot @ $this")
                mHovering = false
                invalidate()
            }

            else -> {
                Log.i(TAG, "other drag event: $event")
                result = mAcceptsDrag
            }
        }
        return result
    }

    /**
     * Processes an ACTION_DROP [DragEvent] received by [onDragEvent]. First we retrieve the
     * [ClipData] object sent to the system as part of the call to [startDrag] to initialize
     * [ClipData] `val data`, and set `val N` to the number of items in the clip data. Next we
     * loop over the [ClipData.Item] `val item` objects in `data`, logging the string value of
     * the `item`, and if our [TextView] field [mReportView] is not null we create [String]
     * `var text` by coercing `item` to a [CharSequence] then converting that to a [String].
     * We next check whether the local state object sent to the system as part of the call to
     * [startDrag] is "this" in which case we append the string " : Dropped on self!" to `text`.
     * Finally we append `text` to [TextView] field [mReportView] and loop for the next
     * [ClipData.Item].
     *
     * @param event [DragEvent] passed to our [onDragEvent] callback
     */
    private fun processDrop(event: DragEvent) {
        val data: ClipData = event.clipData
        val n: Int = data.itemCount
        for (i in 0 until n) {
            val item: ClipData.Item = data.getItemAt(i)
            Log.i(TAG, "Dropped item $i : $item")
            if (mReportView != null) {
                var text = item.coerceToText(context).toString()
                if (event.localState === this as Any) {
                    text += " : Dropped on self!"
                }
                mReportView!!.append(text)
            }
        }
    }

    companion object {
        /**
         * TAG used for logging
         */
        const val TAG: String = "DraggableDot"

        /**
         * Number of steps in green, white, and alpha colors used when drawing the green or white circle
         * around our dot when a drag is in progress (the effect is not really noticeable to me).
         */
        private const val NUM_GLOW_STEPS = 10

        /**
         * Size of a green step to use when drawing the green around our dot when a drag is in progress
         * (and the dot being dragged is not over us).
         */
        private const val GREEN_STEP = 0x0000FF00 / NUM_GLOW_STEPS

        /**
         * Size of a white step to use when drawing the white around our dot when a drag is in progress
         * (and the dot being dragged is over us).
         */
        private const val WHITE_STEP = 0x00FFFFFF / NUM_GLOW_STEPS

        /**
         * Size of the alpha step used when drawing the green or white circle around our dot when a drag
         * is in progress
         */
        private const val ALPHA_STEP = -0x1000000 / NUM_GLOW_STEPS

        @Suppress("unused")
        const val ANR_NONE: Int = 0

        /**
         * Value of `mAnrType` set by dot:anr="thumbnail"
         */
        const val ANR_SHADOW: Int = 1

        /**
         * Value of `mAnrType` set by dot:anr="drop"
         */
        const val ANR_DROP: Int = 2
    }

    /**
     * Init block for the Constructor that is called when inflating a `DraggableDot` from XML.
     * This is called when a `DraggableDot` is being constructed from an XML file, supplying
     * attributes that were specified in the XML file. First we we enable our view to receive
     * focus and to be clickable. We initialize our field `mLegend` with an empty string. We
     * initialize our `Paint` field `mPaint` with a new instance, set its antialias flag to true,
     * its stroke width to 6, and its color to a darkish red. We initialize our `TextPaint` field
     * `mLegendPaint` with a new instance, set its antialias flag to true, set its text size to 12
     * times the logical density of the display, set its text alignment to CENTER, and set its
     * color to a slightly bluish white. We initialize our `Paint` field `mGlow` with a new
     * instance, set its antialias flag to true, its stroke width to 1, and set its style to STROKE.
     *
     * Next we initialize our `TypedArray` variable `val a` with styled attribute information in
     * this Context's theme to the attributes of R.styleable.DraggableDot (defined by a
     * "declare-styleable" element in the "resources" section of values/attrs.xml: "radius",
     * "legend", and "anr"). We initialize our variable `val n` with the the number of indices
     * in the `TypedArray` variable `a` that actually have data, then loop through them setting
     * our `Int` variable `val attr` to the attribute type of each, which we switch on:
     *
     *  * R.styleable.DraggableDot_radius - we set the value of our `Int` field `mRadius` to
     *  the value of the dimensional unit attribute at index `attr` in raw pixels.
     *
     *  * R.styleable.DraggableDot_legend - we set our `CharSequence` field `mLegend` to
     *  the styled string value for the attribute at index `attr` of `a`.
     *
     *  * R.styleable.DraggableDot_anr - we set our `Int` field `mAnrType` to the integer
     *  value for the attribute at index `attr` of `a` (defaulting to 0).
     *
     * When done processing all the attributes we recycle `a`.
     *
     * Finally we set our `OnLongClickListener` to a lambda which creates a string describing
     * our instance in `ClipData` variable `data`, and starts a drag and drop operation.
     *
     * Parameter: context The `Context` the view is running in, through which it can
     * access the current theme, resources, etc.
     * Parameter: attrs   The attributes of the XML tag that is inflating the view.
     */
    init {
        isFocusable = true
        isClickable = true
        mLegend = ""
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = 6f
        mPaint.color = -0x300000
        mLegendPaint = TextPaint()
        mLegendPaint.isAntiAlias = true
        mLegendPaint.textSize = resources.displayMetrics.density * 12
        mLegendPaint.textAlign = Paint.Align.CENTER
        mLegendPaint.color = -0xf0f01
        mGlow = Paint()
        mGlow.isAntiAlias = true
        mGlow.strokeWidth = 1f
        mGlow.style = Paint.Style.STROKE

        // look up any layout-defined attributes
        context.withStyledAttributes(set = attrs, attrs = R.styleable.DraggableDot) {
            val n: Int = indexCount
            for (i in 0 until n) {
                when (val attr: Int = getIndex(i)) {
                    R.styleable.DraggableDot_radius -> {
                        mRadius = getDimensionPixelSize(/* index = */ attr, /* defValue = */ 0)
                    }

                    R.styleable.DraggableDot_legend -> {
                        mLegend = getText(/* index = */ attr)
                    }

                    R.styleable.DraggableDot_anr -> {
                        mAnrType = getInt(/* index = */ attr, /* defValue = */ 0)
                    }
                }
            }
        }
        Log.i(
            TAG, "DraggableDot @ " + this + " : radius=" + mRadius + " legend='" + mLegend
                + "' anr=" + mAnrType
        )
        setOnLongClickListener { v: View ->

            /**
             * Called when a view has been clicked and held. First we set the text of our [TextView]
             * field [mReportView] to the empty string (note that the "thumbnail" or drag ANR dot
             * hangs before the view can be displayed). Next we create [ClipData] `val data` to be
             * a new instance of [ClipData] holding data of the type MIMETYPE_TEXT_PLAIN which uses
             * "dot" as the user-visible label for the clip data, and a formatted string which
             * contains the text "Dot : " concatenated to the string value of [View] parameter [v]
             * as the actual text in the clip. Next we call the [startDrag] method of [View]
             * parameter [v] to start a drag and drop operation using `data` as the [ClipData]
             * object pointing to the data to be transferred by the drag and drop operation, an
             * [ANRShadowBuilder] object for building the drag shadow which is constructed using
             * [v] as the [View] (any [View] in scope can be used) and if [mAnrType] is equal to
             * ANR_SHADOW the [Boolean] flag value *true* so that an ANR will be generated when the
             * dot is long-clicked (this dot is nasty) otherwise false so the dot is a normal dot.
             * We also include [v] cast to [Any] in the arguments of [startDrag] to use as the local
             * state Object which contains local data about the drag and drop operation, and 0 for
             * the flags that control the drag and drop operation (no flags). Finally we return true
             * to the caller.
             *
             * @param v The view that was clicked and held.
             * @return true if the callback consumed the long click, false otherwise.
             */
            mReportView!!.text = ""
            val data = ClipData.newPlainText("dot", "Dot : $v")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                v.startDragAndDrop(
                    /* data = */ data,
                    /* shadowBuilder = */ ANRShadowBuilder(v, mAnrType == ANR_SHADOW),
                    /* myLocalState = */ v as Any,
                    /* flags = */ 0
                )
            } else {
                @Suppress("DEPRECATION")  // Needed for SDK older than N
                v.startDrag(
                    /* data = */ data,
                    /* shadowBuilder = */ ANRShadowBuilder(v, mAnrType == ANR_SHADOW),
                    /* myLocalState = */ v as Any,
                    /* flags = */ 0
                )
            }
            true
        }
    }
}