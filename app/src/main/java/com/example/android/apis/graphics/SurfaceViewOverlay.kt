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
package com.example.android.apis.graphics

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

/**
 * Need the following import to get access to the app resources, since this class is in a sub-package.
 */
import com.example.android.apis.R

/**
 * Demonstration of overlays placed on top of a SurfaceView. Shows how to use a FrameLayout to layer
 * views within it, and how to use View.setVisibility(View.VISIBLE), View.INVISIBLE, and View.GONE to
 * toggle which ones are shown. Good use of a translucent background as well.
 */
@Suppress("MemberVisibilityCanBePrivate")
class SurfaceViewOverlay : AppCompatActivity() {
    /**
     * [LinearLayout] which contains our two "Hide Me!" buttons (id R.id.hidecontainer)
     */
    var mVictimContainer: View? = null
    /**
     * First "Hide Me!" [Button] (id R.id.hideme1)
     */
    var mVictim1: View? = null
    /**
     * Second "Hide Me!" [Button] (id R.id.hideme2)
     */
    var mVictim2: View? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.surface_view_overlay.
     * Then we locate `GLSurfaceView glSurfaceView` in our layout (id R.id.glsurfaceview), and
     * set its renderer to a new instance of `CubeRenderer` (a pair of tumbling cubes). We locate
     * `View mVictimContainer` with id R.id.hidecontainer. We locate `View mVictim1` with
     * id R.id.hideme1 and set its `OnClickListener` to a new instance of `HideMeListener`
     * constructed for it, and we locate `View mVictim2` with id R.id.hideme2 and set its
     * `OnClickListener` to a new instance of `HideMeListener` constructed for it.
     * We locate `Button visibleButton` id R.id.vis and set its `OnClickListener` to
     * `OnClickListener mVisibleListener`, `Button invisibleButton` id R.id.invis and set
     * its `OnclickListener` to `OnClickListener mInvisibleListener`, and locate
     * `Button goneButton` id R.id.gone and set its `OnClickListener` to
     * `OnClickListener mGoneListener`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.surface_view_overlay)
        val glSurfaceView = findViewById<GLSurfaceView>(R.id.glsurfaceview)
        glSurfaceView.setRenderer(CubeRenderer(false))
        // Find the views whose visibility will change
        mVictimContainer = findViewById(R.id.hidecontainer)
        mVictim1 = findViewById(R.id.hideme1)
        mVictim1!!.setOnClickListener(HideMeListener(mVictim1))
        mVictim2 = findViewById(R.id.hideme2)
        mVictim2!!.setOnClickListener(HideMeListener(mVictim2))
        // Find our buttons
        val visibleButton = findViewById<Button>(R.id.vis)
        val invisibleButton = findViewById<Button>(R.id.invis)
        val goneButton = findViewById<Button>(R.id.gone)
        // Wire each button to a click listener
        visibleButton.setOnClickListener(mVisibleListener)
        invisibleButton.setOnClickListener(mInvisibleListener)
        goneButton.setOnClickListener(mGoneListener)
    }

    /**
     * Called after [.onRestoreInstanceState], [.onRestart], or [.onPause], for
     * your activity to start interacting with the user. We simply call through to our super's
     * implementation of `onResume`.
     */
    @Suppress("RedundantOverride")
    override fun onResume() { // Ideally a game should implement onResume() and onPause()
// to take appropriate action when the activity looses focus
        super.onResume()
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. We simply call through to our super's implementation of `onPause`.
     */
    @Suppress("RedundantOverride")
    override fun onPause() { // Ideally a game should implement onResume() and onPause()
// to take appropriate action when the activity looses focus
        super.onPause()
    }

    /**
     * `OnClickListener` which sets the visibility of the `View` it is constructed for
     * to INVISIBLE.
     */
    internal inner class HideMeListener
    /**
     * Our constructor. We simple save our parameter `View target` in our field
     * `View mTarget`.
     *
     *  target `View` to make invisible if it is clicked
     */
    (
            /**
             * `View` we were constructed to make INVISIBLE when it is clicked.
             */
            val mTarget: View?) : View.OnClickListener {

        /**
         * Called when a view whose `OnClickListener` we are has been clicked. We simply set
         * the visibility of our `View mTarget` to INVISIBLE.
         *
         * @param v `View` that was clicked.
         */
        override fun onClick(v: View) {
            mTarget!!.visibility = View.INVISIBLE
        }

    }

    /**
     * `OnClickListener` for the `Button` "Vis", id R.id.vis in our layout. It sets the
     * visibility of `View mVictim1`, `View mVictim2` and `View mVictimContainer`
     * to VISIBLE when the `Button` is clicked.
     */
    /**
     * Called when a view whose `OnClickListener` we are has been clicked. When clicked we
     * set the visibility of `View mVictim1`, `View mVictim2` and `View mVictimContainer`
     * to VISIBLE.
     *
     *  v The view that was clicked.
     */
    var mVisibleListener: View.OnClickListener = View.OnClickListener{
        mVictim1!!.visibility = View.VISIBLE
        mVictim2!!.visibility = View.VISIBLE
        mVictimContainer!!.visibility = View.VISIBLE
    }

    /**
     * `OnClickListener` for the `Button` "Invis", id R.id.invis in our layout. It sets the
     * visibility of `View mVictim1`, `View mVictim2` and `View mVictimContainer`
     * to INVISIBLE when the `Button` is clicked.
     */
    /**
     * Called when a view whose `OnClickListener` we are has been clicked. When clicked we
     * set the visibility of `View mVictim1`, `View mVictim2` and `View mVictimContainer`
     * to INVISIBLE.
     *
     *  v The view that was clicked.
     */
    var mInvisibleListener: View.OnClickListener = View.OnClickListener{
        mVictim1!!.visibility = View.INVISIBLE
        mVictim2!!.visibility = View.INVISIBLE
        mVictimContainer!!.visibility = View.INVISIBLE
    }
    /**
     * `OnClickListener` for the `Button` "Gone", id R.id.gone in our layout. It sets the
     * visibility of `View mVictim1`, `View mVictim2` and `View mVictimContainer`
     * to GONE when the `Button` is clicked.
     */
    /**
     * Called when a view whose `OnClickListener` we are has been clicked. When clicked we
     * set the visibility of `View mVictim1`, `View mVictim2` and `View mVictimContainer`
     * to GONE.
     *
     * v The view that was clicked.
     */
    var mGoneListener: View.OnClickListener = View.OnClickListener {
        mVictim1!!.visibility = View.GONE
        mVictim2!!.visibility = View.GONE
        mVictimContainer!!.visibility = View.GONE
    }
}