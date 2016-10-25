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

package com.example.android.apis.app;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import com.example.android.apis.R;

import java.io.IOException;

import android.app.Activity;
import android.app.WallpaperManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

/**
 * <h3>SetWallpaper Activity</h3>
 * <p>
 * <p>This demonstrates the how to write an activity that gets the current system wallpaper,
 * modifies it and sets the modified bitmap as system wallpaper.</p>
 */
public class SetWallpaperActivity extends Activity {
    final static private int[] mColors =
            {Color.BLUE, Color.GREEN, Color.RED, Color.LTGRAY, Color.MAGENTA, Color.CYAN,
                    Color.YELLOW, Color.WHITE};

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.wallpaper_2. Next we set
     * our variable WallpaperManager wallpaperManager to an instance of WallpaperManager associated
     * with our Activity's Context. We use <code>wallpaperManager</code> to retrieve the current
     * system wallpaper to our variable <code>Drawable wallpaperDrawable</code>. We set our variable
     * <code>ImageView imageView</code> to the ImageView in our layout R.id.imageview, enable the
     * drawing cache for imageView, and then set the drawable wallpaperDrawable as the content of
     * imageView. Next we locate the Button "Randomize" (R.id.randomize) and set its OnClickListener
     * to an anonymous class which sets a random color filter for wallpaperDrawable, sets the
     * modified drawable as the new content of imageView and invalidates imageView causing it to
     * redraw itself with the new content. Finally we locate the Button "Set Wallpaper"
     * (R.id.setwallpaper) and set its OnClickListener to an anonymous class which instructs our
     * instance of WallpaperManager wallpaperManager to change the current system wallpaper to the
     * bitmap cached in <code>imageView</code>.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);
        // See res/layout/wallpaper_2.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.wallpaper_2);

        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        final Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        final ImageView imageView = (ImageView) findViewById(R.id.imageview);
        imageView.setDrawingCacheEnabled(true);
        imageView.setImageDrawable(wallpaperDrawable);

        Button randomize = (Button) findViewById(R.id.randomize);
        randomize.setOnClickListener(new OnClickListener() {
            /**
             * Called when the "Randomize" (R.id.randomize) Button is clicked. First we pick a
             * random color from our <code>mColors</code> array of colors and set that color
             * to be the color filter for the drawable <code>wallpaperDrawable</code>,
             *
             * @param view View of Button that was clicked
             */
            @Override
            public void onClick(View view) {
                int mColor = (int) Math.floor(Math.random() * mColors.length);
                wallpaperDrawable.setColorFilter(mColors[mColor], PorterDuff.Mode.MULTIPLY);
                imageView.setImageDrawable(wallpaperDrawable);
                imageView.invalidate();
            }
        });

        Button setWallpaper = (Button) findViewById(R.id.setwallpaper);
        setWallpaper.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    wallpaperManager.setBitmap(imageView.getDrawingCache());
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

