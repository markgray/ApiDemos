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
@file:Suppress("DEPRECATION")
// TODO: replace AsyncTask with coroutine approach.
package com.example.android.apis.app

import android.annotation.SuppressLint
import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.BaseColumns
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.ListFragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.example.android.apis.app.LoaderThrottle.MainTable.Companion.CONTENT_URI
import com.example.android.apis.graphics.Utilities.id2p

/**
 * Demonstration of bottom to top implementation of a content provider holding
 * structured data through displaying it in the UI, using throttling to reduce
 * the number of queries done when its data changes. Implements a custom [CursorLoader]
 * which pretends to be a SQLite database, and simulates a slow provider of data
 * [SimpleProvider]
 */
@Suppress("MemberVisibilityCanBePrivate")
class LoaderThrottle : AppCompatActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. We fetch a handle to the support `FragmentManager` for interacting with fragments
     * associated with this activity to initialize our variable `val fm`. Then we use `fm` to search
     * for a fragment with the ID android.R.id.content (the root view) and if one is found to already
     * exist, we do nothing and return. If the result of the search was *null* however, we create a
     * new instance of [ThrottledLoaderListFragment] to initialize our variable  `val list` and use
     * `fm` to begin a `FragmentTransaction` which we use to add `list` to the activity state using
     * the ID android.R.id.content and commit the transaction.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use this
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fm = supportFragmentManager

        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            val list = ThrottledLoaderListFragment()
            fm.beginTransaction().add(android.R.id.content, list).commit()
        }
    }

    /**
     * Definition of the contract for the main table of our provider.
     */
    class MainTable
    /**
     * private so that this class cannot be instantiated
     */
    private constructor() : BaseColumns {
        companion object {

            /**
             * The table name offered by this provider
             */
            const val TABLE_NAME: String = "main"

            /**
             * The content:// style URL for this table:
             * "content://com.example.android.apis.app.LoaderThrottle/main"
             */
            val CONTENT_URI: Uri = "content://$AUTHORITY/main".toUri()

            /**
             * The content URI base for a single row of data. Callers must
             * append a numeric row id to this Uri to retrieve a row:
             * "content://com.example.android.apis.app.LoaderThrottle/main/"
             */
            val CONTENT_ID_URI_BASE: Uri = "content://$AUTHORITY/main/".toUri()

            /**
             * The MIME type of [CONTENT_URI].
             */
            const val CONTENT_TYPE: String = "vnd.android.cursor.dir/vnd.example.api-demos-throttle"

            /**
             * The MIME type of a [CONTENT_URI] sub-directory of a single row.
             */
            const val CONTENT_ITEM_TYPE: String =
                "vnd.android.cursor.item/vnd.example.api-demos-throttle"

            /**
             * The default sort order for this table
             */
            const val DEFAULT_SORT_ORDER: String = "data COLLATE LOCALIZED ASC"

            /**
             * Column name for the single column holding our data. Type: TEXT
             */
            const val COLUMN_NAME_DATA: String = "data"
        }
    }

    /**
     * This class helps open, create, and upgrade the database file.
     * Constructor which simply calls the super's constructor to create an [SQLiteOpenHelper]
     * using our DATABASE_NAME, DATABASE_VERSION, and requesting the default cursor factory.
     *
     * @param context [Context] the [SimpleProvider] subclass of [ContentProvider] is running in
     */
    internal class DatabaseHelper(context: Context) :
        SQLiteOpenHelper(
            context,
            DATABASE_NAME,
            null,
            DATABASE_VERSION
        ) {

        /**
         * Called when the database is created for the first time. This is where the
         * creation of tables and the initial population of the tables should happen.
         * Creates the underlying database with table name and column names taken from the
         * NotePad class. We simply use our [SQLiteDatabase] parameter [db] to execute the
         * single SQL statement which uses the "CREATE TABLE" command to create a new table in our
         * SQLite database [db], with the name given by MainTable.TABLE_NAME ("main"), the
         * columns given by:
         *
         *  * BaseColumns._ID ("_id") an INTEGER PRIMARY KEY column
         *  * MainTable.COLUMN_NAME_DATA ("TEXT") a TEXT column
         *
         * @param db The database.
         */
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE " + MainTable.TABLE_NAME + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY,"
                    + MainTable.COLUMN_NAME_DATA + " TEXT"
                    + ");"
            )
        }

        /**
         * Called when the database needs to be upgraded. The implementation
         * should use this method to drop tables, add tables, or do anything else it
         * needs to upgrade to the new schema version.
         *
         * The SQLite ALTER TABLE documentation can be found at http://sqlite.org/lang_altertable.html
         * If you add new columns you can use ALTER TABLE to insert them into a live table. If you
         * rename or remove columns you can use ALTER TABLE to rename the old table, then create the
         * new table and then populate the new table with the contents of the old table.
         *
         * This method executes within a transaction.  If an exception is thrown, all changes
         * will automatically be rolled back.
         *
         * Our implementation demonstrates that the provider must consider what happens when the
         * underlying data store is changed. In this sample, the database is upgraded by destroying
         * the existing data then calling [onCreate]. A real application should upgrade the
         * database in place.
         *
         * First we execute the SQL command "DROP TABLE IF EXISTS notes", which removes the table
         * "notes" added with the CREATE TABLE statement. The dropped table is completely removed
         * from the database schema and the disk file. The table can not be recovered. All indices
         * and triggers associated with the table are also deleted. The optional IF EXISTS clause
         * suppresses the error that would normally result if the table does not exist. Then we call
         * our callback [onCreate] which recreates the table
         *
         * @param db         The database.
         * @param oldVersion The old database version.
         * @param newVersion The new database version.
         */
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

            // Logs that the database is being upgraded
            Log.w(
                TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data"
            )

            // Kills the table and existing data
            db.execSQL("DROP TABLE IF EXISTS notes")

            // Recreates the database with a new version
            onCreate(db)
        }

        /**
         * Our static constants.
         */
        companion object {

            /**
             * name of the database file.
             */
            private const val DATABASE_NAME = "loader_throttle.db"

            /**
             * Version number of the database
             */
            private const val DATABASE_VERSION = 2
        }
    }

    /**
     * A very simple implementation of a content provider. It is specified in AndroidManifest.xml
     * using the `<provider>` element, and the attributes:
     *  * android:name=".app.LoaderThrottle$SimpleProvider" The name of the class that implements
     *  the content provider, a subclass of [ContentProvider]. This should be a fully qualified
     *  class name, however, as a shorthand, if the first character of the name is a period, it is
     *  appended to the package name specified in the `<manifest>` element.
     *  * android:authorities="com.example.android.apis.app.LoaderThrottle" A list of one or more
     *  URI authorities that identify data offered by the content provider. Multiple authorities
     *  are listed by separating their names with a semicolon. To avoid conflicts, authority names
     *  should use a Java-style naming convention (such as com.example.provider.cartoonprovider).
     *  Typically, it's the name of the ContentProvider subclass that implements the provider.
     *  There is no default. At least one authority must be specified. It is used by our
     *  [CursorLoader] to connect to this provider.
     *  * android:enabled="@bool/atLeastHoneycomb" Whether or not the content provider can be
     *  instantiated by the system — "true" if it can be, and "false" if not. This makes the
     *  provider available only on Android versions Honeycomb and newer.
     */
    class SimpleProvider : ContentProvider() {
        /**
         * A projection map used to select columns from the database
         */
        private val mNotesProjectionMap: HashMap<String, String>

        /**
         * Uri matcher to decode incoming URIs.
         */
        private val mUriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        /**
         * Handle to a new DatabaseHelper.
         */
        private var mOpenHelper: DatabaseHelper? = null

        /**
         * Global provider initialization. We initialize our `UriMatcher` field `mUriMatcher` with
         * a new instance of `UriMatcher` with the code to match for the root URI specified as
         * UriMatcher.NO_MATCH (a code to specify that a Uri can not match the root), and we add a
         * Uri to `mUriMatcher` to match the authority AUTHORITY:
         * ("com.example.android.apis.app.LoaderThrottle"), TABLE_NAME ("main"), with MAIN (1) the
         * code that is returned when a URI matches, and a Uri to match the authority AUTHORITY for
         * table "main/#", with MAIN_ID (2) the code that is returned when a URI matches it. We
         * initialize our `HashMap<String, String>` field `mNotesProjectionMap` with an empty
         * `HashMap<>`, then put the String MainTable._ID to map to itself, and the String
         * MainTable.COLUMN_NAME_DATA to map to itself.
         */
        init {
            // Create and initialize URI matcher.
            mUriMatcher.addURI(AUTHORITY, MainTable.TABLE_NAME, MAIN)
            mUriMatcher.addURI(AUTHORITY, MainTable.TABLE_NAME + "/#", MAIN_ID)

            // Create and initialize projection map for all columns.  This is
            // simply an identity mapping.
            mNotesProjectionMap = HashMap()
            mNotesProjectionMap[BaseColumns._ID] = BaseColumns._ID
            mNotesProjectionMap[MainTable.COLUMN_NAME_DATA] = MainTable.COLUMN_NAME_DATA
        }

        /**
         * Initialize our content provider on startup. This method is called for all registered
         * content providers on the application main thread at application launch time. It must
         * not perform lengthy operations, or application startup will be delayed.
         *
         * We simply initialize our [DatabaseHelper] field [mOpenHelper] with a new instance of
         * [DatabaseHelper] using the [Context] this provider is running in as the [Context],
         * and return *true* to our caller.
         *
         * @return true if the provider was successfully loaded, false otherwise
         */
        override fun onCreate(): Boolean {
            mOpenHelper = DatabaseHelper(context!!)
            // Assumes that any failures will be reported by a thrown exception.
            return true
        }

        /**
         * Handle incoming query requests from clients. These queries are generated by the
         * [CursorLoader] created in [ThrottledLoaderListFragment] based on the AUTHORITY
         * "com.example.android.apis.app.LoaderThrottle". The AndroidManifest.xml attribute
         * android:authorities in the `<provider>` element for the provider which is named using
         * the attribute android:name=".app.LoaderThrottle$SimpleProvider" (which is this class)
         * refers the [CursorLoader] to call this method when requesting more data.
         * [ThrottledLoaderListFragment] returns the [CursorLoader] it creates from its
         * `onCreateLoader` callback, and it is then used by the system to fill our [ListView].
         *
         * First we initialize our variable `var selectionArgsLocal` to our parameter [selectionArgs]
         * and our variable `var sortOrderLocal` to our parameter [sortOrder] (a kotlin quirk makes
         * this necessary if we want to modify them for local useage). Then we create a new instance
         * of [SQLiteQueryBuilder] to initialize our variable `val qb` and set the list of tables to
         * query to `MainTable.TABLE_NAME` ("main" the only table in our pretend database). Then we
         * switch based on the return value of our [UriMatcher] field [mUriMatcher] when matching
         * the [Uri] parameter [uri] (our app only uses the MAIN [Uri], but the MAIN_ID option is
         * included for completeness (probably because all this code was pasted from another app)).
         * In the "MAIN" case we call the `setProjectionMap` method of `qb` to set the projection
         * map for the query to the `HashMap<String, String>` field [mNotesProjectionMap]. The
         * projection map maps from column names that the caller passes into [query] to database
         * column names. This is useful for renaming columns as well as disambiguating column names
         * when doing joins. For example you could map "name" to "people.name". If a projection map
         * is set it must contain all column names the user may request, even if the key and value
         * are the same, and in our case there are two entries MainTable._ID and
         * MainTable.COLUMN_NAME_DATA and both entries point to themselves (more code pasting?).
         * In the MAIN_ID case we also set the projection map to [mNotesProjectionMap], then we
         * append the chunk: MainTable._ID + "=?"  to the WHERE clause of the query, and append the
         * last path segment of the [Uri] parameter [uri] to the `String[]` parameter copy of
         * [selectionArgs] we have in `selectionArgsLocal`. (But since the MAIN_ID case never occurs
         * in our app, this is all just academic IMO). In either case we continue by setting the
         * `sortOrderLocal` copy of our parameter [sortOrder] to MainTable.DEFAULT_SORT_ORDER if it
         * was empty. Next we use our [DatabaseHelper] field [mOpenHelper] to open its readable
         * [SQLiteDatabase] to initialize our variable `val db`, create a [Cursor] to initialize our
         * variable `val c` which performs a query of `db` and then Register `c` to watch the content
         * [Uri] parameter [uri] for changes. (This can be the URI of a specific data row (for
         * example, "content://my_provider_type/23"), or a a generic URI for a content type.)
         * Finally we return `c` to the caller.
         *
         * @param uri The URI to query. This will be the full URI sent by the client; if the client
         * is requesting a specific record, the URI will end in a record number that the
         * implementation should parse and add to a WHERE or HAVING clause, specifying that _id value.
         * @param projection The list of columns to put into the cursor. If *null* all
         * columns are included.
         * @param selection A selection criteria to apply when filtering rows. If *null*
         * then all rows are included.
         * @param selectionArgs You may include ?'s in [selection], which will be replaced by the
         * values from [selectionArgs], in order that they appear in [selection]. The values will
         * be bound as Strings.
         * @param sortOrder How the rows in the cursor should be sorted. If *null* then the provider
         * is free to define the sort order.
         * @return a [Cursor] or *null*.
         */
        override fun query(
            uri: Uri, projection: Array<String>?, selection: String?,
            selectionArgs: Array<String>?, sortOrder: String?
        ): Cursor? {
            var selectionArgsLocal = selectionArgs
            var sortOrderLocal = sortOrder

            // Constructs a new query builder and sets its table name
            val qb = SQLiteQueryBuilder()
            qb.tables = MainTable.TABLE_NAME

            when (mUriMatcher.match(uri)) {
                MAIN ->
                    // If the incoming URI is for main table.
                    qb.projectionMap = mNotesProjectionMap

                MAIN_ID -> {
                    // The incoming URI is for a single row.
                    qb.projectionMap = mNotesProjectionMap
                    qb.appendWhere(BaseColumns._ID + "=?")
                    selectionArgsLocal = DatabaseUtils.appendSelectionArgs(
                        selectionArgsLocal,
                        arrayOf(uri.lastPathSegment!!)
                    )
                }

                else -> throw IllegalArgumentException("Unknown URI $uri")
            }


            if (TextUtils.isEmpty(sortOrderLocal)) {
                sortOrderLocal = MainTable.DEFAULT_SORT_ORDER
            }

            val db = mOpenHelper!!.readableDatabase

            val c = qb.query(
                db,
                projection,
                selection,
                selectionArgsLocal,
                null,
                null,
                sortOrderLocal
            )/* no group *//* no filter */


            c.setNotificationUri(context!!.contentResolver, uri)
            return c
        }

        /**
         * Implement this to handle requests for the MIME type of the data at the given URI. The
         * returned MIME type should start with `vnd.android.cursor.item` for a single record,
         * or `vnd.android.cursor.dir/` for multiple items.
         *
         * Based on the matching or the `Uri uri` using our `UriMatcher mUriMatcher` we
         * return:
         *
         *  * MAIN - MainTable.CONTENT_TYPE ("vnd.android.cursor.dir/vnd.example.api-demos-throttle"
         *  * MAIN_ID - MainTable.CONTENT_ITEM_TYPE ("vnd.android.cursor.item/vnd.example.api-demos-throttle")
         *  * no match throws an [IllegalArgumentException]
         *
         * @param uri the URI to query.
         * @return a MIME type string, or *null* if there is no type.
         */
        override fun getType(uri: Uri): String {
            return when (mUriMatcher.match(uri)) {
                MAIN -> MainTable.CONTENT_TYPE
                MAIN_ID -> MainTable.CONTENT_ITEM_TYPE
                else -> throw IllegalArgumentException("Unknown URI $uri")
            }
        }

        /**
         * Implement this to handle requests to insert a new row. As a courtesy, call
         * [notifyChange()][ContentResolver.notifyChange] after inserting.
         *
         * Called from [ContentResolver.insert] which is called from our background data
         * generating thread which is started using the "populate" button on the menu.
         *
         * First we check to make sure that our [Uri] parameter [uri] is of the correct type
         * (our `UriMatcher mUriMatcher` matches it to MAIN (its AUTHORITY is
         * "com.example.android.apis.app.LoaderThrottle" and its path is "main")). If it is not a
         * reference to our main URI the [require] method throws an [IllegalArgumentException].
         *
         * Next we initialize the [ContentValues] variable `val values`, either with the value of
         * our [ContentValues] parameter [initialValues], or an new empty set of values if
         * [initialValues] is *null* (it is never *null* in our app BTW).
         *
         * If `values` does NOT contain a column with the key COLUMN_NAME_DATA ("data") we put
         * the empty String in `values` under that key (another probable legacy from code pasting
         * as it never lacks a value for that column key in our app).
         *
         * We then open (or create) a [SQLiteDatabase] to initialize our variable `val db`, and try
         * to insert the row that `values` contains into `db` saving the row ID of the newly inserted
         * row (or -1 if an error occurred) in our [Long] variable `val rowId`. If no error occurred
         * (`rowId > 0`) we create a [Uri] to initialize our variable `val noteUri` by appending
         * `rowId` to our content URI base for a single row of data (MainTable.CONTENT_ID_URI_BASE),
         * notify registered observers that a row was updated and attempt to sync changes to the
         * network, and finally return `noteUri` to the caller. If an error had occurred
         * (`rowId == -1`) we throw an [SQLException] with an appropriate message.
         *
         * See:
         *  * https://developer.android.com/reference/android/content/ContentProvider.html
         *  * https://developer.android.com/reference/android/content/ContentValues.html
         *
         * @param uri           The content:// URI of the insertion request. This must not be `null`.
         * @param initialValues A set of column_name/value pairs to add to the database.
         * This must not be `null`.
         * @return The URI for the newly inserted item.
         */
        override fun insert(uri: Uri, initialValues: ContentValues?): Uri {
            require(mUriMatcher.match(uri) == MAIN) { // Can only insert into to main URI.
                "Unknown URI $uri"
            }

            val values: ContentValues = if (initialValues != null) {
                ContentValues(initialValues)
            } else {
                ContentValues()
            }

            if (!values.containsKey(MainTable.COLUMN_NAME_DATA)) {
                values.put(MainTable.COLUMN_NAME_DATA, "")
            }

            val db = mOpenHelper!!.writableDatabase

            val rowId = db.insert(MainTable.TABLE_NAME, null, values)

            // If the insert succeeded, the row ID exists.
            if (rowId > 0) {
                val noteUri = ContentUris.withAppendedId(MainTable.CONTENT_ID_URI_BASE, rowId)

                context!!.contentResolver.notifyChange(noteUri, null)
                return noteUri
            }

            throw SQLException("Failed to insert row into $uri")
        }

        /**
         * Implement this to handle requests to delete one or more rows. The implementation should
         * apply the selection clause when performing deletion, allowing the operation to affect
         * multiple rows in a directory. As a courtesy, call
         * [notifyChange()][ContentResolver.notifyChange] after deleting.
         *
         * The implementation is responsible for parsing out a row ID at the end of the URI, if a
         * specific row is being deleted. That is, the client would pass in
         * `content://contacts/people/22` and the implementation is responsible for parsing the
         * record number (22) when creating a SQL statement.
         *
         * First we open our [SQLiteDatabase] to initialize our variable `val db`, then we declare
         * local [String] variable `val finalWhere` (used for Uri's containing a row ID, case
         * MAIN_ID:, it will be a SQL WHERE clause we build which includes the ID contained in the
         * [String] parameter [where] passed us), and declare [Int] varible `val count` (used to
         * save the return value of [SQLiteDatabase.delete] so we can return it to the caller).
         *
         * Next we branch based on the matching of our [Uri] parameter [uri] using our [UriMatcher]
         * field [mUriMatcher]:
         *  * MAIN - we call the `delete` method of our [SQLiteDatabase] variable `db` for the table
         *  TABLE_NAME ("main") and passing in the parameters [where] and [whereArgs] unchanged. We
         *  save the return value from `delete` (the number of rows affected) in `count` to later
         *  return to the caller. Note: in our app both [where] and [whereArgs] are always null, so
         *  all rows in our database are deleted.
         *  * MAIN_ID - never used in our app, but included due to code pasting I guess. We create
         *  a [String] SQL command using the row ID parsed from our [Uri] parameter [uri] to
         *  initialize `finalWhere` and call the `delete` method of our [SQLiteDatabase] variable
         *  `db` for the table TABLE_NAME ("main"), using `finalWhere` as the WHERE parameter, and
         *  the unmodified [whereArgs]. We save the return value from `delete` (the number of rows
         *  affected) in `count` to later return to the caller.
         *  * default - We throw an `IllegalArgumentException`.
         *
         * Then before we return, we notify registered observers that a row was updated and attempt
         * to sync changes to the network. Finally we return `count` (the number of rows deleted)
         * to the caller.
         *
         * @param uri       The full URI to query, including a row ID (if a specific record is requested).
         * @param where     An optional restriction to apply to rows when deleting.
         * @param whereArgs You may include ?'s in selection, which will be replaced by the values
         * from [whereArgs], in order that they appear in the selection. The values will be bound
         * as Strings.
         * @return The number of rows affected.
         */
        override fun delete(uri: Uri, where: String?, whereArgs: Array<String>?): Int {
            val db = mOpenHelper!!.writableDatabase
            val finalWhere: String

            val count: Int

            when (mUriMatcher.match(uri)) {
                MAIN ->
                    // If URI is main table, delete uses incoming where clause and args.
                    count = db.delete(MainTable.TABLE_NAME, where, whereArgs)

                // If the incoming URI matches a single note ID, does the delete based on the
                // incoming data, but modifies the where clause to restrict it to the
                // particular note ID.
                MAIN_ID -> {
                    // If URI is for a particular row ID, delete is based on incoming
                    // data but modified to restrict to the given ID.
                    finalWhere = DatabaseUtils.concatenateWhere(
                        BaseColumns._ID + " = " + ContentUris.parseId(uri), where
                    )
                    count = db.delete(MainTable.TABLE_NAME, finalWhere, whereArgs)
                }

                else -> throw IllegalArgumentException("Unknown URI $uri")
            }


            context!!.contentResolver.notifyChange(uri, null)

            return count
        }

        /**
         * Implement this to handle requests to update one or more rows. (This is never called in our
         * app but for completeness we will comment it.) The implementation should update all rows
         * matching the selection to set the columns according to the provided values map. As a
         * courtesy, call [notifyChange()][ContentResolver.notifyChange] after updating.
         *
         * First we use our [DatabaseHelper] field [mOpenHelper] to open our [SQLiteDatabase] and
         * save a reference to it in our variable `val db`. Then we declare our [Int] variable
         * `val count` (used to return the number of rows affected by the call), and our [String]
         * variable `val finalWhere` (used to construct a WHERE SQL statement when a single row is
         * specified in the [Uri] parameter [uri]). Next we branch based on the matching of [uri]
         * using our [UriMatcher] field [mUriMatcher]:
         *  * MAIN - we call the `update` method of our [SQLiteDatabase] `db` for the table
         *  TABLE_NAME ("main") and passing in the parameters [where] and [whereArgs] unchanged.
         *  We save the return value from `update` (the number of rows affected) in `count`.
         *  * MAIN_ID - We create a [String] SQL command using the row ID parsed from our [Uri]
         *  parameter [uri] to initialize `finalWhere` and call the `update` method of our
         *  [SQLiteDatabase] variable `db` for the table TABLE_NAME ("main"), using `finalWhere`
         *  as the WHERE parameter, and the unmodified [whereArgs]. We save the return value from
         *  `update` (the number of rows affected) in `count` to later return to the caller.
         *  * default - We throw an [IllegalArgumentException].
         *
         * Then before we return, we notify registered observers that a row was updated and attempt
         * to sync changes to the network. Finally we return `count` (the number of rows affected)
         * to the caller.
         *
         * @param uri       The URI to query. This can potentially have a record ID if this
         * is an update request for a specific record.
         * @param values    A set of column_name/value pairs to update in the database.
         * This must not be `null`.
         * @param where     An optional filter to match rows to update.
         * @param whereArgs You may include ?'s in [where], which will be replaced by the values
         * from [whereArgs], in the order that they appear in the selection. The values will be
         * bound as Strings.
         * @return the number of rows affected.
         */
        override fun update(
            uri: Uri,
            values: ContentValues?,
            where: String?,
            whereArgs: Array<String>?
        ): Int {
            val db = mOpenHelper!!.writableDatabase
            val count: Int
            val finalWhere: String

            when (mUriMatcher.match(uri)) {
                MAIN ->
                    // If URI is main table, update uses incoming where clause and args.
                    count = db.update(MainTable.TABLE_NAME, values, where, whereArgs)

                MAIN_ID -> {
                    // If URI is for a particular row ID, update is based on incoming
                    // data but modified to restrict to the given ID.
                    finalWhere = DatabaseUtils.concatenateWhere(
                        BaseColumns._ID + " = " + ContentUris.parseId(uri), where
                    )
                    count = db.update(MainTable.TABLE_NAME, values, finalWhere, whereArgs)
                }

                else -> throw IllegalArgumentException("Unknown URI $uri")
            }


            context!!.contentResolver.notifyChange(uri, null)

            return count
        }

        /**
         * Our static constants.
         */
        companion object {

            /**
             * The incoming URI matches the main table URI pattern
             */
            private const val MAIN = 1

            /**
             * The incoming URI matches the main table row ID URI pattern
             */
            private const val MAIN_ID = 2
        }
    }

    /**
     * This is our content fragment which does all the UI work.
     */
    class ThrottledLoaderListFragment : ListFragment(), LoaderManager.LoaderCallbacks<Cursor> {

        /**
         * This is the Adapter being used to display the list's data.
         */
        internal lateinit var simpleCursorAdapter: SimpleCursorAdapter

        /**
         * If non-null, this is the current filter the user has provided. (unused legacy of code pasting
         */
        @Suppress("unused")
        internal var mCurFilter: String? = null

        /**
         * Task we have running to populate the database.
         */
        internal var mPopulatingTask: AsyncTask<Void, Void, Void>? = null

        /**
         * Called when the fragment's activity has been created and this fragment's view hierarchy
         * instantiated. First we call through to our super's implementation of `onActivityCreated`,
         * then we set the text of our empty text `TextView` to some instructions on how to fill
         * our [ListView] with data, and report that this fragment would like to participate in
         * populating the options menu by receiving a call to [onCreateOptionsMenu] and related
         * methods.
         *
         * Next we initialize our [SimpleCursorAdapter] field [simpleCursorAdapter] with a new instance of
         * [SimpleCursorAdapter] (an empty adapter we will use to display the loaded data).
         * We use the Activity this fragment is currently associated with as the [Context],
         * the system layout file android.R.layout.simple_list_item_1 for the item layout, *null*
         * for the [Cursor] (since we will create an assign the cursor later), the single column
         * MainTable.COLUMN_NAME_DATA for the list of column names representing the data to bind to
         * the UI, and the single resource ID android.R.id.text1 for the view that the data in the
         * column should be displayed in. Having created [simpleCursorAdapter] we use it to provide the
         * cursor for our `ListView`.
         *
         * Next call `setListShown(false)` in order to start out with a progress indicator.
         * Finally we retrieve the [LoaderManager] for this fragment, (creating it if needed) and
         * use it to ensure a loader is initialized and active with the ID 0, no arguments, and
         * using *this* for its `LoaderCallbacks` callbacks.
         *
         * @param savedInstanceState we do not override `onSaveInstanceState` so do not use this
         */
        @Deprecated("Deprecated in Java")
        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            getListView().setPadding(
                id2p(8),
                id2p(120),
                id2p(8),
                id2p(60),
            )

            setEmptyText("No data.  Select 'Populate' to fill with data from Z to A at a rate of 4 per second.")
            setHasOptionsMenu(true)

            // Create an empty adapter we will use to display the loaded data.

            simpleCursorAdapter = SimpleCursorAdapter(
                requireActivity(),
                android.R.layout.simple_list_item_1, null,
                arrayOf(MainTable.COLUMN_NAME_DATA),
                intArrayOf(android.R.id.text1), 0
            )
            listAdapter = simpleCursorAdapter

            // Start out with a progress indicator.
            setListShown(false)

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            LoaderManager.getInstance(this).initLoader(0, null, this)
        }

        /**
         * Initialize the contents of the Activity's standard options menu. First we add a
         * `[MenuItem]` for the "Populate" function, specifying NONE for its groupId,
         * POPULATE_ID (Menu.FIRST) for its itemId, 0 for its order, and "Populate" for its
         * title (text to display for the item). We then add a [MenuItem] for the "Clear"
         * function, specifying NONE for its groupId, CLEAR_ID (2) for its itemId, 0 for its
         * order, and "Clear" for its title. After adding each [MenuItem] we set their flag
         * SHOW_AS_ACTION_IF_ROOM (show this item as a button in an Action Bar if the system
         * decides there is room for it.
         *
         * @param menu     The options menu in which you place your items.
         * @param inflater an inflater you can use to instantiate menu XML files into Menu objects.
         * (we do not use it, since we build the menu using code).
         */
        @Deprecated("Deprecated in Java")
        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            menu.add(Menu.NONE, POPULATE_ID, 0, "Populate")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            menu.add(Menu.NONE, CLEAR_ID, 0, "Clear")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }

        /**
         * This hook is called whenever an item in your options menu is selected. First we get a
         * [ContentResolver] instance for our application's package to initialize our variable
         * `val cr`. Then we switch based on the item ID of the [MenuItem] parameter [item] which
         * was selected:
         *  * POPULATE_ID - ("POPULATE" [MenuItem]) If there is already an existing
         *  `AsyncTask<Void, Void, Void>` in our field [mPopulatingTask] running, we cancel that
         *  task specifying that it not be interrupted. Then we create a new [mPopulatingTask]
         *  *object* with the `doInBackground` override using [ContentResolver] `cr` to insert
         *  the letters 'Z' to 'A' into our MainTable.CONTENT_URI with a 250 millisecond sleep
         *  between characters. We instruct [mPopulatingTask] to execute using the
         *  AsyncTask.THREAD_POOL_EXECUTOR (allows multiple tasks to run in parallel). Finally
         *  we return true to the caller to indicate that we consumed the [MenuItem] click here.
         *  * CLEAR_ID ("CLEAR" [MenuItem]) If there is already an existing
         *  `AsyncTask<Void, Void, Void>` in our field [mPopulatingTask] running, we cancel that
         *  task specifying that it not be interrupted, then set [mPopulatingTask] to *null*. Then
         *  we create an `AsyncTask<Void, Void, Void>` to initialize our variable `val task` with
         *  the `doInBackground`* override using [ContentResolver] `cr` to delete the entire contents
         *  of the MainTable.CONTENT_URI database. We start `task` executing in the background and
         *  return true to the caller to indicate that we consumed the [MenuItem] click here.
         *  * default - We return the result returned by our super's implementation of
         *  `onOptionsItemSelected(item)`.
         *
         * @param item The menu item that was selected.
         * @return boolean Return *false* to allow normal menu processing to
         * proceed, *true* to consume it here.
         */
        @Deprecated("Deprecated in Java")
        @SuppressLint("StaticFieldLeak")
        override fun onOptionsItemSelected(item: MenuItem): Boolean {

            val cr = requireActivity().contentResolver

            when (item.itemId) {
                POPULATE_ID -> {
                    if (mPopulatingTask != null) {
                        mPopulatingTask!!.cancel(false)
                    }
                    mPopulatingTask = object : AsyncTask<Void, Void, Void>() {
                        /**
                         * Override this method to perform a computation on a background thread. Our for
                         * loop goes through the characters of the alphabet from 'Z' to 'A', first checking
                         * to see if our task has been canceled and if so we break from the for loop
                         * (this happens if the "POPULATE" `MenuItem` is selected again while this
                         * task is running, or the "CLEAR" `MenuItem` is selected).
                         *
                         * We create a `StringBuilder builder` with its contents initialized to
                         * the String "Data ", then we append the current `char c` to it. We create
                         * `ContentValues values` and put the String from `builder` in it
                         * using the key COLUMN_NAME_DATA ("data"). We then use our `ContentResolver cr`
                         * to insert our new data into the database controlled by the provider of
                         * MainTable.CONTENT_URI ("content://com.example.android.apis.app.LoaderThrottle/main")
                         * (which is the class LoaderThrottle$SimpleProvider). After doing this we pause
                         * for 250 milliseconds (unless we receive an InterruptedException). Finally
                         * when we have generated a line for each character in the alphabet, we return
                         * null to the caller.
                         *
                         * @param params The parameters of the task (We have no parameters, thus Void)
                         *
                         * @return A result, defined by the subclass of this task (We return no result so Void)
                         */
                        @Deprecated("Deprecated in Java")
                        override fun doInBackground(vararg params: Void): Void? {
                            var c = 'Z'
                            while (c >= 'A') {
                                if (isCancelled) {
                                    break
                                }

                                val builder = StringBuilder("Data ")
                                builder.append(c)
                                val values = ContentValues()
                                values.put(MainTable.COLUMN_NAME_DATA, builder.toString())
                                cr.insert(CONTENT_URI, values)
                                // Wait a bit between each insert.
                                try {
                                    Thread.sleep(250)
                                } catch (_: InterruptedException) {
                                    Log.i(TAG, "Sleep interrupted")
                                }

                                c--
                            }
                            return null
                        }
                    }
                    mPopulatingTask!!.executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR, null, null
                    )
                    return true
                }

                CLEAR_ID -> {
                    if (mPopulatingTask != null) {
                        mPopulatingTask!!.cancel(false)
                        mPopulatingTask = null
                    }
                    val task = object : AsyncTask<Void, Void, Void>() {
                        /**
                         * This method runs on a background thread when `execute` is called.
                         * We use our `ContentResolver cr` to delete all the data in the
                         * database controlled by the provider of MainTable.CONTENT_URI
                         * ("content://com.example.android.apis.app.LoaderThrottle/main")
                         * (which is the class LoaderThrottle$SimpleProvider), and return null to the
                         * caller.
                         *
                         * @param params The parameters of the task (we have no parameters so Void
                         * is used
                         * @return we have nothing to return, so return null here
                         */
                        @Deprecated("Deprecated in Java")
                        override fun doInBackground(vararg params: Void): Void? {
                            cr.delete(CONTENT_URI, null, null)
                            return null
                        }
                    }
                    task.execute(null, null)
                    return true
                }

                else -> return super.onOptionsItemSelected(item)
            }
        }

        /**
         * This method is called when an item in the list is selected. We simply log the row [id]
         * of the row that was clicked.
         *
         * @param l        The ListView where the click happened
         * @param v        The view that was clicked within the ListView
         * @param position The position of the view in the list
         * @param id       The row id of the item that was clicked
         */
        override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
            // Insert desired behavior here.
            Log.i(TAG, "Item clicked: $id")
        }

        /**
         * Instantiate and return a new Loader for the given ID. We create a fully specified cursor
         * loader [CursorLoader] for the content [Uri] given in [MainTable.CONTENT_URI] to initialize
         * our variable `val cl`, set the amount to throttle updates to 2000 milliseconds, and return
         * it to the caller.
         *
         * @param id   The ID whose loader is to be created. (We only use one, so ignore this)
         * @param args Any arguments supplied by the caller. (We do not use arguments)
         * @return Return a new [Loader] instance that is ready to start loading.
         */
        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {

            val cl = CursorLoader(
                requireActivity(),
                CONTENT_URI,
                PROJECTION,
                null,
                null,
                null
            )
            cl.setUpdateThrottle(2000) // update at most every 2 seconds.
            return cl
        }

        /**
         * Called when a previously created loader has finished its load. We swap in the new [Cursor],
         * then cause our [ListView] to be shown. If the state of the `Fragment` is "Resumed"
         * we use the call `setListShown(true)` (normal case), and after an orientation change
         * we use the call `setListShownNoAnimation(true)`
         *
         * @param loader The Loader that has finished.
         * @param data   The data generated by the Loader.
         */
        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
            simpleCursorAdapter.swapCursor(data)

            // The list should now be shown.
            if (isResumed) {
                setListShown(true)
            } else {
                setListShownNoAnimation(true)
            }
        }

        /**
         * Called when a previously created loader is being reset, and thus
         * making its data unavailable. The application should at this point
         * remove any references it has to the Loader's data. We just swap in
         * a null [Cursor] for our [SimpleCursorAdapter] field [simpleCursorAdapter] to use.
         *
         * @param loader The Loader that is being reset.
         */
        override fun onLoaderReset(loader: Loader<Cursor>) {
            simpleCursorAdapter.swapCursor(null)
        }

        /**
         * Our static constants
         */
        companion object {

            // Menu identifiers
            /**
             * Convenience constant for locating the "Populate" menu item
             */
            internal const val POPULATE_ID = Menu.FIRST

            /**
             * Convenience constant for locating the "Clear" menu item
             */
            internal const val CLEAR_ID = Menu.FIRST + 1

            /**
             * These are the rows that we will retrieve.
             */
            internal val PROJECTION = arrayOf(BaseColumns._ID, MainTable.COLUMN_NAME_DATA)
        }
    }

    /**
     * Our static constants
     */
    companion object {
        // Debugging.
        internal const val TAG = "LoaderThrottle"

        /**
         * The authority we use to get to our sample provider.
         */
        const val AUTHORITY: String = "com.example.android.apis.app.LoaderThrottle"
    }
}

