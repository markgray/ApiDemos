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
package com.example.android.apis.content

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Shows how to use the external storage directory api for both public and app private directories.
 */
class ExternalStorage : AppCompatActivity() {
    /**
     * The [LinearLayout] R.id.layout inside of our layout file R.layout.external_storage into which
     * we add the three "storage controls" (inflated and configured instances of the layout file
     * R.layout.external_storage_item) which we use to exercise the external storage directory api.
     */
    private var mLayout: ViewGroup? = null

    /**
     * Class which is used to hold references to the three important Views (the root view itself, and
     * the two control [Button]'s: CREATE and DELETE) contained in each of our three "storage
     * controls": [mExternalStoragePublicPicture], [mExternalStoragePrivatePicture] and
     * [mExternalStoragePrivateFile].
     */
    class Item {
        var mRoot: View? = null
        var mCreate: Button? = null
        var mDelete: Button? = null
    }

    /**
     * Storage control used to create and delete a picture in the DIRECTORY_PICTURES of the public
     * storage of the device. **Note:** This will not work on Android Q and above.
     */
    var mExternalStoragePublicPicture: Item? = null

    /**
     * Storage control used to create and delete a picture in the DIRECTORY_PICTURES of the private
     * storage of the device (internal to the application, and not visible to the user).
     */
    var mExternalStoragePrivatePicture: Item? = null

    /**
     * Storage control used to create and delete a picture in the root directory of the private
     * storage of the device (internal to the application, and not visible to the user).
     */
    var mExternalStoragePrivateFile: Item? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to R.layout.external_storage (our layout file).
     * We initialize our [ViewGroup] field [mLayout] by locating the [LinearLayout] in our
     * UI with ID R.id.layout. This is the [ViewGroup] into which we will place our three
     * "storage controls". To create these controls we call our method [createStorageControls]
     * which inflates the layout file R.layout.external_storage_item, sets the text of the two
     * [TextView]'s in it to the first two arguments we pass it, locates the two Buttons in
     * the layout and sets their `OnClickListener`'s to the next two parameters. It then
     * returns an [Item] instance which contains references to the inflated layout [View]
     * (field `mRoot`) and the two [Button]'s in `mRoot`, the fields `mCreate` and `mDelete`.
     * The parameters passed to [createStorageControls] for our three controls are as follows:
     *
     *  * mExternalStoragePublicPicture
     *      * Picture: getExternalStoragePublicDirectory
     *      * /storage/emulated/0/Pictures
     *      * Create: an `OnclickListener` which calls our methods [createExternalStoragePublicPicture]
     *      and [updateExternalStorageState]
     *      * Delete: an `OnclickListener` which calls our methods [deleteExternalStoragePublicPicture]
     *      and [updateExternalStorageState]
     *  * mExternalStoragePrivatePicture
     *      * Picture getExternalFilesDir
     *      * /storage/emulated/0/Android/data/com.example.android.apis/files/Pictures
     *      * Create: an `OnclickListener` which calls our methods
     *      [createExternalStoragePrivatePicture] and [updateExternalStorageState]
     *      * Delete: an `OnclickListener` which calls our methods
     *      [deleteExternalStoragePrivatePicture] and [updateExternalStorageState]
     *  * mExternalStoragePrivateFile
     *      * File getExternalFilesDir
     *      * /storage/emulated/0/Android/data/com.example.android.apis/files
     *      * Create: an `OnclickListener` which calls our methods
     *      [createExternalStoragePrivateFile] and [updateExternalStorageState]
     *      * Delete: an `OnclickListener` which calls our methods
     *      [deleteExternalStoragePrivateFile] and [updateExternalStorageState]
     *
     * Upon return from [createStorageControls] after creating each of these controls, we add
     * the [View] in [Item.mRoot] of the [Item] returned to [ViewGroup] field [mLayout].
     * Finally we call our method [startWatchingExternalStorage] which creates and registers
     * [BroadcastReceiver] field [mExternalStorageReceiver] to receive broadcasts about changes in
     * the file system state which require us to call our method `updateExternalStorageState`
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @Suppress("RedundantSamConstructor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.external_storage)
        mLayout = findViewById(R.id.layout)
        @Suppress("DEPRECATION")
        mExternalStoragePublicPicture = createStorageControls(
            "Picture: getExternalStoragePublicDirectory",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            View.OnClickListener {
                createExternalStoragePublicPicture()
                updateExternalStorageState()
            },
            View.OnClickListener {
                deleteExternalStoragePublicPicture()
                updateExternalStorageState()
            })
        mLayout!!.addView(mExternalStoragePublicPicture!!.mRoot)
        mExternalStoragePrivatePicture = createStorageControls(
            "Picture getExternalFilesDir",
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            View.OnClickListener {
                createExternalStoragePrivatePicture()
                updateExternalStorageState()
            },
            View.OnClickListener {
                deleteExternalStoragePrivatePicture()
                updateExternalStorageState()
            })
        mLayout!!.addView(mExternalStoragePrivatePicture!!.mRoot)
        mExternalStoragePrivateFile = createStorageControls(
            "File getExternalFilesDir",
            getExternalFilesDir(null),
            View.OnClickListener {
                createExternalStoragePrivateFile()
                updateExternalStorageState()
            },
            View.OnClickListener {
                deleteExternalStoragePrivateFile()
                updateExternalStorageState()
            })
        mLayout!!.addView(mExternalStoragePrivateFile!!.mRoot)
        startWatchingExternalStorage()
    }

