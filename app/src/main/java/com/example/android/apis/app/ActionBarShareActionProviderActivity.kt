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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.widget.ShareActionProvider
import com.example.android.apis.R
import java.io.*

/**
 * This activity demonstrates how to use an android.view.ActionProvider
 * for adding functionality to the Action Bar. In particular this demo is adding
 * a menu item with ShareActionProvider as its action provider. The
 * ShareActionProvider is responsible for managing the UI for sharing actions.
 */
class ActionBarShareActionProviderActivity : Activity() {

    private lateinit var context : Context
    /**
     * Called when the activity is starting. First we call our super's implementation of onCreate,
     * then we call our method copyPrivateRawResourceToPubliclyAccessibleFile which copies our
     * demo photo from our private raw resource content to a publicly readable file so that the
     * latter can be shared with other applications.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = applicationContext
        copyPrivateRawResourceToPubliclyAccessibleFile()
    }

    /**
     * Initialize the contents of the Activity's standard options menu. You should place your
     * menu items into menu. First we get a MenuInflater with this context and use it
     * to inflate a menu hierarchy from our menu R.menu.action_bar_share_action_provider into the
     * options "menu" passed us. Then we set MenuItem actionItem to the "Share with..." action
     * found at R.id.menu_item_share_action_provider_action_bar in our menu. We fetch the action
     * provider specified for actionItem into ShareActionProvider actionProvider, set the file
     * name of the file for persisting the share history to DEFAULT_SHARE_HISTORY_FILE_NAME (the
     * default name for storing share history), then use our method createShareIntent to create a
     * sharing Intent and set the share Intent of ShareActionProvider actionProvider to this
     * Intent. Then we set MenuItem overflowItem to the "Share with..." action in the overflow menu
     * found at R.id.menu_item_share_action_provider_overflow in our menu. We fetch the action
     * provider specified for overflowItem into ShareActionProvider overflowProvider, set the file
     * name of the file for persisting the share history to DEFAULT_SHARE_HISTORY_FILE_NAME (the
     * default name for storing share history), then use our method createShareIntent to create a
     * sharing Intent and set the share Intent of ShareActionProvider overflowProvider to this
     * Intent. Finally we return true so that our menu will be displayed.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate your menu.
        menuInflater.inflate(R.menu.action_bar_share_action_provider, menu)

        // Set file with share history to the provider and set the share intent.
        val actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar)
        val actionProvider = actionItem.actionProvider as ShareActionProvider
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME)
        // Note that you can set/change the intent any time,
        // say when the user has selected an image.
        actionProvider.setShareIntent(createShareIntent())

        // Set file with share history to the provider and set the share intent.
        val overflowItem = menu.findItem(R.id.menu_item_share_action_provider_overflow)
        val overflowProvider = overflowItem.actionProvider as ShareActionProvider
        overflowProvider.setShareHistoryFileName(
                ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME)
        // Note that you can set/change the intent any time,
        // say when the user has selected an image.
        overflowProvider.setShareIntent(createShareIntent())

        return true
    }

    /**
     * Creates a sharing Intent. We first create the Intent shareIntent with the action
     * ACTION_SEND, and we set an explicit MIME data type of any kind of `image`. We create Uri uri for the
     * file created by copyPrivateRawResourceToPubliclyAccessibleFile using the absolute path on
     * the filesystem where the file was created, and add this Uri as extended data to the Intent
     * shareIntent. Finally we return the Intent shareIntent.
     *
     * @return The sharing intent.
     */
    @SuppressLint("SetWorldReadable")
    private fun createShareIntent():Intent {
        val shareIntent = Intent(Intent.ACTION_SEND)
        val path = context.filesDir.absolutePath + "shared.png"
        val f = File(path)
        f.setReadable(true, false)
        shareIntent.type = "image/*"
        shareIntent.data = Uri.fromFile(f)
        return shareIntent
    }

    /**
     * Copies a private raw resource content to a publicly readable file such that the latter can
     * be shared with other applications. First we initialize our variables InputStream inputStream
     * and FileOutputStream outputStream to null, then wrapped in a try intended to catch a possible
     * FileNotFoundException we open the data stream InputStream inputStream for reading the raw
     * resource R.raw.robot, and open the private file SHARED_FILE_NAME ("shared.png") associated
     * with this Context's application package for writing using FileOutputStream outputStream,
     * (creating the file if it doesn't already exist). Then in a loop surrounded by a try intended
     * to catch IOException we read up to 1024 bytes at a time from inputStream into "byte[] buffer"
     * and write the bytes read to outputStream as long as InputStream.read returns > 0 bytes read.
     * Our "finally" block for the outer try for catching FileNotFoundException closes inputStream
     * and outputStream (both calls surrounded by their own try intended to catch IOException.
     */
    @Suppress("DEPRECATION")
    @SuppressLint("WorldReadableFiles")
    private fun copyPrivateRawResourceToPubliclyAccessibleFile() {
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            inputStream = resources.openRawResource(R.raw.robot)
            outputStream = openFileOutput(SHARED_FILE_NAME,
                    Context.MODE_PRIVATE or Context.MODE_APPEND)
            val buffer = ByteArray(1024)
            var length: Int
            try {
                length = inputStream.read(buffer)
                while (length > 0) {
                    outputStream!!.write(buffer, 0, length)
                    length = inputStream.read(buffer)
                }
            } catch (ioe: IOException) {
                /* ignore */
            }

        } catch (fnfe: FileNotFoundException) {
            /* ignore */
        } finally {
            try {
                inputStream!!.close()
            } catch (ioe: IOException) {
                /* ignore */
            }
            try {
                outputStream?.close()
            } catch (ioe: IOException) {
                /* ignore */
            }
        }
    }

    companion object {

        /**
         * Name of the shared world readable file that we copy our raw resource R.raw.robot to.
         */
        private const val SHARED_FILE_NAME = "shared.png"
    }
}