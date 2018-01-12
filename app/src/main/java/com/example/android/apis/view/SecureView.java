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
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * This activity demonstrates two different ways in which views can be made more secure to
 * touch spoofing attacks by leveraging framework features.
 * <p>
 * The activity presents 3 buttons that ostensibly perform a risky security critical
 * function.  Under ordinary circumstances, the user would never click on these buttons
 * or would at least think long and hard about it.  However, a carefully crafted toast can
 * overlay the contents of the activity in such a way as to make the user believe the buttons
 * are innocuous.  Since the toast cannot receive input, the touches are passed down to the
 * activity potentially yielding an effect other than what the user intended.
 * <p>
 * To simulate the spoofing risk, this activity pops up a specially crafted overlay as
 * a toast lay-ed out so as to cover the buttons and part of the descriptive text.
 * For the purposes of this demonstration, pretend that the overlay was actually popped
 * up by a malicious application published by the International Cabal of Evil Penguins.
 * <p>
 * The 3 buttons are set up as follows:
 * <p>
 * 1. The "unsecured button" does not apply any touch filtering of any kind.
 * When the toast appears, this button remains clickable as usual which creates an
 * opportunity for spoofing to occur.
 * <p>
 * 2. The "built-in secured button" leverages the android:filterTouchesWhenObscured view
 * attribute to ask the framework to filter out touches when the window is obscured.
 * When the toast appears, the button does not receive the touch and appears to be inoperable.
 * <p>
 * 3. The "custom secured button" adds a touch listener to the button which intercepts the
 * touch event and checks whether the window is obscured.  If so, it warns the user and
 * drops the touch event.  This example is intended to demonstrate how a view can
 * perform its own filtering and provide additional feedback by examining the {@code MotionEvent}
 * flags to determine whether the window is obscured.  Here we use a touch listener but
 * a custom view subclass could perform the filtering by overriding
 * {@code View.onFilterTouchEventForSecurity(MotionEvent)}.
 * <p>
 * Refer to the comments on {@code View} for more information about view security.
 */
