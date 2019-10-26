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

package com.example.android.apis.app

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.app.Presentation
import android.content.Context
import android.content.DialogInterface
import android.graphics.Point
import android.graphics.drawable.GradientDrawable
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.util.SparseArray
import android.view.Display
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.CompoundButton.OnCheckedChangeListener

import androidx.appcompat.app.AppCompatActivity

import com.example.android.apis.R
import kotlin.math.max


/**
 * <h3>Presentation Activity</h3>
 *
 *
 *
 * This demonstrates how to create an activity that shows some content
 * on a secondary display using a [Presentation].
 *
 *
 * The activity uses the [DisplayManager] API to enumerate displays.
 * When the user selects a display, the activity opens a [Presentation]
 * on that display.  We show a different photograph in each presentation
 * on a unique background along with a label describing the display.
 * We also write information about displays and display-related events to
 * the Android log which you can read using **adb logcat**.
 *
 *
 * You can try this out using an HDMI or Wifi display or by using the
 * "Simulate secondary displays" feature in Development Settings to create a few
 * simulated secondary displays.  Each display will appear in the list along with a
 * checkbox to show a presentation on that display.
 *
 *
 * See also the [PresentationWithMediaRouterActivity] sample which
 * uses the media router to automatically select a secondary display
 * on which to show content based on the currently selected route.
 *
 */
