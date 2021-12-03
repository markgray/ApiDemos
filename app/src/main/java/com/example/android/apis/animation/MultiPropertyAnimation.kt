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
import android.animation.AnimatorSet
import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import java.util.ArrayList

/**
 * Uses AnimatorSet.playTogether(Animator... items) to play four different
 * animations at once: yBouncer, yAlphaBouncer, whxyBouncer, and yxBouncer,
 * all of which are set up in the method createAnimation().
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class MultiPropertyAnimation : AppCompatActivity() {

    /**
     * Called when the activity is starting. Sets the content view to the layout file
     * R.layout.animation_multi_property, locates the LinearLayout container with the
     * id R.id.container, creates an instance of MyAnimationView animView, and .addView()'s
     * it to container. Locates the RUN Button (Button starter at R.id.startButton) and sets
     * the OnClickListener to start the animation running.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animation_multi_property)
        val container = findViewById<LinearLayout>(R.id.container)
        val animView = MyAnimationView(this)
        container.addView(animView)

        val starter = findViewById<Button>(R.id.startButton)
        starter.setOnClickListener {
            animView.startAnimation()
        }

    }

    /**
     * This is the View that the demo runs in. It consists of 4 balls with different animations
     * assigned to each of them: yBouncer, yAlphaBouncer, whxyBouncer, and yxBouncer.
     */
    inner class MyAnimationView
    /**
     * Adds four balls to the `ArrayList<ShapeHolder> balls`.
     *
     * @param context the MultiPropertyAnimation Activity context
     */
    (context: Context) : View(context), ValueAnimator.AnimatorUpdateListener {

        val balls = ArrayList<ShapeHolder>()
        internal var animation: AnimatorSet? = null
        internal var bounceAnim: Animator? = null
        internal var ball: ShapeHolder? = null

        init {
            addBall(50f, 0f)
            addBall(150f, 0f)
            addBall(250f, 0f)
            addBall(350f, 0f)
        }

        /**
         * Create the animations used for the four balls.
         *  - `balls[0]` ObjectAnimator yBouncer is an ObjectAnimator of the "y" coordinate of the
         * ShapeHolder holding balls[0]. It uses a BounceInterpolator with a Duration
         * of "DURATION" (1500 milliseconds). It results in a ball which falls from
         * the top of the screen and bounces several times at the bottom of the screen.
         * The AnimatorUpdateListener is set to "this" and the overridden callback
         * this.onAnimationUpdate merely invalidates the View causing our onDraw callback
         * to be called for every frame of this animation in order to draw all four balls.
         *  - `balls[1]` ObjectAnimator yAlphaBouncer is an ObjectAnimator constructed from two
         * PropertyValuesHolder's pvhY (animates "y" from the top to bottom of the View)
         * and pvhAlpha (animates "alpha" from 1.0f to 0.0f). Its duration is set to
         * "DURATION/2" (750 milliseconds), its TimeInterpolator is set to an instance
         * of AccelerateInterpolator, its repeat count is 1, and the repeat mode is REVERSE.
         * This results in a ball which drops from the top to the bottom while fading out
         * at the same time, then returning to the top while fading in.
         *  - `balls[2]` ObjectAnimator whxyBouncer is an ObjectAnimator created from 4 PropertyValuesHolder's
         * pvhW ("width"), pvhH ("height"), pvTX ("x"), and pvTY ("y") using the method
         * ObjectAnimator.ofPropertyValuesHolder with a duration of DURATION/2 (750
         * milliseconds), a repeat count of 1 and a repeat mode of REVERSE. It has the
         * effect of expanding the size of the ball then shrinking it back to the
         * original size without moving.
         *  - `balls[3]` ObjectAnimator yxBouncer is an ObjectAnimator created from PropertyValuesHolder's
         * for "x" and "y" (pvhX and pvhY). pvhY animates "y" from the top to the bottom
         * of the View using the default linear animation, pvhX is a PropertyValuesHolder
         * created from three Keyframe's (kf0, kf1, and kf2) which divides the animation of
         * "x" into three segments moving the "x" coordinate of the ball's ShapeHolder from
         * the current position to 100f pixels to right, to 50f pixels to the right. The
         * duration of yxBouncer is set to DURATION/2 (750 milliseconds), the repeat count
         * is 1, and the repeat mode is REVERSE. The result is a ball which falls from the
         * top of the View to the bottom with a slight jog to the right, and then rises
         * back to the top using the same track.
         *
         * Then the field bounceAnim is set to an instance of AnimatorSet with all four ball
         * animations set up to play at the same time.
         */
        private fun createAnimation() {
            if (bounceAnim == null) {
                var ball: ShapeHolder = balls[0]
                val yBouncer = ObjectAnimator.ofFloat(
                        ball,
                        "y",
                        ball.y,
                        height - BALL_SIZE
                ).setDuration(DURATION.toLong())
                yBouncer.interpolator = BounceInterpolator()
                yBouncer.addUpdateListener(this)

                ball = balls[1]
                var pvhY = PropertyValuesHolder.ofFloat(
                        "y",
                        ball.y,
                        height - BALL_SIZE
                )
                val pvhAlpha = PropertyValuesHolder.ofFloat(
                        "alpha",
                        1.0f,
                        0f
                )
                val yAlphaBouncer = ObjectAnimator.ofPropertyValuesHolder(
                        ball,
                        pvhY,
                        pvhAlpha
                ).setDuration((DURATION / 2).toLong())
                yAlphaBouncer.interpolator = AccelerateInterpolator()
                yAlphaBouncer.repeatCount = 1
                yAlphaBouncer.repeatMode = ValueAnimator.REVERSE


                ball = balls[2]
                val pvhW = PropertyValuesHolder.ofFloat(
                        "width",
                        ball.width, ball.width * 2
                )
                val pvhH = PropertyValuesHolder.ofFloat(
                        "height",
                        ball.height,
                        ball.height * 2
                )
                val pvTX = PropertyValuesHolder.ofFloat(
                        "x",
                        ball.x,
                        ball.x - BALL_SIZE / 2f
                )
                val pvTY = PropertyValuesHolder.ofFloat(
                        "y",
                        ball.y,
                        ball.y - BALL_SIZE / 2f
                )
                val whxyBouncer = ObjectAnimator.ofPropertyValuesHolder(
                        ball, pvhW,
                        pvhH,
                        pvTX,
                        pvTY
                ).setDuration((DURATION / 2).toLong())
                whxyBouncer.repeatCount = 1
                whxyBouncer.repeatMode = ValueAnimator.REVERSE

                ball = balls[3]
                pvhY = PropertyValuesHolder.ofFloat(
                        "y",
                        ball.y,
                        height - BALL_SIZE
                )
                val ballX = ball.x
                val kf0 = Keyframe.ofFloat(0f, ballX)
                val kf1 = Keyframe.ofFloat(.5f, ballX + 100f)
                val kf2 = Keyframe.ofFloat(1f, ballX + 50f)
                val pvhX = PropertyValuesHolder.ofKeyframe(
                        "x",
                        kf0,
                        kf1,
                        kf2
                )
                val yxBouncer = ObjectAnimator.ofPropertyValuesHolder(
                        ball,
                        pvhY,
                        pvhX
                ).setDuration((DURATION / 2).toLong())
                yxBouncer.repeatCount = 1
                yxBouncer.repeatMode = ValueAnimator.REVERSE

                @SuppressLint("Recycle")
                bounceAnim = AnimatorSet()
                (bounceAnim as AnimatorSet).playTogether(
                        yBouncer,
                        yAlphaBouncer,
                        whxyBouncer,
                        yxBouncer
                )
            }
        }

        /**
         * Called from the onClick of the RUN button, this method creates the animations (if this
         * is the first time) and starts the animations running.
         */
        fun startAnimation() {
            createAnimation()
            bounceAnim!!.start()
        }

        /**
         * Creates a circle, and places it in a ShapeHolder that it configures with the (x,y)
         * coordinates that it is called with and a random color, then adds that ShapeHolder
         * to the `ArrayList<ShapeHolder> balls`, returning the new ShapeHolder to allow chaining.
         *
         * @param x x coordinate of ball
         * @param y y coordinate of ball
         * @return ShapeHolder containing a ball at (x,y) with a random color.
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
            val gradient = RadialGradient(37.5f, 12.5f,
                    50f, color, darkColor, Shader.TileMode.CLAMP)
            paint.shader = gradient
            shapeHolder.paint = paint
            balls.add(shapeHolder)
            return shapeHolder
        }

        /**
         * Called when the View needs to draw itself. For each of the ShapeHolder ball's in the
         * `ArrayList<ShapeHolder> balls` the canvas has a translation to the current (x,y) ball
         * location pre-concatenated to it (the x,y coordinates are fetched from the ShapeHolder),
         * the Shape contained in the ShapeHolder is then fetched and instructed to draw itself.
         * The canvas is then restored to its previous state by pre-concatenating a translation
         * that is the inverse of the previous one that moved the canvas to the ball's (x,y)
         * location.
         *
         * @param canvas Canvas that we will draw our View on
         */
        override fun onDraw(canvas: Canvas) {
            for (ball in balls) {
                canvas.translate(ball.x, ball.y)

                ball.shape!!.draw(canvas)
                canvas.translate(-ball.x, -ball.y)
            }
        }

        /**
         * This callback is called to notify us of the occurrence of another frame of an animation,
         * and is called by the animation used for balls[0] because of the anim.addUpdateListener(this)
         * included in the creation of the animation. We merely invalidate() the View so that onDraw()
         * will be called at some point in the future.
         *
         * @param animation animation which has moved to a new frame
         */
        override fun onAnimationUpdate(animation: ValueAnimator) {
            invalidate()
        }
    }

    companion object {

        private const val DURATION = 1500
        private const val BALL_SIZE = 100f
    }
}