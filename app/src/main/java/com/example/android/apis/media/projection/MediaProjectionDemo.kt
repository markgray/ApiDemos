/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.example.android.apis.media.projection

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import com.example.android.apis.media.projection.MediaProjectionDemo.Companion.RESOLUTIONS

/**
 * Shows how to use a ProjectionManager.createScreenCaptureIntent to capture screen content to a
 * VirtualDisplay which is created using MediaProjection.createVirtualDisplay to display to a
 * SurfaceView.
 * RequiresApi(Build.VERSION_CODES.LOLLIPOP)
 */
class MediaProjectionDemo : AppCompatActivity() {
    /**
     * Screen density expressed as dots-per-inch. May be either DENSITY_LOW, DENSITY_MEDIUM, or
     * DENSITY_HIGH. Retrieved from the display metrics of the default screen in our [onCreate]
     * method.
     */
    private var mScreenDensity = 0

    /**
     * [MediaProjectionManager] that we use to create an intent to do a screen capture and to
     * retrieve the screen capture token granting applications the ability to capture screen contents
     * to our [MediaProjection] field [mMediaProjection] from the intent data that is received by
     * our method [onActivityResult] after the activity launched by our screen capture intent
     * finishes its task.
     */
    private var mProjectionManager: MediaProjectionManager? = null

    /**
     * Width of the virtual display we display our screen capture in.
     */
    private var mDisplayWidth = 0

    /**
     * Height of the virtual display we display our screen capture in.
     */
    private var mDisplayHeight = 0

    /**
     * Flag to indicate that we are currently "sharing" the screen to our virtual display.
     */
    private var mScreenSharing = false

    /**
     * [MediaProjection] token granting applications the ability to capture screen contents,
     * it is retrieved from the intent data passed to our [onActivityResult] method from the
     * activity started for result with a screen capture intent.
     */
    private var mMediaProjection: MediaProjection? = null

    /**
     * [VirtualDisplay] used to display our screen capture.
     */
    private var mVirtualDisplay: VirtualDisplay? = null

    /**
     * The [Surface] that provides direct access to the surface object underlying our [SurfaceView]
     * field [mSurfaceView]
     */
    private var mSurface: Surface? = null

    /**
     * [SurfaceView] with id R.id.surface in our layout file, we use it to display our virtual
     * display.
     */
    private var mSurfaceView: SurfaceView? = null

