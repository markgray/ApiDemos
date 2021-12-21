/*
 * Copyright (C) 2010 The Android Open Source Project
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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates launching the contacts app to pick a contact.  Does not
 * require permission to read contacts, as that permission will be granted
 * when the selected contact is returned.
 */
@Suppress("MemberVisibilityCanBePrivate")
class PickContact : AppCompatActivity() {
    /**
     * Toast we will toast the contact data base query result in
     */
    var mToast: Toast? = null

    /**
     * [ResultDisplayer] instance that launched contact data base query, it is set to *this* in the
     * `onClick` override of [ResultDisplayer] when the Button using *this* [ResultDisplayer] as its
     * `OnClickListener` is clicked, and used in our [onActivityResult] when the [Intent] launched
     * returns a result in order to access the field `ResultDisplayer.mMsg` to use in the toast.
     */
    var mPendingResult: ResultDisplayer? = null

    /**
     * This is a custom `OnClickListener` which is used for each of the `Button`'s in our UI. It
     * uses its constructor to specify a string to be used as part of the toast which will be shown
     * by `onActivityResult`, as well as the MIME data type used in the [Intent] which launches the
     * contact data base query.
     */
    inner class ResultDisplayer(
        /**
         * String to use as part of the toast displayed when the result of the contact database
         * query is returned to [onActivityResult].
         */
        var mMsg: String,
        /**
         * MIME data type to use for the [Intent] launching the contact database query.
         */
        var mMimeType: String
    ) : View.OnClickListener {

        /**
         * Called when the Button is clicked that uses *this* instance of [ResultDisplayer] as its
         * `OnClickListener`. First we create [Intent] variable `val intent` with the action
         * ACTION_GET_CONTENT (Allows the user to select a particular kind of data and return it).
         * We set an explicit MIME data type for `intent` to be the contents of our field
         * [String] field [mMimeType] (which is set for this instance in our constructor), set our
         * Activity's [ResultDisplayer] field [mPendingResult] to this so that we can be found by
         * [handleRequestCodes] when a result for a query is returned, and finally we use `intent`
         * in a call to the `launch` method of our [ActivityResultLauncher] field [contactsLauncher]
         * to launch an activity for which we would like a result when it finishes. (Said result will
         * be returned to [handleRequestCodes] by the lambda of the call to [registerForActivityResult]
         * in the sweet by-and-by).
         *
         * @param v View of Button that was clicked
         */
        override fun onClick(v: View) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = mMimeType
            mPendingResult = this
            contactsLauncher.launch(intent) // launches our intent with 1 as its request code.
        }
    }

    /**
     * This is the [ActivityResultLauncher] that we use to launch the [Intent] to have the contacts
     * provider ask the user to select a contact from the database. The lambda of the call to
     * [registerForActivityResult] calls our [handleRequestCodes] method with the results of the
     * activity launched.
     */
    private val contactsLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            // handleRequestCodes is just the renamed original `onActivityResult`
            handleRequestCodes(
                requestCode = 1,
                resultCode = result.resultCode,
                data =  result.data
            )

        }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.pick_contact. We
     * locate each of the four Buttons in our UI and set their `OnClickListener` to an
     * instance of [ResultDisplayer] using the parameters of the constructor to customize the
     * behavior when a particular Button is clicked as follows:
     *
     *  * R.id.pick_contact "Selected contact" "vnd.android.cursor.item/contact"
     *  * R.id.pick_person "Selected person" "vnd.android.cursor.item/person"
     *  * R.id.pick_phone "Selected phone" "vnd.android.cursor.item/phone_v2"
     *  * R.id.pick_address "Selected address" "vnd.android.cursor.item/postal-address_v2"
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pick_contact)
        // Watch for button clicks.
        findViewById<View>(R.id.pick_contact).setOnClickListener(
            ResultDisplayer("Selected contact", CONTACT)
        )
        findViewById<View>(R.id.pick_person).setOnClickListener(
            ResultDisplayer("Selected person", PERSON)
        )
        findViewById<View>(R.id.pick_phone).setOnClickListener(
            ResultDisplayer("Selected phone", PHONE)
        )
        findViewById<View>(R.id.pick_address).setOnClickListener(
            ResultDisplayer("Selected address", POSTAL)
        )
    }

    /**
     * Called when an activity you launched exits, giving you the [requestCode] you started it with,
     * the [resultCode] it returned, and any additional data from it.
     *
     * If our [Intent] parameter [data] is non-*null*, we retrieve the `Uri` data this intent is
     * operating on to initialize variable `val uri` . If `uri` is non-*null* we allocate [Cursor]
     * variable `var c`, setting it to *null*, then wrapped in a try block we use a `ContentResolver`
     * instance for our application's package to query `uri` for the column containing the unique ID
     * for a row, saving the [Cursor] returned in `c`. If `c` is now non-*null* and we can successfully
     * move to the first row of `c` we retrieve the `int` value of column 0 of that row to initialize
     * variable `val id`. If there has been a toast already toasted using our [Toast] field [mToast]
     * we cancel it.
     *
     * We now create [String] variable `val txt` using our [ResultDisplayer] field [mPendingResult]
     * to retrieve the value of the [String] field `mMsg` of the [ResultDisplayer] instance whose
     * result we are receiving after its `Button` was clicked, concatenated with the string value
     * of `uri` and the value of `id`. We set [Toast] field [mToast] to a toast that uses `txt` as
     * its text and show it.
     *
     * The finally block of our *try* then closes the [Cursor] `c` if it is not null.
     *
     * @param requestCode The integer request code originally supplied to [startActivityForResult],
     * allowing you to identify who this result came from.
     * @param resultCode  The integer result code returned by the child activity through its
     * `setResult()` method.
     * @param data An [Intent], which can return result data to the caller (various data can be
     * attached as [Intent] "extras").
     */
    @Suppress("UNUSED_PARAMETER")
    fun handleRequestCodes(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
            val uri = data.data
            if (uri != null) {
                var c: Cursor? = null
                try {
                    c = contentResolver.query(
                        uri,
                        arrayOf(BaseColumns._ID),
                        null,
                        null,
                        null
                    )
                    if (c != null && c.moveToFirst()) {
                        val id = c.getInt(0)
                        if (mToast != null) {
                            mToast!!.cancel()
                        }
                        val txt = mPendingResult!!.mMsg + ":\n" + uri + "\nid: " + id
                        @SuppressLint("ShowToast")
                        mToast = Toast.makeText(this, txt, Toast.LENGTH_LONG)
                        mToast!!.show()
                    }
                } finally {
                    c?.close()
                }
            }
        }
    }

    /**
     * Our static constants
     */
    companion object {
        /**
         * The MIME type of a CONTENT_URI subdirectory of a single person.
         * Constant Value: "vnd.android.cursor.item/contact"
         */
        const val CONTACT = ContactsContract.Contacts.CONTENT_ITEM_TYPE

        /**
         * The MIME type of a CONTENT_URI subdirectory of a single person.
         * Constant Value: "vnd.android.cursor.item/person"
         */
        const val PERSON = "vnd.android.cursor.item/person"

        /**
         * A data kind representing a telephone number. MIME type used when storing this in data table.
         * Constant Value: "vnd.android.cursor.item/phone_v2"
         */
        const val PHONE = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE

        /**
         * A data kind representing a postal addresses and MIME type used when storing this in data table.
         * Constant Value: "vnd.android.cursor.item/postal-address_v2"
         */
        const val POSTAL = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
    }
}