/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.example.android.apis.graphics

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RectShape
import android.graphics.drawable.shapes.RoundRectShape
import android.graphics.drawable.shapes.Shape
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Shows "material design" effects of simple draggable shapes that generate a shadow casting outline
 * on touching the screen.
 */
@SuppressLint("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ShadowCardDrag : AppCompatActivity() {
    /**
     * [ShapeDrawable] presently being used for the draggable, it is cycled by the R.id.shape_select
     * [Button] to be one of [RectShape], [OvalShape], [RoundRectShape] and [TriangleShape] which
     * are contained in our `ArrayList<Shape>` field [mShapes].
     */
    private val mCardBackground = ShapeDrawable()

    /**
     * List containing the different [Shape] types which our draggable card can be set to, it
     * is filled in our [onCreate] method with a [RectShape], [OvalShape], [RoundRectShape] and
     * [TriangleShape], and cycled through when the R.id.shape_select "Select Shape" [Button]
     * is pressed.
     */
    private val mShapes = ArrayList<Shape>()

    /**
     * The logical density of the display. This is a scaling factor for the Density Independent Pixel
     * unit, where one DIP is one pixel on an approximately 160 dpi screen (for example a 240x320,
     * 1.5"x2" screen), providing the baseline of the system's display. Thus on a 160dpi screen this
     * density value will be 1; on a 120 dpi screen it would be .75; etc. It is retrieved from the
     * current display metrics of the packages resources in our [onCreate] method, and used
     * whenever it is necessary to scale DPI measurements to pixels.
     */
    private var mDensity = 0f

    /**
     * Our draggable card, ID R.id.card in our layout file R.layout.shadow_card_drag.
     */
    private var mCard: View? = null

    /**
     * Class which handles tilting and/or shading when those checkboxes are checked.
     */
    private val mDragState = CardDragState()

    /**
     * true if the "Enable Tilt" checkbox is checked. The card will be "tilted" in proportion to the
     * velocity of movement.
     */
    private var mTiltEnabled = false

    /**
     * true if the "Enable Shading checkbox is checked. If so, a color filter will be applied to the
     * card background darkening it in proportion to the velocity of movement.
     */
    private var mShadingEnabled = false

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.shadow_card_drag.
     * We initialize our field [mDensity] with the logical density of the display. We call our
     * method [initShapes] to fill our `ArrayList<Shape>` field [mShapes] with four different
     * [ShapeDrawable] objects: a [RectShape], an [OvalShape], a [RoundRectShape], and a
     * [TriangleShape]. We fetch the [Paint] used to draw our [ShapeDrawable] field [mCardBackground]
     * and set its color to WHITE, then set its background to the first [Shape] in [mShapes] (a
     * [RectShape]). We locate the [TextView] in our layout with id R.id.card and set its background
     * to [mCardBackground]. We next initialize our "Enable Tilt" checkbox, our "Enable Shading"
     * checkbox, and our "Select Shape" button. Finally we initialize an `OnTouchListener` for our
     * entire `FrameLayout` (id R.id.card_parent) to allow us to move and animate the card according
     * to the touch events received.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shadow_card_drag)
        mDensity = resources.displayMetrics.density
        initShapes()
        mCardBackground.paint.color = Color.WHITE
        mCardBackground.shape = mShapes[0]
        mCard = findViewById(R.id.card)
        mCard!!.background = mCardBackground
        initTiltEnable()
        initShadingEnable()
        initShapeButton()
        initTouchListener()
    }

    /**
     * Initializes the [View.OnTouchListener] of the [FrameLayout] holding our entire UI to allow
     * the user to move the card with his finger. First we set [View] `val cardParent` to the view
     * in our layout with id `R.id.card_parent` (the [FrameLayout] holding our entire UI). Then we
     * set its `OnTouchListener` to an anonymous class which will translate the [MotionEvent]
     * received into movement of the card.
     */
    private fun initTouchListener() {

        /**
         * Enable any touch on the parent to drag the card. Note that this doesn't do a proper hit
         * test, so any drag (including off of the card) will work.
         *
         * This enables the user to see the effect more clearly for the purpose of this demo.
         */
        val cardParent = findViewById<View>(R.id.card_parent)
        cardParent.setOnTouchListener(object : OnTouchListener {
            /**
             * Distance in the X direction of the last ACTION_DOWN [MotionEvent] received with
             * respect to the current X coordinate of the card. Used to translate the X coordinate
             * of future ACTION_MOVE events to a new location for the card.
             */
            var downX = 0f

            /**
             * Distance in the Y direction of the last ACTION_DOWN `MotionEvent` received with
             * respect to the current Y coordinate of the card. Used to translate the Y coordinate
             * of future ACTION_MOVE events to a new location for the card.
             */
            var downY = 0f

            /**
             * The time (in ms) when the user originally pressed down to start a stream of position
             * events. Set when an ACTION_DOWN event is received, but never used.
             */
            var downTime: Long = 0

            /**
             * Called when a touch event is dispatched to our view. We switch based on the kind of
             * action of the [MotionEvent] parameter [event]:
             *
             *  * ACTION_DOWN - First we calculate how far the event coordinates are from the
             *  current location of the card, setting [downX] and [downY] so that they may be
             *  used to calculate where to move the card when future ACTION_MOVE  events are
             *  received. Then we create an [ObjectAnimator] `val upAnim` to animate the property
             *  named "translationZ" of [View] field [mCard], set its duration to 100 milliseconds,
             *  its interpolator to a new instance of [DecelerateInterpolator], and then start it
             *  running. If the flag [mTiltEnabled] is true, we call the `onDown` method of
             *  [CardDragState] field [mDragState] to initialize it for a "tilting" animation
             *  of the card.
             *
             *  * ACTION_MOVE - We move the card in the X direction by the change in position of
             *  the current event with respect to the position of the last ACTION_DOWN event,
             *  and do the same for the Y direction. Then if our flag [mTiltEnabled] is
             *  true we call the `onMove` method of [CardDragState] field [mDragState] with
             *  the time of our event, and its x and y coordinates. The `onMove` method
             *  will tilt the card in proportion to the current momentum.
             *
             *  * ACTION_UP - We create [ObjectAnimator] `val downAnim` to animate the "translationZ"
             *  attribute of [View] field [mCard] to 0, set its duration to 100 milliseconds, set
             *  its interpolator to a new instance of [AccelerateInterpolator], and start the
             *  animation running. Then if our flag [mTiltEnabled] is true we call the `onUp`
             *  method of [CardDragState] field [mDragState] to animate the flattening of the
             *  tilting which was in progress.
             *
             * In all cases we return true to the caller to signal that we have consumed the event.
             *
             * @param v The view the touch event has been dispatched to.
             * @param event The [MotionEvent] object containing full information about the event.
             * @return True if the listener has consumed the event, false otherwise. We always return
             * true.
             */
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.x - mCard!!.translationX
                        downY = event.y - mCard!!.translationY
                        downTime = event.downTime
                        val upAnim =
                            ObjectAnimator.ofFloat(mCard!!, "translationZ", MAX_Z_DP * mDensity)
                        upAnim.duration = 100
                        upAnim.interpolator = DecelerateInterpolator()
                        upAnim.start()
                        if (mTiltEnabled) {
                            mDragState.onDown(event.downTime, event.x, event.y)
                        }
                    }

                    MotionEvent.ACTION_MOVE -> {
                        mCard!!.translationX = event.x - downX
                        mCard!!.translationY = event.y - downY
                        if (mTiltEnabled) {
                            mDragState.onMove(event.eventTime, event.x, event.y)
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        val downAnim = ObjectAnimator.ofFloat(mCard!!, "translationZ", 0f)
                        downAnim.duration = 100
                        downAnim.interpolator = AccelerateInterpolator()
                        downAnim.start()
                        if (mTiltEnabled) {
                            mDragState.onUp()
                        }
                    }
                }
                return true
            }
        })
    }

    /**
     * Sets the [View.OnClickListener] of the "Select Shape" button (id R.id.shape_select) to an
     * anonymous class which steps through the list of [Shape] objects in `ArrayList<Shape>` field
     * [mShapes] setting the [ShapeDrawable] field [mCardBackground] to a different one every time
     * it is clicked.
     */
    private fun initShapeButton() {
        val shapeButton = findViewById<Button>(R.id.shape_select)
        shapeButton.setOnClickListener(object : View.OnClickListener {
            /**
             * The index of the [Shape] in `ArrayList<Shape>` field [mShapes] which is currently
             * being used as the [ShapeDrawable] of [ShapeDrawable] field [mCardBackground].
             */
            var index = 0

            /**
             * Called when our button is clicked. First we increment `index` modulo the number
             * of [Shape] objects in the `ArrayList<Shape>` field [mShapes], then we fetch
             * the [Shape] at index `index` and use it to set the [ShapeDrawable] of [ShapeDrawable]
             * field [mCardBackground].
             *
             * @param v `View` that was clicked
             */
            override fun onClick(v: View) {
                index = (index + 1) % mShapes.size
                mCardBackground.shape = mShapes[index]
            }
        })
    }

    /**
     * Sets the [] of the "Enable Shading" checkbox (with id
     * R.id.shading_check) to a lambda which saves the value of its [Boolean] parameter `isChecked`
     * in the field [mShadingEnabled] and if it is not enabled now, sets the color filter of
     * [ShapeDrawable] field [mCardBackground] to null.
     */
    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private fun initShadingEnable() {
        val shadingCheck = findViewById<CheckBox>(R.id.shading_check)
        shadingCheck.setOnCheckedChangeListener { buttonView, isChecked ->
            /**
             * Called when the checked state of a compound button has changed. First we save the
             * value of [Boolean] parameter `isChecked` in the field [mShadingEnabled] and if it
             * is not enabled now, we set the color filter of [ShapeDrawable] field [mCardBackground]
             * to null.
             *
             * @param buttonView The compound button view whose state has changed.
             * @param isChecked  The new checked state of buttonView.
             */
            mShadingEnabled = isChecked
            if (!mShadingEnabled) {
                mCardBackground.colorFilter = null
            }
        }
    }

    /**
     * Sets the [CompoundButton.OnCheckedChangeListener] of the "Enable Tilt" checkbox (with id
     * R.id.tilt_check) to a lambda which saves the value of its [Boolean] parameter `isChecked`
     * in the field [mTiltEnabled] and if it is not enabled now, calls the `onUp` method of
     * [CardDragState] field [mDragState] to animate the flattening of the card if it was tilted
     * at the moment.
     */
    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private fun initTiltEnable() {
        val tiltCheck = findViewById<CheckBox>(R.id.tilt_check)
        tiltCheck.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            /**
             * Called when the checked state of a compound button has changed. First we save the
             * value of [Boolean] parameter `isChecked` in the field [mTiltEnabled] and if it is
             * not enabled now, we call the `onUp` method of [CardDragState] field [mDragState]
             * to animate the flattening of the card if it was tilted at the moment.
             *
             * @param buttonView The compound button view whose state has changed.
             * @param isChecked  The new checked state of buttonView.
             */
            mTiltEnabled = isChecked
            if (!mTiltEnabled) {
                mDragState.onUp()
            }
        }
    }

    /**
     * Fills the list of [Shape] objects in `ArrayList<Shape>` field [mShapes] with the following
     * new instances: a [RectShape], an [OvalShape], a [RoundRectShape] and a [TriangleShape].
     */
    private fun initShapes() {
        mShapes.add(RectShape())
        mShapes.add(OvalShape())
        val r = 10 * mDensity
        val radii = floatArrayOf(r, r, r, r, r, r, r, r)
        mShapes.add(RoundRectShape(radii, null, null))
        mShapes.add(TriangleShape())
    }

    /**
     * Class used to animate the "tilt" and "shading" of the card iff those features are selected.
     */
    private inner class CardDragState {
        /**
         * Time of the last event we were informed of when our [onDown] or [onMove] methods were
         * called. Used to calculate the speed that the card is being moved at in order to scale
         * the "tilt" and/or "shading" of the card proportionately.
         */
        var lastEventTime: Long = 0

        /**
         * X location of the last event we were informed of when our [onDown] or [onMove] methods
         * were called. Used to calculate the speed that the card is being moved at in order to
         * scale the "tilt" and/or "shading" of the card proportionately.
         */
        var lastX = 0f

        /**
         * Y location of the last event we were informed of when our [onDown] or [onMove] methods
         * were called. Used to calculate the speed that the card is being moved at in order to
         * scale the "tilt" and/or "shading" of the card proportionately.
         */
        var lastY = 0f

        /**
         * Calculated "momentum" in the X direction of our card, used to scale the tilt about the
         * Y axis, and the shading of the card
         */
        var momentumX = 0f

        /**
         * Calculated "momentum" in the Y direction of our card, used to scale the tilt about the
         * X axis, and the shading of the card
         */
        var momentumY = 0f

        /**
         * Called when the [OnTouchListener] of our [FrameLayout] (id R.id.card_parent) receives an
         * ACTION_DOWN event and only if tilt is enabled. We save our event time [eventTime] parameter
         * in our field [lastEventTime], and the [x] and [y] coordinates of the event in our fields
         * [lastX] and [lastY] respectively. We then set both [momentumX] and [momentumY] fields
         * to 0.
         *
         * @param eventTime time that the ACTION_DOWN event occurred at
         * @param x         x coordinate of the event
         * @param y         y coordinate of the event
         */
        fun onDown(eventTime: Long, x: Float, y: Float) {
            lastEventTime = eventTime
            lastX = x
            lastY = y
            momentumX = 0f
            momentumY = 0f
        }

        /**
         * Called when the [OnTouchListener] of our [FrameLayout] (id R.id.card_parent) receives an
         * ACTION_MOVE event and only if tilt is enabled. First we calculate the change in time
         * `val deltaT` of the event time since our [lastEventTime] field was set. If this is not 0,
         * we calculate and scale values for [momentumX] and [momentumY] based on the movement since
         * the last event, and rotate the card about the X axis proportionately to the value of
         * `-momentumY` and rotate the card about the Y axis proportionately to the value of
         * `-momentumX`. Then if shading is enabled, we calculate a [Float] value `val alphaDarkening`
         * proportional to the momentum, scale it into a byte, and use that byte to create an rgb
         * color which we set as the color filter using PorterDuff MULTIPLY mode for [ShapeDrawable]
         * field [mCardBackground]. Finally we save the event time in our field [lastEventTime], and
         * the [x] and [y] coordinates of the event in our fields [lastX] and [lastY] respectively.
         *
         * @param eventTime time that the ACTION_MOVE event occurred at
         * @param x         x coordinate of the event
         * @param y         y coordinate of the event
         */
        fun onMove(eventTime: Long, x: Float, y: Float) {
            val deltaT = eventTime - lastEventTime
            if (deltaT != 0L) {
                val newMomentumX = (x - lastX) / (mDensity * deltaT)
                val newMomentumY = (y - lastY) / (mDensity * deltaT)
                momentumX = 0.9f * momentumX + 0.1f * (newMomentumX * MOMENTUM_SCALE)
                momentumY = 0.9f * momentumY + 0.1f * (newMomentumY * MOMENTUM_SCALE)
                momentumX = momentumX.coerceAtMost(
                    maximumValue = MAX_ANGLE.toFloat()
                ).coerceAtLeast(
                    minimumValue = -MAX_ANGLE.toFloat()
                )
                momentumY = momentumY.coerceAtMost(
                    maximumValue = MAX_ANGLE.toFloat()
                ).coerceAtLeast(
                    minimumValue = -MAX_ANGLE.toFloat()
                )
                mCard!!.rotationX = -momentumY
                mCard!!.rotationY = momentumX
                if (mShadingEnabled) {
                    var alphaDarkening = (momentumX * momentumX + momentumY * momentumY) / (90 * 90)
                    alphaDarkening /= 2f
                    val alphaByte = 0xff - ((alphaDarkening * 255).toInt() and 0xff)
                    val color = Color.rgb(alphaByte, alphaByte, alphaByte)
                    val porterDuffColorFilter = PorterDuffColorFilter(
                        color, PorterDuff.Mode.MULTIPLY
                    )
                    mCardBackground.colorFilter = porterDuffColorFilter
                }
            }
            lastX = x
            lastY = y
            lastEventTime = eventTime
        }

        /**
         * Called when the [OnTouchListener] of our [FrameLayout] (id R.id.card_parent) receives an
         * ACTION_UP event when tilt is enabled, and it is also called from the
         * [CompoundButton.OnCheckedChangeListener] of the "Enable Tilt" checkbox when tilt is
         * disabled. First we create [ObjectAnimator] `val flattenX` to animate the "rotationX"
         * attribute of our [View] field [mCard] to 0 rotation, set its duration to 100  milliseconds,
         * its interpolator to an [AccelerateInterpolator], then start it running. Next we create
         * [ObjectAnimator] `val flattenY` to animate the "rotationY" attribute of our [View] field
         * [mCard] to 0 rotation, set its duration to 100  milliseconds, its interpolator to an
         * [AccelerateInterpolator], then start it running. Finally we set the color filter of
         * [ShapeDrawable] field [mCardBackground] to null.
         */
        fun onUp() {
            val flattenX = ObjectAnimator.ofFloat(
                /* target = */ mCard!!,
                /* propertyName = */ "rotationX",
                /* ...values = */ 0f
            )
            flattenX.duration = 100
            flattenX.interpolator = AccelerateInterpolator()
            flattenX.start()
            val flattenY = ObjectAnimator.ofFloat(
                /* target = */ mCard!!,
                /* propertyName = */ "rotationY",
                /* ...values = */ 0f
            )
            flattenY.duration = 100
            flattenY.interpolator = AccelerateInterpolator()
            flattenY.start()
            mCardBackground.colorFilter = null
        }
    }

    /**
     * Simple shape example that generates a shadow casting outline.
     */
    private class TriangleShape : Shape() {
        /**
         * [Path] which draws our triangular shape.
         */
        private val mPath = Path()

        /**
         * Callback method called when [resize] is executed. First we clear any lines and curves
         * from the [Path] field [mPath], making it empty. We move the path to (0,0), draw a line
         * to (`width`,0), draw a line to `(width/2,height)`, then draw a line back to (0,0)
         * (a triangle). Finally we close the current contour.
         *
         * @param width the new width of the Shape
         * @param height the new height of the Shape
         */
        override fun onResize(width: Float, height: Float) {
            mPath.reset()
            mPath.moveTo(0f, 0f)
            mPath.lineTo(width, 0f)
            mPath.lineTo(width / 2, height)
            mPath.lineTo(0f, 0f)
            mPath.close()
        }

        /**
         * Draw this shape into the provided [Canvas], with the provided [Paint]. We simply call the
         * `drawPath` method of [Canvas] [canvas] to draw [Path] field [mPath] using [Paint] parameter
         * [paint].
         *
         * @param canvas the Canvas within which this shape should be drawn
         * @param paint  the Paint object that defines this shape's characteristics
         */
        override fun draw(canvas: Canvas, paint: Paint) {
            canvas.drawPath(mPath, paint)
        }

        /**
         * Compute the Outline of the shape and return it in the supplied Outline parameter. We simply
         * call the `setConvexPath` method of [Outline] parametr [outline] to construct an outline
         * of our [Path] field [mPath].
         *
         * @param outline The Outline to be populated with the result. Should not be null.
         */
        override fun getOutline(outline: Outline) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                outline.setPath(mPath)
            } else {
                @Suppress("DEPRECATION")
                outline.setConvexPath(mPath)
            }
        }
    }

    companion object {
        /**
         * Maximum Z value for animation of the android:translationZ attribute of the draggable card.
         */
        private const val MAX_Z_DP = 10f

        /**
         * Scale used to scale the "momentum" in X and Y direction when determining how much to tilt the
         * card (only when the "Enable Tilt" checkbox is checked)
         */
        private const val MOMENTUM_SCALE = 10f

        /**
         * Maximum angle for the tilt of the card (used only when the "Enable Tilt" checkbox is checked)
         */
        private const val MAX_ANGLE = 10
    }
}