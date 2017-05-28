/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.example.android.apis.content;

//Need the following import to get access to the app resources, since this
//class is in a sub-package.

import com.example.android.apis.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Shows how to use the external storage directory api for both public and app private directories.
 */
public class ExternalStorage extends Activity {
    /**
     * The {@code LinearLayout} R.id.layout inside of our layout file R.layout.external_storage into
     * which we add the three "storage controls" (inflated and configured instances of the layout file
     * R.layout.external_storage_item) which we use to exercise the external storage directory api.
     */
    ViewGroup mLayout;

    /**
     * Class which is used to hold references to the three important Views (the root view itself, and
     * the two control {@code Button}'s: CREATE and DELETE) contained in each of our three "storage
     * controls": {@code mExternalStoragePublicPicture}, {@code mExternalStoragePrivatePicture} and
     * {@code mExternalStoragePrivateFile}.
     */
    static class Item {
        View mRoot;
        Button mCreate;
        Button mDelete;
    }

    /**
     * Storage control used to create and delete a picture in the DIRECTORY_PICTURES of the public
     * storage of the device.
     */
    Item mExternalStoragePublicPicture;
    /**
     * Storage control used to create and delete a picture in the DIRECTORY_PICTURES of the private
     * storage of the device (internal to the application, and not visible to the user).
     */
    Item mExternalStoragePrivatePicture;
    /**
     * Storage control used to create and delete a picture in the root directory of the private
     * storage of the device (internal to the application, and not visible to the user).
     */
    Item mExternalStoragePrivateFile;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to R.layout.external_storage (our layout file).
     * We initialize our field {@code ViewGroup mLayout} by locating the {@code LinearLayout} in our
     * UI with ID R.id.layout. This is the {@code ViewGroup} into which we will place our three
     * "storage controls". To create these controls we call our method {@code createStorageControls}
     * which inflates the layout file R.layout.external_storage_item, sets the text of the two
     * {@code TextView}'s in it to the first two arguments we pass it, locates the two Buttons in
     * the layout and sets their {@code OnClickListener}'s to the next two parameters. It then
     * returns an {@code Item} instance which contains references to the inflated layout {@code View}
     * (field {@code mRoot}) and the two {@code Button}'s in {@code mRoot}, fields {@code mCreate}
     * and {@code mDelete}. The parameters passed to {@code createStorageControls} for our three
     * controls are as follows:
     * <ul>
     * <li>
     * mExternalStoragePublicPicture
     * <ul>
     * <li>Picture: getExternalStoragePublicDirectory</li>
     * <li>/storage/emulated/0/Pictures</li>
     * <li>Create: an {@code OnclickListener} which calls our methods
     * {@code createExternalStoragePublicPicture} and {@code updateExternalStorageState}</li>
     * <li>Delete: an {@code OnclickListener} which calls our methods
     * {@code deleteExternalStoragePublicPicture} and {@code updateExternalStorageState}</li>
     * </ul>
     * </li>
     * <li>
     * mExternalStoragePrivatePicture
     * <ul>
     * <li>Picture getExternalFilesDir</li>
     * <li>/storage/emulated/0/Android/data/com.example.android.apis/files/Pictures</li>
     * <li>Create: an {@code OnclickListener} which calls our methods
     * {@code createExternalStoragePrivatePicture} and {@code updateExternalStorageState}</li>
     * <li>Delete: an {@code OnclickListener} which calls our methods
     * {@code deleteExternalStoragePrivatePicture} and {@code updateExternalStorageState}</li>
     * </ul>
     * </li>
     * <li>
     * mExternalStoragePrivateFile
     * <ul>
     * <li>File getExternalFilesDir</li>
     * <li>/storage/emulated/0/Android/data/com.example.android.apis/files</li>
     * <li>Create: an {@code OnclickListener} which calls our methods
     * {@code createExternalStoragePrivateFile} and {@code updateExternalStorageState}</li>
     * <li>Delete: an {@code OnclickListener} which calls our methods
     * {@code deleteExternalStoragePrivateFile} and {@code updateExternalStorageState}</li>
     * </ul>
     * </li>
     * </ul>
     * Upon return from {@code createStorageControls} after creating each of these controls, we add
     * the {@code View Item.mRoot} of the {@code Item} returned to {@code ViewGroup mLayout}.
     * <p>
     * Finally we call our method {@code startWatchingExternalStorage} which creates and registers
     * {@code BroadcastReceiver mExternalStorageReceiver} to receive broadcasts about changes in
     * the file system state which require us to call our method {@code updateExternalStorageState}
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.external_storage);
        mLayout = (ViewGroup) findViewById(R.id.layout);

        mExternalStoragePublicPicture = createStorageControls(
                "Picture: getExternalStoragePublicDirectory",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                new View.OnClickListener() {
                    public void onClick(View v) {
                        createExternalStoragePublicPicture();
                        updateExternalStorageState();
                    }
                },
                new View.OnClickListener() {
                    public void onClick(View v) {
                        deleteExternalStoragePublicPicture();
                        updateExternalStorageState();
                    }
                });
        mLayout.addView(mExternalStoragePublicPicture.mRoot);

        mExternalStoragePrivatePicture = createStorageControls(
                "Picture getExternalFilesDir",
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                new View.OnClickListener() {
                    public void onClick(View v) {
                        createExternalStoragePrivatePicture();
                        updateExternalStorageState();
                    }
                },
                new View.OnClickListener() {
                    public void onClick(View v) {
                        deleteExternalStoragePrivatePicture();
                        updateExternalStorageState();
                    }
                });
        mLayout.addView(mExternalStoragePrivatePicture.mRoot);

        mExternalStoragePrivateFile = createStorageControls(
                "File getExternalFilesDir",
                getExternalFilesDir(null),
                new View.OnClickListener() {
                    public void onClick(View v) {
                        createExternalStoragePrivateFile();
                        updateExternalStorageState();
                    }
                },
                new View.OnClickListener() {
                    public void onClick(View v) {
                        deleteExternalStoragePrivateFile();
                        updateExternalStorageState();
                    }
                });
        mLayout.addView(mExternalStoragePrivateFile.mRoot);

        startWatchingExternalStorage();
    }

    /**
     * Perform any final cleanup before an activity is destroyed. First we call through to our super's
     * implementation of {@code onDestroy}, then we call our method {@code stopWatchingExternalStorage}
     * which unregisters the receiver {@code BroadcastReceiver mExternalStorageReceiver}.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopWatchingExternalStorage();
    }

    /**
     * We are called only from our method {@code updateExternalStorageState} to update the state of
     * the {@code Button}'s in our three "storage controls" to be enabled or disabled based on the
     * state of the storage system. 
     *
     * @param available Unused
     * @param writeable True if we have permission to write to external storage ie. the system method
     *                  {@code getExternalStorageState} returns MEDIA_MOUNTED
     */
    void handleExternalStorageState(@SuppressWarnings("UnusedParameters") boolean available, boolean writeable) {
        boolean has = hasExternalStoragePublicPicture();
        mExternalStoragePublicPicture.mCreate.setEnabled(writeable && !has);
        mExternalStoragePublicPicture.mDelete.setEnabled(writeable && has);
        has = hasExternalStoragePrivatePicture();
        mExternalStoragePrivatePicture.mCreate.setEnabled(writeable && !has);
        mExternalStoragePrivatePicture.mDelete.setEnabled(writeable && has);
        has = hasExternalStoragePrivateFile();
        mExternalStoragePrivateFile.mCreate.setEnabled(writeable && !has);
        mExternalStoragePrivateFile.mDelete.setEnabled(writeable && has);
    }


    BroadcastReceiver mExternalStorageReceiver;
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;

    void updateExternalStorageState() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        handleExternalStorageState(mExternalStorageAvailable, mExternalStorageWriteable);
    }

    void startWatchingExternalStorage() {
        mExternalStorageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("test", "Storage: " + intent.getData());
                updateExternalStorageState();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        registerReceiver(mExternalStorageReceiver, filter);
        updateExternalStorageState();
    }

    void stopWatchingExternalStorage() {
        unregisterReceiver(mExternalStorageReceiver);
    }


    void createExternalStoragePublicPicture() {
        // Create a path where we will place our picture in the user's
        // public pictures directory.  Note that you should be careful about
        // what you place here, since the user often manages these files.  For
        // pictures and other media owned by the application, consider
        // Context.getExternalMediaDir().
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, "DemoPicture.jpg");

        try {
            // Make sure the Pictures directory exists.
            //noinspection ResultOfMethodCallIgnored
            path.mkdirs();

            // Very simple code to copy a picture from the application's
            // resource into the external file.  Note that this code does
            // no error checking, and assumes the picture is small (does not
            // try to copy it in chunks).  Note that if external storage is
            // not currently mounted this will silently fail.
            InputStream is = getResources().openRawResource(R.raw.balloons);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            //noinspection ResultOfMethodCallIgnored
            is.read(data);
            os.write(data);
            is.close();
            os.close();

            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(this,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing " + file, e);
        }
    }

    void deleteExternalStoragePublicPicture() {
        // Create a path where we will place our picture in the user's
        // public pictures directory and delete the file.  If external
        // storage is not currently mounted this will fail.
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, "DemoPicture.jpg");
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    boolean hasExternalStoragePublicPicture() {
        // Create a path where we will place our picture in the user's
        // public pictures directory and check if the file exists.  If
        // external storage is not currently mounted this will think the
        // picture doesn't exist.
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, "DemoPicture.jpg");
        return file.exists();
    }


    void createExternalStoragePrivatePicture() {
        // Create a path where we will place our picture in our own private
        // pictures directory.  Note that we don't really need to place a
        // picture in DIRECTORY_PICTURES, since the media scanner will see
        // all media in these directories; this may be useful with other
        // media types such as DIRECTORY_MUSIC however to help it classify
        // your media for display to the user.
        File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(path, "DemoPicture.jpg");

        try {
            // Very simple code to copy a picture from the application's
            // resource into the external file.  Note that this code does
            // no error checking, and assumes the picture is small (does not
            // try to copy it in chunks).  Note that if external storage is
            // not currently mounted this will silently fail.
            InputStream is = getResources().openRawResource(R.raw.balloons);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            //noinspection ResultOfMethodCallIgnored
            is.read(data);
            os.write(data);
            is.close();
            os.close();

            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(this,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing " + file, e);
        }
    }

    void deleteExternalStoragePrivatePicture() {
        // Create a path where we will place our picture in the user's
        // public pictures directory and delete the file.  If external
        // storage is not currently mounted this will fail.
        File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (path != null) {
            File file = new File(path, "DemoPicture.jpg");
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    boolean hasExternalStoragePrivatePicture() {
        // Create a path where we will place our picture in the user's
        // public pictures directory and check if the file exists.  If
        // external storage is not currently mounted this will think the
        // picture doesn't exist.
        File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (path != null) {
            File file = new File(path, "DemoPicture.jpg");
            return file.exists();
        }
        return false;
    }


    void createExternalStoragePrivateFile() {
        // Create a path where we will place our private file on external
        // storage.
        File file = new File(getExternalFilesDir(null), "DemoFile.jpg");

        try {
            // Very simple code to copy a picture from the application's
            // resource into the external file.  Note that this code does
            // no error checking, and assumes the picture is small (does not
            // try to copy it in chunks).  Note that if external storage is
            // not currently mounted this will silently fail.
            InputStream is = getResources().openRawResource(R.raw.balloons);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            //noinspection ResultOfMethodCallIgnored
            is.read(data);
            os.write(data);
            is.close();
            os.close();
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing " + file, e);
        }
    }

    void deleteExternalStoragePrivateFile() {
        // Get path for the file on external storage.  If external
        // storage is not currently mounted this will fail.
        File file = new File(getExternalFilesDir(null), "DemoFile.jpg");
        //noinspection ConstantConditions
        if (file != null) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    boolean hasExternalStoragePrivateFile() {
        // Get path for the file on external storage.  If external
        // storage is not currently mounted this will fail.
        File file = new File(getExternalFilesDir(null), "DemoFile.jpg");
        //noinspection SimplifiableIfStatement,ConstantConditions
        if (file != null) {
            return file.exists();
        }
        return false;
    }


    @SuppressLint("InflateParams")
    Item createStorageControls(CharSequence label, File path,
                               View.OnClickListener createClick,
                               View.OnClickListener deleteClick) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        Item item = new Item();
        item.mRoot = inflater.inflate(R.layout.external_storage_item, null);
        TextView tv = (TextView) item.mRoot.findViewById(R.id.label);
        tv.setText(label);
        if (path != null) {
            tv = (TextView) item.mRoot.findViewById(R.id.path);
            tv.setText(path.toString());
        }
        item.mCreate = (Button) item.mRoot.findViewById(R.id.create);
        item.mCreate.setOnClickListener(createClick);
        item.mDelete = (Button) item.mRoot.findViewById(R.id.delete);
        item.mDelete.setOnClickListener(deleteClick);
        return item;
    }
}
