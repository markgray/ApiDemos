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

package com.example.android.apis.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.input.InputManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.InputDevice;
import android.view.InputDevice.MotionRange;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates how to process input events received from game controllers.
 * It also shows how to detect when input devices are added, removed or reconfigured.
 * <p>
 * This activity displays button states and joystick positions.
 * Also writes detailed information about relevant input events to the log.
 * <p>
 * The game controller is also uses to control a very simple game.  See {@code GameView}
 * for the game itself, it is used by our layout file R.layout.game_controller_input.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class GameControllerInput extends AppCompatActivity implements InputManager.InputDeviceListener {
    /**
     * TAG used for logging.
     */
    private static final String TAG = "GameControllerInput";

    /**
     * {@code InputManager} for interacting with input devices.
     */
    private InputManager mInputManager;
    /**
     * Array of {@code InputDeviceState} Objects using the device ID as the index.
     */
    private SparseArray<InputDeviceState> mInputDeviceStates;
    /**
     * Reference to the {@code GameView} in our layout file with ID R.id.game
     */
    private GameView mGame;
    /**
     * {@code ListView} in our layout file with ID R.id.summary, fed by the adapter
     * {@code SummaryAdapter mSummaryAdapter}, we use it to output information we
     * receive as an {@code InputDeviceListener} in a very nifty way.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private ListView mSummaryList;
    /**
     * {@code SummaryAdapter} which feeds {@code ListView mSummaryList}, displays textual representations
     * of {@code InputDeviceState} Objects our {@code InputDeviceListener} interface receives.
     */
    private SummaryAdapter mSummaryAdapter;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.game_controller_input.
     * Then we initialize our field {@code InputManager mInputManager} with an new instance for
     * interacting with input devices. Next we initialize our fields
     * {@code SparseArray<InputDeviceState> mInputDeviceStates} with a new instance of {@code SparseArray},
     * and {@code SummaryAdapter mSummaryAdapter} with a new instance of {@code SummaryAdapter} using
     * this as the {@code Context} and a resources instance for our application's package.
     * <p>
     * We initialize our field {@code GameView mGame} by finding the view with ID R.id.game, and our
     * field {@code ListView mSummaryList} by finding the view with ID R.id.summary. We set the adapter
     * of {@code mSummaryList} to {@code mSummaryAdapter}, and its {@code OnItemClickListener} to an
     * anonymous class which calls through to the {@code onItemClick} method of {@code mSummaryAdapter}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_controller_input);

        mInputManager = (InputManager) getSystemService(Context.INPUT_SERVICE);

        mInputDeviceStates = new SparseArray<>();
        mSummaryAdapter = new SummaryAdapter(this, getResources());

        mGame = findViewById(R.id.game);

        mSummaryList = findViewById(R.id.summary);
        mSummaryList.setAdapter(mSummaryAdapter);
        mSummaryList.setOnItemClickListener(
                (parent, view, position, id) -> mSummaryAdapter.onItemClick(position)
        );
    }

    /**
     * Called after {@code onRestoreInstanceState}, {@code onRestart}, or {@code onPause}, for our
     * activity to start interacting with the user. First we call through to our super's implementation
     * of {@code onResume}. We then use {@code InputManager mInputManager} to register "this" as an
     * input device listener to watch for when input devices are added, removed or reconfigured. Next
     * we use {@code mInputManager} to fetch the ids of all input devices in the system to the array
     * {@code int[] ids}. Then we loop for each {@code int id} in {@code int[] ids} calling our method
     * {@code getInputDeviceState} for it. {@code getInputDeviceState} maintains the {@code SparseArray}
     * {@code SparseArray<InputDeviceState> mInputDeviceStates} and if the device ID it is called for
     * is not already in the array, gets information about that input device from {@code mInputManager}
     * in a {@code InputDevice}, creates a {@code InputDeviceState} from it and stores it in the array
     * {@code mInputDeviceStates}. If the device ID already had an entry it just returns that to the
     * caller.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Register an input device listener to watch when input devices are
        // added, removed or reconfigured.
        mInputManager.registerInputDeviceListener(this, null);

        // Query all input devices.
        // We do this so that we can see them in the log as they are enumerated.
        int[] ids = mInputManager.getInputDeviceIds();
        for (int id : ids) {
            getInputDeviceState(id);
        }
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. We call through to our super's implementation of {@code onPause}
     * then use {@code InputManager mInputManager} to unregister "this" as an {@code InputDeviceListener}.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Remove the input device listener when the activity is paused.
        mInputManager.unregisterInputDeviceListener(this);
    }

    /**
     * Called when the current {@code Window} of the activity gains or loses focus. This is the best
     * indicator of whether this activity is visible to the user. First we call through to our super's
     * implementation of {@code onWindowFocusChanged}, then we request focus for our field
     * {@code GameView mGame}.
     *
     * @param hasFocus Whether the window of this activity has focus.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        mGame.requestFocus();
    }

    /**
     * Called to process key events. You can override this to intercept all key events before they
     * are dispatched to the window. First we call our method {@code getInputDeviceState} to initialize
     * {@code InputDeviceState state} with the {@code InputDeviceState} of the device ID that generated
     * our parameter {@code KeyEvent event}. If {@code state} is not null, we switch on the value of
     * the action of {@code event}:
     * <ul>
     * <li>
     * ACTION_DOWN - if the {@code onKeyDown} method of {@code state} returns true (the key is
     * a key used by the game), we call the {@code show} method of {@code SummaryAdapter mSummaryAdapter}
     * with {@code state} as the argument. We then break
     * </li>
     * <li>
     * ACTION_UP - if the {@code onKeyUp} method of {@code state} returns true (the key is
     * a key used by the game), we call the {@code show} method of {@code SummaryAdapter mSummaryAdapter}
     * with {@code state} as the argument. We then break
     * </li>
     * </ul>
     * Finally we return the value returned by our super's implementation of {@code dispatchKeyEvent} to
     * our caller.
     *
     * @param event The key event.
     * @return boolean Return true if this event was consumed.
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Update device state for visualization and logging.
        InputDeviceState state = getInputDeviceState(event.getDeviceId());
        if (state != null) {
            switch (event.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    if (state.onKeyDown(event)) {
                        mSummaryAdapter.show(state);
                    }
                    break;
                case KeyEvent.ACTION_UP:
                    if (state.onKeyUp(event)) {
                        mSummaryAdapter.show(state);
                    }
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * Called to process generic motion events. You can override this to intercept all generic motion
     * events before they are dispatched to the window. Only if the {@code MotionEvent event} is from
     * a SOURCE_CLASS_JOYSTICK input source (a joystick) and the action of {@code event} is ACTION_MOVE
     * we call our method {@code getInputDeviceState} to initialize {@code InputDeviceState state} with
     * the {@code InputDeviceState} of the device ID that generated our parameter {@code KeyEvent event}.
     * Then if {@code state} is not null, and its {@code onJoystickMotion} method returns true given
     * {@code MotionEvent event} (it always returns true) we call the {@code show} method of
     * {@code SummaryAdapter mSummaryAdapter} with {@code state} as the argument. Finally we return
     * the value returned by our super's implementation of {@code dispatchGenericMotionEvent} to our
     * caller.
     *
     * @param event The generic motion event.
     * @return boolean Return true if this event was consumed.
     */
    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        // Check that the event came from a joystick since a generic motion event
        // could be almost anything.
        if (event.isFromSource(InputDevice.SOURCE_CLASS_JOYSTICK)
                && event.getAction() == MotionEvent.ACTION_MOVE) {
            // Update device state for visualization and logging.
            InputDeviceState state = getInputDeviceState(event.getDeviceId());
            if (state != null && state.onJoystickMotion(event)) {
                mSummaryAdapter.show(state);
            }
        }
        return super.dispatchGenericMotionEvent(event);
    }

    /**
     * Returns an {@code InputDeviceState} object for {@code deviceId}, from the cache contained in
     * {@code SparseArray<InputDeviceState> mInputDeviceStates}, or freshly created (and cached for
     * future calls. First we try to initialize {@code InputDeviceState state} by using {@code deviceId}
     * to fetch it from our cache {@code SparseArray<InputDeviceState> mInputDeviceStates}. If it is
     * null, we use {@code InputManager mInputManager} to initialize {@code InputDevice device} with
     * information about the input device with ID {@code deviceId}. If {@code device} is null, we
     * return null to our caller. Otherwise we set {@code state} to a new instance of {@code InputDeviceState}
     * created from {@code device}, store {@code state} in our cache {@code mInputDeviceStates} under the
     * index {@code deviceId}, and log the new device. Finally we return {@code state} to our caller.
     *
     * @param deviceId Device ID
     * @return an {@code InputDeviceState} object for {@code deviceId}, from the cache contained in
     * {@code SparseArray<InputDeviceState> mInputDeviceStates}, or freshly created (and cached for
     * future calls.
     */
    private InputDeviceState getInputDeviceState(int deviceId) {
        InputDeviceState state = mInputDeviceStates.get(deviceId);
        if (state == null) {
            final InputDevice device = mInputManager.getInputDevice(deviceId);
            if (device == null) {
                return null;
            }
            state = new InputDeviceState(device);
            mInputDeviceStates.put(deviceId, state);
            Log.i(TAG, "Device enumerated: " + state.mDevice);
        }
        return state;
    }

    /**
     * Called whenever an input device has been added to the system, part of the {@code InputDeviceListener}
     * interface. We call our method {@code getInputDeviceState} to initialize {@code InputDeviceState state}
     * with the {@code InputDeviceState} of the device ID of our parameter {@code deviceId}. We then
     * log the string value of the {@code InputDevice} object for that device.
     *
     * @param deviceId The id of the input device that was added.
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    public void onInputDeviceAdded(int deviceId) {
        InputDeviceState state = getInputDeviceState(deviceId);
        Log.i(TAG, "Device added: " + state.mDevice);
    }

    /**
     * Called whenever the properties of an input device have changed since they were last queried,
     * part of the {@code InputDeviceListener} interface. part of the {@code InputDeviceListener} interface
     * If {@code state} is not null, we remove the entry for {@code deviceId} from our cache
     * {@code SparseArray<InputDeviceState> mInputDeviceStates}, and call our method {@code getInputDeviceState}
     * to create a new {@code InputDeviceState} for {@code deviceId} to set {@code state} to (and cache it).
     * We then log the string value of the {@code InputDevice} object for that device.
     *
     * @param deviceId The id of the input device that changed.
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    public void onInputDeviceChanged(int deviceId) {
        InputDeviceState state = mInputDeviceStates.get(deviceId);
        if (state != null) {
            mInputDeviceStates.remove(deviceId);
            state = getInputDeviceState(deviceId);
            Log.i(TAG, "Device changed: " + state.mDevice);
        }
    }

    /**
     * Called whenever an input device has been removed from the system, part of the {@code InputDeviceListener}
     * interface. We call our method {@code getInputDeviceState} to initialize {@code InputDeviceState state}
     * with the {@code InputDeviceState} of the device ID of our parameter {@code deviceId}. If {@code state}
     * is not null we log the string value of the {@code InputDevice} object for that device, then remove
     * the entry for {@code deviceId} from our cache {@code SparseArray<InputDeviceState> mInputDeviceStates}.
     *
     * @param deviceId The id of the input device that was removed.
     */
    @Override
    public void onInputDeviceRemoved(int deviceId) {
        InputDeviceState state = mInputDeviceStates.get(deviceId);
        if (state != null) {
            Log.i(TAG, "Device removed: " + state.mDevice);
            mInputDeviceStates.remove(deviceId);
        }
    }

    /**
     * Tracks the state of joystick axes and game controller buttons for a particular
     * input device for diagnostic purposes.
     */
    @SuppressWarnings("WeakerAccess")
    private static class InputDeviceState {
        /**
         * The {@code InputDevice} that we were created for, set in our constructor.
         */
        private final InputDevice mDevice;
        /**
         * Axis ID's if we are created for a SOURCE_CLASS_JOYSTICK (joystick).
         */
        private final int[] mAxes;
        /**
         * Axis values reported to {@code onJoystickMotion} in the last received {@code MotionEvent}.
         */
        private final float[] mAxisValues;
        /**
         * {@code SparseIntArray} holding the state of the keys of our device (1 for pressed, 0 for
         * not pressed), indexed by the key code of the key events received (this is the physical key
         * that was pressed, not the Unicode character).
         */
        private final SparseIntArray mKeys;

        /**
         * Our constructor. First we save our parameter {@code InputDevice device} in our field
         * {@code InputDevice mDevice}. We initialize our variable {@code int numAxes} to 0, and
         * initialize {@code List<MotionRange> ranges} to the ranges for all axes supported by the
         * device {@code device}. We loop over the {@code MotionRange range} in {@code ranges} (if
         * any), and if {@code range} is from a SOURCE_CLASS_JOYSTICK input device (a joystick) we
         * increment {@code numAxes}.
         * <p>
         * We next allocate {@code numAxes} entries for our fields {@code int[] mAxes} and
         * {@code float[] mAxisValues}. We initialize our variable {@code int i} to 0, then once
         * again loop over the {@code MotionRange range} in {@code ranges} (if any), and if
         * {@code range} is from a SOURCE_CLASS_JOYSTICK input device (a joystick) we set
         * {@code mAxes[i++]} to the axis ID of {@code range}.
         * <p>
         * Finally we initialize {@code SparseIntArray mKeys} with a new instance.
         *
         * @param device {@code InputDevice} we are created for.
         */
        public InputDeviceState(InputDevice device) {
            mDevice = device;

            int numAxes = 0;
            final List<MotionRange> ranges = device.getMotionRanges();
            for (MotionRange range : ranges) {
                if (range.isFromSource(InputDevice.SOURCE_CLASS_JOYSTICK)) {
                    numAxes += 1;
                }
            }

            mAxes = new int[numAxes];
            mAxisValues = new float[numAxes];
            int i = 0;
            for (MotionRange range : ranges) {
                if (range.isFromSource(InputDevice.SOURCE_CLASS_JOYSTICK)) {
                    mAxes[i++] = range.getAxis();
                }
            }

            mKeys = new SparseIntArray();
        }

        /**
         * Getter for our {@code InputDevice mDevice} field.
         *
         * @return the value of our {@code InputDevice mDevice} field
         */
        public InputDevice getDevice() {
            return mDevice;
        }

        /**
         * Getter for the length of the {@code int[] mAxes} array (number of axis supported by the
         * device).
         *
         * @return Number of axis supported by the device
         */
        public int getAxisCount() {
            return mAxes.length;
        }

        /**
         * Getter for the Axis ID from the {@code int[] mAxes} array (the entry at index {@code axisIndex})
         *
         * @param axisIndex index for axis we are looking for
         * @return Axis ID of axis at index {@code axisIndex} of our device
         */
        public int getAxis(int axisIndex) {
            return mAxes[axisIndex];
        }

        /**
         * Getter for the current value of the axis at index {@code axisIndex} of our device.
         *
         * @param axisIndex index for axis we are looking for
         * @return Current value of the axis at index {@code axisIndex} of our device
         */
        public float getAxisValue(int axisIndex) {
            return mAxisValues[axisIndex];
        }

        /**
         * Getter for the number of key-value mappings in the {@code SparseIntArray mKeys} array.
         *
         * @return number of key-value mappings in the {@code SparseIntArray mKeys} array.
         */
        public int getKeyCount() {
            return mKeys.size();
        }

        /**
         * Given an index in the range 0...size()-1, returns the keycode from the {@code int keyIndex}
         * key-value mapping that {@code SparseIntArray mKeys} stores. We simply return the key that
         * the {@code keyAt} method of {@code SparseIntArray mKeys} returns ({@code mKeys} stores the
         * state of that keycode using the keycode as the key).
         *
         * @param keyIndex index (0...size()-1) of the key we are interested in
         * @return keycode that is the key in the {@code SparseIntArray mKeys} array for index
         * {@code keyIndex}
         */
        public int getKeyCode(int keyIndex) {
            return mKeys.keyAt(keyIndex);
        }

        /**
         * Returns true if the key stored in the {@code int keyIndex} entry ((0...size()-1) of the
         * {@code SparseIntArray mKeys} array) of {@code mKeys} is pressed, false otherwise. We
         * simply return the result of checking whether the value returned by the {@code valueAt}
         * method of {@code SparseIntArray mKeys} for {@code keyIndex} is not 0.
         *
         * @param keyIndex index (0...size()-1) of the key we are interested in
         * @return true if the key is pressed, false if it is not.
         */
        public boolean isKeyPressed(int keyIndex) {
            return mKeys.valueAt(keyIndex) != 0;
        }

        /**
         * Called by our {@code dispatchKeyEvent} override to determine if the keycode which generated
         * the {@code KeyEvent} it received is one that our game is interested in, and also to record
         * the state of that key in our {@code SparseIntArray mKeys} array if it is one. We first fetch
         * the keycode from the {@code KeyEvent} to initialize our variable {@code int keyCode}. If our
         * {@code isGameKey} method determines that it is a key our game is interested in we do some
         * more processing (if not, we return false to the caller). If the repeat count of our parameter
         * {@code KeyEvent event} is 0, we put the value 1 into the {@code mKeys} array under the key
         * {@code keyCode}, set {@code String symbolicName} to the symbolic name of the keycode, log
         * a message about the "Key Down" occurrence, and in both cases return true to the caller.
         *
         * @param event {@code KeyEvent} received by our {@code dispatchKeyEvent} override.
         * @return true if the keycode is one our game is interested in, false if not.
         */
        public boolean onKeyDown(KeyEvent event) {
            final int keyCode = event.getKeyCode();
            if (isGameKey(keyCode)) {
                if (event.getRepeatCount() == 0) {
                    mKeys.put(keyCode, 1);
                    final String symbolicName = KeyEvent.keyCodeToString(keyCode);
                    Log.i(TAG, mDevice.getName() + " - Key Down: " + symbolicName);
                }
                return true;
            }
            return false;
        }

        /**
         * Called by our {@code dispatchKeyEvent} override to determine if the keycode which generated
         * the {@code KeyEvent} it received is one that our game is interested in, and also to record
         * the state of that key in our {@code SparseIntArray mKeys} array if it is one. We first fetch
         * the keycode from the {@code KeyEvent} to initialize our variable {@code int keyCode}. If our
         * {@code isGameKey} method determines that it is a key our game is interested in we do some
         * more processing (if not, we return false to the caller). We first make sure the keycode is
         * already in our {@code SparseIntArray mKeys} array by calling the {@code indexOfKey} method
         * of {@code mKeys}, and if the result is less that 0 it is not in the array, so we do not
         * record of log its state. If it is already in the array (a key down event has previously
         * occurred), we put the value 0 into the {@code mKeys} array under the key {@code keyCode},
         * set {@code String symbolicName} to the symbolic name of the keycode, log a message about
         * the "Key Up" occurrence, and in both cases return true to the caller.
         *
         * @param event {@code KeyEvent} received by our {@code dispatchKeyEvent} override.
         * @return true if the keycode is one our game is interested in, false if not.
         */
        public boolean onKeyUp(KeyEvent event) {
            final int keyCode = event.getKeyCode();
            if (isGameKey(keyCode)) {
                int index = mKeys.indexOfKey(keyCode);
                if (index >= 0) {
                    mKeys.put(keyCode, 0);
                    final String symbolicName = KeyEvent.keyCodeToString(keyCode);
                    Log.i(TAG, mDevice.getName() + " - Key Up: " + symbolicName);
                }
                return true;
            }
            return false;
        }

        /**
         * Called by our {@code dispatchGenericMotionEvent} override to record and log the contents
         * of the {@code MotionEvent} it received. First we create {@code StringBuilder message} and
         * append a string consisting of the name of the device we are following with the string
         * "Joystick Motion" appended to it. We initialize {@code int historySize} with the the number
         * of historical points in  {@code MotionEvent event}. Then we loop over {@code int i} of all
         * the axis in our {@code int[] mAxes} array, setting {@code int axis} to the axis identifier
         * contained in {@code mAxes[i]}, and {@code float value} to the value of that axis contained
         * in the {@code MotionEvent event}. We then save this value in our {@code mAxisValues[i]}
         * array, and append a string consisting of the symbolic name of the axis followed by a ":"
         * to {@code StringBuilder message}. We now loop through all the historical axis values for
         * that axis appending all the values for it separated by a "," to {@code StringBuilder message}.
         * Finally we append the value {@code value} and a "\n" to {@code message} and loop back for the
         * next axis.
         * <p>
         * When done with all the axis we log the string value {@code message} under our tag {@code TAG},
         * and return true to the caller.
         *
         * @param event {@code MotionEvent} received by our {@code dispatchGenericMotionEvent} override
         * @return always returns true.
         */
        public boolean onJoystickMotion(MotionEvent event) {
            StringBuilder message = new StringBuilder();
            message.append(mDevice.getName()).append(" - Joystick Motion:\n");

            final int historySize = event.getHistorySize();
            for (int i = 0; i < mAxes.length; i++) {
                final int axis = mAxes[i];
                final float value = event.getAxisValue(axis);
                mAxisValues[i] = value;
                message.append("  ").append(MotionEvent.axisToString(axis)).append(": ");

                // Append all historical values in the batch.
                for (int historyPos = 0; historyPos < historySize; historyPos++) {
                    message.append(event.getHistoricalAxisValue(axis, historyPos));
                    message.append(", ");
                }

                // Append the current value.
                message.append(value);
                message.append("\n");
            }
            Log.i(TAG, message.toString());
            return true;
        }

        /**
         * Check whether this is a key we care about. We switch on {@code int keyCode} returning true
         * if the key is one of KEYCODE_DPAD_UP, KEYCODE_DPAD_DOWN, KEYCODE_DPAD_LEFT, KEYCODE_DPAD_RIGHT,
         * KEYCODE_DPAD_CENTER, or KEYCODE_SPACE. Otherwise we return the result of calling the
         * {@code KeyEvent.isGamepadButton} method for {@code keyCode} to the caller.
         *
         * @param keyCode keycode we are to check for
         * @return true if {@code keyCode} is one of the six keycodes we are interested in, false if not.
         */
        private static boolean isGameKey(int keyCode) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_SPACE:
                    return true;
                default:
                    return KeyEvent.isGamepadButton(keyCode);
            }
        }
    }

    /**
     * A list adapter that displays a summary of the device state.
     */
    @SuppressWarnings("WeakerAccess")
    private static class SummaryAdapter extends BaseAdapter {
        /**
         * Base of the row ID's used for the headings for device (0), axes (1), and keys (2)
         */
        private static final int BASE_ID_HEADING = 1 << 10;
        /**
         * Base of the row ID used for the device name {@code TextColumn} item
         */
        private static final int BASE_ID_DEVICE_ITEM = 2 << 10;
        /**
         * Base of the row ID used for the individual axes items
         */
        private static final int BASE_ID_AXIS_ITEM = 3 << 10;
        /**
         * Base of the row ID used for the "keys" items (the keycode is or'ed with it)
         */
        private static final int BASE_ID_KEY_ITEM = 4 << 10;

        /**
         * {@code Context} to use toast a message, this when constructed in the {@code onCreate}
         * method of the {@code GameControllerInput} activity.
         */
        private final Context mContext;
        /**
         * {@code Resources} instance to use to access resources, the value returned by the
         * {@code getResources} method when constructed in the {@code onCreate} method of the
         * {@code GameControllerInput} activity.
         */
        private final Resources mResources;

        /**
         * {@code SparseArray} used to hold both axes, and keycode {@code Item} objects, indexed by
         * BASE_ID_AXIS_ITEM or'ed with the axis ID for axes, and indexed by BASE_ID_KEY_ITEM or'ed
         * with the keyCode for keys.
         */
        private final SparseArray<Item> mDataItems = new SparseArray<>();
        /**
         * The list shown in our {@code ListView}. It is populated with {@code Item} objects to display
         * the {@code InputDeviceState} of the last device that received a {@code KeyEvent} or
         * {@code MotionEvent} we are interested in.
         */
        private final ArrayList<Item> mVisibleItems = new ArrayList<>();

        /**
         * {@code Heading} for the device heading row (the string "Input Device" will be written to
         * its {@code TextView} when the {@code Heading.initView} method is called.
         */
        private final Heading mDeviceHeading;
        /**
         * {@code TextColumn} for the device name row, consists of two {@code TextView} views, one
         * with the string "Name", and the other set to the name of the device whose event we are
         * displaying.
         */
        private final TextColumn mDeviceNameTextColumn;

        /**
         * {@code Heading} for the axes heading row (the string "Axes" will be written to its
         * {@code TextView} when the {@code Heading.initView} method is called.
         */
        private final Heading mAxesHeading;
        /**
         * {@code Heading} for the keys heading row (the string "Keys and Buttons" will be written
         * to its {@code TextView} when the {@code Heading.initView} method is called).
         */
        private final Heading mKeysHeading;

        /**
         * {@code InputDeviceState} we are currently displaying, it is passed to our {@code show}
         * method when a new event we are interested in is received by our callbacks.
         */
        private InputDeviceState mState;

        /**
         * Our constructor. First we save our parameters {@code Context context} in our field
         * {@code Context mContext}, and {@code Resources resources} in our field
         * {@code Resources mResources}. We initialize our field {@code Heading mDeviceHeading} with
         * a new instance using the row ID BASE_ID_HEADING or'ed with 0 (1024) and the string
         * R.string.game_controller_input_label_device_name ("Input Device"). We initialize our field
         * {@code TextColumn mDeviceNameTextColumn} with a new instance using the row ID BASE_ID_DEVICE_ITEM
         * or'ed with 0 (2048), and the string R.string.game_controller_input_label_device_name ("Name").
         * We initialize our field {@code Heading mAxesHeading} with a new instance using the row ID
         * BASE_ID_HEADING or'ed with 1 (1025) and the string R.string.game_controller_input_heading_axes
         * ("Axes"). We initialize our field {@code Heading mKeysHeading} with a new instance using the row ID
         * BASE_ID_HEADING or'ed with 2 (1026) and the string R.string.game_controller_input_heading_keys
         * ("Keys and Buttons")
         *
         * @param context   {@code Context} to use to toast messages
         * @param resources {@code Resources} instance to use to access resources.
         */
        @SuppressWarnings("PointlessBitwiseExpression")
        public SummaryAdapter(Context context, Resources resources) {
            mContext = context;
            mResources = resources;

            mDeviceHeading = new Heading(BASE_ID_HEADING | 0,
                    mResources.getString(R.string.game_controller_input_heading_device));
            mDeviceNameTextColumn = new TextColumn(BASE_ID_DEVICE_ITEM | 0,
                    mResources.getString(R.string.game_controller_input_label_device_name));

            mAxesHeading = new Heading(BASE_ID_HEADING | 1,
                    mResources.getString(R.string.game_controller_input_heading_axes));
            mKeysHeading = new Heading(BASE_ID_HEADING | 2,
                    mResources.getString(R.string.game_controller_input_heading_keys));
        }

        /**
         * Called from the {@code OnItemClickListener} of {@code ListView mSummaryList} (our
         * {@code ListView}). If our field {@code InputDeviceState mState} is not null we toast a
         * message which displays the string value of the {@code InputDevice mDevice} field in
         * {@code mState}.
         *
         * @param position position in our {@code ListView} that was clicked UNUSED
         */
        @SuppressWarnings("UnusedParameters")
        public void onItemClick(int position) {
            if (mState != null) {
                Toast toast = Toast.makeText(mContext, mState.getDevice().toString(), Toast.LENGTH_LONG);
                toast.show();
            }
        }

        /**
         * Called from our {@code dispatchKeyEvent} and {@code dispatchGenericMotionEvent} to display
         * the {@code InputDeviceState} of the device which just received a {@code KeyEvent} or a
         * {@code MotionEvent} that we are interested in. First we save our parameter
         * {@code InputDeviceState state} in our field {@code InputDeviceState mState}, then we clear
         * our field {@code ArrayList<Item> mVisibleItems} which contains the list of {@code Item}
         * objects we are displaying.
         * <p>
         * We begin to rebuild {@code mVisibleItems} by first adding our {@code Heading mDeviceHeading}
         * (a {@code TextView} with the text "Input Device"), setting the text of the content view
         * of {@code TextColumn mDeviceNameTextColumn} to the device name of {@code InputDeviceState state},
         * and adding {@code mDeviceNameTextColumn} to {@code mVisibleItems}.
         * <p>
         * Next we populate {@code mVisibleItems} with the axes information by first adding our
         * {@code Heading mAxesHeading} (a TextView with the text "Axes"), then looping over all of
         * the axes contained in {@code state} we fetch each {@code int axis}, form {@code int id}
         * from it by or'ing it with BASE_ID_AXIS_ITEM, and trying to get the {@code TextColumn column}
         * for that {@code id} from our field {@code SparseArray<Item> mDataItems}. If the result is
         * null we create a new {@code TextColumn} for {@code column} from {@code id} and the string
         * value of the {@code axis} and put that {@code column} in {@code mDataItems} under the key
         * {@code id}. We now set the content {@code TextView} of {@code column} to the string value
         * of the axis value and add {@code column} to {@code mVisibleItems}.
         * <p>
         * Next we populate {@code mVisibleItems} with the keys information by first adding our
         * {@code Heading mKeysHeading} (a TextView with the text "Keys and Buttons"), then looping
         * over all of keys in {@code state} we fetch each {@code int keyCode}, form {@code int id}
         * from it by or'ing it with BASE_ID_KEY_ITEM, and trying to get the {@code TextColumn column}
         * for that {@code id} from our field {@code SparseArray<Item> mDataItems}. If the result is
         * null we create a new {@code TextColumn} for {@code column} from {@code id} and the string
         * value of the {@code keyCode} and put that {@code column} in {@code mDataItems} under the
         * key {@code id}. We now set the content {@code TextView} of {@code column} to the string
         * R.string.game_controller_input_key_pressed ("Pressed") if the key in {@code state} is
         * pressed, or R.string.game_controller_input_key_released ("Released") if it is not pressed
         * and add {@code column} to {@code mVisibleItems}.
         * <p>
         * After doing all this we call the method {@code notifyDataSetChanged} to notify the system
         * that the underlying data has been changed and any View reflecting the data set should
         * refresh itself.
         *
         * @param state {@code InputDeviceState} of the device which just received an event we are
         *              interested in.
         */
        public void show(InputDeviceState state) {
            mState = state;
            mVisibleItems.clear();

            // Populate device information.
            mVisibleItems.add(mDeviceHeading);
            mDeviceNameTextColumn.setContent(state.getDevice().getName());
            mVisibleItems.add(mDeviceNameTextColumn);

            // Populate axes.
            mVisibleItems.add(mAxesHeading);
            final int axisCount = state.getAxisCount();
            for (int i = 0; i < axisCount; i++) {
                final int axis = state.getAxis(i);
                final int id = BASE_ID_AXIS_ITEM | axis;
                TextColumn column = (TextColumn) mDataItems.get(id);
                if (column == null) {
                    column = new TextColumn(id, MotionEvent.axisToString(axis));
                    mDataItems.put(id, column);
                }
                column.setContent(Float.toString(state.getAxisValue(i)));
                mVisibleItems.add(column);
            }

            // Populate keys.
            mVisibleItems.add(mKeysHeading);
            final int keyCount = state.getKeyCount();
            for (int i = 0; i < keyCount; i++) {
                final int keyCode = state.getKeyCode(i);
                final int id = BASE_ID_KEY_ITEM | keyCode;
                TextColumn column = (TextColumn) mDataItems.get(id);
                if (column == null) {
                    column = new TextColumn(id, KeyEvent.keyCodeToString(keyCode));
                    mDataItems.put(id, column);
                }
                column.setContent(mResources.getString(state.isKeyPressed(i)
                        ? R.string.game_controller_input_key_pressed
                        : R.string.game_controller_input_key_released));
                mVisibleItems.add(column);
            }

            notifyDataSetChanged();
        }

        /**
         * Indicates whether the item ids are stable across changes to the underlying data. We just
         * return true.
         *
         * @return True since the same id always refers to the same object.
         */
        @Override
        public boolean hasStableIds() {
            return true;
        }

        /**
         * How many items are in the data set represented by this Adapter. We return the size of our
         * field {@code ArrayList<Item> mVisibleItems}.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            return mVisibleItems.size();
        }

        /**
         * Get the data item associated with the specified position in the data set. We return the
         * contents of our field {@code ArrayList<Item> mVisibleItems} at position {@code position}.
         *
         * @param position Position of the item within the adapter's data set that is wanted.
         * @return The data at the specified position.
         */
        @Override
        public Item getItem(int position) {
            return mVisibleItems.get(position);
        }

        /**
         * Get the row id associated with the specified position in the list. We call our method
         * {@code getItem} to get the {@code Item} at position {@code position}, and return the
         * ID of that {@code Item} that its {@code getItemId} method returns.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return getItem(position).getItemId();
        }

        /**
         * Get a View that displays the data at the specified position in the data set. We call our
         * method {@code getItem} to get the {@code Item} at position {@code position}, and return
         * the result of calling that objects {@code getView} method, which either returns the View
         * that that object has already been using, or one it creates and initializes.
         *
         * @param position    The position of the item within the adapter's data set of the item
         *                    whose view we want.
         * @param convertView The old view to reuse, if possible.
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).getView(convertView, parent);
        }

        /**
         * Abstract base class for the {@code Heading}, and {@code TextColumn} classes.
         */
        private static abstract class Item {
            /**
             * Stable Item ID that is returned by our method {@code getItemId}, which is called from
             * the {@code getItemId} method of the {@code SummaryAdapter} which is using us. It is
             * set to one of its parameters by our constructor.
             */
            private final int mItemId;
            /**
             * Resource ID for the layout which will display our information. It is set to one of
             * its parameters by our constructor.
             */
            private final int mLayoutResourceId;
            /**
             * {@code View} which is displaying our information. If one does not already exist, our
             * {@code getView} method will create one by inflating the layout file pointed to by
             * our field {@code mLayoutResourceId}.
             */
            private View mView;

            /**
             * Our constructor. We merely save our parameters {@code itemId} in our field {@code mItemId},
             * and {@code layoutResourceId} in our field {@code mLayoutResourceId}.
             *
             * @param itemId           Stable Item ID for this {@code Item} instance
             * @param layoutResourceId Resource ID pointing to a layout file to display our information.
             */
            public Item(int itemId, int layoutResourceId) {
                mItemId = itemId;
                mLayoutResourceId = layoutResourceId;
            }

            /**
             * Returns the stable ID of this {@code Item} that is stored in our field {@code mItemId}.
             *
             * @return The stable ID of this {@code Item}
             */
            public long getItemId() {
                return mItemId;
            }

            /**
             * Returns a {@code View} updated to hold our latest information. If our field
             * {@code View mView} is null, we initialize {@code LayoutInflater inflater} with an
             * instance of the system level service LAYOUT_INFLATER_SERVICE, and use it to inflate
             * our layout file {@code mLayoutResourceId} using our parameter {@code parent} as the
             * object that provides a set of LayoutParams values for root of the returned hierarchy
             * to create a new instance for {@code mView}, and call our method {@code initView} to
             * initialize {@code mView}.
             * <p>
             * In either case we call our method {@code updateView} to update the information displayed
             * by {@code mView} and return {@code mView} to the caller.
             *
             * @param convertView UNUSED The old view to reuse, if possible.
             * @param parent      The parent that this view will eventually be attached to
             * @return {@code View} holding our information.
             */
            @SuppressWarnings("UnusedParameters")
            public View getView(View convertView, ViewGroup parent) {
                if (mView == null) {
                    LayoutInflater inflater = (LayoutInflater)
                            parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    //noinspection ConstantConditions
                    mView = inflater.inflate(mLayoutResourceId, parent, false);
                    initView(mView);
                }
                updateView(mView);
                return mView;
            }

            /**
             * Derived classes should override this to do any class specific initialization.
             *
             * @param view {@code View} which will hold the information for this {@code Item}
             */
            protected void initView(View view) {
            }

            /**
             * Derived classes should override this to do any class specific updating of its view.
             *
             * @param view {@code View} which will hold the information for this {@code Item}
             */
            protected void updateView(View view) {
            }
        }

        /**
         * {@code Item} which displays constant heading text in a {@code TextView}.
         */
        private static class Heading extends Item {
            /**
             * String we are supposed to display in our {@code TextView}
             */
            private final String mLabel;

            /**
             * Our constructor. First we call our super's constructor with our parameter {@code itemId}
             * and our layout resource R.layout.game_controller_input_heading (consists of a single
             * {@code TextView} with no ID). Then we save our parameter {@code String label} in our
             * field {@code String mLabel}.
             *
             * @param itemId Stable ID for this {@code Item}
             * @param label  String to display in our {@code TextView}
             */
            public Heading(int itemId, String label) {
                super(itemId, R.layout.game_controller_input_heading);
                mLabel = label;
            }

            /**
             * Initializes our view by setting the text of its {@code TextView} to the string in our
             * field {@code mLabel}
             *
             * @param view {@code View} which will hold the information for this {@code Item}
             */
            @Override
            public void initView(View view) {
                TextView textView = (TextView) view;
                textView.setText(mLabel);
            }
        }

        /**
         * {@code Item} which displays constant heading text in one {@code TextView} and varying
         * information from its field {@code String mContent} in a second one.
         */
        private static class TextColumn extends Item {
            /**
             * Constant string to display in our first {@code TextView}, set by our constructor.
             */
            private final String mLabel;

            /**
             * Varying information to display in our second {@code TextView}, set by calling our
             * method {@code setContent}.
             */
            private String mContent;
            /**
             * Point to the {@code TextView} in our layout that is used for varying information.
             */
            private TextView mContentView;

            /**
             * Our constructor. First we call our super's constructor with our parameter {@code itemId}
             * and our layout resource R.layout.game_controller_input_text_column (consists of a
             * horizontal {@code LinearLayout} with two {@code TextView} objects with the ID's
             * R.id.label (for the constant heading), and R.id.content (for the varying information).
             * Then we save our parameter {@code String label} in our field {@code String mLabel}.
             *
             * @param itemId Stable ID for this {@code Item}
             * @param label  String to display in our constant first {@code TextView}
             */
            public TextColumn(int itemId, String label) {
                super(itemId, R.layout.game_controller_input_text_column);
                mLabel = label;
            }

            /**
             * Saves the value of its parameter {@code String content} in our field {@code mContent}.
             *
             * @param content Varying information that we are to display in our {@code TextView}
             *                with ID R.id.content ({@code mContentView})
             */
            public void setContent(String content) {
                mContent = content;
            }

            /**
             * Initializes this {@code TextColumn} object. We initialize {@code TextView textView} by
             * finding the view in {@code view} with ID R.id.label and set its text to our field
             * {@code mLabel}. We then initialize our field {@code TextView mContentView} by finding
             * the view in {@code view} with ID R.id.content.
             *
             * @param view {@code View} which will hold the information for this {@code Item}
             */
            @Override
            public void initView(View view) {
                TextView textView = view.findViewById(R.id.label);
                textView.setText(mLabel);

                mContentView = view.findViewById(R.id.content);
            }

            /**
             * Updates the text displayed in {@code TextView mContentView} with the latest contents
             * of {@code String mContent}.
             *
             * @param view {@code View} which will hold the information for this {@code Item}
             */
            @Override
            public void updateView(View view) {
                mContentView.setText(mContent);
            }
        }
    }
}
