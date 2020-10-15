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

package com.example.android.apis.app

import com.example.android.apis.R
import com.example.android.apis.Shakespeare

import android.annotation.TargetApi
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ScrollView
import android.widget.TextView

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.ListFragment

import androidx.fragment.app.FragmentActivity

/**
 * Demonstration of using fragments to implement different activity layouts.
 * This sample provides a different layout (and activity flow) when run in
 * landscape. It crashes as it was in landscape mode because of a reference
 * to the non-existent containerViewId R.id.a_item in the call at line 156:
 *
 * (FragmentTransaction) ft.replace(R.id.a_item, details)
 *
 * This was obviously added by a runaway modification script, and the container
 * id should be R.id.details
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class FragmentLayout : FragmentActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.fragment_layout which is
     * either layout-land/fragment_layout.xml or layout/fragment_layout.xml depending on orientation.
     *
     * @param savedInstanceState we do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_layout)
    }

    /**
     * This is a secondary activity, to show what the user has selected
     * when the screen is not large enough to show it all in one activity.
     */
    class DetailsActivity : FragmentActivity() {

        /**
         * Called when the activity is starting. First we call through to our super's implementation
         * of `onCreate`. Then using a `Resources` instance for the application's package we get the
         * current configuration that is in effect for this resource object and check whether the
         * orientation of the screen given by the field [Configuration.orientation] is currently set
         * to Configuration.ORIENTATION_LANDSCAPE in which case this Activity is not needed since
         * a dual pane version is in use, so we finish this Activity and return to caller. Otherwise
         * we are in Configuration.ORIENTATION_PORTRAIT and are needed. If we are being recreated
         * [savedInstanceState] is not *null* and the system will have taken care of restoring our
         * [Fragment] so we are done. If it is *null* this is the first time and we need to add a new
         * instance of our [Fragment]. To do this we first create a new instance of [DetailsFragment]
         * to initialize our variable `var details`, set its arguments to a map of the extended data
         * added to the [Intent] which launched this Activity, and finally using the support
         * `FragmentManager` for interacting with fragments associated with this activity we begin
         * a [FragmentTransaction], use it to add `details` to the activity state, and then commit
         * that [FragmentTransaction].
         *
         * @param savedInstanceState if null, first time initializations are needed
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                // If the screen is now in landscape mode, we can show the
                // dialog in-line with the list so we don't need this activity.
                finish()
                return
            }

            if (savedInstanceState == null) {
                // During initial setup, plug in the details fragment.
                val details = DetailsFragment()
                details.arguments = intent.extras
                supportFragmentManager.beginTransaction().add(android.R.id.content, details).commit()
            }
        }
    }

    /**
     * This is the "top-level" fragment, showing a list of items that the
     * user can pick.  Upon picking an item, it takes care of displaying the
     * data to the user as appropriate based on the current UI layout.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    class TitlesFragment : ListFragment() {
        /**
         * Flag to indicate whether we are in ORIENTATION_LANDSCAPE dual pane mode
         */
        internal var mDualPane: Boolean = false
        /**
         * Currently selected title to be displayed
         */
        internal var mCurCheckPosition = 0

        /**
         * Called when the fragment's activity has been created and this fragment's view hierarchy
         * instantiated. First we call through to our super's implementation of `onActivityCreated`.
         * Next we set the cursor for the list view of this [ListFragment] to an [ArrayAdapter]
         * consisting of the `String[]` array [Shakespeare.TITLES]. Then we determine whether we are
         * in ORIENTATION_LANDSCAPE (dual pane mode) by searching our Activity's content view for
         * a view with the id R.id.details, saving a reference to it in our [View] variable
         * `val detailsFrame`. If `detailsFrame` is not *null* and the View is VISIBLE we set our
         * [Boolean] field [mDualPane] to *true*. If [savedInstanceState] is not *null* we use it to
         * retrieve the value of our field [mCurCheckPosition] which our callback [onSaveInstanceState]
         * saved under the key "curChoice". If we have determined that we are in dual pane mode
         * (ORIENTATION_LANDSCAPE) we set the choice mode for our [ListView] to CHOICE_MODE_SINGLE
         * so that the currently selected item is highlighted, and then call our method [showDetails]
         * with [mCurCheckPosition] to display the details of this selected item in the other pane.
         *
         * @param savedInstanceState If not *null* it contains [mCurCheckPosition] saved by our
         * callback [onSaveInstanceState] under the key "curChoice"
         */
        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)

            // Populate list with our static array of titles.

            listAdapter = ArrayAdapter(requireActivity(),
                    android.R.layout.simple_list_item_activated_1, Shakespeare.TITLES)

            // Check to see if we have a frame in which to embed the details
            // fragment directly in the containing UI.
            val detailsFrame = requireActivity().findViewById<View>(R.id.details)
            mDualPane = detailsFrame != null && detailsFrame.visibility == View.VISIBLE

            if (savedInstanceState != null) {
                // Restore last state for checked position.
                mCurCheckPosition = savedInstanceState.getInt("curChoice", 0)
            }

            if (mDualPane) {
                // In dual-pane mode, the list view highlights the selected item.
                listView.choiceMode = ListView.CHOICE_MODE_SINGLE
                // Make sure our UI is in the correct state.
                showDetails(mCurCheckPosition)
            }
        }

        /**
         * Called to ask the fragment to save its current dynamic state, so it
         * can later be reconstructed in a new instance of its process when
         * restarted.  If a new instance of the fragment later needs to be
         * created, the data you place in the Bundle here will be available
         * in the Bundle given to [onCreate], [onCreateView], and [onActivityCreated].
         *
         * This corresponds to [FragmentActivity.onSaveInstanceState] and most of the discussion
         * there applies here as well.  Note however: this method may be called at any time before
         * [onDestroy]. There are many situations where a fragment may be mostly torn down (such as
         * when placed on the back stack with no UI showing), but its state will not be saved until
         * its owning activity actually needs to save its state.
         *
         * First we call through to our super's implementation of `onSaveInstanceState`, then we
         * insert the value of our [Int] field [mCurCheckPosition] into the mapping of the [Bundle]
         * parameter [outState] under the key "curChoice".
         *
         * @param outState [Bundle] in which to place your saved state.
         */
        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putInt("curChoice", mCurCheckPosition)
        }

        /**
         * This method will be called when an item in the list is selected. We simply call our method
         * [showDetails] using the position of the view in the list that was selected as the
         * index to the `String[]` array [Shakespeare.DIALOGUE] we wish to have displayed.
         *
         * @param l        The [ListView] where the click happened
         * @param v        The view that was clicked within the ListView
         * @param position The position of the view in the list
         * @param id       The row id of the item that was clicked
         */
        override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
            showDetails(position)
        }

        /**
         * Helper function to show the details of a selected item, either by displaying a fragment
         * in-place in the current UI if we are in dual pane landscape mode, or starting a whole
         * new activity in which it is displayed for portrait mode.
         *
         * First we save our [Int] parameter [index] in our [Int] field [mCurCheckPosition].
         * Then if we are in dual pane mode ([mDualPane] is true: device is using the layout file
         * layout-land/fragment_layout.xml) we set the checked state of the specified position
         * [index], then we use the support `FragmentManager` to search for a fragment with the id
         * R.id.details (the id we use when adding a [DetailsFragment]) and use the value returned
         * to initialize our [DetailsFragment] variable `var details`, and if no Fragment with that
         * id is found, or the `getShownIndex` method of `details` returns an index different from
         * the one just selected we need to add a [DetailsFragment] for the new [index]. To do
         * this we first create a new instance of [DetailsFragment] with the new [index] into the
         * `String[]` array [Shakespeare.DIALOGUE] and save the reference in `details`, use the
         * support `FragmentManager` for interacting with fragments associated with this fragment's
         * activity to begin a [FragmentTransaction] to initialize our variable `val ft`, then use
         * `ft` to replace the existing fragment with id R.id.details (if any) with
         * our new `details` [DetailsFragment], set a transition animation of TRANSIT_FRAGMENT_FADE,
         * and finally commit the [FragmentTransaction]. If we are not in dual pane mode (i.e.
         * [mDualPane] is false: device is using layout/fragment_layout.xml) we create an [Intent]
         * to initialize our variable `var intent`, set its class to [DetailsActivity.class], add
         * extended data for the parameter [index] under the key "index" to the intent, and start
         * that Intent as a new Activity.
         *
         * @param index index into the String[] Shakespeare.DIALOGUE we wish to have displayed
         */
        internal fun showDetails(index: Int) {
            mCurCheckPosition = index

            if (mDualPane) {
                // We can display everything in-place with fragments, so update
                // the list to highlight the selected item and show the data.
                listView.setItemChecked(index, true)

                // Check what fragment is currently shown, replace if needed.

                var details = requireActivity().supportFragmentManager.findFragmentById(R.id.details) as DetailsFragment?
                if (details == null || details.shownIndex != index) {
                    // Make new fragment to show this selection.
                    details = DetailsFragment.newInstance(index)

                    // Execute a transaction, replacing any existing fragment
                    // with this one inside the frame.

                    val ft = parentFragmentManager.beginTransaction()
                    if (index == 0) {
                        ft.replace(R.id.details, details)
                    } else {
                        ft.replace(R.id.details, details)
                    }
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    ft.commit()
                }

            } else {
                // Otherwise we need to launch a new activity to display
                // the dialog fragment with selected text.
                val intent = Intent()

                intent.setClass(requireActivity(), DetailsActivity::class.java)
                intent.putExtra("index", index)
                startActivity(intent)
            }
        }
    }

    /**
     * This is the secondary fragment, displaying the details of a particular item.
     */
    class DetailsFragment : Fragment() {

        /**
         * Return the [Int] argument supplied to `setArguments(Bundle)`, (if any) which were
         * stored under the key "index".
         *
         * @return integer argument which was stored under the key "index" or 0
         */
        val shownIndex: Int
            get() = requireArguments().getInt("index", 0)

        /**
         * Called to have the fragment instantiate its user interface view. First we check to see if
         * we have a containing frame, and if we do not it means we do not need to inflate our View
         * because in the present layout (portrait orientation) we would not be displayed, so we just
         * return *null* to the caller. Otherwise we create a [ScrollView] instance to initialize our
         * variable `val scroller`, create a [TextView] to initialize our variable `val text`,
         * configure the padding of `text`, add `text` to `scroller`, set the text of `text` to
         * the element of the `String[]` array [Shakespeare.DIALOGUE] selected by our arguments when
         * we were created, and finally we return `scroller` to the caller.
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
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            if (container == null) {
                // We have different layouts, and in one of them this
                // fragment's containing frame doesn't exist.  The fragment
                // may still be created from its saved state, but there is
                // no reason to try to create its view hierarchy because it
                // won't be displayed.  Note this is not needed -- we could
                // just run the code below, where we would create and return
                // the view hierarchy; it would just never be used.
                return null
            }

            val scroller = ScrollView(activity)
            val text = TextView(activity)

            val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    4f, requireActivity().resources.displayMetrics).toInt()
            text.setPadding(padding, padding, padding, padding)
            scroller.addView(text)
            text.text = Shakespeare.DIALOGUE[shownIndex]
            return scroller
        }

        /**
         * Our static factory method.
         */
        companion object {
            /**
             * Create a new instance of [DetailsFragment], initialized to show the text at [index].
             * First we create a new instance of [DetailsFragment] to initialize our variable `val f`,
             * then we create a [Bundle] to initialize our variable `val args` and add our parameter
             * [index] to it under the key "index". We set the arguments of `f` to `args` and return
             * `f` to the caller.
             *
             * @param index index into the `String[]` array [Shakespeare.DIALOGUE] to display
             *
             * @return a new instance of [DetailsFragment] with its arguments set to include the
             * value of [index] stored under the key "index".
             */
            fun newInstance(index: Int): DetailsFragment {
                val f = DetailsFragment()

                // Supply index input as an argument.
                val args = Bundle()
                args.putInt("index", index)
                f.arguments = args

                return f
            }
        }
    }

}
