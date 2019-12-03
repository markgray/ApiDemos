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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

//Need the following import to get access to the app resources, since this
//class is in a sub-package.
import com.example.android.apis.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Shows how to use the external storage directory api for both public and app private directories.
 */
public class ExternalStorage extends AppCompatActivity {
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
        mLayout = findViewById(R.id.layout);

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
     * state of the storage system. First we call our method {@code hasExternalStoragePublicPicture}
     * which checks to see if the file we want to write to in the external storage public picture
     * directory already exists and returns true if it does to set {@code boolean has}. Then we
     * enable the "CREATE" Button of the "Picture getExternalPublicDirectory" controls if the file
     * system is {@code writeable} and there is no picture already there ({@code !has}), and then
     * we enable the "DELETE" Button if the file system is {@code writeable} and there <b>is</b> a
     * picture already there.
     * <p>
     * We do much the same thing for the other two "storage controls", except for them we set
     * {@code boolean has} by calling the methods {@code hasExternalStoragePrivatePicture} and
     * {@code hasExternalStoragePrivateFile} respectively.
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

    /**
     * {@code BroadcastReceiver} for the actions ACTION_MEDIA_MOUNTED, and ACTION_MEDIA_REMOVED, it
     * just calls our method {@code updateExternalStorageState}
     */
    BroadcastReceiver mExternalStorageReceiver;
    /**
     * External storage is present (but not necessarily writeable). Set to true in our method
     * {@code updateExternalStorageState} if the current state of the primary shared/external
     * storage media returned by {@code getExternalStorageState} is either MEDIA_MOUNTED or
     * MEDIA_MOUNTED_READ_ONLY. (Otherwise false). Used only as the argument to a call to our
     * method {@code handleExternalStorageState}
     */
    boolean mExternalStorageAvailable = false;
    /**
     * External storage is present and writeable. Set to true in our method
     * {@code updateExternalStorageState} if the current state of the primary shared/external
     * storage media returned by {@code getExternalStorageState} is MEDIA_MOUNTED. (Otherwise
     * false). Used only as the argument to a call to our method {@code handleExternalStorageState}
     */
    boolean mExternalStorageWriteable = false;

    /**
     * Queries the current state of the primary shared/external storage media, setting our fields
     * {@code boolean mExternalStorageAvailable}, and {@code boolean mExternalStorageWriteable} to
     * reflect that state and then calling our method {@code handleExternalStorageState} to update
     * the enabled/disabled state of the three "storage controls" in our UI.
     */
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

    /**
     * Called from {@code onCreate}, we initialize our field {@code BroadcastReceiver mExternalStorageReceiver}
     * with an anonymous class whose {@code onReceive} override calls our method {@code updateExternalStorageState}
     * whenever it receives a broadcast intent. We create an {@code IntentFilter filter}, add the actions
     * ACTION_MEDIA_MOUNTED, and ACTION_MEDIA_REMOVED to {@code filter} and then register {@code mExternalStorageReceiver}
     * to be run in the main activity thread and called with any broadcast Intent that matches {@code filter}.
     * Finally we call our method {@code updateExternalStorageState} to update the enabled/disabled state
     * of our "storage controls" Buttons based on the state of the external storage.
     */
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

    /**
     * Called from our {@code onDestroy} override as part of the cleanup before our activity is
     * destroyed, we just unregister our previously registered {@code BroadcastReceiver mExternalStorageReceiver}.
     * All filters that have been registered for this {@code BroadcastReceiver} will be removed.
     */
    void stopWatchingExternalStorage() {
        unregisterReceiver(mExternalStorageReceiver);
    }

