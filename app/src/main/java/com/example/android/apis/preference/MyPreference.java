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

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * This is an example of a custom preference type. The preference counts the
 * number of clicks it has received and stores/retrieves it from the storage.
 * It is inflated from the xml file xml/advanced_preferences.xml
 */
public class MyPreference extends Preference {
    /**
     * The current value of our counter preference.
     */
    private int mClickCounter;

    /**
     * This is the constructor called by the inflater. First we call our super's constructor, then
     * we call the method {@code setWidgetLayoutResource} to set the layout for the controllable widget
     * portion of our {@code Preference} to our layout file R.layout.preference_widget_mypreference
     * (which contains only a {@code TextView}).
     *
     * @param context The Context this is associated with, through which it can access the current
     *                theme, resources, {@code SharedPreferences}, etc.
     * @param attrs   The attributes of the XML tag that is inflating the preference.
     */
    public MyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWidgetLayoutResource(R.layout.preference_widget_mypreference);
    }

    /**
     * Binds the created View to the data for this Preference. First we call through to our super's
     * implementation of {@code onBindView}, then we locate the view with ID R.id.mypreference_widget
     * in our parameter {@code View view} to set {@code TextView myTextView} and if that is not null
     * we set its text to the string value of our field {@code int mClickCounter}.
     *
     * @param view The View that shows this Preference.
     */
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        // Set our custom views inside the layout
        final TextView myTextView = (TextView) view.findViewById(R.id.mypreference_widget);
        if (myTextView != null) {
            myTextView.setText(String.valueOf(mClickCounter));
        }
    }

    /**
     * Processes a click on this preference. First we set {@code int newValue} to one more than our
     * field {@code int mClickCounter}, then if calling the method {@code callChangeListener} with
     * {@code newValue} returns false (the client rejects changing the preference to {@code newValue})
     * we return having done nothing. Otherwise we set our field {@code int mClickCounter} to
     * {@code newValue}, call {@code persistInt} to save {@code mClickCounter} to the shared preference
     * file, and call {@code Preference.notifyChanged} to notify the system that the preference value
     * has changed so that the UI can be refreshed.
     */
    @Override
    protected void onClick() {
        int newValue = mClickCounter + 1;
        // Give the client a chance to ignore this change if they deem it
        // invalid
        if (!callChangeListener(newValue)) {
            // They don't want the value to be set
            return;
        }

        // Increment counter
        mClickCounter = newValue;

        // Save to persistent storage (this method will make sure this
        // preference should be persistent, along with other useful checks)
        persistInt(mClickCounter);

        // Data has changed, notify so UI can be refreshed!
        notifyChanged();
    }

    /**
     * Called when our {@code Preference} is being inflated and the default value attribute needs to
     * be read. We retrieve the integer value for the attribute at {@code index}, defaulting to 0,
     * and return this value to the caller.
     *
     * @param a     The set of attributes.
     * @param index The index of the default value attribute.
     * @return The default value of this preference type.
     */
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // This preference type's value type is Integer, so we read the default
        // value from the attributes as an Integer.
        return a.getInteger(index, 0);
    }

    /**
     * Implement this to set the initial value of the Preference. If our parameter {@code restoreValue}
     * is true we set our field {@code int mClickCounter} to the persisted int stored in the shared
     * preferences, defaulting to the current value of {@code mClickCounter}. If {@code restoreValue}
     * is false we cast {@code defaultValue} to {@code Integer} to set {@code int value}, set
     * {@code mClickCounter} to {@code value}, and call {@code persistInt} to save {@code value} to
     * the shared preference file.
     *
     * @param restoreValue True to restore the persisted value, false to use the given
     *                     {@code defaultValue}
     * @param defaultValue The default value for this Preference. Only use this if
     *                     {@code restoreValue} is false.
     */
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            // Restore state
            mClickCounter = getPersistedInt(mClickCounter);
        } else {
            // Set state
            int value = (Integer) defaultValue;
            mClickCounter = value;
            persistInt(value);
        }
    }

    /**
     * Hook allowing a Preference to generate a representation of its internal state that can later
     * be used to create a new instance with that same state. First we set {@code Parcelable superState}
     * to the value returned by our super's implementation of {@code onSaveInstanceState}. Then if the
     * method {@code isPersistent} returns true (this Preference is persistent) we just return
     * {@code superState} to the caller. Otherwise we create {@code SavedState myState} from the contents
     * of {@code superState}, set its {@code clickCounter} field to {@code mClickCounter}, and return
     * it to the caller. ({@code SavedState} is a subclass of {@code BaseSavedState} and is defined
     * in this file later on).
     *
     * @return A Parcelable object containing the current dynamic state of this Preference, or null
     * if there is nothing interesting to save.
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        /*
         * Suppose a client uses this preference type without persisting. We
         * must save the instance state so it is able to, for example, survive
         * orientation changes.
         */

        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        // Save the instance state
        final SavedState myState = new SavedState(superState);
        myState.clickCounter = mClickCounter;
        return myState;
    }

    /**
     * Hook allowing a Preference to re-apply a representation of its internal state that had previously
     * been generated by {@code onSaveInstanceState}. First we make sure that our parameter
     * {@code Parcelable state} is an instance of {@code SavedState}, and if not we did not save our
     * state in it, so we just call our super's implementation of {@code onRestoreInstanceState} and
     * return. Otherwise we cast {@code state} to set {@code SavedState myState}, call our super's
     * implementation of {@code onRestoreInstanceState} with the super state that saved in {@code myState}.
     * We then set our field {@code mClickCounter} to the {@code clickCounter} field of {@code myState},
     * and call {@code Preference.notifyChanged} to notify the system that the preference value has
     * changed so that the UI can be refreshed.
     *
     * @param state The saved state that had previously been returned by
     *              {@code onSaveInstanceState}.
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // Restore the instance state
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mClickCounter = myState.clickCounter;
        notifyChanged();
    }

    /**
     * SavedState, a subclass of {@code BaseSavedState}, will store the state of {@code MyPreference},
     * a subclass of {@code Preference}.
     * <p>
     * It is important to always call through to super methods.
     */
    @SuppressWarnings("WeakerAccess")
    private static class SavedState extends BaseSavedState {
        /**
         * Field that {@code MyPreference} stores its {@code mClickCounter} field in.
         */
        int clickCounter;

        /**
         * Our constructor.
         *
         * @param source {@code Parcelable} returned by the {@code Preference} implementation of
         *               {@code onSaveInstanceState}
         */
        public SavedState(Parcel source) {
            super(source);

            // Restore the click counter
            clickCounter = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            // Save the click counter
            dest.writeInt(clickCounter);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
