/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.content.ComponentName
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Front-end for launching [LocalSampleInstrumentation] example
 * instrumentation class.
 * TODO: Un-comment the android:name="android.intent.category.SAMPLE_CODE" in AndroidManifest and test
 */
class LocalSample : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.local_sample)

        // Watch for button clicks.
        val button = findViewById<View>(R.id.go) as Button
        button.setOnClickListener(mGoListener)
    }

    private val mGoListener = View.OnClickListener {
        startInstrumentation(ComponentName(this@LocalSample,
                LocalSampleInstrumentation::class.java), null, null)
    }
}