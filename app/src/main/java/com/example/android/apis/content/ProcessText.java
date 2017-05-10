package com.example.android.apis.content;

//Need the following import to get access to the app resources, since this
//class is in a sub-package.
import com.example.android.apis.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

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
@TargetApi(Build.VERSION_CODES.M)
public class ProcessText extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_text_main);

        CharSequence text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        boolean readonly =
                getIntent().getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false);

        EditText edit = (EditText) findViewById(R.id.process_text_received_text_editable);
        edit.setText(text);
        edit.setSelection(edit.getText().length());

        Button finishButton = (Button) findViewById(R.id.process_text_finish_button);
        finishButton.setText(readonly
                ? R.string.process_text_finish_readonly : R.string.process_text_finish);
        finishButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        finish();
                    }
                });
    }

    @Override
    public void finish() {
        EditText edit = (EditText) findViewById(R.id.process_text_received_text_editable);
        Intent intent = getIntent();
        intent.putExtra(Intent.EXTRA_PROCESS_TEXT, edit.getText());
        setResult(RESULT_OK, intent);
        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

}
