/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.android.apis.accessibility

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.CHECKED_STATE_TRUE
import com.example.android.apis.R
import java.util.Locale

/**
 * This class demonstrates how an accessibility service can query
 * window content to improve the feedback given to the user.
 */
class TaskBackService : AccessibilityService(), OnInitListener {

    /** Flag whether Text-To-Speech is initialized.  */
    private var mTextToSpeechInitialized: Boolean = false

    /** Handle to the Text-To-Speech engine.  */
    private var mTts: TextToSpeech? = null

    /**
     * {@inheritDoc}
     */
    public override fun onServiceConnected() {
        // Initializes the Text-To-Speech engine as soon as the service is connected.
        mTts = TextToSpeech(applicationContext, this)
    }

    /**
     * Processes an AccessibilityEvent, by traversing the View's tree and
     * putting together a message to speak to the user.
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!mTextToSpeechInitialized) {
            Log.e(LOG_TAG, "Text-To-Speech engine not ready.  Bailing out.")
            return
        }

        // This AccessibilityNodeInfo represents the view that fired the
        // AccessibilityEvent. The following code will use it to traverse the
        // view hierarchy, using this node as a starting point.
        //
        // NOTE: Every method that returns an AccessibilityNodeInfo may return null,
        // because the explored window is in another process and the
        // corresponding View might be gone by the time your request reaches the
        // view hierarchy.
        val source = event.source ?: return

        // Grab the parent of the view that fired the event.
        val rowNode = getListItemNodeInfo(source) ?: return

        // Using this parent, get references to both child nodes, the label and the checkbox.
        val labelNode = rowNode.getChild(0)
        if (labelNode == null) {
            @Suppress("DEPRECATION") // Object pooling has been discontinued. Calling this function now will have no effect.
            rowNode.recycle()
            return
        }

        val completeNode = rowNode.getChild(1)
        if (completeNode == null) {
            @Suppress("DEPRECATION") // Object pooling has been discontinued. Calling this function now will have no effect.
            rowNode.recycle()
            return
        }

        // Determine what the task is and whether or not it's complete, based on
        // the text inside the label, and the state of the check-box.
        if (rowNode.childCount < 2 || !rowNode.getChild(1).isCheckable) {
            @Suppress("DEPRECATION") // Object pooling has been discontinued. Calling this function now will have no effect.
            rowNode.recycle()
            return
        }

        val taskLabel = labelNode.text
        val isComplete = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            completeNode.checked == CHECKED_STATE_TRUE
        } else {
            @Suppress("DEPRECATION") // Needed for older than BAKLAVA
            completeNode.isChecked
        }

        val completeStr: String = if (isComplete) {
            getString(R.string.task_complete)
        } else {
            getString(R.string.task_not_complete)
        }

        val taskStr = getString(R.string.task_complete_template, taskLabel, completeStr)
        val utterance = StringBuilder(taskStr)

        // The custom ListView added extra context to the event by adding an
        // AccessibilityRecord to it. Extract that from the event and read it.
        val records = event.recordCount
        for (i in 0 until records) {
            val record = event.getRecord(i)
            val contentDescription = record.contentDescription
            if (!TextUtils.isEmpty(contentDescription)) {
                utterance.append(SEPARATOR)
                utterance.append(contentDescription)
            }
        }

        // Announce the utterance.
        @SuppressLint("ObsoleteSdkInt")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTts!!.speak(utterance.toString(), TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            @Suppress("DEPRECATION")
            mTts!!.speak(utterance.toString(), TextToSpeech.QUEUE_FLUSH, null)
        }
        Log.d(LOG_TAG, utterance.toString())
    }

    private fun getListItemNodeInfo(source: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current = source
        while (true) {
            val parent = current.parent ?: return null
            if (TASK_LIST_VIEW_CLASS_NAME.contentEquals(parent.className)) {
                return current
            }
            // NOTE: Recycle the infos.
            val oldCurrent = current
            current = parent
            @Suppress("DEPRECATION") // Object pooling has been discontinued. Calling this function now will have no effect.
            oldCurrent.recycle()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onInterrupt() {
        /* do nothing */
    }

    /**
     * {@inheritDoc}
     */
    override fun onInit(status: Int) {
        // Set a flag so that the TaskBackService knows that the Text-To-Speech
        // engine has been initialized, and can now handle speaking requests.
        if (status == TextToSpeech.SUCCESS) {
            mTts!!.language = Locale.US
            mTextToSpeechInitialized = true
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onDestroy() {
        super.onDestroy()
        if (mTextToSpeechInitialized) {
            mTts!!.shutdown()
        }
    }

    companion object {

        /** Tag for logging.  */
        private const val LOG_TAG = "TaskBackService"

        /** Comma separator.  */
        private const val SEPARATOR = ", "

        /** The class name of TaskListView - for simplicity we speak only its items.  */
        private const val TASK_LIST_VIEW_CLASS_NAME =
            "com.example.android.apis.accessibility.TaskListView"
    }
}
