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
@file:Suppress("DEPRECATION")

package com.example.android.apis.preference

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.preference.Preference
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.example.android.apis.R

/**
 * This is an example of a custom preference type. The preference counts the
 * number of clicks it has received and stores/retrieves it from the storage.
 * It is inflated from the xml file xml/advanced_preferences.xml
 */
class MyPreference(context: Context?, attrs: AttributeSet?) : Preference(context, attrs) {
    /**
     * The current value of our counter preference.
     */
    private var mClickCounter = 0

    /**
     * Binds the created View to the data for this Preference. First we call through to our super's
     * implementation of `onBindView`, then we locate the view with ID R.id.mypreference_widget
     * in our [View] parameter [view] to set [TextView] `val myTextView` and if that is not null
     * we set its text to the string value of our [Int] field [mClickCounter].
     *
     * @param view The View that shows this Preference.
     */
    override fun onBindView(view: View) {
        super.onBindView(view)

        // Set our custom views inside the layout
        val myTextView = view.findViewById<TextView>(R.id.mypreference_widget)
        if (myTextView != null) {
            myTextView.text = mClickCounter.toString()
        }
    }

    /**
     * Processes a click on this preference. First we set [Int] `val newValue` to one more than our
     * [Int] field [mClickCounter], then if calling the method [callChangeListener] with `newValue`
     * returns false (the client rejects changing the preference to `newValue`) we return having
     * done nothing. Otherwise we set our [Int] field [mClickCounter] to `newValue`, call [persistInt]
     * to save [mClickCounter] to the shared preference file, and call [Preference.notifyChanged] to
     * notify the system that the preference value has changed so that the UI can be refreshed.
     */
    override fun onClick() {
        val newValue = mClickCounter + 1
        // Give the client a chance to ignore this change if they deem it
        // invalid
        if (!callChangeListener(newValue)) {
            // They don't want the value to be set
            return
        }

        // Increment counter
        mClickCounter = newValue

        // Save to persistent storage (this method will make sure this
        // preference should be persistent, along with other useful checks)
        persistInt(mClickCounter)

        // Data has changed, notify so UI can be refreshed!
        notifyChanged()
    }

