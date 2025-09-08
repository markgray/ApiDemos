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
package com.example.android.apis.view

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This demonstrates the use of the [PopupMenu] class. Clicking the button will inflate and
 * show a popup menu from an XML resource.
 */
class PopupMenu1 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.popup_menu_1.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.popup_menu_1)
    }

    /**
     * Inflates from xml and pops up a [PopupMenu]. Set as the `OnClickListener` for the
     * button in our layout file using the attribute android:onClick="onPopupButtonClick". First we
     * initialize our [PopupMenu] variable `val popup` with a new instance using our [View] parameter
     * [button] as the anchor. Then we get a `MenuInflater` for `popup` and use it to inflate the
     * menu layout file R.menu.popup into the menu associated with `popup`. Next we set the
     * `OnMenuItemClickListener` of `popup` to an a lambda which toasts a string displaying the
     * title of the `MenuItem` that was clicked. Finally we call the `show` method of `popup` to
     * show the menu popup anchored to the view specified during construction.
     *
     * @param button [View] that has been clicked.
     */
    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    fun onPopupButtonClick(button: View?) {
        val popup = PopupMenu(/* context = */ this, /* anchor = */ button)
        popup.menuInflater.inflate(R.menu.popup, popup.menu)
        popup.setOnMenuItemClickListener { item ->

            /**
             * This method will be invoked when a menu item is clicked if the item itself did not
             * already handle the event. We toast a message displaying the string "Clicked popup menu
             * item " concatenated to the title of our parameter `MenuItem item`.
             *
             * @param item `MenuItem` that was clicked
             * @return true if the event was handled, false otherwise.
             */
            Toast.makeText(
                /* context = */ this@PopupMenu1,
                /* text = */ "Clicked popup menu item " + item.title,
                /* duration = */ Toast.LENGTH_SHORT
            ).show()
            true
        }
        popup.show()
    }
}