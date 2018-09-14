/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Example that exercises client side of {@link DocumentsContract}. Layout is created programmatically.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@SuppressLint("SetTextI18n")
public class DocumentsSample extends Activity {
    /**
     * TAG used for logging
     */
    private static final String TAG = "DocumentsSample";

    /**
     * Request code used when calling {@code startActivityForResult} when the Buttons with labels
     * "OPEN_DOC <b>&#42;/&#42;</b>" (as well as the mime types "image/&#42;", "audio/ogg",
     * "text/plain, application/msword" and "GET_CONTENT <b>&#42;/&#42;</b>". In our
     * {@code onActivityResult} override we use it to branch to an area of code which requests
     * FLAG_GRANT_READ_URI_PERMISSION for the Uri returned in the {@code Intent data} result,
     * opens that Uri, calls our method {@code readFullyNoClose} to read the contents of the file
     * into a {@code byte[]} array, which we use only to determine the number of bytes read which
     * we then display in our UI.
     */
    private static final int CODE_READ = 42;
    /**
     * Request code used when calling {@code startActivityForResult} when the Buttons with label
     * "CREATE_DOC <b>&#42;/&#42;</b>". In our {@code onActivityResult} override we use it to branch
     * to an area of code which requests FLAG_GRANT_WRITE_URI_PERMISSION for the Uri returned in the
     * {@code Intent data} result, and writes the String "THE COMPLETE WORKS OF SHAKESPEARE" to
     * the URI.
     */
    private static final int CODE_WRITE = 43;
    /**
     * Request code used when calling {@code startActivityForResult} when the Buttons with label
     * "OPEN_DOC_TREE". In our {@code onActivityResult} override we use it to branch to an area of
     * code which reads through a directory tree, creates a directory and some files then deletes
     * one of the files.
     */
    private static final int CODE_TREE = 44;
    /**
     * Request code used when calling {@code startActivityForResult} when the Buttons with label
     * "OPEN_DOC <b>&#42;/&#42;</b> for rename". In our {@code onActivityResult} override we use it to
     * branch to an area of code which renames the Uri returned in the {@code Intent data} result to
     * "MEOW.TEST", then opens and tries to read using the new name.
     */
    private static final int CODE_RENAME = 45;

