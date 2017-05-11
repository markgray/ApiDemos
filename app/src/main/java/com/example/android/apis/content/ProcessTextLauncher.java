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
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.process_text_send
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_text_send);
    }

    /**
     * Initialize the contents of the Activity's standard options menu. We disable the menu by
     * returning false.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

}
