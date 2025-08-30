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

package com.example.android.apis.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import kotlin.math.max
import kotlin.math.min

/**
 * This class is an [AccessibilityService] that controls the state of
 * display magnification in response to key events. It demonstrates the
 * following key features of the Android accessibility APIs:
 *
 *  1. Basic implementation of an AccessibilityService
 *  1. Observing and responding to user-generated key events
 *  1. Querying and modifying the state of display magnification
 *
 * It includes the file xml/magnification_service.xml describing the service, which
 * is referenced by a meta-data android:name="android.accessibilityservice"
 * android:resource="@xml/magnification_service" element in AndroidManifest.xml.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
class MagnificationService : AccessibilityService() {

    /**
     * Callback for [AccessibilityEvent]s. We do not need.
     *
     * @param event The new event. This event is owned by the caller and cannot be used after
     * this method returns. Services wishing to use the event after this method returns should
     * make a copy.
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // No events required for this service.
    }

    /**
     * Callback for interrupting the accessibility feedback.
     */
    override fun onInterrupt() {
        // No interruptible actions taken by this service.
    }

    /**
     * Callback that allows an accessibility service to observe the key events
     * before they are passed to the rest of the system. This means that the events
     * are first delivered here before they are passed to the device policy, the
     * input method, or applications.
     *
     * **Note:** It is important that key events are handled in such
     * a way that the event stream that would be passed to the rest of the system
     * is well-formed. For example, handling the down event but not the up event
     * and vice versa would generate an inconsistent event stream.
     *
     * **Note:** The key events delivered in this method are copies
     * and modifying them will have no effect on the events that will be passed
     * to the system. This method is intended to perform purely filtering
     * functionality.
     *
     * We initialize our variable `int keyCode` with the keycode of our parameter
     * `KeyEvent event`. If `keyCode` is not KEYCODE_VOLUME_UP or KEYCODE_VOLUME_DOWN
     * we return false so that the event will be delivered as usual. We initialize our
     * variable `int action` with the action of `event`. If `action` is ACTION_UP
     * (the key has been released) we call our method `handleVolumeKey` with true if
     * `keyCode` is KEYCODE_VOLUME_UP, and false if it is KEYCODE_VOLUME_DOWN. We then
     * return true to consume the event here.
     *
     * @param event The event to be processed.
     * @return If true then the event will be consumed and not delivered to
     * applications, otherwise it will be delivered as usual.
     */
    override fun onKeyEvent(event: KeyEvent): Boolean {
        // Only consume volume key events.
        val keyCode = event.keyCode
        if (keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
            return false
        }

        // Handle the event when the user releases the volume key. To prevent
        // the keys from actually adjusting the device volume, we'll ignore
        // the result of handleVolumeKey() and always return true to consume
        // the events.
        val action = event.action
        if (action == KeyEvent.ACTION_UP) {
            handleVolumeKey(keyCode == KeyEvent.KEYCODE_VOLUME_UP)
        }

        // Consume all volume key events.
        return true
    }

    /**
     * Adjusts the magnification scale in response to volume key actions. First we initialize our
     * variable `MagnificationController controller` with the magnification controller. We
     * initialize `float currScale` with the current magnification scale that the `getScale`
     * method of `controller` returns. We initialize `float increment` to 0.1f if our
     * parameter `isVolumeUp` is true or -0.1f if it is false, then initialize `float nextScale`
     * to the maximum of 1.0f and the minimum of 5.0f and `currScale` added to `increment`
     * (clamping the scale between 1x and 5x). If `nextScale` is equal to `currScale` we
     * return false without having done nothing. Otherwise we initialize `DisplayMetrics metrics`
     * by using a Resources instance for the application's package to retrieve the current display
     * metrics, set the magnification scale of `controller` animating to the new scale, and
     * set the center of `controller` to an X coordinate of half the absolute width of the display,
     * a Y coordinate of half the absolute height of the display, also animating to the new center.
     * Finally we return true to consume the event here.
     *
     * @param isVolumeUp `true` if the volume up key was pressed or
     * `false` if the volume down key was pressed
     * @return `true` if the magnification scale changed as a result of
     * the key
     */
    private fun handleVolumeKey(isVolumeUp: Boolean): Boolean {
        // Obtain the controller on-demand, which allows us to avoid
        // dependencies on the accessibility service's lifecycle.
        val controller: MagnificationController = magnificationController

        // Adjust the current scale based on which volume key was pressed,
        // constraining the scale between 1x and 5x.
        val currScale: Float = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            controller.getMagnificationConfig()!!.scale
        } else {
            @Suppress("DEPRECATION") // Needed for devices older than TIRAMISU
            controller.scale
        }
        val increment = if (isVolumeUp) 0.1f else -0.1f
        val nextScale = max(1f, min(5f, currScale + increment))
        if (nextScale == currScale) {
            return false
        }

        // Set the pivot, then scale around it.
        val metrics = resources.displayMetrics
        @Suppress("DEPRECATION") // TODO: Use MagnificationConfig.Builder for "T"
        controller.setScale(nextScale, true /* animate */)
        @Suppress("DEPRECATION") // TODO: Use MagnificationConfig.Builder for "T"
        controller.setCenter(
            metrics.widthPixels / 2f,
            metrics.heightPixels / 2f,
            true
        )
        return true
    }

    /**
     * This method is a part of the [AccessibilityService] lifecycle and is
     * called after the system has successfully bound to the service. If is
     * convenient to use this method for setting the [AccessibilityServiceInfo].
     *
     * First we initialize `AccessibilityServiceInfo info` with an [AccessibilityServiceInfo]
     * describing this [AccessibilityService]. If `info` is null, we are not really connected
     * so we return having done nothing. Otherwise we or in the FLAG_REQUEST_FILTER_KEY_EVENTS flag
     * (if this flag is set the accessibility service will receive the key events before applications
     * allowing it implement global shortcuts) into the `flags` field of `info` and then
     * call the `setServiceInfo` method to set the [AccessibilityServiceInfo] that describes
     * this service to `info`. Finally we add an anonymous `OnMagnificationChangedListener`
     * to receive notification of changes in the state of magnification. Its `onMagnificationChanged`
     * override just logs the new magnification scale.
     */
    public override fun onServiceConnected() {
        val info = serviceInfo
            ?: // If we fail to obtain the service info, the service is not really
            // connected and we should avoid setting anything up.
            return

        // We declared our intent to request key filtering in the meta-data
        // attached to our service in the manifest. Now, we can explicitly
        // turn on key filtering when needed.
        info.flags = info.flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        serviceInfo = info

        // Set up a listener for changes in the state of magnification.
        @Suppress("UNUSED_ANONYMOUS_PARAMETER")
        magnificationController.addListener { controller, region, scale, centerX, centerY ->
            /**
             * Called when the magnified region, scale, or center changes. We just log a message
             * about the new magnification scale `float scale`.
             *
             * @param controller the magnification controller
             * @param region the magnification region
             * @param scale the new scale
             * @param centerX the new X coordinate, in unscaled coordinates, around which
             * magnification is focused
             * @param centerY the new Y coordinate, in unscaled coordinates, around which
             * magnification is focused
             */
            Log.e(LOG_TAG, "Magnification scale is now $scale")
        }
    }

    companion object {
        /**
         * TAG used for logging.
         */
        private const val LOG_TAG = "MagnificationService"
    }
}
