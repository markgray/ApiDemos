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

package com.example.android.apis.graphics;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.apis.R;

import java.util.ArrayList;

/**
 * Clever use of Animator and AnimatorSet to move card stack using "material design" shadowing.
 * The properties being animated are: translationY (expandAnimators), translationZ (towardAnimators),
 * rotationY and translationX (moveAwayAnimators), rotationY and translationX (moveBackAnimators),
 * translationZ (awayAnimators), and translationY (collapseAnimators). Crashes for less than v21 due
 * to AndroidManifest android:theme="@android:style/Theme.Material.Light"
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ShadowCardStack extends Activity {

    /**
     * X coordinate in DP that the cards "slide away" to in the {@code Animator slideAway}, (where it
     * is scaled to pixels using the display's logical density before being used).
     */
    private static final float X_SHIFT_DP = 1000;
    /**
     * Used to calculate the {@code targetY} for each individual card that that card "expands to"
     * in the {@code Animator expand}, (where it is scaled to pixels using the display's logical
     * density before being used). It is essentially the size in DP of the top edge of the card that
     * is visible when the stack is expanded.
     */
    private static final float Y_SHIFT_DP = 50;
    /**
     * Each card has an animation of its "translationZ" attribute by a multiple of this depending on
     * the location of the card in the stack in the {@code Animator toward}, (where it is scaled to
     * pixels using the display's logical density before being used). It is essentially the height
     * of a card above the card below it when the stack is expanded.
     */
    private static final float Z_LIFT_DP = 8;
    /**
     * Angle to which the cards are rotated around the y axis using the "rotationY" attribute as
     * they begin to "slide away" in the {@code Animator rotateAway}. It is a subtle animation when
     * the "slide away" happens so fast, but is visible if you change the "slide away" duration to
     * a much longer time period.
     */
    private static final float ROTATE_DEGREES = 15;

    /**
     * Turns a list of {@code Animator} objects into an {@code AnimatorSet}, with the {@code Animator}
     * objects set to play together, with a start delay of {@code startDelay} milliseconds, and returns
     * it to the caller.
     *
     * @param items      list of {@code Animator} objects
     * @param startDelay amount of time, in milliseconds, to delay starting the animation after its
     *                   {@code start()} method is called.
     * @return An {@code AnimatorSet} containing all of the {@code Animator} objects in {@code items},
     * configured to play together with a start delay of {@code startDelay}
     */
    public AnimatorSet createSet(ArrayList<Animator> items, long startDelay) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(items);
        set.setStartDelay(startDelay);
        return set;
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout R.layout.shadow_card_stack. Next
     * we fetch the current logical density of our display to {@code float density} to use to scale
     * DP to pixels. We find the {@code ViewGroup} with id R.id.card_parent and save a pointer to it
     * in {@code ViewGroup cardParent}. Next we convert {@code X_SHIFT_DP} to pixels and assign the
     * value to {@code float X}, {@code Y_SHIFT_DP} to {@code float Y} and {@code Z_LIFT_DP} to
     * {@code float Z} for later use. We create 6 lists of {@code Animator} objects:
     * <ul>
     * <li>{@code towardAnimators} - contains "translationZ" {@code Animator} objects for moving towards the viewer</li>
     * <li>{@code expandAnimators} - contains "translationY" {@code Animator} objects for expanding from 1 card to 5 stacked cards</li>
     * <li>{@code moveAwayAnimators} - contains "rotationY" and "translationX" {@code Animator} objects for moving away</li>
     * <li>{@code moveBackAnimators} - contains "rotationY" and "translationX" {@code Animator} objects for moving back</li>
     * <li>{@code awayAnimators} - contains "translationZ" {@code Animator} objects for moving Z back to 0</li>
     * <li>{@code collapseAnimators} - contains "translationY" {@code Animator} objects for moving Y back to 0</li>
     * </ul>
     * We now initialize {@code max} to the number of child {@code TextView} objects in our layout (5 in
     * our case), and loop for each of these. We fetch a reference to the current child to {@code TextView card},
     * set its text to display the "Card number" it represents. We calculate the Y coordinate we want that
     * card to "expand" to, create an {@code Animator expand} to animate the cards "translationY" attribute
     * to that coordinate and add it to the {@code expandAnimators} list. We calculate how high we want
     * that card to rise toward the viewer, create an {@code Animator toward} to animate the cards "translationZ"
     * to that point and add it to the {@code towardAnimators} list. We set the x location of the point around
     * which the view is rotated to X_SHIFT_DP (1000), create an {@code Animator rotateAway} to rotate every
     * card except card 0 (the bottom card) by ROTATE_DEGREES (15), set its start delay to 1000ms for card 0,
     * 800ms for card 1, 600ms for card 2, 400ms for card 3, and 200ms for card 4 (top of stack), set its
     * duration to 100ms, and add it to the {@code moveAwayAnimators} list. We create an {@code Animator slideAway}
     * to animate the cards "translationX" to 0 for card 0, and to {@code X} for all other cards, set its start
     * delay to 1000ms for card 0, 800ms for card 1, 600ms for card 2, 400ms for card 3, and 200ms for card 4
     * (top of stack), set its duration to 100ms and add it to the {@code moveAwayAnimators} list. We create
     * an {@code Animator rotateBack} to animate the cards "rotationY" to 0, set its start delay to 0ms
     * for card 0, 200ms for card 1, 400ms for card 2, 600ms for card 3, and 800ms for card 4 (top of stack),
     * and add it to the {@code moveBackAnimators} list. We create an {@code Animator slideBack} to animate the
     * cards "translationX" to 0, set its start delay to 0ms for card 0, 200ms for card 1, 400ms for card 2,
     * 600ms for card 3, and 800ms for card 4 (top of stack), and add it to the {@code moveBackAnimators} list.
     * We create an {@code Animator away} to animate the cards "translationZ" to 0, set its start delay to 0ms
     * for card 0, 200ms for card 1, 400ms for card 2, 600ms for card 3, and 800ms for card 4 (top of stack),
     * and add it to the {@code awayAnimators} list. And finally we create an {@code Animator collapse} to
     * animate the cards "translationY" to 0, and add it to the {@code collapseAnimators} list.
     * <p>
     * When we are done creating the animators for all five cards, we create {@code AnimatorSet totalSet}
     * and set it up to play each of the {@code AnimatorSet} objects that our method {@code createSet}
     * creates from our 6 {@code Animator} lists. The {@code expandAnimators} run together after a start
     * delay of 250ms, followed by the {@code towardAnimators}, followed after a 250ms delay by the
     * {@code moveAwayAnimators}, followed by the {@code moveBackAnimators}, followed after a 250ms
     * delay by the {@code awayAnimators}, followed by the {@code collapseAnimators}. We then start
     * {@code totalSet} running and set its {@code AnimatorListener} to a new instance of our
     * {@code RepeatListener(totalSet)} which restarts the {@code totalSet} animation when it ends.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shadow_card_stack);

        float density = getResources().getDisplayMetrics().density;

        final ViewGroup cardParent = (ViewGroup) findViewById(R.id.card_parent);

        final float X = X_SHIFT_DP * density;
        final float Y = Y_SHIFT_DP * density;
        final float Z = Z_LIFT_DP * density;

        ArrayList<Animator> towardAnimators = new ArrayList<>();
        ArrayList<Animator> expandAnimators = new ArrayList<>();
        ArrayList<Animator> moveAwayAnimators = new ArrayList<>();
        ArrayList<Animator> moveBackAnimators = new ArrayList<>();
        ArrayList<Animator> awayAnimators = new ArrayList<>();
        ArrayList<Animator> collapseAnimators = new ArrayList<>();

        final int max = cardParent.getChildCount();
        for (int i = 0; i < max; i++) {
            TextView card = (TextView) cardParent.getChildAt(i);
            card.setText("Card number " + i);

            float targetY = (i - (max - 1) / 2.0f) * Y;
            Animator expand = ObjectAnimator.ofFloat(card, "translationY", targetY);
            expandAnimators.add(expand);

            Animator toward = ObjectAnimator.ofFloat(card, "translationZ", i * Z);
            toward.setStartDelay(200 * ((max) - i));
            towardAnimators.add(toward);

            card.setPivotX(X_SHIFT_DP);
            Animator rotateAway = ObjectAnimator.ofFloat(card, "rotationY", i == 0 ? 0 : ROTATE_DEGREES);
            rotateAway.setStartDelay(200 * ((max) - i));
            rotateAway.setDuration(100);
            moveAwayAnimators.add(rotateAway);
            Animator slideAway = ObjectAnimator.ofFloat(card, "translationX", i == 0 ? 0 : X);
            slideAway.setStartDelay(200 * ((max) - i));
            slideAway.setDuration(100);
            moveAwayAnimators.add(slideAway);

            Animator rotateBack = ObjectAnimator.ofFloat(card, "rotationY", 0);
            rotateBack.setStartDelay(200 * i);
            moveBackAnimators.add(rotateBack);
            Animator slideBack = ObjectAnimator.ofFloat(card, "translationX", 0);
            slideBack.setStartDelay(200 * i);
            moveBackAnimators.add(slideBack);

            Animator away = ObjectAnimator.ofFloat(card, "translationZ", 0);
            away.setStartDelay(200 * i);
            awayAnimators.add(away);

            Animator collapse = ObjectAnimator.ofFloat(card, "translationY", 0);
            collapseAnimators.add(collapse);
        }

        AnimatorSet totalSet = new AnimatorSet();
        totalSet.playSequentially(
                createSet(expandAnimators, 250),
                createSet(towardAnimators, 0),

                createSet(moveAwayAnimators, 250),
                createSet(moveBackAnimators, 0),

                createSet(awayAnimators, 250),
                createSet(collapseAnimators, 0));
        totalSet.start();
        totalSet.addListener(new RepeatListener(totalSet));
    }

    /**
     * {@code AnimatorListener} which starts its animation over again when it ends.
     */
    @SuppressWarnings("WeakerAccess")
    public static class RepeatListener implements Animator.AnimatorListener {
        /**
         * The {@code Animator} we were constructed to listen to.
         */
        final Animator mRepeatAnimator;

        /**
         * Our constructor simply saves its argument in our field {@code Animator mRepeatAnimator}.
         *
         * @param repeatAnimator {@code Animator} object that we are created to listen to.
         */
        public RepeatListener(Animator repeatAnimator) {
            mRepeatAnimator = repeatAnimator;
        }

        /**
         * Notifies the start of the animation. We ignore.
         *
         * @param animation The started animation.
         */
        @Override
        public void onAnimationStart(Animator animation) {
        }

        /**
         * Notifies the end of the animation. If we are being called for the same {@code Animator} we
         * were created to listen to, we start it running again.
         *
         * @param animation The animation which reached its end.
         */
        @Override
        public void onAnimationEnd(Animator animation) {
            if (animation == mRepeatAnimator) {
                mRepeatAnimator.start();
            }
        }

        /**
         * Notifies the cancellation of the animation. We do nothing.
         *
         * @param animation The animation which was canceled.
         */
        @Override
        public void onAnimationCancel(Animator animation) {
        }

        /**
         * Notifies the repetition of the animation. We do nothing.
         *
         * @param animation The animation which was repeated.
         */
        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }
}
