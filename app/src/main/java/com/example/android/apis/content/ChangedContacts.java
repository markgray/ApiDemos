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

package com.example.android.apis.content;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Shows how to access the contacts database and list those that have changed or been deleted since
 * a certain time. Layout is created by java code, includes instructive use of a ListView to contain
 * the results of the Cursor queries.
 */
@SuppressWarnings("WeakerAccess")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ChangedContacts extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Used for the preferences file 'name' when accessing shared preferences
     */
    private static final String CLASS = ChangedContacts.class.getSimpleName();

    /**
     * Preference file key for the timestamp of the latest change to the contact database (Starts at 0)
     * and is updated when the database is read for the first time
     */
    private static final String PREF_KEY_CHANGE = "timestamp_change";
    /**
     * Preference file key for the timestamp of the latest delete from the contact database (Starts at 0)
     * and is updated when the database is read for the first time
     */
    private static final String PREF_KEY_DELETE = "timestamp_delete";

    /**
     * ID for the {@code CursorLoader} used to feed data about changed contacts to fill {@code ListView mList}
     */
    private static final int ID_CHANGE_LOADER = 1;
    /**
     * ID for the {@code CursorLoader} used to feed data about deleted contacts to fill {@code ListView mList}
     */
    private static final int ID_DELETE_LOADER = 2;

    /**
     * To see this in action, "clear data" for the contacts storage app in the system settings.
     * Then come into this app and hit any of the delta buttons.  This will cause the contacts
     * database to be re-created.
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast toast = Toast.makeText(context, "Contacts database created.", Toast.LENGTH_SHORT);
            toast.show();
        }
    };

    /**
     * {@code CursorAdapter} subclass used to fill {@code ListView mList} with data from the
     * ID_DELETE_LOADER {@code CursorLoader} which is configured to query the contacts data base
     * deleted contact table with the selection based on those whose CONTACT_DELETED_TIMESTAMP is
     * greater than {@code long mSearchTime} which is read from our preference file at start of
     * search, and updated to the newest contact change received after every "Deleted since" Button
     * click. The deleted contact table holds a log of deleted contacts.
     */
    private DeleteAdapter mDeleteAdapter;
    /**
     * {@code CursorAdapter} subclass used to fill {@code ListView mList} with data from the
     * ID_CHANGE_LOADER {@code CursorLoader} which is configured to query the contacts data base
     * with the selection based on those whose last changed timestamp is greater than
     * {@code long mSearchTime} which is read from our preference file at start of search, and
     * updated to the newest contact change received after every "Changed Since" Button click.
     */
    private ChangeAdapter mChangeAdapter;
    /**
     * Last time stamp, which is read from the preferences data base using key PREF_KEY_CHANGE or
     * PREF_KEY_DELETE depending on whether the "Changed Since" or "Deleted Since" {@code CursorLoader}
     * is being configured. Both values are saved and retrieved by the same routines: saveLastTimestamp,
     * and getLastTimestamp.
     */
    private long mSearchTime;
    /**
     * {@code TextView} used to display number of contact changes or number of contact deletes since
     * {@code mSearchTime}
     */
    private TextView mDisplayView;
    /**
     * {@code ListView} used to display changed or deleted contacts retrieved by the
     * {@code ChangeAdapter mChangeAdapter} or {@code DeleteAdapter mDeleteAdapter}
     */
    private ListView mList;
    /**
     * {@code Button} used to search the contacts database for deleted contacts
     */
    private Button mDeleteButton;
    /**
     * {@code Button} used to search the contacts database for changed contacts
     */
    private Button mChangeButton;
    /**
     * {@code Button} used to reset PREF_KEY_CHANGE, and PREF_KEY_DELETE timestamps in the preferences
     * data base to 0
     */
    @SuppressWarnings("FieldCanBeLocal")
    private Button mClearPreferences;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we initialize our fields {@code DeleteAdapter mDeleteAdapter} and
     * {@code ChangeAdapter mChangeAdapter} with new instances of their respective {@code CursorAdapter}
     * subclasses.
     *
     * Next we create {@code LinearLayout main} and set its orientation to VERTICAL.
     *
     * We initialize our field {@code Button mChangeButton}, setting its text to "Changed since" with
     * the value of the timestamp stored in our preference file under the key PREF_KEY_CHANGE appended
     * to it, and then set its {@code OnClickListener} to an anonymous function which calls our method
     * {@code changeClick()}.
     *
     * We initialize our field {@code Button mDeleteButton}, setting its text to "Deleted since" with
     * the value of the timestamp stored in our preference file under the key PREF_KEY_DELETE appended
     * to it, and then set its {@code OnClickListener} to an anonymous function which calls our method
     * {@code deleteClick()}.
     *
     * We initialize our field {@code Button mClearPreferences}, setting its text to "Clear Preferences",
     * and then set its {@code OnClickListener} to an anonymous function which resets both PREF_KEY_CHANGE
     * and PREF_KEY_DELETE to zero and updates the text contained in {@code mChangeButton} and
     * {@code mDeleteButton} to reflect this.
     *
     * We now add {@code mChangeButton}, {@code mDeleteButton} and {@code mClearPreferences} to the
     * {@code LinearLayout main}.
     *
     * We create a new {@code TextView} for our field {@code TextView mDisplayView}, configure the
     * padding to have 5 pixels around its sides, and add it to {@code LinearLayout main}.
     *
     * We create a new {@code ListView} for our field {@code ListView mList}, set its layout params
     * to be WRAP_CONTENT for both width and height, with its weight set to 1.0, and add it to
     * {@code LinearLayout main}.
     *
     * Finally we set our content view to {@code LinearLayout main}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDeleteAdapter = new DeleteAdapter(this, null, 0);
        mChangeAdapter = new ChangeAdapter(this, null, 0);

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);

        mChangeButton = new Button(this);
        mChangeButton.setText("Changed since " + getLastTimestamp(0, PREF_KEY_CHANGE));
        mChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeClick();
            }
        });

        mDeleteButton = new Button(this);
        mDeleteButton.setText("Deleted since " + getLastTimestamp(0, PREF_KEY_DELETE));
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteClick();
            }
        });

        mClearPreferences = new Button(this);
        mClearPreferences.setText("Clear Preferences");
        mClearPreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLastTimestamp(0, PREF_KEY_CHANGE);
                mChangeButton.setText("Changed since " + getLastTimestamp(0, PREF_KEY_CHANGE));
                saveLastTimestamp(0, PREF_KEY_DELETE);
                mDeleteButton.setText("Deleted since " + getLastTimestamp(0, PREF_KEY_DELETE));
            }
        });

        main.addView(mChangeButton);
        main.addView(mDeleteButton);
        main.addView(mClearPreferences);

        mDisplayView = new TextView(this);
        mDisplayView.setPadding(5, 5, 5, 5);
        main.addView(mDisplayView);

        mList = new ListView(this);
        final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
        mList.setLayoutParams(new LinearLayout.LayoutParams(WRAP, WRAP, 1f));
        main.addView(mList);

        setContentView(main);
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or
     * {@link #onPause}, for your activity to start interacting with the user.
     * This is a good place to begin animations, open exclusive-access devices
     * (such as the camera), etc.
     *
     * First we call through to our super's implementation of {@code onResume}. Then we create
     * {@code IntentFilter filter}, set its action to CONTACTS_DATABASE_CREATED and register our
     * {@code BroadcastReceiver mReceiver} field to receive broadcasts that match {@code filter}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ContactsContract.Intents.CONTACTS_DATABASE_CREATED);
        registerReceiver(mReceiver, filter);
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed. The counterpart to
     * {@link #onResume}.
     *
     * First we call through to our super's implementation of {@code onPause}, then we unregister
     * our field {@code BroadcastReceiver mReceiver} as a broadcast receiver.
     */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    /**
     * {@code OnClickListener} for the {@code Button mChangeButton}, it causes the ID_CHANGE_LOADER
     * {@code CursorLoader} to re-fetch its data using the latest timestamp.
     */
    private void changeClick() {
        mChangeAdapter.swapCursor(null);
        LoaderManager manager = getLoaderManager();
        manager.destroyLoader(ID_DELETE_LOADER);
        manager.restartLoader(ID_CHANGE_LOADER, null, this);
    }

    /**
     * {@code OnClickListener} for the {@code Button mDeleteButton}, it causes the ID_DELETE_LOADER
     * {@code CursorLoader} to re-fetch its data using the latest timestamp.
     */
    private void deleteClick() {
        mDeleteAdapter.swapCursor(null);
        LoaderManager manager = getLoaderManager();
        manager.destroyLoader(ID_CHANGE_LOADER);
        manager.restartLoader(ID_DELETE_LOADER, null, this);
    }

    private void saveLastTimestamp(long time, String key) {
        SharedPreferences pref = getSharedPreferences(CLASS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key, time);
        editor.commit();
    }

    private long getLastTimestamp(long time, String key) {
        SharedPreferences pref = getSharedPreferences(CLASS, Context.MODE_PRIVATE);
        return pref.getLong(key, time);
    }

    /**
     * Instantiate and return a new Loader for the given ID. We switch on the {@code id} parameter
     * and return the {@code CursorLoader} created by the appropriate method:
     * <ul>
     * <li>ID_CHANGE_LOADER -- {@code getChangeLoader()}</li>
     * <li>ID_DELETE_LOADER -- {@code getDeleteLoader()}</li>
     * </ul>
     * This is called by the {@code LoaderManager} for this activity as a callback as a result of
     * a call to {@code restartLoader} (start a new or restarts an existing Loader, register the
     * callbacks -- "this" in our case. See {@code changeClick} and {@code deleteClick}.)
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_CHANGE_LOADER:
                return getChangeLoader();
            case ID_DELETE_LOADER:
                return getDeleteLoader();
        }
        return null;
    }

    private CursorLoader getChangeLoader() {
        String[] projection = new String[]{
                ContactsContract.Data._ID,
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.Data.CONTACT_LAST_UPDATED_TIMESTAMP
        };

        mSearchTime = getLastTimestamp(0, PREF_KEY_CHANGE);

        String selection = ContactsContract.Data.CONTACT_LAST_UPDATED_TIMESTAMP + " > ?";
        String[] bindArgs = new String[]{mSearchTime + ""};
        return new CursorLoader(this, ContactsContract.Data.CONTENT_URI, projection,
                selection, bindArgs, ContactsContract.Data.CONTACT_LAST_UPDATED_TIMESTAMP
                + " desc, " + ContactsContract.Data.CONTACT_ID + " desc");
    }

    private CursorLoader getDeleteLoader() {
        String[] projection = new String[]{
                ContactsContract.DeletedContacts.CONTACT_ID,
                ContactsContract.DeletedContacts.CONTACT_DELETED_TIMESTAMP
        };

        mSearchTime = getLastTimestamp(0, PREF_KEY_DELETE);

        String selection = ContactsContract.DeletedContacts.CONTACT_DELETED_TIMESTAMP + " > ?";
        String[] bindArgs = new String[]{mSearchTime + ""};
        return new CursorLoader(this, ContactsContract.DeletedContacts.CONTENT_URI, projection,
                selection, bindArgs, ContactsContract.DeletedContacts.CONTACT_DELETED_TIMESTAMP +
                " desc");
    }

    /**
     * Called when a previously created loader has finished its load.
     *
     * @param cursorLoader The Loader that has finished.
     * @param data The data generated by the Loader.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        long timestamp = 0;

        switch (cursorLoader.getId()) {
            case ID_CHANGE_LOADER:
                mDisplayView.setText(data.getCount() + " change(s) since " + mSearchTime);
                mList.setAdapter(mChangeAdapter);
                mChangeAdapter.swapCursor(data);

                // Save the largest timestamp returned.  Only need the first one due to the sort
                // order.
                if (data.moveToNext()) {
                    timestamp = data.getLong(data.getColumnIndex(ContactsContract.Data.CONTACT_LAST_UPDATED_TIMESTAMP));
                    data.moveToPrevious();
                }
                if (timestamp > 0) {
                    saveLastTimestamp(timestamp, PREF_KEY_CHANGE);
                    mChangeButton.setText("Changed since " + timestamp);
                }
                break;
            case ID_DELETE_LOADER:
                mDisplayView.setText(data.getCount() + " delete(s) since " + mSearchTime);
                mList.setAdapter(mDeleteAdapter);
                mDeleteAdapter.swapCursor(new DeleteCursorWrapper(data));
                if (data.moveToNext()) {
                    timestamp = data.getLong(data.getColumnIndex(ContactsContract.DeletedContacts.CONTACT_DELETED_TIMESTAMP));
                    data.moveToPrevious();
                }
                if (timestamp > 0) {
                    saveLastTimestamp(timestamp, PREF_KEY_DELETE);
                    mDeleteButton.setText("Deleted since " + timestamp);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mDisplayView.setText("");
        switch (cursorLoader.getId()) {
            case ID_CHANGE_LOADER:
                mChangeAdapter.swapCursor(null);
                break;
            case ID_DELETE_LOADER:
                mDeleteAdapter.swapCursor(null);
                break;
        }
    }

    private class DeleteCursorWrapper extends CursorWrapper {

        /**
         * Creates a cursor wrapper.
         *
         * @param cursor The underlying cursor to wrap.
         */
        public DeleteCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        @Override
        public int getColumnIndexOrThrow(String columnName) {
            if (columnName.equals("_id")) {
                return super.getColumnIndex(ContactsContract.DeletedContacts.CONTACT_ID);
            }
            return super.getColumnIndex(columnName);
        }
    }

    private static class DeleteAdapter extends CursorAdapter {

        private Context mContext;

        public DeleteAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            this.mContext = context;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LinearLayout item = new LinearLayout(mContext);
            item.addView(buildText(context));
            item.addView(buildText(context));
            return item;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            LinearLayout item = (LinearLayout) view;
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.DeletedContacts.CONTACT_ID));
            String timestamp = cursor.getString(cursor.getColumnIndex(ContactsContract.DeletedContacts.CONTACT_DELETED_TIMESTAMP));

            setText(item.getChildAt(0), id);
            setText(item.getChildAt(1), timestamp);
        }
    }

    private static class ChangeAdapter extends CursorAdapter {

        private Context mContext;

        public ChangeAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            mContext = context;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LinearLayout item = new LinearLayout(mContext);
            item.addView(buildText(context));
            item.addView(buildText(context));
            item.addView(buildText(context));
            return item;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            LinearLayout item = (LinearLayout) view;

            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            String timestamp = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_LAST_UPDATED_TIMESTAMP));

            setText(item.getChildAt(0), id);
            setText(item.getChildAt(1), name);
            setText(item.getChildAt(2), timestamp);
        }
    }

    private static void setText(View view, String value) {
        TextView text = (TextView) view;
        text.setText(value);
    }

    private static TextView buildText(Context context) {
        TextView view = new TextView(context);
        view.setPadding(3, 3, 3, 3);
        return view;
    }
}
