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

import com.example.android.apis.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

/**
 * Uses a TextSwitcher to animate changes in the View controlled by the TextSwitcher.
 * Two TextView's are requested from the app's overriding of ViewSwitcher.ViewFactory
 * method makeView, and when the View needs to be updated TextSwitcher.setText is
 * called to change the text and animate to it.
 */
public class TextSwitcher1 extends Activity implements ViewSwitcher.ViewFactory, View.OnClickListener {
    /**
     * {@code TextSwitcher} in our layout file with id R.id.switcher
     */
    private TextSwitcher mSwitcher;
    /**
     * Counter used as the text in the {@code TextView} that the {@code setText} method of our field
     * {@code TextSwitcher mSwitcher} switches to.
     */
    private int mCounter = 0;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.text_switcher_1.
     * We initialize our field {@code TextSwitcher mSwitcher} by finding the view in our layout with
     * the id R.id.switcher and set its {@code ViewFactory} to "this". We create a fade in animation
     * from android.R.anim.fade_in to initialize {@code Animation in}, and a fade out animation from
     * android.R.anim.fade_out to initialize {@code Animation out}, then set the in animation of
     * {@code mSwitcher} to {@code in} and the out animation to {@code out}.
     * <p>
     * We initialize our variable {@code Button nextButton} by finding the view with id R.id.next
     * ("Next") and set its {@code OnClickListener} to "this". Finally we call our method
     * {@code updateCounter} to have {@code mSwitcher} set its text to the current string value of
     * our field {@code int mCounter}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.text_switcher_1);

        mSwitcher = (TextSwitcher) findViewById(R.id.switcher);
        mSwitcher.setFactory(this);

        Animation in = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out);
        mSwitcher.setInAnimation(in);
        mSwitcher.setOutAnimation(out);

        Button nextButton = (Button) findViewById(R.id.next);
        nextButton.setOnClickListener(this);

        updateCounter();
    }

    /**
     * Called when the button with id R.id.next ("Next") is clicked. We increment our field
     * {@code int mCounter}, then call our method {@code updateCounter} to have {@code mSwitcher}
     * set its text to the new string value of our field {@code int mCounter}.
     *
     * @param v View that was clicked
     */
    @Override
    public void onClick(View v) {
        mCounter++;
        updateCounter();
    }

    /**
     * Called to switch the text that {@code TextSwitcher mSwitcher} is displaying in its
     * {@code TextView} to the string value of {@code int mCounter}. The {@code setText}
     * method of {@code mSwitcher} does this for us.
     */
    private void updateCounter() {
        mSwitcher.setText(String.valueOf(mCounter));
    }

    /**
     * Creates a new {@code View} to be added in a {@code ViewSwitcher}. We create a new instance to
     * initialize our variable {@code TextView t}, set its gravity to TOP and CENTER_HORIZONTAL, and
     * set its text size to 36. We log the fact that we were asked to create a new {@code TextView}
     * then return {@code t} to the caller.
     *
     * @return a {@code View}
     */
    @Override
    public View makeView() {
        TextView t = new TextView(this);
        t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        t.setTextSize(36);
        Log.i("makeView", "New View requested");
        return t;
    }
}
