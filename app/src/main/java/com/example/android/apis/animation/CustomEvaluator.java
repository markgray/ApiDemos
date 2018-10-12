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

package com.example.android.apis.animation;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.android.apis.R;

/**
 * Implements the TypeEvaluator interface to animate using a custom:
 * "public Object evaluate(float fraction, Object startValue, Object endValue)"
 * function. The x and y coordinates of an "animation.ShapeHolder ball" are
 * animated by calling evaluate, and onAnimationUpdate is called which calls
 * invalidate() which causes the onDraw method to be called.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CustomEvaluator extends Activity {

    /**
     * Logical density of our display.
     */
    float mDensity;
    /**
     * Called when the activity is starting. First we call our super's implementation of {@code onCreate},
     * then we set our content view to our layout file R.layout.animator_custom_evaluator. We initialize
     * our field {@code mDensity} with the logical density of our display, then initialize {@code LinearLayout container}
     * by finding the view with id R.id.container. Next we create a new instance of {@code MyAnimationView} and
     * add it to {@code container}. We initialize {@code Button starter} by finding the view with id
     * R.id.startButton and set its {@code OnClickListener} to an anonymous class which calls the
     * {@code startAnimation} method of {@code animView} to start the animation running.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animator_custom_evaluator);

        mDensity = getResources().getDisplayMetrics().density;

        LinearLayout container = findViewById(R.id.container);
        final MyAnimationView animView = new MyAnimationView(this);
        container.addView(animView);

        Button starter = findViewById(R.id.startButton);
        starter.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when our view has been clicked. We just call the {@code startAnimation} method
             * of {@code animView} to start the animation running.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                animView.startAnimation();
            }
        });
    }

    /**
     * Class used to hold x, y coordinates
     */
    @SuppressWarnings("WeakerAccess")
    public class XYHolder {
        /**
         * X coordinate
         */
        private float mX;
        /**
         * Y coordinate
         */
        private float mY;

        /**
         * Create a XYHolder at x,y
         *
         * @param x coordinate
         * @param y coordinate
         */
        public XYHolder(float x, float y) {
            mX = x;
            mY = y;
        }

        /**
         * Get the x coordinate of this XYHolder
         *
         * @return x coordinate
         */
        public float getX() {
            return mX;
        }

        /**
         * Set the x coordinate of this XYHolder
         *
         * @param x coordinate
         */
        public void setX(float x) {
            mX = x;
        }

        /**
         * Get the y coordinate of this XYHolder
         *
         * @return y coordinate
         */
        public float getY() {
            return mY;
        }

        /**
         * Set the y coordinate of this XYHolder
         *
         * @param y coordinate
         */
        public void setY(float y) {
            mY = y;
        }
    }

    /**
     * Interface for use with the setEvaluator(TypeEvaluator) function. Evaluators allow developers
     * to create animations on arbitrary property types, by allowing them to supply custom
     * evaluators for types that are not automatically understood and used by the animation system.
     */
    public class XYEvaluator implements TypeEvaluator {

        /**
         * This function returns the result of linearly interpolating the start and end values,
         * with fraction representing the proportion between the start and end values.
         * The calculation is a simple parametric calculation: result = x0 + t * (x1 - x0),
         * where x0 is startValue, x1 is endValue, and t is fraction.
         *
         * @param fraction   The fraction from the starting to the ending values
         * @param startValue The start value.
         * @param endValue   The end value.
         * @return A linear interpolation between the start and end values, given the
         *         <b>fraction</b> parameter.
         */
        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            XYHolder startXY = (XYHolder) startValue;
            XYHolder endXY = (XYHolder) endValue;
            return new XYHolder(startXY.getX() + fraction * (endXY.getX() - startXY.getX()),
                    startXY.getY() + fraction * (endXY.getY() - startXY.getY()));
        }
    }

    /**
     * Class designed to hold a ball's ShapeHolder (and not much else)
     */
    @SuppressWarnings("WeakerAccess")
    public class BallXYHolder {

        /**
         * Our {@code ShapeHolder}
         */
        private ShapeHolder mBall;

        /**
         * Creates a BallXYHolder containing the ShapeHolder ball
         *
         * @param ball ShapeHolder for this BallXYHolder
         */
        public BallXYHolder(ShapeHolder ball) {
            mBall = ball;
        }

        /**
         * Sets the x and y coordinates of our {@code ShapeHolder mBall} to the x and y coordinates
         * of our parameter.
         *
         * @param xyHolder {@code XYHolder} to use for the x and y coordinates.
         */
        @SuppressWarnings("unused") // Not true, it is used by ObjectAnimator behind our backs.
        public void setXY(XYHolder xyHolder) {
            mBall.setX(xyHolder.getX());
            mBall.setY(xyHolder.getY());
        }

        /**
         * Constructs and returns a new instance of {@code XYHolder} containing the x and y
         * coordinates of our {@code ShapeHolder mBall}
         *
         * @return {@code XYHolder} holding the x and y coordinates of our {@code ShapeHolder mBall}
         */
        @SuppressWarnings("unused")
        public XYHolder getXY() {
            return new XYHolder(mBall.getX(), mBall.getY());
        }
    }

    /**
     * View which contains our animation
     */
    public class MyAnimationView extends View implements ValueAnimator.AnimatorUpdateListener {

        /**
         * The {@code ValueAnimator} (XYEvaluator TypeEvaluator) we are using to animate our ball.
         */
        ValueAnimator bounceAnim = null;
        /**
         * The {@code ShapeHolder} holding our ball
         */
        ShapeHolder ball;
        /**
         * {@code BallXYHolder} holding the {@code ShapeHolder} holding our ball
         */
        BallXYHolder ballHolder;

        /**
         * Our constructor. First we call our super's constructor, then we call our method
         * {@code createBall} to create a {@code ShapeHolder} holding a ball which is 25px by 25px
         * which we use to initialize our field {@code ShapeHolder ball}. Then we initialize our
         * field {@code BallXYHolder ballHolder} with a new instance holding {@code ball}.
         *
         * @param context Context which in our case is derived from super of Activity
         */
        public MyAnimationView(Context context) {
            super(context);
            ball = createBall(25, 25);
            ballHolder = new BallXYHolder(ball);
        }

        /**
         * If this is the first time the animation has run, a ValueAnimator is created that animates
         * between Object values and saved in our field {@code ValueAnimator bounceAnim}. We initialize
         * {@code XYHolder startXY} with a new instance positioned at (0,0), and {@code XYHolder endXY}
         * with a new instance positioned at (300,500). We then initialize {@code bounceAnim} with an
         * an {@code ObjectAnimator} that animates the "xY" property of {@code BallXYHolder ballHolder}
         * between {@code startXY} and {@code endXY} using a new instance of {@code XYEvaluator} as the
         * {@code TypeEvaluator} that will be called on each animation frame to provide the necessary
         * interpolation between the Object values to derive the animated value. Then we set the duration
         * of the animation to 1500 milliseconds, and add this as an {@code AnimatorUpdateListener}
         * (our {@code onAnimationUpdate} override will be called on every animation frame, after the
         * current frame's values have been calculated)
         */
        private void createAnimation() {
            if (bounceAnim == null) {
                XYHolder startXY = new XYHolder(0f, 0f);
                XYHolder endXY = new XYHolder(300f, 500f);
                bounceAnim = ObjectAnimator.ofObject(ballHolder, "xY",
                        new XYEvaluator(), startXY, endXY);
                bounceAnim.setDuration(1500);
                bounceAnim.addUpdateListener(this);
            }
        }

        /**
         * Called when the PLAY button is clicked, it first creates the animation (if this is the
         * first time the button is clicked), and then starts the animation running.
         */
        public void startAnimation() {
            createAnimation();
            bounceAnim.start();
        }

        /**
         * Creates a ball at coordinates x, y. The ball is constructed of an OvalShape resized
         * to 50px x 50px, placed in a ShapeDrawable and that ShapeDrawable is used in creating
         * a ShapeHolder to hold it. The ShapeHolder has its x and y coordinates set to the
         * method's arguments x,y. Random colors and a RadialGradient are used to initialize a
         * Paint and that Paint is stored in the ShapeHolder. Finally we return that
         * {@code ShapeHolder} to the caller.
         *
         * @param x x coordinate for ball
         * @param y y coordinate for ball
         * @return ShapeHolder containing the new ball
         */
        @SuppressWarnings("SameParameterValue")
        private ShapeHolder createBall(float x, float y) {
            OvalShape circle = new OvalShape();
            circle.resize(50f, 50f);
            ShapeDrawable drawable = new ShapeDrawable(circle);
            ShapeHolder shapeHolder = new ShapeHolder(drawable);
            shapeHolder.setX(x - 25f);
            shapeHolder.setY(y - 25f);
            int red = (int)(Math.random() * 255);
            int green = (int)(Math.random() * 255);
            int blue = (int)(Math.random() * 255);
            int color = 0xff000000 | red << 16 | green << 8 | blue;
            Paint paint = drawable.getPaint(); //new Paint(Paint.ANTI_ALIAS_FLAG);
            int darkColor = 0xff000000 | red/4 << 16 | green/4 << 8 | blue/4;
            RadialGradient gradient = new RadialGradient(37.5f, 12.5f,
                    50f, color, darkColor, Shader.TileMode.CLAMP);
            paint.setShader(gradient);
            shapeHolder.setPaint(paint);
            return shapeHolder;
        }

        /**
         * This callback draws the MyAnimationView after every invalidate() call. The current
         * matrix and clip are saved onto a private stack, the current matrix is scaled by the
         * screen density {@code mDensity}, then the current matrix is pre-concatenated with a
         * translation to the coordinate x, y of the ball's ShapeHolder, and the ShapeDrawable
         * in the ShapeHolder is told to draw itself. Canvas.restore() then removes all
         * modifications to the matrix/clip state.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.save();
            canvas.scale(mDensity, mDensity);
            canvas.translate(ball.getX(), ball.getY());
            ball.getShape().draw(canvas);
            canvas.restore();
        }

        /**
         * This callback is called on the occurrence of another frame of an animation which has
         * had addUpdateListener(this) called to add "this" as a listener to the set of listeners
         * that are sent update events throughout the life of an animation. This method is called
         * on all listeners for every frame of the animation, after the values for the animation
         * have been calculated. It simply calls invalidate() to invalidate the whole view.
         * If the view is visible, onDraw(android.graphics.Canvas) will be called at some point
         * in the future.
         *
         * @param animation The animation which has a new frame
         */
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            invalidate();
        }

    }
}