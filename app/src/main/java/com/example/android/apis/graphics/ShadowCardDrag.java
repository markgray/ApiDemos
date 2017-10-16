/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.android.apis.R;

import java.util.ArrayList;

/**
 * Shows "material design" effects of simple draggable shapes that generate a shadow casting outline
 * on touching the screen.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ShadowCardDrag extends Activity {
    /**
     * Maximum Z value for animation of the android:translationZ attribute of the draggable card.
     */
    private static final float MAX_Z_DP = 10;
    /**
     * Scale used to scale the "momentum" in X and Y direction when determining how much to tilt the
     * card (only when the "Enable Tilt" checkbox is checked)
     */
    private static final float MOMENTUM_SCALE = 10;
    /**
     * Maximum angle for the tilt of the card (used only when the "Enable Tilt" checkbox is checked)
     */
    private static final int MAX_ANGLE = 10;
    /**
     * {@code ShapeDrawable} presently being used for the draggable, it is cycled by the R.id.shape_select
     * {@code Button} to be one of {@code RectShape}, {@code OvalShape}, {@code RoundRectShape} and
     * {@code TriangleShape} which are contained in {@code ArrayList<Shape> mShapes}.
     */
    private final ShapeDrawable mCardBackground = new ShapeDrawable();
    /**
     * List containing the different {@code Shape} types which our draggable card can be set to, it
     * is filled in our {@code onCreate} method with {@code RectShape}, {@code OvalShape},
     * {@code RoundRectShape} and {@code TriangleShape}, and cycled through when the R.id.shape_select
     * "Select Shape" {@code Button} is pressed.
     */
    private final ArrayList<Shape> mShapes = new ArrayList<>();
    /**
     * The logical density of the display. This is a scaling factor for the Density Independent Pixel
     * unit, where one DIP is one pixel on an approximately 160 dpi screen (for example a 240x320,
     * 1.5"x2" screen), providing the baseline of the system's display. Thus on a 160dpi screen this
     * density value will be 1; on a 120 dpi screen it would be .75; etc. It is retrieved from the
     * current display metrics of the packages resources in our {@code onCreate} method, and used
     * whenever it is necessary to scale DPI measurements to pixels.
     */
    private float mDensity;
    /**
     * Our draggable card, ID R.id.card in our layout file R.layout.shadow_card_drag.
     */
    private View mCard;

    /**
     * Class which handles tilting and/or shading when those checkboxes are checked.
     */
    private final CardDragState mDragState = new CardDragState();
    /**
     * true if the "Enable Tilt" checkbox is checked. The card will be "tilted" in proportion to the
     * velocity of movement.
     */
    private boolean mTiltEnabled;
    /**
     * true if the "Enable Shading checkbox is checked. If so, a color filter will be applied to the
     * card background darkening it in proportion to the velocity of movement.
     */
    private boolean mShadingEnabled;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.shadow_card_drag.
     * We initialize our field {@code mDensity} with the logical density of the display. We call our
     * method {@code initShapes} to fill our field {@code ArrayList<Shape> mShapes} with four
     * different {@code ShapeDrawable} objects: a {@code RectShape}, an {@code OvalShape}, a
     * {@code RoundRectShape}, and a {@code TriangleShape}. We fetch the {@code Paint} used to draw
     * our field {@code ShapeDrawable mCardBackground} and set its color to WHITE, then set its
     * background to the first {@code Shape} in {@code mShapes} (a {@code RectShape}). We locate the
     * {@code TextView} in our layout with id R.id.card and set its background to {@code mCardBackground}.
     * We next initialize our "Enable Tilt" checkbox, our "Enable Shading" checkbox, and our "Select
     * Shape" button. Finally we initialize an {@code OnTouchListener} for our entire {@code FrameLayout}
     * (id R.id.card_parent) to allow us to move and animate the card according to the touch events
     * received.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shadow_card_drag);

        mDensity = getResources().getDisplayMetrics().density;

        initShapes();
        mCardBackground.getPaint().setColor(Color.WHITE);
        mCardBackground.setShape(mShapes.get(0));
        mCard = findViewById(R.id.card);
        mCard.setBackground(mCardBackground);

        initTiltEnable();
        initShadingEnable();
        initShapeButton();
        initTouchListener();
    }

    /**
     * Initializes the {@code OnTouchListener} of the {@code FrameLayout} to allow the user to move
     * the card with his finger. First we set {@code View cardParent} to the view in our layout with
     * id {@code R.id.card_parent} (the {@code FrameLayout} holding our entire UI). Then we set its
     * {@code OnTouchListener} to an anonymous class which will translate the {@code MotionEvent}
     * received into movement of the card.
     */
    private void initTouchListener() {
    /*
     * Enable any touch on the parent to drag the card. Note that this doesn't do a proper hit
     * test, so any drag (including off of the card) will work.
     *
     * This enables the user to see the effect more clearly for the purpose of this demo.
     */
        final View cardParent = findViewById(R.id.card_parent);

        cardParent.setOnTouchListener(new View.OnTouchListener() {
            /**
             * Distance in the X direction of the last ACTION_DOWN {@code MotionEvent} received with
             * respect to the current X coordinate of the card. Used to translate the X coordinate
             * of future ACTION_MOVE events to a new location for the card.
             */
            float downX;
            /**
             * Distance in the Y direction of the last ACTION_DOWN {@code MotionEvent} received with
             * respect to the current Y coordinate of the card. Used to translate the Y coordinate
             * of future ACTION_MOVE events to a new location for the card.
             */
            float downY;
            /**
             * The time (in ms) when the user originally pressed down to start a stream of position
             * events. Set when an ACTION_DOWN event is received, but never used.
             */
            long downTime;

            /**
             * Called when a touch event is dispatched to our view. We switch based on the kind of
             * action of the {@code MotionEvent event}:
             * <ul>
             *     <li>
             *         ACTION_DOWN - First we calculate how far the event coordinates are from the
             *         current location of the card, setting {@code downX} and {@code downY} so that
             *         they may be used to calculate where to move the card when future ACTION_MOVE
             *         events are received. Then we create an {@code ObjectAnimator upAnim} to animate
             *         the property name "translationZ" of {@code View mCard}, set its duration to
             *         100 milliseconds, its interpolator to a new instance of {@code DecelerateInterpolator},
             *         and then start it running. If the flag {@code mTiltEnabled} is true, we call the
             *         {@code onDown} method of {@code CardDragState mDragState} to initialize it for
             *         a "tilting" animation of the card.
             *     </li>
             *     <li>
             *         ACTION_MOVE - 
             *     </li>
             * </ul>
             *
             * @param v The view the touch event has been dispatched to.
             * @param event The MotionEvent object containing full information about the event.
             * @return True if the listener has consumed the event, false otherwise. We always return
             * true.
             */
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX() - mCard.getTranslationX();
                        downY = event.getY() - mCard.getTranslationY();
                        downTime = event.getDownTime();
                        ObjectAnimator upAnim = ObjectAnimator.ofFloat(mCard, "translationZ", MAX_Z_DP * mDensity);
                        upAnim.setDuration(100);
                        upAnim.setInterpolator(new DecelerateInterpolator());
                        upAnim.start();
                        if (mTiltEnabled) {
                            mDragState.onDown(event.getDownTime(), event.getX(), event.getY());
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mCard.setTranslationX(event.getX() - downX);
                        mCard.setTranslationY(event.getY() - downY);
                        if (mTiltEnabled) {
                            mDragState.onMove(event.getEventTime(), event.getX(), event.getY());
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        ObjectAnimator downAnim = ObjectAnimator.ofFloat(mCard, "translationZ", 0);
                        downAnim.setDuration(100);
                        downAnim.setInterpolator(new AccelerateInterpolator());
                        downAnim.start();
                        if (mTiltEnabled) {
                            mDragState.onUp();
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void initShapeButton() {
        final Button shapeButton = (Button) findViewById(R.id.shape_select);
        shapeButton.setOnClickListener(new View.OnClickListener() {
            int index = 0;

            @Override
            public void onClick(View v) {
                index = (index + 1) % mShapes.size();
                mCardBackground.setShape(mShapes.get(index));
            }
        });
    }

    private void initShadingEnable() {
        final CheckBox shadingCheck = (CheckBox) findViewById(R.id.shading_check);
        shadingCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mShadingEnabled = isChecked;
                if (!mShadingEnabled) {
                    mCardBackground.setColorFilter(null);
                }
            }
        });
    }

    private void initTiltEnable() {
        final CheckBox tiltCheck = (CheckBox) findViewById(R.id.tilt_check);
        tiltCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTiltEnabled = isChecked;
                if (!mTiltEnabled) {
                    mDragState.onUp();
                }
            }
        });
    }

    private void initShapes() {
        mShapes.add(new RectShape());
        mShapes.add(new OvalShape());
        float r = 10 * mDensity;
        float radii[] = new float[]{r, r, r, r, r, r, r, r};
        mShapes.add(new RoundRectShape(radii, null, null));
        mShapes.add(new TriangleShape());
    }

    @SuppressWarnings("WeakerAccess")
    private class CardDragState {
        long lastEventTime;
        float lastX;
        float lastY;

        float momentumX;
        float momentumY;

        public void onDown(long eventTime, float x, float y) {
            lastEventTime = eventTime;
            lastX = x;
            lastY = y;

            momentumX = 0;
            momentumY = 0;
        }

        public void onMove(long eventTime, float x, float y) {
            final long deltaT = eventTime - lastEventTime;

            if (deltaT != 0) {
                float newMomentumX = (x - lastX) / (mDensity * deltaT);
                float newMomentumY = (y - lastY) / (mDensity * deltaT);

                momentumX = 0.9f * momentumX + 0.1f * (newMomentumX * MOMENTUM_SCALE);
                momentumY = 0.9f * momentumY + 0.1f * (newMomentumY * MOMENTUM_SCALE);

                momentumX = Math.max(Math.min((momentumX), MAX_ANGLE), -MAX_ANGLE);
                momentumY = Math.max(Math.min((momentumY), MAX_ANGLE), -MAX_ANGLE);

                //noinspection SuspiciousNameCombination
                mCard.setRotationX(-momentumY);
                //noinspection SuspiciousNameCombination
                mCard.setRotationY(momentumX);

                if (mShadingEnabled) {
                    float alphaDarkening = (momentumX * momentumX + momentumY * momentumY) / (90 * 90);
                    alphaDarkening /= 2;

                    int alphaByte = 0xff - ((int) (alphaDarkening * 255) & 0xff);
                    int color = Color.rgb(alphaByte, alphaByte, alphaByte);
                    mCardBackground.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                }
            }

            lastX = x;
            lastY = y;
            lastEventTime = eventTime;
        }

        public void onUp() {
            ObjectAnimator flattenX = ObjectAnimator.ofFloat(mCard, "rotationX", 0);
            flattenX.setDuration(100);
            flattenX.setInterpolator(new AccelerateInterpolator());
            flattenX.start();

            ObjectAnimator flattenY = ObjectAnimator.ofFloat(mCard, "rotationY", 0);
            flattenY.setDuration(100);
            flattenY.setInterpolator(new AccelerateInterpolator());
            flattenY.start();
            mCardBackground.setColorFilter(null);
        }
    }

    /**
     * Simple shape example that generates a shadow casting outline.
     */
    private static class TriangleShape extends Shape {
        private final Path mPath = new Path();

        @Override
        protected void onResize(float width, float height) {
            mPath.reset();
            mPath.moveTo(0, 0);
            mPath.lineTo(width, 0);
            mPath.lineTo(width / 2, height);
            mPath.lineTo(0, 0);
            mPath.close();
        }

        @Override
        public void draw(Canvas canvas, Paint paint) {
            canvas.drawPath(mPath, paint);
        }

        @Override
        public void getOutline(@SuppressWarnings("NullableProblems") Outline outline) {
            outline.setConvexPath(mPath);
        }
    }
}