    /**
     * Called when our [Preference] is being inflated and the default value attribute needs to
     * be read. We retrieve the integer value for the attribute at [index], defaulting to 0, and
     * return this value to the caller.
     *
     * @param a     The set of attributes.
     * @param index The index of the default value attribute.
     * @return The default value of this preference type.
     */
    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        // This preference type's value type is Integer, so we read the default
        // value from the attributes as an Integer.
        return a.getInteger(index, 0)
    }

    /**
     * Implement this to set the initial value of the [Preference]. If our parameter [restoreValue]
     * is true we set our [Int] field [mClickCounter] to the persisted [Int] stored in the shared
     * preferences, defaulting to the current value of [mClickCounter]. If [restoreValue] is false
     * we cast [defaultValue] to [Int] to set `val value`, set [mClickCounter] to `value`, and call
     * [persistInt] to save `value` to the shared preference file.
     *
     * @param restoreValue True to restore the persisted value, false to use the given [defaultValue]
     * @param defaultValue The default value for this Preference. Only use this if [restoreValue] is
     * false.
     */
    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        if (restoreValue) {
            // Restore state
            mClickCounter = getPersistedInt(mClickCounter)
        } else {
            // Set state
            val value = defaultValue as Int
            mClickCounter = value
            persistInt(value)
        }
    }

    /**
     * Hook allowing a Preference to generate a representation of its internal state that can later
     * be used to create a new instance with that same state. First we set [Parcelable] variable
     * `val superState` to the value returned by our super's implementation of `onSaveInstanceState`.
     * Then if the method [isPersistent] returns true (this [Preference] is persistent) we just return
     * `superState` to the caller. Otherwise we create [SavedState] `val myState` from the contents
     * of `superState`, set its `clickCounter` field to [mClickCounter], and return it to the caller.
     * ([SavedState] is a subclass of [Preference.BaseSavedState] and is defined in this file later
     * on).
     *
     * @return A [Parcelable] object containing the current dynamic state of this Preference, or null
     * if there is nothing interesting to save.
     */
    override fun onSaveInstanceState(): Parcelable {
        /*
         * Suppose a client uses this preference type without persisting. We
         * must save the instance state so it is able to, for example, survive
         * orientation changes.
         */
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState
        }

        // Save the instance state
        val myState = SavedState(superState)
        myState.clickCounter = mClickCounter
        return myState
    }

    /**
     * Hook allowing a [Preference] to re-apply a representation of its internal state that had
     * previously been generated by [onSaveInstanceState]. First we make sure that our [Parcelable]
     * parameter [state] is an instance of [SavedState], and if not we did not save our state in it,
     * so we just call our super's implementation of `onRestoreInstanceState` and return. Otherwise
     * we cast [state] to set [SavedState] `val myState`, call our super's implementation of
     * `onRestoreInstanceState` with the super state that is saved in `myState`. We then set our
     * [Int] field [mClickCounter] to the `clickCounter` field of `myState`, and call the method
     * [Preference.notifyChanged] to notify the system that the preference value has changed so that
     * the UI can be refreshed.
     *
     * @param state The saved state that had previously been returned by [onSaveInstanceState].
     */
    override fun onRestoreInstanceState(state: Parcelable) {
        if (state.javaClass != SavedState::class.java) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state)
            return
        }

        // Restore the instance state
        val myState = state as SavedState
        super.onRestoreInstanceState(myState.superState)
        mClickCounter = myState.clickCounter
        notifyChanged()
    }

    /**
     * SavedState, a subclass of [Preference.BaseSavedState], will store the state of [MyPreference],
     * which is a subclass of [Preference]. It is important to always call through to super methods.
     */
    private class SavedState : BaseSavedState {
        /**
         * Field that [MyPreference] stores its `mClickCounter` field in.
         */
        var clickCounter = 0

        /**
         * Our constructor. First we call our super's constructor, then we read an [Int] value from
         * [Parcel] parameter [source] from the current `dataPosition()` into our [Int] field
         * [clickCounter].
         *
         * @param source [Parcelable] returned by the [Preference] implementation of [onSaveInstanceState]
         */
        constructor(source: Parcel) : super(source) {

            // Restore the click counter
            clickCounter = source.readInt()
        }

        /**
         * Flatten this object in to a Parcel. First we call our super's implementation of `writeToParcel`,
         * then we call the [Parcel.writeInt] method of [Parcel] parameter [dest] to write the value of
         * our [Int] field [clickCounter] into the parcel at the current `dest.dataPosition()`, growing
         * its `dataCapacity()` if needed.
         *
         * @param dest  The [Parcel] in which the object should be written.
         * @param flags Additional flags about how the object should be written.
         */
        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)

            // Save the click counter
            dest.writeInt(clickCounter)
        }

        /**
         * Constructor called by derived classes when creating their [SavedState] objects. We just call
         * our super's constructor.
         *
         * @param superState The state of the superclass of this view
         */
        constructor(superState: Parcelable?) : super(superState)

        companion object {
            /**
             * Interface that must be implemented and provided as a public CREATOR field that
             * generates instances of our Parcelable class from a Parcel.
             */
            @Suppress("unused")
            val CREATOR: Creator<SavedState> = object : Creator<SavedState> {
                /**
                 * Create a new instance of the Parcelable class, instantiating it from the given
                 * [Parcel] whose data had previously been written by [Parcelable.writeToParcel].
                 * We simply return a new instance of [SavedState] created from our [Parcel]
                 * parameter `in`
                 *
                 * @param in The [Parcel] to read the object's data from.
                 * @return Returns a new instance of the Parcelable class.
                 */
                override fun createFromParcel(`in`: Parcel): SavedState? {
                    return SavedState(`in`)
                }

                /**
                 * Create a new array of the [Parcelable] class. We simply return a new array of
                 * all null [SavedState] objects of size [size].
                 *
                 * @param size Size of the array.
                 * @return Returns an array of the [Parcelable] class, with every entry initialized
                 * to null.
                 */
                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    /**
     * This is the init block of our constructor. We just call the method `setWidgetLayoutResource`
     * method to set the layout for the controllable widget portion of our `Preference` to our layout
     * file R.layout.preference_widget_mypreference (which contains only a `TextView`).
     */
    init {
        widgetLayoutResource = R.layout.preference_widget_mypreference
    }
}