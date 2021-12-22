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
@file:Suppress("DEPRECATION")
// TODO: replace use of Camera with android.hardware.camera2
package com.example.android.apis.graphics

import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Context
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager

import androidx.appcompat.app.AppCompatActivity

import com.example.android.apis.R

import java.io.IOException
import kotlin.math.abs

// ----------------------------------------------------------------------
/**
 * Shows how to create a SurfaceView for the deprecated Camera api (use android.hardware.camera2)
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
class CameraPreview : AppCompatActivity() {
    /**
     * Our instance of our class [Preview]
     */
    private var mPreview: Preview? = null

    /**
     * Our instance of the [Camera] class
     */
    var mCamera: Camera? = null

    /**
     * The number of physical cameras available on this device
     */
    var numberOfCameras = 0

    /**
     * Physical camera number that we currently have open
     */
    var cameraCurrentlyLocked = 0

    /**
     * The first rear facing camera
     */
    var defaultCameraId = 0

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we request the FEATURE_NO_TITLE feature for our window, and add
     * FLAG_FULLSCREEN to its flags so that we have full use of the screen (apart from the navigation
     * bar at the bottom of the screen). Next we initialize our [Preview] field [mPreview] with
     * a new instance of [Preview] and set it as our content view. We initialize our [Int] field
     * [numberOfCameras] with the number of physical cameras available on this device. We create an
     * instance of [CameraInfo] for `val cameraInfo` and loop through the number of cameras of
     * the device retrieving the camera information for each camera in turn to `cameraInfo`, and if
     * the camera is a CAMERA_FACING_BACK type we save its id to our [Int] field [defaultCameraId].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState], so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**
         * Hide the window title.
         */
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        /**
         * Create a RelativeLayout container that will hold a SurfaceView,
         * and set it as the content of our activity.
         */
        mPreview = Preview(this)
        setContentView(mPreview)
        /**
         * Find the total number of cameras available
         */
        numberOfCameras = Camera.getNumberOfCameras()
        /**
         * Find the ID of the default camera
         */
        val cameraInfo = CameraInfo()
        for (i in 0 until numberOfCameras) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                defaultCameraId = i
            }
        }
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for your activity to start
     * interacting with the user. This is a good place to begin animations, open exclusive-access
     * devices (such as the camera), etc.
     *
     * First we call through to our super's implementation of `onResume` then we set our [Camera]
     * field [mCamera] to the first rear facing (default) camera, set our [Int] field
     * [cameraCurrentlyLocked] to the value of our [Int] field [defaultCameraId], and instruct our
     * [Preview] field [mPreview] to set the camera it is displaying to [mCamera].
     */
    override fun onResume() {
        super.onResume()
        /**
         * Open the default i.e. the first rear facing camera.
         */
        mCamera = Camera.open()
        cameraCurrentlyLocked = defaultCameraId
        mPreview!!.setCamera(mCamera)
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. The counterpart to [onResume].
     *
     * First we call through to our super's implementation of `onPause`, then if we have a [Camera]
     * in our field [mCamera] in use we instruct our [Preview] field [mPreview] to display a *null*
     * image in place of the camera preview it has been showing and to set its own [Camera] field
     * [Preview.mCamera] to null (Do not be confused by the fact that we use the same name for
     * our [Camera] field [mCamera]!) Then we disconnect and release the [Camera] in [mCamera]'s
     * resources and set the field to null.
     */
    override fun onPause() {
        super.onPause()
        /**
         * Because the Camera object is a shared resource, it's very
         * important to release it when the activity is paused.
         */
        if (mCamera != null) {
            mPreview!!.setCamera(null)
            mCamera!!.release()
            mCamera = null
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu. You should place your menu
     * items in to the [Menu] parameter [menu].
     *
     * We fetch a [MenuInflater] for `val inflater` for this context, and use it to inflate our menu
     * layout file R.menu.camera_menu into our [Menu] parameter [menu], and we return *true* to the
     * caller so that our menu will be displayed.
     *
     * @param menu The options [Menu] in which we place our items.
     * @return You must return *true* for the menu to be displayed.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        /**
         * Inflate our menu which can gather user input for switching camera
         */
        val inflater = menuInflater
        inflater.inflate(R.menu.camera_menu, menu)
        return true
    }

    /**
     * This hook is called whenever an item in our options menu is selected. We switch based on the
     * identifier for the [MenuItem] parameter [item], and if it is our R.id.switch_cam menu item we
     * first check to see if the device has only one camera and if so we display an [AlertDialog]
     * stating "Device has only one camera!" and return true to consume the menu selection. If we do
     * have more than one camera, we first check to see if we are already connected to a camera via
     * [Camera] field [mCamera] and if so we instruct it to stop its preview, instruct our [Preview]
     * field [mPreview] to set its camera displayed to *null*, release [mCamera] and set it to null.
     * We now open the next camera in numerical order modulo [numberOfCameras] to set [mCamera],
     * store this camera number in [cameraCurrentlyLocked], and instruct [Preview] field [mPreview]
     * to switch to this camera and start its preview display. Finally we return *true* to consume
     * the menu selection here.
     *
     * @param item The menu item that was selected.
     * @return [Boolean] Return *false* to allow normal menu processing to proceed,
     * *true* to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /**
         * Handle item selection
         */
        return when (item.itemId) {
            R.id.switch_cam -> {
                /**
                 * check for availability of multiple cameras
                 */
                if (numberOfCameras == 1) {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(this.getString(R.string.camera_alert))
                        .setNeutralButton("Close", null)
                    val alert = builder.create()
                    alert.show()
                    return true
                }
                /**
                 * OK, we have multiple cameras. Release this camera -> cameraCurrentlyLocked
                 */
                if (mCamera != null) {
                    mCamera!!.stopPreview()
                    mPreview!!.setCamera(null)
                    mCamera!!.release()
                    mCamera = null
                }
                /**
                 * Acquire the next camera and request Preview to reconfigure parameters.
                 */
                mCamera = Camera.open((cameraCurrentlyLocked + 1) % numberOfCameras)
                cameraCurrentlyLocked = (cameraCurrentlyLocked + 1) % numberOfCameras
                mPreview!!.switchCamera(mCamera)
                /**
                 * Start the preview
                 */
                @Suppress("DEPRECATION")
                mCamera!!.startPreview()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * A simple wrapper around a [Camera] and a [SurfaceView] that renders a centered preview of the
     * [Camera] to the surface. We need to center the [SurfaceView] because not all devices have
     * cameras that support preview sizes at the same aspect ratio as the device's display.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    class Preview(context: Context?) : ViewGroup(context), SurfaceHolder.Callback {
        /**
         * [SurfaceView] we create in our constructor, and the only view in our [ViewGroup].
         */
        var mSurfaceView: SurfaceView = SurfaceView(context)

        /**
         * [SurfaceHolder] of our [SurfaceView] field [mSurfaceView], we use it to set the preview
         * display of our [Camera] field [mCamera].
         */
        var mHolder: SurfaceHolder

        /**
         * Optimal preview size chosen from amongst the supported preview sizes of the camera
         * `List<Size> mSupportedPreviewSizes` field by our method [getOptimalPreviewSize] based
         * on the sizes passed to our [onMeasure] override.
         */
        @Suppress("DEPRECATION")
        var mPreviewSize: Camera.Size? = null

        /**
         * List of supported preview sizes for our current [Camera] field [mCamera] as returned by
         * the call to the method `getSupportedPreviewSizes` on the [Camera.Parameters] for the
         * current camera returned by the method [Camera.getParameters]
         */
        @Suppress("DEPRECATION")
        var mSupportedPreviewSizes: List<Camera.Size>? = null

        /**
         * Current [Camera] instance whose preview we are displaying
         */
        @Suppress("DEPRECATION")
        var mCamera: Camera? = null

        /**
         * Sets the [Camera] instance we are associated with. First we set our [Camera] field
         * [mCamera] to our [Camera] parameter [camera], then if it is not *null*, we fetch the
         * list of preview sizes supported by [mCamera] to our field `List<Size> mSupportedPreviewSizes`
         * and request that a layout pass of the view tree be scheduled.
         *
         * @param camera [Camera] we are to use
         */
        @Suppress("DEPRECATION")
        fun setCamera(camera: Camera?) {
            mCamera = camera
            if (mCamera != null) {
                mSupportedPreviewSizes = mCamera!!.parameters.supportedPreviewSizes
                requestLayout()
            }
        }

        /**
         * Called when the user selects the "Switch Camera" menu item. First we call our [setCamera]
         * method to set our [mCamera] field to our [Camera] parameter [camera]. Then wrapped in a
         * *try* block intended to catch and log [IOException] we call the `setPreviewDisplay` method
         * of [camera] to have it set the `Surface` to be used for display to our [SurfaceHolder]
         * field [mHolder]. After exiting the *try* block we initialize `val parameters` to the
         * [Camera.Parameters] of [camera], then call the `setPreviewSize` method of `parameters`
         * to set the dimensions for preview pictures to the width and height of our [Camera.Size]
         * field [mPreviewSize]. We then call the [requestLayout] method to call a layout pass of
         * our view tree. Finally we set the [Camera.Parameters] of [camera] to `parameters`.
         *
         * @param camera new [Camera] to show the preview of
         */
        @Suppress("DEPRECATION")
        fun switchCamera(camera: Camera?) {
            setCamera(camera)
            try {
                camera!!.setPreviewDisplay(mHolder)
            } catch (exception: IOException) {
                Log.e(TAG, "IOException caused by setPreviewDisplay()", exception)
            }
            val parameters = camera!!.parameters
            parameters.setPreviewSize(mPreviewSize!!.width, mPreviewSize!!.height)
            requestLayout()
            camera.parameters = parameters
        }

        /**
         * Measure the view and its content to determine the measured width and the measured height.
         * This method is invoked by [measure] and should be overridden by subclasses to provide
         * accurate and efficient measurement of their contents.
         *
         * First we set [Int] variable `val width` to the size resolved from our view's suggested
         * minimum width as constrained by [widthMeasureSpec], and `val height` to the size resolved
         * from our view's suggested minimum height as constrained by [heightMeasureSpec]. Then we
         * call [setMeasuredDimension] with `(width,height)` to store the measured width and measured
         * height. Finally, if our list of the camera's supported preview sizes in our field
         * `List<Size> mSupportedPreviewSizes` is not *null*, we call our method [getOptimalPreviewSize]
         * to determine the optimal preview size for our width and height and set our [Camera.Size]
         * field [mPreviewSize] to it.
         *
         * @param widthMeasureSpec  horizontal space requirements as imposed by the parent.
         * The requirements are encoded with [android.view.View.MeasureSpec].
         * @param heightMeasureSpec vertical space requirements as imposed by the parent.
         * The requirements are encoded with [android.view.View.MeasureSpec].
         */
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            /**
             * We purposely disregard child measurements because we act as a
             * wrapper to a SurfaceView that centers the camera preview instead
             * of stretching it.
             */
            val width = View.resolveSize(suggestedMinimumWidth, widthMeasureSpec)
            val height = View.resolveSize(suggestedMinimumHeight, heightMeasureSpec)
            setMeasuredDimension(width, height)
            if (mSupportedPreviewSizes != null) {
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height)
            }
        }

        /**
         * Called from layout when this view should assign a size and position to each of its children.
         * We do nothing unless the [changed] flag passed us is *true*, and we have at least 1 child
         * [View]. If we do need to do something, we first fetch a reference to our 1 and only child
         * to [View] variable `val child`, calculate the `val width`, and `val height` allowed us by
         * our parent using the right, left, bottom and top positions of our view relative to our
         * parent. We set `var previewWidth` to this `width`, and `var previewHeight` to this `height`,
         * then if [Camera.Size] field [mPreviewSize] is not null we reset `previewWidth` to the field
         * `mPreviewSize.width` and `previewHeight` to the field `mPreviewSize.height` instead. Then
         * we calculate the coordinates necessary to center our child [SurfaceView] within our View
         * and instruct our `child` to layout itself using these coordinates.
         *
         * @param changed This is a new size or position for this view
         * @param l       Left position, relative to parent
         * @param t       Top position, relative to parent
         * @param r       Right position, relative to parent
         * @param b       Bottom position, relative to parent
         */
        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            if (changed && childCount > 0) {
                val child = getChildAt(0)
                val width = r - l
                val height = b - t
                var previewWidth = width
                var previewHeight = height
                @Suppress("DEPRECATION")
                if (mPreviewSize != null) {
                    previewWidth = mPreviewSize!!.width
                    previewHeight = mPreviewSize!!.height
                }
                /**
                 * Center the child SurfaceView within the parent.
                 */
                if (width * previewHeight > height * previewWidth) {
                    val scaledChildWidth = previewWidth * height / previewHeight
                    child.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height)
                } else {
                    val scaledChildHeight = previewHeight * width / previewWidth
                    child.layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2)
                }
            }
        }

        /**
         * This is called immediately after the surface is first created. Wrapped in a try block
         * intended* to catch and log [IOException], we test to make sure our [Camera] field [mCamera]
         * is not *null* first and then instruct it to set the `Surface` it will use for live preview
         * to our [SurfaceHolder] parameter [holder].
         *
         * @param holder The [SurfaceHolder] whose surface is being created.
         */
        override fun surfaceCreated(holder: SurfaceHolder) {
            /**
             * The Surface has been created, acquire the camera and tell it where to draw.
             */
            try {
                if (mCamera != null) {
                    @Suppress("DEPRECATION")
                    mCamera!!.setPreviewDisplay(holder)
                }
            } catch (exception: IOException) {
                Log.e(TAG, "IOException caused by setPreviewDisplay()", exception)
            }
        }

        /**
         * This is called immediately before a surface is being destroyed. If our [Camera] field
         * [mCamera] is not *null* we instruct it to stop capturing and drawing preview frames to
         * the surface, and reset the camera for a future call to `startPreview()`.
         *
         * @param holder The [SurfaceHolder] whose surface is being destroyed
         */
        override fun surfaceDestroyed(holder: SurfaceHolder) {
            /**
             * Surface will be destroyed when we return, so stop the preview.
             */
            if (mCamera != null) {
                @Suppress("DEPRECATION")
                mCamera!!.stopPreview()
            }
        }

        /**
         * Calculates and returns the size from the list of supported preview sizes for this camera
         * which will make optimal use of the screen space we have available. If our parameter [sizes]
         * is *null*, we return *null* having done nothing. Otherwise we define ASPECT_TOLERANCE to
         * be 0.1, this is used to search the supported preview sizes for a size whose aspect ratio
         * is closest to that defined by our parameters [w] and [h]. We calculate this target aspect
         * ratio `val targetRatio` to be the [Double] of `w/h`. We initialize [Camera.Size] variable
         * `var optimalSize` to *null*, [Double] variable `var minDiff` to be the largest positive
         * finite value of type [Double].
         *
         * Now in order to find a [Camera.Size] in `List<Size> sizes` which most closely matches our
         * window's aspect ratio, we loop through all the `size`'s in `List<Size> sizes`, calculate
         * its [Double] `val ratio` aspect ratio by dividing its `size.width` field by its
         * `size.height` field, and if the absolute difference between `ratio` and `targetRatio` is
         * greater than ASPECT_TOLERANCE we skip it. Otherwise if the absolute difference between
         * the `size.height` and our [h] parmeter is less than the current value of `minDiff` we set
         * `optimalSize` to `size` and update `minDiff` to be the absolute difference between the
         * `size.height` and our [h] parameter.
         *
         * If this loop failed to find a best match for the aspect ratio (`optimalSize` is still
         * *null*) we search `List<Size> sizes` for a [Camera.Size] whose field `height` is closest
         * to [h] and set `optimalSize` to it.
         *
         * In either case we return `optimalSize` to the caller.
         *
         * @param sizes list of supported preview sizes of our `Camera`
         * @param w     width of the window we will use to display the preview in
         * @param h     height of the window we will use to display the preview in
         * @return best [Camera.Size] from the list of supported preview sizes passed us.
         */
        @Suppress("DEPRECATION")
        private fun getOptimalPreviewSize(sizes: List<Camera.Size>?, w: Int, h: Int): Camera.Size? {
            if (sizes == null) return null
            val targetRatio = w.toDouble() / h
            var optimalSize: Camera.Size? = null
            var minDiff = Double.MAX_VALUE
            /**
             * Try to find an size match aspect ratio and size
             */
            for (size in sizes) {
                val ratio = size.width.toDouble() / size.height
                if (abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
                if (abs(size.height - h) < minDiff) {
                    optimalSize = size
                    minDiff = abs(size.height - h).toDouble()
                }
            }
            /**
             * Cannot find the one match the aspect ratio, ignore the requirement
             */
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE
                for (size in sizes) {
                    if (abs(size.height - h) < minDiff) {
                        optimalSize = size
                        minDiff = abs(size.height - h).toDouble()
                    }
                }
            }
            return optimalSize
        }

        /**
         * This is called immediately after any structural changes (format or size) have been made
         * to the surface. You should at this point update the imagery in the surface.
         *
         * First we fetch the current settings for the [Camera] service we have open in [Camera]
         * field [mCamera] to [Camera.Parameters] variable `val parameters`, we set the preview size
         * of `parameters` to the optimal size we want as contained in [Camera.Size] field
         * [mPreviewSize], request that a layout pass be scheduled, set the parameters of [mCamera]
         * to the modified `parameters`, and instruct [mCamera] to start capturing and drawing
         * preview frames to the screen.
         *
         * @param holder The [SurfaceHolder] whose surface has changed.
         * @param format The new PixelFormat of the surface.
         * @param w      The new width of the surface.
         * @param h      The new height of the surface.
         */
        @Suppress("DEPRECATION")
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            /**
             * Now that the size is known, set up the camera parameters and begin the preview.
             */
            val parameters = mCamera!!.parameters
            parameters.setPreviewSize(mPreviewSize!!.width, mPreviewSize!!.height)
            requestLayout()
            mCamera!!.parameters = parameters
            mCamera!!.startPreview()
        }

        companion object {
            const val ASPECT_TOLERANCE = 0.1

            /**
             * TAG for logging
             */
            private const val TAG = "Preview"
        }

        /**
         * Initializes our `ViewGroup`. First we call through to our super's constructor. Next we
         * initialize our field `SurfaceView mSurfaceView` with an instance of `SurfaceView`
         * and add it to our `ViewGroup`. We initialize our field `SurfaceHolder mHolder` by
         * fetching the `SurfaceHolder` providing access and control over `mSurfaceView`'s
         * underlying surface, add "this" as the `SurfaceHolder.Callback` for `mHolder` (our
         * overrides of `surfaceCreated`, `surfaceDestroyed`, and `surfaceChanged` will
         * be called). Finally we set the type of `mHolder` to SURFACE_TYPE_PUSH_BUFFERS (This is
         * ignored, and is deprecated as of API 11!)
         *
         * Parameter: `Context` of application "this" when called from `onCreate`
         */
        init {
            addView(mSurfaceView)
            /**
             * Install *this* as a SurfaceHolder.Callback so we get notified
             * when the underlying surface is created and destroyed.
             */
            mHolder = mSurfaceView.holder
            mHolder.addCallback(this)
            @Suppress("DEPRECATION")
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }
    }
}
