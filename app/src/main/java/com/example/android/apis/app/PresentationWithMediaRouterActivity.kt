/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.annotation.TargetApi
import android.app.Activity
import android.app.MediaRouteActionProvider
import android.app.Presentation
import android.content.Context
import android.content.DialogInterface
import android.media.MediaRouter
import android.media.MediaRouter.RouteInfo
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.example.android.apis.R
import com.example.android.apis.graphics.CubeRenderer


/**
 * Presentation Activity
 *
 * This demonstrates how to create an activity that shows some content
 * on a secondary display using a [Presentation].
 *
 * The activity uses the [MediaRouter] API to automatically detect when
 * a presentation display is available and to allow the user to control the
 * media routes using a menu item.  When a presentation display is available,
 * we stop showing content in the main activity and instead open up a
 * [Presentation] on the preferred presentation display.  When a presentation
 * display is removed, we revert to showing content in the main activity.
 * We also write information about displays and display-related events to
 * the Android log which you can read using **adb logcat**.
 *
 * You can try this out using an HDMI or Wifi display or by using the
 * "Simulate secondary displays" feature in Development Settings to create a few
 * simulated secondary displays.  Each display will appear in the list along with a
 * checkbox to show a presentation on that display.
 *
 * See also the [PresentationActivity] sample which
 * uses the low-level display manager to enumerate displays and to show multiple
 * simultaneous presentations on different displays.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class PresentationWithMediaRouterActivity : Activity() {

    private var mMediaRouter: MediaRouter? = null // MediaRouter we will use control the routing of media channels
    private var mPresentation: DemoPresentation? = null // The presentation to show on the secondary display.
    private var mSurfaceView: GLSurfaceView? = null // GLSurfaceView in our layout for our openGL demo
    private var mInfoTextView: TextView? = null // text view where we will show information about what's happening.
    private var mPaused: Boolean = false // Flag set in onPause to true, set to false in onResume so updateContents

    /**
     * Interface for receiving events about media routing changes. All methods of this interface
     * will be called from the application's main thread. Added in onResume, removed in onPause.
     */
    private val mMediaRouterCallback = object : MediaRouter.SimpleCallback() {

        /**
         * Called when the supplied route becomes selected as the active route
         * for the given route type. We simply call updatePresentation to update
         * the routing for the presentation.
         *
         * @param router the MediaRouter reporting the event
         * @param type Type flag set indicating the routes that have been selected
         * @param info Route that has been selected for the given route types
         */
        override fun onRouteSelected(router: MediaRouter, type: Int, info: RouteInfo) {
            Log.d(TAG, "onRouteSelected: type=$type, info=$info")
            updatePresentation()
        }

        /**
         * Called when the supplied route becomes unselected as the active route
         * for the given route type. We simply call updatePresentation to update
         * the routing for the presentation.
         *
         * @param router the MediaRouter reporting the event
         * @param type Type flag set indicating the routes that have been unselected
         * @param info Route that has been unselected for the given route types
         */
        override fun onRouteUnselected(router: MediaRouter, type: Int, info: RouteInfo) {
            Log.d(TAG, "onRouteUnselected: type=$type, info=$info")
            updatePresentation()
        }

        /**
         * Called when a route's presentation display changes.
         *
         *
         * This method is called whenever the route's presentation display becomes
         * available, is removes or has changes to some of its properties (such as its size).
         *
         * We simply call updatePresentation to update the routing for the presentation.
         *
         * @param router the MediaRouter reporting the event
         * @param info The route whose presentation display changed
         */
        override fun onRoutePresentationDisplayChanged(router: MediaRouter, info: RouteInfo) {
            Log.d(TAG, "onRoutePresentationDisplayChanged: info=$info")
            updatePresentation()
        }
    }

    /**
     * Listens for when presentations are dismissed. Used in updatePresentation()
     */
    private val mOnDismissListener = DialogInterface.OnDismissListener { dialog ->
        /**
         * This method will be invoked when the dialog is dismissed. We check to make sure
         * the the dialog passed us is the current DemoPresentation mPresentation, and if so
         * we set mPresentation to null and call updateContents to show the demo on the main
         * display instead.
         *
         * @param dialog The dialog that was dismissed will be passed into the method.
         */
        if (dialog === mPresentation) {
            Log.i(TAG, "Presentation was dismissed.")
            mPresentation = null
            updateContents()
        }
    }
    // knows to call onPause or onResume for the GLSurfaceView mSurfaceView.

    /**
     * Initialization of the Activity after it is first created.  Must at least
     * call [setContentView()][android.app.Activity.setContentView] to
     * describe what is to be displayed in the screen.
     *
     * First we call through to our super's implementation of onCreate. Then we fetch the handle to
     * the MediaRouter system-level service and save it in our field MediaRouter mMediaRouter. We
     * set our content view to our layout file R.layout.presentation_with_media_router_activity.
     * We locate the GLSurfaceView in our layout (R.id.surface_view) and save it in our field
     * GLSurfaceView mSurfaceView, then set the renderer associated with this view to a new instance
     * of CubeRenderer, which also starts the thread that will call the renderer, which in turn
     * causes the openGL rendering to start. Finally we locate the TextView R.id.info and save it
     * in TextView mInfoTextView for later use.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState)

        // Get the media router service.
        mMediaRouter = getSystemService(Context.MEDIA_ROUTER_SERVICE) as MediaRouter

        // See assets/res/any/layout/presentation_with_media_router_activity.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.presentation_with_media_router_activity)

        // Set up the surface view for visual interest.
        mSurfaceView = findViewById(R.id.surface_view)
        mSurfaceView!!.setRenderer(CubeRenderer(false))

        // Get a text view where we will show information about what's happening.
        mInfoTextView = findViewById(R.id.info)
    }

    /**
     * Called after [.onRestoreInstanceState], [.onRestart], or
     * [.onPause], for your activity to start interacting with the user.
     *
     * First we call through to our super's implementation of onResume, then we add the callback
     * MediaRouter.SimpleCallback mMediaRouterCallback to listen to events relating to
     * ROUTE_TYPE_LIVE_VIDEO to our MediaRouter mMediaRouter. Finally we reset the mPaused flag
     * to false and call updatePresentation to update the routing and the presentation contents.
     */
    override fun onResume() {
        // Be sure to call the super class.
        super.onResume()

        // Listen for changes to media routes.
        mMediaRouter!!.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback)

        // Update the presentation based on the currently selected route.
        mPaused = false
        updatePresentation()
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed.  The counterpart to
     * [.onResume].
     *
     * First we call through to the super's implementation of onPause, then we remove our media
     * routing callback from MediaRouter mMediaRouter, set the mPaused flag to true and call
     * updateContents to pause the rendering by calling onPause on the SurfaceView that is being
     * rendered to.
     */
    override fun onPause() {
        // Be sure to call the super class.
        super.onPause()

        // Stop listening for changes to media routes.
        mMediaRouter!!.removeCallback(mMediaRouterCallback)

        // Pause rendering.
        mPaused = true
        updateContents()
    }

    /**
     * Called when you are no longer visible to the user.  You will next
     * receive either [.onRestart], [.onDestroy], or nothing,
     * depending on later user activity.
     *
     * First we call through to our super's implementation of onStop. Then if we are rendering
     * DemoPresentation mPresentation to a secondary display, we dismiss mPresentation, removing
     * it from the screen and set mPresentation to null.
     */
    override fun onStop() {
        // Be sure to call the super class.
        super.onStop()

        // Dismiss the presentation when the activity is not visible.
        if (mPresentation != null) {
            Log.i(TAG, "Dismiss presentation activity invisible.")
            mPresentation!!.dismiss()
            mPresentation = null
        }
    }

    /**
     * First we call through to our super's implementation of onCreateOptionsMenu. Then we inflate
     * our menu layout into the Menu menu parameter. We locate the menu item R.id.menu_media_route
     * and save it in MenuItem mediaRouteMenuItem, get the ActionProvider for mediaRouteMenuItem
     * (android:actionProviderClass="android.app.MediaRouteActionProvider") and save it in
     * MediaRouteActionProvider mediaRouteActionProvider, then set the types of routes that will
     * be shown in the media route chooser dialog launched by this button to ROUTE_TYPE_LIVE_VIDEO
     * (Route type flag for live video). Finally we return true to show the menu.
     *
     * @param menu The options menu in which we placed our items.
     * @return true to show the menu
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Be sure to call the super class.
        super.onCreateOptionsMenu(menu)

        // Inflate the menu and configure the media router action provider.
        menuInflater.inflate(R.menu.presentation_with_media_router_menu, menu)

        val mediaRouteMenuItem = menu.findItem(R.id.menu_media_route)
        val mediaRouteActionProvider = mediaRouteMenuItem.actionProvider as MediaRouteActionProvider
        mediaRouteActionProvider.setRouteTypes(MediaRouter.ROUTE_TYPE_LIVE_VIDEO)

        // Return true to show the menu.
        return true
    }

    /**
     * Update the routing for the demonstration. First we retrieve the the currently selected route
     * for ROUTE_TYPE_LIVE_VIDEO to MediaRouter.RouteInfo route. Then if the route is not null, we
     * set Display presentationDisplay to the external Display that is being used by the application,
     * otherwise we set presentationDisplay to null. If the display has changed (mPresentation is
     * not null, and the display used by mPresentation is not the same as Display presentationDisplay
     * we dismiss the old mPresentation and set mPresentation to null. Then if mPresentation is null
     * and there is an external display available in presentationDisplay we create a new
     * DemoPresentation mPresentation for presentationDisplay, set the DialogInterface.OnDismissListener
     * of mPresentation to mOnDismissListener, and wrapped in a try block intended to catch
     * WindowManager.InvalidDisplayException we instruct mPresentation to start the presentation and
     * display it on the external display. If mPresentation.show() fails to connect to the Display
     * it will throw WindowManager.InvalidDisplayException and we set mPresentation to null. Finally
     * we call updateContents which will display our rotating cubes either in the main activity or
     * on the external display and will display some text explaining what is happening.
     */
    private fun updatePresentation() {
        // Get the current route and its presentation display.
        val route = mMediaRouter!!.getSelectedRoute(
                MediaRouter.ROUTE_TYPE_LIVE_VIDEO)
        val presentationDisplay = route?.presentationDisplay

        // Dismiss the current presentation if the display has changed.
        if (mPresentation != null && mPresentation!!.display != presentationDisplay) {
            Log.i(TAG, "Dismissing presentation because the current route no longer " + "has a presentation display.")
            mPresentation!!.dismiss()
            mPresentation = null
        }

        // Show a new presentation if needed.
        if (mPresentation == null && presentationDisplay != null) {
            Log.i(TAG, "Showing presentation on display: $presentationDisplay")
            mPresentation = DemoPresentation(this, presentationDisplay)
            mPresentation!!.setOnDismissListener(mOnDismissListener)
            try {
                mPresentation!!.show()
            } catch (ex: WindowManager.InvalidDisplayException) {
                Log.w(TAG, "Couldn't show presentation!  Display was removed in " + "the meantime.", ex)
                mPresentation = null
            }

        }

        // Update the contents playing in this activity.
        updateContents()
    }

    /**
     * Show either the content in the main activity or the content in the presentation along with
     * some descriptive text about what is happening. If mPresentation is not null we are meant to
     * display on an external display, so we set the text in the TextView mInfoTextView to a
     * formatted string: "Now playing on secondary display" with the name of the external display
     * added to it, we set the visibility of the GLSurfaceView mSurfaceView in the main view to
     * invisible and call mSurfaceView's onPause callback. Then if the Activity has been paused
     * (mPaused == true) we call the onPause callback of the SurfaceView being used by
     * DemoPresentation mPresentation, otherwise we call its onResume callback. On the other hand
     * if mPresentation is null we are to display on the main display so we set the text in the
     * TextView mInfoTextView to a formatted string: "Now playing on main display" with the name of
     * the default display added to it, we set the visibility of the GLSurfaceView mSurfaceView in
     * the main view to VISIBLE, and if the Activity has been paused (mPaused == true) we call the
     * onPause callback of the SurfaceView mSurfaceView, otherwise we call its onResume callback.
     */
    private fun updateContents() {
        // Show either the content in the main activity or the content in the presentation
        // along with some descriptive text about what is happening.
        if (mPresentation != null) {
            mInfoTextView!!.text = resources.getString(
                    R.string.presentation_with_media_router_now_playing_remotely,
                    mPresentation!!.display.name)
            mSurfaceView!!.visibility = View.INVISIBLE
            mSurfaceView!!.onPause()
            if (mPaused) {
                mPresentation!!.surfaceView!!.onPause()
            } else {
                mPresentation!!.surfaceView!!.onResume()
            }
        } else {
            mInfoTextView!!.text = resources.getString(
                    R.string.presentation_with_media_router_now_playing_locally,
                    windowManager.defaultDisplay.name)
            mSurfaceView!!.visibility = View.VISIBLE
            if (mPaused) {
                mSurfaceView!!.onPause()
            } else {
                mSurfaceView!!.onResume()
            }
        }
    }

    /**
     * The presentation to show on the secondary display.
     *
     * Note that this display may have different metrics from the display on which
     * the main activity is showing so we must be careful to use the presentation's
     * own [Context] whenever we load resources.
     */
    private class DemoPresentation
    /**
     * Creates a new presentation that is attached to the specified display using the default theme.
     *
     * @param context The context of the application that is showing the presentation. The
     * presentation will create its own context (see getContext()) based on
     * this context and information about the associated display.
     * @param display The display to which the presentation should be attached.
     */
    (context: Context, display: Display) : Presentation(context, display) {
        /**
         * Getter method for our field GLSurfaceView mSurfaceView, used in updateContents().
         *
         * @return contents of our field GLSurfaceView mSurfaceView
         */
        var surfaceView: GLSurfaceView? = null
            private set // The GLSurfaceView in the presentation layout

        /**
         * Similar to [Activity.onCreate], you should initialize your dialog
         * in this method, including calling [.setContentView].
         *
         * We set our content view to our layout file R.layout.presentation_with_media_router_content,
         * locate the GLSurfaceView in that layout (R.id.surface_view) and save it in our field
         * GLSurfaceView mSurfaceView, then set the renderer of mSurfaceView to a new instance of
         * CubeRenderer which also starts the thread that will call the renderer, which in turn
         * causes the rendering to start.
         *
         * @param savedInstanceState always null since onSaveInstanceState is not overridden
         */
        override fun onCreate(savedInstanceState: Bundle) {
            // Be sure to call the super class.
            super.onCreate(savedInstanceState)

            // Get the resources for the context of the presentation.
            // Notice that we are getting the resources from the context of the presentation.

            @Suppress("UNUSED_VARIABLE")
            val r = context.resources

            // Inflate the layout.
            setContentView(R.layout.presentation_with_media_router_content)

            // Set up the surface view for visual interest.
            surfaceView = findViewById(R.id.surface_view)
            surfaceView!!.setRenderer(CubeRenderer(false))
        }
    }

    companion object {
        private const val TAG = "PresentationWMRActivity" // TAG for logging
    }
}

