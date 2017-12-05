/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Shows how to implement draggable views.
 */
@SuppressLint("SetTextI18n")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DragAndDropDemo extends Activity {
    /**
     * Target {@code TextView} to display some messages in.
     */
    TextView mResultText;
    /**
     * This {@code DraggableDot} is invisible until we receive an ACTION_DRAG_STARTED {@code DragEvent}
     * whereupon we set it to be visible.
     */
    DraggableDot mHiddenDot;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.drag_layout.
     * We initialize {@code TextView text} by finding the view with ID R.id.drag_text, then we locate
     * the views with ID R.id.drag_dot_1, R.id.drag_dot_2, and R.id.drag_dot_3 in turn and call their
     * method {@code setReportView(text)}. We initialize our field {@code DraggableDot mHiddenDot} by
     * finding the view with ID R.id.drag_dot_hidden, and call its method {@code setReportView(text)}
     * ({@code setReportView(text)} saves {@code text} in the instance's field {@code TextView mReportView}
     * so that it can write to it when it wants to). We initialize our field {@code TextView mResultText}
     * by finding the view with ID R.id.drag_result_text, and our variable {@code View MainView} by
     * finding the view with ID R.id.drag_main. Finally we set the {@code OnDragListener} of
     * {@code View MainView} to an anonymous class which writes info about the {@code DragEvent}
     * it receives to {@code TextView mResultText}, as well as monkeying with the visibility of
     * {@code DraggableDot mHiddenDot}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drag_layout);

        TextView text = (TextView) findViewById(R.id.drag_text);
        DraggableDot dot = (DraggableDot) findViewById(R.id.drag_dot_1);
        dot.setReportView(text);
        dot = (DraggableDot) findViewById(R.id.drag_dot_2);
        dot.setReportView(text);
        dot = (DraggableDot) findViewById(R.id.drag_dot_3);
        dot.setReportView(text);

        mHiddenDot = (DraggableDot) findViewById(R.id.drag_dot_hidden);
        mHiddenDot.setReportView(text);

        mResultText = (TextView) findViewById(R.id.drag_result_text);
        View MainView = findViewById(R.id.drag_main);
        MainView.setOnDragListener(new View.OnDragListener() {
            /**
             * Called when a drag event is dispatched to a view. This allows listeners to get a
             * chance to override base View behavior. We retrieve the action from our parameter
             * {@code DragEvent event} to our variable {@code int action}, then switch on it as
             * follows:
             * <ul>
             *     <li>
             *         ACTION_DRAG_STARTED - we set the text of {@code TextView mResultText} to the string
             *         "ACTION_DRAG_STARTED", set the visibility of {@code DraggableDot mHiddenDot} to
             *         visible, then return true so that we will get more drag events.
             *     </li>
             *     <li>
             *         ACTION_DRAG_ENTERED - we set the text of {@code TextView mResultText} to the string
             *         "ACTION_DRAG_ENTERED" and break
             *     </li>
             *     <li>
             *         ACTION_DRAG_EXITED - we set the text of {@code TextView mResultText} to the string
             *         "ACTION_DRAG_EXITED" and break
             *     </li>
             *     <li>
             *         ACTION_DROP - we set the text of {@code TextView mResultText} to the string
             *         "ACTION_DROP" and break
             *     </li>
             *     <li>
             *         ACTION_DRAG_ENDED - we set the visibility of {@code DraggableDot mHiddenDot}
             *         back to invisible, and fetch the result of our parameter {@code DragEvent event}
             *         to {@code boolean dropped} and if it is true we set the text of {@code TextView mResultText}
             *         to the string "Dropped", otherwise we set it to "No drop". Finally we break.
             *     </li>
             *     <li>
             *         default - we set the text of {@code TextView mResultText} to the string value
             *         of {@code action} and break
             *     </li>
             * </ul>
             * If we have executed a break instead of a return above, we now return false.
             *
             * @param v     The View that received the drag event.
             * @param event The {@link android.view.DragEvent} object for the drag event.
             * @return {@code true} if the drag event was handled successfully, or {@code false}
             * if the drag event was not handled. We need to return true from ACTION_DRAG_STARTED
             * if we want to receive any more drag events.
             */
            @Override
            public boolean onDrag(View v, DragEvent event) {
                final int action = event.getAction();
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED: {
                        // Bring up a fourth draggable dot on the fly. Note that it
                        // is properly notified about the ongoing drag, and lights up
                        // to indicate that it can handle the current content.
                        mResultText.setText("ACTION_DRAG_STARTED");
                        mHiddenDot.setVisibility(View.VISIBLE);
                        return true;
                    }

                    case DragEvent.ACTION_DRAG_ENTERED: {
                        mResultText.setText("ACTION_DRAG_ENTERED");
                    }
                    break;

                    case DragEvent.ACTION_DRAG_EXITED: {
                        mResultText.setText("ACTION_DRAG_EXITED");
                    }
                    break;

                    case DragEvent.ACTION_DROP: {
                        mResultText.setText("ACTION_DROP");
                    }
                    break;

                    case DragEvent.ACTION_DRAG_ENDED: {
                        // Hide the surprise again
                        mHiddenDot.setVisibility(View.INVISIBLE);

                        // Report the drop/no-drop result to the user
                        final boolean dropped = event.getResult();
                        mResultText.setText(dropped ? "Dropped!" : "No drop");
                    }
                    break;

                    default: {
                        mResultText.setText(String.valueOf(action));
                    }
                }
                return false;
            }
        });
    }
}