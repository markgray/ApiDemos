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

import android.content.Context;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import android.view.*;

/**
 * Shows the use of Canvas.saveLayerAlpha() and Canvas.restore() to save and restore
 * Canvas settings while doing some drawing in an off screen buffer.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Layers extends GraphicsActivity {

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
     * This custom {@code View} consists solely of a BLUE circle drawn slightly offset on top of a
     * RED circle. The drawing is done to an offscreen bitmap allocated and redirected to by the
     * method {@code saveLayerAlpha}, then displayed onscreen by {@code restore}.
     */
    private static class SampleView extends View {

        /**
         * {@code Paint} used to draw in our {@code onDraw} method
         */
        private Paint mPaint;

        /**
         * Our constructor. First we call through to our super's constructor, then we enable focus for
         * our view, allocate a new instance of {@code Paint} for our field {@code Paint mPaint} and
         * set its antialias flag to true.
         *
         * @param context "this" {@code Layers} activity when called from {@code onCreate}, used for
         *                resources.
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
        }

        /**
         * We implement this to do our drawing. First we set the entire {@code Canvas canvas} to
         * WHITE, then we move the canvas to the location (10,10). We call {@code saveLayerAlpha}
         * to allocate and redirect drawing instructions sent to {@code Canvas canvas} to an offscreen
         * bitmap until the matching restore displays that bitmap and restores the canvas to its
         * original settings. The bitmap is 200x200 with 0x88 specified as the alpha value to be used
         * when {@code restore} draws the bitmap to the {@code Canvas canvas}.
         * <p>
         * We then set the color of {@code Paint mPaint} to RED and use it to draw a circle of radius
         * 75 pixels centered at the point (75,75), set the color of {@code mPaint} to BLUE and use
         * it to draw a circle of radius 75 pixels centered at the point (125,125). Both of these
         * {@code drawCircle} commands have been done to the offscreen bitmap that {@code canvas} is
         * redirecting commands to, and this bitmap is now transferred to {@code Canvas canvas} and
         * the settings saved by {@code saveLayerAlpha} are restored by a call to {@code canvas.restore}.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);

            canvas.translate(10, 10);

            // Saves future drawing commands to offscreen bitmap buffer
            canvas.saveLayerAlpha(0, 0, 200, 200, 0x88);

            mPaint.setColor(Color.RED);
            canvas.drawCircle(75, 75, 75, mPaint);
            mPaint.setColor(Color.BLUE);
            canvas.drawCircle(125, 125, 75, mPaint);

            // Transfers offscreen buffer to screen
            canvas.restore();
        }
    }
}

