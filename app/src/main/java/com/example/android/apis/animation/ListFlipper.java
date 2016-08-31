/*
 * Copyright (C) 2010 The Android Open Source Project
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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import android.animation.AnimatorListenerAdapter;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.android.apis.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

/**
 * Uses fancy custom ObjectAnimator to swap two list views occupying the same space in a LinearLayout,
 * by setting one to android:visibility="gone" and the other to android:visibility="visible"
 * when they are to be "flipped". The english list starts as the visible list as defined in the
 * Layout xml file.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ListFlipper extends Activity {

    @SuppressWarnings("unused")
    private static final int DURATION = 1500;
    @SuppressWarnings("unused")
    private SeekBar mSeekBar;

    private static final String[] LIST_STRINGS_EN = new String[] {
            "One",
            "Two",
            "Three",
            "Four",
            "Five",
            "Six"
    };
    private static final String[] LIST_STRINGS_FR = new String[] {
            "Un",
            "Deux",
            "Trois",
            "Quatre",
            "Le Five",
            "Six"
    };

    ListView mEnglishList;
    ListView mFrenchList;

    /** Called when the activity is first created. */
    /**
     * First we call through to our super's implementation of onCreate, the we set our content
     * view to our layout file R.layout.rotating_list. We set our fields ListView mEnglishList, and
     * ListView mFrenchList to the respective ListView's R.id.list_en and R.id.list_fr. We create
     * the Adapter's for our ListView's: ArrayAdapter<String> adapterEn, and ArrayAdapter<String>
     * adapterFr from the String[]'s LIST_STRINGS_EN and LIST_STRINGS_FR, and setAdapter them to
     * their ListView. Then we set the degrees that the mFrenchList ListView (currently GONE) is
     * rotated around the vertical axis to -90f (face down). Finally we locate the "FLIP" Button
     * in our layout (R.id.button) and set its OnClickListener to a callback which will call our
     * method flipit() which will animate between the two ListView's.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not called
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rotating_list);
        //FrameLayout container = (LinearLayout) findViewById(R.id.container);
        mEnglishList = (ListView) findViewById(R.id.list_en);
        mFrenchList = (ListView) findViewById(R.id.list_fr);

        // Prepare the ListView
        final ArrayAdapter<String> adapterEn = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, LIST_STRINGS_EN);
        // Prepare the ListView
        final ArrayAdapter<String> adapterFr = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, LIST_STRINGS_FR);

        mEnglishList.setAdapter(adapterEn);
        mFrenchList.setAdapter(adapterFr);
        mFrenchList.setRotationY(-90f);

        Button starter = (Button) findViewById(R.id.button);
        starter.setOnClickListener(new View.OnClickListener() {
            /**
             * When the "FLIP" Button is clicked we call the method flipit() which runs an
             * animation swapping the two ListView's mEnglishList and mFrenchList.
             *
             * @param v Button which has been clicked
             */
            @Override
            public void onClick(View v) {
                flipit();
            }
        });
    }

    private Interpolator accelerator = new AccelerateInterpolator();
    private Interpolator decelerator = new DecelerateInterpolator();
    /**
     * This method creates and start's the animation to flip between the two ListView's. First we
     * determine which of our two ListView's (mEnglishList or mFrenchList) is currently GONE and
     * which ListView is currently VISIBLE and set the variables ListView invisibleList and
     * ListView visibleList accordingly. Then we create an ObjectAnimator visToInvis which
     * animates the "rotationY" from 0f to 90f, set its duration to 500 milliseconds, and set its
     * interpolator to an instance of AccelerateInterpolator. We then create an ObjectAnimator
     * invisToVis which animates the "rotationY" from -90f to 0f, set its duration to 500
     * milliseconds, and set its interpolator to an instance of DecelerateInterpolator. We add
     * an AnimatorListenerAdapter to ObjectAnimator visToInvis which sets the visibility of
     * visibleList to GONE, starts ObjectAnimator invisToVis, and set the visibility of
     * invisibleList to VISIBLE. Finally we start ObjectAnimator visToInvis.
     */
    private void flipit() {
        final ListView visibleList;
        final ListView invisibleList;
        if (mEnglishList.getVisibility() == View.GONE) {
            visibleList = mFrenchList;
            invisibleList = mEnglishList;
        } else {
            invisibleList = mFrenchList;
            visibleList = mEnglishList;
        }
        ObjectAnimator visToInvis = ObjectAnimator.ofFloat(visibleList, "rotationY", 0f, 90f);
        visToInvis.setDuration(500);
        visToInvis.setInterpolator(accelerator);
        final ObjectAnimator invisToVis = ObjectAnimator.ofFloat(invisibleList, "rotationY",
                -90f, 0f);
        invisToVis.setDuration(500);
        invisToVis.setInterpolator(decelerator);
        visToInvis.addListener(new AnimatorListenerAdapter() {
            /**
             * Notifies the end of the animation. When the visToInvis animation ends we set the
             * visibility of visibleList to GONE, start the invisToVis animation and set the
             * visibility of invisibleList to VISIBLE.
             *
             * @param anim The animation which reached its end.
             */
            @Override
            public void onAnimationEnd(Animator anim) {
                visibleList.setVisibility(View.GONE);
                invisToVis.start();
                invisibleList.setVisibility(View.VISIBLE);
            }
        });
        visToInvis.start();
    }


}