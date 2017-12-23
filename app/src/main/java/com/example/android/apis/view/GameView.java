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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A trivial joystick based physics game to demonstrate joystick handling.
 * <p>
 * If the game controller has a vibrator, then it is used to provide feedback
 * when a bullet is fired or the ship crashes into an obstacle.  Otherwise, the
 * system vibrator is used for that purpose.
 * <p>
 * see GameControllerInput
 */
@SuppressWarnings("PointlessBitwiseExpression")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class GameView extends View {
    /**
     * Length of delay between running of our animation runnable background thread.
     */
    private final long ANIMATION_TIME_STEP = 1000 / 60;
    /**
     * Maximum number of obstacles to have in existence at any given time.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final int MAX_OBSTACLES = 12;

    /**
     * {@code Random} instance we use to generate random numbers whenever needed.
     */
    private final Random mRandom;
    /**
     * {@code Ship} instance that we control with our non-existent game controller.
     */
    private Ship mShip;
    /**
     * List of {@code Bullet} objects that are currently in flight.
     */
    private final List<Bullet> mBullets;
    /**
     * List of {@code Obstacle} objects that are currently in existence.
     */
    private final List<Obstacle> mObstacles;

    /**
     * Milliseconds since boot of the previous time that our method {@code step} was called to advance
     * the animation of the game.
     */
    private long mLastStepTime;
    /**
     * Set to the device of the last {@code MotionEvent} received by our {@code onGenericMotionEvent}
     * callback if it is a SOURCE_CLASS_JOYSTICK device, and used to move our spaceship.
     */
    private InputDevice mLastInputDevice;

    /**
     * Bit in our field {@code int mDPadState} we use to indicate that the KEYCODE_DPAD_LEFT key is
     * currently pressed.
     */
    private static final int DPAD_STATE_LEFT = 1 << 0;
    /**
     * Bit in our field {@code int mDPadState} we use to indicate that the KEYCODE_DPAD_RIGHT key is
     * currently pressed.
     */
    private static final int DPAD_STATE_RIGHT = 1 << 1;
    /**
     * Bit in our field {@code int mDPadState} we use to indicate that the KEYCODE_DPAD_UP key is
     * currently pressed.
     */
    private static final int DPAD_STATE_UP = 1 << 2;
    /**
     * Bit in our field {@code int mDPadState} we use to indicate that the KEYCODE_DPAD_DOWN key is
     * currently pressed.
     */
    private static final int DPAD_STATE_DOWN = 1 << 3;

    /**
     * Contains bit fields to indicate which of our dpad keys are currently pressed: KEYCODE_DPAD_LEFT,
     * KEYCODE_DPAD_RIGHT, KEYCODE_DPAD_UP, and/or KEYCODE_DPAD_DOWN.
     */
    private int mDPadState;

    /**
     * Size of the spaceship in pixels given the logical density of the display.
     */
    private float mShipSize;
    /**
     * Speed that the ship can accelerate in pixels given the logical density of the display.
     */
    private float mMaxShipThrust;
    /**
     * Maximum speed that the ship can reach in pixels given the logical density of the display.
     */
    private float mMaxShipSpeed;

    /**
     * Size of the a bullet in pixels given the logical density of the display.
     */
    private float mBulletSize;
    /**
     * Speed of a bullet in pixels given the logical density of the display.
     */
    private float mBulletSpeed;

    /**
     * Minimum size of an obstacle in pixels given the logical density of the display.
     */
    private float mMinObstacleSize;
    /**
     * Maximum size of an obstacle in pixels given the logical density of the display.
     */
    private float mMaxObstacleSize;
    /**
     * Minimum speed of an obstacle in pixels given the logical density of the display.
     */
    private float mMinObstacleSpeed;
    /**
     * Maximum speed of an obstacle in pixels given the logical density of the display.
     */
    private float mMaxObstacleSpeed;

    /**
     * Background thread that runs every ANIMATION_TIME_STEP milliseconds to animate the next frame
     * of our game.
     */
    private final Runnable mAnimationRunnable = new Runnable() {
        /**
         * Our handler calls this every ANIMATION_TIME_STEP milliseconds to animate our next frame.
         * We simply call our method {@code animateFrame}.
         */
        @Override
        public void run() {
            animateFrame();
        }
    };

    /**
     * Constructor that is called when inflating a view from XML. First we call our super's constructor,
     * and allocate new instances for our fields {@code Random mRandom}, {@code List<Bullet> mBullets},
     * and {@code List<Obstacle> mObstacles}. We enable our view to receive focus, and to receive
     * focus in touch mode. We initialize {@code float baseSize} to 5.0 times the logical density of
     * our display, and {@code float baseSpeed} to be 3.0 times {@code baseSize}. We initialize our
     * field {@code mShipSize} to be 3.0 times {@code baseSize}, {@code mMaxShipThrust} to be 0.25
     * times {@code baseSpeed}, and {@code mMaxShipSpeed} to be 12 times {@code baseSpeed}. We initialize
     * {@code mBulletSize} to be {@code baseSize}, and {@code mBulletSpeed} to be 12 times {@code baseSpeed}.
     * We initialize {@code mMinObstacleSize} to be 2 times {@code baseSize}, {@code mMaxObstacleSize}
     * to be 12 times {@code baseSize}, {@code mMinObstacleSpeed} to be {@code baseSpeed}, and
     * {@code mMaxObstacleSpeed} to be 3 times {@code baseSpeed}.
     *
     * @param context The Context the view is running in, through which it can access the current
     *                theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mRandom = new Random();
        mBullets = new ArrayList<>();
        mObstacles = new ArrayList<>();

        setFocusable(true);
        setFocusableInTouchMode(true);

        float baseSize = getContext().getResources().getDisplayMetrics().density * 5f;
        float baseSpeed = baseSize * 3;

        mShipSize = baseSize * 3;
        mMaxShipThrust = baseSpeed * 0.25f;
        mMaxShipSpeed = baseSpeed * 12;

        mBulletSize = baseSize;
        mBulletSpeed = baseSpeed * 12;

        mMinObstacleSize = baseSize * 2;
        mMaxObstacleSize = baseSize * 12;
        mMinObstacleSpeed = baseSpeed;
        mMaxObstacleSpeed = baseSpeed * 3;
    }

    /**
     * This is called during layout when the size of this view has changed. First we call our super's
     * implementation of {@code onSizeChanged}, then we call our method {@code reset} to reset the
     * game.
     *
     * @param w    Current width of this view.
     * @param h    Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Reset the game when the view changes size.
        reset();
    }

    /**
     * Callback for a key being pressed. First we call our method {@code ensureInitialized} to make
     * sure we have a spaceship to play with. Then we initialize our variable {@code handled} to
     * false. Then if the {@code getRepeatCount} method of our parameter {@code KeyEvent event} returns
     * 0 (we only want to handle the keys on initial down but not on auto-repeat), we switch on the
     * value of {@code keyCode}:
     * <ul>
     * <li>
     * KEYCODE_DPAD_LEFT - we call the {@code setHeadingX} method of our field {@code Ship mShip}
     * with a value of -1, set the DPAD_STATE_LEFT bit in {@code mDPadState}, set {@code handled}
     * to true, and break.
     * </li>
     * <li>
     * KEYCODE_DPAD_RIGHT - we call the {@code setHeadingX} method of our field {@code Ship mShip}
     * with a value of 1, set the DPAD_STATE_RIGHT bit in {@code mDPadState}, set {@code handled}
     * to true, and break.
     * </li>
     * <li>
     * KEYCODE_DPAD_UP - we call the {@code setHeadingY} method of our field {@code Ship mShip}
     * with a value of -1, set the DPAD_STATE_UP bit in {@code mDPadState}, set {@code handled}
     * to true, and break.
     * </li>
     * <li>
     * KEYCODE_DPAD_DOWN - we call the {@code setHeadingY} method of our field {@code Ship mShip}
     * with a value of 1, set the DPAD_STATE_DOWN bit in {@code mDPadState}, set {@code handled}
     * to true, and break.
     * </li>
     * <li>
     * default - if our {@code isFireKey} method returns true for {@code keyCode}, we call our
     * method {@code fire}, set {@code handled} to true, and break.
     * </li>
     * </ul>
     * Having handled the keys we are interested in, we check if {@code handled} is true, and if so
     * we call our method {@code step} with the time that the {@code KeyEvent event} occurred, and
     * return true to our caller. Otherwise we return the value returned by our super's implementation
     * of {@code onKeyDown}.
     *
     * @param keyCode A key code that represents the button pressed
     * @param event   The KeyEvent object that defines the button action.
     * @return true if we handled the event, false otherwise.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        ensureInitialized();

        // Handle DPad keys and fire button on initial down but not on auto-repeat.
        boolean handled = false;
        if (event.getRepeatCount() == 0) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    mShip.setHeadingX(-1);
                    mDPadState |= DPAD_STATE_LEFT;
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    mShip.setHeadingX(1);
                    mDPadState |= DPAD_STATE_RIGHT;
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    mShip.setHeadingY(-1);
                    mDPadState |= DPAD_STATE_UP;
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    mShip.setHeadingY(1);
                    mDPadState |= DPAD_STATE_DOWN;
                    handled = true;
                    break;
                default:
                    if (isFireKey(keyCode)) {
                        fire();
                        handled = true;
                    }
                    break;
            }
        }
        if (handled) {
            step(event.getEventTime());
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Called when a key is released. First we call our method {@code ensureInitialized} to make
     * sure we have a spaceship to play with. Then we initialize our variable {@code handled} to
     * false, and we switch on the value of {@code keyCode}:
     * <ul>
     * <li>
     * KEYCODE_DPAD_LEFT - we call the {@code setHeadingX} method of our field {@code Ship mShip}
     * with a value of 0, clear the DPAD_STATE_LEFT bit in {@code mDPadState}, set {@code handled}
     * to true, and break.
     * </li>
     * <li>
     * KEYCODE_DPAD_RIGHT - we call the {@code setHeadingX} method of our field {@code Ship mShip}
     * with a value of 0, clear the DPAD_STATE_RIGHT bit in {@code mDPadState}, set {@code handled}
     * to true, and break.
     * </li>
     * <li>
     * KEYCODE_DPAD_UP - we call the {@code setHeadingY} method of our field {@code Ship mShip}
     * with a value of 0, clear the DPAD_STATE_UP bit in {@code mDPadState}, set {@code handled}
     * to true, and break.
     * </li>
     * <li>
     * KEYCODE_DPAD_DOWN - we call the {@code setHeadingY} method of our field {@code Ship mShip}
     * with a value of 0, clear the DPAD_STATE_DOWN bit in {@code mDPadState}, set {@code handled}
     * to true, and break.
     * </li>
     * <li>
     * default - the our method {@code isFireKey} returns true for {@code keyCode} we set {@code handled}
     * to true and break.
     * </li>
     * </ul>
     * If {@code handled} is now true, we call our method {@code step} with the time that the
     * {@code KeyEvent event} occurred, and return true to our caller. Otherwise we return the value
     * returned by our super's implementation of {@code onKeyDown}.
     *
     * @param keyCode A key code that represents the button pressed, from
     *                {@link android.view.KeyEvent}.
     * @param event   The KeyEvent object that defines the button action.
     * @return true if we handled the event.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        ensureInitialized();

        // Handle keys going up.
        boolean handled = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mShip.setHeadingX(0);
                mDPadState &= ~DPAD_STATE_LEFT;
                handled = true;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mShip.setHeadingX(0);
                mDPadState &= ~DPAD_STATE_RIGHT;
                handled = true;
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                mShip.setHeadingY(0);
                mDPadState &= ~DPAD_STATE_UP;
                handled = true;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mShip.setHeadingY(0);
                mDPadState &= ~DPAD_STATE_DOWN;
                handled = true;
                break;
            default:
                if (isFireKey(keyCode)) {
                    handled = true;
                }
                break;
        }
        if (handled) {
            step(event.getEventTime());
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * Convenience function to check whether our parameter {@code keyCode} is either a gamepad button,
     * KEYCODE_DPAD_CENTER, or KEYCODE_SPACE (in which case we return true).
     *
     * @param keyCode keycode we are to check to see if it is a "fire" key
     * @return true if {@code keyCode} is a "fire" key, false if not.
     */
    private static boolean isFireKey(int keyCode) {
        return KeyEvent.isGamepadButton(keyCode)
                || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                || keyCode == KeyEvent.KEYCODE_SPACE;
    }

    /**
     * We implement this method to handle generic motion events. First we call our method
     * {@code ensureInitialized} to make sure we have a spaceship to play with. Then if the
     * {@code MotionEvent event} is from the source SOURCE_CLASS_JOYSTICK (a joystick), and the
     * action of {@code event} is ACTION_MOVE it is an {@code MotionEvent} we may be interested in
     * so we do some more processing. If {@code mLastInputDevice} is null or it does not have the
     * same input device ID as the {@code MotionEvent event} we set {@code mLastInputDevice} to the
     * {@code InputDevice} of {@code event} (if that is still null we return false to the caller
     * as the {@code MotionEvent event} is obviously invalid). We make sure that none of our DPAD
     * keys are pressed by checking the value of {@code mDPadState}, and if any are set we ignore
     * the joystick by returning true to the caller.
     * <p>
     * Now we are ready to process all historical movement samples in the batch. First we initialize
     * our variable {@code int historySize} with the number of historical points in {@code event},
     * then we loop over {@code i} for all these points calling our method {@code processJoystickInput}
     * with {@code event} and {@code i} (this method will determine an X and Y value for that movement,
     * change the heading of our spaceship appropriately and call our method {@code step} to advance
     * the animation to the time that that sample occurred). When done with the historical samples,
     * we call {@code processJoystickInput} again with -1 as the sample number to process the current
     * movement sample in the batch and return true to the caller.
     * <p>
     * If the {@code MotionEvent event} is not from a joystick we return the value returned by our
     * super's implementation of {@code onGenericMotionEvent}.
     *
     * @param event The generic motion event being processed.
     * @return True if the event was handled, false otherwise.
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        ensureInitialized();

        // Check that the event came from a joystick since a generic motion event
        // could be almost anything.
        if (event.isFromSource(InputDevice.SOURCE_CLASS_JOYSTICK)
                && event.getAction() == MotionEvent.ACTION_MOVE) {
            // Cache the most recently obtained device information.
            // The device information may change over time but it can be
            // somewhat expensive to query.
            if (mLastInputDevice == null || mLastInputDevice.getId() != event.getDeviceId()) {
                mLastInputDevice = event.getDevice();
                // It's possible for the device id to be invalid.
                // In that case, getDevice() will return null.
                if (mLastInputDevice == null) {
                    return false;
                }
            }

            // Ignore joystick while the DPad is pressed to avoid conflicting motions.
            if (mDPadState != 0) {
                return true;
            }

            // Process all historical movement samples in the batch.
            final int historySize = event.getHistorySize();
            for (int i = 0; i < historySize; i++) {
                processJoystickInput(event, i);
            }

            // Process the current movement sample in the batch.
            processJoystickInput(event, -1);
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    /**
     * Called by our {@code onGenericMotionEvent} callback to process the {@code MotionEvent} it
     * received from a joystick. We first try to set our variable {@code float x} by calling our
     * method {@code getCenteredAxis} to retrieve the AXIS_X axis from the {@code historyPos} sample
     * in {@code event} (we also pass it {@code mLastInputDevice} so it can determine the range of
     * the device). If that returns 0, we try to get the AXIS_HAT_X axis, and if that returns 0 also
     * we try to get the AXIS_Z axis.
     * <p>
     * Next we try to set our variable {@code float y} by calling our method {@code getCenteredAxis}
     * to retrieve the AXIS_Y axis from the {@code historyPos} sample in {@code event} (we also pass
     * it {@code mLastInputDevice} so it can determine the range of the device). If that returns 0,
     * we try to get the AXIS_HAT_Y axis, and if that returns 0 also we try to get the AXIS_RZ axis.
     * <p>
     * We have to try the extra axes because many game pads with two joysticks report the position
     * of the second joystick using the other axis types.
     * <p>
     * Once we have extracted the (x,y) coordinates from {@code event} we call the {@code setHeading}
     * method of {@code Ship mShip} to change its heading, and call our {@code step} method to advance
     * the animation, using the time the event occurred if {@code historyPos} is less than 0, or the
     * time that the historical movement {@code historyPos} occurred between this event and the previous
     * event if greater or equal to zero.
     *
     * @param event      {@code MotionEvent} that we received in our {@code onGenericMotionEvent} callback.
     * @param historyPos number of the historical movement sample in the batch (-1 for the current
     *                   movement).
     */
    private void processJoystickInput(MotionEvent event, int historyPos) {
        // Get joystick position.
        // Many game pads with two joysticks report the position of the second joystick
        // using the Z and RZ axes so we also handle those.
        // In a real game, we would allow the user to configure the axes manually.
        float x = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_X, historyPos);
        if (x == 0) {
            x = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_HAT_X, historyPos);
        }
        if (x == 0) {
            x = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_Z, historyPos);
        }

        float y = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_Y, historyPos);
        if (y == 0) {
            y = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_HAT_Y, historyPos);
        }
        if (y == 0) {
            y = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_RZ, historyPos);
        }

        // Set the ship heading.
        mShip.setHeading(x, y);
        step(historyPos < 0 ? event.getEventTime() : event.getHistoricalEventTime(historyPos));
    }

    /**
     * Returns the value of the requested joystick axis from the {@code MotionEvent} passed it. First
     * we initialize our variable {@code InputDevice.MotionRange range} by calling the {@code getMotionRange}
     * method of {@code InputDevice device} for the {@code axis} we are interested in and the source
     * of {@code event}. If that is not null, we initialize our variable {@code float flat} with the
     * extent of the center flat position with respect to this axis. If {@code historyPos} is less than
     * 0 we initialize our variable {@code float value} with the value of the requested axis for the
     * for the current movement, otherwise we initialize it to the value of the requested axis for the
     * historical movement {@code historyPos}. If the absolute value of {@code value} is greater than
     * than the {@code flat} range we return {@code value} to the caller, otherwise we return 0.
     *
     * @param event      joystick {@code MotionEvent} received by our {@code onGenericMotionEvent} callback.
     * @param device     {@code InputDevice} of the device which sent us the event.
     * @param axis       joystick axis we are interested in.
     * @param historyPos number of the historical movement sample in the batch (-1 for the current
     *                   movement).
     * @return value of the requested axis for the event or historical event we are interested in
     * (or 0 if the value is within the "flat" range of the device).
     */
    private static float getCenteredAxis(MotionEvent event, InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range = device.getMotionRange(axis, event.getSource());
        if (range != null) {
            final float flat = range.getFlat();
            final float value = historyPos < 0 ? event.getAxisValue(axis)
                    : event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the joystick axis center.
            // A joystick at rest does not always report an absolute position of (0,0).
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    /**
     * Called when the window containing this view gains or loses focus. If our parameter {@code hasWindowFocus}
     * is true, we get a handler associated with the thread running this View (This handler can be used to pump
     * events in the UI events queue) and add our {@code Runnable mAnimationRunnable} to its message queue
     * with a delay of ANIMATION_TIME_STEP (16). We then set {@code mLastStepTime} to the current milliseconds
     * since boot.
     * <p>
     * If {@code hasWindowFocus} is false, we remove all scheduled {@code Runnable mAnimationRunnable} from
     * the handler associated with the thread running this View, set {@code mDPadState} (no keys pressed),
     * and if {@code Ship mShip} is not null we call its {@code setHeading} method to set its heading to
     * (0,0) and its {@code setVelocity} method to set its velocity to (0,0).
     * <p>
     * Finally we return the value returned by our super's implementation of {@code onWindowFocusChanged} to
     * our caller.
     *
     * @param hasWindowFocus True if the window containing this view now has
     *                       focus, false otherwise.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        // Turn on and off animations based on the window focus.
        // Alternately, we could update the game state using the Activity onResume()
        // and onPause() lifecycle events.
        if (hasWindowFocus) {
            getHandler().postDelayed(mAnimationRunnable, ANIMATION_TIME_STEP);
            mLastStepTime = SystemClock.uptimeMillis();
        } else {
            getHandler().removeCallbacks(mAnimationRunnable);

            mDPadState = 0;
            if (mShip != null) {
                mShip.setHeading(0, 0);
                mShip.setVelocity(0, 0);
            }
        }

        super.onWindowFocusChanged(hasWindowFocus);
    }

    /**
     * Called to have the spaceship fire its gun. First we make sure that {@code Ship mShip} is not
     * null, and that its {@code isDestroyed} method returns false, returning having done nothing if
     * we no longer have a spaceship. If we are still alive we initialize {@code Bullet bullet} with
     * a new instance, call its {@code setPosition} method to set its initial position to the initial
     * position to the position {@code mShip} dictates for a bullet by its {@code getBulletInitialX}
     * and {@code getBulletInitialY} methods, and call its {@code setVelocity} method to set its
     * velocity to the bullet velocity that {@code mShip} dictates for a bullet using its
     * {@code getBulletVelocityX} and {@code getBulletVelocityY} methods. We then add {@code bullet}
     * to our list of bullets in {@code List<Bullet> mBullets}. Finally we get the vibrator service
     * associated with the device {@code InputDevice mLastInputDevice} and ask it to vibrate for 20
     * milliseconds.
     */
    private void fire() {
        if (mShip != null && !mShip.isDestroyed()) {
            Bullet bullet = new Bullet();
            bullet.setPosition(mShip.getBulletInitialX(), mShip.getBulletInitialY());
            bullet.setVelocity(mShip.getBulletVelocityX(mBulletSpeed), mShip.getBulletVelocityY(mBulletSpeed));
            mBullets.add(bullet);

            getVibrator().vibrate(20);
        }
    }

    /**
     * Convenience function to call {@code reset} if {@code Ship mShip} is null.
     */
    private void ensureInitialized() {
        if (mShip == null) {
            reset();
        }
    }

    /**
     * Called when an obstacle hits our {@code Ship mShip} in our {@code step} method. We simply get
     * the vibrator service associated with the device {@code InputDevice mLastInputDevice} and ask
     * it to vibrate for a series of pulses to simulate a "crash" of our spaceship.
     */
    private void crash() {
        getVibrator().vibrate(new long[]{0, 20, 20, 40, 40, 80, 40, 300}, -1);
    }

    /**
     * Resets the game to the starting conditions. First we create a new instance for {@code Ship mShip},
     * then we clear our list of bullets in {@code List<Bullet> mBullets} and our list of obstacles
     * in {@code List<Obstacle> mObstacles}.
     */
    private void reset() {
        mShip = new Ship();
        mBullets.clear();
        mObstacles.clear();
    }

    /**
     * Gets a {@code Vibrator} instance to use for some buzzing, either the vibrator associated with
     * the current input device or the system level vibrator if the device lacks a vibrator. First we
     * check that {@code mLastInputDevice} is not null, and if it is not we initialize our variable
     * {@code Vibrator vibrator} by calling the {@code getVibrator} method of {@code mLastInputDevice}.
     * If the {@code hasVibrator} method of {@code vibrator} returns true we return {@code vibrator}
     * to the caller.
     * <p>
     * Otherwise we return a {@code Vibrator} from the system level service VIBRATOR_SERVICE.
     *
     * @return vibrator service associated with the device {@code InputDevice mLastInputDevice} or
     * a {@code Vibrator} from the system level service VIBRATOR_SERVICE if the device lacks one.
     */
    private Vibrator getVibrator() {
        if (mLastInputDevice != null) {
            Vibrator vibrator = mLastInputDevice.getVibrator();
            if (vibrator.hasVibrator()) {
                return vibrator;
            }
        }
        return (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Called from the {@code run} method of {@code Runnable mAnimationRunnable} to animate the next
     * frame of our game. First we initialize our variable {@code long currentStepTime} with the time
     * since boot in milliseconds. We then call our method {@code step} with {@code currentStepTime}
     * as the argument to move all the {@code Sprite} objects in our game to the new time. We initialize
     * {@code Handler handler} with a handler associated with the thread running our View and if it is
     * not null, we add {@code mAnimationRunnable} to its message queue to be run ANIMATION_TIME_STEP
     * (16) milliseconds from {@code currentStepTime}, and invalidate our view so the new {@code Sprite}
     * locations will eventually be drawn.
     */
    void animateFrame() {
        long currentStepTime = SystemClock.uptimeMillis();
        step(currentStepTime);

        Handler handler = getHandler();
        if (handler != null) {
            handler.postAtTime(mAnimationRunnable, currentStepTime + ANIMATION_TIME_STEP);
            invalidate();
        }
    }

    /**
     * Moves all the {@code Sprite} objects in our game to the new time {@code long currentStepTime},
     * and removes any that are destroyed when that is done. First we calculate {@code float tau},
     * the number of seconds between {@code mLastStepTime} and {@code currentStepTime}, then we set
     * {@code mLastStepTime} to {@code currentStepTime}. We call our method {@code ensureInitialized}
     * to make sure we have a spaceship to play with, then we call the {@code accelerate} method of
     * {@code Ship mShip} to increase its velocity by the amount {@code mMaxShipThrust} will increase
     * it in {@code tau} seconds (up to the maximum of {@code mMaxShipSpeed}. We then call the
     * {@code step} method of {@code Ship mShip} with {@code tau} as the delta time, and if that
     * returns false (the movement causes the spaceship to be destroyed) we call our method {@code reset}
     * to reset the game to the initial conditions.
     * <p>
     * Next we move all the bullets in our list {@code List<Bullet> mBullets}. To do this we first
     * initialize our variable {@code int numBullets} with the size of {@code mBullets}. Next we loop
     * over {@code i} for all the bullets in {@code mBullets} fetching the i'th bullet to our variable
     * {@code Bullet bullet} and call its method {@code step} with {@code tau} as the delta time. If
     * {@code step} returns false (the bullet has expired for some reason) we remove it from
     * {@code mBullets}, decrement {@code i} and decrement {@code numBullets} then loop around for the
     * next bullet.
     * <p>
     * Now we need to move all the obstacles in our list {@code List<Obstacle> mObstacles}. To do this
     * we first initialize our variable {@code int numObstacles} with the size of {@code mObstacles}.
     * Next we loop over {@code i} for all the obstacles in {@code mObstacles} fetching the i'th
     * obstacle to our variable {@code Obstacle obstacle} and call its method {@code step} with
     * {@code tau} as the delta time. If {@code step} returns false (the obstacle has expired for
     * some reason) we remove it from {@code mObstacles}, decrement {@code i} and decrement
     * {@code numObstacles} then loop around for the next obstacle.
     * <p>
     * Now we have to check for collisions between bullets and obstacles. To do this we loop in an
     * outer loop over {@code i} for the {@code numBullets} left in {@code mBullets} fetching each
     * bullet in turn to our variable {@code Bullet bullet}. In an inner loop we loop over {@code j}
     * for the {@code numObstacles} obstacles remaining in {@code mObstacles} fetching each obstacle
     * in turn to our variable {@code Obstacle obstacle}. We then call the {@code collidesWith} method
     * of {@code bullet} for {@code obstacle} and if that returns true we call the {@code destroy}
     * method of {@code bullet} and the {@code destroy} method of {@code obstacle} and break out of
     * the inner obstacle loop and loop around for the next bullet. If it returns false we loop
     * around for the next combination of bullet and obstacle.
     *
     * Next we check for collisions between the spaceship and obstacles. To do this we loop over
     * {@code i} for the {@code numObstacles} in this list {@code List<Obstacle> mObstacles} fetching
     * each in turn to {@code Obstacle obstacle}, we then call the {@code collidesWith} method of
     * {@code mShip} with {@code obstacle} and if it returns true we call the {@code destroy} method
     * of {@code mShip} and the {@code destroy} method of {@code obstacle} and break out of the loop.
     *
     * We now want to Spawn more obstacles offscreen when needed to replace any destroyed. In an
     * outer loop with the label "OuterLoop:" we loop while the size of {@code mObstacles} is less
     * than MAX_OBSTACLES (12). We define {@code float minDistance} to be 4 times the size of our
     * spaceship {@code mShipSize}, define {@code float size} to be a random obstacle size between
     * {@code mMinObstacleSize} and {@code mMaxObstacleSize}, declare the floats {@code positionX}
     * and {@code positionY}, and set {@code tries} to 0.
     *
     * Then in an inner loop we loop choosing random values for {@code positionX} and {@code positionY}
     * as long as that position is too close to our spaceship (closer than {@code minDistance}), each
     * time incrementing {@code tries} and giving up and breaking out of the outer loop ("OuterLoop:")
     * when {@code tries} is greater than 10. In this inner loop we first choose a random {@code edge}
     * (0-3) to spawn from. We switch on {@code edge}:
     * <ul>
     *     <li>
     *         0: (left edge) we set {@code positionX} to {@code -size} and {@code positionY} to a
     *         random percentage of the height of our view.
     *     </li>
     *     <li>
     *         1: (right edge) we set {@code positionX} to the width of our view plus {@code size}
     *         and {@code positionY} to a random percentage of the height of our view.
     *     </li>
     *     <li>
     *         2: (top edge) we set {@code positionX} to a random percentage of the width of our view,
     *         and {@code positionY} to {@code -size}.
     *     </li>
     *     <li>
     *         default: (bottom edge) we set {@code positionX} to a random percentage of the width of our view,
     *         and {@code positionY} to the height of our view plus {@code size}.
     *     </li>
     * </ul>
     * At the end of this inner loop we increment {@code tries} and give up and break out of the outer
     * loop ("OuterLoop:") if {@code tries} is greater than 10. If it is not, we evaluate our while
     * expression to test whether the obstacle is less than {@code minDistance} from our ship by
     * calling the {@code distanceTo} method of {@code mShip} with {@code positionX} and {@code positionY}
     * as the parameters, and loop back in the inner loop to try another position if it is too close.
     *
     * If it is not, we initialize {@code float direction} to a random percentage of 2 pi, {@code float speed}
     * to be a random number between {@code mMinObstacleSpeed} and {@code mMaxObstacleSpeed}, initialize
     * {@code float velocityX} to be the X component of {@code speed} given the {@code direction}, and
     * {@code float velocityY} to be the Y component of {@code speed} given the {@code direction}. We
     * now create a new instance {@code Obstacle obstacle}, set its position to {@code positionX},
     * {@code positionY}, its size to {@code size}, its velocity to {@code (velocityX, velocityY)},
     * and then add it to {@code mObstacles}.
     *
     * @param currentStepTime current time of the frame we are to build
     */
    private void step(long currentStepTime) {
        float tau = (currentStepTime - mLastStepTime) * 0.001f;
        mLastStepTime = currentStepTime;

        ensureInitialized();

        // Move the ship.
        mShip.accelerate(tau, mMaxShipThrust, mMaxShipSpeed);
        if (!mShip.step(tau)) {
            reset();
        }

        // Move the bullets.
        int numBullets = mBullets.size();
        for (int i = 0; i < numBullets; i++) {
            final Bullet bullet = mBullets.get(i);
            if (!bullet.step(tau)) {
                mBullets.remove(i);
                i -= 1;
                numBullets -= 1;
            }
        }

        // Move obstacles.
        int numObstacles = mObstacles.size();
        for (int i = 0; i < numObstacles; i++) {
            final Obstacle obstacle = mObstacles.get(i);
            if (!obstacle.step(tau)) {
                mObstacles.remove(i);
                i -= 1;
                numObstacles -= 1;
            }
        }

        // Check for collisions between bullets and obstacles.
        for (int i = 0; i < numBullets; i++) {
            final Bullet bullet = mBullets.get(i);
            for (int j = 0; j < numObstacles; j++) {
                final Obstacle obstacle = mObstacles.get(j);
                if (bullet.collidesWith(obstacle)) {
                    bullet.destroy();
                    obstacle.destroy();
                    break;
                }
            }
        }

        // Check for collisions between the ship and obstacles.
        for (int i = 0; i < numObstacles; i++) {
            final Obstacle obstacle = mObstacles.get(i);
            if (mShip.collidesWith(obstacle)) {
                mShip.destroy();
                obstacle.destroy();
                break;
            }
        }

        // Spawn more obstacles offscreen when needed.
        // Avoid putting them right on top of the ship.
        OuterLoop:
        while (mObstacles.size() < MAX_OBSTACLES) {
            final float minDistance = mShipSize * 4;
            float size = mRandom.nextFloat() * (mMaxObstacleSize - mMinObstacleSize) + mMinObstacleSize;
            float positionX, positionY;
            int tries = 0;
            do {
                int edge = mRandom.nextInt(4);
                switch (edge) {
                    case 0:
                        positionX = -size;
                        positionY = mRandom.nextInt(getHeight());
                        break;
                    case 1:
                        positionX = getWidth() + size;
                        positionY = mRandom.nextInt(getHeight());
                        break;
                    case 2:
                        positionX = mRandom.nextInt(getWidth());
                        positionY = -size;
                        break;
                    default:
                        positionX = mRandom.nextInt(getWidth());
                        positionY = getHeight() + size;
                        break;
                }
                if (++tries > 10) {
                    break OuterLoop;
                }
            } while (mShip.distanceTo(positionX, positionY) < minDistance);

            float direction = mRandom.nextFloat() * (float) Math.PI * 2;
            float speed = mRandom.nextFloat() * (mMaxObstacleSpeed - mMinObstacleSpeed) + mMinObstacleSpeed;
            float velocityX = (float) Math.cos(direction) * speed;
            float velocityY = (float) Math.sin(direction) * speed;

            Obstacle obstacle = new Obstacle();
            obstacle.setPosition(positionX, positionY);
            obstacle.setSize(size);
            obstacle.setVelocity(velocityX, velocityY);
            mObstacles.add(obstacle);
        }
    }

    /**
     * We implement this to do our drawing. First we call our super's implementation of {@code onDraw},
     * then if {@code mShip} is not null we ask it to draw itself on the {@code Canvas canvas}. We
     * initialize {@code int numBullets} to the number of bullets in {@code mBullets}, and loop over
     * them fetching each in turn to {@code Bullet bullet} and instructing that {@code Bullet} to draw
     * itself. We initialize {@code int numObstacles} to the number of obstacles in {@code mObstacles},
     * and loop over them fetching each in turn to {@code Obstacle obstacle} and instructing that
     * {@code Obstacle} to draw itself.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the ship.
        if (mShip != null) {
            mShip.draw(canvas);
        }

        // Draw bullets.
        int numBullets = mBullets.size();
        for (int i = 0; i < numBullets; i++) {
            final Bullet bullet = mBullets.get(i);
            bullet.draw(canvas);
        }

        // Draw obstacles.
        int numObstacles = mObstacles.size();
        for (int i = 0; i < numObstacles; i++) {
            final Obstacle obstacle = mObstacles.get(i);
            obstacle.draw(canvas);
        }
    }

    /**
     * Convenience function for calling {@code Math.hypot}, and returning its result cast to float.
     *
     * @param x length of x component
     * @param y length of y component
     * @return the result of calling {@code Math.hypot} for x and y, cast to float
     */
    static float pythag(float x, float y) {
        return (float) Math.hypot(x, y);
    }

    /**
     * Convenience function to calculate a color value that starts at {@code from} and is animated
     * to {@code to} based on the value of {@code alpha}.
     *
     * @param alpha value of alpha component
     * @param from starting value for this color component
     * @param to ending value for this color component
     * @return a value that is positioned by {@code alpha} between {@code from} and {@code to}
     */
    static int blend(float alpha, int from, int to) {
        return from + (int) ((to - from) * alpha);
    }

    /**
     * Convenience function to animate the color of {@code Paint paint} between a "from" color and a
     * "to" color based on the current value of our parameter {@code float alpha}.
     *
     * @param paint {@code Paint} whose color we are to set
     * @param alpha a value between 0 and 1.0 which determines where between from and to we are currently
     * @param a1 from alpha color component
     * @param r1 from red color component
     * @param g1 from green color component
     * @param b1 from blue color component
     * @param a2 to alpha color component
     * @param r2 to red color component
     * @param g2 to green color component
     * @param b2 ti blue color component
     */
    @SuppressWarnings("SameParameterValue")
    static void setPaintARGBBlend(Paint paint, float alpha,
                                  int a1, int r1, int g1, int b1,
                                  int a2, int r2, int g2, int b2) {
        paint.setARGB(blend(alpha, a1, a2), blend(alpha, r1, r2),
                blend(alpha, g1, g2), blend(alpha, b1, b2));
    }

    /**
     * Base class for our {@code Ship}, {@code Bullet}, and {@code Obstacle} objects.
     */
    @SuppressWarnings("WeakerAccess")
    private abstract class Sprite {
        protected float mPositionX;
        protected float mPositionY;
        protected float mVelocityX;
        protected float mVelocityY;
        protected float mSize;
        protected boolean mDestroyed;
        protected float mDestroyAnimProgress;

        public void setPosition(float x, float y) {
            mPositionX = x;
            mPositionY = y;
        }

        public void setVelocity(float x, float y) {
            mVelocityX = x;
            mVelocityY = y;
        }

        public void setSize(float size) {
            mSize = size;
        }

        public float distanceTo(float x, float y) {
            return pythag(mPositionX - x, mPositionY - y);
        }

        public float distanceTo(Sprite other) {
            return distanceTo(other.mPositionX, other.mPositionY);
        }

        public boolean collidesWith(Sprite other) {
            // Really bad collision detection.
            return !mDestroyed && !other.mDestroyed
                    && distanceTo(other) <= Math.max(mSize, other.mSize)
                    + Math.min(mSize, other.mSize) * 0.5f;
        }

        public boolean isDestroyed() {
            return mDestroyed;
        }

        public boolean step(float tau) {
            mPositionX += mVelocityX * tau;
            mPositionY += mVelocityY * tau;

            if (mDestroyed) {
                mDestroyAnimProgress += tau / getDestroyAnimDuration();
                if (mDestroyAnimProgress >= 1.0f) {
                    return false;
                }
            }
            return true;
        }

        public abstract void draw(Canvas canvas);

        public abstract float getDestroyAnimDuration();

        protected boolean isOutsidePlayfield() {
            final int width = GameView.this.getWidth();
            final int height = GameView.this.getHeight();
            return mPositionX < 0 || mPositionX >= width
                    || mPositionY < 0 || mPositionY >= height;
        }

        protected void wrapAtPlayfieldBoundary() {
            final int width = GameView.this.getWidth();
            final int height = GameView.this.getHeight();
            while (mPositionX <= -mSize) {
                mPositionX += width + mSize * 2;
            }
            while (mPositionX >= width + mSize) {
                mPositionX -= width + mSize * 2;
            }
            while (mPositionY <= -mSize) {
                mPositionY += height + mSize * 2;
            }
            while (mPositionY >= height + mSize) {
                mPositionY -= height + mSize * 2;
            }
        }

        public void destroy() {
            mDestroyed = true;
            step(0);
        }
    }

    @SuppressWarnings("WeakerAccess")
    private class Ship extends Sprite {
        private static final float CORNER_ANGLE = (float) Math.PI * 2 / 3;
        private static final float TO_DEGREES = (float) (180.0 / Math.PI);

        private float mHeadingX;
        private float mHeadingY;
        private float mHeadingAngle;
        private float mHeadingMagnitude;
        private final Paint mPaint;
        private final Path mPath;


        public Ship() {
            mPaint = new Paint();
            mPaint.setStyle(Style.FILL);

            setPosition(getWidth() * 0.5f, getHeight() * 0.5f);
            setVelocity(0, 0);
            setSize(mShipSize);

            mPath = new Path();
            mPath.moveTo(0, 0);
            mPath.lineTo((float) Math.cos(-CORNER_ANGLE) * mSize,
                    (float) Math.sin(-CORNER_ANGLE) * mSize);
            mPath.lineTo(mSize, 0);
            mPath.lineTo((float) Math.cos(CORNER_ANGLE) * mSize,
                    (float) Math.sin(CORNER_ANGLE) * mSize);
            mPath.lineTo(0, 0);
        }

        public void setHeadingX(float x) {
            mHeadingX = x;
            updateHeading();
        }

        public void setHeadingY(float y) {
            mHeadingY = y;
            updateHeading();
        }

        public void setHeading(float x, float y) {
            mHeadingX = x;
            mHeadingY = y;
            updateHeading();
        }

        private void updateHeading() {
            mHeadingMagnitude = pythag(mHeadingX, mHeadingY);
            if (mHeadingMagnitude > 0.1f) {
                mHeadingAngle = (float) Math.atan2(mHeadingY, mHeadingX);
            }
        }

        private float polarX(float radius) {
            return (float) Math.cos(mHeadingAngle) * radius;
        }

        private float polarY(float radius) {
            return (float) Math.sin(mHeadingAngle) * radius;
        }

        public float getBulletInitialX() {
            return mPositionX + polarX(mSize);
        }

        public float getBulletInitialY() {
            return mPositionY + polarY(mSize);
        }

        public float getBulletVelocityX(float relativeSpeed) {
            return mVelocityX + polarX(relativeSpeed);
        }

        public float getBulletVelocityY(float relativeSpeed) {
            return mVelocityY + polarY(relativeSpeed);
        }

        @SuppressWarnings("UnusedParameters")
        public void accelerate(float tau, float maxThrust, float maxSpeed) {
            final float thrust = mHeadingMagnitude * maxThrust;
            mVelocityX += polarX(thrust);
            mVelocityY += polarY(thrust);

            final float speed = pythag(mVelocityX, mVelocityY);
            if (speed > maxSpeed) {
                final float scale = maxSpeed / speed;
                mVelocityX = mVelocityX * scale;
                mVelocityY = mVelocityY * scale;
            }
        }

        @Override
        public boolean step(float tau) {
            if (!super.step(tau)) {
                return false;
            }
            wrapAtPlayfieldBoundary();
            return true;
        }

        @Override
        public void draw(Canvas canvas) {
            setPaintARGBBlend(mPaint, mDestroyAnimProgress,
                    255, 63, 255, 63,
                    0, 255, 0, 0);

            canvas.save();
            canvas.translate(mPositionX, mPositionY);
            canvas.rotate(mHeadingAngle * TO_DEGREES);
            canvas.drawPath(mPath, mPaint);
            canvas.restore();
        }

        @Override
        public float getDestroyAnimDuration() {
            return 1.0f;
        }

        @Override
        public void destroy() {
            super.destroy();
            crash();
        }
    }

    @SuppressWarnings("WeakerAccess")
    private class Bullet extends Sprite {
        private final Paint mPaint;

        public Bullet() {
            mPaint = new Paint();
            mPaint.setStyle(Style.FILL);

            setSize(mBulletSize);
        }

        @Override
        public boolean step(float tau) {
            //noinspection SimplifiableIfStatement
            if (!super.step(tau)) {
                return false;
            }
            return !isOutsidePlayfield();
        }

        public void draw(Canvas canvas) {
            setPaintARGBBlend(mPaint, mDestroyAnimProgress,
                    255, 255, 255, 0,
                    0, 255, 255, 255);
            canvas.drawCircle(mPositionX, mPositionY, mSize, mPaint);
        }

        @Override
        public float getDestroyAnimDuration() {
            return 0.125f;
        }
    }

    @SuppressWarnings("WeakerAccess")
    private class Obstacle extends Sprite {
        private final Paint mPaint;

        public Obstacle() {
            mPaint = new Paint();
            mPaint.setARGB(255, 127, 127, 255);
            mPaint.setStyle(Style.FILL);
        }

        @Override
        public boolean step(float tau) {
            if (!super.step(tau)) {
                return false;
            }
            wrapAtPlayfieldBoundary();
            return true;
        }

        public void draw(Canvas canvas) {
            setPaintARGBBlend(mPaint, mDestroyAnimProgress,
                    255, 127, 127, 255,
                    0, 255, 0, 0);
            canvas.drawCircle(mPositionX, mPositionY,
                    mSize * (1.0f - mDestroyAnimProgress), mPaint);
        }

        @Override
        public float getDestroyAnimDuration() {
            return 0.25f;
        }
    }
}
