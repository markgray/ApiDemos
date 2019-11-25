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
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

import java.io.File
import java.io.IOException

/**
 * Demonstration of package installation and un-installation using the original (non-Session)
 * package installation API that uses [Intent.ACTION_INSTALL_PACKAGE].
 *
 * @see InstallApkSessionApi for a demo of the newer Session API.
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
class InstallApk : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.install_apk. Next
     * we locate the [Button]'s in our layout and set their `OnClickListener` as follows:
     *
     *  * R.id.unknown_source "UNKNOWN SOURCE" -- [mUnknownSourceListener]
     *  * R.id.my_source "MY SOURCE" -- [mMySourceListener]
     *  * R.id.uninstall "UNINSTALL" -- [mUninstallListener]
     *  * R.id.uninstall_result "UNINSTALL W/RESULT" -- [mUninstallResultListener]
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.install_apk)
        // Watch for button clicks.
        var button = findViewById<Button>(R.id.unknown_source)
        button.setOnClickListener(mUnknownSourceListener)
        button = findViewById(R.id.my_source)
        button.setOnClickListener(mMySourceListener)
        button = findViewById(R.id.uninstall)
        button.setOnClickListener(mUninstallListener)
        button = findViewById(R.id.uninstall_result)
        button.setOnClickListener(mUninstallResultListener)
    }

    /**
     * Called when an activity you launched exits, giving you the `requestCode` you started it
     * with, the `resultCode` it returned, and any additional data from it.  The `resultCode`
     * will be RESULT_CANCELED if the activity explicitly returned that, didn't return any result,
     * or crashed during its operation.
     *
     * You will receive this call immediately before [onResume] when your activity is re-starting.
     *
     * If the `requestCode` request code the child was launched with was REQUEST_INSTALL we
     * branch based on the value of `resultCode`:
     *
     *  * RESULT_OK -- we toast "Install succeeded!"
     *  * RESULT_CANCELED -- we toast "Install canceled!"
     *  * otherwise we toast "Install Failed!"
     *
     * Likewise if the `requestCode` request code the child was launched with was REQUEST_UNINSTALL
     * we branch based on the value of `resultCode`:
     *
     *  * RESULT_OK -- we toast "Uninstall succeeded!"
     *  * RESULT_CANCELED -- we toast "Uninstall canceled!"
     *  * otherwise we toast "Uninstall Failed!"
     *
     * @param requestCode The integer request code originally supplied to [startActivityForResult],
     * allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through [setResult]
     * @param intent An [Intent], which can return result data to the caller (various data can be
     * attached as Intent "extras").
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == REQUEST_INSTALL) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    Toast.makeText(this, "Install succeeded!", Toast.LENGTH_SHORT).show()
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(this, "Install canceled!", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Install Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == REQUEST_UNINSTALL) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    Toast.makeText(this, "Uninstall succeeded!", Toast.LENGTH_SHORT).show()
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(this, "Uninstall canceled!", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Uninstall Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * `OnClickListener` for the [Button] with ID R.id.unknown_source "UNKNOWN SOURCE". When
     * clicked it creates an [Intent] variable `val intent` with the action ACTION_INSTALL_PACKAGE,
     * sets the data of `intent` to the [Uri] our method [getApkUri] creates for the copy of the
     * resource file with the name "HelloActivity.apk" it makes, sets the flags of `intent` to
     * FLAG_GRANT_READ_URI_PERMISSION (the recipient of this [Intent] will be granted permission to
     * perform read operations on the URI in the Intent's data and any URIs specified in its
     * `ClipData`). Finally it uses `intent` to launch the activity requested.
     */
    private val mUnknownSourceListener = View.OnClickListener {
        @Suppress("DEPRECATION")
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
        intent.data = getApkUri("HelloActivity.apk")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(intent)
    }
    /**
     * `OnClickListener` for the [Button] with ID R.id.my_source "MY SOURCE". When clicked it
     * creates an [Intent] variable `val intent` with the action ACTION_INSTALL_PACKAGE, sets
     * the data of `intent` to the [Uri] our method [getApkUri] creates for the copy of the
     * resource file with the name "HelloActivity.apk" it makes, sets the flags of `intent` to
     * FLAG_GRANT_READ_URI_PERMISSION (the recipient of this Intent will be granted permission to
     * perform read operations on the URI in the Intent's data and any URIs specified in its
     * `ClipData`), adds *true* as an extra under the key EXTRA_NOT_UNKNOWN_SOURCE (specifies
     * that the application being installed should not be treated as coming from an unknown source,
     * but as coming from the app invoking the [Intent]), adds *true* as an extra under the key
     * EXTRA_RETURN_RESULT (the installer UI should return to the application the result code of
     * the install/uninstall), adds our package name as an extra under the key
     * EXTRA_INSTALLER_PACKAGE_NAME (specifies the installer package name), and finally it uses
     * `intent` to launch the activity requested asking for it to return a result.
     */
    private val mMySourceListener = View.OnClickListener {
        @Suppress("DEPRECATION")
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
        intent.data = getApkUri("HelloActivity.apk")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
        intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME,
                applicationInfo.packageName)
        startActivityForResult(intent, REQUEST_INSTALL)
    }
    /**
     * `OnClickListener` for the [Button] with ID R.id.uninstall "UNINSTALL". When clicked it
     * creates an [Intent] variable `val intent` with the action ACTION_UNINSTALL_PACKAGE, sets
     * the data that [Intent] `intent` is operating on to the URI formed from the [String]
     * "package:com.example.android.helloactivity". Finally it uses `intent` to launch
     * the activity requested.
     */
    private val mUninstallListener = View.OnClickListener {
        @Suppress("DEPRECATION")
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
        intent.data = Uri.parse("package:com.example.android.helloactivity")
        startActivity(intent)
    }
    /**
     * `OnClickListener` for the [Button] with ID R.id.uninstall_result "UNINSTALL W/RESULT". When
     * clicked it creates an [Intent] variable `val intent` with the action ACTION_UNINSTALL_PACKAGE,
     * sets the data that `Intent intent` is operating on to the URI formed from the [String]
     * "package:com.example.android.helloactivity", adds the extra EXTRA_RETURN_RESULT set to *true*
     * (specifies that the installer UI should return to the application the result code of the
     * install/uninstall. The returned result code will be RESULT_OK on success or RESULT_FIRST_USER
     * on failure). Finally it uses `intent` to launch the activity requested asking for it to
     * return a result and specifying the request code to be REQUEST_UNINSTALL.
     */
    private val mUninstallResultListener = View.OnClickListener {
        @Suppress("DEPRECATION")
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
        intent.data = Uri.parse("package:com.example.android.helloactivity")
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
        startActivityForResult(intent, REQUEST_UNINSTALL)
    }

    /**
     * Returns a [Uri] pointing to the APK to install, either a MODE_WORLD_READABLE file for devices
     * with an SDK less than N, or a [FileProvider] created [Uri] for N and greater. We initialize
     * [Boolean] variable `val useFileProvider` to *true* for SDK's greater than or equal to N, or
     * *false* for less than N. We initialize [String] variable `val tempFilename` to the string
     * "tmp.apk", and allocate 16384 bytes for [Byte] array variable `val buffer`. We initialize
     * [Int] variable`val fileMode` to MODE_PRIVATE (the created file can only be accessed by the
     * calling application) if `useFileProvider` is *true* or to MODE_WORLD_READABLE ( allow all
     * other applications to have read access) if it is *false*. Wrapped in a try with resources
     * consisting of `val inputStream` opened to read our asset file
     * `String assetName`, and `FileOutputStream fout` opened to write to `tempFilename`
     * with file mode `fileMode` we declare `int n` then loop while reading from `is`
     * into `buffer` as long as the number of bytes read (which we save in `n`) is greater
     * than or equal to 0, writing the contents of `buffer` to `fout`.
     *
     *
     * When done, if `useFileProvider` is true we initialize `File toInstall` with a new
     * instance created for the directory path of the directory on the filesystem where files created
     * with [.openFileOutput] are stored and the filename `tempFilename`. We then return
     * the `Uri` created by the `getUriForFile` method of `FileProvider` from
     * `toInstall`. If `useFileProvider` is false we return the `Uri` that the
     * `fromFile` method of `Uri` creates for the absolute path on the filesystem where
     * `tempFilename` is located.
     *
     * @param assetName name of the file in our assets directory we wish to install
     * @return `Uri` pointing to the temporary file copy of the `assetName` that we
     * make.
     */
    @Suppress("SameParameterValue")
    private fun getApkUri(assetName: String): Uri {
        /**
         * Before N, a MODE_WORLD_READABLE file could be passed via the ACTION_INSTALL_PACKAGE
         * [Intent]. Since N, MODE_WORLD_READABLE files are forbidden, and a [FileProvider] is
         * recommended.
         */
        val useFileProvider = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
        /**
         * Copy the given asset out into a file so that it can be installed.
         * Returns the path to the file.
         */
        val tempFilename = "tmp.apk"
        val buffer = ByteArray(16384)
        @Suppress("DEPRECATION")
        @SuppressLint("WorldReadableFiles")
        val fileMode = if (useFileProvider) Context.MODE_PRIVATE else Context.MODE_WORLD_READABLE
        try {
            assets.open(assetName).use { inputStream ->
                openFileOutput(tempFilename, fileMode).use { fout ->
                    var n: Int
                    while (inputStream.read(buffer).also { n = it } >= 0) {
                        fout.write(buffer, 0, n)
                    }
                }
            }
        } catch (e: IOException) {
            Log.i(TAG, "Failed to write temporary APK file", e)
        }
        return if (useFileProvider) {
            val toInstall = File(this.filesDir, tempFilename)
            FileProvider.getUriForFile(
                    this, "com.example.android.apis.installapkprovider", toInstall)
        } else {
            Uri.fromFile(getFileStreamPath(tempFilename))
        }
    }

    companion object {
        /**
         * Request code used for `startActivityForResult` when starting the `Intent` with
         * the action ACTION_INSTALL_PACKAGE, and checked for in `onActivityResult` when the
         * activity is completed.
         */
        const val REQUEST_INSTALL = 1
        /**
         * Request code used for `startActivityForResult` when starting the `Intent` with
         * the action ACTION_UNINSTALL_PACKAGE, and checked for in `onActivityResult` when the
         * activity is completed.
         */
        const val REQUEST_UNINSTALL = 2
        /**
         * TAG used for logging
         */
        private const val TAG = "InstallApk"
    }
}