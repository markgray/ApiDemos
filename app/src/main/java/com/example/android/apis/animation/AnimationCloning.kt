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

import android.animation.AnimatorSet
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
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.withTranslation
import com.example.android.apis.R

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
 * RequiresApi(Build.VERSION_CODES.HONEYCOMB)
 */
@Suppress("MemberVisibilityCanBePrivate")
class AnimationCloning : AppCompatActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.animation_cloning.
     * We initialize `LinearLayout container` by finding the view with id R.id.container, then
     * we initialize `MyAnimationView animView` with a new instance and add that view to
     * `container`. We initialize `Button starter` by finding the view with the id
     * R.id.startButton ("Run") and set its `OnClickListener` to a lambda which calls
     * the `startAnimation` method of `animView` which creates the animation if this is
     * the first time it is called then starts the animation running.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animation_cloning)

        val container = findViewById<LinearLayout>(R.id.container)
        val animView = MyAnimationView(this)
        container.addView(animView)

        val starter = findViewById<Button>(R.id.startButton)
        starter.setOnClickListener {
            animView.startAnimation()
        }
    }

    /**
     * A custom view with 4 animated balls in it.
     * Our constructor. First we call our super's constructor then we initialize our field
     * `float mDensity` with the logical density of the display. Then we create 4 balls
     * and add them to the `ArrayList<ShapeHolder> balls` using our method `addBall`
     *
     * @param context Context which in our case is derived from super of Activity
     */
    inner class MyAnimationView(context: Context) : View(context),
        ValueAnimator.AnimatorUpdateListener {

        /**
         * List holding our 4 balls.
         */
        val balls: ArrayList<ShapeHolder> = ArrayList()

        /**
         * `AnimatorSet` we use to hold the animations for all 4 balls, created in our method
         * `createAnimation` and started by our method `startAnimation`.
         */
        internal var animation: AnimatorSet? = null

        /**
         * logical density of the display.
         */
        private val mDensity: Float = getContext().resources.displayMetrics.density

        init {

            addBall(50f * mDensity, 25f * mDensity)
            addBall(150f * mDensity, 25f * mDensity)
            addBall(250f * mDensity, 25f * mDensity)
            addBall(350f * mDensity, 25f * mDensity)
        }

        /**
         * Creates the `AnimatorSet animation`. First we create `ObjectAnimator anim1`
         * which moves balls{0} y coordinate from 0f to the bottom of the View. We create
         * `ObjectAnimator anim2` as a clone of `anim1` and target it to balls{1}.
         * We add "this" as an `AnimatorUpdateListener` for `anim1` which causes
         * `this.onAnimationUpdate` to be called for every frame of this animation (it simply
         * calls invalidate() to cause redraw of the view). We initialize `ShapeHolder ball2`
         * with balls{2}, then create `ObjectAnimator animDown` to animate the y coordinate of
         * `ball2` from 0f to the bottom of the View and set its interpolator to an
         * `AccelerateInterpolator`. We create `ObjectAnimator animUp` to animate
         * `ball2` y coordinate from the bottom of the View to 0f and set its interpolator to
         * an `DecelerateInterpolator`. We create `AnimatorSet s1` and configure it to
         * play sequentially `animDown` followed by `animUp`. We then set the
         * `AnimatorUpdateListener` of both `animDown` and `animUp` to "this"
         * (our override of `onAnimationUpdate` will be called for every frame  of these
         * animations. We clone `s1` to initialize `AnimatorSet s2`, and its target to
         * balls{3}. We create the master `AnimatorSet animation`, and add `anim1`,
         * `anim2`, and `s1` set to play together, and `s1` and `s2` are
         * then added to play sequentially (the first three balls start their animations when the
         * "Run" button is clicked, and the fourth ball starts its animation only after the other
         * three have finished their animations).
         */
        private fun createAnimation() {
            if (animation == null) {
                val anim1 = ObjectAnimator.ofFloat(
                    balls[0], "y",
                    0f, height - balls[0].height
                ).setDuration(500)
                val anim2 = anim1.clone()
                anim2.target = balls[1]
                anim1.addUpdateListener(this)

                val ball2 = balls[2]
                val animDown = ObjectAnimator.ofFloat(
                    ball2, "y",
                    0f, height - ball2.height
                ).setDuration(500)
                animDown.interpolator = AccelerateInterpolator()
                val animUp = ObjectAnimator.ofFloat(
                    ball2, "y",
                    height - ball2.height, 0f
                ).setDuration(500)
                animUp.interpolator = DecelerateInterpolator()
                val s1 = AnimatorSet()
                s1.playSequentially(animDown, animUp)
                animDown.addUpdateListener(this)
                animUp.addUpdateListener(this)
                val s2 = s1.clone()
                s2.setTarget(balls[3])

                @SuppressLint("Recycle") // It gets started later in startAnimation()
                animation = AnimatorSet()
                animation!!.playTogether(anim1, anim2, s1)
                animation!!.playSequentially(s1, s2)
            }
        }

        /**
         * Creates, configures, then adds a randomly colored ball at coordinates `(x,y)` to our
         * field `ArrayList<ShapeHolder> balls`. First we initialize our variable
         * `OvalShape circle` with a new instance, then we resize it to 50dp x 50dp, place it
         * in a `ShapeDrawable drawable` and use that `ShapeDrawable` to create
         * `ShapeHolder shapeHolder` to hold it. We set the x and y coordinates of `shapeHolder`
         * to our arguments `x` and `y` respectively. We create a random `int red`
         * between 100 and 255, a random `int green` between 100 and 255, and a random
         * `int blue` between 100 and 255. We then shift them into the appropriate bit positions
         * for a 32 bit color and or the three colors along with a maximum alpha field to form the color
         * `int color`. We retrieve the `Paint` used to draw `drawable` to initialize
         * `Paint paint`. We create `int darkColor` using our random colors `red`,
         * `green` and `blue` divided by 4 before being shifted into position and or'ed
         * together with a maximum alpha value. `RadialGradient gradient` is then created with
         * 37.5 as the x-coordinate of the center of the radius, 12.5 as the y-coordinate of the center
         * of the radius, 50. as the radius of the circle for the gradient, `color` as the color
         * at the center of the circle, `darkColor` as the color at the edge of the circle, and
         * using CLAMP Shader tiling mode (replicate the edge color if the shader draws outside of its
         * original bounds). We set `gradient` as the shader of `paint`, add `shapeHolder`
         * to `ArrayList<ShapeHolder> balls` and return `shapeHolder` to the caller.
         *
         * @param x x coordinate for new ball
         * @param y y coordinate for new ball
         * @return ShapeHolder containing the new ball
         */
        private fun addBall(x: Float, y: Float): ShapeHolder {
            val circle = OvalShape()
            circle.resize(50f * mDensity, 50f * mDensity)
            val drawable = ShapeDrawable(circle)
            val shapeHolder = ShapeHolder(drawable)
            shapeHolder.x = x - 25f
            shapeHolder.y = y - 25f
            val red = (100 + Math.random() * 155).toInt()
            val green = (100 + Math.random() * 155).toInt()
            val blue = (100 + Math.random() * 155).toInt()
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
         * This callback draws its `MyAnimationView` after every invalidate() call. We loop over
         * `int i` for all the `ShapeHolder` objects in `ArrayList<ShapeHolder> balls`
         * initializing `ShapeHolder shapeHolder` with the `ShapeHolder` in `balls`
         * at index `i`. We save the current matrix and clip of `canvas` onto a private
         * stack, pre-concatenate the matrix of `canvas` with a translation to the coordinates
         * `(x,y)` of the ShapeHolder, then instruct the `ShapeDrawable` in `shapeHolder`
         * to draw itself. We then remove all modifications to the matrix/clip state of `canvas`
         * and loop around for the next ball.
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

        /**
         * Called when the RUN button is clicked, we first call our method `createAnimation` to
         * create the animation `AnimatorSet animation` (if this is the first time the button is
         * clicked), and then start the animation running.
         */
        fun startAnimation() {
            createAnimation()
            animation!!.start()
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