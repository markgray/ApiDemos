/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.example.android.apis.app

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.app.WallpaperManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import java.io.IOException
import kotlin.math.floor

/**
 * SetWallpaper Activity
 *
 * This demonstrates the how to write an activity that gets the current system wallpaper,
 * modifies it and sets the modified bitmap as system wallpaper.
 */
class SetWallpaperActivity : AppCompatActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.wallpaper_2. Next we set
     * our [WallpaperManager] variable `val wallpaperManager` to an instance of [WallpaperManager]
     * associated with our Activity's Context. We use `wallpaperManager` to retrieve the current
     * system wallpaper to our variable `Drawable` variable `val wallpaperDrawable`. We set our
     * [ImageView] variable `val imageView` to the [ImageView] in our layout file with the ID
     * R.id.imageview, enable the drawing cache for `imageView`, and then set the drawable
     * `wallpaperDrawable` as the content of `imageView`. Next we locate the [Button] "Randomize"
     * (R.id.randomize) to initialize our [Button] variable `val randomize` and set its `OnClickListener`
     * to a lambda which sets a random color filter for `wallpaperDrawable`, sets the modified drawable
     * as the new content of `imageView` and invalidates `imageView` causing it to redraw itself with
     * the new content. Finally we locate the [Button] "Set Wallpaper" (R.id.setwallpaper) to initialie
     * our variable `val setWallpaper` and set its `OnClickListener`] to a lambda which instructs our
     * instance of [WallpaperManager] `wallpaperManager` to change the current system wallpaper to the
     * bitmap cached in `imageView`.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState)
        // See res/layout/wallpaper_2.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.wallpaper_2)

        val wallpaperManager = WallpaperManager.getInstance(this)
        val wallpaperDrawable = wallpaperManager.drawable
        val imageView = findViewById<View>(R.id.imageview) as ImageView
        @Suppress("DEPRECATION")
        imageView.isDrawingCacheEnabled = true
        imageView.setImageDrawable(wallpaperDrawable)

        val randomize = findViewById<View>(R.id.randomize) as Button
        /**
         * Called when the "Randomize" (R.id.randomize) Button is clicked. First we pick a
         * random color from our **mColors** array of colors and set that color
         * to be the color filter for the drawable **wallpaperDrawable**, then we
         * set the drawable wallpaperDrawable as the content of the ImageView imageView, and
         * finally we invalidate imageView causing its onDraw method to be called sometime in
         * the future.
         *
         * Parameter: View of Button that was clicked
         */
        randomize.setOnClickListener {
            val mColor = floor(Math.random() * mColors.size).toInt()
            @Suppress("DEPRECATION")
            wallpaperDrawable.setColorFilter(mColors[mColor], PorterDuff.Mode.MULTIPLY)
            imageView.setImageDrawable(wallpaperDrawable)
            imageView.invalidate()
        }

        val setWallpaper = findViewById<View>(R.id.setwallpaper) as Button
        /**
         * Called when the Button "Set Wallpaper" (R.id.setwallpaper) is clicked. Wrapped in a
         * try intended to catch IOException we instruct WallpaperManager wallpaperManager to
         * change the current system wallpaper to the bitmap we get from our imageView by
         * calling getDrawingCache()
         *
         * Parameter View of the Button that was clicked
         */
        setWallpaper.setOnClickListener {
            try {
                @Suppress("DEPRECATION")
                wallpaperManager.setBitmap(imageView.drawingCache)
                finish()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Our static constants.
     */
    companion object {
        /**
         * Array of colors to be used as the random filter applied to our wallpaper.
         */
        private val mColors = intArrayOf(Color.BLUE, Color.GREEN, Color.RED,
                Color.LTGRAY, Color.MAGENTA, Color.CYAN, Color.YELLOW, Color.WHITE)
    }
}