@Suppress("UNUSED_ANONYMOUS_PARAMETER")
@TargetApi(Build.VERSION_CODES.M)
class PresentationActivity
    : AppCompatActivity(), OnCheckedChangeListener, OnClickListener, OnItemSelectedListener {

    /**
     * Used to manage the properties of attached displays
     */
    private var mDisplayManager: DisplayManager? = null
    /**
     * An ArrayAdapter<Display> to list displays
    </Display> */
    private var mDisplayListAdapter: DisplayListAdapter? = null
    /**
     * Checkbox in layout for displaying all displays
     */
    private var mShowAllDisplaysCheckbox: CheckBox? = null
    /**
     * ListView in layout in which we display our list of displays.
     */
    private var mListView: ListView? = null
    /**
     * Used to cycle through the 8 photos displayed
     */
    private var mNextImageNumber: Int = 0

    /**
     * List of presentation contents indexed by displayId. This state persists so that we can
     * restore the old presentation contents when the activity is paused or resumed.
     */
    private var mSavedPresentationContents: SparseArray<DemoPresentationContents>? = null

    /**
     * List of all currently visible presentations indexed by display id.
     */
    private val mActivePresentations = SparseArray<DemoPresentation>()

    /**
     * Returns the `mNextImageNumber` to use to index into the `int PHOTOS[]` array of
     * resource id's in order choose a photo resource id to display, then performs a modular increment
     * of `mNextImageNumber` modulus `PHOTOS.length` in order to wrap around to 0 when all
     * photos have been used.
     *
     * @return index to int PHOTOS[] array of resource id's that is next to be displayed
     */
    private val nextPhoto: Int
        get() {
            val photo = mNextImageNumber
            mNextImageNumber = (mNextImageNumber + 1) % PHOTOS.size
            return photo
        }

    /**
     * Listens for displays to be added, changed or removed.
     * We use it to update the list and show a new [Presentation] when a
     * display is connected.
     *
     *
     * Note that we don't bother dismissing the [Presentation] when a
     * display is removed, although we could.  The presentation API takes care
     * of doing that automatically for us.
     */
    private val mDisplayListener = object : DisplayManager.DisplayListener {
        /**
         * Called whenever a logical display has been added to the system.
         * Use [DisplayManager.getDisplay] to get more information about
         * the display.
         *
         *
         * We simply instruct our `DisplayListAdapter mDisplayListAdapter` to update
         * its contents to show information about all current displays.
         *
         * @param displayId The id of the logical display that was added.
         */
        override fun onDisplayAdded(displayId: Int) {
            Log.d(TAG, "Display #$displayId added.")
            mDisplayListAdapter!!.updateContents()
        }

        /**
         * Called whenever the properties of a logical display have changed.
         *
         *
         * We simply instruct our `DisplayListAdapter mDisplayListAdapter` to update
         * its contents to show information about all current displays.
         *
         * @param displayId The id of the logical display that changed.
         */
        override fun onDisplayChanged(displayId: Int) {
            Log.d(TAG, "Display #$displayId changed.")
            mDisplayListAdapter!!.updateContents()
        }

        /**
         * Called whenever a logical display has been removed from the system.
         *
         *
         * We simply instruct our `DisplayListAdapter mDisplayListAdapter` to update
         * its contents to show information about all current displays.
         *
         * @param displayId The id of the logical display that was removed.
         */
        override fun onDisplayRemoved(displayId: Int) {
            Log.d(TAG, "Display #$displayId removed.")
            mDisplayListAdapter!!.updateContents()
        }
    }

    /**
     * This callback will be invoked when the dialog is dismissed. We cast our dialog
     * parameter to `DemoPresentation presentation`, retrieve the `displayId`
     * display id from the Display used for the `presentation`, remove the value
     * stored under the `displayId` key from `mActivePresentations`, and then
     * notify our `DisplayListAdapter mDisplayListAdapter` that the underlying data
     * has been changed and any View reflecting the data set should refresh itself.
     *
     * Parameter: dialog The dialog that was dismissed
     */
    private val mOnDismissListener = DialogInterface.OnDismissListener { dialog ->

        if(dialog == null) return@OnDismissListener
        val presentation = dialog as DemoPresentation
        val displayId = presentation.display.displayId
        Log.d(TAG, "Presentation on display #$displayId was dismissed.")
        mActivePresentations.delete(displayId)
        mDisplayListAdapter!!.notifyDataSetChanged()
    }

    /**
     * Initialization of the Activity after it is first created.  Must at least
     * call [setContentView()][android.app.Activity.setContentView] to
     * describe what is to be displayed in the screen.
     *
     * First we call through to our super's implementation of `onCreate`. Then if our parameter
     * `savedInstanceState` is not null (our Activity is being restarted after previously being
     * shut down) we retrieve the value of `SparseArray<DemoPresentation> mActivePresentations`
     * that we saved when our callback `onSaveInstanceState` was called, and if it is null we
     * simply set `mActivePresentations` to a new instance of`SparseArray<DemoPresentation>`.
     * Next we set our field `DisplayManager mDisplayManager` to the handle for the DISPLAY_SERVICE
     * system-level service. We set our content view to our layout file R.layout.presentation_activity,
     * find the CheckBox R.id.show_all_displays ("Show all displays") save a reference in the field
     * `CheckBox mShowAllDisplaysCheckbox` and set its `OnCheckedListener` to "this". We
     * set our field `DisplayListAdapter mDisplayListAdapter` to a new instance of our class
     * `DisplayListAdapter` using "this" as its Context, find the `ListView` R.id.display_list
     * in our layout, save a reference in our field `ListView mListView`, and set the adapter of
     * `mListView` to `mDisplayListAdapter`.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [.onSaveInstanceState].  ***Note: Otherwise it is null.***
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState)

        // Restore saved instance state.
        mSavedPresentationContents = if (savedInstanceState != null) {
            savedInstanceState.getSparseParcelableArray(PRESENTATION_KEY)
        } else {
            SparseArray()
        }

        // Get the display manager service.
        mDisplayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

        // See assets/res/any/layout/presentation_activity.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.presentation_activity)

        // Set up checkbox to toggle between showing all displays or only presentation displays.
        mShowAllDisplaysCheckbox = findViewById(R.id.show_all_displays)
        mShowAllDisplaysCheckbox!!.setOnCheckedChangeListener(this)

        // Set up the list of displays.
        mDisplayListAdapter = DisplayListAdapter(this)
        mListView = findViewById(R.id.display_list)
        mListView!!.adapter = mDisplayListAdapter
    }

    /**
     * Called after [.onRestoreInstanceState], [.onRestart], or
     * [.onPause], for your activity to start interacting with the user.
     *
     *
     * First we call through to our super's implementation of `onResume`. Then we update the
     * contents of the display list adapter to show information about all current displays by calling
     * our method `DisplayListAdapter.updateContents()`. Then we restore presentations from
     * before the activity was paused by first finding the number of Display's known to the
     * `mDisplayListAdapter` adapter, then looping though those Display's, fetching the
     * `Display display` from the adapter and using the display id to index into the
     * `SparseArray mSavedPresentationContents` to fetch the `DemoPresentationContents`
     * contents for that Display and if the "contents" is not null we show that "contents" on the
     * specified display. When done restoring the presentations we remove all key-value mappings from
     * the `SparseArray mSavedPresentationContents`. Finally we register our display listener
     * `DisplayManager.DisplayListener mDisplayListener` to receive notifications about when
     * displays are added, removed or changed.
     */
    override fun onResume() {
        // Be sure to call the super class.
        super.onResume()

        // Update our list of displays on resume.
        mDisplayListAdapter!!.updateContents()

        // Restore presentations from before the activity was paused.
        val numDisplays = mDisplayListAdapter!!.count
        for (i in 0 until numDisplays) {
            val display = mDisplayListAdapter!!.getItem(i)

            val contents = mSavedPresentationContents!!.get(display!!.displayId)
            if (contents != null) {
                showPresentation(display, contents)
            }
        }
        mSavedPresentationContents!!.clear()

        // Register to receive events from the display manager.
        mDisplayManager!!.registerDisplayListener(mDisplayListener, null)
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed.  The counterpart to
     * [.onResume].
     *
     *
     * First we call through to our super's implementation of `onPause`. Then we unregister
     * our display listener `DisplayManager.DisplayListener mDisplayListener`. Then we loop
     * through `SparseArray<DemoPresentation> mActivePresentations` (our list of all currently
     * visible presentations indexed by display id) fetching the `DemoPresentation presentation`
     * stored there and the `int displayId` it was stored under and use these to add a mapping
     * from the `displayId` to `presentation`. Then we call `presentation.dismiss()`
     * to dismiss that presentation. When done saving all the presentations to `mSavedPresentationContents`
     * and dismissing them we remove all key-value mappings the `SparseArray mActivePresentations`.
     */
    override fun onPause() {
        // Be sure to call the super class.
        super.onPause()

        // Unregister from the display manager.
        mDisplayManager!!.unregisterDisplayListener(mDisplayListener)

        // Dismiss all of our presentations but remember their contents.
        Log.d(TAG, "Activity is being paused.  Dismissing all active presentation.")
        for (i in 0 until mActivePresentations.size()) {
            val presentation = mActivePresentations.valueAt(i)
            val displayId = mActivePresentations.keyAt(i)
            mSavedPresentationContents!!.put(displayId, presentation.mContents)
            presentation.dismiss()
        }
        mActivePresentations.clear()
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in [.onCreate] or
     * [.onRestoreInstanceState] (the [Bundle] populated by this method
     * will be passed to both).
     *
     *
     * First we call through to our super's implementation of `onSaveInstanceState`, then we
     * insert our `SparseArray<DemoPresentationContents> mSavedPresentationContents` into the
     * mapping of the `Bundle outState` using the key PRESENTATION_KEY ("presentation").
     *
     * @param outState Bundle in which to place your saved state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        // Be sure to call the super class.
        super.onSaveInstanceState(outState)
        outState.putSparseParcelableArray(PRESENTATION_KEY, mSavedPresentationContents)
    }

    /**
     * Shows a [Presentation] on the specified display.
     *
     *
     * First we retrieve the `displayId` from the `Display display` passed us, if there
     * is already a `DemoPresentation` in `mActivePresentations` for that display we
     * return having done nothing. Otherwise we create a `DemoPresentation presentation` for
     * that `Display display` and `DemoPresentationContents contents`, show the presentation
     * on that Display, set the `OnDismissListener` of the presentation to `mOnDismissListener`,
     * and finally add the presentation to our `SparseArray<DemoPresentation> mActivePresentations`
     * under the key `displayId`.
     *
     * @param display Display to show the DemoPresentationContents contents on
     * @param contents Information about the content we want to show in the presentation.
     */
    private fun showPresentation(display: Display, contents: DemoPresentationContents) {
        val displayId = display.displayId
        if (mActivePresentations.get(displayId) != null) {
            return
        }

        Log.d(TAG, "Showing presentation photo #" + contents.photo
                + " on display #" + displayId + ".")

        val presentation = DemoPresentation(this, display, contents)
        presentation.show()
        presentation.setOnDismissListener(mOnDismissListener)
        mActivePresentations.put(displayId, presentation)
    }

    /**
     * Hides a [Presentation] on the specified display.
     *
     *
     * First we fetch the display Id of the `Display display` into `displayId`, then we
     * retrieve the `DemoPresentation presentation` stored under the key `displayId` in
     * `SparseArray<DemoPresentation> mActivePresentations`, and if there is no presentation
     * recorded there we return having done nothing. If there is a presentation on that `displayId`
     * we `dismiss()` it and delete the presentation stored under the key `displayId` in
     * the `mActivePresentations` array.
     *
     * @param display Display whose presentation we want to hide.
     */
    private fun hidePresentation(display: Display) {
        val displayId = display.displayId
        val presentation = mActivePresentations.get(displayId) ?: return

        Log.d(TAG, "Dismissing presentation on display #$displayId.")

        presentation.dismiss()
        mActivePresentations.delete(displayId)
    }

    /**
     * Sets the display mode of the [Presentation] on the specified display
     * if it is already shown.
     *
     *
     * First we fetch the `int displayId` display Id from the `Display display`, and if
     * there is no `DemoPresentation presentation` stored in the `SparseArray` referenced
     * by `mActivePresentations` under that key we return having done nothing. Otherwise we call
     * `setPreferredDisplayMode` to set the preferred display mode of the presentation being
     * displayed on that screen to the `int displayModeId` passed to us.
     *
     * @param display Display to set the display mode on
     * @param displayModeId display mode Id to set the display's Display.Mode to.
     */
    private fun setPresentationDisplayMode(display: Display, displayModeId: Int) {
        val displayId = display.displayId
        val presentation = mActivePresentations.get(displayId) ?: return

        presentation.setPreferredDisplayMode(displayModeId)
    }

    /**
     * Called when the show all displays checkbox is toggled or when an item in the list of
     * displays is checked or unchecked.
     *
     *
     * First we check if it was the "show all displays checkbox" whose checked state changed and if
     * it is we call `mDisplayListAdapter.updateContents()` to update the contents of the display
     * list adapter to show information about all current displays. Otherwise one of the items in the
     * list of displays has been clicked so we retrieve the `CompoundButton buttonView`'s tag
     * to `Display display` and if the checkbox is now checked (our parameter `isChecked`
     * is true) we create a new `DemoPresentationContents contents` from the next photo due to
     * be displayed (as determined by our method `getNextPhoto()`) and show this on the display
     * `display`, and if it is not checked we hide the presentation on that display by calling
     * `hidePresentation(display)`. In either case we then call `mDisplayListAdapter.updateContents()`
     * to update the contents of the display list adapter.
     *
     * @param buttonView The compound button view whose state has changed.
     * @param isChecked The new checked state of buttonView.
     */
    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView === mShowAllDisplaysCheckbox) {
            // Show all displays checkbox was toggled.
            mDisplayListAdapter!!.updateContents()
        } else {
            // Display item checkbox was toggled.
            val display = buttonView.tag as Display
            if (isChecked) {
                val contents = DemoPresentationContents(nextPhoto)
                showPresentation(display, contents)
            } else {
                hidePresentation(display)
            }
            mDisplayListAdapter!!.updateContents()
        }
    }

    /**
     * Called when the Info button next to a display is clicked to show information about the
     * display. First we retrieve the context of the Button clicked into `Context context`,
     * then we create `AlertDialog.Builder builder` using this context. We retrieve the Display
     * the Button is associated with to `Display display` by calling `getTag()` on the
     * Button. We initialize `Resources r` with a Resources instance for the application's
     * package. We then use our `AlertDialog.Builder builder` to create an AlertDialog alert
     * with the title "Display #? Info" (with the ? replaced by the display Id), a message displaying
     * the information returned by `Display.toString()`, and a neutral Button with the text "OK"
     * which dismisses the `AlertDialog` when clicked. Finally we show this `AlertDialog alert`.
     *
     * @param v Button which was clicked
     */
    override fun onClick(v: View) {
        val context = v.context
        val builder = AlertDialog.Builder(context)
        val display = v.tag as Display
        val r = context.resources
        val alert = builder
                .setTitle(r.getString(
                        R.string.presentation_alert_info_text, display.displayId))
                .setMessage(display.toString())
                .setNeutralButton(R.string.presentation_alert_dismiss_text
                ) { dialog, which ->
                    /**
                     * Called when the "OK" Button is clicked, we simply dismiss the dialog.
                     *
                     * @param dialog The dialog that received the click.
                     * @param which The button that was clicked (e.g.
                     * [DialogInterface.BUTTON1]) or
                     * the position of the item clicked.
                     */
                    dialog.dismiss()
                }
                .create()
        alert.show()
    }

    /**
     * Called when a display mode has been selected. We retrieve the tag from `AdapterView parent`
     * into `Display display`, retrieve the supported modes of this display into the array
     * `Display.Mode[] modes`, then we set the display mode of the Presentation on the specified
     * display (if it is already shown) to the mode[] of the position of the view in the adapter. (Only
     * used for Display's that have more than one mode.)
     *
     * @param parent The AdapterView where the selection happened
     * @param view The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id The row id of the item that is selected
     */
    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val display = parent.tag as Display
        val modes = display.supportedModes
        setPresentationDisplayMode(display, if (position >= 1 && position <= modes.size)
            modes[position - 1].modeId
        else
            0)
    }

    /**
     * Called when a display mode has been unselected. We retrieve the tag from `AdapterView parent`
     * into `Display display` and set the display mode of the Display to 0 (no preference) (Only
     * used for Display's that have more than one mode.)
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    override fun onNothingSelected(parent: AdapterView<*>) {
        val display = parent.tag as Display
        setPresentationDisplayMode(display, 0)
    }

    /**
     * Display list adapter.
     * Shows information about all displays.
     */
    private inner class DisplayListAdapter
    /**
     * Initializes this instance of `DisplayListAdapter` by first calling through to our
     * super's constructor using the layout file R.layout.presentation_list_item for it to use when
     * instantiating views, and then we save our parameter context in our field Context mContext.
     *
     * Parameter: context PresentationActivity Context ("this" in onCreate())
     */
    (
            /**
             * `Context` passed to our constructor.
             */
            internal val mContext: Context) : ArrayAdapter<Display>(mContext, R.layout.presentation_list_item) {

        /**
         * This method just checks whether the `CheckBox mShowAllDisplaysCheckbox` ("Show all
         * displays") is checked, and if so returns null so that `DisplayManager.getDisplays`
         * will return all Display's. If it is not checked it returns the String DISPLAY_CATEGORY_PRESENTATION
         * so that `getDisplays` will return only attached presentation Display's.
         *
         * @return If the "Show all displays" checkbox is not checked we return the String
         * DISPLAY_CATEGORY_PRESENTATION, if checked we return null
         */
        private val displayCategory: String?
            get() = if (mShowAllDisplaysCheckbox!!.isChecked)
                null
            else
                DisplayManager.DISPLAY_CATEGORY_PRESENTATION

        /**
         * Get a View that displays the data at the specified position in the data set. You can either
         * create a View manually or inflate it from an XML layout file. When the View is inflated, the
         * parent View (GridView, ListView...) will apply default layout parameters unless you use
         * [android.view.LayoutInflater.inflate]
         * to specify a root view and to prevent attachment to the root.
         *
         *
         * First we check if the `View convertView` is null (View is not being recycled) and
         * if so we inflate the layout file for our list item (R.layout.presentation_list_item) into
         * `View v`, and if convertView is not null we set View v to convertView (to reuse the
         * old recycled View). Next we call `ArrayAdapter.getItem(position)` to get the get the
         * Display associated with the specified position in the data set and save it in our variable
         * `Display display`. We retrieve the display Id from `display` and save it as
         * `int displayId`. We retrieve to our variable `DemoPresentation presentation`
         * the `DemoPresentation` stored using the key `displayId` in our list of visible
         * presentations `SparseArray<DemoPresentation> mActivePresentations`. If there is a
         * `DemoPresentation` stored there (`presentation` != null) our variable
         * `DemoPresentationContents contents` is set to the `mContents` field of
         * `presentation`, otherwise it is set to null. If `contents` has been set to
         * null by the above statement, we set `contents` to the value stored under the
         * `displayId` key in the list of persistent presentation contents (saved in
         * `onSaveInstanceState` and restored in `onCreate`) in our array
         * `SparseArray<DemoPresentationContents> mSavedPresentationContents`. Next we locate
         * the CheckBox R.id.checkbox_presentation in our item `View v` and save it in
         * `CheckBox cb`. We set the tag of `cb` to `Display display`, set the
         * `OnCheckedChangeListener` to "this", and set the checked state of `cb` if
         * `contents` is not null, unchecked otherwise. Next we find the `TextView tv`
         * (R.id.display_id) in `v` and set its text to a formatted String containing the
         * `displayId` and the name of the Display. We locate Button b (R.id.info "INFO" Button
         * in `v`), set its tag to `Display display`, and its `OnClickListener`
         * to "this". We locate `Spinner s` (R.id.modes in `v`) and if there is no content,
         * or the Display only has one mode we set the visibility of "s" to "GONE" and set the adapter
         * of s to null. Otherwise we create `ArrayAdapter<String> modeAdapter` using `mContext`
         * and the system layout file android.R.layout.simple_list_item_1, set the visibility of `s`
         * to "VISIBLE", set the adapter of `s` to `modeAdapter`, set the tag of `s` to
         * `Display display`, and set "this" as the `OnItemSelectedListener` for `s`.
         * Then we load up `modeAdapter` with "<default mode>" as the 0'th entry, and every
         * `Display.Mode mode` in the array of supported modes for the Display we fetched earlier
         * to `Display.Mode[] modes` (using a formatted text String which contains the mode Id,
         * physical width, physical height, and refresh rate for the mode). While adding the supported
         * modes we check to see if the mode Id matches the mode Id of the current contents, and if so
         * we set that mode in the adapter to be the selected one. Finally we return the `View v`
         * we have created (or reused) and configured.
         *
         * @param position The position of the item within the adapter's data set of the item whose
         * view we want.
         * @param convertView The old view to reuse, if possible. Note: You should check that this view
         * is non-null and of an appropriate type before using. If it is not possible to convert
         * this view to display the correct data, this method can create a new view.
         * Heterogeneous lists can specify their number of view types, so that this View is
         * always of the right type (see [.getViewTypeCount] and
         * [.getItemViewType]).
         * @param parent The parent that this view will eventually be attached to
         *
         * @return A View corresponding to the data at the specified position.
        </default> */
        @SuppressLint("InflateParams", "DefaultLocale")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val v: View = convertView ?: (mContext as AppCompatActivity)
                    .layoutInflater
                    .inflate(R.layout.presentation_list_item, null)

            val display = getItem(position)

            val displayId = display!!.displayId

            val presentation = mActivePresentations.get(displayId)
            var contents: DemoPresentationContents? = presentation?.mContents
            if (contents == null) {
                contents = mSavedPresentationContents!!.get(displayId)
            }

            val cb = v.findViewById<CheckBox>(R.id.checkbox_presentation)
            cb.tag = display
            cb.setOnCheckedChangeListener(this@PresentationActivity)
            cb.isChecked = contents != null

            val tv = v.findViewById<TextView>(R.id.display_id)
            tv.text = v.context.resources.getString(
                    R.string.presentation_display_id_text, displayId, display.name)

            val b = v.findViewById<Button>(R.id.info)
            b.tag = display
            b.setOnClickListener(this@PresentationActivity)

            val s = v.findViewById<Spinner>(R.id.modes)
            val modes = display.supportedModes
            if (contents == null || modes.size == 1) {
                s.visibility = View.GONE
                s.adapter = null
            } else {
                val modeAdapter = ArrayAdapter<String>(mContext,
                        android.R.layout.simple_list_item_1)
                s.visibility = View.VISIBLE
                s.adapter = modeAdapter
                s.tag = display
                s.onItemSelectedListener = this@PresentationActivity

                modeAdapter.add("<default mode>")

                for (mode in modes) {
                    modeAdapter.add(String.format("Mode %d: %dx%d/%.1ffps",
                            mode.modeId,
                            mode.physicalWidth, mode.physicalHeight,
                            mode.refreshRate))
                    if (contents.displayModeId == mode.modeId) {
                        s.setSelection(modeAdapter.count - 1)
                    }
                }
            }

            return v
        }

        /**
         * Update the contents of the display list adapter to show information about all current
         * displays. First we remove all elements from the current list by calling the method
         * `ArrayAdapter.clear()`. Then we initialize `String displayCategory` with the
         * value our method `getDisplayCategory()` returns. That method checks whether the
         * `CheckBox mShowAllDisplaysCheckbox` ("Show all displays") is checked and if so
         * returns null, otherwise it returns the string DisplayManager.DISPLAY_CATEGORY_PRESENTATION
         * ("android.hardware.display.category.PRESENTATION"). We initialize `Display[] displays`
         * with the Display's returned by the `getDisplays` method of `DisplayManager mDisplayManager`
         * returns for `displayCategory`. If `displayCategory` was null all Display's will
         * be returned, if it was DISPLAY_CATEGORY_PRESENTATION `getDisplays` will return only
         * the presentation secondary Display's attached. Finally we add all the Display's in
         * `Display[] displays` to our ArrayAdapter.
         */
        fun updateContents() {
            clear()

            val displayCategory = displayCategory
            val displays = mDisplayManager!!.getDisplays(displayCategory)
            addAll(*displays)

            Log.d(TAG, "There are currently " + displays.size + " displays connected.")
            for (display in displays) {
                Log.d(TAG, "  $display")
            }
        }
    }

    /**
     * The presentation to show on the secondary display.
     *
     *
     * Note that the presentation display may have different metrics from the display on which
     * the main activity is showing so we must be careful to use the presentation's
     * own [Context] whenever we load resources.
     */
    private inner class DemoPresentation
    /**
     * Initializes this instance by saving the parameter `contents` in our field
     * `DemoPresentationContents mContents` (after calling our super's constructor).
     *
     * @param context The context of the application that is showing the presentation. The
     * presentation will create its own context (see getContext()) based on this context
     * and information about the associated display.
     * @param display The display to which the presentation should be attached.
     * Parameter: contents Information about the content we want to show in the presentation.
     */
    (context: Context, display: Display,
     /**
      * Information about the content we are showing in the presentation.
      */
     internal val mContents: DemoPresentationContents) : Presentation(context, display) {

        /**
         * Sets the preferred display mode id for the presentation. First we store the parameter `modeId`
         * in the `displayModeId` field of our field `DemoPresentationContents mContents`, then
         * we retrieve the WindowManager.LayoutParams window attributes for the current window to
         * `WindowManager.LayoutParams params`, set the field `displayModeId` of `params`
         * to the `modeId` parameter passed us, and then set the window attributes for the current
         * window to the modified `WindowManager.LayoutParams params`.
         *
         * @param modeId Id of the preferred display mode for the window. This must be one of the
         * supported modes obtained for the display(s) the window is on. A value of 0 means
         * no preference.
         */
        fun setPreferredDisplayMode(modeId: Int) {
            mContents.displayModeId = modeId


            val params = window!!.attributes
            params.preferredDisplayModeId = modeId
            window!!.attributes = params
        }

        /**
         * We initialize this instance of our DemoPresentation dialog in this method. First we call
         * through to our super's implementation of `onCreate`, then we retrieve the resources
         * for the context of the presentation to our variable `Resources r`, set our content
         * view to our layout file R.layout.presentation_content, retrieve the Display that this
         * presentation appears on to our variable `Display display`, set `int displayId`
         * to the display id of `Display display`, and set `int photo` to the photo field
         * of our field `DemoPresentationContents mContents`. Next we locate `TextView text`
         * in our layout (R.id.text) and set the text to a formatted String (R.string.presentation_photo_text)
         * containing the photo number, display Id, and the Display name. We locate `ImageView image`
         * in our layout (R.id.image) and set the `ImageView` to display the `photo` entry
         * in our `int[] PHOTOS` array of drawable resource id's. Then in order to set the background
         * to a random gradient we create `GradientDrawable drawable`, set its shape to RECTANGLE,
         * and set the type of gradient to RADIAL_GRADIENT. We create `Point p` and set it to the
         * display size in pixels, then use it to set the radius of our `GradientDrawable drawable`
         * to half of whichever dimension of the display was largest. We set the colors of `drawable`
         * to the `DemoPresentationContents mContents` field `int[] colors` (array of random
         * colors), and finally we set the background of the root element of our View (android.R.id.content)
         * to GradientDrawable drawable.
         *
         * @param savedInstanceState holds the result from the most recent call to
         * [.onSaveInstanceState], or null if this is the first time.
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            // Be sure to call the super class.
            super.onCreate(savedInstanceState)

            // Get the resources for the context of the presentation.
            // Notice that we are getting the resources from the context of the presentation.
            val r = context.resources

            // Inflate the layout.
            setContentView(R.layout.presentation_content)

            val display = display
            val displayId = display.displayId
            val photo = mContents.photo

            // Show a caption to describe what's going on.
            val text = findViewById<TextView>(R.id.text)
            text.text = r.getString(R.string.presentation_photo_text,
                    photo, displayId, display.name)

            // Show a n image for visual interest.
            val image = findViewById<ImageView>(R.id.image)
            @Suppress("DEPRECATION")
            image.setImageDrawable(r.getDrawable(PHOTOS[photo]))

            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.RECTANGLE
            drawable.gradientType = GradientDrawable.RADIAL_GRADIENT

            // Set the background to a random gradient.
            val p = Point()
            getDisplay().getSize(p)
            drawable.gradientRadius = (max(p.x, p.y) / 2).toFloat()
            drawable.colors = mContents.colors
            findViewById<View>(android.R.id.content).background = drawable
        }
    }

    /**
     * Information about the content we want to show in the presentation.
     */
    private class DemoPresentationContents : Parcelable {
        /**
         * Index into int[] PHOTOS array of Drawable resource id's for our photo
         */
        internal val photo: Int
        /**
         * Array of random colors used for the background
         */
        internal val colors: IntArray
        /**
         * Display mode Id we are to use.
         */
        internal var displayModeId: Int = 0

        /**
         * Constructs a DemoPresentationContents instance using the parameter `photo` as its
         * field `int photo`, and initializes its `int [] colors` array to two random
         * colors to be used when creating the background gradient for this instance.
         *
         * @param photo index into the `int[] PHOTOS` array of Drawable resource Ids
         */
        constructor(photo: Int) {
            this.photo = photo
            colors = intArrayOf((Math.random() * Integer.MAX_VALUE).toInt() or -0x1000000, (Math.random() * Integer.MAX_VALUE).toInt() or -0x1000000)
        }

        /**
         * Constructs a DemoPresentationContents instance by reading the parameter `Parcel in`
         * which was previously written to by `writeToParcel`. Our `Parcel` should consist
         * of 4 ints which we read in order to set the values of our fields `photo`, `colors[0]`,
         * `colors[1]`, and `displayModeId`.
         *
         * @param in The Parcel to read the object's data from.
         */
        private constructor(`in`: Parcel) {
            photo = `in`.readInt()
            colors = intArrayOf(`in`.readInt(), `in`.readInt())
            displayModeId = `in`.readInt()
        }

        /**
         * Part of the `Parcelable` interface. Describe the kinds of special objects contained
         * in this  Parcelable's marshalled representation. We simply return 0.
         *
         * @return a bitmask indicating the set of special object types marshalled by the Parcelable.
         */
        override fun describeContents(): Int {
            return 0
        }

        /**
         * Part of the `Parcelable` interface. Flatten this object into a `Parcel`. We
         * simply write the four int's of our fields: `photo`, `colors[0]`, `colors[1]`,
         * and `displayModeId` into the `Parcel dest` at the current `dataPosition()`,
         * growing `dataCapacity()` if needed.
         *
         * @param dest The Parcel in which the object should be written.
         * @param flags Additional flags about how the object should be written.
         * May be 0 or [.PARCELABLE_WRITE_RETURN_VALUE].
         */
        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(photo)
            dest.writeInt(colors[0])
            dest.writeInt(colors[1])
            dest.writeInt(displayModeId)
        }

        companion object {

            /**
             * Interface that must be implemented and provided as a public CREATOR field that generates
             * instances of your Parcelable class from a Parcel.
             */
            @Suppress("unused")
            @JvmField
            val CREATOR: Parcelable.Creator<DemoPresentationContents> = object : Parcelable.Creator<DemoPresentationContents> {
                /**
                 * Create a new instance of the Parcelable class, instantiating it
                 * from the given Parcel whose data had previously been written by
                 * [Parcelable.writeToParcel()][Parcelable.writeToParcel].
                 *
                 *
                 * We simply use the `Parcel in` to call our constructor that uses a
                 * `Parcel` argument.
                 *
                 * @param in The Parcel to read the object's data from.
                 * @return Returns a new instance of the Parcelable class DemoPresentationContents
                 */
                override fun createFromParcel(`in`: Parcel): DemoPresentationContents {
                    return DemoPresentationContents(`in`)
                }

                /**
                 * Create a new array of the Parcelable class DemoPresentationContents
                 *
                 * @param size Size of the array.
                 * @return Returns an array of the Parcelable class, with every entry
                 * initialized to null.
                 */
                override fun newArray(size: Int): Array<DemoPresentationContents?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {

        /**
         * Key for storing saved instance state.
         */
        private const val PRESENTATION_KEY = "presentation"

        /**
         * The content that we want to show on the presentation.
         */
        private val PHOTOS = intArrayOf(R.drawable.frantic, R.drawable.photo1, R.drawable.photo2, R.drawable.photo3, R.drawable.photo4, R.drawable.photo5, R.drawable.photo6, R.drawable.sample_4)

        /**
         * TAG used for logging
         */
        private const val TAG = "PresentationActivity"
    }
}

