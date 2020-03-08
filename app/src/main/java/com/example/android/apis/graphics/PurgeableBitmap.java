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

package com.example.android.apis.graphics;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.jetbrains.annotations.NotNull;

/**
 * PurgeableBitmap demonstrates the effects of setting Bitmaps as being
 * purgeable.
 * <p>
 * In the NonPurgeable case, an encoded bitstream is decoded to a different
 * Bitmap over and over again up to 200 times until out-of-memory occurs.
 * In contrast, the Purgeable case shows that the system can complete decoding
 * the encoded bitstream 200 times without hitting the out-of-memory case.
 */
public class PurgeableBitmap extends GraphicsActivity {

    /**
     * Our instance of {@code PurgeableBitmapView}
     */
    private PurgeableBitmapView mView;
    /**
     * {@code Handler} instance used to call the {@code update} method of {@code mView} every 100
     * milliseconds, and to call our method {@code showAlertDialog} when it has finished loading
     * its bitmaps.
     */
    private final RefreshHandler mRedrawHandler = new RefreshHandler();

    /**
     * {@code Handler} class used to call the {@code update} method of {@code mView} every 100
     * milliseconds, and to call our method {@code showAlertDialog} when it has finished loading
     * its bitmaps.
     */
    @SuppressLint("HandlerLeak")
    public class RefreshHandler extends Handler {

        /**
         * We implement this to receive messages. First we call the {@code update} method of
         * {@code PurgeableBitmapView mView} saving the return value in {@code int index}. If
         * {@code index} is greater than 0, we ran out of memory and we call {@code showAlertDialog}
         * to display this fact and the number of bitmaps we loaded before this happened (the value
         * of {@code index}). If {@code index} is less than 0, we loaded all bitmaps without running
         * out of memory and we call {@code showAlertDialog} to display this fact and the number of
         * bitmaps we loaded (the value of {@code -index}) (we also call the {@code invalidate} method
         * so {@code mView} will draw the last bitmap). If {@code index} is 0 we are still working
         * so we just call the {@code invalidate} method so {@code mView} will draw the bitmap it
         * just loaded.
         *
         * @param msg A {@link android.os.Message Message} object
         */
        @Override
        public void handleMessage(@NotNull Message msg) {
            int index = mView.update(this);
            if (index > 0) {
                showAlertDialog(getDialogMessage(true, index));
            } else if (index < 0) {
                mView.invalidate();
                showAlertDialog(getDialogMessage(false, -index));
            } else {
                mView.invalidate();
            }
        }

        /**
         * This method is called to schedule a message to be sent to this handler with a delay of
         * {@code delayMillis}, which will cause our {@code handleMessage} method to be called in
         * the sweet by and by. It is used in the {@code onCreate} method of {@code PurgeableBitmap}
         * with a delay of 0 to start the ball rolling, and then in the {@code update} method of
         * {@code PurgeableBitmapView} with a delay of 100 when there are more bitmaps yet to be
         * loaded.
         *
         * First we remove all messages with the field {@code what} set to 0, then we call the method
         * {@code sendMessageDelayed} with a {@code Message} whose field {@code what} is set to 0,
         * specifying a delay of our parameter {@code delayMillis} milliseconds.
         *
         * @param delayMillis delay in milliseconds to send message to ourselves.
         */
        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }

    /**
     * Called when the activity is starting. First we call though to our super's implementation of
     * {@code onCreate}, then we initialize our field {@code PurgeableBitmapView mView} with a new
     * instance of {@code PurgeableBitmapView}. We pass its constructor the return value of our method
     * {@code detectIfPurgeableRequest} which queries the {@code PackageManager} to determine if the
     * textual label associated with our activity in the AndroidManifest was "Purgeable", returning
     * true if so. {@code PurgeableBitmapView} sets the {@code inPurgeable} field of the options used
     * to create its bitmaps to that value (it is ignored since Lollipop). We then call the {@code sleep}
     * method of our field {@code RefreshHandler mRedrawHandler} with a delay of 0 to start the loading
     * of bitmaps by {@code mView}. Finally we set our content view to {@code mView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = new PurgeableBitmapView(this, detectIfPurgeableRequest());
        mRedrawHandler.sleep(0);
        setContentView(mView);
    }

    /**
     * Called to determine if the user started us using a "Purgeable" label, or "NonPurgeable" label.
     * First we retrieve an instance of {@code PackageManager} to {@code PackageManager pm}, and use
     * it to retrieve to {@code ActivityInfo info} the metaData data Bundles that are associated with
     * the component name of this activity. From {@code info} we load the textual label associated with
     * the activity into {@code CharSequence labelSeq}. We slit {@code labelSeq} using "/" as the
     * delimiter into {@code String[] components}. Then if the last string in {@code components} is
     * "Purgeable" we return true, otherwise we return false.
     *
     * @return true if the android:label in the AndroidManifest associated with this instance of
     * {@code PurgeableBitmap} ended in the string "Purgeable", false otherwise (an activity-alias
     * element uses an android:label that ends in "Purgeable", but the android:targetActivity activity
     * element uses an android:label that ends in "NonPurgeable" and both elements can be used to
     * start this activity).
     */
    private boolean detectIfPurgeableRequest() {
        PackageManager pm = getPackageManager();
        @SuppressWarnings("UnusedAssignment")
        CharSequence labelSeq = null;
        try {
            ActivityInfo info = pm.getActivityInfo(this.getComponentName(), PackageManager.GET_META_DATA);
            labelSeq = info.loadLabel(pm);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        String[] components = labelSeq.toString().split("/");
        //noinspection RedundantIfStatement
        if (components[components.length - 1].equals("Purgeable")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Generates a {@code String} summarising the results of the loading of bitmaps by our instance
     * of {@code PurgeableBitmapView}. We create a new instance of {@code StringBuilder} for
     * {@code StringBuilder sb}, then if our parameter {@code isOutOfMemory} is true we build in
     * {@code sb} a string stating when the out of memory occurs. Otherwise we build a string stating
     * the complete decoding occurred, and how many bitmaps were loaded. Finally we return the string
     * value of {@code sb} to the caller.
     *
     * @param isOutOfMemory flag to indicate that {@code PurgeableBitmapView} ran out of memory.
     * @param index Number of bitmaps successfully loaded
     * @return {@code String} describing what happened, suitable for use in a dialog.
     */
    private String getDialogMessage(boolean isOutOfMemory, int index) {
        StringBuilder sb = new StringBuilder();
        if (isOutOfMemory) {
            sb.append("Out of memory occurs when the ");
            sb.append(index);
            sb.append("th Bitmap is decoded.");
        } else {
            sb.append("Complete decoding ")
                    .append(index)
                    .append(" bitmaps without running out of memory.");
        }
        return sb.toString();
    }

    /**
     * Builds and shows an {@code AlertDialog} to display its string parameter.
     *
     * @param message string to display in our {@code AlertDialog}
     */
    private void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


}
