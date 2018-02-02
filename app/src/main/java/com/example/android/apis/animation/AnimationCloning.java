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

import android.annotation.TargetApi;
import android.os.Build;
import android.widget.Button;

import com.example.android.apis.R;

import android.animation.*;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Creates an ObjectAnimator to animate the y position of an object from 0 to the
 * bottom of the View, .clones it and uses .setTarget to set it as the animation
 * of a second View. Then it creates two ObjectAnimator's to: animate the y position
 * of an object down, and a second to animate y position up again and creates an AnimatorSet
 * to play them sequentially, clones this AnimatorSet and .setTarget's the clone as the
 * AnimatorSet for a second object. Uses an AnimatorSet play the first two ObjectAnimator's
 * and first AnimatorSet, requesting that they be run at the same time by calling
 * playTogether(ObjectAnimator1,ObjectAnimator2,AnimatorSet1), and the second AnimatorSet
 * to run after the first AnimatorSet by calling playSequentially(AnimatorSet1,AnimatorSet2).
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AnimationCloning extends Activity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.animation_cloning.
     * We initialize {@code LinearLayout container} by finding the view with id R.id.container, then
     * we initialize {@code MyAnimationView animView} with a new instance and add that view to
     * {@code container}. We initialize {@code Button starter} by finding the view with the id
     * R.id.startButton ("Run") and set its {@code OnClickListener} to an anonymous class which calls
     * the {@code startAnimation} method of {@code animView} which creates the animation if this is
     * the first time it is called then starts the animation running.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animation_cloning);

        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        final MyAnimationView animView = new MyAnimationView(this);
        container.addView(animView);

        Button starter = (Button) findViewById(R.id.startButton);
        starter.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when the button with id R.id.startButton ("Run") is clicked, we just call the
             * {@code startAnimation} method of {@code MyAnimationView animView}.
             *
             * @param v View that was clicked
             */
            @Override
            public void onClick(View v) {
                animView.startAnimation();
            }
        });
    }

    /**
     * A custom view with 4 animated balls in it.
     */
    public class MyAnimationView extends View implements ValueAnimator.AnimatorUpdateListener {

        /**
         * List holding our 4 balls.
         */
        public final ArrayList<ShapeHolder> balls = new ArrayList<>();
        /**
         * {@code AnimatorSet} we use to hold the animations for all 4 balls, created in our method
         * {@code createAnimation} and started by our method {@code startAnimation}.
         */
        AnimatorSet animation = null;
        /**
         * logical density of the display.
         */
        private float mDensity;

        /**
         * Our constructor. First we call our super's constructor then we initialize our field
         * {@code float mDensity} with the logical density of the display. Then we create 4 balls
         * and add them to the {@code ArrayList<ShapeHolder> balls} using our method {@code addBall}
         *
         * @param context Context which in our case is derived from super of Activity
         */
        public MyAnimationView(Context context) {
            super(context);

            mDensity = getContext().getResources().getDisplayMetrics().density;

            addBall(50f * mDensity, 25f * mDensity);
            addBall(150f * mDensity, 25f * mDensity);
            addBall(250f * mDensity, 25f * mDensity);
            addBall(350f * mDensity, 25f * mDensity);
        }

        /**
         * Creates the {@code AnimatorSet animation}. First we create {@code ObjectAnimator anim1}
         * which moves balls{0} y coordinate from 0f to the bottom of the View. We create
         * {@code ObjectAnimator anim2} as a clone of {@code anim1} and target it to balls{1}.
         * We add "this" as an {@code AnimatorUpdateListener} for {@code anim1} which causes
         * {@code this.onAnimationUpdate} to be called for every frame of this animation (it simply
         * calls invalidate() to cause redraw of the view). We initialize {@code ShapeHolder ball2}
         * with balls{2}, then create {@code ObjectAnimator animDown} to animate the y coordinate of
         * {@code ball2} from 0f to the bottom of the View and set its interpolator to an
         * {@code AccelerateInterpolator}. We create {@code ObjectAnimator animUp} to animate
         * {@code ball2} y coordinate from the bottom of the View to 0f and set its interpolator to
         * an {@code DecelerateInterpolator}. We create {@code AnimatorSet s1} and configure it to
         * play sequentially {@code animDown} followed by {@code animUp}. We then set the
         * {@code AnimatorUpdateListener} of both {@code animDown} and {@code animUp} to "this"
         * (our override of {@code onAnimationUpdate} will be called for every frame  of these
         * animations. We clone {@code s1} to initialize {@code AnimatorSet s2}, and its target to
         * balls{3}. We create the master {@code AnimatorSet animation}, and add {@code anim1},
         * {@code anim2}, and {@code s1} set to play together, and {@code s1} and {@code s2} are
         * then added to play sequentially (the first three balls start their animations when the
         * "Run" button is clicked, and the fourth ball starts its animation only after the other
         * three have finished their animations).
         */
        private void createAnimation() {
            if (animation == null) {
                ObjectAnimator anim1 = ObjectAnimator.ofFloat(balls.get(0), "y",
                        0f, getHeight() - balls.get(0).getHeight()).setDuration(500);
                ObjectAnimator anim2 = anim1.clone();
                anim2.setTarget(balls.get(1));
                anim1.addUpdateListener(this);

                ShapeHolder ball2 = balls.get(2);
                ObjectAnimator animDown = ObjectAnimator.ofFloat(ball2, "y",
                        0f, getHeight() - ball2.getHeight()).setDuration(500);
                animDown.setInterpolator(new AccelerateInterpolator());
                ObjectAnimator animUp = ObjectAnimator.ofFloat(ball2, "y",
                        getHeight() - ball2.getHeight(), 0f).setDuration(500);
                animUp.setInterpolator(new DecelerateInterpolator());
                AnimatorSet s1 = new AnimatorSet();
                s1.playSequentially(animDown, animUp);
                animDown.addUpdateListener(this);
                animUp.addUpdateListener(this);
                AnimatorSet s2 = s1.clone();
                s2.setTarget(balls.get(3));

                animation = new AnimatorSet();
                animation.playTogether(anim1, anim2, s1);
                animation.playSequentially(s1, s2);
            }
        }

        /**
         * Creates, configures, then adds a randomly colored ball at coordinates {@code (x,y)} to our
         * field {@code ArrayList<ShapeHolder> balls}. First we initialize our variable
         * {@code OvalShape circle} with a new instance, then we resize it to 50dp x 50dp, place it
         * in a {@code ShapeDrawable drawable} and use that {@code ShapeDrawable} to create
         * {@code ShapeHolder shapeHolder} to hold it. We set the x and y coordinates of {@code shapeHolder}
         * to our arguments {@code x} and {@code y} respectively. We create a random {@code int red}
         * between 100 and 255, a random {@code int green} between 100 and 255, and a random
         * {@code int blue} between 100 and 255. We then shift them into the appropriate bit positions
         * for a 32 bit color and or the three colors along with a maximum alpha field to form the color
         * {@code int color}. We retrieve the {@code Paint} used to draw {@code drawable} to initialize
         * {@code Paint paint}. We create {@code int darkColor} using our random colors {@code red},
         * {@code green} and {@code blue} divided by 4 before being shifted into position and or'ed
         * together with a maximum alpha value. {@code RadialGradient gradient} is then created with
         * 37.5 as the x-coordinate of the center of the radius, 12.5 as the y-coordinate of the center
         * of the radius, 50. as the radius of the circle for the gradient, {@code color} as the color
         * at the center of the circle, {@code darkColor} as the color at the edge of the circle, and
         * using CLAMP Shader tiling mode (replicate the edge color if the shader draws outside of its
         * original bounds). We set {@code gradient} as the shader of {@code paint}, add {@code shapeHolder}
         * to {@code ArrayList<ShapeHolder> balls} and return {@code shapeHolder} to the caller.
         *
         * @param x x coordinate for new ball
         * @param y y coordinate for new ball
         * @return ShapeHolder containing the new ball
         */
        @SuppressWarnings("UnusedReturnValue")
        private ShapeHolder addBall(float x, float y) {
            OvalShape circle = new OvalShape();
            circle.resize(50f * mDensity, 50f * mDensity);
            ShapeDrawable drawable = new ShapeDrawable(circle);
            ShapeHolder shapeHolder = new ShapeHolder(drawable);
            shapeHolder.setX(x - 25f);
            shapeHolder.setY(y - 25f);
            int red = (int) (100 + Math.random() * 155);
            int green = (int) (100 + Math.random() * 155);
            int blue = (int) (100 + Math.random() * 155);
            int color = 0xff000000 | red << 16 | green << 8 | blue;
            Paint paint = drawable.getPaint(); //new Paint(Paint.ANTI_ALIAS_FLAG);
            int darkColor = 0xff000000 | red / 4 << 16 | green / 4 << 8 | blue / 4;
            RadialGradient gradient = new RadialGradient(37.5f, 12.5f,
                    50f, color, darkColor, Shader.TileMode.CLAMP);
            paint.setShader(gradient);
            shapeHolder.setPaint(paint);
            balls.add(shapeHolder);
            return shapeHolder;
        }

        /**
         * This callback draws its {@code MyAnimationView} after every invalidate() call. We loop over
         * {@code int i} for all the {@code ShapeHolder} objects in {@code ArrayList<ShapeHolder> balls}
         * initializing {@code ShapeHolder shapeHolder} with the {@code ShapeHolder} in {@code balls}
         * at index {@code i}. We save the current matrix and clip of {@code canvas} onto a private
         * stack, pre-concatenate the matrix of {@code canvas} with a translation to the coordinates
         * {@code (x,y)} of the ShapeHolder, then instruct the {@code ShapeDrawable} in {@code shapeHolder}
         * to draw itself. We then remove all modifications to the matrix/clip state of {@code canvas}
         * and loop around for the next ball.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            for (int i = 0; i < balls.size(); ++i) {
                ShapeHolder shapeHolder = balls.get(i);
                canvas.save();
                canvas.translate(shapeHolder.getX(), shapeHolder.getY());
                shapeHolder.getShape().draw(canvas);
                canvas.restore();
            }
        }

        /**
         * Called when the RUN button is clicked, we first call our method {@code createAnimation} to
         * create the animation {@code AnimatorSet animation} (if this is the first time the button is
         * clicked), and then start the animation running.
         */
        public void startAnimation() {
            createAnimation();
            animation.start();
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