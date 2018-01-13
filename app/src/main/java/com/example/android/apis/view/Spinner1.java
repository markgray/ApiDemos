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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import com.example.android.apis.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Demonstrates the use of the Spinner widget. It creates two {@code ArrayAdapter<CharSequence>}
 * from string-array resources and uses {@code setDropDownViewResource} to set the view used by the
 * adapter to create drop down views, then uses {@code setAdapter} to set the adapter used by
 * each Spinner. {@code setOnItemSelectedListener} is used to set an {@code OnItemSelectedListener}
 * for each which involves implementing {@code onItemSelected} and {@code onNothingSelected} to
 * receive callbacks when an item in the Spinner's is selected.
 */
public class Spinner1 extends Activity {
    /**
     * Called to create and show a {@code Toast} constructed from our parameter {@code msg}
     *
     * @param msg message to display in our toast
     */
    void showToast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.spinner_1. We
     * initialize our variable {@code Spinner s1} by finding the view in our layout with the id
     * R.id.spinner1. We initialize our variable {@code ArrayAdapter<CharSequence> adapter} by
     * constructing an instance to display the contents of the resource string-array with the id
     * R.array.colors using the layout android.R.layout.simple_spinner_item, and set the drop down
     * view resource of {@code adapter} to the layout file android.R.layout.simple_spinner_dropdown_item.
     * We set the adapter of {@code s1} to {@code adapter} and set its {@code OnItemSelectedListener}
     * to an anonymous class whose {@code onItemSelected} override calls our method {@code showToast}
     * to display the string created by concatenating the string "Spinner1: position=" followed by the
     * string value of the position of the view in the adapter that has been selected (0 to 5), followed
     * by the string " id=", followed by the string value of the row id of the item that is selected (0 to 5).
     * <p>
     * We initialize our variable {@code Spinner s2} by finding the view in our layout with the id
     * R.id.spinner2. We set our variable {@code adapter} by constructing an instance to display the
     * contents of the resource string-array with the id R.array.planets using the layout
     * android.R.layout.simple_spinner_item, and set the drop down view resource of {@code adapter}
     * to the layout file android.R.layout.simple_spinner_dropdown_item. We set the adapter of {@code s2}
     * to {@code adapter} and set its {@code OnItemSelectedListener} to an anonymous class whose
     * {@code onItemSelected} override calls our method {@code showToast} to display the string created
     * by concatenating the string "Spinner2: position=" followed by the string value of the position
     * of the view in the adapter that has been selected (0 to 8), followed by the string " id=",
     * followed by the string value of the row id of the item that is selected (0 to 8).
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spinner_1);

        Spinner s1 = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.colors, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(adapter);
        s1.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    /**
                     * Callback method to be invoked when an item in this view has been selected. We
                     * call our method {@code showToast} to display the string created by concatenating
                     * the string "Spinner1: position=" followed by the string value of the position of
                     * the view in the adapter that has been selected (0 to 5), followed by the string
                     * " id=", followed by the string value of the row id of the item that is selected (0 to 5).
                     *
                     * @param parent The AdapterView where the selection happened
                     * @param view The view within the AdapterView that was clicked
                     * @param position The position of the view in the adapter
                     * @param id The row id of the item that is selected
                     */
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        showToast("Spinner1: position=" + position + " id=" + id);
                    }

                    /**
                     * Callback method to be invoked when the selection disappears from this view.
                     * We call our method {@code showToast} to display the string "Spinner1: unselected".
                     *
                     * @param parent The AdapterView that now contains no selected item.
                     */
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        showToast("Spinner1: unselected");
                    }
                });

        Spinner s2 = (Spinner) findViewById(R.id.spinner2);
        adapter = ArrayAdapter.createFromResource(this, R.array.planets,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s2.setAdapter(adapter);
        s2.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    /**
                     * Callback method to be invoked when an item in this view has been selected. We
                     * call our method {@code showToast} to display the string created by concatenating
                     * the string "Spinner2: position=" followed by the string value of the position of
                     * the view in the adapter that has been selected (0 to 8), followed by the string
                     * " id=", followed by the string value of the row id of the item that is selected (0 to 8).
                     *
                     * @param parent The AdapterView where the selection happened
                     * @param view The view within the AdapterView that was clicked
                     * @param position The position of the view in the adapter
                     * @param id The row id of the item that is selected
                     */
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        showToast("Spinner2: position=" + position + " id=" + id);
                    }

                    /**
                     * Callback method to be invoked when the selection disappears from this view.
                     * We call our method {@code showToast} to display the string "Spinner2: unselected".
                     *
                     * @param parent The AdapterView that now contains no selected item.
                     */
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        showToast("Spinner2: unselected");
                    }
                });
    }
}
