/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;

/**
 * Used by the {@code AnimateDrawables} demo which:
 * Shows how to use the Animation api (in this case TranslateAnimation) in order to move a jpg
 * around a Canvas. Uses AnimateDrawable which extends ProxyDrawable (A neat way to package the
 * methods required when extending Drawable, overriding only draw in AnimateDrawable)
 */
@SuppressWarnings("WeakerAccess")
public class AnimateDrawable extends ProxyDrawable {

    /**
     * Animation we are to use to move our {@code Drawable} around the {@code Canvas} passed to our
     * {@code draw} method.
     */
    private Animation mAnimation;
    /**
     * Transformation which we modify for each animation step then use to modify the matrix of the
     * {@code Canvas} passed to our {@code draw} method.
     */
    private Transformation mTransformation = new Transformation();

    /**
     * Unused constructor which simply passes its parameter {@code Drawable target} to our super's
     * constructor.
     *
     * @param target {@code Drawable} we are to proxy for
     */
    @SuppressWarnings("unused")
    public AnimateDrawable(Drawable target) {
        super(target);
    }

    /**
     * We pass our parameter {@code Drawable target} to our super's constructor, then save our
     * parameter {@code Animation animation} in our field {@code Animation mAnimation}.
     *
     * @param target    {@code Drawable} we are to proxy for
     * @param animation {@code Animation} we are to apply to our proxy {@code Drawable}
     */
    public AnimateDrawable(Drawable target, Animation animation) {
        super(target);
        mAnimation = animation;
    }

    /**
     * A getter method for our field {@code Animation mAnimation}.
     *
     * @return the {@code Animation} instance reference we have saved in our field {@code Animation mAnimation}.
     */
    public Animation getAnimation() {
        return mAnimation;
    }

    /**
     * A setter method for our field {@code Animation mAnimation}.
     *
     * @param anim {@code Animation} instance reference to save in our field {@code Animation mAnimation}.
     */
    public void setAnimation(Animation anim) {
        mAnimation = anim;
    }

    /**
     * Indicates whether the {@code Animation mAnimation} has started or not. If {@code mAnimation}
     * is not null, we pass the call through to it. Unused.
     *
     * @return true if the animation has started, false otherwise
     */
    @SuppressWarnings("unused")
    public boolean hasStarted() {
        return mAnimation != null && mAnimation.hasStarted();
    }

    /**
     * Indicates whether the {@code Animation mAnimation} has ended or not. If {@code mAnimation}
     * is not null, we pass the call through to it. Unused.
     *
     * @return true if the animation has ended, false otherwise
     */
    @SuppressWarnings("unused")
    public boolean hasEnded() {
        return mAnimation == null || mAnimation.hasEnded();
    }

    /**
     * Called when we are required to draw our drawable into our parameter {@code Canvas canvas}.
     * First we call our super to retrieve the {@code Drawable dr} that we (and it) were constructed
     * with and if {@code dr} is not null we:
     * <ul>
     * <li>
     * Save the current matrix and clip onto a private stack, saving the value to pass to
     * {@code restoreToCount()} to balance this {@code save()} in {@code int sc}.
     * </li>
     * <li>
     * Copy a reference to our field {@code Animation mAnimation} to {@code Animation anim} (Why?)
     * </li>
     * <li>
     * If {@code anim} is not null, we get the transformation to apply for the current animation
     * time in milliseconds to our field {@code Transformation mTransformation}, and pre-concatenate
     * the 3x3 Matrix representing the transformation to apply to the coordinates of the object
     * being animated to the current matrix of {@code Canvas canvas}.
     * </li>
     * <li>
     * We instruct our {@code Drawable dr} to draw itself
     * </li>
     * <li>
     * Finally we restore the state of the {@code Canvas canvas} to the one we saved in {@code sc}.
     * </li>
     * </ul>
     *
     * @param canvas The canvas to draw into
     */
    @Override
    public void draw(@NonNull Canvas canvas) {
        Drawable dr = getProxy();
        if (dr != null) {
            int sc = canvas.save();
            Animation anim = mAnimation;
            if (anim != null) {
                anim.getTransformation(
                        AnimationUtils.currentAnimationTimeMillis(),
                        mTransformation);
                canvas.concat(mTransformation.getMatrix());
            }
            dr.draw(canvas);
            canvas.restoreToCount(sc);
        }
    }
}

