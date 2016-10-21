/*
 * Copyright (C) 2009 The Android Open Source Project
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

import com.example.android.apis.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * ReorderOnLaunch is the first of a sequence of four Activities: ReorderTwo, ReorderThree, and
 * ReorderFour follow.  A button on the fourth will use the Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
 * flag to bring the second of the activities to the front of the history stack. After that,
 * proceeding back through the history should begin with the newly front most second reorder
 * activity, then the fourth, the third, and finally the first.
 */
public class ReorderFour extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.reorder_four. Then we find
     * the Button "Bring the second in front" (R.id.reorder_second_to_front) and set its OnClickListener
     * to OnClickListener mClickListener.
     *
     * @param savedState always null since onSaveInstanceState is not overridden
     */
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        
        setContentView(R.layout.reorder_four);
        
        Button twoButton = (Button) findViewById(R.id.reorder_second_to_front);
        twoButton.setOnClickListener(mClickListener);
    }

    /**
     * OnClickListener to the "Bring the second in front" (R.id.reorder_second_to_front) Button.
     */
    private final OnClickListener mClickListener = new OnClickListener() {
        /**
         * Called when the "Bring the second in front" (R.id.reorder_second_to_front) Button is clicked.
         * First we create an Intent intent to launch the Activity ReorderTwo, then we add the flag
         * FLAG_ACTIVITY_REORDER_TO_FRONT to that Intent (causes the activity to be brought to the
         * front of its task's history stack if it is already running when the Intent is launched).
         * Finally we start the Activity using Intent intent.
         *
         * @param v View of Button that was clicked
         */
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ReorderFour.this, ReorderTwo.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    };
}