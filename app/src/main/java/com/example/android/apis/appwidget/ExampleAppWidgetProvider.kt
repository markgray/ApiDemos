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

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import com.example.android.apis.R
import com.example.android.apis.appwidget.ExampleAppWidgetConfigure.Companion.deleteTitlePref
import com.example.android.apis.appwidget.ExampleAppWidgetConfigure.Companion.loadTitlePref

/**
 * A widget provider.  We have a string that we pull from a preference in order to show
 * the configuration settings and the current time when the widget was updated.  We also
 * register a BroadcastReceiver for time-changed and timezone-changed broadcasts, and
 * update then too.
 *
 *
 * See also the following files:
 *
 *  * ExampleAppWidgetConfigure.java
 *  * ExampleBroadcastReceiver.java
 *  * res/layout/appwidget_configure.xml
 *  * res/layout/appwidget_provider.xml
 *  * res/xml/appwidget_provider.xml
 *
 */
class ExampleAppWidgetProvider : AppWidgetProvider() {
    /**
     * Called in response to the `AppWidgetManager.ACTION_APPWIDGET_UPDATE` broadcast when this
     * AppWidget provider is being asked to provide [RemoteViews] for a set of AppWidgets.
     * Override this method to implement your own AppWidget functionality.
     *
     * We initialize our [Int] variable `val numAppWidgetIds` to the size of our [IntArray] parameter
     * [appWidgetIds]. Then we loop from `i` = 0 until `numAppWidgetIds` calling our [updateAppWidget]
     * method with the [Context] parameter [context], the [AppWidgetManager] parameter [appWidgetManager],
     * the [Int] `appWidgetId` that is stored in `appWidgetIds[ i ]`, and the [String] `titlePrefix`
     * that is returned by our [loadTitlePref] method for the current `appWidgetId`.
     *
     * @param context The [Context] in which this receiver is running.
     * @param appWidgetManager A [AppWidgetManager] object to call [AppWidgetManager.updateAppWidget]
     * when it is time to update your AppWidget.
     * @param appWidgetIds The `appWidgetIds` for which an update is needed. Note that this may be all
     * of the AppWidgets known to this provider, or just a subset of them.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate")
        // For each widget that needs an update, get the text that we should display:
        //   - Create a RemoteViews object for it
        //   - Set the text in the RemoteViews object
        //   - Tell the AppWidgetManager to show that views object for the widget.
        val numAppWidgetIds = appWidgetIds.size
        for (i in 0 until numAppWidgetIds) {
            val appWidgetId = appWidgetIds[i]
            val titlePrefix = loadTitlePref(context, appWidgetId)
            updateAppWidget(context, appWidgetManager, appWidgetId, titlePrefix)
        }
    }

    /**
     * Called in response to the `AppWidgetManager.ACTION_APPWIDGET_DELETED` broadcast when
     * one or more AppWidget instances have been deleted. Override this method to implement your
     * own AppWidget functionality.
     *
     * We initialize our [Int] variable `val numAppWidgetIds` to the size of our [IntArray] parameter
     * [appWidgetIds]. Then we loop from `i` = 0 until `numAppWidgetIds` calling our [deleteTitlePref]
     * method to delete the preference associated with the `appWidgetId` that is stored in
     * `appWidgetIds[ i ]`.
     *
     * @param context The [Context] in which this receiver is running.
     * @param appWidgetIds The `appWidgetIds` that have been deleted from their host.
     */
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Log.d(TAG, "onDeleted")
        // When the user deletes the widget, delete the preference associated with it.
        val numAppWidgetIds = appWidgetIds.size
        for (i in 0 until numAppWidgetIds) {
            deleteTitlePref(context, appWidgetIds[i])
        }
    }

    /**
     * Called in response to the `AppWidgetManager.ACTION_APPWIDGET_ENABLED` broadcast when
     * the AppWidget provider is first enabled. This is only called when the first instance of the
     * AppWidget provider is added to a host. Override this method to implement your own
     * AppWidget functionality.
     *
     * We initialize our [PackageManager] variable `val pm` with the handle to the system level
     * service that can retrieve various kinds of information related to the application packages
     * that are currently installed on the device. Then we call the [PackageManager.setComponentEnabledSetting]
     * method of `pm` to set the enabled setting for the component [ComponentName] whose package name
     * is "com.example.android.apis", and class is ".appwidget.ExampleBroadcastReceiver" to
     * [PackageManager.COMPONENT_ENABLED_STATE_ENABLED] (Flag for [PackageManager.setComponentEnabledSetting]:
     * This component or application is in an enabled state. Since this is the default state, you
     * will normally not need to set this). The `flags` are [PackageManager.DONT_KILL_APP] (Flag for
     * [PackageManager.setComponentEnabledSetting]: Don't kill the app containing the component.
     * Note that this flag should only be used if the calling package is the owner of this component).
     *
     * @param context The [Context] in which this receiver is running.
     */
    override fun onEnabled(context: Context) {
        Log.d(TAG, "onEnabled")
        // When the first widget is created, register for the TIMEZONE_CHANGED and TIME_CHANGED
        // broadcasts.  We don't want to be listening for these if nobody has our widget active.
        // This setting is sticky across reboots, but that doesn't matter, because this will
        // be called after boot if there is a widget instance for this provider.
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            ComponentName(
                "com.example.android.apis",
                ".appwidget.ExampleBroadcastReceiver"
            ),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    /**
     * Called in response to the `AppWidgetManager.ACTION_APPWIDGET_DISABLED` broadcast
     * when the last AppWidget instance for this provider is deleted. Override this method to
     * implement your own AppWidget functionality.
     *
     * We initialize our [PackageManager] variable `val pm` with the handle to the system level
     * service that can retrieve various kinds of information related to the application packages
     * that are currently installed on the device. Then we call the [PackageManager.setComponentEnabledSetting]
     * method of `pm` to set the enabled setting for the component [ComponentName] whose package name
     * is "com.example.android.apis", and class is ".appwidget.ExampleBroadcastReceiver" to
     * [PackageManager.COMPONENT_ENABLED_STATE_ENABLED] (Flag for [PackageManager.setComponentEnabledSetting]:
     * This component or application is in an enabled state. Since this is the default state, you
     * will normally not need to set this). The `flags` are [PackageManager.DONT_KILL_APP] (Flag for
     * [PackageManager.setComponentEnabledSetting]: Don't kill the app containing the component.
     * Note that this flag should only be used if the calling package is the owner of this component).
     *
     * @param context The [Context] in which this receiver is running.
     */
    override fun onDisabled(context: Context) {
        // When the first widget is created, stop listening for the TIMEZONE_CHANGED and
        // TIME_CHANGED broadcasts.
        Log.d(TAG, "onDisabled")
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            ComponentName(
                "com.example.android.apis",
                ".appwidget.ExampleBroadcastReceiver"
            ),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    companion object {
        /**
         * String used as TAG for logging statements.
         */
        private const val TAG = "ExampleAppWidgetProvide"

        /**
         * This function is called by the `onUpdate` override of our `AppWidgetProvider` as well
         * as by the `ExampleAppWidgetConfigure` class when its configuration of our AppWidget is
         * done. We log the values of our parameters `appWidgetId` and `titlePrefix`. We initialize
         * our [CharSequence] variable `val text` with the [String] that the [Context.getString]
         * method of our [Context] parameter [context] returns when it formats the format [String]
         * with resource ID [R.string.appwidget_text_format] ("%1$s \n%2$s") using the arguments
         * that are returned by the [loadTitlePref] method for `appWidgetId` ("WidgetPREFIX" by default)
         * and the hex string value of the current value of the system clock's `elapsedRealtime()`
         * (Time since boot, including time spent in sleep).
         *
         * Next we initialize our [RemoteViews] variable `val views` with a [RemoteViews] object that
         * will inflate the layout file with resource ID [R.layout.appwidget_provider] (this is the
         * layout that contains only the `TextView` with ID [R.id.appwidget_text] which is used to
         * display the text of our widget). We then call the [RemoteViews.setTextViewText] method of
         * `views` to set the text of the `TextView` with ID [R.id.appwidget_text] to our [CharSequence]
         * variable `text`.
         *
         * Finally we call the [AppWidgetManager.updateAppWidget] method of our [AppWidgetManager]
         * parameter `appWidgetManager` to have it update the `appWidgetId` widget with the description
         * of the widget views in our [RemoteViews] variable `views`.
         *
         * @param context The [Context] in which this receiver is running.
         * @param appWidgetManager The [AppWidgetManager] that is responsible for our widget.
         * @param appWidgetId The ID of the widget we are to update.
         * @param titlePrefix The prefix part of the string we are to display, this is stored in the
         */
        @JvmStatic
        fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int, titlePrefix: String
        ) {
            Log.d(TAG, "updateAppWidget appWidgetId=$appWidgetId titlePrefix=$titlePrefix")
            // Getting the string this way allows the string to be localized.  The format
            // string is filled in using java.util.Formatter-style format strings.
            val text: CharSequence = context.getString(
                R.string.appwidget_text_format,
                loadTitlePref(context, appWidgetId),
                "0x" + java.lang.Long.toHexString(SystemClock.elapsedRealtime())
            )

            // Construct the RemoteViews object.  It takes the package name (in our case, it's our
            // package, but it needs this because on the other side it's the widget host inflating
            // the layout from our package).
            val views = RemoteViews(
                context.packageName,
                R.layout.appwidget_provider
            )
            views.setTextViewText(R.id.appwidget_text, text)

            // Tell the widget manager
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}