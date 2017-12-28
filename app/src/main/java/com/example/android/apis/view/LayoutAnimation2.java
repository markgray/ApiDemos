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

import android.app.ListActivity;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Shows how to use a LayoutAnimationController to animate the layout of a list in a ListActivity
 * Too fast on Nexus 6 to see effect
 */
public class LayoutAnimation2 extends ListActivity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. The we set our list adapter to a new instance of {@code ArrayAdapter} which
     * is constructed using our array {@code String[] mStrings} as the data, and the system layout
     * android.R.layout.simple_list_item_1 as the layout file containing a TextView to use when
     * instantiating views.
     * <p>
     * We create a new instance for {@code AnimationSet set} with the value true as the parameter so
     * that all of the animations in the set should use the interpolator associated with {@code set}.
     * We create {@code Animation animation} as an {@code AlphaAnimation} that goes from 0.0 to 1.0f,
     * set its duration to 50 milliseconds, and add it to {@code set}. We now set {@code animation}
     * to a new instance of {@code TranslateAnimation} with a from X value of 0.0 RELATIVE_TO_SELF,
     * a to X value of 1.0f RELATIVE_TO_SELF, a from Y value of -1.0 RELATIVE_TO_SELF, and a to Y
     * value of 0.0 RELATIVE_TO_SELF, set its duration to 100 milliseconds and add it to {@code set}.
     * <p>
     * We create {@code LayoutAnimationController controller} to use {@code set} with a delay of 0.5.
     * Finally we fetch our {@code ListView} to {@code ListView listView} and set ist layout animation
     * controller to {@code controller}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mStrings));

        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(50);
        set.addAnimation(animation);

        animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(100);
        set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
        ListView listView = getListView();
        listView.setLayoutAnimation(controller);
    }

    /**
     * The data that our {@code ArrayAdapter} uses to fill our {@code ListView}.
     */
    private String[] mStrings = {
            "Bordeaux",
            "Lyon",
            "Marseille",
            "Nancy",
            "Paris",
            "Toulouse",
            "Strasbourg"
    };
}
