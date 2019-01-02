/*
 * Copyright (C) 2013 The Android Open Source Project
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
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

/**
 * Allows you to choose the animation that occurs when the screen is rotated:
 * either ROTATION_ANIMATION_ROTATE, ROTATION_ANIMATION_CROSSFADE,
 * ROTATION_ANIMATION_JUMPCUT, or ROTATION_ANIMATION_SEAMLESS.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class RotationAnimation extends Activity {

    /**
     * Current rotationAnimation to use when setting WindowManager.LayoutParams.rotationAnimation
     */
    private int mRotationAnimation = LayoutParams.ROTATION_ANIMATION_ROTATE;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.rotation_animation. We then
     * call our method {@code setRotationAnimation} to set the window attributes value of
     * WindowManager.LayoutParams.rotationAnimation to our field {@code mRotationAnimation}. Next
     * we locate the CheckBox R.id.windowFullscreen ("FULLSCREEN") and set its OnCheckChangedListener
     * to an anonymous class which calls our method setFullscreen when the CheckBox changes state.
     * Finally we locate the RadioGroup R.id.rotation_radio_group and set its OnCheckedChangeListener
     * to an anonymous class which chooses the value to set mRotationAnimation to based on the radio
     * button which has been checked:
     * <ul>
     *     <li>
     *         R.id.rotate LayoutParams.ROTATION_ANIMATION_ROTATE, specifies that this
     *         window will visually rotate in or out following a rotation.
     *     </li>
     *     <li>
     *         R.id.crossfade LayoutParams.ROTATION_ANIMATION_CROSSFADE, specifies that
     *         this window will fade in or out following a rotation
     *     </li>
     *     <li>
     *         R.id.jumpcut LayoutParams.ROTATION_ANIMATION_JUMPCUT, specifies that this
     *         window will immediately disappear or appear following a rotation.
     *     </li>
     *     <li>
     *         R.id.seamless LayoutParams.ROTATION_ANIMATION_SEAMLESS, specifies seamless
     *         rotation mode, works like JUMPCUT but will fall back to CROSSFADE if
     *         rotation can't be applied without pausing the screen.
     *     </li>
     * </ul>
     * and then calls our method setRotationAnimation to change the window attributes using our
     * our field {@code mRotationAnimation} for the WindowManager.LayoutParams.rotationAnimation.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rotation_animation);

        setRotationAnimation(mRotationAnimation);

        ((CheckBox)findViewById(R.id.windowFullscreen)).setOnCheckedChangeListener(
            new CompoundButton.OnCheckedChangeListener() {
                /**
                 * Called when the checked state of our R.id.windowFullscreen compound button
                 * changed. We simply call our method setFullscreen with the isChecked parameter
                 * passed us.
                 *
                 * @param buttonView The compound button view whose state has changed.
                 * @param isChecked The new checked state of buttonView.
                 */
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setFullscreen(isChecked);
                }
            }
        );

        ((RadioGroup)findViewById(R.id.rotation_radio_group)).setOnCheckedChangeListener(
            new RadioGroup.OnCheckedChangeListener() {
                /**
                 * Called when the checked radio button has changed. When the selection is
                 * cleared, checkedId is -1. We switch on checkedId and set mRotationAnimation
                 * to the appropriate value:
                 * <ul>
                 *     <li>
                 *         R.id.rotate LayoutParams.ROTATION_ANIMATION_ROTATE, specifies that this
                 *         window will visually rotate in or out following a rotation.
                 *     </li>
                 *     <li>
                 *         R.id.crossfade LayoutParams.ROTATION_ANIMATION_CROSSFADE, specifies that
                  *         this window will fade in or out following a rotation
                 *     </li>
                 *     <li>
                 *         R.id.jumpcut LayoutParams.ROTATION_ANIMATION_JUMPCUT, specifies that this
                 *         window will immediately disappear or appear following a rotation.
                 *     </li>
                 *     <li>
                 *         R.id.seamless LayoutParams.ROTATION_ANIMATION_SEAMLESS, specifies seamless
                 *         rotation mode, works like JUMPCUT but will fall back to CROSSFADE if
                 *         rotation can't be applied without pausing the screen.
                 *     </li>
                 * </ul>
                 * and then we call our method setRotationAnimation to change the window
                 * attributes using this value for WindowManager.LayoutParams.rotationAnimation
                 *
                 * @param group the group in which the checked radio button has changed
                 * @param checkedId the unique identifier of the newly checked radio button
                 */
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        default:
                        case R.id.rotate:
                            mRotationAnimation = LayoutParams.ROTATION_ANIMATION_ROTATE;
                            break;
                        case R.id.crossfade:
                            mRotationAnimation = LayoutParams.ROTATION_ANIMATION_CROSSFADE;
                            break;
                        case R.id.jumpcut:
                            mRotationAnimation = LayoutParams.ROTATION_ANIMATION_JUMPCUT;
                            break;
                        case R.id.seamless:
                            mRotationAnimation = LayoutParams.ROTATION_ANIMATION_SEAMLESS;
                            break;
                    }
                    setRotationAnimation(mRotationAnimation);
                }
            }
        );
    }

    /**
     * Called when the FULLSCREEN CheckBox changes state. We set or clear the Window Attributes
     * WindowManager.LayoutParams.FLAG_FULLSCREEN bit according to the CheckBox state.
     *
     * @param on if true set the FLAG_FULLSCREEN bit or the window attributes, if false clear it.
     */
    private void setFullscreen(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |=  WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            winParams.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        win.setAttributes(winParams);
    }

    /**
     * Set the window attributes value of WindowManager.LayoutParams.rotationAnimation to our
     * parameter. We initialize {@code Window win} to the current {@link android.view.Window}
     * for the activity, initialize {@code WindowManager.LayoutParams winParams} to the current window
     * attributes associated with {@code win}, set the {@code rotationAnimation} field of
     * {@code winParams} to our field {@code rotationAnimation}, then call the {@code setAttributes}
     * method of {@code win} to set its window attributes to {@code winParams}.
     *
     * @param rotationAnimation WindowManager.LayoutParams.rotationAnimation to use
     */
    private void setRotationAnimation(int rotationAnimation) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.rotationAnimation = rotationAnimation;
        win.setAttributes(winParams);
    }
}
