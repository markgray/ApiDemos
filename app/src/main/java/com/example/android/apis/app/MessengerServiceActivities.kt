package com.example.android.apis.app

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.example.android.apis.R

@Suppress("MemberVisibilityCanBePrivate")
@SuppressLint("SetTextI18n")
class MessengerServiceActivities {
    /**
     * This is an example of implementing an application service that uses the [Messenger] class for
     * communicating with clients. This allows for remote interaction with a service, without
     * needing to define an AIDL interface. Uses [MessengerService] as the service we bind to.
     */
    class Binding : AppCompatActivity() {
        /**
         * [Messenger] for communicating with service.
         */
        var mService: Messenger? = null
        /**
         * Flag indicating whether we have called bind on the service.
         */
        var mIsBound = false
        /**
         * The [TextView] we are using to show state information.
         */
        var mCallbackText: TextView? = null

        /**
         * [Handler] of incoming messages from service, used in constructing [Messenger] field
         * [mMessenger] thereby becoming the [Handler] to any messages sent to [mMessenger].
         * [mMessenger] is then used as the `replyTo` field of any [Message] sent to [mService]
         * that we create from the [IBinder] `service` we receive in the `onServiceConnected`
         * callback after binding to [MessengerService].
         *
         * [MessengerService] maintains a list of attached clients in its `ArrayList<Messenger>`
         * field `mClients` which it fills from the `replyTo` field in its own [IncomingHandler]
         * override of `handleMessage` when it receives a [Message] with the `what` field set to
         * MSG_REGISTER_CLIENT. Clients are removed from this list when it receives a [Message]
         * with the `what` field set to MSG_UNREGISTER_CLIENT
         *
         * [MessengerService] uses its own [IncomingHandler] as the [Handler] for the [Messenger]
         * field [mMessenger] whose backing [IBinder] it returns from its `onBind(Intent intent)`
         * override, which is invoked when a client binds to its service.
         */
        @SuppressLint("HandlerLeak")
        internal inner class IncomingHandler : Handler() {
            /**
             * Subclasses must implement this to receive messages. We switch on the User-defined
             * message code contained in the `what` field used when the `Message msg`
             * was constructed by the [MessengerService] service, and if it is MSG_SET_VALUE
             * we set the text of [TextView] field [mCallbackText] to the value stored in the field
             * `arg1`, otherwise we pass `msg` on to our super's implementation of `handleMessage`.
             *
             * @param msg [Message] containing a description and arbitrary data object that can be
             * sent to a [Handler]
             */
            override fun handleMessage(msg: Message) {
                if (msg.what == MessengerService.MSG_SET_VALUE) {
                    mCallbackText!!.text = "Received from service: " + msg.arg1
                } else {
                    super.handleMessage(msg)
                }
            }
        }

        /**
         * Target we publish for clients to send messages to IncomingHandler.
         */
        val mMessenger = Messenger(IncomingHandler())
        /**
         * Class for interacting with the main interface of the service.
         */
        private val mConnection: ServiceConnection = object : ServiceConnection {
            /**
             * Called when a connection to the Service has been established, with the
             * [android.os.IBinder] of the communication channel to the Service.
             * In our case it is the [IBinder] backing the [Messenger] field `mMessenger`
             * in [MessengerService] which we will send our [Message] to.
             *
             * First we initialize our [Messenger] field [mService] using [IBinder] parameter
             * [service] as the [IBinder] that this [Messenger] should communicate with.
             * ([MessengerService] returns the [IBinder] backing its [Messenger] field `mMessenger`
             * from its `onBind` override which we receive and make use of here.) Then we set the
             * text of [TextView] field [mCallbackText] to "Attached".
             *
             * Next, wrapped in a try block intended to catch [RemoteException] we create a [Message]
             * for variable `var msg` using MSG_REGISTER_CLIENT as the `what` field, and set the
             * `replyTo` field to our [Messenger] field [mMessenger]. We then send this [Message]
             * to [MessengerService] using the [Messenger] field [mService] we created from the
             * [IBinder] returned from [MessengerService].
             *
             * We then  create a new [Message] for `msg` using MSG_SET_VALUE as the `what` field
             * and the `hashCode()` of *this* as an arbitrary value for the field `arg1` and send
             * that message to [MessengerService] (It will echo it back to all the clients connected
             * to it, and upon receiving it in our `handleMessage` override we will display the
             * value in [TextView] field [mCallbackText].
             *
             * Finally we toast the message "Connected to remote service".
             *
             * @param className The concrete component name of the service that has been connected.
             * It consists of the package (a [String]) it exists in, and the class (a String) name
             * inside of that package.
             * @param service   The [IBinder] of the Service's communication channel, which you
             * can now make calls on.
             */
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                mService = Messenger(service)
                mCallbackText!!.text = "Attached."
                /**
                 * We want to monitor the service for as long as we are
                 * connected to it.
                 */
                try {
                    var msg = Message.obtain(null, MessengerService.MSG_REGISTER_CLIENT)
                    msg.replyTo = mMessenger
                    mService!!.send(msg)
                    /**
                     * Give it some value as an example.
                     */
                    msg = Message.obtain(null, MessengerService.MSG_SET_VALUE, this.hashCode(), 0)
                    mService!!.send(msg)
                } catch (e: RemoteException) {
                    /**
                     * In this case the service has crashed before we could even
                     * do anything with it; we can count on soon being
                     * disconnected (and then reconnected if it can be restarted)
                     * so there is no need to do anything here.
                     */
                }
                // As part of the sample, tell the user what happened.
                Toast.makeText(this@Binding, R.string.remote_service_connected, Toast.LENGTH_SHORT).show()
            }

