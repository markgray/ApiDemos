package com.example.android.apis.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.apis.R;

@SuppressLint("SetTextI18n")
public class MessengerServiceActivities {
    /**
     * This is an example of implementing an application service that uses the Messenger class for
     * communicating with clients. This allows for remote interaction with a service, without
     * needing to define an AIDL interface. Uses {@code MessengerService.java} as the service we
     * bind to.
     */
    public static class Binding extends Activity {

        /**
         * Messenger for communicating with service.
         */
        Messenger mService = null;
        /**
         * Flag indicating whether we have called bind on the service.
         */
        boolean mIsBound;
        /**
         * Some text view we are using to show state information.
         */
        TextView mCallbackText;

        /**
         * Handler of incoming messages from service, used in constructing {@code Messenger mMessenger}
         * thereby becoming the {@code Handler} to any messages sent to {@code mMessenger}.
         * {@code mMessenger} is then used as the {@code replyTo} field of any {@code Message} sent
         * to the {@code Messenger mService} we create from the {@code IBinder service} we receive
         * in the {@code onServiceConnected} callback after binding to {@code MessengerService}.
         * <p>
         * {@code MessengerService} maintains a list of attached clients in
         * {@code ArrayList<Messenger> mClients} which it fills from the {@code replyTo} field in
         * its own {@code IncomingHandler} override of {@code handleMessage} when it receives a
         * {@code Message} with the {@code what} field set to MSG_REGISTER_CLIENT. Clients are removed
         * from this list when it receives a {@code Message} with the {@code what} field set to
         * MSG_UNREGISTER_CLIENT
         * <p>
         * {@code MessengerService} uses its own {@code IncomingHandler} as the {@code Handler} for
         * the {@code Messenger mMessenger} whose backing IBinder it returns from its
         * {@code IBinder onBind(Intent intent)} override which is invoked when a client binds to
         * its service.
         */
        @SuppressLint("HandlerLeak")
        class IncomingHandler extends Handler {
            /**
             * Subclasses must implement this to receive messages. We switch on the User-defined
             * message code contained in the {@code what} field used when the {@code Message msg}
             * was constructed by the {@code MessengerService} service, and if it is MSG_SET_VALUE
             * we set the text of {@code TextView mCallbackText} to the value stored in the field
             * {@code arg1}, otherwise we pass {@code msg} on to our super's implementation of
             * {@code handleMessage}.
             *
             * @param msg message containing a description and arbitrary data object that can be sent
             *            to a Handler
             */
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MessengerService.MSG_SET_VALUE:
                        mCallbackText.setText("Received from service: " + msg.arg1);
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }

        /**
         * Target we publish for clients to send messages to IncomingHandler.
         */
        final Messenger mMessenger = new Messenger(new IncomingHandler());

