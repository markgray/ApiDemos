/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.example.android.apis.accessibility

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This is the entry activity for a sample that demonstrates how to implement an
 * `AccessibilityService`, namely the [ClockBackService].
 */
class ClockBackActivity : AppCompatActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.accessibility_service.
     * We initialize our variable `ImageButton button` by finding the view with the id R.id.button
     * and set its `OnClickListener` to lambda which starts the system settings activity.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.accessibility_service)

        // Add a shortcut to the accessibility settings.
        val button = findViewById<ImageButton>(R.id.button)

        /*
         * Called when the button with id R.id.button is clicked, we just launch the settings
         * activity.
         *
         * v View that was clicked
         */
        button.setOnClickListener {
            startActivity(sSettingsIntent)
        }
    }

    companion object {

        /**
         * An intent for launching the system settings.
         */
        private val sSettingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }
}
