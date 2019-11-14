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

package com.example.android.apis.app;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity used by StatusBarNotification to show the notification to the user.
 */
public class NotificationDisplay extends AppCompatActivity implements View.OnClickListener {
    /**
     * Initialization of the Activity after it is first created. First we call through to our super's
     * implementation of {@code onCreate}. Then we set the flag FLAG_DIM_BEHIND to dim any windows
     * behind our window. We create {@code RelativeLayout container}, create {@code ImageButton button}
     * set its image to the resource ID sent us as an extra in our {@code Intent} using the key
     * "moodimg", and set the {@code OnClickListener} of {@code button} to "this". We create
     * {@code RelativeLayout.LayoutParams lp} to have WRAP_CONTENT as its x and y dimensions, and
     * add the layout rule CENTER_IN_PARENT to it. We add {@code button} to {@code container} using
     * {@code lp} as its layout parameters, and finally set our content view to {@code container}.
     *
     * @param icicle We do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle icicle) {
        // Be sure to call the super class.
        super.onCreate(icicle);

        // Have the system dim any windows behind this one.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        
        RelativeLayout container = new RelativeLayout(this);
        
        ImageButton button = new ImageButton(this);
        button.setImageResource(getIntent().getIntExtra("moodimg", 0));
        button.setOnClickListener(this);
        
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        
        container.addView(button, lp);
        
        setContentView(container);
    }

    /**
     * Called when the user clicks on us. First we cancel the notification, then we create an
     * {@code Intent intent} to launch {@code StatusBarNotifications}, set the action to ACTION_MAIN
     * to start it as a main entry point, (does not expect to receive data), set the flag
     * FLAG_ACTIVITY_NEW_TASK (since task is already running for the {@code StatusBarNotifications}
     * activity a new activity will not be started; instead, the current task will simply be brought to
     * the front of the screen with the state it was last in), and we start the activity using
     * {@code intent}. Finally we call {@code finish()} to close our own activity.
     *
     * @param v View that was clicked on.
     */
    public void onClick(View v) {
        // The user has confirmed this notification, so remove it.
        //noinspection ConstantConditions
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .cancel(R.layout.status_bar_notifications);
        
        // Pressing on the button brings the user back to our mood ring,
        // as part of the api demos app.  Note the use of NEW_TASK here,
        // since the notification display activity is run as a separate task.
        Intent intent = new Intent(this, StatusBarNotifications.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        
        // We're done.
        finish();
    }
}
