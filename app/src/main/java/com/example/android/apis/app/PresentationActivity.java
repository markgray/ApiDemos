/*
 * Copyright (C) 2012 The Android Open Source Project
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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.apis.R;


/**
 * <h3>Presentation Activity</h3>
 *
 * <p>
 * This demonstrates how to create an activity that shows some content
 * on a secondary display using a {@link Presentation}.
 * </p><p>
 * The activity uses the {@link DisplayManager} API to enumerate displays.
 * When the user selects a display, the activity opens a {@link Presentation}
 * on that display.  We show a different photograph in each presentation
 * on a unique background along with a label describing the display.
 * We also write information about displays and display-related events to
 * the Android log which you can read using <code>adb logcat</code>.
 * </p><p>
 * You can try this out using an HDMI or Wifi display or by using the
 * "Simulate secondary displays" feature in Development Settings to create a few
 * simulated secondary displays.  Each display will appear in the list along with a
 * checkbox to show a presentation on that display.
 * </p><p>
 * See also the {@link PresentationWithMediaRouterActivity} sample which
 * uses the media router to automatically select a secondary display
 * on which to show content based on the currently selected route.
 * </p>
 */
@TargetApi(Build.VERSION_CODES.M)
public class PresentationActivity extends Activity
       implements OnCheckedChangeListener, OnClickListener, OnItemSelectedListener {

    private final String TAG = "PresentationActivity"; // Used for logging

    // Key for storing saved instance state.
    private static final String PRESENTATION_KEY = "presentation";

    // The content that we want to show on the presentation.
    private static final int[] PHOTOS = new int[] {
        R.drawable.frantic,
        R.drawable.photo1, R.drawable.photo2, R.drawable.photo3,
        R.drawable.photo4, R.drawable.photo5, R.drawable.photo6,
        R.drawable.sample_4,
    };

    private DisplayManager mDisplayManager; // Used to manage the properties of attached displays
    private DisplayListAdapter mDisplayListAdapter; // An ArrayAdapter<Display> to list displays
    private CheckBox mShowAllDisplaysCheckbox; // Checkbox in layout for displaying all displays
    @SuppressWarnings("FieldCanBeLocal")
    private ListView mListView; // ListView in layout
    private int mNextImageNumber; // Used to cycle through the 8 photos displayed

    // List of presentation contents indexed by displayId.
    // This state persists so that we can restore the old presentation
    // contents when the activity is paused or resumed.
    private SparseArray<DemoPresentationContents> mSavedPresentationContents;

    // List of all currently visible presentations indexed by display id.
    private final SparseArray<DemoPresentation> mActivePresentations =
            new SparseArray<>();

    /**
     * Initialization of the Activity after it is first created.  Must at least
     * call {@link android.app.Activity#setContentView setContentView()} to
     * describe what is to be displayed in the screen.
     *
     * First we call through to our super's implementation of onCreate. Then if savedInstanceState
     * is not null (our Activity is being restarted after previously being shut down) we retrieve the
     * value of SparseArray<DemoPresentation> mActivePresentations that we saved when our callback
     * onSaveInstanceState was called, and if it is null we simply set mActivePresentations to a new
     * instance of SparseArray<DemoPresentation>. Next we set our field DisplayManager mDisplayManager
     * to the handle for the DISPLAY_SERVICE system-level service. We set our content view to our
     * layout file R.layout.presentation_activity, find the CheckBox R.id.show_all_displays ("Show
     * all displays") save a reference in the field CheckBox mShowAllDisplaysCheckbox and set its
     * OnCheckedListener to "this". We set our field DisplayListAdapter mDisplayListAdapter to a new
     * instance of our class DisplayListAdapter using "this" as its Context, find the ListView
     * R.id.display_list in our layout, save a reference in our field ListView mListView, and set the
     * adapter of mListView to mDisplayListAdapter.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

        // Restore saved instance state.
        if (savedInstanceState != null) {
            mSavedPresentationContents =
                    savedInstanceState.getSparseParcelableArray(PRESENTATION_KEY);
        } else {
            mSavedPresentationContents = new SparseArray<>();
        }

        // Get the display manager service.
        mDisplayManager = (DisplayManager)getSystemService(Context.DISPLAY_SERVICE);

        // See assets/res/any/layout/presentation_activity.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.presentation_activity);

        // Set up checkbox to toggle between showing all displays or only presentation displays.
        mShowAllDisplaysCheckbox = (CheckBox)findViewById(R.id.show_all_displays);
        mShowAllDisplaysCheckbox.setOnCheckedChangeListener(this);

        // Set up the list of displays.
        mDisplayListAdapter = new DisplayListAdapter(this);
        mListView = (ListView)findViewById(R.id.display_list);
        mListView.setAdapter(mDisplayListAdapter);
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or
     * {@link #onPause}, for your activity to start interacting with the user.
     *
     * First we call through to our super's implementation of onResume. Then we update the contents
     * of the display list adapter to show information about all current displays by calling our
     * method DisplayListAdapter.updateContents(). Then we restore presentations from before the
     * activity was paused by first finding the number of Display's known to the mDisplayListAdapter
     * adapter, then looping though those Display's, fetching the Display display from the adapter
     * and using the display id to index into the SparseArray mSavedPresentationContents to fetch
     * the DemoPresentationContents contents for that Display and if the "contents" is not null we
     * shows that "contents" on the specified display. When done restoring the presentations we
     * remove all key-value mappings from the SparseArray mSavedPresentationContents. Finally we
     * register our display listener "DisplayManager.DisplayListener mDisplayListener" to receive
     * notifications about when displays are added, removed or changed.
     */
    @Override
    protected void onResume() {
        // Be sure to call the super class.
        super.onResume();

        // Update our list of displays on resume.
        mDisplayListAdapter.updateContents();

        // Restore presentations from before the activity was paused.
        final int numDisplays = mDisplayListAdapter.getCount();
        for (int i = 0; i < numDisplays; i++) {
            final Display display = mDisplayListAdapter.getItem(i);
            //noinspection ConstantConditions
            final DemoPresentationContents contents =
                    mSavedPresentationContents.get(display.getDisplayId());
            if (contents != null) {
                showPresentation(display, contents);
            }
        }
        mSavedPresentationContents.clear();

        // Register to receive events from the display manager.
        mDisplayManager.registerDisplayListener(mDisplayListener, null);
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed.  The counterpart to
     * {@link #onResume}.
     *
     * First we call through to our super's implementation of onPause. Then we unregister our
     * display listener "DisplayManager.DisplayListener mDisplayListener". Then we loop through
     * "SparseArray<DemoPresentation> mActivePresentations" (our list of all currently visible
     * presentations indexed by display id) fetching the "DemoPresentation presentation" stored
     * there and the "int displayId" it was stored under and use these to add a mapping from the
     * "displayId" to "presentation". Then we call presentation.dismiss() to dismiss that
     * presentation. When done saving all the presentations to "mSavedPresentationContents" and
     * dismissing them we remove all key-value mappings the SparseArray mActivePresentations.
     */
    @Override
    protected void onPause() {
        // Be sure to call the super class.
        super.onPause();

        // Unregister from the display manager.
        mDisplayManager.unregisterDisplayListener(mDisplayListener);

        // Dismiss all of our presentations but remember their contents.
        Log.d(TAG, "Activity is being paused.  Dismissing all active presentation.");
        for (int i = 0; i < mActivePresentations.size(); i++) {
            DemoPresentation presentation = mActivePresentations.valueAt(i);
            int displayId = mActivePresentations.keyAt(i);
            mSavedPresentationContents.put(displayId, presentation.mContents);
            presentation.dismiss();
        }
        mActivePresentations.clear();
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in {@link #onCreate} or
     * {@link #onRestoreInstanceState} (the {@link Bundle} populated by this method
     * will be passed to both).
     *
     * First we call through to our super's implementation of onSaveInstanceState, then we insert
     * our SparseArray<DemoPresentationContents> mSavedPresentationContents into the mapping of
     * the Bundle outState using the key PRESENTATION_KEY ("presentation").
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Be sure to call the super class.
        super.onSaveInstanceState(outState);
        outState.putSparseParcelableArray(PRESENTATION_KEY, mSavedPresentationContents);
    }

    /**
     * Shows a {@link Presentation} on the specified display.
     *
     * First we retrieve the displayId from the Display display passed us, if there is already
     * a DemoPresentation in mActivePresentations for that display we return having done nothing.
     * Otherwise we create a DemoPresentation presentation for that Display display and
     * DemoPresentationContents contents, show the presentation on that Display, set the
     * OnDismissListener of the presentation to "mOnDismissListener", and finally add the
     * presentation to our SparseArray<DemoPresentation> mActivePresentations under the key
     * displayId.
     *
     * @param display Display to show the DemoPresentationContents contents on
     * @param contents Information about the content we want to show in the presentation.
     */
    private void showPresentation(Display display, DemoPresentationContents contents) {
        final int displayId = display.getDisplayId();
        if (mActivePresentations.get(displayId) != null) {
            return;
        }

        Log.d(TAG, "Showing presentation photo #" + contents.photo
                + " on display #" + displayId + ".");

        DemoPresentation presentation = new DemoPresentation(this, display, contents);
        presentation.show();
        presentation.setOnDismissListener(mOnDismissListener);
        mActivePresentations.put(displayId, presentation);
    }

    /**
     * Hides a {@link Presentation} on the specified display.
     *
     * First we fetch the display Id of the Display display into displayId, then we retrieve the
     * DemoPresentation presentation stored under the key displayId in SparseArray<DemoPresentation>
     * mActivePresentations, and if there is no presentation recorded there we return having done
     * nothing. If there is a presentation on that displayId we dismiss() it and delete the
     * presentation stored under the key displayId in the mActivePresentations array.
     *
     * @param display Display whose presentation we want to hide.
     */
    private void hidePresentation(Display display) {
        final int displayId = display.getDisplayId();
        DemoPresentation presentation = mActivePresentations.get(displayId);
        if (presentation == null) {
            return;
        }

        Log.d(TAG, "Dismissing presentation on display #" + displayId + ".");

        presentation.dismiss();
        mActivePresentations.delete(displayId);
    }

    /**
     * Sets the display mode of the {@link Presentation} on the specified display
     * if it is already shown.
     *
     * First we fetch the display Id from the Display display, and if there is no DemoPresentation
     * presentation stored in SparseArray<DemoPresentation> mActivePresentations under that key we
     * return having done nothing. Otherwise we call setPreferredDisplayMode to set the preferred
     * display mode of the presentation being displayed on that screen to the "int displayModeId"
     * passed to us.
     *
     * @param display Display to set the display mode on
     * @param displayModeId display mode Id to set the display's Display.Mode to.
     */
    private void setPresentationDisplayMode(Display display, int displayModeId) {
        final int displayId = display.getDisplayId();
        DemoPresentation presentation = mActivePresentations.get(displayId);
        if (presentation == null) {
            return;
        }

        presentation.setPreferredDisplayMode(displayModeId);
    }

    /**
     * Returns the mNextImageNumber to use to index into the int PHOTOS[] array of resource id's
     * in order choose a photo resource id to display, then performs a modular increment of
     * mNextImageNumber modulus PHOTOS.length in order to wrap around to 0 when all photos have
     * been used.
     *
     * @return index to int PHOTOS[] array of resource id's that is next to be displayed
     */
    private int getNextPhoto() {
        final int photo = mNextImageNumber;
        mNextImageNumber = (mNextImageNumber + 1) % PHOTOS.length;
        return photo;
    }

    /**
     * Called when the show all displays checkbox is toggled or when an item in the list of
     * displays is checked or unchecked.
     *
     * First we check if it was the "show all displays checkbox" and if it is we call
     * mDisplayListAdapter.updateContents() to update the contents of the display list adapter
     * to show information about all current displays. Otherwise one of the items in the list of
     * displays has been clicked so we retrieve the CompoundButton buttonView's tag to Display
     * display and if the checkbox is now checked (isChecked == true) we create a new
     * DemoPresentationContents contents from the next photo due to be displayed and show this
     * contents DemoPresentationContents on the Display display, and if it is not checked we hide
     * the presentation on that display by calling hidePresentation(display). In either case we
     * then call mDisplayListAdapter.updateContents() to update the contents of the display list
     * adapter.
     *
     * @param buttonView The compound button view whose state has changed.
     * @param isChecked The new checked state of buttonView.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mShowAllDisplaysCheckbox) {
            // Show all displays checkbox was toggled.
            mDisplayListAdapter.updateContents();
        } else {
            // Display item checkbox was toggled.
            final Display display = (Display)buttonView.getTag();
            if (isChecked) {
                DemoPresentationContents contents = new DemoPresentationContents(getNextPhoto());
                showPresentation(display, contents);
            } else {
                hidePresentation(display);
            }
            mDisplayListAdapter.updateContents();
        }
    }

    /**
     * Called when the Info button next to a display is clicked to show information about the
     * display. First we retrieve the context of the Button clicked into Context context, then
     * we create AlertDialog.Builder builder using this context. We retrieve the Display the
     * Button is associated with to Display display by calling getTag() on the Button. We create
     * a Resources instance for the application's package in Resources r. We then use our
     * AlertDialog.Builder builder to create an AlertDialog alert with the title "Display #? Info"
     * (with the ? replaced by the display Id), a message displaying the information returned by
     * Display.toString(), and a neutral Button with the text "OK" which dismisses the AlertDialog
     * when clicked. Finally we show this AlertDialog alert.
     *
     * @param v Button which was clicked
     */
    @Override
    public void onClick(View v) {
        Context context = v.getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final Display display = (Display)v.getTag();
        Resources r = context.getResources();
        AlertDialog alert = builder
                .setTitle(r.getString(
                        R.string.presentation_alert_info_text, display.getDisplayId()))
                .setMessage(display.toString())
                .setNeutralButton(R.string.presentation_alert_dismiss_text,
                        new DialogInterface.OnClickListener() {
                            /**
                             * Called when the "OK" Button is clicked we simply dismiss the dialog.
                             *
                             * @param dialog The dialog that received the click.
                             * @param which The button that was clicked (e.g.
                             *              {@link DialogInterface#BUTTON1}) or
                             *              the position of the item clicked.
                             */
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                    })
                .create();
        alert.show();
    }

    /**
     * Called when a display mode has been selected. We retrieve the tag from the AdapterView parent
     * into Display display, retrieve the supported modes of this display into Display.Mode[] modes,
     * then we set the display mode of the Presentation on the specified display (if it is already
     * shown) to the mode[] of the position of the view in the adapter. (Only used for Display's that
     * have more than one mode.)
     *
     * @param parent The AdapterView where the selection happened
     * @param view The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final Display display = (Display)parent.getTag();
        final Display.Mode[] modes = display.getSupportedModes();
        setPresentationDisplayMode(display, position >= 1 && position <= modes.length ?
                modes[position - 1].getModeId() : 0);
    }

    /**
     * Called when a display mode has been unselected. We retrieve the tag from the AdapterView parent
     * into Display display and set the display mode of the Display to 0 (no preference) (Only used
     * for Display's that have more than one mode.)
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        final Display display = (Display)parent.getTag();
        setPresentationDisplayMode(display, 0);
    }

    /**
     * Listens for displays to be added, changed or removed.
     * We use it to update the list and show a new {@link Presentation} when a
     * display is connected.
     *
     * Note that we don't bother dismissing the {@link Presentation} when a
     * display is removed, although we could.  The presentation API takes care
     * of doing that automatically for us.
     */
    private final DisplayManager.DisplayListener mDisplayListener =
            new DisplayManager.DisplayListener() {
                /**
                 * Called whenever a logical display has been added to the system.
                 * Use {@link DisplayManager#getDisplay} to get more information about
                 * the display.
                 *
                 * We simply instruct our DisplayListAdapter mDisplayListAdapter to update its
                 * contents to show information about all current displays.
                 *
                 * @param displayId The id of the logical display that was added.
                 */
                @Override
                public void onDisplayAdded(int displayId) {
                    Log.d(TAG, "Display #" + displayId + " added.");
                    mDisplayListAdapter.updateContents();
                }
                /**
                 * Called whenever the properties of a logical display have changed.
                 *
                 * We simply instruct our DisplayListAdapter mDisplayListAdapter to update its
                 * contents to show information about all current displays.
                 *
                 * @param displayId The id of the logical display that changed.
                 */
                @Override
                public void onDisplayChanged(int displayId) {
                    Log.d(TAG, "Display #" + displayId + " changed.");
                    mDisplayListAdapter.updateContents();
                }
                /**
                 * Called whenever a logical display has been removed from the system.
                 *
                 * We simply instruct our DisplayListAdapter mDisplayListAdapter to update its
                 * contents to show information about all current displays.
                 *
                 * @param displayId The id of the logical display that was removed.
                 */
                @Override
                public void onDisplayRemoved(int displayId) {
                    Log.d(TAG, "Display #" + displayId + " removed.");
                    mDisplayListAdapter.updateContents();
                }
            };

    /**
     * Listens for when presentations are dismissed.
     */
    private final DialogInterface.OnDismissListener mOnDismissListener =
            new DialogInterface.OnDismissListener() {
                /**
                 * This method will be invoked when the dialog is dismissed. We cast our dialog
                 * parameter to DemoPresentation presentation, retrieve the displayId from the
                 * Display used for the DemoPresentation presentation, remove the displayId key
                 * value from SparseArray<DemoPresentation> mActivePresentations, and then
                 * notify our DisplayListAdapter mDisplayListAdapter that the underlying data has
                 * been changed and any View reflecting the data set should refresh itself.
                 *
                 * @param dialog The dialog that was dismissed
                 */
                @Override
                public void onDismiss(DialogInterface dialog) {
                    DemoPresentation presentation = (DemoPresentation) dialog;
                    int displayId = presentation.getDisplay().getDisplayId();
                    Log.d(TAG, "Presentation on display #" + displayId + " was dismissed.");
                    mActivePresentations.delete(displayId);
                    mDisplayListAdapter.notifyDataSetChanged();
                }
            };

    /**
     * List adapter.
     * Shows information about all displays.
     */
    private final class DisplayListAdapter extends ArrayAdapter<Display> {
        final Context mContext;

        /**
         * Initializes this instance of DisplayListAdapter by first calling through to our super's
         * constructor using the layout file R.layout.presentation_list_item for it to use when
         * instantiating views, and then we save our parameter context in our field Context mContext.
         *
         * @param context PresentationActivity Context ("this" in onCreate())
         */
        @SuppressWarnings("WeakerAccess")
        public DisplayListAdapter(Context context) {
            super(context, R.layout.presentation_list_item);
            mContext = context;
        }

        /**
         * Get a View that displays the data at the specified position in the data set. You can either
         * create a View manually or inflate it from an XML layout file. When the View is inflated, the
         * parent View (GridView, ListView...) will apply default layout parameters unless you use
         * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
         * to specify a root view and to prevent attachment to the root.
         *
         * @param position The position of the item within the adapter's data set of the item whose
         *                 view we want.
         * @param convertView The old view to reuse, if possible. Note: You should check that this view
         *        is non-null and of an appropriate type before using. If it is not possible to convert
         *        this view to display the correct data, this method can create a new view.
         *        Heterogeneous lists can specify their number of view types, so that this View is
         *        always of the right type (see {@link #getViewTypeCount()} and
         *        {@link #getItemViewType(int)}).
         * @param parent The parent that this view will eventually be attached to
         *
         * @return A View corresponding to the data at the specified position.
         */
        @SuppressWarnings("NullableProblems")
        @SuppressLint({"InflateParams", "DefaultLocale"})
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View v;
            if (convertView == null) {
                v = ((Activity) mContext).getLayoutInflater().inflate(
                        R.layout.presentation_list_item, null);
            } else {
                v = convertView;
            }

            final Display display = getItem(position);
            //noinspection ConstantConditions
            final int displayId = display.getDisplayId();

            DemoPresentation presentation = mActivePresentations.get(displayId);
            DemoPresentationContents contents = presentation != null ?
                    presentation.mContents : null;
            if (contents == null) {
                contents = mSavedPresentationContents.get(displayId);
            }

            CheckBox cb = (CheckBox)v.findViewById(R.id.checkbox_presentation);
            cb.setTag(display);
            cb.setOnCheckedChangeListener(PresentationActivity.this);
            cb.setChecked(contents != null);

            TextView tv = (TextView)v.findViewById(R.id.display_id);
            tv.setText(v.getContext().getResources().getString(
                    R.string.presentation_display_id_text, displayId, display.getName()));

            Button b = (Button)v.findViewById(R.id.info);
            b.setTag(display);
            b.setOnClickListener(PresentationActivity.this);

            Spinner s = (Spinner)v.findViewById(R.id.modes);
            Display.Mode[] modes = display.getSupportedModes();
            if (contents == null || modes.length == 1) {
                s.setVisibility(View.GONE);
                s.setAdapter(null);
            } else {
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(mContext,
                        android.R.layout.simple_list_item_1);
                s.setVisibility(View.VISIBLE);
                s.setAdapter(modeAdapter);
                s.setTag(display);
                s.setOnItemSelectedListener(PresentationActivity.this);

                modeAdapter.add("<default mode>");

                for (Display.Mode mode : modes) {
                    modeAdapter.add(String.format("Mode %d: %dx%d/%.1ffps",
                            mode.getModeId(),
                            mode.getPhysicalWidth(), mode.getPhysicalHeight(),
                            mode.getRefreshRate()));
                    if (contents.displayModeId == mode.getModeId()) {
                        s.setSelection(modeAdapter.getCount() - 1);
                    }
                }
            }

            return v;
        }

        /**
         * Update the contents of the display list adapter to show
         * information about all current displays.
         */
        @SuppressWarnings("WeakerAccess")
        public void updateContents() {
            clear();

            String displayCategory = getDisplayCategory();
            Display[] displays = mDisplayManager.getDisplays(displayCategory);
            addAll(displays);

            Log.d(TAG, "There are currently " + displays.length + " displays connected.");
            for (Display display : displays) {
                Log.d(TAG, "  " + display);
            }
        }

        private String getDisplayCategory() {
            return mShowAllDisplaysCheckbox.isChecked() ? null :
                DisplayManager.DISPLAY_CATEGORY_PRESENTATION;
        }
    }

    /**
     * The presentation to show on the secondary display.
     *
     * Note that the presentation display may have different metrics from the display on which
     * the main activity is showing so we must be careful to use the presentation's
     * own {@link Context} whenever we load resources.
     */
    private final class DemoPresentation extends Presentation {

        final DemoPresentationContents mContents;

        @SuppressWarnings("WeakerAccess")
        public DemoPresentation(Context context, Display display,
                                DemoPresentationContents contents) {
            super(context, display);
            mContents = contents;
        }

        /**
         * Sets the preferred display mode id for the presentation.
         */
        @SuppressWarnings("WeakerAccess")
        public void setPreferredDisplayMode(int modeId) {
            mContents.displayModeId = modeId;

            //noinspection ConstantConditions
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.preferredDisplayModeId = modeId;
            getWindow().setAttributes(params);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // Be sure to call the super class.
            super.onCreate(savedInstanceState);

            // Get the resources for the context of the presentation.
            // Notice that we are getting the resources from the context of the presentation.
            Resources r = getContext().getResources();

            // Inflate the layout.
            setContentView(R.layout.presentation_content);

            final Display display = getDisplay();
            final int displayId = display.getDisplayId();
            final int photo = mContents.photo;

            // Show a caption to describe what's going on.
            TextView text = (TextView)findViewById(R.id.text);
            text.setText(r.getString(R.string.presentation_photo_text,
                    photo, displayId, display.getName()));

            // Show a n image for visual interest.
            ImageView image = (ImageView)findViewById(R.id.image);
            //noinspection deprecation
            image.setImageDrawable(r.getDrawable(PHOTOS[photo]));

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);

            // Set the background to a random gradient.
            Point p = new Point();
            getDisplay().getSize(p);
            drawable.setGradientRadius(Math.max(p.x, p.y) / 2);
            drawable.setColors(mContents.colors);
            findViewById(android.R.id.content).setBackground(drawable);
        }
    }

    /**
     * Information about the content we want to show in the presentation.
     */
    private final static class DemoPresentationContents implements Parcelable {
        final int photo;
        final int[] colors;
        int displayModeId;

        public static final Creator<DemoPresentationContents> CREATOR =
                new Creator<DemoPresentationContents>() {
            @Override
            public DemoPresentationContents createFromParcel(Parcel in) {
                return new DemoPresentationContents(in);
            }

            @Override
            public DemoPresentationContents[] newArray(int size) {
                return new DemoPresentationContents[size];
            }
        };

        @SuppressWarnings("WeakerAccess")
        public DemoPresentationContents(int photo) {
            this.photo = photo;
            colors = new int[] {
                    ((int) (Math.random() * Integer.MAX_VALUE)) | 0xFF000000,
                    ((int) (Math.random() * Integer.MAX_VALUE)) | 0xFF000000 };
        }

        private DemoPresentationContents(Parcel in) {
            photo = in.readInt();
            colors = new int[] { in.readInt(), in.readInt() };
            displayModeId = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(photo);
            dest.writeInt(colors[0]);
            dest.writeInt(colors[1]);
            dest.writeInt(displayModeId);
        }
    }
}

