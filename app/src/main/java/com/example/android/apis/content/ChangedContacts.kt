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
 * limitations under the License
 */
@file:Suppress("DEPRECATION")

package com.example.android.apis.content

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.LoaderManager
import android.content.*
import android.database.Cursor
import android.database.CursorWrapper
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/**
 * Shows how to access the contacts database and list those that have changed or been deleted since
 * a certain time. Layout is created by java code, includes instructive use of a ListView to contain
 * the results of the Cursor queries.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class ChangedContacts : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * To see this in action, "clear data" for the contacts storage app in the system settings.
     * Then come into this app and hit any of the delta buttons.  This will cause the contacts
     * database to be re-created.
     */
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val toast = Toast.makeText(context, "Contacts database created.", Toast.LENGTH_SHORT)
            toast.show()
        }
    }
    /**
     * `CursorAdapter` subclass used to fill `ListView mList` with data from the
     * ID_DELETE_LOADER `CursorLoader` which is configured to query the contacts data base
     * deleted contact table with the selection based on those whose CONTACT_DELETED_TIMESTAMP is
     * greater than `long mSearchTime` which is read from our preference file at start of
     * search, and updated to the newest contact change received after every "Deleted since" Button
     * click. The deleted contact table holds a log of deleted contacts.
     */
    private var mDeleteAdapter: DeleteAdapter? = null
    /**
     * `CursorAdapter` subclass used to fill `ListView mList` with data from the
     * ID_CHANGE_LOADER `CursorLoader` which is configured to query the contacts data base
     * with the selection based on those whose last changed timestamp is greater than
     * `long mSearchTime` which is read from our preference file at start of search, and
     * updated to the newest contact change received after every "Changed Since" Button click.
     */
    private var mChangeAdapter: ChangeAdapter? = null
    /**
     * Last time stamp, which is read from the preferences data base using key PREF_KEY_CHANGE or
     * PREF_KEY_DELETE depending on whether the "Changed Since" or "Deleted Since" `CursorLoader`
     * is being configured. Both values are saved and retrieved by the same routines: saveLastTimestamp,
     * and getLastTimestamp.
     */
    private var mSearchTime: Long = 0
    /**
     * `TextView` used to display number of contact changes or number of contact deletes since
     * `mSearchTime`
     */
    private var mDisplayView: TextView? = null
    /**
     * `ListView` used to display changed or deleted contacts retrieved by the
     * `ChangeAdapter mChangeAdapter` or `DeleteAdapter mDeleteAdapter`
     */
    private var mList: ListView? = null
    /**
     * `Button` used to search the contacts database for deleted contacts
     */
    private var mDeleteButton: Button? = null
    /**
     * `Button` used to search the contacts database for changed contacts
     */
    private var mChangeButton: Button? = null
    /**
     * `Button` used to reset PREF_KEY_CHANGE, and PREF_KEY_DELETE timestamps in the preferences
     * data base to 0
     */
    private var mClearPreferences: Button? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we initialize our fields `DeleteAdapter mDeleteAdapter` and
     * `ChangeAdapter mChangeAdapter` with new instances of their respective `CursorAdapter`
     * subclasses.
     *
     *
     * Next we create `LinearLayout main` and set its orientation to VERTICAL.
     *
     *
     * We initialize our field `Button mChangeButton`, setting its text to "Changed since" with
     * the value of the timestamp stored in our preference file under the key PREF_KEY_CHANGE appended
     * to it, and then set its `OnClickListener` to an anonymous function which calls our method
     * `changeClick()`.
     *
     *
     * We initialize our field `Button mDeleteButton`, setting its text to "Deleted since" with
     * the value of the timestamp stored in our preference file under the key PREF_KEY_DELETE appended
     * to it, and then set its `OnClickListener` to an anonymous function which calls our method
     * `deleteClick()`.
     *
     *
     * We initialize our field `Button mClearPreferences`, setting its text to "Clear Preferences",
     * and then set its `OnClickListener` to an anonymous function which resets both PREF_KEY_CHANGE
     * and PREF_KEY_DELETE to zero and updates the text contained in `mChangeButton` and
     * `mDeleteButton` to reflect this.
     *
     *
     * We now add `mChangeButton`, `mDeleteButton` and `mClearPreferences` to the
     * `LinearLayout main`.
     *
     *
     * We create a new `TextView` for our field `TextView mDisplayView`, configure the
     * padding to have 5 pixels around its sides, and add it to `LinearLayout main`.
     *
     *
     * We create a new `ListView` for our field `ListView mList`, set its layout params
     * to be WRAP_CONTENT for both width and height, with its weight set to 1.0, and add it to
     * `LinearLayout main`.
     *
     *
     * Finally we set our content view to `LinearLayout main`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use
     */
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDeleteAdapter = DeleteAdapter(this, null, 0)
        mChangeAdapter = ChangeAdapter(this, null, 0)
        val main = LinearLayout(this)
        main.orientation = LinearLayout.VERTICAL
        mChangeButton = Button(this)
        mChangeButton!!.text = "Changed since " + getLastTimestamp(0, PREF_KEY_CHANGE)
        mChangeButton!!.setOnClickListener { changeClick() }
        mDeleteButton = Button(this)
        mDeleteButton!!.text = "Deleted since " + getLastTimestamp(0, PREF_KEY_DELETE)
        mDeleteButton!!.setOnClickListener { deleteClick() }
        mClearPreferences = Button(this)
        mClearPreferences!!.text = "Clear Preferences"
        mClearPreferences!!.setOnClickListener {
            saveLastTimestamp(0, PREF_KEY_CHANGE)
            mChangeButton!!.text = "Changed since " + getLastTimestamp(0, PREF_KEY_CHANGE)
            saveLastTimestamp(0, PREF_KEY_DELETE)
            mDeleteButton!!.text = "Deleted since " + getLastTimestamp(0, PREF_KEY_DELETE)
        }
        main.addView(mChangeButton)
        main.addView(mDeleteButton)
        main.addView(mClearPreferences)
        mDisplayView = TextView(this)
        mDisplayView!!.setPadding(5, 5, 5, 5)
        main.addView(mDisplayView)
        mList = ListView(this)
        mList!!.layoutParams = LinearLayout.LayoutParams(WRAP, WRAP, 1f)
        main.addView(mList)
        setContentView(main)
    }

    /**
     * Called after [.onRestoreInstanceState], [.onRestart], or
     * [.onPause], for your activity to start interacting with the user.
     * This is a good place to begin animations, open exclusive-access devices
     * (such as the camera), etc.
     *
     *
     * First we call through to our super's implementation of `onResume`. Then we create
     * `IntentFilter filter`, set its action to CONTACTS_DATABASE_CREATED and register our
     * `BroadcastReceiver mReceiver` field to receive broadcasts that match `filter`.
     */
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction(ContactsContract.Intents.CONTACTS_DATABASE_CREATED)
        registerReceiver(mReceiver, filter)
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed. The counterpart to
     * [.onResume].
     *
     *
     * First we call through to our super's implementation of `onPause`, then we unregister
     * our field `BroadcastReceiver mReceiver` as a broadcast receiver.
     */
    override fun onPause() {
        super.onPause()
        unregisterReceiver(mReceiver)
    }

    /**
     * `OnClickListener` for the `Button mChangeButton`, it causes the ID_CHANGE_LOADER
     * `CursorLoader` to re-fetch its data using the latest timestamp.
     */
    private fun changeClick() {
        mChangeAdapter!!.swapCursor(null)
        val manager = loaderManager
        manager.destroyLoader(ID_DELETE_LOADER)
        manager.restartLoader(ID_CHANGE_LOADER, Bundle(), this)
    }

    /**
     * `OnClickListener` for the `Button mDeleteButton`, it causes the ID_DELETE_LOADER
     * `CursorLoader` to re-fetch its data using the latest timestamp.
     */
    private fun deleteClick() {
        mDeleteAdapter!!.swapCursor(null)
        val manager = loaderManager
        manager.destroyLoader(ID_CHANGE_LOADER)
        manager.restartLoader(ID_DELETE_LOADER, Bundle(), this)
    }

    /**
     * Saves a timestamp `long time` in the shared preferences file under the key `String key`
     * (PREF_KEY_CHANGE or PREF_KEY_DELETE in our case). First we retrieve a `SharedPreferences pref`
     * for our CLASS name ("ChangedContacts"), and we create an `SharedPreferences.Editor editor`
     * for `pref`. We use `editor` to store our parameter `time` under the key `key`
     * and commit the change to the preference file.
     *
     * @param time timestamp to save in shared preferences file
     * @param key  key to save the timestamp under
     */
    @SuppressLint("ApplySharedPref")
    private fun saveLastTimestamp(time: Long, key: String) {
        val pref = getSharedPreferences(CLASS, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putLong(key, time)
        editor.commit()
    }

    /**
     * Retrieves a timestamp `long time` from the shared preferences file that was stored under the
     * key `String key` (PREF_KEY_CHANGE or PREF_KEY_DELETE in our case). First we retrieve a
     * `SharedPreferences pref` for our CLASS name ("ChangedContacts"), and we use it to retrieve
     * and return the value stored under the key `key` (defaulting to our parameter `time`
     * if none was stored yet.)
     *
     * @param time timestamp to save in shared preferences file
     * @param key  key to save the timestamp under
     */
    @Suppress("SameParameterValue")
    private fun getLastTimestamp(time: Long, key: String): Long {
        val pref = getSharedPreferences(CLASS, Context.MODE_PRIVATE)
        return pref.getLong(key, time)
    }

    /**
     * Instantiate and return a new Loader for the given ID. We switch on the `id` parameter
     * and return the `CursorLoader` created by the appropriate method:
     *
     *  * ID_CHANGE_LOADER -- `getChangeLoader()`
     *  * ID_DELETE_LOADER -- `getDeleteLoader()`
     *
     * This is called by the `LoaderManager` for this activity as a callback as a result of
     * a call to `restartLoader` (start a new or restarts an existing Loader, register the
     * callbacks -- "this" in our case. See `changeClick` and `deleteClick`.)
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    override fun onCreateLoader(id: Int, args: Bundle): Loader<Cursor>? {
        when (id) {
            ID_CHANGE_LOADER -> return changeLoader
            ID_DELETE_LOADER -> return deleteLoader
        }
        return null
    }

    /**
     * Creates a `CursorLoader` configured to retrieve contacts from the contacts provider which
     * have been changed after the timestamp `mSearchTime`. First we create `String[] projection`
     * containing the names of the columns we want to retrieve: _ID, CONTACT_ID, DISPLAY_NAME and
     * CONTACT_LAST_UPDATED_TIMESTAMP. Then we fetch `mSearchTime` from the value stored in the
     * preferences file under the key PREF_KEY_CHANGE. We create `String selection` as the selection
     * query string, requesting that the CONTACT_LAST_UPDATED_TIMESTAMP be greater than the value supplied
     * by the selection arguments `String[] bindArgs` (`mSearchTime` formatted as a String).
     * Finally we return a `CursorLoader` configured for this selection criteria, and sorted
     * in descending order based on the CONTACT_LAST_UPDATED_TIMESTAMP, and CONTACT_ID columns.
     *
     * @return `CursorLoader` configured to retrieve selected contacts from the contacts provider
     */
    private val changeLoader: CursorLoader
        get() {
            val projection = arrayOf(
                    ContactsContract.Data._ID,
                    ContactsContract.Data.CONTACT_ID,
                    ContactsContract.Data.DISPLAY_NAME,
                    ContactsContract.Data.CONTACT_LAST_UPDATED_TIMESTAMP
            )
            mSearchTime = getLastTimestamp(0, PREF_KEY_CHANGE)
            val selection = ContactsContract.Data.CONTACT_LAST_UPDATED_TIMESTAMP + " > ?"
            val bindArgs = arrayOf(mSearchTime.toString() + "")
            return CursorLoader(this, ContactsContract.Data.CONTENT_URI, projection,
                    selection, bindArgs, ContactsContract.Data.CONTACT_LAST_UPDATED_TIMESTAMP
                    + " desc, " + ContactsContract.Data.CONTACT_ID + " desc")
        }

    /**
     * Creates a `CursorLoader` configured to retrieve contacts from the contacts provider deleted
     * contact table which have been deleted after the timestamp `mSearchTime`. First we create
     * `String[] projection` containing the names of the columns we want to retrieve: _ID, and
     * CONTACT_DELETED_TIMESTAMP. Then we fetch `mSearchTime` from the value stored in the
     * preferences file under the key PREF_KEY_DELETE. We create `String selection` as the selection
     * query string, requesting that the CONTACT_DELETED_TIMESTAMP be greater than the value supplied
     * by the selection arguments `String[] bindArgs` (`mSearchTime` formatted as a String).
     * Finally we return a `CursorLoader` configured for this selection criteria, and sorted
     * in descending order based on the CONTACT_DELETED_TIMESTAMP column.
     *
     * @return `CursorLoader` configured to retrieve selected contacts from the contacts provider
     * deleted contact table. (This table holds a log of deleted contacts.)
     */
    private val deleteLoader: CursorLoader
        get() {
            val projection = arrayOf(
                    ContactsContract.DeletedContacts.CONTACT_ID,
                    ContactsContract.DeletedContacts.CONTACT_DELETED_TIMESTAMP
            )
            mSearchTime = getLastTimestamp(0, PREF_KEY_DELETE)
            val selection = ContactsContract.DeletedContacts.CONTACT_DELETED_TIMESTAMP + " > ?"
            val bindArgs = arrayOf(mSearchTime.toString() + "")
            return CursorLoader(this, ContactsContract.DeletedContacts.CONTENT_URI, projection,
                    selection, bindArgs, ContactsContract.DeletedContacts.CONTACT_DELETED_TIMESTAMP +
                    " desc")
        }

    /**
     * Called when a previously created loader has finished its load. First we initialize `timestamp`
     * to zero, then we switch based on the ID of the loader:
     *
     *  *
     * ID_CHANGE_LOADER - we set the text of `TextView mDisplayView` to display the number of
     * rows ("Changes") in the cursor and the value of `mSearchTime` we used to filter for
     * changes. We set the adapter for `ListView mList` to `ChangeAdapter mChangeAdapter`,
     * and instruct `mChangeAdapter` to swap in the new `Cursor data`. We try to move
     * `data` to the first row and if successful set `timestamp` to the value stored in
     * the column CONTACT_LAST_UPDATED_TIMESTAMP, then move `data` back to the previous row.
     * If `timestamp` is now nonzero we save its value as the new value for the key PREF_KEY_CHANGE
     * in our preference file and update the text of the `Button mChangeButton` to reflect the
     * new time that will be used as the filter when the Button is clicked. (Because of the descending
     * sort of the cursor, the timestamp of the first row will be the latest.)
     *
     *  *
     * ID_DELETE_LOADER - we set the text of `TextView mDisplayView` to display the number of
     * rows ("deletes") in the cursor and the value of `mSearchTime` we used to filter for
     * deletes. We set the adapter for `ListView mList` to `DeleteAdapter mDeleteAdapter`,
     * and instruct `mChangeAdapter` to swap in the new `Cursor data` wrapped in a new
     * instance of `DeleteCursorWrapper`. We try to move `data` to the first row and if
     * successful set `timestamp` to the value stored in the column CONTACT_DELETED_TIMESTAMP,
     * then move `data` back to the previous row. If `timestamp` is now nonzero we save
     * its value as the new value for the key PREF_KEY_DELETE in our preference file and update the
     * text of the `Button mDeleteButton` to reflect the new time that will be used as the
     * filter when the Button is clicked. (Because of the descending sort of the cursor, the timestamp
     * of the first row will be the latest.)
     *
     *
     *
     * @param cursorLoader The Loader that has finished.
     * @param data         The data generated by the Loader.
     */
    @SuppressLint("SetTextI18n")
    override fun onLoadFinished(cursorLoader: Loader<Cursor>, data: Cursor) {
        var timestamp: Long = 0
        when (cursorLoader.id) {
            ID_CHANGE_LOADER -> {
                mDisplayView!!.text = data.count.toString() + " change(s) since " + mSearchTime
                mList!!.adapter = mChangeAdapter
                mChangeAdapter!!.swapCursor(data)
                // Save the largest timestamp returned.  Only need the first one due to the sort
// order.
                if (data.moveToNext()) {
                    timestamp = data.getLong(data.getColumnIndex(ContactsContract.Data.CONTACT_LAST_UPDATED_TIMESTAMP))
                    data.moveToPrevious()
                }
                if (timestamp > 0) {
                    saveLastTimestamp(timestamp, PREF_KEY_CHANGE)
                    mChangeButton!!.text = "Changed since $timestamp"
                }
            }
            ID_DELETE_LOADER -> {
                mDisplayView!!.text = data.count.toString() + " delete(s) since " + mSearchTime
                mList!!.adapter = mDeleteAdapter
                mDeleteAdapter!!.swapCursor(DeleteCursorWrapper(data))
                if (data.moveToNext()) {
                    timestamp = data.getLong(data.getColumnIndex(ContactsContract.DeletedContacts.CONTACT_DELETED_TIMESTAMP))
                    data.moveToPrevious()
                }
                if (timestamp > 0) {
                    saveLastTimestamp(timestamp, PREF_KEY_DELETE)
                    mDeleteButton!!.text = "Deleted since $timestamp"
                }
            }
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     *
     * We set the text of `TextView mDisplayView` to the empty string, then switch based on the
     * ID of `Loader<Cursor> cursorLoader`:
     *
     *  * ID_CHANGE_LOADER - we swap in a null Cursor for `ChangeAdapter mChangeAdapter`
     *  * ID_DELETE_LOADER - we swap in a null Cursor for `ChangeAdapter mDeleteAdapter`
     *
     *
     * @param cursorLoader The Loader that is being reset.
     */
    override fun onLoaderReset(cursorLoader: Loader<Cursor>) {
        mDisplayView!!.text = ""
        when (cursorLoader.id) {
            ID_CHANGE_LOADER -> mChangeAdapter!!.swapCursor(null)
            ID_DELETE_LOADER -> mDeleteAdapter!!.swapCursor(null)
        }
    }

    /**
     * Wrapper class for Cursor that delegates all calls to the actual cursor object. We use this to
     * extend a cursor while overriding only the method `getColumnIndexOrThrow`.
     */
    private inner class DeleteCursorWrapper
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */(cursor: Cursor?) : CursorWrapper(cursor) {
        /**
         * The super's implementation of this returns the zero-based index for the given column name,
         * or throws [IllegalArgumentException] if the column doesn't exist.
         *
         *
         * We override this in order to replace attempts to reference the `columnName` "_id"
         * (which does not exit in our data) with a call to our super's implementation of
         * `getColumnIndex` asking for the column name CONTACT_ID ("contact_id") which serves
         * the same purpose in our data set. If the call is for another column, we simply pass the
         * call through to our super's implementation of `getColumnIndex`.
         *
         *
         * No exception is thrown even if the column does not exist (-1 is returned instead).
         *
         * @param columnName the name of the target column.
         * @return the zero-based column index for the given column name
         */
        override fun getColumnIndexOrThrow(columnName: String): Int {
            return if (columnName == "_id") {
                super.getColumnIndex(ContactsContract.DeletedContacts.CONTACT_ID)
            } else super.getColumnIndex(columnName)
        }
    }

    /**
     * A subclass of `CursorAdapter` customized to display the contents of a `DeleteCursorWrapper`
     * wrapped `CursorLoader` configured to retrieve the deleted contact table of the contacts
     * provider.
     */
    private class DeleteAdapter
    /**
     * Recommended constructor. First we call through to our super's constructor, then we save
     * the `Context context` parameter in our field `Context mContext` for later use.
     *
     * @param c       The cursor from which to get the data.
     * @param mContext The context
     * @param flags   Flags used to determine the behavior of the adapter; may
     * be any combination of [.FLAG_AUTO_REQUERY] and
     * [.FLAG_REGISTER_CONTENT_OBSERVER].
     */(
            /**
             * Context passed to our constructor, used to create a `LinearLayout` to hold the
             * `TextView`'s we display individual items in.
             */
            private val mContext: Context, c: Cursor?, flags: Int) : CursorAdapter(mContext, c, flags) {

        /**
         * Makes a new view to hold the data pointed to by cursor. We create a `LinearLayout item`
         * then we add two `TextView`'s to it created by our method `buildText(context)`.
         * Finally we return `item` to the caller.
         *
         * @param context Interface to application's global information
         * @param cursor  The cursor from which to get the data. The cursor is already
         * moved to the correct position.
         * @param parent  The parent to which the new view is attached to
         * @return the newly created view.
         */
        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            val item = LinearLayout(mContext)
            item.addView(buildText(context))
            item.addView(buildText(context))
            return item
        }

        /**
         * Bind an existing view to the data pointed to by cursor. We cast our parameter `View view`
         * to `LinearLayout item`. Next we fetch `String id` from `Cursor cursor` column
         * CONTACT_ID, and `String timestamp` from its column CONTACT_DELETED_TIMESTAMP. We use
         * `id` to set the text of child 0 of `item`, and `timestamp` to set
         * the text of child 1.
         *
         * @param view    Existing view, returned earlier by newView
         * @param context Interface to application's global information
         * @param cursor  The cursor from which to get the data. The cursor is already
         * moved to the correct position.
         */
        override fun bindView(view: View, context: Context, cursor: Cursor) {
            val item = view as LinearLayout
            val id = cursor.getString(cursor.getColumnIndex(ContactsContract.DeletedContacts.CONTACT_ID))
            val timestamp = cursor.getString(cursor.getColumnIndex(ContactsContract.DeletedContacts.CONTACT_DELETED_TIMESTAMP))
            setText(item.getChildAt(0), id)
            setText(item.getChildAt(1), timestamp)
        }

    }

    /**
     * A subclass of `CursorAdapter` customized to display the contents of a `CursorLoader`
     * configured to retrieve the "changed after" contacts from the contacts provider
     */
    private class ChangeAdapter
    /**
     * Recommended constructor. First we call through to our super's constructor, then we save
     * the `Context context` parameter in our field `Context mContext` for later use.
     *
     * @param c       The cursor from which to get the data.
     * @param mContext The context
     * @param flags   Flags used to determine the behavior of the adapter; may
     * be any combination of [.FLAG_AUTO_REQUERY] and
     * [.FLAG_REGISTER_CONTENT_OBSERVER].
     */(
            /**
             * Context passed to our constructor, used to create a `LinearLayout` to hold the
             * `TextView`'s we display individual items in.
             */
            private val mContext: Context, c: Cursor?, flags: Int) : CursorAdapter(mContext, c, flags) {

        /**
         * Makes a new view to hold the data pointed to by cursor. We create a `LinearLayout item`
         * then we add three `TextView`'s to it created by our method `buildText(context)`.
         * Finally we return `item` to the caller.
         *
         * @param context Interface to application's global information
         * @param cursor  The cursor from which to get the data. The cursor is already
         * moved to the correct position.
         * @param parent  The parent to which the new view is attached to
         * @return the newly created view.
         */
        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            val item = LinearLayout(mContext)
            item.addView(buildText(context))
            item.addView(buildText(context))
            item.addView(buildText(context))
            return item
        }

        /**
         * Bind an existing view to the data pointed to by cursor. We cast our parameter `View view`
         * to `LinearLayout item`. Next we fetch `String id` from `Cursor cursor` column
         * CONTACT_ID, `String name` form column DISPLAY_NAME and `String timestamp` from
         * its column CONTACT_LAST_UPDATED_TIMESTAMP. We use `id` to set the text of child 0 of
         * `item`, `name` to set the text of child 1, and `timestamp` to set the text
         * of child 2.
         *
         * @param view    Existing view, returned earlier by newView
         * @param context Interface to application's global information
         * @param cursor  The cursor from which to get the data. The cursor is already
         * moved to the correct position.
         */
        override fun bindView(view: View, context: Context, cursor: Cursor) {
            val item = view as LinearLayout
            val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID))
            val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
            val timestamp = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_LAST_UPDATED_TIMESTAMP))
            setText(item.getChildAt(0), id)
            setText(item.getChildAt(1), name)
            setText(item.getChildAt(2), timestamp)
        }

    }

    companion object {

        const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
        /**
         * Used for the preferences file 'name' when accessing shared preferences
         */
        private val CLASS = ChangedContacts::class.java.simpleName
        /**
         * Preference file key for the timestamp of the latest change to the contact database (Starts at 0)
         * and is updated when the database is read for the first time
         */
        private const val PREF_KEY_CHANGE = "timestamp_change"
        /**
         * Preference file key for the timestamp of the latest delete from the contact database (Starts at 0)
         * and is updated when the database is read for the first time
         */
        private const val PREF_KEY_DELETE = "timestamp_delete"
        /**
         * ID for the `CursorLoader` used to feed data about changed contacts to fill `ListView mList`
         */
        private const val ID_CHANGE_LOADER = 1
        /**
         * ID for the `CursorLoader` used to feed data about deleted contacts to fill `ListView mList`
         */
        private const val ID_DELETE_LOADER = 2

        /**
         * Convenience function that casts the `View view` returned by a call to `getChildAt` to
         * a `TextView text`, and then sets the text of `text` to our parameter `value`.
         *
         * @param view  `View` of `TextView` whose text we wish to set
         * @param value `String` to set the text to
         */
        private fun setText(view: View, value: String) {
            val text = view as TextView
            text.text = value
        }

        /**
         * Convenience function to create a `TextView` and set its padding to 3 pixels on each side.
         *
         * @param context `Context` to use when constructing our `TextView`
         * @return a configured `TextView`
         */
        private fun buildText(context: Context): TextView {
            val view = TextView(context)
            view.setPadding(3, 3, 3, 3)
            return view
        }
    }
}