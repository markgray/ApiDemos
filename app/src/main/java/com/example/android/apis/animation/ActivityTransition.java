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
 * shared view as the epicenter of the transition. The xml layout file
 * layout/image_block.xml sets android:onClick="clicked" to use for each thumbnail
 * in the GridLayout and the {@code clicked()} method creates an intent to launch
 * ActivityTransitionDetails.class using a bundle containing an
 * ActivityOptions.makeSceneTransitionAnimation() which causes the thumbnail to "expand"
 * into the image detail version. When the ImageView in the image detail version is clicked,
 * the reverse transition to ActivityTransition activity occurs. The animation is set up using
 * AndroidManifest android:theme="@style/ActivityTransitionTheme" which contains elements which point
 * to files in res/transition
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ActivityTransition extends Activity {

    @SuppressWarnings("unused")
    private static final String TAG = "ActivityTransition";

    /**
     * Key used to store the transition name in the extra of the {@code Intent} that is used to
     * launch both us and {@code ActivityTransitionDetails}
     */
    private static final String KEY_ID = "ViewTransitionValues:id";

    /**
     * This is the {@code ImageView} in our {@code GridView} which was clicked, and which we
     * "share" during the transition to {@code ActivityTransitionDetails} and back again.
     */
    private ImageView mHero;

    /**
     * This is the list of the jpg drawables which populates our {@code GridView}
     */
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

    /**
     * This is the list of the resource ids of the {@code ImageView}s in our layout file's
     * {@code GridView} (file layout/image_block.xml)
     */
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

    /**
     * String name of the {@code ImageView}s in our layout file, used as the android:transitionName
     * attribute for the respective {@code ImageView} in our layout file's {@code GridView} and
     * passed as an extra to the {@code Intent} that launches both us and {@code ActivityTransitionDetails}
     * stored under the key KEY_ID ("ViewTransitionValues:id")
     */
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
     * Passed a string name of an item returns the R.id.* for the thumbnail in the layout. We call
     * our method {@code getIndexForKey(id)} to convert the {@code String id} to the index in the
     * {@code String[] NAMES} array which us occupied by an equal string, then use that index to access
     * the corresponding entry in the {@code int[] IDS} array which we return to our caller.
     *
     * @param id String name of item
     * @return Resource R.id.* of that item in layout
     */
    public static int getIdForKey(String id) {
        return IDS[getIndexForKey(id)];
    }

    /**
     * Passed a string name of an item returns the R.drawable.* for the image. We call our method
     * {@code getIndexForKey(id)} to convert the {@code String id} to the index in the {@code String[] NAMES}
     * array which is occupied by an equal string, then use that index to access the corresponding
     * entry in the {@code int[] DRAWABLES} array which we return to our caller.
     *
     * @param id String name of item
     * @return R.drawable.* for the image
     */
    public static int getDrawableIdForKey(String id) {
        return DRAWABLES[getIndexForKey(id)];
    }

    /**
     * Searches the array of names of id string and returns the index number for that string. We loop
     * over {@code int i} for all of the strings in {@code String[] NAMES} setting {@code String name}
     * to the current {@code NAMES[i]} then if {@code name} is equal to our argument {@code String id}
     * we return {@code i} to the caller. If none of the strings in {@code NAMES} match {@code id} we
     * return 2 to the caller.
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
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Then we set a random background color for our window chosen by our method
     * {@code randomColor}, and set our content view to our layout file R.layout.image_block.
     * Finally we call our method {@code setupHero} which sets up the transition "hero" if the activity
     * was launched by a {@code clicked()} return from the activity {@code ActivityTransitionDetails}.
     * (If the back button was pushed instead, {@code onCreate} is not called again and the background
     * color remains the same.)
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
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
     * does nothing on an initial launching. We retrieve the extra stored under the key KEY_ID
     * ("ViewTransitionValues:id") in the {@code Intent} that launched us in order to initialize
     * {@code String name}. We set our field {@code ImageView mHero} to null, then if {@code name}
     * is not null (the KEY_ID extra was found) we set {@code mHero} by finding the resource id
     * corresponding to {@code name} returned by our method {@code getIdForKey} then finding the
     * {@code ImageView} in our layout with that id. Finally we set the shared element callback to
     * an anonymous class using the method {@code setEnterSharedElementCallback}. That anonymous
     * class adds {@code mHero} to the {@code sharedElements} map passed it under the key "hero".
     */
    private void setupHero() {
        String name = getIntent().getStringExtra(KEY_ID);
        mHero = null;
        if (name != null) {
            mHero = findViewById(getIdForKey(name));
            setEnterSharedElementCallback(new SharedElementCallback() {
                /**
                 * Lets the SharedElementCallback adjust the mapping of shared element names to
                 * Views. We just add our field {@code ImageView mHero} to our argument
                 * {@code sharedElements} under the key "hero".
                 *
                 * @param names The names of all shared elements transferred from the calling Activity
                 *              or Fragment in the order they were provided.
                 * @param sharedElements The mapping of shared element names to Views. The best guess
                 *                       will be filled into sharedElements based on the transitionNames.
                 */
                @Override
                public void onMapSharedElements(List<String> names,
                                                Map<String, View> sharedElements) {
                    sharedElements.put("hero", mHero);
                }
            });
        }
    }

    /**
     * This is called by each ImageView in image_block.xml's GridView using android:onClick="clicked".
     * First we set our field {@code ImageView mHero} to the {@code View v} that was clicked. Then we
     * create {@code Intent intent} with the activity {@code ActivityTransitionDetails} as the class
     * that is to be launched by the intent. We initialize {@code String transitionName} to the
     * transition name of {@code v} (this is set by the android:transitionName attribute of the view
     * in the layout file). We add {@code transitionName} as an extra to {@code intent} using the key
     * KEY_ID ("ViewTransitionValues:id"). We create {@code ActivityOptions activityOptions} by
     * calling the method {@code makeSceneTransitionAnimation} to create an ActivityOptions that uses
     * {@code mHero} as the View to transition to in the started Activity, and "hero" as the shared
     * element name as used in the target Activity. We then start the activity specified by our
     * {@code intent} with a "bundled up" {@code activityOptions} as the bundle (this all causes the
     * transition between our activities to use cross-Activity scene animations with {@code mHero}
     * as the epicenter of the transition).
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
     * Create a random color with maximum alpha and the three RGB colors <=128 intensity.
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
