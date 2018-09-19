/*
 * Copyright (C) 2018 The Android Open Source Project
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

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.android.apis.R;

/**
 * Minimal demo of Picture in Picture mode
 */
@SuppressWarnings("FieldCanBeLocal")
public class PictureInPicture extends Activity {

    /**
     * Button with id R.id.enter_pip, its {@code OnClickListener} calls the method
     * {@code enterPictureInPictureMode} to enter Picture in Picture mode.
     */
    private Button mEnterPip;

    /**
     * Called when the activity is starting. First we call our super's implementation of {@code onCreate},
     * then we set our content view to our layout file R.layout.picture_in_picture. We initialize our
     * field {@code Button mEnterPip} by finding the view with id R.id.enter_pip then set its
     * {@code OnClickListener} to an anonymous class whose {@code onClick} override calls the method
     * {@code enterPictureInPictureMode} to enter picture-in-picture mode.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_in_picture);

        mEnterPip = findViewById(R.id.enter_pip);
        mEnterPip.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when the button with id R.id.enter_pip is clicked, we just call the method
             * {@code enterPictureInPictureMode} to enter picture-in-picture mode.
             *
             * @param v {@code View} that was clicked.
             */
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                enterPictureInPictureMode();
            }
        });
    }

    /**
     * Called as part of the activity lifecycle when an activity is about to go
     * into the background as the result of user choice. We just call the method
     * {@code enterPictureInPictureMode} to enter picture-in-picture mode.
     */
    @TargetApi(Build.VERSION_CODES.N)
    @Override
    protected void onUserLeaveHint() {
        enterPictureInPictureMode();
    }
}
