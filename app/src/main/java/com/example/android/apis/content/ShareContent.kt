/*
 * Copyright (c) 2015, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.apis.content

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Example of sharing content from a private content provider. Uses the `provider`
 * ".content.FileProvider" to pipe the image/jpeg to the app that the user selects to handle it.
 * RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
 */
class ShareContent : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.share_content.
     * Next we locate the `Button` R.id.share_image ("Share Image") and set its `OnClickListener`
     * to an anonymous class which creates an ACTION_CHOOSER `Intent` which is configured to
     * allow the user to choose an app capable of handling an "image/jpeg" data stream to be
     * provided by our `ContentProvider` class com.example.android.apis.content.FileProvider
     * when it reads the resource file R.drawable.jellies from our APK. We then launch this
     * `Intent`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.share_content)
        // Watch for button clicks.
        /**
         * First we create an [Intent] with action ACTION_SEND (Deliver some data to someone else).
         * Next we add the flag FLAG_GRANT_READ_URI_PERMISSION (the recipient of this [Intent]
         * will be granted permission to perform read operations on the URI in the Intent's data
         * and any URIs specified in its [ClipData]). We create [Uri.Builder] `val b`, add the
         * scheme "content" to it, and the authority "com.example.android.apis.content.FileProvider".
         * We create a [TypedValue] `val tv` (Container for a dynamically typed data value. Primarily
         * used with Resources for holding resource values). We place the raw data associated with
         * the resource ID R.drawable.jellies into `tv`. We append to `b` the string value of the
         * field `tv.assetCookie`, and the string value of `tv.string` and then use `b` to build
         * [Uri] `val uri`. We set the type of [Intent] `intent` to "image/jpeg", then add `uri`
         * as an extra using the key EXTRA_STREAM. We next set the [ClipData] associated with this
         * [Intent] `intent` to a [ClipData] created to hold `uri`. Finally we create an ACTION_CHOOSER
         * [Intent] from `intent` and start that `Activity`.
         *
         * Parameter: View of Button that was clicked
         */
        findViewById<View>(R.id.share_image).setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val b = Uri.Builder()
            b.scheme("content")
            b.authority("com.example.android.apis.content.FileProvider")
            val tv = TypedValue()
            resources.getValue(R.drawable.jellies, tv, true)
            b.appendEncodedPath(tv.assetCookie.toString())
            b.appendEncodedPath(tv.string.toString())
            val uri = b.build()
            intent.type = "image/jpeg"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.clipData = ClipData.newUri(contentResolver, "image", uri)
            startActivity(Intent.createChooser(intent, "Select share target"))
        }
    }
}