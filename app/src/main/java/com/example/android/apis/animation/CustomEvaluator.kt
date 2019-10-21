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

@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "MemberVisibilityCanBePrivate")

package com.example.android.apis.animation

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
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
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Implements the TypeEvaluator interface to animate using a custom:
 * "public Object evaluate(float fraction, Object startValue, Object endValue)"
 * function. The x and y coordinates of an "animation.ShapeHolder ball" are
 * animated by calling evaluate, and onAnimationUpdate is called which calls
 * invalidate() which causes the onDraw method to be called.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class CustomEvaluator : AppCompatActivity() {

    /**
     * Logical density of our display.
     */
    internal var mDensity: Float = 0.toFloat()

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file R.layout.animator_custom_evaluator. We initialize
     * our field `mDensity` with the logical density of our display, then initialize `LinearLayout container`
     * by finding the view with id R.id.container. Next we create a new instance of `MyAnimationView` and
     * add it to `container`. We initialize `Button starter` by finding the view with id
     * R.id.startButton and set its `OnClickListener` to a lambda which calls the `startAnimation`
     * method of `animView` to start the animation running.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animator_custom_evaluator)

        mDensity = resources.displayMetrics.density

        val container = findViewById<LinearLayout>(R.id.container)
        val animView = MyAnimationView(this)
        container.addView(animView)

        val starter = findViewById<Button>(R.id.startButton)
        starter.setOnClickListener {
            animView.startAnimation()
        }
    }

    /**
     * Class used to hold x, y coordinates
     */
    class XYHolder
    /**
     * Create a XYHolder at x,y
     *
     * @param x coordinate
     * @param y coordinate
     */
    (
            /**
             * X coordinate of this XYHolder
             */
            var x: Float,
            /**
             * Y coordinate of this XYHolder
             */
            var y: Float)

    /**
     * Interface for use with the setEvaluator(TypeEvaluator) function. Evaluators allow developers
     * to create animations on arbitrary property types, by allowing them to supply custom
     * evaluators for types that are not automatically understood and used by the animation system.
     */
    inner class XYEvaluator : TypeEvaluator<XYHolder> {

        /**
         * This function returns the result of linearly interpolating the start and end values,
         * with fraction representing the proportion between the start and end values.
         * The calculation is a simple parametric calculation: result = x0 + t * (x1 - x0),
         * where x0 is startValue, x1 is endValue, and t is fraction.
         *
         * @param fraction   The fraction from the starting to the ending values
         * @param startValue The start value.
         * @param endValue   The end value.
         * @return A linear interpolation between the start and end values, given the
         * **fraction** parameter.
         */
        override fun evaluate(fraction: Float, startValue: XYHolder, endValue: XYHolder): XYHolder {
            return XYHolder(startValue.x + fraction * (endValue.x - startValue.x),
                    startValue.y + fraction * (endValue.y - startValue.y))
        }
    }

    /**
     * Class designed to hold a ball's ShapeHolder (and not much else)
     */
    inner class BallXYHolder
    (
            /**
             * Our `ShapeHolder`
             */
            private val mBall: ShapeHolder) {

        /**
         * Constructs and returns a new instance of `XYHolder` containing the x and y
         * coordinates of our `ShapeHolder mBall` it is used by ObjectAnimator behind our backs.
         */
        @Suppress("unused") // it is used by ObjectAnimator behind our backs.
        var xY: XYHolder
            get() = XYHolder(mBall.x, mBall.y)
            set(xyHolder) {
                mBall.x = xyHolder.x
                mBall.y = xyHolder.y
            }
    }

    /**
     * View which contains our animation
     */
    inner class MyAnimationView
    /**
     * Our constructor. First we call our super's constructor, then we call our method
     * `createBall` to create a `ShapeHolder` holding a ball which is 25px by 25px
     * which we use to initialize our field `ShapeHolder ball`. Then we initialize our
     * field `BallXYHolder ballHolder` with a new instance holding `ball`.
     *
     * @param context Context which in our case is derived from super of Activity
     */
    (context: Context) : View(context), ValueAnimator.AnimatorUpdateListener {

        /**
         * The `ValueAnimator` (XYEvaluator TypeEvaluator) we are using to animate our ball.
         */
        internal var bounceAnim: ValueAnimator? = null
        /**
         * The `ShapeHolder` holding our ball
         */
        internal var ball: ShapeHolder
        /**
         * `BallXYHolder` holding the `ShapeHolder` holding our ball
         */
        internal var ballHolder: BallXYHolder

        init {
            ball = createBall(25f, 25f)
            ballHolder = BallXYHolder(ball)
        }

        /**
         * If this is the first time the animation has run, a ValueAnimator is created that animates
         * between Object values and saved in our field `ValueAnimator bounceAnim`. We initialize
         * `XYHolder startXY` with a new instance positioned at (0,0), and `XYHolder endXY`
         * with a new instance positioned at (300,500). We then initialize `bounceAnim` with an
         * an `ObjectAnimator` that animates the "xY" property of `BallXYHolder ballHolder`
         * between `startXY` and `endXY` using a new instance of `XYEvaluator` as the
         * `TypeEvaluator` that will be called on each animation frame to provide the necessary
         * interpolation between the Object values to derive the animated value. Then we set the duration
         * of the animation to 1500 milliseconds, and add *this* as an `AnimatorUpdateListener`
         * (our `onAnimationUpdate` override will be called on every animation frame, after the
         * current frame's values have been calculated)
         */
        private fun createAnimation() {
            if (bounceAnim == null) {
                val startXY = XYHolder(0f, 0f)
                val endXY = XYHolder(300f, 500f)
                bounceAnim = ObjectAnimator.ofObject(ballHolder, "xY",
                        XYEvaluator(), startXY, endXY)
                bounceAnim!!.duration = 1500
                bounceAnim!!.addUpdateListener(this)
            }
        }

        /**
         * Called when the PLAY button is clicked, it first creates the animation (if this is the
         * first time the button is clicked), and then starts the animation running.
         */
        fun startAnimation() {
            createAnimation()
            bounceAnim!!.start()
        }

        /**
         * Creates a ball at coordinates x, y. The ball is constructed of an OvalShape resized
         * to 50px x 50px, placed in a ShapeDrawable and that ShapeDrawable is used in creating
         * a ShapeHolder to hold it. The ShapeHolder has its x and y coordinates set to the
         * method's arguments x,y. Random colors and a RadialGradient are used to initialize a
         * Paint and that Paint is stored in the ShapeHolder. Finally we return that
         * `ShapeHolder` to the caller.
         *
         * @param x x coordinate for ball
         * @param y y coordinate for ball
         * @return ShapeHolder containing the new ball
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
            val gradient = RadialGradient(37.5f, 12.5f,
                    50f, color, darkColor, Shader.TileMode.CLAMP)
            paint.shader = gradient
            shapeHolder.paint = paint
            return shapeHolder
        }

        /**
         * This callback draws the MyAnimationView after every invalidate() call. The current
         * matrix and clip are saved onto a private stack, the current matrix is scaled by the
         * screen density `mDensity`, then the current matrix is pre-concatenated with a
         * translation to the coordinate x, y of the ball's ShapeHolder, and the ShapeDrawable
         * in the ShapeHolder is told to draw itself. Canvas.restore() then removes all
         * modifications to the matrix/clip state.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.save()
            canvas.scale(mDensity, mDensity)
            canvas.translate(ball.x, ball.y)

            ball.shape!!.draw(canvas)
            canvas.restore()
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
        override fun onAnimationUpdate(animation: ValueAnimator) {
            invalidate()
        }

    }
}