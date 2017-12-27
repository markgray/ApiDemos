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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Part of the {@link Hover} demo activity.
 * <p>
 * The Interceptor view is a simple subclass of LinearLayout whose sole purpose
 * is to override {@link #onInterceptHoverEvent}.  When the checkbox in the
 * hover activity is checked, the interceptor view will intercept hover events.
 * <p>
 * When this view intercepts hover events, its children will not receive
 * hover events.  This can be useful in some cases when implementing a custom
 * view group that would like to prevent its children from being hovered
 * under certain situations.  Usually such custom views will be much more
 * interesting and complex than our little Interceptor example here.
 */
public class HoverInterceptorView extends LinearLayout {
    /**
     * Flag to indicate whether we should intercept hover events or not (true we intercept them,
     * false we let them through to our children).
     */
    private boolean mInterceptHover;

    /**
     * Constructor that is called when we are being inflated from XML. We just call our super's
     * constructor.
     *
     * @param context The Context the view is running in, through which it can access the current
     *                theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public HoverInterceptorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * We implement this method to intercept hover events before they are handled by child views. If
     * our field {@code boolean mInterceptHover} is true, we return true to our caller, thereby
     * intercepting the hover event. Otherwise we return the value returned by our super's
     * implementation of {@code onInterceptHoverEvent}.
     *
     * @param event The motion event that describes the hover.
     * @return True if the view group would like to intercept the hover event
     * and prevent its children from receiving it.
     */
    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        if (mInterceptHover) {
            return true;
        }
        return super.onInterceptHoverEvent(event);
    }

    /**
     * We implement this method to handle hover events. We initialize our variable {@code TextView textView}
     * by finding the view with ID R.id.intercept_message (it is the child view we use to display status
     * messages). If our field {@code mInterceptHover} is true and the {@code MotionEvent event} is not
     * a ACTION_HOVER_EXIT event, we set the text of {@code textView} to the string given by the resource
     * ID R.string.hover_intercept_message_intercepted ("Intercepted hover event instead of sending it to
     * the button.  Om nom nom!") and return true to the caller. Otherwise we set the text of {@code textView}
     * to the string with ID R.string.hover_intercept_message_initial ("Try hovering over the button.")
     * on return the value returned by our super's implementation of {@code onHoverEvent} to the caller.
     *
     * @param event The motion event that describes the hover.
     * @return True if the view handled the hover event.
     */
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        TextView textView = (TextView) findViewById(R.id.intercept_message);
        if (mInterceptHover && event.getAction() != MotionEvent.ACTION_HOVER_EXIT) {
            textView.setText(getResources().getString(R.string.hover_intercept_message_intercepted));
            return true;
        }
        textView.setText(getResources().getString(R.string.hover_intercept_message_initial));
        return super.onHoverEvent(event);
    }

    /**
     * Setter for our field {@code boolean mInterceptHover}.
     *
     * @param intercept Value to set {@code boolean mInterceptHover} to
     */
    public void setInterceptHover(boolean intercept) {
        mInterceptHover = intercept;
    }
}
