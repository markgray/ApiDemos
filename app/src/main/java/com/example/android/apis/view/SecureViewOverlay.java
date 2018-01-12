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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * This view is part of the {@link SecureView} demonstration activity.
 * <p>
 * This view is constructed in such a way as to obscure the buttons and descriptive
 * text of the activity in a poor attempt to fool the user into clicking on the buttons
 * despite the activity telling the user that they may be harmful.
 */
public class SecureViewOverlay extends ViewGroup {
    /**
     * {@code SecureView} {@code Activity} to use to find views we need to obscure.
     */
    private SecureView mActivity;

    /**
     * Constructor called to perform inflation from XML. We just call our super's constructor.
     *
     * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public SecureViewOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Saves its parameter {@code SecureView activity} in our field {@code SecureView mActivity} to
     * later use to find the views we need to obscure.
     *
     * @param activity "this" of the {@code SecureView} {@code Activity} we are to spoof.
     */
    public void setActivityToSpoof(SecureView activity) {
        this.mActivity = activity;
    }

    /**
     * Measure the view and its content to determine the measured width and the measured height. We
     * call the method {@code measureChildren} to ask all of the children of our {@code ViewGroup}
     * to measure themselves, then we call our super's implementation of {@code onMeasure}.
     *
     * @param widthMeasureSpec  horizontal space requirements as imposed by the parent.
     * @param heightMeasureSpec vertical space requirements as imposed by the parent.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Called from layout when this view should assign a size and position to each of its children.
     * <p>
     * We call our method {@code spoofLayout} with the view in our the layout file that inflated to
     * us that has the ID R.id.secure_view_overlay_description, and the view in the activity we
     * are spoofing ({@code SecureView mActivity}) with ID R.id.secure_view_description.
     * <p>
     * We call our method {@code spoofLayout} with the view in our the layout file that inflated to
     * us that has the ID R.id.secure_view_overlay_button1, and the view in the activity we
     * are spoofing ({@code SecureView mActivity}) with ID R.id.secure_view_unsecure_button.
     * <p>
     * We call our method {@code spoofLayout} with the view in our the layout file that inflated to
     * us that has the ID R.id.secure_view_overlay_button2, and the view in the activity we
     * are spoofing ({@code SecureView mActivity}) with ID R.id.secure_view_builtin_secure_button.
     * <p>
     * We call our method {@code spoofLayout} with the view in our the layout file that inflated to
     * us that has the ID R.id.secure_view_overlay_button3, and the view in the activity we
     * are spoofing ({@code SecureView mActivity}) with ID R.id.secure_view_custom_secure_button.
     * <p>
     * In each of these calls to {@code spoofLayout} the position of the view of the activity we are
     * spoofing is used to position the view in our own layout so that the original is obscured by
     * our layout.
     *
     * @param changed This is a new size or position for this view
     * @param l       Left position, relative to parent
     * @param t       Top position, relative to parent
     * @param r       Right position, relative to parent
     * @param b       Bottom position, relative to parent
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        spoofLayout(findViewById(R.id.secure_view_overlay_description),
                mActivity.findViewById(R.id.secure_view_description));
        spoofLayout(findViewById(R.id.secure_view_overlay_button1),
                mActivity.findViewById(R.id.secure_view_unsecure_button));
        spoofLayout(findViewById(R.id.secure_view_overlay_button2),
                mActivity.findViewById(R.id.secure_view_builtin_secure_button));
        spoofLayout(findViewById(R.id.secure_view_overlay_button3),
                mActivity.findViewById(R.id.secure_view_custom_secure_button));
    }

    /**
     * Causes the parameter {@code View spoof} to be laid out over the parameter {@code View original}
     * to obscure it. We allocate an {@code int[2]} array to initialize our variable {@code int[] globalPos},
     * and call the method {@code getLocationOnScreen} to load it with the coordinates of this {@code ViewGroup}
     * on the screen. We then initialize {@code int x} with {@code globalPos[0]}, and {@code int y} with
     * {@code globalPos[1]}. We call the {@code getLocationOnScreen} method of our parameter {@code View original}
     * to load {@code globalPos} with the coordinates of the view we want to obscure, then set {@code x}
     * to {@code globalPos[0] - x} and {@code y} to {@code globalPos[1] - y}. Finally we call the {@code layout}
     * method of {@code spoof} to assign the size and position of {@code spoof} to be top left at {@code (x,y)},
     * and bottom right at ({@code x} plus the width of {@code original}, {@code y} plus the height of {@code original}).
     *
     * @param spoof    view within our layout we need to position
     * @param original the view in the activity we are spoofing that we want to obscure.
     */
    private void spoofLayout(View spoof, View original) {
        final int[] globalPos = new int[2];
        getLocationOnScreen(globalPos);
        int x = globalPos[0];
        int y = globalPos[1];

        original.getLocationOnScreen(globalPos);
        x = globalPos[0] - x;
        y = globalPos[1] - y;
        spoof.layout(x, y, x + original.getWidth(), y + original.getHeight());
    }
}
