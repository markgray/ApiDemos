/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.example.android.apis.graphics

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import java.util.*

/**
 * Clever use of [Animator] and [AnimatorSet] to move card stack using "material design" shadowing.
 * The properties being animated are: translationY (expandAnimators), translationZ (towardAnimators),
 * rotationY and translationX (moveAwayAnimators), rotationY and translationX (moveBackAnimators),
 * translationZ (awayAnimators), and translationY (collapseAnimators).
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class ShadowCardStack : AppCompatActivity() {
    /**
     * Turns a list of [Animator] objects into an [AnimatorSet], with the [Animator] objects set to
     * play together, with a start delay of [Long] parameter [startDelay]  milliseconds, and returns
     * it to the caller.
     *
     * @param items      list of [Animator] objects
     * @param startDelay amount of time, in milliseconds, to delay starting the animation after its
     * `start()` method is called.
     * @return An [AnimatorSet] containing all of the [Animator] objects in `ArrayList<Animator>`
     * parameter [items], configured to play together with a start delay of [startDelay]
     */
    fun createSet(items: ArrayList<Animator>?, startDelay: Long): AnimatorSet {
        val set = AnimatorSet()
        set.playTogether(items)
        set.startDelay = startDelay
        return set
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout R.layout.shadow_card_stack. Next
     * we fetch the current logical density of our display to `float density` to use to scale
     * DP to pixels. We find the `ViewGroup` with id R.id.card_parent and save a pointer to it
     * in `ViewGroup cardParent`. Next we convert `X_SHIFT_DP` to pixels and assign the
     * value to `float X`, `Y_SHIFT_DP` to `float Y` and `Z_LIFT_DP` to
     * `float Z` for later use. We create 6 lists of `Animator` objects:
     *
     *  * `towardAnimators` - contains "translationZ" `Animator` objects for moving towards the viewer
     *  * `expandAnimators` - contains "translationY" `Animator` objects for expanding from 1 card to 5 stacked cards
     *  * `moveAwayAnimators` - contains "rotationY" and "translationX" `Animator` objects for moving away
     *  * `moveBackAnimators` - contains "rotationY" and "translationX" `Animator` objects for moving back
     *  * `awayAnimators` - contains "translationZ" `Animator` objects for moving Z back to 0
     *  * `collapseAnimators` - contains "translationY" `Animator` objects for moving Y back to 0
     *
     * We now initialize `max` to the number of child `TextView` objects in our layout (5 in
     * our case), and loop for each of these. We fetch a reference to the current child to `TextView card`,
     * set its text to display the "Card number" it represents. We calculate the Y coordinate we want that
     * card to "expand" to, create an `Animator expand` to animate the cards "translationY" attribute
     * to that coordinate and add it to the `expandAnimators` list. We calculate how high we want
     * that card to rise toward the viewer, create an `Animator toward` to animate the cards "translationZ"
     * to that point and add it to the `towardAnimators` list. We set the x location of the point around
     * which the view is rotated to X_SHIFT_DP (1000), create an `Animator rotateAway` to rotate every
     * card except card 0 (the bottom card) by ROTATE_DEGREES (15), set its start delay to 1000ms for card 0,
     * 800ms for card 1, 600ms for card 2, 400ms for card 3, and 200ms for card 4 (top of stack), set its
     * duration to 100ms, and add it to the `moveAwayAnimators` list. We create an `Animator slideAway`
     * to animate the cards "translationX" to 0 for card 0, and to `X` for all other cards, set its start
     * delay to 1000ms for card 0, 800ms for card 1, 600ms for card 2, 400ms for card 3, and 200ms for card 4
     * (top of stack), set its duration to 100ms and add it to the `moveAwayAnimators` list. We create
     * an `Animator rotateBack` to animate the cards "rotationY" to 0, set its start delay to 0ms
     * for card 0, 200ms for card 1, 400ms for card 2, 600ms for card 3, and 800ms for card 4 (top of stack),
     * and add it to the `moveBackAnimators` list. We create an `Animator slideBack` to animate the
     * cards "translationX" to 0, set its start delay to 0ms for card 0, 200ms for card 1, 400ms for card 2,
     * 600ms for card 3, and 800ms for card 4 (top of stack), and add it to the `moveBackAnimators` list.
     * We create an `Animator away` to animate the cards "translationZ" to 0, set its start delay to 0ms
     * for card 0, 200ms for card 1, 400ms for card 2, 600ms for card 3, and 800ms for card 4 (top of stack),
     * and add it to the `awayAnimators` list. And finally we create an `Animator collapse` to
     * animate the cards "translationY" to 0, and add it to the `collapseAnimators` list.
     *
     *
     * When we are done creating the animators for all five cards, we create `AnimatorSet totalSet`
     * and set it up to play each of the `AnimatorSet` objects that our method `createSet`
     * creates from our 6 `Animator` lists. The `expandAnimators` run together after a start
     * delay of 250ms, followed by the `towardAnimators`, followed after a 250ms delay by the
     * `moveAwayAnimators`, followed by the `moveBackAnimators`, followed after a 250ms
     * delay by the `awayAnimators`, followed by the `collapseAnimators`. We then start
     * `totalSet` running and set its `AnimatorListener` to a new instance of our
     * `RepeatListener(totalSet)` which restarts the `totalSet` animation when it ends.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shadow_card_stack)
        val density = resources.displayMetrics.density
        val cardParent = findViewById<ViewGroup>(R.id.card_parent)
        val xInPixels = X_SHIFT_DP * density
        val yInPixels = Y_SHIFT_DP * density
        val zInPixels = Z_LIFT_DP * density
        val towardAnimators = ArrayList<Animator>()
        val expandAnimators = ArrayList<Animator>()
        val moveAwayAnimators = ArrayList<Animator>()
        val moveBackAnimators = ArrayList<Animator>()
        val awayAnimators = ArrayList<Animator>()
        val collapseAnimators = ArrayList<Animator>()
        val max = cardParent.childCount
        for (i in 0 until max) {
            val card = cardParent.getChildAt(i) as TextView
            card.text = "Card number $i"
            val targetY = (i - (max - 1) / 2.0f) * yInPixels
            val expand: Animator = ObjectAnimator.ofFloat(card, "translationY", targetY)
            expandAnimators.add(expand)
            val toward: Animator = ObjectAnimator.ofFloat(card, "translationZ", i * zInPixels)
            toward.startDelay = 200 * (max - i).toLong()
            towardAnimators.add(toward)
            card.pivotX = X_SHIFT_DP
            val rotateY: Float = if (i == 0) 0f else ROTATE_DEGREES
            val rotateAway: Animator = ObjectAnimator.ofFloat(card, "rotationY", rotateY)
            rotateAway.startDelay = 200 * (max - i).toLong()
            rotateAway.duration = 100
            moveAwayAnimators.add(rotateAway)
            val translateX: Float = if (i == 0) 0f else xInPixels
            val slideAway: Animator = ObjectAnimator.ofFloat(card, "translationX", translateX)
            slideAway.startDelay = 200 * (max - i).toLong()
            slideAway.duration = 100
            moveAwayAnimators.add(slideAway)
            val rotateBack: Animator = ObjectAnimator.ofFloat(card, "rotationY", 0f)
            rotateBack.startDelay = 200 * i.toLong()
            moveBackAnimators.add(rotateBack)
            val slideBack: Animator = ObjectAnimator.ofFloat(card, "translationX", 0f)
            slideBack.startDelay = 200 * i.toLong()
            moveBackAnimators.add(slideBack)
            val away: Animator = ObjectAnimator.ofFloat(card, "translationZ", 0f)
            away.startDelay = 200 * i.toLong()
            awayAnimators.add(away)
            val collapse: Animator = ObjectAnimator.ofFloat(card, "translationY", 0f)
            collapseAnimators.add(collapse)
        }
        val totalSet = AnimatorSet()
        totalSet.playSequentially(
                createSet(expandAnimators, 250),
                createSet(towardAnimators, 0),
                createSet(moveAwayAnimators, 250),
                createSet(moveBackAnimators, 0),
                createSet(awayAnimators, 250),
                createSet(collapseAnimators, 0))
        totalSet.start()
        totalSet.addListener(RepeatListener(totalSet))
    }

    /**
     * `AnimatorListener` which starts its animation over again when it ends.
     */
    class RepeatListener
    /**
     * Our constructor simply saves its argument in our field `Animator mRepeatAnimator`.
     *
     *  repeatAnimator `Animator` object that we are created to listen to.
     */
    (
            /**
             * The `Animator` we were constructed to listen to.
             */
            val mRepeatAnimator: Animator) : Animator.AnimatorListener {

        /**
         * Notifies the start of the animation. We ignore.
         *
         * @param animation The started animation.
         */
        override fun onAnimationStart(animation: Animator) {}

        /**
         * Notifies the end of the animation. If we are being called for the same `Animator` we
         * were created to listen to, we start it running again.
         *
         * @param animation The animation which reached its end.
         */
        override fun onAnimationEnd(animation: Animator) {
            if (animation === mRepeatAnimator) {
                mRepeatAnimator.start()
            }
        }

        /**
         * Notifies the cancellation of the animation. We do nothing.
         *
         * @param animation The animation which was canceled.
         */
        override fun onAnimationCancel(animation: Animator) {}

        /**
         * Notifies the repetition of the animation. We do nothing.
         *
         * @param animation The animation which was repeated.
         */
        override fun onAnimationRepeat(animation: Animator) {}

    }

    companion object {
        /**
         * X coordinate in DP that the cards "slide away" to in the `Animator slideAway`, (where it
         * is scaled to pixels using the display's logical density before being used).
         */
        private const val X_SHIFT_DP = 1000f
        /**
         * Used to calculate the `targetY` for each individual card that that card "expands to"
         * in the `Animator expand`, (where it is scaled to pixels using the display's logical
         * density before being used). It is essentially the size in DP of the top edge of the card that
         * is visible when the stack is expanded.
         */
        private const val Y_SHIFT_DP = 50f
        /**
         * Each card has an animation of its "translationZ" attribute by a multiple of this depending on
         * the location of the card in the stack in the `Animator toward`, (where it is scaled to
         * pixels using the display's logical density before being used). It is essentially the height
         * of a card above the card below it when the stack is expanded.
         */
        private const val Z_LIFT_DP = 8f
        /**
         * Angle to which the cards are rotated around the y axis using the "rotationY" attribute as
         * they begin to "slide away" in the `Animator rotateAway`. It is a subtle animation when
         * the "slide away" happens so fast, but is visible if you change the "slide away" duration to
         * a much longer time period.
         */
        private const val ROTATE_DEGREES = 15f
    }
}