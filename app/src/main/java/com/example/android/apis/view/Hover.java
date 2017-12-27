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
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.android.apis.R;


/**
 * Demonstrates how to use {@link View#onHoverEvent}, {@link ViewGroup#onInterceptHoverEvent},
 * and {@link View#setOnHoverListener}.
 * <p>
 * This activity displays a few buttons and text fields and entices the user
 * to hover over them using a mouse or touch pad.  It displays feedback reporting
 * the position of the pointing device and the label of the view being hovered.
 * <p>
 * A button changes from dark green to bright yellow when hovered.
 * This effect is achieved by using a state-list drawable to select among different
 * background shapes and colors based on the hover state of the button.
 * <p>
 * A View#OnHoverEventListener is used to listen for hover events within the
 * container.  The container will re
 * <p>
 * A checkbox is used to control whether a special view, the Interceptor, will intercept
 * events before they are sent to its child (a button).  When the Interceptor
 * is intercepting events, the button will not change state as the pointer hovers
 * over it because the interceptor itself will grab the events.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class Hover extends Activity {
    /**
     * {@code TextView} in our layout with ID R.id.message, it is used to display strings describing
     * the actions of the hover events received.
     */
    private TextView mMessageTextView;
    /**
     * {@code CheckBox} in our layout with ID R.id.intercept_checkbox, it is used to select whether
     * our {@code HoverInterceptorView mInterceptor} intercepts hover events or not.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private CheckBox mInterceptCheckBox;
    /**
     * {@code HoverInterceptorView} in our layout with ID R.id.interceptor, it is a custom
     * {@code LinearLayout} whose sole purpose is to intercept hover events that would otherwise be
     * sent to its children if the {@code CheckBox mInterceptCheckBox} is checked.
     */
    private HoverInterceptorView mInterceptor;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.hover. We initialize
     * our field {@code TextView mMessageTextView} by finding the view with ID R.id.message,
     * {@code CheckBox mInterceptCheckBox} by finding the view with ID R.id.intercept_checkbox, and
     * {@code HoverInterceptorView mInterceptor} by finding the view with ID R.id.interceptor.
     * <p>
     * We initialize our variable {@code View container} by finding the view with ID R.id.container
     * and set its {@code OnHoverListener} to an anonymous class whose {@code onHover} method will
     * set the text of {@code TextView mMessageTextView} to different strings depending on the
     * action type of the {@code MotionEvent} it receives.
     * <p>
     * Finally we set the {@code OnCheckedChangeListener} of {@code CheckBox mInterceptCheckBox} to
     * an anonymous class which calls the {@code setInterceptHover} method of {@code mInterceptor}
     * with the new state of the CheckBox.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hover);

        mMessageTextView = (TextView) findViewById(R.id.message);
        mInterceptCheckBox = (CheckBox) findViewById(R.id.intercept_checkbox);
        mInterceptor = (HoverInterceptorView) findViewById(R.id.interceptor);

        View container = findViewById(R.id.container);
        container.setOnHoverListener(new View.OnHoverListener() {
            /**
             * Called when a hover event is dispatched to a view. This allows listeners to get a
             * chance to respond before the target view. We switch on the kind of action being
             * performed by the {@code MotionEvent event}:
             * <ul>
             *     <li>
             *         ACTION_HOVER_ENTER - we set the text of {@code TextView mMessageTextView} to a
             *     formatted string using the format R.string.hover_message_entered_at with the
             *     X and Y coordinates of {@code Event} filling in the two float arguments, then break.
             *     </li>
             *     <li>
             *         ACTION_HOVER_MOVE - we set the text of {@code TextView mMessageTextView} to a
             *     formatted string using the format R.string.hover_message_moved_at with the
             *     X and Y coordinates of {@code Event} filling in the two float arguments, then break.
             *     </li>
             *     <li>
             *         ACTION_HOVER_EXIT - we set the text of {@code TextView mMessageTextView} to a
             *     formatted string using the format R.string.hover_message_exited_at with the
             *     X and Y coordinates of {@code Event} filling in the two float arguments, then break.
             *     </li>
             * </ul>
             * Finally we return true to the caller.
             *
             * @param v The view the hover event has been dispatched to.
             * @param event The MotionEvent object containing full information about the event.
             * @return True if the listener has consumed the event, false otherwise.
             */
            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        mMessageTextView.setText(Hover.this.getResources().getString(
                                R.string.hover_message_entered_at,
                                event.getX(), event.getY()));
                        break;
                    case MotionEvent.ACTION_HOVER_MOVE:
                        mMessageTextView.setText(Hover.this.getResources().getString(
                                R.string.hover_message_moved_at,
                                event.getX(), event.getY()));
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        mMessageTextView.setText(Hover.this.getResources().getString(
                                R.string.hover_message_exited_at,
                                event.getX(), event.getY()));
                        break;
                }
                return false;
            }
        });

        mInterceptCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * Called when the checked state of a compound button has changed. We simply call the
             * {@code setInterceptHover} method of {@code HoverInterceptorView mInterceptor} with
             * the new checked value {@code isChecked}.
             *
             * @param buttonView The compound button view whose state has changed.
             * @param isChecked  The new checked state of buttonView.
             */
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mInterceptor.setInterceptHover(isChecked);
            }
        });
    }
}
