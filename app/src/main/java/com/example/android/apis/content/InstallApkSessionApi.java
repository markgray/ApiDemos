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

package com.example.android.apis.content;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Demonstration of package installation and un-installation using the package installer Session
 * API. AndroidManifest activity element has an android:launchMode="singleTop" attribute.
 *
 * @see InstallApk for a demo of the original (non-Session) API.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class InstallApkSessionApi extends AppCompatActivity {
    /**
     * Action of the {@code Intent} used for the install status receiver.
     */
    private static final String PACKAGE_INSTALLED_ACTION =
            "com.example.android.apis.content.SESSION_API_PACKAGE_INSTALLED";

    /**
     * Called when the activity is starting. First we call our super's implementation of {@code onCreate},
     * then we set our content view to our layout file R.layout.install_apk_session_api. We initialize
     * {@code Button button} by finding the view with id R.id.install and set its {@code OnClickListener}
     * to an anonymous class which creates, configures, and commits a package installer session.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.install_apk_session_api);

        // Watch for button clicks.
        Button button = findViewById(R.id.install);
        button.setOnClickListener(new OnClickListener() {
            /**
             * Called when the button with id R.id.install ("Install") is clicked. We initialize our
             * variable {@code PackageInstaller.Session session} to null, then wrapped in a try block
             * intended to catch IOException in order to convert it to a RuntimeException, and catch
             * RuntimeException in order to abandon {@code session} if it is not null and rethrow the
             * RuntimeException we:
             * <ul>
             *     <li>
             *         Initialize {@code PackageInstaller packageInstaller} with the interface that
             *         offers the ability to install, upgrade, and remove applications on the device
             *         as returned by a PackageManager instance for our context.
             *     </li>
             *     <li>
             *         Initialize {@code PackageInstaller.SessionParams params} with a new instance
             *         whose mode is MODE_FULL_INSTALL (Mode for an install session whose staged APKs
             *         should fully replace any existing APKs for the target app).
             *     </li>
             *     <li>
             *         Use {@code packageInstaller} to create a new package installer session saving
             *         the id of the session in {@code int sessionId}.
             *     </li>
             *     <li>
             *         Use {@code packageInstaller} to open session {@code sessionId} to actively
             *         perform work, saving the {@code Session} instance in {@code session}.
             *     </li>
             *     <li>
             *         Call our method {@code addApkToInstallSession} to read our sample APK assets
             *         file "HelloActivity.apk" and write it into {@code session} under the name
             *         "package".
             *     </li>
             *     <li>
             *         Initialize {@code Context context} with this {@code InstallApkSessionApi} context.
             *     </li>
             *     <li>
             *         Initialize {@code Intent intent} with a new instance intended for our activities
             *         class {@code InstallApkSessionApi.class}, set its action to PACKAGE_INSTALLED_ACTION
             *         and use it as the {@code Intent} that will be fired by {@code PendingIntent pendingIntent}
             *         using a request code of 0 and no flags.
             *     </li>
             *     <li>
             *         Initialize {@code IntentSender statusReceiver} with an {@code IntentSender}
             *         object that wraps the existing sender of {@code PendingIntent pendingIntent},
             *         and use it as the status receiver callback when we commit {@code session}
             *         causing it to do its work.
             *     </li>
             * </ul>
             *
             * @param v {@code View} that was clicked.
             */
            @Override
            public void onClick(View v) {
                PackageInstaller.Session session = null;
                try {
                    PackageInstaller packageInstaller = getPackageManager().getPackageInstaller();
                    PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                            PackageInstaller.SessionParams.MODE_FULL_INSTALL);
                    int sessionId = packageInstaller.createSession(params);
                    session = packageInstaller.openSession(sessionId);

                    addApkToInstallSession("HelloActivity.apk", session);

                    // Create an install status receiver.
                    Context context = InstallApkSessionApi.this;
                    Intent intent = new Intent(context, InstallApkSessionApi.class);
                    intent.setAction(PACKAGE_INSTALLED_ACTION);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                    IntentSender statusReceiver = pendingIntent.getIntentSender();

                    // Commit the session (this will start the installation workflow).
                    session.commit(statusReceiver);
                } catch (IOException e) {
                    throw new RuntimeException("Couldn't install package", e);
                } catch (RuntimeException e) {
                    if (session != null) {
                        session.abandon();
                    }
                    throw e;
                }
            }
        });
    }

    /**
     * Reads the file in our assets with the filename {@code String assetName} and writes it to the
     * {@code PackageInstaller.Session session}. Wrapped in a try block with the closeable asset
     * consisting of {@code OutputStream packageInSession} (a stream to write an APK file into the
     * PackageInstaller Session {@code session} using the name "package") and {@code InputStream is}
     * (an {@code InputStream} for the file with file name {@code String assetName}) we:
     * <ul>
     *     <li>
     *         Allocate 16384 bytes for {@code byte[] buffer}
     *     </li>
     *     <li>
     *         Declare {@code int n}
     *     </li>
     *     <li>
     *         Loop while there is data to be read from {@code is} into {@code buffer} saving the number
     *         of bytes read in {@code n} then writing {@code n} bytes from {@code buffer} into
     *         {@code packageInSession}.
     *     </li>
     * </ul>
     *
     * @param assetName File name of the apk in our assets that we should read in
     * @param session Package session we should write our apk to.
     * @throws IOException if an IO error occurs.
     */
    @SuppressWarnings("SameParameterValue")
    private void addApkToInstallSession(String assetName, PackageInstaller.Session session)
            throws IOException {
        // It's recommended to pass the file size to openWrite(). Otherwise installation may fail
        // if the disk is almost full.
        try (OutputStream packageInSession = session.openWrite("package", 0, -1);
             InputStream is = getAssets().open(assetName)) {
            byte[] buffer = new byte[16384];
            int n;
            while ((n = is.read(buffer)) >= 0) {
                packageInSession.write(buffer, 0, n);
            }
        }
    }

    // Note: this Activity must run in singleTop launchMode for it to be able to receive the intent
    // in onNewIntent().
    /**
     * This is called for activities that set launchMode to "singleTop" in their package, or if a
     * client used the {@link Intent#FLAG_ACTIVITY_SINGLE_TOP} flag when calling {@link #startActivity}.
     * In either case, when the activity is re-launched while at the top of the activity stack instead
     * of a new instance of the activity being started, onNewIntent() will be called on the existing
     * instance with the Intent that was used to re-launch it.
     * <p>
     * First we initialize {@code Bundle extras} by fetching the extras stored in our parameter
     * {@code Intent intent}. If the action of {@code intent} is PACKAGE_INSTALLED_ACTION then we
     * initialize {@code int status} with the int stored in {@code extras} under the key EXTRA_STATUS
     * (status of the operation), and {@code String message} with the string stored in {@code extras}
     * under the key EXTRA_STATUS_MESSAGE (detailed string representation of the status, including
     * raw details that are useful for debugging). Then we switch on the value of {@code status}:
     * <ul>
     *     <li>
     *         STATUS_PENDING_USER_ACTION: User action is currently required to proceed. We initialize
     *         {@code Intent confirmIntent} by fetching the intent stored in {@code extras} under the
     *         key EXTRA_INTENT, start that activity running and break.
     *     </li>
     *     <li>
     *         STATUS_SUCCESS: The operation succeeded. We toast the message "Install succeeded!" and
     *         break.
     *     </li>
     *     <li>
     *         STATUS_FAILURE: The operation failed in a generic way. The system will always try to
     *         provide a more specific failure reason, but in some rare cases this may be delivered.
     *         We just fall through.
     *     </li>
     *     <li>
     *         STATUS_FAILURE_ABORTED: The operation failed because it was actively aborted. For
     *         example, the user actively declined requested permissions, or the session was abandoned.
     *         We just fall through.
     *     </li>
     *     <li>
     *         STATUS_FAILURE_BLOCKED: The operation failed because it was blocked. For example, a
     *         device policy may be blocking the operation, a package verifier may have blocked the
     *         operation, or the app may be required for core system operation. We just fall through.
     *     </li>
     *     <li>
     *         STATUS_FAILURE_CONFLICT: The operation failed because it conflicts (or is inconsistent
     *         with) with another package already installed on the device. For example, an existing
     *         permission, incompatible certificates, etc. The user may be able to uninstall another
     *         app to fix the issue. We just fall through.
     *     </li>
     *     <li>
     *         STATUS_FAILURE_INCOMPATIBLE: The operation failed because it is fundamentally incompatible
     *         with this device. For example, the app may require a hardware feature that doesn't exist,
     *         it may be missing native code for the ABIs supported by the device, or it requires a newer
     *         SDK version, etc. We just fall through.
     *     </li>
     *     <li>
     *         STATUS_FAILURE_INVALID: The operation failed because one or more of the APKs was invalid.
     *         For example, they might be malformed, corrupt, incorrectly signed, mismatched, etc.
     *         We just fall through.
     *     </li>
     *     <li>
     *         STATUS_FAILURE_STORAGE: The operation failed because of storage issues. For example,
     *         the device may be running low on space, or external media may be unavailable. The
     *         user may be able to help free space or insert different external media. We toast a
     *         message consisting of the string "Install failed! " concatenated to the string value
     *         of {@code status} concatenated to {@code message} then break.
     *     </li>
     *     <li>
     *         default: We toast a message consisting of the string "Unrecognized status received
     *         from installer: " concatenated to {@code status}.
     *     </li>
     * </ul>
     *
     * @param intent The new intent that was started for the activity.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (PACKAGE_INSTALLED_ACTION.equals(intent.getAction())) {
            //noinspection ConstantConditions
            int status = extras.getInt(PackageInstaller.EXTRA_STATUS);
            String message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE);

            switch (status) {
                case PackageInstaller.STATUS_PENDING_USER_ACTION:
                    // This test app isn't privileged, so the user has to confirm the install.
                    Intent confirmIntent = (Intent) extras.get(Intent.EXTRA_INTENT);
                    startActivity(confirmIntent);
                    break;

                case PackageInstaller.STATUS_SUCCESS:
                    Toast.makeText(this, "Install succeeded!", Toast.LENGTH_SHORT).show();
                    break;

                case PackageInstaller.STATUS_FAILURE:
                case PackageInstaller.STATUS_FAILURE_ABORTED:
                case PackageInstaller.STATUS_FAILURE_BLOCKED:
                case PackageInstaller.STATUS_FAILURE_CONFLICT:
                case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                case PackageInstaller.STATUS_FAILURE_INVALID:
                case PackageInstaller.STATUS_FAILURE_STORAGE:
                    Toast.makeText(this, "Install failed! " + status + ", " + message,
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this, "Unrecognized status received from installer: " + status,
                            Toast.LENGTH_SHORT).show();
            }
        }
    }
}
