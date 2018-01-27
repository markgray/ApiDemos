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

package com.example.android.apis.accessibility;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.android.apis.R;

/**
 * Demonstrates how to implement accessibility support of custom views. Custom view
 * is a tailored widget developed by extending the base classes in the android.view
 * package. This sample shows how to implement the accessibility behavior via both
 * inheritance (non backwards compatible) and composition (backwards compatible).
 * <p>
 * While the Android framework has a diverse portfolio of views tailored for various
 * use cases, sometimes a developer needs a specific functionality not implemented
 * by the standard views. A solution is to write a custom view that extends one of the
 * base view classes. While implementing the desired functionality a developer should
 * also implement accessibility support for that new functionality such that
 * disabled users can leverage it.
 * </p>
 */
public class CustomViewAccessibilityActivity extends Activity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.custom_view_accessibility.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_view_accessibility);
    }

    /**
     * Demonstrates how to enhance the accessibility support via inheritance.
     * <p>
     * <strong>Note:</strong> Using inheritance may break your application's
     * backwards compatibility. In particular, overriding a method that takes as
     * an argument or returns a class not present on an older platform
     * version will prevent your application from running on that platform.
     * For example, {@code AccessibilityNodeInfo} was introduced in
     * {@code ICE_CREAM_SANDWICH API 14}, thus overriding
     * {@code View.onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo)}
     * will prevent you application from running on a platform older than
     * {@code ICE_CREAM_SANDWICH API 14}.
     * </p>
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static class AccessibleCompoundButtonInheritance extends BaseToggleButton {

        /**
         * Perform inflation from XML. We just call our super's constructor.
         *
         * @param context The Context the view is running in, through which it can
         *        access the current theme, resources, etc.
         * @param attrs The attributes of the XML tag that is inflating the view.
         */
        public AccessibleCompoundButtonInheritance(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        /**
         * Initializes an {@code AccessibilityEvent} with information about this View (which is the
         * event source). First we call our super's implementation of {@code onInitializeAccessibilityEvent},
         * then we set the source of the {@code AccessibilityEvent event} to the checked state based
         * on whether our custom {@code BaseToggleButton} is checked or not.
         *
         * @param event The event to initialize.
         */
        @Override
        public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(event);
            // We called the super implementation to let super classes
            // set appropriate event properties. Then we add the new property
            // (checked) which is not supported by a super class.
            event.setChecked(isChecked());
        }

        /**
         * Initializes an {@code AccessibilityNodeInfo} with information about this view. First we
         * call our super's implementation to let super classes set appropriate info properties.
         * Then we call the {@code setCheckable} method of {@code info} to set that this node is
         * checkable, and call its {@code setChecked} method to set its checked state to that of our
         * custom {@code BaseToggleButton}. We call our {@code getText} method to get the text of
         * our custom view into {@code CharSequence text} and if it is not empty we call the
         * {@code setText} method of {@code info} to set its text to {@code text}.
         *
         * @param info The instance to initialize.
         */
        @Override
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            // We called the super implementation to let super classes set
            // appropriate info properties. Then we add our properties
            // (checkable and checked) which are not supported by a super class.
            info.setCheckable(true);
            info.setChecked(isChecked());
            // Very often you will need to add only the text on the custom view.
            CharSequence text = getText();
            if (!TextUtils.isEmpty(text)) {
                info.setText(text);
            }
        }

        /**
         * Called from {@code dispatchPopulateAccessibilityEvent(AccessibilityEvent)}
         * giving this View a chance to to populate the accessibility event with its
         * text content. First we call our super's implementation to populate its text
         * into the event. Then we call our {@code getText} method to get the text of
         * our custom view into {@code CharSequence text} and if it is not empty we call the
         * {@code getText} method of {@code event} to get the current text list of the event, and
         * then add {@code text} to this list.
         *
         * @param event The accessibility event which to populate.
         */
        @Override
        public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
            super.onPopulateAccessibilityEvent(event);
            // We called the super implementation to populate its text to the
            // event. Then we add our text not present in a super class.
            // Very often you will need to add only the text on the custom view.
            CharSequence text = getText();
            if (!TextUtils.isEmpty(text)) {
                event.getText().add(text);
            }
        }
    }

    /**
     * Demonstrates how to enhance the accessibility support via composition.
     * <p>
     * <strong>Note:</strong> Using composition ensures that your application is
     * backwards compatible. The android-support-v4 library has API that allow
     * using the accessibility APIs in a backwards compatible manner.
     * </p>
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static class AccessibleCompoundButtonComposition extends BaseToggleButton {

        /**
         * Perform inflation from XML. First we call our super's constructor, then we call our
         * method {@code tryInstallAccessibilityDelegate} to attempt to install an Accessibility
         * Delegate.
         *
         * @param context The Context the view is running in, through which it can
         *        access the current theme, resources, etc.
         * @param attrs The attributes of the XML tag that is inflating the view.
         */
        public AccessibleCompoundButtonComposition(Context context, AttributeSet attrs) {
            super(context, attrs);
            tryInstallAccessibilityDelegate();
        }

        /**
         * Tries to install an Accessibility Delegate if the API of our device is 14 or greater. If
         * the user-visible SDK version of our framework is less than 14 we return having done
         * nothing. Otherwise we call the method {@code setAccessibilityDelegate} to set the delegate
         * for implementing accessibility support via composition to an anonymous {@code AccessibilityDelegate}
         * class which overrides the three methods {@code onInitializeAccessibilityEvent},
         * {@code onInitializeAccessibilityNodeInfo} and {@code onPopulateAccessibilityEvent} with
         * custom methods which add our custom accessibility features to the framework.
         */
        public void tryInstallAccessibilityDelegate() {
            // If the API version of the platform we are running is too old
            // and does not support the AccessibilityDelegate APIs, do not
            // call View.setAccessibilityDelegate(AccessibilityDelegate) or
            // refer to AccessibilityDelegate, otherwise an exception will
            // be thrown.
            // NOTE: The android-support-v4 library contains APIs that enable
            // using the accessibility APIs in a backwards compatible fashion.
            if (Build.VERSION.SDK_INT < 14) {
                return;
            }
            // AccessibilityDelegate allows clients to override its methods that
            // correspond to the accessibility methods in View and register the
            // delegate in the View essentially injecting the accessibility support.
            setAccessibilityDelegate(new AccessibilityDelegate() {
                /**
                 * Initializes an {@code AccessibilityEvent} with information about this View (which is the
                 * event source). First we call our super's implementation of {@code onInitializeAccessibilityEvent},
                 * then we set the source of the {@code AccessibilityEvent event} to the checked state based
                 * on whether our custom {@code BaseToggleButton} is checked or not.
                 *
                 * @param host The View hosting the delegate.
                 * @param event The event to initialize.
                 */
                @Override
                public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
                    super.onInitializeAccessibilityEvent(host, event);
                    // We called the super implementation to let super classes
                    // set appropriate event properties. Then we add the new property
                    // (checked) which is not supported by a super class.
                    event.setChecked(isChecked());
                }

                /**
                 * Initializes an {@code AccessibilityNodeInfo} with information about this view. First we
                 * call our super's implementation to let super classes set appropriate info properties.
                 * Then we call the {@code setCheckable} method of {@code info} to set that this node is
                 * checkable, and call its {@code setChecked} method to set its checked state to that of our
                 * custom {@code BaseToggleButton}. We call our {@code getText} method to get the text of
                 * our custom view into {@code CharSequence text} and if it is not empty we call the
                 * {@code setText} method of {@code info} to set its text to {@code text}.
                 *
                 * @param host The View hosting the delegate.
                 * @param info The instance to initialize.
                 */
                @Override
                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    // We called the super implementation to let super classes set
                    // appropriate info properties. Then we add our properties
                    // (checkable and checked) which are not supported by a super class.
                    info.setCheckable(true);
                    info.setChecked(isChecked());
                    // Very often you will need to add only the text on the custom view.
                    CharSequence text = getText();
                    if (!TextUtils.isEmpty(text)) {
                        info.setText(text);
                    }
                }

                /**
                 * Gives a chance to the host View to populate the accessibility event with its
                 * text content. First we call our super's implementation to populate its text
                 * into the event. Then we call our {@code getText} method to get the text of
                 * our custom view into {@code CharSequence text} and if it is not empty we call the
                 * {@code getText} method of {@code event} to get the current text list of the event, and
                 * then add {@code text} to this list.
                 *
                 * @param host The View hosting the delegate.
                 * @param event The accessibility event which to populate.
                 */
                @Override
                public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
                    super.onPopulateAccessibilityEvent(host, event);
                    // We called the super implementation to populate its text to the
                    // event. Then we add our text not present in a super class.
                    // Very often you will need to add only the text on the custom view.
                    CharSequence text = getText();
                    if (!TextUtils.isEmpty(text)) {
                        event.getText().add(text);
                    }
                }
            });
        }
    }

    /**
     * This is a base toggle button class whose accessibility is not tailored
     * to reflect the new functionality it implements.
     * <p>
     * <strong>Note:</strong> This is not a sample implementation of a toggle
     * button, rather a simple class needed to demonstrate how to refine the
     * accessibility support of a custom View.
     * </p>
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static class BaseToggleButton extends View {
        /**
         * Flag to indicate whether our toggle button is checked (true) or not checked (false)
         */
        private boolean mChecked;

        /**
         * Text to read when the button is on, comes from R.string.accessibility_custom_on ("On")
         */
        private CharSequence mTextOn;
        /**
         * Text to read when the button is off, comes from R.string.accessibility_custom_off ("Off")
         */
        private CharSequence mTextOff;

        /**
         * {@code StaticLayout} which should show the text {@code mTextOn} (Invisible because of color choice)
         */
        private Layout mOnLayout;
        /**
         * {@code StaticLayout} which should show the text {@code mTextOff} (Invisible because of color choice)
         */
        private Layout mOffLayout;

        /**
         * {@code TextPaint} used to draw the text in our toggle buttons.
         */
        private TextPaint mTextPaint;

        /**
         * Perform inflation from XML. We call our three argument constructor using the default
         * style android.R.attr.buttonStyle.
         *
         * @param context The Context the view is running in, through which it can
         *        access the current theme, resources, etc.
         * @param attrs The attributes of the XML tag that is inflating the view.
         */
        public BaseToggleButton(Context context, AttributeSet attrs) {
            this(context, attrs, android.R.attr.buttonStyle);
        }

        /**
         * Perform inflation from XML and apply a class-specific base style from a theme attribute.
         * First we call our super's constructor, then we initialize our field {@code TextPaint mTextPaint}
         * with a new instance of {@code TextPaint} whose anti alias flag is set.
         *
         * @param context The Context the view is running in, through which it can
         *        access the current theme, resources, etc.
         * @param attrs The attributes of the XML tag that is inflating the view.
         * @param defStyle An attribute in the current theme that contains a
         *        reference to a style resource that supplies default values for
         *        the view. Can be 0 to not look for defaults.
         */
        public BaseToggleButton(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);

            mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.textSize, typedValue, true);
            final int textSize = (int) typedValue.getDimension(
                    context.getResources().getDisplayMetrics());
            mTextPaint.setTextSize(textSize);

            context.getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
            final int textColor = context.getResources().getColor(typedValue.resourceId);
            mTextPaint.setColor(textColor);

            mTextOn = context.getString(R.string.accessibility_custom_on);
            mTextOff = context.getString(R.string.accessibility_custom_off);
        }

        public boolean isChecked() {
            return mChecked;
        }

        public CharSequence getText() {
            return mChecked ? mTextOn : mTextOff;
        }

        @Override
        public boolean performClick() {
            final boolean handled = super.performClick();
            if (!handled) {
                mChecked ^= true;
                invalidate();
            }
            return handled;
        }

        @Override
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (mOnLayout == null) {
                mOnLayout = makeLayout(mTextOn);
            }
            if (mOffLayout == null) {
                mOffLayout = makeLayout(mTextOff);
            }
            final int minWidth = Math.max(mOnLayout.getWidth(), mOffLayout.getWidth())
                    + getPaddingLeft() + getPaddingRight();
            final int minHeight = Math.max(mOnLayout.getHeight(), mOffLayout.getHeight())
                    + getPaddingLeft() + getPaddingRight();
            setMeasuredDimension(resolveSizeAndState(minWidth, widthMeasureSpec, 0),
                    resolveSizeAndState(minHeight, heightMeasureSpec, 0));
        }

        private Layout makeLayout(CharSequence text) {
            return new StaticLayout(text, mTextPaint,
                    (int) Math.ceil(Layout.getDesiredWidth(text, mTextPaint)),
                    Layout.Alignment.ALIGN_NORMAL, 1.f, 0, true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.save();
            canvas.translate(getPaddingLeft(), getPaddingRight());
            Layout switchText = mChecked ? mOnLayout : mOffLayout;
            switchText.draw(canvas);
            canvas.restore();
        }
    }
}
