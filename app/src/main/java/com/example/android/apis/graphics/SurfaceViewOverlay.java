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

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

//Need the following import to get access to the app resources, since this
//class is in a sub-package.
import com.example.android.apis.R;

/**
 * Demonstration of overlays placed on top of a SurfaceView. Shows how to use a FrameLayout to layer
 * views within it, and how to use View.setVisibility(View.VISIBLE), View.INVISIBLE, and View.GONE to
 * toggle which ones are shown. Good use of a translucent background as well.
 */
public class SurfaceViewOverlay extends Activity {
    /**
     * {@code LinearLayout} which contains our two "Hide Me!" buttons (id R.id.hidecontainer)
     */
    View mVictimContainer;
    /**
     * First "Hide Me!" {@code Button} (id R.id.hideme1)
     */
    View mVictim1;
    /**
     * Second "Hide Me!" {@code Button} (id R.id.hideme2)
     */
    View mVictim2;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.surface_view_overlay.
     * Then we locate {@code GLSurfaceView glSurfaceView} in our layout (id R.id.glsurfaceview), and
     * set its renderer to a new instance of {@code CubeRenderer} (a pair of tumbling cubes). We locate
     * {@code View mVictimContainer} with id R.id.hidecontainer. We locate {@code View mVictim1} with
     * id R.id.hideme1 and set its {@code OnClickListener} to a new instance of {@code HideMeListener}
     * constructed for it, and we locate {@code View mVictim2} with id R.id.hideme2 and set its
     * {@code OnClickListener} to a new instance of {@code HideMeListener} constructed for it.
     * We locate {@code Button visibleButton} id R.id.vis and set its {@code OnClickListener} to
     * {@code OnClickListener mVisibleListener}, {@code Button invisibleButton} id R.id.invis and set
     * its {@code OnclickListener} to {@code OnClickListener mInvisibleListener}, and locate
     * {@code Button goneButton} id R.id.gone and set its {@code OnClickListener} to
     * {@code OnClickListener mGoneListener}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.surface_view_overlay);

        GLSurfaceView glSurfaceView = (GLSurfaceView) findViewById(R.id.glsurfaceview);
        glSurfaceView.setRenderer(new CubeRenderer(false));

        // Find the views whose visibility will change
        mVictimContainer = findViewById(R.id.hidecontainer);
        mVictim1 = findViewById(R.id.hideme1);
        mVictim1.setOnClickListener(new HideMeListener(mVictim1));
        mVictim2 = findViewById(R.id.hideme2);
        mVictim2.setOnClickListener(new HideMeListener(mVictim2));

        // Find our buttons
        Button visibleButton = (Button) findViewById(R.id.vis);
        Button invisibleButton = (Button) findViewById(R.id.invis);
        Button goneButton = (Button) findViewById(R.id.gone);

        // Wire each button to a click listener
        visibleButton.setOnClickListener(mVisibleListener);
        invisibleButton.setOnClickListener(mInvisibleListener);
        goneButton.setOnClickListener(mGoneListener);
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or {@link #onPause}, for
     * your activity to start interacting with the user. We simply call through to our super's
     * implementation of {@code onResume}.
     */
    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. We simply call through to our super's implementation of {@code onPause}.
     */
    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
    }

    /**
     * {@code OnClickListener} which sets the visibility of the {@code View} it is constructed for
     * to INVISIBLE.
     */
    @SuppressWarnings("WeakerAccess")
    class HideMeListener implements OnClickListener {
        /**
         * {@code View} we were constructed to make INVISIBLE when it is clicked.
         */
        final View mTarget;

        /**
         * Our constructor. We simple save our parameter {@code View target} in our field
         * {@code View mTarget}.
         *
         * @param target {@code View} to make invisible if it is clicked
         */
        HideMeListener(View target) {
            mTarget = target;
        }

        /**
         * Called when a view whose {@code OnClickListener} we are has been clicked. We simply set
         * the visibility of our {@code View mTarget} to INVISIBLE.
         *
         * @param v {@code View} that was clicked.
         */
        @Override
        public void onClick(View v) {
            mTarget.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * {@code OnClickListener} for the {@code Button} "Vis", id R.id.vis in our layout. It sets the
     * visibility of {@code View mVictim1}, {@code View mVictim2} and {@code View mVictimContainer}
     * to VISIBLE when the {@code Button} is clicked.
     */
    OnClickListener mVisibleListener = new OnClickListener() {
        /**
         * Called when a view whose {@code OnClickListener} we are has been clicked. When clicked we
         * set the visibility of {@code View mVictim1}, {@code View mVictim2} and {@code View mVictimContainer}
         * to VISIBLE.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            mVictim1.setVisibility(View.VISIBLE);
            mVictim2.setVisibility(View.VISIBLE);
            mVictimContainer.setVisibility(View.VISIBLE);
        }
    };

    /**
     * {@code OnClickListener} for the {@code Button} "Invis", id R.id.invis in our layout. It sets the
     * visibility of {@code View mVictim1}, {@code View mVictim2} and {@code View mVictimContainer}
     * to INVISIBLE when the {@code Button} is clicked.
     */
    OnClickListener mInvisibleListener = new OnClickListener() {
        /**
         * Called when a view whose {@code OnClickListener} we are has been clicked. When clicked we
         * set the visibility of {@code View mVictim1}, {@code View mVictim2} and {@code View mVictimContainer}
         * to INVISIBLE.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            mVictim1.setVisibility(View.INVISIBLE);
            mVictim2.setVisibility(View.INVISIBLE);
            mVictimContainer.setVisibility(View.INVISIBLE);
        }
    };

    /**
     * {@code OnClickListener} for the {@code Button} "Gone", id R.id.gone in our layout. It sets the
     * visibility of {@code View mVictim1}, {@code View mVictim2} and {@code View mVictimContainer}
     * to GONE when the {@code Button} is clicked.
     */
    OnClickListener mGoneListener = new OnClickListener() {
        /**
         * Called when a view whose {@code OnClickListener} we are has been clicked. When clicked we
         * set the visibility of {@code View mVictim1}, {@code View mVictim2} and {@code View mVictimContainer}
         * to GONE.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            mVictim1.setVisibility(View.GONE);
            mVictim2.setVisibility(View.GONE);
            mVictimContainer.setVisibility(View.GONE);
        }
    };
}
