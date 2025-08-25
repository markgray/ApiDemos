package com.example.android.apis

import android.Manifest
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

/**
 * This `Activity` just asks the user for permissions when the app is first run.
 */
class AskForPermissions : AppCompatActivity() {
    /**
     * List of permissions requested in AndroidManifest.xml
     */
    var permissions: Array<String> = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.VIBRATE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.INTERNET,
        Manifest.permission.SET_WALLPAPER,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.NFC,
        Manifest.permission.TRANSMIT_IR,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.CAMERA
    )

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.activity_ask_for_permissions.
     * We locate `Button askUser` with ID R.id.ask_for_permission ("ASK USER FOR PERMISSIONS")
     * and set its `OnClickListener` to a lambda which creates a `SnackBar` with the message "Do you
     * want grant this app some dangerous permissions?", and an action which calls the method
     * `ActivityCompat.requestPermissions` with our list of `String[] permissions` we need granted
     * to us.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask_for_permissions)

        val askUser = findViewById<Button>(R.id.ask_for_permission)
        askUser?.setOnClickListener { v ->
            ActivityCompat.requestPermissions(
                this@AskForPermissions,
                permissions,
                1
            )
        }

    }
}
