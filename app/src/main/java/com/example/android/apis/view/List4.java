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

package com.example.android.apis.view;

import com.example.android.apis.Shakespeare;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A list view example where the data comes from a custom ListAdapter which displays from
 * an array of strings using a custom view (reusing recycled views when one is given to it.)
 */
public class List4 extends ListActivity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Then we set our list adapter to a new instance of {@code SpeechListAdapter}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use our own list adapter
        setListAdapter(new SpeechListAdapter(this));
    }


    /**
     * A sample ListAdapter that presents content from arrays of speeches and text.
     */
    @SuppressWarnings("WeakerAccess")
    private class SpeechListAdapter extends BaseAdapter {
        /**
         * Remember our context so we can use it when constructing views.
         */
        private Context mContext;

        /**
         * Our constructor, we simply save our parameter {@code Context context} in our field
         * {@code Context mContext}.
         *
         * @param context {@code Context} to use when constructing views.
         */
        public SpeechListAdapter(Context context) {
            mContext = context;
        }

        /**
         * The number of items in the list is determined by the number of speeches in our array.
         */
        @Override
        public int getCount() {
            return Shakespeare.TITLES.length;
        }

        /**
         * Get the data item associated with the specified position in the data set. Since the data
         * comes from an array, just returning the index is sufficient to get at the data. If we
         * were using a more complex data structure, we would return whatever object represents one
         * row in the list.
         *
         * @param position Position of the item whose data we want within the adapter's data set.
         * @return The data at the specified position.
         */
        @Override
        public Object getItem(int position) {
            return position;
        }

        /**
         * Get the row id associated with the specified position in the list. We use the array index
         * as a unique id, so we just return our parameter {@code position} to the caller.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * Get a View that displays the data at the specified position in the data set. First we
         * declare {@code SpeechView sv}. Then if our parameter {@code convertView} is null we create
         * a new instance for {@code SpeechView sv} using the text {@code Shakespeare.TITLES[position]}
         * as the title, and {@code Shakespeare.DIALOGUE[position]}. If {@code convertView} is not
         * null, we cast it to a {@code SpeechView} to set {@code sv}, and call the {@code setTitle}
         * method of {@code sv} to set its title to {@code Shakespeare.TITLES[position]}, and its
         * method {@code setDialogue} to set its dialog to {@code Shakespeare.DIALOGUE[position]}.
         * In either case we return {@code sv} to our caller.
         *
         * @param position    The position of the item within the adapter's data set whose view we want.
         * @param convertView The old view to reuse, if possible.
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SpeechView sv;
            if (convertView == null) {
                sv = new SpeechView(mContext, Shakespeare.TITLES[position],
                        Shakespeare.DIALOGUE[position]);
            } else {
                sv = (SpeechView) convertView;
                sv.setTitle(Shakespeare.TITLES[position]);
                sv.setDialogue(Shakespeare.DIALOGUE[position]);
            }

            return sv;
        }
    }

    /**
     * We will use a SpeechView to display each speech. It's just a LinearLayout
     * with two text fields.
     */
    private class SpeechView extends LinearLayout {
        /**
         * {@code TextView} we use to display the title of our speech.
         */
        private TextView mTitle;
        /**
         * {@code TextView} we use to display the dialog of our speech.
         */
        private TextView mDialogue;

        /**
         * Our constructor. First we call our super's constructor, then we set our orientation to
         * VERTICAL. We create a new instance for {@code TextView mTitle}, set its text to our
         * parameter {@code title}, and add it to our {@code LinearLayout} using a {@code LayoutParams}
         * instance specifying MATCH_PARENT for the width, and WRAP_CONTENT for the height. We create
         * a new instance for {@code TextView mDialogue}, set its text to our parameter {@code words},
         * and add it to our {@code LinearLayout} using a {@code LayoutParams} instance specifying
         * MATCH_PARENT for the width, and WRAP_CONTENT for the height.
         *
         * @param context {@code Context} to use to construct Views
         * @param title   Text to use as our title
         * @param words   Text to use as our dialog
         */
        public SpeechView(Context context, String title, String words) {
            super(context);

            this.setOrientation(VERTICAL);

            // Here we build the child views in code. They could also have
            // been specified in an XML file.

            mTitle = new TextView(context);
            mTitle.setText(title);
            addView(mTitle, new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            mDialogue = new TextView(context);
            mDialogue.setText(words);
            addView(mDialogue, new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }

        /**
         * Convenience method to set the title of a SpeechView, we simply set the text of our field
         * {@code TextView mTitle} to our parameter {@code title}.
         *
         * @param title String to use as our title
         */
        public void setTitle(String title) {
            mTitle.setText(title);
        }

        /**
         * Convenience method to set the dialogue of a SpeechView, we simply set the text of our field
         * {@code TextView mDialogue} to our parameter {@code words}.
         *
         * @param words String to use as our dialog
         */
        public void setDialogue(String words) {
            mDialogue.setText(words);
        }
    }
}
