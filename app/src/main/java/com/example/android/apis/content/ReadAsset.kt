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
package com.example.android.apis.content

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

import java.io.IOException

/**
 * Shows how to read a data file contained in the app's apk "assets" directory using [getAssets] to
 * get an `AssetManager` and then how to use that `AssetManager`'s method `open(String filename)`
 * to open the "file" as an `InputStream`.
 */
class ReadAsset : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.read_asset.
     * Wrapped in a try block intended to catch [IOException] and rethrow it as a [RuntimeException]
     * we open an `InputStream` for `val inputStream` by obtaining an `AssetManager` instance for
     * the application's package and using it to `open` the asset "read_asset.txt" using the mode
     * ACCESS_STREAMING (the `AssetManager.open` method provides access to files that have been
     * bundled with an application as assets -- that is, files placed in to the "assets" directory).
     * We set [Int] variable `val size` to an estimate of the number of bytes that can be read (or
     * skipped over) the input stream `inputStream` without blocking by the next invocation of a
     * method for this input stream. We use `size` to allocate a [ByteArray] for `var buffer`, read
     * the entire contents of `inputStream` into `buffer` and then close `inputStream`. We convert
     * `buffer` to [String] variable `val text`, locate the [TextView] in our layout with ID R.id.text
     * to initialize variable `val tv` and set its text to `text`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.read_asset)
        try {
            val inputStream = assets.open("read_asset.txt")

            /**
             * We guarantee that the available method returns the total
             * size of the asset...  of course, this does mean that a single
             * asset can't be more than 2 gigs.
             */
            val size = inputStream.available()

            /**
             * Read the entire asset into a local byte buffer.
             */
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            /**
             * Convert the buffer into a string.
             */
            val text = String(buffer)

            /**
             * Finally stick the string into the text view.
             */
            val tv = findViewById<TextView>(R.id.text)
            tv.text = text
        } catch (e: IOException) {
            /**
             * Should never happen!
             */
            throw RuntimeException(e)
        }
    }
}