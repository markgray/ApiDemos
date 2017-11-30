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

package com.example.android.apis.view;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import com.example.android.apis.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Shows the effect of selecting each of seven different types of TranslateAnimation interpolator:
 * "Accelerate", "Decelerate", "Accelerate/Decelerate", "Anticipate", "Overshoot",
 * "Anticipate/Overshoot", and "Bounce".
 */
public class Animation3 extends Activity implements AdapterView.OnItemSelectedListener {
    /**
     * The list of types of interpolators used to create the {@code Adapter} which is used by the
     * {@code Spinner} with ID R.id.spinner in our layout file.
     */
    private static final String[] INTERPOLATORS = {
            "Accelerate", "Decelerate", "Accelerate/Decelerate",
            "Anticipate", "Overshoot", "Anticipate/Overshoot",
            "Bounce"
    };

    /**
     * Called when our activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.animation_3. We
     * initialize {@code Spinner s} by locating the spinner with ID R.id.spinner in our layout, then
     * create {@code ArrayAdapter<String> adapter} using android.R.layout.simple_spinner_item as the
     * resource ID for a layout file containing a TextView to use when instantiating views, and our
     * array {@code String[] INTERPOLATORS} as the objects to represent in the {@code Spinner}, we
     * set the layout resource to create the drop down views of {@code adapter} to the resource file
     * android.R.layout.simple_spinner_dropdown_item, and then set {@code adapter} as the
     * {@code SpinnerAdapter} for {@code s}. Finally we set "this" as the {@code OnItemSelectedListener}
     * for {@code s}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animation_3);

        Spinner s = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, INTERPOLATORS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);
        s.setOnItemSelectedListener(this);
    }

    /**
     * Callback method to be invoked when an item in the {@code Spinner} with ID R.id.spinner has
     * been selected. First we locate the {@code View target} in our layout with ID R.id.target
     * (the TextView with the text "Interpolators" which we animate), then we find its parent view
     * {@code View targetParent} (the main LinearLayout holding the entire UI). We create
     * {@code Animation a} which is a {@code TranslateAnimation} with a {@code fromXDelta} of 0.0,
     * a {@code toXDelta} which is calculated to be the width of {@code targetParent} minus the width
     * of {@code target} minus the left and right padding of {@code targetParent}, and with 0.0
     * for both {@code fromYDelta} and {@code toYDelta}. We then set the duration of {@code a} to
     * 1000 milliseconds, its start offset to 300 milliseconds (when the animation should start
     * relative to the start time), its repeat mode to RESTART (when it reaches the end it restarts
     * from the beginning), and its repeat count to INFINITE.
     *
     * Next we switch based on the parameter {@code position} to choose the type of interpolator:
     * <ul>
     *     <li>
     *         0 - "Accelerate" we load the interpolator of {@code a} from the resource with ID
     *         android.R.anim.accelerate_interpolator.
     *     </li>
     *     <li>
     *         1 - "Decelerate" we load the interpolator of {@code a} from the resource with ID
     *         android.R.anim.decelerate_interpolator
     *     </li>
     *     <li>
     *         2 - "Accelerate/Decelerate" we load the interpolator of {@code a} from the resource with ID
     *         android.R.anim.accelerate_decelerate_interpolator
     *     </li>
     *     <li>
     *         3 - "Anticipate" we load the interpolator of {@code a} from the resource with ID
     *         android.R.anim.anticipate_interpolator
     *     </li>
     *     <li>
     *         4 - "Overshoot" we load the interpolator of {@code a} from the resource with ID
     *         android.R.anim.overshoot_interpolator
     *     </li>
     *     <li>
     *         5 - "Anticipate/Overshoot" we load the interpolator of {@code a} from the resource with ID
     *         android.R.anim.anticipate_overshoot_interpolator
     *     </li>
     *     <li>
     *         6 - "Bounce" we load the interpolator of {@code a} from the resource with ID
     *         android.R.anim.bounce_interpolator
     *     </li>
     * </ul>
     * Finally we instruct {@code target} to start {@code Animation a} now.
     *
     * @param parent   The AdapterView where the selection happened
     * @param v        The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        final View target = findViewById(R.id.target);
        final View targetParent = (View) target.getParent();

        Animation a = new TranslateAnimation(0.0f,
                targetParent.getWidth() - target.getWidth() - targetParent.getPaddingLeft() -
                        targetParent.getPaddingRight(), 0.0f, 0.0f);
        a.setDuration(1000);
        a.setStartOffset(300);
        a.setRepeatMode(Animation.RESTART);
        a.setRepeatCount(Animation.INFINITE);

        switch (position) {
            case 0:
                a.setInterpolator(AnimationUtils.loadInterpolator(this,
                        android.R.anim.accelerate_interpolator));
                break;
            case 1:
                a.setInterpolator(AnimationUtils.loadInterpolator(this,
                        android.R.anim.decelerate_interpolator));
                break;
            case 2:
                a.setInterpolator(AnimationUtils.loadInterpolator(this,
                        android.R.anim.accelerate_decelerate_interpolator));
                break;
            case 3:
                a.setInterpolator(AnimationUtils.loadInterpolator(this,
                        android.R.anim.anticipate_interpolator));
                break;
            case 4:
                a.setInterpolator(AnimationUtils.loadInterpolator(this,
                        android.R.anim.overshoot_interpolator));
                break;
            case 5:
                a.setInterpolator(AnimationUtils.loadInterpolator(this,
                        android.R.anim.anticipate_overshoot_interpolator));
                break;
            case 6:
                a.setInterpolator(AnimationUtils.loadInterpolator(this,
                        android.R.anim.bounce_interpolator));
                break;
        }

        target.startAnimation(a);
    }

    /**
     * Callback method to be invoked when the selection disappears from this view. We ignore it.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}