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
import com.example.android.apis.Shakespeare;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Demonstration of using fragments to implement different activity layouts.
 * This sample provides a different layout (and activity flow) when run in
 * landscape. It crashes as it was in landscape mode because of a reference
 * to the non-existent containerViewId R.id.a_item in the call at line 156:
 * <p>
 * (FragmentTransaction) ft.replace(R.id.a_item, details)
 * <p>
 * This was obviously added by a runaway modification script, and the container
 * id should be R.id.details
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentLayout extends Activity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.fragment_layout which is
     * either layout-land/fragment_layout.xml or layout/fragment_layout depending on orientation.
     *
     * @param savedInstanceState we do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_layout);
    }

    /**
     * This is a secondary activity, to show what the user has selected
     * when the screen is not large enough to show it all in one activity.
     */
    public static class DetailsActivity extends Activity {

        /**
         * Called when the activity is starting. First we call through to our super's implementation
         * of onCreate. Then using a Resources instance for the application's package we get the
         * current configuration that is in effect for this resource object and check whether the
         * orientation of the screen given by the field Configuration.orientation is currently set
         * to Configuration.ORIENTATION_LANDSCAPE in which case this Activity is not needed since
         * a dual pane version is in use, so we finish this Activity and return to caller. Otherwise
         * we are in Configuration.ORIENTATION_PORTRAIT and are needed. If we are being recreated
         * <b>savedInstanceState</b> is not null and the system will have taken care of restoring our
         * Fragment so we are done. If it is null this is the first time and we need to add a new
         * instance of our Fragment. To do this we first create a new instance of the Fragment
         * <b>DetailsFragment details</b>, set its arguments to a map of the extras added to the
         * Intent which launched this Activity, and finally using the FragmentManager for interacting
         * with fragments associated with this activity we create a FragmentTransaction, use it to
         * add <b>details</b> to the activity state, and then commit that FragmentTransaction.
         *
         * @param savedInstanceState if null, first time initializations are needed
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE) {
                // If the screen is now in landscape mode, we can show the
                // dialog in-line with the list so we don't need this activity.
                finish();
                return;
            }

            if (savedInstanceState == null) {
                // During initial setup, plug in the details fragment.
                DetailsFragment details = new DetailsFragment();
                details.setArguments(getIntent().getExtras());
                getFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
            }
        }
    }

    /**
     * This is the "top-level" fragment, showing a list of items that the
     * user can pick.  Upon picking an item, it takes care of displaying the
     * data to the user as appropriate based on the current UI layout.
     */
    public static class TitlesFragment extends ListFragment {
        boolean mDualPane; // Flag to indicate whether we are in ORIENTATION_LANDSCAPE dual pane mode
        int mCurCheckPosition = 0; // Currently selected title to be displayed

        /**
         * Called when the fragment's activity has been created and this fragment's view hierarchy
         * instantiated. First we call through to our super's implementation of onActivityCreated.
         * Next we set the cursor for the list view of this ListFragment to an <b>ArrayAdapter</b>
         * consisting of the String[] array Shakespeare.TITLES. Then we determine whether we are
         * in ORIENTATION_LANDSCAPE (dual pane mode) by searching our Activity's content view for
         * a view with the id R.id.details, saving a reference to it in <b>View detailsFrame</b>.
         * If <b>detailsFrame</b> is not null and the View is VISIBLE we set our field <b>mDualPane</b>
         * to true. If <b>savedInstanceState</b> is not null we use it to retrieve the value of our
         * field mCurCheckPosition which our callback onSaveInstanceState saved under the key
         * "curChoice". If we have determined that we are in dual pane mode (ORIENTATION_LANDSCAPE)
         * we set the choice mode for our ListView to CHOICE_MODE_SINGLE so that the currently
         * selected item is highlighted, and then call our method <b>showDetails(mCurCheckPosition)</b>
         * to display the details of the selected item in the other pane.
         *
         * @param savedInstanceState If not null it contains mCurCheckPosition saved by our callback
         *                           onSaveInstanceState under the key "curChoice"
         */
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // Populate list with our static array of titles.
            setListAdapter(new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_activated_1, Shakespeare.INSTANCE.getTITLES()));

            // Check to see if we have a frame in which to embed the details
            // fragment directly in the containing UI.
            View detailsFrame = getActivity().findViewById(R.id.details);
            mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

            if (savedInstanceState != null) {
                // Restore last state for checked position.
                mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
            }

            if (mDualPane) {
                // In dual-pane mode, the list view highlights the selected item.
                getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                // Make sure our UI is in the correct state.
                showDetails(mCurCheckPosition);
            }
        }

        /**
         * Called to ask the fragment to save its current dynamic state, so it
         * can later be reconstructed in a new instance of its process is
         * restarted.  If a new instance of the fragment later needs to be
         * created, the data you place in the Bundle here will be available
         * in the Bundle given to {@link #onCreate(Bundle)},
         * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, and
         * {@link #onActivityCreated(Bundle)}.
         * <p>
         * <p>This corresponds to {@link Activity#onSaveInstanceState(Bundle)
         * Activity.onSaveInstanceState(Bundle)} and most of the discussion there
         * applies here as well.  Note however: <em>this method may be called
         * at any time before {@link #onDestroy()}</em>.  There are many situations
         * where a fragment may be mostly torn down (such as when placed on the
         * back stack with no UI showing), but its state will not be saved until
         * its owning activity actually needs to save its state.
         * <p>
         * First we call through to our super's implementation of onSaveInstanceState, then we
         * insert the value of our field <b>int mCurCheckPosition</b> into the mapping of the
         * <b>Bundle outState</b> parameter under the key "curChoice".
         *
         * @param outState Bundle in which to place your saved state.
         */
        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("curChoice", mCurCheckPosition);
        }

        /**
         * This method will be called when an item in the list is selected. We simply call our method
         * <b>showDetails</b> using the position of the view in the list that was selected as the
         * index to the String[] Shakespeare.DIALOGUE array we wish to have displayed.
         *
         * @param l        The ListView where the click happened
         * @param v        The view that was clicked within the ListView
         * @param position The position of the view in the list
         * @param id       The row id of the item that was clicked
         */
        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            showDetails(position);
        }

        /**
         * Helper function to show the details of a selected item, either by displaying a fragment
         * in-place in the current UI if we are in dual pane landscape mode, or starting a whole
         * new activity in which it is displayed for portrait mode.
         * <p>
         * First we save our parameter <b>int index</b> in our field <b>int mCurCheckPosition</b>.
         * Then if we are in dual pane mode (<b>boolean mDualPane</b> is true: device is using
         * layout-land/fragment_layout.xml) we set the checked state of the specified position
         * <b>index</b>, then we use the FragmentManager to search for a fragment with the id
         * R.id.details (the id we use when adding DetailFragment), and if no Fragment with that id
         * is found, or the method DetailsFragment.getShownIndex() returns an index different from
         * the one just selected we need to add a DetailsFragment for the new <b>index</b>. To do
         * this we first create a new instance of DetailsFragment with the new index into the
         * String[] Shakespeare.DIALOGUE array, use the FragmentManager for interacting with
         * fragments associated with this fragment's activity to begin <b>FragmentTransaction ft</b>
         * then use <b>ft</b> to replace the existing fragment with id R.id.details (if any) with
         * our new <b>DetailsFragment details</b>, set a transition animation of TRANSIT_FRAGMENT_FADE,
         * and finally commit the FragmentTransaction. If we are not in dual pane mode (i.e.
         * <b>boolean mDualPane</b> is false: device is using layout/fragment_layout.xml) we create
         * an <b>Intent intent</b>, set its class to DetailsActivity.class, add extended data for the
         * parameter <b>int index</b> under the key "index" to the intent, and start that Intent as
         * a new Activity.
         *
         * @param index index into the String[] Shakespeare.DIALOGUE we wish to have displayed
         */
        void showDetails(int index) {
            mCurCheckPosition = index;

            if (mDualPane) {
                // We can display everything in-place with fragments, so update
                // the list to highlight the selected item and show the data.
                getListView().setItemChecked(index, true);

                // Check what fragment is currently shown, replace if needed.
                DetailsFragment details = (DetailsFragment)
                        getFragmentManager().findFragmentById(R.id.details);
                if (details == null || details.getShownIndex() != index) {
                    // Make new fragment to show this selection.
                    details = DetailsFragment.newInstance(index);

                    // Execute a transaction, replacing any existing fragment
                    // with this one inside the frame.
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    if (index == 0) {
                        ft.replace(R.id.details, details);
                    } else {
                        ft.replace(R.id.details, details);
                    }
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.commit();
                }

            } else {
                // Otherwise we need to launch a new activity to display
                // the dialog fragment with selected text.
                Intent intent = new Intent();
                intent.setClass(getActivity(), DetailsActivity.class);
                intent.putExtra("index", index);
                startActivity(intent);
            }
        }
    }

    /**
     * This is the secondary fragment, displaying the details of a particular item.
     */
    public static class DetailsFragment extends Fragment {
        /**
         * Create a new instance of DetailsFragment, initialized to show the text at 'index'. First
         * we create a new instance of <b>DetailsFragment f</b>, then we create a <b>Bundle args</b>
         * and add our parameter <b>int index</b> to it using the key "index". We set the arguments
         * of <b>f</b> to <b>args</b> and return <b>f</b> to the caller.
         *
         * @param index index into the String[] Shakespeare.DIALOGUE array to display
         *
         * @return a new instance of DetailsFragment with its arguments set to include the value of
         *         <b>int index</b> stored under the key "index".
         */
        public static DetailsFragment newInstance(int index) {
            DetailsFragment f = new DetailsFragment();

            // Supply index input as an argument.
            Bundle args = new Bundle();
            args.putInt("index", index);
            f.setArguments(args);

            return f;
        }

        /**
         * Return the <b>int</b> argument supplied to setArguments(Bundle), (if any) which were
         * stored under the key "index".
         *
         * @return integer argument which was stored under the key "index" or 0
         */
        public int getShownIndex() {
            return getArguments().getInt("index", 0);
        }

        /**
         * Called to have the fragment instantiate its user interface view. First we check to see if
         * we have a containing frame, and if we do not it means we do not need to inflate our View
         * because in the present layout (portrait orientation) we would not be displayed, so we just
         * return null to the caller. Otherwise we create a <b>ScrollView scroller</b>, create a
         * <b>TextView text</b> to place inside <b>scroller</b>, configure the padding of <b>text</b>,
         * add the <b>TextView text</b> to <b>ScrollView scroller</b>, set the text of <b>text</b> to
         * the element of the <b>String[] Shakespeare.DIALOGUE</b> selected by our arguments when we
         * were created, and finally we return <b>ScrollView scroller</b> to the caller.
         *
         * @param inflater The LayoutInflater object that can be used to inflate
         * any views in the fragment,
         * @param container If non-null, this is the parent view that the fragment's
         * UI should be attached to.  The fragment should not add the view itself,
         * but this can be used to generate the LayoutParams of the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         * from a previous saved state as given here.
         *
         * @return Return the View for the fragment's UI, or null
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            if (container == null) {
                // We have different layouts, and in one of them this
                // fragment's containing frame doesn't exist.  The fragment
                // may still be created from its saved state, but there is
                // no reason to try to create its view hierarchy because it
                // won't be displayed.  Note this is not needed -- we could
                // just run the code below, where we would create and return
                // the view hierarchy; it would just never be used.
                return null;
            }

            ScrollView scroller = new ScrollView(getActivity());
            TextView text = new TextView(getActivity());
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    4, getActivity().getResources().getDisplayMetrics());
            text.setPadding(padding, padding, padding, padding);
            scroller.addView(text);
            text.setText(Shakespeare.INSTANCE.getDIALOGUE()[getShownIndex()]);
            return scroller;
        }
    }

}
