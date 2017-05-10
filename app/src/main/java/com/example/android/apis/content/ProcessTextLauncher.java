package com.example.android.apis.content;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

//Need the following import to get access to the app resources, since this
//class is in a sub-package.
import com.example.android.apis.R;

/**
 * Shows how to handle text selection intents, when text in .content.ProcessTextLauncher is selected,
 * Marshmallow and above allow you to send the selected text to the .content.ProcessText Activity
 * using the "android.intent.action.PROCESS_TEXT" action. This is specified in the {@code <activity>}
 * element for ".content.ProcessText" in AndroidManifest.xml by its {@code <intent-filter>}
 * element, which contains an {@code <action>} element with the attribute
 * android:name="android.intent.action.PROCESS_TEXT", a {@code <category>} element with the attribute
 * android:name="android.intent.category.DEFAULT", and a {@code <data>} element with the attribute
 * android:mimeType="text/plain".
 */
public class ProcessTextLauncher extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_text_send);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

}
