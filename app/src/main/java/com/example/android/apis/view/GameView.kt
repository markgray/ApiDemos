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
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.os.SystemClock
import android.os.Vibrator
import android.util.AttributeSet
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.math.*

/**
 * A trivial joystick based physics game to demonstrate joystick handling.
 *
 *
 * If the game controller has a vibrator, then it is used to provide feedback
 * when a bullet is fired or the ship crashes into an obstacle.  Otherwise, the
 * system vibrator is used for that purpose.
 *
 *
 * see GameControllerInput
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class GameView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    /**
     * `Random` instance we use to generate random numbers whenever needed.
     */
    private val mRandom: Random = Random()

    /**
     * `Ship` instance that we control with our non-existent game controller.
     */
    private var mShip: Ship? = null

    /**
     * List of `Bullet` objects that are currently in flight.
     */
    private val mBullets: MutableList<Bullet>

    /**
     * List of `Obstacle` objects that are currently in existence.
     */
    private val mObstacles: MutableList<Obstacle>

    /**
     * Milliseconds since boot of the previous time that our method `step` was called to advance
     * the animation of the game.
     */
    private var mLastStepTime: Long = 0

    /**
     * Set to the device of the last `MotionEvent` received by our `onGenericMotionEvent`
     * callback if it is a SOURCE_CLASS_JOYSTICK device, and used to move our spaceship.
     */
    private var mLastInputDevice: InputDevice? = null

    /**
     * Contains bit fields to indicate which of our dpad keys are currently pressed: KEYCODE_DPAD_LEFT,
     * KEYCODE_DPAD_RIGHT, KEYCODE_DPAD_UP, and/or KEYCODE_DPAD_DOWN.
     */
    private var mDPadState = 0

    /**
     * Size of the spaceship in pixels given the logical density of the display.
     */
    private val mShipSize: Float

    /**
     * Speed that the ship can accelerate in pixels given the logical density of the display.
     */
    private val mMaxShipThrust: Float

    /**
     * Maximum speed that the ship can reach in pixels given the logical density of the display.
     */
    private val mMaxShipSpeed: Float

    /**
     * Size of the a bullet in pixels given the logical density of the display.
     */
    private val mBulletSize: Float

    /**
     * Speed of a bullet in pixels given the logical density of the display.
     */
    private val mBulletSpeed: Float

    /**
     * Minimum size of an obstacle in pixels given the logical density of the display.
     */
    private val mMinObstacleSize: Float

    /**
     * Maximum size of an obstacle in pixels given the logical density of the display.
     */
    private val mMaxObstacleSize: Float

    /**
     * Minimum speed of an obstacle in pixels given the logical density of the display.
     */
    private val mMinObstacleSpeed: Float

    /**
     * Maximum speed of an obstacle in pixels given the logical density of the display.
     */
    private val mMaxObstacleSpeed: Float

    /**
     * Background thread that runs every ANIMATION_TIME_STEP milliseconds to animate the next frame
     * of our game.
     */
    private val mAnimationRunnable: Runnable = Runnable {
        animateFrame()
    }

    /**
     * This is called during layout when the size of this view has changed. First we call our super's
     * implementation of `onSizeChanged`, then we call our method `reset` to reset the
     * game.
     *
     * @param w    Current width of this view.
     * @param h    Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Reset the game when the view changes size.
        reset()
    }

    /**
     * Callback for a key being pressed. First we call our method `ensureInitialized` to make
     * sure we have a spaceship to play with. Then we initialize our variable `handled` to
     * false. Then if the `getRepeatCount` method of our parameter `KeyEvent event` returns
     * 0 (we only want to handle the keys on initial down but not on auto-repeat), we switch on the
     * value of `keyCode`:
     *
     *  *
     * KEYCODE_DPAD_LEFT - we call the `setHeadingX` method of our field `Ship mShip`
     * with a value of -1, set the DPAD_STATE_LEFT bit in `mDPadState`, set `handled`
     * to true, and break.
     *
     *  *
     * KEYCODE_DPAD_RIGHT - we call the `setHeadingX` method of our field `Ship mShip`
     * with a value of 1, set the DPAD_STATE_RIGHT bit in `mDPadState`, set `handled`
     * to true, and break.
     *
     *  *
     * KEYCODE_DPAD_UP - we call the `setHeadingY` method of our field `Ship mShip`
     * with a value of -1, set the DPAD_STATE_UP bit in `mDPadState`, set `handled`
     * to true, and break.
     *
     *  *
     * KEYCODE_DPAD_DOWN - we call the `setHeadingY` method of our field `Ship mShip`
     * with a value of 1, set the DPAD_STATE_DOWN bit in `mDPadState`, set `handled`
     * to true, and break.
     *
     *  *
     * default - if our `isFireKey` method returns true for `keyCode`, we call our
     * method `fire`, set `handled` to true, and break.
     *
     *
     * Having handled the keys we are interested in, we check if `handled` is true, and if so
     * we call our method `step` with the time that the `KeyEvent event` occurred, and
     * return true to our caller. Otherwise we return the value returned by our super's implementation
     * of `onKeyDown`.
     *
     * @param keyCode A key code that represents the button pressed
     * @param event   The KeyEvent object that defines the button action.
     * @return true if we handled the event, false otherwise.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        ensureInitialized()

        // Handle DPad keys and fire button on initial down but not on auto-repeat.
        var handled = false
        if (event.repeatCount == 0) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    mShip!!.setHeadingX(-1f)
                    mDPadState = mDPadState or DPAD_STATE_LEFT
                    handled = true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    mShip!!.setHeadingX(1f)
                    mDPadState = mDPadState or DPAD_STATE_RIGHT
                    handled = true
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    mShip!!.setHeadingY(-1f)
                    mDPadState = mDPadState or DPAD_STATE_UP
                    handled = true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    mShip!!.setHeadingY(1f)
                    mDPadState = mDPadState or DPAD_STATE_DOWN
                    handled = true
                }
                else -> if (isFireKey(keyCode)) {
                    fire()
                    handled = true
                }
            }
        }
        if (handled) {
            step(event.eventTime)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * Called when a key is released. First we call our method `ensureInitialized` to make
     * sure we have a spaceship to play with. Then we initialize our variable `handled` to
     * false, and we switch on the value of `keyCode`:
     *
     *  *
     * KEYCODE_DPAD_LEFT - we call the `setHeadingX` method of our field `Ship mShip`
     * with a value of 0, clear the DPAD_STATE_LEFT bit in `mDPadState`, set `handled`
     * to true, and break.
     *
     *  *
     * KEYCODE_DPAD_RIGHT - we call the `setHeadingX` method of our field `Ship mShip`
     * with a value of 0, clear the DPAD_STATE_RIGHT bit in `mDPadState`, set `handled`
     * to true, and break.
     *
     *  *
     * KEYCODE_DPAD_UP - we call the `setHeadingY` method of our field `Ship mShip`
     * with a value of 0, clear the DPAD_STATE_UP bit in `mDPadState`, set `handled`
     * to true, and break.
     *
     *  *
     * KEYCODE_DPAD_DOWN - we call the `setHeadingY` method of our field `Ship mShip`
     * with a value of 0, clear the DPAD_STATE_DOWN bit in `mDPadState`, set `handled`
     * to true, and break.
     *
     *  *
     * default - the our method `isFireKey` returns true for `keyCode` we set `handled`
     * to true and break.
     *
     *
     * If `handled` is now true, we call our method `step` with the time that the
     * `KeyEvent event` occurred, and return true to our caller. Otherwise we return the value
     * returned by our super's implementation of `onKeyDown`.
     *
     * @param keyCode A key code that represents the button pressed, from
     * [android.view.KeyEvent].
     * @param event   The KeyEvent object that defines the button action.
     * @return true if we handled the event.
     */
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        ensureInitialized()

        // Handle keys going up.
        var handled = false
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                mShip!!.setHeadingX(0f)
                mDPadState = mDPadState and DPAD_STATE_LEFT.inv()
                handled = true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                mShip!!.setHeadingX(0f)
                mDPadState = mDPadState and DPAD_STATE_RIGHT.inv()
                handled = true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                mShip!!.setHeadingY(0f)
                mDPadState = mDPadState and DPAD_STATE_UP.inv()
                handled = true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                mShip!!.setHeadingY(0f)
                mDPadState = mDPadState and DPAD_STATE_DOWN.inv()
                handled = true
            }
            else -> if (isFireKey(keyCode)) {
                handled = true
            }
        }
        if (handled) {
            step(event.eventTime)
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    /**
     * We implement this method to handle generic motion events. First we call our method
     * `ensureInitialized` to make sure we have a spaceship to play with. Then if the
     * `MotionEvent event` is from the source SOURCE_CLASS_JOYSTICK (a joystick), and the
     * action of `event` is ACTION_MOVE it is an `MotionEvent` we may be interested in
     * so we do some more processing. If `mLastInputDevice` is null or it does not have the
     * same input device ID as the `MotionEvent event` we set `mLastInputDevice` to the
     * `InputDevice` of `event` (if that is still null we return false to the caller
     * as the `MotionEvent event` is obviously invalid). We make sure that none of our DPAD
     * keys are pressed by checking the value of `mDPadState`, and if any are set we ignore
     * the joystick by returning true to the caller.
     *
     *
     * Now we are ready to process all historical movement samples in the batch. First we initialize
     * our variable `int historySize` with the number of historical points in `event`,
     * then we loop over `i` for all these points calling our method `processJoystickInput`
     * with `event` and `i` (this method will determine an X and Y value for that movement,
     * change the heading of our spaceship appropriately and call our method `step` to advance
     * the animation to the time that that sample occurred). When done with the historical samples,
     * we call `processJoystickInput` again with -1 as the sample number to process the current
     * movement sample in the batch and return true to the caller.
     *
     *
     * If the `MotionEvent event` is not from a joystick we return the value returned by our
     * super's implementation of `onGenericMotionEvent`.
     *
     * @param event The generic motion event being processed.
     * @return True if the event was handled, false otherwise.
     */
    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        ensureInitialized()

        // Check that the event came from a joystick since a generic motion event
        // could be almost anything.
        if ((event.isFromSource(InputDevice.SOURCE_CLASS_JOYSTICK)
                        && event.action == MotionEvent.ACTION_MOVE)) {
            // Cache the most recently obtained device information.
            // The device information may change over time but it can be
            // somewhat expensive to query.
            if (mLastInputDevice == null || mLastInputDevice!!.id != event.deviceId) {
                mLastInputDevice = event.device
                // It's possible for the device id to be invalid.
                // In that case, getDevice() will return null.
                if (mLastInputDevice == null) {
                    return false
                }
            }

            // Ignore joystick while the DPad is pressed to avoid conflicting motions.
            if (mDPadState != 0) {
                return true
            }

            // Process all historical movement samples in the batch.
            val historySize = event.historySize
            for (i in 0 until historySize) {
                processJoystickInput(event, i)
            }

            // Process the current movement sample in the batch.
            processJoystickInput(event, -1)
            return true
        }
        return super.onGenericMotionEvent(event)
    }

    /**
     * Called by our `onGenericMotionEvent` callback to process the `MotionEvent` it
     * received from a joystick. We first try to set our variable `float x` by calling our
     * method `getCenteredAxis` to retrieve the AXIS_X axis from the `historyPos` sample
     * in `event` (we also pass it `mLastInputDevice` so it can determine the range of
     * the device). If that returns 0, we try to get the AXIS_HAT_X axis, and if that returns 0 also
     * we try to get the AXIS_Z axis.
     *
     *
     * Next we try to set our variable `float y` by calling our method `getCenteredAxis`
     * to retrieve the AXIS_Y axis from the `historyPos` sample in `event` (we also pass
     * it `mLastInputDevice` so it can determine the range of the device). If that returns 0,
     * we try to get the AXIS_HAT_Y axis, and if that returns 0 also we try to get the AXIS_RZ axis.
     *
     *
     * We have to try the extra axes because many game pads with two joysticks report the position
     * of the second joystick using the other axis types.
     *
     *
     * Once we have extracted the (x,y) coordinates from `event` we call the `setHeading`
     * method of `Ship mShip` to change its heading, and call our `step` method to advance
     * the animation, using the time the event occurred if `historyPos` is less than 0, or the
     * time that the historical movement `historyPos` occurred between this event and the previous
     * event if greater or equal to zero.
     *
     * @param event      `MotionEvent` that we received in our `onGenericMotionEvent` callback.
     * @param historyPos number of the historical movement sample in the batch (-1 for the current
     * movement).
     */
    private fun processJoystickInput(event: MotionEvent, historyPos: Int) {
        // Get joystick position.
        // Many game pads with two joysticks report the position of the second joystick
        // using the Z and RZ axes so we also handle those.
        // In a real game, we would allow the user to configure the axes manually.
        var x = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_X, historyPos)
        if (x == 0f) {
            x = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_HAT_X, historyPos)
        }
        if (x == 0f) {
            x = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_Z, historyPos)
        }
        var y = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_Y, historyPos)
        if (y == 0f) {
            y = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_HAT_Y, historyPos)
        }
        if (y == 0f) {
            y = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_RZ, historyPos)
        }

        // Set the ship heading.
        mShip!!.setHeading(x, y)
        step(if (historyPos < 0) event.eventTime else event.getHistoricalEventTime(historyPos))
    }

    /**
     * Called when the window containing this view gains or loses focus. If our parameter `hasWindowFocus`
     * is true, we get a handler associated with the thread running this View (This handler can be used to pump
     * events in the UI events queue) and add our `Runnable mAnimationRunnable` to its message queue
     * with a delay of ANIMATION_TIME_STEP (16). We then set `mLastStepTime` to the current milliseconds
     * since boot.
     *
     *
     * If `hasWindowFocus` is false, we remove all scheduled `Runnable mAnimationRunnable` from
     * the handler associated with the thread running this View, set `mDPadState` (no keys pressed),
     * and if `Ship mShip` is not null we call its `setHeading` method to set its heading to
     * (0,0) and its `setVelocity` method to set its velocity to (0,0).
     *
     *
     * Finally we return the value returned by our super's implementation of `onWindowFocusChanged` to
     * our caller.
     *
     * @param hasWindowFocus True if the window containing this view now has
     * focus, false otherwise.
     */
    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        // Turn on and off animations based on the window focus.
        // Alternately, we could update the game state using the Activity onResume()
        // and onPause() lifecycle events.
        if (hasWindowFocus) {
            handler.postDelayed(mAnimationRunnable, ANIMATION_TIME_STEP)
            mLastStepTime = SystemClock.uptimeMillis()
        } else {
            handler.removeCallbacks(mAnimationRunnable)
            mDPadState = 0
            if (mShip != null) {
                mShip!!.setHeading(0f, 0f)
                mShip!!.setVelocity(0f, 0f)
            }
        }
        super.onWindowFocusChanged(hasWindowFocus)
    }

    /**
     * Called to have the spaceship fire its gun. First we make sure that `Ship mShip` is not
     * null, and that its `isDestroyed` method returns false, returning having done nothing if
     * we no longer have a spaceship. If we are still alive we initialize `Bullet bullet` with
     * a new instance, call its `setPosition` method to set its initial position to the initial
     * position to the position `mShip` dictates for a bullet by its `getBulletInitialX`
     * and `getBulletInitialY` methods, and call its `setVelocity` method to set its
     * velocity to the bullet velocity that `mShip` dictates for a bullet using its
     * `getBulletVelocityX` and `getBulletVelocityY` methods. We then add `bullet`
     * to our list of bullets in `List<Bullet> mBullets`. Finally we get the vibrator service
     * associated with the device `InputDevice mLastInputDevice` and ask it to vibrate for 20
     * milliseconds.
     */
    private fun fire() {
        if (mShip != null && !mShip!!.isDestroyed) {
            val bullet = Bullet()
            bullet.setPosition(mShip!!.bulletInitialX, mShip!!.bulletInitialY)
            bullet.setVelocity(mShip!!.getBulletVelocityX(mBulletSpeed), mShip!!.getBulletVelocityY(mBulletSpeed))
            mBullets.add(bullet)
            @Suppress("DEPRECATION")
            vibrator.vibrate(20)
        }
    }

    /**
     * Convenience function to call `reset` if `Ship mShip` is null.
     */
    private fun ensureInitialized() {
        if (mShip == null) {
            reset()
        }
    }

    /**
     * Called when an obstacle hits our `Ship mShip` in our `step` method. We simply get
     * the vibrator service associated with the device `InputDevice mLastInputDevice` and ask
     * it to vibrate for a series of pulses to simulate a "crash" of our spaceship.
     */
    private fun crash() {
        @Suppress("DEPRECATION")
        vibrator.vibrate(longArrayOf(0, 20, 20, 40, 40, 80, 40, 300), -1)
    }

    /**
     * Resets the game to the starting conditions. First we create a new instance for `Ship mShip`,
     * then we clear our list of bullets in `List<Bullet> mBullets` and our list of obstacles
     * in `List<Obstacle> mObstacles`.
     */
    private fun reset() {
        mShip = Ship()
        mBullets.clear()
        mObstacles.clear()
    }

    /**
     * Gets a `Vibrator` instance to use for some buzzing, either the vibrator associated with
     * the current input device or the system level vibrator if the device lacks a vibrator. First we
     * check that `mLastInputDevice` is not null, and if it is not we initialize our variable
     * `Vibrator vibrator` by calling the `getVibrator` method of `mLastInputDevice`.
     * If the `hasVibrator` method of `vibrator` returns true we return `vibrator`
     * to the caller.
     *
     *
     * Otherwise we return a `Vibrator` from the system level service VIBRATOR_SERVICE.
     *
     * @return vibrator service associated with the device `InputDevice mLastInputDevice` or
     * a `Vibrator` from the system level service VIBRATOR_SERVICE if the device lacks one.
     */
    private val vibrator: Vibrator
        get() {
            if (mLastInputDevice != null) {
                val vibrator = mLastInputDevice!!.vibrator
                if (vibrator.hasVibrator()) {
                    return vibrator
                }
            }
            return context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

    /**
     * Called from the `run` method of `Runnable mAnimationRunnable` to animate the next
     * frame of our game. First we initialize our variable `long currentStepTime` with the time
     * since boot in milliseconds. We then call our method `step` with `currentStepTime`
     * as the argument to move all the `Sprite` objects in our game to the new time. We initialize
     * `Handler handler` with a handler associated with the thread running our View and if it is
     * not null, we add `mAnimationRunnable` to its message queue to be run ANIMATION_TIME_STEP
     * (16) milliseconds from `currentStepTime`, and invalidate our view so the new `Sprite`
     * locations will eventually be drawn.
     */
    fun animateFrame() {
        val currentStepTime = SystemClock.uptimeMillis()
        step(currentStepTime)
        val handler = handler
        if (handler != null) {
            handler.postAtTime(mAnimationRunnable, currentStepTime + ANIMATION_TIME_STEP)
            invalidate()
        }
    }

    /**
     * Moves all the `Sprite` objects in our game to the new time `long currentStepTime`,
     * and removes any that are destroyed when that is done. First we calculate `float tau`,
     * the number of seconds between `mLastStepTime` and `currentStepTime`, then we set
     * `mLastStepTime` to `currentStepTime`. We call our method `ensureInitialized`
     * to make sure we have a spaceship to play with, then we call the `accelerate` method of
     * `Ship mShip` to increase its velocity by the amount `mMaxShipThrust` will increase
     * it in `tau` seconds (up to the maximum of `mMaxShipSpeed`. We then call the
     * `step` method of `Ship mShip` with `tau` as the delta time, and if that
     * returns false (the movement causes the spaceship to be destroyed) we call our method `reset`
     * to reset the game to the initial conditions.
     *
     *
     * Next we move all the bullets in our list `List<Bullet> mBullets`. To do this we first
     * initialize our variable `int numBullets` with the size of `mBullets`. Next we loop
     * over `i` for all the bullets in `mBullets` fetching the i'th bullet to our variable
     * `Bullet bullet` and call its method `step` with `tau` as the delta time. If
     * `step` returns false (the bullet has expired for some reason) we remove it from
     * `mBullets`, decrement `i` and decrement `numBullets` then loop around for the
     * next bullet.
     *
     *
     * Now we need to move all the obstacles in our list `List<Obstacle> mObstacles`. To do this
     * we first initialize our variable `int numObstacles` with the size of `mObstacles`.
     * Next we loop over `i` for all the obstacles in `mObstacles` fetching the i'th
     * obstacle to our variable `Obstacle obstacle` and call its method `step` with
     * `tau` as the delta time. If `step` returns false (the obstacle has expired for
     * some reason) we remove it from `mObstacles`, decrement `i` and decrement
     * `numObstacles` then loop around for the next obstacle.
     *
     *
     * Now we have to check for collisions between bullets and obstacles. To do this we loop in an
     * outer loop over `i` for the `numBullets` left in `mBullets` fetching each
     * bullet in turn to our variable `Bullet bullet`. In an inner loop we loop over `j`
     * for the `numObstacles` obstacles remaining in `mObstacles` fetching each obstacle
     * in turn to our variable `Obstacle obstacle`. We then call the `collidesWith` method
     * of `bullet` for `obstacle` and if that returns true we call the `destroy`
     * method of `bullet` and the `destroy` method of `obstacle` and break out of
     * the inner obstacle loop and loop around for the next bullet. If it returns false we loop
     * around for the next combination of bullet and obstacle.
     *
     *
     * Next we check for collisions between the spaceship and obstacles. To do this we loop over
     * `i` for the `numObstacles` in this list `List<Obstacle> mObstacles` fetching
     * each in turn to `Obstacle obstacle`, we then call the `collidesWith` method of
     * `mShip` with `obstacle` and if it returns true we call the `destroy` method
     * of `mShip` and the `destroy` method of `obstacle` and break out of the loop.
     *
     *
     * We now want to Spawn more obstacles offscreen when needed to replace any destroyed. In an
     * outer loop with the label "OuterLoop:" we loop while the size of `mObstacles` is less
     * than MAX_OBSTACLES (12). We define `float minDistance` to be 4 times the size of our
     * spaceship `mShipSize`, define `float size` to be a random obstacle size between
     * `mMinObstacleSize` and `mMaxObstacleSize`, declare the floats `positionX`
     * and `positionY`, and set `tries` to 0.
     *
     *
     * Then in an inner loop we loop choosing random values for `positionX` and `positionY`
     * as long as that position is too close to our spaceship (closer than `minDistance`), each
     * time incrementing `tries` and giving up and breaking out of the outer loop ("OuterLoop:")
     * when `tries` is greater than 10. In this inner loop we first choose a random `edge`
     * (0-3) to spawn from. We switch on `edge`:
     *
     *  *
     * 0: (left edge) we set `positionX` to `-size` and `positionY` to a
     * random percentage of the height of our view.
     *
     *  *
     * 1: (right edge) we set `positionX` to the width of our view plus `size`
     * and `positionY` to a random percentage of the height of our view.
     *
     *  *
     * 2: (top edge) we set `positionX` to a random percentage of the width of our view,
     * and `positionY` to `-size`.
     *
     *  *
     * default: (bottom edge) we set `positionX` to a random percentage of the width of our view,
     * and `positionY` to the height of our view plus `size`.
     *
     *
     * At the end of this inner loop we increment `tries` and give up and break out of the outer
     * loop ("OuterLoop:") if `tries` is greater than 10. If it is not, we evaluate our while
     * expression to test whether the obstacle is less than `minDistance` from our ship by
     * calling the `distanceTo` method of `mShip` with `positionX` and `positionY`
     * as the parameters, and loop back in the inner loop to try another position if it is too close.
     *
     *
     * If it is not, we initialize `float direction` to a random percentage of 2 pi, `float speed`
     * to be a random number between `mMinObstacleSpeed` and `mMaxObstacleSpeed`, initialize
     * `float velocityX` to be the X component of `speed` given the `direction`, and
     * `float velocityY` to be the Y component of `speed` given the `direction`. We
     * now create a new instance `Obstacle obstacle`, set its position to `positionX`,
     * `positionY`, its size to `size`, its velocity to `(velocityX, velocityY)`,
     * and then add it to `mObstacles`.
     *
     * @param currentStepTime current time of the frame we are to build
     */
    private fun step(currentStepTime: Long) {
        val tau = (currentStepTime - mLastStepTime) * 0.001f
        mLastStepTime = currentStepTime
        ensureInitialized()

        // Move the ship.
        mShip!!.accelerate(tau, mMaxShipThrust, mMaxShipSpeed)
        if (!mShip!!.step(tau)) {
            reset()
        }

        // Move the bullets.
        var numBullets = mBullets.size
        run {
            var i = 0
            while (i < numBullets) {
                val bullet: Bullet = mBullets[i]
                if (!bullet.step(tau)) {
                    mBullets.removeAt(i)
                    i -= 1
                    numBullets -= 1
                }
                i++
            }
        }

        // Move obstacles.
        var numObstacles = mObstacles.size
        run {
            var i = 0
            while (i < numObstacles) {
                val obstacle: Obstacle = mObstacles[i]
                if (!obstacle.step(tau)) {
                    mObstacles.removeAt(i)
                    i -= 1
                    numObstacles -= 1
                }
                i++
            }
        }

        // Check for collisions between bullets and obstacles.
        for (i in 0 until numBullets) {
            val bullet = mBullets[i]
            for (j in 0 until numObstacles) {
                val obstacle = mObstacles[j]
                if (bullet.collidesWith(obstacle)) {
                    bullet.destroy()
                    obstacle.destroy()
                    break
                }
            }
        }

        // Check for collisions between the ship and obstacles.
        for (i in 0 until numObstacles) {
            val obstacle = mObstacles[i]
            if (mShip!!.collidesWith(obstacle)) {
                mShip!!.destroy()
                obstacle.destroy()
                break
            }
        }

        // Spawn more obstacles offscreen when needed.
        // Avoid putting them right on top of the ship.
        OuterLoop@ while (mObstacles.size < MAX_OBSTACLES) {
            val minDistance = mShipSize * 4
            val size = mRandom.nextFloat() * (mMaxObstacleSize - mMinObstacleSize) + mMinObstacleSize
            var positionX: Float
            var positionY: Float
            var tries = 0
            do {
                when (mRandom.nextInt(4)) {
                    0 -> {
                        positionX = -size
                        positionY = mRandom.nextInt(height).toFloat()
                    }
                    1 -> {
                        positionX = width + size
                        positionY = mRandom.nextInt(height).toFloat()
                    }
                    2 -> {
                        positionX = mRandom.nextInt(width).toFloat()
                        positionY = -size
                    }
                    else -> {
                        positionX = mRandom.nextInt(width).toFloat()
                        positionY = height + size
                    }
                }
                if (++tries > 10) {
                    break@OuterLoop
                }
            } while (mShip!!.distanceTo(positionX, positionY) < minDistance)
            val direction = mRandom.nextFloat() * Math.PI.toFloat() * 2
            val speed = mRandom.nextFloat() * (mMaxObstacleSpeed - mMinObstacleSpeed) + mMinObstacleSpeed
            val velocityX = cos(direction.toDouble()).toFloat() * speed
            val velocityY = sin(direction.toDouble()).toFloat() * speed
            val obstacle = Obstacle()
            obstacle.setPosition(positionX, positionY)
            obstacle.setSize(size)
            obstacle.setVelocity(velocityX, velocityY)
            mObstacles.add(obstacle)
        }
    }

    /**
     * We implement this to do our drawing. First we call our super's implementation of `onDraw`,
     * then if `mShip` is not null we ask it to draw itself on the `Canvas canvas`. We
     * initialize `int numBullets` to the number of bullets in `mBullets`, and loop over
     * them fetching each in turn to `Bullet bullet` and instructing that `Bullet` to draw
     * itself. We initialize `int numObstacles` to the number of obstacles in `mObstacles`,
     * and loop over them fetching each in turn to `Obstacle obstacle` and instructing that
     * `Obstacle` to draw itself.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the ship.
        if (mShip != null) {
            mShip!!.draw(canvas)
        }

        // Draw bullets.
        val numBullets = mBullets.size
        for (i in 0 until numBullets) {
            val bullet = mBullets[i]
            bullet.draw(canvas)
        }

        // Draw obstacles.
        val numObstacles = mObstacles.size
        for (i in 0 until numObstacles) {
            val obstacle = mObstacles[i]
            obstacle.draw(canvas)
        }
    }

    /**
     * Base class for our `Ship`, `Bullet`, and `Obstacle` objects.
     */
    private abstract inner class Sprite {
        /**
         * X coordinate of the position of the `Sprite` in pixels
         */
        protected var mPositionX = 0f

        /**
         * Y coordinate of the position of the `Sprite` in pixels
         */
        protected var mPositionY = 0f

        /**
         * X component of the velocity of the `Sprite` in pixels per second
         */
        protected var mVelocityX = 0f

        /**
         * Y component of the velocity of the `Sprite` in pixels per second
         */
        protected var mVelocityY = 0f

        /**
         * Size of the `Sprite` in pixels
         */
        protected var mSize = 0f

        /**
         * Getter for our `mDestroyed` field.
         *
         * @return the value of our field `mDestroyed`.
         */
        /**
         * Flag to indicate that the `Sprite` has been destroyed
         */
        var isDestroyed = false
            protected set

        /**
         * How far along in the destruction animation we are, ranges from 0 (start) to 1.0 (gone).
         */
        protected var mDestroyAnimProgress = 0f

        /**
         * Setter for the position of the `Sprite`, just saves its parameters `x` and
         * `y` in our fields `mPositionX` and `mPositionY` respectively.
         *
         * @param x new X coordinate of the `Sprite`
         * @param y new Y coordinate of the `Sprite`
         */
        fun setPosition(x: Float, y: Float) {
            mPositionX = x
            mPositionY = y
        }

        /**
         * Setter for the velocity of the `Sprite`, just saves its parameters `x` and
         * `y` in our fields `mVelocityX` and `mVelocityY` respectively.
         *
         * @param x new X component of the `Sprite` velocity
         * @param y new Y component of the `Sprite` velocity
         */
        fun setVelocity(x: Float, y: Float) {
            mVelocityX = x
            mVelocityY = y
        }

        /**
         * Setter for the size of the `Sprite`, just saves its parameter `size` in our
         * field `mSize`.
         *
         * @param size new size of the `Sprite`
         */
        fun setSize(size: Float) {
            mSize = size
        }

        /**
         * Calculates the distance from our position to a point that has the coordinates given by our
         * parameters `x` and `y` by calling our method `pythag` on the results
         * of subtracting `x` from `mPositionX` and `y` from `mPositionY`.
         *
         * @param x X coordinate of the point we are interested in
         * @param y Y coordinate of the point we are interested in
         * @return distance from our position to the point (x,y) in pixels
         */
        fun distanceTo(x: Float, y: Float): Float {
            return pythag(mPositionX - x, mPositionY - y)
        }

        /**
         * Calculates the distance between us and the position of `Sprite other` by calling
         * our `distanceTo(float x, float y)` method with the `mPositionX` and
         * `mPositionY` fields of our parameter `Sprite other`.
         *
         * @param other the `Sprite` we wish to measure the distance to
         * @return the distance in pixels between us and the position of `Sprite other`
         */
        fun distanceTo(other: Sprite): Float {
            return distanceTo(other.mPositionX, other.mPositionY)
        }

        /**
         * Detects whether we are colliding with our parameter `Sprite other`. The short circuit
         * and argument of our return statement returns false if we have been destroyed (our field
         * `mDestroyed` is true), and false if our parameter `Sprite other` has been
         * destroyed (its `mDestroyed` is true), and false if `Sprite other` is farther
         * away than the maximum of the size of the two `Sprite` objects plus 0.5 times the
         * minimum of the size of the two `Sprite` objects. If we have not been destroyed, and
         * `Sprite other` has not been destroyed, AND we are closer than the maximum of the size
         * of the two `Sprite` objects plus 0.5 times the minimum of the size of the two
         * `Sprite` objects we return true, the two `Sprite` objects are colliding.
         *
         * @param other `Sprite` we are checking for collision with us
         * @return true if we are colliding with the `Sprite other`
         */
        fun collidesWith(other: Sprite): Boolean {
            // Really bad collision detection.
            return (!isDestroyed && !other.isDestroyed
                    && (distanceTo(other) <= mSize.coerceAtLeast(other.mSize)
                    + mSize.coerceAtMost(other.mSize) * 0.5f))
        }

        /**
         * Base method to advance our animation by `float tau` seconds, derived classes override
         * us to add any special handling required by their objects, calling us to do the basic step
         * operations. First we add `tau` times `mVelocityX` to `mPositionX` and
         * add `tau` times `mVelocityY` to `mPositionY`. If we have been destroyed
         * (`mDestroyed` is true) we add `tau` divided by the value returned by our
         * overridden method `getDestroyAnimDuration` to `mDestroyAnimProgress` and if
         * the result is greater than 1.0f we return false to our caller (our destruction animation
         * has reached its end). We fall through to return true to the caller (same as if we had not
         * been destroyed).
         *
         * @param tau delta time in seconds to step our animation
         * @return true if our `Sprite` object has successfully been moved, false if it has
         * disappeared from the game.
         */
        open fun step(tau: Float): Boolean {
            mPositionX += mVelocityX * tau
            mPositionY += mVelocityY * tau
            if (isDestroyed) {
                mDestroyAnimProgress += tau / destroyAnimDuration
                if (mDestroyAnimProgress >= 1.0f) {
                    return false
                }
            }
            return true
        }

        /**
         * Derived classes must override this to get drawn.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        abstract fun draw(canvas: Canvas)

        /**
         * Derived classes must override this to specify a divisor of the delta time `tau` to
         * use to calculate a new value for `mDestroyAnimProgress` in our method `step`
         * if we have been destroyed.
         *
         * @return divisor of the delta time `tau` to use to calculate a new value for
         * `mDestroyAnimProgress` in our method `step` if we have been destroyed.
         */
        abstract val destroyAnimDuration: Float

        /**
         * Convenience function to check whether our position is outside of our view. We initialize
         * `int width` with the width of our `GameView` instance, and `int height`
         * with the height of our `GameView` instance. We return true if `mPositionX` is
         * less than 0, or `mPositionX` is greater than or equal to `width`, or
         * `mPositionY` is less than 0, or `mPositionY` is greater than or equal to
         * `height`. Otherwise we return false.
         *
         * @return true if our position is outside of our view, false if it is inside the view
         */
        protected val isOutsidePlayfield: Boolean
            get() {
                val width = this@GameView.width
                val height = this@GameView.height
                return (mPositionX < 0) || (mPositionX >= width
                        ) || (mPositionY < 0) || (mPositionY >= height)
            }

        /**
         * Wraps the values of `mPositionX` and `mPositionY` around to the other side of
         * our view when they fall outside of our view. We initialize `int width` with the
         * width of our `GameView` instance, and `int height` with the height of our
         * `GameView` instance.
         *
         *
         * While `mPositionX` is less than or equal to `-mSize` we add `width` plus
         * 2 times `mSize` to it.
         *
         *
         * While `mPositionX` is greater than or equal to `width` plus `mSize` we
         * subtract `width` plus 2 times `mSize` to it.
         *
         *
         * While `mPositionY` is less than or equal to `-mSize` we add `height`
         * plus 2 times `mSize` to it.
         *
         *
         * While `mPositionY` is greater than or equal to `height` plus `mSize` we
         * subtract `height` plus 2 times `mSize` to it.
         */
        protected fun wrapAtPlayfieldBoundary() {
            val width = this@GameView.width
            val height = this@GameView.height
            while (mPositionX <= -mSize) {
                mPositionX += width + mSize * 2
            }
            while (mPositionX >= width + mSize) {
                mPositionX -= width + mSize * 2
            }
            while (mPositionY <= -mSize) {
                mPositionY += height + mSize * 2
            }
            while (mPositionY >= height + mSize) {
                mPositionY -= height + mSize * 2
            }
        }

        /**
         * Called when our `Sprite` object has been destroyed. We set our flag `mDestroyed`
         * to true, and call our method `step` with a delta time `tau` of 0 to begin our
         * destruction animation.
         */
        open fun destroy() {
            isDestroyed = true
            step(0f)
        }
    }

    /**
     * `Sprite` subclass adding functionality needed to model our spaceship.
     */
    private inner class Ship : Sprite() {
        /**
         * X coordinate of the arrowhead "point" of our spaceship, relative to the center of our view.
         */
        private var mHeadingX = 0f

        /**
         * Y coordinate of the arrowhead "point" of our spaceship, relative to the center of our view.
         */
        private var mHeadingY = 0f

        /**
         * Polar coordinate angle of the heading of our spaceship in radians.
         */
        private var mHeadingAngle = 0f

        /**
         * Polar coordinate length of the heading of our spaceship in pixels.
         */
        private var mHeadingMagnitude = 0f

        /**
         * `Paint` we use to draw our spaceship.
         */
        private val mPaint: Paint = Paint()

        /**
         * `Path` defining the shape of our spaceship (an arrowhead), created in our constructor
         * and used by our `draw` method to draw it by calling `Canvas.drawPath`.
         */
        private val mPath: Path

        /**
         * Setter for the X coordinate of our heading, we set our field `mHeadingX` to our
         * parameter `x` and call our method `updateHeading` to calculate and set the
         * polar equivalent of the new heading.
         *
         * @param x value to set the X coordinate of our heading to
         */
        fun setHeadingX(x: Float) {
            mHeadingX = x
            updateHeading()
        }

        /**
         * Setter for the Y coordinate of our heading, we set our field `mHeadingY` to our
         * parameter `y` and call our method `updateHeading` to calculate and set the
         * polar equivalent of the new heading.
         *
         * @param y value to set the Y coordinate of our heading to
         */
        fun setHeadingY(y: Float) {
            mHeadingY = y
            updateHeading()
        }

        /**
         * Setter for both the X and the Y coordinate of our heading. We set our field `mHeadingX`
         * to our parameter `x` and our field `mHeadingY` to our parameter `y` then
         * call our method `updateHeading` to calculate and set the polar equivalent of the new
         * heading.
         *
         * @param x value to set the X coordinate of our heading to
         * @param y value to set the Y coordinate of our heading to
         */
        fun setHeading(x: Float, y: Float) {
            mHeadingX = x
            mHeadingY = y
            updateHeading()
        }

        /**
         * Updates the polar coordinate version of our heading from our fields `mHeadingX` and
         * `mHeadingY`. We call our method `pythag` with our fields `mHeadingX` and
         * `mHeadingY` as the parameters to calculate `mHeadingMagnitude`, and if the
         * result is greater than 0.1, we set `mHeadingAngle` to the `Math.atan2` of
         * `mHeadingX` and `mHeadingY`.
         */
        private fun updateHeading() {
            mHeadingMagnitude = pythag(mHeadingX, mHeadingY)
            if (mHeadingMagnitude > 0.1f) {
                mHeadingAngle = atan2(mHeadingY.toDouble(), mHeadingX.toDouble()).toFloat()
            }
        }

        /**
         * Calculates the X coordinate of the end of a vector with the length of its parameter
         * `radius` pointing in the same direction as our ship. Simple trig equation.
         *
         * @param radius length of vector whose X coordinate we are interested in
         * @return X coordinate of the end of the vector
         */
        private fun polarX(radius: Float): Float {
            return cos(mHeadingAngle.toDouble()).toFloat() * radius
        }

        /**
         * Calculates the Y coordinate of the end of a vector with the length of its parameter
         * `radius` pointing in the same direction as our ship. Simple trig equation.
         *
         * @param radius length of vector whose Y coordinate we are interested in
         * @return Y coordinate of the end of the vector
         */
        private fun polarY(radius: Float): Float {
            return sin(mHeadingAngle.toDouble()).toFloat() * radius
        }

        /**
         * Calculates the initial X coordinate of a bullet being fired from our spaceship, by adding
         * the X coordinate of the spaceships position to the X coordinate of the front of the
         * spaceship calculated by our method `polarX(mSize)`.
         *
         * @return initial X coordinate of a bullet being fired from our spaceship.
         */
        val bulletInitialX: Float
            get() = mPositionX + polarX(mSize)

        /**
         * Calculates the initial Y coordinate of a bullet being fired from our spaceship, by adding
         * the Y coordinate of the spaceships position to the Y coordinate of the front of the
         * spaceship calculated by our method `polarY(mSize)`.
         *
         * @return initial Y coordinate of a bullet being fired from our spaceship.
         */
        val bulletInitialY: Float
            get() = mPositionY + polarY(mSize)

        /**
         * Calculates the X component of the absolute velocity of a bullet (that is, its velocity
         * relative to the view) given the relative speed of a bullet as given by our parameter
         * `relativeSpeed`, and the X component of the velocity of the spaceship. We do this
         * by adding the X component of the velocity of the spaceship (`mVelocityX`) to the
         * X component of `relativeSpeed` when the bullet leaves the front of the spaceship
         * as calculated by our method `polarX(mSize)`.
         *
         * @param relativeSpeed speed of the bullet relative to the spaceship
         * @return X component of the velocity of a bullet relative to the view.
         */
        fun getBulletVelocityX(relativeSpeed: Float): Float {
            return mVelocityX + polarX(relativeSpeed)
        }

        /**
         * Calculates the Y component of the absolute velocity of a bullet (that is, its velocity
         * relative to the view) given the relative speed of a bullet as given by our parameter
         * `relativeSpeed`, and the Y component of the velocity of the spaceship. We do this
         * by adding the Y component of the velocity of the spaceship (`mVelocityY`) to the
         * Y component of `relativeSpeed` when the bullet leaves the front of the spaceship
         * as calculated by our method `polarY(mSize)`.
         *
         * @param relativeSpeed speed of the bullet relative to the spaceship
         * @return X component of the velocity of a bullet relative to the view.
         */
        fun getBulletVelocityY(relativeSpeed: Float): Float {
            return mVelocityY + polarY(relativeSpeed)
        }

        /**
         * Applies the maximum thrust to the spaceship in the direction that the spaceship is
         * heading. We initialize `float thrust` to the current polar heading coordinate
         * `mHeadingMagnitude` times our parameter `maxThrust`. We add the X component
         * of `thrust` to `mVelocityX`, and the Y component to `mVelocityY`. We
         * calculate the new speed `float speed ` (pixels per second) by calling our method
         * `pythag(mVelocityX, mVelocityY)`. If `speed` is greater than `maxSpeed`
         * we calculate `float scale` to be `maxSpeed` divided by `speed` and scale
         * both `mVelocityX` and `mVelocityY` by it.
         *
         * @param tau       delta time that the thrust is being applied UNUSED
         * @param maxThrust maximum thrust of the spaceship
         * @param maxSpeed  maximum speed of the spaceship
         */
        @Suppress("UNUSED_PARAMETER")
        fun accelerate(tau: Float, maxThrust: Float, maxSpeed: Float) {
            val thrust = mHeadingMagnitude * maxThrust
            mVelocityX += polarX(thrust)
            mVelocityY += polarY(thrust)
            val speed = pythag(mVelocityX, mVelocityY)
            if (speed > maxSpeed) {
                val scale = maxSpeed / speed
                mVelocityX *= scale
                mVelocityY *= scale
            }
        }

        /**
         * Called to advance our spaceship's animation by `float tau` seconds. If our super's
         * implementation of `step` returns false (our `Sprite` object has disappeared
         * from the game) we return false to the caller. Otherwise we make sure that our spaceship
         * wraps around to the other side of the view if we cross one of the edges by calling our
         * method `wrapAtPlayfieldBoundary`, and return true to the caller.
         *
         * @param tau delta time in seconds to step our animation
         * @return true if our `Ship` object has successfully been moved, false if it has
         * disappeared from the game.
         */
        override fun step(tau: Float): Boolean {
            if (!super.step(tau)) {
                return false
            }
            wrapAtPlayfieldBoundary()
            return true
        }

        /**
         * We implement this to get drawn. First we call the method `setPaintARGBBlend` to set
         * the color of `mPaint` to a color that is appropriate for the stage of destruction
         * given by `mDestroyAnimProgress` (a puke green shade to start with an alpha of 255,
         * which morphs to RED with an alpha of 0 when we are fully destroyed, we stay a puke green
         * until we hit an obstacle of course). Then we save the state of `Canvas canvas` on
         * its private stack, move it to `(mPositionX, mPositionY)`, rotate the canvas to
         * `mHeadingAngle` (converted to degrees by multiplying it by TO_DEGREES), draw the
         * `Path mPath` defining our shape using `mPaint` as the paint, and restore the
         * state of `canvas`.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        override fun draw(canvas: Canvas) {
            setPaintARGBBlend(mPaint, mDestroyAnimProgress,
                    255, 63, 255, 63,
                    0, 255, 0, 0)
            canvas.save()
            canvas.translate(mPositionX, mPositionY)
            canvas.rotate(mHeadingAngle * TO_DEGREES)
            canvas.drawPath(mPath, mPaint)
            canvas.restore()
        }

        /**
         * Just returns 1.0 to use as the scaling factor to divide the delta time `tau` by in
         * order to calculate the value to add to `mDestroyAnimProgress` in `Sprite.step`
         * (if we are currently animating our destruction that is).
         *
         * @return scaling factor to divide the delta time `tau` by in order to calculate the
         * value to add to `mDestroyAnimProgress` in `Sprite.step` (if we are currently
         * animating our destruction that is).
         */
        override val destroyAnimDuration: Float
            get() = 1.0f

        /**
         * Called when we have collided with an obstacle and have entered our "destruction phase".
         * First we call our super's implementation of `destroy` to initiate the destruction
         * animation, then we call our method `crash` to perform some appropriate vibrating.
         */
        override fun destroy() {
            super.destroy()
            crash()
        }

        /**
         * Our constructor. We initialize our field `Paint mPaint` with a new instance, and set
         * its style to FILL. We set our position to the center of our view, set our velocity to 0,
         * and set our ship size to `mShipSize`. We create a new instance for `Path mPath`,
         * move to (0,0), draw a line to (-19.687501987396608,-34.09974912658822), draw a line to
         * (39.375,0), draw a line to (-19.687501987396608,34.09974912658822), and draw a line to
         * (0,0) (an arrowhead shape, the values are for a Pixel phone, other phones with different
         * display densities will result in values scaled for that density).
         */
        init {
            mPaint.style = Paint.Style.FILL
            setPosition(width * 0.5f, height * 0.5f)
            setVelocity(0f, 0f)
            setSize(mShipSize)
            mPath = Path()
            mPath.moveTo(0f, 0f)
            mPath.lineTo(cos(-CORNER_ANGLE.toDouble()).toFloat() * mSize,
                    sin(CORNER_ANGLE.toDouble()).toFloat() * mSize)
            mPath.lineTo(mSize, 0f)
            mPath.lineTo(cos(CORNER_ANGLE.toDouble()).toFloat() * mSize,
                    sin(CORNER_ANGLE.toDouble()).toFloat() * mSize)
            mPath.lineTo(0f, 0f)
        }
    }

    /**
     * `Sprite` subclass adding functionality needed to model a bullet.
     */
    private inner class Bullet : Sprite() {
        /**
         * `Paint` we use to draw our bullet.
         */
        private val mPaint: Paint = Paint()

        /**
         * Called to advance our bullet's animation by `float tau` seconds. If our super's
         * implementation of `step` returns false (our `Sprite` object has disappeared
         * from the game) we return false to the caller. Otherwise we return the negation of the
         * value returned by the method `isOutsidePlayfield` (ie. false if it is outside of the
         * playing field, our bullet disappears from the game if it leaves the playing field).
         *
         * @param tau delta time in seconds to step our animation
         * @return true if our `Bullet` object has successfully been moved, false if it has
         * disappeared from the game.
         */
        override fun step(tau: Float): Boolean {
            return if (!super.step(tau)) {
                false
            } else !isOutsidePlayfield
        }

        /**
         * We implement this to get drawn. First we call the method `setPaintARGBBlend` to set
         * the color of `mPaint` to a color that is appropriate for the stage of destruction
         * given by `mDestroyAnimProgress` (a bright yellow shade to start with an alpha of 255,
         * which morphs to white with an alpha of 0 when we are fully destroyed, we stay bright yellow
         * until we hit an obstacle of course). Then we draw a circle at `(mPositionX,mPositionY)`
         * using `mPaint`.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        override fun draw(canvas: Canvas) {
            setPaintARGBBlend(mPaint, mDestroyAnimProgress,
                    255, 255, 255, 0,
                    0, 255, 255, 255)
            canvas.drawCircle(mPositionX, mPositionY, mSize, mPaint)
        }

        /**
         * Just returns 0.125 to use as the scaling factor to divide the delta time `tau` by in
         * order to calculate the value to add to `mDestroyAnimProgress` in `Sprite.step`
         * (if we are currently animating our destruction that is).
         *
         * @return scaling factor to divide the delta time `tau` by in order to calculate the
         * value to add to `mDestroyAnimProgress` in `Sprite.step` (if we are currently
         * animating our destruction that is).
         */
        override val destroyAnimDuration: Float
            get() = 0.125f

        /**
         * Our constructor. First we allocate a new instance for `Paint mPaint`, and set its
         * style to FILL. Then we set our size to `mBulletSize` by calling `setSize`.
         */
        init {
            mPaint.style = Paint.Style.FILL
            setSize(mBulletSize)
        }
    }

    /**
     * `Sprite` subclass adding functionality needed to model an obstacle
     */
    private inner class Obstacle : Sprite() {
        /**
         * `Paint` we use to draw our obstacle.
         */
        private val mPaint: Paint = Paint()

        /**
         * Called to advance our obstacle's animation by `float tau` seconds. If our super's
         * implementation of `step` returns false (our `Sprite` object has disappeared
         * from the game) we return false to the caller. Otherwise we make sure that our obstacle
         * wraps around to the other side of the view if we cross one of the edges by calling our
         * method `wrapAtPlayfieldBoundary`, and return true to the caller.
         *
         * @param tau delta time in seconds to step our animation
         * @return true if our `Obstacle` object has successfully been moved, false if it has
         * disappeared from the game.
         */
        override fun step(tau: Float): Boolean {
            if (!super.step(tau)) {
                return false
            }
            wrapAtPlayfieldBoundary()
            return true
        }

        /**
         * We implement this to get drawn. First we call the method `setPaintARGBBlend` to set
         * the color of `mPaint` to a color that is appropriate for the stage of destruction
         * given by `mDestroyAnimProgress` (a blue shade to start with an alpha of 255, which
         * morphs to RED with an alpha of 0 when we are fully destroyed, we stay blue until we hit
         * another `Sprite` of course). Then we draw a circle at `(mPositionX,mPositionY)`
         * using `mPaint` of a size which starts at `mSize` and decreases to 0.0 when we
         * are animating our destruction.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        override fun draw(canvas: Canvas) {
            setPaintARGBBlend(mPaint, mDestroyAnimProgress,
                    255, 127, 127, 255,
                    0, 255, 0, 0)
            canvas.drawCircle(mPositionX, mPositionY,
                    mSize * (1.0f - mDestroyAnimProgress), mPaint)
        }

        /**
         * Just returns 0.25 to use as the scaling factor to divide the delta time `tau` by in
         * order to calculate the value to add to `mDestroyAnimProgress` in `Sprite.step`
         * (if we are currently animating our destruction that is).
         *
         * @return scaling factor to divide the delta time `tau` by in order to calculate the
         * value to add to `mDestroyAnimProgress` in `Sprite.step` (if we are currently
         * animating our destruction that is).
         */
        override val destroyAnimDuration: Float
            get() = 0.25f

        /**
         * Our constructor. First we allocate a new instance for `Paint mPaint`, set its color
         * to a shade of blue and set its style to FILL.
         */
        init {
            mPaint.setARGB(255, 127, 127, 255)
            mPaint.style = Paint.Style.FILL
        }
    }

    /**
     * Constructor that is called when inflating a view from XML. First we call our super's constructor,
     * and allocate new instances for our fields `Random mRandom`, `List<Bullet> mBullets`,
     * and `List<Obstacle> mObstacles`. We enable our view to receive focus, and to receive
     * focus in touch mode. We initialize `float baseSize` to 5.0 times the logical density of
     * our display, and `float baseSpeed` to be 3.0 times `baseSize`. We initialize our
     * field `mShipSize` to be 3.0 times `baseSize`, `mMaxShipThrust` to be 0.25
     * times `baseSpeed`, and `mMaxShipSpeed` to be 12 times `baseSpeed`. We initialize
     * `mBulletSize` to be `baseSize`, and `mBulletSpeed` to be 12 times `baseSpeed`.
     * We initialize `mMinObstacleSize` to be 2 times `baseSize`, `mMaxObstacleSize`
     * to be 12 times `baseSize`, `mMinObstacleSpeed` to be `baseSpeed`, and
     * `mMaxObstacleSpeed` to be 3 times `baseSpeed`.
     *
     *  context The Context the view is running in, through which it can access the current
     * theme, resources, etc.
     *  attrs   The attributes of the XML tag that is inflating the view.
     */
    init {
        mBullets = ArrayList()
        mObstacles = ArrayList()
        isFocusable = true
        isFocusableInTouchMode = true
        val baseSize = getContext().resources.displayMetrics.density * 5f
        val baseSpeed = baseSize * 3
        mShipSize = baseSize * 3
        mMaxShipThrust = baseSpeed * 0.25f
        mMaxShipSpeed = baseSpeed * 12
        mBulletSize = baseSize
        mBulletSpeed = baseSpeed * 12
        mMinObstacleSize = baseSize * 2
        mMaxObstacleSize = baseSize * 12
        mMinObstacleSpeed = baseSpeed
        mMaxObstacleSpeed = baseSpeed * 3
    }

    companion object {
        /**
         * Length of delay between running of our animation runnable background thread.
         */
        private const val ANIMATION_TIME_STEP = 1000 / 60.toLong()

        /**
         * Maximum number of obstacles to have in existence at any given time.
         */
        private const val MAX_OBSTACLES = 12

        /**
         * Bit in our field `int mDPadState` we use to indicate that the KEYCODE_DPAD_LEFT key is
         * currently pressed.
         */
        private const val DPAD_STATE_LEFT = 1 shl 0

        /**
         * Bit in our field `int mDPadState` we use to indicate that the KEYCODE_DPAD_RIGHT key is
         * currently pressed.
         */
        private const val DPAD_STATE_RIGHT = 1 shl 1

        /**
         * Bit in our field `int mDPadState` we use to indicate that the KEYCODE_DPAD_UP key is
         * currently pressed.
         */
        private const val DPAD_STATE_UP = 1 shl 2

        /**
         * Bit in our field `int mDPadState` we use to indicate that the KEYCODE_DPAD_DOWN key is
         * currently pressed.
         */
        private const val DPAD_STATE_DOWN = 1 shl 3

        /**
         * Convenience function to check whether our parameter `keyCode` is either a gamepad button,
         * KEYCODE_DPAD_CENTER, or KEYCODE_SPACE (in which case we return true).
         *
         * @param keyCode keycode we are to check to see if it is a "fire" key
         * @return true if `keyCode` is a "fire" key, false if not.
         */
        private fun isFireKey(keyCode: Int): Boolean {
            return (KeyEvent.isGamepadButton(keyCode)
                    || (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                    ) || (keyCode == KeyEvent.KEYCODE_SPACE))
        }

        /**
         * Returns the value of the requested joystick axis from the `MotionEvent` passed it. First
         * we initialize our variable `InputDevice.MotionRange range` by calling the `getMotionRange`
         * method of `InputDevice device` for the `axis` we are interested in and the source
         * of `event`. If that is not null, we initialize our variable `float flat` with the
         * extent of the center flat position with respect to this axis. If `historyPos` is less than
         * 0 we initialize our variable `float value` with the value of the requested axis for the
         * for the current movement, otherwise we initialize it to the value of the requested axis for the
         * historical movement `historyPos`. If the absolute value of `value` is greater than
         * than the `flat` range we return `value` to the caller, otherwise we return 0.
         *
         * @param event      joystick `MotionEvent` received by our `onGenericMotionEvent` callback.
         * @param device     `InputDevice` of the device which sent us the event.
         * @param axis       joystick axis we are interested in.
         * @param historyPos number of the historical movement sample in the batch (-1 for the current
         * movement).
         * @return value of the requested axis for the event or historical event we are interested in
         * (or 0 if the value is within the "flat" range of the device).
         */
        private fun getCenteredAxis(event: MotionEvent, device: InputDevice?, axis: Int, historyPos: Int): Float {
            val range = device!!.getMotionRange(axis, event.source)
            if (range != null) {
                val flat = range.flat
                val value = if (historyPos < 0) event.getAxisValue(axis) else event.getHistoricalAxisValue(axis, historyPos)

                // Ignore axis values that are within the 'flat' region of the joystick axis center.
                // A joystick at rest does not always report an absolute position of (0,0).
                if (abs(value) > flat) {
                    return value
                }
            }
            return 0f
        }

        /**
         * Convenience function for calling `Math.hypot`, and returning its result cast to float.
         *
         * @param x length of x component
         * @param y length of y component
         * @return the result of calling `Math.hypot` for x and y, cast to float
         */
        fun pythag(x: Float, y: Float): Float {
            return hypot(x.toDouble(), y.toDouble()).toFloat()
        }

        /**
         * Convenience function to calculate a color value that starts at `from` and is animated
         * to `to` based on the value of `alpha`.
         *
         * @param alpha value of alpha component
         * @param from  starting value for this color component
         * @param to    ending value for this color component
         * @return a value that is positioned by `alpha` between `from` and `to`
         */
        fun blend(alpha: Float, from: Int, to: Int): Int {
            return from + ((to - from) * alpha).toInt()
        }

        /**
         * Convenience function to animate the color of `Paint paint` between a "from" color and a
         * "to" color based on the current value of our parameter `float alpha`.
         *
         * @param paint `Paint` whose color we are to set
         * @param alpha a value between 0 and 1.0 which determines where between from and to we are currently
         * @param a1    from alpha color component
         * @param r1    from red color component
         * @param g1    from green color component
         * @param b1    from blue color component
         * @param a2    to alpha color component
         * @param r2    to red color component
         * @param g2    to green color component
         * @param b2    ti blue color component
         */
        fun setPaintARGBBlend(paint: Paint, alpha: Float,
                              a1: Int, r1: Int, g1: Int, b1: Int,
                              a2: Int, r2: Int, g2: Int, b2: Int) {
            paint.setARGB(blend(alpha, a1, a2), blend(alpha, r1, r2),
                    blend(alpha, g1, g2), blend(alpha, b1, b2))
        }

        /**
         * Angle of our arrowhead "point" of our spaceship.
         */
        private const val CORNER_ANGLE = Math.PI.toFloat() * 2 / 3

        /**
         * Constant used to convert radians to degrees (by multiplying)
         */
        private const val TO_DEGREES = (180.0 / Math.PI).toFloat()
    }

}