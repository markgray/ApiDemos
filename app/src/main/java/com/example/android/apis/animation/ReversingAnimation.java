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

        private void createAnimation() {
            if (bounceAnim == null) {
                bounceAnim = ObjectAnimator.ofFloat(ball, "y", ball.getY(), getHeight() - 50f).
                        setDuration(1500);
                bounceAnim.setInterpolator(new AccelerateInterpolator(2f));
                bounceAnim.addUpdateListener(this);
            }
        }

        public void startAnimation() {
            createAnimation();
            bounceAnim.start();
        }

        public void reverseAnimation() {
            createAnimation();
            bounceAnim.reverse();
        }

        @SuppressWarnings("unused")
        public void seek(long seekTime) {
            createAnimation();
            bounceAnim.setCurrentPlayTime(seekTime);
        }

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