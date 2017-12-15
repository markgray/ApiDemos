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

import com.example.android.apis.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.input.InputManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.InputDevice.MotionRange;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
public class GameControllerInput extends Activity implements InputManager.InputDeviceListener {
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

        mGame = (GameView) findViewById(R.id.game);

        mSummaryList = (ListView) findViewById(R.id.summary);
        mSummaryList.setAdapter(mSummaryAdapter);
        mSummaryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSummaryAdapter.onItemClick(position);
            }
        });
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

        public int getAxisCount() {
            return mAxes.length;
        }

        public int getAxis(int axisIndex) {
            return mAxes[axisIndex];
        }

        public float getAxisValue(int axisIndex) {
            return mAxisValues[axisIndex];
        }

        public int getKeyCount() {
            return mKeys.size();
        }

        public int getKeyCode(int keyIndex) {
            return mKeys.keyAt(keyIndex);
        }

        public boolean isKeyPressed(int keyIndex) {
            return mKeys.valueAt(keyIndex) != 0;
        }

        public boolean onKeyDown(KeyEvent event) {
            final int keyCode = event.getKeyCode();
            if (isGameKey(keyCode)) {
                if (event.getRepeatCount() == 0) {
                    final String symbolicName = KeyEvent.keyCodeToString(keyCode);
                    mKeys.put(keyCode, 1);
                    Log.i(TAG, mDevice.getName() + " - Key Down: " + symbolicName);
                }
                return true;
            }
            return false;
        }

        public boolean onKeyUp(KeyEvent event) {
            final int keyCode = event.getKeyCode();
            if (isGameKey(keyCode)) {
                int index = mKeys.indexOfKey(keyCode);
                if (index >= 0) {
                    final String symbolicName = KeyEvent.keyCodeToString(keyCode);
                    mKeys.put(keyCode, 0);
                    Log.i(TAG, mDevice.getName() + " - Key Up: " + symbolicName);
                }
                return true;
            }
            return false;
        }

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

        // Check whether this is a key we care about.
        // In a real game, we would probably let the user configure which keys to use
        // instead of hardcoding the keys like this.
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
        private static final int BASE_ID_HEADING = 1 << 10;
        private static final int BASE_ID_DEVICE_ITEM = 2 << 10;
        private static final int BASE_ID_AXIS_ITEM = 3 << 10;
        private static final int BASE_ID_KEY_ITEM = 4 << 10;

        private final Context mContext;
        private final Resources mResources;

        private final SparseArray<Item> mDataItems = new SparseArray<>();
        private final ArrayList<Item> mVisibleItems = new ArrayList<>();

        private final Heading mDeviceHeading;
        private final TextColumn mDeviceNameTextColumn;

        private final Heading mAxesHeading;
        private final Heading mKeysHeading;

        private InputDeviceState mState;

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

        @SuppressWarnings("UnusedParameters")
        public void onItemClick(int position) {
            if (mState != null) {
                Toast toast = Toast.makeText(mContext, mState.getDevice().toString(), Toast.LENGTH_LONG);
                toast.show();
            }
        }

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

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public int getCount() {
            return mVisibleItems.size();
        }

        @Override
        public Item getItem(int position) {
            return mVisibleItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getItemId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).getView(convertView, parent);
        }

        private static abstract class Item {
            private final int mItemId;
            private final int mLayoutResourceId;
            private View mView;

            public Item(int itemId, int layoutResourceId) {
                mItemId = itemId;
                mLayoutResourceId = layoutResourceId;
            }

            public long getItemId() {
                return mItemId;
            }

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

            protected void initView(View view) {
            }

            protected void updateView(View view) {
            }
        }

        private static class Heading extends Item {
            private final String mLabel;

            public Heading(int itemId, String label) {
                super(itemId, R.layout.game_controller_input_heading);
                mLabel = label;
            }

            @Override
            public void initView(View view) {
                TextView textView = (TextView) view;
                textView.setText(mLabel);
            }
        }

        private static class TextColumn extends Item {
            private final String mLabel;

            private String mContent;
            private TextView mContentView;

            public TextColumn(int itemId, String label) {
                super(itemId, R.layout.game_controller_input_text_column);
                mLabel = label;
            }

            public void setContent(String content) {
                mContent = content;
            }

            @Override
            public void initView(View view) {
                TextView textView = (TextView) view.findViewById(R.id.label);
                textView.setText(mLabel);

                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public void updateView(View view) {
                mContentView.setText(mContent);
            }
        }
    }
}
