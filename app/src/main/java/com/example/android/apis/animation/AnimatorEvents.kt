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
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.withTranslation
import com.example.android.apis.R

/**
 * Supposed to show when the various Sequencer Events and Animator Events:
 * Start Repeat Cancel and End occur, but Repeat Events are not generated
 * for the Sequencer because the api does not support setRepeatCount on a
 * AnimatorSet.
 */
@SuppressLint("ObsoleteSdkInt")
@Suppress("MemberVisibilityCanBePrivate")
@RequiresApi(Build.VERSION_CODES.HONEYCOMB)
class AnimatorEvents : AppCompatActivity() {
    /**
     * `TextView` with id R.id.startText ("Start") in "Sequencer Events:" row of the animation
     * event display, it has its alpha increased from .5f to 1f in the `onAnimationStart`
     * override if the argument passed it is an instance of `AnimatorSet`.
     */
    internal lateinit var startText: TextView

    /**
     * `TextView` with id R.id.repeatText ("Repeat") in "Sequencer Events:" row of the animation
     * event display, it has its alpha increased from .5f to 1f in the `onAnimationRepeat`
     * override if the argument passed it is an instance of `AnimatorSet`.
     */
    internal lateinit var repeatText: TextView

    /**
     * `TextView` with id R.id.cancelText ("Cancel") in "Sequencer Events:" row of the animation
     * event display, it has its alpha increased from .5f to 1f in the `onAnimationCancel`
     * override if the argument passed it is an instance of `AnimatorSet`.
     */
    internal lateinit var cancelText: TextView

    /**
     * `TextView` with id R.id.endText ("End") in "Sequencer Events:" row of the animation
     * event display, it has its alpha increased from .5f to 1f in the `onAnimationEnd`
     * override if the argument passed it is an instance of `AnimatorSet`.
     */
    internal lateinit var endText: TextView

    /**
     * `TextView` with id R.id.startTextAnimator ("Start") in "Animator Events:" row of the
     * animation event display, it has its alpha increased from .5f to 1f in the `onAnimationStart`
     * override if the argument passed it is NOT an instance of `AnimatorSet`.
     */
    internal lateinit var startTextAnimator: TextView

    /**
     * `TextView` with id R.id.repeatTextAnimator ("Repeat") in "Animator Events:" row of the
     * animation event display, it has its alpha increased from .5f to 1f in the `onAnimationRepeat`
     * override if the argument passed it is NOT an instance of `AnimatorSet`.
     */
    internal lateinit var repeatTextAnimator: TextView

    /**
     * `TextView` with id R.id.cancelTextAnimator ("Cancel") in "Animator Events:" row of the
     * animation event display, it has its alpha increased from .5f to 1f in the `onAnimationCancel`
     * override if the argument passed it is NOT an instance of `AnimatorSet`.
     */
    internal lateinit var cancelTextAnimator: TextView

