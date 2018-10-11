/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.example.android.apis.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityService.MagnificationController.OnMagnificationChangedListener;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Region;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

/**
 * This class is an {@link AccessibilityService} that controls the state of
 * display magnification in response to key events. It demonstrates the
 * following key features of the Android accessibility APIs:
 * <ol>
 *   <li>Basic implementation of an AccessibilityService
 *   <li>Observing and respond to user-generated key events
 *   <li>Querying and modifying the state of display magnification
 * </ol>
 * It includes the file xml/magnification_service.xml describing the service, which is referenced by
 * a meta-data android:name="android.accessibilityservice" android:resource="@xml/magnification_service"
 * element in AndroidManifest.xml.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class MagnificationService extends AccessibilityService {
    /**
     * TAG used for logging.
     */
    private static final String LOG_TAG = "MagnificationService";

    /**
     * Callback for {@link android.view.accessibility.AccessibilityEvent}s. We do not need.
     *
     * @param event The new event. This event is owned by the caller and cannot be used after
     * this method returns. Services wishing to use the event after this method returns should
     * make a copy.
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // No events required for this service.
    }

    /**
     * Callback for interrupting the accessibility feedback.
     */
    @Override
    public void onInterrupt() {
        // No interruptible actions taken by this service.
    }

    /**
     * Callback that allows an accessibility service to observe the key events
     * before they are passed to the rest of the system. This means that the events
     * are first delivered here before they are passed to the device policy, the
     * input method, or applications.
     * <p>
     * <strong>Note:</strong> It is important that key events are handled in such
     * a way that the event stream that would be passed to the rest of the system
     * is well-formed. For example, handling the down event but not the up event
     * and vice versa would generate an inconsistent event stream.
     * <p>
     * <strong>Note:</strong> The key events delivered in this method are copies
     * and modifying them will have no effect on the events that will be passed
     * to the system. This method is intended to perform purely filtering
     * functionality.
     * <p>
     * We initialize our variable {@code int keyCode} with the keycode of our parameter
     * {@code KeyEvent event}. If {@code keyCode} is not KEYCODE_VOLUME_UP or KEYCODE_VOLUME_DOWN
     * we return false so that the event will be delivered as usual. We initialize our
     * variable {@code int action} with the action of {@code event}. If {@code action} is ACTION_UP
     * (the key has been released) we call our method {@code handleVolumeKey} with true if
     * {@code keyCode} is KEYCODE_VOLUME_UP, and false if it is KEYCODE_VOLUME_DOWN. We then
     * return true to consume the event here.
     *
     * @param event The event to be processed.
     * @return If true then the event will be consumed and not delivered to
     *         applications, otherwise it will be delivered as usual.
     */
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        // Only consume volume key events.
        final int keyCode = event.getKeyCode();
        if (keyCode != KeyEvent.KEYCODE_VOLUME_UP
                && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
            return false;
        }

        // Handle the event when the user releases the volume key. To prevent
        // the keys from actually adjusting the device volume, we'll ignore
        // the result of handleVolumeKey() and always return true to consume
        // the events.
        final int action = event.getAction();
        if (action == KeyEvent.ACTION_UP) {
            handleVolumeKey(keyCode == KeyEvent.KEYCODE_VOLUME_UP);
        }

        // Consume all volume key events.
        return true;
    }

    /**
     * Adjusts the magnification scale in response to volume key actions. First we initialize our
     * variable {@code MagnificationController controller} with the magnification controller. We
     * initialize {@code float currScale} with the current magnification scale that the {@code getScale}
     * method of {@code controller} returns. We initialize {@code float increment} to 0.1f if our
     * parameter {@code isVolumeUp} is true or -0.1f if it is false, then initialize {@code float nextScale}
     * to the maximum of 1.0f and the minimum of 5.0f and {@code currScale} added to {@code increment}
     * (clamping the scale between 1x and 5x). If {@code nextScale} is equal to {@code currScale} we
     * return false without having done nothing. Otherwise we initialize {@code DisplayMetrics metrics}
     * by using a Resources instance for the application's package to retrieve the current display
     * metrics, set the magnification scale of {@code controller} animating to the new scale, and
     * set the center of {@code controller} to an X coordinate of half the absolute width of the display,
     * a Y coordinate of half the absolute height of the display, also animating to the new center.
     * Finally we return true to consume the event here.
     *
     * @param isVolumeUp {@code true} if the volume up key was pressed or
     *                   {@code false} if the volume down key was pressed
     * @return {@code true} if the magnification scale changed as a result of
     *         the key
     */
    @SuppressWarnings("UnusedReturnValue")
    private boolean handleVolumeKey(boolean isVolumeUp) {
        // Obtain the controller on-demand, which allows us to avoid
        // dependencies on the accessibility service's lifecycle.
        final MagnificationController controller = getMagnificationController();

        // Adjust the current scale based on which volume key was pressed,
        // constraining the scale between 1x and 5x.
        final float currScale = controller.getScale();
        final float increment = isVolumeUp ? 0.1f : -0.1f;
        final float nextScale = Math.max(1f, Math.min(5f, currScale + increment));
        if (nextScale == currScale) {
            return false;
        }

        // Set the pivot, then scale around it.
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        controller.setScale(nextScale, true /* animate */);
        controller.setCenter(metrics.widthPixels / 2f, metrics.heightPixels / 2f, true);
        return true;
    }

    /**
     * This method is a part of the {@link AccessibilityService} lifecycle and is
     * called after the system has successfully bound to the service. If is
     * convenient to use this method for setting the {@link AccessibilityServiceInfo}.
     * <p>
     * First we initialize {@code AccessibilityServiceInfo info} with an {@link AccessibilityServiceInfo}
     * describing this {@link AccessibilityService}. If {@code info} is null, we are not really connected
     * so we return having done nothing. Otherwise we or in the FLAG_REQUEST_FILTER_KEY_EVENTS flag
     * (if this flag is set the accessibility service will receive the key events before applications
     * allowing it implement global shortcuts) into the {@code flags} field of {@code info} and then
     * call the {@code setServiceInfo} method to set the {@link AccessibilityServiceInfo} that describes
     * this service to {@code info}. Finally we add an anonymous {@code OnMagnificationChangedListener}
     * to receive notification of changes in the state of magnification. Its {@code onMagnificationChanged}
     * override just logs the new magnification scale.
     *
     * @see AccessibilityServiceInfo
     * @see #setServiceInfo(AccessibilityServiceInfo)
     */
    @Override
    public void onServiceConnected() {
        final AccessibilityServiceInfo info = getServiceInfo();
        if (info == null) {
            // If we fail to obtain the service info, the service is not really
            // connected and we should avoid setting anything up.
            return;
        }

        // We declared our intent to request key filtering in the meta-data
        // attached to our service in the manifest. Now, we can explicitly
        // turn on key filtering when needed.
        info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        setServiceInfo(info);

        // Set up a listener for changes in the state of magnification.
        getMagnificationController().addListener(new OnMagnificationChangedListener() {
            /**
             * Called when the magnified region, scale, or center changes. We just log a message
             * about the new magnification scale {@code float scale}.
             *
             * @param controller the magnification controller
             * @param region the magnification region
             * @param scale the new scale
             * @param centerX the new X coordinate, in unscaled coordinates, around which
             * magnification is focused
             * @param centerY the new Y coordinate, in unscaled coordinates, around which
             * magnification is focused
             */
            @Override
            public void onMagnificationChanged(@NonNull MagnificationController controller,
                                               @NonNull Region region, float scale, float centerX, float centerY) {
                Log.e(LOG_TAG, "Magnification scale is now " + scale);
            }
        });
    }
}
