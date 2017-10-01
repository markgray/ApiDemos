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

package com.example.android.apis.graphics;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

/**
 * Shows a rotating triangle using a bitmap made from raw/robot.png as the texture. It uses a
 * GLSurfaceView.Renderer that uses the Android-specific android.opengl.GLESXXX static OpenGL ES
 * APIs. The static APIs expose more of the OpenGL ES features than the javax.microedition.khronos.opengles APIs,
 * and also provide a programming model that is closer to the C OpenGL ES APIs, which may make it easier
 * to reuse code and documentation written for the C OpenGL ES APIs. It uses android:theme="@style/ThemeCurrentDialog"
 * in the AndroidManifest.xml so the SurfaceView is shrunk to appear in a dialog size window.
 */
public class TriangleActivity extends Activity {

    /**
     * Our {@code GLSurfaceView} we use for drawing.
     */
    private GLSurfaceView mGLView;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Next we initialize our field {@code GLSurfaceView mGLView} with a new instance
     * of {@code GLSurfaceView}, and set its renderer to a new instance of {@code StaticTriangleRenderer}.
     * Finally we set our content view to {@code mGLView}. Note that since our entry in the AndroidManifest
     * uses android:theme="@style/ThemeCurrentDialog" we appear in a dialog sized window above the main
     * activity which spawned us.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLView = new GLSurfaceView(this);
        mGLView.setRenderer(new StaticTriangleRenderer(this));
        /*ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.raw.robot);
        setContentView(imageView);*/
        setContentView(mGLView);
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of {@code onPause}
     * then we pass the call on to the {@code onPause} method of our field {@code GLSurfaceView mGLView}.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or {@link #onPause}, for
     * your activity to start interacting with the user. First we call through to our super's
     * implementation of {@code onResume} then we pass the call on to the {@code onResume} method of
     * our field {@code GLSurfaceView mGLView}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }
}
