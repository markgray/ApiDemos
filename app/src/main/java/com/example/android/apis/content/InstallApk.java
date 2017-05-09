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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.apis.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Shows how to use Intent.ACTION_INSTALL_PACKAGE, and Intent.ACTION_UNINSTALL_PACKAGE to install
 * and uninstall packages.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class InstallApk extends Activity {
    /**
     * Request code used for {@code startActivityForResult} when starting the {@code Intent} with
     * the action ACTION_INSTALL_PACKAGE, and checked for in {@code onActivityResult} when the
     * activity is completed.
     */
    static final int REQUEST_INSTALL = 1;
    /**
     * Request code used for {@code startActivityForResult} when starting the {@code Intent} with
     * the action ACTION_UNINSTALL_PACKAGE, and checked for in {@code onActivityResult} when the
     * activity is completed.
     */
    static final int REQUEST_UNINSTALL = 2;
    /**
     * TAG used for logging
     */
    private static final String TAG = "InstallApk";

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.install_apk. Next
     * we locate the {@code Button}'s in our layout and set their {@code OnClickListener} as follows:
     * <ul>
     * <li>R.id.unknown_source "UNKNOWN SOURCE" -- {@code mUnknownSourceListener}</li>
     * <li>R.id.my_source "MY SOURCE" -- {@code mMySourceListener}</li>
     * <li>R.id.replace "REPLACE" -- {@code mReplaceListener}</li>
     * <li>R.id.uninstall "UNINSTALL" -- {@code mUninstallListener}</li>
     * <li>R.id.uninstall_result "UNINSTALL W/RESULT" -- {@code mUninstallResultListener}</li>
     * </ul>
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.install_apk);

        // Watch for button clicks.
        Button button = (Button) findViewById(R.id.unknown_source);
        button.setOnClickListener(mUnknownSourceListener);
        button = (Button) findViewById(R.id.my_source);
        button.setOnClickListener(mMySourceListener);
        button = (Button) findViewById(R.id.replace);
        button.setOnClickListener(mReplaceListener);
        button = (Button) findViewById(R.id.uninstall);
        button.setOnClickListener(mUninstallListener);
        button = (Button) findViewById(R.id.uninstall_result);
        button.setOnClickListener(mUninstallResultListener);
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p>
     * You will receive this call immediately before onResume() when your
     * activity is re-starting.
     *
     * If the {@code requestCode} request code the child was launched with was REQUEST_INSTALL we
     * branch based on the value of {@code resultCode}:
     * <ul>
     *     <li>RESULT_OK -- we toast "Install succeeded!"</li>
     *     <li>RESULT_CANCELED -- we toast "Install canceled!"</li>
     *     <li>otherwise we toast "Install Failed!"</li>
     * </ul>
     * Likewise if the {@code requestCode} request code the child was launched with was REQUEST_UNINSTALL
     * we branch based on the value of {@code resultCode}:
     * <ul>
     *     <li>RESULT_OK -- we toast "Uninstall succeeded!"</li>
     *     <li>RESULT_CANCELED -- we toast "Uninstall canceled!"</li>
     *     <li>otherwise we toast "Uninstall Failed!"</li>
     * </ul>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param intent      An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_INSTALL) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Install succeeded!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Install canceled!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Install Failed!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_UNINSTALL) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Uninstall succeeded!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Uninstall canceled!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Uninstall Failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * {@code OnClickListener} for the Button with ID R.id.unknown_source "UNKNOWN SOURCE"
     */
    private OnClickListener mUnknownSourceListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(Uri.fromFile(prepareApk("HelloActivity.apk")));
            startActivity(intent);
        }
    };

    private OnClickListener mMySourceListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(Uri.fromFile(prepareApk("HelloActivity.apk")));
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, getApplicationInfo().packageName);
            startActivityForResult(intent, REQUEST_INSTALL);
        }
    };

    private OnClickListener mReplaceListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(Uri.fromFile(prepareApk("HelloActivity.apk")));
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            //noinspection deprecation
            intent.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);
            intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, getApplicationInfo().packageName);
            startActivityForResult(intent, REQUEST_INSTALL);
        }
    };

    private OnClickListener mUninstallListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
            intent.setData(Uri.parse("package:com.example.android.helloactivity"));
            startActivity(intent);
        }
    };

    private OnClickListener mUninstallResultListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
            intent.setData(Uri.parse("package:com.example.android.helloactivity"));
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            startActivityForResult(intent, REQUEST_UNINSTALL);
        }
    };

    @SuppressLint("WorldReadableFiles")
    private File prepareApk(String assetName) {
        // Copy the given asset out into a file so that it can be installed.
        // Returns the path to the file.
        byte[] buffer = new byte[8192];
        InputStream is = null;
        FileOutputStream fout = null;
        try {
            is = getAssets().open(assetName);
            //noinspection deprecation
            fout = openFileOutput("tmp.apk", Context.MODE_WORLD_READABLE);
            int n;
            while ((n = is.read(buffer)) >= 0) {
                fout.write(buffer, 0, n);
            }
        } catch (IOException e) {
            Log.i("InstallApk", "Failed transferring", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Log.i(TAG, e.getLocalizedMessage());
            }
            try {
                if (fout != null) {
                    fout.close();
                }
            } catch (IOException e) {
                Log.i(TAG, e.getLocalizedMessage());
            }
        }

        return getFileStreamPath("tmp.apk");
    }
}
