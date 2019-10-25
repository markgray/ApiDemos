package com.example.android.apis.app

import android.annotation.TargetApi
import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View.OnClickListener
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Example of various Intent flags to modify the activity stack.
 * This activity demonstrates how to build a backstack of activities into an array of Intent[]
 * and use it to replace the current backstack by using makeRestartActivityTask() to create
 * Intent[0] with the flags set to re-launch ApiDemos task in its base state, Intent[1] to
 * launch ApiDemos at the "Views" "com.example.android.apis.Path", Intent[2] to launch ApiDemos
 * at the "Views/Lists" "com.example.android.apis.Path" and requesting to reset the back stack
 * to these three "activities". It then launches these intents using startActivities() if the
 * FLAG_ACTIVITY_CLEAR_TASK button is pressed, or putting them into a PendingIntent which it
 * "later" calls using PendingIntent.send()
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class IntentActivityFlags : AppCompatActivity() {

    /**
     * OnClickListener for the R.id.flag_activity_clear_task ("FLAG_ACTIVITY_CLEAR_TASK") Button
     * We create an Intent[] array with three Intents and then start this set of activities
     * as a synthesized task stack.
     *
     * Parameter: v R.id.flag_activity_clear_task ("FLAG_ACTIVITY_CLEAR_TASK") Button which was clicked
     */
    private val mFlagActivityClearTaskListener = OnClickListener {
        startActivities(buildIntentsToViewsLists())
    }

    /**
     * OnClickListener for the R.id.flag_activity_clear_task_pi ("FLAG_ACTIVITY_CLEAR_TASK (PI)")
     * Button.
     * We create a PendingIntent using the same Intent[] array used for the "FLAG_ACTIVITY_CLEAR_TASK"
     * Button, and then perform the operation associated with this PendingIntent. This causes the
     * Intent[2] to be started again with the Intent[] array used as a synthesized task stack.
     *
     * Parameter: v R.id.flag_activity_clear_task_pi ("FLAG_ACTIVITY_CLEAR_TASK (PI)") Button which
     * was clicked
     */
    private val mFlagActivityClearTaskPIListener = OnClickListener {
        val context = this@IntentActivityFlags

        val pi = PendingIntent.getActivities(context, 0,
                buildIntentsToViewsLists(), PendingIntent.FLAG_UPDATE_CURRENT)

        try {
            pi.send()
        } catch (e: CanceledException) {
            Log.w("IntentActivityFlags", "Failed sending PendingIntent", e)
        }
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.intent_activity_flags.
     * Next we locate the Button R.id.flag_activity_clear_task ("FLAG_ACTIVITY_CLEAR_TASK") and set
     * its OnClickListener to mFlagActivityClearTaskListener, and finally we locate the Button
     * R.id.flag_activity_clear_task_pi ("FLAG_ACTIVITY_CLEAR_TASK (PI)") and set its OnClickListener
     * to mFlagActivityClearTaskPIListener.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.intent_activity_flags)

        // Watch for button clicks.
        var button = findViewById<Button>(R.id.flag_activity_clear_task)
        button.setOnClickListener(mFlagActivityClearTaskListener)
        button = findViewById(R.id.flag_activity_clear_task_pi)
        button.setOnClickListener(mFlagActivityClearTaskPIListener)
    }

    /**
     * This creates an array of Intent objects representing the back stack for a user going into
     * the Views/Lists API demos. First we create an array of three Intents[] intents:
     *
     *  - `intents[0]` is an Intent that can be used to re-launch an application's task in its base
     * state using a component identifier for com.example.android.apis.ApiDemos as
     * the activity component that is the root of the task.
     *  - `intents[1]` is an Intent with the component name of com.example.android.apis.ApiDemos and
     * with the added extended date "Views" stored as "com.example.android.apis.Path"
     * (ApiDemos fetches this value to determine how to populate the List it displays)
     *  - `intents[2]` is an Intent with the component name of com.example.android.apis.ApiDemos and
     * with the added extended date "Views/Lists" stored as "com.example.android.apis.Path"
     * (ApiDemos fetches this value to determine how to populate the List it displays)
     *
     * Finally we return the Intent[] intents to the caller.
     *
     * @return an array of Intent's
     */
    private fun buildIntentsToViewsLists(): Array<Intent?> {
        // We are going to rebuild our task with a new back stack.  This will
        // be done by launching an array of Intents, representing the new
        // back stack to be created, with the first entry holding the root
        // and requesting to reset the back stack.
        val intents = arrayOfNulls<Intent>(3)

        // First: root activity of ApiDemos.
        // This is a convenient way to make the proper Intent to launch and
        // reset an application's task.
        intents[0] = Intent.makeRestartActivityTask(ComponentName(this,
                com.example.android.apis.ApiDemos::class.java))

        var intent = Intent(Intent.ACTION_MAIN)
        intent.setClass(this@IntentActivityFlags, com.example.android.apis.ApiDemos::class.java)
        intent.putExtra("com.example.android.apis.Path", "Views")
        intents[1] = intent

        intent = Intent(Intent.ACTION_MAIN)
        intent.setClass(this@IntentActivityFlags, com.example.android.apis.ApiDemos::class.java)
        intent.putExtra("com.example.android.apis.Path", "Views/Lists")

        intents[2] = intent
        return intents
    }
}
