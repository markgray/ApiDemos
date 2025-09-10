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

package com.example.android.apis.animation

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.withTranslation
import com.example.android.apis.R

/**
 * Demonstrates the use of android.animation.ValueAnimator.reverse() method to play an
 * animation in "reverse".
 * RequiresApi(Build.VERSION_CODES.HONEYCOMB)
 */
@Suppress("MemberVisibilityCanBePrivate")
class ReversingAnimation : AppCompatActivity() {

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
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animation_reversing)
        val container = findViewById<LinearLayout>(R.id.container)
        val animView = MyAnimationView(this)
        container.addView(animView)

        val starter = findViewById<Button>(R.id.startButton)
        starter.setOnClickListener {
            animView.startAnimation()
        }

        val reverser = findViewById<Button>(R.id.reverseButton)
        reverser.setOnClickListener {
            animView.reverseAnimation()
        }

    }

    /**
     * This custom View consists of a ball which has an AccelerateInterpolator to animate it.
     * If MyAnimationView.startAnimation is called it falls from the top to the bottom of the View
     * and stays there. If MyAnimationView.reverseAnimation is called it "falls" from the bottom
     * to the top of the View (no matter where it starts) and stays there.
     * Initializes a new instance of MyAnimationView. First calls our super's constructor,
     * then creates a ShapeHolder ball containing a 25px by 25px ball.
     *
     * @param context ReversingAnimation Activity Context
     */
    inner class MyAnimationView(context: Context) : View(context),
        ValueAnimator.AnimatorUpdateListener {

        /**
         * `ObjectAnimator` which animates our `ShapeHolder ball`
         */
        internal var bounceAnim: ValueAnimator? = null

        /**
         * `ShapeHolder` holding our ball, it is the object which is moved via its "y" property
         */
        internal var ball: ShapeHolder

        init {
            ball = createBall(25f, 25f)
        }

        /**
         * If the ValueAnimator bounceAnim has not already been created, we create an ObjectAnimator
         * that animates "y" from the current position to 50px from the bottom with a duration of
         * 1500 milliseconds, and set the interpolator used to an AccelerateInterpolator with a
         * factor of 2.0. We set the ValueAnimator.AnimatorUpdateListener to "this" so that our
         * callback override of onAnimationUpdate is called for every frame of the animation.
         * (It just invalidates the View causing it to be redrawn every frame.)
         */
        private fun createAnimation() {
            if (bounceAnim == null) {
                @SuppressLint("Recycle")
                bounceAnim = ObjectAnimator.ofFloat(ball, "y", ball.y, height - 50f)
                    .setDuration(1500)
                bounceAnim!!.interpolator = AccelerateInterpolator(2f)
                bounceAnim!!.addUpdateListener(this)
            }
        }

        /**
         * Create the animation bounceAnim (if necessary) and start it running.
         */
        fun startAnimation() {
            createAnimation()
            bounceAnim!!.start()
        }

        /**
         * Create the animation bounceAnim (if necessary) and play the ValueAnimator in reverse.
         * If the animation is already running, it will stop itself and play backwards from the
         * point reached when reverse was called. If the animation is not currently running,
         * then it will start from the end and play backwards. This behavior is only set for
         * the current animation; future playing of the animation will use the default behavior
         * of playing forward.
         */
        fun reverseAnimation() {
            createAnimation()
            bounceAnim!!.reverse()
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
        fun seek(seekTime: Long) {
            createAnimation()
            bounceAnim!!.currentPlayTime = seekTime
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
        @Suppress("SameParameterValue")
        private fun createBall(x: Float, y: Float): ShapeHolder {
            val circle = OvalShape()
            circle.resize(50f, 50f)
            val drawable = ShapeDrawable(circle)
            val shapeHolder = ShapeHolder(drawable)
            shapeHolder.x = x - 25f
            shapeHolder.y = y - 25f
            val red = (Math.random() * 255).toInt()
            val green = (Math.random() * 255).toInt()
            val blue = (Math.random() * 255).toInt()
            val color = -0x1000000 or (red shl 16) or (green shl 8) or blue
            val paint = drawable.paint //new Paint(Paint.ANTI_ALIAS_FLAG);
            val darkColor = -0x1000000 or (red / 4 shl 16) or (green / 4 shl 8) or blue / 4
            val gradient = RadialGradient(
                37.5f, 12.5f,
                50f, color, darkColor, Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            shapeHolder.paint = paint
            return shapeHolder
        }

        /**
         * Does the drawing of our MyAnimationView View. First we save the current matrix and
         * clip of our Canvas onto a private stack, then we pre-concatenate the current matrix
         * with a translation to the ball's ShapeHolder's current (x,y) position, then we instruct
         * the ShapeDrawable (ball) in the ShapeHolder to draw itself, and finally we remove our
         * modifications to the matrix/clip which moved the canvas to our ball's location.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.withTranslation(x = ball.x, y = ball.y) {
                ball.shape!!.draw(this)
            }
        }

        /**
         * Notifies the occurrence of another frame of the animation. We just invalidate the
         * MyAnimationView View causing our onDraw method override to be called to draw
         * ourselves.
         *
         * @param animation The animation which has moved to another frame
         */
        override fun onAnimationUpdate(animation: ValueAnimator) {
            invalidate()
        }

    }
}