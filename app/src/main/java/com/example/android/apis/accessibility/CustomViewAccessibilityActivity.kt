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

package com.example.android.apis.accessibility

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.android.apis.R
import kotlin.math.ceil
import kotlin.math.max

/**
 * Demonstrates how to implement accessibility support of custom views. Custom view
 * is a tailored widget developed by extending the base classes in the android.view
 * package. This sample shows how to implement the accessibility behavior via both
 * inheritance (non backwards compatible) and composition (backwards compatible).
 *
 * While the Android framework has a diverse portfolio of views tailored for various
 * use cases, sometimes a developer needs a specific functionality not implemented
 * by the standard views. A solution is to write a custom view that extends one of the
 * base view classes. While implementing the desired functionality a developer should
 * also implement accessibility support for that new functionality such that
 * disabled users can leverage it.
 */
class CustomViewAccessibilityActivity : Activity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.custom_view_accessibility.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_view_accessibility)
    }

    /**
     * Demonstrates how to enhance the accessibility support via inheritance.
     *
     * **Note:** Using inheritance may break your application's
     * backwards compatibility. In particular, overriding a method that takes as
     * an argument or returns a class not present on an older platform
     * version will prevent your application from running on that platform.
     * For example, `AccessibilityNodeInfo` was introduced in
     * `ICE_CREAM_SANDWICH API 14`, thus overriding
     * `View.onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo)`
     * will prevent you application from running on a platform older than
     * `ICE_CREAM_SANDWICH API 14`.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    class AccessibleCompoundButtonInheritance
    /**
     * Perform inflation from XML. We just call our super's constructor.
     *
     * @param context The Context the view is running in, through which it can
     * access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    (context: Context, attrs: AttributeSet) : BaseToggleButton(context, attrs) {

        /**
         * Initializes an `AccessibilityEvent` with information about this View (which is the
         * event source). First we call our super's implementation of `onInitializeAccessibilityEvent`,
         * then we set the source of the `AccessibilityEvent event` to the checked state based
         * on whether our custom `BaseToggleButton` is checked or not.
         *
         * @param event The event to initialize.
         */
        override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
            super.onInitializeAccessibilityEvent(event)
            // We called the super implementation to let super classes
            // set appropriate event properties. Then we add the new property
            // (checked) which is not supported by a super class.
            event.isChecked = isChecked
        }

        /**
         * Initializes an `AccessibilityNodeInfo` with information about this view. First we
         * call our super's implementation to let super classes set appropriate info properties.
         * Then we call the `setCheckable` method of `info` to set that this node is
         * checkable, and call its `setChecked` method to set its checked state to that of our
         * custom `BaseToggleButton`. We call our `getText` method to get the text of
         * our custom view into `CharSequence text` and if it is not empty we call the
         * `setText` method of `info` to set its text to `text`.
         *
         * @param info The instance to initialize.
         */
        override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(info)
            // We called the super implementation to let super classes set
            // appropriate info properties. Then we add our properties
            // (checkable and checked) which are not supported by a super class.
            info.isCheckable = true
            info.isChecked = isChecked
            // Very often you will need to add only the text on the custom view.
            val text = text
            if (!TextUtils.isEmpty(text)) {
                info.text = text
            }
        }

        /**
         * Called from `dispatchPopulateAccessibilityEvent(AccessibilityEvent)`
         * giving this View a chance to to populate the accessibility event with its
         * text content. First we call our super's implementation to populate its text
         * into the event. Then we call our `getText` method to get the text of
         * our custom view into `CharSequence text` and if it is not empty we call the
         * `getText` method of `event` to get the current text list of the event, and
         * then add `text` to this list.
         *
         * @param event The accessibility event which to populate.
         */
        override fun onPopulateAccessibilityEvent(event: AccessibilityEvent) {
            super.onPopulateAccessibilityEvent(event)
            // We called the super implementation to populate its text to the
            // event. Then we add our text not present in a super class.
            // Very often you will need to add only the text on the custom view.
            val text = text
            if (!TextUtils.isEmpty(text)) {
                event.text.add(text)
            }
        }
    }

    /**
     * Demonstrates how to enhance the accessibility support via composition.
     *
     * **Note:** Using composition ensures that your application is
     * backwards compatible. The android-support-v4 library has API that allow
     * using the accessibility APIs in a backwards compatible manner.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    class AccessibleCompoundButtonComposition
    /**
     * Perform inflation from XML. First we call our super's constructor, then we call our
     * method `tryInstallAccessibilityDelegate` to attempt to install an Accessibility
     * Delegate.
     *
     * @param context The Context the view is running in, through which it can
     * access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    (context: Context, attrs: AttributeSet) : BaseToggleButton(context, attrs) {

        init {
            tryInstallAccessibilityDelegate()
        }

        /**
         * Tries to install an Accessibility Delegate if the API of our device is 14 or greater. If
         * the user-visible SDK version of our framework is less than 14 we return having done
         * nothing. Otherwise we call the method `setAccessibilityDelegate` to set the delegate
         * for implementing accessibility support via composition to an anonymous `AccessibilityDelegate`
         * class which overrides the three methods `onInitializeAccessibilityEvent`,
         * `onInitializeAccessibilityNodeInfo` and `onPopulateAccessibilityEvent` with
         * custom methods which add our custom accessibility features to the framework.
         */
        @SuppressLint("ObsoleteSdkInt")
        fun tryInstallAccessibilityDelegate() {
            // If the API version of the platform we are running is too old
            // and does not support the AccessibilityDelegate APIs, do not
            // call View.setAccessibilityDelegate(AccessibilityDelegate) or
            // refer to AccessibilityDelegate, otherwise an exception will
            // be thrown.
            // NOTE: The android-support-v4 library contains APIs that enable
            // using the accessibility APIs in a backwards compatible fashion.
            if (Build.VERSION.SDK_INT < 14) {
                return
            }
            // AccessibilityDelegate allows clients to override its methods that
            // correspond to the accessibility methods in View and register the
            // delegate in the View essentially injecting the accessibility support.
            accessibilityDelegate = object : View.AccessibilityDelegate() {
                /**
                 * Initializes an `AccessibilityEvent` with information about this View (which is the
                 * event source). First we call our super's implementation of `onInitializeAccessibilityEvent`,
                 * then we set the source of the `AccessibilityEvent event` to the checked state based
                 * on whether our custom `BaseToggleButton` is checked or not.
                 *
                 * @param host The View hosting the delegate.
                 * @param event The event to initialize.
                 */
                override fun onInitializeAccessibilityEvent(host: View, event: AccessibilityEvent) {
                    super.onInitializeAccessibilityEvent(host, event)
                    // We called the super implementation to let super classes
                    // set appropriate event properties. Then we add the new property
                    // (checked) which is not supported by a super class.
                    event.isChecked = isChecked
                }

                /**
                 * Initializes an `AccessibilityNodeInfo` with information about this view. First we
                 * call our super's implementation to let super classes set appropriate info properties.
                 * Then we call the `setCheckable` method of `info` to set that this node is
                 * checkable, and call its `setChecked` method to set its checked state to that of our
                 * custom `BaseToggleButton`. We call our `getText` method to get the text of
                 * our custom view into `CharSequence text` and if it is not empty we call the
                 * `setText` method of `info` to set its text to `text`.
                 *
                 * @param host The View hosting the delegate.
                 * @param info The instance to initialize.
                 */
                override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    // We called the super implementation to let super classes set
                    // appropriate info properties. Then we add our properties
                    // (checkable and checked) which are not supported by a super class.
                    info.isCheckable = true
                    info.isChecked = isChecked
                    // Very often you will need to add only the text on the custom view.
                    val text = text
                    if (!TextUtils.isEmpty(text)) {
                        info.text = text
                    }
                }

                /**
                 * Gives a chance to the host View to populate the accessibility event with its
                 * text content. First we call our super's implementation to populate its text
                 * into the event. Then we call our `getText` method to get the text of
                 * our custom view into `CharSequence text` and if it is not empty we call the
                 * `getText` method of `event` to get the current text list of the event, and
                 * then add `text` to this list.
                 *
                 * @param host The View hosting the delegate.
                 * @param event The accessibility event which to populate.
                 */
                override fun onPopulateAccessibilityEvent(host: View, event: AccessibilityEvent) {
                    super.onPopulateAccessibilityEvent(host, event)
                    // We called the super implementation to populate its text to the
                    // event. Then we add our text not present in a super class.
                    // Very often you will need to add only the text on the custom view.
                    val text = text
                    if (!TextUtils.isEmpty(text)) {
                        event.text.add(text)
                    }
                }
            }
        }
    }

    /**
     * This is a base toggle button class whose accessibility is not tailored
     * to reflect the new functionality it implements.
     *
     * **Note:** This is not a sample implementation of a toggle
     * button, rather a simple class needed to demonstrate how to refine the
     * accessibility support of a custom View.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    open class BaseToggleButton
    /**
     * Perform inflation from XML and apply a class-specific base style from a theme attribute.
     * First we call our super's constructor, then we initialize our field `TextPaint mTextPaint`
     * with a new instance of `TextPaint` whose anti alias flag is set. We initialize our
     * variable `TypedValue typedValue` with a new instance, and fetch the resolved attribute
     * android.R.attr.textSize (size of text) into it by using our parameter `Context context`
     * to fetch the `Theme` object associated with it and using its `resolveAttribute`
     * method to resolve that attribute, saving its return value in `boolean valid`. We declare
     * `int textSize` and initialize `DisplayMetrics displayMetrics` with the current
     * display metrics that are in effect for the resources of `context`.
     *
     * If `valid` is true (it is never so!) we set `textSize` to the data of `typedValue`
     * as a dimension, scaled by the display density and scaling information of `displayMetrics`
     * and cast to `int`. If `valid` is false we set `textSize` to 15 times the
     * logical density of the display, cast to `int`. In either case we set the text size of
     * `mTextPaint` to `textSize`.
     *
     * We fetch the resolved attribute android.R.attr.textColorPrimary (most prominent text color)
     * into `typedValue`, then initialize `int textColor` by retrieving the color from
     * the resource id of it. We then set the color of `mTextPaint` to `textColor`.
     *
     * We set `mTextOn` to the string with resource id R.string.accessibility_custom_on ("On"),
     * and `mTextOff` to the string with resource id R.string.accessibility_custom_off ("Off").
     *
     * @param context  The Context the view is running in, through which it can
     * access the current theme, resources, etc.
     * @param attrs    The attributes of the XML tag that is inflating the view.
     * @param defStyle An attribute in the current theme that contains a
     * reference to a style resource that supplies default values for
     * the view. Can be 0 to not look for defaults.
     */
    @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyle: Int = android.R.attr.buttonStyle) : View(context, attrs, defStyle) {
        /**
         * Flag to indicate whether our toggle button is checked (true) or not checked (false).
         * Returns whether our toggle button is checked (true) or not (false). We just return the
         * value of our field `boolean mChecked` to the caller.
         *
         * @return value of our field `boolean mChecked`
         */
        var isChecked: Boolean = false
            private set

        /**
         * Text to read when the button is on, comes from R.string.accessibility_custom_on ("On")
         */
        private val mTextOn: CharSequence
        /**
         * Text to read when the button is off, comes from R.string.accessibility_custom_off ("Off")
         */
        private val mTextOff: CharSequence

        /**
         * `StaticLayout` which should show the text `mTextOn` (Invisible because of color choice)
         */
        private var mOnLayout: Layout? = null
        /**
         * `StaticLayout` which should show the text `mTextOff` (Invisible because of color choice)
         */
        private var mOffLayout: Layout? = null

        /**
         * `TextPaint` used to draw the text in our toggle buttons.
         */
        private val mTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

        /**
         * Called to get the text string corresponding to the current checked or unchecked state. If
         * our field `mChecked` is true we return `mTextOn` ("On"), if false we return
         * `mTextOff` ("Off").
         *
         * @return string corresponding to the current checked or unchecked state.
         */
        val text: CharSequence
            get() = if (isChecked) mTextOn else mTextOff

        init {

            val typedValue = TypedValue()
            val valid = context.theme.resolveAttribute(android.R.attr.textSize, typedValue, true)

            val textSize: Int
            val displayMetrics = context.resources.displayMetrics
            textSize = if (valid) {
                typedValue.getDimension(displayMetrics).toInt()
            } else {
                (15 * displayMetrics.density).toInt()
            }
            mTextPaint.textSize = textSize.toFloat()

            context.theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
            @Suppress("DEPRECATION")
            val textColor = context.resources.getColor(typedValue.resourceId)
            mTextPaint.color = textColor

            mTextOn = context.getString(R.string.accessibility_custom_on)
            mTextOff = context.getString(R.string.accessibility_custom_off)
        }

        /**
         * Call this view's `OnClickListener`, if it is defined. First we call our super's
         * implementation of `performClick` saving the return value in `boolean handled`. If
         * `handled` is false we toggle the value of `mChecked` and invalidate our view. In
         * either case we return `handled` to the caller.
         *
         * @return True there was an assigned OnClickListener that was called, false
         * otherwise is returned.
         */
        override fun performClick(): Boolean {
            val handled = super.performClick()
            if (!handled) {
                isChecked = isChecked xor true
                invalidate()
            }
            return handled
        }

        /**
         * Measure the view and its content to determine the measured width and the measured height.
         * If `Layout mOnLayout` is null we set it to the layout created by our method
         * `makeLayout` from the string `mTextOn`. If `Layout mOffLayout` is null
         * we set it to the layout created by our method `makeLayout` from the string
         * `mTextOff`. We initialize `int minWidth` to the maximum of the widths of
         * `mOnLayout` and `mOffLayout` plus the left padding and right padding. We
         * initialize `int minHeight` to the maximum of the heights of `mOnLayout` and
         * `mOffLayout` plus the top padding and bottom padding. Finally we call the method
         * `setMeasuredDimension` with the values returned by the method `resolveSizeAndState`
         * reconciling our desired sizes `minWidth` and `minHeight` with the constraints
         * imposed our parameters `widthMeasureSpec` and `heightMeasureSpec` respectively.
         *
         * @param widthMeasureSpec  horizontal space requirements as imposed by the parent.
         * @param heightMeasureSpec vertical space requirements as imposed by the parent.
         */
        public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            if (mOnLayout == null) {
                mOnLayout = makeLayout(mTextOn)
            }
            if (mOffLayout == null) {
                mOffLayout = makeLayout(mTextOff)
            }
            val minWidth = (max(mOnLayout!!.width, mOffLayout!!.width)
                    + paddingLeft + paddingRight)
            val minHeight = (max(mOnLayout!!.height, mOffLayout!!.height)
                    + paddingTop + paddingBottom)
            setMeasuredDimension(resolveSizeAndState(minWidth, widthMeasureSpec, 0),
                    resolveSizeAndState(minHeight, heightMeasureSpec, 0))
        }

        /**
         * Creates and returns a `StaticLayout` which uses `TextPaint mTextPaint` to
         * draw our parameter `CharSequence text`. We just return a new instance of
         * `StaticLayout` constructed using its 7 parameter constructor, with our parameter
         * `text` as the source text, `mTextPaint` as the `TextPaint`, the int value
         * of the ceiling of the desired width of `text` when drawn using `mTextPaint`,
         * ALIGN_NORMAL as the layout alignment, and three more values I am too lazy to search for
         * the meaning of.
         *
         * @param text string to write in our static layout.
         * @return a `StaticLayout` which displays our parameter `CharSequence text`
         */
        private fun makeLayout(text: CharSequence): Layout {
            @Suppress("DEPRECATION")
            return StaticLayout(text, mTextPaint,
                    ceil(Layout.getDesiredWidth(text, mTextPaint).toDouble()).toInt(),
                    Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true)
        }

        /**
         * We implement this to do our drawing. First we call our super's implementation of `onDraw`,
         * then we save the current matrix and clip of `canvas` onto a private stack. We translate
         * `canvas` to the (x,y) coordinates of the left padding and top padding. We set the variable
         * `Layout switchText` to `mOnLayout` if `mChecked` is true and to `mOffLayout`
         * if it is false, then instruct `switchText` to draw itself on `canvas`. Finally we
         * remove all modifications to the matrix/clip state of `canvas`.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.save()
            canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
            val switchText = if (isChecked) mOnLayout else mOffLayout
            switchText!!.draw(canvas)
            canvas.restore()
        }
    }
}
