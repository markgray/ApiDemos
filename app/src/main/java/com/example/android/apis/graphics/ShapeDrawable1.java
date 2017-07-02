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

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.*;
import android.os.Bundle;
import android.view.*;

/**
 * Drawing using the Drawable class methods, many of them usable in {@code <shape>} xml drawables.
 */
public class ShapeDrawable1 extends GraphicsActivity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to a new instance of our class {@code SampleView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }

    /**
     * Custom view which holds and draws our 7 {@code ShapeDrawable} examples.
     */
    private static class SampleView extends View {
        /**
         * Contains references to our 7 {@code ShapeDrawable} examples.
         */
        private ShapeDrawable[] mDrawables;

        /**
         * Creates and returns a {@code SweepGradient} {@code Shader} that draws a sweep gradient
         * around a center point (150,25), with the colors red, green, blue, red.
         *
         * @return Shader that draws a sweep gradient around a center point (150,25), with the colors
         * red, green, blue, red.
         */
        private static Shader makeSweep() {
            return new SweepGradient(150, 25,
                    new int[]{0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFF0000},
                    null);
        }

        /**
         * Creates and returns a shader that draws a linear gradient along the line (0,0) to (50,50)
         * using the colors red, green, and blue evenly distributed along the line, and using the tile
         * mode MIRROR (repeats the shader's image horizontally and vertically, alternating mirror
         * images so that adjacent images always seam)
         *
         * @return a {@code LinearGradient} {@code Shader} shader that draws a linear gradient along
         * the line (0,0) to (50,50) using the colors red, green, and blue evenly distributed along
         * the line, and using the tile mode MIRROR (repeats the shader's image horizontally and
         * vertically, alternating mirror images so that adjacent images always seam)
         */
        private static Shader makeLinear() {
            return new LinearGradient(0, 0, 50, 50,
                    new int[]{0xFFFF0000, 0xFF00FF00, 0xFF0000FF},
                    null, Shader.TileMode.MIRROR);
        }

        /**
         * Creates and returns a {@code BitmapShader} {@code Shader} which "tiles" a shape using a
         * {@code Bitmap}. First we allocate {@code int[] pixels} with the four colors Red, Green,
         * Blue, and Black. Then we create the 2 by 2 {@code Bitmap bm} from {@code pixels} using
         * the type ARGB_8888. Finally we return a {@code BitmapShader} created from {@code bm} with
         * a tile mode of REPEAT for both x and y directions.
         *
         * @return a {@code BitmapShader} {@code Shader} which "tiles" a shape using a {@code Bitmap}
         */
        private static Shader makeTiling() {
            int[] pixels = new int[]{0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0};
            Bitmap bm = Bitmap.createBitmap(pixels, 2, 2, Bitmap.Config.ARGB_8888);

            return new BitmapShader(bm, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        }

        /**
         * Custom {@code ShapeDrawable} which draws its shape twice, once using the {@code Paint}
         * passed to its {@code onDraw} method, and once using its private {@code Paint} (which is
         * configured to be a stroked style paint, and can be accessed and customized using the
         * getter method {@code getStrokePaint})
         */
        private static class MyShapeDrawable extends ShapeDrawable {
            /**
             * {@code Paint} used to draw the shape a second time in our {@code onDraw} method. It is
             * configured to use the STROKE style, and can be accessed and customized using the
             * getter method {@code getStrokePaint})
             */
            private Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            /**
             * Our constructor. First we call through to our super's constructor, then we set the
             * style of our field {@code Paint mStrokePaint} to STROKE (Geometry and text drawn with
             * this style will be stroked, respecting the stroke-related fields on the paint)
             *
             * @param s {@code Shape} we are to draw (an {@code ArcShape} in our case)
             */
            @SuppressWarnings("WeakerAccess")
            public MyShapeDrawable(Shape s) {
                super(s);
                mStrokePaint.setStyle(Paint.Style.STROKE);
            }

            /**
             * Returns a reference to our private field {@code Paint mStrokePaint} so that the user
             * can modify (or use) it.
             *
             * @return reference to our private field {@code Paint mStrokePaint}.
             */
            @SuppressWarnings("WeakerAccess")
            public Paint getStrokePaint() {
                return mStrokePaint;
            }

            /**
             * Called from the drawable's draw() method after the canvas has been set to
             * draw the shape at (0,0). Subclasses can override for special effects such
             * as multiple layers, stroking, etc.
             * <p>
             * First we instruct the {@code Shape s} to draw itself on {@code Canvas c} using
             * {@code Paint p}, then instruct the {@code Shape s} to draw itself again on
             * {@code Canvas c} using our {@code Paint mStrokePaint}
             *
             * @param s {@code Shape} we are to draw
             * @param c {@code Canvas} we are to draw our {@code Shape s} to positioned to draw at (0,0)
             * @param p {@code Paint} we are supposed to use.
             */
            @Override
            protected void onDraw(Shape s, Canvas c, Paint p) {
                s.draw(c, p);
                s.draw(c, mStrokePaint);
            }
        }

        /**
         * Constructor for our {@code SampleView}.
         *
         * @param context {@code Context} to use for resources, "this" {@code ShapeDrawable1} activity
         *                when called from {@code onCreate} in our case.
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            float[] outerR = new float[]{12, 12, 12, 12, 0, 0, 0, 0};
            RectF inset = new RectF(6, 6, 6, 6);
            float[] innerR = new float[]{12, 12, 0, 0, 12, 12, 0, 0};

            Path path = new Path();
            path.moveTo(50, 0);
            path.lineTo(0, 50);
            path.lineTo(50, 100);
            path.lineTo(100, 50);
            path.close();

            mDrawables = new ShapeDrawable[7];
            mDrawables[0] = new ShapeDrawable(new RectShape());
            mDrawables[1] = new ShapeDrawable(new OvalShape());
            mDrawables[2] = new ShapeDrawable(new RoundRectShape(outerR, null, null));
            mDrawables[3] = new ShapeDrawable(new RoundRectShape(outerR, inset, null));
            mDrawables[4] = new ShapeDrawable(new RoundRectShape(outerR, inset, innerR));
            mDrawables[5] = new ShapeDrawable(new PathShape(path, 100, 100));
            mDrawables[6] = new MyShapeDrawable(new ArcShape(45, -270));

            mDrawables[0].getPaint().setColor(0xFFFF0000);
            mDrawables[1].getPaint().setColor(0xFF00FF00);
            mDrawables[2].getPaint().setColor(0xFF0000FF);
            mDrawables[3].getPaint().setShader(makeSweep());
            mDrawables[4].getPaint().setShader(makeLinear());
            mDrawables[5].getPaint().setShader(makeTiling());
            mDrawables[6].getPaint().setColor(0x88FF8844);

            PathEffect pe = new DiscretePathEffect(10, 4);
            PathEffect pe2 = new CornerPathEffect(4);
            mDrawables[3].getPaint().setPathEffect(new ComposePathEffect(pe2, pe));

            MyShapeDrawable msd = (MyShapeDrawable) mDrawables[6];
            msd.getStrokePaint().setStrokeWidth(4);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            final float scale = getResources().getDisplayMetrics().density;
            int x = (int) (10 * scale);
            int y = (int) (10 * scale);
            int width = (int) (300 * scale);
            int height = (int) (50 * scale);

            for (Drawable dr : mDrawables) {
                dr.setBounds(x, y, x + width, y + height);
                dr.draw(canvas);
                y += height + (int) (5 * scale);
            }
        }
    }
}

