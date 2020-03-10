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

package com.example.android.apis.media.projection;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows how to use a ProjectionManager.createScreenCaptureIntent to capture screen content to a
 * VirtualDisplay which is created using MediaProjection.createVirtualDisplay to display to a
 * SurfaceView.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MediaProjectionDemo extends AppCompatActivity {
    /**
     * TAG used for logging.
     */
    private static final String TAG = "MediaProjectionDemo";
    /**
     * Request code used when starting the intent created by {@code createScreenCaptureIntent} (for
     * result). It is returned in the {@code requestCode} parameter when our {@code onActivityResult}
     * method is called.
     */
    private static final int PERMISSION_CODE = 1;
    /**
     * List of {@code Resolution} objects which are used to populate the spinner R.id.spinner, and
     * which the user can select to set the resolution of the virtual display.
     */
    private static final List<Resolution> RESOLUTIONS = new ArrayList<Resolution>() {{
        add(new Resolution(640,360));
        add(new Resolution(960,540));
        add(new Resolution(1366,768));
        add(new Resolution(1600,900));
    }};

    /**
     * Screen density expressed as dots-per-inch. May be either DENSITY_LOW, DENSITY_MEDIUM, or
     * DENSITY_HIGH. Retrieved from the display metrics of the default screen in our {@code onCreate}
     * method.
     */
    private int mScreenDensity;
    /**
     * {@code MediaProjectionManager} that we use to create an intent to do a screen capture and to
     * retrieve the screen capture token granting applications the ability to capture screen contents
     * to our field {@code MediaProjection mMediaProjection} from the intent data that is received by
     * our method {@code onActivityResult} after the activity launched by our screen capture intent
     * finishes its task.
     */
    private MediaProjectionManager mProjectionManager;

    /**
     * Width of the virtual display we display our screen capture in.
     */
    private int mDisplayWidth;
    /**
     * Height of the virtual display we display our screen capture in.
     */
    private int mDisplayHeight;
    /**
     * Flag to indicate that we are currently "sharing" the screen to our virtual display.
     */
    private boolean mScreenSharing;

    /**
     * {@code MediaProjection} token granting applications the ability to capture screen contents,
     * it is retrieved from the intent data passed to our {@code onActivityResult} method from the
     * activity started for result with a screen capture intent.
     */
    private MediaProjection mMediaProjection;
    /**
     * {@code VirtualDisplay} used to display our screen capture.
     */
    private VirtualDisplay mVirtualDisplay;
    /**
     * The {@code Surface} that provides direct access to the surface object underlying our field
     * {@code SurfaceView mSurfaceView}
     */
    private Surface mSurface;
    /**
     * {@code SurfaceView} with id R.id.surface in our layout file, we use it to display our virtual
     * display.
     */
    private SurfaceView mSurfaceView;
    /**
     * {@code ToggleButton} with id R.id.screen_sharing_toggle in our layout file, its on click
     * callback is set with the attribute android:onClick="onToggleScreenShare". {@code onToggleScreenShare}
     * toggles screen sharing, calling {@code shareScreen} if the new state is "isChecked", or
     * {@code stopScreenSharing} if the new state is not checked.
     */
    private ToggleButton mToggle;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.media_projection.
     * We create a new instance for {@code DisplayMetrics metrics}, then retrieve the window manager
     * for showing custom windows, fetch the Display upon which this WindowManager instance will
     * create new windows, and get the display metrics that describe the size and density of this
     * display into {@code DisplayMetrics metrics}. We then initialize our field {@code int mScreenDensity}
     * with the screen density field in {@code metrics}, expressed as dots-per-inch (may be either
     * DENSITY_LOW, DENSITY_MEDIUM, or DENSITY_HIGH).
     * <p>
     * We initialize our field {@code SurfaceView mSurfaceView} by locating the {@code SurfaceView}
     * in our layout file with ID R.id.surface. We fetch the SurfaceHolder providing access and
     * control of the underlying surface of {@code SurfaceView mSurfaceView}, then set our field
     * {@code Surface mSurface} to its {@code Surface}. Next we initialize our field
     * {@code MediaProjectionManager mProjectionManager} with the handle to the system-level service
     * MEDIA_PROJECTION_SERVICE (used for managing media projection sessions).
     * <p>
     * We create {@code ArrayAdapter<Resolution> arrayAdapter} from our list {@code List<Resolution> RESOLUTIONS}
     * using android.R.layout.simple_list_item_1 as the layout file containing a TextView to use when
     * instantiating views. We locate the {@code Spinner} with ID R.id.spinner in our layout file in
     * order to set {@code Spinner s}, set its adapter to {@code arrayAdapter}, set its {@code OnItemSelectedListener}
     * to a new instance of our class {@code ResolutionSelector}, and set its selection to 0.
     * <p>
     * Finally we locate the {@code ToggleButton} with ID R.id.screen_sharing_toggle in our layout
     * file in order to initialize our field {@code ToggleButton mToggle}, and call its method
     * {@code setSaveEnabled(false)} to disable state saving.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_projection);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        mSurfaceView = findViewById(R.id.surface);
        mSurface = mSurfaceView.getHolder().getSurface();
        mProjectionManager =
            (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        ArrayAdapter<Resolution> arrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, RESOLUTIONS);
        Spinner s = findViewById(R.id.spinner);
        s.setAdapter(arrayAdapter);
        s.setOnItemSelectedListener(new ResolutionSelector());
        s.setSelection(0);

        mToggle = findViewById(R.id.screen_sharing_toggle);
        mToggle.setSaveEnabled(false);
    }

    /**
     * Called when you are no longer visible to the user. We call our method {@code stopScreenSharing}
     * to release the virtual display and destroy its underlying surface. Finally we call our super's
     * implementation of {@code onStop}.
     */
    @Override
    protected void onStop() {
        stopScreenSharing();
        super.onStop();
    }

    /**
     * Perform any final cleanup before an activity is destroyed. First we call through to our super's
     * implementation of {@code onDestroy}, then if our field {@code MediaProjection mMediaProjection}
     * is not null, we stop the projection, and set {@code mMediaProjection} to null.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it. First we make sure that the
     * {@code requestCode} returned is PERMISSION_CODE, and if not we log the problem and return
     * having done nothing. If the {@code resultCode} is not RESULT_OK, we toast the message "User
     * denied screen sharing permission" and return having done nothing.
     * <p>
     * If everything is correct, we initialize our field {@code MediaProjection mMediaProjection} to
     * the MediaProjection obtained from the successful screen capture request contained in the returned
     * {@code Intent data}. We register a new instance of our class {@code MediaProjectionCallback}
     * as the callback for {@code MediaProjection mMediaProjection}, and finally initialize our field
     * {@code VirtualDisplay mVirtualDisplay} with the instance of {@code VirtualDisplay} returned by
     * our method {@code createVirtualDisplay}.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(),
     *                    allowing you to identify who this result came from.
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param data        An Intent, which can return result data to the caller (various data can be
     *                    attached to Intent "extras").
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PERMISSION_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this,
                    "User denied screen sharing permission", Toast.LENGTH_SHORT).show();
            return;
        }
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjection.registerCallback(new MediaProjectionCallback(), null);
        mVirtualDisplay = createVirtualDisplay();
    }

    /**
     * Set as the {@code OnClickListener} of the {@code ToggleButton} with ID R.id.screen_sharing_toggle
     * in our layout file using the attribute android:onClick="onToggleScreenShare". If the new state
     * of the {@code View view} is "checked" we call our method {@code shareScreen}, otherwise we call
     * our method {@code stopScreenSharing}.
     *
     * @param view View ({@code ToggleButton}) which has been toggled
     */
    public void onToggleScreenShare(View view) {
        if (((ToggleButton) view).isChecked()) {
            shareScreen();
        } else {
            stopScreenSharing();
        }
    }

    /**
     * Starts sharing the screen. First we set our flag {@code boolean mScreenSharing} to true, then
     * if our field {@code Surface mSurface} is null we return without doing anything more. If our
     * field {@code MediaProjection mMediaProjection} is null we start an activity for its result
     * using the intent to start screen capture created by our {@code MediaProjectionManager mProjectionManager}
     * and using PERMISSION_CODE as the request code, and return. Otherwise we set our field
     * {@code VirtualDisplay mVirtualDisplay} to the {@code VirtualDisplay} returned by our method
     * {@code createVirtualDisplay}.
     */
    private void shareScreen() {
        mScreenSharing = true;
        if (mSurface == null) {
            return;
        }
        if (mMediaProjection == null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), PERMISSION_CODE);
            return;
        }
        mVirtualDisplay = createVirtualDisplay();
    }

    /**
     * Stops screen sharing. If {@code ToggleButton mToggle} is "checked", we set it to unchecked,
     * then we set our flag {@code mScreenSharing} to false. If our field {@code VirtualDisplay mVirtualDisplay}
     * is not null, we release the virtual display and destroy its underlying surface, and set
     * {@code mVirtualDisplay} to null.
     */
    private void stopScreenSharing() {
        if (mToggle.isChecked()) {
            mToggle.setChecked(false);
        }

        mScreenSharing = false;
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
    }

    /**
     * Creates and returns a {@code VirtualDisplay} that continuously displays the screen capture to
     * {@code Surface mSurface}. We return the {@code VirtualDisplay} returned from the
     * {@code createVirtualDisplay} method of our field {@code MediaProjection mMediaProjection}.
     * The parameters passed to its {@code createVirtualDisplay} are:
     * <ul>
     * <li>
     * name String: The name of the virtual display: "ScreenSharingDemo"
     * </li>
     * <li>
     * width int: The width of the virtual display in pixels: our field {@code mDisplayWidth}
     * </li>
     * <li>
     * height int: The height of the virtual display in pixels: our field {@code mDisplayHeight}
     * </li>
     * <li>
     * dpi int: The density of the virtual display in dpi: our filed {@code mScreenDensity}
     * </li>
     * <li>
     * flags int: A combination of virtual display flags: VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
     * (an auto-mirroring virtual display, continuously capturing and displaying the screen)
     * </li>
     * <li>
     * surface Surface: The surface to which the content of the virtual display should be rendered:
     * our field {@code Surface mSurface}
     * </li>
     * <li>
     * callback VirtualDisplay.Callback: Callback to call when the virtual display's state changes:
     * null, so no callback.
     * </li>
     * <li>
     * handler Handler: The Handler on which the callback should be invoked: null so the callback
     * should be invoked on the calling thread's main Looper (if we had a callback).
     * </li>
     * </ul>
     *
     * @return a {@code VirtualDisplay} drawing the screen capture to {@code Surface mSurface}.
     */
    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("ScreenSharingDemo",
                mDisplayWidth, mDisplayHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mSurface, null /*Callbacks*/, null /*Handler*/);
    }

    /**
     * Re-sizes our {@code VirtualDisplay mVirtualDisplay} to be {@code mDisplayWidth} by {@code mDisplayHeight}
     * with density {@code mScreenDensity} if {@code mVirtualDisplay} is not null. Called only from
     * the {@code surfaceChanged} method of our class {@code SurfaceCallbacks} which is UNUSED, so this
     * is unused as well.
     */
    private void resizeVirtualDisplay() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.resize(mDisplayWidth, mDisplayHeight, mScreenDensity);
    }

    /**
     * {@code OnItemSelectedListener} of the {@code Spinner} with ID R.id.spinner, it allows the
     * user to select a {@code Resolution} from the list {@code List<Resolution> RESOLUTIONS}.
     */
    private class ResolutionSelector implements Spinner.OnItemSelectedListener {
        /**
         * Callback method to be invoked when a new item in this view has been selected. First we
         * retrieve the item that has been selected to {@code Resolution r}. We fetch the layout
         * parameters of {@code SurfaceView mSurfaceView} (the view our virtual display is drawing
         * to) into {@code ViewGroup.LayoutParams lp}. We fetch our packages resources current
         * configuration in order to check the current orientation. If the orientation is ORIENTATION_LANDSCAPE
         * we set {@code mDisplayHeight} to {@code r.y} and {@code mDisplayWidth} to {@code r.x}, otherwise
         * we set {@code mDisplayHeight} to {@code r.x} and {@code mDisplayWidth} to {@code r.y}.
         * We set the {@code height} field of {@code lp} to {@code mDisplayHeight} and the {@code width}
         * field to {@code mDisplayWidth}, and finally set the layout parameters of {@code mSurfaceView}
         * to the modified {@code lp}.
         *
         * @param parent The AdapterView where the selection happened
         * @param v      The view within the AdapterView that was clicked
         * @param pos    The position of the view in the adapter
         * @param id     The row id of the item that is selected
         */
        @SuppressWarnings("SuspiciousNameCombination")
        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
            Resolution r = (Resolution) parent.getItemAtPosition(pos);
            ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mDisplayHeight = r.y;
                mDisplayWidth = r.x;
            } else {
                mDisplayHeight = r.x;
                mDisplayWidth = r.y;
            }
            lp.height = mDisplayHeight;
            lp.width = mDisplayWidth;
            mSurfaceView.setLayoutParams(lp);
        }

        /**
         * Callback method to be invoked when the selection disappears from this view. We ignore it.
         *
         * @param parent The AdapterView that now contains no selected item.
         */
        @Override
        public void onNothingSelected(AdapterView<?> parent) { /* Ignore */ }
    }

    /**
     * Callback for the projection session.
     */
    private class MediaProjectionCallback extends MediaProjection.Callback {
        /**
         * Called when the MediaProjection session is no longer valid. We set {@code mMediaProjection}
         * to null and call our method {@code stopScreenSharing} to release the virtual display if
         * it exists.
         */
        @Override
        public void onStop() {
            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    /**
     * We implement this interface to receive information about changes to the surface, but do not use
     */
    @SuppressWarnings("unused")
    private class SurfaceCallbacks implements SurfaceHolder.Callback {
        /**
         * This is called immediately after any structural changes (format or size) have been made
         * to the surface.  You should at this point update the imagery in the surface.  This method
         * is always called at least once, after {@link #surfaceCreated}.
         * <p>
         * We store the new {@code width} in {@code mDisplayWidth}, the new {@code height} in
         * {@code mDisplayHeight} and call our method {@code resizeVirtualDisplay} to resize our
         * virtual display.
         *
         * @param holder The SurfaceHolder whose surface has changed.
         * @param format The new PixelFormat of the surface.
         * @param width  The new width of the surface.
         * @param height The new height of the surface.
         */
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mDisplayWidth = width;
            mDisplayHeight = height;
            resizeVirtualDisplay();
        }

        /**
         * This is called immediately after the surface is first created. We initialize our field
         * {@code Surface mSurface} with direct access to the surface object of {@code holder}. Then
         * if our flag {@code boolean mScreenSharing} is true, we call our method {@code shareScreen}
         * to start sharing the screen.
         *
         * @param holder The SurfaceHolder whose surface is being created.
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurface = holder.getSurface();
            if (mScreenSharing) {
                shareScreen();
            }
        }

        /**
         * This is called immediately before a surface is being destroyed. If our flag
         * {@code boolean mScreenSharing} is not true we call our method {@code stopScreenSharing}
         * to stop sharing the screen.
         *
         * @param holder The SurfaceHolder whose surface is being destroyed.
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (!mScreenSharing) {
                stopScreenSharing();
            }
        }
    }

    /**
     * Class used to hold each of the available screen resolutions that the user can choose using the
     * {@code Spinner} with ID R.id.spinner.
     */
    @SuppressWarnings("WeakerAccess")
    private static class Resolution {
        /**
         * x dimension of the resolution
         */
        int x;
        /**
         * y dimension of the resolution
         */
        int y;

        /**
         * Our constructor, simply initializes our fields with our parameters.
         *
         * @param x x dimension of the resolution
         * @param y y dimension of the resolution
         */
        @SuppressWarnings("WeakerAccess")
        public Resolution(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Returns a string containing a concise, human-readable description of this object. We simply
         * return the string representation of our two fields separated with the string "x".
         *
         * @return a printable representation of this object.
         */
        @NotNull
        @Override
        public String toString() {
            return x + "x" + y;
        }
    }
}
