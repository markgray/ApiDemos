/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.android.apis.R;

/**
 * This demonstrates the use of the PopupMenu class. Clicking the button will inflate and
 * show a popup menu from an XML resource.
 */
public class PopupMenu1 extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.popup_menu_1.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_menu_1);
    }

    /**
     * Inflates from xml and pops up a {@code PopupMenu}. Set as the {@code OnClickListener} for the
     * button in our layout file using the attribute android:onClick="onPopupButtonClick". First we
     * initialize our variable {@code PopupMenu popup} with a new instance using our parameter
     * {@code View button} as the anchor. Then we get a {@code MenuInflater} for {@code popup} and
     * use it to inflate the menu layout file R.menu.popup into the menu associated with {@code popup}.
     * Next we set the {@code OnMenuItemClickListener} of {@code popup} to an anonymous class which
     * toasts a string displaying the title of the {@code MenuItem} that was clicked. Finally we call
     * the {@code show} method of {@code popup} to show the menu popup anchored to the view specified
     * during construction.
     *
     * @param button View that has been clicked.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onPopupButtonClick(View button) {
        PopupMenu popup = new PopupMenu(this, button);
        popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            /**
             * This method will be invoked when a menu item is clicked if the item itself did not
             * already handle the event. We toast a message displaying the string "Clicked popup menu
             * item " concatenated to the title of our parameter {@code MenuItem item}.
             *
             * @param item {@code MenuItem} that was clicked
             * @return true if the event was handled, false otherwise.
             */
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(PopupMenu1.this, "Clicked popup menu item " + item.getTitle(),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        popup.show();
    }
}
