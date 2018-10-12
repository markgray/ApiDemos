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
 * Supposed to show when the various Sequencer Events and Animator Events:
 * Start Repeat Cancel and End occur, but Repeat Events are not generated
 * for the Sequencer because the api does not support setRepeatCount on a
 * AnimatorSet.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AnimatorEvents extends Activity {
    /**
     * {@code TextView} with id R.id.startText ("Start") in "Sequencer Events:" row of the animation
     * event display, it has its alpha increased from .5f to 1f in the {@code onAnimationStart}
     * override if the argument passed it is an instance of {@code AnimatorSet}.
     */
    TextView startText;
    /**
     * {@code TextView} with id R.id.repeatText ("Repeat") in "Sequencer Events:" row of the animation
     * event display, it has its alpha increased from .5f to 1f in the {@code onAnimationRepeat}
     * override if the argument passed it is an instance of {@code AnimatorSet}.
     */
    TextView repeatText;
    /**
     * {@code TextView} with id R.id.cancelText ("Cancel") in "Sequencer Events:" row of the animation
     * event display, it has its alpha increased from .5f to 1f in the {@code onAnimationCancel}
     * override if the argument passed it is an instance of {@code AnimatorSet}.
     */
    TextView cancelText;
    /**
     * {@code TextView} with id R.id.endText ("End") in "Sequencer Events:" row of the animation
     * event display, it has its alpha increased from .5f to 1f in the {@code onAnimationEnd}
     * override if the argument passed it is an instance of {@code AnimatorSet}.
     */
    TextView endText;
    /**
     * {@code TextView} with id R.id.startTextAnimator ("Start") in "Animator Events:" row of the
     * animation event display, it has its alpha increased from .5f to 1f in the {@code onAnimationStart}
     * override if the argument passed it is NOT an instance of {@code AnimatorSet}.
     */
    TextView startTextAnimator;
    /**
     * {@code TextView} with id R.id.repeatTextAnimator ("Repeat") in "Animator Events:" row of the
     * animation event display, it has its alpha increased from .5f to 1f in the {@code onAnimationRepeat}
     * override if the argument passed it is NOT an instance of {@code AnimatorSet}.
     */
    TextView repeatTextAnimator;
    /**
     * {@code TextView} with id R.id.cancelTextAnimator ("Cancel") in "Animator Events:" row of the
     * animation event display, it has its alpha increased from .5f to 1f in the {@code onAnimationCancel}
     * override if the argument passed it is NOT an instance of {@code AnimatorSet}.
     */
    TextView cancelTextAnimator;
    /**
     * {@code TextView} with id R.id.endTextAnimator ("End") in "Animator Events:" row of the
     * animation event display, it has its alpha increased from .5f to 1f in the {@code onAnimationEnd}
     * override if the argument passed it is NOT an instance of {@code AnimatorSet}.
     */
    TextView endTextAnimator;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.animator_events.
     * We initialize {@code LinearLayout container} by finding the view with id R.id.container,
     * create a new instance for {@code MyAnimationView animView} and add that view to {@code container}.
     * We initialize our field {@code TextView startText} by finding the view with id R.id.startText
     * and set its alpha to .5f, initialize our field {@code TextView repeatText} by finding the view
     * with id R.id.repeatText and set its alpha to .5f, initialize our field {@code TextView cancelText}
     * by finding the view with id R.id.cancelText and set its alpha to .5f, initialize our field
     * {@code TextView endText} by finding the view with id R.id.endText and set its alpha to .5f,
     * initialize our field {@code TextView startTextAnimator} by finding the view with id
     * R.id.startTextAnimator and set its alpha to .5f, initialize our field {@code TextView repeatTextAnimator}
     * by finding the view with id R.id.repeatTextAnimator and set its alpha to .5f, initialize our field
     * {@code TextView cancelTextAnimator} by finding the view with id R.id.cancelTextAnimator and set
     * its alpha to .5f, and initialize our field {@code TextView endTextAnimator} by finding the view
     * with id R.id.endTextAnimator and set its alpha to .5f.
     * <p>
     * We initialize {@code CheckBox endCB} by finding the view with id R.id.endCB ("End Immediately").
     * We initialize {@code Button starter} by finding the view with id R.id.startButton ("Play") and
     * set its {@code OnClickListener} to an anonymous class which calls the {@code startAnimation}
     * method of {@code MyAnimationView animView} with the checked state of {@code CheckBox endCB}.
     * <p>
     * We initialize {@code Button canceler} by finding the view with id R.id.cancelButton ("Cancel")
     * and set its {@code OnClickListener} to an anonymous class which calls the {@code cancelAnimation}
     * method of {@code MyAnimationView animView}.
     * <p>
     * Finally we initialize {@code Button ender} by finding the view with id R.id.endButton ("End")
     * and set its {@code OnClickListener} to an anonymous class which calls the {@code endAnimation}
     * method of {@code MyAnimationView animView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animator_events);

        LinearLayout container = findViewById(R.id.container);
        final MyAnimationView animView = new MyAnimationView(this);
        container.addView(animView);
        startText = findViewById(R.id.startText);
        startText.setAlpha(.5f);
        repeatText = findViewById(R.id.repeatText);
        repeatText.setAlpha(.5f);
        cancelText = findViewById(R.id.cancelText);
        cancelText.setAlpha(.5f);
        endText = findViewById(R.id.endText);
        endText.setAlpha(.5f);
        startTextAnimator = findViewById(R.id.startTextAnimator);
        startTextAnimator.setAlpha(.5f);
        repeatTextAnimator = findViewById(R.id.repeatTextAnimator);
        repeatTextAnimator.setAlpha(.5f);
        cancelTextAnimator = findViewById(R.id.cancelTextAnimator);
        cancelTextAnimator.setAlpha(.5f);
        endTextAnimator = findViewById(R.id.endTextAnimator);
        endTextAnimator.setAlpha(.5f);

        final CheckBox endCB = findViewById(R.id.endCB);
        Button starter = findViewById(R.id.startButton);
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

        Button canceler = findViewById(R.id.cancelButton);
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

        Button ender = findViewById(R.id.endButton);
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
        /**
         * {@code AnimatorSet} created in method {@code createAnimation} which moves
         * {@code ShapeHolder ball} in both x and y directions
         */
        Animator animation;
        /**
         * The ball which we move.
         */
        ShapeHolder ball;
        /**
         * Flag set when "End Immediately" checkbox is checked, if true in {@code onAnimationStart}
         * callback causes the {@code end} method of {@code Animator animation} to be called
         * causing our animation to stop immediately.
         */
        boolean endImmediately = false;

        /**
         * Our constructor. First we call our super's constructor, then we initialize our field
         * {@code ShapeHolder ball} with a 25 pixel diameter circle created by our method
         * {@code createBall}.
         *
         * @param context context of the Application
         */
        public MyAnimationView(Context context) {
            super(context);
            ball = createBall(25, 25);
        }

        /**
         * Creates the {@code Animator animation} for the {@code ShapeHolder ball} (if it does not
         * already exist). If {@code animation} is not null we do nothing, if it is null we first
         * create {@code ObjectAnimator yAnim} to animate the "y" property name (the y coordinate)
         * {@code ShapeHolder ball} from the current position to the bottom minus 50px with a
         * duration of 1500 milliseconds. We set its repeat count to 2, its repeat mode to
         * REVERSE using a factor of 2.0 {@code AccelerateInterpolator} (starts slow and ends fast).
         * We add "this" as an {@code AnimatorUpdateListener} for {@code yAnim}, and also add "this"
         * as a {@code AnimatorListener}.
         * <p>
         * We create {@code ObjectAnimator xAnim} to animates the "x" property name (the y coordinate)
         * of the ball from the current position to the current position plus 300px with a duration
         * of 1000 milliseconds. We set its repeat count to 2, its repeatMode to REVERSE, and set
         * its interpolator to an {@code AccelerateInterpolator} using a factor of 2.0 (starts slow
         * and ends fast). {@code ObjectAnimator alphaAnim} and {@code AnimatorSet alphaSeq} are
         * strange in that they are played during the creation of the {@code AnimatorSet animation}
         * and do not participate with the event demonstration. Together they animate the alpha of
         * the {@code ShapeHolder ball} from 1f to .5f with a duration of 1000 milliseconds.
         * <p>
         * The finish of the process is to create the {@code AnimatorSet animation}, configure it to
         * play together {@code yAnim}, and {@code xAnim}. We then add "this" as an {@code AnimatorListener}
         * to {@code animation}.
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

        /**
         * Starts {@code Animator animation} running. First we set our field {@code endImmediately}
         * to our argument {@code endImmediately} for later use by the callback onAnimationStart.
         * Then we set the alpha of the text used to display the occurrence of events to .5f
         * ({@code startText}, {@code repeatText}, {@code cancelText}, {@code endText},
         * {@code startTextAnimator}, {@code repeatTextAnimator}, {@code cancelTextAnimator}, and
         * {@code endTextAnimator}) to signify that they have not occurred yet. Then we call our
         * method {@code createAnimation} to create the {@code Animator animation}. Finally we call
         * the {@code start} method of {@code animation} to start the animation running.
         *
         * @param endImmediately used to set the field {@code endImmediately} for use by the callback
         *                       {@code onAnimationStart} (which will call the {@code end} method of
         *                       {@code animation} immediately ending the animation.)
         */
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

        /**
         * Cancel the {@code Animator animation} (used by the CANCEL Button's {@code onClick} call
         * back). First we call our method {@code createAnimation} to create {@code Animator animation}
         * if it does not exist yet, then we call the {@code cancel} method of {@code animation}.
         */
        public void cancelAnimation() {
            createAnimation();
            animation.cancel();
        }

        /**
         * End the {@code Animator animation} (used by the END Button's {@code onClick} call back).
         * First we call our method {@code createAnimation} to create {@code Animator animation} if
         * it does not exist yet, then we call the {@code end} method of {@code animation}.
         */
        public void endAnimation() {
            createAnimation();
            animation.end();
        }

        /**
         * Creates and returns a {@code ShapeHolder} holding a "ball". First we create an
         * {@code OvalShape circle}, re-size it to be a 50px by 50px circle, create a
         * {@code ShapeDrawable drawable} from it, and place it in a {@code ShapeHolder shapeHolder}.
         * We sets the (x,y) coordinates of the {@code ShapeHolder} to the calling parameters of the
         * method minus 25 pixels. We initialize {@code int red} with a random number between 0 and
         * 255, and do the same for {@code int green} and {@code int blue}. We then initialize
         * {@code int color} by shifting these variables into the appropriate positions to form a
         * 32 bit RGB color and or them together along with a maximum alpha value. We initialize
         * {@code Paint paint} by retrieving the paint used to draw {@code ShapeDrawable drawable}.
         * We initialize {@code int darkColor} by or'ing together the RGB values used for {@code color}
         * divided by 4 along with a maximum alpha value. We create {@code RadialGradient gradient}
         * with a center x coordinate of 37.5, center y coordinate of 12.5, a radius of 50, with
         * {@code color} as the color at the center, {@code darkColor} as the color at the edge, and
         * using CLAMP tiling mode (replicates the edge color if the shader draws outside of its
         * original bounds). We set the shader of {@code paint} to {@code gradient} and set the paint
         * of {@code shapeHolder} to {@code paint}, then return {@code shapeHolder} to the caller.
         *
         * @param x x coordinate of the ShapeHolder created
         * @param y y coordinate of the ShapeHolder created
         * @return ShapeHolder containing the created ball
         */
        @SuppressWarnings("SameParameterValue")
        private ShapeHolder createBall(float x, float y) {
            OvalShape circle = new OvalShape();
            circle.resize(50f, 50f);
            ShapeDrawable drawable = new ShapeDrawable(circle);
            ShapeHolder shapeHolder = new ShapeHolder(drawable);
            shapeHolder.setX(x - 25f);
            shapeHolder.setY(y - 25f);
            int red = (int) (Math.random() * 255);
            int green = (int) (Math.random() * 255);
            int blue = (int) (Math.random() * 255);
            int color = 0xff000000 | red << 16 | green << 8 | blue;
            Paint paint = drawable.getPaint(); //new Paint(Paint.ANTI_ALIAS_FLAG);
            int darkColor = 0xff000000 | red / 4 << 16 | green / 4 << 8 | blue / 4;
            RadialGradient gradient = new RadialGradient(37.5f, 12.5f,
                    50f, color, darkColor, Shader.TileMode.CLAMP);
            paint.setShader(gradient);
            shapeHolder.setPaint(paint);
            return shapeHolder;
        }

        /**
         * Does the drawing of the {@code MyAnimationView} every time invalidate() is called. First
         * we save the current matrix and clip of {@code Canvas canvas} onto a private stack.
         * Then we pre-concatenate the current matrix with a translation to the current x,y coordinates
         * of {@code ShapeHolder ball}, instruct the {@code ShapeDrawable} of {@code ShapeHolder ball}
         * to draw itself, and then restore the matrix/clip state to the state saved on the private
         * stack.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.save();
            canvas.translate(ball.getX(), ball.getY());
            ball.getShape().draw(canvas);
            canvas.restore();
        }

        /**
         * Part of the {@code AnimatorUpdateListener} interface which we implement, this callback is
         * called to notify us of the occurrence of another frame of the animation. We simply
         * invalidate the whole view which will cause our {@code onDraw(Canvas)} callback to be
         * called to redraw our view using the new values set in the ShapeHolder by the animation.
         *
         * @param animation The animation which was has advanced by a frame.
         */
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            invalidate();
        }

        /**
         * Part of the {@code AnimatorListener} interface which we implement, this callback is called
         * to notify us of the start of the animation. We set the alpha of the text used to indicate
         * the occurrence of the "Start" event to 1.0f, either startText for our AnimatorSet
         * instance signaling the event, or else startTextAnimator for sequencer events caused by
         * our yAnim ObjectAnimator. If the "End Immediately" checkbox is checked the animation is
         * ended by calling animation.end().
         *
         * @param animation The started animation.
         */
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

        /**
         * Part of the AnimatorListener interface which we implement, this callback is called to
         * notify us of the end of the animation. We set the alpha of the text used to indicate
         * the occurrence of the "End" event to 1.0f, either endText for our AnimatorSet
         * instance signaling the event, or else endTextAnimator for sequencer events caused by
         * our yAnim ObjectAnimator.
         *
         * @param animation The animation which reached its end.
         */
        @Override
        public void onAnimationEnd(Animator animation) {
            if (animation instanceof AnimatorSet) {
                endText.setAlpha(1f);
            } else {
                endTextAnimator.setAlpha(1f);
            }
        }

        /**
         * Part of the AnimatorListener interface which we implement, this callback is called to
         * notify us of the cancellation of the animation. We set the alpha of the text used to indicate
         * the occurrence of the "Cancel" event to 1.0f, either cancelText for our AnimatorSet
         * instance signaling the event, or else cancelTextAnimator for sequencer events caused by
         * our yAnim ObjectAnimator.
         *
         * @param animation The animation which has been canceled
         */
        @Override
        public void onAnimationCancel(Animator animation) {
            if (animation instanceof AnimatorSet) {
                cancelText.setAlpha(1f);
            } else {
                cancelTextAnimator.setAlpha(1f);
            }
        }

        /**
         * Part of the AnimatorListener interface which we implement, this callback is called to
         * notify us of the repeat of the animation. We set the alpha of the text used to indicate
         * the occurrence of the "Repeat" event to 1.0f, either repeatText for our AnimatorSet
         * instance signaling the event, or else repeatTextAnimator for sequencer events caused by
         * our yAnim ObjectAnimator.
         *
         * @param animation The animation which has been repeated
         */
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