    /**
     * [ToggleButton] with id R.id.screen_sharing_toggle in our layout file, its on click callback
     * is set with the attribute android:onClick="onToggleScreenShare". [onToggleScreenShare]
     * toggles screen sharing, calling [shareScreen] if the new state is "isChecked", or
     * [stopScreenSharing] if the new state is not checked.
     */
    private var mToggle: ToggleButton? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.media_projection. We
     * create a new instance of [DisplayMetrics] for variable `val metrics`, then retrieve the
     * window manager for showing custom windows, fetch the Display upon which this WindowManager
     * instance will create new windows, and get the display metrics that describe the size and
     * density of this display into [DisplayMetrics] variable `metrics`. We then initialize our
     * [Int] field [mScreenDensity] with the screen density field in `metrics`, expressed as
     * dots-per-inch (may be either DENSITY_LOW, DENSITY_MEDIUM, or DENSITY_HIGH).
     *
     * We initialize our [SurfaceView] field [mSurfaceView] by locating the [SurfaceView] in our
     * layout file with ID R.id.surface. We fetch the [SurfaceHolder] providing access and
     * control of the underlying surface of [mSurfaceView], then set our [Surface] field [mSurface]
     * to its [Surface]. Next we initialize our [MediaProjectionManager] field [mProjectionManager]
     * with the handle to the system-level service MEDIA_PROJECTION_SERVICE (used for managing media
     * projection sessions).
     *
     * We create `ArrayAdapter<Resolution>` variable `val arrayAdapter` from our `List<Resolution>`
     * list [RESOLUTIONS] using android.R.layout.simple_list_item_1 as the layout file containing a
     * `TextView` to use when instantiating views. We locate the [Spinner] with ID R.id.spinner in
     * our layout file in order to set [Spinner] variable `val s`, set its adapter to `arrayAdapter`,
     * set its `OnItemSelectedListener` to a new instance of our class [ResolutionSelector], and set
     * its selection to 0.
     *
     * Finally we locate the [ToggleButton] with ID R.id.screen_sharing_toggle in our layout
     * file in order to initialize our [ToggleButton] field [mToggle], and call its method
     * `setSaveEnabled(false)` to disable state saving.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.media_projection)
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(metrics)
        mScreenDensity = metrics.densityDpi
        mSurfaceView = findViewById(R.id.surface)
        mSurface = mSurfaceView!!.holder.surface
        mProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val arrayAdapter = ArrayAdapter(
            /* context = */ this,
            /* resource = */ android.R.layout.simple_list_item_1,
            /* objects = */ RESOLUTIONS
        )
        val s = findViewById<Spinner>(R.id.spinner)
        s.adapter = arrayAdapter
        s.onItemSelectedListener = ResolutionSelector()
        s.setSelection(0)
        mToggle = findViewById(R.id.screen_sharing_toggle)
        mToggle!!.isSaveEnabled = false
    }

    /**
     * Called when you are no longer visible to the user. We call our method [stopScreenSharing]
     * to release the virtual display and destroy its underlying surface. Finally we call our super's
     * implementation of `onStop`.
     */
    override fun onStop() {
        stopScreenSharing()
        super.onStop()
    }

    /**
     * Perform any final cleanup before an activity is destroyed. First we call through to our
     * super's implementation of `onDestroy`, then if our [MediaProjection] field [mMediaProjection]
     * is not null, we stop the projection, and set [mMediaProjection] to null.
     */
    public override fun onDestroy() {
        super.onDestroy()
        if (mMediaProjection != null) {
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
    }

    /**
     * Called when an activity you launched exits, giving you the [requestCode] you started it with,
     * the [resultCode] it returned, and any additional data from it. First we make sure that the
     * [requestCode] returned is PERMISSION_CODE, and if not we log the problem and return having
     * done nothing. If the [resultCode] is not RESULT_OK, we toast the message "User denied screen
     * sharing permission" and return having done nothing.
     *
     * If everything is correct, we initialize our [MediaProjection] field [mMediaProjection] to
     * the [MediaProjection] obtained from the successful screen capture request contained in the
     * returned [Intent] parameter [data]. We register a new instance of our [MediaProjectionCallback]
     * class as the callback for [MediaProjection] field [mMediaProjection], and finally initialize
     * our [VirtualDisplay] field [mVirtualDisplay] with the instance of [VirtualDisplay] returned
     * by our method [createVirtualDisplay].
     *
     * @param requestCode The integer request code originally supplied to [startActivityForResult],
     * allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through its `setResult`.
     * @param data An [Intent], which can return result data to the caller (various data can be
     * attached as [Intent] "extras").
     */
    @Deprecated("Deprecated in Java")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PERMISSION_CODE) {
            Log.e(TAG, "Unknown request code: $requestCode")
            return
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(
                this,
                "User denied screen sharing permission", Toast.LENGTH_SHORT
            ).show()
            return
        }
        mMediaProjection = mProjectionManager!!.getMediaProjection(resultCode, data!!)
        mMediaProjection!!.registerCallback(MediaProjectionCallback(), null)
        mVirtualDisplay = createVirtualDisplay()
    }

    /**
     * Set as the `OnClickListener` of the [ToggleButton] with ID R.id.screen_sharing_toggle
     * in our layout file using the attribute android:onClick="onToggleScreenShare". If the new
     * state of the [View] parameter [view] is "checked" we call our method [shareScreen], otherwise
     * we call our method [stopScreenSharing].
     *
     * @param view View (`ToggleButton`) which has been toggled
     */
    fun onToggleScreenShare(view: View) {
        if ((view as ToggleButton).isChecked) {
            shareScreen()
        } else {
            stopScreenSharing()
        }
    }

    /**
     * Starts sharing the screen. First we set our [Boolean] flag field [mScreenSharing] to true,
     * then if our [Surface] field [mSurface] is null we return without doing anything more. If our
     * [MediaProjection] field [mMediaProjection] is null we start an activity for its result using
     * the intent to start screen capture created by our [MediaProjectionManager] field
     * [mProjectionManager] and using PERMISSION_CODE as the request code, and return. Otherwise
     * we set our [VirtualDisplay] field [mVirtualDisplay] to the [VirtualDisplay] returned by our
     * method [createVirtualDisplay].
     */
    private fun shareScreen() {
        mScreenSharing = true
        if (mSurface == null) {
            return
        }
        if (mMediaProjection == null) {
            @Suppress("DEPRECATION")
            startActivityForResult(
                mProjectionManager!!.createScreenCaptureIntent(),
                PERMISSION_CODE
            )
            return
        }
        mVirtualDisplay = createVirtualDisplay()
    }

    /**
     * Stops screen sharing. If [ToggleButton] field [mToggle] is "checked", we set it to unchecked,
     * then we set our [Boolean] flag field [mScreenSharing] to false. If our [VirtualDisplay] field
     * [mVirtualDisplay] is not null, we release the virtual display and destroy its underlying
     * surface, and set [mVirtualDisplay] to null.
     */
    private fun stopScreenSharing() {
        if (mToggle!!.isChecked) {
            mToggle!!.isChecked = false
        }
        mScreenSharing = false
        if (mVirtualDisplay != null) {
            mVirtualDisplay!!.release()
            mVirtualDisplay = null
        }
    }

    /**
     * Creates and returns a [VirtualDisplay] that continuously displays the screen capture to
     * [Surface] field [mSurface]. We return the [VirtualDisplay] returned from the method
     * `createVirtualDisplay` of our [MediaProjection] field  [mMediaProjection]. The parameters
     * passed to its `createVirtualDisplay` method are:
     *
     *  * name String: The name of the virtual display: "ScreenSharingDemo"
     *
     *  * width int: The width of the virtual display in pixels: our field [mDisplayWidth]
     *
     *  * height int: The height of the virtual display in pixels: our field [mDisplayHeight]
     *
     *  * dpi int: The density of the virtual display in dpi: our filed [mScreenDensity]
     *
     *  * flags int: A combination of virtual display flags: VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
     *  (an auto-mirroring virtual display, continuously capturing and displaying the screen)
     *
     *  * surface [Surface]: The surface to which the content of the virtual display should be
     *  rendered: our [Surface] field [mSurface]
     *
     *  * callback `VirtualDisplay.Callback`: Callback to call when the virtual display's state
     *  changes: null, so no callback.
     *
     *  * handler `Handler`: The `Handler` on which the callback should be invoked: null so the
     *  callback should be invoked on the calling thread's main Looper (if we had a callback).
     *
     * @return a [VirtualDisplay] drawing the screen capture to [Surface] field [mSurface].
     */
    private fun createVirtualDisplay(): VirtualDisplay {
        return mMediaProjection!!.createVirtualDisplay(
            "ScreenSharingDemo",
            mDisplayWidth,
            mDisplayHeight,
            mScreenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mSurface,
            null /*Callbacks*/,
            null /*Handler*/
        )!!
    }

    /**
     * Re-sizes our [VirtualDisplay] field [mVirtualDisplay] to be [mDisplayWidth] by [mDisplayHeight]
     * with density [mScreenDensity] if [mVirtualDisplay] is not null. Called only from the
     * `surfaceChanged` method of our class [SurfaceCallbacks] which is UNUSED, so this is unused
     * as well.
     */
    private fun resizeVirtualDisplay() {
        if (mVirtualDisplay == null) {
            return
        }
        mVirtualDisplay!!.resize(mDisplayWidth, mDisplayHeight, mScreenDensity)
    }

    /**
     * [OnItemSelectedListener] of the [Spinner] with ID R.id.spinner, it allows the user to select
     * a [Resolution] from the list `List<Resolution>` field [RESOLUTIONS].
     */
    private inner class ResolutionSelector : OnItemSelectedListener {
        /**
         * Callback method to be invoked when a new item in this view has been selected. First we
         * retrieve the item that has been selected to [Resolution] variable `val r`. We fetch the
         * layout parameters of [SurfaceView] field [mSurfaceView] (the view our virtual display is
         * drawing to) into `ViewGroup.LayoutParams` variable `val lp`. We fetch our packages
         * resources current configuration in order to check the current orientation. If the
         * orientation is ORIENTATION_LANDSCAPE we set [mDisplayHeight] to `r.y` and [mDisplayWidth]
         * to `r.x`, otherwise we set [mDisplayHeight] to `r.x` and [mDisplayWidth] to `r.y`. We set
         * the `height` field of `lp` to [mDisplayHeight] and the `width` field to [mDisplayWidth],
         * and finally set the layout parameters of [mSurfaceView] to the modified `lp`.
         *
         * @param parent The [AdapterView] where the selection happened
         * @param v      The [View] within the [AdapterView] that was clicked
         * @param pos    The position of the view in the adapter
         * @param id     The row id of the item that is selected
         */
        override fun onItemSelected(parent: AdapterView<*>, v: View?, pos: Int, id: Long) {
            val r = parent.getItemAtPosition(pos) as Resolution
            val lp = mSurfaceView!!.layoutParams
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mDisplayHeight = r.y
                mDisplayWidth = r.x
            } else {
                mDisplayHeight = r.x
                mDisplayWidth = r.y
            }
            lp.height = mDisplayHeight
            lp.width = mDisplayWidth
            mSurfaceView!!.layoutParams = lp
        }

        /**
         * Callback method to be invoked when the selection disappears from this view. We ignore it.
         *
         * @param parent The AdapterView that now contains no selected item.
         */
        override fun onNothingSelected(parent: AdapterView<*>?) { /* Ignore */
        }
    }

    /**
     * Callback for the projection session.
     */
    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        /**
         * Called when the [MediaProjection] session is no longer valid. We set [mMediaProjection]
         * to null and call our method [stopScreenSharing] to release the virtual display if it
         * exists.
         */
        override fun onStop() {
            mMediaProjection = null
            stopScreenSharing()
        }
    }

    /**
     * We implement this interface to receive information about changes to the surface, but do not use
     */
    @Suppress("unused")
    private inner class SurfaceCallbacks : SurfaceHolder.Callback {
        /**
         * This is called immediately after any structural changes (format or size) have been made
         * to the surface. You should at this point update the imagery in the surface. This method
         * is always called at least once, after [surfaceCreated].
         *
         * We store the new [width] parameter in our [mDisplayWidth] field, the new [height]
         * parameter in our [mDisplayHeight] field and call our method [resizeVirtualDisplay] to
         * resize our virtual display.
         *
         * @param holder The [SurfaceHolder] whose surface has changed.
         * @param format The new `PixelFormat` of the surface.
         * @param width  The new width of the surface.
         * @param height The new height of the surface.
         */
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            mDisplayWidth = width
            mDisplayHeight = height
            resizeVirtualDisplay()
        }

        /**
         * This is called immediately after the surface is first created. We initialize our [Surface]
         * field [mSurface] with direct access to the surface object of [SurfaceHolder] parameter
         * [holder]. Then if our [Boolean] flag field [mScreenSharing] is true, we call our method
         * [shareScreen] to start sharing the screen.
         *
         * @param holder The [SurfaceHolder] whose [Surface] is being created.
         */
        override fun surfaceCreated(holder: SurfaceHolder) {
            mSurface = holder.surface
            if (mScreenSharing) {
                shareScreen()
            }
        }

        /**
         * This is called immediately before a surface is being destroyed. If our [Boolean] flag
         * field [mScreenSharing] is not true we call our method [stopScreenSharing] to stop sharing
         * the screen.
         *
         * @param holder The [SurfaceHolder] whose [Surface] is being destroyed.
         */
        override fun surfaceDestroyed(holder: SurfaceHolder) {
            if (!mScreenSharing) {
                stopScreenSharing()
            }
        }
    }

    /**
     * Class used to hold each of the available screen resolutions that the user can choose using the
     * [Spinner] with ID R.id.spinner.
     */
    private class Resolution
    /**
     * Our constructor, simply initializes our fields with our parameters.
     *
     * @param x x dimension of the resolution
     * @param y y dimension of the resolution
     */
        (
        /**
         * x dimension of the resolution
         */
        var x: Int,
        /**
         * y dimension of the resolution
         */
        var y: Int
    ) {

        /**
         * Returns a string containing a concise, human-readable description of this object. We
         * simply return the string representation of our two fields separated with the string "x".
         *
         * @return a printable representation of this object.
         */
        override fun toString(): String {
            return x.toString() + "x" + y
        }

    }

    companion object {
        /**
         * TAG used for logging.
         */
        private const val TAG = "MediaProjectionDemo"

        /**
         * Request code used when starting the intent created by `createScreenCaptureIntent` (for
         * result). It is returned in the `requestCode` parameter when our `onActivityResult`
         * method is called.
         */
        private const val PERMISSION_CODE = 1

        /**
         * List of `Resolution` objects which are used to populate the spinner R.id.spinner, and
         * which the user can select to set the resolution of the virtual display.
         */
        private val RESOLUTIONS: List<Resolution> = object : ArrayList<Resolution>() {
            init {
                add(Resolution(640, 360))
                add(Resolution(960, 540))
                add(Resolution(1366, 768))
                add(Resolution(1600, 900))
            }
        }
    }
}