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

import android.animation.Animator
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
import android.util.Log
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This application demonstrates the seeking capability of ValueAnimator. The SeekBar in the
 * UI allows you to set the position of the animation. Pressing the Run button will play from
 * the current position of the animation.
 * RequiresApi(Build.VERSION_CODES.HONEYCOMB)
 */
@Suppress("MemberVisibilityCanBePrivate")
class AnimationSeeking : AppCompatActivity() {
    /**
     * The `SeekBar` in our layout with id R.id.seekBar used by the user to adjust the position
     * of the animation.
     */
    private var mSeekBar: SeekBar? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.animation_seeking.
     * We initialize `LinearLayout container` by finding the view with the id R.id.container,
     * initialize `MyAnimationView animView` with a new instance, and add it to our
     * `LinearLayout container`. We initialize `Button starter` by finding the view with
     * id R.id.startButton ("Run") and set its `OnClickListener` to an anonymous class which
     * calls the `startAnimation()` method of the `MyAnimationView animView` when the
     * button is clicked. We initialize our field `SeekBar mSeekBar` by finding the view with
     * id R.id.seekBar, set its maximum value to `DURATION` (1500), and set its
     * `OnSeekBarChangeListener` to an anonymous class whose `onProgressChanged` override
     * calls the `seek` method of `MyAnimationView animView` to set the animation to the
     * time indicated by the position of the seekbar (0-1500ms) whenever the user changes the setting.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animation_seeking)

        val container = findViewById<LinearLayout>(R.id.container)
        val animView = MyAnimationView(this)
        container.addView(animView)

        val starter = findViewById<Button>(R.id.startButton)
        starter.setOnClickListener {
            animView.startAnimation()
        }

        mSeekBar = findViewById(R.id.seekBar)
        mSeekBar!!.max = DURATION
        mSeekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            /**
             * Notification that the user has finished a touch gesture. Just overridden because
             * the interface is abstract.
             *
             * @param seekBar The SeekBar in which the touch gesture began.
             */
            override fun onStopTrackingTouch(seekBar: SeekBar) {}

            /**
             * Notification that the user has started a touch gesture. Just overridden because
             * the interface is abstract.
             *
             * @param seekBar The SeekBar in which the touch gesture began,
             */
            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            /**
             * Notification that the progress level has changed. We first check to see if
             * MyAnimationView animView has been drawn yet (otherwise we are seeking too
             * soon), if it has been then we call the method MyAnimationView.seek to position
             * the animation at the setting of the SeekBar.
             *
             * @param seekBar The SeekBar whose progress has changed
             * @param progress The current progress level. This will be in the range 0..DURATION
             * where DURATION was set by ProgressBar.setMax(DURATION) above.
             * @param fromUser True if the progress change was initiated by the user.
             */
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // prevent seeking on app creation
                if (animView.height != 0) {
                    animView.seek(progress.toLong())
                }
            }
        })
    }

    /**
     * This is the custom View for our demo. It holds a "ball" with an animation attached to it
     * which causes the ball to fall from the top of the View to the bottom and bounce at the
     * bottom.
     */
    inner class MyAnimationView
    /**
     * Our constructor. First we call through to our super's constructor, then we create a
     * `ShapeHolder ball` located at (200,0) (adding it to the list of balls contained in
     * `ArrayList<ShapeHolder> balls` for no apparent reason (cut and paste can be odd).
     *
     * @param context AnimationSeeking Activity context
     */
        (context: Context) : View(context), ValueAnimator.AnimatorUpdateListener,
        Animator.AnimatorListener {

        /**
         * List of balls contained inside `ShapeHolder` objects, which we do not actually use
         */
        val balls: ArrayList<ShapeHolder> = ArrayList()

        /**
         * `ObjectAnimator` which bounces the ball using a `BounceInterpolator`
         */
        internal var bounceAnim: ValueAnimator? = null

        /**
         * The one and only ball, which bounces and whose animation is controlled by the seekbar
         */
        internal var ball: ShapeHolder

        init {
            ball = addBall(200f, 0f)
        }

        /**
         * Creates an `ValueAnimator bounceAnim` iff it is null at the moment. This is an
         * `ObjectAnimator` that animates the `ShapeHolder ball` objects "y" field
         * between float values starting at the current "y" position of the ball, and the bottom of
         * our `MyAnimationView` (offset by the ball's size). The duration of the animation is
         * 1500 milliseconds, and it uses a `BounceInterpolator`. Before returning we set its
         * `AnimatorUpdateListener` to "this" so that our `onAnimationUpdate` override
         * is called for every frame of the animation, and its `AnimatorListener` to "this" as
         * well so that we are sent events through the life of an animation, such as start, repeat,
         * and end.
         */
        private fun createAnimation() {
            if (bounceAnim == null) {
                @SuppressLint("Recycle") // It is started in startAnimation()
                bounceAnim = ObjectAnimator.ofFloat(
                    ball,
                    "y",
                    ball.y,
                    height - BALL_SIZE
                ).setDuration(1500)
                bounceAnim!!.interpolator = BounceInterpolator()
                bounceAnim!!.addUpdateListener(this)
                bounceAnim!!.addListener(this)
            }
        }

        /**
         * First we create the `ValueAnimator bounceAnim` if it does not already exist, then
         * start it running. We are called only from the `onClick` method of the "RUN" Button.
         */
        fun startAnimation() {
            createAnimation()
            bounceAnim!!.start()
        }

        /**
         * First we create the `ValueAnimator bounceAnim` if it does not already exist, then
         * we set the position of `bounceAnim` to the specified point in time. Called only
         * from the `onProgressChanged` callback of the `SeekBar mSeekBar`.
         *
         * @param seekTime The time, in milliseconds, to which the animation is advanced or rewound.
         */
        fun seek(seekTime: Long) {
            createAnimation()
            bounceAnim!!.currentPlayTime = seekTime
        }

        /**
         * Creates and returns a `ShapeHolder` holding a "ball". First it creates an
         * `OvalShape circle`, re-sizes it to be a 50px by 50px circle, creates a
         * `ShapeDrawable drawable` from it, and places it in a `ShapeHolder shapeHolder`.
         * It sets the (x,y) coordinates of the `ShapeHolder` to the calling parameters of the
         * method, generates a random color and a dark version of that color and creates a
         * `RadialGradient gradient` from them which it sets as the shader of the paint which
         * it fetches from `drawable` and then assigns to `shapeHolder`. It then returns
         * the ShapeHolder it has created and initialized. Note: `ArrayList<ShapeHolder> balls`
         * has this ShapeHolder add()'ed to it, but `balls` is not used in this demo (the
         * method was copied from a multi-ball demo).
         *
         * @param x x coordinate for ShapeHolder
         * @param y y coordinate for ShapeHolder
         * @return ShapeHolder containing "ball" at (x,y)
         */
        @Suppress("SameParameterValue")
        private fun addBall(x: Float, y: Float): ShapeHolder {
            val circle = OvalShape()
            circle.resize(BALL_SIZE, BALL_SIZE)
            val drawable = ShapeDrawable(circle)
            val shapeHolder = ShapeHolder(drawable)
            shapeHolder.x = x
            shapeHolder.y = y
            val red = (100 + Math.random() * 155).toInt()
            val green = (100 + Math.random() * 155).toInt()
            val blue = (100 + Math.random() * 155).toInt()
            val color = -0x1000000 or (red shl 16) or (green shl 8) or blue
            val paint = drawable.paint
            val darkColor = -0x1000000 or (red / 4 shl 16) or (green / 4 shl 8) or blue / 4
            val gradient = RadialGradient(
                37.5f, 12.5f,
                50f, color, darkColor, Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            shapeHolder.paint = paint
            balls.add(shapeHolder)
            return shapeHolder
        }

        /**
         * Called to do the drawing of our view. First we pre-concatenate the current matrix
         * with a translation to the current (x,y) position of `ShapeHolder ball`, then we
         * instruct the `ShapeDrawable` we retrieve from `ball` to draw itself.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.translate(ball.x, ball.y)

            ball.shape!!.draw(canvas)
        }

        /**
         * This is the callback for the interface `AnimatorUpdateListener`, it is called to
         * notify us of the occurrence of another frame of the animation. First we `invalidate`
         * the View ensuring that our `onDraw` override will be called at some point in the
         * future, then we fetch the current position of the animation in time, which is equal to
         * the current time minus the time that the animation started BUT do nothing with it (the
         * commented out line would have set the SeekBar mSeekBar to this value.)
         *
         * @param animation The animation which has moved to a new frame
         */
        override fun onAnimationUpdate(animation: ValueAnimator) {
            invalidate()
            @Suppress("UNUSED_VARIABLE", "unused")
            val playtime = bounceAnim!!.currentPlayTime
            //mSeekBar.setProgress((int)playtime);
        }

        /**
         * Part of the `AnimatorListener` interface. Notifies the cancellation of the animation.
         * We do nothing.
         *
         * @param animation The animation which was canceled
         */
        override fun onAnimationCancel(animation: Animator) {}

        /**
         * Part of the `AnimatorListener` interface. Notifies the end of the animation.
         * For no apparent reason we remove the ball whose animation has ended from the unused
         * `ArrayList<ShapeHolder> balls`.
         *
         * @param animation The animation which reached its end.
         */
        override fun onAnimationEnd(animation: Animator) {

            balls.remove((animation as ObjectAnimator).target) // Useless relic of Cut and paste?
            Log.i(TAG, "onAnimationEnd called")
        }

        /**
         * Part of the `AnimatorListener` interface. Notifies the repetition of the animation.
         * We do nothing.
         *
         * @param animation The animation which was repeated.
         */
        override fun onAnimationRepeat(animation: Animator) {}

        /**
         * Part of the `AnimatorListener` interface. Notifies the start of the animation.
         * We only log the message "onAnimationStart called".
         *
         * @param animation The started animation.
         */
        override fun onAnimationStart(animation: Animator) {
            Log.i(TAG, "onAnimationStart called")
        }
    }

    companion object {
        /**
         * Maximum value to use for our `SeekBar`.
         */
        private const val DURATION = 1500

        /**
         * TAG used for logging.
         */
        private const val TAG = "AnimationSeeking"

        /**
         * Ball size in pixels
         */
        private const val BALL_SIZE = 100f
    }
}