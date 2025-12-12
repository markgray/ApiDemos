/*
 * Copyright (C) 2009 The Android Open Source Project
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
@file:Suppress("unused", "ReplaceNotNullAssertionWithElvisReturn")

package com.example.android.apis.graphics

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message

/**
 * PurgeableBitmap demonstrates the effects of setting Bitmaps as being
 * purgeable.
 *
 * In the NonPurgeable case, an encoded bitstream is decoded to a different
 * Bitmap over and over again up to 200 times until out-of-memory occurs.
 * In contrast, the Purgeable case shows that the system can complete decoding
 * the encoded bitstream 200 times without hitting the out-of-memory case.
 */
class PurgeableBitmap : GraphicsActivity() {
    /**
     * Our instance of `PurgeableBitmapView`
     */
    private var mView: PurgeableBitmapView? = null

    /**
     * `Handler` instance used to call the `update` method of `mView` every 100
     * milliseconds, and to call our method `showAlertDialog` when it has finished loading
     * its bitmaps.
     */
    private val mRedrawHandler = RefreshHandler()

    /**
     * `Handler` class used to call the `update` method of `mView` every 100
     * milliseconds, and to call our method `showAlertDialog` when it has finished loading
     * its bitmaps.
     */
    @SuppressLint("HandlerLeak")
    inner class RefreshHandler : Handler(Looper.myLooper()!!) {
        /**
         * We implement this to receive messages. First we call the `update` method of
         * `PurgeableBitmapView mView` saving the return value in `int index`. If
         * `index` is greater than 0, we ran out of memory and we call `showAlertDialog`
         * to display this fact and the number of bitmaps we loaded before this happened (the value
         * of `index`). If `index` is less than 0, we loaded all bitmaps without running
         * out of memory and we call `showAlertDialog` to display this fact and the number of
         * bitmaps we loaded (the value of `-index`) (we also call the `invalidate` method
         * so `mView` will draw the last bitmap). If `index` is 0 we are still working
         * so we just call the `invalidate` method so `mView` will draw the bitmap it
         * just loaded.
         *
         * @param msg A [Message][android.os.Message] object
         */
        override fun handleMessage(msg: Message) {
            val index = mView!!.update(this)
            when {
                index > 0 -> {
                    showAlertDialog(getDialogMessage(true, index))
                }

                index < 0 -> {
                    mView!!.invalidate()
                    showAlertDialog(getDialogMessage(false, -index))
                }

                else -> {
                    mView!!.invalidate()
                }
            }
        }

        /**
         * This method is called to schedule a message to be sent to this handler with a delay of
         * `delayMillis`, which will cause our `handleMessage` method to be called in
         * the sweet by and by. It is used in the `onCreate` method of `PurgeableBitmap`
         * with a delay of 0 to start the ball rolling, and then in the `update` method of
         * `PurgeableBitmapView` with a delay of 100 when there are more bitmaps yet to be
         * loaded.
         *
         * First we remove all messages with the field `what` set to 0, then we call the method
         * `sendMessageDelayed` with a `Message` whose field `what` is set to 0,
         * specifying a delay of our parameter `delayMillis` milliseconds.
         *
         * @param delayMillis delay in milliseconds to send message to ourselves.
         */
        fun sleep(delayMillis: Long) {
            this.removeMessages(0)
            sendMessageDelayed(obtainMessage(0), delayMillis)
        }
    }

    /**
     * Called when the activity is starting. First we call though to our super's implementation of
     * `onCreate`, then we initialize our field `PurgeableBitmapView mView` with a new
     * instance of `PurgeableBitmapView`. We pass its constructor the return value of our method
     * `detectIfPurgeableRequest` which queries the `PackageManager` to determine if the
     * textual label associated with our activity in the AndroidManifest was "Purgeable", returning
     * true if so. `PurgeableBitmapView` sets the `inPurgeable` field of the options used
     * to create its bitmaps to that value (it is ignored since Lollipop). We then call the `sleep`
     * method of our field `RefreshHandler mRedrawHandler` with a delay of 0 to start the loading
     * of bitmaps by `mView`. Finally we set our content view to `mView`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mView = PurgeableBitmapView(this, detectIfPurgeableRequest())
        mRedrawHandler.sleep(0)
        setContentView(mView!!)
    }

    /**
     * Called to determine if the user started us using a "Purgeable" label, or "NonPurgeable" label.
     * First we retrieve an instance of `PackageManager` to `PackageManager pm`, and use
     * it to retrieve to `ActivityInfo info` the metaData data Bundles that are associated with
     * the component name of this activity. From `info` we load the textual label associated with
     * the activity into `CharSequence labelSeq`. We slit `labelSeq` using "/" as the
     * delimiter into `String[] components`. Then if the last string in `components` is
     * "Purgeable" we return true, otherwise we return false.
     *
     * @return true if the android:label in the AndroidManifest associated with this instance of
     * `PurgeableBitmap` ended in the string "Purgeable", false otherwise (an activity-alias
     * element uses an android:label that ends in "Purgeable", but the android:targetActivity activity
     * element uses an android:label that ends in "NonPurgeable" and both elements can be used to
     * start this activity).
     */
    private fun detectIfPurgeableRequest(): Boolean {
        val pm = packageManager
        val labelSeq = try {
            val info = pm.getActivityInfo(
                /* component = */ this.componentName,
                /* flags = */ PackageManager.GET_META_DATA
            )
            info.loadLabel(pm)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return false
        }
        val components = labelSeq.toString().split("/").toTypedArray()
        return components[components.size - 1] == "Purgeable"
    }

    /**
     * Generates a `String` summarising the results of the loading of bitmaps by our instance
     * of `PurgeableBitmapView`. We create a new instance of `StringBuilder` for
     * `StringBuilder sb`, then if our parameter `isOutOfMemory` is true we build in
     * `sb` a string stating when the out of memory occurs. Otherwise we build a string stating
     * the complete decoding occurred, and how many bitmaps were loaded. Finally we return the string
     * value of `sb` to the caller.
     *
     * @param isOutOfMemory flag to indicate that `PurgeableBitmapView` ran out of memory.
     * @param index Number of bitmaps successfully loaded
     * @return `String` describing what happened, suitable for use in a dialog.
     */
    private fun getDialogMessage(isOutOfMemory: Boolean, index: Int): String {
        val sb = StringBuilder()
        if (isOutOfMemory) {
            sb.append("Out of memory occurs when the ")
            sb.append(index)
            sb.append("th Bitmap is decoded.")
        } else {
            sb.append("Complete decoding ")
                .append(index)
                .append(" bitmaps without running out of memory.")
        }
        return sb.toString()
    }

    /**
     * Builds and shows an `AlertDialog` to display its string parameter.
     *
     * @param message string to display in our `AlertDialog`
     */
    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private fun showAlertDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog: DialogInterface?, id: Int -> }
        val alert = builder.create()
        alert.show()
    }
}