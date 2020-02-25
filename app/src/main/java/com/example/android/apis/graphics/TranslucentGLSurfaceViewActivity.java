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

package com.example.android.apis.graphics;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


/**
 * Wrapper activity demonstrating the use of {@link GLSurfaceView} to display translucent 3D graphics.
 * Sets the background of the GLSurfaceView of a CubeRenderer to Translucent, allowing the user to
 * see the ApiDemos application behind the rotating cube.
 */
public class TranslucentGLSurfaceViewActivity extends AppCompatActivity {

    /**
     * Our {@code GLSurfaceView}.
     */
    private GLSurfaceView mGLSurfaceView;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we initialize our field {@code GLSurfaceView mGLSurfaceView} with a
     * new instance of {@code GLSurfaceView}. We install a config chooser for {@code mGLSurfaceView}
     * which will choose a config with depthSize of 16 and stencilSize of 0, with redSize, greenSize,
     * blueSize and alphaSize 8 bit. We set the renderer of {@code mGLSurfaceView} to a new instance
     * of {@code CubeRenderer}, passing it the flag true so that it will use a translucent background.
     * We fetch an instance of {@code SurfaceHolder} from {@code mGLSurfaceView} which provides access
     * and control over its underlying surface and set its {@code PixelFormat} to TRANSLUCENT. Finally
     * we set our content view to {@code mGLSurfaceView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create our Preview view and set it as the content of our
        // Activity
        mGLSurfaceView = new GLSurfaceView(this);
        // We want an 8888 pixel format because that's required for
        // a translucent window.
        // And we want a depth buffer.
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        // Tell the cube renderer that we want to render a translucent version
        // of the cube:
        mGLSurfaceView.setRenderer(new CubeRenderer(true));
        // Use a surface format with an Alpha channel:
        mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(mGLSurfaceView);
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or {@link #onPause}, for
     * our activity to start interacting with the user. First we call through to our super's
     * implementation of {@code onResume}, then we pass the call on to the {@code onResume} method
     * of our field {@code GLSurfaceView mGLSurfaceView}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of {@code onPause},
     * then we pass the call on to the {@code onPause} method of our field {@code GLSurfaceView mGLSurfaceView}.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }
}

