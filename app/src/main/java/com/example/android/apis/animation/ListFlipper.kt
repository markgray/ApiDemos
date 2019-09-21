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

@file:Suppress("MemberVisibilityCanBePrivate")

package com.example.android.apis.animation

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import com.example.android.apis.R

/**
 * Uses fancy custom ObjectAnimator to swap two list views occupying the same space in a LinearLayout,
 * by setting one to android:visibility="gone" and the other to android:visibility="visible"
 * when they are to be "flipped". The english list starts as the visible list as defined in the
 * Layout xml file.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class ListFlipper : Activity() {

    /**
     * `ListView` in our layout with id R.id.list_en, hold our english numbers.
     */
    internal lateinit var mEnglishList: ListView
    /**
     * `ListView` in our layout with id R.id.list_fr, hold our french numbers.
     */
    internal lateinit var mFrenchList: ListView

    /**
     * `AccelerateInterpolator` used for `ObjectAnimator visToInvis` which rotates the
     * visible `ListView` around the "y" axis until it becomes invisible.
     */
    private val accelerator = AccelerateInterpolator()
    /**
     * `DecelerateInterpolator` used for `ObjectAnimator invisToVis` which rotates the
     * invisible `ListView` around the "y" axis until it becomes visible.
     */
    private val decelerator = DecelerateInterpolator()

    /**
     * Called when the activity starting. First we call through to our super's implementation of
     * onCreate, the we set our content view to our layout file R.layout.rotating_list. We set our
     * fields ListView mEnglishList, and ListView mFrenchList to the respective ListView's
     * R.id.list_en and R.id.list_fr. We create the Adapter's for our ListView's:
     *
     *
     * ArrayAdapter<String> adapterEn, and ArrayAdapter<String>
    </String></String> *
     *
     * adapterFr from the String[]'s LIST_STRINGS_EN and LIST_STRINGS_FR, and setAdapter them to
     * their ListView. Then we set the degrees that the mFrenchList ListView (currently GONE) is
     * rotated around the vertical axis to -90f (face down). Finally we locate the "FLIP" Button
     * in our layout (R.id.button) and set its OnClickListener to a callback which will call our
     * method flipit() which will animate between the two ListView's.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rotating_list)
        //FrameLayout container = (LinearLayout) findViewById(R.id.container);
        mEnglishList = findViewById(R.id.list_en)
        mFrenchList = findViewById(R.id.list_fr)

        // Prepare the ListView
        val adapterEn = ArrayAdapter(this,
                android.R.layout.simple_list_item_1, LIST_STRINGS_EN)
        // Prepare the ListView
        val adapterFr = ArrayAdapter(this,
                android.R.layout.simple_list_item_1, LIST_STRINGS_FR)

        mEnglishList.adapter = adapterEn
        mFrenchList.adapter = adapterFr
        mFrenchList.rotationY = -90f

        val starter = findViewById<Button>(R.id.button)
        starter.setOnClickListener {
            flipit()
        }
    }

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
    private fun flipit() {
        val visibleList: ListView
        val invisibleList: ListView
        if (mEnglishList.visibility == View.GONE) {
            visibleList = mFrenchList
            invisibleList = mEnglishList
        } else {
            invisibleList = mFrenchList
            visibleList = mEnglishList
        }
        val visToInvis = ObjectAnimator.ofFloat(visibleList, "rotationY", 0f, 90f)
        visToInvis.duration = 500
        visToInvis.interpolator = accelerator
        val invisToVis = ObjectAnimator.ofFloat(invisibleList, "rotationY",
                -90f, 0f)
        invisToVis.duration = 500
        invisToVis.interpolator = decelerator
        visToInvis.addListener(object : AnimatorListenerAdapter() {
            /**
             * Notifies the end of the animation. When the visToInvis animation ends we set the
             * visibility of visibleList to GONE, start the invisToVis animation and set the
             * visibility of invisibleList to VISIBLE.
             *
             * @param anim The animation which reached its end.
             */
            override fun onAnimationEnd(anim: Animator) {
                visibleList.visibility = View.GONE
                invisToVis.start()
                invisibleList.visibility = View.VISIBLE
            }
        })
        visToInvis.start()
    }

    companion object {

        /**
         * Our list of english numbers.
         */
        private val LIST_STRINGS_EN = arrayOf("One", "Two", "Three", "Four", "Five", "Six")
        /**
         * Our list of french numbers.
         */
        private val LIST_STRINGS_FR = arrayOf("Un", "Deux", "Trois", "Quatre", "Le Five", "Six")
    }


}