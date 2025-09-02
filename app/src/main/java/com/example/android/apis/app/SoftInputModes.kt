package com.example.android.apis.app

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates how the various soft input modes impact window resizing when the
 * IME is displayed:
 *  - "Unspecified" (The system will try to pick one or the other depending
 * on the contents of the window);
 *  - "Resize" (allow the window to be resized when an input method is shown,
 * so that its contents are not covered by the input method);
 *  - "Pan" (window will pan when an input method is shown, so it doesn't need
 * to deal with resizing but is just panned by the framework to ensure the current
 * input focus is visible);
 *  - "Nothing" (window will not adjust for a shown input method. The window will
 * not be resized, and it will not be panned to make its focus visible)
 *
 * They are set with: `getWindow().setSoftInputMode(mResizeModeValues[ position ])`
 */
@SuppressLint("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.HONEYCOMB)
class SoftInputModes : AppCompatActivity() {
    /**
     * spinner used to select the soft input mode
     */
    internal lateinit var mResizeMode: Spinner

    /**
     * labels used for Spinner
     */
    private val mResizeModeLabels = arrayOf<CharSequence>(
        "Unspecified", "Resize", "Pan", "Nothing"
    )

    /**
     * LayoutParams constants corresponding to labels
     */
    internal val mResizeModeValues = intArrayOf(
        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED,
        @Suppress("DEPRECATION")
        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE,
        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN,
        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
    )

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.soft_input_modes. Next
     * we locate the [Spinner] in our layout (R.id.resize_mode) and use it to initialize our field
     * [mResizeMode]. We construct an `ArrayAdapter<CharSequence>` to initialize our variable
     * `val adapter` using our field `CharSequence[]` field [mResizeModeLabels] as the [CharSequence]
     * objects used by android.R.layout.simple_spinner_item to populate the list. We set the layout
     * resource to create the drop down views of the `adapter` to be the system layout file
     * android.R.layout.simple_spinner_dropdown_item. We now set this `adapter` to be the one
     * used by our [Spinner] field [mResizeMode], set its current selection to 0, and finally set its
     * `OnItemSelectedListener` to an anonymous class which sets the soft input mode to use for the
     * window to the one selected in the `Spinner` `mResizeMode` (or SOFT_INPUT_ADJUST_UNSPECIFIED
     * if none is selected).
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState)

        // See assets/res/any/layout/soft_input_modes.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.soft_input_modes)

        mResizeMode = findViewById<View>(R.id.resize_mode) as Spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            mResizeModeLabels
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mResizeMode.adapter = adapter
        mResizeMode.setSelection(0)
        mResizeMode.onItemSelectedListener = object : OnItemSelectedListener {
            /**
             * Called when an item in the [Spinner] field [mResizeMode] is selected. We simply set
             * the soft input mode to the value corresponding of the position of the item selected
             * in the adapter.
             *
             * @param parent The [AdapterView] where the selection happened
             * @param view The [View] within the [AdapterView] that was clicked
             * @param position The position of the [View] in the adapter
             * @param id The row id of the item that is selected
             */
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                window.setSoftInputMode(mResizeModeValues[position])
            }

            /**
             * Callback method to be invoked when the selection disappears from this
             * view. The selection can disappear for instance when touch is activated
             * or when the adapter becomes empty. Can't figure out how to trigger this
             * but all we do is set the soft input mode to the first (zeroth) of the
             * choices in the adapter: SOFT_INPUT_ADJUST_UNSPECIFIED
             *
             * @param parent The [AdapterView] that now contains no selected item.
             */
            override fun onNothingSelected(parent: AdapterView<*>) {
                window.setSoftInputMode(mResizeModeValues[0])
            }
        }
    }
}
