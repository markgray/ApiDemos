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
 * the Android log which you can read using <b>adb logcat</b>.
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

    /**
     * TAG used for logging
     */
    private final String TAG = "PresentationActivity";

    /**
     * Key for storing saved instance state.
     */
    private static final String PRESENTATION_KEY = "presentation";

    /**
     * The content that we want to show on the presentation.
     */
    private static final int[] PHOTOS = new int[] {
        R.drawable.frantic,
        R.drawable.photo1, R.drawable.photo2, R.drawable.photo3,
        R.drawable.photo4, R.drawable.photo5, R.drawable.photo6,
        R.drawable.sample_4,
    };

    /**
     * Used to manage the properties of attached displays
     */
    private DisplayManager mDisplayManager;
    /**
     * An ArrayAdapter<Display> to list displays
     */
    private DisplayListAdapter mDisplayListAdapter;
    /**
     * Checkbox in layout for displaying all displays
     */
    private CheckBox mShowAllDisplaysCheckbox;
    /**
     * ListView in layout in which we display our list of displays.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private ListView mListView;
    /**
     * Used to cycle through the 8 photos displayed
     */
    private int mNextImageNumber;

    /**
     * List of presentation contents indexed by displayId. This state persists so that we can
     * restore the old presentation contents when the activity is paused or resumed.
     */
    private SparseArray<DemoPresentationContents> mSavedPresentationContents;

    /**
     * List of all currently visible presentations indexed by display id.
     */
    private final SparseArray<DemoPresentation> mActivePresentations = new SparseArray<>();

    /**
     * Initialization of the Activity after it is first created.  Must at least
     * call {@link android.app.Activity#setContentView setContentView()} to
     * describe what is to be displayed in the screen.
     * <p>
     * First we call through to our super's implementation of {@code onCreate}. Then if our parameter
     * {@code savedInstanceState} is not null (our Activity is being restarted after previously being
     * shut down) we retrieve the value of {@code SparseArray<DemoPresentation> mActivePresentations}
     * that we saved when our callback {@code onSaveInstanceState} was called, and if it is null we
     * simply set {@code mActivePresentations} to a new instance of{@code  SparseArray<DemoPresentation>}.
     * Next we set our field {@code DisplayManager mDisplayManager} to the handle for the DISPLAY_SERVICE
     * system-level service. We set our content view to our layout file R.layout.presentation_activity,
     * find the CheckBox R.id.show_all_displays ("Show all displays") save a reference in the field
     * {@code CheckBox mShowAllDisplaysCheckbox} and set its {@code OnCheckedListener} to "this". We
     * set our field {@code DisplayListAdapter mDisplayListAdapter} to a new instance of our class
     * {@code DisplayListAdapter} using "this" as its Context, find the {@code ListView} R.id.display_list
     * in our layout, save a reference in our field {@code ListView mListView}, and set the adapter of
     * {@code mListView} to {@code mDisplayListAdapter}.
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
        mShowAllDisplaysCheckbox = findViewById(R.id.show_all_displays);
        mShowAllDisplaysCheckbox.setOnCheckedChangeListener(this);

        // Set up the list of displays.
        mDisplayListAdapter = new DisplayListAdapter(this);
        mListView = findViewById(R.id.display_list);
        mListView.setAdapter(mDisplayListAdapter);
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or
     * {@link #onPause}, for your activity to start interacting with the user.
     * <p>
     * First we call through to our super's implementation of {@code onResume}. Then we update the
     * contents of the display list adapter to show information about all current displays by calling
     * our method {@code DisplayListAdapter.updateContents()}. Then we restore presentations from
     * before the activity was paused by first finding the number of Display's known to the
     * {@code mDisplayListAdapter} adapter, then looping though those Display's, fetching the
     * {@code Display display} from the adapter and using the display id to index into the
     * {@code SparseArray mSavedPresentationContents} to fetch the {@code DemoPresentationContents}
     * contents for that Display and if the "contents" is not null we show that "contents" on the
     * specified display. When done restoring the presentations we remove all key-value mappings from
     * the {@code SparseArray mSavedPresentationContents}. Finally we register our display listener
     * {@code DisplayManager.DisplayListener mDisplayListener} to receive notifications about when
     * displays are added, removed or changed.
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
     * <p>
     * First we call through to our super's implementation of {@code onPause}. Then we unregister
     * our display listener {@code DisplayManager.DisplayListener mDisplayListener}. Then we loop
     * through {@code SparseArray<DemoPresentation> mActivePresentations} (our list of all currently
     * visible presentations indexed by display id) fetching the {@code DemoPresentation presentation}
     * stored there and the {@code int displayId} it was stored under and use these to add a mapping
     * from the {@code displayId} to {@code presentation}. Then we call {@code presentation.dismiss()}
     * to dismiss that presentation. When done saving all the presentations to {@code mSavedPresentationContents}
     * and dismissing them we remove all key-value mappings the {@code SparseArray mActivePresentations}.
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
     * <p>
     * First we call through to our super's implementation of {@code onSaveInstanceState}, then we
     * insert our {@code SparseArray<DemoPresentationContents> mSavedPresentationContents} into the
     * mapping of the {@code Bundle outState} using the key PRESENTATION_KEY ("presentation").
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
     * <p>
     * First we retrieve the {@code displayId} from the {@code Display display} passed us, if there
     * is already a {@code DemoPresentation} in {@code mActivePresentations} for that display we
     * return having done nothing. Otherwise we create a {@code DemoPresentation presentation} for
     * that {@code Display display} and {@code DemoPresentationContents contents}, show the presentation
     * on that Display, set the {@code OnDismissListener} of the presentation to {@code mOnDismissListener},
     * and finally add the presentation to our {@code SparseArray<DemoPresentation> mActivePresentations}
     * under the key {@code displayId}.
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
     * <p>
     * First we fetch the display Id of the {@code Display display} into {@code displayId}, then we
     * retrieve the {@code DemoPresentation presentation} stored under the key {@code displayId} in
     * {@code SparseArray<DemoPresentation> mActivePresentations}, and if there is no presentation
     * recorded there we return having done nothing. If there is a presentation on that {@code displayId}
     * we {@code dismiss()} it and delete the presentation stored under the key {@code displayId} in
     * the {@code mActivePresentations} array.
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
     * <p>
     * First we fetch the {@code int displayId} display Id from the {@code Display display}, and if
     * there is no {@code DemoPresentation presentation} stored in the {@code SparseArray} referenced
     * by {@code mActivePresentations} under that key we return having done nothing. Otherwise we call
     * {@code setPreferredDisplayMode} to set the preferred display mode of the presentation being
     * displayed on that screen to the {@code int displayModeId} passed to us.
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
     * Returns the {@code mNextImageNumber} to use to index into the {@code int PHOTOS[]} array of
     * resource id's in order choose a photo resource id to display, then performs a modular increment
     * of {@code mNextImageNumber} modulus {@code PHOTOS.length} in order to wrap around to 0 when all
     * photos have been used.
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
     * <p>
     * First we check if it was the "show all displays checkbox" whose checked state changed and if
     * it is we call {@code mDisplayListAdapter.updateContents()} to update the contents of the display
     * list adapter to show information about all current displays. Otherwise one of the items in the
     * list of displays has been clicked so we retrieve the {@code CompoundButton buttonView}'s tag
     * to {@code Display display} and if the checkbox is now checked (our parameter {@code isChecked}
     * is true) we create a new {@code DemoPresentationContents contents} from the next photo due to
     * be displayed (as determined by our method {@code getNextPhoto()}) and show this on the display
     * {@code display}, and if it is not checked we hide the presentation on that display by calling
     * {@code hidePresentation(display)}. In either case we then call {@code mDisplayListAdapter.updateContents()}
     * to update the contents of the display list adapter.
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
     * display. First we retrieve the context of the Button clicked into {@code Context context},
     * then we create {@code AlertDialog.Builder builder} using this context. We retrieve the Display
     * the Button is associated with to {@code Display display} by calling {@code getTag()} on the
     * Button. We initialize {@code Resources r} with a Resources instance for the application's
     * package. We then use our {@code AlertDialog.Builder builder} to create an AlertDialog alert
     * with the title "Display #? Info" (with the ? replaced by the display Id), a message displaying
     * the information returned by {@code Display.toString()}, and a neutral Button with the text "OK"
     * which dismisses the {@code AlertDialog} when clicked. Finally we show this {@code AlertDialog alert}.
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
                             * Called when the "OK" Button is clicked, we simply dismiss the dialog.
                             *
                             * @param dialog The dialog that received the click.
                             * @param which The button that was clicked (e.g.
                             *        {@link DialogInterface#BUTTON1}) or
                             *        the position of the item clicked.
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
     * Called when a display mode has been selected. We retrieve the tag from {@code AdapterView parent}
     * into {@code Display display}, retrieve the supported modes of this display into the array
     * {@code Display.Mode[] modes}, then we set the display mode of the Presentation on the specified
     * display (if it is already shown) to the mode[] of the position of the view in the adapter. (Only
     * used for Display's that have more than one mode.)
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
     * Called when a display mode has been unselected. We retrieve the tag from {@code AdapterView parent}
     * into {@code Display display} and set the display mode of the Display to 0 (no preference) (Only
     * used for Display's that have more than one mode.)
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
     * <p>
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
                 * <p>
                 * We simply instruct our {@code DisplayListAdapter mDisplayListAdapter} to update
                 * its contents to show information about all current displays.
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
                 * <p>
                 * We simply instruct our {@code DisplayListAdapter mDisplayListAdapter} to update
                 * its contents to show information about all current displays.
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
                 * <p>
                 * We simply instruct our {@code DisplayListAdapter mDisplayListAdapter} to update
                 * its contents to show information about all current displays.
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
                 * parameter to {@code DemoPresentation presentation}, retrieve the {@code displayId}
                 * display id from the Display used for the {@code presentation}, remove the value
                 * stored under the {@code displayId} key from {@code mActivePresentations}, and then
                 * notify our {@code DisplayListAdapter mDisplayListAdapter} that the underlying data
                 * has been changed and any View reflecting the data set should refresh itself.
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
     * Display list adapter.
     * Shows information about all displays.
     */
    private final class DisplayListAdapter extends ArrayAdapter<Display> {
        /**
         * {@code Context} passed to our constructor.
         */
        final Context mContext;

        /**
         * Initializes this instance of {@code DisplayListAdapter} by first calling through to our
         * super's constructor using the layout file R.layout.presentation_list_item for it to use when
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
         * <p>
         * First we check if the {@code View convertView} is null (View is not being recycled) and
         * if so we inflate the layout file for our list item (R.layout.presentation_list_item) into
         * {@code View v}, and if convertView is not null we set View v to convertView (to reuse the
         * old recycled View). Next we call {@code ArrayAdapter.getItem(position)} to get the get the
         * Display associated with the specified position in the data set and save it in our variable
         * {@code Display display}. We retrieve the display Id from {@code display} and save it as
         * {@code int displayId}. We retrieve to our variable {@code DemoPresentation presentation}
         * the {@code DemoPresentation} stored using the key {@code displayId} in our list of visible
         * presentations {@code SparseArray<DemoPresentation> mActivePresentations}. If there is a
         * {@code DemoPresentation} stored there ({@code presentation} != null) our variable
         * {@code DemoPresentationContents contents} is set to the {@code mContents} field of
         * {@code presentation}, otherwise it is set to null. If {@code contents} has been set to
         * null by the above statement, we set {@code contents} to the value stored under the
         * {@code displayId} key in the list of persistent presentation contents (saved in
         * {@code onSaveInstanceState} and restored in {@code onCreate}) in our array
         * {@code SparseArray<DemoPresentationContents> mSavedPresentationContents}. Next we locate
         * the CheckBox R.id.checkbox_presentation in our item {@code View v} and save it in
         * {@code CheckBox cb}. We set the tag of {@code cb} to {@code Display display}, set the
         * {@code OnCheckedChangeListener} to "this", and set the checked state of {@code cb} if
         * {@code contents} is not null, unchecked otherwise. Next we find the {@code TextView tv}
         * (R.id.display_id) in {@code v} and set its text to a formatted String containing the
         * {@code displayId} and the name of the Display. We locate Button b (R.id.info "INFO" Button
         * in {@code v}), set its tag to {@code Display display}, and its {@code OnClickListener}
         * to "this". We locate {@code Spinner s} (R.id.modes in {@code v}) and if there is no content,
         * or the Display only has one mode we set the visibility of "s" to "GONE" and set the adapter
         * of s to null. Otherwise we create {@code ArrayAdapter<String> modeAdapter} using {@code mContext}
         * and the system layout file android.R.layout.simple_list_item_1, set the visibility of {@code s}
         * to "VISIBLE", set the adapter of {@code s} to {@code modeAdapter}, set the tag of {@code s} to
         * {@code Display display}, and set "this" as the {@code OnItemSelectedListener} for {@code s}.
         * Then we load up {@code modeAdapter} with "<default mode>" as the 0'th entry, and every
         * {@code Display.Mode mode} in the array of supported modes for the Display we fetched earlier
         * to {@code Display.Mode[] modes} (using a formatted text String which contains the mode Id,
         * physical width, physical height, and refresh rate for the mode). While adding the supported
         * modes we check to see if the mode Id matches the mode Id of the current contents, and if so
         * we set that mode in the adapter to be the selected one. Finally we return the {@code View v}
         * we have created (or reused) and configured.
         *
         * @param position The position of the item within the adapter's data set of the item whose
         *        view we want.
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

            CheckBox cb = v.findViewById(R.id.checkbox_presentation);
            cb.setTag(display);
            cb.setOnCheckedChangeListener(PresentationActivity.this);
            cb.setChecked(contents != null);

            TextView tv = v.findViewById(R.id.display_id);
            tv.setText(v.getContext().getResources().getString(
                    R.string.presentation_display_id_text, displayId, display.getName()));

            Button b = v.findViewById(R.id.info);
            b.setTag(display);
            b.setOnClickListener(PresentationActivity.this);

            Spinner s = v.findViewById(R.id.modes);
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
         * Update the contents of the display list adapter to show information about all current
         * displays. First we remove all elements from the current list by calling the method
         * {@code ArrayAdapter.clear()}. Then we initialize {@code String displayCategory} with the
         * value our method {@code getDisplayCategory()} returns. That method checks whether the
         * {@code CheckBox mShowAllDisplaysCheckbox} ("Show all displays") is checked and if so
         * returns null, otherwise it returns the string DisplayManager.DISPLAY_CATEGORY_PRESENTATION
         * ("android.hardware.display.category.PRESENTATION"). We initialize {@code Display[] displays}
         * with the Display's returned by the {@code getDisplays} method of {@code DisplayManager mDisplayManager}
         * returns for {@code displayCategory}. If {@code displayCategory} was null all Display's will
         * be returned, if it was DISPLAY_CATEGORY_PRESENTATION {@code getDisplays} will return only
         * the presentation secondary Display's attached. Finally we add all the Display's in
         * {@code Display[] displays} to our ArrayAdapter.
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

        /**
         * This method just checks whether the {@code CheckBox mShowAllDisplaysCheckbox} ("Show all
         * displays") is checked, and if so returns null so that {@code DisplayManager.getDisplays}
         * will return all Display's. If it is not checked it returns the String DISPLAY_CATEGORY_PRESENTATION
         * so that {@code getDisplays} will return only attached presentation Display's.
         *
         * @return If the "Show all displays" checkbox is not checked we return the String
         *         DISPLAY_CATEGORY_PRESENTATION, if checked we return null
         */
        private String getDisplayCategory() {
            return mShowAllDisplaysCheckbox.isChecked() ? null :
                DisplayManager.DISPLAY_CATEGORY_PRESENTATION;
        }
    }

    /**
     * The presentation to show on the secondary display.
     * <p>
     * Note that the presentation display may have different metrics from the display on which
     * the main activity is showing so we must be careful to use the presentation's
     * own {@link Context} whenever we load resources.
     */
    private final class DemoPresentation extends Presentation {

        /**
         * Information about the content we are showing in the presentation.
         */
        final DemoPresentationContents mContents;

        /**
         * Initializes this instance by saving the parameter {@code contents} in our field
         * {@code DemoPresentationContents mContents} (after calling our super's constructor).
         *
         * @param context The context of the application that is showing the presentation. The
         *        presentation will create its own context (see getContext()) based on this context
         *        and information about the associated display.
         * @param display The display to which the presentation should be attached.
         * @param contents Information about the content we want to show in the presentation.
         */
        @SuppressWarnings("WeakerAccess")
        public DemoPresentation(Context context, Display display, DemoPresentationContents contents) {
            super(context, display);
            mContents = contents;
        }

        /**
         * Sets the preferred display mode id for the presentation. First we store the parameter {@code modeId}
         * in the {@code displayModeId} field of our field {@code DemoPresentationContents mContents}, then
         * we retrieve the WindowManager.LayoutParams window attributes for the current window to
         * {@code WindowManager.LayoutParams params}, set the field {@code displayModeId} of {@code params}
         * to the {@code modeId} parameter passed us, and then set the window attributes for the current
         * window to the modified {@code WindowManager.LayoutParams params}.
         *
         * @param modeId Id of the preferred display mode for the window. This must be one of the
         *        supported modes obtained for the display(s) the window is on. A value of 0 means
         *        no preference.
         */
        @SuppressWarnings("WeakerAccess")
        public void setPreferredDisplayMode(int modeId) {
            mContents.displayModeId = modeId;

            //noinspection ConstantConditions
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.preferredDisplayModeId = modeId;
            getWindow().setAttributes(params);
        }

        /**
         * We initialize this instance of our DemoPresentation dialog in this method. First we call
         * through to our super's implementation of {@code onCreate}, then we retrieve the resources
         * for the context of the presentation to our variable {@code Resources r}, set our content
         * view to our layout file R.layout.presentation_content, retrieve the Display that this
         * presentation appears on to our variable {@code Display display}, set {@code int displayId}
         * to the display id of {@code Display display}, and set {@code int photo} to the photo field
         * of our field {@code DemoPresentationContents mContents}. Next we locate {@code TextView text}
         * in our layout (R.id.text) and set the text to a formatted String (R.string.presentation_photo_text)
         * containing the photo number, display Id, and the Display name. We locate {@code ImageView image}
         * in our layout (R.id.image) and set the {@code ImageView} to display the {@code photo} entry
         * in our {@code int[] PHOTOS} array of drawable resource id's. Then in order to set the background
         * to a random gradient we create {@code GradientDrawable drawable}, set its shape to RECTANGLE,
         * and set the type of gradient to RADIAL_GRADIENT. We create {@code Point p} and set it to the
         * display size in pixels, then use it to set the radius of our {@code GradientDrawable drawable}
         * to half of whichever dimension of the display was largest. We set the colors of {@code drawable}
         * to the {@code DemoPresentationContents mContents} field {@code int[] colors} (array of random
         * colors), and finally we set the background of the root element of our View (android.R.id.content)
         * to GradientDrawable drawable.
         *
         * @param savedInstanceState holds the result from the most recent call to
         *        {@link #onSaveInstanceState}, or null if this is the first time.
         */
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
            TextView text = findViewById(R.id.text);
            text.setText(r.getString(R.string.presentation_photo_text,
                    photo, displayId, display.getName()));

            // Show a n image for visual interest.
            ImageView image = findViewById(R.id.image);
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
        /**
         * Index into int[] PHOTOS array of Drawable resource id's for our photo
         */
        final int photo;
        /**
         * Array of random colors used for the background
         */
        final int[] colors;
        /**
         * Display mode Id we are to use.
         */
        int displayModeId;

        /**
         * Interface that must be implemented and provided as a public CREATOR field that generates
         * instances of your Parcelable class from a Parcel.
         */
        public static final Creator<DemoPresentationContents> CREATOR =
                new Creator<DemoPresentationContents>() {
                    /**
                     * Create a new instance of the Parcelable class, instantiating it
                     * from the given Parcel whose data had previously been written by
                     * {@link Parcelable#writeToParcel Parcelable.writeToParcel()}.
                     * <p>
                     * We simply use the {@code Parcel in} to call our constructor that uses a
                     * {@code Parcel} argument.
                     *
                     * @param in The Parcel to read the object's data from.
                     * @return Returns a new instance of the Parcelable class DemoPresentationContents
                     */
                    @Override
                    public DemoPresentationContents createFromParcel(Parcel in) {
                        return new DemoPresentationContents(in);
                    }
                    /**
                     * Create a new array of the Parcelable class DemoPresentationContents
                     *
                     * @param size Size of the array.
                     * @return Returns an array of the Parcelable class, with every entry
                     *         initialized to null.
                     */
                    @Override
                    public DemoPresentationContents[] newArray(int size) {
                        return new DemoPresentationContents[size];
                    }
                };

        /**
         * Constructs a DemoPresentationContents instance using the parameter {@code photo} as its
         * field {@code int photo}, and initializes its {@code int [] colors} array to two random
         * colors to be used when creating the background gradient for this instance.
         *
         * @param photo index into the {@code int[] PHOTOS} array of Drawable resource Ids
         */
        @SuppressWarnings("WeakerAccess")
        public DemoPresentationContents(int photo) {
            this.photo = photo;
            colors = new int[] {
                    ((int) (Math.random() * Integer.MAX_VALUE)) | 0xFF000000,
                    ((int) (Math.random() * Integer.MAX_VALUE)) | 0xFF000000 };
        }

        /**
         * Constructs a DemoPresentationContents instance by reading the parameter {@code Parcel in}
         * which was previously written to by {@code writeToParcel}. Our {@code Parcel} should consist
         * of 4 ints which we read in order to set the values of our fields {@code photo}, {@code colors[0]},
         * {@code colors[1]}, and {@code displayModeId}.
         *
         * @param in The Parcel to read the object's data from.
         */
        private DemoPresentationContents(Parcel in) {
            photo = in.readInt();
            colors = new int[] { in.readInt(), in.readInt() };
            displayModeId = in.readInt();
        }

        /**
         * Part of the {@code Parcelable} interface. Describe the kinds of special objects contained
         * in this  Parcelable's marshalled representation. We simply return 0.
         *
         * @return a bitmask indicating the set of special object types marshalled by the Parcelable.
         */
        @Override
        public int describeContents() {
            return 0;
        }

        /**
         * Part of the {@code Parcelable} interface. Flatten this object into a {@code Parcel}. We
         * simply write the four int's of our fields: {@code photo}, {@code colors[0]}, {@code colors[1]},
         * and {@code displayModeId} into the {@code Parcel dest} at the current {@code dataPosition()},
         * growing {@code dataCapacity()} if needed.
         *
         * @param dest The Parcel in which the object should be written.
         * @param flags Additional flags about how the object should be written.
         *        May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
         */
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(photo);
            dest.writeInt(colors[0]);
            dest.writeInt(colors[1]);
            dest.writeInt(displayModeId);
        }
    }
}

