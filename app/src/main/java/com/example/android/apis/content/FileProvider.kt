/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.annotation.SuppressLint
import android.content.ContentProvider
import android.content.ContentProvider.PipeDataWriter
import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * A very simple content provider that can serve arbitrary asset files from our .apk.
 * Used by `ActionBarShareActionProviderActivity`, `ShareContent`, `ContentBrowserActivity`,
 * `ContentBrowserNavActivity`, `SystemUIModes`, `VideoPlayerActivity`.
 */
@SuppressLint("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.HONEYCOMB)
class FileProvider : ContentProvider(), PipeDataWriter<InputStream> {
    /**
     * Implement this to initialize your content provider on startup, we simply return *true*
     *
     * @return *true* -- the provider was successfully loaded,
     */
    override fun onCreate(): Boolean {
        return true
    }

    /**
     * Implement this to handle query requests from clients. We make a copy of our `Array<String>`
     * parameter [projection] to initialize `var projectionLocal`, initialize `var displayNameIndex`
     * and `var sizeIndex` to -1. If our parameter `projectionLocal` is null we create a projection
     * consisting of both of our columns: DISPLAY_NAME and SIZE for `projectionLocal`. We go through
     * the [String] entries in `projectionLocal` one by one indexed by `i`: if the current one
     * matches DISPLAY_NAME we set the variable `displayNameIndex` to the index value `i`, and if it
     * matches SIZE we set the variable `sizeIndex` to `i`. We create a new [MatrixCursor] `var cursor`
     * using `projectionLocal` for the `String[]` column names, and an `Object[]` `val result` to
     * hold the row for `cursor` which we will build. We build this row by going through the columns
     * needed to build a row and if the index of the column matches `displayNameIndex` we store the
     * decoded path of the [Uri] parameter [uri] in that column, and if the index of the column matches
     * `sizeIndex` we store the arbitrary value 42 in that column (the size of the file being piped
     * is unknown, but gmail needs it.) Then we add the row `result` to `cursor` and return it to
     * the caller.
     *
     * @param uri The URI to query. This will be the full URI sent by the client; if the client is
     * requesting a specific record, the URI will end in a record number that the implementation
     * should parse and add to a WHERE or HAVING clause, specifying that _id value.
     * @param projection The list of columns to put into the cursor. If *null* all columns are included.
     * @param selection A selection criteria to apply when filtering rows. If *null* then all rows
     * are included.
     * @param selectionArgs You may include quesstion marks in [selection], which will be replaced
     * by the values from [selectionArgs], in order that they appear in the selection. The values
     * will be bound as Strings.
     * @param sortOrder How the rows in the cursor should be sorted. If *null* then the provider is
     * free to define the sort order.
     * @return a [Cursor] or *null*.
     */
    @Suppress("RedundantNullableReturnType")
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        /**
         * content providers that support open and openAssetFile should support queries for all
         * android.provider.OpenableColumns.
         */
        var projectionLocal = projection
        var displayNameIndex = -1
        var sizeIndex = -1
        /**
         * If projection is *null*, return all columns
         */
        if (projectionLocal == null) {
            projectionLocal = arrayOf(
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE
            )
        }
        for (i in projectionLocal.indices) {
            if (OpenableColumns.DISPLAY_NAME == projectionLocal[i]) {
                displayNameIndex = i
            }
            if (OpenableColumns.SIZE == projectionLocal[i]) {
                sizeIndex = i
            }
        }
        val cursor = MatrixCursor(projectionLocal)
        val result = arrayOfNulls<Any>(projectionLocal.size)
        for (i in result.indices) {
            if (i == displayNameIndex) {
                result[i] = uri.path
            }
            if (i == sizeIndex) {
                /**
                 * Size is unknown, so let us pretend it is 42 and surprise, it works!
                 */
                result[i] = 42L
            }
        }
        cursor.addRow(result)
        return cursor
    }

    /**
     * Implement this to handle requests to insert a new row. We return *null*, having done nothing.
     *
     * @param uri The content:// URI of the insertion request. This must not be *null*.
     * @param values A set of column_name/value pairs to add to the database. This must not be *null*
     * @return The URI for the newly inserted item (we always return *null*
     */
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        /**
         * Don't support inserts.
         */
        return null
    }

    /**
     * Implement this to handle requests to delete one or more rows. We return 0, having done nothing.
     *
     * @param uri The full URI to query, including a row ID (if a specific record is requested).
     * @param selection An optional restriction to apply to rows when deleting.
     * @return The number of rows affected (we always return 0)
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        /**
         * Don't support deletes.
         */
        return 0
    }

    /**
     * Implement this to handle requests to update one or more rows. We return 0, having done nothing.
     *
     * @param uri The URI to query. This can potentially have a record ID if this is an update
     * request for a specific record.
     * @param values A set of column_name/value pairs to update in the database. This must not be *null*
     * @param selection An optional filter to match rows to update.
     * @return the number of rows affected (we always return 0)
     */
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int { // Don't support updates.
        return 0
    }

    /**
     * Implement this to handle requests for the MIME type of the data at the given URI. We always
     * return the String "image/jpeg" for this demo.
     *
     * @param uri the URI to query.
     * @return a MIME type string, or *null* if there is no type (we always return "image/jpeg")
     */
    @Suppress("RedundantNullableReturnType")
    override fun getType(uri: Uri): String? {
        /**
         * For this sample, assume all files are JPEGs.
         */
        return "image/jpeg"
    }

    /**
     * Override this to handle requests to open a file blob. Wrapped in a try block intended to catch
     * [IOException], we initialize [String] `val path` to the decoded path of our [Uri] parameter
     * [uri] (Since [uri] is "content://com.example.android.apis.content.FileProvider/2/res/drawable-nodpi-v4/jellies.jpg"
     * that is: "/2/res/drawable-nodpi-v4/jellies.jpg" in our case), locate the index of the '/'
     * character following the cookie to set `val off` and if we don't find it where we expect it we
     * throw [FileNotFoundException]. We extract the cookie substring ("2" in our case) and convert
     * it to [Int] `val cookie`, and set [String] `val assetPath` to the rest of the `path` following
     * the '/' character that terminated the cookie part of the path. ("res/drawable-nodpi-v4/jellies.jpg"
     * in our case).
     *
     * We create [AssetFileDescriptor] `val asset` by getting the context this provider is running
     * in, using it to get an `AssetManager` instance for the application's package, which we in
     * turn use to open a non-asset file descriptor using `cookie` and `assetPath`.
     *
     * We then create and return a [ParcelFileDescriptor] constructed using the [ParcelFileDescriptor]
     * returned by `openPipeHelper` which it builds using [uri] as the URI whose data is to be written,
     * a mime type of "image/jpeg", a *null* option [Bundle], a `FileInputStream` created from `asset`
     * as the arguments to the function that will actually stream the data, and *this* as the
     * `PipeDataWriter<InputStream>` that will actually stream the data from the `FileInputStream`
     * argument.
     *
     * @param uri The URI whose file is to be opened.
     * @param mode Access mode for the file.  May be "r" for read-only access, "rw" for read and
     * write access, or "rwt" for read and write access that truncates any existing file.
     * @return Returns a new ParcelFileDescriptor which you can use to access the file.
     *
     * @throws FileNotFoundException Throws [FileNotFoundException] if there is no file associated
     * with the given URI or the mode is invalid.
     * @throws SecurityException Throws [SecurityException] if the caller does not have permission
     * to access the file.
     */
    @Throws(FileNotFoundException::class)
    override fun openFile(
        uri: Uri,
        mode: String
    ): ParcelFileDescriptor? { // Try to open an asset with the given name.
        return try {
            val path = uri.path
            val off = path!!.indexOf('/', 1)
            if (off < 0 || off >= path.length - 1) {
                throw FileNotFoundException("Unable to open $uri")
            }
            val cookie = path.substring(1, off).toInt()
            val assetPath = path.substring(off + 1)
            val asset: AssetFileDescriptor = context!!.assets.openNonAssetFd(cookie, assetPath)
            ParcelFileDescriptor(
                openPipeHelper(
                    uri, "image/jpeg",
                    null,
                    asset.createInputStream(),
                    this
                )
            )
        } catch (_: IOException) {
            throw FileNotFoundException("Unable to open $uri")
        }
    }

    /**
     * Called from a background thread to stream data out to a pipe. Note that the pipe is blocking,
     * so this thread can block on writes for an arbitrary amount of time if the client is slow
     * at reading.
     *
     * First we allocate 8192 bytes for [Byte] array `val buffer`, declare an [Int] `var n` to hold
     * the number of bytes read for each read attempt, and create [FileOutputStream] `val fout` using
     * the actual `FileDescriptor` associated with our [ParcelFileDescriptor] parameter [output].
     *
     * Then wrapped in a try block intended to catch [IOException] we read from our [InputStream]
     * parameter [args] into `buffer` capturing the number of bytes read in `n` and for as long as
     * `n` is greater than or equal to 0, we write the `n` bytes in `buffer` to [FileOutputStream]
     * `fout`. When we are at the end of file of `args`, `n` will be -1 and we fall through to the
     * finally block where we close both `args` and `fout`.
     *
     * @param output The pipe where data should be written. This will be closed for you upon
     * returning from this function.
     * @param uri The URI whose data is to be written.
     * @param mimeType The desired type of data to be written.
     * @param opts Options supplied by caller.
     * @param args Our own custom arguments, the [InputStream] we will use to read our resource
     * file from.
     */
    override fun writeDataToPipe(
        output: ParcelFileDescriptor,
        uri: Uri,
        mimeType: String,
        opts: Bundle?,
        args: InputStream?
    ) {
        /**
         * Transfer data from the asset to the pipe the client is reading.
         */
        val buffer = ByteArray(8192)
        var n: Int
        val fout = FileOutputStream(output.fileDescriptor)
        try {
            while (args!!.read(buffer).also { n = it } >= 0) {
                fout.write(buffer, 0, n)
            }
        } catch (e: IOException) {
            Log.i(TAG, "Failed transferring", e)
        } finally {
            try {
                args!!.close()
            } catch (e: IOException) {
                Log.i(TAG, e.localizedMessage!!)
            }
            try {
                fout.close()
            } catch (e: IOException) {
                Log.i(TAG, e.localizedMessage!!)
            }
        }
    }

    /**
     * Our static constant
     */
    companion object {
        /**
         * TAG for logging
         */
        private const val TAG = "FileProvider"
    }
}