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
         * Constructor for our {@code SampleView}. First we call through to our super's constructor,
         * and then we enable our {@code View} to receive focus. Then we allocate and initialize 3
         * arrays we use later for defining {@code RoundRectShape}'s:
         * <ul>
         *     <li>
         *         {@code float[] outerR} is an array  of 8 radius values, for the outer round rect.
         *         The first two floats are for the top-left corner (remaining pairs correspond
         *         clockwise).
         *     </li>
         *     <li>
         *         {@code RectF inset} A RectF that specifies the distance from the inner rect to
         *         each side of the outer rect. For no inner, we pass null.
         *     </li>
         *     <li>
         *         {@code float[] innerR} An array of 8 radius values, for the inner round rect. The
         *         first two floats are for the top-left corner (remaining pairs correspond clockwise).
         *         For no rounded corners on the inner rectangle, we pass null. If inset parameter is
         *         null, this parameter is ignored.
         *     </li>
         * </ul>
         * Next we define {@code Path path} to be a diamond shaped rectangle which we will later use
         * for the {@code PathShape} used for {@code mDrawables[5]}.
         *
         * We initialize our field {@code ShapeDrawable[] mDrawables} by allocating 7 {@code ShapeDrawable}'s.
         * Then we initialize each of these:
         * <ul>
         *     <li>
         *         {@code mDrawables[0]} is a new instance of {@code RectShape}, we will later set the
         *         color of its {@code Paint} to Red.
         *     </li>
         *     <li>
         *         {@code mDrawables[1]} is a new instance of {@code OvalShape}, we will later set the
         *         color of its {@code Paint} to Blue.
         *     </li>
         *     <li>
         *         {@code mDrawables[2]} is a new instance of {@code RoundRectShape} defined using only
         *         {@code outerR} as the outer round rect, we will later set the color of its {@code Paint}
         *         to Green.
         *     </li>
         *     <li>
         *         {@code mDrawables[3]} is a new instance of {@code RoundRectShape} defined using
         *         {@code outerR} as the outer round rect, and {@code inset} as the {@code RectF}
         *         that specifies the distance from the inner rect to each side of the outer rect.
         *         We will later set its {@code Shader} to a {@code SweepGradient Shader} created by
         *         our method {@code makeSweep}.
         *     </li>
         *     <li>
         *         {@code mDrawables[4]} is a new instance of {@code RoundRectShape} defined using
         *         {@code outerR} as the outer round rect, {@code inset} as the {@code RectF} that
         *         specifies the distance from the inner rect to each side of the outer rect, and
         *         {@code innerR} as the inner round rect. We will later set its {@code Shader} to a
         *         {@code LinearGradient Shader} created by our method {@code makeLinear}
         *     </li>
         *     <li>
         *         {@code mDrawables[5]} is a {@code PathShape} using {@code Path path} as its {@code Path}
         *         and 100 for both the standard width and standard height. We will later set its
         *         {@code Shader} to a {@code BitmapShader Shader} created by our method {@code makeTiling}.
         *     </li>
         *     <li>
         *         {@code mDrawables[6]} is a custom {@code MyShapeDrawable} wrapping an {@code ArcShape}
         *         spanning 45 degrees to -270 degrees. We later set the color of its {@code Paint} to
         *         0x88FF8844 (A Brown), and set the stroke width of the private {@code Paint} used to
         *         draw the {@code ArcShape} a second time after the native {@code Paint} is used to 4.
         *     </li>
         * </ul>
         * For {@code mDrawables[3]} we create {@code PathEffect pe} (a {@code DiscretePathEffect} which
         * chops the path into lines of 10, randomly deviating from the original path by 4) and
         * {@code PathEffect pe2} (a {@code CornerPathEffect} which transforms geometries that are
         * drawn (either STROKE or FILL styles) by replacing any sharp angles between line segments
         * into rounded angles of the specified radius 4). We then set the {@code PathEffect} of
         * {@code mDrawables[3]}'s {@code Paint} to a {@code PathEffect} constructed from {@code pe2}
         * and {@code pe} whose effect is to apply first {@code pe} and then {@code pe2} when drawing.
         *
         * @param context {@code Context} to use for resources, "this" {@code ShapeDrawable1} activity
         *                when called from {@code onCreate} in our case.
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            /*
             * Outer round rectangle for {@code RoundRectShape}
             */
            float[] outerR = new float[]{12, 12, 12, 12, 0, 0, 0, 0};
            /*
             * {@code RectF} that specifies the distance from the inner rect to each side of the
             * outer rect when used for {@code RoundRectShape}
             */
            RectF inset = new RectF(6, 6, 6, 6);
            /*
             * An array of 8 radius values, for the inner round rectangle used for {@code RoundRectShape}
             */
            float[] innerR = new float[]{12, 12, 0, 0, 12, 12, 0, 0};

            /*
             * Diamond shaped path used for the {@code PathShape} used for {@code mDrawables[5]}
             */
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

