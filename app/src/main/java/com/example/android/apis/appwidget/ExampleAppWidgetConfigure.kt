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
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import java.util.*

/**
 * The configuration screen for the ExampleAppWidgetProvider widget sample.
 */
@Suppress("MemberVisibilityCanBePrivate")
class ExampleAppWidgetConfigure : AppCompatActivity() {
    var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    var mAppWidgetPrefix: EditText? = null
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(Activity.RESULT_CANCELED)

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
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }
        mAppWidgetPrefix!!.setText(loadTitlePref(this@ExampleAppWidgetConfigure, mAppWidgetId))
    }

    var mOnClickListener = View.OnClickListener {
        val context: Context = this@ExampleAppWidgetConfigure

        // When the button is clicked, save the string in our prefs and return that they
        // clicked OK.
        val titlePrefix = mAppWidgetPrefix!!.text.toString()
        saveTitlePref(context, mAppWidgetId, titlePrefix)

        // Push widget update to surface with newly set prefix
        val appWidgetManager = AppWidgetManager.getInstance(context)
        ExampleAppWidgetProvider.updateAppWidget(context, appWidgetManager,
                mAppWidgetId, titlePrefix)

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    companion object {
        const val TAG = "ExampleAppWidgetConfigure"
        private const val PREFS_NAME = "com.example.android.apis.appwidget.ExampleAppWidgetProvider"
        private const val PREF_PREFIX_KEY = "prefix_"

        // Write the prefix to the SharedPreferences object for this widget
        @SuppressLint("ApplySharedPref")
        fun saveTitlePref(context: Context, appWidgetId: Int, text: String?) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
            prefs.commit()
        }

        // Read the prefix from the SharedPreferences object for this widget.
        // If there is no preference saved, get the default from a resource
        @JvmStatic
        fun loadTitlePref(context: Context, appWidgetId: Int): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val prefix = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
            return prefix ?: context.getString(R.string.appwidget_prefix_default)
        }

        @Suppress("UNUSED_PARAMETER")
        @JvmStatic
        fun deleteTitlePref(context: Context?, appWidgetId: Int) {}
        @Suppress("UNUSED_PARAMETER")
        @JvmStatic
        fun loadAllTitlePrefs(context: Context?, appWidgetIds: ArrayList<Int?>?,
                              texts: ArrayList<String?>?) {
        }
    }
}