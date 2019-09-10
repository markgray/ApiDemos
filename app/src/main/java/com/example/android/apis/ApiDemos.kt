/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.example.android.apis

import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.SimpleAdapter
import java.text.Collator
import java.util.*

/**
 * This is the controlling activity for the entire application and is responsible for parsing the
 * information returned by the `PackageManager` and using this information to populate the `ListView`
 * it creates with entries allowing one to navigate to the various demo activities included in the
 * app.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class ApiDemos : ListActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Then we initialize our variable `Intent intent` by fetching the
     * `Intent` that launched our activity, and initialize our variable `String path` by
     * retrieving any string that was stored as an extra in `intent` under the key
     * "com.example.android.apis.Path". If `path` is null, we set it to the empty string "".
     * We set our adapter to a new instance of `SimpleAdapter` intended to display the list of
     * map of `String` to `Object` returned by our method `getData` for the current
     * value of `path` using the layout android.R.layout.simple_list_item_1 to display the
     * column "title" in the TextView with id android.R.id.text1 (each `Map<String, Object>`
     * in the list has 2 entries, the name under the key "title" and an `Intent` to launch if
     * the list entry is selected under the key "intent"). Finally we enable type filtering
     * for our `ListView`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        var path = intent.getStringExtra("com.example.android.apis.Path")

        if (path == null) {
            path = ""
        }

        listAdapter = SimpleAdapter(this, getData(path),
                android.R.layout.simple_list_item_1, arrayOf("title"),
                intArrayOf(android.R.id.text1))
        listView.isTextFilterEnabled = true
    }

    /**
     * Queries a `PackageManager` to retrieve a `List<ResolveInfo>` for all the activities
     * in our manifest which have action ACTION_MAIN, and category CATEGORY_SAMPLE_CODE. If then goes
     * through that list creating an `Map<String, Object>` if the label begins with the parameter
     * `String prefix`, and that label has no further "/" characters (in this case the remaining
     * leaf of its label is stored under the key "title" and an `Intent` to launch that activity
     * is stored under the key "intent"). For all labels which begin with `prefix` but whose
     * label still has one or more following "/" characters, a single entry for all of them is created
     * whose "title" is the next segment in their label and whose "intent" is an `Intent` created
     * by our method `browseIntent` to relaunch this `ApiDemos` activity with the extra
     * "com.example.android.apis.Path" used to store the next value of `String prefix` for its
     * `onCreate` method to use when calling us to create the data used by its adapter.
     * (Very clever! Needs careful explanation).
     *
     * First we initialize our variable `List<Map<String, Object>> myData` with an instance of
     * `ArrayList` (this is the list we will return to our caller). We initialize our variable
     * `Intent mainIntent` with an intent with the action ACTION_MAIN (Start as a main entry
     * point, does not expect to receive data) and a null uri, and add the category CATEGORY_SAMPLE_CODE
     * (To be used as a sample code example (not part of the normal user experience)). We initialize
     * our variable `PackageManager pm` with a `PackageManager` instance for finding global
     * package information, then retrieve all activities that can be performed for `mainIntent`
     * (that is all activities performing the action ACTION_MAIN and with the category CATEGORY_SAMPLE_CODE)
     * saving the results in `List<ResolveInfo> list`. If `list` is null we return `myData`
     * to the caller.
     *
     * We declare `String[] prefixPath`, and set our variable `String prefixWithSlash` to
     * our parameter `prefix`. If `prefix` is equal to "" (the empty string, as it will be
     * at the beginning of our app) we set `prefixPath` to null, otherwise we split `prefixPath`
     * on the "/" character saving the results in `String[] prefixPath`, and set `prefixWithSlash`
     * to the string formed by adding an "/" to the end of `prefix`.
     *
     * We initialize `int len` to the length of `List<ResolveInfo> list`, and create a new
     * instance of `HashMap` to initialize our  variable `Map<String, Boolean> entries`.
     * We now loop over `int i` for the `len` `ResolveInfo` objects in `list`.
     * We initialize `ResolveInfo info` with the entry at `i` in `list`, then use
     * `pm` to load the label of `info` into our variable `CharSequence labelSeq`.
     * If `labelSeq` is not null, we initialize `String label` to the string value of
     * `labelSeq`, or if it is null we use the `name` field of the `activityInfo`
     * field of `info` to initialize `label`.
     *
     * If the length of `prefixWithSlash` is 0, or `label` starts with the string
     * `prefixWithSlash` we have found an activity we want to process further. We split
     * `label` on the "/" character and save the results in `String[] labelPath`. If
     * `prefixPath` is equal to null, we initialize `String nextLabel` to the contents
     * of `labelPath[0]` (this happens the first time `onCreate` calls us), otherwise
     * we initialize it to the contents of `labelPath[ prefixPath.length ]` (the path segment in
     * the activities label which follows `prefixPath`).
     *
     * If our `prefixPath` array is one shorter than our `labelPath` array (we have found
     * a leaf "node" activity with no further segments in its path) we call our method `addItem`
     * to add a `Map<String, Object>` to `myData` with `nextLabel` stored under the
     * key "label", and an `Intent` stored under the key "intent" which we create using our method
     * `activityIntent` from the `packageName` field of the `applicationInfo` field
     * of the `activityInfo` field of `info` for the name of the package implementing the
     * desired component, and the `name` field of the `activityInfo` field of `info`
     * for the name of the class inside of the application package that will be used for the Intent.
     *
     * If there are more than one segments left in the `labelPath` array path compared to the
     * `prefixPath` array we first check to see if our map `entries` is null, skipping this
     * activity if it is not null (previous activities shared our "path" prefix). If it is null this
     * is the first of possibly several more which share this "path" prefix, so we call our method
     * `addItem` to add a `Map<String, Object>` to `myData` with `nextLabel`
     * stored under the "label" key and stored under the "intent" key will be an intent created by our
     * method `browseIntent` using the path string of `nextLabel` if `prefix` is
     * equal to the empty string "", or the string formed by concatenating `prefix` to a "/"
     * character followed by `nextLabel` if `prefix` is not equal to "" (`browseIntent`
     * creates an intent which launches this `ApiDemos` activity again only with an extra stored
     * under the key "com.example.android.apis.Path" which contains its argument `String path`).
     * Finally we store true under the `nextLabel` key in our map `entries` so we will skip
     * duplicate list entries for activities which share this sub-path, and then we loop back to process
     * the next `ResolveInfo` object in `list`.
     *
     * After processing all of `list` we sort `myData` using our `Comparator`
     * `sDisplayNameComparator` and return `myData` to our caller.
     *
     * @param prefix Prefix string to use to filter entries to those our caller is interested in
     * @return List of `Map<String, Object>` of all the activities in our manifest which have
     * action ACTION_MAIN, and category CATEGORY_SAMPLE_CODE, and whose label begins with our parameter
     * `String prefix`. The next "path segment" is stored under the key "title" and an appropriate
     * `Intent` to deal with it is stored under the key "intent".
     */
    fun getData(prefix: String): List<Map<String, Any>> {
        val myData = ArrayList<Map<String, Any>>()

        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_SAMPLE_CODE)

        val pm = packageManager
        val list = pm.queryIntentActivities(mainIntent, 0) ?: return myData

        val prefixPath: Array<String>?
        var prefixWithSlash = prefix

        if (prefix == "") {
            prefixPath = null
        } else {
            prefixPath = prefix.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            prefixWithSlash = "$prefix/"
        }

        val len = list.size

        val entries = HashMap<String, Boolean>()

        for (i in 0 until len) {
            val info = list[i]
            val labelSeq = info.loadLabel(pm)
            val label = labelSeq?.toString() ?: info.activityInfo.name

            if (prefixWithSlash.isEmpty() || label.startsWith(prefixWithSlash)) {

                val labelPath = label.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                val nextLabel = if (prefixPath == null) labelPath[0] else labelPath[prefixPath.size]

                if (prefixPath?.size ?: 0 == labelPath.size - 1) {
                    addItem(myData, nextLabel, activityIntent(
                            info.activityInfo.applicationInfo.packageName,
                            info.activityInfo.name))
                } else {
                    if (entries[nextLabel] == null) {
                        addItem(myData, nextLabel, browseIntent(if (prefix == "") nextLabel else "$prefix/$nextLabel"))
                        entries[nextLabel] = true
                    }
                }
            }
        }

        Collections.sort(myData, sDisplayNameComparator)

        return myData
    }

    /**
     * Creates and returns an `Intent` created from the `activityInfo.applicationInfo.packageName`
     * and `activityInfo.name` fields of a `ResolveInfo` object returned by the `PackageManager`
     * method `queryIntentActivities`. We initialize our variable `Intent result` with a new
     * instance, then call its `setClassName` method using our parameters `pkg` and `componentName`
     * to set its explicit application package name and class name. Finally we return `result` to
     * the caller.
     *
     * @param pkg           The name of the package implementing the desired component.
     * @param componentName The name of a class inside of the application package that will be
     * used as the component for this Intent.
     * @return an `Intent` to launch the activity specified by our two parameters
     */
    protected fun activityIntent(pkg: String, componentName: String): Intent {
        val result = Intent()
        result.setClassName(pkg, componentName)
        return result
    }

    /**
     * Creates and returns an `Intent` designed to relaunch this `ApiDemos` activity with
     * its parameter `String path` stored as an extra under the key "com.example.android.apis.Path".
     * First we initialize our variable `Intent result` with a new instance, then we set its class
     * to `ApiDemos.class` using this as the Context of the application package implementing this
     * class. We then add our parameter `path` as an extra for `result` under the key
     * "com.example.android.apis.Path" and return `result` to the caller.
     *
     * @param path path to add as an extra under the key "com.example.android.apis.Path"
     * @return an intent to relaunch this `ApiDemos` activity with a different path extra.
     */
    protected fun browseIntent(path: String): Intent {
        val result = Intent()
        result.setClass(this, ApiDemos::class.java)
        result.putExtra("com.example.android.apis.Path", path)
        return result
    }

    /**
     * Creates a new `Map<String, Object>` to hold our parameters `String name` and
     * `Intent intent` and adds it to its parameter `List<Map<String, Object>> data`.
     * First we initialize our variable `Map<String, Object> temp` with a new instance of
     * `HashMap`. Then we add to it our parameter `name` using the key "title" and our
     * parameter `intent` using the key "intent". Finally we add `temp` to our parameter
     * `List<Map<String, Object>> data`.
     *
     * @param data   List we are to add a new item to
     * @param name   String we are to store in the map item under the key "title"
     * @param intent Intent we are to store in the map item under the key "intent"
     */
    protected fun addItem(data: MutableList<Map<String, Any>>, name: String, intent: Intent) {
        val temp = HashMap<String, Any>()
        temp["title"] = name
        temp["intent"] = intent
        data.add(temp)
    }

    /**
     * This method will be called when an item in our `ListView` is selected. We initialize our
     * variable `Map<String, Object> map` by fetching the item at position `position` in
     * `ListView l`. Then we initialize our variable `Intent intent` by constructing an
     * `Intent` from the object in `map` stored under the key "intent" add the category
     * CATEGORY_SAMPLE_CODE to `intent` and use it to start the activity it was designed to
     * start.
     *
     * @param l        The ListView where the click happened
     * @param v        The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     */
    @Suppress("UNCHECKED_CAST")
    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val map = l.getItemAtPosition(position) as Map<String, Any>

        val intent = Intent(map["intent"] as Intent?)
        intent.addCategory(Intent.CATEGORY_SAMPLE_CODE)
        startActivity(intent)
    }

    companion object {

        /**
         * Custom `Comparator` to sort our `List<Map<String, Object>> myData` list using
         * the strings stored under the key "title" as the alphabetical sort key.
         */
        private val sDisplayNameComparator = object : Comparator<Map<String, Any>> {
            /**
             * `Collator` instance for the current default locale
             * that we use to compare two strings
             */
            private val collator = Collator.getInstance()

            /**
             * Compares two `Map<String, Object>` objects to determine their relative ordering.
             * We sort based on the `String` stored under the key "title" in each object, so
             * we just return the value returned by the `compare` method of `collator`
             * for the values stored under the key "title" in our two parameters.
             *
             * @param map1 an `Map<String, Object>` object
             * @param map2 a second `Map<String, Object>` object to compare with `map1`
             * @return an integer < 0 if `map1` is less than `map2`, 0 if they are
             * equal, and > 0 if `map1` is greater than `map2`.
             */
            override fun compare(map1: Map<String, Any>, map2: Map<String, Any>): Int {
                return collator.compare(map1["title"], map2["title"])
            }
        }
    }
}
