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
package com.example.android.apis.animation

import android.annotation.TargetApi
import android.app.ActivityOptions
import android.app.SharedElementCallback
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Uses ActivityOptions.makeSceneTransitionAnimation to transition using a
 * shared view as the epicenter of the transition. The xml layout file
 * layout/image_block.xml sets android:onClick="clicked" to use for each thumbnail
 * in the GridLayout and the `clicked()` method creates an intent to launch
 * ActivityTransitionDetails.class using a bundle containing an
 * ActivityOptions.makeSceneTransitionAnimation() which causes the thumbnail to "expand"
 * into the image detail version. When the ImageView in the image detail version is clicked,
 * the reverse transition to ActivityTransition activity occurs. The animation is set up using
 * AndroidManifest android:theme="@style/ActivityTransitionTheme" which contains elements which point
 * to files in res/transition
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class ActivityTransition : AppCompatActivity() {

    /**
     * This is the [ImageView] in our `GridView` which was clicked, and which we
     * "share" during the transition to [ActivityTransitionDetails] and back again.
     */
    private var mHero: ImageView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Then we set a random background color for our window chosen by our method
     * [randomColor], and set our content view to our layout file R.layout.image_block.
     * Finally we call our method [setupHero] which sets up the transition "hero" if the activity
     * was launched by a `clicked()` return from the activity `ActivityTransitionDetails`.
     * (If the back button was pushed instead, `onCreate` is not called again and the background
     * color remains the same.)
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(randomColor()))
        setContentView(R.layout.image_block)
        setupHero()
    }

    /**
     * Sets up the [SharedElementCallback] for a clicked() return from [ActivityTransitionDetails],
     * does nothing on an initial launching. We retrieve the extra stored under the key KEY_ID
     * ("ViewTransitionValues:id") in the [Intent] that launched us in order to initialize
     * `String name`. We set our [ImageView] field [mHero] to *null*, then if `name`
     * is not *null* (the KEY_ID extra was found) we set [mHero] by finding the resource id
     * corresponding to `name` returned by our method [getIdForKey] then finding the
     * [ImageView] in our layout with that id. Finally we set the shared element callback to
     * an anonymous class using the method `setEnterSharedElementCallback`. That anonymous
     * class adds `mHero` to the `sharedElements` map passed to its override of the `onMapSharedElements`
     * method under the key "hero".
     */
    private fun setupHero() {
        val name = intent.getStringExtra(KEY_ID)
        mHero = null
        if (name != null) {
            mHero = findViewById(getIdForKey(name))
            setEnterSharedElementCallback(object : SharedElementCallback() {
                /**
                 * Lets the [SharedElementCallback] adjust the mapping of shared element names to
                 * [View]'s. We just add our [ImageView] field [mHero] to our argument
                 * `sharedElements` under the key "hero".
                 *
                 * @param names The names of all shared elements transferred from the calling Activity
                 * or Fragment in the order they were provided.
                 * @param sharedElements The mapping of shared element names to Views. The best guess
                 * will be filled into sharedElements based on the transitionNames.
                 */
                override fun onMapSharedElements(names: List<String>,
                                                 sharedElements: MutableMap<String, View>) {
                    sharedElements["hero"] = mHero as View
                }
            })
        }
    }

    /**
     * This is called by each [ImageView] in image_block.xml's GridView using the attribute
     * android:onClick="clicked". First we set our [ImageView] field [mHero] to the `View v`
     * that was clicked. Then we create [Intent] `intent` with the activity `ActivityTransitionDetails`
     * as the class that is to be launched by the intent. We initialize `String transitionName`
     * to the transition name of `v` (this is set by the android:transitionName attribute of the
     * view in the layout file). We add `transitionName` as an extra to `intent` using the key
     * KEY_ID ("ViewTransitionValues:id"). We create `ActivityOptions activityOptions` by
     * calling the method `makeSceneTransitionAnimation` to create an [ActivityOptions] that uses
     * [mHero] as the [View] to transition to in the started Activity, and "hero" as the shared
     * element name as used in the target Activity. We then start the activity specified by our
     * `intent` with a "bundled up" `activityOptions` as the bundle (this all causes the
     * transition between our activities to use cross-Activity scene animations with `mHero`
     * as the epicenter of the transition).
     *
     * @param v View in the GridView which has been clicked
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun clicked(v: View) {
        mHero = v as ImageView
        val intent = Intent(this, ActivityTransitionDetails::class.java)
        val transitionName = v.getTransitionName()
        intent.putExtra(KEY_ID, transitionName)
        val activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                this, mHero,
                "hero"
        )
        startActivity(intent, activityOptions.toBundle())
    }

    /**
     * Our static constants, and methods.
     */
    companion object {
        /**
         * A TAG that could be used for logging but isn't
         */
        @Suppress("unused")
        private const val TAG = "ActivityTransition"

        /**
         * Key used to store the transition name in the extra of the `Intent` that is used to
         * launch both us and `ActivityTransitionDetails`
         */
        private const val KEY_ID = "ViewTransitionValues:id"

        /**
         * This is the list of the jpg drawables which populates our `GridView`
         */
        val DRAWABLES = intArrayOf(
                R.drawable.ball, R.drawable.block, R.drawable.ducky, R.drawable.jellies,
                R.drawable.mug, R.drawable.pencil, R.drawable.scissors, R.drawable.woot
        )

        /**
         * This is the list of the resource ids of the `ImageView`s in our layout file's
         * `GridView` (file layout/image_block.xml)
         */
        val IDS = intArrayOf(
                R.id.ball, R.id.block, R.id.ducky, R.id.jellies,
                R.id.mug, R.id.pencil, R.id.scissors, R.id.woot
        )

        /**
         * String name of the `ImageView`s in our layout file, used as the android:transitionName
         * attribute for the respective `ImageView` in our layout file's `GridView` and
         * passed as an extra to the `Intent` that launches both us and `ActivityTransitionDetails`
         * stored under the key KEY_ID ("ViewTransitionValues:id")
         */
        val NAMES = arrayOf(
                "ball", "block", "ducky", "jellies", "mug", "pencil", "scissors", "woot"
        )

        /**
         * Passed a string name of an item returns the R.id.* for the thumbnail in the layout. We
         * call our method `getIndexForKey(id)` to convert the `String id` to the index in the
         * `String[] NAMES` array which is occupied by an equal string, then use that index to
         * access the corresponding entry in the `int[] IDS` array which we return to our caller.
         *
         * @param id String name of item
         * @return Resource R.id.* of that item in layout
         */
        fun getIdForKey(id: String): Int {
            return IDS[getIndexForKey(id)]
        }

        /**
         * Passed a string name of an item returns the R.drawable.* for the image. We call our method
         * `getIndexForKey(id)` to convert the `String id` to the index in the `String[] NAMES`
         * array which is occupied by an equal string, then use that index to access the corresponding
         * entry in the `int[] DRAWABLES` array which we return to our caller.
         *
         * @param id String name of item
         * @return R.drawable.* for the image
         */
        fun getDrawableIdForKey(id: String): Int {
            return DRAWABLES[getIndexForKey(id)]
        }

        /**
         * Searches the array of names of id string and returns the index number for that string.
         * We loop over `int i` for all of the strings in `String[] NAMES` setting `String name`
         * to the current `NAMES[ i ]` then if `name` is equal to our argument `String id`
         * we return `i` to the caller. If none of the strings in `NAMES` match `id` we
         * return 2 to the caller.
         *
         * @param id String name of an item
         * @return Index in the arrays for it (or "2" if not found)
         */
        fun getIndexForKey(id: String): Int {
            for (i in NAMES.indices) {
                val name = NAMES[i]
                if (name == id) {
                    return i
                }
            }
            return 2
        }

        /**
         * Create a random color with maximum alpha and the three RGB colors <=128 intensity.
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
