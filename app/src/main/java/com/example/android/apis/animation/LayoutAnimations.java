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

package com.example.android.apis.animation;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import android.animation.Animator;
import android.animation.ObjectAnimator;
import com.example.android.apis.R;

import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.LayoutTransition;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

/**
 * This application demonstrates how to use LayoutTransition to automate transition animations
 * as items are removed from or added to a container.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LayoutAnimations extends Activity {

    private static final String TAG = "LayoutAnimations";
    private int numButtons = 1;
    ViewGroup container = null;
    Animator defaultAppearingAnim, defaultDisappearingAnim;
    Animator defaultChangingAppearingAnim, defaultChangingDisappearingAnim;
    Animator customAppearingAnim, customDisappearingAnim;
    Animator customChangingAppearingAnim, customChangingDisappearingAnim;
    Animator currentAppearingAnim, currentDisappearingAnim;
    Animator currentChangingAppearingAnim, currentChangingDisappearingAnim;

    /** Called when the activity is first created. */
    /**
     * Sets the content view to layout_animations. Creates a FixedGridLayout container instance
     * and configures its cell height, and cell width, creates LayoutTransition transitioner with
     * default animations, sets it as the LayoutTransition for container, and squirrels away the
     * default animations for later use. It then calls the method createCustomAnimations to create
     * custom animations using its argument transitioner only to fetch the default value of the
     * duration of the animations (see createCustomAnimations). setupTransition() is used to
     * switch LayoutTransition transitioner between the default and custom animations for the four
     * different animations used in a layout transition based on the state of the CheckBox's and is
     * only called when the state of one of the 5 CheckBox's changes (the CheckBox for choosing
     * custom instead of default animations, as well as the 4 CheckBox's selecting "In", "Out",
     * "Changing-In" and "Changing-Out" animations or disabling them if un-checked."
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_animations);

        container = new FixedGridLayout(this);
        container.setClipChildren(false);
        ((FixedGridLayout)container).setCellHeight(200);
        ((FixedGridLayout)container).setCellWidth(400);
        final LayoutTransition transitioner = new LayoutTransition();
        container.setLayoutTransition(transitioner);
        defaultAppearingAnim = transitioner.getAnimator(LayoutTransition.APPEARING);
        defaultDisappearingAnim =
                transitioner.getAnimator(LayoutTransition.DISAPPEARING);
        defaultChangingAppearingAnim =
                transitioner.getAnimator(LayoutTransition.CHANGE_APPEARING);
        defaultChangingDisappearingAnim =
                transitioner.getAnimator(LayoutTransition.CHANGE_DISAPPEARING);
        createCustomAnimations(transitioner);
        currentAppearingAnim = defaultAppearingAnim;
        currentDisappearingAnim = defaultDisappearingAnim;
        currentChangingAppearingAnim = defaultChangingAppearingAnim;
        currentChangingDisappearingAnim = defaultChangingDisappearingAnim;

        ViewGroup parent = (ViewGroup) findViewById(R.id.parent);
        parent.addView(container);
        parent.setClipChildren(false);
        Button addButton = (Button) findViewById(R.id.addNewButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Creates a new Button using this activity as its context, sets the minimum height
             * to 64 pixels and minimum width to 64 pixels, sets the text displayed in the button
             * to the number of buttons created and increments that number, sets the OnClickListener
             * of the Button to remove itself when clicked. It then adds the Button to the ViewGroup
             * (FixedGridLayout) container at position 1 (or 0 if no Button's have been created yet.
             *
             * @param v addButton View when it is clicked
             */
            @Override
            public void onClick(View v) {
                Button newButton = new Button(LayoutAnimations.this);
                newButton.setMinHeight(64);
                newButton.setMinWidth(64);
                newButton.setText(String.valueOf(numButtons++));
                newButton.setOnClickListener(new View.OnClickListener() {
                    /**
                     * Removes itself when it is clicked
                     *
                     * @param v Button View which was clicked
                     */
                    @Override
                    public void onClick(View v) {
                        container.removeView(v);
                    }
                });
                container.addView(newButton, Math.min(1, container.getChildCount()));
            }
        });

        CheckBox customAnimCB = (CheckBox) findViewById(R.id.customAnimCB);
        customAnimCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * Called when the checked state of the "Custom Animations" CheckBox has changed,
             * it just calls setupTransition to change the LayoutTransition transitioner as
             * needed.
             *
             * @param buttonView The compound button view whose state has changed.
             * @param isChecked  The new checked state of buttonView.
             */
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "Custom Checkbox");
                setupTransition(transitioner);
            }
        });

        // Check for disabled animations
        CheckBox appearingCB = (CheckBox) findViewById(R.id.appearingCB);
        appearingCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * Called when the checked state of the "In" (appearing) CheckBox has changed,
             * it just calls setupTransition to change the LayoutTransition transitioner as
             * needed.
             *
             * @param buttonView The compound button view whose state has changed.
             * @param isChecked  The new checked state of buttonView.
             */
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "Appearing Checkbox");
                setupTransition(transitioner);
            }
        });
        CheckBox disappearingCB = (CheckBox) findViewById(R.id.disappearingCB);
        disappearingCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * Called when the checked state of the "Out" (disappearing) CheckBox has changed,
             * it just calls setupTransition to change the LayoutTransition transitioner as
             * needed.
             *
             * @param buttonView The compound button view whose state has changed.
             * @param isChecked  The new checked state of buttonView.
             */
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "Disappearing Checkbox");
                setupTransition(transitioner);
            }
        });
        CheckBox changingAppearingCB = (CheckBox) findViewById(R.id.changingAppearingCB);
        changingAppearingCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * Called when the checked state of the "Changing In" (CHANGING_APPEARING) CheckBox
             * has changed, it just calls setupTransition to change the LayoutTransition
             * transitioner as needed.
             *
             * @param buttonView The compound button view whose state has changed.
             * @param isChecked  The new checked state of buttonView.
             */
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "Changing Appearing Checkbox");
                setupTransition(transitioner);
            }
        });
        CheckBox changingDisappearingCB = (CheckBox) findViewById(R.id.changingDisappearingCB);
        changingDisappearingCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * Called when the checked state of the "Changing Out" (CHANGING_DISAPPEARING) CheckBox
             * has changed, it just calls setupTransition to change the LayoutTransition
             * transitioner as needed.
             *
             * @param buttonView The compound button view whose state has changed.
             * @param isChecked  The new checked state of buttonView.
             */
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "Changing Disappearing Checkbox");
                setupTransition(transitioner);
            }
        });
    }

    /**
     * This method sets the Animations used for the four different animation types used in a
     * layout transition (APPEARING, DISAPPEARING, CHANGE_APPEARING, and CHANGE_DISAPPEARING)
     * to be either the default animations, or the custom animations (created by the method
     * createCustomAnimations) based on the state of the checkboxes at the moment. It is called
     * only when one of the five checkboxes changes state. It first finds each of the checkboxes,
     * then sets each of the four animation types by choosing animations (default or custom)
     * using a double ternary operator which decides the animation to be used based on the
     * isChecked() state of the relevant CheckBox's for that animation.
     *
     * @param transition LayoutTransition to be modified
     */
    private void setupTransition(LayoutTransition transition) {
        CheckBox customAnimCB = (CheckBox) findViewById(R.id.customAnimCB);
        CheckBox appearingCB = (CheckBox) findViewById(R.id.appearingCB);
        CheckBox disappearingCB = (CheckBox) findViewById(R.id.disappearingCB);
        CheckBox changingAppearingCB = (CheckBox) findViewById(R.id.changingAppearingCB);
        CheckBox changingDisappearingCB = (CheckBox) findViewById(R.id.changingDisappearingCB);
        transition.setAnimator(LayoutTransition.APPEARING, appearingCB.isChecked() ?
                (customAnimCB.isChecked() ? customAppearingAnim : defaultAppearingAnim) : null);
        transition.setAnimator(LayoutTransition.DISAPPEARING, disappearingCB.isChecked() ?
                (customAnimCB.isChecked() ? customDisappearingAnim : defaultDisappearingAnim) : null);
        transition.setAnimator(LayoutTransition.CHANGE_APPEARING, changingAppearingCB.isChecked() ?
                (customAnimCB.isChecked() ? customChangingAppearingAnim :
                        defaultChangingAppearingAnim) : null);
        transition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING,
                changingDisappearingCB.isChecked() ?
                (customAnimCB.isChecked() ? customChangingDisappearingAnim :
                        defaultChangingDisappearingAnim) : null);
    }

    /**
     * This method creates the four different custom animations which can be selected to be used
     * by when the appropriate CheckBox's are "checked"
     *
     * For the CHANGE_APPEARING (Changing while Adding) part of the animation it defines property
     * value holders to animate property "left" from 0 to 1, "top" from 0 to 1, "right" from 0 to 1,
     * "bottom" from 0 to 1, "scaleX" from 1f to 0f to 1f, "scaleY" from 1f to 0f to 1f. It then
     * creates an ObjectAnimator customChangingAppearingAnim for these properties, sets its duration
     * to use the same duration of the current LayoutTransition transition and sets
     * customChangingAppearingAnim to be the CHANGE_APPEARING animation of transition. It adds an
     * AnimatorListenerAdapter to customChangingAppearingAnim which overrides onAnimationEnd and
     * scales the Button added to full size. This animation has has the appearance of a card
     * flipping right to left from the back side to the front side. You can see this animation in
     * action by clicking the ADD BUTTON Button when both the "Custom Animations" and "In"
     * CheckBox are checked.
     *
     * For the CHANGE_DISAPPEARING (Changing while Removing) part of the animation it defines an
     * additional PropertyValueHolder for "rotation" constructed of three KeyFrame's (kf0 - a
     * starting value of the rotation of 0f lasting 0f, kf1 - a rotation of 360f degrees lasting
     * .9999f of the frame, and kf2 - an ending rotation of 0f degrees. It combines these in the
     * "rotation" property value holder pvhRotation, then combines the "left", "top", "right", and
     * "bottom" PropertyValuesHolder's used for the CHANGE_APPEARING animation to create the
     * ObjectAnimator customChangingDisappearingAnim, sets the duration of
     * customChangingDisappearingAnim to be the same as the current LayoutTransition transition,
     * and sets customChangingDisappearingAnim to be the CHANGE_DISAPPEARING animation of
     * transition. It adds an AnimatorListenerAdapter to customChangingDisappearingAnim which
     * overrides onAnimationEnd to set the rotation of the Button to 0f degrees. It has the
     * effect of rotating the Button's to the right of the Button removed clockwise while moving
     * them into their new positions when both the "Custom Animations" and "Out" CheckBox are checked.
     *
     * For the APPEARING (Adding) part of the animation it creates a simple "rotationY"
     * ObjectAnimator customAppearingAnim which rotates the Button from 90f degrees to 0f degrees,
     * sets the duration of customAppearingAnim to be the same as the current LayoutTransition
     * transition, and sets customAppearingAnim to be the APPEARING animation of transition. It
     * adds an AnimatorListenerAdapter to customAppearingAnim which overrides onAnimationEnd to
     * set the rotation of the Button to 0f degrees. It has the effect of rotating the appearing
     * Button's about the y axis when the ADD BUTTON button is pressed, starting from sticking
     * directly out of the plane of the View, to flat when both the "Custom Animations" and
     * "Changing In" CheckBox are checked.
     *
     * For the DISAPPEARING (Removing) part of the animation it creates a simple "rotationX"
     * ObjectAnimator customDisappearingAnim which rotates the Button from 0f degrees (flat) to 90f degrees
     * (sticking out of the plane), sets the duration of customDisappearingAnim to be the same as the current
     * LayoutTransition transition, and sets customDisappearingAnim to be the DISAPPEARING animation of
     * transition. It add an AnimatorListenerAdapter to customDisappearingAnim which overrides onAnimationEnd
     * to set the rotation of the Button to 0f degrees. It has the effect of rotating the
     * disappearing Button about the x axis when the Button is clicked starting from flat to
     * sticking vertically out of the plane when both the "Custom Animations" and "Changing Out"
     * CheckBox are checked..

     * @param transition LayoutTransition which is to be modified
     */
    private void createCustomAnimations(LayoutTransition transition) {
        // Changing while Adding
        PropertyValuesHolder pvhLeft =
                PropertyValuesHolder.ofInt("left", 0, 1);
        PropertyValuesHolder pvhTop =
                PropertyValuesHolder.ofInt("top", 0, 1);
        PropertyValuesHolder pvhRight =
                PropertyValuesHolder.ofInt("right", 0, 1);
        PropertyValuesHolder pvhBottom =
                PropertyValuesHolder.ofInt("bottom", 0, 1);
        PropertyValuesHolder pvhScaleX =
                PropertyValuesHolder.ofFloat("scaleX", 1f, 0f, 1f);
        PropertyValuesHolder pvhScaleY =
                PropertyValuesHolder.ofFloat("scaleY", 1f, 0f, 1f);
        customChangingAppearingAnim = ObjectAnimator.ofPropertyValuesHolder(
                        this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhScaleX, pvhScaleY).
                setDuration(transition.getDuration(LayoutTransition.CHANGE_APPEARING));
        customChangingAppearingAnim.addListener(new AnimatorListenerAdapter() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setScaleX(1f);
                view.setScaleY(1f);
            }
        });

        // Changing while Removing
        Keyframe kf0 = Keyframe.ofFloat(0f, 0f);
        Keyframe kf1 = Keyframe.ofFloat(.9999f, 360f);
        Keyframe kf2 = Keyframe.ofFloat(1f, 0f);
        PropertyValuesHolder pvhRotation =
                PropertyValuesHolder.ofKeyframe("rotation", kf0, kf1, kf2);
        customChangingDisappearingAnim = ObjectAnimator.ofPropertyValuesHolder(
                        this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhRotation).
                setDuration(transition.getDuration(LayoutTransition.CHANGE_DISAPPEARING));
        customChangingDisappearingAnim.addListener(new AnimatorListenerAdapter() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setRotation(0f);
            }
        });

        // Adding
        customAppearingAnim = ObjectAnimator.ofFloat(null, "rotationY", 90f, 0f).
                setDuration(transition.getDuration(LayoutTransition.APPEARING));
        customAppearingAnim.addListener(new AnimatorListenerAdapter() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setRotationY(0f);
            }
        });

        // Removing
        customDisappearingAnim = ObjectAnimator.ofFloat(null, "rotationX", 0f, 90f).
                setDuration(transition.getDuration(LayoutTransition.DISAPPEARING));
        customDisappearingAnim.addListener(new AnimatorListenerAdapter() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setRotationX(0f);
            }
        });

    }
}