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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R;

import android.os.Bundle;

/**
 * Uses xml to draw {@code <shape>} ImageView(s). shape_5.xml has a {@code <gradient>}
 */
public class GradientDrawable1 extends GraphicsActivity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.shape_drawable_1.
     * This file contains five {@code ImageView}'s separated by a "line" {@code ImageView}. The
     * {@code ImageView}'s are created using xml {@code <shape>} drawables contained in our
     * res/drawable/ directory:
     * <ul>
     *     <li>
     *         drawable/shape_1.xml draws a rectangle by using the default of android:shape="rectangle",
     *         with a {@code <stroke>} of 2dp wide and color "#ff000000" (Black, maximum alpha), and
     *         padding of 1dp around all sides. Its size is defined in the layout file that uses it
     *         to be android:layout_width="match_parent", and android:layout_height="50dip".
     *     </li>
     *     <li>
     *         drawable/line.xml uses android:shape="line" to draw a line between each of the "featured"
     *         {@code <shape>} {@code ImageView}'s and uses a {@code <stroke>} with android:width="1dp",
     *         android:color="#FF000000" (Black), android:dashGap="2dp", and android:dashWidth="1dp"
     *         to make it a dashed line. Its size is set by a {@code <size>} element with attribute
     *         android:height="5dp"
     *     </li>
     *     <li>
     *         drawable/shape_2.xml draws a rectangle by using the default of android:shape="rectangle",
     *         whose color is set by a {@code <solid>} element with attribute android:color="#FF0000FF"
     *         (Dark blue). It surrounds the rectangle with a dashed line defined by a {@code <stroke>}
     *         element with attributes android:width="4dp", android:color="#FFFFFFFF" (White),
     *         android:dashGap="2dp", and android:dashWidth="1dp". It uses a {@code <corners>} element
     *         to round the corners with an attribute of android:radius="4dp". Its size is defined in
     *         the layout file that uses it to be android:layout_width="match_parent", and
     *         android:layout_height="50dip".
     *     </li>
     *     <li>
     *         drawable/shape_3.xml uses android:shape="oval" attribute to draw an oval. It surrounds
     *         the oval with a dashed line defined by a {@code <stroke>} element with attributes
     *         android:width="4dp", android:color="#99000000" (slightly translucent Black that looks
     *         gray), android:dashGap="2dp", and android:dashWidth="4dp". It uses a {@code <padding>}
     *         element to set the padding to 7dp on all sides, It has a {@code <corners android:radius="4dp" />}
     *         element, but that is only used for rectangles so I don't think it has an effect. Its size
     *         is defined in the layout file that uses it to be android:layout_width="match_parent", and
     *         android:layout_height="50dip".
     *     </li>
     *     <li>
     *         drawable/shape_4.xml uses android:shape="line" to draw a line, and uses a {@code <stroke>}
     *         with android:width="1dp", android:color="#FF000000" (Black), android:dashGap="2dp", and
     *         android:dashWidth="1dp" to make it a dashed line. The size of the {@code ImageView} it
     *         occupies is defined in the layout file that uses it to be android:layout_width="match_parent",
     *         and android:layout_height="50dip".
     *     </li>
     *     <li>
     *         drawable/shape_5 uses the attribute android:shape="rectangle" to draw a rectangle, its
     *         color is set by a {@code <gradient>} element with attributes android:angle="270" (top to
     *         bottom), android:endColor="#80FF00FF", and android:startColor="#FFFF0000". It uses a
     *         {@code <padding>} element to set the padding to 7dp around all sides, and uses a
     *         {@code <corners>} element with attribute android:radius="8dp" to round the corners.
     *         The size of the {@code ImageView} it occupies is defined in the layout file that uses
     *         it to be android:layout_width="match_parent", and android:layout_height="50dip".
     *     </li>
     * </ul>
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shape_drawable_1);
    }
}
