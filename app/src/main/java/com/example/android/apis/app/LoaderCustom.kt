/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.example.android.apis.app

import android.R.drawable
import android.R.id
import android.R.layout
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.OperationCanceledException
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnCloseListener
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.ListFragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.AsyncTaskLoader
import androidx.loader.content.Loader
import com.example.android.apis.R
import com.example.android.apis.graphics.Utilities.id2p
import java.io.File
import java.text.Collator
import java.util.Collections

/**
 * Demonstration of the implementation of a custom Loader. Shows how to implement a custom
 * [AsyncTaskLoader], it uses the system function [PackageManager.getInstalledApplications]
 * to retrieve a `List<ApplicationInfo>` containing the AndroidManifest information for
 * all the installed apps.
 */
@Suppress("MemberVisibilityCanBePrivate")
class LoaderCustom : AppCompatActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, Then we retrieve a handle for the `FragmentManager` for interacting with fragments
     * associated with this activity to initialize our variable `val fm`. Then if when using `fm` to
     * find a Fragment with the ID android.R.id.content we find none (first time running), we create
     * a new instance of our [AppListFragment] fragment to initialize our variable `val list`, use
     * `fm` to begin a new `FragmentTransaction` which we use to add `list` to the activity state
     * with the ID android.R.id.content (our root view), and then commit the `FragmentTransaction`.
     * If `fm` did find a Fragment with ID android.R.id.content then we are being recreated after an
     * orientation change and need do nothing.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fm = supportFragmentManager

        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(id.content) == null) {
            val list = AppListFragment()
            fm.beginTransaction().add(id.content, list).commit()
        } else {
            Log.i(TAG, "There is already an android.R.id.content Fragment")
        }
    }

    /**
     * This class holds the per-item data in our Loader.
     * Constructor for an instance holding `ApplicationInfo info` describing a particular
     * package. We initialize our [AppListLoader] field [mLoader] with the value of our
     * parameter `AppListLoader loader`, and our field `ApplicationInfo mInfo` with
     * the value of our parameter `ApplicationInfo info`. Finally we initialize our field
     * `File mApkFile` with a new `File` instance derived from the pathname given in
     * the field `info.sourceDir` (full path to the base APK for the application).
     *
     * @property mLoader [AppListLoader] which created us using its *this*
     * @property applicationInfo ApplicationInfo for package we are assigned to, one of the
     * [ApplicationInfo] instances from the list that is returned from the call to
     * `PackageManager.getInstalledApplications`
     */
    class AppEntry(
        private val mLoader: AppListLoader,
        val applicationInfo: ApplicationInfo
    ) {
        /**
         * Full path to the base APK for the package
         */
        private val mApkFile: File = File(applicationInfo.sourceDir)

        /**
         * Application label as discovered by the method loadLabel
         */
        var label: String? = null
            private set

        /**
         * Icon loaded from application apk by the method getIcon
         */
        private var mIcon: Drawable? = null

        /**
         * Flag indicating whether we found an apk for the package
         */
        private var mMounted: Boolean = false

        /**
         * Getter method for our [Drawable] field [mIcon] (loading it from the apk or supplying
         * a default icon if necessary.)
         *
         * If the current value of [mIcon] is null (our first time being called, or the apk was not
         * found to load an Icon from) we check to see if our [File] field [mApkFile] exists, and if
         * it does we set [mIcon] to the drawable returned by calling the method `loadIcon` using
         * our instance of [ApplicationInfo] in our field [applicationInfo] and return [mIcon] to
         * the caller. If our [File] field [mApkFile] does not exist we set
         * our [Boolean] flag field [mMounted] to *false* and fall through to return the system
         * drawable android.R.drawable.sym_def_app_icon. (Never setting [mIcon] notice, so the same
         * code path will likely be followed again -- might be more efficient to set [mIcon] to the
         * default icon for the next time, or does this logic allow for an apk file to suddenly
         * appear between calls to this method?)
         *
         * If [mIcon] is not *null*, we check to see if our flag [mMounted] is *false* and if so
         * we check to see if our [File] field [mApkFile] exists, and if it does we set our flag
         * [mMounted] to *true*, set [mIcon] to the drawable returned by calling the method
         * `loadIcon` using our instance of [ApplicationInfo] in our field [applicationInfo]
         * and return the [Drawable] in our field [mIcon] to the caller. If [mMounted] was *true*
         * we simply return [mIcon] to the caller.
         *
         * @return either the contents of our [Drawable] field [mIcon] or the system default app
         * icon android.R.drawable.sym_def_app_icon
         */
        val icon: Drawable?
            get() {
                if (mIcon == null) {
                    if (mApkFile.exists()) {
                        mIcon = applicationInfo.loadIcon(mLoader.mPm)
                        return mIcon
                    } else {
                        mMounted = false
                    }
                } else if (!mMounted) {
                    // If the app wasn't mounted but is now mounted, reload
                    // its icon.
                    if (mApkFile.exists()) {
                        mMounted = true
                        mIcon = applicationInfo.loadIcon(mLoader.mPm)
                        return mIcon
                    }
                } else {
                    return mIcon
                }

                return ResourcesCompat.getDrawable(
                    mLoader.context.resources,
                    drawable.sym_def_app_icon,
                    null
                )
            }

        /**
         * Returns a string containing a concise, human-readable description of this object, which
         * in our case is the [String] field [label] which is set by our method [loadLabel].
         *
         * @return a printable representation of this object, in our case the [String] field [label]
         * which is the current textual label associated with the application we describe, or the
         * packageName if none could be loaded.
         */
        override fun toString(): String {
            return label!!
        }

        /**
         * Makes sure our [String] field [label] is loaded, either from the apk, or the contents
         * of the field `packageName` of our [ApplicationInfo] field [applicationInfo].
         *
         * If [label] is currently *null*, or the apk has not been mounted (our flag
         * field [mMounted] is *false*) we check to see if our [File] field [mApkFile] exists
         * and if it does not we set [mMounted] to *false* and set [label] to the contents
         * of the `packageName` field our our [ApplicationInfo] field [applicationInfo]. If the
         * apk file does exist we set [mMounted] to *true* and try to load the label from the apk
         * to initialize our [CharSequence] variable `val label`. If successful we set [label] to
         * the [String] value of `label`, otherwise we set [label] to the contents of the
         * `packageName` field our our [ApplicationInfo] field [applicationInfo].
         *
         * If [label] is currently not *null*, and the apk has been mounted (our flag [mMounted] is
         * *true*) we do nothing.
         *
         * @param context traces back to the an application context retrieved from the Context
         * passed to the constructor, which is called with `getActivity()` in our case
         */
        internal fun loadLabel(context: Context) {
            if (label == null || !mMounted) {
                if (!mApkFile.exists()) {
                    mMounted = false
                    label = applicationInfo.packageName
                } else {
                    mMounted = true
                    val label: CharSequence = applicationInfo.loadLabel(context.packageManager)

                    this.label = label.toString()
                }
            }
        }
    }

    /**
     * Helper for determining if the configuration has changed in an interesting way so we need to
     * rebuild the app list. To use this class one creates an instance of this class when your loader
     * class is instantiated such as is done in our [AppListLoader] class, where it uses an instance
     * of [InterestingConfigChanges] in its `mLastConfig` field for this purpose. Then when you need
     * to decide whether a configuration change has necessitated a reload of your data, call
     * `mLastConfig.applyNewConfig(getContext().getResources())`, and if the result returned is
     * true, a change has occurred in screen density, or the `Configuration` fields for CONFIG_LOCALE,
     * CONFIG_UI_MODE, and/or CONFIG_SCREEN_LAYOUT have changed since last updated. If the result is
     * false, no interesting Configuration changes have occurred.
     */
    class InterestingConfigChanges {
        /**
         * Starts out as an invalid [Configuration] and is updated using the current
         * [Resources] by our method [applyNewConfig]
         */
        internal val mLastConfiguration = Configuration()

        /**
         * The screen density expressed as dots-per-inch. May be either DENSITY_LOW, DENSITY_MEDIUM,
         * or DENSITY_HIGH.
         */
        internal var mLastDensity: Int = 0

        /**
         * Called to update our [Configuration] field [mLastConfiguration] with the latest values
         * of application resources, and to determine if any changes in the configuration necessitate
         * action on the part of the caller. First we update our field containing the previous values
         * of configuration [mLastConfiguration], saving the bit mask of changed fields in our [Int]
         * variable `val configChanges`. Then we fetch the current display metrics for screen
         * density in dpi, and compare it with the previous value stored our [mLastDensity] field to
         * set our [Boolean] variable `val densityChanged`. Then we check whether the density changed,
         * or whether the bit fields for CONFIG_LOCALE, CONFIG_UI_MODE, and/or CONFIG_SCREEN_LAYOUT
         * are set in `configChanges` and if so we update [mLastDensity] and return *true* to the
         * caller in order to indicate that an "interesting config change" has occurred. Otherwise
         * we return *false* to indicate that no change of interest has occurred.
         *
         * @param res Class for accessing an application's resources, it is acquired by calling
         * `getContext().getResources()` in the `onStartLoading` callback of [AppListLoader] (our
         * custom `AsyncTaskLoader<List<AppEntry>>`
         * @return *true* if a change has occurred which requires us to reload our list of
         * application entries.
         */
        internal fun applyNewConfig(res: Resources): Boolean {
            val configChanges = mLastConfiguration.updateFrom(res.configuration)
            val densityChanged = mLastDensity != res.displayMetrics.densityDpi
            if (densityChanged || configChanges and (ActivityInfo.CONFIG_LOCALE
                    or ActivityInfo.CONFIG_UI_MODE or ActivityInfo.CONFIG_SCREEN_LAYOUT) != 0
            ) {
                mLastDensity = res.displayMetrics.densityDpi
                return true
            }
            return false
        }
    }

    /**
     * Helper class to look for interesting changes to the installed apps so that the loader can be
     * updated. It does this by registering itself as a [BroadcastReceiver] for the various
     * package change broadcast [Intent]'s using [IntentFilter]'s for the actions:
     *
     *  * `Intent.ACTION_PACKAGE_ADDED`
     *  * `Intent.ACTION_PACKAGE_REMOVED`
     *  * `Intent.ACTION_PACKAGE_CHANGED`
     *  * `Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE`
     *  * `Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE`
     *
     * Then it calls the `onContentChanged` method of our [AppListLoader] field [mLoader] (which it
     * inherits unchanged from its superclass [AsyncTaskLoader]) when it receives one of these
     * [Intent]'s in its [onReceive] override.
     * Constructor that initializes our [AppListLoader] field [mLoader] with the parameter
     * passed it, and registers itself to receive the broadcast `Intent`'s we are interested
     * in. The default constructor saves its parameter in our [AppListLoader] field [mLoader].
     * Then in our *init* block we create an [IntentFilter] instance that matches the action
     * ACTION_PACKAGE_ADDED to initialize our variable `val filter`, then add the additional
     * actions ACTION_PACKAGE_REMOVED, and ACTION_PACKAGE_CHANGED to `filter`. We use our field
     * [mLoader] to obtain its [Context] which we use to register *this* as a [BroadcastReceiver]
     * for the actions in `filter`. We create an empty [IntentFilter] to initialize our variable
     * `val sdFilter` and add to this empty filter the actions ACTION_EXTERNAL_APPLICATIONS_AVAILABLE
     * and ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE. We use our [AppListLoader] field [mLoader]
     * to obtain its [Context] which we use to register *this* as a [BroadcastReceiver]
     * for the actions in `sdFilter`.
     *
     * @param mLoader used to obtain [Context] where needed and to call the callback method
     * `onContentChanged` (which it  inherits unchanged from its superclass `AsyncTaskLoader`)
     * when the [AppListLoader] needs to reload its data.
     */
    class PackageIntentReceiver(
        internal val mLoader: AppListLoader
    ) : BroadcastReceiver() {

        init {
            val filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED)
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED)
            filter.addDataScheme("package")
            mLoader.context.registerReceiver(this, filter)
            // Register for events related to sdcard installation.
            val sdFilter = IntentFilter()
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE)
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE)
            mLoader.context.registerReceiver(this, sdFilter)
        }

        /**
         * This method is called when the [BroadcastReceiver] is receiving an [Intent] broadcast.
         * We merely inform our [AppListLoader] field [mLoader] that the data it is handling may have
         * changed by calling its callback method `onContentChanged` (which it inherits
         * unchanged from its superclass `AsyncTaskLoader`).
         *
         * @param context The [Context] in which the receiver is running.
         * @param intent  The [Intent] being received.
         */
        override fun onReceive(context: Context, intent: Intent) {
            // Tell the loader about the change.
            mLoader.onContentChanged()
        }
    }

    /**
     * A custom [Loader] that loads all of the installed applications.
     * Constructor which initializes our [PackageManager] field [mPm] with a
     * [PackageManager] instance.
     *
     * @param context used only to pass on to our super's constructor
     */
    open class AppListLoader(context: Context) : AsyncTaskLoader<List<AppEntry>>(context) {

        /**
         * Helper for determining if the configuration has changed in a way that may require us
         * to redisplay it.
         */
        internal val mLastConfig = InterestingConfigChanges()

        /**
         * [PackageManager] instance we use to retrieve package information for the installed
         * packages.
         */
        @Suppress("LeakingThis")
        internal val mPm: PackageManager = getContext().packageManager

        /**
         * Our list of [AppEntry] Objects describing the installed applications which we supply
         * to those using us as a [Loader].
         */
        internal var mApps: List<AppEntry>? = null

        /**
         * Helper class to look for interesting changes to the installed apps so that the loader can
         * be updated, it registers itself for package changing broadcast Intents and calls our
         * super's method `onContentChanged` when it receives one in its `onReceive` method.
         * (The super then arranges for new data to be loaded by calling `onForceLoad` which
         * creates a new `LoadTask` and executes it.)
         */
        internal var mPackageObserver: PackageIntentReceiver? = null

        /**
         * This is where the bulk of our work is done. This function is called in a background
         * thread and should generate a new set of data to be published by the loader. First we
         * use our [PackageManager] field [mPm] to retrieve all known applications to our variable
         * `List<ApplicationInfo> apps`. If no apps are returned (`apps` is null) we allocate
         * an empty `ArrayList<>` for `apps`. We initialize our [Context] variable `val context`
         * with an application context retrieved from the [Context] passed to the constructor. We
         * create our return list `List<AppEntry>` to initialize our variable `val entries`, and
         * populate it with an [AppEntry] for each of the [ApplicationInfo] instances in `apps`.
         * When done we sort `entries` using our `Comparator<AppEntry> ALPHA_COMPARATOR`. Finally
         * we return `entries` to the caller.
         *
         * @return The result of the load operation.
         */
        @SuppressLint("QueryPermissionsNeeded")
        override fun loadInBackground(): List<AppEntry>? {
            // Retrieve all known applications.

            @SuppressLint("InlinedApi")
            var apps: List<ApplicationInfo>? = mPm.getInstalledApplications(
                PackageManager.MATCH_UNINSTALLED_PACKAGES
                    or PackageManager.MATCH_DISABLED_COMPONENTS
            )

            if (apps == null) {
                apps = ArrayList()
            }

            val context = context

            // Create corresponding array of entries and load their labels.
            val entries = ArrayList<AppEntry>(apps.size)
            for (i in apps.indices) {
                val entry = AppEntry(this, apps[i])
                entry.loadLabel(context)
                entries.add(entry)
            }

            // Sort the list.
            Collections.sort(entries, ALPHA_COMPARATOR)

            // Done!
            return entries
        }

        /**
         * Called when there is new data to deliver to the client. The super class will take care
         * of delivering it; the implementation here just adds a little more logic. Must be called
         * from the process's main thread.
         *
         * First we check whether this load has been reset. That is, either the loader has not yet
         * been started for the first time, or its `reset()` method has been called. If so, we call
         * our method `onReleaseResources(apps)` if `apps` is currently holding data we do not need.
         * (Since we are using only a `List`, `onReleaseResources` does nothing, but in
         * an app using a cursor it would close the cursor.)
         *
         * We save our `List<AppEntry>` field [mApps] in our variable `val oldApps` and set
         * [mApps] to the value of our `List<AppEntry>` parameter [apps]. If our load has
         * been started (i.e. `startLoading()` has been called and no calls to `stopLoading()` or
         * `reset()` have yet been made) we call our super's implementation of `deliverResult` to
         * do the actual delivering of the data in [apps] to the client. Finally if `oldApps` is
         * not null, we call our method `onReleaseResources(oldApps)` to release resources (again,
         * not needed in our case).
         *
         * @param apps the result of the load
         */
        override fun deliverResult(apps: List<AppEntry>?) {
            if (isReset) {
                // An async query came in while the loader is stopped.  We
                // don't need the result.
                if (apps != null) {
                    onReleaseResources(apps)
                }
            }

            val oldApps = mApps
            mApps = apps

            if (isStarted) {
                // If the Loader is currently started, we can immediately
                // deliver its results.
                super.deliverResult(apps)
            }

            // At this point we can release the resources associated with
            // 'oldApps' if needed; now that the new result is delivered we
            // know that it is no longer in use.
            if (oldApps != null) {
                onReleaseResources(oldApps)
            }
        }

        /**
         * Handles a request to start the Loader. Subclasses of [Loader] (via our extension
         * of [AsyncTaskLoader]) must implement this to take care of loading their data, as
         * per [startLoading]. This is not called by clients directly, but as a result of
         * a call to [startLoading].
         *
         * If we currently have a result available in our `List<AppEntry>` field [mApps], we deliver
         * it immediately by calling our override of [deliverResult] with [mApps] as its parameter.
         * Then if it is currently *null*, we create an instance of [PackageIntentReceiver] to
         * initialize our field [mPackageObserver] which will register us for broadcast [Intent]'s
         * for changes in the installed packages which will (probably) invalidate our results
         * thereby necessitating a forced reload. Then we check whether configuration changes might
         * have occurred which would require our data to be redisplayed and save the result in the
         * [Boolean] flag variable `val configChange`.
         *
         * Finally based on whether the current flag indicating whether the loader's content had
         * changed while it was stopped is set, or [mApps] is still *null*, or `configChange`
         * is *true* we force an asynchronous load by calling `forceLoad()`. This will ignore a
         * previously loaded data set and load a new one. It does this by calling through to the
         * implementation's `onForceLoad()`.
         */
        override fun onStartLoading() {
            if (mApps != null) {
                // If we currently have a result available, deliver it
                // immediately.
                deliverResult(mApps)
            }

            // Start watching for changes in the app data.
            if (mPackageObserver == null) {
                mPackageObserver = PackageIntentReceiver(this)
            }

            // Has something interesting in the configuration changed since we
            // last built the app list?
            val configChange = mLastConfig.applyNewConfig(context.resources)

            if (takeContentChanged() || mApps == null || configChange) {
                // If the data has changed since the last time it was loaded
                // or is not currently available, start a load.
                forceLoad()
            }
        }

        /**
         * Subclasses of [Loader] must implement this to take care of stopping their loader,
         * as per [stopLoading].  This is not called by clients directly, but as a result
         * of a call to [stopLoading]. This will always be called from the process's main
         * thread. We simply call the method [cancelLoad].
         */
        override fun onStopLoading() {
            // Attempt to cancel the current load task if possible.
            if (cancelLoad()) {
                Log.i(TAG, "cancelLoad() returned true")
            } else {
                Log.i(TAG, "cancelLoad() returned false")
            }

        }

        /**
         * Called if the task was canceled before it was completed. Gives the class a chance
         * to clean up post-cancellation and to properly dispose of the result. First we call
         * through to our super's implementation of `onCanceled`, then we call our method
         * [onReleaseResources] (which does nothing, but serves as an example in case the
         * code is reused in an application which has resources which need to released.)
         *
         * @param apps The value that was returned by [loadInBackground], or *null* if the task
         * threw [OperationCanceledException].
         */
        override fun onCanceled(apps: List<AppEntry>?) {
            super.onCanceled(apps)

            // At this point we can release the resources associated with 'apps'
            // if needed.
            onReleaseResources(apps)
        }

        /**
         * Handles a request to completely reset the Loader. First we call through to our super's
         * implementation of `onReset`, then we call our override of [onStopLoading] to
         * cancel the load, then we call our method [onReleaseResources] (which does nothing, but
         * serves as an example in case the code is reused in an application which has resources
         * which need to released.) Finally if our [PackageIntentReceiver] field [mPackageObserver]
         * is listening for package change broadcasts, we unregister it and set it to null.
         */
        override fun onReset() {
            super.onReset()

            // Ensure the loader is stopped
            onStopLoading()

            // At this point we can release the resources associated with 'apps'
            // if needed.
            if (mApps != null) {
                onReleaseResources(mApps)
                mApps = null
            }

            // Stop monitoring for changes.
            if (mPackageObserver != null) {
                context.unregisterReceiver(mPackageObserver)
                mPackageObserver = null
            }
        }

        /**
         * Helper function to take care of releasing resources associated with an actively loaded
         * data set. For a simple `List<>` there is nothing to do, so we do nothing. For
         * something like a `Cursor`, we would close it here.
         */
        @Suppress("UNUSED_PARAMETER")
        private fun onReleaseResources(apps: List<AppEntry>?) {
            // For a simple List<> there is nothing to do.  For something
            // like a Cursor, we would close it here.
        }
    }

    /**
     * `ListAdapter` used as cursor to populate the [ListFragment]'s list of our Fragment
     * [AppListFragment]
     * Constructor for a new instance of [AppListAdapter]. First we call through to our super's
     * constructor supplying a stock system layout for a `TwoLineListItem` (for no apparent
     * reason since we use our own layout file for our list item Views), and then we initialize our
     * [LayoutInflater] field [mInflater] with the [LayoutInflater] returned from the system-level
     * service LAYOUT_INFLATER_SERVICE.
     *
     * @param context This is the [Context] to use, in our case it is the [Activity]
     * returned by `getActivity()`
     */
    class AppListAdapter(context: Context) :
        ArrayAdapter<AppEntry>(context, layout.simple_list_item_2) {

        /**
         * `LayoutInflater` created in constructor using the `Context` passed it to get
         * the handle to the system-level service LAYOUT_INFLATER_SERVICE which returns a
         * `LayoutInflater` for inflating layout resources in this context. `mInflater`
         * is then used in our `getView` override when it needs to inflate the layout file
         * for a `List` item `View`.
         */
        private val mInflater: LayoutInflater =
            context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        /**
         * Sets the contents of our [ArrayAdapter]. First we remove all elements from our list,
         * and then if our `List<AppEntry>` parameter [data] is not *null*, we add all the elements
         * in [data] to the end of our [ArrayAdapter].
         *
         * @param data list of package information returned from package manager by our background
         * loader (or null if it is being invalidated after a loader reset.)
         */
        fun setData(data: List<AppEntry>?) {
            clear()
            if (data != null) {
                addAll(data)
            }
        }

        /**
         * Get a [View] that displays the data at the specified position in the data set. First we
         * declare a [View] variable `val view`, and if our [View] parameter [convertView] is *null*
         * we use our [LayoutInflater] field [mInflater] to inflate the list item layout file
         * R.layout.list_item_icon_text into `view`. If [convertView] is not *null* we recycle it
         * by setting `view` to it. Next we fetch the item at [position] to initialize our [AppEntry]
         * variable `val item`, set the [ImageView] with ID R.id.icon in `view` to the icon associated
         * with the data in `item` and set the text of the [TextView] with ID R.id.text in `view` to
         * the `label` associated with `item`. Finally we return `view` to the caller.
         *
         * @param position    The position of the item within the adapter's data set of the item
         * whose view we want.
         * @param convertView The old view to reuse, if possible.
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View = convertView ?: mInflater.inflate(
                R.layout.list_item_icon_text,
                parent,
                false
            )

            val item = getItem(position)

            (view.findViewById<View>(R.id.icon) as ImageView).setImageDrawable(item!!.icon)
            (view.findViewById<View>(R.id.text) as TextView).text = item.label

            return view
        }
    }

    /**
     * The [ListFragment] which displays the data from our custom [AppListAdapter] in its list.
     */
    class AppListFragment : ListFragment(),
        OnQueryTextListener,
        OnCloseListener,
        LoaderManager.LoaderCallbacks<List<AppEntry>> {

        /**
         * This is the Adapter being used to display the list's data.
         */
        internal lateinit var appListAdapter: AppListAdapter

        /**
         * The [SearchView] for doing filtering.
         */
        internal lateinit var mSearchView: SearchView

        /**
         * If non-null, this is the current filter the user has provided.
         */
        internal var mCurFilter: String? = null

        /**
         * Called when all saved state has been restored into the view hierarchy of the fragment.
         * This is called after [onViewCreated] and before [onStart]. First we call through to our
         * super's implementation of `onViewStateRestored`, then we set the empty text to be shown
         * in our [ListView] if we are unable to load any data to the string "No applications". Then
         * we report that this fragment would like to participate in populating the options menu by
         * receiving a call to [onCreateOptionsMenu] and related methods. We initialize our
         * [AppListAdapter] field [appListAdapter] with an instance of our custom adapter [AppListAdapter]
         * using the [Activity] our Fragment is associated with as the [Context]. and set our list
         * adapter to [appListAdapter]. We set our [ListView] to display an indeterminate progress
         * indicator while we wait for our loader to finish loading its data. Then we initialize
         * the loader using *this* for the [LoaderManager.LoaderCallbacks] parameter (so our methods
         * [onCreateLoader], [onLoadFinished], and [onLoaderReset] will be called.)
         *
         * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
         */
        override fun onViewStateRestored(savedInstanceState: Bundle?) {
            super.onViewStateRestored(savedInstanceState)
            getListView().setPadding(
                id2p(8),
                id2p(150),
                id2p(8),
                id2p(60),
            )

            // Give some text to display if there is no data.  In a real
            // application this would come from a resource.
            setEmptyText("No applications")

            // We have a menu item to show in action bar.
            @Suppress("DEPRECATION")
            setHasOptionsMenu(true)

            // Create an empty adapter we will use to display the loaded data.
            appListAdapter = AppListAdapter(activity as Context)
            listAdapter = appListAdapter

            // Start out with a progress indicator.
            setListShown(false)

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            LoaderManager.getInstance(this).initLoader(0, null, this)
        }

        /**
         * Custom [SearchView] which clears its search text when it is collapsed.
         */
        class MySearchView
        /**
         * Simple constructor, just calls super's constructor.
         *
         * @param context Activity returned from `getActivity` in our case.
         */
            (context: Context) : SearchView(context) {

            /**
             * Called when this view is collapsed as an action view.
             * See [MenuItem.collapseActionView].
             *
             * The normal [SearchView] doesn't clear its search text when collapsed,
             * so we will do this for it. We set the query string in the text field
             * to the empty [String] (passing *false* to not submit it), and then call
             * our super's implementation of `onActionViewCollapsed`.
             */
            override fun onActionViewCollapsed() {
                setQuery("", false)
                super.onActionViewCollapsed()
            }
        }

        /**
         * Initialize the contents of the Activity's standard options menu.  You
         * should place your menu items in to the [Menu] parameter [menu]. For this method
         * to be called, you must have first called [setHasOptionsMenu].
         * See [Activity.onCreateOptionsMenu] for more information.
         *
         * First we create a [MenuItem] to initialize our variable `val item` by adding a [MenuItem]
         * with the title "Search" to our [Menu] parameter [menu]. We set the ICON of `item` to the
         * system drawable ic_menu_search, and set the show as action flags SHOW_AS_ACTION_IF_ROOM
         * and SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW. We initialize our [SearchView] field [mSearchView]
         * with a new instance of [MySearchView], set its [OnQueryTextListener] to *this*, set its
         * [OnCloseListener] to *this*, and set its iconified by default property to *true*. Finally
         * we set the action view of `item` to our [mSearchView] field.
         *
         * @param menu     The options menu in which you place your items.
         * @param inflater [MenuInflater] to use to inflate xml layout file (unused)
         */
        @Deprecated("Deprecated in Java")
        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            // Place an action bar item for searching.
            val item = menu.add("Search")
            item.setIcon(drawable.ic_menu_search)
            item.setShowAsAction(
                MenuItem.SHOW_AS_ACTION_IF_ROOM or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW
            )
            mSearchView = MySearchView(activity as Context)
            mSearchView.setOnQueryTextListener(this)
            mSearchView.setOnCloseListener(this)
            mSearchView.setIconifiedByDefault(true)
            item.actionView = mSearchView
        }

        /**
         * Called when the query text is changed by the user. If the [newText] entered by the
         * user is not empty, we set our [String] field [mCurFilter] to it, otherwise we set
         * [mCurFilter] to *null*. We get a `Filter` for our [AppListAdapter] field [appListAdapter]
         * and then use it to start an asynchronous filtering operation using [mCurFilter]
         * (canceling all previous non-executed filtering requests and posting a new filtering
         * request that will be executed later.) Finally we return *true* to indicate that the
         * action was handled by us.
         *
         * @param newText the new content of the query text field.
         * @return *false* if the [SearchView] should perform the default action of showing any
         * suggestions if available, *true* if the action was handled by the listener.
         */
        override fun onQueryTextChange(newText: String): Boolean {
            // Called when the action bar search text has changed.  Since this
            // is a simple array adapter, we can just have it do the filtering.
            mCurFilter = if (!TextUtils.isEmpty(newText)) newText else null
            appListAdapter.filter.filter(mCurFilter)
            return true
        }

        /**
         * Called when the user submits the query. This could be due to a key press on the keyboard
         * or due to pressing a submit button. The listener can override the standard behavior by
         * returning *true* to indicate that it has handled the submit request. Otherwise return
         * *false* to let the [SearchView] handle the submission by launching any associated intent.
         *
         * We don't care about this, so we just return *true*
         *
         * @param query the query text that is to be submitted
         * @return *true* if the query has been handled by the listener, *false* to let the
         * [SearchView] perform the default action.
         */
        override fun onQueryTextSubmit(query: String): Boolean {
            // Don't care about this.
            return true
        }

        /**
         * The user is attempting to close the [SearchView]. If the query in our [SearchView] field
         * [mSearchView] is not empty, we set the query to null and submit it. Finally we return
         * *true* to indicate we have consumed the event.
         *
         * @return *true* if the listener wants to override the default behavior of clearing the
         * text field and dismissing it, *false* otherwise.
         */
        override fun onClose(): Boolean {
            if (!TextUtils.isEmpty(mSearchView.query)) {
                mSearchView.setQuery(null, true)
            }
            return true
        }

        /**
         * This method will be called when an item in the list of the [ListView] is selected.
         * Subclasses should override. Subclasses can call
         * `getListView().getItemAtPosition(position)` if they need to access the data associated
         * with the selected item.
         *
         * We just log the event having happened.
         *
         * @param l        The [ListView] where the click happened
         * @param v        The [View] that was clicked within the ListView
         * @param position The position of the view in the list
         * @param id       The row id of the item that was clicked
         */
        override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
            // Insert desired behavior here.
            Log.i("LoaderCustom", "Item clicked: $id")
        }

        /**
         * Instantiate and return a new [Loader] for the given ID. We just return an [AppListLoader]
         * created using the [Activity] of [LoaderCustom] as its [Context].
         *
         * @param id   The ID whose loader is to be created.
         * @param args Any arguments supplied by the caller.
         * @return Return a new [Loader] instance that is ready to start loading.
         */
        override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<AppEntry>> {
            // This is called when a new Loader needs to be created.  This
            // sample only has one Loader with no arguments, so it is simple.
            return AppListLoader(activity as Context)
        }

        /**
         * Called when a previously created loader has finished its load. We set the data of our
         * [AppListAdapter] field [appListAdapter] to our `List<AppEntry>` parameter [data]. If our
         * Fragment is in the `Resumed` state (newly created) we set our `List` to be shown,
         * otherwise (an orientation change has occurred) we set our `List` to be shown without the
         * animation from the previous state (don't know why, because the animation looks nifty).
         *
         * @param loader The [Loader] that has finished.
         * @param data   The data generated by the Loader.
         */
        override fun onLoadFinished(loader: Loader<List<AppEntry>>, data: List<AppEntry>) {
            // Set the new data in the adapter.
            appListAdapter.setData(data)

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
         * remove any references it has to the Loader's data.
         *
         * We simply set the data of our [AppListAdapter] field [appListAdapter] to null.
         *
         * @param loader The Loader that is being reset.
         */
        override fun onLoaderReset(loader: Loader<List<AppEntry>>) {
            // Clear the data in the adapter.
            appListAdapter.setData(null)
        }
    }

    /**
     * Our static constants.
     */
    companion object {
        /**
         * TAG used for logging.
         */
        internal var TAG = "LoaderCustom"

        /**
         * Perform alphabetical comparison of application [AppEntry] objects.
         */
        val ALPHA_COMPARATOR: Comparator<AppEntry> = object : Comparator<AppEntry> {
            /**
             * Collator object for the default locale.
             */
            private val sCollator = Collator.getInstance()

            /**
             * Compares the two specified [AppEntry] Objects to determine their relative ordering.
             * The ordering implied by the return value of this method for all possible pairs of
             * `(object1, object2)` should form an *equivalence relation*.
             * This means that:
             *
             *  * `compare(a,a)` returns zero for all `a`
             *  * the sign of `compare(a,b)` must be the opposite of the sign of `compare(b,a)`
             *    for all pairs of (a,b)
             *  * From `compare(a,b) > 0` and `compare(b,c) > 0` it must follow
             *    `compare(a,c) > 0` for all possible combinations of `(a,b,c)`
             *
             * We simply return the results of applying our [Collator] field [sCollator] to the
             * labels fetched from `object1` and `object2` respectively, thereby defining an
             * alphabetical ordering of [AppEntry] Objects based on the alphabetical ordering of
             * their labels as fetched from their [String] field `mLabel`.
             *
             * @param object1 an [AppEntry].
             * @param object2 a second [AppEntry] to compare with [object1].
             * @return an integer < 0 if [object1] is less than [object2], 0 if they are
             * equal, and > 0 if [object1] is greater than [object2].
             * @throws ClassCastException if objects are not of the correct type.
             */
            override fun compare(object1: AppEntry, object2: AppEntry): Int {
                return sCollator.compare(object1.label, object2.label)
            }
        }
    }
}
