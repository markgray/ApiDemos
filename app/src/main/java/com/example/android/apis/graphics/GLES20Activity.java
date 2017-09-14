/*
 * Copyright (C) 2009 The Android Open Source Project
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
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

/**
 * This sample shows how to check for OpenGL ES 2.0 support at runtime, and then use either
 * OpenGL ES 1.0 or OpenGL ES 2.0, as appropriate. Since even froyo uses 2.0 this is not all
 * that important it seems. Does show a simple example of using Vertex shaders, and Fragment
 * shaders (Pixel shaders).
 */
public class GLES20Activity extends Activity {

    /**
     * {@code GLSurfaceView} we use for drawing. Its renderer is either {@code GLES20TriangleRenderer}
     * for OpenGL ES 2.0, or {@code TriangleRenderer} for OpenGL ES 1.0.
     */
    private GLSurfaceView mGLSurfaceView;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we initialize our field {@code GLSurfaceView mGLSurfaceView} with a new
     * instance of {@code GLSurfaceView}. We call our method {@code detectOpenGLES20} to determine
     * whether our device supports OpenGL ES 2.0 or above, and if it does we inform the default
     * EGLContextFactory and default EGLConfigChooser to pick EGLContext client version 2, and then
     * we set the renderer of {@code GLSurfaceView mGLSurfaceView} to a new instance of
     * {@code GLES20TriangleRenderer}.
     * <p>
     * If our device does not support OpenGL ES 2.0 or above we set the renderer of
     * {@code GLSurfaceView mGLSurfaceView} to a new instance of {@code TriangleRenderer}.
     * <p>
     * In either case we then set our content view to {@code mGLSurfaceView}.
     *
     * @param savedInstanceState We do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);
        if (detectOpenGLES20()) {
            // Tell the surface view we want to create an OpenGL ES 2.0-compatible
            // context, and set an OpenGL ES 2.0-compatible renderer.
            mGLSurfaceView.setEGLContextClientVersion(2);
            mGLSurfaceView.setRenderer(new GLES20TriangleRenderer(this));
        } else {
            // Set an OpenGL ES 1.x-compatible renderer. In a real application
            // this renderer might approximate the same output as the 2.0 renderer.
            mGLSurfaceView.setRenderer(new TriangleRenderer(this));
        }
        setContentView(mGLSurfaceView);
    }

    /**
     * Checks to see if the device supports OpenGL ES 2.0 or above. First we fetch a handle to the
     * ACTIVITY_SERVICE system level service to {@code ActivityManager am} (the {@code ActivityManager}
     * allows you to interact with the overall activities running in the system). We use {@code am}
     * to get the device configuration attributes into {@code ConfigurationInfo info}. We then return
     * true if the field {@code info.reqGlEsVersion} is greater than or equal to 0x20000 (OpenGL ES 2.0
     * or above is used by the application).
     *
     * @return true if the device supports OpenGL ES 2.0 or above, false otherwise
     */
    private boolean detectOpenGLES20() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info.reqGlEsVersion >= 0x20000);
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or {@link #onPause}, for
     * your activity to start interacting with the user. First we call through to our super's
     * implementation of {@code onResume}, then we pass the call on to the {@code onResume} method
     * of {@code GLSurfaceView mGLSurfaceView}.
     */
    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mGLSurfaceView.onResume();
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. The counterpart to {@link #onResume}. First we call through to our
     * super's implementation of {@code onPause}, then we pass the call on to the {@code onPause}
     * method of {@code GLSurfaceView mGLSurfaceView}.
     */
    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mGLSurfaceView.onPause();
    }
}
