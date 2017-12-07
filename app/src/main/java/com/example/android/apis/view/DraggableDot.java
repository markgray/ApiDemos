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

package com.example.android.apis.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.TextView;

import com.example.android.apis.R;

/**
 * Used by {@code DragAndDropDemo} to draw the dots which the user can drag.
 */
@SuppressLint("SetTextI18n")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DraggableDot extends View {
    /**
     * TAG used for logging
     */
    static final String TAG = "DraggableDot";

    /**
     * Flag used to indicate that a drag has started. Set to true when we receive a ACTION_DRAG_STARTED
     * event, and false when we receive a ACTION_DRAG_ENDED event. It is used in our {@code onDraw}
     * method to decide whether to light up as a potential target.
     */
    private boolean mDragInProgress;
    /**
     * Flag used to indicate that the dot being dragged is over our {@code DraggableDot} instance.
     * Set to true when we receive a ACTION_DRAG_ENTERED event, false when we receive either an
     * ACTION_DRAG_EXITED, or a ACTION_DRAG_ENDED event. It is used in our {@code onDraw} method to
     * decide whether to "light" our {@code DraggableDot} with a green (false) or white (true) circle.
     */
    private boolean mHovering;
    /**
     * Flag used to indicate that we accept drops of the {@code DraggableDot} being dragged. If is
     * set to true when we receive a ACTION_DRAG_STARTED event, and never set to false again so it may
     * be unnecessary.
     */
    private boolean mAcceptsDrag;
    /**
     * {@code TextView} we are to append the drag's textual conversion to if it is dropped on us.
     */
    TextView mReportView;

    /**
     * {@code Paint} used to draw our Red dot with.
     */
    private Paint mPaint;
    /**
     * {@code TextPaint} used to draw the "legend" in the center of our dot. The "legend" comes from
     * the dot:legend attribute as set in the layout file for this instance of {@code DraggableDot}.
     */
    private TextPaint mLegendPaint;
    /**
     * {@code Paint} used to draw the green or white circle around our dot when a drag is in progress
     * if we are a potential target to be dropped on.
     */
    private Paint mGlow;
    /**
     * Number of steps in green, white, and alpha colors used when drawing the green or white circle
     * around our dot when a drag is in progress (the effect is not really noticeable to me).
     */
    private static final int NUM_GLOW_STEPS = 10;
    /**
     * Size of a green step to use when drawing the green around our dot when a drag is in progress
     * (and the dot being dragged is not over us).
     */
    private static final int GREEN_STEP = 0x0000FF00 / NUM_GLOW_STEPS;
    /**
     * Size of a white step to use when drawing the white around our dot when a drag is in progress
     * (and the dot being dragged is over us).
     */
    private static final int WHITE_STEP = 0x00FFFFFF / NUM_GLOW_STEPS;
    /**
     * Size of the alpha step used when drawing the green or white circle around our dot when a drag
     * is in progress
     */
    private static final int ALPHA_STEP = 0xFF000000 / NUM_GLOW_STEPS;

    /**
     * Radius of our dot, set by the dot:radius="64dp" attribute in our layout.
     */
    int mRadius;
    /**
     * The type of ANR (application not responding) that our dot should produce, set by the
     * dot:anr="thumbnail", and dot:anr="drop" attributes in our layout. The "dot:anr" attribute
     * is defined in values/attrs.xml using a "declare-styleable" element with the value of
     * "thumbnail" 1, and the value of "drop" 2. The "thumbnail" ANR causes a call to our method
     * {@code sleepSixSeconds} when a dot with that attribute is long clicked, the "drop" ANR
     * causes a call to {@code sleepSixSeconds} when another dot is "dropped" on a dot with that
     * attribute.
     */
    int mAnrType;
    /**
     * The text to display in the center of our dot, set by the dot:legend attribute in our layout.
     */
    CharSequence mLegend;

    @SuppressWarnings("unused")
    static final int ANR_NONE = 0;
    /**
     * Value of {@code mAnrType} set by dot:anr="thumbnail"
     */
    static final int ANR_SHADOW = 1;
    /**
     * Value of {@code mAnrType} set by dot:anr="drop"
     */
    static final int ANR_DROP = 2;

    /**
     * Sleeps for 6 seconds in an attempt to generate an ANR (application not responding). We initialize
     * our variable {@code long start} with the milliseconds of non-sleep uptime since boot, then we
     * call {@code Thread.sleep} to sleep for 1000 milliseconds as long as the milliseconds of non-sleep
     * uptime since boot is less than {@code start} + 6000 (ie. we hang for 6-7 seconds).
     */
    void sleepSixSeconds() {
        // hang forever; good for producing ANRs
        long start = SystemClock.uptimeMillis();
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.i(TAG, e.getLocalizedMessage());
            }
        } while (SystemClock.uptimeMillis() < start + 6000);
    }

    /**
     * Shadow builder that can ANR if desired
     */
    class ANRShadowBuilder extends DragShadowBuilder {
        /**
         * Flag to indicate whether we should force an ANR when we are long clicked.
         */
        boolean mDoAnr;

        /**
         * Constructs a shadow image builder based on a View which can optionally force an ANR.
         * First we call our super's constructor, then we save our parameter {@code boolean doAnr}
         * in our field {@code boolean mDoAnr}.
         *
         * @param view  A View. Any View in scope can be used.
         * @param doAnr Flag to indicate whether we should for a ANR when we are long clicked.
         */
        @SuppressWarnings("WeakerAccess")
        public ANRShadowBuilder(View view, boolean doAnr) {
            super(view);
            mDoAnr = doAnr;
        }

        /**
         * Draws the shadow image. First if our flag {@code mDoAnr} is true we call our method
         * {@code sleepSixSeconds} to try to force an ANR, then we call our super's implementation
         * of {@code onDrawShadow}.
         *
         * @param canvas A {@link android.graphics.Canvas} object in which to draw the shadow image.
         */
        @Override
        public void onDrawShadow(Canvas canvas) {
            if (mDoAnr) {
                sleepSixSeconds();
            }
            super.onDrawShadow(canvas);
        }
    }

    /**
     * Constructor that is called when inflating a {@code DraggableDot} from XML. This is called when
     * a {@code DraggableDot} is being constructed from an XML file, supplying attributes that were
     * specified in the XML file. First we call through to our super's constructor, then we enable
     * our view to receive focus and to be clickable. We initialize our field {@code mLegend} with
     * an empty string. We initialize our field {@code Paint mPaint} with a new instance, set its
     * antialias flag to true, its stroke width to 6, and its color to a darkish red. We initialize
     * our field {@code TextPaint mLegendPaint} with a new instance, set its antialias flag to true,
     * set its text size to 12 times the logical density of the display, set its text alignment to
     * CENTER, and set its color to a slightly bluish white. We initialize our field {@code Paint mGlow}
     * with a new instance, set its antialias flag to true, its stroke width to 1, and set its style
     * to STROKE.
     * <p>
     * Next we initialize our variable {@code TypedArray a} with styled attribute information in this
     * Context's theme to the attributes of R.styleable.DraggableDot (defined by a "declare-styleable"
     * element in the "resources" section of values/attrs.xml: "radius", "legend", and "anr"). We
     * initialize our variable {@code int N} with the the number of indices in the {@code TypedArray a}
     * that actually have data, then loop through them setting our variable {@code int attr} to the
     * attribute type of each, which we switch on:
     * <ul>
     * <li>
     * R.styleable.DraggableDot_radius - we set the value of our field {@code int mRadius} to
     * the value of the dimensional unit attribute at index {@code attr} in raw pixels.
     * </li>
     * <li>
     * R.styleable.DraggableDot_legend - we set our field {@code CharSequence mLegend} to
     * the styled string value for the attribute at index {@code attr} of {@code a}.
     * </li>
     * <li>
     * R.styleable.DraggableDot_anr - we set our field {@code int mAnrType} to the integer
     * value for the attribute at index {@code attr} of {@code a} (defaulting to 0).
     * </li>
     * </ul>
     * When done processing all the attributes we recycle {@code a}.
     * <p>
     * Finally we set our {@code OnLongClickListener} to an anonymous class which creates a string
     * describing our instance in {@code ClipData data}, and starts a drag and drop operation.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public DraggableDot(final Context context, AttributeSet attrs) {
        super(context, attrs);

        setFocusable(true);
        setClickable(true);

        mLegend = "";

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(6);
        mPaint.setColor(0xFFD00000);

        mLegendPaint = new TextPaint();
        mLegendPaint.setAntiAlias(true);
        mLegendPaint.setTextSize(getResources().getDisplayMetrics().density * 12);
        mLegendPaint.setTextAlign(Paint.Align.CENTER);
        mLegendPaint.setColor(0xFFF0F0FF);

        mGlow = new Paint();
        mGlow.setAntiAlias(true);
        mGlow.setStrokeWidth(1);
        mGlow.setStyle(Paint.Style.STROKE);

        // look up any layout-defined attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DraggableDot);

        final int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.DraggableDot_radius: {
                    mRadius = a.getDimensionPixelSize(attr, 0);
                }
                break;

                case R.styleable.DraggableDot_legend: {
                    mLegend = a.getText(attr);
                }
                break;

                case R.styleable.DraggableDot_anr: {
                    mAnrType = a.getInt(attr, 0);
                }
                break;
            }
        }
        a.recycle();

        Log.i(TAG, "DraggableDot @ " + this + " : radius=" + mRadius + " legend='" + mLegend
                + "' anr=" + mAnrType);

        setOnLongClickListener(new View.OnLongClickListener() {
            /**
             * Called when a view has been clicked and held. First we set the text of our field
             * {@code TextView mReportView} to the empty string (note that the "thumbnail" or drag
             * ANR dot hangs before the view can be displayed). Next we create {@code ClipData data}
             * to be a new ClipData holding data of the type MIMETYPE_TEXT_PLAIN which uses "dot" as
             * the user-visible label for the clip data, and a formatted string which contains the
             * text "Dot : " concatenated to the string value of {@code View v} as the actual text
             * in the clip. Next we call the {@code startDrag} method of {@code View v} to start a
             * drag and drop operation using {@code data} as the ClipData object pointing to the data
             * to be transferred by the drag and drop operation, an {@code ANRShadowBuilder} object
             * for building the drag shadow which is constructed using {@code v} as the view (any
             * View in scope can be used) and if {@code mAnrType} is equal to ANR_SHADOW the flag
             * true so that an ANR will be generated when the dot is long-clicked (this dot is nasty)
             * otherwise false so the dot is a normal dot. We also include {@code v} cast to Object
             * in the arguments of {@code startDrag} to use as the local state Object which contains
             * local data about the drag and drop operation, and 0 for the flags that control the
             * drag and drop operation (no flags). Finally we return true to the caller.
             *
             * @param v The view that was clicked and held.
             * @return true if the callback consumed the long click, false otherwise.
             */
            @Override
            public boolean onLongClick(View v) {
                mReportView.setText("");
                ClipData data = ClipData.newPlainText("dot", "Dot : " + v.toString());
                //noinspection RedundantCast
                v.startDrag(data, new ANRShadowBuilder(v, mAnrType == ANR_SHADOW), (Object) v, 0);
                return true;
            }
        });
    }

    /**
     * A setter method for {@code TextView mReportView}, just sets our field {@code mReportView}
     * to its argument.
     *
     * @param view TextView that we want to set {@code TextView mReportView} to
     */
    void setReportView(TextView view) {
        mReportView = view;
    }

    /**
     * We implement this to draw our View when required to do so. First we initialize our variable
     * {@code float wf} with the width of our View, and {@code float hf} with our height. We calculate
     * the center X {@code float cx} and center Y {@code float cy} to be half of the {@code wf} and
     * {@code hf} respectively. We then subtract the left and right padding from {@code wf} and the
     * top and bottom padding from {@code hf}. We calculate {@code float rad} (our radius) to be half
     * of the smaller of {@code wf} and {@code hf} then instruct {@code Canvas canvas} to draw a circle
     * at (cy,cy) with a radius of {@code rad} using {@code Paint mPaint} as the paint.
     * <p>
     * Then if our field {@code mLegend} is not null, and has 1 or more characters in it we instruct
     * {@code canvas} to draw the text in {@code mLegend} with the center of the text with {@code cx}
     * as the X coordinate and {@code cy} with half of the line spacing of {@code mLegendPaint} added
     * to it as the Y coordinate, and using {@code TextPaint mLegendPaint} as the paint.
     * <p>
     * Next we check whether we are in the middle of a drag ({@code mDragInProgress} is true) and we
     * are configured to accept drops ({@code mAcceptsDrag} is true). If so we want to "light up as
     * a potential target". To do this we loop for NUM_GLOW_STEPS (10) times setting {@code int color}
     * to WHITE_STEP if the drag is over us ({@code mHovering} is true) or GREEN_STEP if not. We now
     * multiply {@code color} or'ed with ALPHA_STEP by the index count of our loop (10 down to 1) and
     * set the color of {@code Paint mGlow} to it. We instruct {@code canvas} to draw a circle centered
     * at (cx,cy) of {@code rad} radius using {@code mGlow} as the paint, subtract 0.5 from {@code rad}
     * and have {@code canvas} draw another circle. We subtract another 0.5 from {@code rad} and continue
     * the loop.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        float wf = getWidth();
        float hf = getHeight();
        final float cx = wf / 2;
        final float cy = hf / 2;
        wf -= getPaddingLeft() + getPaddingRight();
        hf -= getPaddingTop() + getPaddingBottom();
        float rad = (wf < hf) ? wf / 2 : hf / 2;
        canvas.drawCircle(cx, cy, rad, mPaint);

        if (mLegend != null && mLegend.length() > 0) {
            canvas.drawText(mLegend, 0, mLegend.length(),
                    cx, cy + mLegendPaint.getFontSpacing() / 2,
                    mLegendPaint);
        }

        // if we're in the middle of a drag, light up as a potential target
        if (mDragInProgress && mAcceptsDrag) {
            for (int i = NUM_GLOW_STEPS; i > 0; i--) {
                int color = (mHovering) ? WHITE_STEP : GREEN_STEP;
                color = i * (color | ALPHA_STEP);
                mGlow.setColor(color);
                canvas.drawCircle(cx, cy, rad, mGlow);
                rad -= 0.5f;
                canvas.drawCircle(cx, cy, rad, mGlow);
                rad -= 0.5f;
            }
        }
    }

    /**
     * Measure the view and its content to determine the measured width and the measured height.
     * We set {@code int totalDiameter} to twice the value of our field {@code mRadius} plus our
     * left padding and right padding, then use {@code totalDiameter} as both the X and Y arguments
     * to {@code setMeasuredDimension}.
     *
     * @param widthSpec  horizontal space requirements as imposed by the parent.
     *                   The requirements are encoded with
     *                   {@link android.view.View.MeasureSpec}.
     * @param heightSpec vertical space requirements as imposed by the parent.
     *                   The requirements are encoded with
     *                   {@link android.view.View.MeasureSpec}.
     */
    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int totalDiameter = 2 * mRadius + getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(totalDiameter, totalDiameter);
    }

    /**
     * Handles drag events sent by the system following a call to {@code startDrag()}. We initialize
     * our variable {@code boolean result} to false, then switch based on the action of our parameter
     * {@code DragEvent event}:
     * <ul>
     * <li>
     * ACTION_DRAG_STARTED - We set our field {@code boolean mDragInProgress} to true, and
     * set our field {@code boolean mAcceptsDrag} and {@code result} to true. Finally we call
     * {@code invalidate} to schedule a redraw so we can "light up as a potential target".
     * Then we break.
     * </li>
     * <li>
     * ACTION_DRAG_ENDED - If {@code mAcceptsDrag} is true we call {@code invalidate} to
     * schedule a redraw so we can "light up as a potential target", then we set our field
     * {@code mDragInProgress} to true and {@code mHovering} to true. Then we break.
     * </li>
     * <li>
     * ACTION_DRAG_LOCATION - We set {@code result} to {@code mAcceptsDrag} and break.
     * </li>
     * <li>
     * ACTION_DROP - If we are configured to ANR when dropped on we call our method
     * {@code sleepSixSeconds} to force an ANR (nasty!), then we call our method
     * {@code processDrop(event)} to process the drop, set {@code result} to true and break.
     * </li>
     * <li>
     * ACTION_DRAG_ENTERED - We set our field {@code mHovering} to true, call {@code invalidate}
     * to schedule a redraw so we can "light up as a WHITE hovered over target", and break.
     * </li>
     * <li>
     * ACTION_DRAG_EXITED - We set our field {@code mHovering} to false, call {@code invalidate}
     * to schedule a redraw so we can "light up as a GREEN potential target", and break.
     * </li>
     * <li>
     * default - We set {@code result} to {@code mAcceptsDrag} and break.
     * </li>
     * </ul>
     * Finally we return {@code result} to the caller.
     *
     * @param event The {@code DragEvent} sent by the system.
     * @return true if the method was successful, otherwise false. The method should return true in
     * response to an action type of ACTION_DRAG_STARTED to receive drag events for the current
     * operation. The method should also return true in response to an action type of ACTION_DROP if
     * it consumed the drop, or false if it didn't.
     */
    @Override
    public boolean onDragEvent(DragEvent event) {
        boolean result = false;
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED: {
                // claim to accept any dragged content
                Log.i(TAG, "Drag started, event=" + event);
                // cache whether we accept the drag to return for LOCATION events
                mDragInProgress = true;
                mAcceptsDrag = result = true;
                // Redraw in the new visual state since we are a potential drop target
                invalidate();
            }
            break;

            case DragEvent.ACTION_DRAG_ENDED: {
                Log.i(TAG, "Drag ended.");
                if (mAcceptsDrag) {
                    invalidate();
                }
                mDragInProgress = false;
                mHovering = false;
            }
            break;

            case DragEvent.ACTION_DRAG_LOCATION: {
                // we returned true to DRAG_STARTED, so return true here
                Log.i(TAG, "... seeing drag locations ...");
                result = mAcceptsDrag;
            }
            break;

            case DragEvent.ACTION_DROP: {
                Log.i(TAG, "Got a drop! dot=" + this + " event=" + event);
                if (mAnrType == ANR_DROP) {
                    sleepSixSeconds();
                }
                processDrop(event);
                result = true;
            }
            break;

            case DragEvent.ACTION_DRAG_ENTERED: {
                Log.i(TAG, "Entered dot @ " + this);
                mHovering = true;
                invalidate();
            }
            break;

            case DragEvent.ACTION_DRAG_EXITED: {
                Log.i(TAG, "Exited dot @ " + this);
                mHovering = false;
                invalidate();
            }
            break;

            default:
                Log.i(TAG, "other drag event: " + event);
                result = mAcceptsDrag;
                break;
        }

        return result;
    }

    private void processDrop(DragEvent event) {
        final ClipData data = event.getClipData();
        final int N = data.getItemCount();
        for (int i = 0; i < N; i++) {
            ClipData.Item item = data.getItemAt(i);
            Log.i(TAG, "Dropped item " + i + " : " + item);
            if (mReportView != null) {
                String text = item.coerceToText(getContext()).toString();
                //noinspection RedundantCast
                if (event.getLocalState() == (Object) this) {
                    text += " : Dropped on self!";
                }
                mReportView.append(text);
            }
        }
    }
}