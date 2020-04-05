/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.example.android.apis.inputmethod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import static android.widget.LinearLayout.VERTICAL;

/**
 * Demonstrates how to show the input method subtype enabler without relying on
 * {@link InputMethodManager#showInputMethodAndSubtypeEnabler(String)}, which is highly likely to be
 * broken.
 */
public class ShowInputMethodAndSubtypeEnabler extends AppCompatActivity {

    /**
     * Called when the activity is starting. First we call our super's implementation of {@code onCreate}.
     * Then we initialize {@code LinearLayout layout} with a new instance and set its orientation to
     * VERTICAL. We initialize {@code Button button} with a new instance, set its text to the string
     * "Show (All IMEs)", set its {@code OnClickListener} to an anonymous class which calls our method
     * {@code showInputMethodAndSubtypeEnabler} with null as the input method id, and then add {@code button}
     * to {@code layout}.
     * <p>
     * Next we loop over the list of {@code InputMethodInfo imi} that is returned by our method
     * {@code getEnabledInputMethodsThatHaveMultipleSubtypes}, initializing {@code Button button} with
     * a new instance, initializing {@code String id} with the id of {@code imi}, setting the text of
     * {@code button} to the string formed by concatenating the string "Show (" followed by {@code id}
     * followed by the string ")", set its {@code OnClickListener} to an anonymous class which calls
     * our method {@code showInputMethodAndSubtypeEnabler} with {@code id} as the input method id,
     * and then add {@code button} to {@code layout} before looping around for the next {@code InputMethodInfo}.
     * <p>
     * When done building {@code layout} we set it as our content view.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(VERTICAL);

        {
            final Button button = new Button(this);
            button.setText("Show (All IMEs)");
            button.setOnClickListener(v ->
                    showInputMethodAndSubtypeEnabler(
                            ShowInputMethodAndSubtypeEnabler.this,
                            null
                    )
            );
            layout.addView(button);
        }

        for (InputMethodInfo imi : getEnabledInputMethodsThatHaveMultipleSubtypes()) {
            final Button button = new Button(this);
            final String id = imi.getId();
            button.setText("Show (" + id + ")");
            button.setOnClickListener(v ->
                    showInputMethodAndSubtypeEnabler(
                            ShowInputMethodAndSubtypeEnabler.this,
                            id
                    )
            );
            layout.addView(button);
        }
        setContentView(layout);
    }

    /**
     * Called to show the input method and subtype enabler {@code Settings} activity for a specific
     * input method id (or all methods if our parameter {@code String inputMethodId} is null or empty).
     * First we initialize {@code Intent intent} with a new instance whose action is specified to be
     * ACTION_INPUT_METHOD_SUBTYPE_SETTINGS (show settings to enable/disable input method subtypes),
     * then set its flags: FLAG_ACTIVITY_NEW_TASK (this activity will become the start of a new task
     * on this history stack), FLAG_ACTIVITY_RESET_TASK_IF_NEEDED (it will be launched as the front
     * door of the task. This will result in the application of any affinities needed to have that
     * task in the proper state (either moving activities to or from it), or simply resetting that
     * task to its initial state if needed.), and FLAG_ACTIVITY_CLEAR_TOP (instead of launching a
     * new instance of the activity, all of the other activities on top of it will be closed and this
     * Intent will be delivered to the (now on top) old activity as a new Intent).
     * <p>
     * If our parameter {@code String inputMethodId} is not null or empty, we add it as an extra to
     * {@code intent} under the key EXTRA_INPUT_METHOD_ID, then start the activity of {@code intent}
     * with null options.
     *
     * @param context {@code Context} to use to access the {@code startActivity} method.
     * @param inputMethodId input method subtype to be displayed in the settings when showing the
     *                      settings to enable/disable input method subtypes. It is stored as an extra
     *                      under the key EXTRA_INPUT_METHOD_ID in the intent used to launch the
     *                      ACTION_INPUT_METHOD_SUBTYPE_SETTINGS action.
     */
    static void showInputMethodAndSubtypeEnabler(Context context, String inputMethodId) {
        final Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (!TextUtils.isEmpty(inputMethodId)) {
            intent.putExtra(Settings.EXTRA_INPUT_METHOD_ID, inputMethodId);
        }
        context.startActivity(intent, null);
    }

    /**
     * Returns a list of all {@code InputMethodInfo} on the device with more than one subtype. First
     * we initialize {@code InputMethodManager imm} with a handle to the system level service
     * INPUT_METHOD_SERVICE, and initialize {@code List<InputMethodInfo> result} with a new instance.
     * If {@code imm} is null we return {@code result}. Otherwise for all {@code InputMethodInfo imi}
     * in the list of {@code InputMethodInfo} returned by the {@code getEnabledInputMethodList} method
     * of {@code imm}  we add {@code imi} to {@code result}. When done we return {@code result} to the
     * caller.
     *
     * @return list of all {@code InputMethodInfo} with more than one subtype.
     */
    private List<InputMethodInfo> getEnabledInputMethodsThatHaveMultipleSubtypes() {
        final InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        final List<InputMethodInfo> result = new ArrayList<>();
        if (imm == null) {
            return result;
        }
        for (InputMethodInfo imi : imm.getEnabledInputMethodList()) {
            if (imi.getSubtypeCount() > 1) {
                result.add(imi);
            }
        }
        return result;
    }
}
