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
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Example that exercises client side of [DocumentsContract].
 */
@SuppressLint("SetTextI18n")
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
     *  code CODE_READ
     *  * "OPEN_DOC image/&#42;" Creates an [Intent] for the action ACTION_OPEN_DOCUMENT with the
     *  mime type of image/&#42;, and starts the activity for a result using the request
     *  code CODE_READ
     *  * "OPEN_DOC audio/ogg" Creates an [Intent] for the action ACTION_OPEN_DOCUMENT with the mime
     *  type of audio/ogg, and starts the activity for a result using the request code CODE_READ
     *  * "OPEN_DOC text/plain, application/msword" Creates an [Intent] for the action
     *  ACTION_OPEN_DOCUMENT with the mime type of &#42;/&#42;, adds the extra EXTRA_MIME_TYPES
     *  consisting of a `String[]` array containing the strings "text/plain", and "application/msword"
     *  and then starts the activity for a result using the request code CODE_READ
     *  * "CREATE_DOC text/plain" Creates an [Intent] for the action ACTION_CREATE_DOCUMENT with the
     *  mime type of "text/plain" and an extra EXTRA_TITLE of "foobar.txt" and starts the activity
     *  for a result using the request code CODE_WRITE
     *  * "CREATE_DOC image/png" Creates an [Intent] for the action ACTION_CREATE_DOCUMENT with the
     *  mime type of "image/png" and an extra EXTRA_TITLE of "mypicture.png" and starts the activity
     *  for a result using the request code CODE_WRITE
     *  * "GET_CONTENT &#42;/&#42;" Creates an [Intent] for the action ACTION_GET_CONTENT with the
     *  mime type of "&#42;/&#42;" and uses the [Intent] to create an action chooser with the title
     *  "Kittens!" which it starts for a result using the request code CODE_READ
     *  * "OPEN_DOC_TREE" Creates an [Intent] for the action ACTION_OPEN_DOCUMENT_TREE and uses the
     *  [Intent] to create an action chooser with the title "Kittens!" which it starts for a result
     *  using the request code CODE_TREE
     *  * "OPEN_DOC &#42;/&#42; for rename" Creates an [Intent] for the action ACTION_OPEN_DOCUMENT
     *  with the mime type of &#42;/&#42; and starts the activity for a result using the request code
     *  CODE_RENAME
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
            startActivityForResult(intent, CODE_READ)
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
            startActivityForResult(intent, CODE_READ)
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
            startActivityForResult(intent, CODE_READ)
        }
        view.addView(button)
        button = Button(context)
        button.text = "OPEN_DOC text/plain, application/msword"
        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                    "text/plain", "application/msword"))
            if (multiple.isChecked) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            if (localOnly.isChecked) {
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }
            startActivityForResult(intent, CODE_READ)
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
            startActivityForResult(intent, CODE_WRITE)
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
            startActivityForResult(intent, CODE_WRITE)
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
            startActivityForResult(Intent.createChooser(intent, "Kittens!"), CODE_READ)
        }
        view.addView(button)
        button = Button(context)
        button.text = "OPEN_DOC_TREE"
        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            if (localOnly.isChecked) {
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }
            startActivityForResult(Intent.createChooser(intent, "Kittens!"), CODE_TREE)
        }
        view.addView(button)
        button = Button(context)
        button.text = "OPEN_DOC */* for rename"
        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(intent, CODE_RENAME)
        }
        view.addView(button)
        val scroll = ScrollView(context)
        scroll.addView(view)
        setContentView(scroll)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
            var `is`: InputStream? = null
            try {
                `is` = cr.openInputStream(uri)
                log("read length=" + readFullyNoClose(`is`).size)
            } catch (e: Exception) {
                log("FAILED TO READ", e)
            } finally {
                closeQuietly(`is`)
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
            val doc = DocumentsContract.buildDocumentUriUsingTree(uri,
                    DocumentsContract.getTreeDocumentId(uri))
            val child = DocumentsContract.buildChildDocumentsUriUsingTree(uri,
                    DocumentsContract.getTreeDocumentId(uri))
            val c = cr.query(child, arrayOf(
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE), null, null, null)
            try {
                while (c!!.moveToNext()) {
                    log("found child=" + c.getString(0) + ", mime=" + c.getString(1))
                }
            } finally {
                closeQuietly(c)
            }
            // Create some documents
            val pic = createDocument(cr, doc, "image/png", "pic.png")
            val dir = createDocument(cr, doc, DocumentsContract.Document.MIME_TYPE_DIR, "my dir")
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
            // And delete the first pic
            if (deleteDocument(cr, pic)) {
                log("deleted untouched pic")
            } else {
                log("FAILED TO DELETE PIC")
            }
        } else if (requestCode == CODE_RENAME) {
            val newUri = renameDocument(cr, uri, "MEOW.TEST")
            log("rename result=$newUri")
            var `is`: InputStream? = null
            try {
                `is` = cr.openInputStream((newUri)!!)
                log("read length=" + readFullyNoClose(`is`).size)
            } catch (e: Exception) {
                log("FAILED TO READ", e)
            } finally {
                closeQuietly(`is`)
            }
        }
    }

    private fun createDocument(resolver: ContentResolver, documentUri: Uri?, mimeType: String, displayName: String): Uri? {
        return try {
            DocumentsContract.createDocument(resolver, documentUri!!, mimeType, displayName)
        } catch (e: Exception) {
            null
        }
    }

    private fun deleteDocument(resolver: ContentResolver, documentUri: Uri?): Boolean {
        return try {
            DocumentsContract.deleteDocument(resolver, documentUri!!)
        } catch (e: Exception) {
            false
        }
    }

    @Suppress("SameParameterValue")
    private fun renameDocument(resolver: ContentResolver, uri: Uri, newName: String): Uri? {
        return try {
            DocumentsContract.renameDocument(resolver, uri, newName)
        } catch (e: Exception) {
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
     * Calls `Log.d` to log our parameters, and also displays the `String msg` in
     * `TextView mResult`.
     * Displays the `String msg` in `TextView mResult`. We just call our method
     * `log(String msg, Throwable t)` with null for `t`.
     *
     * @param msg String to `Log.d` and display in `TextView mResult`
     * @param t   An exception to log
     *
     */
    private fun log(msg: String, t: Throwable? = null) {
        Log.d(TAG, msg, t)
        mResult!!.text = mResult!!.text.toString() + "\n" + msg
    }

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
         * FLAG_GRANT_READ_URI_PERMISSION for the Uri returned in the `data` [Intent] result,
         * opens that Uri, calls our method [readFullyNoClose] to read the contents of the file
         * into a `Byte[]` array, which we use only to determine the number of bytes read which
         * we then display in our UI.
         */
        private const val CODE_READ = 42
        /**
         * Request code used when calling [startActivityForResult] when the Buttons with label
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
         * we use it to branch to an area of code which renames the Uri returned in the [Intent]
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
                } catch (ignored: Exception) {
                }
            }
        }
    }
}