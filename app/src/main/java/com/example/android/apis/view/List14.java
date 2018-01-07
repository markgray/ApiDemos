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

package com.example.android.apis.view;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.apis.R;

/**
 * Demonstrates how to write an efficient list adapter. The adapter used in this example binds
 * to an ImageView and to a TextView for each row in the list.
 * <p>
 * To work efficiently the adapter implemented here uses two techniques:
 * <ul>
 * <li>
 * It reuses the convertView passed to getView() to avoid inflating View when it is not necessary
 * </li>
 * <li>
 * It uses the ViewHolder pattern to avoid calling findViewById() when it is not necessary
 * </li>
 * </ul>
 * The ViewHolder pattern consists in storing a data structure in the tag of the view returned by
 * getView(). This data structures contains references to the views we want to bind data to, thus
 * avoiding calls to findViewById() every time getView() is invoked.
 */
public class List14 extends ListActivity {
    /**
     * A reference to the array of cheeses that we use for our database.
     */
    private static final String[] DATA = Cheeses.sCheeseStrings;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Then we set our list adapter to a new instance of {@code EfficientAdapter}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new EfficientAdapter(this));
    }

    /**
     * Our efficient list adapter.
     */
    @SuppressWarnings("WeakerAccess")
    private static class EfficientAdapter extends BaseAdapter {
        /**
         * A {@code LayoutInflater} obtained from the {@code Context} passed to our constructor.
         */
        private LayoutInflater mInflater;
        /**
         * {@code Bitmap} decoded from the resource png R.drawable.icon48x48_1
         */
        private Bitmap mIcon1;
        /**
         * {@code Bitmap} decoded from the resource png R.drawable.icon48x48_2 (one in drawable-mdpi
         * and one in drawable-hdpi)
         */
        private Bitmap mIcon2;

        /**
         * Our constructor. First we initialize our field {@code LayoutInflater mInflater} by obtaining
         * the {@code LayoutInflater} from the {@code Context context} passed us (this from the
         * {@code onCreate} override of {@code List14}). Then we initialize {@code Bitmap mIcon1} by
         * decoding the png with resource ID R.drawable.icon48x48_1, and {@code Bitmap mIcon2} by
         * decoding the png with resource ID R.drawable.icon48x48_2.
         *
         * @param context {@code Context} to use to access resources (and the {@code LayoutInflater})
         */
        public EfficientAdapter(Context context) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);

            // Icons bound to the rows.
            mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon48x48_1);
            mIcon2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon48x48_2);
        }

        /**
         * How many items are in the data set represented by this Adapter. The number of items in
         * the list is determined by the number of cheeses in our array, so we return the length of
         * our array {@code DATA}.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            return DATA.length;
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
        @Override
        public Object getItem(int position) {
            return position;
        }

        /**
         * Get the row id associated with the specified position in the list. We use the array index
         * as a unique id.
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
         * declare {@code ViewHolder holder}. If {@code convertView} is null we set it by using
         * {@code mInflater} to inflate the layout R.layout.list_item_icon_text using {@code parent}
         * to supply layout parameters. Then we create a new instance for {@code ViewHolder holder},
         * set its {@code text} field by finding the view in {@code convertView} with ID R.id.text,
         * and set its {@code icon} field by finding the view in {@code convertView} with ID R.id.icon.
         * We then set the tag of {@code convertView} to {@code holder}. If {@code convertView} is not
         * null we just set {@code holder} by fetching the tag from {@code convertView}.
         * <p>
         * We now set the text of {@code holder.text} to the string at {@code DATA[position]}, and set
         * the content of {@code holder.icon} to {@code mIcon1} if {@code position} is odd, and to
         * {@code mIcon2} if it is even. Finally we return {@code convertView} to the caller.
         *
         * @param position    The position of the item within the adapter's data set whose view we want.
         * @param convertView The old view to reuse, if possible.
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unnecessary calls
            // to findViewById() on each row.
            ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to re-inflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_icon_text, parent, false);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            holder.text.setText(DATA[position]);
            holder.icon.setImageBitmap((position & 1) == 1 ? mIcon1 : mIcon2);

            return convertView;
        }

        /**
         * Data structure we use to hold references to the views in our row item view group, it is
         * stored in the tag of the view group so that recycled views can be reused without having
         * to find these views again.
         */
        static class ViewHolder {
            TextView text;
            ImageView icon;
        }
    }
}
