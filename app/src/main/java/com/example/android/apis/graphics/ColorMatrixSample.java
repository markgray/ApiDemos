/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.View;

/**
 * Uses ColorMatrixColorFilter's to create three different versions of a jpeg, animating them
 * through different "contrasts". One changes both scale and translate, one changes scale only,
 * and one changes translate only. (The original is at top of left column, scale and translate
 * to right of it, scale only second row, and translate only is in the third row.)
 */
public class ColorMatrixSample extends GraphicsActivity {

    /**
     * First we call through to our super's implementation of {@code onCreate}, then we set our
     * content view to a new instance of {@code SampleView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }

    /**
     * Custom {@code View} class which displays the same jpg (R.drawable.balloons) four different ways,
     * one without any animation of the {@code ColorMatrix} used to draw it, and three with the
     * {@code ColorMatrix} used to draw it animated in different ways.
     */
    private static class SampleView extends View {
        /**
         * Apparently created to prevent "draw allocation" warning, but warning is issued for the
         * allocation of {@code ColorMatrixColorFilter} any way? Used only in {@code draw} method
         * and then only after copying it to {@code Paint paint}.
         */
        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        /**
         * {@code Bitmap} of our resource jpg R.drawable.balloons
         */
        private Bitmap mBitmap;
        /**
         * Animated angle [0...180] incremented in steps of 2 degrees round robin every time {@code draw}
         * is called. It is used to create a contrast value of [0..1], which is used as an argument to our
         * methods {@code setContrast}, {@code setContrastScaleOnly}. and {@code setContrastTranslateOnly}
         * which use it to modify the {@code ColorMatrixColorFilter} they are passed as their second argument.
         */
        private float mAngle;

        /**
         * Our constructor, it simply calls our super's constructor, then initializes our field
         * {@code Bitmap mBitmap} with a {@code Bitmap} decoded from our resource jpg R.drawable.balloons.
         *
         * @param context {@code Context} to use to fetch resources
         */
        public SampleView(Context context) {
            super(context);

            mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.balloons);
        }

        @SuppressWarnings("unused")
        private static void setTranslate(ColorMatrix cm, float dr, float dg, float db, float da) {
            cm.set(new float[] {
                   2, 0, 0, 0, dr,
                   0, 2, 0, 0, dg,
                   0, 0, 2, 0, db,
                   0, 0, 0, 1, da });
        }

        private static void setContrast(ColorMatrix cm, float contrast) {
            float scale = contrast + 1.f;
            float translate = (-.5f * scale + .5f) * 255.f;
            cm.set(new float[] {
                   scale, 0, 0, 0, translate,
                   0, scale, 0, 0, translate,
                   0, 0, scale, 0, translate,
                   0, 0, 0, 1, 0 });
        }

        private static void setContrastTranslateOnly(ColorMatrix cm, float contrast) {
            float scale = contrast + 1.f;
            float translate = (-.5f * scale + .5f) * 255.f;
            cm.set(new float[] {
                   1, 0, 0, 0, translate,
                   0, 1, 0, 0, translate,
                   0, 0, 1, 0, translate,
                   0, 0, 0, 1, 0 });
        }

        private static void setContrastScaleOnly(ColorMatrix cm, float contrast) {
            float scale = contrast + 1.f;
            @SuppressWarnings("unused")
            float translate = (-.5f * scale + .5f) * 255.f;
            cm.set(new float[] {
                   scale, 0, 0, 0, 0,
                   0, scale, 0, 0, 0,
                   0, 0, scale, 0, 0,
                   0, 0, 0, 1, 0 });
        }

        @SuppressLint("DrawAllocation")
        @Override protected void onDraw(Canvas canvas) {
            Paint paint = mPaint;
            float x = 20;
            float y = 20;

            canvas.drawColor(Color.WHITE);

            paint.setColorFilter(null);
            canvas.drawBitmap(mBitmap, x, y, paint);

            ColorMatrix cm = new ColorMatrix();

            mAngle += 2;
            if (mAngle > 180) {
                mAngle = 0;
            }

            //convert our animated angle [-180...180] to a contrast value of [-1..1]
            float contrast = mAngle / 180.f;

            setContrast(cm, contrast);
            paint.setColorFilter(new ColorMatrixColorFilter(cm));
            canvas.drawBitmap(mBitmap, x + mBitmap.getWidth() + 10, y, paint);

            setContrastScaleOnly(cm, contrast);
            paint.setColorFilter(new ColorMatrixColorFilter(cm));
            canvas.drawBitmap(mBitmap, x, y + mBitmap.getHeight() + 10, paint);

            setContrastTranslateOnly(cm, contrast);
            paint.setColorFilter(new ColorMatrixColorFilter(cm));
            canvas.drawBitmap(mBitmap, x, y + 2*(mBitmap.getHeight() + 10), paint);

            invalidate();
        }
    }
}

