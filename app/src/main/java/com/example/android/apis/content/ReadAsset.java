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

package com.example.android.apis.content;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Shows how to read a data file contained in the app's apk "assets" directory using getAssets() to
 * get an AssetManager and AssetManager.open(String filename) to open the "file" as an InputStream.
 */
public class ReadAsset extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.read_asset.
     *
     * Wrapped in a try block intended to catch IOException we open {@code InputStream is} by
     * obtaining an AssetManager instance for the application's package and using it to {@code open}
     * the asset "read_asset.txt" using ACCESS_STREAMING mode (the {@code AssetManager.open} method
     * provides access to files that have been bundled with an application as assets -- that is,
     * files placed in to the "assets" directory). We set {@code int size} to an estimate of the
     * number of bytes that can be read (or skipped over) the input stream {@code is} without
     * blocking by the next invocation of a method for this input stream. We use {@code size} to
     * allocate {@code byte[] buffer}, read the entire contents of {@code is} into {@code buffer}
     * and then close {@code is}. We convert {@code buffer} to {@code String text}, locate the
     * {@code TextView tv} in our layout with ID R.id.text, and set its text to {@code text}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_asset);

        try {
            InputStream is = getAssets().open("read_asset.txt");
            
            // We guarantee that the available method returns the total
            // size of the asset...  of course, this does mean that a single
            // asset can't be more than 2 gigs.
            int size = is.available();
            
            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            is.close();
            
            // Convert the buffer into a string.
            String text = new String(buffer);
            
            // Finally stick the string into the text view.
            TextView tv = (TextView)findViewById(R.id.text);
            tv.setText(text);
        } catch (IOException e) {
            // Should never happen!
            throw new RuntimeException(e);
        }
    }
}

