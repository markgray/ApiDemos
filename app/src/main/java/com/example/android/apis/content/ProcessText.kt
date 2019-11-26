package com.example.android.apis.content

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

//Need the following import to get access to the app resources, since this
//class is in a sub-package.
import com.example.android.apis.R

/**
 * Shows how to handle text selection intents, when text in .content.ProcessTextLauncher is selected,
 * Marshmallow and above allow you to send the selected text to the .content.ProcessText Activity
 * using the "android.intent.action.PROCESS_TEXT" action. This is specified in the `<activity>`
 * element for ".content.ProcessText" in AndroidManifest.xml by its `<intent-filter>`
 * element, which contains an:
 *
 * `<action>` element with the attribute android:name="android.intent.action.PROCESS_TEXT",
 *
 * `<category>` element with the attribute android:name="android.intent.category.DEFAULT"
 *
 * and a `<data>` element with the attribute android:mimeType="text/plain".
 */
@TargetApi(Build.VERSION_CODES.M)
class ProcessText : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.process_text_main.
     * We retrieve the [CharSequence] from the [Intent] that launched that was stored under the key
     * EXTRA_PROCESS_TEXT (The name of the extra used to define the text to be processed, as a
     * CharSequence) in order to initialize our variable `val text`. We retrieve the [Boolean] flag
     * which was stored using the key EXTRA_PROCESS_TEXT_READONLY (The name of the boolean extra
     * used to define if the processed text will be used as read-only) to initialize our variable
     * `val readonly`. We locate the [EditText] in our layout file with ID
     * R.id.process_text_received_text_editable to initialize variable `val edit`, set its text to
     * [CharSequence] `text` and set the cursor of `edit` to the end of the text that is now
     * contains.
     *
     * We locate the [Button] with ID R.id.process_text_finish_button to initialize our variable
     * `val finishButton` and if our flag `readonly` is *true* set its text to "Finish", and if
     * *false* set it to "Replace". Finally we set the `OnClickListener` of `finishButton` to a
     * lambda which calls our override of the method [finish] when clicked, which builds an
     * [Intent] containing the text from [EditText] `edit`, and sets it as our result before
     * calling our super's implementation of `finish()`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.process_text_main)
        val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        val readonly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)
        val edit = findViewById<EditText>(R.id.process_text_received_text_editable)
        edit.setText(text)
        edit.setSelection(edit.text.length)
        val finishButton = findViewById<Button>(R.id.process_text_finish_button)
        finishButton.setText(if (readonly) R.string.process_text_finish_readonly else R.string.process_text_finish)
        finishButton.setOnClickListener { finish() }
    }

    /**
     * Call this when your activity is done and should be closed. The `ActivityResult` is propagated
     * back to whoever launched you via their override of `onActivityResult()`. First we locate the
     * [EditText] with ID R.id.process_text_received_text_editable to initialize our variable
     * `val edit`, then we create an [Intent] to initialize varible `val intent`, store the text
     * from [EditText] `edit` as an extra in the [Intent] `intent` under the key EXTRA_PROCESS_TEXT.
     * We then set the result that our activity will return to our caller to `intent` with the result
     * code RESULT_OK. Finally we call our super's implementation of `finish()` to close our activity.
     */
    override fun finish() {
        val edit = findViewById<EditText>(R.id.process_text_received_text_editable)
        val intent = intent
        intent.putExtra(Intent.EXTRA_PROCESS_TEXT, edit.text)
        setResult(Activity.RESULT_OK, intent)
        super.finish()
    }

    /**
     * Initialize the contents of the Activity's standard options menu. We simply return false so
     * that the options menu will not be displayed.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return *true* for the menu to be displayed;
     * if you return *false* it will not be shown.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }
}