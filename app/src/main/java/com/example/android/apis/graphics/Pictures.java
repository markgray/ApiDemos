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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.view.View;

import java.io.*;

/**
 * Shows how to use the Picture class to record drawing instructions performed on the Canvas returned
 * by Picture.beginRecording(width, height), then after calling Picture.endRecording() the Picture is
 * turned into a Drawable by calling PictureDrawable(Picture). Then in the onDraw method it first
 * draws the Picture using the Canvas.drawPicture(Picture) method then it stretches the Picture when
 * drawing using the Canvas.drawPicture(Picture,RectF) method, then it draws the Drawable it created
 * with PictureDrawable(Picture) to draw using Drawable.draw(Canvas), then it writes the Picture to
 * a ByteArrayOutputStream and draws it by reading that ByteArrayOutputStream back in and then drawing
 * it using Canvas.drawPicture(Picture.createFromStream(is))
 */
public class Pictures extends GraphicsActivity {

    /**
     * Called when the activity is starting. First we call our super's implementation of {@code onCreate}
     * then we set our content view to a new instance of {@code SampleView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }

    /**
     * Custom view demonstrating some of the features of the {@code Picture} class.
     */
    private static class SampleView extends View {
        /**
         * Our {@code Picture} consisting of a pink circle (the alpha of 0x88 lets the white background
         * lighten the red circle) with the green text "Pictures" partially obscuring it.
         */
        private Picture mPicture;
        /**
         * {@code PictureDrawable} created out of {@code Picture mPicture}.
         */
        private Drawable mDrawable;

        /**
         * Draws a pink circle (the alpha of 0x88 lets the white background lighten the red circle)
         * with the green text "Pictures" partially obscuring it. First we allocate a new instance of
         * {@code Paint} for {@code Paint p}, with the ANTI_ALIAS_FLAG set. We set the color of {@code p}
         * to red with an alpha of 0x88 and use it to draw a 40 pixel radius circle on {@code Canvas canvas}
         * with its center at (50,50). We set the color of {@code p} to GREEN, set its text size to 30,
         * and use it to draw the text "Pictures" at location (60,60) on {@code canvas}.
         *
         * @param canvas {@code Canvas} to draw to, it is created by calling the {@code beginRecording}
         *               method of our field {@code Picture mPicture}.
         */
        static void drawSomething(Canvas canvas) {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

            p.setColor(0x88FF0000);
            canvas.drawCircle(50, 50, 40, p);

            p.setColor(Color.GREEN);
            p.setTextSize(30);
            canvas.drawText("Pictures", 60, 60, p);
        }

        /**
         * Our constructor. First we call our super's constructor, then we enable our view to receive
         * focus, and to receive focus in touch mode. We create a new instance of {@code Picture} for
         * our field {@code Picture mPicture}, call its {@code beginRecording} method specifying a
         * width of 200, and a height of 100, and pass the {@code Canvas} returned to our method
         * {@code drawSomething}. When {@code drawSomething} is done drawing to the {@code mPicture}
         * canvas we call the {@code endRecording} method of {@code mPicture}. Finally we create a
         * {@code PictureDrawable} from {@code mPicture} for our field {@code Drawable mDrawable}.
         *
         * @param context {@code Context} to use to access resources, "this" when called from the
         *                {@code onCreate} method of the {@code Pictures} activity.
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            setFocusableInTouchMode(true);

            mPicture = new Picture();
            drawSomething(mPicture.beginRecording(200, 100));
            mPicture.endRecording();

            mDrawable = new PictureDrawable(mPicture);
        }

        /**
         * We implement this to do our drawing.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @SuppressLint("DrawAllocation")
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);

            canvas.drawPicture(mPicture);

            canvas.drawPicture(mPicture, new RectF(0, 100, getWidth(), 200));

            mDrawable.setBounds(0, 200, getWidth(), 300);
            mDrawable.draw(canvas);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            //noinspection deprecation
            mPicture.writeToStream(os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            canvas.translate(0, 300);
            //noinspection deprecation
            canvas.drawPicture(Picture.createFromStream(is));
        }
    }
}

