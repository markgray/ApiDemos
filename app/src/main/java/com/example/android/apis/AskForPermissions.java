package com.example.android.apis;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * This {@code Activity} just asks the user for permissions when the app is first run.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class AskForPermissions extends Activity {
    /**
     * List of permissions requested in AndroidManifest.xml
     */
    public String[] permissions = {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.VIBRATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.SET_WALLPAPER,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.RECEIVE_MMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.NFC,
            Manifest.permission.TRANSMIT_IR,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.activity_ask_for_permissions.
     * We locate {@code Button askUser} with ID R.id.ask_for_permission ("ASK USER FOR PERMISSIONS")
     * and set its {@code OnClickListener} to an anonymous class which toasts a message "Going to ask
     * user for permission", then creates a {@code SnackBar} with the message "Do you want grant this
     * app some dangerous permissions?", and an action which calls the method {@code ActivityCompat.requestPermissions}
     * with our list of {@code String[] permissions} we need granted to us.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_for_permissions);

        Button askUser = findViewById(R.id.ask_for_permission);
        if (askUser != null) {
            askUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Going to ask user for permission", Toast.LENGTH_LONG).show();
                    Snackbar.make(v, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityCompat.requestPermissions(AskForPermissions.this, permissions, 1);
                                }
                            })
                            .show();
                }
            });
        }

    }
}
