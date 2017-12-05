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
     *
     */
    int mRadius;
    int mAnrType;
    CharSequence mLegend;

    @SuppressWarnings("unused")
    static final int ANR_NONE = 0;
    static final int ANR_SHADOW = 1;
    static final int ANR_DROP = 2;

    void sleepSixSeconds() {
        // hang forever; good for producing ANRs
/*
        long start = SystemClock.uptimeMillis();
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.i(TAG, e.getLocalizedMessage());
            }
        } while (SystemClock.uptimeMillis() < start + 6000);
*/
    }

    // Shadow builder that can ANR if desired
    class ANRShadowBuilder extends DragShadowBuilder {
        boolean mDoAnr;

        @SuppressWarnings("WeakerAccess")
        public ANRShadowBuilder(View view, boolean doAnr) {
            super(view);
            mDoAnr = doAnr;
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            if (mDoAnr) {
                sleepSixSeconds();
            }
            super.onDrawShadow(canvas);
        }
    }

    public DraggableDot(Context context, AttributeSet attrs) {
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
        @SuppressLint("Recycle")
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.DraggableDot);

        final int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
            case R.styleable.DraggableDot_radius: {
                mRadius = a.getDimensionPixelSize(attr, 0);
            } break;

            case R.styleable.DraggableDot_legend: {
                mLegend = a.getText(attr);
            } break;

            case R.styleable.DraggableDot_anr: {
                mAnrType = a.getInt(attr, 0);
            } break;
            }
        }

        Log.i(TAG, "DraggableDot @ " + this + " : radius=" + mRadius + " legend='" + mLegend
                + "' anr=" + mAnrType);

        setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                switch (mAnrType) {
                    case ANR_SHADOW: {
                        mReportView.setText("I am going to ANR_SHADOW\n");
                        break;
                    }
                    case ANR_DROP: {
                        mReportView.setText("I am going to ANR_DROP\n");
                        break;
                    }
                    default: {
                        mReportView.setText("");
                    }
                }
                ClipData data = ClipData.newPlainText("dot", "Dot : " + v.toString());
                //noinspection RedundantCast
                v.startDrag(data, new ANRShadowBuilder(v, mAnrType == ANR_SHADOW), (Object)v, 0);
                return true;
            }
        });
    }

    void setReportView(TextView view) {
        mReportView = view;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float wf = getWidth();
        float hf = getHeight();
        final float cx = wf/2;
        final float cy = hf/2;
        wf -= getPaddingLeft() + getPaddingRight();
        hf -= getPaddingTop() + getPaddingBottom();
        float rad = (wf < hf) ? wf/2 : hf/2;
        canvas.drawCircle(cx, cy, rad, mPaint);

        if (mLegend != null && mLegend.length() > 0) {
            canvas.drawText(mLegend, 0, mLegend.length(),
                    cx, cy + mLegendPaint.getFontSpacing()/2,
                    mLegendPaint);
        }

        // if we're in the middle of a drag, light up as a potential target
        if (mDragInProgress && mAcceptsDrag) {
            for (int i = NUM_GLOW_STEPS; i > 0; i--) {
                int color = (mHovering) ? WHITE_STEP : GREEN_STEP;
                color = i*(color | ALPHA_STEP);
                mGlow.setColor(color);
                canvas.drawCircle(cx, cy, rad, mGlow);
                rad -= 0.5f;
                canvas.drawCircle(cx, cy, rad, mGlow);
                rad -= 0.5f;
            }
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int totalDiameter = 2*mRadius + getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(totalDiameter, totalDiameter);
    }

    /**
     * Drag and drop
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
            // Redraw in the new visual state if we are a potential drop target
            //noinspection ConstantConditions
            if (mAcceptsDrag) {
                invalidate();
            }
        } break;

        case DragEvent.ACTION_DRAG_ENDED: {
            Log.i(TAG, "Drag ended.");
            if (mAcceptsDrag) {
                invalidate();
            }
            mDragInProgress = false;
            mHovering = false;
        } break;

        case DragEvent.ACTION_DRAG_LOCATION: {
            // we returned true to DRAG_STARTED, so return true here
            Log.i(TAG, "... seeing drag locations ...");
            result = mAcceptsDrag;
        } break;

        case DragEvent.ACTION_DROP: {
            Log.i(TAG, "Got a drop! dot=" + this + " event=" + event);
            if (mAnrType == ANR_DROP) {
                sleepSixSeconds();
            }
            processDrop(event);
            result = true;
        } break;

        case DragEvent.ACTION_DRAG_ENTERED: {
            Log.i(TAG, "Entered dot @ " + this);
            mHovering = true;
            invalidate();
        } break;

        case DragEvent.ACTION_DRAG_EXITED: {
            Log.i(TAG, "Exited dot @ " + this);
            mHovering = false;
            invalidate();
        } break;

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