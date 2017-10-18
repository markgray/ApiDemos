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
     *
     * @param items list of {@code Animator} objects
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

            float targetY = (i - (max-1) / 2.0f) * Y;
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

    @SuppressWarnings("WeakerAccess")
    public static class RepeatListener implements Animator.AnimatorListener {
        final Animator mRepeatAnimator;
        public RepeatListener(Animator repeatAnimator) {
            mRepeatAnimator = repeatAnimator;
        }

        @Override
        public void onAnimationStart(Animator animation) {}

        @Override
        public void onAnimationEnd(Animator animation) {
            if (animation == mRepeatAnimator) {
                mRepeatAnimator.start();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {}

        @Override
        public void onAnimationRepeat(Animator animation) {}
    }
}
