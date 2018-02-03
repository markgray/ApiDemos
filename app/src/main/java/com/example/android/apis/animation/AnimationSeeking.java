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
import com.example.android.apis.R;

import java.util.ArrayList;

import android.animation.ValueAnimator;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
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
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;

/**
 * This application demonstrates the seeking capability of ValueAnimator. The SeekBar in the
 * UI allows you to set the position of the animation. Pressing the Run button will play from
 * the current position of the animation.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AnimationSeeking extends Activity {
    /**
     * Maximum value to use for our {@code SeekBar}.
     */
    private static final int DURATION = 1500;
    /**
     * TAG used for logging.
     */
    private static final String TAG = "AnimationSeeking";
    /**
     * The {@code SeekBar} in our layout with id R.id.seekBar used by the user to adjust the position
     * of the animation.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private SeekBar mSeekBar;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.animation_seeking.
     * We initialize {@code LinearLayout container} by finding the view with the id R.id.container,
     * initialize {@code MyAnimationView animView} with a new instance, and add it to our
     * {@code LinearLayout container}. We initialize {@code Button starter} by finding the view with
     * id R.id.startButton ("Run") and set its {@code OnClickListener} to an anonymous class which
     * calls the {@code startAnimation()} method of the {@code MyAnimationView animView} when the
     * button is clicked. We initialize our field {@code SeekBar mSeekBar} by finding the view with
     * id R.id.seekBar, set its maximum value to {@code DURATION} (1500), and set its
     * {@code OnSeekBarChangeListener} to an anonymous class whose {@code onProgressChanged} override
     * calls the {@code seek} method of {@code MyAnimationView animView} to set the animation to the
     * time indicated by the position of the seekbar (0-1500ms)whenever the user changes the setting.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animation_seeking);

        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        final MyAnimationView animView = new MyAnimationView(this);
        container.addView(animView);

        Button starter = (Button) findViewById(R.id.startButton);
        starter.setOnClickListener(new View.OnClickListener() {
            /**
             * Starts the animation when Button is clicked by calling the {@code startAnimation} method
             * of {@code MyAnimationView animView}.
             *
             * @param v RUN Button View that was clicked
             */
            @Override
            public void onClick(View v) {
                animView.startAnimation();
            }
        });

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setMax(DURATION);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * Notification that the user has finished a touch gesture. Just overridden because
             * the interface is abstract.
             *
             * @param seekBar The SeekBar in which the touch gesture began.
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            /**
             * Notification that the user has started a touch gesture. Just overridden because
             * the interface is abstract.
             *
             * @param seekBar The SeekBar in which the touch gesture began,
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            /**
             * Notification that the progress level has changed. We first check to see if
             * MyAnimationView animView has been drawn yet (otherwise we are seeking too
             * soon), if it has been then we call the method MyAnimationView.seek to position
             * the animation at the setting of the SeekBar.
             *
             * @param seekBar The SeekBar whose progress has changed
             * @param progress The current progress level. This will be in the range 0..DURATION
             *        where DURATION was set by ProgressBar.setMax(DURATION) above.
             * @param fromUser True if the progress change was initiated by the user.
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // prevent seeking on app creation
                if (animView.getHeight() != 0) {
                    animView.seek(progress);
                }
            }
        });
    }

    /**
     * This is the custom View for our demo. It holds a "ball" with an animation attached to it
     * which causes the ball to fall from the top of the View to the bottom and bounce at the
     * bottom.
     */
    @SuppressWarnings("unused")
    public class MyAnimationView extends View implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
        /**
         * A pinkish red shade
         */
        private static final int RED = 0xffFF8080;
        /**
         * A dirty blue shade
         */
        private static final int BLUE = 0xff8080FF;
        /**
         * A turquoise shade of blue/ green
         */
        private static final int CYAN = 0xff80ffff;
        /**
         * Puke green
         */
        private static final int GREEN = 0xff80ff80;
        private static final float BALL_SIZE = 100f;

        public final ArrayList<ShapeHolder> balls = new ArrayList<>();
        AnimatorSet animation = null;
        ValueAnimator bounceAnim = null;
        ShapeHolder ball = null;

        /**
         * Call through to the super's constructor, then create a ShapeHolder ball located at (200,0)
         *
         * @param context AnimationSeeking Activity context
         */
        public MyAnimationView(Context context) {
            super(context);
            ball = addBall(200, 0);
        }

        /**
         * Creates an ValueAnimator bounceAnim iff it is null at the moment. bounceAnim is an
         * ObjectAnimator that animates the ShapeHolder ball's "y" field between float values
         * starting at the current "y" position of the ball, and the bottom of our MyAnimationView
         * (offset by the ball's size). The duration of the animation is 1500 milliseconds, and it
         * uses a BounceInterpolator. Before returning we set the AnimatorUpdateListener to "this"
         * so that our onAnimationUpdate override is called for every frame of the animation.
         */
        private void createAnimation() {
            if (bounceAnim == null) {
                bounceAnim = ObjectAnimator.ofFloat(ball, "y",
                        ball.getY(), getHeight() - BALL_SIZE).setDuration(1500);
                bounceAnim.setInterpolator(new BounceInterpolator());
                bounceAnim.addUpdateListener(this);
                bounceAnim.addListener(this);
            }
        }

        /**
         * First we create the ValueAnimator bounceAnim if need be, then start it running. We are
         * called only from the onClick of the "RUN" Button.
         */
        public void startAnimation() {
            createAnimation();
            bounceAnim.start();
        }

        /**
         * First we create the ValueAnimator bounceAnim if need be, then we set the position of
         * bounceAnim to the specified point in time. Called only from the onProgressChanged
         * callback of the SeekBar mSeekBar.
         *
         * @param seekTime The time, in milliseconds, to which the animation is advanced or rewound.
         */
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
         * created and initialized. ArrayList<ShapeHolder> balls has this ShapeHolder add()'ed to
         * it, but balls is not used in this demo (the method was copied from a multi-ball demo).
         *
         * @param x x coordinate for ShapeHolder
         * @param y y coordinate for ShapeHolder
         * @return ShapeHolder containing "ball" at (x,y)
         */
        @SuppressWarnings("SameParameterValue")
        private ShapeHolder addBall(float x, float y) {
            OvalShape circle = new OvalShape();
            circle.resize(BALL_SIZE, BALL_SIZE);
            ShapeDrawable drawable = new ShapeDrawable(circle);
            ShapeHolder shapeHolder = new ShapeHolder(drawable);
            shapeHolder.setX(x);
            shapeHolder.setY(y);
            int red = (int)(100 + Math.random() * 155);
            int green = (int)(100 + Math.random() * 155);
            int blue = (int)(100 + Math.random() * 155);
            int color = 0xff000000 | red << 16 | green << 8 | blue;
            Paint paint = drawable.getPaint();
            int darkColor = 0xff000000 | red/4 << 16 | green/4 << 8 | blue/4;
            RadialGradient gradient = new RadialGradient(37.5f, 12.5f,
                    50f, color, darkColor, Shader.TileMode.CLAMP);
            paint.setShader(gradient);
            shapeHolder.setPaint(paint);
            balls.add(shapeHolder);
            return shapeHolder;
        }

        /**
         * Called to do the drawing of our view. First we pre-concatenate the current matrix
         * with a translation to the ball's ShapeHolder's current (x,y) position, then we instruct
         * the ShapeDrawable (ball) in the ShapeHolder to draw itself.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.translate(ball.getX(), ball.getY());
            ball.getShape().draw(canvas);
        }

        /**
         * This is the callback for the interface AnimatorUpdateListener, it is called to notify
         * us of the occurrence of another frame of the animation. First we invalidate() the View
         * ensuring that onDraw will be called at some point in the future, then we fetch the
         * current position of the animation in time, which is equal to the current time minus
         * the time that the animation started BUT do nothing with it (the commented out line
         * would have set the SeekBar mSeekBar to this value.)
         *
         * @param animation The animation which has moved to a new frame
         */
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            invalidate();
            long playtime = bounceAnim.getCurrentPlayTime();
            //mSeekBar.setProgress((int)playtime);
        }

        /**
         * Part of the AnimatorListener interface. NOT CALLED without bounceAnim.addListener(this)
         * Notifies the cancellation of the animation. This callback is not invoked for
         * animations with repeat count set to INFINITE.
         *
         * @param animation The animation which was canceled
         */
        @Override
        public void onAnimationCancel(Animator animation) {
        }

        /**
         * Part of the AnimatorListener interface. NOT CALLED without bounceAnim.addListener(this)
         * Notifies the end of the animation. This callback is not invoked
         * for animations with repeat count set to INFINITE.
         * For no apparent reason we remove the ball whose animation has ended from the unused
         * ArrayList<ShapeHolder> balls.
         *
         * @param animation The animation which reached its end.
         */
        @Override
        public void onAnimationEnd(Animator animation) {
            //noinspection SuspiciousMethodCalls
            balls.remove(((ObjectAnimator)animation).getTarget()); // Useless relic of Cut and paste?
            Log.i(TAG, "onAnimationEnd called");
        }

        /**
         * Part of the AnimatorListener interface. NOT CALLED without bounceAnim.addListener(this)
         * Notifies the repetition of the animation.
         *
         * @param animation The animation which was repeated.
         */
        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        /**
         * Part of the AnimatorListener interface. NOT CALLED without bounceAnim.addListener(this)
         * Notifies the start of the animation.
         *
         * @param animation The started animation.
         */
        @Override
        public void onAnimationStart(Animator animation) {
            Log.i(TAG, "onAnimationStart called");
        }
    }
}