    /**
     * `TextView` with id R.id.endTextAnimator ("End") in "Animator Events:" row of the
     * animation event display, it has its alpha increased from .5f to 1f in the `onAnimationEnd`
     * override if the argument passed it is NOT an instance of `AnimatorSet`.
     */
    internal lateinit var endTextAnimator: TextView

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.animator_events.
     * We initialize `LinearLayout container` by finding the view with id R.id.container,
     * create a new instance for `MyAnimationView animView` and add that view to `container`.
     * We initialize our field `TextView startText` by finding the view with id R.id.startText
     * and set its alpha to .5f, initialize our field `TextView repeatText` by finding the view
     * with id R.id.repeatText and set its alpha to .5f, initialize our field `TextView cancelText`
     * by finding the view with id R.id.cancelText and set its alpha to .5f, initialize our field
     * `TextView endText` by finding the view with id R.id.endText and set its alpha to .5f,
     * initialize our field `TextView startTextAnimator` by finding the view with id
     * R.id.startTextAnimator and set its alpha to .5f, initialize our field `TextView repeatTextAnimator`
     * by finding the view with id R.id.repeatTextAnimator and set its alpha to .5f, initialize our field
     * `TextView cancelTextAnimator` by finding the view with id R.id.cancelTextAnimator and set
     * its alpha to .5f, and initialize our field `TextView endTextAnimator` by finding the view
     * with id R.id.endTextAnimator and set its alpha to .5f.
     *
     * We initialize `CheckBox endCB` by finding the view with id R.id.endCB ("End Immediately").
     * We initialize `Button starter` by finding the view with id R.id.startButton ("Play") and
     * set its `OnClickListener` to an anonymous class which calls the `startAnimation`
     * method of `MyAnimationView animView` with the checked state of `CheckBox endCB`.
     *
     * We initialize `Button canceler` by finding the view with id R.id.cancelButton ("Cancel")
     * and set its `OnClickListener` to an anonymous class which calls the `cancelAnimation`
     * method of `MyAnimationView animView`.
     *
     * Finally we initialize `Button ender` by finding the view with id R.id.endButton ("End")
     * and set its `OnClickListener` to an anonymous class which calls the `endAnimation`
     * method of `MyAnimationView animView`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animator_events)

        val container = findViewById<LinearLayout>(R.id.container)
        val animView = MyAnimationView(this)
        container.addView(animView)
        startText = findViewById(R.id.startText)
        startText.alpha = .5f
        repeatText = findViewById(R.id.repeatText)
        repeatText.alpha = .5f
        cancelText = findViewById(R.id.cancelText)
        cancelText.alpha = .5f
        endText = findViewById(R.id.endText)
        endText.alpha = .5f
        startTextAnimator = findViewById(R.id.startTextAnimator)
        startTextAnimator.alpha = .5f
        repeatTextAnimator = findViewById(R.id.repeatTextAnimator)
        repeatTextAnimator.alpha = .5f
        cancelTextAnimator = findViewById(R.id.cancelTextAnimator)
        cancelTextAnimator.alpha = .5f
        endTextAnimator = findViewById(R.id.endTextAnimator)
        endTextAnimator.alpha = .5f

        val endCB = findViewById<CheckBox>(R.id.endCB)
        val starter = findViewById<Button>(R.id.startButton)
        starter.setOnClickListener {
            animView.startAnimation(endCB.isChecked)
        }

        val canceler = findViewById<Button>(R.id.cancelButton)
        canceler.setOnClickListener {
            animView.cancelAnimation()
        }

        val ender = findViewById<Button>(R.id.endButton)
        ender.setOnClickListener {
            animView.endAnimation()
        }

    }

    /**
     * This is the view containing our animated ball, which listens for notifications from an
     * animation by implementing Animator.AnimatorListener. Notifications indicate animation
     * related events, such as the end or the repetition of the animation. It also implements
     * ValueAnimator.AnimatorUpdateListener receiving callbacks for every frame of the animation.
     */
    inner class MyAnimationView
    /**
     * Our constructor. First we call our super's constructor, then we initialize our field
     * `ShapeHolder ball` with a 25 pixel diameter circle created by our method
     * `createBall`.
     *
     * @param context context of the Application
     */
        (context: Context) : View(context), Animator.AnimatorListener,
        ValueAnimator.AnimatorUpdateListener {

        @Suppress("unused")
        val balls: ArrayList<ShapeHolder> = ArrayList()

        /**
         * `AnimatorSet` created in method `createAnimation` which moves
         * `ShapeHolder ball` in both x and y directions
         */
        internal var animation: Animator? = null

        /**
         * The ball which we move.
         */
        internal var ball: ShapeHolder

        /**
         * Flag set when "End Immediately" checkbox is checked, if true in `onAnimationStart`
         * callback causes the `end` method of `Animator animation` to be called
         * causing our animation to stop immediately.
         */
        internal var endImmediately = false

        init {
            ball = createBall(25f, 25f)
        }

        /**
         * Creates the `Animator animation` for the `ShapeHolder ball` (if it does not
         * already exist). If `animation` is not null we do nothing, if it is null we first
         * create `ObjectAnimator yAnim` to animate the "y" property name (the y coordinate)
         * `ShapeHolder ball` from the current position to the bottom minus 50px with a
         * duration of 1500 milliseconds. We set its repeat count to 2, its repeat mode to
         * REVERSE using a factor of 2.0 `AccelerateInterpolator` (starts slow and ends fast).
         * We add "this" as an `AnimatorUpdateListener` for `yAnim`, and also add "this"
         * as a `AnimatorListener`.
         *
         * We create `ObjectAnimator xAnim` to animates the "x" property name (the y coordinate)
         * of the ball from the current position to the current position plus 300px with a duration
         * of 1000 milliseconds. We set its repeat count to 2, its repeatMode to REVERSE, and set
         * its interpolator to an `AccelerateInterpolator` using a factor of 2.0 (starts slow
         * and ends fast). `ObjectAnimator alphaAnim` and `AnimatorSet alphaSeq` are
         * strange in that they are played during the creation of the `AnimatorSet animation`
         * and do not participate with the event demonstration. Together they animate the alpha of
         * the `ShapeHolder ball` from 1f to .5f with a duration of 1000 milliseconds.
         *
         * The finish of the process is to create the `AnimatorSet animation`, configure it to
         * play together `yAnim`, and `xAnim`. We then add "this" as an `AnimatorListener`
         * to `animation`.
         */
        private fun createAnimation() {
            if (animation == null) {
                val yAnim = ObjectAnimator.ofFloat(
                    ball, "y",
                    ball.y, height - 50f
                ).setDuration(1500)
                yAnim.repeatCount = 2
                yAnim.repeatMode = ValueAnimator.REVERSE
                yAnim.interpolator = AccelerateInterpolator(2f)
                yAnim.addUpdateListener(this)
                yAnim.addListener(this)

                val xAnim = ObjectAnimator.ofFloat(
                    ball, "x",
                    ball.x, ball.x + 300
                ).setDuration(1000)
                xAnim.startDelay = 0
                xAnim.repeatCount = 2
                xAnim.repeatMode = ValueAnimator.REVERSE
                xAnim.interpolator = AccelerateInterpolator(2f)

                val alphaAnim = ObjectAnimator
                    .ofFloat(ball, "alpha", 1f, .5f)
                    .setDuration(1000)

                @SuppressLint("Recycle") // Lint is right: start() is never called, how odd.
                val alphaSeq = AnimatorSet()
                alphaSeq.play(alphaAnim)

                @SuppressLint("Recycle") // It is started in startAnimation()
                animation = AnimatorSet()
                (animation as AnimatorSet).playTogether(yAnim, xAnim)
                animation!!.addListener(this)
            }
        }

        /**
         * Starts `Animator animation` running. First we set our field `endImmediately`
         * to our argument `endImmediately` for later use by the callback onAnimationStart.
         * Then we set the alpha of the text used to display the occurrence of events to .5f
         * (`startText`, `repeatText`, `cancelText`, `endText`, `startTextAnimator`,
         * `repeatTextAnimator`, `cancelTextAnimator`, and `endTextAnimator`) to signify that
         * they have not occurred yet. Then we call our method `createAnimation` to create the
         * `Animator animation`. Finally we call the `start` method of `animation` to start the
         * animation running.
         *
         * @param endImmediately used to set the field `endImmediately` for use by the callback
         * `onAnimationStart` (which will call the `end` method of
         * `animation` immediately ending the animation.)
         */
        fun startAnimation(endImmediately: Boolean) {
            this.endImmediately = endImmediately
            startText.alpha = .5f
            repeatText.alpha = .5f
            cancelText.alpha = .5f
            endText.alpha = .5f
            startTextAnimator.alpha = .5f
            repeatTextAnimator.alpha = .5f
            cancelTextAnimator.alpha = .5f
            endTextAnimator.alpha = .5f
            createAnimation()
            animation!!.start()
        }

        /**
         * Cancel the `Animator animation` (used by the CANCEL Button's `onClick` call
         * back). First we call our method `createAnimation` to create `Animator animation`
         * if it does not exist yet, then we call the `cancel` method of `animation`.
         */
        fun cancelAnimation() {
            createAnimation()
            animation!!.cancel()
        }

        /**
         * End the `Animator animation` (used by the END Button's `onClick` call back).
         * First we call our method `createAnimation` to create `Animator animation` if
         * it does not exist yet, then we call the `end` method of `animation`.
         */
        fun endAnimation() {
            createAnimation()
            animation!!.end()
        }

        /**
         * Creates and returns a `ShapeHolder` holding a "ball". First we create an
         * `OvalShape circle`, re-size it to be a 50px by 50px circle, create a
         * `ShapeDrawable drawable` from it, and place it in a `ShapeHolder shapeHolder`.
         * We sets the (x,y) coordinates of the `ShapeHolder` to the calling parameters of the
         * method minus 25 pixels. We initialize `int red` with a random number between 0 and
         * 255, and do the same for `int green` and `int blue`. We then initialize
         * `int color` by shifting these variables into the appropriate positions to form a
         * 32 bit RGB color and or them together along with a maximum alpha value. We initialize
         * `Paint paint` by retrieving the paint used to draw `ShapeDrawable drawable`.
         * We initialize `int darkColor` by or'ing together the RGB values used for `color`
         * divided by 4 along with a maximum alpha value. We create `RadialGradient gradient`
         * with a center x coordinate of 37.5, center y coordinate of 12.5, a radius of 50, with
         * `color` as the color at the center, `darkColor` as the color at the edge, and
         * using CLAMP tiling mode (replicates the edge color if the shader draws outside of its
         * original bounds). We set the shader of `paint` to `gradient` and set the paint
         * of `shapeHolder` to `paint`, then return `shapeHolder` to the caller.
         *
         * @param x x coordinate of the ShapeHolder created
         * @param y y coordinate of the ShapeHolder created
         * @return ShapeHolder containing the created ball
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
         * Does the drawing of the `MyAnimationView` every time invalidate() is called. First
         * we save the current matrix and clip of `Canvas canvas` onto a private stack.
         * Then we pre-concatenate the current matrix with a translation to the current x,y coordinates
         * of `ShapeHolder ball`, instruct the `ShapeDrawable` of `ShapeHolder ball`
         * to draw itself, and then restore the matrix/clip state to the state saved on the private
         * stack.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.withTranslation(x = ball.x, y = ball.y) {
                ball.shape!!.draw(this)
            }
        }

        /**
         * Part of the `AnimatorUpdateListener` interface which we implement, this callback is
         * called to notify us of the occurrence of another frame of the animation. We simply
         * invalidate the whole view which will cause our `onDraw(Canvas)` callback to be
         * called to redraw our view using the new values set in the ShapeHolder by the animation.
         *
         * @param animation The animation which was has advanced by a frame.
         */
        override fun onAnimationUpdate(animation: ValueAnimator) {
            invalidate()
        }

        /**
         * Part of the `AnimatorListener` interface which we implement, this callback is called
         * to notify us of the start of the animation. We set the alpha of the text used to indicate
         * the occurrence of the "Start" event to 1.0f, either startText for our AnimatorSet
         * instance signaling the event, or else startTextAnimator for sequencer events caused by
         * our yAnim ObjectAnimator. If the "End Immediately" checkbox is checked the animation is
         * ended by calling animation.end().
         *
         * @param animation The started animation.
         */
        override fun onAnimationStart(animation: Animator) {
            if (animation is AnimatorSet) {
                startText.alpha = 1f
            } else {
                startTextAnimator.alpha = 1f
            }
            if (endImmediately) {
                animation.end()
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
        override fun onAnimationEnd(animation: Animator) {
            if (animation is AnimatorSet) {
                endText.alpha = 1f
            } else {
                endTextAnimator.alpha = 1f
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
        override fun onAnimationCancel(animation: Animator) {
            if (animation is AnimatorSet) {
                cancelText.alpha = 1f
            } else {
                cancelTextAnimator.alpha = 1f
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
        override fun onAnimationRepeat(animation: Animator) {
            if (animation is AnimatorSet) {
                repeatText.alpha = 1f
            } else {
                repeatTextAnimator.alpha = 1f
            }
        }
    }
}