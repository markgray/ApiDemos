package com.example.android.apis.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.android.apis.R;

/**
 * Demonstrates how the various soft input modes impact window resizing:
 *
 *  "Unspecified" (The system will try to pick one or the other depending
 *                on the contents of the window);
 *  "Resize" (allow the window to be resized when an input method is shown,
 *                so that its contents are not covered by the input method);
 *  "Pan" (window will pan when an input method is shown, so it doesn't need
 *                to deal with resizing but is just panned by the framework
 *                to ensure the current input focus is visible);
 *  "Nothing" (window will not adjust for a shown input method. The window will
 *                not be resized, and it will not be panned to make its focus
 *                visible)
 *
 * effect the resizing of the UI windows when the IME is displayed. They are set
 * with: getWindow().setSoftInputMode(mResizeModeValues[position])
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SoftInputModes extends Activity {
    Spinner mResizeMode; // spinner used to select the soft input mode
    final CharSequence[] mResizeModeLabels = new CharSequence[] { // labels used for Spinner
            "Unspecified", "Resize", "Pan", "Nothing"
    };
    final int[] mResizeModeValues = new int[] { // LayoutParams constants corresponding to labels
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED,
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE,
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN,
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING,
    };
    
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.soft_input_modes. Next we
     * locate the Spinner in our layout (R.id.resize_mode) and use it to initialize our field
     * mResizeMode. We create ArrayAdapter<CharSequence> adapter using our field CharSequence[]
     * mResizeModeLabels as the CharSequence objects used by android.R.layout.simple_spinner_item
     * to populate the list
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

        // See assets/res/any/layout/save_restore_state.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.soft_input_modes);
        
        mResizeMode = (Spinner)findViewById(R.id.resize_mode);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mResizeModeLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mResizeMode.setAdapter(adapter);
        mResizeMode.setSelection(0);
        mResizeMode.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        getWindow().setSoftInputMode(mResizeModeValues[position]);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        getWindow().setSoftInputMode(mResizeModeValues[0]);
                    }
                });
    }
}
