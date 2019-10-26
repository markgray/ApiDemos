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

package com.example.android.apis.app

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Minimal demo of Picture in Picture mode
 */
@RequiresApi(Build.VERSION_CODES.O)
class PictureInPicture : AppCompatActivity() {

    /**
     * Button with id R.id.enter_pip, its `OnClickListener` calls the method
     * `enterPictureInPictureMode` to enter Picture in Picture mode.
     *
     * Parameter: v `View` that was clicked.
     */

    private var mEnterPip: Button? = null

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file R.layout.picture_in_picture. We initialize our
     * [Button] field ` mEnterPip` by finding the view with id R.id.enter_pip then set its
     * `OnClickListener` to an anonymous class whose `onClick` override calls the method
     * `enterPictureInPictureMode` to enter picture-in-picture mode.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.picture_in_picture)

        mEnterPip = findViewById(R.id.enter_pip)
        mEnterPip!!.setOnClickListener {
            val pictureInPictureParams: PictureInPictureParams =
                    PictureInPictureParams.Builder().build()
            enterPictureInPictureMode(pictureInPictureParams)
        }
    }

    /**
     * Called as part of the activity lifecycle when an activity is about to go
     * into the background as the result of user choice. We just call the method
     * `enterPictureInPictureMode` to enter picture-in-picture mode.
     */
    override fun onUserLeaveHint() {
        val pictureInPictureParams: PictureInPictureParams =
                PictureInPictureParams.Builder().build()
        enterPictureInPictureMode(pictureInPictureParams)
    }
}
