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
package com.example.android.apis.graphics

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.EmbossMaskFilter
import android.graphics.MaskFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.createBitmap
import com.example.android.apis.graphics.ColorPickerDialog.OnColorChangedListener
import kotlin.math.abs

/**
 * Shows off some Canvas drawing methods and `View.onTouchEvent` usage.
 */
class FingerPaint : GraphicsActivity(), OnColorChangedListener {
    /**
     * Current [Paint] to use for drawing loci of finger movements.
     */
    private var mPaint: Paint? = null

    /**
     * An [EmbossMaskFilter] which can be added to [mPaint] using the options menu,
     * applies a shadow like effect to the line being drawn.
     */
    private var mEmboss: MaskFilter? = null

    /**
     * A [BlurMaskFilter] which can be added to [mPaint] using the options menu,
     * applies a blur effect to the line being drawn.
     */
    private var mBlur: MaskFilter? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to an instance of [MyView]. Next we allocate a
     * [Paint] for our filed [mPaint], set its antialias flag, set its dither flag, set its color
     * to RED, set the style to STROKE, set the stroke join to ROUND, set its stroke cap to ROUND,
     * and set its stroke width to 12. We initialize our [MaskFilter] filed [mEmboss] with an
     * instance of [EmbossMaskFilter] configured to use a light source direction of (1,1,1), ambient
     * light value of 0.4f, coefficient for specular highlights of 6.0, and blur before lighting of
     * 3.5f.
     *
     * Finally we initialize our [MaskFilter] field [mBlur] with a [BlurMaskFilter] configured
     * to use a blur radius of 8.0f, and a blur type of NORMAL.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(MyView(this))
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.isDither = true
        mPaint!!.color = -0x10000
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeJoin = Paint.Join.ROUND
        mPaint!!.strokeCap = Paint.Cap.ROUND
        mPaint!!.strokeWidth = 12f
        @Suppress("DEPRECATION") // "This subclass is not supported and should not be instantiated"
        mEmboss = EmbossMaskFilter(
            floatArrayOf(1f, 1f, 1f),
            0.4f, 6F, 3.5f
        )
        mBlur = BlurMaskFilter(8F, BlurMaskFilter.Blur.NORMAL)
    }

    /**
     * This method will be called when the user has chosen a new color using [ColorPickerDialog].
     * We simply set the color of [Paint] field [mPaint] to the `color` the user chose using the
     * dialog.
     *
     * @param color new color chosen by user using [ColorPickerDialog]
     */
    override fun colorChanged(color: Int) {
        mPaint!!.color = color
    }

    /**
     * Custom View which displays the loci drawn by the user's finger.
     *
     * @param c [Context] to use for resources.
     */
    inner class MyView(c: Context?) : View(c) {
        /**
         * [Bitmap] which is used to save all lines drawn. It is updated with the latest
         * [Path] field [mPath] in method `touch_up` every time we receive the event
         * MotionEvent.ACTION_UP by drawing to [Canvas] field [mCanvas] (which is a [Canvas]
         * created from [mBitmap]) and used in our [onDraw] override to draw the old lines
         * before drawing the current [mPath].
         */
        private var mBitmap: Bitmap? = null

        /**
         * [Canvas] for the [Bitmap] field [mBitmap] created in [onSizeChanged] that allows
         * us to draw into [mBitmap]
         */
        private var mCanvas: Canvas? = null

        /**
         * [Path] traced by user's finger, collected from `MotionEvent`'s received in our
         * `MyView.onTouchEvent` method.
         */
        private val mPath: Path = Path()

        /**
         * [Paint] used to draw [Bitmap] field [mBitmap] (the accumulated finger loci tracing)
         * used in our [onDraw] override.
         */
        private val mBitmapPaint: Paint = Paint(Paint.DITHER_FLAG)

        /**
         * This is called during layout when the size of this view has changed. If
         * you were just added to the view hierarchy, you're called with the old
         * values of 0.
         *
         * First we call through to our super's implementation of `onSizeChanged`, then we set
         * out field [Bitmap] field [mBitmap] to a [w] by [h] [Bitmap] with a config of ARGB_8888.
         * Finally we set our [Canvas] field [mCanvas] to a canvas that can be used to draw into
         * the bitmap [mBitmap].
         *
         * @param w    Current width of this view.
         * @param h    Current height of this view.
         * @param oldw Old width of this view.
         * @param oldh Old height of this view.
         */
        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            mBitmap = createBitmap(w, h)
            mCanvas = Canvas(mBitmap!!)
        }

        /**
         * We implement this to do our drawing. First we fill the entire [Canvas] parameter [canvas]
         * with the color 0xFFAAAAAA (a light gray), then we draw [Bitmap] field [mBitmap] (our
         * accumulated finger tracing lines) using [Paint] field [mBitmapPaint], and finally we draw
         * the current finger loci being built in [Path] field [mPath] using [Paint] field [mPaint].
         *
         * @param canvas the canvas on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(-0x555556)
            canvas.drawBitmap(mBitmap!!, 0f, 0f, mBitmapPaint)
            canvas.drawPath(mPath, mPaint!!)
        }

        /**
         * Current location of the finger
         */
        private var mX = 0f
        private var mY = 0f

        /**
         * Called when our `onTouchEvent` override receives a ACTION_DOWN motion event. First
         * we clear all lines and curves from our current finger loci [Path] field [mPath] making
         * it empty. Then we set the beginning of the next contour of [mPath] to (x,y), and save
         * the position in our fields [mX] and [mY].
         *
         * @param x x coordinate of the `MotionEvent`
         * @param y y coordinate of the `MotionEvent`
         */
        private fun touchStart(x: Float, y: Float) {
            mPath.reset()
            mPath.moveTo(x, y)
            mX = x
            mY = y
        }

        /**
         * Called when our `onTouchEvent` override receives a ACTION_MOVE motion event. First
         * we calculate how far the motion event moved `dx` from `mX` in the x direction
         * and `dy` from `mY` in the y direction and if either of these is greater than
         * or equal to TOUCH_TOLERANCE we add a quadratic bezier from the last point `mPath`
         * was moved to, approaching control point (mX,mY), and ending at the point given by
         * `[(x+mX)/2, (y+mY)/2]`. We then save (x,y) in our fields [mX] and [mY] respectively.
         *
         * @param x x coordinate of the `MotionEvent`
         * @param y y coordinate of the `MotionEvent`
         */
        private fun touchMove(x: Float, y: Float) {
            val dx = abs(x - mX)
            val dy = abs(y - mY)
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
                mX = x
                mY = y
            }
        }

        /**
         * Called when our `onTouchEvent` override receives an ACTION_UP motion event. First we
         * add a line to [Path] field [mPath] from the last point to the point (mX,mY), then we
         * commit [mPath] to our offscreen [Bitmap] field [mBitmap] by writing to it using [Canvas]
         * field [mCanvas], then we clear all lines and curves from our current finger loci in
         * [Path] field [mPath] making it empty.
         */
        private fun touchUp() {
            mPath.lineTo(mX, mY)
            // commit the path to our offscreen
            mCanvas!!.drawPath(mPath, mPaint!!)
            // kill this so we don't double draw
            mPath.reset()
        }

        /**
         * We implement this method to handle touch screen motion events. First we fetch the x
         * coordinate of the [MotionEvent] parameter [event] to the [Float] variable `val x` and
         * the y coordinate to `val y`. Then we switch based on the action that is being reported
         * in `event`:
         *
         *  * ACTION_DOWN - we call our method [touchStart] with the coordinate (x,y) in
         *  order to begin recording a new loci of finger tracings, then call `invalidate`
         *  to request that our view be redrawn.
         *
         *  * ACTION_MOVE - we call our method [touchMove] with the coordinate (x,y) in
         *  order to draw a bezier curve from the last location to this new location, then
         *  call `invalidate` to request that our view be redrawn.
         *
         *  * ACTION_UP - we call our method [touchUp] which finishes our [Path] field [mPath]
         *  by drawing a line to our last point at (mX,mY), commits [mPath] to our  offscreen
         *  accumulated finger tracings contained in [Bitmap] field [mBitmap] and  empties [mPath],
         *  then we call `invalidate` to request that our view be redrawn.
         *
         * Finally we return *true* to the caller to indicate that we have consumed the [MotionEvent].
         *
         * @param event The motion event.
         * @return *true* if the event was handled, *false* otherwise.
         */
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchStart(x, y)
                    invalidate()
                }

                MotionEvent.ACTION_MOVE -> {
                    touchMove(x, y)
                    invalidate()
                }

                MotionEvent.ACTION_UP -> {
                    touchUp()
                    invalidate()
                }
            }
            return true
        }

    }

    /**
     * Initialize the contents of the Activity's standard options menu. First we call through to our
     * super's implementation of `onCreateOptionsMenu`. Then we add our menu options to
     * our [Menu] parameter [menu]:
     *
     *  * COLOR_MENU_ID - "Color" allows the user to select a color
     *
     *  * EMBOSS_MENU_ID - "Emboss" allows you to toggle the usage of the `EmbossMaskFilter`
     *
     *  * BLUR_MENU_ID - "Blur" allows you to toggle the usage of the `BlurMaskFilter`
     *
     *  * ERASE_MENU_ID - "Erase" sets the Porter-Duff transfer mode to CLEAR
     *
     *  * SRCATOP_MENU_ID - "SrcATop" sets the Porter-Duff transfer mode to SRC_ATOP
     *
     * Finally we return *true* so the menu will be displayed.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return *true* for the menu to be displayed;
     * if you return *false* it will not be shown
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menu.add(0, COLOR_MENU_ID, 0, "Color")
            .setShortcut('3', 'c')
        menu.add(0, EMBOSS_MENU_ID, 0, "Emboss")
            .setShortcut('4', 's')
        menu.add(0, BLUR_MENU_ID, 0, "Blur")
            .setShortcut('5', 'z')
        menu.add(0, ERASE_MENU_ID, 0, "Erase")
            .setShortcut('5', 'z')
        menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop")
            .setShortcut('5', 'z')
        /*   Is this the mechanism to extend with filter effects?
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(
                              Menu.ALTERNATIVE, 0,
                              new ComponentName(this, NotesList.class),
                              null, intent, 0, null);
        */
        return true
    }

    /**
     * Prepare the Screen's standard options menu to be displayed.  This is
     * called right before the menu is shown, every time it is shown.  You can
     * use this method to efficiently enable/disable items or otherwise
     * dynamically modify the contents.
     *
     * We simply call through to our super's implementation of `onPrepareOptionsMenu` and return
     * *true* so the menu will be displayed
     *
     * @param menu The options menu as last shown or first initialized by
     * onCreateOptionsMenu().
     * @return You must return *true* for the menu to be displayed;
     * if you return *false* it will not be shown.
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        return true
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate). You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     *
     * First we clear the previous Porter-Duff transfer mode of `Paint mPaint`, and set the
     * alpha to its max. Then we switch on the item ID of our [MenuItem] parameter [item]:
     *
     *  * COLOR_MENU_ID - "Color" allows the user to select a color. We create and `show` an
     *  instance of [ColorPickerDialog] which will allow the user to select a new color using
     *  a "color wheel" and upon selection our method [colorChanged] will be called with that
     *  selection which it will use to set the color of [Paint] field [mPaint].
     *
     *  * EMBOSS_MENU_ID - "Emboss" allows you to toggle the usage of the [EmbossMaskFilter]. If
     *  the current [MaskFilter] for [Paint] field [mPaint] is not [MaskFilter] field [mEmboss] we
     *  set the [MaskFilter] to [mEmboss], and if it is we set it to *null*.
     *
     *  * BLUR_MENU_ID - "Blur" allows you to toggle the usage of the [BlurMaskFilter]. If
     *  the current [MaskFilter] for [Paint] field [mPaint] is not [MaskFilter] field [mBlur] we
     *  set the [MaskFilter] to [mBlur], and if it is we set it to *null*.
     *
     *  * ERASE_MENU_ID - "Erase" sets the Porter-Duff transfer mode to CLEAR
     *
     *  * SRCATOP_MENU_ID - "SrcATop" sets the Porter-Duff transfer mode to SRC_ATOP, and sets the
     *  alpha component to the max value.
     *
     * Finally we return the value returned by our super's implementation of `onOptionsItemSelected`
     * to the caller
     *
     * @param item The menu item that was selected.
     * @return boolean Return *false* to allow normal menu processing to
     * proceed, *true* to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mPaint!!.xfermode = null
        mPaint!!.alpha = 0xFF
        when (item.itemId) {
            COLOR_MENU_ID -> {
                ColorPickerDialog(this, this, mPaint!!.color).show()
                return true
            }

            EMBOSS_MENU_ID -> {
                if (mPaint!!.maskFilter !== mEmboss) {
                    mPaint!!.maskFilter = mEmboss
                } else {
                    mPaint!!.maskFilter = null
                }
                return true
            }

            BLUR_MENU_ID -> {
                if (mPaint!!.maskFilter !== mBlur) {
                    mPaint!!.maskFilter = mBlur
                } else {
                    mPaint!!.maskFilter = null
                }
                return true
            }

            ERASE_MENU_ID -> {
                mPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                return true
            }

            SRCATOP_MENU_ID -> {
                mPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
                mPaint!!.alpha = 0x80
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        /**
         * Menu ID for our "Color" option
         */
        private const val COLOR_MENU_ID = Menu.FIRST

        /**
         * Menu ID for our "Emboss" option
         */
        private const val EMBOSS_MENU_ID = Menu.FIRST + 1

        /**
         * Menu ID for our "Blur" option
         */
        private const val BLUR_MENU_ID = Menu.FIRST + 2

        /**
         * Menu ID for our "Erase" option
         */
        private const val ERASE_MENU_ID = Menu.FIRST + 3

        /**
         * Menu ID for our "SrcATop" option
         */
        private const val SRCATOP_MENU_ID = Menu.FIRST + 4

        /**
         * Finger movements below this value are ignored.
         */
        private const val TOUCH_TOLERANCE = 4f
    }
}