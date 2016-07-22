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
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.widget.CheckBox;
import android.widget.TextView;
import com.example.android.apis.R;

import java.util.ArrayList;

import android.animation.ValueAnimator;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * This demo shows how the AnimatorListener events work.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AnimatorEvents extends Activity {

    TextView startText, repeatText, cancelText, endText;
    TextView startTextAnimator, repeatTextAnimator, cancelTextAnimator, endTextAnimator;

    /**
     * Sets the content view to animator_events layout, and addView's an instance of MyAnimationView
     * to the layout. It then locates the TextView's used as output (start, repeat, cancel, and end
     * for both Sequencer and Animator events) and applies setAlpha(.5f) to make them appear as not
     * having occurred, the AnimatorListener callbacks will apply setAlpha(1.0f) to signify that
     * the callback has been called. It then locates the control buttons (PLAY, CANCEL, END) and
     * sets OnClickListener's to perform these actions
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animator_events);
        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        final MyAnimationView animView = new MyAnimationView(this);
        container.addView(animView);
        startText = (TextView) findViewById(R.id.startText);
        startText.setAlpha(.5f);
        repeatText = (TextView) findViewById(R.id.repeatText);
        repeatText.setAlpha(.5f);
        cancelText = (TextView) findViewById(R.id.cancelText);
        cancelText.setAlpha(.5f);
        endText = (TextView) findViewById(R.id.endText);
        endText.setAlpha(.5f);
        startTextAnimator = (TextView) findViewById(R.id.startTextAnimator);
        startTextAnimator.setAlpha(.5f);
        repeatTextAnimator = (TextView) findViewById(R.id.repeatTextAnimator);
        repeatTextAnimator.setAlpha(.5f);
        cancelTextAnimator = (TextView) findViewById(R.id.cancelTextAnimator);
        cancelTextAnimator.setAlpha(.5f);
        endTextAnimator = (TextView) findViewById(R.id.endTextAnimator);
        endTextAnimator.setAlpha(.5f);
        final CheckBox endCB = (CheckBox) findViewById(R.id.endCB);

        Button starter = (Button) findViewById(R.id.startButton);
        starter.setOnClickListener(new View.OnClickListener() {
            /**
             * Start the animation of MyAnimationView, passing the state of the end immediately
             * checkbox as the parameter.
             *
             * @param v starter Button view which was clicked
             */
            @Override
            public void onClick(View v) {
                animView.startAnimation(endCB.isChecked());
            }
        });

        Button canceler = (Button) findViewById(R.id.cancelButton);
        canceler.setOnClickListener(new View.OnClickListener() {
            /**
             * Cancel the animation of MyAnimationView
             *
             * @param v cancel Button which was clicked
             */
            @Override
            public void onClick(View v) {
                animView.cancelAnimation();
            }
        });

        Button ender = (Button) findViewById(R.id.endButton);
        ender.setOnClickListener(new View.OnClickListener() {
            /**
             * End the animation of MyAnimationView
             *
             * @param v end button which was clicked
             */
            @Override
            public void onClick(View v) {
                animView.endAnimation();
            }
        });

    }

    /**
     * This is the view containing our animated ball, which listens for notifications from an
     * animation by implementing Animator.AnimatorListener. Notifications indicate animation
     * related events, such as the end or the repetition of the animation. It also implements
     * ValueAnimator.AnimatorUpdateListener receiving callbacks for every frame of the animation.
     */
    public class MyAnimationView extends View implements Animator.AnimatorListener,
            ValueAnimator.AnimatorUpdateListener {

        @SuppressWarnings("unused")
        public final ArrayList<ShapeHolder> balls = new ArrayList<>();
        Animator animation;
        ShapeHolder ball = null;
        boolean endImmediately = false;

        /**
         * Initializes ball to be a 25px by 25px circle. See createBall(float, float)
         *
         * @param context context of the Application
         */
        public MyAnimationView(Context context) {
            super(context);
            ball = createBall(25, 25);
        }

        /**
         * Creates the "Animator animation" for the "ShapeHolder ball". yAnim animates the "y"
         * coordinate of the ball ShapeHolder from the current position to the bottom minus 50px
         * with a duration of 1500 milliseconds, with a repeat count of 2, a repeatMode of
         * ValueAnimator.REVERSE using a factor of 2.0 AccelerateInterpolator (starts slow and
         * ends fast). xAnim animates the "x" coordinate of the ball from the current position
         * to the current position plus 300px with a duration of 1000 milliseconds, with a repeat
         * count of 2, a repeatMode of ValueAnimator.REVERSE using a factor of 2.0
         * AccelerateInterpolator (starts slow and ends fast). alphaAnim and alphaSeq are strange
         * in that they are played during the creation of the "AnimatorSet animation" and do not
         * participate with the event demonstration. Together they animate the alpha of the
         * ShapeHolder ball from 1f to .5f with a duration of 1000 milliseconds. The finish of the
         * process is to create the AnimatorSet animation, and use it to .playTogether yAnim, and
         * xAnim. yAnim has its addUpdateListener set to "this" (causing this.onAnimationUpdate
         * to be called and invalidate() the View) and addListener set to "this" (causing
         * onAnimationStart, onAnimationEnd, onAnimationCancel, and onAnimationRepeat to be
         * called when these events occur) and animation also has its addListener set to this
         * (causing the same event callbacks to called).
         */
        private void createAnimation() {
            if (animation == null) {
                ObjectAnimator yAnim = ObjectAnimator.ofFloat(ball, "y",
                        ball.getY(), getHeight() - 50f).setDuration(1500);
                yAnim.setRepeatCount(2);
                yAnim.setRepeatMode(ValueAnimator.REVERSE);
                yAnim.setInterpolator(new AccelerateInterpolator(2f));
                yAnim.addUpdateListener(this);
                yAnim.addListener(this);

                ObjectAnimator xAnim = ObjectAnimator.ofFloat(ball, "x",
                        ball.getX(), ball.getX() + 300).setDuration(1000);
                xAnim.setStartDelay(0);
                xAnim.setRepeatCount(2);
                xAnim.setRepeatMode(ValueAnimator.REVERSE);
                xAnim.setInterpolator(new AccelerateInterpolator(2f));

                ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(ball, "alpha", 1f, .5f).
                        setDuration(1000);
                AnimatorSet alphaSeq = new AnimatorSet();
                alphaSeq.play(alphaAnim);

                animation = new AnimatorSet();
                ((AnimatorSet) animation).playTogether(yAnim, xAnim);
                animation.addListener(this);
            }
        }

        public void startAnimation(boolean endImmediately) {
            this.endImmediately = endImmediately;
            startText.setAlpha(.5f);
            repeatText.setAlpha(.5f);
            cancelText.setAlpha(.5f);
            endText.setAlpha(.5f);
            startTextAnimator.setAlpha(.5f);
            repeatTextAnimator.setAlpha(.5f);
            cancelTextAnimator.setAlpha(.5f);
            endTextAnimator.setAlpha(.5f);
            createAnimation();
            animation.start();
        }

        public void cancelAnimation() {
            createAnimation();
            animation.cancel();
        }

        public void endAnimation() {
            createAnimation();
            animation.end();
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

        @Override
        public void onAnimationStart(Animator animation) {
            if (animation instanceof AnimatorSet) {
                startText.setAlpha(1f);
            } else {
                startTextAnimator.setAlpha(1f);
            }
            if (endImmediately) {
                animation.end();
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (animation instanceof AnimatorSet) {
                endText.setAlpha(1f);
            } else {
                endTextAnimator.setAlpha(1f);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            if (animation instanceof AnimatorSet) {
                cancelText.setAlpha(1f);
            } else {
                cancelTextAnimator.setAlpha(1f);
            }
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            if (animation instanceof AnimatorSet) {
                repeatText.setAlpha(1f);
            } else {
                repeatTextAnimator.setAlpha(1f);
            }
        }
    }
}