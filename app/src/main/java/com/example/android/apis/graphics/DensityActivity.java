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

//Need the following import to get access to the app resources, since this
//class is in a sub-package.

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.android.apis.R;

/**
 * This activity demonstrates various ways density can cause the scaling of
 * bitmaps and drawables. Includes sample code for different ways to get
 * drawables onto the different dpi screens.
 */
@SuppressWarnings("deprecation")
public class DensityActivity extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we fetch a handle to the system-level service LAYOUT_INFLATER_SERVICE
     * to {@code LayoutInflater li}. We set our title to R.string.density_title, and the system
     * chooses the title from 5 different strings depending on the screen density:
     * <ul>
     * <li>values/strings.xml "Density: Unknown Screen"</li>
     * <li>values-hdpi/strings.xml "Density: High"</li>
     * <li>values-ldpi/strings.xml "Density: Low"</li>
     * <li>values-mdpi/strings.xml "Density: Medium"</li>
     * <li>values-xhdpi/strings.xml "Density: Extra High"</li>
     * </ul>
     * We create an instance of {@code LinearLayout root} and set its orientation to VERTICAL. This is
     * the {@code ViewGroup} we will add 9 rows of {@code LinearLayout layout}'s to, each row consisting
     * of 120dpi, 160dpi and 240dpi images stored and loaded using different approaches:
     * <ul>
     * <li>
     * "Pre-scaled bitmap in drawable" uses our method {@code addBitmapDrawable} to add the
     * resource images logo120dpi.png, logo160dpi.png, and logo240dpi.png (loaded using scaling)
     * to {@code LinearLayout layout} which we then add to {@code root} using our method
     * {@code addChildToRoot}
     * </li>
     * <li>
     * "Auto-scaled bitmap in drawable" uses our method {@code addBitmapDrawable} to add the
     * resource images logo120dpi.png, logo160dpi.png, and logo240dpi.png (loaded without scaling)
     * to {@code LinearLayout layout} which we then add to {@code root} using our method
     * {@code addChildToRoot}
     * </li>
     * <li>
     * "Pre-scaled resource drawable" uses our method {@code addResourceDrawable} to load the
     * resource images logo120dpi.png, logo160dpi.png, and logo240dpi.png, loaded using
     * {@code getDrawable} and used to set the background of a view which it adds
     * to {@code LinearLayout layout} which we then add to {@code root} using our method
     * {@code addChildToRoot}
     * </li>
     * <li>
     *     "Inflated layout" inflates the layout file R.layout.density_image_views which creates a
     *     {@code LinearLayout layout} containing three {@code ImageView}'s which use the resource
     *     images logo120dpi.png, logo160dpi.png, and logo240dpi.png as their content which we then
     *     add to {@code root} using our method {@code addChildToRoot}
     * </li>
     * </ul>
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        this.setTitle(R.string.density_title);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        LinearLayout layout = new LinearLayout(this);
        addBitmapDrawable(layout, R.drawable.logo120dpi, true);
        addBitmapDrawable(layout, R.drawable.logo160dpi, true);
        addBitmapDrawable(layout, R.drawable.logo240dpi, true);
        addLabelToRoot(root, "Pre-scaled bitmap in drawable");
        addChildToRoot(root, layout);

        layout = new LinearLayout(this);
        addBitmapDrawable(layout, R.drawable.logo120dpi, false);
        addBitmapDrawable(layout, R.drawable.logo160dpi, false);
        addBitmapDrawable(layout, R.drawable.logo240dpi, false);
        addLabelToRoot(root, "Auto-scaled bitmap in drawable");
        addChildToRoot(root, layout);

        layout = new LinearLayout(this);
        addResourceDrawable(layout, R.drawable.logo120dpi);
        addResourceDrawable(layout, R.drawable.logo160dpi);
        addResourceDrawable(layout, R.drawable.logo240dpi);
        addLabelToRoot(root, "Pre-scaled resource drawable");
        addChildToRoot(root, layout);

        layout = (LinearLayout) li.inflate(R.layout.density_image_views, root, false);
        addLabelToRoot(root, "Inflated layout");
        addChildToRoot(root, layout);

        layout = (LinearLayout) li.inflate(R.layout.density_styled_image_views, root, false);
        addLabelToRoot(root, "Inflated styled layout");
        addChildToRoot(root, layout);

        layout = new LinearLayout(this);
        addCanvasBitmap(layout, R.drawable.logo120dpi, true);
        addCanvasBitmap(layout, R.drawable.logo160dpi, true);
        addCanvasBitmap(layout, R.drawable.logo240dpi, true);
        addLabelToRoot(root, "Pre-scaled bitmap");
        addChildToRoot(root, layout);

        layout = new LinearLayout(this);
        addCanvasBitmap(layout, R.drawable.logo120dpi, false);
        addCanvasBitmap(layout, R.drawable.logo160dpi, false);
        addCanvasBitmap(layout, R.drawable.logo240dpi, false);
        addLabelToRoot(root, "Auto-scaled bitmap");
        addChildToRoot(root, layout);

        layout = new LinearLayout(this);
        addResourceDrawable(layout, R.drawable.logonodpi120);
        addResourceDrawable(layout, R.drawable.logonodpi160);
        addResourceDrawable(layout, R.drawable.logonodpi240);
        addLabelToRoot(root, "No-dpi resource drawable");
        addChildToRoot(root, layout);

        layout = new LinearLayout(this);
        addNinePatchResourceDrawable(layout, R.drawable.smlnpatch120dpi);
        addNinePatchResourceDrawable(layout, R.drawable.smlnpatch160dpi);
        addNinePatchResourceDrawable(layout, R.drawable.smlnpatch240dpi);
        addLabelToRoot(root, "Pre-scaled 9-patch resource drawable");
        addChildToRoot(root, layout);

        setContentView(scrollWrap(root));
    }

    private View scrollWrap(View view) {
        ScrollView scroller = new ScrollView(this);
        scroller.addView(view, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.MATCH_PARENT));
        return scroller;
    }

    private void addLabelToRoot(LinearLayout root, String text) {
        TextView label = new TextView(this);
        label.setText(text);
        root.addView(label, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    /**
     * Adds the {@code LinearLayout layout} to {@code LinearLayout root} using the layout parameters
     * LinearLayout root, and WRAP_CONTENT
     *
     * @param root   {@code LinearLayout} {@code ViewGroup} which we want to add
     *               {@code LinearLayout layout} to
     * @param layout {@code LinearLayout} we are to add to {@code LinearLayout root}
     */
    private void addChildToRoot(LinearLayout root, LinearLayout layout) {
        root.addView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    /**
     * Adds a {@code Bitmap} decoded from resource ID {@code resource} to {@code LinearLayout layout},
     * optionally using scaling if the flag {@code boolean scale} is true.
     *
     * @param layout   {@code LinearLayout} we are to add the resource {@code Drawable} to
     * @param resource resource ID of a {@code Drawable} to read and use from our resources
     * @param scale    flag indicating whether our method {@code loadAndPrintDpi} should use scaling when
     *                 decoding the resource image.
     */
    private void addBitmapDrawable(LinearLayout layout, int resource, boolean scale) {
        Bitmap bitmap;
        bitmap = loadAndPrintDpi(resource, scale);

        View view = new View(this);

        final BitmapDrawable d = new BitmapDrawable(getResources(), bitmap);
        if (!scale) d.setTargetDensity(getResources().getDisplayMetrics());
        //noinspection deprecation
        view.setBackgroundDrawable(d);

        view.setLayoutParams(new LinearLayout.LayoutParams(d.getIntrinsicWidth(), d.getIntrinsicHeight()));
        layout.addView(view);
    }

    /**
     * Creates a {@code View view}, loads the resource drawable with resource ID {@code resource} into
     * a {@code Drawable d} and sets the background drawable of {@code view} to it. Sets the layout
     * parameters of {@code view} to the intrinsic width and height of {@code d} then adds {@code view}
     * to {@code LinearLayout layout}.
     *
     * @param layout   {@code LinearLayout} {@code ViewGroup} we are to add the resource image with
     *                 resource ID {@code resource} to.
     * @param resource resource ID of an resource image to load.
     */
    private void addResourceDrawable(LinearLayout layout, int resource) {
        View view = new View(this);

        final Drawable d = getResources().getDrawable(resource);
        view.setBackgroundDrawable(d);

        //noinspection ConstantConditions
        view.setLayoutParams(new LinearLayout.LayoutParams(d.getIntrinsicWidth(), d.getIntrinsicHeight()));
        layout.addView(view);
    }

    private void addCanvasBitmap(LinearLayout layout, int resource, boolean scale) {
        Bitmap bitmap;
        bitmap = loadAndPrintDpi(resource, scale);

        ScaledBitmapView view = new ScaledBitmapView(this, bitmap);

        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(view);
    }

    private void addNinePatchResourceDrawable(LinearLayout layout, int resource) {
        View view = new View(this);

        final Drawable d = getResources().getDrawable(resource);
        view.setBackgroundDrawable(d);

        //noinspection ConstantConditions
        Log.i("foo", "9-patch #" + Integer.toHexString(resource)
                + " w=" + d.getIntrinsicWidth() + " h=" + d.getIntrinsicHeight());
        view.setLayoutParams(new LinearLayout.LayoutParams(d.getIntrinsicWidth() * 2, d.getIntrinsicHeight() * 2));
        layout.addView(view);
    }

    /**
     * Decodes the resource image with the resource ID {@code id} into a {@code Bitmap} which it returns,
     * optionally applying scaling if {@code boolean scale} is true.
     *
     * @param id    resource ID of an image to load into the {@code Bitmap} we return
     * @param scale if true we allow {@code decodeResource} to scale the image, false if we want it unscaled
     * @return {@code Bitmap} decoded from resource ID {@code id} resource image
     */
    private Bitmap loadAndPrintDpi(int id, boolean scale) {
        Bitmap bitmap;
        if (scale) {
            bitmap = BitmapFactory.decodeResource(getResources(), id);
        } else {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inScaled = false;
            bitmap = BitmapFactory.decodeResource(getResources(), id, opts);
        }
        return bitmap;
    }

    private class ScaledBitmapView extends View {
        private Bitmap mBitmap;

        public ScaledBitmapView(Context context, Bitmap bitmap) {
            super(context);
            mBitmap = bitmap;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            final DisplayMetrics metrics = getResources().getDisplayMetrics();
            setMeasuredDimension(
                    mBitmap.getScaledWidth(metrics),
                    mBitmap.getScaledHeight(metrics));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawBitmap(mBitmap, 0.0f, 0.0f, null);
        }
    }
}
