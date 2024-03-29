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

package com.example.android.apis.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Uses Intent.createChooser(Intent, "title") to create a "chooser intent"  from three different
 * mime types ("audio/(any), "image/(any), and "(any)/(any)") of Intent.ACTION_GET_CONTENT Intents
 * based on which button the user clicks, and it then hands the created "chooser Intent" to
 * startActivity(Intent)
 *
 * Note: occurrences of slash star in comments are replace by /(any) to avoid kotlin compiler bug.
 */
@Suppress("UNUSED_PARAMETER")
class Intents : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.intents.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.intents)
    }

    /**
     * Set as the OnClickListener for the R.id.get_music ("GET MUSIC") Button in the layout using
     * the attribute android:onClick="onGetMusic". First we create an Intent intent with the
     * action ACTION_GET_CONTENT, set the type of the Intent to "audio/(any) and finally create an
     * action chooser Intent with the title "Select music" from Intent intent, and start that chooser
     * Activity using the chooser Intent.
     *
     * @param view R.id.get_music ("GET MUSIC") Button which was clicked
     */
    fun onGetMusic(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        startActivity(Intent.createChooser(intent, "Select music"))
    }

    /**
     * Set as the OnClickListener for the R.id.get_image ("GET IMAGE") Button in the layout using
     * the attribute android:onClick="onGetImage". First we create an Intent intent with the
     * action ACTION_GET_CONTENT, set the type of the Intent to "image/(any) and finally create an
     * action chooser Intent with the title "Select image" from Intent intent, and start that chooser
     * Activity using the chooser Intent.
     *
     * @param view R.id.get_image ("GET IMAGE") Button which was clicked
     */
    fun onGetImage(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivity(Intent.createChooser(intent, "Select image"))
    }

    /**
     * Set as the OnClickListener for the R.id.get_stream ("GET STREAM") Button in the layout using
     * the attribute android:onClick="onGetStream". First we create an Intent intent with the
     * action ACTION_GET_CONTENT, set the type of the Intent to "*&#47;*" and finally create an
     * action chooser Intent with the title "Select stream" from Intent intent, and start that chooser
     * Activity using the chooser Intent.
     *
     * @param view R.id.get_stream ("GET STREAM") Button which was clicked
     */
    fun onGetStream(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivity(Intent.createChooser(intent, "Select stream"))
    }
}
