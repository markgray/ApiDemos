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

import android.animation.*
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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
import java.util.*

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
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class AnimationLoading : AppCompatActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.animation_loading.
     * We initialize `LinearLayout container` by finding the view with id R.id.container,
     * initialize `MyAnimationView animView` with a new instance and then add it to
     * `container`. We initialize `Button starter` by finding the view with id
     * R.id.startButton ("Run") and set its `OnClickListener` to an anonymous class which calls
     * the `startAnimation` method of `animView` to start its animation.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animation_loading)

        val container = findViewById<LinearLayout>(R.id.container)
        val animView = MyAnimationView(this)
        container.addView(animView)

        val starter = findViewById<Button>(R.id.startButton)
        starter.setOnClickListener {
            animView.startAnimation()
        }
    }

    /**
     * This is the custom View which contains our animation demonstration
     */
    inner class MyAnimationView
    /**
     * Creates the eight balls (0-7) which are in our View. (Two of the balls (5 and 7) start in
     * the same place). The method `addBall` is called with the x and y coordinates for the
     * call being created with the 2 argument version assigning a random color to the ball for
     * balls 0, 1, 2, 4, 5, 6 and the 3 argument version assigning the color GREEN to ball 3 and
     * YELLOW to ball 7.
     *
     * @param context The Context the view is running in, through which it can access the
     * current theme, resources, etc., "this" in the `onCreate` method
     * of the `AnimationLoading` activity.
     */
    (context: Context) : View(context), ValueAnimator.AnimatorUpdateListener {

        /**
         * List holding the `ShapeHolder` objects which hold the balls
         */
        val balls = ArrayList<ShapeHolder>()
        /**
         * `AnimatorSet` which holds all the animations for the 8 balls we animate
         */
        internal var animation: Animator? = null

        init {
            addBall(50f, 50f)
            addBall(200f, 50f)
            addBall(350f, 50f)
            addBall(500f, 50f, Color.GREEN)
            addBall(650f, 50f)
            addBall(800f, 50f)
            addBall(950f, 50f)
            addBall(800f, 50f, Color.YELLOW)
        }

        /**
         * Loads, creates and configures the Animator animation used for the 8 balls. If this is
         * the first time it is called (animation == null) it creates animators for the balls as
         * follows:
         *  - balls[0] (50,50) Uses an ObjectAnimator anim created by loading the animation from
         *  the file R.animator.object_animator which animates "y" from the starting point
         *  to 200 with a repeat count of 1 and a repeatMode of "reverse", uses "this" as
         *  the UpdateListener which causes our classes override of onAnimationUpdate to
         *  be called which invalidates the View and sets the "y" value of the ShapeHolder
         *  holding balls[0] to the current value of the animation.
         *  - balls[1] (200,50) Uses a ValueAnimator fader which it creates by loading the file
         *  R.animator.animator which animates a value from 1 to 0 with a repeat count
         *  of 1 and a repeatMode of "reverse", and sets the UpdateListener to an
         *  AnimatorUpdateListener which sets the alpha of the ShapeHolder holding
         *  balls[1] to the current value of the animation (relying on the call to
         *  invalidate() for balls[0] to trigger a re-draw of the View.)
         *  - balls[2] (350,50) Uses an AnimatorSet seq which it creates by loading the file
         *  R.animator.animator_set which creates two objectAnimator's to animate the "x"
         *  value from the current value to 200, and the "y" value from the current value
         *  to 400 with a repeat count of 1 and a repeatMode of "reverse"
         *  - balls[3] (500,50) Color.GREEN Uses an ObjectAnimator colorizer which it creates by
         *  loading the file R.animator.color_animator which animates the value "color"
         *  of the ShapeHolder holding balls[3] from "#0f0" to "#00ffff" with a repeat
         *  count of 1 and a repeatMode of "reverse"
         *  - balls[4] (650,50) Use an ObjectAnimator animPvh which it loads from the file
         *  R.animator.object_animator_pvh which animates "x" from 0 to 400, and "y"
         *  from 0 to 200 using propertyValuesHolder's
         *  - balls[5] (800,50) Uses an ObjectAnimator animPvhKf which it creates by loading the file
         *  R.animator.object_animator_pvh_kf which uses propertyValuesHolder to hold
         *  keyframe specs for x and y and uses the default linear interpolator
         *  - balls[6] (950,50) Uses a ValueAnimator faderKf which it loads from the file
         *  R.animator.value_animator_pvh_kf which uses propertyValuesHolder to hold
         *  keyframe specs for a value, it then sets the UpdateListener to an
         *  AnimatorUpdateListener which sets the alpha of the ShapeHolder holding balls[6]
         *  to the current animated value.
         *  - balls[7] (800,50) Color.YELLOW Uses an ObjectAnimator animPvhKfInterpolated which
         *  it loads from R.animator.object_animator_pvh_kf_interpolated which uses
         *  propertyValuesHolder's to hold keyframe specs for "x" and "y" and has an
         *  accelerate interpolator applied on each keyframe interval. In comparison,
         *  the animation defined in R.anim.object_animator_pvh_kf for balls[5] uses
         *  the default linear interpolator throughout the animation. As these two
         *  animations use the exact same path, the effect of the per-keyframe interpolator
         *  has been made obvious.
         *
         * It then creates the `AnimatorSet animation` configures it to playTogether the
         * 8 Animator's created for the 8 balls.
         */
        private fun createAnimation() {
            val appContext = this@AnimationLoading

            if (animation == null) {
                val anim = AnimatorInflater.loadAnimator(appContext, R.animator.object_animator) as ObjectAnimator
                anim.addUpdateListener(this)
                anim.target = balls[0]

                val fader = AnimatorInflater.loadAnimator(appContext, R.animator.animator) as ValueAnimator
                fader.addUpdateListener { animation -> balls[1].setAlpha(animation.animatedValue as Float) }

                val seq = AnimatorInflater.loadAnimator(appContext,
                        R.animator.animator_set) as AnimatorSet
                seq.setTarget(balls[2])

                val colorizer = AnimatorInflater.loadAnimator(appContext, R.animator.color_animator) as ObjectAnimator
                colorizer.target = balls[3]

                val animPvh = AnimatorInflater.loadAnimator(appContext, R.animator.object_animator_pvh) as ObjectAnimator
                animPvh.target = balls[4]


                val animPvhKf = AnimatorInflater.loadAnimator(appContext, R.animator.object_animator_pvh_kf) as ObjectAnimator
                animPvhKf.target = balls[5]

                val faderKf = AnimatorInflater.loadAnimator(appContext, R.animator.value_animator_pvh_kf) as ValueAnimator
                faderKf.addUpdateListener { animation -> balls[6].setAlpha(animation.animatedValue as Float) }

                // This animation has an accelerate interpolator applied on each
                // keyframe interval. In comparison, the animation defined in
                // R.anim.object_animator_pvh_kf uses the default linear interpolator
                // throughout the animation. As these two animations use the
                // exact same path, the effect of the per-keyframe interpolator
                // has been made obvious.
                val animPvhKfInterpolated = AnimatorInflater.loadAnimator(appContext, R.animator.object_animator_pvh_kf_interpolated) as ObjectAnimator
                animPvhKfInterpolated.target = balls[7]

                animation = AnimatorSet()
                (animation as AnimatorSet).playTogether(anim, fader, seq, colorizer, animPvh,
                        animPvhKf, faderKf, animPvhKfInterpolated)

            }
        }

        /**
         * Calls `createAnimation()` to create the `AnimatorSet animation` (if it does
         * not exist yet) then calls `animation.start()` to start the animation running. Called
         * from the `onClickListener` for the "RUN" button.
         */
        fun startAnimation() {
            createAnimation()
            animation!!.start()
        }

        /**
         * Creates a ball in a `ShapeHolder`. Creates `OvalShape circle`, resize's it to
         * be a BALL_SIZE by BALL_SIZE circle (100x100), creates `ShapeDrawable drawable` from
         * `circle`, creates `ShapeHolder shapeHolder` containing `drawable`, and
         * sets the "x" and "y" coordinates of `shapeHolder` to the (x,y) arguments then returns
         * `shapeHolder` to the caller.
         *
         * @param x x coordinate for ball
         * @param y y coordinate for ball
         * @return ShapeHolder containing ball at (x, y)
         */
        private fun createBall(x: Float, y: Float): ShapeHolder {
            val circle = OvalShape()
            circle.resize(BALL_SIZE, BALL_SIZE)
            val drawable = ShapeDrawable(circle)
            val shapeHolder = ShapeHolder(drawable)
            shapeHolder.x = x
            shapeHolder.y = y
            return shapeHolder
        }

        /**
         * Adds a new ball contained in a `shapeHolder` to `ArrayList<ShapeHolder> balls`
         * at location `(x,y)`, and with Color `color`. It does this by creating a ball
         * containing `ShapeHolder shapeHolder` at `(x,y)` by calling `createBall(x, y)`,
         * sets the color of `shapeHolder` to the `color` argument by calling the
         * `setColor` method of `shapeHolder` and finally adds `shapeHolder` to
         * `balls`.
         *
         * @param x     x coordinate for ball
         * @param y     y coordinate for ball
         * @param color color of ball
         */
        @Suppress("SameParameterValue")
        private fun addBall(x: Float, y: Float, color: Int) {
            val shapeHolder = createBall(x, y)
            shapeHolder.color = color
            balls.add(shapeHolder)
        }

        /**
         * Adds a new ball in a `ShapeHolder` to `ArrayList<ShapeHolder> balls` at
         * location `(x,y)`, with a random color. It does this by creating a ball inside
         * `ShapeHolder shapeHolder` at `(x,y)` by calling createBall(x, y). It then
         * creates a random `int red` between 100 and 255, a random `int green` between
         * 100 and 255, and a random `int blue` between 100 and 255. It then shifts them into
         * the appropriate bit positions for a 32 bit color and or'd the three colors along with a
         * maximum alpha field to form the color `int color`. It initializes `Paint paint`
         * by fetching the paint from the `ShapeDrawable` of `shapeHolder`. It creates
         * `int darkColor` using the random colors `red`, `green` and `blue`
         * divided by 4 before being shifted into position and or'ed together with a maximum alpha
         * value. `RadialGradient gradient` is then created with 37.5 as the x-coordinate of
         * the center of the radius, 12.5 as the y-coordinate of the center of the radius, 50. as the
         * radius of the circle for the gradient, `color` as the color at the center of the
         * circle, `darkColor` as the color at the edge of the circle, and using CLAMP Shader
         * tiling mode (replicate the edge color if the shader draws outside of its original bounds).
         * It sets `gradient` as the shader of `paint`, adds `shapeHolder` to
         * `ArrayList<ShapeHolder> balls` and returns `shapeHolder` to the caller.
         *
         * The Paint instance "paint" is fetched from "shapeHolder" and Random colors and a
         * RadialGradient created and are used to set the Shader used by the ShapeHolder's Paint
         * instance, and the ShapeHolder is then add()'ed to the balls List.
         *
         * @param x x coordinate for ball
         * @param y y coordinate for ball
         */
        @Suppress("SameParameterValue")
        private fun addBall(x: Float, y: Float) {
            val shapeHolder = createBall(x, y)
            val red = (100 + Math.random() * 155).toInt()
            val green = (100 + Math.random() * 155).toInt()
            val blue = (100 + Math.random() * 155).toInt()
            val color = -0x1000000 or (red shl 16) or (green shl 8) or blue
            val paint = shapeHolder.shape!!.paint
            val darkColor = -0x1000000 or (red / 4 shl 16) or (green / 4 shl 8) or blue / 4
            val gradient = RadialGradient(37.5f, 12.5f,
                    50f, color, darkColor, Shader.TileMode.CLAMP)
            paint.shader = gradient
            balls.add(shapeHolder)
        }

        /**
         * Called when the View needs to draw itself. For each of the `ShapeHolder ball` objects
         * in the `ArrayList<ShapeHolder> balls` list the `Canvas canvas` argument has a
         * translation to the current (x,y) ball location pre-concatenated to it (the x,y coordinates
         * are fetched from the `ShapeHolder ball`), the `ShapeDrawable` contained in the
         * `ShapeHolder ball` is then fetched and instructed to draw itself. The canvas is then
         * restored to its previous state by pre-concatenating a translation that is the inverse of
         * the previous one that moved the canvas to the ball's (x,y) location.
         *
         * @param canvas the canvas on which the background will be drawn
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
         * and is called by the animation used for `balls[0]` because of the use of the method
         * `anim.addUpdateListener(this)` included in the creation of the animation. First we
         * call `invalidate()` to invalidate the `View` so that `onDraw()` will be
         * called at some point in the future, then we fetch the `ShapeHolder` holding
         * `balls[0]` and set the y coordinate to the current value specified by the
         * `ValueAnimator animation` argument.
         *
         * @param animation animation which has moved to a new frame
         */
        override fun onAnimationUpdate(animation: ValueAnimator) {
            invalidate()
            val ball = balls[0]
            ball.y = animation.animatedValue as Float
        }
    }

    companion object {

        @Suppress("unused")
        private const val DURATION = 1500

        /**
         * Ball size in pixels, used in the method `createBall` to resize the `OvalShape`
         * used to create the balls.
         */
        private const val BALL_SIZE = 100f
    }
}