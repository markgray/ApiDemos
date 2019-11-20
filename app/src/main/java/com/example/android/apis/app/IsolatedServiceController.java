package com.example.android.apis.app;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.android.apis.R;

@SuppressLint("SetTextI18n")
public class IsolatedServiceController extends AppCompatActivity {
    /**
     * Static inner class used to hold and manipulate our two services IsolateService and
     * IsolatedService2
     */
    @SuppressWarnings("FieldCanBeLocal")
    static class ServiceInfo {
        final Activity mActivity; // "this" Activity in our call from onCreate of Controller Activity
        final Class<?> mClz; // Class of the service to be controlled
        final TextView mStatus; // TextView to display status in
        boolean mServiceBound; // boolean flag for whether we are bound to or not
        IRemoteService mService; // Defined in IRemoteService.aidl

        /**
         * This constructor initializes fields needed to start the {@code Class<?> clz} service,
         * sets the {@code OnClickListener} of Buttons used to control the service, and saves a
         * reference to the {@code TextView} to use to display the status of the service.
         *
         * First we initialize our field {@code Activity mActivity} to our parameter {@code Activity activity},
         * and our field {@code Class<?> mClz} to our parameter {@code Class<?> clz}. Then we locate
         * the {@code Button} with resource ID {@code start} and set its {@code OnClickListener} to
         * {@code OnClickListener mStartListener}, locate the {@code Button} with resource ID {@code stop}
         * and set its {@code OnClickListener} to {@code OnClickListener mStopListener}, and locate
         * the {@code CheckBox} with resource ID {@code bind} and set its {@code OnClickListener} to
         * {@code OnClickListener mBindListener}. Finally we initialize our field {@code TextView mStatus}
         * by locating the {@code TextView} with resource ID {@code status}.
         *
         * @param activity "this" when called from {@code onCreate} of {@code Controller} Activity
         * @param clz class of the service, either {@code IsolatedService.class} or {@code IsolatedService2.class}
         * @param start resource ID for the start Button for the service
         * @param stop resource ID for the stop Button for the service
         * @param bind resource ID for the bind Button for the service
         * @param status resource ID for the status TextView for the service
         */
        ServiceInfo(Activity activity, Class<?> clz, int start, int stop, int bind, int status) {
            mActivity = activity;
            mClz = clz;
            Button button = mActivity.findViewById(start);
            button.setOnClickListener(mStartListener);
            button = mActivity.findViewById(stop);
            button.setOnClickListener(mStopListener);
            CheckBox cb = mActivity.findViewById(bind);
            cb.setOnClickListener(mBindListener);
            mStatus = mActivity.findViewById(status);
        }

        /**
         * Called from the {@code onDestroy} callback of the {@code Controller} activity. If we
         * are currently bound to our service, we disconnect from it.
         */
        void destroy() {
            if (mServiceBound) {
                mActivity.unbindService(mConnection);
            }
        }

