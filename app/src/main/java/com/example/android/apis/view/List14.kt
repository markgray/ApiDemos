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

package com.example.android.apis.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import com.example.android.apis.view.List14.EfficientAdapter

/**
 * Demonstrates how to write an efficient list adapter. The adapter used in this example binds
 * to an [ImageView] and to a [TextView] for each row in the list. To work efficiently the adapter
 * implemented here uses two techniques:
 *
 *  * It reuses the convertView passed to [EfficientAdapter.getView] to avoid inflating View when
 *  it is not necessary
 *
 *  * It uses the `ViewHolder` pattern to avoid calling [findViewById] when it is not necessary
 *
 * The ViewHolder pattern consists in storing a data structure in the tag of the view returned by
 * `getView()`. This data structures contains references to the views we want to bind data to, thus
 * avoiding calls to [findViewById] every time `getView()` is invoked.
 */
class List14 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file `R.layout.list_14` and initialize
     * our [ListView] variable `val list` by finding the view with ID `R.id.list`. Finally we set the
     * list adapter of `list` to a new instance of [EfficientAdapter].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_14)
        val list: ListView = findViewById(R.id.list)
        list.adapter = EfficientAdapter(this)
    }

    /**
     * Our efficient list adapter.
     */
    private class EfficientAdapter(context: Context) : BaseAdapter() {
        /**
         * A [LayoutInflater] obtained from the [Context] passed to our constructor.
         */
        private val mInflater: LayoutInflater = LayoutInflater.from(context)

        /**
         * [Bitmap] decoded from the resource png R.drawable.icon48x48_1
         */
        private val mIcon1: Bitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.icon48x48_1
        )

        /**
         * [Bitmap] decoded from the resource png R.drawable.icon48x48_2 (one in drawable-mdpi
         * and one in drawable-hdpi)
         */
        private val mIcon2: Bitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.icon48x48_2
        )

        /**
         * How many items are in the data set represented by this Adapter. The number of items in
         * the list is determined by the number of cheeses in our array, so we return the length of
         * our array [DATA].
         *
         * @return Count of items.
         */
        override fun getCount(): Int {
            return DATA.size
        }

        /**
         * Get the data item associated with the specified position in the data set. Since the data
         * comes from an array, just returning the index is sufficient to get at the data. If we were
         * using a more complex data structure, we would return whatever object represents one row in
         * the list.
         *
         * @param position Position of the item within the adapter's data set whose data we want.
         * @return The data at the specified position.
         */
        override fun getItem(position: Int): Any {
            return position
        }

        /**
         * Get the row id associated with the specified position in the list. We use the array index
         * as a unique id.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        /**
         * Get a View that displays the data at the specified position in the data set. First we
         * declare [ViewHolder] variable `val holder`. If `convertViewVar` is null we initialize it
         * by using [mInflater] to inflate the layout R.layout.list_item_icon_text using [parent]
         * to supply layout parameters. Then we create a new instance of [ViewHolder] for `holder`,
         * set its `text` field by finding the view in `convertViewVar` with ID R.id.text, and set
         * its `icon` field by finding the view in `convertViewVar` with ID R.id.icon. We then set
         * the tag of `convertViewVar` to `holder`. If on the other hand `convertViewVar` is not
         * null we just set `holder` by fetching the tag from [convertView].
         *
         * We now set the text of `holder.text` to the string at index [position] in [DATA], and set
         * the content of `holder.icon` to [mIcon1] if [position] is odd, and to [mIcon2] if it is
         * even. Finally we return `convertViewVar` to the caller.
         *
         * @param position    The position of the item within the adapter's data set whose view we want.
         * @param convertView The old view to reuse, if possible.
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            // A ViewHolder keeps references to children views to avoid unnecessary calls
            // to findViewById() on each row.
            var convertViewVar = convertView
            val holder: ViewHolder

            // When convertView is not null, we can reuse it directly, there is no need
            // to re-inflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertViewVar == null) {
                convertViewVar = mInflater.inflate(
                    R.layout.list_item_icon_text,
                    parent,
                    false
                )
                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = ViewHolder()
                holder.text = convertViewVar.findViewById(R.id.text)
                holder.icon = convertViewVar.findViewById(R.id.icon)
                convertViewVar.tag = holder
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = convertViewVar.tag as ViewHolder
            }

            // Bind the data efficiently with the holder.
            holder.text!!.text = DATA[position]
            holder.icon!!.setImageBitmap(if (position and 1 == 1) mIcon1 else mIcon2)
            return convertViewVar!!
        }

        /**
         * Data structure we use to hold references to the views in our row item view group, it is
         * stored in the tag of the view group so that recycled views can be reused without having
         * to find these views again.
         */
        class ViewHolder {
            var text: TextView? = null
            var icon: ImageView? = null
        }

    }

    companion object {
        /**
         * A reference to the array of cheeses that we use for our database.
         */
        private val DATA = Cheeses.sCheeseStrings
    }
}
