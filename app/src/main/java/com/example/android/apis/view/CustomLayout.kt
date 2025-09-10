/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews.RemoteView
import androidx.core.content.withStyledAttributes
import com.example.android.apis.R

/**
 * Example of writing a custom layout manager. This is a fairly full-featured
 * layout manager that is relatively general, handling all layout cases. You
 * can simplify it for more specific cases. Used by CustomLayoutActivity.kt in
 * its layout file layout/custom_layout.xml
 * RequiresApi(Build.VERSION_CODES.HONEYCOMB)
 */
@RemoteView
class CustomLayout : ViewGroup {
    /**
     * The amount of space used by children in the left gutter.
     */
    private var mLeftWidth = 0

    /**
     * The amount of space used by children in the right gutter.
     */
    private var mRightWidth = 0

    /**
     * These are used for computing child frames based on their gravity.
     * The frame of the containing space, in which the object will be placed.
     */
    private val mTmpContainerRect = Rect()

    /**
     * Receives the computed frame of the object in its container.
     */
    private val mTmpChildRect = Rect()

    /**
     * Our constructor, simply calls our super's constructor.
     *
     * @param context [Context] to use to access resources
     */
    constructor(context: Context?) : super(context)

    /**
     * Constructor that is called when inflating from xml.
     *
     * @param context  The Context the view is running in, through which it can access the current
     * theme, resources, etc.
     * @param attrs    The attributes of the XML tag that is inflating the view.
     * @param defStyle An attribute in the current theme that contains a reference to a style
     * resource that supplies default values for the view. Can be 0 to not look for defaults.
     */
    @JvmOverloads
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int = 0
    ) : super(context, attrs, defStyle)

    /**
     * Any layout manager that doesn't scroll will want to implement this. We return false to the
     * caller because we do not scroll.
     */
    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    /**
     * Ask all children to measure themselves and compute the measurement of this layout based on
     * the children. This method is invoked by `measure(int, int)` and should be overridden
     * by subclasses to provide accurate and efficient measurement of their contents.
     *
     * First we initialize our [Int] variable `val count` with the number of children we have. Then
     * we initialize our fields [mLeftWidth] and [mRightWidth] to 0. Next we initialize our variables
     * `var maxHeight`, `var maxWidth`, and `var childState` to 0.
     *
     * Now we loop over all `count` of our children using `i` as the index. We initialize
     * our [View] variable `val child` with the child at position `i`, and if the visibility of
     * `child` is not GONE we ask `child` to measure itself given the [widthMeasureSpec] and
     * [heightMeasureSpec] passed us. Next we initialize [LayoutParams] variable `val lp` with the
     * layout parameters of `child`. If the `position` field of `lp` is POSITION_LEFT
     * the child needs to go in the left gutter so we add the maximum of `maxWidth` and the
     * measured width of `child` plus the `leftMargin` plus the `rightMargin` fields of `lp` to
     * [mLeftWidth]. If the `position` field of `lp` is POSITION_RIGHT the child needs to go in
     * the right gutter so we add the maximum of `maxWidth` and the measured width of `child` plus
     * the `leftMargin` plus the `rightMargin` fields of `lp` to [mRightWidth]. Otherwise we add
     * the maximum of `maxWidth` and the measured width of `child` plus the `leftMargin` plus the
     * `rightMargin` fields of `lp` to `maxWidth`. We set `maxHeight` to the maximum of `maxHeight`
     * and the sum of the measured height of `child` plus the `topMargin` plus the `bottomMargin`
     * fields of `lp`. We set `childState` to the result of combining the previous value of
     * `childState` with the state bits of `child` then loop around for the next of our children.
     *
     * Having processed all of our children we add [mLeftWidth] and [mRightWidth] to `maxWidth`
     * (total width is the maximum width of all inner children plus the width of the children in
     * the gutters). We then make sure the `maxHeight` and `maxWidth` are bigger than our suggested
     * minimum height and width.
     *
     * Finally we call the method [setMeasuredDimension] to store the measured width and
     * measured height, where the width is given by the return value of [View.resolveSizeAndState]
     * with `maxWidth` as how big our view wants to be, [widthMeasureSpec] as the restraints
     * imposed by our parent, and `childState` as the size information bit mask for our children,
     * and where the height is given by the return value of [View.resolveSizeAndState] with
     * `maxHeight` as how big our view wants to be, [heightMeasureSpec] as the restraints
     * imposed by our parent, and `childState` left shifted by MEASURED_HEIGHT_STATE_SHIFT (16)
     * as the size information bit mask for our children.
     *
     * @param widthMeasureSpec  horizontal space requirements as imposed by the parent.
     * @param heightMeasureSpec vertical space requirements as imposed by the parent.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val count = childCount

        // These keep track of the space we are using on the left and right for
        // views positioned there; we need member variables so we can also use
        // these for layout later.
        mLeftWidth = 0
        mRightWidth = 0

        // Measurement will ultimately be computing these values.
        var maxHeight = 0
        var maxWidth = 0
        var childState = 0

        // Iterate through all children, measuring them and computing our dimensions
        // from their size.
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                // Measure the child.
                measureChildWithMargins(
                    /* child = */ child,
                    /* parentWidthMeasureSpec = */ widthMeasureSpec,
                    /* widthUsed = */ 0,
                    /* parentHeightMeasureSpec = */ heightMeasureSpec,
                    /* heightUsed = */ 0
                )

                // Update our size information based on the layout params.  Children
                // that asked to be positioned on the left or right go in those gutters.
                val lp = child.layoutParams as LayoutParams
                when (lp.position) {
                    LayoutParams.POSITION_LEFT -> {
                        mLeftWidth += maxWidth.coerceAtLeast(
                            child.measuredWidth + lp.leftMargin + lp.rightMargin
                        )
                    }

                    LayoutParams.POSITION_RIGHT -> {
                        mRightWidth += maxWidth.coerceAtLeast(
                            child.measuredWidth + lp.leftMargin + lp.rightMargin
                        )
                    }

                    else -> {
                        maxWidth = maxWidth.coerceAtLeast(
                            child.measuredWidth + lp.leftMargin + lp.rightMargin
                        )
                    }
                }
                maxHeight = maxHeight.coerceAtLeast(
                    child.measuredHeight + lp.topMargin + lp.bottomMargin
                )
                childState = combineMeasuredStates(childState, child.measuredState)
            }
        }

        // Total width is the maximum width of all inner children plus the gutters.
        maxWidth += mLeftWidth + mRightWidth

        // Check against our minimum height and width
        maxHeight = maxHeight.coerceAtLeast(suggestedMinimumHeight)
        maxWidth = maxWidth.coerceAtLeast(suggestedMinimumWidth)

        // Report our final dimensions.
        setMeasuredDimension(
            /* measuredWidth = */ resolveSizeAndState(
                /* size = */ maxWidth,
                /* measureSpec = */ widthMeasureSpec,
                /* childMeasuredState = */ childState
            ),
            /* measuredHeight = */ resolveSizeAndState(
                /* size = */ maxHeight,
                /* measureSpec = */ heightMeasureSpec,
                /* childMeasuredState = */ childState shl MEASURED_HEIGHT_STATE_SHIFT
            )
        )
    }

    /**
     * Called from layout when this view should assign a size and position to each of its children.
     * First we initialize our [Int] variable `val count` with the number of children we have. We
     * initialize `var leftPos` with the left padding of our view, and `var rightPos` with the
     * result of subtracting our parameter [left] and our right padding from our parameter [right].
     * We initialize `val middleLeft` with the result of adding `leftPos` to [mLeftWidth] (the left
     * side of the middle region between the gutters) and `val middleRight` with the result of
     * subtracting [mRightWidth] from `rightPos` (the right side of the middle region between the
     * gutters). We initialize `val parentTop` with our top padding, and `val parentBottom` with the
     * result of subtracting [top] and our bottom padding from [bottom].
     *
     * Now we loop over all `count` of our children using `i` as the index. We initialize
     * our [View] variable `val child` with the child at position `i`, and if the visibility of
     * `child` is not GONE we initialize [LayoutParams] variable `val lp` with the layout parameters
     * of `child`, initialize `val width` with the measured width of `child` and `val height` with
     * its measured height.
     *
     * We then switch on the value of the `position` field of `lp`:
     *
     *  Left gutter: If the `position` field of `lp` is POSITION_LEFT we set the `left` field of
     *  [mTmpContainerRect] to the sum of `leftPos` and the `leftMargin` field of `lp` and the
     *  `right` field of `mTmpContainerRect` to `leftPos` plus `width` plus the `rightMargin`
     *  field of `lp`. We then set `leftPos` to the `right` field of [mTmpContainerRect].
     *
     *  Right gutter: If the `position` field of `lp` is POSITION_RIGHT we set the `right` field
     *  of [mTmpContainerRect] to `rightPos` minus the `rightMargin` field of `lp` and the `left`
     *  field of `mTmpContainerRect` to `rightPos` minus `width` minus the `leftMargin` field of
     *  `lp`. We then set `rightPos` to the `left` field of [mTmpContainerRect].
     *
     *  Middle: Otherwise the child is in the middle so we set the `left` field of [mTmpContainerRect]
     *  to `middleLeft` plus the `leftMargin` field of `lp`, and the `right` field of
     *  [mTmpContainerRect] to `middleRight` minus the `rightMargin` field of `lp`.
     *
     * In all cases we set the `top` field of [mTmpContainerRect] to `parentTop` plus the `topMargin`
     * field of `lp` and the `bottom` field of [mTmpContainerRect] to `parentBottom` minus the
     * `bottomMargin` field of `lp`.
     *
     * Having determined where we want `child` positioned we now call [Gravity.apply] which takes
     * the `gravity` field of `lp` and applies it along with `width` and `height` of the child and
     * uses [mTmpContainerRect] as the preliminary frame of the containing space, in which the
     * object will be placed to create the computed frame of the object in its container
     * [mTmpChildRect].
     *
     * Finally we instruct `child` to layout itself with its left X coordinate the `left`
     * field of [mTmpChildRect], its top Y coordinate the `top` field of [mTmpChildRect],
     * its right X coordinate the `right` field of [mTmpChildRect], and its bottom Y
     * coordinate the `bottom` field of [mTmpChildRect]. Then we loop around for the next
     * child view.
     *
     * @param changed This is a new size or position for this view if true
     * @param left    Left position, relative to parent
     * @param top     Top position, relative to parent
     * @param right   Right position, relative to parent
     * @param bottom  Bottom position, relative to parent
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val count = childCount

        // These are the far left and right edges in which we are performing layout.
        var leftPos = paddingLeft
        var rightPos = right - left - paddingRight

        // This is the middle region inside of the gutter.
        val middleLeft = leftPos + mLeftWidth
        val middleRight = rightPos - mRightWidth

        // These are the top and bottom edges in which we are performing layout.
        val parentTop = paddingTop
        val parentBottom = bottom - top - paddingBottom
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val lp = child.layoutParams as LayoutParams
                val width = child.measuredWidth
                val height = child.measuredHeight

                // Compute the frame in which we are placing this child.
                when (lp.position) {
                    LayoutParams.POSITION_LEFT -> {
                        mTmpContainerRect.left = leftPos + lp.leftMargin
                        mTmpContainerRect.right = leftPos + width + lp.rightMargin
                        leftPos = mTmpContainerRect.right
                    }

                    LayoutParams.POSITION_RIGHT -> {
                        mTmpContainerRect.right = rightPos - lp.rightMargin
                        mTmpContainerRect.left = rightPos - width - lp.leftMargin
                        rightPos = mTmpContainerRect.left
                    }

                    else -> {
                        mTmpContainerRect.left = middleLeft + lp.leftMargin
                        mTmpContainerRect.right = middleRight - lp.rightMargin
                    }
                }
                mTmpContainerRect.top = parentTop + lp.topMargin
                mTmpContainerRect.bottom = parentBottom - lp.bottomMargin

                // Use the child's gravity and size to determine its final
                // frame within its container.
                Gravity.apply(
                    /* gravity = */ lp.gravity,
                    /* w = */ width,
                    /* h = */ height,
                    /* container = */ mTmpContainerRect,
                    /* outRect = */ mTmpChildRect
                )

                // Place the child.
                child.layout(
                    mTmpChildRect.left, mTmpChildRect.top,
                    mTmpChildRect.right, mTmpChildRect.bottom
                )
            }
        }
    }

    // ----------------------------------------------------------------------
    // The rest of the implementation is for custom per-child layout parameters.
    // If you do not need these (for example you are writing a layout manager
    // that does fixed positioning of its children), you can drop all of this.

    /**
     * Returns a new set of layout parameters based on the supplied attributes set. We return a new
     * instance of our `MarginLayoutParams` descendant of [LayoutParams] created using the
     * context we are running in, and our [AttributeSet] parameter [attrs].
     *
     * @param attrs the attributes to build the layout parameters from
     * @return an instance of [LayoutParams] or one of its descendants
     */
    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    /**
     * Returns a set of default layout parameters. We just return a new instance of [LayoutParams]
     * with its width and height both set to MATCH_PARENT.
     *
     * @return a set of default layout parameters or null
     */
    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    /**
     * Returns a safe set of layout parameters based on the supplied layout params. We just return
     * an instance of our [CustomLayout.LayoutParams], which in turn just calls its super's
     * constructor with its parameter [p].
     *
     * @param p The layout parameters to convert into a suitable set of layout parameters
     * for this ViewGroup.
     * @return an instance of [ViewGroup.LayoutParams] or one of its descendants
     */
    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return LayoutParams(p)
    }

    /**
     * Returns true if [ViewGroup.LayoutParams] is an instance of our [CustomLayout.LayoutParams],
     * false otherwise.
     *
     * @param p instance of [ViewGroup.LayoutParams] we are to check
     * @return true if `p` is an instance of our [CustomLayout.LayoutParams].
     */
    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    /**
     * Custom per-child layout information.
     */
    class LayoutParams : MarginLayoutParams {
        /**
         * The gravity to apply with the View to which these layout parameters are associated.
         */
        var gravity: Int = Gravity.TOP or Gravity.START

        /**
         * Position of the child whose to which these layout parameters are associated.
         */
        var position: Int = POSITION_MIDDLE

        /**
         * Creates a new set of layout parameters. The values are extracted from the supplied
         * attributes set and context. This constructor is called when a layout is inflated from an
         * xml file. First we call our super's constructor, then we initialize `TypedArray` variable
         * `val a` with styled attribute information in this [Context]'s theme for the attribute
         * declare-styleable with resource ID R.styleable.CustomLayoutLP (which defines the attr
         * layout_gravity and layout_position). We then initialize our field `gravity` with the
         * value stored in `a` under the key R.styleable.CustomLayoutLP_android_layout_gravity
         * (defaulting to the current value of `gravity` if none is given), and initialize `position`
         * with the value stored in `a` under the key R.styleable.CustomLayoutLP_layout_position
         * (defaulting to the current value of `position` if none is given). Finally we recycle `a`
         *
         * @param c     the application environment
         * @param attrs the set of attributes from which to extract the layout parameters' values
         */
        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {

            // Pull the layout param values from the layout XML during
            // inflation.  This is not needed if you don't care about
            // changing the layout behavior in XML.
            c.withStyledAttributes(set = attrs, attrs = R.styleable.CustomLayoutLP) {
                gravity = getInt(R.styleable.CustomLayoutLP_android_layout_gravity, gravity)
                position = getInt(R.styleable.CustomLayoutLP_layout_position, position)
            }
        }

        /**
         * Creates a new set of layout parameters with the specified width and height. We just call
         * our super's constructor.
         *
         * @param width  width of the view
         * @param height height of the view
         */
        constructor(width: Int, height: Int) : super(width, height)

        /**
         * Copy constructor. Clones the width and height values of the source. We just call our
         * super's constructor.
         *
         * @param source The layout params to copy from.
         */
        constructor(source: ViewGroup.LayoutParams?) : super(source)

        companion object {
            /**
             * Constant for a child located in the middle of its parent
             */
            var POSITION_MIDDLE: Int = 0

            /**
             * Constant for a child located in the left gutter
             */
            var POSITION_LEFT: Int = 1

            /**
             * Constant for a child located in the right gutter
             */
            var POSITION_RIGHT: Int = 2
        }
    }
}