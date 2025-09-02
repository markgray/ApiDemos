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

package com.example.android.apis.content

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Example that exercises client side of [DocumentsContract].
 */
@SuppressLint("SetTextI18n", "ObsoleteSdkInt")
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class DocumentsSample : AppCompatActivity() {
    /**
     * [TextView] used to display information concerning the results of our actions using our
     * [log] method.
     */
    private var mResult: TextView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we save the [Context] of "this" in `val context`  for later use. We create
     * a [LinearLayout] `val view` and set its orientation to VERTICAL. We initialize our [TextView]
     * field [mResult] with an instance of [TextView] and add it to `view`. We create a [CheckBox]
     * `val multiple`, set its text to "ALLOW_MULTIPLE" and add it to `view`. We create a [CheckBox]
     * `var localOnly`, set its text to "LOCAL_ONLY" and add it to `view`.
     *
     * Now we create nine Buttons, set their `OnClickListener`'s to exercise various aspects of the
     * [DocumentsContract] api and add them to `view`:
     *
     *  * "OPEN_DOC &#42;/&#42;" Creates an [Intent] for the action ACTION_OPEN_DOCUMENT with
     *  the mime type of &#42;/&#42;, and starts the activity for a result using the request
     *  code CODE_READ by calling the `launch` method of [launcherCodeRead]
     *  * "OPEN_DOC image/&#42;" Creates an [Intent] for the action ACTION_OPEN_DOCUMENT with the
     *  mime type of image/&#42;, and starts the activity for a result using the request
     *  code CODE_READ by calling the `launch` method of [launcherCodeRead]
     *  * "OPEN_DOC audio/ogg" Creates an [Intent] for the action ACTION_OPEN_DOCUMENT with the mime
     *  type of audio/ogg, and starts the activity for a result using the request code CODE_READ by
     *  calling the `launch` method of [launcherCodeRead]
     *  * "OPEN_DOC text/plain, application/msword" Creates an [Intent] for the action
     *  ACTION_OPEN_DOCUMENT with the mime type of &#42;/&#42;, adds the extra EXTRA_MIME_TYPES
     *  consisting of a `String[]` array containing the strings "text/plain", and "application/msword"
     *  and then starts the activity for a result using the request code CODE_READ by calling the
     *  `launch` method of [launcherCodeRead]
     *  * "CREATE_DOC text/plain" Creates an [Intent] for the action ACTION_CREATE_DOCUMENT with the
     *  mime type of "text/plain" and an extra EXTRA_TITLE of "foobar.txt" and starts the activity
     *  for a result using the request code CODE_WRITE by calling the `launch` method of
     *  [launcherCodeWrite]
     *  * "CREATE_DOC image/png" Creates an [Intent] for the action ACTION_CREATE_DOCUMENT with the
     *  mime type of "image/png" and an extra EXTRA_TITLE of "mypicture.png" and starts the activity
     *  for a result using the request code CODE_WRITE by calling the `launch` method of
     *  [launcherCodeWrite]
     *  * "GET_CONTENT &#42;/&#42;" Creates an [Intent] for the action ACTION_GET_CONTENT with the
     *  mime type of "&#42;/&#42;" and uses the [Intent] to create an action chooser with the title
     *  "Kittens!" which it starts for a result using the request code CODE_READ by calling the
     *  `launch` method of [launcherCodeRead]
     *  * "OPEN_DOC_TREE" Creates an [Intent] for the action ACTION_OPEN_DOCUMENT_TREE and uses the
     *  [Intent] to create an action chooser with the title "Kittens!" which it starts for a result
     *  using the request code CODE_TREE by calling the `launch` method of [launcherCodeTree]
     *  * "OPEN_DOC &#42;/&#42; for rename" Creates an [Intent] for the action ACTION_OPEN_DOCUMENT
     *  with the mime type of &#42;/&#42; and starts the activity for a result using the request code
     *  CODE_RENAME by calling the `launch` method of [launcherCodeRename]
     *
     * Note that in each case if it is appropriate to specify a local only version of the request,
     * `onClick` checks to see if [CheckBox] `localOnly` is checked and if so adds the extra
     * EXTRA_LOCAL_ONLY set to *true* to the [Intent]. Similarly if it is appropriate to allow
     * multiple selections `onClick` checks to see if [CheckBox] `multiple` is checked and if so
     * adds the extra EXTRA_ALLOW_MULTIPLE set to *true*.
     *
     * Finally we create a [ScrollView] `val scroll`, add `view` to it, and then set our content
     * view to `scroll`.
     *
     * @param icicle we do not override [onSaveInstanceState] so do not use
     */
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        val context: Context = this

        /**
         * LinearLayout we will place our UI in programmatically
         */
        val view = LinearLayout(context)
        view.orientation = LinearLayout.VERTICAL
        view.setPadding(
            dpToPixel(8, this),
            dpToPixel(150, this),
            dpToPixel(8, this),
            dpToPixel(60, this)
        )
        mResult = TextView(context)
        view.addView(mResult)
        /**
         * CheckBox used to specify that multiple selections are allowed
         */
        val multiple = CheckBox(context)
        multiple.text = "ALLOW_MULTIPLE"
        view.addView(multiple)
        /**
         * Checkbox used to indicate that an intent should only return data that is on the local device
         */
        val localOnly = CheckBox(context)
        localOnly.text = "LOCAL_ONLY"
        view.addView(localOnly)
        /**
         * [Button] we will reuse to create and add nine Buttons to our UI
         */
        @Suppress("JoinDeclarationAndAssignment")
        var button: Button
        button = Button(context)
        button.text = "OPEN_DOC */*"
        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            if (multiple.isChecked) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            if (localOnly.isChecked) {
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }
            launcherCodeRead.launch(intent) // Launch a CODE_READ request code
        }
        view.addView(button)
        button = Button(context)
        button.text = "OPEN_DOC image/*"
        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            if (multiple.isChecked) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            if (localOnly.isChecked) {
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }
            launcherCodeRead.launch(intent) // Launch a CODE_READ request code
        }
        view.addView(button)
        button = Button(context)
        button.text = "OPEN_DOC audio/ogg"
        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "audio/ogg"
            if (multiple.isChecked) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            if (localOnly.isChecked) {
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }
            launcherCodeRead.launch(intent) // Launch a CODE_READ request code
        }
        view.addView(button)
        button = Button(context)
        button.text = "OPEN_DOC text/plain, application/msword"
        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            intent.putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "text/plain", "application/msword"
                )
            )
            if (multiple.isChecked) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            if (localOnly.isChecked) {
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }
            launcherCodeRead.launch(intent) // Launch a CODE_READ request code
        }
        view.addView(button)
        button = Button(context)
        button.text = "CREATE_DOC text/plain"
        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TITLE, "foobar.txt")
            if (localOnly.isChecked) {
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }
            launcherCodeWrite.launch(intent) // Launch a CODE_WRITE request code
        }
        view.addView(button)
        button = Button(context)
        button.text = "CREATE_DOC image/png"
        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/png"
            intent.putExtra(Intent.EXTRA_TITLE, "mypicture.png")
            if (localOnly.isChecked) {
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }
            launcherCodeWrite.launch(intent) // Launch a CODE_WRITE request code
        }
        view.addView(button)
        button = Button(context)
        button.text = "GET_CONTENT */*"
        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            if (multiple.isChecked) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            if (localOnly.isChecked) {
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }
            launcherCodeRead.launch(Intent.createChooser(intent, "Kittens!")) // CODE_READ
        }
        view.addView(button)
        button = Button(context)
        button.text = "OPEN_DOC_TREE"
        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            if (localOnly.isChecked) {
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }
            launcherCodeTree.launch(Intent.createChooser(intent, "Kittens!")) // CODE_TREE
        }
        view.addView(button)
        button = Button(context)
        button.text = "OPEN_DOC */* for rename"
        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            launcherCodeRename.launch(intent) // CODE_RENAME request code
        }
        view.addView(button)
        val scroll = ScrollView(context)
        scroll.addView(view)
        setContentView(scroll)
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density. First we
     * fetch a [Resources] instance for `val resources`, then we fetch the current display
     * metrics that are in effect for this resource object to [DisplayMetrics] `val metrics`.
     * Finally we return our [dp] parameter multiplied by the the screen density expressed as
     * dots-per-inch, divided by the reference density used throughout the system.
     *
     * @param dp      A value in dp (density independent pixels) unit which we need to convert
     *                into pixels
     * @param context [Context] to get resources and device specific display metrics
     * @return An [Int] value to represent px equivalent to dp depending on device density
     */
    private fun dpToPixel(dp: Int, context: Context): Int {
        val resources: Resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    }


    /**
     * [ActivityResultLauncher] used to launch a [CODE_READ] request code [Intent].
     */
    private val launcherCodeRead: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            handleRequestCodes(
                requestCode = CODE_READ,
                resultCode = result.resultCode,
                data = result.data
            )
        }

    /**
     * [ActivityResultLauncher] used to launch a [CODE_WRITE] request code [Intent].
     */
    private val launcherCodeWrite: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            handleRequestCodes(
                requestCode = CODE_WRITE,
                resultCode = result.resultCode,
                data = result.data
            )
        }

    /**
     * [ActivityResultLauncher] used to launch a [CODE_TREE] request code [Intent].
     */
    private val launcherCodeTree: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            handleRequestCodes(
                requestCode = CODE_TREE,
                resultCode = result.resultCode,
                data = result.data
            )
        }

    /**
     * [ActivityResultLauncher] used to launch a [CODE_RENAME] request code [Intent].
     */
    private val launcherCodeRename: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            handleRequestCodes(
                requestCode = CODE_RENAME,
                resultCode = result.resultCode,
                data = result.data
            )
        }

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it. First we call our super's
     * implementation of `onActivityResult`. Then we set [ContentResolver] `val cr` to a
     * [ContentResolver] instance for our application's package. We call our method [clearLog] to
     * clear the text from our result reporting [TextView] field [mResult]. Next we use our method
     * [log] to display the result code and the [String] representation of the [Intent] parameter
     * [data] (Which will be an URI which looks like:
     * "Intent {dat=content://com.android.providers.media.documents/&#42;}". If [data] is not *null*
     * we set [Uri] `val uri` to the URI data this intent is carrying (which would look like:
     * content://com.android.providers.media.documents/document/image%3A603). If `uri` is not *null*
     * we log the [Boolean] result of a test of `uri` to see if the given URI represents a
     * [DocumentsContract.Document] backed by a `DocumentsProvider`.
     *
     * Next we branch on the value of the request code parameter [requestCode] used to launch the
     * activity whose results we have received:
     *
     *  * CODE_READ - wrapped in try block intended to catch [SecurityException], we use
     *  [ContentResolver] `cr` to take the persistable URI permission grant
     *  FLAG_GRANT_READ_URI_PERMISSION that has been offered for `uri` (Once taken, the permission
     *  grant will be remembered across device reboots. Only URI permissions granted with
     *  FLAG_GRANT_PERSISTABLE_URI_PERMISSION can be persisted.) We then declare [InputStream]
     *  `var inputStream` as *null*, and wrapped in *try* block intended to catch any [Exception],
     *  we use `cr` to open the input stream `inputStream` using the content associated with content
     *  URI `uri`, and then call our method [readFullyNoClose] to read the entire contents of
     *  `inputStream` into a [Byte] array which we use only to display the length. In our *finally*
     *  block we call our method [closeQuietly] to close `inputStream`.
     *  * CODE_WRITE - wrapped in try block intended to catch [SecurityException], we use
     *  [ContentResolver] `cr` to take the persistable URI permission grant
     *  FLAG_GRANT_WRITE_URI_PERMISSION that has been offered for `uri` (Once taken, the permission
     *  grant will be remembered across device reboots. Only URI permissions granted with
     *  FLAG_GRANT_PERSISTABLE_URI_PERMISSION can be persisted.) We then declare [OutputStream]
     *  `var os` as *null*, and wrapped in *try* block intended to catch any [Exception], we use
     *  `cr` to open the output stream `os` using the content associated with content URI `uri`.
     *  We write the [String] "THE COMPLETE WORKS OF SHAKESPEARE" to `os`. In our *finally* block
     *  we call our method [closeQuietly] to close `os`
     *  * CODE_TREE - We build [Uri] `val doc` to be an URI representing COLUMN_DOCUMENT_ID from the
     *  [Uri] `uri` (This can then be used to access documents under a user-selected directory tree,
     *  since it doesn't require the user to separately confirm each new document access.) We build
     *  [Uri] `val child` to be an URI representing the children of the target directory [Uri] `uri`.
     *  We use [ContentResolver] `cr` to query the content provider for `child` using a projection
     *  consisting of the two columns DocumentsContract.Document.COLUMN_DISPLAY_NAME and
     *  DocumentsContract.Document.COLUMN_MIME_TYPE, with the selection *null* (all rows), selection
     *  arguments of *null*, and sort order of *null* and store the `Cursor` returned in `val c`
     *  (the `Cursor` will reference the contents or the target directory). We then move the cursor
     *  from row to row, using our method [log] to display the value of column 0 and column 1 of the
     *  row pointed to by `c` (COLUMN_DISPLAY_NAME and COLUMN_MIME_TYPE based on the projection
     *  requested). We then call our method [closeQuietly] to close `c`. Next we call our method
     *  [createDocument] to create a "image/png" file ("pic.png") inside of [Uri] `doc` saving the
     *  [Uri] returned that points to the file in `val pic`, a directory  "my dir" inside of `doc`
     *  saving the [Uri] returned that points to the directory in `val dir`, then using the [Uri]
     *  `dir` for "my dir" we create a "image/png" file ("pic2.png") inside of that new subdirectory
     *  saving the [Uri] returned that points to that file in `val dirPic`. We display messages
     *  informing the user of our actions and the [Uri] for each of these new documents. Then we
     *  open [OutputStream] `var os` using the [Uri] `dirPic` and write the string "THE COMPLETE
     *  WORKS OF SHAKESPEARE" to the file "pic2.png", log a message to this effect, and finally call
     *  our method [deleteDocument] to delete the empty file "pic.png" using its [Uri] `pic` then
     *  log a message to this effect.
     *  * CODE_RENAME - We call our method [renameDocument] to use the [ContentResolver] `cr` to
     *  resolve and rename the [Uri] `uri` selected by the user to "MEOW.TEST", then use the [Uri]
     *  `val newUri` returned from this operation to open an [InputStream] `var inputStream` for
     *  this renamed file which we attempt to read using our method [readFullyNoClose], logging
     *  only the number of bytes read if this is successful.
     *
     * @param requestCode The integer request code originally supplied to [startActivityForResult],
     * allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through its [setResult].
     * @param data An [Intent], which can return result data to the caller (various data can be
     * attached to the [Intent] as "extras").
     */
    private fun handleRequestCodes(requestCode: Int, resultCode: Int, data: Intent?) {
        val cr = contentResolver
        clearLog()
        log("resultCode=$resultCode")
        log("data=$data")
        val uri = data?.data
        if (uri != null) {
            log("isDocumentUri=" + DocumentsContract.isDocumentUri(this, uri))
        } else {
            log("missing URI?")
            return
        }
        if (requestCode == CODE_READ) {
            try {
                cr.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                log("FAILED TO TAKE PERMISSION", e)
            }
            var inputStream: InputStream? = null
            try {
                inputStream = cr.openInputStream(uri)
                log("read length=" + readFullyNoClose(inputStream).size)
            } catch (e: Exception) {
                log("FAILED TO READ", e)
            } finally {
                closeQuietly(inputStream)
            }
        } else if (requestCode == CODE_WRITE) {
            try {
                cr.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            } catch (e: SecurityException) {
                log("FAILED TO TAKE PERMISSION", e)
            }
            var os: OutputStream? = null
            try {
                os = cr.openOutputStream(uri)
                os!!.write("THE COMPLETE WORKS OF SHAKESPEARE".toByteArray())
                log("wrote data")
            } catch (e: Exception) {
                log("FAILED TO WRITE", e)
            } finally {
                closeQuietly(os)
            }
        } else if (requestCode == CODE_TREE) { // Find existing docs
            val doc = DocumentsContract.buildDocumentUriUsingTree(
                uri,
                DocumentsContract.getTreeDocumentId(uri)
            )
            val child = DocumentsContract.buildChildDocumentsUriUsingTree(
                uri,
                DocumentsContract.getTreeDocumentId(uri)
            )
            val c = cr.query(
                child,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE
                ),
                null,
                null,
                null
            )
            try {
                while (c!!.moveToNext()) {
                    log("found child=" + c.getString(0) + ", mime=" + c.getString(1))
                }
            } finally {
                closeQuietly(c)
            }
            /**
             * Create some documents
             */
            val pic = createDocument(cr, doc, "image/png", "pic.png")
            val dir = createDocument(
                cr,
                doc,
                DocumentsContract.Document.MIME_TYPE_DIR,
                "my dir"
            )
            val dirPic = createDocument(cr, dir, "image/png", "pic2.png")
            log("created $pic")
            log("created $dir")
            log("created $dirPic")
            // Write to one of them
            var os: OutputStream? = null
            try {
                os = cr.openOutputStream((dirPic)!!)
                os!!.write("THE COMPLETE WORKS OF SHAKESPEARE".toByteArray())
                log("wrote data")
            } catch (e: Exception) {
                log("FAILED TO WRITE", e)
            } finally {
                closeQuietly(os)
            }
            /**
             * And delete the first pic
             */
            if (deleteDocument(cr, pic)) {
                log("deleted untouched pic")
            } else {
                log("FAILED TO DELETE PIC")
            }
        } else if (requestCode == CODE_RENAME) {
            val newUri = renameDocument(cr, uri, "MEOW.TEST")
            log("rename result=$newUri")
            var inputStream: InputStream? = null
            try {
                inputStream = cr.openInputStream((newUri)!!)
                log("read length=" + readFullyNoClose(inputStream).size)
            } catch (e: Exception) {
                log("FAILED TO READ", e)
            } finally {
                closeQuietly(inputStream)
            }
        }
    }

    /**
     * Create a new document in a given directory with the given MIME type and display name. We
     * return the [Uri] that a *try* block intended to catch any [Exception] thrown by a call to
     * [DocumentsContract.createDocument] with our parameters, which will either be the [Uri] that
     * the method returns when it succeeds (which will be a [Uri] pointing to the newly created
     * document) or *null* if an [Exception] is thrown.
     *
     * @param resolver [ContentResolver] instance for our application's package.
     * @param documentUri directory [Uri] with flag Document.FLAG_DIR_SUPPORTS_CREATE in which to
     * place the new document
     * @param mimeType MIME type of new document
     * @param displayName name of new document
     * @return [Uri] pointing to the newly created document, or *null* if we failed
     */
    private fun createDocument(
        resolver: ContentResolver,
        documentUri: Uri?,
        mimeType: String,
        displayName: String
    ): Uri? {
        return try {
            DocumentsContract.createDocument(
                resolver,
                documentUri!!,
                mimeType,
                displayName
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Delete the given document. We return the value returned by a *try* block intended to catch
     * any [Exception] which will be the value returned by [DocumentsContract.deleteDocument] for
     * our parameters, or *false* if the call throws an [Exception].
     *
     * @param resolver [ContentResolver] instance for our application's package.
     * @param documentUri [Uri] pointing to the document with flag Document.FLAG_SUPPORTS_DELETE
     * @return *true* if the delete succeeded, otherwise *false*.
     */
    private fun deleteDocument(resolver: ContentResolver, documentUri: Uri?): Boolean {
        return try {
            DocumentsContract.deleteDocument(resolver, documentUri!!)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Change the display name of an existing document. We return the value returned by a *try*
     * block intended to catch any [Exception] which will be the [Uri] returned by the method
     * [DocumentsContract.renameDocument] when passed our parameters or *null* if the method throws
     * an [Exception].
     *
     * @param resolver {@code ContentResolver} instance for our application's package.
     * @param uri      document with {@link Document#FLAG_SUPPORTS_RENAME}
     * @param newName  updated name for document
     * @return the existing or new document after the rename, or {@code null} if failed.
     */
    @Suppress("SameParameterValue")
    private fun renameDocument(resolver: ContentResolver, uri: Uri, newName: String): Uri? {
        return try {
            DocumentsContract.renameDocument(resolver, uri, newName)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Sets the text of `TextView mResult` to null
     */
    private fun clearLog() {
        mResult!!.text = null
    }

    /**
     * Calls [Log.d] to log our parameters, and also displays the [String] parameter [msg] in
     * [TextView] field [mResult].
     *
     * @param msg String to `Log.d` and display in `TextView mResult`
     * @param t   An exception to log
     *
     */
    private fun log(msg: String, t: Throwable? = null) {
        Log.d(TAG, msg, t)
        mResult!!.text = mResult!!.text.toString() + "\n" + msg
    }

    /**
     * Our static constants and methods
     */
    companion object {
        /**
         * TAG used for logging
         */
        private const val TAG = "DocumentsSample"

        /**
         * Request code used when calling [startActivityForResult] when the Buttons with labels
         * "OPEN_DOC &#42;/&#42;" (as well as the mime types "image/&#42;", "audio/ogg",
         * "text/plain, application/msword" and "GET_CONTENT &#42;/&#42;" are clicked. In our
         * [onActivityResult] override we use it to branch to an area of code which requests
         * FLAG_GRANT_READ_URI_PERMISSION for the [Uri] returned in the `data` [Intent] result,
         * opens that [Uri], calls our method [readFullyNoClose] to read the contents of the file
         * into a `Byte[]` array, which we use only to determine the number of bytes read which
         * we then display in our UI.
         */
        private const val CODE_READ = 42

        /**
         * Request code used when calling [startActivityForResult] when the [Button] with label
         * "CREATE_DOC &#42;/&#42;" is clicked. In our [onActivityResult] override we use it
         * to branch to an area of code which requests FLAG_GRANT_WRITE_URI_PERMISSION for the [Uri]
         * returned in the [Intent] `data`, and writes the String "THE COMPLETE WORKS OF SHAKESPEARE"
         * to the URI.
         */
        private const val CODE_WRITE = 43

        /**
         * Request code used when calling [startActivityForResult] when the Button with label
         * "OPEN_DOC_TREE" is clicked. In our [onActivityResult] override we use it to branch to an
         * area of code which reads through a directory tree, creates a directory and some files
         * then deletes one of the files.
         */
        private const val CODE_TREE = 44

        /**
         * Request code used when calling [startActivityForResult] when the Button with label
         * "OPEN_DOC &#42;/&#42; for rename" is clicked. In our [onActivityResult] override
         * we use it to branch to an area of code which renames the [Uri] returned in the [Intent]
         * `data` result to "MEOW.TEST", then opens and tries to read using the new name.
         */
        private const val CODE_RENAME = 45

        /**
         * Reads the entire contents of the [InputStream] parameter [inputStream] into a
         * [ByteArrayOutputStream] `val bytes`, then converts that to a [ByteArray] `val buffer`
         * which it returns. First we create a new instance of [ByteArrayOutputStream] `val bytes`,
         * allocate 1024 bytes for [ByteArray] `val buffer`, and declare the [Int] variable `val count`.
         * Then while the `count` of bytes read from our [InputStream] parameter [inputStream] into
         * `buffer` is not -1, we write those `count` bytes to `bytes`. When end of file is reached
         * we return a newly allocated byte array created from the content of `bytes`.
         *
         * @param inputStream [InputStream] to read
         * @return a [Byte] array of the data read
         * @throws IOException if first byte cannot be read for any reason other than the end of the
         * file, if the input stream has been closed, or if some other I/O error occurs.
         */
        @Throws(IOException::class)
        fun readFullyNoClose(inputStream: InputStream?): ByteArray {
            val bytes = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var count: Int
            while ((inputStream!!.read(buffer).also { count = it }) != -1) {
                bytes.write(buffer, 0, count)
            }
            return bytes.toByteArray()
        }

        /**
         * Closes the Stream (input or output) passed to it, ignoring any [Exception] thrown (except
         * for [RuntimeException], which it re-throws). If our parameter is not *null* we attempt to
         * close our [AutoCloseable] parameter [closeable]. This is wrapped in a try block which
         * catches then rethrows [RuntimeException], but ignores all other [Exception]'s.
         *
         * @param closeable stream to close ({@code InputStream} or {@code OutputStream}
         */
        fun closeQuietly(closeable: AutoCloseable?) {
            if (closeable != null) {
                try {
                    closeable.close()
                } catch (rethrown: RuntimeException) {
                    throw rethrown
                } catch (_: Exception) {
                }
            }
        }
    }
}