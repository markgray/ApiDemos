/*
 * Copyright (C) 2014 The Android Open Source Project
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

import com.example.android.apis.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 *
 */
public class ActivityTransitionDetails extends Activity {

    @SuppressWarnings("unused")
    private static final String TAG = "ActivityTransitionDetails";

    private static final String KEY_ID = "ViewTransitionValues:id";

    private int mImageResourceId = R.drawable.ducky;

    private String mName = "ducky";

    /**
     * Sets the background to a random color, sets the activity content from a layout resource which
     * will be inflated, adding all top-level views to the activity, locates the ImageView for the
     * titleImage and sets the image displayed according to the KEY_ID name bundled up in the Intent
     * which launched the activity.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the data it most recently
     *                           supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(randomColor()));
        setContentView(R.layout.image_details);
        ImageView titleImage = (ImageView) findViewById(R.id.titleImage);
        titleImage.setImageDrawable(getHeroDrawable());
    }

    /**
     * Retrieves the name stored as an extra in the Intent launching the activity under the
     * key KEY_ID, uses ActivityTransition.getDrawableIdForKey(name) to look it up in the
     * list DRAWABLES and returns a drawable based on the name. Defaults to R.drawable.ducky
     * if for some reason the Intent did not contain a KEY_ID name.
     *
     * @return Drawable to be displayed full size
     */
    private Drawable getHeroDrawable() {
        String name = getIntent().getStringExtra(KEY_ID);
        if (name != null) {
            mName = name;
            mImageResourceId = ActivityTransition.getDrawableIdForKey(name);
        }

        Drawable drawable;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            drawable = getResources().getDrawable(mImageResourceId);
        } else {
            drawable = getResources().getDrawable(mImageResourceId, null);
        }
        return drawable;
    }

    /**
     * This method is specified as the click callback for the full sized ImageView using
     * android:onClick="clicked" in the image_details.xml layout file. That ImageView contains
     * an android:transitionName element naming the transitionName "hero" and is used to put
     * an ActivityOptions.makeSceneTransitionAnimation into the bundle used with the Intent
     * to launch ActivityTransition.class
     *
     * @param v ImageView R.id.titleImage clicked on
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void clicked(View v) {
        Intent intent = new Intent(this, ActivityTransition.class);
        intent.putExtra(KEY_ID, mName);
        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(this, v, "hero");
        startActivity(intent, activityOptions.toBundle());
    }

    /**
     * Create a random color with maximum alpha and the three RGB colors <=128 intensity
     *
     * @return Random color
     */
    private static int randomColor() {
        int red = (int) (Math.random() * 128);
        int green = (int) (Math.random() * 128);
        int blue = (int) (Math.random() * 128);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }
}
