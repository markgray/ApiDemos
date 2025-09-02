/*
 * Copyright (C) 2008 The Android Open Source Project
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
package com.example.android.apis.appwidget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.android.apis.R

/**
 * The configuration screen for the [ExampleAppWidgetProvider] widget sample.
 */
@Suppress("MemberVisibilityCanBePrivate")
class ExampleAppWidgetConfigure : AppCompatActivity() {
    /**
     * The ID of the AppWidget that we are configuring. It is read from the extras of the Intent
     * that launches this Activity under the key `AppWidgetManager.EXTRA_APPWIDGET_ID`, and
     * defaults to `AppWidgetManager.INVALID_APPWIDGET_ID` if it is not found.
     */
    var mAppWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    /**
     * [EditText] in our UI with ID R.id.appwidget_prefix that the user uses to configure the
     * title prefix for our App Widget.
     */
    var mAppWidgetPrefix: EditText? = null

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our result to CANCELED, so that if the user presses the back button this will
     * cause the widget host to cancel out of the widget placement. We set our content view to our
     * layout file R.layout.appwidget_configure, then initialize our [EditText] field
     * [mAppWidgetPrefix] by finding the view with ID R.id.appwidget_prefix. We then find the
     * "Save" button (R.id.save_button) and set its OnClickListener to our [View.OnClickListener]
     * field [mOnClickListener].
     *
     * We initialize our [Intent] variable `val intent` to the intent that started this activity,
     * and initialize [Bundle] variable `val extras` to any extras that were supplied in `intent`.
     * If `extras` is not null, we set our field [mAppWidgetId] to the [Int] stored in `extras`
     * under the key [AppWidgetManager.EXTRA_APPWIDGET_ID], defaulting to the value
     * [AppWidgetManager.INVALID_APPWIDGET_ID] if it is not found there.
     *
     * If [mAppWidgetId] is still [AppWidgetManager.INVALID_APPWIDGET_ID] we call [finish] to end
     * this Activity. Otherwise we set the text of our [EditText] field [mAppWidgetPrefix] to the
     * [String] that our [loadTitlePref] method loads from our shared preferences file for the
     * app widget ID [mAppWidgetId].
     *
     * @param icicle If the activity is being re-initialized after previously being shut down then
     * this [Bundle] contains the data it most recently supplied in [onSaveInstanceState].
     * **Note: Otherwise it is null.** We do not use it.
     */
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED)

        // Set the view layout resource to use.
        setContentView(R.layout.appwidget_configure)

        // Find the EditText
        mAppWidgetPrefix = findViewById(R.id.appwidget_prefix)

        // Bind the action for the save button.
        findViewById<View>(R.id.save_button).setOnClickListener(mOnClickListener)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }
        mAppWidgetPrefix!!.setText(loadTitlePref(this@ExampleAppWidgetConfigure, mAppWidgetId))
    }

    /**
     * [View.OnClickListener] for the "Save" button in our UI (R.id.save_button). When the button
     * is clicked, we initialize our [Context] variable `val context` to `this`
     * [ExampleAppWidgetConfigure] activity. We initialize [String] variable `val titlePrefix` to
     * the text contained in our [EditText] field [mAppWidgetPrefix]. We then call our method
     * [saveTitlePref] to save `titlePrefix` in our shared preferences file under the key formed by
     * concatenating [PREF_PREFIX_KEY] with the string value of [mAppWidgetId].
     *
     * Next we initialize [AppWidgetManager] variable `val appWidgetManager` to the instance for our
     * `context` and call the [ExampleAppWidgetProvider.updateAppWidget] method to have it update
     * the app widget with ID [mAppWidgetId] to display `titlePrefix`.
     *
     * Finally we create [Intent] variable `val resultValue`, store [mAppWidgetId] as an extra in it
     * under the key [AppWidgetManager.EXTRA_APPWIDGET_ID], set our result to `RESULT_OK` with
     * `resultValue` as the data [Intent] to propagate back to the originating activity, and call
     * [finish] to close this activity.
     */
    var mOnClickListener: View.OnClickListener = View.OnClickListener {
        val context: Context = this@ExampleAppWidgetConfigure

        // When the button is clicked, save the string in our prefs and return that they
        // clicked OK.
        val titlePrefix = mAppWidgetPrefix!!.text.toString()
        saveTitlePref(context, mAppWidgetId, titlePrefix)

        // Push widget update to surface with newly set prefix
        val appWidgetManager = AppWidgetManager.getInstance(context)
        ExampleAppWidgetProvider.updateAppWidget(
            context, appWidgetManager,
            mAppWidgetId, titlePrefix
        )

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    companion object {
        /**
         * TAG used for logging.
         */
        const val TAG: String = "ExampleAppWidgetConfigure"

        /**
         * Name of the shared preferences file that we use.
         */
        private const val PREFS_NAME = "com.example.android.apis.appwidget.ExampleAppWidgetProvider"

        /**
         * Prefix part of the key name that we use to store the title prefix for a particular app
         * widget in our shared preferences file. The app widget ID is appended to it to make the
         * complete key.
         */
        private const val PREF_PREFIX_KEY = "prefix_"

        /**
         * Writes the title of the app widget to the `SharedPreferences` file with the key formed by
         * appeneding the [appWidgetId] to the [PREF_PREFIX_KEY]. We retrieve a `SharedPreferences`
         * instance for the shared preferences file [PREFS_NAME], then retrieve
         * a `SharedPreferences.Editor` for it, instruct it to store our [String] parameter [text]
         * under the key created by concatenating [PREF_PREFIX_KEY] with our [Int] parameter
         * [appWidgetId] and then call the `commit` method of the editor to commit our preferences
         * changes back from this Editor to the `SharedPreferences` object it is editing.
         *
         * @param context the [Context] to use to retrieve the shared preferences file,
         * `this` [ExampleAppWidgetConfigure] activity in our caller.
         * @param appWidgetId the ID of the app widget we are saving the title for.
         * @param text the [String] to save as the title for our app widget.
         */
        @SuppressLint("ApplySharedPref")
        fun saveTitlePref(context: Context, appWidgetId: Int, text: String?) {
            context.getSharedPreferences(PREFS_NAME, 0).edit(commit = true) {
                putString(PREF_PREFIX_KEY + appWidgetId, text)
            }
        }

        /**
         * Reads the title of the app widget from the `SharedPreferences` object for this widget.
         * We initialize our `SharedPreferences` variable `val prefs` with the instance that accesses
         * the shared preferences file [PREFS_NAME]. Then we initialize our [String] variable
         * `val prefix` to the value stored under the key created by concatenating [PREF_PREFIX_KEY]
         * with our [appWidgetId] parameter, defaulting to `null` if it is not found. If `prefix` is
         * not `null` we return it to the caller, and if it is null we return the default string
         * found in our string resources under the ID `R.string.appwidget_prefix_default`
         * ("Default:").
         *
         * @param context the [Context] to use to retrieve the shared preferences file,
         * `this` [ExampleAppWidgetConfigure] activity in our caller.
         * @param appWidgetId the ID of the app widget we are to load the title prefix for.
         * @return the [String] we loaded for the title prefix for the app widget with ID [appWidgetId]
         * or the default value R.string.appwidget_prefix_default ("Oh hai") if none had been saved.
         */
        @JvmStatic
        fun loadTitlePref(context: Context, appWidgetId: Int): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val prefix = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
            return prefix ?: context.getString(R.string.appwidget_prefix_default)
        }

        @Suppress("UNUSED_PARAMETER")
        @JvmStatic
        fun deleteTitlePref(context: Context?, appWidgetId: Int) {
        }

        @Suppress("UNUSED_PARAMETER")
        @JvmStatic
        fun loadAllTitlePrefs(
            context: Context?, appWidgetIds: ArrayList<Int?>?,
            texts: ArrayList<String?>?
        ) {
        }
    }
}