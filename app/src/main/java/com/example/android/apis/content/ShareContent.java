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

package com.example.android.apis.content;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import com.example.android.apis.R;

/**
 * Example of sharing content from a private content provider. Uses the <provider>
 * ".content.FileProvider" to pipe the image/jpeg to the app the user selects to handle it.
 * -> fails writing to gmail due to java.io.IOException: write failed: EPIPE (Broken pipe)
 * TODO: fix bug!
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ShareContent extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.share_content.
     * Next we locate the Button R.id.share_image ("Share Image") and set its {@code OnClickListener}
     * to an anonymous class which creates an ACTION_CHOOSER {@code Intent} which is configured to
     * allow the user to choose an app capable of handling an "image/jpeg" data stream to be
     * provided by our {@code ContentProvider} class com.example.android.apis.content.FileProvider
     * when it reads the resource file R.drawable.jellies from our APK. We then launch this
     * {@code Intent}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.share_content);

        // Watch for button clicks.
        findViewById(R.id.share_image).setOnClickListener(new View.OnClickListener() {
            /**
             * First we create an {@code Intent} with action ACTION_SEND (Deliver some data to someone
             * else). Next we add the flag FLAG_GRANT_READ_URI_PERMISSION (the recipient of this Intent
             * will be granted permission to perform read operations on the URI in the Intent's data
             * and any URIs specified in its ClipData). We create {@code Uri.Builder b}, add the scheme
             * "content" to it, and the authority "com.example.android.apis.content.FileProvider".
             * We create a {@code TypedValue tv} (Container for a dynamically typed data value. Primarily
             * used with Resources for holding resource values). We place the raw data associated with
             * the resource ID R.drawable.jellies into {@code tv}. We append to {@code b} the string
             * value of the field {@code tv.assetCookie}, and the string value of {@code tv.string}
             * and then use {@code b} to build {@code Uri uri}. We set the type of {@code Intent intent}
             * to "image/jpeg", then add {@code uri} as an extra using the key EXTRA_STREAM. We next
             * set the {@code ClipData} associated with this {@code Intent intent} to a {@code ClipData}
             * created to hold {@code uri}. Finally we create an ACTION_CHOOSER {@code Intent} from
             * {@code intent} and start that {@code Activity}.
             *
             * @param v View of Button that was clicked
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri.Builder b = new Uri.Builder();
                b.scheme("content");
                b.authority("com.example.android.apis.content.FileProvider");
                TypedValue tv = new TypedValue();
                getResources().getValue(R.drawable.jellies, tv, true);
                b.appendEncodedPath(Integer.toString(tv.assetCookie));
                b.appendEncodedPath(tv.string.toString());
                Uri uri = b.build();
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.setClipData(ClipData.newUri(getContentResolver(), "image", uri));
                startActivity(Intent.createChooser(intent, "Select share target"));
            }
        });
    }
}
