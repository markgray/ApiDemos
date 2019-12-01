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
     * `TextView` used to display information concerning the results of our actions using our
     * `log` method.
     */
    private var mResult: TextView? = null

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        val context: Context = this
        // LinearLayout we will place our UI in programmatically
        val view = LinearLayout(context)
        view.orientation = LinearLayout.VERTICAL
        mResult = TextView(context)
        view.addView(mResult)
        // CheckBox used to specify that multiple selections are allowed
        val multiple = CheckBox(context)
        multiple.text = "ALLOW_MULTIPLE"
        view.addView(multiple)
        // Checkbox used to indicate that an intent should only return data that is on the local device.
        val localOnly = CheckBox(context)
        localOnly.text = "LOCAL_ONLY"
        view.addView(localOnly)
        // Button we will reuse to create and add nine Buttons to our UI
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
        private const val CODE_READ = 42
        private const val CODE_WRITE = 43
        private const val CODE_TREE = 44
        private const val CODE_RENAME = 45
        @Throws(IOException::class)
        fun readFullyNoClose(`in`: InputStream?): ByteArray {
            val bytes = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var count: Int
            while ((`in`!!.read(buffer).also { count = it }) != -1) {
                bytes.write(buffer, 0, count)
            }
            return bytes.toByteArray()
        }

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