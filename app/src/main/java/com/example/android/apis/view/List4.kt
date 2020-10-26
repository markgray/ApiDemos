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
@file:Suppress("DEPRECATION")

package com.example.android.apis.view

import android.app.ListActivity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.example.android.apis.Shakespeare.DIALOGUE
import com.example.android.apis.Shakespeare.TITLES

/**
 * A list view example where the data comes from a custom `ListAdapter` which displays from
 * an array of strings using a custom view (reusing recycled views when one is given to it.)
 * TODO: Use ListFragment or RecyclerView instead of ListActivity
 */
class List4 : ListActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Then we set our list adapter to a new instance of [SpeechListAdapter].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use our own list adapter
        listAdapter = SpeechListAdapter(this)
    }

    /**
     * A sample `ListAdapter` that presents content from arrays of speeches and text.
     */
    private inner class SpeechListAdapter
    /**
     * Our constructor, we simply save our [Context] parameter  context` in our field [mContext].
     *
     *  @param mContext [Context] to use when constructing views.
     */
    (
            /**
             * Remember our context so we can use it when constructing views.
             */
            private val mContext: Context) : BaseAdapter() {

        /**
         * The number of items in the list is determined by the number of speeches in our array.
         */
        override fun getCount(): Int {
            return TITLES.size
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
        override fun getItem(position: Int): Any {
            return position
        }

        /**
         * Get the row id associated with the specified position in the list. We use the array index
         * as a unique id, so we just return our parameter [position] to the caller.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        /**
         * Get a [View] that displays the data at the specified position in the data set. First we
         * declare [SpeechView] variable `val sv`. Then if our [View] parameter [convertView] is null
         * we create a new instance for [SpeechView] variable `sv` using the text at index [position]
         * in [TITLES] as the title, and the text at index [position] in [DIALOGUE]. If [convertView]
         * is not null, we cast it to a [SpeechView] to set `sv`, and call the `setTitle` method of
         * `sv` to set its title to the text at index [position] in [TITLES], and its method
         * `setDialogue` to set its dialog to the text at index [position] in [DIALOGUE]. In either
         * case we return `sv` to our caller.
         *
         * @param position    The position of the item within the adapter's data set whose view we want.
         * @param convertView The old [View] to reuse, if possible.
         * @param parent      The parent that this [View] will eventually be attached to
         * @return A [View] corresponding to the data at the specified position.
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val sv: SpeechView
            if (convertView == null) {
                sv = SpeechView(mContext, TITLES[position],
                        DIALOGUE[position])
            } else {
                sv = convertView as SpeechView
                sv.setTitle(TITLES[position])
                sv.setDialogue(DIALOGUE[position])
            }
            return sv
        }

    }

    /**
     * We will use a [SpeechView] to display each speech.
     * It's just a [LinearLayout] with two text fields.
     */
    private inner class SpeechView(context: Context?, title: String?, words: String?) : LinearLayout(context) {
        /**
         * [TextView] we use to display the title of our speech.
         */
        private val mTitle: TextView

        /**
         * [TextView] we use to display the dialog of our speech.
         */
        private val mDialogue: TextView

        /**
         * Convenience method to set the title of a [SpeechView], we simply set the text of our
         * [TextView] field [mTitle] to our parameter [title].
         *
         * @param title [String] to use as our title
         */
        fun setTitle(title: String?) {
            mTitle.text = title
        }

        /**
         * Convenience method to set the dialogue of a [SpeechView], we simply set the text of our
         * [TextView] field [mDialogue] to our parameter [words].
         *
         * @param words [String] to use as our dialog
         */
        fun setDialogue(words: String?) {
            mDialogue.text = words
        }

        /**
         * The init block of our constructor. First we set our orientation to VERTICAL. We create a
         * new instance for `TextView` field `mTitle`, set its text to our parameter `title`, and add
         * it to our `LinearLayout` using a `LayoutParams` instance specifying MATCH_PARENT for the
         * width, and WRAP_CONTENT for the height. We create a new instance for `TextView` field
         * `mDialogue`, set its text to our parameter `words`, and add it to our `LinearLayout` using
         * a `LayoutParams` instance specifying MATCH_PARENT for the width, and WRAP_CONTENT for the
         * height.
         */
        init {
            this.orientation = VERTICAL

            // Here we build the child views in code. They could also have
            // been specified in an XML file.
            mTitle = TextView(context)
            mTitle.text = title
            addView(
                    mTitle,
                    LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT
                    )
            )
            mDialogue = TextView(context)
            mDialogue.text = words
            addView(
                    mDialogue,
                    LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT
                    )
            )
        }
    }
}