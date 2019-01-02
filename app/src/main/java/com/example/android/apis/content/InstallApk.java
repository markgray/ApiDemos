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
import com.example.android.apis.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Demonstration of package installation and un-installation using the original (non-Session)
 * package installation API that uses {@link Intent#ACTION_INSTALL_PACKAGE}.
 *
 * @see InstallApkSessionApi for a demo of the newer Session API.
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

    /*
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.install_apk. Next
     * we locate the {@code Button}'s in our layout and set their {@code OnClickListener} as follows:
     * <ul>
     * <li>R.id.unknown_source "UNKNOWN SOURCE" -- {@code mUnknownSourceListener}</li>
     * <li>R.id.my_source "MY SOURCE" -- {@code mMySourceListener}</li>
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
        Button button = findViewById(R.id.unknown_source);
        button.setOnClickListener(mUnknownSourceListener);
        button = findViewById(R.id.my_source);
        button.setOnClickListener(mMySourceListener);
        button = findViewById(R.id.uninstall);
        button.setOnClickListener(mUninstallListener);
        button = findViewById(R.id.uninstall_result);
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
     * <p>
     * If the {@code requestCode} request code the child was launched with was REQUEST_INSTALL we
     * branch based on the value of {@code resultCode}:
     * <ul>
     * <li>RESULT_OK -- we toast "Install succeeded!"</li>
     * <li>RESULT_CANCELED -- we toast "Install canceled!"</li>
     * <li>otherwise we toast "Install Failed!"</li>
     * </ul>
     * Likewise if the {@code requestCode} request code the child was launched with was REQUEST_UNINSTALL
     * we branch based on the value of {@code resultCode}:
     * <ul>
     * <li>RESULT_OK -- we toast "Uninstall succeeded!"</li>
     * <li>RESULT_CANCELED -- we toast "Uninstall canceled!"</li>
     * <li>otherwise we toast "Uninstall Failed!"</li>
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
     * {@code OnClickListener} for the Button with ID R.id.unknown_source "UNKNOWN SOURCE". When
     * clicked it creates an {@code Intent intent} with the action ACTION_INSTALL_PACKAGE, sets the
     * data of {@code intent} to the {@code Uri} our method {@code getApkUri} creates for the copy
     * of the resource file with the name "HelloActivity.apk" it makes, sets the flags of {@code intent}
     * to FLAG_GRANT_READ_URI_PERMISSION (the recipient of this Intent will be granted permission to
     * perform read operations on the URI in the Intent's data and any URIs specified in its ClipData).
     * Finally it uses {@code intent} to launch the activity requested.
     */
    private OnClickListener mUnknownSourceListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(getApkUri("HelloActivity.apk"));
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    };

    /**
     * {@code OnClickListener} for the Button with ID R.id.my_source "MY SOURCE". When clicked it
     * creates an {@code Intent intent} with the action ACTION_INSTALL_PACKAGE, sets the data of
     * {@code intent} to the {@code Uri} our method {@code getApkUri} creates for the copy of the
     * resource file with the name "HelloActivity.apk" it makes, sets the flags of {@code intent} to
     * FLAG_GRANT_READ_URI_PERMISSION (the recipient of this Intent will be granted permission to
     * perform read operations on the URI in the Intent's data and any URIs specified in its ClipData),
     * adds true as an extra under the key EXTRA_NOT_UNKNOWN_SOURCE (specifies that the application
     * being installed should not be treated as coming from an unknown source, but as coming from the
     * app invoking the Intent), adds true as an extra under the key EXTRA_RETURN_RESULT (the installer
     * UI should return to the application the result code of the install/uninstall), adds our package
     * name as an extra under the key EXTRA_INSTALLER_PACKAGE_NAME (specifies the installer package
     * name), and finally it uses {@code intent} to launch the activity requested
     * asking for it to return a result.
     */
    private OnClickListener mMySourceListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(getApkUri("HelloActivity.apk"));
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME,
                    getApplicationInfo().packageName);
            startActivityForResult(intent, REQUEST_INSTALL);
        }
    };

    /**
     * {@code OnClickListener} for the Button with ID R.id.uninstall "UNINSTALL". When
     * clicked it creates an {@code Intent intent} with the action ACTION_UNINSTALL_PACKAGE, sets the
     * data that {@code Intent intent} is operating on to the URI formed from the String
     * "package:com.example.android.helloactivity". Finally it uses {@code intent} to launch
     * the activity requested.
     */
    private OnClickListener mUninstallListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
            intent.setData(Uri.parse("package:com.example.android.helloactivity"));
            startActivity(intent);
        }
    };

    /**
     * {@code OnClickListener} for the Button with ID R.id.uninstall_result "UNINSTALL W/RESULT". When
     * clicked it creates an {@code Intent intent} with the action ACTION_UNINSTALL_PACKAGE, sets the
     * data that {@code Intent intent} is operating on to the URI formed from the String
     * "package:com.example.android.helloactivity", adds the extra EXTRA_RETURN_RESULT set to true
     * (specifies that the installer UI should return to the application the result code of the
     * install/uninstall. The returned result code will be RESULT_OK on success or RESULT_FIRST_USER
     * on failure). Finally it uses {@code intent} to launch the activity requested asking for it to
     * return a result and specifying the request code to be REQUEST_UNINSTALL.
     */
    private OnClickListener mUninstallResultListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
            intent.setData(Uri.parse("package:com.example.android.helloactivity"));
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            startActivityForResult(intent, REQUEST_UNINSTALL);
        }
    };

    /**
     * Returns a Uri pointing to the APK to install, either a MODE_WORLD_READABLE file for devices
     * with an SDK less than N, or a {@code FileProvider} created Uri for N and greater. We initialize
     * {@code boolean useFileProvider} to true for SDK's greater than or equal to N, or false for less
     * than N. We initialize {@code String tempFilename} to the string "tmp.apk", and allocate 16384
     * bytes for {@code byte[] buffer}. We initialize {@code int fileMode} to MODE_PRIVATE (the created
     * file can only be accessed by the calling application) if {@code useFileProvider} is true or to
     * MODE_WORLD_READABLE ( allow all other applications to have read access) if it is false. Wrapped
     * in a try with resources consisting of {@code InputStream is} opened to read our asset file
     * {@code String assetName}, and {@code FileOutputStream fout} opened to write to {@code tempFilename}
     * with file mode {@code fileMode} we declare {@code int n} then loop while reading from {@code is}
     * into {@code buffer} as long as the number of bytes read (which we save in {@code n}) is greater
     * than or equal to 0, writing the contents of {@code buffer} to {@code fout}.
     * <p>
     * When done, if {@code useFileProvider} is true we initialize {@code File toInstall} with a new
     * instance created for the directory path of the directory on the filesystem where files created
     * with {@link #openFileOutput} are stored and the filename {@code tempFilename}. We then return
     * the {@code Uri} created by the {@code getUriForFile} method of {@code FileProvider} from
     * {@code toInstall}. If {@code useFileProvider} is false we return the {@code Uri} that the
     * {@code fromFile} method of {@code Uri} creates for the absolute path on the filesystem where
     * {@code tempFilename} is located.
     *
     * @param assetName name of the file in our assets directory we wish to install
     * @return {@code Uri} pointing to the temporary file copy of the {@code assetName} that we
     * make.
     */
    @SuppressWarnings("SameParameterValue")
    private Uri getApkUri(String assetName) {
        // Before N, a MODE_WORLD_READABLE file could be passed via the ACTION_INSTALL_PACKAGE
        // Intent. Since N, MODE_WORLD_READABLE files are forbidden, and a FileProvider is
        // recommended.
        boolean useFileProvider = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;

        // Copy the given asset out into a file so that it can be installed.
        // Returns the path to the file.
        String tempFilename = "tmp.apk";
        byte[] buffer = new byte[16384];
        @SuppressLint("WorldReadableFiles")
        int fileMode = useFileProvider ? Context.MODE_PRIVATE : Context.MODE_WORLD_READABLE;

        try (InputStream is = getAssets().open(assetName);
            FileOutputStream fout = openFileOutput(tempFilename, fileMode)) {
            int n;
            while ((n=is.read(buffer)) >= 0) {
                fout.write(buffer, 0, n);
            }
        } catch (IOException e) {
            Log.i(TAG, "Failed to write temporary APK file", e);
        }

        if (useFileProvider) {
            File toInstall = new File(this.getFilesDir(), tempFilename);
            return FileProvider.getUriForFile(
                    this, "com.example.android.apis.installapkprovider", toInstall);
        } else {
            return Uri.fromFile(getFileStreamPath(tempFilename));
        }
    }
}
