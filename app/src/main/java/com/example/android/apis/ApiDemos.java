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

package com.example.android.apis;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiDemos extends ListActivity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Then we initialize our variable {@code Intent intent} by fetching the
     * {@code Intent} that launched our activity, and initialize our variable {@code String path} by
     * retrieving any string that was stored as an extra in {@code intent} under the key
     * "com.example.android.apis.Path". If {@code path} is null, we set it to the empty string "".
     * We set our adapter to a new instance of {@code SimpleAdapter} intended to display the list of
     * map of {@code String} to {@code Object} returned by our method {@code getData} for the current
     * value of {@code path} using the layout android.R.layout.simple_list_item_1 to display the
     * column "title" in the TextView with id android.R.id.text1 (each {@code Map<String, Object>}
     * in the list has 2 entries, the name under the key "title" and an {@code Intent} to launch if
     * the list entry is selected under the key "intent"). Finally we enable type filtering
     * for our {@code ListView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String path = intent.getStringExtra("com.example.android.apis.Path");

        if (path == null) {
            path = "";
        }

        setListAdapter(new SimpleAdapter(this, getData(path),
                android.R.layout.simple_list_item_1, new String[]{"title"},
                new int[]{android.R.id.text1}));
        getListView().setTextFilterEnabled(true);
    }

    /**
     * Queries a {@code PackageManager} to retrieve a {@code List<ResolveInfo>} for all the activities
     * in our manifest which have action ACTION_MAIN, and category CATEGORY_SAMPLE_CODE. If then goes
     * through that list creating an {@code Map<String, Object>} if the label begins with the parameter
     * {@code String prefix}, and that label has no further "/" characters (in this case the remaining
     * leaf of its label is stored under the key "title" and an {@code Intent} to launch that activity
     * is stored under the key "intent"). For all labels which begin with {@code prefix} but whose
     * label still has one or more following "/" characters, a single entry for all of them is created
     * whose "title" is the next segment in their label and whose "intent" is an {@code Intent} created
     * by our method {@code browseIntent} to relaunch this {@code ApiDemos} activity with the extra
     * "com.example.android.apis.Path" used to store the next value of {@code String prefix} for its
     * {@code onCreate} method to use when calling us to create the data used by its adapter.
     * (Very clever! Needs careful explanation).
     * <p>
     * First we initialize our variable {@code List<Map<String, Object>> myData} with an instance of
     * {@code ArrayList} (this is the list we will return to our caller). We initialize our variable
     * {@code Intent mainIntent} with an intent with the action ACTION_MAIN (Start as a main entry
     * point, does not expect to receive data) and a null uri, and add the category CATEGORY_SAMPLE_CODE
     * (To be used as a sample code example (not part of the normal user experience)). We initialize
     * our variable {@code PackageManager pm} with a {@code PackageManager} instance for finding global
     * package information, then retrieve all activities that can be performed for {@code mainIntent}
     * (that is all activities performing the action ACTION_MAIN and with the category CATEGORY_SAMPLE_CODE)
     * saving the results in {@code List<ResolveInfo> list}. If {@code list} is null we return {@code myData}
     * to the caller.
     * <p>
     * We declare {@code String[] prefixPath}, and set our variable {@code String prefixWithSlash} to
     * our parameter {@code prefix}. If {@code prefix} is equal to "" (the empty string, as it will be
     * at the beginning of our app) we set {@code prefixPath} to null, otherwise we split {@code prefixPath}
     * on the "/" character saving the results in {@code String[] prefixPath}, and set {@code prefixWithSlash}
     * to the string formed by adding an "/" to the end of {@code prefix}.
     * <p>
     * We initialize {@code int len} to the length of {@code List<ResolveInfo> list}, and create a new
     * instance of {@code HashMap} to initialize our  variable {@code Map<String, Boolean> entries}.
     * We now loop over {@code int i} for the {@code len} {@code ResolveInfo} objects in {@code list}.
     * We initialize {@code ResolveInfo info} with the entry at {@code i} in {@code list}, then use
     * {@code pm} to load the label of {@code info} into our variable {@code CharSequence labelSeq}.
     * If {@code labelSeq} is not null, we initialize {@code String label} to the string value of
     * {@code labelSeq}, or if it is null we use the {@code name} field of the {@code activityInfo}
     * field of {@code info} to initialize {@code label}.
     * <p>
     * If the length of {@code prefixWithSlash} is 0, or {@code label} starts with the string
     * {@code prefixWithSlash} we have found an activity we want to process further. We split
     * {@code label} on the "/" character and save the results in {@code String[] labelPath}. If
     * {@code prefixPath} is equal to null, we initialize {@code String nextLabel} to the contents
     * of {@code labelPath[0]} (this happens the first time {@code onCreate} calls us), otherwise
     * we initialize it to the contents of {@code labelPath[prefixPath.length]} (the path segment in
     * the activities label which follows {@code prefixPath}).
     * <p>
     * If our {@code prefixPath} array is one shorter than our {@code labelPath} array (we have found
     * a leaf "node" activity with no further segments in its path) we call our method {@code addItem}
     * to add a {@code Map<String, Object>} to {@code myData} with {@code nextLabel} stored under the
     * key "label", and an {@code Intent} stored under the key "intent" which we create using our method
     * {@code activityIntent} from the {@code packageName} field of the {@code applicationInfo} field
     * of the {@code activityInfo} field of {@code info} for the name of the package implementing the
     * desired component, and the {@code name} field of the {@code activityInfo} field of {@code info}
     * for the name of the class inside of the application package that will be used for the Intent.
     * <p>
     * If there are more than one segments left in the {@code labelPath} array path compared to the
     * {@code prefixPath} array we first check to see if our map {@code entries} is null, skipping this
     * activity if it is not null (previous activities shared our "path" prefix). If it is null this
     * is the first of possibly several more which share this "path" prefix, so we call our method
     * {@code addItem} to add a {@code Map<String, Object>} to {@code myData} with {@code nextLabel}
     * stored under the "label" key and stored under the "intent" key will be an intent created by our
     * method {@code browseIntent} using the path string of {@code nextLabel} is {@code prefix} is
     * equal to the empty string "", or the string formed by concatenating {@code prefix} to a "/"
     * character followed by {@code nextLabel} if {@code prefix} is not equal to "" ({@code browseIntent}
     * creates an intent which launches this {@code ApiDemos} activity again only with an extra stored
     * under the key "com.example.android.apis.Path" which contains its argument {@code String path}).
     * Finally we store true under the {@code nextLabel} key in our map {@code entries} so we will skip
     * duplicate list entries for activities which share this sub-path, and then we loop back to process
     * the next {@code ResolveInfo} object in {@code list}.
     * <p>
     * After processing all of {@code list} we sort {@code myData} using our {@code Comparator}
     * {@code sDisplayNameComparator} and return {@code myData} to our caller.
     *
     * @param prefix Prefix string to use to filter entries to those our caller is interested in
     * @return List of {@code Map<String, Object>} of all the activities in our manifest which have
     * action ACTION_MAIN, and category CATEGORY_SAMPLE_CODE, and whose label begins with our parameter
     * {@code String prefix}. The next "path segment" is stored under the key "title" and an appropriate
     * {@code Intent} to deal with it is stored under the key "intent".
     */
    protected List<Map<String, Object>> getData(String prefix) {
        List<Map<String, Object>> myData = new ArrayList<>();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_SAMPLE_CODE);

        PackageManager pm = getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(mainIntent, 0);

        if (null == list)
            return myData;

        String[] prefixPath;
        String prefixWithSlash = prefix;

        if (prefix.equals("")) {
            prefixPath = null;
        } else {
            prefixPath = prefix.split("/");
            prefixWithSlash = prefix + "/";
        }

        int len = list.size();

        Map<String, Boolean> entries = new HashMap<>();

        for (int i = 0; i < len; i++) {
            ResolveInfo info = list.get(i);
            CharSequence labelSeq = info.loadLabel(pm);
            String label = labelSeq != null
                    ? labelSeq.toString()
                    : info.activityInfo.name;

            if (prefixWithSlash.length() == 0 || label.startsWith(prefixWithSlash)) {

                String[] labelPath = label.split("/");

                String nextLabel = prefixPath == null ? labelPath[0] : labelPath[prefixPath.length];

                if ((prefixPath != null ? prefixPath.length : 0) == labelPath.length - 1) {
                    addItem(myData, nextLabel, activityIntent(
                            info.activityInfo.applicationInfo.packageName,
                            info.activityInfo.name));
                } else {
                    if (entries.get(nextLabel) == null) {
                        addItem(myData, nextLabel, browseIntent(prefix.equals("") ? nextLabel : prefix + "/" + nextLabel));
                        entries.put(nextLabel, true);
                    }
                }
            }
        }

        Collections.sort(myData, sDisplayNameComparator);

        return myData;
    }

    /**
     * Custom {@code Comparator} to sort our {@code List<Map<String, Object>> myData} list using
     * the strings stored under the key "title" as the alphabetical sort key.
     */
    private final static Comparator<Map<String, Object>> sDisplayNameComparator =
            new Comparator<Map<String, Object>>() {
                private final Collator collator = Collator.getInstance();

                @Override
                public int compare(Map<String, Object> map1, Map<String, Object> map2) {
                    return collator.compare(map1.get("title"), map2.get("title"));
                }
            };

    protected Intent activityIntent(String pkg, String componentName) {
        Intent result = new Intent();
        result.setClassName(pkg, componentName);
        return result;
    }

    protected Intent browseIntent(String path) {
        Intent result = new Intent();
        result.setClass(this, ApiDemos.class);
        result.putExtra("com.example.android.apis.Path", path);
        return result;
    }

    protected void addItem(List<Map<String, Object>> data, String name, Intent intent) {
        Map<String, Object> temp = new HashMap<>();
        temp.put("title", name);
        temp.put("intent", intent);
        data.add(temp);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Map<String, Object> map = (Map<String, Object>) l.getItemAtPosition(position);

        Intent intent = new Intent((Intent) map.get("intent"));
        intent.addCategory(Intent.CATEGORY_SAMPLE_CODE);
        startActivity(intent);
    }
}
