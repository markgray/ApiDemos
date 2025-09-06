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
package com.example.android.apis.inputmethod

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.graphics.Utilities.id2p
import com.example.android.apis.inputmethod.ShowInputMethodAndSubtypeEnabler.Companion.showInputMethodAndSubtypeEnabler

/**
 * Demonstrates how to show the input method subtype enabler without relying on
 * [InputMethodManager.showInputMethodAndSubtypeEnabler], which is highly likely to be
 * broken.
 */
@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class ShowInputMethodAndSubtypeEnabler : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`.
     * Then we initialize [LinearLayout] variable `val layout` with a new instance and set its
     * orientation to VERTICAL. We initialize [Button] variable `val button` with a new instance,
     * set its text to the string "Show (All IMEs)", set its `OnClickListener` to a lambda which
     * calls our method [showInputMethodAndSubtypeEnabler] with null as the input method id, and
     * then add `button` to `layout`.
     *
     * Next we loop over the list of [InputMethodInfo] `imi` that is returned by our method
     * `getEnabledInputMethodsThatHaveMultipleSubtypes` (kotlin prefers we call it our property
     * [enabledInputMethodsThatHaveMultipleSubtypes] which is silly of course), initializing
     * [Button] variable `val button` with a new instance, initializing [String] variable `val id`
     * with the id of `imi`, setting the text of `button` to the string formed by concatenating the
     * string "Show (" followed by `id` followed by the string ")", set its `OnClickListener` to a
     * lambda which calls our method [showInputMethodAndSubtypeEnabler] with `id` as the input
     * method id, and then add `button` to `layout` before looping around for the next
     * [InputMethodInfo].
     *
     * When done building `layout` we set it as our content view.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(0, id2p(160), 0, id2p(60))
        run {
            val button = Button(this)
            button.text = "Show (All IMEs)"
            button.setOnClickListener { v: View? ->
                showInputMethodAndSubtypeEnabler(
                    this@ShowInputMethodAndSubtypeEnabler,
                    null
                )
            }
            layout.addView(button)
        }
        for (imi in enabledInputMethodsThatHaveMultipleSubtypes) {
            val button = Button(this)
            val id = imi.id
            button.text = "Show ($id)"
            button.setOnClickListener { v: View? ->
                showInputMethodAndSubtypeEnabler(
                    this@ShowInputMethodAndSubtypeEnabler,
                    id
                )
            }
            layout.addView(button)
        }
        setContentView(layout)
    }

    /**
     * Returns a list of all [InputMethodInfo] on the device with more than one subtype. First
     * we initialize [InputMethodManager] variable `val imm` with a handle to the system level
     * service INPUT_METHOD_SERVICE, and initialize `List<InputMethodInfo>` variable `val result`
     * with a new instance. If `imm` is null we return `result`. Otherwise for all [InputMethodInfo]
     * `imi` in the list of [InputMethodInfo] returned by the `getEnabledInputMethodList` method
     * of `imm` (kotlin prefers to call this the `enabledInputMethodList` property)  we add `imi`
     * to `result`. When done we return `result` to the caller.
     *
     * @return list of all [InputMethodInfo] with more than one subtype.
     */
    @Suppress("SENSELESS_COMPARISON")
    private val enabledInputMethodsThatHaveMultipleSubtypes: List<InputMethodInfo>
        get() {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            val result: MutableList<InputMethodInfo> = ArrayList()
            if (imm == null) {
                return result
            }
            for (imi in imm.enabledInputMethodList) {
                if (imi.subtypeCount > 1) {
                    result.add(imi)
                }
            }
            return result
        }

    companion object {
        /**
         * Called to show the input method and subtype enabler [Settings] activity for a specific
         * input method id (or all methods if our [String] parameter [inputMethodId] is null or
         * empty). First we initialize [Intent] variable `val intent` with a new instance whose
         * action is specified to be ACTION_INPUT_METHOD_SUBTYPE_SETTINGS (show settings to
         * enable/disable input method subtypes), then set its flags: FLAG_ACTIVITY_NEW_TASK (this
         * activity will become the start of a new task on this history stack),
         * FLAG_ACTIVITY_RESET_TASK_IF_NEEDED (it will be launched as the front door of the task.
         * This will result in the application of any affinities needed to have that task in the
         * proper state (either moving activities to or from it), or simply resetting that task to
         * its initial state if needed.), and FLAG_ACTIVITY_CLEAR_TOP (instead of launching a new
         * instance of the activity, all of the other activities on top of it will be closed and
         * this [Intent] will be delivered to the (now on top) old activity as a new [Intent]).
         *
         * If our [String] parameter [inputMethodId] is not null or empty, we add it as an extra to
         * `intent` under the key EXTRA_INPUT_METHOD_ID, then start the activity of `intent`
         * with null options.
         *
         * @param context [Context] to use to access the `startActivity` method.
         * @param inputMethodId input method subtype to be displayed in the settings when showing
         * the settings to enable/disable input method subtypes. It is stored as an extra under the
         * key EXTRA_INPUT_METHOD_ID in the intent used to launch the ACTION_INPUT_METHOD_SUBTYPE_SETTINGS
         * action.
         */
        fun showInputMethodAndSubtypeEnabler(context: Context, inputMethodId: String?) {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS)
            intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK
                or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (!TextUtils.isEmpty(inputMethodId)) {
                intent.putExtra(Settings.EXTRA_INPUT_METHOD_ID, inputMethodId)
            }
            context.startActivity(intent, null)
        }
    }
}