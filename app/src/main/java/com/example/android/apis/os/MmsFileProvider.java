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

package com.example.android.apis.os;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * A very simple content provider that can serve mms files from our cache directory so that
 * SmsManager#sendMultimdeiaMessage and SmsManager#downloadMultimediaMessage can read/write
 * the content of the MMS messages to send/download.
 */
@SuppressWarnings({"NullableProblems", "ConstantConditions"})
public class MmsFileProvider extends ContentProvider {
    /**
     * We implement this to initialize our content provider on startup. We simply return true.
     *
     * @return true to indicate the provider was successfully loaded.
     */
    @Override
    public boolean onCreate() {
        return true;
    }

    /**
     * We would implement this to handle query requests from clients. We ignore it.
     *
     * @param uri           The URI to query. This will be the full URI sent by the client;
     *                      if the client is requesting a specific record, the URI will end in a record number
     *                      that the implementation should parse and add to a WHERE or HAVING clause, specifying
     *                      that _id value.
     * @param projection    The list of columns to put into the cursor. If
     *                      {@code null} all columns are included.
     * @param selection     A selection criteria to apply when filtering rows.
     *                      If {@code null} then all rows are included.
     * @param selectionArgs You may include ?s in selection, which will be replaced by
     *                      the values from selectionArgs, in order that they appear in the selection.
     *                      The values will be bound as Strings.
     * @param sortOrder     How the rows in the cursor should be sorted.
     *                      If {@code null} then the provider is free to define the sort order.
     * @return a Cursor or {@code null}.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Don't support queries.
        return null;
    }

    /**
     * We would Implement this to handle requests to insert a new row. We ignore it.
     *
     * @param uri    The content:// URI of the insertion request. This must not be {@code null}.
     * @param values A set of column_name/value pairs to add to the database. This must not be {@code null}.
     * @return The URI for the newly inserted item.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Don't support inserts.
        return null;
    }

    /**
     * We would implement this to handle requests to delete one or more rows. We ignore it.
     *
     * @param uri       The full URI to query, including a row ID (if a specific record is requested).
     * @param selection An optional restriction to apply to rows when deleting.
     * @return The number of rows affected.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Don't support deletes.
        return 0;
    }

    /**
     * We would implement this to handle requests to update one or more rows. We ignore it.
     *
     * @param uri       The URI to query. This can potentially have a record ID if this is an update
     *                  request for a specific record.
     * @param values    A set of column_name/value pairs to update in the database.
     * @param selection An optional filter to match rows to update.
     * @return the number of rows affected.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Don't support updates.
        return 0;
    }

    /**
     * We would implement this to handle requests for the MIME type of the data at the given URI.
     * We ignore it.
     *
     * @param uri the URI to query.
     * @return a MIME type string, or {@code null} if there is no type.
     */
    @Override
    public String getType(Uri uri) {
        // For this sample, assume all files have no type.
        return null;
    }

    /**
     * We override this to handle requests to open a file blob. First we create {@code File file} to
     * reference the file in the application specific cache directory on the filesystem with the name
     * specified by the decoded path of our parameter {@code Uri uri}. If our parameter {@code fileMode}
     * is the string "r", we initialize {@code int mode} to MODE_READ_ONLY, otherwise we initialize it
     * to the mode created by or'ing together MODE_WRITE_ONLY, MODE_TRUNCATE, and MODE_CREATE.
     * <p>
     * Finally we return a new {@code ParcelFileDescriptor} for accessing {@code file} with access
     * mode {@code mode}.
     *
     * @param uri      The URI whose file is to be opened.
     * @param fileMode Access mode for the file.  May be "r" for read-only access, "rw" for read and
     *                 write access, or "rwt" for read and write access that truncates any existing file.
     * @return Returns a new ParcelFileDescriptor which you can use to access the file.
     */
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String fileMode) throws FileNotFoundException {
        File file = new File(getContext().getCacheDir(), uri.getPath());
        int mode = (TextUtils.equals(fileMode, "r") ? ParcelFileDescriptor.MODE_READ_ONLY :
                ParcelFileDescriptor.MODE_WRITE_ONLY
                        | ParcelFileDescriptor.MODE_TRUNCATE
                        | ParcelFileDescriptor.MODE_CREATE);
        return ParcelFileDescriptor.open(file, mode);
    }
}
