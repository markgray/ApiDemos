package com.example.android.apis.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Controller activity for the IsolatedService and IsolatedService2 isolated services
 */
@Suppress("MemberVisibilityCanBePrivate")
@SuppressLint("SetTextI18n")
class IsolatedServiceController : AppCompatActivity() {
    /**
     * Static inner class used to hold and manipulate our two services IsolateService and
     * IsolatedService2
     */
    class ServiceInfo(// "this" Activity in our call from onCreate of Controller Activity
            val mActivity: Activity, // Class of the service to be controlled
            val mClz: Class<*>, start: Int, stop: Int, bind: Int, status: Int) {
        lateinit var mStatus // TextView to display status in
                : TextView
        var mServiceBound: Boolean = false // boolean flag for whether we are bound to or not = false
        var mService // Defined in IRemoteService.aidl
                : IRemoteService? = null

        /**
         * Called from the `onDestroy` callback of the `Controller` activity. If we
         * are currently bound to our service, we disconnect from it.
         */
        fun destroy() {
            if (mServiceBound) {
                mActivity.unbindService(mConnection)
            }
        }

        /**
         * `OnClickListener` for the Button with resource ID `start`, we create an
         * `Intent` to start the service `Class<?> mClz` and start it when the Button
         * is clicked.
         */
        private val mStartListener = View.OnClickListener { mActivity.startService(Intent(mActivity, mClz)) }
        /**
         * `OnClickListener` for the Button with resource ID `stop`, we create an
         * `Intent` to stop the service `Class<?> mClz` and stop it when the Button
         * is clicked.
         */
        private val mStopListener = View.OnClickListener { mActivity.stopService(Intent(mActivity, mClz)) }
        /**
         * `OnClickListener` for the `CheckBox` with resource ID `bind`. When
         * we are clicked, we check to see if our `CheckBox` is checked and if so we check
         * to make sure we are not already bound (`mServiceBound` is true), and if not
         * we bind to the service `Class<?> mClz` using `ServiceConnection mConnection`
         * as the `ServiceConnection` object, set our flag `mServiceBound` to true,
         * and set the text of `TextView mStatus` to "BOUND". If it is not checked and we
         * are bound to the service, we disconnect from the service, set our flag `mServiceBound`
         * to false and set the text of `mStatus` to the empty String.
         */
        private val mBindListener = View.OnClickListener { v ->
            if ((v as CheckBox).isChecked) {
                if (!mServiceBound) {
                    if (mActivity.bindService(Intent(mActivity, mClz), mConnection, Context.BIND_AUTO_CREATE)) {
                        mServiceBound = true
                        mStatus.text = "BOUND"
                    }
                }
            } else {
                if (mServiceBound) {
                    mActivity.unbindService(mConnection)
                    mServiceBound = false
                    mStatus.text = ""
                }
            }
        }
        /**
         * `ServiceConnection` created and bound to when the "bind" `CheckBox` gets checked,
         * and disconnected from when it gets unchecked.
         */
        private val mConnection: ServiceConnection = object : ServiceConnection {
            /**
             * Called when a connection to the Service has been established, with
             * the [android.os.IBinder] of the communication channel to the
             * Service. Called on the main thread.
             *
             * We initialize our field `IRemoteService mService` from
             * the `IBinder service` passed us, If our `mServiceBound` flag is
             * true we set the text of our `TextView mStatus` to "CONNECTED".
             *
             * @param className The concrete component name of the service that has
             * been connected.
             * @param service The IBinder of the Service's communication channel,
             * which you can now make calls on.
             */
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                mService = IRemoteService.Stub.asInterface(service)
                if (mServiceBound) {
                    mStatus.text = "CONNECTED"
                }
            }

            /**
             * Called when a connection to the Service has been lost.  This typically
             * happens when the process hosting the service has crashed or been killed.
             * This does *not* remove the ServiceConnection itself -- this
             * binding to the service will remain active, and you will receive a call
             * to [.onServiceConnected] when the Service is next running.
             *
             * We set our field `IRemoteService mService` to null, and if our flag
             * `mServiceBound` is true we set the text of our `TextView mStatus`
             * to "DISCONNECTED".
             *
             * @param className The concrete component name of the service whose
             * connection has been lost.
             */
            override fun onServiceDisconnected(className: ComponentName) { // This is called when the connection with the service has been
// unexpectedly disconnected -- that is, its process crashed.
                mService = null
                if (mServiceBound) {
                    mStatus.text = "DISCONNECTED"
                }
            }
        }

        /**
         * This constructor initializes fields needed to start the `Class<?> clz` service,
         * sets the `OnClickListener` of Buttons used to control the service, and saves a
         * reference to the `TextView` to use to display the status of the service.
         *
         * First we initialize our field `Activity mActivity` to our parameter `Activity activity`,
         * and our field `Class<?> mClz` to our parameter `Class<?> clz`. Then we locate
         * the `Button` with resource ID `start` and set its `OnClickListener` to
         * `OnClickListener mStartListener`, locate the `Button` with resource ID `stop`
         * and set its `OnClickListener` to `OnClickListener mStopListener`, and locate
         * the `CheckBox` with resource ID `bind` and set its `OnClickListener` to
         * `OnClickListener mBindListener`. Finally we initialize our field `TextView mStatus`
         * by locating the `TextView` with resource ID `status`.
         *
         * Parameter: activity "this" when called from `onCreate` of `Controller` Activity
         * Parameter: clz class of the service, either `IsolatedService.class` or `IsolatedService2.class`
         * Parameter: start resource ID for the start Button for the service
         * Parameter: stop resource ID for the stop Button for the service
         * Parameter: bind resource ID for the bind Button for the service
         * Parameter: status resource ID for the status TextView for the service
         */
        init {
            var button = mActivity.findViewById<Button>(start)
            button.setOnClickListener(mStartListener)
            button = mActivity.findViewById(stop)
            button.setOnClickListener(mStopListener)
            val cb = mActivity.findViewById<CheckBox>(bind)
            cb.setOnClickListener(mBindListener)
            mStatus = mActivity.findViewById(status)
        }
    }

    /**
     * Holds the service information for our service `IsolatedService`
     */
    var mService1: ServiceInfo? = null
    /**
     * Holds the service information for our service `IsolatedService2`
     */
    var mService2: ServiceInfo? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation
     * of `onCreate`, then we set our content view to our layout file
     * R.layout.isolated_service_controller. We create `ServiceInfo` instances to initialize
     * our fields `ServiceInfo mService1`, and `ServiceInfo mService2`. The constructor
     * not only initializes fields, it also sets the `OnClickListener` for the various widgets
     * in our UI based on the resource IDs passed it.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.isolated_service_controller)
        mService1 = ServiceInfo(this, IsolatedService::class.java, R.id.start1, R.id.stop1, R.id.bind1, R.id.status1)
        mService2 = ServiceInfo(this, IsolatedService2::class.java, R.id.start2, R.id.stop2, R.id.bind2, R.id.status2)
    }

    /**
     * Perform any final cleanup before an activity is destroyed.  This can
     * happen either because the activity is finishing (someone called
     * [.finish] on it, or because the system is temporarily destroying
     * this instance of the activity to save space.  You can distinguish
     * between these two scenarios with the [.isFinishing] method.
     *
     * First we call through to our super's implementation of `onDestroy`, then we instruct
     * the services referenced by `ServiceInfo mService1` and `ServiceInfo mService2`
     * to unbind from their service if they are currently bound to it.
     */
    override fun onDestroy() {
        super.onDestroy()
        mService1!!.destroy()
        mService2!!.destroy()
    }
}