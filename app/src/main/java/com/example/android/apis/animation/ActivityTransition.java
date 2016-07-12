/*
 * Copyright (C) 2013 The Android Open Source Project
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
import android.app.SharedElementCallback;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.List;
import java.util.Map;

/**
 * Uses ActivityOptions.makeSceneTransitionAnimation to transition using a
 * shared view as the epicenter of the transition.
 */
public class ActivityTransition extends Activity {

    @SuppressWarnings("unused")
    private static final String TAG = "ActivityTransition";

    private static final String KEY_ID = "ViewTransitionValues:id";

    private ImageView mHero;

    public static final int[] DRAWABLES = {
            R.drawable.ball,
            R.drawable.block,
            R.drawable.ducky,
            R.drawable.jellies,
            R.drawable.mug,
            R.drawable.pencil,
            R.drawable.scissors,
            R.drawable.woot,
    };

    public static final int[] IDS = {
            R.id.ball,
            R.id.block,
            R.id.ducky,
            R.id.jellies,
            R.id.mug,
            R.id.pencil,
            R.id.scissors,
            R.id.woot,
    };

    public static final String[] NAMES = {
            "ball",
            "block",
            "ducky",
            "jellies",
            "mug",
            "pencil",
            "scissors",
            "woot",
    };

    /**
     * Passed a string name of an item returns the R.id.* for the thumbnail in the layout
     *
     * @param id String name of item
     * @return Resource R.id.* of that item in layout
     */
    public static int getIdForKey(String id) {
        return IDS[getIndexForKey(id)];
    }

    /**
     * Passed a string name of an item returns the R.drawable.* for the image
     * @param id String name of item
     * @return R.drawable.* for the image
     */
    public static int getDrawableIdForKey(String id) {
        return DRAWABLES[getIndexForKey(id)];
    }

    /**
     * Searches the array of names of id string and returns the index number for that string
     *
     * @param id String name of an item
     * @return Index in the arrays for it (or "2" if not found)
     */
    public static int getIndexForKey(String id) {
        for (int i = 0; i < NAMES.length; i++) {
            String name = NAMES[i];
            if (name.equals(id)) {
                return i;
            }
        }
        return 2;
    }

    /**
     * Sets a random background color, Loads the activity layout and sets up the transition
     * "hero" if the activity was launched by an clicked() return from ActivityTransitionDetails
     * (If the back button was pushed instead, onCreate is not called again and the background color
     * remains the same.)
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(randomColor()));
        setContentView(R.layout.image_block);
        setupHero();
    }

    /**
     * Sets up the SharedElementCallback for a clicked() return from ActivityTransitionDetails,
     * does nothing on an initial launching.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupHero() {
        String name = getIntent().getStringExtra(KEY_ID);
        mHero = null;
        if (name != null) {
            mHero = (ImageView) findViewById(getIdForKey(name));
            setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names,
                        Map<String, View> sharedElements) {
                    sharedElements.put("hero", mHero);
                }
            });
        }
    }

    /**
     * This is called by each ImageView in image_block.xml's GridView using android:onClick="clicked"
     * The ImageView's contain an android:transitionName element naming the transitionName to be the
     * same as the ImageView's drawable, and this is retrieved by v.getTransitionName()
     *
     * @param v View in the GridView which has been clicked
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void clicked(View v) {
        mHero = (ImageView) v;
        Intent intent = new Intent(this, ActivityTransitionDetails.class);
        String transitionName = v.getTransitionName();
        intent.putExtra(KEY_ID, transitionName);
        ActivityOptions activityOptions
                = ActivityOptions.makeSceneTransitionAnimation(this, mHero, "hero");
        startActivity(intent, activityOptions.toBundle());
    }

    /**
     * Create a random color with maximum alpha and the three RGB colors <=128 intensity
     *
     * @return Random color
     */
    private static int randomColor() {
        int red = (int)(Math.random() * 128);
        int green = (int)(Math.random() * 128);
        int blue = (int)(Math.random() * 128);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }
}