        /**
         * {@code OnClickListener} for the Button with resource ID {@code start}, we create an
         * {@code Intent} to start the service {@code Class<?> mClz} and start it when the Button
         * is clicked.
         */
        private View.OnClickListener mStartListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.startService(new Intent(mActivity, mClz));
            }
        };

        /**
         * {@code OnClickListener} for the Button with resource ID {@code stop}, we create an
         * {@code Intent} to stop the service {@code Class<?> mClz} and stop it when the Button
         * is clicked.
         */
        private View.OnClickListener mStopListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.stopService(new Intent(mActivity, mClz));
            }
        };

        /**
         * {@code OnClickListener} for the {@code CheckBox} with resource ID {@code bind}. When
         * we are clicked, we check to see if our {@code CheckBox} is checked and if so we check
         * to make sure we are not already bound ({@code mServiceBound} is true), and if not
         * we bind to the service {@code Class<?> mClz} using {@code ServiceConnection mConnection}
         * as the {@code ServiceConnection} object, set our flag {@code mServiceBound} to true,
         * and set the text of {@code TextView mStatus} to "BOUND". If it is not checked and we
         * are bound to the service, we disconnect from the service, set our flag {@code mServiceBound}
         * to false and set the text of {@code mStatus} to the empty String.
         */
        private View.OnClickListener mBindListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    if (!mServiceBound) {
                        if (mActivity.bindService(new Intent(mActivity, mClz), mConnection, Context.BIND_AUTO_CREATE)) {
                            mServiceBound = true;
                            mStatus.setText("BOUND");
                        }
                    }
                } else {
                    if (mServiceBound) {
                        mActivity.unbindService(mConnection);
                        mServiceBound = false;
                        mStatus.setText("");
                    }
                }
            }
        };

        /**
         * {@code ServiceConnection} created and bound to when the "bind" {@code CheckBox} gets checked,
         * and disconnected from when it gets unchecked.
         */
        private ServiceConnection mConnection = new ServiceConnection() {
            /**
             * Called when a connection to the Service has been established, with
             * the {@link android.os.IBinder} of the communication channel to the
             * Service. Called on the main thread.
             *
             * We initialize our field {@code IRemoteService mService} from
             * the {@code IBinder service} passed us, If our {@code mServiceBound} flag is
             * true we set the text of our {@code TextView mStatus} to "CONNECTED".
             *
             * @param className The concrete component name of the service that has
             * been connected.
             * @param service The IBinder of the Service's communication channel,
             * which you can now make calls on.
             */
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                mService = IRemoteService.Stub.asInterface(service);
                if (mServiceBound) {
                    mStatus.setText("CONNECTED");
                }
            }

            /**
             * Called when a connection to the Service has been lost.  This typically
             * happens when the process hosting the service has crashed or been killed.
             * This does <em>not</em> remove the ServiceConnection itself -- this
             * binding to the service will remain active, and you will receive a call
             * to {@link #onServiceConnected} when the Service is next running.
             *
             * We set our field {@code IRemoteService mService} to null, and if our flag
             * {@code mServiceBound} is true we set the text of our {@code TextView mStatus}
             * to "DISCONNECTED".
             *
             * @param className The concrete component name of the service whose
             * connection has been lost.
             */
            @Override
            public void onServiceDisconnected(ComponentName className) {
                // This is called when the connection with the service has been
                // unexpectedly disconnected -- that is, its process crashed.
                mService = null;
                if (mServiceBound) {
                    mStatus.setText("DISCONNECTED");
                }
            }
        };
    }

    /**
     * Holds the service information for our service {@code IsolatedService}
     */
    ServiceInfo mService1;
    /**
     * Holds the service information for our service {@code IsolatedService2}
     */
    ServiceInfo mService2;

    /**
     * Called when the activity is starting. First we call through to our super's implementation
     * of {@code onCreate}, then we set our content view to our layout file
     * R.layout.isolated_service_controller. We create {@code ServiceInfo} instances to initialize
     * our fields {@code ServiceInfo mService1}, and {@code ServiceInfo mService2}. The constructor
     * not only initializes fields, it also sets the {@code OnClickListener} for the various widgets
     * in our UI based on the resource IDs passed it.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.isolated_service_controller);

        mService1 = new ServiceInfo(this, IsolatedService.class, R.id.start1, R.id.stop1, R.id.bind1, R.id.status1);
        mService2 = new ServiceInfo(this, IsolatedService2.class, R.id.start2, R.id.stop2, R.id.bind2, R.id.status2);
    }

    /**
     * Perform any final cleanup before an activity is destroyed.  This can
     * happen either because the activity is finishing (someone called
     * {@link #finish} on it, or because the system is temporarily destroying
     * this instance of the activity to save space.  You can distinguish
     * between these two scenarios with the {@link #isFinishing} method.
     *
     * First we call through to our super's implementation of {@code onDestroy}, then we instruct
     * the services referenced by {@code ServiceInfo mService1} and {@code ServiceInfo mService2}
     * to unbind from their service if they are currently bound to it.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService1.destroy();
        mService2.destroy();
    }
}