    /**
     * {@code TextView} used to display information concerning the results of our actions using our
     * {@code log} method.
     */
    private TextView mResult;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we save the {@code Context context} of "this" for later use. We create
     * a {@code LinearLayout view} and set its orientation to VERTICAL. We initialize our field
     * {@code TextView mResult} with an instance of {@code TextView} and add it to {@code view}. We
     * create a {@code CheckBox multiple}, set its text to "ALLOW_MULTIPLE" and add it to {@code view}.
     * We create a {@code CheckBox localOnly}, set its text to "LOCAL_ONLY" and add it to {@code view}.
     * <p>
     * Now we create nine Buttons and set their {@code OnClickListener}'s to exercise various aspects
     * of the DocumentsContract api:
     * <ul>
     * <li>
     * "OPEN_DOC <b>&#42;/&#42;</b>" Creates an {@code Intent} for the action ACTION_OPEN_DOCUMENT
     * with the mime type of <b>&#42;/&#42;</b>, and starts the activity for a result using the
     * request code CODE_READ
     * </li>
     * <li>
     * "OPEN_DOC image/&#42;" Creates an {@code Intent} for the action ACTION_OPEN_DOCUMENT
     * with the mime type of <b>image/&#42;</b>, and starts the activity for a result using the
     * request code CODE_READ
     * </li>
     * <li>
     * "OPEN_DOC audio/ogg" Creates an {@code Intent} for the action ACTION_OPEN_DOCUMENT
     * with the mime type of <b>audio/ogg</b>, and starts the activity for a result using the
     * request code CODE_READ
     * </li>
     * <li>
     * "OPEN_DOC text/plain, application/msword" Creates an {@code Intent} for the action
     * ACTION_OPEN_DOCUMENT with the mime type of <b>&#42;/&#42;</b>, adds the extra
     * EXTRA_MIME_TYPES consisting of a String[] array containing the strings "text/plain",
     * and "application/msword" and then starts the activity for a result using the request
     * code CODE_READ
     * </li>
     * <li>
     * "CREATE_DOC text/plain" Creates an {@code Intent} for the action ACTION_CREATE_DOCUMENT
     * with the mime type of "text/plain" and an extra EXTRA_TITLE of "foobar.txt" and starts
     * the activity for a result using the request code CODE_WRITE
     * </li>
     * <li>
     * "CREATE_DOC image/png" Creates an {@code Intent} for the action ACTION_CREATE_DOCUMENT
     * with the mime type of "image/png" and an extra EXTRA_TITLE of "mypicture.png" and starts
     * the activity for a result using the request code CODE_WRITE
     * </li>
     * <li>
     * "GET_CONTENT <b>&#42;/&#42;</b>" Creates an {@code Intent} for the action ACTION_GET_CONTENT
     * with the mime type of "<b>&#42;/&#42;</b>" and uses the {@code Intent} to create an
     * action chooser with the title "Kittens!" which it starts for a result using the request
     * code CODE_READ
     * </li>
     * <li>
     * "OPEN_DOC_TREE" Creates an {@code Intent} for the action ACTION_OPEN_DOCUMENT_TREE and
     * uses the {@code Intent} to create an action chooser with the title "Kittens!" which
     * it starts for a result using the request code CODE_TREE
     * </li>
     * <li>
     * "OPEN_DOC <b>&#42;/&#42;</b> for rename" Creates an {@code Intent} for the action
     * ACTION_OPEN_DOCUMENT with the mime type of <b>&#42;/&#42;</b> and starts the activity
     * for a result using the request code  CODE_RENAME
     * </li>
     * </ul>
     * Note that in each case if it is appropriate to specify a local only version of the request,
     * {@code onClick} checks to see if {@code CheckBox localOnly} is checked and if so adds the
     * extra EXTRA_LOCAL_ONLY set to true to the {@code Intent}. Similarly if it is appropriate to
     * allow multiple selections {@code onClick} checks to see if {@code CheckBox multiple} is checked
     * and if so adds the extra EXTRA_ALLOW_MULTIPLE set to true.
     * <p>
     * Finally we create a {@code ScrollView scroll}, add {@code view} to it, and then set our content
     * view to {@code scroll}
     *
     * @param icicle we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        /*
         * {@code Context} used to create views ("this" is an {@code Activity} which extends
         * {@code ContextThemeWrapper which extends {@code ContextThemeWrapper} which extends
         * {@code Context})
         */
        final Context context = this;

