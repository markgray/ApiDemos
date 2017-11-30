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

package com.example.android.apis.view;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import com.example.android.apis.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.ViewFlipper;

/**
 * Shows how to use the four different animations available for a ViewFlipper: "Push up", "Push left",
 * "Cross fade", and "Hyperspace". A ViewFlipper is a Simple ViewAnimator that will animate between
 * two or more views that have been added to it. Only one child is shown at a time. If requested, it
 * can automatically flip between each child at a regular interval.
 */
public class Animation2 extends Activity implements AdapterView.OnItemSelectedListener {
    /**
     * The {@code ViewFlipper} in our layout with ID R.id.flipper
     */
    private ViewFlipper mFlipper;

    /**
     * Strings used for the {@code Adapter} used by the {@code Spinner} with ID R.id.spinner
     */
    private String[] mStrings = {
            "Push up", "Push left", "Cross fade", "Hyperspace"
    };

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.animation_2. We
     * initialize our field {@code ViewFlipper mFlipper} by locating the {@code ViewFlipper} with ID
     * R.id.flipper in our layout file, and instruct it to "start flipping" (starts a timer for it to
     * cycle through its child views).
     * <p>
     * Next we set {@code Spinner s} by locating the view with ID R.id.spinner, then we create
     * {@code ArrayAdapter<String> adapter} from our field {@code String[] mStrings} using the
     * android.R.layout.simple_spinner_item as the resource ID for a layout file containing a
     * TextView to use when instantiating views, and set android.R.layout.simple_spinner_dropdown_item
     * as the layout resource defining the drop down views.
     * <p>
     * We then set {@code adapter} as the adapter for {@code Spinner s}, and set "this" as the
     * {@code OnItemSelectedListener} for {@code s}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animation_2);

        mFlipper = ((ViewFlipper) this.findViewById(R.id.flipper));
        mFlipper.startFlipping();

        Spinner s = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mStrings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);
        s.setOnItemSelectedListener(this);
    }

    /**
     * Callback method to be invoked when an item in the {@code Spinner} with ID R.id.spinner has been
     * selected. We switch base on the value of our parameter {@code position}:
     * <ul>
     * <li>
     * 0 - "Push up" we set the in animation of {@code ViewFlipper mFlipper} to the {@code Animation}
     * loaded from R.anim.push_left_in, and its out animation to one loaded from R.anim.push_left_out
     * </li>
     * <li>
     * 1 - "Push left" we set the in animation of {@code ViewFlipper mFlipper} to the {@code Animation}
     * loaded from R.anim.push_up_in, and its out animation to one loaded from R.anim.push_up_out
     * </li>
     * <li>
     * 2 - "Cross fade" we set the in animation of {@code ViewFlipper mFlipper} to the {@code Animation}
     * loaded from android.R.anim.fade_in, and its out animation to one loaded from android.R.anim.fade_out
     * </li>
     * <li>
     * default - "Hyperspace" we set the in animation of {@code ViewFlipper mFlipper} to the {@code Animation}
     * loaded from R.anim.hyperspace_in, and its out animation to one loaded from R.anim.hyperspace_out
     * </li>
     * </ul>
     * The eight different animation resource files contain XML elements for performing the animations:
     * <ul>
     * <li>
     * R.anim.push_up_in - contains a {@code <translate>} for y from 100p to 0p with a duration
     * of 300 milliseconds, and an {@code <alpha>} from 0.0 to 1.0 also with a duration of 300
     * milliseconds.
     * </li>
     * <li>
     * R.anim.push_up_out - contains a {@code <translate>} for y from 0p to -100p with a duration
     * of 300 milliseconds, and an {@code <alpha>} from 1.0 to 0.0 also with a duration of 300
     * milliseconds.
     * </li>
     * <li>
     * R.anim.push_left_in - contains a {@code <translate>} for x from 100p to 0p with a duration
     * of 300 milliseconds, and an {@code <alpha>} from 0.0 to 1.0 also with a duration of 300
     * milliseconds.
     * </li>
     * <li>
     * R.anim.push_left_out - contains a {@code <translate>} for x from 0p to -100p with a duration
     * of 300 milliseconds, and an {@code <alpha>} from 1.0 to 0.0 also with a duration of 300
     * milliseconds.
     * </li>
     * <li>
     * android.R.anim.fade_in - contains an {@code <alpha>} from 0.0 to 1.0 using an interpolator
     * of android:interpolator="@interpolator/decelerate_quad", and a duration of config_longAnimTime
     * (500 milliseconds).
     * </li>
     * <li>
     * android.R.anim.fade_out - contains an {@code <alpha>} from 1.0 to 0.0 using an interpolator
     * of android:interpolator="@interpolator/accelerate_quad", and a duration of config_mediumAnimTime
     * (400 milliseconds).
     * </li>
     * <li>
     * R.anim.hyperspace_in - contains an {@code <alpha>} from 0.0 to 1.0, with a duration of 300
     * milliseconds and an start offset of 1200 milliseconds
     * </li>
     * <ul>
     * R.anim.hyperspace_out - contains a {@code <scale>} that scales x from 1.0 to 1.4, y from
     * 1.0 to 0.6, android:pivotX="50%", android:pivotY="50%", android:fillEnabled="true",
     * android:fillAfter="false" and a duration of 700 milliseconds. This is followed by a
     * {@code <set>} which contains a {@code <scale>} and a {@code <rotate>}, both with a start
     * offset of 700 milliseconds. The {@code <set>} uses accelerate_interpolator as its interpolator,
     * the {@code <scale>} scales x from 1.4 to 0.0, y from 0.6 to 0, android:pivotX="50%",
     * android:pivotY="50%", android:fillEnabled="true", android:fillBefore="false", android:fillAfter="true"
     * and a duration of 400 milliseconds, the {@code <rotate>} rotates from 0 degrees to -45 degrees,
     * with android:toYScale="0.0", android:pivotX="50%", android:pivotY="50%", android:fillEnabled="true",
     * android:fillBefore="false", android:fillAfter="true" and a duration of 400 milliseconds.
     * </ul>
     * </ul>
     *
     * @param parent   The AdapterView where the selection happened
     * @param v        The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        switch (position) {

            case 0:
                mFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_up_in));
                mFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_up_out));
                break;
            case 1:
                mFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
                mFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
                break;
            case 2:
                mFlipper.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
                mFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                break;
            default:
                mFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.hyperspace_in));
                mFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.hyperspace_out));
                break;
        }
    }

    /**
     * Callback method to be invoked when the selection disappears from this {@code Spinner}. We ignore.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