    /**
     * Perform any final cleanup before an activity is destroyed. First we call through to our
     * super's implementation of `onDestroy`, then we call our method [stopWatchingExternalStorage]
     * which unregisters the [BroadcastReceiver] field [mExternalStorageReceiver].
     */
    override fun onDestroy() {
        super.onDestroy()
        stopWatchingExternalStorage()
    }

    /**
     * We are called only from our method [updateExternalStorageState] to update the state of the
     * [Button]'s in our three "storage controls" to be enabled or disabled based on the state of
     * the storage system. First we call our method [hasExternalStoragePublicPicture] which checks
     * to see if the file we want to write to in the external storage public picture directory
     * already exists and returns *true* if it does to set [Boolean] `var has`. Then we enable the
     * "CREATE" Button of the "Picture getExternalPublicDirectory" controls if the file system is
     * [writeable] and there is no picture already there (ie: `!has`), and then we enable the "DELETE"
     * [Button] if the file system is [writeable] and there **is** a picture already there.
     *
     * We do much the same thing for the other two "storage controls", except for them we set
     * `has` by calling the methods [hasExternalStoragePrivatePicture] and
     * [hasExternalStoragePrivateFile] respectively.
     *
     * @param available Unused
     * @param writeable *true* if we have permission to write to external storage ie. the system
     * method `getExternalStorageState` returns MEDIA_MOUNTED
     */
    @Suppress("UNUSED_PARAMETER")
    fun handleExternalStorageState(available: Boolean, writeable: Boolean) {
        var has = hasExternalStoragePublicPicture()
        mExternalStoragePublicPicture!!.mCreate!!.isEnabled = writeable && !has
        mExternalStoragePublicPicture!!.mDelete!!.isEnabled = writeable && has
        has = hasExternalStoragePrivatePicture()
        mExternalStoragePrivatePicture!!.mCreate!!.isEnabled = writeable && !has
        mExternalStoragePrivatePicture!!.mDelete!!.isEnabled = writeable && has
        has = hasExternalStoragePrivateFile()
        mExternalStoragePrivateFile!!.mCreate!!.isEnabled = writeable && !has
        mExternalStoragePrivateFile!!.mDelete!!.isEnabled = writeable && has
    }

    /**
     * [BroadcastReceiver] for the actions ACTION_MEDIA_MOUNTED, and ACTION_MEDIA_REMOVED, it
     * just calls our method [updateExternalStorageState] once created.
     */
    private var mExternalStorageReceiver: BroadcastReceiver? = null

    /**
     * External storage is present (but not necessarily writeable). Set to *true* in our method
     * [updateExternalStorageState] if the current state of the primary shared/external storage
     * media returned by `getExternalStorageState` is either MEDIA_MOUNTED or MEDIA_MOUNTED_READ_ONLY.
     * (Otherwise *false*). Used only as the argument to a call to our method
     * [handleExternalStorageState]
     */
    private var mExternalStorageAvailable = false