        // LinearLayout we will place our UI in programmatically
        final LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);

        mResult = new TextView(context);
        view.addView(mResult);

        // CheckBox used to specify that multiple selections are allowed
        final CheckBox multiple = new CheckBox(context);
        multiple.setText("ALLOW_MULTIPLE");
        view.addView(multiple);

        // Checkbox used to indicate that an intent should only return data that is on the local device.
        final CheckBox localOnly = new CheckBox(context);
        localOnly.setText("LOCAL_ONLY");
        view.addView(localOnly);

        // Button we will reuse to create and add nine Buttons to our UI
        Button button;
        button = new Button(context);
        button.setText("OPEN_DOC */*");
        button.setOnClickListener(new OnClickListener() {
            /**
             * When the "OPEN_DOC <b>&#42;/&#42;</b>" Button is clicked we create an {@code Intent intent}
             * with the action ACTION_OPEN_DOCUMENT (Allow the user to select and return one or more
             * existing documents), add the category CATEGORY_OPENABLE to {@code intent} (Categories
             * provide additional detail about the action the intent performs, and when resolving an intent,
             * only activities that provide all of the requested categories will be used), and set the mime
             * type to "<b>&#42;/&#42;</b>". Then if the {@code CheckBox multiple} is checked we add
             * the extra EXTRA_ALLOW_MULTIPLE set to true (indicate that the intent can allow the user to
             * select and return multiple items), and if the {@code CheckBox localOnly} is checked we add
             * the extra EXTRA_LOCAL_ONLY set to true (indicate that the intent should only return data
             * that is on the local device).
             * <p>
             * Finally we use {@code intent} to start the appropriate activity for a result using the
             * request code CODE_READ.
             *
             * @param v View of the Button that was clicked
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                if (multiple.isChecked()) {
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                if (localOnly.isChecked()) {
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                }
                startActivityForResult(intent, CODE_READ);
            }
        });
        view.addView(button);

        button = new Button(context);
        button.setText("OPEN_DOC image/*");
        button.setOnClickListener(new OnClickListener() {
            /**
             * When the "OPEN_DOC <b>image/&#42;</b>" Button is clicked we create an {@code Intent intent}
             * with the action ACTION_OPEN_DOCUMENT (Allow the user to select and return one or more
             * existing documents), add the category CATEGORY_OPENABLE to {@code intent} (Categories
             * provide additional detail about the action the intent performs, and when resolving an intent,
             * only activities that provide all of the requested categories will be used), and set the mime
             * type to "<b>image/&#42;</b>". Then if the {@code CheckBox multiple} is checked we add
             * the extra EXTRA_ALLOW_MULTIPLE set to true (indicate that the intent can allow the user to
             * select and return multiple items), and if the {@code CheckBox localOnly} is checked we add
             * the extra EXTRA_LOCAL_ONLY set to true (indicate that the intent should only return data
             * that is on the local device).
             * <p>
             * Finally we use {@code intent} to start the appropriate activity for a result using the
             * request code CODE_READ.
             *
             * @param v View of the Button that was clicked
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                if (multiple.isChecked()) {
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                if (localOnly.isChecked()) {
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                }
                startActivityForResult(intent, CODE_READ);
            }
        });
        view.addView(button);

        button = new Button(context);
        button.setText("OPEN_DOC audio/ogg");
        button.setOnClickListener(new OnClickListener() {
            /**
             * When the "OPEN_DOC <b>audio/ogg</b>" Button is clicked we create an {@code Intent intent}
             * with the action ACTION_OPEN_DOCUMENT (Allow the user to select and return one or more
             * existing documents), add the category CATEGORY_OPENABLE to {@code intent} (Categories
             * provide additional detail about the action the intent performs, and when resolving an intent,
             * only activities that provide all of the requested categories will be used), and set the mime
             * type to "<b>audio/ogg</b>". Then if the {@code CheckBox multiple} is checked we add
             * the extra EXTRA_ALLOW_MULTIPLE set to true (indicate that the intent can allow the user to
             * select and return multiple items), and if the {@code CheckBox localOnly} is checked we add
             * the extra EXTRA_LOCAL_ONLY set to true (indicate that the intent should only return data
             * that is on the local device).
             * <p>
             * Finally we use {@code intent} to start the appropriate activity for a result using the
             * request code CODE_READ.
             *
             * @param v View of the Button that was clicked
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("audio/ogg");
                if (multiple.isChecked()) {
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                if (localOnly.isChecked()) {
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                }
                startActivityForResult(intent, CODE_READ);
            }
        });
        view.addView(button);

        button = new Button(context);
        button.setText("OPEN_DOC text/plain, application/msword");
        button.setOnClickListener(new OnClickListener() {
            /**
             * When the "OPEN_DOC text/plain, application/msword" Button is clicked we create an
             * {@code Intent intent} with the action ACTION_OPEN_DOCUMENT (Allow the user to select and
             * return one or more existing documents), add the category CATEGORY_OPENABLE to {@code intent}
             * (Categories provide additional detail about the action the intent performs, and when resolving
             * an intent, only activities that provide all of the requested categories will be used),
             * and set the mime type to "<b>&#42;/&#42;</b>" and adds the extra EXTRA_MIME_TYPES consisting
             * of a String[] array containing the strings "text/plain", and "application/msword" . Then if
             * the {@code CheckBox multiple} is checked we add the extra EXTRA_ALLOW_MULTIPLE set to true
             * (indicate that the intent can allow the user to select and return multiple items), and if
             * the {@code CheckBox localOnly} is checked we add the extra EXTRA_LOCAL_ONLY set to true
             * (indicate that the intent should only return data that is on the local device).
             * <p>
             * Finally we use {@code intent} to start the appropriate activity for a result using the
             * request code CODE_READ.
             *
             * @param v View of the Button that was clicked
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                        "text/plain", "application/msword"});
                if (multiple.isChecked()) {
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                if (localOnly.isChecked()) {
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                }
                startActivityForResult(intent, CODE_READ);
            }
        });
        view.addView(button);

        button = new Button(context);
        button.setText("CREATE_DOC text/plain");
        button.setOnClickListener(new OnClickListener() {
            /**
             * When the "CREATE_DOC text/plain" Button is clicked we create an {@code Intent intent}
             * with the action ACTION_CREATE_DOCUMENT (Allow the user to create a new document), add
             * the category CATEGORY_OPENABLE to {@code intent} (Categories provide additional detail
             * about the action the intent performs, and when resolving an intent, only activities
             * that provide all of the requested categories will be used), set the mime type to
             * "text/plain", and add the extra EXTRA_TITLE with the value "foobar.txt". Then if
             * the {@code CheckBox localOnly} is checked we add the extra EXTRA_LOCAL_ONLY set to true
             * (indicate that the intent should only return data that is on the local device).
             * <p>
             * Finally we use {@code intent} to start the appropriate activity for a result using the
             * request code CODE_WRITE.
             *
             * @param v View of the Button that was clicked
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, "foobar.txt");
                if (localOnly.isChecked()) {
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                }
                startActivityForResult(intent, CODE_WRITE);
            }
        });
        view.addView(button);

        button = new Button(context);
        button.setText("CREATE_DOC image/png");
        button.setOnClickListener(new OnClickListener() {
            /**
             * When the "CREATE_DOC image/png" Button is clicked we create an {@code Intent intent}
             * with the action ACTION_CREATE_DOCUMENT (Allow the user to create a new document), add
             * the category CATEGORY_OPENABLE to {@code intent} (Categories provide additional detail
             * about the action the intent performs, and when resolving an intent, only activities
             * that provide all of the requested categories will be used), set the mime type to
             * "image/png", and add the extra EXTRA_TITLE with the value "mypicture.png". Then if
             * the {@code CheckBox localOnly} is checked we add the extra EXTRA_LOCAL_ONLY set to true
             * (indicate that the intent should only return data that is on the local device).
             * <p>
             * Finally we use {@code intent} to start the appropriate activity for a result using the
             * request code CODE_WRITE.
             *
             * @param v View of the Button that was clicked
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/png");
                intent.putExtra(Intent.EXTRA_TITLE, "mypicture.png");
                if (localOnly.isChecked()) {
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                }
                startActivityForResult(intent, CODE_WRITE);
            }
        });
        view.addView(button);

        button = new Button(context);
        button.setText("GET_CONTENT */*");
        button.setOnClickListener(new OnClickListener() {
            /**
             * When the "GET_CONTENT <b>&#42;/&#42;</b>" Button is clicked we create an {@code Intent intent}
             * with the action ACTION_GET_CONTENT (Allow the user to select a particular kind of data and
             * return it. This is different than ACTION_PICK in that here we just say what kind of data is
             * desired, not a URI of existing data from which the user can pick. An ACTION_GET_CONTENT could
             * allow the user to create the data as it runs (for example taking a picture or recording a
             * sound), let them browse over the web and download the desired data, etc.). We add the
             * category CATEGORY_OPENABLE to {@code intent} (Categories provide additional detail about
             * the action the intent performs, and when resolving an intent, only activities that provide
             * all of the requested categories will be used), and set the mime type to <b>&#42;/&#42;</b>.
             * Then if the {@code CheckBox multiple} is checked we add the extra EXTRA_ALLOW_MULTIPLE set
             * to true (indicates that the intent can allow the user to select and return multiple items),
             * and if the {@code CheckBox localOnly} is checked we add the extra EXTRA_LOCAL_ONLY set to true
             * (indicates that the intent should only return data that is on the local device).
             * <p>
             * Finally we use {@code intent} to create an action chooser {@code Intent} with the title
             * "Kittens!" which it starts for a result using the request code CODE_READ
             *
             * @param v  View of the Button that was clicked
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                if (multiple.isChecked()) {
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                if (localOnly.isChecked()) {
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                }
                startActivityForResult(Intent.createChooser(intent, "Kittens!"), CODE_READ);
            }
        });
        view.addView(button);

        button = new Button(context);
        button.setText("OPEN_DOC_TREE");
        button.setOnClickListener(new OnClickListener() {
            /**
             * When the "OPEN_DOC_TREE" Button is clicked we create an {@code Intent intent} with the
             * action ACTION_OPEN_DOCUMENT_TREE (Allow the user to pick a directory subtree). Then if
             * the {@code CheckBox localOnly} is checked we add the extra EXTRA_LOCAL_ONLY set to true
             * (indicates that the intent should only return data that is on the local device).
             * <p>
             * Finally we use {@code intent} to create an action chooser {@code Intent} with the title
             * "Kittens!" which it starts for a result using the request code CODE_TREE
             *
             * @param v View of the Button that was clicked
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                if (localOnly.isChecked()) {
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                }
                startActivityForResult(Intent.createChooser(intent, "Kittens!"), CODE_TREE);
            }
        });
        view.addView(button);

        button = new Button(context);
        button.setText("OPEN_DOC */* for rename");
        button.setOnClickListener(new OnClickListener() {
            /**
             * When the "OPEN_DOC <b>&#42;/&#42;</b> for rename" Button is clicked we create an
             * {@code Intent intent} with the action ACTION_OPEN_DOCUMENT (Allow the user to select and
             * return one or more existing documents), We add the category CATEGORY_OPENABLE to
             * {@code intent} (Categories provide additional detail about the action the intent  performs
             * and when resolving an intent, only activities that provide all of the requested categories
             * will be used), and set the mime type to <b>&#42;/&#42;</b>.
             * <p>
             * Finally we use {@code intent} to start the appropriate activity for a result using the
             * request code CODE_RENAME
             *
             * @param v View of the Button that was clicked
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, CODE_RENAME);
            }
        });
        view.addView(button);

        final ScrollView scroll = new ScrollView(context);
        scroll.addView(view);

        setContentView(scroll);
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * <p>
     * First we set {@code ContentResolver cr} to a ContentResolver instance for our application's
     * package. We call our method {@code clearLog} to clear the text from our result reporting
     * {@code TextView mResult}. Next we use our method {@code log} to display the result code and
     * the String representation of the {@code Intent data} (Which will be an URI which looks like:
     * "Intent {dat=content://com.android.providers.media.documents/* }". If {@code data} is not
     * null we set {@code Uri uri} to the URI data this intent is carrying (which would look like:
     * content://com.android.providers.media.documents/document/image%3A603) If {@code uri} is not
     * null we log boolean result of a test of {@code uri} to see if the given URI represents a
     * {@code DocumentsContract.Document} backed by a {@code DocumentsProvider}.
     * <p>
     * Next we branch on the value of the request code {@code requestCode} used to launch the
     * activity whose results we have received:
     * <ul>
     * <li>
     * CODE_READ - wrapped in try block intended to catch SecurityException, we use
     * {@code ContentResolver cr} to take the persistable URI permission grant
     * FLAG_GRANT_READ_URI_PERMISSION that has been offered for {@code uri} (Once taken,
     * the permission grant will be remembered across device reboots. Only URI permissions
     * granted with FLAG_GRANT_PERSISTABLE_URI_PERMISSION can be persisted.) We then declare
     * {@code InputStream is} as null, and wrapped in try block intended to catch any Exception,
     * we use {@code cr} to open the input stream {@code is} using the content associated with
     * content URI {@code uri}, and then call our method {@code readFullyNoClose} to read the
     * entire contents of {@code is} into a {@code byte[]} array which we use only to display
     * the length. In our finally block we call our method {@code closeQuietly} to close
     * {@code is}.
     * </li>
     * <li>
     * CODE_WRITE - wrapped in try block intended to catch SecurityException, we use
     * {@code ContentResolver cr} to take the persistable URI permission grant
     * FLAG_GRANT_WRITE_URI_PERMISSION that has been  offered for {@code uri} (Once taken,
     * the permission grant will be remembered across device reboots. Only URI permissions
     * granted with FLAG_GRANT_PERSISTABLE_URI_PERMISSION can be persisted.) We then declare
     * {@code OutputStream os} as null, and wrapped in try block intended to catch any Exception,
     * we use {@code cr} to open the output stream {@code os} using the content associated with
     * content URI {@code uri}. We write the String "THE COMPLETE WORKS OF SHAKESPEARE" to
     * {@code os}. In our finally block we call our method {@code closeQuietly} to close
     * {@code os}.
     * </li>
     * <li>
     * CODE_TREE - We build {@code Uri doc} to be an URI representing COLUMN_DOCUMENT_ID from
     * the {@code Uri uri} (This can then be used to access documents under a user-selected
     * directory tree, since it doesn't require the user to separately confirm each new
     * document access.) We build {@code Uri child} to be an URI representing the children
     * of the target directory {@code Uri uri}. We create {@code String[] projection} to target
     * Document.COLUMN_DISPLAY_NAME, and Document.COLUMN_MIME_TYPE and use {@code ContentResolver cr}
     * to query the content provider for {@code child} using {@code projection} to create a
     * {@code Cursor c} referencing the contents or the target directory. We then move the cursor
     * from row to row, using our method {@code log} to display the value of column 0 and column 1
     * of the {@code c} (COLUMN_DISPLAY_NAME and COLUMN_MIME_TYPE based on the projection requested).
     * We then call our method {@code closeQuietly} to close {@code c}. Next we proceed to create
     * a "image/png" file ("pic.png") inside of {@code Uri doc}, a directory "my dir" inside of
     * {@code doc}, then using the {@code Uri dir} for "my dir" we create a "image/png" file
     * ("pic2.png") inside of that new subdirectory. We display messages informing the user of our
     * actions and the {@code Uri} for these new documents. Then we open and write the string
     * "THE COMPLETE WORKS OF SHAKESPEARE" to the file  "pic2.png", display a message to this effect,
     * and finally delete the empty file "pic.png" also displaying a message to this effect.
     * </li>
     * <li>
     * CODE_RENAME - We use the {@code ContentResolver cr} to resolve and rename the {@code Uri uri}
     * selected by the user to "MEOW.TEST", then use the {@code Uri newUri} returned from this operation
     * to open an {@code InputStream is} for this renamed file which we attempt to read using our
     * method {@code readFullyNoClose}, displaying only the number of bytes read if this is successful.
     * </li>
     * </ul>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final ContentResolver cr = getContentResolver();

        clearLog();

        log("resultCode=" + resultCode);
        log("data=" + String.valueOf(data));

        final Uri uri = data != null ? data.getData() : null;
        if (uri != null) {
            log("isDocumentUri=" + DocumentsContract.isDocumentUri(this, uri));
        } else {
            log("missing URI?");
            return;
        }

        if (requestCode == CODE_READ) {
            try {
                cr.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException e) {
                log("FAILED TO TAKE PERMISSION", e);
            }
            InputStream is = null;
            try {
                is = cr.openInputStream(uri);
                //noinspection ConstantConditions
                log("read length=" + readFullyNoClose(is).length);
            } catch (Exception e) {
                log("FAILED TO READ", e);
            } finally {
                closeQuietly(is);
            }
        } else if (requestCode == CODE_WRITE) {
            try {
                cr.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } catch (SecurityException e) {
                log("FAILED TO TAKE PERMISSION", e);
            }
            OutputStream os = null;
            try {
                os = cr.openOutputStream(uri);
                //noinspection ConstantConditions
                os.write("THE COMPLETE WORKS OF SHAKESPEARE".getBytes());
                log("wrote data");
            } catch (Exception e) {
                log("FAILED TO WRITE", e);
            } finally {
                closeQuietly(os);
            }
        } else if (requestCode == CODE_TREE) {
            // Find existing docs
            Uri doc = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri));
            Uri child = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri));
            final String[] projection = {Document.COLUMN_DISPLAY_NAME, Document.COLUMN_MIME_TYPE};
            Cursor c = cr.query(child, projection, null, null, null);
            try {
                //noinspection ConstantConditions
                while (c.moveToNext()) {
                    log("found child=" + c.getString(0) + ", mime=" + c.getString(1));
                }
            } finally {
                closeQuietly(c);
            }

            // Create some documents
            Uri pic = null;
            try {
                pic = DocumentsContract.createDocument(cr, doc, "image/png", "pic.png");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Uri dir = null;
            try {
                dir = DocumentsContract.createDocument(cr, doc, Document.MIME_TYPE_DIR, "my dir");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Uri dirPic = null;
            try {
                dirPic = DocumentsContract.createDocument(cr, dir, "image/png", "pic2.png");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            log("created " + pic);
            log("created " + dir);
            log("created " + dirPic);

            // Write to one of them
            OutputStream os = null;
            try {
                //noinspection ConstantConditions
                os = cr.openOutputStream(dirPic);
                //noinspection ConstantConditions
                os.write("THE COMPLETE WORKS OF SHAKESPEARE".getBytes());
                log("wrote data");
            } catch (Exception e) {
                log("FAILED TO WRITE", e);
            } finally {
                closeQuietly(os);
            }

            // And delete the first pic
            try {
                if (DocumentsContract.deleteDocument(cr, pic)) {
                    log("deleted untouched pic");
                } else {
                    log("FAILED TO DELETE PIC");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (requestCode == CODE_RENAME) {
            Uri newUri = null;
            try {
                newUri = DocumentsContract.renameDocument(cr, uri, "MEOW.TEST");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            log("rename result=" + newUri);

            InputStream is = null;
            try {
                //noinspection ConstantConditions
                is = cr.openInputStream(newUri);
                //noinspection ConstantConditions
                log("read length=" + readFullyNoClose(is).length);
            } catch (Exception e) {
                log("FAILED TO READ", e);
            } finally {
                closeQuietly(is);
            }
        }
    }

    /**
     * Sets the text of {@code TextView mResult} to null
     */
    private void clearLog() {
        mResult.setText(null);
    }

    /**
     * Displays the {@code String msg} in {@code TextView mResult}. We just call our method
     * {@code log(String msg, Throwable t)} with null for {@code t}.
     *
     * @param msg String to display in {@code TextView mResult}
     */
    private void log(String msg) {
        log(msg, null);
    }

    /**
     * Calls {@code Log.d} to log our parameters, and also displays the {@code String msg} in
     * {@code TextView mResult}.
     *
     * @param msg String to {@code Log.d} and display in {@code TextView mResult}
     * @param t   An exception to log
     */
    private void log(String msg, Throwable t) {
        Log.d(TAG, msg, t);
        mResult.setText(mResult.getText() + "\n" + msg);
    }

    /**
     * Reads the entire contents of the parameter {@code InputStream in} into a {@code ByteArrayOutputStream}
     * then converts that to a {@code byte[]} array which it returns. First we create a new instance
     * of {@code ByteArrayOutputStream bytes}, allocate 1024 bytes for {@code byte[] buffer}, and
     * declare the variable {@code int count}. Then while the {@code count} of bytes read from our
     * parameter {@code InputStream in} into {@code buffer} is not -1, we write those {@code count}
     * bytes to {@code bytes}. When end of file is reached we return a newly allocated byte array
     * created from the content of {@code bytes}.
     *
     * @param in {@code InputStream} to read
     * @return a {@code byte[]} array of the data read
     * @throws IOException if first byte cannot be read for any reason other than the end of the file,
     *                     if the input stream has been closed, or if some other I/O error occurs.
     */
    public static byte[] readFullyNoClose(InputStream in) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        while ((count = in.read(buffer)) != -1) {
            bytes.write(buffer, 0, count);
        }
        return bytes.toByteArray();
    }

    /**
     * Closes the Stream (input or output) passed to it, ignoring any Exception thrown (except for
     * RuntimeException, which it re-throws). If our parameter is not null we attempt to close our
     * parameter {@code AutoCloseable closeable}. This is wrapped in a try block which catches then
     * rethrows RuntimeException, but ignores all other Exception's.
     *
     * @param closeable stream to close ({@code InputStream} or {@code OutputStream}
     */
    public static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }
}
