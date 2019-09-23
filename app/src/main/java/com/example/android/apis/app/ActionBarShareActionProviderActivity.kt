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

import android.annotation.TargetApi
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.widget.ShareActionProvider
import com.example.android.apis.R

/**
 * This activity demonstrates how to use an [android.view.ActionProvider]
 * for adding functionality to the Action Bar. In particular this demo is adding
 * a menu item with ShareActionProvider as its action provider. The
 * ShareActionProvider is responsible for managing the UI for sharing actions.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class ActionBarShareActionProviderActivity : Activity() {

    /**
     * Called when the activity is starting. We just call our super's implementation of `onCreate`
     *
     * @param savedInstanceState always null since [onSaveInstanceState] is not overridden
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Initialize the contents of the Activity's standard options menu. You should place your
     * menu items into [menu]. First we get a `MenuInflater` with this context and use it
     * to inflate a menu hierarchy from our menu R.menu.action_bar_share_action_provider into the
     * options "menu" passed us. Then we set `MenuItem actionItem` to the "Share with..." action
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
     * Creates a sharing [Intent]. We first create the `Intent shareIntent` with the action
     * ACTION_SEND, and then add the flag [Intent.FLAG_GRANT_READ_URI_PERMISSION] to it. We
     * initialize our `val b` to a new instance of [Uri.Builder], set the scheme of `b` to
     * "content", and set its authority to our "com.example.android.apis.content.FileProvider"
     * (our content/FileProvider `ContentProvider` which provides access to resources in our
     * apk to other apps). We initialize our `val tv` to a new instance of [TypedValue], and use
     * it to hold the resource data for our raw asset png file with id R.raw.robot. We then append
     * to our [Uri.Builder] `b` the encoded path of the asset cookie of `tv` for asset R.raw.robot
     * followed by the encoded path of the string value for R.raw.robot. We then initialize our
     * `val uri` to the [Uri] that results from building `b`. We next set the mime type of `shareIntent`
     * to "image/png", add `uri` as an extra under the key [Intent.EXTRA_STREAM], and set the clip
     * data of `shareIntent` to a new instance of [ClipData] holding a [Uri] whose `ContentResolver`
     * is a `ContentResolver` instance for our application's package, whose user-visible label for
     * the clip data is "image", and whose [Uri] is our `uri`. Finally we return `shareIntent` to
     * the caller.
     *
     * @return The sharing intent.
     */
    private fun createShareIntent(): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val b: Uri.Builder = Uri.Builder()
        b.scheme("content")
        b.authority("com.example.android.apis.content.FileProvider")
        val tv = TypedValue()
        resources.getValue(R.raw.robot, tv, true)
        b.appendEncodedPath(tv.assetCookie.toString())
        b.appendEncodedPath(tv.string.toString())
        val uri: Uri = b.build()
        shareIntent.type = "image/png"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.clipData = ClipData.newUri(contentResolver, "image", uri)
        return shareIntent
    }

}
