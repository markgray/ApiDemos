/*
 * Copyright (C) 2011 The Android Open Source Project
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
package com.example.android.apis.view

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.hardware.input.InputManager
import android.hardware.input.InputManager.InputDeviceListener
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.util.SparseIntArray
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import java.util.*

/**
 * Demonstrates how to process input events received from game controllers.
 * It also shows how to detect when input devices are added, removed or reconfigured.
 *
 * This activity displays button states and joystick positions.
 * Also writes detailed information about relevant input events to the log.
 *
 * The game controller is also used to control a very simple game.  See [GameView]
 * for the game itself, it is used by our layout file R.layout.game_controller_input.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class GameControllerInput : AppCompatActivity(), InputDeviceListener {
    /**
     * [InputManager] for interacting with input devices.
     */
    private var mInputManager: InputManager? = null

    /**
     * Array of [InputDeviceState] Objects using the device ID as the index.
     */
    private var mInputDeviceStates: SparseArray<InputDeviceState>? = null

    /**
     * Reference to the [GameView] in our layout file with ID R.id.game
     */
    private var mGame: GameView? = null

    /**
     * [ListView] in our layout file with ID R.id.summary, fed by the [SummaryAdapter] adapter field
     * [mSummaryAdapter], we use it to output information we receive as an [InputDeviceListener] in
     * a very nifty way.
     */
    private var mSummaryList: ListView? = null

    /**
     * [SummaryAdapter] which feeds [ListView] field [mSummaryList], displays textual representations
     * of [InputDeviceState] Objects our [InputDeviceListener] interface receives.
     */
    private var mSummaryAdapter: SummaryAdapter? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.game_controller_input.
     * Then we initialize our [InputManager] field [mInputManager] with an new instance for
     * interacting with input devices. Next we initialize our `SparseArray<InputDeviceState>` field
     *  [mInputDeviceStates] with a new instance of [SparseArray], and our [SummaryAdapter] field
     *  [mSummaryAdapter] with a new instance of [SummaryAdapter] using this as the [Context] and a
     *  resources instance for our application's package.
     *
     * We initialize our [GameView] field [mGame] by finding the view with ID R.id.game, and our
     * [ListView] field [mSummaryList] by finding the view with ID R.id.summary. We set the adapter
     * of [mSummaryList] to [mSummaryAdapter], and its `OnItemClickListener` to a lambda which calls
     * through to the `onItemClick` method of [mSummaryAdapter].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_controller_input)
        mInputManager = getSystemService(Context.INPUT_SERVICE) as InputManager
        mInputDeviceStates = SparseArray()
        mSummaryAdapter = SummaryAdapter(this, resources)
        mGame = findViewById(R.id.game)
        mSummaryList = findViewById(R.id.summary)
        mSummaryList!!.adapter = mSummaryAdapter
        mSummaryList!!.setOnItemClickListener {
            parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            mSummaryAdapter!!.onItemClick(position)
        }

    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for our activity to start
     * interacting with the user. First we call through to our super's implementation of `onResume`.
     * We then use [InputManager] field [mInputManager] to register "this" as an input device listener
     * to watch for when input devices are added, removed or reconfigured. Next we use [mInputManager]
     * to fetch the ids of all input devices in the system to the [Int] array `val ids`. Then we loop
     * for each [mInputManager] `id` in `ids` calling our method [getInputDeviceState] for it.
     * [getInputDeviceState] maintains the  `SparseArray<InputDeviceState>` field [mInputDeviceStates]
     * and if the device ID it is called for is not already in the array, gets information about that
     * input device from [mInputManager] in an [InputDevice], creates a [InputDeviceState] from it
     * and stores it in the array [mInputDeviceStates]. If the device ID already had an entry it just
     * returns that to the caller.
     */
    override fun onResume() {
        super.onResume()

        // Register an input device listener to watch when input devices are
        // added, removed or reconfigured.
        mInputManager!!.registerInputDeviceListener(this, null)

        // Query all input devices.
        // We do this so that we can see them in the log as they are enumerated.
        val ids = mInputManager!!.inputDeviceIds
        for (id in ids) {
            getInputDeviceState(id)
        }
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. We call through to our super's implementation of `onPause`
     * then use [InputManager] field [mInputManager] to unregister "this" as an [InputDeviceListener].
     */
    override fun onPause() {
        super.onPause()

        // Remove the input device listener when the activity is paused.
        mInputManager!!.unregisterInputDeviceListener(this)
    }

    /**
     * Called when the current [Window] of the activity gains or loses focus. This is the best
     * indicator of whether this activity is visible to the user. First we call through to our
     * super's implementation of `onWindowFocusChanged`, then we request focus for our [GameView]
     * field [mGame].
     *
     * @param hasFocus Whether the window of this activity has focus.
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        mGame!!.requestFocus()
    }

    /**
     * Called to process key events. You can override this to intercept all key events before they
     * are dispatched to the [Window]. First we call our method [getInputDeviceState] to initialize
     * [InputDeviceState] `val state` with the [InputDeviceState] of the device ID that generated
     * our [KeyEvent] parameter [event]. If `state` is not null, we switch on the value of the
     * action of [event]:
     *
     *  * ACTION_DOWN - if the `onKeyDown` method of `state` returns true (the key is
     *  a key used by the game), we call the `show` method of [SummaryAdapter] field
     *  [mSummaryAdapter]  with `state` as the argument. We then break
     *
     *  * ACTION_UP - if the `onKeyUp` method of `state` returns true (the key is a key used by
     *  the game), we call the `show` method of [SummaryAdapter] field [mSummaryAdapter] with
     *  `state` as the argument. We then break
     *
     * Finally we return the value returned by our super's implementation of `dispatchKeyEvent` to
     * our caller.
     *
     * @param event The key event.
     * @return boolean Return true if this event was consumed.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Update device state for visualization and logging.
        val state = getInputDeviceState(event.deviceId)
        if (state != null) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> if (state.onKeyDown(event)) {
                    mSummaryAdapter!!.show(state)
                }
                KeyEvent.ACTION_UP -> if (state.onKeyUp(event)) {
                    mSummaryAdapter!!.show(state)
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    /**
     * Called to process generic motion events. You can override this to intercept all generic motion
     * events before they are dispatched to the window. Only if the [MotionEvent] parameter [event]
     * is from a SOURCE_CLASS_JOYSTICK input source (a joystick) and the action of [event] is
     * ACTION_MOVE we call our method [getInputDeviceState] to initialize [InputDeviceState]
     * `val state` with the [InputDeviceState] of the device ID that generated our [KeyEvent]
     * parameter  [event]. Then if `state` is not null, and its `onJoystickMotion` method returns
     * true given [MotionEvent] parameter [event] (it always returns true) we call the `show` method
     * of [SummaryAdapter] field [mSummaryAdapter] with `state` as the argument. Finally we return
     * the value returned by our super's implementation of `dispatchGenericMotionEvent` to our
     * caller.
     *
     * @param event The generic motion event.
     * @return boolean Return true if this event was consumed.
     */
    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        // Check that the event came from a joystick since a generic motion event
        // could be almost anything.
        if (event.isFromSource(InputDevice.SOURCE_CLASS_JOYSTICK)
                && event.action == MotionEvent.ACTION_MOVE) {
            // Update device state for visualization and logging.
            val state = getInputDeviceState(event.deviceId)
            if (state != null && state.onJoystickMotion(event)) {
                mSummaryAdapter!!.show(state)
            }
        }
        return super.dispatchGenericMotionEvent(event)
    }

    /**
     * Returns an [InputDeviceState] object for [deviceId], from the cache contained in
     * `SparseArray<InputDeviceState>` field [mInputDeviceStates], or freshly created (and
     * cached for future calls. First we try to initialize [InputDeviceState] `val state` by
     * using [deviceId] to fetch it from our `SparseArray<InputDeviceState>` cache field
     * [mInputDeviceStates]. If it is null, we use [InputManager] field [mInputManager] to
     * initialize [InputDevice] `val device` with information about the input device with ID
     * [deviceId]. If `device` is null, we return null to our caller. Otherwise we set `state`
     * to a new instance of [InputDeviceState] created from `device`, store `state` in our cache
     * [mInputDeviceStates] under the index [deviceId], and log the new device. Finally we return
     * `state` to our caller.
     *
     * @param deviceId Device ID
     * @return an [InputDeviceState] object for [deviceId], from the cache contained in
     * `SparseArray<InputDeviceState>` field [mInputDeviceStates], or freshly created (and
     * then cached for future calls) if this is the first time the device has appeared.
     */
    private fun getInputDeviceState(deviceId: Int): InputDeviceState? {
        var state = mInputDeviceStates!![deviceId]
        if (state == null) {
            val device = mInputManager!!.getInputDevice(deviceId) ?: return null
            state = InputDeviceState(device)
            mInputDeviceStates!!.put(deviceId, state)
            Log.i(TAG, "Device enumerated: " + state.device)
        }
        return state
    }

    /**
     * Called whenever an input device has been added to the system, part of the [InputDeviceListener]
     * interface. We call our method [getInputDeviceState] to initialize [InputDeviceState] `val state`
     * with the [InputDeviceState] of the device ID of our parameter [deviceId]. We then log the string
     * value of the [InputDevice] object for that device.
     *
     * @param deviceId The id of the input device that was added.
     */
    override fun onInputDeviceAdded(deviceId: Int) {
        val state = getInputDeviceState(deviceId)
        Log.i(TAG, "Device added: " + state!!.device)
    }

    /**
     * Called whenever the properties of an input device have changed since they were last queried,
     * part of the [InputDeviceListener] interface. We initialize [InputDeviceState] `var state`
     * by retrieving the [InputDeviceState] stored under the key [deviceId] in our
     * `SparseArray<InputDeviceState>` cache field [mInputDeviceStates]. If `state` is not null, we
     * remove the entry for [deviceId] from [mInputDeviceStates], and call our method
     * [getInputDeviceState] to create a new [InputDeviceState] for [deviceId] to set `state` to
     * (and also have it cache it in [mInputDeviceStates]). We then log the string value of the
     * [InputDevice] object for that device.
     *
     * @param deviceId The id of the input device that changed.
     */
    override fun onInputDeviceChanged(deviceId: Int) {
        var state = mInputDeviceStates!![deviceId]
        if (state != null) {
            mInputDeviceStates!!.remove(deviceId)
            state = getInputDeviceState(deviceId)
            Log.i(TAG, "Device changed: " + state!!.device)
        }
    }

    /**
     * Called whenever an input device has been removed from the system, part of the [InputDeviceListener]
     * interface. We call our method [getInputDeviceState] to initialize [InputDeviceState] `val state`
     * with the [InputDeviceState] of the device ID of our parameter [deviceId]. If `state` is not null
     * we log the string value of the [InputDevice] object for that device, then remove the entry for
     * [deviceId] from our `SparseArray<InputDeviceState>` cache field [mInputDeviceStates].
     *
     * @param deviceId The id of the input device that was removed.
     */
    override fun onInputDeviceRemoved(deviceId: Int) {
        val state = mInputDeviceStates!![deviceId]
        if (state != null) {
            Log.i(TAG, "Device removed: " + state.device)
            mInputDeviceStates!!.remove(deviceId)
        }
    }

    /**
     * Tracks the state of joystick axes and game controller buttons for a particular
     * input device for diagnostic purposes.
     */
    private class InputDeviceState(
            /**
             * The [InputDevice] that we were created for, set in our constructor.
             */
            val device: InputDevice
    ) {

        /**
         * Axis ID's if we are created for a SOURCE_CLASS_JOYSTICK (joystick).
         */
        private val mAxes: IntArray

        /**
         * Axis values reported to [onJoystickMotion] in the last received [MotionEvent].
         */
        private val mAxisValues: FloatArray

        /**
         * [SparseIntArray] holding the state of the keys of our device (1 for pressed, 0 for
         * not pressed), indexed by the key code of the key events received (this is the physical
         * key that was pressed, not the Unicode character).
         */
        private val mKeys: SparseIntArray

        /**
         * Number of axis supported by the device, which is just the size of our [mAxes] field.
         */
        val axisCount: Int
            get() = mAxes.size

        /**
         * Getter for the Axis ID from the [mAxes] array field (the entry at index [axisIndex])
         *
         * @param axisIndex index for axis we are looking for
         * @return Axis ID of axis at index [axisIndex] of our device
         */
        fun getAxis(axisIndex: Int): Int {
            return mAxes[axisIndex]
        }

        /**
         * Getter for the current value of the axis at index [axisIndex] of our device.
         *
         * @param axisIndex index for axis we are looking for
         * @return Current value of the axis at index [axisIndex] of our device
         */
        fun getAxisValue(axisIndex: Int): Float {
            return mAxisValues[axisIndex]
        }

        /**
         * Getter for the number of key-value mappings in the [SparseIntArray] array field [mKeys].
         *
         * @return number of key-value mappings in the [SparseIntArray] array field [mKeys].
         */
        val keyCount: Int
            get() = mKeys.size()

        /**
         * Given an index in the range 0...size()-1, returns the keycode from the [keyIndex]
         * key-value mapping that [SparseIntArray] field [mKeys] stores. We simply return the
         * key that the `keyAt` method of [mKeys] returns ([mKeys] stores the state of that
         * keycode using the keycode as the key).
         *
         * @param keyIndex index (0...size()-1) of the key we are interested in
         * @return keycode that is the key in the [SparseIntArray] field [mKeys] array for index
         * [keyIndex].
         */
        fun getKeyCode(keyIndex: Int): Int {
            return mKeys.keyAt(keyIndex)
        }

        /**
         * Returns true if the key stored in the [keyIndex] entry (for [keyIndex] (0...size()-1) of
         * the [SparseIntArray] field [mKeys]) of `mKeys` is pressed, false otherwise. We
         * simply return the result of checking whether the value returned by the `valueAt`
         * method of [mKeys] for [keyIndex] is not 0.
         *
         * @param keyIndex index (0...size()-1) of the key we are interested in
         * @return true if the key is pressed, false if it is not.
         */
        fun isKeyPressed(keyIndex: Int): Boolean {
            return mKeys.valueAt(keyIndex) != 0
        }

        /**
         * Called by our [dispatchKeyEvent] override to determine if the keycode which generated
         * the [KeyEvent] it received is one that our game is interested in, and also to record
         * the state of that key in our [SparseIntArray] field [mKeys] if it is one. We first fetch
         * the keycode from the [KeyEvent] to initialize our variable `val keyCode`. If our
         * [isGameKey] method determines that it is a key our game is interested in we do some
         * more processing (if not, we return false to the caller). If the repeat count of our
         * [KeyEvent] parameter [event] is 0, we put the value 1 into the [mKeys] array under the
         * key `keyCode`, set [String] `val symbolicName` to the symbolic name of the keycode, log
         * a message about the "Key Down" occurrence, and in both cases return true to the caller.
         *
         * @param event [KeyEvent] received by our [dispatchKeyEvent] override.
         * @return true if the keycode is one our game is interested in, false if not.
         */
        fun onKeyDown(event: KeyEvent): Boolean {
            val keyCode = event.keyCode
            if (isGameKey(keyCode)) {
                if (event.repeatCount == 0) {
                    mKeys.put(keyCode, 1)
                    val symbolicName = KeyEvent.keyCodeToString(keyCode)
                    Log.i(TAG, device.name + " - Key Down: " + symbolicName)
                }
                return true
            }
            return false
        }

        /**
         * Called by our [dispatchKeyEvent] override to determine if the keycode which generated
         * the [KeyEvent] it received is one that our game is interested in, and also to record
         * the state of that key in our [SparseIntArray] field [mKeys] if it is one. We first fetch
         * the keycode from the [KeyEvent] to initialize our variable `val keyCode`. If our
         * [isGameKey] method determines that it is a key our game is interested in we do some
         * more processing (if not, we return false to the caller). We first make sure the keycode
         * is already in our [mKeys] array by calling the `indexOfKey` method of [mKeys], and if the
         * result is less that 0 it is not in the array, so we do not record of log its state. If it
         * is already in the array (a key down event has previously occurred), we put the value 0
         * into the [mKeys] array under the key `keyCode`, set [String] `val symbolicName` to the
         * symbolic name of the keycode, log a message about the "Key Up" occurrence, and in both
         * cases return true to the caller.
         *
         * @param event [KeyEvent] received by our [dispatchKeyEvent] override.
         * @return true if the keycode is one our game is interested in, false if not.
         */
        fun onKeyUp(event: KeyEvent): Boolean {
            val keyCode = event.keyCode
            if (isGameKey(keyCode)) {
                val index = mKeys.indexOfKey(keyCode)
                if (index >= 0) {
                    mKeys.put(keyCode, 0)
                    val symbolicName = KeyEvent.keyCodeToString(keyCode)
                    Log.i(TAG, device.name + " - Key Up: " + symbolicName)
                }
                return true
            }
            return false
        }

        /**
         * Called by our [dispatchGenericMotionEvent] override to record and log the contents of the
         * [MotionEvent] it received. First we create [StringBuilder] `val message` and append to it
         * a string consisting of the name of the device we are following with the string "Joystick
         * Motion" appended to it. We initialize `val historySize` with the the number of historical
         * points in  [MotionEvent] parameter [event]. Then we loop over `i` for all the axis in our
         * [mAxes] array, setting `val axis` to the axis identifier contained in the i'th entry in
         * [mAxes], and `val value` to the value of that axis contained in the [MotionEvent] parameter
         * [event]. We then save this value in the i'th position in our [mAxisValues] array, and
         * append a string consisting of the symbolic name of the axis followed by a ":" to
         * [StringBuilder] `message`. We now loop through all the historical axis values for
         * that axis appending all the values for it separated by a "," to `message`. Finally we
         * append the value `value` and a "\n" to `message` and loop back for the next axis.
         *
         * When done with all the axis we log the string value `message` under our tag `TAG`,
         * and return true to the caller.
         *
         * @param event [MotionEvent] received by our [dispatchGenericMotionEvent] override
         * @return always returns true.
         */
        fun onJoystickMotion(event: MotionEvent): Boolean {
            val message = StringBuilder()
            message.append(device.name).append(" - Joystick Motion:\n")
            val historySize = event.historySize
            for (i in mAxes.indices) {
                val axis = mAxes[i]
                val value = event.getAxisValue(axis)
                mAxisValues[i] = value
                message.append("  ").append(MotionEvent.axisToString(axis)).append(": ")

                // Append all historical values in the batch.
                for (historyPos in 0 until historySize) {
                    message.append(event.getHistoricalAxisValue(axis, historyPos))
                    message.append(", ")
                }

                // Append the current value.
                message.append(value)
                message.append("\n")
            }
            Log.i(TAG, message.toString())
            return true
        }

        companion object {
            /**
             * Check whether this is a key we care about. We switch on [keyCode] returning true
             * if the key is one of KEYCODE_DPAD_UP, KEYCODE_DPAD_DOWN, KEYCODE_DPAD_LEFT,
             * KEYCODE_DPAD_RIGHT, KEYCODE_DPAD_CENTER, or KEYCODE_SPACE. Otherwise we return
             * the result of calling the `KeyEvent.isGamepadButton` method for [keyCode] to the
             * caller.
             *
             * @param keyCode keycode we are to check for
             * @return true if [keyCode] is one of the six keycodes we are interested in, false if not.
             */
            private fun isGameKey(keyCode: Int): Boolean {
                return when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_SPACE -> true
                    else -> KeyEvent.isGamepadButton(keyCode)
                }
            }
        }

        /**
         * The init block of our constructor. First we initialize our variable `var numAxes` to 0,
         * and initialize `List<MotionRange>` `val ranges` to the ranges for all axes supported by
         * the device `device`. We loop over the `MotionRange range` in `ranges` (if any), and if
         * `range` is from a SOURCE_CLASS_JOYSTICK input device (a joystick) we increment `numAxes`.
         *
         * We next allocate `numAxes` entries for our `IntArray` field `mAxes` and `FloatArray`
         * field `mAxisValues`. We initialize our variable `var i` to 0, then once again loop over
         * the `MotionRange` `var range` in `ranges` (if any), and if `range` is from a
         * SOURCE_CLASS_JOYSTICK input device (a joystick) we set `mAxes[i++]` to the axis ID of
         * `range`. Finally we initialize `SparseIntArray` field `mKeys` with a new instance.
         */
        init {
            var numAxes = 0
            val ranges = device.motionRanges
            for (range in ranges) {
                if (range.isFromSource(InputDevice.SOURCE_CLASS_JOYSTICK)) {
                    numAxes += 1
                }
            }
            mAxes = IntArray(numAxes)
            mAxisValues = FloatArray(numAxes)
            var i = 0
            for (range in ranges) {
                if (range.isFromSource(InputDevice.SOURCE_CLASS_JOYSTICK)) {
                    mAxes[i++] = range.axis
                }
            }
            mKeys = SparseIntArray()
        }
    }

    /**
     * A list adapter that displays a summary of the device state.
     */
    private class SummaryAdapter(
            /**
             * [Context]` to use toast a message, this when constructed in the `onCreate`
             * method of the [GameControllerInput] activity.
             */
            private val mContext: Context,
            /**
             * [Resources] instance to use to access resources, the value returned by the
             * `getResources` method when constructed in the `onCreate` method of the
             * [GameControllerInput] activity.
             */
            private val mResources: Resources) : BaseAdapter() {

        /**
         * [SparseArray] used to hold both axes, and keycode [Item] objects, indexed by
         * BASE_ID_AXIS_ITEM or'ed with the axis ID for axes, and indexed by BASE_ID_KEY_ITEM or'ed
         * with the keyCode for keys.
         */
        private val mDataItems = SparseArray<Item>()

        /**
         * The list shown in our [ListView]. It is populated with [Item] objects to display
         * the [InputDeviceState] of the last device that received a [KeyEvent] or [MotionEvent]
         * we are interested in.
         */
        private val mVisibleItems = ArrayList<Item>()

        /**
         * [Heading] for the device heading row (the string "Input Device" will be written to
         * its [TextView] when the [Heading.initView] method is called.
         */
        private val mDeviceHeading: Heading

        /**
         * `TextColumn` for the device name row, consists of two `TextView` views, one
         * with the string "Name", and the other set to the name of the device whose event we are
         * displaying.
         */
        private val mDeviceNameTextColumn: TextColumn

        /**
         * `Heading` for the axes heading row (the string "Axes" will be written to its
         * `TextView` when the `Heading.initView` method is called.
         */
        private val mAxesHeading: Heading

        /**
         * `Heading` for the keys heading row (the string "Keys and Buttons" will be written
         * to its `TextView` when the `Heading.initView` method is called).
         */
        private val mKeysHeading: Heading

        /**
         * `InputDeviceState` we are currently displaying, it is passed to our `show`
         * method when a new event we are interested in is received by our callbacks.
         */
        private var mState: InputDeviceState? = null

        /**
         * Called from the `OnItemClickListener` of `ListView mSummaryList` (our
         * `ListView`). If our field `InputDeviceState mState` is not null we toast a
         * message which displays the string value of the `InputDevice mDevice` field in
         * `mState`.
         *
         * @param position position in our `ListView` that was clicked UNUSED
         */
        @Suppress("UNUSED_PARAMETER")
        fun onItemClick(position: Int) {
            if (mState != null) {
                val toast = Toast.makeText(mContext, mState!!.device.toString(), Toast.LENGTH_LONG)
                toast.show()
            }
        }

        /**
         * Called from our `dispatchKeyEvent` and `dispatchGenericMotionEvent` to display
         * the `InputDeviceState` of the device which just received a `KeyEvent` or a
         * `MotionEvent` that we are interested in. First we save our parameter
         * `InputDeviceState state` in our field `InputDeviceState mState`, then we clear
         * our field `ArrayList<Item> mVisibleItems` which contains the list of `Item`
         * objects we are displaying.
         *
         *
         * We begin to rebuild `mVisibleItems` by first adding our `Heading mDeviceHeading`
         * (a `TextView` with the text "Input Device"), setting the text of the content view
         * of `TextColumn mDeviceNameTextColumn` to the device name of `InputDeviceState state`,
         * and adding `mDeviceNameTextColumn` to `mVisibleItems`.
         *
         *
         * Next we populate `mVisibleItems` with the axes information by first adding our
         * `Heading mAxesHeading` (a TextView with the text "Axes"), then looping over all of
         * the axes contained in `state` we fetch each `int axis`, form `int id`
         * from it by or'ing it with BASE_ID_AXIS_ITEM, and trying to get the `TextColumn column`
         * for that `id` from our field `SparseArray<Item> mDataItems`. If the result is
         * null we create a new `TextColumn` for `column` from `id` and the string
         * value of the `axis` and put that `column` in `mDataItems` under the key
         * `id`. We now set the content `TextView` of `column` to the string value
         * of the axis value and add `column` to `mVisibleItems`.
         *
         *
         * Next we populate `mVisibleItems` with the keys information by first adding our
         * `Heading mKeysHeading` (a TextView with the text "Keys and Buttons"), then looping
         * over all of keys in `state` we fetch each `int keyCode`, form `int id`
         * from it by or'ing it with BASE_ID_KEY_ITEM, and trying to get the `TextColumn column`
         * for that `id` from our field `SparseArray<Item> mDataItems`. If the result is
         * null we create a new `TextColumn` for `column` from `id` and the string
         * value of the `keyCode` and put that `column` in `mDataItems` under the
         * key `id`. We now set the content `TextView` of `column` to the string
         * R.string.game_controller_input_key_pressed ("Pressed") if the key in `state` is
         * pressed, or R.string.game_controller_input_key_released ("Released") if it is not pressed
         * and add `column` to `mVisibleItems`.
         *
         *
         * After doing all this we call the method `notifyDataSetChanged` to notify the system
         * that the underlying data has been changed and any View reflecting the data set should
         * refresh itself.
         *
         * @param state `InputDeviceState` of the device which just received an event we are
         * interested in.
         */
        fun show(state: InputDeviceState) {
            mState = state
            mVisibleItems.clear()

            // Populate device information.
            mVisibleItems.add(mDeviceHeading)
            mDeviceNameTextColumn.setContent(state.device.name)
            mVisibleItems.add(mDeviceNameTextColumn)

            // Populate axes.
            mVisibleItems.add(mAxesHeading)
            val axisCount = state.axisCount
            for (i in 0 until axisCount) {
                val axis = state.getAxis(i)
                val id = BASE_ID_AXIS_ITEM or axis
                var column = mDataItems[id] as TextColumn
                @Suppress("SENSELESS_COMPARISON")
                if (column == null) {
                    column = TextColumn(id, MotionEvent.axisToString(axis))
                    mDataItems.put(id, column)
                }
                column.setContent(state.getAxisValue(i).toString())
                mVisibleItems.add(column)
            }

            // Populate keys.
            mVisibleItems.add(mKeysHeading)
            val keyCount = state.keyCount
            for (i in 0 until keyCount) {
                val keyCode = state.getKeyCode(i)
                val id = BASE_ID_KEY_ITEM or keyCode
                var column = mDataItems[id] as TextColumn
                @Suppress("SENSELESS_COMPARISON")
                if (column == null) {
                    column = TextColumn(id, KeyEvent.keyCodeToString(keyCode))
                    mDataItems.put(id, column)
                }
                column.setContent(mResources.getString(if (state.isKeyPressed(i)) R.string.game_controller_input_key_pressed else R.string.game_controller_input_key_released))
                mVisibleItems.add(column)
            }
            notifyDataSetChanged()
        }

        /**
         * Indicates whether the item ids are stable across changes to the underlying data. We just
         * return true.
         *
         * @return True since the same id always refers to the same object.
         */
        override fun hasStableIds(): Boolean {
            return true
        }

        /**
         * How many items are in the data set represented by this Adapter. We return the size of our
         * field `ArrayList<Item> mVisibleItems`.
         *
         * @return Count of items.
         */
        override fun getCount(): Int {
            return mVisibleItems.size
        }

        /**
         * Get the data item associated with the specified position in the data set. We return the
         * contents of our field `ArrayList<Item> mVisibleItems` at position `position`.
         *
         * @param position Position of the item within the adapter's data set that is wanted.
         * @return The data at the specified position.
         */
        override fun getItem(position: Int): Item {
            return mVisibleItems[position]
        }

        /**
         * Get the row id associated with the specified position in the list. We call our method
         * `getItem` to get the `Item` at position `position`, and return the
         * ID of that `Item` that its `getItemId` method returns.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        override fun getItemId(position: Int): Long {
            return getItem(position).mItemId.toLong()
        }

        /**
         * Get a View that displays the data at the specified position in the data set. We call our
         * method `getItem` to get the `Item` at position `position`, and return
         * the result of calling that objects `getView` method, which either returns the View
         * that that object has already been using, or one it creates and initializes.
         *
         * @param position    The position of the item within the adapter's data set of the item
         * whose view we want.
         * @param convertView The old view to reuse, if possible.
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            return getItem(position).getView(convertView, parent)!!
        }

        /**
         * Abstract base class for the `Heading`, and `TextColumn` classes.
         */
        private abstract class Item
        /**
         * Our constructor. We merely save our parameters `itemId` in our field `mItemId`,
         * and `layoutResourceId` in our field `mLayoutResourceId`.
         *
         * Parameter: itemId           Stable Item ID for this `Item` instance
         * Parameter: layoutResourceId Resource ID pointing to a layout file to display our information.
         */(
                /**
                 * Stable Item ID that is returned by our method `getItemId`, which is called from
                 * the `getItemId` method of the `SummaryAdapter` which is using us. It is
                 * set to one of its parameters by our constructor.
                 */
                val mItemId: Int,
                /**
                 * Resource ID for the layout which will display our information. It is set to one of
                 * its parameters by our constructor.
                 */
                private val mLayoutResourceId: Int) {

            /**
             * `View` which is displaying our information. If one does not already exist, our
             * `getView` method will create one by inflating the layout file pointed to by
             * our field `mLayoutResourceId`.
             */
            private var mView: View? = null

            /**
             * Returns the stable ID of this `Item` that is stored in our field `mItemId`.
             *
             * @return The stable ID of this `Item`
             */
            @Suppress("unused")
            val itemId: Long
                get() = mItemId.toLong()

            /**
             * Returns a `View` updated to hold our latest information. If our field
             * `View mView` is null, we initialize `LayoutInflater inflater` with an
             * instance of the system level service LAYOUT_INFLATER_SERVICE, and use it to inflate
             * our layout file `mLayoutResourceId` using our parameter `parent` as the
             * object that provides a set of LayoutParams values for root of the returned hierarchy
             * to create a new instance for `mView`, and call our method `initView` to
             * initialize `mView`.
             *
             *
             * In either case we call our method `updateView` to update the information displayed
             * by `mView` and return `mView` to the caller.
             *
             * @param convertView UNUSED The old view to reuse, if possible.
             * @param parent      The parent that this view will eventually be attached to
             * @return `View` holding our information.
             */
            @Suppress("UNUSED_PARAMETER")
            fun getView(convertView: View?, parent: ViewGroup): View? {
                if (mView == null) {
                    val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    mView = inflater.inflate(mLayoutResourceId, parent, false)
                    initView(mView)
                }
                updateView(mView)
                return mView
            }

            /**
             * Derived classes should override this to do any class specific initialization.
             *
             * @param view `View` which will hold the information for this `Item`
             */
            protected open fun initView(view: View?) {}

            /**
             * Derived classes should override this to do any class specific updating of its view.
             *
             * @param view `View` which will hold the information for this `Item`
             */
            protected open fun updateView(view: View?) {}

        }

        /**
         * `Item` which displays constant heading text in a `TextView`.
         */
        private class Heading
        /**
         * Our constructor. First we call our super's constructor with our parameter `itemId`
         * and our layout resource R.layout.game_controller_input_heading (consists of a single
         * `TextView` with no ID). Then we save our parameter `String label` in our
         * field `String mLabel`.
         *
         * Parameter: itemId Stable ID for this `Item`
         * Parameter: label  String to display in our `TextView`
         */(itemId: Int,
            /**
             * String we are supposed to display in our `TextView`
             */
            private val mLabel: String) : Item(itemId, R.layout.game_controller_input_heading) {

            /**
             * Initializes our view by setting the text of its `TextView` to the string in our
             * field `mLabel`
             *
             * @param view `View` which will hold the information for this `Item`
             */
            public override fun initView(view: View?) {
                val textView = view as TextView?
                textView!!.text = mLabel
            }

        }

        /**
         * `Item` which displays constant heading text in one `TextView` and varying
         * information from its field `String mContent` in a second one.
         */
        private class TextColumn
        /**
         * Our constructor. First we call our super's constructor with our parameter `itemId`
         * and our layout resource R.layout.game_controller_input_text_column (consists of a
         * horizontal `LinearLayout` with two `TextView` objects with the ID's
         * R.id.label (for the constant heading), and R.id.content (for the varying information).
         * Then we save our parameter `String label` in our field `String mLabel`.
         *
         * @param itemId Stable ID for this `Item`
         * Parameter: label  String to display in our constant first `TextView`
         */(itemId: Int,
            /**
             * Constant string to display in our first `TextView`, set by our constructor.
             */
            private val mLabel: String) : Item(itemId, R.layout.game_controller_input_text_column) {

            /**
             * Varying information to display in our second `TextView`, set by calling our
             * method `setContent`.
             */
            private var mContent: String? = null

            /**
             * Point to the `TextView` in our layout that is used for varying information.
             */
            private var mContentView: TextView? = null

            /**
             * Saves the value of its parameter `String content` in our field `mContent`.
             *
             * @param content Varying information that we are to display in our `TextView`
             * with ID R.id.content (`mContentView`)
             */
            fun setContent(content: String?) {
                mContent = content
            }

            /**
             * Initializes this `TextColumn` object. We initialize `TextView textView` by
             * finding the view in `view` with ID R.id.label and set its text to our field
             * `mLabel`. We then initialize our field `TextView mContentView` by finding
             * the view in `view` with ID R.id.content.
             *
             * @param view `View` which will hold the information for this `Item`
             */
            public override fun initView(view: View?) {
                val textView = view!!.findViewById<TextView>(R.id.label)
                textView.text = mLabel
                mContentView = view.findViewById(R.id.content)
            }

            /**
             * Updates the text displayed in `TextView mContentView` with the latest contents
             * of `String mContent`.
             *
             * @param view `View` which will hold the information for this `Item`
             */
            public override fun updateView(view: View?) {
                mContentView!!.text = mContent
            }

        }

        companion object {
            /**
             * Base of the row ID's used for the headings for device (0), axes (1), and keys (2)
             */
            private const val BASE_ID_HEADING = 1 shl 10

            /**
             * Base of the row ID used for the device name `TextColumn` item
             */
            private const val BASE_ID_DEVICE_ITEM = 2 shl 10

            /**
             * Base of the row ID used for the individual axes items
             */
            private const val BASE_ID_AXIS_ITEM = 3 shl 10

            /**
             * Base of the row ID used for the "keys" items (the keycode is or'ed with it)
             */
            private const val BASE_ID_KEY_ITEM = 4 shl 10
        }

        /**
         * Our constructor. First we save our parameters `Context context` in our field
         * `Context mContext`, and `Resources resources` in our field
         * `Resources mResources`. We initialize our field `Heading mDeviceHeading` with
         * a new instance using the row ID BASE_ID_HEADING or'ed with 0 (1024) and the string
         * R.string.game_controller_input_label_device_name ("Input Device"). We initialize our field
         * `TextColumn mDeviceNameTextColumn` with a new instance using the row ID BASE_ID_DEVICE_ITEM
         * or'ed with 0 (2048), and the string R.string.game_controller_input_label_device_name ("Name").
         * We initialize our field `Heading mAxesHeading` with a new instance using the row ID
         * BASE_ID_HEADING or'ed with 1 (1025) and the string R.string.game_controller_input_heading_axes
         * ("Axes"). We initialize our field `Heading mKeysHeading` with a new instance using the row ID
         * BASE_ID_HEADING or'ed with 2 (1026) and the string R.string.game_controller_input_heading_keys
         * ("Keys and Buttons")
         *
         * Parameter: context   `Context` to use to toast messages
         * Parameter: resources `Resources` instance to use to access resources.
         */
        init {
            mDeviceHeading = Heading(BASE_ID_HEADING or 0,
                    mResources.getString(R.string.game_controller_input_heading_device))
            mDeviceNameTextColumn = TextColumn(BASE_ID_DEVICE_ITEM or 0,
                    mResources.getString(R.string.game_controller_input_label_device_name))
            mAxesHeading = Heading(BASE_ID_HEADING or 1,
                    mResources.getString(R.string.game_controller_input_heading_axes))
            mKeysHeading = Heading(BASE_ID_HEADING or 2,
                    mResources.getString(R.string.game_controller_input_heading_keys))
        }
    }

    companion object {
        /**
         * TAG used for logging.
         */
        private const val TAG = "GameControllerInput"
    }
}