        /**
         * Class for interacting with the main interface of the service.
         */
        private ServiceConnection mConnection = new ServiceConnection() {
            /**
             * Called when a connection to the Service has been established, with the
             * {@link android.os.IBinder} of the communication channel to the Service.
             * In our case it is the {@code IBinder} backing the {@code Messenger mMessenger}
             * in {@code MessengerService} which we will send our {@code Message} to.
             * <p>
             * First we initialize our field {@code Messenger mService} using {@code IBinder service}
             * as the {@code IBinder} that this {@code Messenger} should communicate with.
             * ({@code MessengerService} returns the {@code IBinder} backing its
             * {@code Messenger mMessenger} from its {@code onBind} override which we receive and
             * make use of here.) Then we set the text of {@code TextView mCallbackText} to "Attached".
             * <p>
             * Next, wrapped in a try block intended to catch RemoteException we create {@code Message msg}
             * using MSG_REGISTER_CLIENT as the {@code what} field, and set the {@code replyTo} field
             * to {@code Messenger mMessenger}. We then send this {@code Message} to {@code MessengerService}
             * using the {@code Messenger mService} we created from the {@code IBinder} returned from
             * {@code MessengerService}.
             * <p>
             * We then  create a new {@code Message msg} using MSG_SET_VALUE as the {@code what} field
             * and the {@code hashCode()} of this as an arbitrary value for the field {@code arg1}
             * and send that message to {@code MessengerService} (It will echo it back to all the
             * clients connected to it, and upon receiving it in {@code handleMessage} we will display
             * the value in {@code TextView mCallbackText}.
             * <p>
             * Finally we toast the message "Connected to remote service".
             *
             * @param className The concrete component name of the service that has been connected.
             *                  It consists of the package (a String) it exists in, and the class
             *                  (a String) name inside of that package.
             * @param service   The IBinder of the Service's communication channel,
             *                  which you can now make calls on.
             */
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {

                mService = new Messenger(service);
                mCallbackText.setText("Attached.");

                // We want to monitor the service for as long as we are
                // connected to it.
                try {
                    Message msg = Message.obtain(null, MessengerService.MSG_REGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);

                    // Give it some value as an example.
                    msg = Message.obtain(null, MessengerService.MSG_SET_VALUE, this.hashCode(), 0);
                    mService.send(msg);
                } catch (RemoteException e) {
                    // In this case the service has crashed before we could even
                    // do anything with it; we can count on soon being
                    // disconnected (and then reconnected if it can be restarted)
                    // so there is no need to do anything here.
                }

                // As part of the sample, tell the user what happened.
                Toast.makeText(Binding.this, R.string.remote_service_connected, Toast.LENGTH_SHORT).show();
            }

            /**
             * Called when a connection to the Service has been lost. We set our field {@code Messenger mService}
             * to null, set the text of {@code TextView mCallbackText} to "Disconnected.", and toast the
             * message "Disconnected from remote service".
             *
             * @param className The concrete component name of the service whose connection has been
             *                  lost. It consists of the package (a String) it exists in, and the class
             *                  (a String) name inside of that package.
             */
            @Override
            public void onServiceDisconnected(ComponentName className) {
                // This is called when the connection with the service has been
                // unexpectedly disconnected -- that is, its process crashed.
                mService = null;
                mCallbackText.setText("Disconnected.");

                // As part of the sample, tell the user what happened.
                Toast.makeText(Binding.this, R.string.remote_service_disconnected, Toast.LENGTH_SHORT).show();
            }
        };

        /**
         * Establish a connection with the {@code MessengerService} service. We create an {@code Intent}
         * specifying {@code MessengerService.class} as the target and use it to connect to that service,
         * (creating it if needed), specifying {@code ServiceConnection mConnection} to receive information
         * as the service is started and stopped in its callbacks {@code onServiceConnected}, and
         * {@code onServiceDisconnected}, and using BIND_AUTO_CREATE as the flag (automatically create
         * the service as long as the binding exists). We then set our flag {@code mIsBound} to true,
         * and set the text of {@code TextView mCallbackText} to "Binding."
         * <p>
         * This is called in {@code OnClickListener mBindListener} which is invoked when the R.id.bind
         * ("Bind Service") Button is clicked.
         */
        void doBindService() {
            // Establish a connection with the service.  We use an explicit
            // class name because there is no reason to be able to let other
            // applications replace our component.
            bindService(new Intent(Binding.this, MessengerService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
            mCallbackText.setText("Binding.");
        }

        void doUnbindService() {
            if (mIsBound) {
                // If we have received the service, and hence registered with
                // it, then now is the time to unregister.
                if (mService != null) {
                    try {
                        Message msg = Message.obtain(null, MessengerService.MSG_UNREGISTER_CLIENT);
                        msg.replyTo = mMessenger;
                        mService.send(msg);
                    } catch (RemoteException e) {
                        // There is nothing special we need to do if the service
                        // has crashed.
                    }
                }

                // Detach our existing connection.
                unbindService(mConnection);
                mIsBound = false;
                mCallbackText.setText("Unbinding.");
            }
        }


        /**
         * Standard initialization of this activity.  Set up the UI, then wait
         * for the user to poke it before doing anything.
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.messenger_service_binding);

            // Watch for button clicks.
            Button button = (Button) findViewById(R.id.bind);
            button.setOnClickListener(mBindListener);
            button = (Button) findViewById(R.id.unbind);
            button.setOnClickListener(mUnbindListener);

            mCallbackText = (TextView) findViewById(R.id.callback);
            mCallbackText.setText("Not attached.");
        }

        private OnClickListener mBindListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                doBindService();
            }
        };

        private OnClickListener mUnbindListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                doUnbindService();
            }
        };
    }
}
