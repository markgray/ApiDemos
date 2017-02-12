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

package com.example.android.apis.app;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.HashMap;

/**
 * Demonstration of bottom to top implementation of a content provider holding
 * structured data through displaying it in the UI, using throttling to reduce
 * the number of queries done when its data changes. Implements a custom CursorLoader
 * which pretends to be a SQLite database, and simulates a slow provider of data
 * {@code SimpleProvider}
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LoaderThrottle extends Activity {
    // Debugging.
    static final String TAG = "LoaderThrottle";

    /**
     * The authority we use to get to our sample provider.
     */
    public static final String AUTHORITY = "com.example.android.apis.app.LoaderThrottle";

    /**
     * Definition of the contract for the main table of our provider.
     */
    @SuppressWarnings("WeakerAccess")
    public static final class MainTable implements BaseColumns {

        /**
         * private so that this class cannot be instantiated
         */
        private MainTable() {
        }

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "main";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/main");

        /**
         * The content URI base for a single row of data. Callers must
         * append a numeric row id to this Uri to retrieve a row
         */
        public static final Uri CONTENT_ID_URI_BASE
                = Uri.parse("content://" + AUTHORITY + "/main/");

        /**
         * The MIME type of {@link #CONTENT_URI}.
         */
        public static final String CONTENT_TYPE
                = "vnd.android.cursor.dir/vnd.example.api-demos-throttle";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single row.
         */
        public static final String CONTENT_ITEM_TYPE
                = "vnd.android.cursor.item/vnd.example.api-demos-throttle";
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "data COLLATE LOCALIZED ASC";

        /**
         * Column name for the single column holding our data.
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_DATA = "data";
    }

    /**
     * This class helps open, create, and upgrade the database file.
     */
    static class DatabaseHelper extends SQLiteOpenHelper {

        /**
         * name of the database file.
         */
        private static final String DATABASE_NAME = "loader_throttle.db";

        /**
         * Version number of the database
         */
        private static final int DATABASE_VERSION = 2;

        /**
         * Constructor which simply calls the super's constructor to create an {@code SQLiteOpenHelper}
         * using our DATABASE_NAME, DATABASE_VERSION, and requesting the default cursor factory.
         *
         * @param context Context the {@code ContentProvider} {@code SimpleProvider} is running in
         */
        DatabaseHelper(Context context) {

            // calls the super constructor, requesting the default cursor factory.
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         * Called when the database is created for the first time. This is where the
         * creation of tables and the initial population of the tables should happen.
         * Creates the underlying database with table name and column names taken from the
         * NotePad class. We simply use our parameter {@code SQLiteDatabase db} to execute the
         * single SQL statement which uses the "CREATE TABLE" command to create a new table in our
         * SQLite database {@code db}, with the name given by MainTable.TABLE_NAME ("main"), the
         * columns given by:
         * <ul>
         * <li>MainTable._ID ("_id") an INTEGER PRIMARY KEY column</li>
         * <li>MainTable.COLUMN_NAME_DATA ("TEXT") a TEXT column</li>
         * </ul>
         *
         * @param db The database.
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + MainTable.TABLE_NAME + " ("
                    + MainTable._ID + " INTEGER PRIMARY KEY,"
                    + MainTable.COLUMN_NAME_DATA + " TEXT"
                    + ");");
        }

        /**
         * Called when the database needs to be upgraded. The implementation
         * should use this method to drop tables, add tables, or do anything else it
         * needs to upgrade to the new schema version.
         * <p>
         * The SQLite ALTER TABLE documentation can be found
         * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
         * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
         * you can use ALTER TABLE to rename the old table, then create the new table and then
         * populate the new table with the contents of the old table.
         * <p>
         * This method executes within a transaction.  If an exception is thrown, all changes
         * will automatically be rolled back.
         * <p>
         * Our implementation demonstrates that the provider must consider what happens when the
         * underlying data store is changed. In this sample, the database is upgraded by destroying
         * the existing data then calling {@code onCreate}. A real application should upgrade the
         * database in place.
         * <p>
         * First we execute the SQL command "DROP TABLE IF EXISTS notes", which removes the table
         * "notes" added with the CREATE TABLE statement. The dropped table is completely removed
         * from the database schema and the disk file. The table can not be recovered. All indices
         * and triggers associated with the table are also deleted. The optional IF EXISTS clause
         * suppresses the error that would normally result if the table does not exist. Then we call
         * our callback {@code onCreate} which recreates the table
         *
         * @param db         The database.
         * @param oldVersion The old database version.
         * @param newVersion The new database version.
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            // Logs that the database is being upgraded
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");

            // Kills the table and existing data
            db.execSQL("DROP TABLE IF EXISTS notes");

            // Recreates the database with a new version
            onCreate(db);
        }
    }

    /**
     * A very simple implementation of a content provider.
     */
    public static class SimpleProvider extends ContentProvider {
        /**
         * A projection map used to select columns from the database
         */
        private final HashMap<String, String> mNotesProjectionMap;

        /**
         * Uri matcher to decode incoming URIs.
         */
        private final UriMatcher mUriMatcher;

        /**
         * The incoming URI matches the main table URI pattern
         */
        private static final int MAIN = 1;

        /**
         * The incoming URI matches the main table row ID URI pattern
         */
        private static final int MAIN_ID = 2;

        /**
         * Handle to a new DatabaseHelper.
         */
        private DatabaseHelper mOpenHelper;

        /**
         * Global provider initialization. We initialize our field {@code UriMatcher mUriMatcher} with
         * a new instance of {@code UriMatcher} with the code to match for the root URI specified as
         * UriMatcher.NO_MATCH (a code to specify that a Uri can not match the root), and we add a Uri
         * to {@code mUriMatcher} to match TABLE_NAME ("main"), with MAIN (1) the code that is returned
         * when a URI matches, and a Uri to match "main/#", with MAIN_ID (2) the code that is returned
         * when a URI matches it. We initialize our field {@code HashMap<String, String> mNotesProjectionMap}
         * with an empty {@code HashMap<>}, then put the String MainTable._ID to map to itself, and
         * the String MainTable.COLUMN_NAME_DATA to map to itself.
         */
        public SimpleProvider() {
            // Create and initialize URI matcher.
            mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            mUriMatcher.addURI(AUTHORITY, MainTable.TABLE_NAME, MAIN);
            mUriMatcher.addURI(AUTHORITY, MainTable.TABLE_NAME + "/#", MAIN_ID);

            // Create and initialize projection map for all columns.  This is
            // simply an identity mapping.
            mNotesProjectionMap = new HashMap<>();
            mNotesProjectionMap.put(MainTable._ID, MainTable._ID);
            mNotesProjectionMap.put(MainTable.COLUMN_NAME_DATA, MainTable.COLUMN_NAME_DATA);
        }

        /**
         * Initialize our content provider on startup. This method is called for all registered
         * content providers on the application main thread at application launch time.  It must
         * not perform lengthy operations, or application startup will be delayed.
         * <p>
         * We simply initialize our field {@code DatabaseHelper mOpenHelper} with a new instance of
         * {@code DatabaseHelper} using the Context this provider is running in as the {@code Context},
         * and return true to our caller.
         *
         * @return true if the provider was successfully loaded, false otherwise
         */
        @Override
        public boolean onCreate() {
            mOpenHelper = new DatabaseHelper(getContext());
            // Assumes that any failures will be reported by a thrown exception.
            return true;
        }

        /**
         * Handle incoming query requests from clients. These queries are generated by the
         * {@code CursorLoader} created in {@code ThrottledLoaderListFragment} based on the AUTHORITY
         * "com.example.android.apis.app.LoaderThrottle" which the AndroidManifest.xml attribute
         * android:authorities points to in the provider element for android:name=".app.LoaderThrottle$SimpleProvider" which mentions our class
         * {@code ThrottledLoaderListFragment} uses that {@code CursorLoader} 
         *
         * First we create a new {@code SQLiteQueryBuilder qb}
         * and set the list of tables to query to {@code MainTable.TABLE_NAME} ("main" the only table in
         * our pretend database). Then we switch based on the return value of our {@code UriMatcher mUriMatcher}
         * when matching the
         *
         * @param uri           The URI to query. This will be the full URI sent by the client; if
         *                      the client is requesting a specific record, the URI will end in a
         *                      record number that the implementation should parse and add to a
         *                      WHERE or HAVING clause, specifying that _id value.
         * @param projection    The list of columns to put into the cursor. If {@code null} all
         *                      columns are included.
         * @param selection     A selection criteria to apply when filtering rows. If {@code null}
         *                      then all rows are included.
         * @param selectionArgs You may include ?s in selection, which will be replaced by the values
         *                      from selectionArgs, in order that they appear in the selection. The
         *                      values will be bound as Strings.
         * @param sortOrder     How the rows in the cursor should be sorted. If {@code null} then
         *                      the provider is free to define the sort order.
         * @return a Cursor or {@code null}.
         */
        @Override
        public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                            String[] selectionArgs, String sortOrder) {

            // Constructs a new query builder and sets its table name
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(MainTable.TABLE_NAME);

            switch (mUriMatcher.match(uri)) {
                case MAIN:
                    // If the incoming URI is for main table.
                    qb.setProjectionMap(mNotesProjectionMap);
                    break;

                case MAIN_ID:
                    // The incoming URI is for a single row.
                    qb.setProjectionMap(mNotesProjectionMap);
                    qb.appendWhere(MainTable._ID + "=?");
                    selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
                            new String[]{uri.getLastPathSegment()});
                    break;

                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }


            if (TextUtils.isEmpty(sortOrder)) {
                sortOrder = MainTable.DEFAULT_SORT_ORDER;
            }

            SQLiteDatabase db = mOpenHelper.getReadableDatabase();

            Cursor c = qb.query(db, projection, selection, selectionArgs,
                    null /* no group */, null /* no filter */, sortOrder);

            //noinspection ConstantConditions
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        }

        /**
         * Return the MIME type for an known URI in the provider.
         */
        @Override
        public String getType(@NonNull Uri uri) {
            switch (mUriMatcher.match(uri)) {
                case MAIN:
                    return MainTable.CONTENT_TYPE;
                case MAIN_ID:
                    return MainTable.CONTENT_ITEM_TYPE;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }

        /**
         * Handler inserting new data.
         */
        @Override
        public Uri insert(@NonNull Uri uri, ContentValues initialValues) {
            if (mUriMatcher.match(uri) != MAIN) {
                // Can only insert into to main URI.
                throw new IllegalArgumentException("Unknown URI " + uri);
            }

            ContentValues values;

            if (initialValues != null) {
                values = new ContentValues(initialValues);
            } else {
                values = new ContentValues();
            }

            if (!values.containsKey(MainTable.COLUMN_NAME_DATA)) {
                values.put(MainTable.COLUMN_NAME_DATA, "");
            }

            SQLiteDatabase db = mOpenHelper.getWritableDatabase();

            long rowId = db.insert(MainTable.TABLE_NAME, null, values);

            // If the insert succeeded, the row ID exists.
            if (rowId > 0) {
                Uri noteUri = ContentUris.withAppendedId(MainTable.CONTENT_ID_URI_BASE, rowId);
                //noinspection ConstantConditions
                getContext().getContentResolver().notifyChange(noteUri, null);
                return noteUri;
            }

            throw new SQLException("Failed to insert row into " + uri);
        }

        /**
         * Handle deleting data.
         */
        @Override
        public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            String finalWhere;

            int count;

            switch (mUriMatcher.match(uri)) {
                case MAIN:
                    // If URI is main table, delete uses incoming where clause and args.
                    count = db.delete(MainTable.TABLE_NAME, where, whereArgs);
                    break;

                // If the incoming URI matches a single note ID, does the delete based on the
                // incoming data, but modifies the where clause to restrict it to the
                // particular note ID.
                case MAIN_ID:
                    // If URI is for a particular row ID, delete is based on incoming
                    // data but modified to restrict to the given ID.
                    finalWhere = DatabaseUtils.concatenateWhere(
                            MainTable._ID + " = " + ContentUris.parseId(uri), where);
                    count = db.delete(MainTable.TABLE_NAME, finalWhere, whereArgs);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }

            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);

            return count;
        }

        /**
         * Handle updating data.
         */
        @Override
        public int update(@NonNull Uri uri, ContentValues values, String where, String[] whereArgs) {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            int count;
            String finalWhere;

            switch (mUriMatcher.match(uri)) {
                case MAIN:
                    // If URI is main table, update uses incoming where clause and args.
                    count = db.update(MainTable.TABLE_NAME, values, where, whereArgs);
                    break;

                case MAIN_ID:
                    // If URI is for a particular row ID, update is based on incoming
                    // data but modified to restrict to the given ID.
                    finalWhere = DatabaseUtils.concatenateWhere(
                            MainTable._ID + " = " + ContentUris.parseId(uri), where);
                    count = db.update(MainTable.TABLE_NAME, values, finalWhere, whereArgs);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }

            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);

            return count;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getFragmentManager();

        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            ThrottledLoaderListFragment list = new ThrottledLoaderListFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
    }

    public static class ThrottledLoaderListFragment extends ListFragment
            implements LoaderManager.LoaderCallbacks<Cursor> {

        // Menu identifiers
        static final int POPULATE_ID = Menu.FIRST;
        static final int CLEAR_ID = Menu.FIRST + 1;

        // This is the Adapter being used to display the list's data.
        SimpleCursorAdapter mAdapter;

        // If non-null, this is the current filter the user has provided.
        @SuppressWarnings("unused")
        String mCurFilter;

        // Task we have running to populate the database.
        AsyncTask<Void, Void, Void> mPopulatingTask;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            setEmptyText("No data.  Select 'Populate' to fill with data from Z to A at a rate of 4 per second.");
            setHasOptionsMenu(true);

            // Create an empty adapter we will use to display the loaded data.
            mAdapter = new SimpleCursorAdapter(getActivity(),
                    android.R.layout.simple_list_item_1, null,
                    new String[]{MainTable.COLUMN_NAME_DATA},
                    new int[]{android.R.id.text1}, 0);
            setListAdapter(mAdapter);

            // Start out with a progress indicator.
            setListShown(false);

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            getLoaderManager().initLoader(0, null, this);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            menu.add(Menu.NONE, POPULATE_ID, 0, "Populate")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.add(Menu.NONE, CLEAR_ID, 0, "Clear")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final ContentResolver cr = getActivity().getContentResolver();

            switch (item.getItemId()) {
                case POPULATE_ID:
                    if (mPopulatingTask != null) {
                        mPopulatingTask.cancel(false);
                    }
                    mPopulatingTask = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            for (char c = 'Z'; c >= 'A'; c--) {
                                if (isCancelled()) {
                                    break;
                                }
                                //noinspection StringBufferReplaceableByString
                                StringBuilder builder = new StringBuilder("Data ");
                                builder.append(c);
                                ContentValues values = new ContentValues();
                                values.put(MainTable.COLUMN_NAME_DATA, builder.toString());
                                cr.insert(MainTable.CONTENT_URI, values);
                                // Wait a bit between each insert.
                                try {
                                    Thread.sleep(250);
                                } catch (InterruptedException e) {
                                    Log.i(TAG, "Sleep interrupted");
                                }
                            }
                            return null;
                        }
                    };
                    mPopulatingTask.executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
                    return true;

                case CLEAR_ID:
                    if (mPopulatingTask != null) {
                        mPopulatingTask.cancel(false);
                        mPopulatingTask = null;
                    }
                    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            cr.delete(MainTable.CONTENT_URI, null, null);
                            return null;
                        }
                    };
                    task.execute((Void[]) null);
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            // Insert desired behavior here.
            Log.i(TAG, "Item clicked: " + id);
        }

        // These are the rows that we will retrieve.
        static final String[] PROJECTION = new String[]{
                MainTable._ID,
                MainTable.COLUMN_NAME_DATA,
        };

        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            CursorLoader cl = new CursorLoader(getActivity(), MainTable.CONTENT_URI,
                    PROJECTION, null, null, null);
            cl.setUpdateThrottle(2000); // update at most every 2 seconds.
            return cl;
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapter.swapCursor(data);

            // The list should now be shown.
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }
    }
}

