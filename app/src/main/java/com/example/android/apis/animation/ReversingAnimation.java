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
import android.animation.ValueAnimator;
import com.example.android.apis.R;

import java.util.ArrayList;

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
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Demonstrates the use of android.animation.ValueAnimator.reverse() method to play an
 * animation in "reverse".
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ReversingAnimation extends Activity {

    /**
     * Called when the activity is starting. First we call through to the super class's
     * implementation of this method, then we set our content view to our layout file
     * R.layout.animation_reversing. We locate the LinearLayout container we will use
     * for our demo View (R.id.container) and addView an instance of our custom view
     * MyAnimationView. We locate our "Play" Button (R.id.startButton) and assign an
     * OnClickListener which will start our animation playing. We locate our "Reverse"
     * Button (R.id.reverseButton) and assign an OnClickListener which will call
     * MyAnimationView.reverseAnimation to play the animation in reverse (whether it has
     * run before to the end or not).
     *
     * @param savedInstanceState always null since onSaveInstanceState is not called
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animation_reversing);
        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        final MyAnimationView animView = new MyAnimationView(this);
        container.addView(animView);

        Button starter = (Button) findViewById(R.id.startButton);
        starter.setOnClickListener(new View.OnClickListener() {
            /**
             * Starts our MyAnimationView animation running
             * (creating it first if necessary)
             *
             * @param v Button View which was clicked
             */
            @Override
            public void onClick(View v) {
                animView.startAnimation();
            }
        });

        Button reverser = (Button) findViewById(R.id.reverseButton);
        reverser.setOnClickListener(new View.OnClickListener() {
            /**
             * Starts our MyAnimationView animation running in reverse
             * (creating it first if necessary)
             *
             * @param v Button View which was clicked
             */
            @Override
            public void onClick(View v) {
                animView.reverseAnimation();
            }
        });

    }

    /**
     * This custom View consists of a ball which has an AccelerateInterpolator to animate it.
     * If MyAnimationView.startAnimation is called it falls from the top to the bottom of the View
     * and stays there. If MyAnimationView.reverseAnimation is called it "falls" from the bottom
     * to the top of the View (no matter where it starts) and stays there.
     */
    public class MyAnimationView extends View implements ValueAnimator.AnimatorUpdateListener {

        @SuppressWarnings("unused")
        public final ArrayList<ShapeHolder> balls = new ArrayList<>();
        ValueAnimator bounceAnim = null;
        ShapeHolder ball = null;

        /**
         * Initializes a new instance of MyAnimationView. First calls our super's constructor,
         * then creates a ShapeHolder ball containing a 25px by 25px ball.
         *
         * @param context ReversingAnimation Activity Context
         */
        public MyAnimationView(Context context) {
            super(context);
            ball = createBall(25, 25);
        }

        /**
         * If the ValueAnimator bounceAnim has not already been created, we create an ObjectAnimator
         * that animates "y" from the current position to 50px from the bottom with a duration of
         * 1500 milliseconds, and set the interpolator used to an AccelerateInterpolator with a
         * factor of 2.0. We set the ValueAnimator.AnimatorUpdateListener to "this" so that our
         * callback override of onAnimationUpdate is called for every frame of the animation.
         * (It just invalidates the View causing it to be redrawn every frame.)
         */
        private void createAnimation() {
            if (bounceAnim == null) {
                bounceAnim = ObjectAnimator.ofFloat(ball, "y", ball.getY(), getHeight() - 50f).
                        setDuration(1500);
                bounceAnim.setInterpolator(new AccelerateInterpolator(2f));
                bounceAnim.addUpdateListener(this);
            }
        }

        /**
         * Create the animation bounceAnim (if necessary) and start it running.
         */
        public void startAnimation() {
            createAnimation();
            bounceAnim.start();
        }

        /**
         * Create the animation bounceAnim (if necessary) and play the ValueAnimator in reverse.
         * If the animation is already running, it will stop itself and play backwards from the
         * point reached when reverse was called. If the animation is not currently running,
         * then it will start from the end and play backwards. This behavior is only set for
         * the current animation; future playing of the animation will use the default behavior
         * of playing forward.
         */
        public void reverseAnimation() {
            createAnimation();
            bounceAnim.reverse();
        }

        /**
         * Although unused, this method will create the animation bounceAnim (if necessary), and
         * set the position of the animation to the specified point in time. This time should be
         * between 0 and the total duration of the animation, including any repetition. If the
         * animation has not yet been started, then it will not advance forward after it is set
         * to this time; it will simply set the time to this value and perform any appropriate
         * actions based on that time. If the animation is already running, then setCurrentPlayTime()
         * will set the current playing time to this value and continue playing from that point.
         *
         * @param seekTime The time, in milliseconds, to which the animation is advanced or rewound.
         */
        @SuppressWarnings("unused")
        public void seek(long seekTime) {
            createAnimation();
            bounceAnim.setCurrentPlayTime(seekTime);
        }

        /**
         * Creates and returns a ShapeHolder holding a "ball". First it creates an OvalShape circle,
         * re-sizes it to be a 50px by 50px circle, creates a ShapeDrawable drawable from it, and
         * places it in a ShapeHolder shapeHolder. It sets the (x,y) coordinates of the ShapeHolder
         * to the calling parameters of the method, generates a random color and a dark version of
         * that color and creates a RadialGradient gradient from them which it sets as the shader
         * of the paint which it assigns to the ShapeHolder. It then returns the ShapeHolder it has
         * created and initialized.
         *
         * @param x x coordinate of ball's ShapeHolder (offset by 25px)
         * @param y y coordinate of ball's ShapeHolder (offset by 25px)
         * @return ShapeHolder containing a ball
         */
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

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.save();
            canvas.translate(ball.getX(), ball.getY());
            ball.getShape().draw(canvas);
            canvas.restore();
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            invalidate();
        }

    }
}