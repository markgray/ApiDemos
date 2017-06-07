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

import com.example.android.apis.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/**
 * Shows how to use the Animation api (in this case TranslateAnimation) in order  to move a jpg
 * around a Canvas. Uses AnimateDrawable which extends ProxyDrawable (A neat way to package the
 * methods required when extending Drawable, overriding only draw in AnimateDrawable)
 */
public class AnimateDrawables extends GraphicsActivity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to a new instance of {@code SampleView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }

    /**
     * Custom View which contains an instance of {@code AnimateDrawable} which it creates from a
     * resource drawable (R.drawable.beach) and an {@code TranslateAnimation} which will move the
     * drawable from (0,0) to (100,200)
     */
    private static class SampleView extends View {
        /**
         * The instance of {@code AnimateDrawable} that does all the work of moving our drawable around.
         */
        private AnimateDrawable mDrawable;

        /**
         * Configures our {@code View}, and creates and configures an instance of {@code AnimateDrawable}
         * which will move around our view's canvas. First we call through to our super's constructor,
         * then we enable this view to receive focus, and to be focusable in touch mode. Next we retrieve
         * our resource drawable R.drawable.beach to {@code Drawable dr}, and set the bounding rectangle
         * of {@code dr} to the intrinsic size of {@code dr} (the size that the drawable would like to be
         * laid out, including any inherent padding). We create {@code Animation an} to be a
         * {@code TranslateAnimation} that moves from (0,0) to (100,200), set its duration to 2000 milliseconds,
         * set its repeat count to INFINITE, and call {@code initialize} to set the size of the object
         * being animated and its parent both to 10 x 10.
         *
         * Now we initialize our field {@code AnimateDrawable mDrawable} with a new instance of
         * {@code AnimateDrawable} created using {@code Drawable dr} and {@code Animation an}.
         * Finally we start the animation {@code an} at the current time in milliseconds.
         *
         * @param context {@code Context} to use to fetch resources
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            setFocusableInTouchMode(true);

            //noinspection deprecation
            Drawable dr = context.getResources().getDrawable(R.drawable.beach);
            //noinspection ConstantConditions
            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());

            Animation an = new TranslateAnimation(0, 100, 0, 200);
            an.setDuration(2000);
            an.setRepeatCount(Animation.INFINITE);
            an.initialize(10, 10, 10, 10);

            mDrawable = new AnimateDrawable(dr, an);
            an.startNow();
        }

        /**
         * Called when the system needs us to draw our view. First we draw the entire bitmap of the
         * {@code Canvas canvas} to the color white, then we instruct our {@code AnimateDrawable mDrawable}
         * to draw itself. Finally we call {@code invalidate} to invalidate our entire view, causing
         * this method to be called again in the sweet by and by.
         *
         * @param canvas {@code Canvas} we are to draw our view in
         */
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);

            mDrawable.draw(canvas);
            invalidate();
        }
    }
}