            /**
             * Called when a connection to the Service has been lost. We set our [Messenger] field
             * [mService] to *null*, set the text of [TextView] field [mCallbackText] to
             * "Disconnected.", and toast the message "Disconnected from remote service".
             *
             * @param className The concrete component name of the service whose connection has been
             * lost. It consists of the package (a [String]) it exists in, and the class (a [String])
             * name inside of that package.
             */
            override fun onServiceDisconnected(className: ComponentName) {
                /**
                 * This is called when the connection with the service has been
                 * unexpectedly disconnected -- that is, its process crashed.
                 */
                mService = null
                mCallbackText!!.text = "Disconnected."
                // As part of the sample, tell the user what happened.
                Toast.makeText(this@Binding, R.string.remote_service_disconnected, Toast.LENGTH_SHORT).show()
            }
        }

        /**
         * Establish a connection with the [MessengerService] service. We create an [Intent]
         * specifying `MessengerService.class` as the target and use it to connect to that service,
         * (creating it if needed), specifying [ServiceConnection] field [mConnection] to receive
         * information as the service is started and stopped in its callbacks `onServiceConnected`,
         * and `onServiceDisconnected`, and using BIND_AUTO_CREATE as the flag (automatically create
         * the service as long as the binding exists). We then set our flag field [mIsBound] to true,
         * and set the text of [TextView] field [mCallbackText] to "Binding."
         *
         * This is called in `OnClickListener` field [mBindListener] which is invoked when the
         * R.id.bind ("Bind Service") Button is clicked.
         */
        fun doBindService() {
            /**
             * Establish a connection with the service. We use an explicit
             * class name because there is no reason to be able to let other
             * applications replace our component.
             */
            bindService(Intent(this@Binding, MessengerService::class.java), mConnection, Context.BIND_AUTO_CREATE)
            mIsBound = true
            mCallbackText!!.text = "Binding."
        }

        /**
         * Disconnect from the [MessengerService] service. First we check our flag field [mIsBound]
         * to see if we have tried to connect yet, and if so we check to see if our `onServiceConnected`
         * callback has been called (that is where our communication channel [Messenger] field
         * [mService] is created from the [IBinder] `service` passed to it). It that is true, wrapped
         * in a try block intended to catch [RemoteException], we create [Message] variable `val msg`
         * with the `what` field set to MSG_UNREGISTER_CLIENT, set the `replyTo` to [Messenger] field
         * [mMessenger] and send the message to [MessengerService] using [mService]. We then disconnect
         * from [ServiceConnection] field [mConnection], set our [mIsBound] field flag to *false* and
         * set the text of [TextView] field [mCallbackText]` to "Unbinding."
         *
         * This is called in the `OnClickListener` field [mUnbindListener] which is invoked when the
         * R.id.unbind ("Unbind Service") Button is clicked.
         */
        fun doUnbindService() {
            if (mIsBound) {
                /**
                 * If we have received the service, and hence registered with
                 * it, then now is the time to unregister.
                 */
                if (mService != null) {
                    try {
                        val msg = Message.obtain(null, MessengerService.MSG_UNREGISTER_CLIENT)
                        msg.replyTo = mMessenger
                        mService!!.send(msg)
                    } catch (e: RemoteException) {
                        /**
                         * There is nothing special we need to do if the service has crashed.
                         */
                    }
                }
                /**
                 * Detach our existing connection.
                 */
                unbindService(mConnection)
                mIsBound = false
                mCallbackText!!.text = "Unbinding."
            }
        }

        /**
         * Called when the activity is starting. First we call through to our super's implementation
         * of `onCreate`, then we set our content view to our layout file
         * R.layout.messenger_service_binding. We locate the [Button] with ID R.id.bind ("Bind
         * Service") and set its `OnClickListener` to `OnClickListener` field [mBindListener]
         * (calls our method [doBindService]), locate the Button with ID R.id.unbind ("Unbind
         * Service") and set its `OnClickListener` to `OnClickListener` field [mUnbindListener]
         * (calls our method [doUnbindService], and finally locate the [TextView] with ID R.id.callback
         * to initialize our [TextView] field [mCallbackText], and set its text to "Not attached".
         *
         * @param savedInstanceState we do not override `onSaveInstanceState` so do not use
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.messenger_service_binding)
            // Watch for button clicks.
            var button = findViewById<Button>(R.id.bind)
            button.setOnClickListener(mBindListener)
            button = findViewById(R.id.unbind)
            button.setOnClickListener(mUnbindListener)
            mCallbackText = findViewById(R.id.callback)
            mCallbackText!!.text = "Not attached."
        }

        /**
         * `OnClickListener` for the R.id.bind ("Bind Service") Button, it calls our method
         * [doBindService] when clicked.
         */
        private val mBindListener = View.OnClickListener { doBindService() }
        /**
         * `OnClickListener` for the R.id.unbind ("Unbind Service") Button, it calls our method
         * [doUnbindService] when clicked.
         */
        private val mUnbindListener = View.OnClickListener { doUnbindService() }
    }
}