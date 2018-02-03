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

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.graphics.Color;

import com.example.android.apis.R;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.ValueAnimator;
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

/**
 * Loads animations from Xml files: R.animator.object_animator (animates ball[0] "y"
 * from 0 to 200, and reverses), R.animator.animator (animates ball[1] alpha from 1 to
 * 0 and reverses), R.animator.animator_set (an animator set which animates ball[2]'s
 * "x" from 0 to 200, and "y" from 0 to 400), R.animator.color_animator (an animator
 * which animates ball[3]'s color from #0f0 to #00ffff), R.animator.object_animator_pvh
 * (an animator which animates ball[4]'s "x" from 0 to 400, and "y" from 0 to 200 using
 * propertyValuesHolder's), R.animator.object_animator_pvh_kf (uses propertyValuesHolder
 * to hold keyframe specs for x and y and uses the default linear interpolator on balls[5]),
 * R.animator.value_animator_pvh_kf (uses propertyValuesHolder to hold keyframe specs
 * for a value which balls[6] uses in an AnimatorUpdateListener for an alpha animation),
 * and R.animator.object_animator_pvh_kf_interpolated (the animation used for balls[7] has an
 * accelerate interpolator applied on each keyframe interval instead of the default used on
 * balls[5], As these two animations use the exact same path, the effect of the per-keyframe
 * interpolator has been made obvious.)
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AnimationLoading extends Activity {

    @SuppressWarnings("unused")
    private static final int DURATION = 1500;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.animation_loading.
     * We initialize {@code LinearLayout container} by finding the view with id R.id.container,
     * initialize {@code MyAnimationView animView} with a new instance and then add it to
     * {@code container}. We initialize {@code Button starter} by finding the view with id
     * R.id.startButton ("Run") and set its {@code OnClickListener} to an anonymous class which calls
     * the {@code startAnimation} method of {@code animView} to start its animation.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animation_loading);

        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        final MyAnimationView animView = new MyAnimationView(this);
        container.addView(animView);

        Button starter = (Button) findViewById(R.id.startButton);
        starter.setOnClickListener(new View.OnClickListener() {
            /**
             * Start the MyAnimationView animView animation running
             *
             * @param v Button View which was clicked
             */
            @Override
            public void onClick(View v) {
                animView.startAnimation();
            }
        });
    }

    /**
     * This is the custom View which contains our animation demonstration
     */
    public class MyAnimationView extends View implements ValueAnimator.AnimatorUpdateListener {

        /**
         * Ball size in pixels, used in the method {@code createBall} to resize the {@code OvalShape}
         * used to create the balls.
         */
        private static final float BALL_SIZE = 100f;

        /**
         * List holding the {@code ShapeHolder} objects which hold the balls
         */
        public final ArrayList<ShapeHolder> balls = new ArrayList<>();
        /**
         * {@code AnimatorSet} which holds all the animations for the 8 balls we animate
         */
        Animator animation = null;

        /**
         * Creates the eight balls (0-7) which are in our View. (Two of the balls (5 and 7) start in
         * the same place). The method {@code addBall} is called with the x and y coordinates for the
         * call being created with the 2 argument version assigning a random color to the ball for
         * balls 0, 1, 2, 4, 5, 6 and the 3 argument version assigning the color GREEN to ball 3 and
         * YELLOW to ball 7.
         *
         * @param context The Context the view is running in, through which it can access the
         *                current theme, resources, etc., "this" in the {@code onCreate} method
         *                of the {@code AnimationLoading} activity.
         */
        public MyAnimationView(Context context) {
            super(context);
            addBall(50, 50);
            addBall(200, 50);
            addBall(350, 50);
            addBall(500, 50, Color.GREEN);
            addBall(650, 50);
            addBall(800, 50);
            addBall(950, 50);
            addBall(800, 50, Color.YELLOW);
        }

        /**
         * Loads, creates and configures the Animator animation used for the 8 balls. If this is
         * the first time it is called (animation == null) it creates animators for the balls as
         * follows:
         * <ul>
         * <li>
         * balls[0] (50,50) Uses an ObjectAnimator anim created by loading the animation from
         * the file R.animator.object_animator which animates "y" from the starting point
         * to 200 with a repeat count of 1 and a repeatMode of "reverse", uses "this" as
         * the UpdateListener which causes our classes override of onAnimationUpdate to
         * be called which invalidates the View and sets the "y" value of the ShapeHolder
         * holding balls[0] to the current value of the animation.
         * </li>
         * <li>
         * balls[1] (200,50) Uses a ValueAnimator fader which it creates by loading the file
         * R.animator.animator which animates a value from 1 to 0 with a repeat count
         * of 1 and a repeatMode of "reverse", and sets the UpdateListener to an
         * AnimatorUpdateListener which sets the alpha of the ShapeHolder holding
         * balls[1] to the current value of the animation (relying on the call to
         * invalidate() for balls[0] to trigger a re-draw of the View.)
         * </li>
         * <li>
         * balls[2] (350,50) Uses an AnimatorSet seq which it creates by loading the file
         * R.animator.animator_set which creates two objectAnimator's to animate the "x"
         * value from the current value to 200, and the "y" value from the current value
         * to 400 with a repeat count of 1 and a repeatMode of "reverse"
         * </li>
         * <li>
         * balls[3] (500,50) Color.GREEN Uses an ObjectAnimator colorizer which it creates by
         * loading the file R.animator.color_animator which animates the value "color"
         * of the ShapeHolder holding balls[3] from "#0f0" to "#00ffff" with a repeat
         * count of 1 and a repeatMode of "reverse"
         * </li>
         * <li>
         * balls[4] (650,50) Use an ObjectAnimator animPvh which it loads from the file
         * R.animator.object_animator_pvh which animates "x" from 0 to 400, and "y"
         * from 0 to 200 using propertyValuesHolder's
         * </li>
         * <li>
         * balls[5] (800,50) Uses an ObjectAnimator animPvhKf which it creates by loading the file
         * R.animator.object_animator_pvh_kf which uses propertyValuesHolder to hold
         * keyframe specs for x and y and uses the default linear interpolator
         * </li>
         * <li>
         * balls[6] (950,50) Uses a ValueAnimator faderKf which it loads from the file
         * R.animator.value_animator_pvh_kf which uses propertyValuesHolder to hold
         * keyframe specs for a value, it then sets the UpdateListener to an
         * AnimatorUpdateListener which sets the alpha of the ShapeHolder holding balls[6]
         * to the current animated value.
         * </li>
         * <li>
         * balls[7] (800,50) Color.YELLOW Uses an ObjectAnimator animPvhKfInterpolated which
         * it loads from R.animator.object_animator_pvh_kf_interpolated which uses
         * propertyValuesHolder's to hold keyframe specs for "x" and "y" and has an
         * accelerate interpolator applied on each keyframe interval. In comparison,
         * the animation defined in R.anim.object_animator_pvh_kf for balls[5] uses
         * the default linear interpolator throughout the animation. As these two
         * animations use the exact same path, the effect of the per-keyframe interpolator
         * has been made obvious.
         * </li>
         * </ul>
         * It then creates the {@code AnimatorSet animation} configures it to playTogether the
         * 8 Animator's created for the 8 balls.
         */
        private void createAnimation() {
            Context appContext = AnimationLoading.this;

            if (animation == null) {
                ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.
                        loadAnimator(appContext, R.animator.object_animator);
                anim.addUpdateListener(this);
                anim.setTarget(balls.get(0));

                ValueAnimator fader = (ValueAnimator) AnimatorInflater.
                        loadAnimator(appContext, R.animator.animator);
                fader.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        balls.get(1).setAlpha((Float) animation.getAnimatedValue());
                    }
                });

                AnimatorSet seq =
                        (AnimatorSet) AnimatorInflater.loadAnimator(appContext,
                                R.animator.animator_set);
                seq.setTarget(balls.get(2));

                ObjectAnimator colorizer = (ObjectAnimator) AnimatorInflater.
                        loadAnimator(appContext, R.animator.color_animator);
                colorizer.setTarget(balls.get(3));

                ObjectAnimator animPvh = (ObjectAnimator) AnimatorInflater.
                        loadAnimator(appContext, R.animator.object_animator_pvh);
                animPvh.setTarget(balls.get(4));


                ObjectAnimator animPvhKf = (ObjectAnimator) AnimatorInflater.
                        loadAnimator(appContext, R.animator.object_animator_pvh_kf);
                animPvhKf.setTarget(balls.get(5));

                ValueAnimator faderKf = (ValueAnimator) AnimatorInflater.
                        loadAnimator(appContext, R.animator.value_animator_pvh_kf);
                faderKf.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        balls.get(6).setAlpha((Float) animation.getAnimatedValue());
                    }
                });

                // This animation has an accelerate interpolator applied on each
                // keyframe interval. In comparison, the animation defined in
                // R.anim.object_animator_pvh_kf uses the default linear interpolator
                // throughout the animation. As these two animations use the
                // exact same path, the effect of the per-keyframe interpolator
                // has been made obvious.
                ObjectAnimator animPvhKfInterpolated = (ObjectAnimator) AnimatorInflater.
                        loadAnimator(appContext, R.animator.object_animator_pvh_kf_interpolated);
                animPvhKfInterpolated.setTarget(balls.get(7));

                animation = new AnimatorSet();
                ((AnimatorSet) animation).playTogether(anim, fader, seq, colorizer, animPvh,
                        animPvhKf, faderKf, animPvhKfInterpolated);

            }
        }

        /**
         * Calls {@code createAnimation()} to create the {@code AnimatorSet animation} (if it does
         * not exist yet) then calls {@code animation.start()} to start the animation running. Called
         * from the {@code onClickListener} for the "RUN" button.
         */
        public void startAnimation() {
            createAnimation();
            animation.start();
        }

        /**
         * Creates a ball in a {@code ShapeHolder}. Creates {@code OvalShape circle}, resize's it to
         * be a BALL_SIZE by BALL_SIZE circle (100x100), creates {@code ShapeDrawable drawable} from
         * {@code circle}, creates {@code ShapeHolder shapeHolder} containing {@code drawable}, and
         * sets the "x" and "y" coordinates of {@code shapeHolder} to the (x,y) arguments then returns
         * {@code shapeHolder} to the caller.
         *
         * @param x x coordinate for ball
         * @param y y coordinate for ball
         * @return ShapeHolder containing ball at (x, y)
         */
        private ShapeHolder createBall(float x, float y) {
            OvalShape circle = new OvalShape();
            circle.resize(BALL_SIZE, BALL_SIZE);
            ShapeDrawable drawable = new ShapeDrawable(circle);
            ShapeHolder shapeHolder = new ShapeHolder(drawable);
            shapeHolder.setX(x);
            shapeHolder.setY(y);
            return shapeHolder;
        }

        /**
         * Adds a new ball contained in a {@code shapeHolder} to {@code ArrayList<ShapeHolder> balls}
         * at location {@code (x,y)}, and with Color {@code color}. It does this by creating a ball
         * containing {@code ShapeHolder shapeHolder} at {@code (x,y)} by calling {@code createBall(x, y)},
         * sets the color of {@code shapeHolder} to the {@code color} argument by calling the
         * {@code setColor} method of {@code shapeHolder} and finally adds {@code shapeHolder} to
         * {@code balls}.
         *
         * @param x     x coordinate for ball
         * @param y     y coordinate for ball
         * @param color color of ball
         */
        @SuppressWarnings("SameParameterValue")
        private void addBall(float x, float y, int color) {
            ShapeHolder shapeHolder = createBall(x, y);
            shapeHolder.setColor(color);
            balls.add(shapeHolder);
        }

        /**
         * Adds a new ball in a {@code ShapeHolder} to {@code ArrayList<ShapeHolder> balls} at
         * location {@code (x,y)}, with a random color. It does this by creating a ball inside
         * {@code ShapeHolder shapeHolder} at {@code (x,y)} by calling createBall(x, y). It then
         * creates a random {@code int red} between 100 and 255, a random {@code int green} between
         * 100 and 255, and a random {@code int blue} between 100 and 255. It then shifts them into
         * the appropriate bit positions for a 32 bit color and or'd the three colors along with a
         * maximum alpha field to form the color {@code int color}. It initializes {@code Paint paint}
         * by fetching the paint from the {@code ShapeDrawable} of {@code shapeHolder}. It creates
         * {@code int darkColor} using the random colors {@code red}, {@code green} and {@code blue}
         * divided by 4 before being shifted into position and or'ed together with a maximum alpha
         * value. {@code RadialGradient gradient} is then created with 37.5 as the x-coordinate of
         * the center of the radius, 12.5 as the y-coordinate of the center of the radius, 50. as the
         * radius of the circle for the gradient, {@code color} as the color at the center of the
         * circle, {@code darkColor} as the color at the edge of the circle, and using CLAMP Shader
         * tiling mode (replicate the edge color if the shader draws outside of its original bounds).
         * It sets {@code gradient} as the shader of {@code paint}, adds {@code shapeHolder} to
         * {@code ArrayList<ShapeHolder> balls} and returns {@code shapeHolder} to the caller.
         * <p>
         * The Paint instance "paint" is fetched from
         * "shapeHolder" and Random colors and a RadialGradient created and are used to set the
         * Shader used by the ShapeHolder's Paint instance, and the ShapeHolder is then add()'ed
         * to the balls List.
         *
         * @param x x coordinate for ball
         * @param y y coordinate for ball
         */
        @SuppressWarnings("SameParameterValue")
        private void addBall(float x, float y) {
            ShapeHolder shapeHolder = createBall(x, y);
            int red = (int) (100 + Math.random() * 155);
            int green = (int) (100 + Math.random() * 155);
            int blue = (int) (100 + Math.random() * 155);
            int color = 0xff000000 | red << 16 | green << 8 | blue;
            Paint paint = shapeHolder.getShape().getPaint();
            int darkColor = 0xff000000 | red / 4 << 16 | green / 4 << 8 | blue / 4;
            RadialGradient gradient = new RadialGradient(37.5f, 12.5f,
                    50f, color, darkColor, Shader.TileMode.CLAMP);
            paint.setShader(gradient);
            balls.add(shapeHolder);
        }

        /**
         * Called when the View needs to draw itself. For each of the {@code ShapeHolder ball} objects
         * in the {@code ArrayList<ShapeHolder> balls} list the {@code Canvas canvas} argument has a
         * translation to the current (x,y) ball location pre-concatenated to it (the x,y coordinates
         * are fetched from the {@code ShapeHolder ball}), the {@code ShapeDrawable} contained in the
         * {@code ShapeHolder ball} is then fetched and instructed to draw itself. The canvas is then
         * restored to its previous state by pre-concatenating a translation that is the inverse of
         * the previous one that moved the canvas to the ball's (x,y) location.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            for (ShapeHolder ball : balls) {
                canvas.translate(ball.getX(), ball.getY());
                ball.getShape().draw(canvas);
                canvas.translate(-ball.getX(), -ball.getY());
            }
        }

        /**
         * This callback is called to notify us of the occurrence of another frame of an animation,
         * and is called by the animation used for {@code balls[0]} because of the use of the method
         * {@code anim.addUpdateListener(this)} included in the creation of the animation. First we
         * call {@code invalidate()} to invalidate the {@code View} so that {@code onDraw()} will be
         * called at some point in the future, then we fetch the {@code ShapeHolder} holding
         * {@code balls[0]} and set the y coordinate to the current value specified by the
         * {@code ValueAnimator animation} argument.
         *
         * @param animation animation which has moved to a new frame
         */
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            invalidate();
            ShapeHolder ball = balls.get(0);
            ball.setY((Float) animation.getAnimatedValue());
        }
    }
}