public class SecureView extends Activity {
    /**
     * Number of times that the user has been tricked into clicking an insecure button, used to
     * select which humorous terrifying message to pop up.
     */
    private int mClickCount;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.secure_view. We
     * initialize our variable {@code Button toastButton} by finding the view in our layout with ID
     * R.id.secure_view_toast_button ("Pop toast") and set its {@code OnClickListener} to an anonymous
     * class which calls our method {@code showOverlay} to pop up a toast obscuring our UI when the
     * button is clicked. We initialize our variable {@code Button unsecureButton} by finding the view
     * with ID R.id.secure_view_unsecure_button ("Don't click! It'll cost you!"))and call our method
     * {@code setClickedAction} with it to set its {@code OnClickListener} to an anonymous class which
     * pops up an alert dialog to terrify the user. We initialize our variable {@code Button builtinSecureButton}
     * by finding the view in our layout with the ID R.id.secure_view_builtin_secure_button ("Don't click!
     * It'll cost you!") and call our method {@code setClickedAction} with it to do the same thing. We
     * initialize our variable {@code Button customSecureButton} by finding the view with ID
     * R.id.secure_view_custom_secure_button ("Don't click! It'll cost you!") and also call our method
     * {@code setClickedAction} with it, followed by calling our method {@code setTouchFilter} with it
     * to add an {@code OnTouchListener} which prevents clicks from reaching the button if the
     * button is obscured.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secure_view);

        Button toastButton = (Button) findViewById(R.id.secure_view_toast_button);
        toastButton.setOnClickListener(new OnClickListener() {
            /**
             * Called when the button with ID R.id.secure_view_toast_button ("Pop toast") is clicked,
             * we just call our method {@code showOverlay} to pop up a toast obscuring our UI.
             *
             * @param v View that was clicked
             */
            @Override
            public void onClick(View v) {
                showOverlay();
            }
        });

        Button unsecureButton = (Button) findViewById(R.id.secure_view_unsecure_button);
        setClickedAction(unsecureButton);

        Button builtinSecureButton = (Button) findViewById(R.id.secure_view_builtin_secure_button);
        setClickedAction(builtinSecureButton);

        Button customSecureButton = (Button) findViewById(R.id.secure_view_custom_secure_button);
        setClickedAction(customSecureButton);
        setTouchFilter(customSecureButton);
    }

    /**
     * Generates a toast view with a special layout that will position itself right on top of this
     * view's interesting widgets. First we fetch the shared {@code LayoutInflater} for our main
     * window and use it to inflate the layout file R.layout.secure_view_overlay into our variable
     * {@code SecureViewOverlay overlay}. We call the {@code setActivityToSpoof} method of {@code overlay}
     * to save a reference to "this" in its {@code SecureView mActivity} field. Next we initialize
     * our variable {@code Toast toast} with a new instance, set its gravity to FILL, with (0,0) as
     * its offset, set is view to {@code overlay}, and show it.
     */
    private void showOverlay() {
        // Generate a toast view with a special layout that will position itself right
        // on top of this view's interesting widgets.  Sneaky huh?
        @SuppressLint("InflateParams")
        SecureViewOverlay overlay = (SecureViewOverlay) getLayoutInflater()
                .inflate(R.layout.secure_view_overlay, null);
        overlay.setActivityToSpoof(this);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.FILL, 0, 0);
        toast.setView(overlay);
        toast.show();
    }

    /**
     * Sets the {@code OnClickListener} of its parameter {@code Button button} to an anonymous class
     * which pops up an {@code AlertDialog} with a random "terrifying" message when the {@code Button}
     * is clicked.
     *
     * @param button {@code Button} whose {@code OnClickListener} we are to set
     */
    private void setClickedAction(Button button) {
        button.setOnClickListener(new OnClickListener() {
            /**
             * Pops up an {@code AlertDialog} with a random "terrifying" message when the {@code Button}
             * is clicked. First we initialize our variable {@code String[] messages} by fetching the
             * string array with resource ID R.array.secure_view_clicked (consists of 5 humorous
             * terrifying strings). We initialize our variable {@code String message} with the string
             * at {@code messages[mClickCount++ % messages.length]}. We create a new instance of
             * {@code AlertDialog.Builder}, set its title to the string with resource ID
             * R.string.secure_view_action_dialog_title ("Oh no!"), set its message to {@code message},
             * set the text of its neutral button to the string with ID R.string.secure_view_action_dialog_dismiss
             * ("Oops..."), and show the dialog.
             *
             * @param v view which has been clicked
             */
            @Override
            public void onClick(View v) {
                String[] messages = getResources().getStringArray(R.array.secure_view_clicked);
                String message = messages[mClickCount++ % messages.length];

                new AlertDialog.Builder(SecureView.this)
                        .setTitle(R.string.secure_view_action_dialog_title)
                        .setMessage(message)
                        .setNeutralButton(getResources().getString(
                                R.string.secure_view_action_dialog_dismiss), null)
                        .show();
            }
        });
    }

    /**
     * Sets the {@code OnTouchListener} of its parameter {@code Button button} to an anonymous class
     * whose {@code onTouch} override prevents touches of {@code button} from reaching its
     * {@code OnClickListener} if the button is obscured (and pops up an {@code AlertDialog} to inform
     * the user about what it did).
     *
     * @param button Button whose {@code OnTouchListener} we are to set
     */
    private void setTouchFilter(final Button button) {
        button.setOnTouchListener(new OnTouchListener() {
            /**
             * Called when a touch event is dispatched to a view. This allows listeners to get a
             * chance to respond before the target view. We fetch the flags from our parameter
             * {@code MotionEvent event}, mask off the FLAG_WINDOW_IS_OBSCURED bit (if 1 indicates
             * that the window that received this motion event is partly or wholly obscured by another
             * visible window above it), and if it is not zero we check to see if the action of
             * {@code event} is ACTION_UP in which case we build and show an {@code AlertDialog} with
             * the title resource ID R.string.secure_view_caught_dialog_title ("Saved!"), the message
             * resource ID R.string.secure_view_caught_dialog_message ("Careful! There appears to be
             * another window partly obscuring this window... Something unutterably HORRIBLE might
             * have happened."), set the text of its neutral button to the string with ID
             * R.string.secure_view_caught_dialog_dismiss ("Phew!"), then show the dialog. We then
             * return true to the caller to prevent the button from processing the touch.
             * <p>
             * If the window is not obscured we return false to the caller, allowing the click to
             * pass through to the button's {@code OnClickListener}.
             *
             * @param v The view the touch event has been dispatched to.
             * @param event The MotionEvent object containing full information about the event.
             * @return True if the listener has consumed the event, false otherwise.
             */
            @TargetApi(Build.VERSION_CODES.GINGERBREAD)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getFlags() & MotionEvent.FLAG_WINDOW_IS_OBSCURED) != 0) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        new AlertDialog.Builder(SecureView.this)
                                .setTitle(R.string.secure_view_caught_dialog_title)
                                .setMessage(R.string.secure_view_caught_dialog_message)
                                .setNeutralButton(getResources().getString(
                                        R.string.secure_view_caught_dialog_dismiss), null)
                                .show();
                    }
                    // Return true to prevent the button from processing the touch.
                    return true;
                }
                return false;
            }
        });
    }
}
