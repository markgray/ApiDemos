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

package com.example.android.apis.preference;

import com.example.android.apis.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

/**
 * Shows how to fill a PreferenceScreen using java code.
 */
@SuppressLint("ExportedPreferenceActivity")
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class PreferencesFromCode extends PreferenceActivity {
    /**
     * Key for the {@code CheckBoxPreference parentCheckBoxPref} which is used as a dependency for
     * {@code CheckBoxPreference childCheckBoxPref}.
     */
    private static final String PARENT_CHECKBOX_PREFERENCE = "parent_checkbox_preference";

    /**
     * Called when the {@code PreferenceActivity} is starting. First we call through to our super's
     * implementation of {@code onCreate}. Next we use the {@code PreferenceManager} used by this
     * activity to create a {@code PreferenceScreen root} for "this". We set the root of the preference
     * hierarchy that this activity is showing to {@code PreferenceScreen root}, and then call our
     * method {@code populatePreferenceHierarchy} to programmatically add preferences to it.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
        setPreferenceScreen(root);
        populatePreferenceHierarchy(root);
    }

    /**
     * Called to programmatically add a bunch of different types of {@code Preference} to the parameter
     * {@code PreferenceScreen root}.
     * <p>
     * Our UI uses four {@code PreferenceCategory} objects to group {@code Preference} objects of
     * similar types:
     * <ul>
     * <li>
     * {@code PreferenceCategory inlinePrefCat} - title "In-line preferences". We create it,
     * set its title and add it to {@code root}. Then we proceed to add two {@code Preference} objects
     * to {@code inlinePrefCat}:
     * </li>
     * <li>
     * <ul>
     * <li>
     * {@code CheckBoxPreference checkboxPref} - we create it, set its key to "checkbox_preference",
     * set its title to "Switch preference", set its summary to "This is a checkbox" and then
     * add it to {@code inlinePrefCat}.
     * </li>
     * <li>
     * {@code SwitchPreference switchPref} - we create it, set its key to "switch_preference",
     * set its title to "Switch preference", set its summary to "This is a switch" and then
     * add it to {@code inlinePrefCat}.
     * </li>
     * </ul>
     * </li>
     * <li>
     * {@code PreferenceCategory dialogBasedPrefCat} - title "Dialog-based preferences". We create it,
     * set its title and add it to {@code root}. Then we proceed to add two {@code Preference} objects
     * to {@code dialogBasedPrefCat}:
     * </li>
     * <li>
     * <ul>
     * <li>
     * {@code EditTextPreference editTextPref} - we create it, set its dialog title to
     * "Enter your favorite animal" (displayed when the dialog is popped up), set its key
     * to "edittext_preference", set its title to "Edit text preference", set its summary
     * to "An example that uses an edit text dialog" and then add it to {@code dialogBasedPrefCat}.
     * </li>
     * <li>
     * {@code ListPreference listPref} - we create it, set its entries to the resource array
     * R.array.entries_list_preference, its values to the resource array R.array.entryvalues_list_preference,
     * its dialog title to "Choose one", its key to "list_preference", its title to "List preference",
     * its summary to "An example that uses a list dialog", and then add it to {@code dialogBasedPrefCat}.
     * </li>
     * </ul>
     * </li>
     * <li>
     * {@code PreferenceCategory launchPrefCat} - title "Launch preferences". We create it,
     * set its title and add it to {@code root}. Then we proceed to add two {@code Preference} objects
     * to {@code launchPrefCat}:
     * </li>
     * <li>
     * <ul>
     * <li>
     * {@code PreferenceScreen screenPref} - we create it, set its key to "screen_preference",
     * set its title to "Screen preference", set its summary to "Shows another screen of preferences",
     * and then add it to {@code launchPrefCat}. We create {@code CheckBoxPreference nextScreenCheckBoxPref},
     * with a key of "screen_preference", a title of "Toggle preference", a summary of "Preference that
     * is on the next screen but same hierarchy", and add it to {@code screenPref} (it will be shown
     * on a new page if {@code screenPref} is clicked).
     * </li>
     * <li>
     * {@code PreferenceScreen intentPref} - we create it, set its {@code Intent} to an {@code Intent}
     * with the action ACTION_VIEW, and the data "http://www.android.com", set its title to
     * "Intent preference", set its summary to "Launches an Activity from an Intent", and add it to
     * {@code launchPrefCat}
     * </li>
     * </ul>
     * </li>
     * <li>
     * {@code PreferenceCategory prefAttrsCat} - title "Preference attributes". We create it,
     * set its title and add it to {@code root}. Then we proceed to add two {@code Preference} objects
     * to {@code prefAttrsCat}:
     * </li>
     * <li>
     * <ul>
     * <li>
     * {@code CheckBoxPreference parentCheckBoxPref} - we create it, set its title to
     * "Parent checkbox preference", set its summary to "This is visually a parent", add it
     * to {@code prefAttrsCat}, and set its key to PARENT_CHECKBOX_PREFERENCE.
     * </li>
     * <li>
     * {@code CheckBoxPreference childCheckBoxPref} - we first retrieve styled attribute
     * information in this Context's theme for R.styleable.TogglePrefAttrs (consists of
     * the styleable attr name="android:preferenceLayoutChild") to {@code TypedArray a},
     * then we create {@code childCheckBoxPref}, set its title to "Child checkbox preference",
     * set its summary to "This is visually a child", set its layout resource to the value
     * of R.styleable.TogglePrefAttrs_android_preferenceLayoutChild (no idea), add it to
     * {@code prefAttrsCat}, set its dependency to PARENT_CHECKBOX_PREFERENCE, and then
     * recycle {@code TypedArray a}.
     * </li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param root {@code PreferenceScreen} we are to programmatically add preferences to.
     */
    private void populatePreferenceHierarchy(PreferenceScreen root) {
        // Inline preferences
        PreferenceCategory inlinePrefCat = new PreferenceCategory(this);
        inlinePrefCat.setTitle(R.string.inline_preferences);
        root.addPreference(inlinePrefCat);

        // Checkbox preference
        CheckBoxPreference checkboxPref = new CheckBoxPreference(this);
        checkboxPref.setKey("checkbox_preference");
        checkboxPref.setTitle(R.string.title_checkbox_preference);
        checkboxPref.setSummary(R.string.summary_checkbox_preference);
        inlinePrefCat.addPreference(checkboxPref);

        // Switch preference
        SwitchPreference switchPref = new SwitchPreference(this);
        switchPref.setKey("switch_preference");
        switchPref.setTitle(R.string.title_switch_preference);
        switchPref.setSummary(R.string.summary_switch_preference);
        inlinePrefCat.addPreference(switchPref);

        // Dialog based preferences
        PreferenceCategory dialogBasedPrefCat = new PreferenceCategory(this);
        dialogBasedPrefCat.setTitle(R.string.dialog_based_preferences);
        root.addPreference(dialogBasedPrefCat);

        // Edit text preference
        EditTextPreference editTextPref = new EditTextPreference(this);
        editTextPref.setDialogTitle(R.string.dialog_title_edittext_preference);
        editTextPref.setKey("edittext_preference");
        editTextPref.setTitle(R.string.title_edittext_preference);
        editTextPref.setSummary(R.string.summary_edittext_preference);
        dialogBasedPrefCat.addPreference(editTextPref);

        // List preference
        ListPreference listPref = new ListPreference(this);
        listPref.setEntries(R.array.entries_list_preference);
        listPref.setEntryValues(R.array.entryvalues_list_preference);
        listPref.setDialogTitle(R.string.dialog_title_list_preference);
        listPref.setKey("list_preference");
        listPref.setTitle(R.string.title_list_preference);
        listPref.setSummary(R.string.summary_list_preference);
        dialogBasedPrefCat.addPreference(listPref);

        // Launch preferences
        PreferenceCategory launchPrefCat = new PreferenceCategory(this);
        launchPrefCat.setTitle(R.string.launch_preferences);
        root.addPreference(launchPrefCat);

        /*
         * The Preferences screenPref serves as a screen break (similar to page
         * break in word processing). Like for other preference types, we assign
         * a key here so that it is able to save and restore its instance state.
         */
        // Screen preference
        PreferenceScreen screenPref = getPreferenceManager().createPreferenceScreen(this);
        screenPref.setKey("screen_preference");
        screenPref.setTitle(R.string.title_screen_preference);
        screenPref.setSummary(R.string.summary_screen_preference);
        launchPrefCat.addPreference(screenPref);

        /*
         * You can add more preferences to screenPref that will be shown on the
         * next screen.
         */

        // Example of next screen toggle preference
        CheckBoxPreference nextScreenCheckBoxPref = new CheckBoxPreference(this);
        nextScreenCheckBoxPref.setKey("next_screen_toggle_preference");
        nextScreenCheckBoxPref.setTitle(R.string.title_next_screen_toggle_preference);
        nextScreenCheckBoxPref.setSummary(R.string.summary_next_screen_toggle_preference);
        screenPref.addPreference(nextScreenCheckBoxPref);

        // Intent preference
        PreferenceScreen intentPref = getPreferenceManager().createPreferenceScreen(this);
        intentPref.setIntent(new Intent().setAction(Intent.ACTION_VIEW)
                .setData(Uri.parse("http://www.android.com")));
        intentPref.setTitle(R.string.title_intent_preference);
        intentPref.setSummary(R.string.summary_intent_preference);
        launchPrefCat.addPreference(intentPref);

        // Preference attributes
        PreferenceCategory prefAttrsCat = new PreferenceCategory(this);
        prefAttrsCat.setTitle(R.string.preference_attributes);
        root.addPreference(prefAttrsCat);

        // Visual parent toggle preference
        CheckBoxPreference parentCheckBoxPref = new CheckBoxPreference(this);
        parentCheckBoxPref.setTitle(R.string.title_parent_preference);
        parentCheckBoxPref.setSummary(R.string.summary_parent_preference);
        prefAttrsCat.addPreference(parentCheckBoxPref);
        parentCheckBoxPref.setKey(PARENT_CHECKBOX_PREFERENCE);

        // Visual child toggle preference
        // See res/values/attrs.xml for the <declare-styleable> that defines
        // TogglePrefAttrs.
        TypedArray a = obtainStyledAttributes(R.styleable.TogglePrefAttrs);
        CheckBoxPreference childCheckBoxPref = new CheckBoxPreference(this);
        childCheckBoxPref.setTitle(R.string.title_child_preference);
        childCheckBoxPref.setSummary(R.string.summary_child_preference);
        childCheckBoxPref.setLayoutResource(a.getResourceId(R.styleable.TogglePrefAttrs_android_preferenceLayoutChild, 0));
        prefAttrsCat.addPreference(childCheckBoxPref);
        childCheckBoxPref.setDependency(PARENT_CHECKBOX_PREFERENCE);
        a.recycle();
    }
}
