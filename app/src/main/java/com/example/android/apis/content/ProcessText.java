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
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.process_text_main.
     * We retrieve {@code CharSequence text} from the {@code Intent} that launched us using the key
     * EXTRA_PROCESS_TEXT (The name of the extra used to define the text to be processed, as a
     * CharSequence). We retrieve the flag {@code boolean readonly} which was stored using the key
     * EXTRA_PROCESS_TEXT_READONLY (The name of the boolean extra used to define if the processed
     * text will be used as read-only). We locate the {@code EditText edit} in our layout file with
     * ID R.id.process_text_received_text_editable, set its text to {@code CharSequence text} and
     * set the cursor of {@code edit} to the end of the text that is now contains.
     * <p>
     * We locate the {@code Button finishButton} with ID R.id.process_text_finish_button and if our
     * flag {@code readonly} is true set its text to "Finish", and if false set it to "Replace".
     * Finally we set the {@code OnClickListener} of {@code finishButton} to an anonymous class
     * which calls our override of the method {@code finish} when clicked, which builds an
     * {@code Intent} containing the text from {@code EditText edit}, and sets it as our result
     * before calling our super's implementation of {@code finish()}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_text_main);

        CharSequence text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        boolean readonly = getIntent().getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false);

        EditText edit = (EditText) findViewById(R.id.process_text_received_text_editable);
        edit.setText(text);
        edit.setSelection(edit.getText().length());

        Button finishButton = (Button) findViewById(R.id.process_text_finish_button);
        finishButton.setText(readonly ? R.string.process_text_finish_readonly : R.string.process_text_finish);
        finishButton.setOnClickListener(
                new OnClickListener() {
                    /**
                     * Called when the R.id.process_text_finish_button {@code Button} is clicked, we
                     * simply call our override {@code finish()} which builds an {@code Intent} of
                     * the contents of our R.id.process_text_received_text_editable {@code EditText},
                     * sets it as our result, then calls our super's implementation of {@code finish}
                     * thereby closing this activity.
                     *
                     * @param arg0 view of the Button that was clicked
                     */
                    @Override
                    public void onClick(View arg0) {
                        finish();
                    }
                });
    }

    /**
     * Call this when your activity is done and should be closed. The ActivityResult is propagated
     * back to whoever launched you via their override of onActivityResult(). First we locate the
     * {@code EditText edit} with ID R.id.process_text_received_text_editable, then we create an
     * {@code Intent intent}, store the text from {@code EditText edit} as an extra in the
     * {@code Intent intent} under the key EXTRA_PROCESS_TEXT. We then set the result that our
     * activity will return to our caller to {@code intent} with the result code RESULT_OK. Finally
     * we call our super's implementation of {@code finish()} to close our activity.
     */
    @Override
    public void finish() {
        EditText edit = (EditText) findViewById(R.id.process_text_received_text_editable);
        Intent intent = getIntent();
        intent.putExtra(Intent.EXTRA_PROCESS_TEXT, edit.getText());
        setResult(RESULT_OK, intent);
        super.finish();
    }

    /**
     * Initialize the contents of the Activity's standard options menu. We simply return false so
     * that the options menu will not be displayed.
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
