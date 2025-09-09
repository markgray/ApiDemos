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
package com.example.android.apis.view

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates the use of the [Spinner] widget. It creates two `ArrayAdapter<CharSequence>`
 * from string-array resources and uses `setDropDownViewResource` to set the view used by the
 * adapter to create drop down views, then uses `setAdapter` to set the adapter used by
 * each Spinner. `setOnItemSelectedListener` is used to set an `OnItemSelectedListener`
 * for each which involves implementing `onItemSelected` and `onNothingSelected` to
 * receive callbacks when an item in the Spinner's is selected.
 */
class Spinner1 : AppCompatActivity() {
    /**
     * Called to create and show a [Toast] constructed from our [CharSequence] parameter [msg]
     *
     * @param msg message to display in our toast
     */
    fun showToast(msg: CharSequence?) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.spinner_1. We
     * initialize our [Spinner] variable `val s1` by finding the view in our layout with the id
     * R.id.spinner1. We initialize our `ArrayAdapter<CharSequence>` variable `var adapter` by
     * constructing an instance to display the contents of the resource string-array with the id
     * R.array.colors using the layout android.R.layout.simple_spinner_item, and set the drop down
     * view resource of `adapter` to the layout file android.R.layout.simple_spinner_dropdown_item.
     * We set the adapter of `s1` to `adapter` and set its `OnItemSelectedListener` to an anonymous
     * class whose `onItemSelected` override calls our method [showToast] to display the string
     * created by concatenating the string "Spinner1: position=" followed by the string value of the
     * position of the view in the adapter that has been selected (0 to 5), followed by the string
     * " id=", followed by the string value of the row id of the item that is selected (0 to 5).
     *
     * We initialize our [Spinner] variable `val s2` by finding the view in our layout with the id
     * R.id.spinner2. We set our variable `adapter` by constructing an instance to display the
     * contents of the resource string-array with the id R.array.planets using the layout
     * android.R.layout.simple_spinner_item, and set the drop down view resource of `adapter`
     * to the layout file android.R.layout.simple_spinner_dropdown_item. We set the adapter of `s2`
     * to `adapter` and set its `OnItemSelectedListener` to an anonymous class whose `onItemSelected`
     * override calls our method [showToast] to display the string created by concatenating the
     * string "Spinner2: position=" followed by the string value of the position of the view in the
     * adapter that has been selected (0 to 8), followed by the string " id=", followed by the string
     * value of the row id of the item that is selected (0 to 8).
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.spinner_1)
        val s1 = findViewById<Spinner>(R.id.spinner1)
        var adapter = ArrayAdapter.createFromResource(
            /* context = */ this,
            /* textArrayResId = */ R.array.colors,
            /* textViewResId = */ android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        s1.adapter = adapter
        s1.onItemSelectedListener = object : OnItemSelectedListener {
            /**
             * Callback method to be invoked when an item in this view has been selected. We call
             * our method [showToast] to display the string created by concatenating the string
             * "Spinner1: position=" followed by the string value of the position of the view in
             * the adapter that has been selected (0 to 5), followed by the string " id=", followed
             * by the string value of the row id of the item that is selected (0 to 5).
             *
             * @param parent The [AdapterView] where the selection happened
             * @param view The [View] within the {AdapterView} that was clicked
             * @param position The position of the view in the adapter
             * @param id The row id of the item that is selected
             */
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                showToast(msg = "Spinner1: position=$position id=$id")
            }

            /**
             * Callback method to be invoked when the selection disappears from this view.
             * We call our method [showToast] to display the string "Spinner1: unselected".
             *
             * @param parent The [AdapterView] that now contains no selected item.
             */
            override fun onNothingSelected(parent: AdapterView<*>?) {
                showToast(msg = "Spinner1: unselected")
            }
        }
        val s2 = findViewById<Spinner>(R.id.spinner2)
        adapter = ArrayAdapter.createFromResource(
            /* context = */ this,
            /* textArrayResId = */ R.array.planets,
            /* textViewResId = */ android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        s2.adapter = adapter
        s2.onItemSelectedListener = object : OnItemSelectedListener {
            /**
             * Callback method to be invoked when an item in this view has been selected. We call
             * our method [showToast] to display the string created by concatenating the string
             * "Spinner2: position=" followed by the string value of the position of the view in
             * the adapter that has been selected (0 to 8), followed by the string " id=", followed
             * by the string value of the row id of the item that is selected (0 to 8).
             *
             * @param parent The [AdapterView] where the selection happened
             * @param view The [View] within the [AdapterView] that was clicked
             * @param position The position of the view in the adapter
             * @param id The row id of the item that is selected
             */
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                showToast(msg = "Spinner2: position=$position id=$id")
            }

            /**
             * Callback method to be invoked when the selection disappears from this view.
             * We call our method [showToast] to display the string "Spinner2: unselected".
             *
             * @param parent The [AdapterView] that now contains no selected item.
             */
            override fun onNothingSelected(parent: AdapterView<*>?) {
                showToast(msg = "Spinner2: unselected")
            }
        }
    }
}