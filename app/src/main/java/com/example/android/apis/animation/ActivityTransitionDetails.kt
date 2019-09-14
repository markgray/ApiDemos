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
package com.example.android.apis.animation

import com.example.android.apis.R

import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView

/**
 * This is the companion activity for the `ActivityTransition` demo, and displays an enlarged
 * version of an `ImageView` when it is clicked, with fancy activity transition between the
 * two activities.
 */
class ActivityTransitionDetails : Activity() {

    /**
     * Resource id of the image we were launched to display, we look it up using the method
     * `ActivityTransition.getDrawableIdForKey` based on the string stored as an extra
     * under the key KEY_ID in the intent that launched us. We default to R.drawable.ducky
     */
    private var mImageResourceId = R.drawable.ducky

    /**
     * String stored as an extra under the key KEY_ID in the intent that launched us.
     * We default to "ducky"
     */
    private var mName = "ducky"

    /**
     * Retrieves a drawable based on the name stored as an extra in the Intent launching the activity
     * under the key KEY_ID. First we initialize `String name` by retrieving the string stored
     * in the intent that launched us under the key KEY_ID ("ViewTransitionValues:id"), if that is
     * not null we set our field `String mName` to it and set our field `int mImageResourceId`
     * to the resource id that the method `ActivityTransition.getDrawableIdForKey` finds for the
     * string `name` (these default to "ducky" and R.drawable.ducky respectively if for some
     * reason the Intent did not contain a KEY_ID name). We declare `Drawable drawable`, and if
     * the build version is less than or equal to LOLLIPOP we set `drawable` using the old deprecated
     * one argument version of `getDrawable` for the resource id `mImageResourceId`,
     * otherwise we use the new two argument version of `getDrawable` to set it. Finally we
     * return `drawable` to the caller.
     *
     * @return Drawable to be displayed full size
     */
    @Suppress("DEPRECATION")
    private val heroDrawable: Drawable
        get() {
            val name = intent.getStringExtra(KEY_ID)
            if (name != null) {
                mName = name
                mImageResourceId = ActivityTransition.getDrawableIdForKey(name)
            }

            return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                resources.getDrawable(mImageResourceId)
            } else {
                resources.getDrawable(mImageResourceId, null)
            }
        }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. We set the background to a random color, and then we set our content view
     * to our layout file R.layout.titleImage. We initialize `ImageView titleImage` by finding
     * the view with the id R.id.titleImage, then set its drawable to the image that our method
     * `getHeroDrawable` finds that corresponds to the name string stored under the key KEY_ID
     * in the Intent which launched our activity.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(randomColor()))
        setContentView(R.layout.image_details)
        val titleImage = findViewById<ImageView>(R.id.titleImage)
        titleImage.setImageDrawable(heroDrawable)
    }

    /**
     * This method is specified as the click callback for the full sized ImageView using
     * android:onClick="clicked" in the image_details.xml layout file. That ImageView contains
     * an android:transitionName element naming the transitionName "hero" and is used to put
     * an ActivityOptions.makeSceneTransitionAnimation into the bundle used with the Intent
     * to launch ActivityTransition.class
     *
     *
     * First we create `Intent intent` with `ActivityTransition` as the target activity
     * to launch. Then we add `mName` as an extra under the key KEY_ID. We create the scene
     * transition animation `ActivityOptions activityOptions` using the shared element name
     * "hero", then start the activity specified in `Intent intent` with a bundled up
     * `activityOptions` as additional options for how the Activity should be started.
     *
     * @param v ImageView R.id.titleImage clicked on
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun clicked(v: View) {
        val intent = Intent(this, ActivityTransition::class.java)
        intent.putExtra(KEY_ID, mName)
        val activityOptions = ActivityOptions.makeSceneTransitionAnimation(this, v, "hero")
        startActivity(intent, activityOptions.toBundle())
    }

    companion object {

        @Suppress("unused")
        private const val TAG = "ActivityTransitionDetails"

        /**
         * Key used to store the transition name in the extra of the `Intent` that is used to
         * launch both us and `ActivityTransition`
         */
        private const val KEY_ID = "ViewTransitionValues:id"

        /**
         * Create a random color with maximum alpha and the three RGB colors <=128 intensity
         *
         * @return Random color
         */
        private fun randomColor(): Int {
            val red = (Math.random() * 128).toInt()
            val green = (Math.random() * 128).toInt()
            val blue = (Math.random() * 128).toInt()
            return -0x1000000 or (red shl 16) or (green shl 8) or blue
        }
    }
}
