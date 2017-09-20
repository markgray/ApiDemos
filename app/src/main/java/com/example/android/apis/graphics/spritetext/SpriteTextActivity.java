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

package com.example.android.apis.graphics.spritetext;

import javax.microedition.khronos.opengles.GL;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

/**
 * Shows how to add text to a GLSurfaceView using OpenGL labels. OpenGL labels are implemented by
 * creating a Bitmap, drawing all the labels into the Bitmap, converting the Bitmap into an Alpha
 * texture, and drawing portions of the texture using glDrawTexiOES. The benefits of this approach
 * are that the labels are drawn using the high quality anti-aliased font rasterizer, full character
 * set support, and all the text labels are stored on a single texture, which makes it faster to use.
 * The drawbacks are that you can only have as many labels as will fit onto one texture, and you have
 * to recreate the whole texture if any label text changes. Characters are too small on lollipop,
 * okay on froyo.
 */
public class SpriteTextActivity extends Activity {
    /**
     * Our {@code GLSurfaceView}, created in our {@code onCreate} method.
     */
    private GLSurfaceView mGLSurfaceView;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we initialize our field {@code GLSurfaceView mGLSurfaceView} with a new
     * instance of {@code GLSurfaceView}. Next we set the {@code GLWrapper} of {@code mGLSurfaceView}
     * to an anonymous class which returns a new instance of {@code MatrixTrackingGL} "wrapping" the
     * GL it is passed when its {@code wrap} method is called ({@code MatrixTrackingGL} implements
     * the various GL variants adding code to track changes to the GL matrices, and to retrieve their
     * contents). Then we set the renderer of {@code mGLSurfaceView} to a new instance of
     * {@code SpriteTextRenderer}, and set our content view to {@code mGLSurfaceView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setGLWrapper(new GLSurfaceView.GLWrapper() {
            /**
             * Wraps a gl interface in another gl interface, in our case a new instance of
             * {@code MatrixTrackingGL}.
             *
             * @param gl a GL interface that is to be wrapped.
             * @return a new instance of {@code MatrixTrackingGL} that wraps the input argument.
             */
            @Override
            public GL wrap(GL gl) {
                return new MatrixTrackingGL(gl);
            }
        });
        mGLSurfaceView.setRenderer(new SpriteTextRenderer(this));
        setContentView(mGLSurfaceView);
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. The counterpart to {@link #onResume}. First we call through to our
     * super's implementation of {@code onPause}, then we call the {@code onPause} method of our
     * field {@code GLSurfaceView mGLSurfaceView}.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or {@link #onPause}, for
     * your activity to start interacting with the user. First we call through to our super's
     * implementation of {@code onResume}, then we call the {@code onResume} method of our
     * field {@code GLSurfaceView mGLSurfaceView}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }
}
