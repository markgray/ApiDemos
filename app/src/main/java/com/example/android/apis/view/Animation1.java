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

package com.example.android.apis.view;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import com.example.android.apis.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Shows use of a translate animation defined in xml to "shake" a TextView. It uses an
 * android:interpolator also defined in xml which consists of a cycleInterpolator whose
 * android:cycles="7".
 */
public class Animation1 extends Activity implements View.OnClickListener {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.animation_1. Then
     * we locate the view with ID R.id.login to set {@code View loginButton} and set its
     * {@code OnClickListener} to this.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animation_1);

        View loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(this);
    }

    /**
     * Called when the view with ID R.id.login has been clicked. We load the animation contained in
     * the resource file R.anim.shake to set {@code Animation shake}. Then we locate the view with
     * ID R.id.pw and have it start running {@code Animation shake}. R.anim.shake contains an
     * {@code <translate>} element with a fromXDelta of 0, a toXDelta of 10, a duration of 1000, and
     * uses the interpolator defined in anim/cycle_7.xml, which contains a {@code <cycleInterpolator>}
     * with android:cycles="7".
     *
     * @param v The view that was clicked.
     */
    public void onClick(View v) {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        findViewById(R.id.pw).startAnimation(shake);
    }

}
