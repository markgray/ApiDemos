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

package com.example.android.apis.content;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.ContentProvider.PipeDataWriter;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * A very simple content provider that can serve arbitrary asset files from
 * our .apk.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FileProvider extends ContentProvider implements PipeDataWriter<InputStream> {
    /**
     * TAG for logging
     */
    private static final String TAG = "FileProvider";

    /**
     * Implement this to initialize your content provider on startup, we simply return true
     *
     * @return true -- the provider was successfully loaded,
     */
    @Override
    public boolean onCreate() {
        return true;
    }

    /**
     * Implement this to handle query requests from clients.
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
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // content providers that support open and openAssetFile should support queries for all
        // android.provider.OpenableColumns.

        int displayNameIndex = -1;
        int sizeIndex = -1;

        // If projection is null, return all columns.
        if (projection == null) {
            projection = new String[]{
                    OpenableColumns.DISPLAY_NAME,
                    OpenableColumns.SIZE};
        }

        for (int i = 0; i < projection.length; i++) {
            if (OpenableColumns.DISPLAY_NAME.equals(projection[i])) {
                displayNameIndex = i;
            }
            if (OpenableColumns.SIZE.equals(projection[i])) {
                sizeIndex = i;
            }
        }

        MatrixCursor cursor = new MatrixCursor(projection);
        Object[] result = new Object[projection.length];

        for (int i = 0; i < result.length; i++) {
            if (i == displayNameIndex) {
                result[i] = uri.getPath();
            }
            if (i == sizeIndex) {
                result[i] = 42L; // Size is unknown, so let us pretend it is 42 and surprise, it works!
            }
        }

        cursor.addRow(result);
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // Don't support inserts.
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Don't support deletes.
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Don't support updates.
        return 0;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        // For this sample, assume all files are JPEGs.
        return "image/jpeg";
    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        // Try to open an asset with the given name.
        try {
            String path = uri.getPath();
            int off = path.indexOf('/', 1);
            if (off < 0 || off >= (path.length() - 1)) {
                throw new FileNotFoundException("Unable to open " + uri);
            }
            int cookie = Integer.parseInt(path.substring(1, off));
            String assetPath = path.substring(off + 1);
            //noinspection ConstantConditions
            AssetFileDescriptor asset = getContext().getAssets().openNonAssetFd(cookie, assetPath);
            return new ParcelFileDescriptor(openPipeHelper(uri, "image/jpeg", null, asset.createInputStream(), this));
        } catch (IOException e) {
            //noinspection UnnecessaryLocalVariable
            FileNotFoundException fnf = new FileNotFoundException("Unable to open " + uri);
            throw fnf;
        }
    }

    @Override
    public void writeDataToPipe(@NonNull ParcelFileDescriptor output,
                                @NonNull Uri uri,
                                @NonNull String mimeType,
                                Bundle opts,
                                InputStream args) {
        // Transfer data from the asset to the pipe the client is reading.
        byte[] buffer = new byte[8192];
        int n;
        FileOutputStream fout = new FileOutputStream(output.getFileDescriptor());
        try {
            while ((n = args.read(buffer)) >= 0) {
                fout.write(buffer, 0, n);
            }
        } catch (IOException e) {
            Log.i(TAG, "Failed transferring", e);
        } finally {
            try {
                args.close();
            } catch (IOException e) {
                Log.i(TAG, e.getLocalizedMessage());
            }
            try {
                fout.close();
            } catch (IOException e) {
                Log.i(TAG, e.getLocalizedMessage());
            }
        }
    }
}