    /**
     * Called when the "CREATE" Button of the "Picture: getExternalStoragePublicDirectory" "storage
     * control" is clicked, it creates a "DemoPicture.jpg" in the standard directory in which to
     * place pictures that are available to the user: DIRECTORY_PICTURES. First we set {@code File path}
     * to the top-level shared/external storage directory for placing files of the type DIRECTORY_PICTURES
     * (/storage/emulated/0/Pictures on my Nexus 6). Then we create a {@code File file} using {@code path}
     * as the directory path, and "DemoPicture.jpg" as the name of the file.
     * <p>
     * Next, wrapped in a try block intended to catch IOException we use {@code path}'s method {@code mkdirs()}
     * to create the directory named by this abstract pathname (if necessary), including any necessary
     * but nonexistent parent directories. Then we create {@code InputStream is} opening a data stream for
     * reading the raw resource R.raw.balloons located in our apk. Now we create {@code OutputStream os},
     * a file output stream to write to the file represented by the specified {@code File file} object,
     * allocate {@code byte[] data} to have room for the number of bytes that can be read that can be read
     * from {@code is}, read all of {@code is} into {@code data}, write all of {@code data} to {@code os}
     * and then close both {@code is} and {@code os}.
     * <p>
     * Finally we call {@code MediaScannerConnection.scanFile} to Tell the media scanner about the
     * new file so that it is immediately available to the user.
     */
    void createExternalStoragePublicPicture() {
        // Create a path where we will place our picture in the user's
        // public pictures directory.  Note that you should be careful about
        // what you place here, since the user often manages these files.  For
        // pictures and other media owned by the application, consider
        // Context.getExternalMediaDirs().
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
                        /**
                         * Called to notify the client when the media scanner has finished
                         * scanning a file. We simply log the information contained in our
                         * parameters.
                         *
                         * @param path the path to the file that has been scanned.
                         * @param uri  the Uri for the file if the scanning operation succeeded
                         *             and the file was added to the media database, or null if
                         *             scanning failed.
                         */
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing " + file, e);
            Toast.makeText(
                    this,
                    this.getString(R.string.public_storage_failure),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    /**
     * Called when the "DELETE" Button of the "Picture: getExternalStoragePublicDirectory" "storage
     * control" is clicked, it deletes "DemoPicture.jpg" from the standard directory in which to
     * place pictures that are available to the user: DIRECTORY_PICTURES. First we set {@code File path}
     * to the top-level shared/external storage directory for placing files of the type DIRECTORY_PICTURES
     * (/storage/emulated/0/Pictures on my Nexus 6). Then we create a {@code File file} using {@code path}
     * as the directory path, and "DemoPicture.jpg" as the name of the file. Finally we delete the file
     * denoted by the pathname {@code File file}.
     */
    void deleteExternalStoragePublicPicture() {
        // Create a path where we will place our picture in the user's
        // public pictures directory and delete the file.  If external
        // storage is not currently mounted this will fail.
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, "DemoPicture.jpg");
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    /**
     * Called from our {@code handleExternalStorageState} method to determine whether the file we want
     * our "Picture: getExternalStoragePublicDirectory" storage control "CREATE" Button to create
     * already exists, and if so the "CREATE" Button will be disabled and the "DELETE" Button enabled.
     * First we set {@code File path} to the top-level shared/external storage directory for placing
     * files of the type DIRECTORY_PICTURES (/storage/emulated/0/Pictures on my Nexus 6). Then we
     * create a {@code File file} using {@code path} as the directory path, and "DemoPicture.jpg"
     * as the name of the file. Finally we return true if and only if the file or directory denoted
     * by abstract pathname {@code file} exists; false otherwise.
     *
     * @return true if the file "DemoPicture.jpg" exists already in the user's public pictures directory
     */
    boolean hasExternalStoragePublicPicture() {
        // Create a path where we will place our picture in the user's
        // public pictures directory and check if the file exists.  If
        // external storage is not currently mounted this will think the
        // picture doesn't exist.
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, "DemoPicture.jpg");
        return file.exists();
    }

    /**
     * Called when the "CREATE" Button of the "Picture getExternalFilesDir" storage control is clicked,
     * it creates a "DemoPicture.jpg" in the directory where the application can place persistent files
     * of type DIRECTORY_PICTURES that it owns. These files are internal to the application.
     * First we set {@code File path} to the path to the directory on the primary shared/external
     * storage device where the application can place persistent files it owns of type DIRECTORY_PICTURES
     * ("/storage/emulated/0/Android/data/com.example.android.apis/files/Pictures" on my Nexus 6).
     * Then we create a {@code File file} using {@code path} as the directory path, and "DemoPicture.jpg"
     * as the name of the file.
     * <p>
     * Next, wrapped in a try block intended to catch IOException we use {@code path}'s method {@code mkdirs()}
     * to create the directory named by this abstract pathname (if necessary), including any necessary
     * but nonexistent parent directories. Then we create {@code InputStream is} opening a data stream for
     * reading the raw resource R.raw.balloons located in our apk. Now we create {@code OutputStream os},
     * a file output stream to write to the file represented by the specified {@code File file} object,
     * allocate {@code byte[] data} to have room for the number of bytes that can be read that can be read
     * from {@code is}, read all of {@code is} into {@code data}, write all of {@code data} to {@code os}
     * and then close both {@code is} and {@code os}.
     * <p>
     * Finally we call {@code MediaScannerConnection.scanFile} to Tell the media scanner about the
     * new file so that it is immediately available to the user.
     */
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
                        /**
                         * Called to notify the client when the media scanner has finished
                         * scanning a file. We simply log the information contained in our
                         * parameters.
                         *
                         * @param path the path to the file that has been scanned.
                         * @param uri  the Uri for the file if the scanning operation succeeded
                         *             and the file was added to the media database, or null if
                         *             scanning failed.
                         */
                        @Override
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

    /**
     * Called when the "DELETE" Button of the "Picture getExternalFilesDir" storage control is clicked,
     * it deletes "DemoPicture.jpg" from the directory where the application can place persistent files
     * of type DIRECTORY_PICTURES that it owns. First we set {@code File path} to the path to the
     * directory where the application can place persistent files it owns of type DIRECTORY_PICTURES
     * ("/storage/emulated/0/Android/data/com.example.android.apis/files/Pictures" on my Nexus 6).
     * Then we create a {@code File file} using {@code path} as the directory path, and "DemoPicture.jpg"
     * as the name of the file. Finally we delete the file denoted by the pathname {@code File file}.
     */
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

    /**
     * Called from our {@code handleExternalStorageState} method to determine whether the file we want
     * our "Picture getExternalFilesDir" storage control "CREATE" Button to create already exists, and
     * if so the "CREATE" Button will be disabled and the "DELETE" Button enabled. First we set
     * {@code File path} the directory where the application can place persistent files it owns of
     * type DIRECTORY_PICTURES ("/storage/emulated/0/Android/data/com.example.android.apis/files/Pictures"
     * on my Nexus 6). Then we create a {@code File file} using {@code path} as the directory path, and
     * "DemoPicture.jpg" as the name of the file. Finally we return true if and only if the file or directory
     * denoted by abstract pathname {@code file} exists; false otherwise.
     *
     * @return true if the file "DemoPicture.jpg" exists already in the applications persistent directory
     * for files of type DIRECTORY_PICTURES
     */
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

    /**
     * Called when the "CREATE" Button of the "File getExternalFilesDir" storage control is clicked,
     * it creates a "DemoPicture.jpg" in the base directory where the application can place persistent
     * files that it owns. These files are internal to the application. First we create a
     * {@code File file} using the absolute path to the directory on the primary shared/external
     * storage device where the application can place persistent files it owns as the directory path,
     * ("/storage/emulated/0/Android/data/com.example.android.apis/files" on my Nexus 6) and
     * "DemoPicture.jpg" as the name of the file.
     * <p>
     * Next, wrapped in a try block intended to catch IOException, we create {@code InputStream is}
     * opening a data stream for reading the raw resource R.raw.balloons located in our apk. Now we
     * create {@code OutputStream os}, a file output stream to write to the file represented by the
     * specified {@code File file} object, allocate {@code byte[] data} to have room for the number
     * of bytes that can be read that can be read from {@code is}, read all of {@code is} into
     * {@code data}, write all of {@code data} to {@code os} and then close both {@code is} and
     * {@code os}.
     */
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

    /**
     * Called when the "DELETE" Button of the "File getExternalFilesDir" storage control is clicked,
     * it deletes "DemoPicture.jpg" from the directory where the application can place persistent files
     * that it owns. First we create a {@code File file} using the absolute path to the directory on
     * the primary shared/external storage device where the application can place persistent files it
     * owns as the directory path, ("/storage/emulated/0/Android/data/com.example.android.apis/files"
     * on my Nexus 6) and "DemoPicture.jpg" as the name of the file. Finally we delete the file denoted
     * by the pathname {@code File file}.
     */
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

    /**
     * Called from our {@code handleExternalStorageState} method to determine whether the file we want
     * our "File getExternalFilesDir" storage control "CREATE" Button to create already exists, and
     * if so the "CREATE" Button will be disabled and the "DELETE" Button enabled. First we create a
     * {@code File file} using the absolute path to the directory on the primary shared/external storage
     * device where the application can place persistent files it owns as the directory path,
     * ("/storage/emulated/0/Android/data/com.example.android.apis/files" on my Nexus 6) and
     * "DemoPicture.jpg" as the name of the file. Finally we return true if and only if the file or
     * directory denoted by abstract pathname {@code file} exists; false otherwise.
     *
     * @return true if the file "DemoPicture.jpg" exists already in the applications persistent directory
     * for files
     */
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

    /**
     * Inflates the layout file R.layout.external_storage_item, configures that {@code View}, then
     * creates and returns an {@code Item} instance containing references to the important {@code View}'s
     * in that {@code View}. First we fetch a handle to the system level service LAYOUT_INFLATER_SERVICE
     * to initialize {@code LayoutInflater inflater}. We create a new instance of {@code Item item},
     * and set its field {@code mRoot} to the {@code View} that results when we inflate the layout file
     * R.layout.external_storage_item. We locate the {@code TextView tv} with ID R.id.label in the {@code View}
     * {@code item.mRoot} and set its text to our parameter {@code CharSequence label}, and if {@code path}
     * is not null, we locate the {@code TextView} with ID R.id.path and set its text to the pathname string
     * of our parameter {@code File path}. We set the field {@code item.mCreate} to the Button with ID
     * R.id.create, and set its {@code OnClickListener} to our parameter {@code View.OnClickListener createClick}.
     * We set the field {@code item.mDelete} to the Button with ID R.id.delete, and set its {@code OnClickListener}
     * to our parameter {@code View.OnClickListener deleteClick}. Finally we return {@code Item item} to the
     * caller.
     *
     * @param label       String to use for the label of the R.id.label {@code TextView} of the layout
     * @param path        File to convert to a String to display in the R.id.path {@code TextView} of the layout
     * @param createClick {@code OnClickListener} for the "CREATE" Button
     * @param deleteClick {@code OnClickListener} for the "DELETE" Button
     * @return An {@code Item} instance containing references to the {@code View} inflated from the
     * layout file R.layout.external_storage_item (field {@code mRoot}), the "CREATE" Button R.id.create
     * in the View {@code mRoot} (field {@code mCreate}), and the "DELETE" Button R.id.delete in the View
     * {@code mRoot} (field {@code mDelete}).
     */
    Item createStorageControls(CharSequence label, File path,
                               View.OnClickListener createClick,
                               View.OnClickListener deleteClick) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        Item item = new Item();
        //noinspection ConstantConditions
        item.mRoot = inflater.inflate(R.layout.external_storage_item, mLayout, false);
        TextView tv = item.mRoot.findViewById(R.id.label);
        tv.setText(label);
        if (path != null) {
            tv = item.mRoot.findViewById(R.id.path);
            tv.setText(path.toString());
        }
        item.mCreate = item.mRoot.findViewById(R.id.create);
        item.mCreate.setOnClickListener(createClick);
        item.mDelete = item.mRoot.findViewById(R.id.delete);
        item.mDelete.setOnClickListener(deleteClick);
        return item;
    }
}
