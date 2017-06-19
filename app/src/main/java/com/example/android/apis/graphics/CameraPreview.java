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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R;

// ----------------------------------------------------------------------

/**
 * Shows how to create a SurfaceView for the deprecated Camera api (use android.hardware.camera2)
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class CameraPreview extends Activity {
    /**
     * Our instance of our class {@code Preview}
     */
    private Preview mPreview;
    /**
     * Our instance of the {@code Camera} class
     */
    @SuppressWarnings("deprecation")
    Camera mCamera;
    /**
     * The number of physical cameras available on this device
     */
    int numberOfCameras;
    /**
     * Physical camera number that we currently have open
     */
    int cameraCurrentlyLocked;

    /**
     * The first rear facing camera
     */
    int defaultCameraId;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we request the FEATURE_NO_TITLE feature for our window, and add
     * FLAG_FULLSCREEN to its flags so that we have full use of the screen (apart from the navigation
     * bar at the bottom of the screen). Next we initialize our field {@code Preview mPreview} with
     * a new instance of {@code Preview} and set it as our content view. We initialize our field
     * {@code int numberOfCameras} with the number of physical cameras available on this device. We
     * create an instance of {@code CameraInfo cameraInfo} and loop through the number of cameras of
     * the device retrieving the camera information for each camera in turn to {@code cameraInfo},
     * and if the camera is a CAMERA_FACING_BACK type we save its id to our field
     * {@code int defaultCameraId}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState}, so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Create a RelativeLayout container that will hold a SurfaceView,
        // and set it as the content of our activity.
        mPreview = new Preview(this);
        setContentView(mPreview);

        // Find the total number of cameras available
        //noinspection deprecation
        numberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the default camera
        @SuppressWarnings("deprecation")
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            //noinspection deprecation
            Camera.getCameraInfo(i, cameraInfo);
            //noinspection deprecation
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                defaultCameraId = i;
            }
        }
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or
     * {@link #onPause}, for your activity to start interacting with the user.
     * This is a good place to begin animations, open exclusive-access devices
     * (such as the camera), etc.
     * <p>
     * First we call through to our super's implementation of {@code onResume} then we set our field
     * {@code Camera mCamera} to the first rear facing (default) camera, set our field
     * {@code int cameraCurrentlyLocked} to the value of our field {@code int defaultCameraId},
     * and instruct our {@code Preview mPreview} to set the camera it is displaying to {@code mCamera}.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Open the default i.e. the first rear facing camera.
        //noinspection deprecation
        mCamera = Camera.open();
        cameraCurrentlyLocked = defaultCameraId;
        mPreview.setCamera(mCamera);
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed.  The counterpart to
     * {@link #onResume}.
     * <p>
     * First we call through to our super's implementation of {@code onPause}, then if we have a
     * {@code Camera mCamera} in use we instruct our {@code Preview mPreview} to display a null
     * image in place of the camera preview it has been showing and to set its own field
     * {@code Camera mCamera} to null (Do not be confused by the fact that we use the same name for
     * our field {@code Camera mCamera}!) Then we disconnect and release the {@code Camera mCamera}
     * resources and set the field to null.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null) {
            mPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     * <p>
     * We fetch a {@code MenuInflater inflater} for this context, and use it to inflate our menu
     * layout file R.menu.camera_menu into our parameter {@code Menu menu}, and we return true to
     * the caller so that our menu will be displayed.
     *
     * @param menu The options menu in which we place our items.
     * @return You must return true for the menu to be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate our menu which can gather user input for switching camera
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.camera_menu, menu);
        return true;
    }

    /**
     * This hook is called whenever an item in our options menu is selected. We switch based on the
     * identifier for the {@code MenuItem item}, and if it is our R.id.switch_cam menu item we first
     * check to see if the device has only one camera and if so we display an {@code AlertDialog}
     * stating "Device has only one camera!" and return true to consume the menu selection. If we do
     * have more than one camera, we first check to see if we are already connected to a camera via
     * {@code Camera mCamera} and if so we instruct it to stop its preview, instruct our
     * {@code Preview mPreview} to set its camera displayed to null, release {@code mCamera} and set
     * it to null. We now open the next camera in numerical order modulo {@code numberOfCameras} to
     * set {@code mCamera}, store this camera number in {@code cameraCurrentlyLocked}, and instruct
     * {@code Preview mPreview} to switch to this camera and start its preview display. Finally we
     * return true to consume the menu selection here.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.switch_cam:
                // check for availability of multiple cameras
                if (numberOfCameras == 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(this.getString(R.string.camera_alert)).setNeutralButton("Close", null);
                    AlertDialog alert = builder.create();
                    alert.show();
                    return true;
                }

                // OK, we have multiple cameras.
                // Release this camera -> cameraCurrentlyLocked
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mPreview.setCamera(null);
                    mCamera.release();
                    mCamera = null;
                }

                // Acquire the next camera and request Preview to reconfigure
                // parameters.
                //noinspection deprecation
                mCamera = Camera.open((cameraCurrentlyLocked + 1) % numberOfCameras);
                cameraCurrentlyLocked = (cameraCurrentlyLocked + 1) % numberOfCameras;
                mPreview.switchCamera(mCamera);

                // Start the preview
                mCamera.startPreview();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

// ----------------------------------------------------------------------

/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
 * to the surface. We need to center the SurfaceView because not all devices have cameras that
 * support preview sizes at the same aspect ratio as the device's display.
 */
