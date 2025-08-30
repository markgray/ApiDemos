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
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.withTranslation
import com.example.android.apis.R

/**
 * Uses several different kinds of ObjectAnimator to animate bouncing color changing balls.
 * When onTouchEvent is called with either a MotionEvent.ACTION_DOWN or MotionEvent.ACTION_MOVE
 * a ball of random color is added at the events event.getX(), event.getY() coordinates.
 * The ball motion and geometry is animated then an animator of the balls alpha is played
 * fading it out from an alpha of 1.0 to 0.0 in 250 milliseconds The onAnimationEnd callback
 * of the fade animation is set to an AnimatorListenerAdapter which removes the ball when
 * the animation is done.
 */
@SuppressLint("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.HONEYCOMB)
class BouncingBalls : AppCompatActivity() {

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to the layout file R.layout.bouncing_balls, locate the LinearLayout
     * within the layout with id R.id.container and add a new instance of `MyAnimationView` to it.
     *
     * @param savedInstanceState Always null since onSaveInstanceState is never called
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bouncing_balls)
        val container = findViewById<LinearLayout>(R.id.container)
        container.addView(MyAnimationView(this))
    }

    /**
     * This class does all the work of creating and animating the bouncing balls. The balls
     * are placed inside an .animation.ShapeHolder as they are created and an animation set is
     * used to perform animation operations on that ShapeHolder.
     * Our constructor. First we call our super's constructor. We initialize `ValueAnimator colorAnim`
     * with an `ObjectAnimator` that animates between int values of the "backgroundColor" property
     * value of this `View` between RED and BLUE. Set its duration to 3000 milliseconds, set the
     * evaluator to be used when calculating its animated values to a new instance of `ArgbEvaluator`
     * (performs type interpolation between integer values that represent ARGB colors), set its repeat
     * count to INFINITE, its repeat mode to REVERSE and then start it running.
     *
     * @param context `Context` to use to access resources, *this* in the `onCreate`
     * override of `BouncingBalls`.
     */
    inner class MyAnimationView(context: Context) : View(context) {

        /**
         * `ArrayList` holding all our balls, each inside its own `ShapeHolder` container.
         */
        val balls: ArrayList<ShapeHolder> = ArrayList()

        init {

            // Animate background color
            // Note that setting the background color will automatically invalidate the
            // view, so that the animated color, and the bouncing balls, get redisplayed on
            // every frame of the animation.
            val colorAnim = ObjectAnimator.ofInt(
                /* target = */ this,
                /* propertyName = */ "backgroundColor",
                /* ...values = */ RED,
                BLUE
            )
            colorAnim.duration = 3000
            colorAnim.setEvaluator(ArgbEvaluator())
            colorAnim.repeatCount = ValueAnimator.INFINITE
            colorAnim.repeatMode = ValueAnimator.REVERSE
            colorAnim.start()
        }

        /**
         * When a touch event occurs this routine is called to handle it. It handles only
         * MotionEvent.ACTION_DOWN and MotionEvent.ACTION_MOVE events, checks for these and
         * returns false (event not handled) if it is a different type of event. Otherwise it
         * spawns a new ball in a ShapeHolder container at the .getX() and .getY() of the event
         * and sets up a complex animation set (animatorSet) to be performed on the ShapeHolder
         * which is then .start()'ed before returning 'true' to indicate the event has been
         * handled.
         *
         * @param event The motion event.
         * @return True if the event was handled, false otherwise.
         */
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action != MotionEvent.ACTION_DOWN && event.action != MotionEvent.ACTION_MOVE) {
                return false
            }
            val newBall = addBall(event.x, event.y)

            // Bouncing animation with squash and stretch
            val startY = newBall.y
            val endY = height - 50f
            val h = height.toFloat()
            val eventY = event.y
            val duration = (500 * ((h - eventY) / h)).toInt()
            val bounceAnim = ObjectAnimator.ofFloat(newBall, "y", startY, endY)
            bounceAnim.duration = duration.toLong()
            bounceAnim.interpolator = AccelerateInterpolator()
            val squashAnim1 = ObjectAnimator.ofFloat(
                newBall, "x", newBall.x,
                newBall.x - 25f
            )
            squashAnim1.duration = (duration / 4).toLong()
            squashAnim1.repeatCount = 1
            squashAnim1.repeatMode = ValueAnimator.REVERSE
            squashAnim1.interpolator = DecelerateInterpolator()
            val squashAnim2 = ObjectAnimator.ofFloat(
                newBall, "width", newBall.width,
                newBall.width + 50
            )
            squashAnim2.duration = (duration / 4).toLong()
            squashAnim2.repeatCount = 1
            squashAnim2.repeatMode = ValueAnimator.REVERSE
            squashAnim2.interpolator = DecelerateInterpolator()
            val stretchAnim1 = ObjectAnimator.ofFloat(
                newBall, "y", endY,
                endY + 25f
            )
            stretchAnim1.duration = (duration / 4).toLong()
            stretchAnim1.repeatCount = 1
            stretchAnim1.interpolator = DecelerateInterpolator()
            stretchAnim1.repeatMode = ValueAnimator.REVERSE
            val stretchAnim2 = ObjectAnimator.ofFloat(
                newBall, "height",
                newBall.height, newBall.height - 25
            )
            stretchAnim2.duration = (duration / 4).toLong()
            stretchAnim2.repeatCount = 1
            stretchAnim2.interpolator = DecelerateInterpolator()
            stretchAnim2.repeatMode = ValueAnimator.REVERSE
            val bounceBackAnim = ObjectAnimator.ofFloat(
                newBall, "y", endY,
                startY
            )
            bounceBackAnim.duration = duration.toLong()
            bounceBackAnim.interpolator = DecelerateInterpolator()
            // Sequence the down/squash&stretch/up animations
            val bouncer = AnimatorSet()
            bouncer.play(bounceAnim).before(squashAnim1)
            bouncer.play(squashAnim1).with(squashAnim2)
            bouncer.play(squashAnim1).with(stretchAnim1)
            bouncer.play(squashAnim1).with(stretchAnim2)
            bouncer.play(bounceBackAnim).after(stretchAnim2)

            // Fading animation - remove the ball when the animation is done
            val fadeAnim = ObjectAnimator.ofFloat(newBall, "alpha", 1f, 0f)
            fadeAnim.duration = 250
            fadeAnim.addListener(object : AnimatorListenerAdapter() {
                /**
                 * Notifies the end of the animation. This callback is not invoked for animations
                 * with repeat count set to INFINITE. We use the `getTarget` method of our
                 * parameter `Animator animation` to retrieve the target object whose property
                 * is being animated by this animation, then call the `remove` method of
                 * `ArrayList<ShapeHolder> balls` to remove it from the list.
                 *
                 * @param animation The animation which reached its end.
                 */
                override fun onAnimationEnd(animation: Animator) {

                    balls.remove((animation as ObjectAnimator).target)

                }
            })

            // Sequence the two animations to play one after the other
            val animatorSet = AnimatorSet()
            animatorSet.play(bouncer).before(fadeAnim)

            // Start the animation
            animatorSet.start()

            return true
        }

        /**
         * Add a ball to the list of ArrayList<ShapeHolder> balls at location (x, y).
         * First create a ShapeDrawable of an OvalShape .resize()'d to 50px x 50px, create
         * a ShapeHolder containing this ShapeDrawable and configure that ShapeHolder to locate
         * it at (x, y), create a Paint with a random color, create a RadialGradient and install
         * it in the Paint, then setPaint() the ShapeHolder with this paint. When done, add the
         * new ball to the balls list, and return the ShapeHolder to the caller.
         *
         * @param x x coordinate of the new ball
         * @param y y coordinate of the new ball
         * @return a ShapeHolder containing a ball located at (x, y)
        </ShapeHolder> */
        private fun addBall(x: Float, y: Float): ShapeHolder {
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
            balls.add(shapeHolder)
            return shapeHolder
        }

        /**
         * Does the drawing of each of of the balls every time the canvas in invalidated. It does
         * this by first saving the current matrix and clip onto a private stack using canvas.save(),
         * then it moves the canvas to the location of the current ball, calls the .draw(Canvas) of
         * the ball's shape to draw it, and then restores the canvas from the stack (repeat for each
         * ball in `ArrayList<ShapeHolder> balls`.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            for (i in balls.indices) {
                val shapeHolder = balls[i]
                canvas.withTranslation(x = shapeHolder.x, y = shapeHolder.y) {
                    shapeHolder.shape!!.draw(this)
                }
            }
        }
    }

    companion object {

        /**
         * The background color property of our `MyAnimationView` is animated between this
         * color and BLUE
         */
        private const val RED = -0x7f80

        /**
         * The background color property of our `MyAnimationView` is animated between this
         * color and RED
         */
        private const val BLUE = -0x7f7f01

        /**
         * TAG that could be used for logging (but isn't).
         */
        @Suppress("unused")
        private const val TAG = "BouncingBalls"
    }
}