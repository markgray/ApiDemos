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

package com.example.android.apis.app;

import com.example.android.apis.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;

/**
 * Demonstrates how fragments can participate in the options menu. Builds menus from two fragments,
 * allowing you to hide them to remove them.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentMenu extends Activity {
    Fragment mFragment1; // MenuFragment instance for our app
    Fragment mFragment2; // Menu2Fragment instance for our app
    CheckBox mCheckBox1; // CheckBox in layout used to show/hide mFragment1
    CheckBox mCheckBox2; // CheckBox in layout used to show/hide mFragment2

    // Update fragment visibility when check boxes are changed.
    final OnClickListener mClickListener = new OnClickListener() {
        /**
         * This is used for both of the CheckBox's in our layout, We simply call our method
         * <b>updateFragmentVisibility()</b> to update our menu given the new state of the two
         * CheckBox's.
         *
         * @param v View of CheckBox which was clicked
         */
        @Override
        public void onClick(View v) {
            updateFragmentVisibility();
        }
    };

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.fragment_menu.
     * <p>
     * Next we get a handle to the FragmentManager for interacting with fragments associated with
     * this activity <b>FragmentManager fm</b>. We then use <b>fm</b> to start a new series of
     * FragmentTransaction's <b>FragmentTransaction ft</b>. We use <b>fm</b> to search for any
     * Fragment's with the tag we use for our <b>MenuFragment</b> instance: "f1", be they currently
     * currently attached to the Activity or on the back stack, in order to initialize our field
     * <b>Fragment mFragment1</b>. If none is found we create a new instance of <b>MenuFragment</b>
     * for <b>mFragment1</b> and use <b>ft</b> to add it to the Activity state with the tag "f1".
     * <p>
     * Then we use <b>fm</b> to search for any Fragment's with the tag we use for our <b>Menu2Fragment</b>
     * instance: "f2", be they currently currently attached to the Activity or on the back stack, in
     * order to initialize our field <b>Fragment mFragment2</b>. If none is found we create a new
     * instance of <b>Menu2Fragment</b> for <b>mFragment2</b> and use <b>ft</b> to add it to the
     * Activity state with the tag "f2". We then schedule a commit for <b>FragmentTransaction ft</b>.
     * <p>
     * We initialize our field <b>CheckBox mCheckBox1</b> by searching for the CheckBox in our layout
     * with id R.id.menu1, and set its OnClickListener to <b>OnClickListener mClickListener</b>. We
     * initialize our field <b>CheckBox mCheckBox2</b> by searching for the CheckBox in our layout
     * with id R.id.menu2, and set its OnClickListener to <b>OnClickListener mClickListener</b>.
     * <b>mClickListener</b> updates the visibility of the two menu Fragment's whenever either
     * CheckBox changes state by calling our method <b>updateFragmentVisibility()</b>.
     * <p>
     * Finally we call our method <b>updateFragmentVisibility()</b> ourselves in order to make sure
     * that our fragments start out with correct visibility.
     *
     * @param savedInstanceState we do not override onSaveInstanceState so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_menu);

        // Make sure the two menu fragments are created.
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        mFragment2 = fm.findFragmentByTag("f2");
        if (mFragment2 == null) {
            mFragment2 = new Menu2Fragment();
            ft.add(mFragment2, "f2");
        }
        ft.commit();

        // Watch check box clicks.
        mCheckBox1 = (CheckBox) findViewById(R.id.menu1);
        mCheckBox1.setOnClickListener(mClickListener);
        mCheckBox2 = (CheckBox) findViewById(R.id.menu2);
        mCheckBox2.setOnClickListener(mClickListener);

        // Make sure fragments start out with correct visibility.
        updateFragmentVisibility();
    }

    /**
     * This method is called after {@link #onStart} when the activity is
     * being re-initialized from a previously saved state, given here in
     * <var>savedInstanceState</var>.  Most implementations will simply use {@link #onCreate}
     * to restore their state, but it is sometimes convenient to do it here
     * after all of the initialization has been done or to allow subclasses to
     * decide whether to use your default implementation.  The default
     * implementation of this method performs a restore of any view state that
     * had previously been frozen by {@link #onSaveInstanceState}.
     * <p>
     * This method is called between {@link #onStart} and {@link #onPostCreate}.
     * <p>
     * First we call through to our super's implementation of onRestoreInstanceState, and then we
     * call our method <b>updateFragmentVisibility()</b> to make sure our menu fragments are updated
     * after CheckBox View state is restored by our super. Note: this causes updateFragmentVisibility
     * to be called twice after a device rotation, once in onCreate and once here but that is necessary
     * because the state of the CheckBox's is not restored yet when onCreate calls updateFragmentVisibility.
     *
     * @param savedInstanceState we do not override onSaveInstanceState so do not use.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Make sure fragments are updated after check box view state is restored.
        updateFragmentVisibility();
    }

    /**
     * Update fragment visibility based on current check box state. First we use  the FragmentManager
     * for interacting with fragments associated with this activity to begin a new series of fragment
     * transactions: <b>FragmentTransaction ft</b>. If <b>CheckBox mCheckBox1</b> is checked we use
     * <b>ft</b> to show <b>MenuFragment mFragment1</b>, if it is not checked we hide the Fragment.
     * If <b>CheckBox mCheckBox2</b> is checked we use <b>ft</b> to show <b>Menu2Fragment mFragment2</b>,
     * if it is not checked we hide the Fragment. Finally we schedule <b>FragmentTransaction ft</b>
     * to be committed.
     */
    void updateFragmentVisibility() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (mCheckBox1.isChecked()) {
            ft.show(mFragment1);
        } else {
            ft.hide(mFragment1);
        }
        if (mCheckBox2.isChecked()) {
            ft.show(mFragment2);
        } else {
            ft.hide(mFragment2);
        }
        ft.commit();
    }

    /**
     * A fragment that displays a menu.  This fragment happens to not have a UI (it does not implement
     * onCreateView), but it could also have one if it wanted. Its entire effect when added to the
     * Activity is caused by the overriding of <b>onCreateOptionsMenu</b>
     */
    public static class MenuFragment extends Fragment {

        /**
         * Called to do initial creation of a fragment.  This is called after
         * {@link #onAttach(Activity)} and before
         * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
         * <p>
         *  Note that this can be called while the fragment's activity is
         * still in the process of being created.  As such, you can not rely
         * on things like the activity's content view hierarchy being initialized
         * at this point.  If you want to do work once the activity itself is
         * created, see {@link #onActivityCreated(Bundle)}.
         * <p>
         * First we call through to our super's implementation of onCreate, and then we report that
         * this fragment would like to participate in populating the options menu by receiving a
         * call to onCreateOptionsMenu(Menu, MenuInflater) and related methods.
         *
         * @param savedInstanceState we do not override onSaveInstanceState so do not use
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        /**
         * Initialize the contents of the Activity's standard options menu.  You
         * should place your menu items in to <var>menu</var>.
         *
         * First we add a menu item with the title "Menu 1a" to the menu, and set how the menu should
         * be displayed to SHOW_AS_ACTION_IF_ROOM (Show this item as a button in an Action Bar if the
         * system decides there is room for it). Then we add a menu item with the title "Menu 1b" to
         * the menu, and also set how the menu should be displayed to SHOW_AS_ACTION_IF_ROOM
         *
         * @param menu The options menu in which you place your items.
         * @param inflater could be used to instantiate menu XML files into Menu objects.
         */
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            menu.add("Menu 1a").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.add("Menu 1b").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    /**
     * Second fragment with a menu.
     */
    public static class Menu2Fragment extends Fragment {

        /**
         * Called to do initial creation of a fragment.  This is called after
         * {@link #onAttach(Activity)} and before
         * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
         * <p>
         *  Note that this can be called while the fragment's activity is
         * still in the process of being created.  As such, you can not rely
         * on things like the activity's content view hierarchy being initialized
         * at this point.  If you want to do work once the activity itself is
         * created, see {@link #onActivityCreated(Bundle)}.
         * <p>
         * First we call through to our super's implementation of onCreate, and then we report that
         * this fragment would like to participate in populating the options menu by receiving a
         * call to onCreateOptionsMenu(Menu, MenuInflater) and related methods.
         *
         * @param savedInstanceState we do not override onSaveInstanceState so do not use
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        /**
         * Initialize the contents of the Activity's standard options menu.  You
         * should place your menu items in to <var>menu</var>.
         *
         * We add a menu item with the title "Menu 2" to the menu, and set how the menu should be
         * displayed to SHOW_AS_ACTION_IF_ROOM (Show this item as a button in an Action Bar if the
         * system decides there is room for it).
         *
         * @param menu The options menu in which you place your items.
         * @param inflater could be used to instantiate menu XML files into Menu objects.
         */
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            menu.add("Menu 2").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }
}