@SuppressWarnings("deprecation")
class Preview extends ViewGroup implements SurfaceHolder.Callback {
    /**
     * TAG for logging
     */
    private final String TAG = "Preview";

    /**
     * {@code SurfaceView} we create in our constructor, and the only view in our {@code ViewGroup}.
     */
    SurfaceView mSurfaceView;
    /**
     * {@code SurfaceHolder} of our {@code SurfaceView mSurfaceView}, we use it to set the preview
     * display of our {@code Camera mCamera}
     */
    SurfaceHolder mHolder;
    /**
     * Optimal preview size chosen from amongst the supported preview sizes of the camera
     * {@code List<Size> mSupportedPreviewSizes} by our method {@code getOptimalPreviewSize}
     * based on the sizes passed to our {@code onMeasure} override.
     */
    Size mPreviewSize;
    /**
     * List of supported preview sizes for our current {@code Camera mCamera} as returned by the
     * call to the method {@code getSupportedPreviewSizes} on the {@code Camera.Parameters} for
     * the current camera returned by the method {@code getParameters}
     */
    List<Size> mSupportedPreviewSizes;
    /**
     * Current {@code Camera} instance whose preview we are displaying
     */
    Camera mCamera;

    /**
     * Initializes our {@code ViewGroup}. First we call through to our super's constructor. Next we
     * initialize our field {@code SurfaceView mSurfaceView} with an instance of {@code SurfaceView}
     * and add it to our {@code ViewGroup}. We initialize our field {@code SurfaceHolder mHolder} by
     * fetching the {@code SurfaceHolder} providing access and control over {@code mSurfaceView}'s
     * underlying surface, add "this" as the {@code SurfaceHolder.Callback} for {@code mHolder} (our
     * overrides of {@code surfaceCreated}, {@code surfaceDestroyed}, and {@code surfaceChanged} will
     * be called). Finally we set the type of {@code mHolder} to SURFACE_TYPE_PUSH_BUFFERS (This is
     * ignored, and is deprecated as of API 11!)
     *
     * @param context {@code Context} of application "this" when called from {@code onCreate}
     */
    Preview(Context context) {
        super(context);

        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * Sets the {@code Camera} instance we are associated with. First we set our field
     * {@code Camera mCamera} to our parameter {@code Camera camera}, then if it is not null,
     * we fetch the list of preview sizes supported by {@code mCamera} to our field
     * {@code List<Size> mSupportedPreviewSizes} and request that a layout pass of the view tree
     * be scheduled.
     *
     * @param camera {@code Camera} we are to use
     */
    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();
        }
    }

    /**
     * Called when the user selects the "Switch Camera" menu item, But we do not have a menu bar!!!
     * so I guess I won't waste any time on this ({@code Camera2} is where it's at anyhow.)
     *
     * @param camera new {@code Camera} to show the preview of
     */
    public void switchCamera(Camera camera) {
        setCamera(camera);
        try {
            camera.setPreviewDisplay(mHolder);
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        requestLayout();

        camera.setParameters(parameters);
    }

    /**
     * Measure the view and its content to determine the measured width and the
     * measured height. This method is invoked by {@link #measure(int, int)} and
     * should be overridden by subclasses to provide accurate and efficient
     * measurement of their contents.
     * <p>
     * First we set {@code int width} to the size resolved from our view's suggested minimum width
     * as constrained by {@code widthMeasureSpec}, and {@code int height} to the size resolved from
     * our view's suggested minimum height as constrained by {@code heightMeasureSpec}. Then we call
     * {@code setMeasuredDimension(width,height)} to store the measured width and measured height.
     * Finally, if our list of the camera's supported preview sizes {@code List<Size> mSupportedPreviewSizes}
     * is not null, we call our method {@code getOptimalPreviewSize} to determine the optimal preview
     * size for our width and height in order to set {@code Size mPreviewSize}.
     *
     * @param widthMeasureSpec  horizontal space requirements as imposed by the parent.
     *                          The requirements are encoded with
     *                          {@link android.view.View.MeasureSpec}.
     * @param heightMeasureSpec vertical space requirements as imposed by the parent.
     *                          The requirements are encoded with
     *                          {@link android.view.View.MeasureSpec}.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    /**
     * Called from layout when this view should assign a size and position to each of its children.
     * We do nothing unless the {@code changed} flag passed us is true, and we have at least 1 child
     * {@code View}. If we do need to do something, we first fetch a reference to our 1 and only child
     * to {@code View child}, calculate the {@code int width}, and {@code int height} allowed us by
     * our parent using the right, left, bottom and top positions of our view relative to our parent.
     * We set {@code int previewWidth} to this {@code width}, and {@code int previewHeight} to this
     * {@code height}, then if {@code Size mPreviewSize} is not null we reset {@code previewWidth}
     * to the field {@code mPreviewSize.width} and {@code previewHeight} to the field
     * {@code mPreviewSize.height} instead. Then we calculate the coordinates necessary to center
     * our child {@code SurfaceView} within our View and instruct our {@code child} to layout itself
     * using these coordinates.
     *
     * @param changed This is a new size or position for this view
     * @param l       Left position, relative to parent
     * @param t       Top position, relative to parent
     * @param r       Right position, relative to parent
     * @param b       Bottom position, relative to parent
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2);
            }
        }
    }

    /**
     * This is called immediately after the surface is first created. Wrapped in a try block intended
     * to catch IOException, we test to make sure our field {@code Camera mCamera} is not null first
     * and then instruct it to set the {@code Surface} it will use for live preview to our parameter
     * {@code SurfaceHolder holder}.
     *
     * @param holder The {@code SurfaceHolder} whose surface is being created.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }

    /**
     * This is called immediately before a surface is being destroyed. If our field {@code Camera mCamera}
     * is not null we instruct it to stop capturing and drawing preview frames to the surface, and reset
     * the camera for a future call to {@code startPreview()}.
     *
     * @param holder The {@code SurfaceHolder} whose surface is being destroyed
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    /**
     * Calculates and returns the size from the list of supported preview sizes for this camera which
     * will make optimal use of the screen space we have available. If our parameter {@code sizes} is
     * null, we return having done nothing. Otherwise we define ASPECT_TOLERANCE to be 0.1, this is
     * used to search the supported preview sizes for a size whose aspect ratio is closest to that
     * defined by our parameters {@code w} and {@code h}. We calculate this target aspect ratio
     * {@code double targetRatio} to be {@code w/h}. We initialize {@code Size optimalSize} to null,
     * {@code double minDiff} to be the largest positive finite value of type double, and set our
     * variable {@code int targetHeight} to our parameter {@code h}.
     * <p>
     * Now in order to find a {@code Size} in {@code List<Size> sizes} which most closely matches our
     * window's aspect ratio, we loop through all the {@code Size size}'s in {@code List<Size> sizes},
     * calculate its {@code double ratio} aspect ratio by dividing its {@code size.width} field by its
     * {@code size.height} field, and if the absolute difference between {@code ratio} and
     * {@code targetRatio} is greater than ASPECT_TOLERANCE we skip it. Otherwise is the absolute
     * difference between the {@code size.height} and our {@code targetHeight} is less than the current
     * value of {@code minDiff} we set {@code optimalSize} to {@code size} and update {@code minDiff}
     * to be the absolute difference between the {@code size.height} and our {@code targetHeight}.
     * <p>
     * If this loop failed to find a best match for the aspect ratio ({@code optimalSize} is still
     * null) we search {@code List<Size> sizes} for a {@code Size} whose field {@code height} is
     * closest to {@code targetHeight} and set {@code optimalSize} to it.
     * <p>
     * In either case we return {@code optimalSize} to the caller.
     *
     * @param sizes list of supported preview sizes of our {@code Camera}
     * @param w     width of the window we will use to display the preview in
     * @param h     height of the window we will use to display the preview in
     * @return best {@code Size} from the list of supported preview sizes passed us.
     */
    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        if (sizes == null) return null;

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        @SuppressWarnings("UnnecessaryLocalVariable")
        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    /**
     * This is called immediately after any structural changes (format or size) have been made to the
     * surface. You should at this point update the imagery in the surface.
     * <p>
     * First we fetch the current settings for the Camera service we have open in {@code Camera mCamera}
     * to {@code Camera.Parameters parameters}, we set the preview size of {@code parameters} to the
     * optimal size we want as contained in {@code Size mPreviewSize}, request that a layout pass be
     * scheduled, set the parameters of {@code mCamera} to the modified {@code parameters}, and instruct
     * {@code mCamera} to start capturing and drawing preview frames to the screen.
     *
     * @param holder The SurfaceHolder whose surface has changed.
     * @param format The new PixelFormat of the surface.
     * @param w      The new width of the surface.
     * @param h      The new height of the surface.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        requestLayout();

        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

}
