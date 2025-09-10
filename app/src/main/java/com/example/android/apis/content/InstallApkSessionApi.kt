/*
 * Copyright (C) 2017 The Android Open Source Project
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
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import java.io.IOException

/**
 * Demonstration of package installation using the package installer Session API.
 * AndroidManifest activity element has an android:launchMode="singleTop" attribute.
 *
 * @see InstallApk for a demo of the original
 * RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
 */
class InstallApkSessionApi : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file R.layout.install_apk_session_api. We initialize
     * [Button] variable `val button` by finding the view with id R.id.install and set its
     * `OnClickListener` to a lambda which creates, configures, and commits a package installer
     * session.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @SuppressLint("RequestInstallPackagesPolicy") // Android no longer allows apk install
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.install_apk_session_api)
        // Watch for button clicks.
        val button = findViewById<Button>(R.id.install)
        /**
         * Called when the button with id R.id.install ("Install") is clicked. We initialize our
         * [PackageInstaller.Session] variable `val session` to null, then wrapped in a try block
         * intended to catch [IOException] in order to convert it to a [RuntimeException], and then
         * catch [RuntimeException] in order to abandon `session` if it is not *null* and rethrow
         * the [RuntimeException] we:
         *
         *  * Initialize [PackageInstaller] variable `val packageInstaller` with the interface that
         *  offers the ability to install, upgrade, and remove applications on the device  as
         *  returned by a `PackageManager` instance for our context.
         *  * Initialize [PackageInstaller.SessionParams] variable `val params` with a new instance
         *  whose mode is MODE_FULL_INSTALL (Mode for an install session whose staged APKs
         *  should fully replace any existing APKs for the target app).
         *  * Use `packageInstaller` to create a new package installer session saving the id of the
         *  session in [Int] variable `val sessionId`.
         *  * Use `packageInstaller` to open session `sessionId` to actively perform work, saving
         *  the `Session` instance in `session`.
         *  * Call our method [addApkToInstallSession] to read our sample APK assets file
         *  "HelloActivity.apk" and write it into `session` under the name "package".
         *  * Initialize [Context] variable `var context` with the context of *this* instance of
         *  [InstallApkSessionApi].
         *  * Initialize [Intent] variable `val intent` with a new instance intended for our
         *  activities class `InstallApkSessionApi.class`, set its action to PACKAGE_INSTALLED_ACTION
         *  and use it as the [Intent] that will be fired by [PendingIntent] variable
         *  `val pendingIntent` using a request code of 0 and no flags.
         *  * Initialize `IntentSender` variable `val statusReceiver` with an `IntentSender`
         *  object that wraps the existing sender of [PendingIntent] `pendingIntent`, and use
         *  it as the status receiver callback when we commit `session` causing it to do its work.
         *
         * Parameter: `View` that was clicked.
         */
        button.setOnClickListener {
            var session: PackageInstaller.Session? = null
            try {
                val packageInstaller = packageManager.packageInstaller
                val params = SessionParams(SessionParams.MODE_FULL_INSTALL)
                val sessionId = packageInstaller.createSession(params)
                session = packageInstaller.openSession(sessionId)
                addApkToInstallSession("HelloActivity.apk", session)
                /**
                 * Create an install status receiver.
                 */
                val context: Context = this@InstallApkSessionApi
                val intent = Intent(context, InstallApkSessionApi::class.java)
                intent.action = PACKAGE_INSTALLED_ACTION
                @SuppressLint("UnspecifiedImmutableFlag")
                val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_MUTABLE
                    )
                } else {
                    PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        0
                    )
                }
                val statusReceiver = pendingIntent.intentSender
                /**
                 * Commit the session (this will start the installation workflow).
                 */
                session.commit(statusReceiver)
            } catch (e: IOException) {
                throw RuntimeException("Couldn't install package", e)
            } catch (e: RuntimeException) {
                session?.abandon()
                throw e
            }
        }
    }

    /**
     * Reads the file in our assets  with the filename of our [String] parameter [assetName] and
     * writes it to the [PackageInstaller.Session] parameter [session]. We open the closeable asset
     * `OutputStream` variable `val packageInSession` (a stream to write an APK file into the
     * [PackageInstaller.Session] parameter [session] using the name "package") and `InputStream`
     * variable `inputStream` (an `InputStream` for the file with file name given by our parameter
     * [assetName]) and we `use` both `packageInSession` and `inputStream` in a *while* loop:
     * (**Note:** the `use` library extension functions catch any exceptions and sees that both
     * `packageInSession` and `inputStream` are closed like a java "try with assets block)
     *
     *  * Allocate 16384 bytes for `byte[] buffer`
     *  * Declare `int n`
     *  * Loop while there is data to be read from `inputStream` into `buffer` saving the number
     *  of bytes read in `n` then writing `n` bytes from `buffer` into `packageInSession`.
     *
     * @param assetName File name of the apk in our assets that we should read in
     * @param session Package session we should write our apk to.
     * @throws IOException if an IO error occurs.
     */
    @Suppress("SameParameterValue")
    @Throws(IOException::class)
    private fun addApkToInstallSession(assetName: String, session: PackageInstaller.Session) {
        /**
         * It's recommended to pass the file size to openWrite(). Otherwise installation may fail
         * if the disk is almost full.
         */
        session.openWrite("package", 0, -1)
            .use { packageInSession ->
                assets.open(assetName).use { inputStream ->
                    val buffer = ByteArray(16384)
                    var n: Int
                    while ((inputStream.read(buffer).also { n = it }) >= 0) {
                        packageInSession.write(buffer, 0, n)
                    }
                }
            }
    }

    /**
     * **Note:** this Activity must run in singleTop launchMode for it to be able to receive the
     * [Intent] in [onNewIntent].
     */

    /**
     * This is called for activities that set launchMode to "singleTop" in their package, or if a
     * client used the [Intent.FLAG_ACTIVITY_SINGLE_TOP] flag when calling [startActivity]. In
     * either case, when the activity is re-launched while at the top of the activity stack instead
     * of a new instance of the activity being started, [onNewIntent] will be called on the existing
     * instance with the Intent that was used to re-launch it.
     *
     * First we initialize [Bundle] variable `val extras` by fetching the extras stored in our
     * [Intent] parameter [intent]. If the action of [intent] is PACKAGE_INSTALLED_ACTION then we
     * initialize [Int] variable `val status` with the int stored in `extras` under the key
     * EXTRA_STATUS (status of the operation), and [String] variable `val message` with the string
     * stored in `extras` under the key EXTRA_STATUS_MESSAGE (detailed string representation of the
     * status, including raw details that are useful for debugging). Then we switch on the value of
     * `status`:
     *
     *  * STATUS_PENDING_USER_ACTION: User action is currently required to proceed. We initialize
     *  [Intent] variable `val confirmIntent` by fetching the [Intent] stored in `extras` under the
     *  key EXTRA_INTENT, start that activity running and break.
     *  * STATUS_SUCCESS: The operation succeeded. We toast the message "Install succeeded!" and
     *  break.
     *  * STATUS_FAILURE: The operation failed in a generic way. The system will always try to
     *  provide a more specific failure reason, but in some rare cases this may be delivered.
     *  We just fall through to the common "Install failed!" Toast.
     *  * STATUS_FAILURE_ABORTED: The operation failed because it was actively aborted. For
     *  example, the user actively declined requested permissions, or the session was abandoned.
     *  We just fall through to the common "Install failed!" Toast.
     *  * STATUS_FAILURE_BLOCKED: The operation failed because it was blocked. For example, a
     *  device policy may be blocking the operation, a package verifier may have blocked the
     *  operation, or the app may be required for core system operation. We just fall through to
     *  the common "Install failed!" Toast.
     *  * STATUS_FAILURE_CONFLICT: The operation failed because it conflicts (or is inconsistent
     *  with) with another package already installed on the device. For example, an existing
     *  permission, incompatible certificates, etc. The user may be able to uninstall another
     *  app to fix the issue. We just fall through to the common "Install failed!" Toast.
     *  * STATUS_FAILURE_INCOMPATIBLE: The operation failed because it is fundamentally incompatible
     *  with this device. For example, the app may require a hardware feature that doesn't exist,
     *  it may be missing native code for the ABIs supported by the device, or it requires a newer
     *  SDK version, etc. We just fall through to the common "Install failed!" Toast.
     *  * STATUS_FAILURE_INVALID: The operation failed because one or more of the APKs was invalid.
     *  For example, they might be malformed, corrupt, incorrectly signed, mismatched, etc.
     *  We just fall through to the common "Install failed!" Toast.
     *  * STATUS_FAILURE_STORAGE: The operation failed because of storage issues. For example,
     *  the device may be running low on space, or external media may be unavailable. The
     *  user may be able to help free space or insert different external media. We toast a
     *  message consisting of the string "Install failed! " concatenated to the string value
     *  of `status` concatenated to `message` then break (**Note:** this is the common Toast for the
     *  preceding failure modes)
     *  * else: We toast a message consisting of the string "Unrecognized status received from
     *  installer: " concatenated to `status`.
     *
     * @param intent The new [Intent] that was started for the activity.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val extras: Bundle? = intent.extras
        Log.i(TAG, "extras is $extras")
        if ((PACKAGE_INSTALLED_ACTION == intent.action)) {
            val status = extras!!.getInt(PackageInstaller.EXTRA_STATUS)
            val message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE)
            when (status) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    /**
                     * This test app isn't privileged, so the user has to confirm the install.
                     */
                    @Suppress("DEPRECATION")
                    val confirmIntent = extras[Intent.EXTRA_INTENT] as Intent?
                    startActivity(confirmIntent)
                }

                PackageInstaller.STATUS_SUCCESS ->
                    Toast.makeText(
                        this,
                        "Install succeeded!",
                        Toast.LENGTH_SHORT
                    ).show()

                PackageInstaller.STATUS_FAILURE, PackageInstaller.STATUS_FAILURE_ABORTED,
                PackageInstaller.STATUS_FAILURE_BLOCKED, PackageInstaller.STATUS_FAILURE_CONFLICT,
                PackageInstaller.STATUS_FAILURE_INCOMPATIBLE, PackageInstaller.STATUS_FAILURE_INVALID,
                PackageInstaller.STATUS_FAILURE_STORAGE ->
                    Toast.makeText(
                        this,
                        "Install failed! $status, $message",
                        Toast.LENGTH_SHORT
                    ).show()

                else -> Toast.makeText(
                    this,
                    "Unrecognized status received from installer: $status",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Our static constants.
     */
    companion object {
        /**
         * Action of the [Intent] used for the install status receiver.
         */
        private const val PACKAGE_INSTALLED_ACTION =
            "com.example.android.apis.content.SESSION_API_PACKAGE_INSTALLED"

        /**
         * TAG used for logging.
         */
        private const val TAG = "InstallApkSessionApi"
    }
}