    /**
     * External storage is present and writeable. Set to *true* in our method
     * [updateExternalStorageState] if the current state of the primary shared/external
     * storage media returned by `getExternalStorageState` is MEDIA_MOUNTED. (Otherwise
     * *false*). Used only as the argument to a call to our method [handleExternalStorageState]
     */
    private var mExternalStorageWriteable = false

    /**
     * Queries the current state of the primary shared/external storage media, setting our [Boolean]
     * fields [mExternalStorageAvailable], and [mExternalStorageWriteable] to reflect that state and
     * then calling our method [handleExternalStorageState] to update the enabled/disabled state of
     * the three "storage controls" in our UI.
     */
    fun updateExternalStorageState() {
        val state = Environment.getExternalStorageState()
        when {
            Environment.MEDIA_MOUNTED == state -> {
                mExternalStorageWriteable = true
                mExternalStorageAvailable = true
            }

            Environment.MEDIA_MOUNTED_READ_ONLY == state -> {
                mExternalStorageAvailable = true
                mExternalStorageWriteable = false
            }

            else -> {
                mExternalStorageWriteable = false
                mExternalStorageAvailable = false
            }
        }
        handleExternalStorageState(mExternalStorageAvailable, mExternalStorageWriteable)
    }

    /**
     * Called from `onCreate`, we initialize our [BroadcastReceiver] field [mExternalStorageReceiver]
     * with an anonymous class whose `onReceive` override calls our method [updateExternalStorageState]
     * whenever it receives a broadcast intent. We create an [IntentFilter] `val filter`, add the
     * actions ACTION_MEDIA_MOUNTED, and ACTION_MEDIA_REMOVED to `filter` and then register
     * [mExternalStorageReceiver] to be run in the main activity thread and called with any broadcast
     * [Intent] that matches `filter`. Finally we call our method [updateExternalStorageState] to
     * update the enabled/disabled state of our "storage controls" Buttons based on the state of the
     * external storage.
     */
    private fun startWatchingExternalStorage() {
        mExternalStorageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.i("test", "Storage: " + intent.data)
                updateExternalStorageState()
            }
        }
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED)
        filter.addAction(Intent.ACTION_MEDIA_REMOVED)
        registerReceiver(mExternalStorageReceiver, filter)
        updateExternalStorageState()
    }

    /**
     * Called from our [onDestroy] override as part of the cleanup before our activity is destroyed,
     * we just unregister our previously registered [BroadcastReceiver] field [mExternalStorageReceiver].
     * All filters that have been registered for this [BroadcastReceiver] will be removed.
     */
    private fun stopWatchingExternalStorage() {
        unregisterReceiver(mExternalStorageReceiver)
    }

    /**
     * Called when the "CREATE" [Button] of the "Picture: getExternalStoragePublicDirectory" "storage
     * control" is clicked, it creates a "DemoPicture.jpg" in the standard directory in which to
     * place pictures that are available to the user: DIRECTORY_PICTURES. First we initiailize [File]
     * variable `val  path` to the top-level shared/external storage directory for placing files of
     * the type DIRECTORY_PICTURES (/storage/emulated/0/Pictures on my Pixel). Then we create a
     * [File] `val file` using `path` as the directory path, and "DemoPicture.jpg" as the name of
     * the file.
     *
     * Next, wrapped in a try block intended to catch [IOException] in order to log and [Toast] any
     * IO error we use `path`'s method `mkdirs()` to create the directory named by this abstract
     * pathname (if necessary), including any necessary but nonexistent parent directories. Then we
     * create [InputStream] `val inputStream` opening a data stream for reading the raw resource
     * R.raw.balloons located in our apk. Now we create [OutputStream] `val os`, a file output stream
     * to write to the file represented by the specified [File] `file` object, allocate [Byte] array
     * `var data` to have room for the number of bytes that can be read  from `inputStream`, read all
     * of `inputStream` into `data`, write all of `data` to `os` and then close both `is` and `os`.
     *
     * Finally we call [MediaScannerConnection.scanFile] to tell the media scanner about the new
     * file so that it is immediately available to the user. **Note:** Running on Android Q and
     * above will just throw an [IOException]: "EACCES (Permission denied)"
     */
    private fun createExternalStoragePublicPicture() {
        /**
         * Create a path where we will place our picture in the user's public pictures directory.
         * Note that you should be careful about what you place here, since the user often manages
         * these files. For pictures and other media owned by the application, consider
         * Context.getExternalMediaDirs().
         */
        @Suppress("DEPRECATION")
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val file = File(path, "DemoPicture.jpg")
        try {
            /**
             * Make sure the Pictures directory exists.
             */
            path.mkdirs()
            /**
             * Very simple code to copy a picture from the application's resource into the external
             * file. Note that this code does no error checking, and assumes the picture is small
             * (does not try to copy it in chunks). Note that if external storage is not currently
             * mounted or the device is running Anroid Q and above this will silently fail.
             */
            val inputStream: InputStream = resources.openRawResource(R.raw.balloons)
            val os: OutputStream = FileOutputStream(file)
            val data = ByteArray(inputStream.available())
            inputStream.read(data)
            os.write(data)
            inputStream.close()
            os.close()
            /**
             * Tell the media scanner about the new file so
             * that it is immediately available to the user.
             */
            MediaScannerConnection.scanFile(
                this,
                arrayOf(file.toString()),
                null
            ) { pathScanned, uri ->
                /**
                 * Called to notify the client when the media scanner has finished
                 * scanning a file. We simply log the information contained in our
                 * parameters.
                 *
                 * @param pathScanned the path to the file that has been scanned.
                 * @param uri  the Uri for the file if the scanning operation succeeded
                 * and the file was added to the media database, or null if
                 * scanning failed.
                 */
                Log.i("ExternalStorage", "Scanned $pathScanned:")
                Log.i("ExternalStorage", "-> uri=$uri")
            }
        } catch (e: IOException) {
            /**
             * Unable to create file, likely because external storage is not currently mounted, we
             * forgot to ask the user for permission to write to external storage, or the device is
             * running Android Q and above.
             */
            Log.w("ExternalStorage", "Error writing $file", e)
            Toast.makeText(
                this,
                this.getString(R.string.public_storage_failure),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Called when the "DELETE" Button of the "Picture: getExternalStoragePublicDirectory" "storage
     * control" is clicked, it deletes "DemoPicture.jpg" from the standard directory in which to
     * place pictures that are available to the user: DIRECTORY_PICTURES. First we initialize [File]
     * variable `val path` to the top-level shared/external storage directory for placing files of
     * the type DIRECTORY_PICTURES (/storage/emulated/0/Pictures on my Pixel). Then we create a [File]
     * `val file` using `path` as the directory path, and "DemoPicture.jpg" as the name of the file.
     * Finally we delete the file denoted by the pathname `file`.
     */
    private fun deleteExternalStoragePublicPicture() {
        /**
         * Create a path where we will place our picture in the user's public pictures directory
         * and delete the file. If external storage is not currently mounted this will fail.
         */
        @Suppress("DEPRECATION")
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val file = File(path, "DemoPicture.jpg")
        file.delete()
    }

    /**
     * Called from our [handleExternalStorageState] method to determine whether the file we want
     * our "Picture: getExternalStoragePublicDirectory" storage control "CREATE" [Button] to create
     * already exists, and if so the "CREATE" [Button] will be disabled and the "DELETE" [Button]
     * enabled. First we initialize [File] `val path` to the top-level shared/external storage
     * directory for placing files of the type DIRECTORY_PICTURES (/storage/emulated/0/Pictures on
     * my Pixel). Then we create a [File] `val file` using `path` as the directory path, and
     * "DemoPicture.jpg" as the name of the file. Finally we return *true* if and only if the file
     * or directory denoted by abstract pathname `file` exists; *false* otherwise. **Note:** This
     * will work even if the app lacks the permission to do anything with the file.
     *
     * @return *true* if the file "DemoPicture.jpg" exists already in the user's public
     * pictures directory
     */
    private fun hasExternalStoragePublicPicture(): Boolean {
        /**
         * Create a path where we will place our picture in the user's public pictures directory
         * and check if the file exists. If external storage is not currently mounted this will
         * think the picture doesn't exist.
         */
        @Suppress("DEPRECATION")
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val file = File(path, "DemoPicture.jpg")
        return file.exists()
    }

    /**
     * Called when the "CREATE" [Button] of the "Picture getExternalFilesDir" storage control is
     * clicked, it creates a "DemoPicture.jpg" in the directory where the application can place
     * persistent files of type DIRECTORY_PICTURES that it owns. These files are internal to the
     * application. First we initialize [File] `val path` to the path to the directory on the
     * primary shared/external storage device where the application can place persistent files it
     * owns of type DIRECTORY_PICTURES
     * ("/storage/emulated/0/Android/data/com.example.android.apis/files/Pictures" on my Pixel).
     * Then we create a [File] `val file` using `path` as the directory path, and "DemoPicture.jpg"
     * as the name of the file.
     *
     * Next, wrapped in a try block intended to catch [IOException] and log the error, we use `path`'s
     * method `mkdirs()` to create the directory named by this abstract pathname (if necessary),
     * including any necessary but nonexistent parent directories. Then we create [InputStream]
     * `val inputStream` opening a data stream for reading the raw resource R.raw.balloons located
     * in our apk. Now we create [OutputStream] `val os`, a file output stream to write to the file
     * represented by the specified [File] `file` object, allocate [Byte] array `val data` to have
     * room for the number of bytes that can be read that can be read from `inputStream`, read all
     * of `inputStream` into `data`, write all of `data` to `os` and then close both `inputStream`
     * and `os`.
     *
     * Finally we call [MediaScannerConnection.scanFile] to tell the media scanner about the new
     * file so that it is immediately available to the user.
     */
    private fun createExternalStoragePrivatePicture() {
        /**
         * Create a path where we will place our picture in our own private
         * pictures directory.  Note that we don't really need to place a
         * picture in DIRECTORY_PICTURES, since the media scanner will see
         * all media in these directories; this may be useful with other
         * media types such as DIRECTORY_MUSIC however to help it classify
         * your media for display to the user.
         */
        val path = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(path, "DemoPicture.jpg")
        try {
            /**
             * Very simple code to copy a picture from the application's
             * resource into the external file. Note that this code does
             * no error checking, and assumes the picture is small (does not
             * try to copy it in chunks). Note that if external storage is
             * not currently mounted this will silently fail.
             */
            val inputStream = resources.openRawResource(R.raw.balloons)
            val os: OutputStream = FileOutputStream(file)
            val data = ByteArray(inputStream.available())
            inputStream.read(data)
            os.write(data)
            inputStream.close()
            os.close()
            /**
             * Tell the media scanner about the new file so
             * that it is immediately available to the user.
             */
            MediaScannerConnection.scanFile(
                this, arrayOf(file.toString()), null
            ) { pathScanned, uri ->
                /**
                 * Called to notify the client when the media scanner has finished
                 * scanning a file. We simply log the information contained in our
                 * parameters.
                 *
                 * @param pathScanned the path to the file that has been scanned.
                 * @param uri  the Uri for the file if the scanning operation succeeded
                 * and the file was added to the media database, or null if
                 * scanning failed.
                 */
                Log.i("ExternalStorage", "Scanned $pathScanned:")
                Log.i("ExternalStorage", "-> uri=$uri")
            }
        } catch (e: IOException) {
            /**
             * Unable to create file, likely because external storage is not currently mounted.
             */
            Log.w("ExternalStorage", "Error writing $file", e)
        }
    }

    /**
     * Called when the "DELETE" [Button] of the "Picture getExternalFilesDir" storage control is
     * clicked, it deletes "DemoPicture.jpg" from the directory where the application can place
     * persistent files of type DIRECTORY_PICTURES that it owns. First we set [File] `val path` to
     * the path to the directory where the application can place persistent files it owns of type
     * DIRECTORY_PICTURES ("/storage/emulated/0/Android/data/com.example.android.apis/files/Pictures"
     * on my Pixel). Then we create a [File] `val file` using `path` as the directory path, and
     * "DemoPicture.jpg" as the name of the file. Finally we delete the file denoted by the pathname
     * [File] `file`.
     */
    private fun deleteExternalStoragePrivatePicture() {
        /**
         * Create a path where we will place our picture in the user's
         * public pictures directory and delete the file If external
         * storage is not currently mounted this will fail.
         */
        val path = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (path != null) {
            val file = File(path, "DemoPicture.jpg")
            file.delete()
        }
    }

    /**
     * Called from our [handleExternalStorageState] method to determine whether the file we want our
     * "Picture getExternalFilesDir" storage control "CREATE" [Button] to create already exists, and
     * if so the "CREATE" [Button] will be disabled and the "DELETE" [Button] enabled. First we set
     * [File] `val path` the directory where the application can place persistent files it owns of
     * type DIRECTORY_PICTURES ("/storage/emulated/0/Android/data/com.example.android.apis/files/Pictures"
     * on my Pixel). Then we create a [File] `val file` using `path` as the directory path, and
     * "DemoPicture.jpg" as the name of the file. Finally we return *true* if and only if the file
     * or directorydenoted by abstract pathname `file` exists; *false* otherwise.
     *
     * @return *true* if the file "DemoPicture.jpg" exists already in the applications persistent
     * directory for files of type DIRECTORY_PICTURES
     */
    private fun hasExternalStoragePrivatePicture(): Boolean {
        /**
         * Create a path where we will place our picture in the user's
         * public pictures directory and check if the file exists. If
         * external storage is not currently mounted this will think the
         * picture doesn't exist.
         */
        val path = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (path != null) {
            val file = File(path, "DemoPicture.jpg")
            return file.exists()
        }
        return false
    }

    /**
     * Called when the "CREATE" Button of the "File getExternalFilesDir" storage control is clicked,
     * it creates a "DemoPicture.jpg" in the base directory where the application can place persistent
     * files that it owns. These files are internal to the application. First we create a [File]
     * `val file` using the absolute path to the directory on the primary shared/external storage
     * device where the application can place persistent files it owns as the directory path,
     * ("/storage/emulated/0/Android/data/com.example.android.apis/files" on my Pixel) and
     * "DemoPicture.jpg" as the name of the file.
     *
     * Next, wrapped in a *try* block intended to catch and log [IOException], we create [InputStream]
     * `val inputStream` opening a data stream for reading the raw resource R.raw.balloons located
     * in our apk. Now we create [OutputStream] `val os`, a file output stream to write to the file
     * represented by the specified `file` [File], allocate [Byte] array `val data` to have room for
     * the number of bytes that can be read from `inputStream`, read all of `inputStream` into `data`,
     * write all of `data` to `os` and then close both `inputStream` and `os`.
     */
    private fun createExternalStoragePrivateFile() {
        /**
         * Create a path where we will place our private file on external storage.
         */
        val file = File(getExternalFilesDir(null), "DemoFile.jpg")
        try {
            /**
             * Very simple code to copy a picture from the application's
             * resource into the external file.  Note that this code does
             * no error checking, and assumes the picture is small (does not
             * try to copy it in chunks).  Note that if external storage is
             * not currently mounted this will silently fail.
             */
            val inputStream = resources.openRawResource(R.raw.balloons)
            val os: OutputStream = FileOutputStream(file)
            val data = ByteArray(inputStream.available())
            inputStream.read(data)
            os.write(data)
            inputStream.close()
            os.close()
        } catch (e: IOException) {
            /**
             * Unable to create file, likely because external storage is not currently mounted.
             */
            Log.w("ExternalStorage", "Error writing $file", e)
        }
    }

    /**
     * Called when the "DELETE" Button of the "File getExternalFilesDir" storage control is clicked,
     * it deletes "DemoPicture.jpg" from the directory where the application can place persistent files
     * that it owns. First we create a `File file` using the absolute path to the directory on
     * the primary shared/external storage device where the application can place persistent files it
     * owns as the directory path, ("/storage/emulated/0/Android/data/com.example.android.apis/files"
     * on my Nexus 6) and "DemoPicture.jpg" as the name of the file. Finally we delete the file denoted
     * by the pathname `File file`.
     */
    private fun deleteExternalStoragePrivateFile() {
        /**
         * Get path for the file on external storage. If external
         * storage is not currently mounted this will fail.
         */
        val file = File(getExternalFilesDir(null), "DemoFile.jpg")
        @Suppress("UNNECESSARY_SAFE_CALL")
        file?.delete()
    }

    /**
     * Called from our [handleExternalStorageState] method to determine whether the file we want
     * our "File getExternalFilesDir" storage control "CREATE" Button to create already exists, and
     * if so the "CREATE" Button will be disabled and the "DELETE" Button enabled. First we create a
     * [File] `val file` using the absolute path to the directory on the primary shared/external
     * storage device where the application can place persistent files it owns as the directory path,
     * ("/storage/emulated/0/Android/data/com.example.android.apis/files" on my Pixel) and
     * "DemoPicture.jpg" as the name of the file. Finally we return *true* if and only if the file or
     * directory denoted by abstract pathname `file` exists; false otherwise.
     *
     * @return *true* if the file "DemoPicture.jpg" exists already in the applications persistent directory
     * for files, otherwise *false*.
     */
    private fun hasExternalStoragePrivateFile(): Boolean {
        /**
         * Get path for the file on external storage. If external
         * storage is not currently mounted this will fail.
         */
        val file = File(getExternalFilesDir(null), "DemoFile.jpg")
        @Suppress("UNNECESSARY_SAFE_CALL", "USELESS_ELVIS")
        return file?.exists() ?: false
    }

    /**
     * Inflates the layout file R.layout.external_storage_item, configures that [View], then
     * creates and returns an [Item] instance containing references to the important [View]'s
     * in that [View]. First we fetch a handle to the system level service LAYOUT_INFLATER_SERVICE
     * to initialize [LayoutInflater] `val inflater`. We create a new instance of [Item] `val item`,
     * and set its field `mRoot` to the [View] that results when we inflate the layout file
     * R.layout.external_storage_item. We locate the [TextView] `val tv` with ID R.id.label in the
     * [View] `item.mRoot` and set its text to our [CharSequence] parameter [label], and if our
     * [path] parameter is not *null*, we locate the [TextView] with ID R.id.path and set its text
     * to the pathname string of our [File] parameter [path]. We set the field `item.mCreate` to the
     * [Button] with ID R.id.create, and set its `OnClickListener` to our [View.OnClickListener]
     * parameter  [createClick]. We set the field `item.mDelete` to the [Button] with ID R.id.delete,
     * and set its [View.OnClickListener] to our [View.OnClickListener] parameter [deleteClick].
     * Finally we return [Item] `item` to the caller.
     *
     * @param label       [String] to use for the label of the R.id.label [TextView] of the layout
     * @param path        [File] to convert to a [String] to display in the R.id.path [TextView]
     * of the layout
     * @param createClick [View.OnClickListener] for the "CREATE" Button
     * @param deleteClick [View.OnClickListener] for the "DELETE" Button
     * @return An [Item] instance containing references to the [View] inflated from the layout file
     * R.layout.external_storage_item (field `mRoot`), the "CREATE" [Button] R.id.create in the [View]
     * `mRoot` (field `mCreate`), and the "DELETE" [Button] R.id.delete in the [View] `mRoot` (field
     * `mDelete`).
     */
    private fun createStorageControls(
        label: CharSequence?,
        path: File?,
        createClick: View.OnClickListener?,
        deleteClick: View.OnClickListener?
    ): Item {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val item = Item()
        item.mRoot = inflater.inflate(R.layout.external_storage_item, mLayout, false)
        var tv = item.mRoot!!.findViewById<TextView>(R.id.label)
        tv.text = label
        if (path != null) {
            tv = item.mRoot!!.findViewById(R.id.path)
            tv.text = path.toString()
        }
        item.mCreate = item.mRoot!!.findViewById(R.id.create)
        item.mCreate!!.setOnClickListener(createClick)
        item.mDelete = item.mRoot!!.findViewById(R.id.delete)
        item.mDelete!!.setOnClickListener(deleteClick)
        return item
    }
}