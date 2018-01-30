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

package com.example.android.apis.accessibility;

import com.example.android.apis.R;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.SparseArray;
import android.view.accessibility.AccessibilityEvent;

import java.util.List;

/**
 * This class is an {@code AccessibilityService} that provides custom feedback
 * for the Clock application that comes by default with Android devices. It
 * demonstrates the following key features of the Android accessibility APIs:
 * <ol>
 * <li>
 * Simple demonstration of how to use the accessibility APIs.
 * </li>
 * <li>
 * Hands-on example of various ways to utilize the accessibility API for
 * providing alternative and complementary feedback.
 * </li>
 * <li>
 * Providing application specific feedback &mdash; the service handles only
 * accessibility events from the clock application.
 * </li>
 * <li>
 * Providing dynamic, context-dependent feedback &mdash; feedback type changes
 * depending on the ringer state.
 * </li>
 * </ol>
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ClockBackService extends AccessibilityService {
    /**
     * Tag for logging from this service.
     */
    private static final String LOG_TAG = "ClockBackService";

    // Fields for configuring how the system handles this accessibility service.

    /**
     * Minimal timeout between accessibility events we want to receive.
     */
    private static final int EVENT_NOTIFICATION_TIMEOUT_MILLIS = 80;

    /**
     * Packages we are interested in.
     * <p>
     * <strong>
     * Note: This code sample will work only on devices shipped with the
     * default Clock application.
     * </strong>
     * </p>
     * This works with AlarmClock and Clock whose package name changes in different releases
     */
    private static final String[] PACKAGE_NAMES = new String[]{
            "com.android.alarmclock", "com.google.android.deskclock", "com.android.deskclock"
    };

    // Message types we are passing around.

    /**
     * Speak.
     */
    private static final int MESSAGE_SPEAK = 1;

    /**
     * Stop speaking.
     */
    private static final int MESSAGE_STOP_SPEAK = 2;

    /**
     * Start the TTS service.
     */
    private static final int MESSAGE_START_TTS = 3;

    /**
     * Stop the TTS service.
     */
    private static final int MESSAGE_SHUTDOWN_TTS = 4;

    /**
     * Play an earcon.
     */
    private static final int MESSAGE_PLAY_EARCON = 5;

    /**
     * Stop playing an earcon.
     */
    private static final int MESSAGE_STOP_PLAY_EARCON = 6;

    /**
     * Vibrate a pattern.
     */
    private static final int MESSAGE_VIBRATE = 7;

    /**
     * Stop vibrating.
     */
    private static final int MESSAGE_STOP_VIBRATE = 8;

    // Screen state broadcast related constants.

    /**
     * Feedback mapping index used as a key for the screen-on broadcast.
     */
    private static final int INDEX_SCREEN_ON = 0x00000100;

    /**
     * Feedback mapping index used as a key for the screen-off broadcast.
     */
    private static final int INDEX_SCREEN_OFF = 0x00000200;

    // Ringer mode change related constants.

    /**
     * Feedback mapping index used as a key for normal ringer mode.
     */
    private static final int INDEX_RINGER_NORMAL = 0x00000400;

    /**
     * Feedback mapping index used as a key for vibration ringer mode.
     */
    private static final int INDEX_RINGER_VIBRATE = 0x00000800;

    /**
     * Feedback mapping index used as a key for silent ringer mode.
     */
    private static final int INDEX_RINGER_SILENT = 0x00001000;

    // Speech related constants.

    /**
     * The queuing mode we are using - interrupt a spoken utterance before
     * speaking another one.
     */
    private static final int QUEUING_MODE_INTERRUPT = 2;

    /**
     * The space string constant.
     */
    private static final String SPACE = " ";

    /**
     * Mapping from integers to vibration patterns for haptic feedback.
     */
    private static final SparseArray<long[]> sVibrationPatterns = new SparseArray<>();

    /*
     * Initializes our {@code sVibrationPatterns} {@code SparseArray}.
     */
    static {
        sVibrationPatterns.put(AccessibilityEvent.TYPE_VIEW_CLICKED, new long[]{
                0L, 100L
        });
        sVibrationPatterns.put(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED, new long[]{
                0L, 100L
        });
        sVibrationPatterns.put(AccessibilityEvent.TYPE_VIEW_SELECTED, new long[]{
                0L, 15L, 10L, 15L
        });
        sVibrationPatterns.put(AccessibilityEvent.TYPE_VIEW_FOCUSED, new long[]{
                0L, 15L, 10L, 15L
        });
        sVibrationPatterns.put(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, new long[]{
                0L, 25L, 50L, 25L, 50L, 25L
        });
        sVibrationPatterns.put(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER, new long[]{
                0L, 15L, 10L, 15L, 15L, 10L
        });
        sVibrationPatterns.put(INDEX_SCREEN_ON, new long[]{
                0L, 10L, 10L, 20L, 20L, 30L
        });
        sVibrationPatterns.put(INDEX_SCREEN_OFF, new long[]{
                0L, 30L, 20L, 20L, 10L, 10L
        });
    }

    /**
     * Mapping from integers to raw sound resource ids.
     */
    @SuppressLint("UseSparseArrays")
    private static SparseArray<Integer> sSoundsResourceIds = new SparseArray<>();

    /*
     * Initializes our {@code sSoundsResourceIds} {@code SparseArray}.
     */
    static {
        sSoundsResourceIds.put(AccessibilityEvent.TYPE_VIEW_CLICKED,
                R.raw.sound_view_clicked);
        sSoundsResourceIds.put(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED,
                R.raw.sound_view_clicked);
        sSoundsResourceIds.put(AccessibilityEvent.TYPE_VIEW_SELECTED,
                R.raw.sound_view_focused_or_selected);
        sSoundsResourceIds.put(AccessibilityEvent.TYPE_VIEW_FOCUSED,
                R.raw.sound_view_focused_or_selected);
        sSoundsResourceIds.put(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                R.raw.sound_window_state_changed);
        sSoundsResourceIds.put(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER,
                R.raw.sound_view_hover_enter);
        sSoundsResourceIds.put(INDEX_SCREEN_ON, R.raw.sound_screen_on);
        sSoundsResourceIds.put(INDEX_SCREEN_OFF, R.raw.sound_screen_off);
        sSoundsResourceIds.put(INDEX_RINGER_SILENT, R.raw.sound_ringer_silent);
        sSoundsResourceIds.put(INDEX_RINGER_VIBRATE, R.raw.sound_ringer_vibrate);
        sSoundsResourceIds.put(INDEX_RINGER_NORMAL, R.raw.sound_ringer_normal);
    }

    // Sound pool related member fields.

    /**
     * Mapping from integers to earcon names - dynamically populated.
     */
    private final SparseArray<String> mEarconNames = new SparseArray<>();

    // Auxiliary fields.

    /**
     * Handle to this service to enable inner classes to access the {@code Context}.
     */
    Context mContext;

    /**
     * The feedback this service is currently providing.
     */
    int mProvidedFeedbackType;

    /**
     * Reusable instance for building utterances.
     */
    private final StringBuilder mUtterance = new StringBuilder();

    // Feedback providing services.

    /**
     * The {@code TextToSpeech} used for speaking.
     */
    private TextToSpeech mTts;

    /**
     * The {@code AudioManager} for detecting ringer state.
     */
    private AudioManager mAudioManager;

    /**
     * Vibrator for providing haptic feedback.
     */
    private Vibrator mVibrator;

    /**
     * Flag if the infrastructure is initialized.
     */
    private boolean isInfrastructureInitialized;

    /**
     * {@code Handler} for executing messages on the service main thread.
     */
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        /**
         * We implement this to receive messages. We switch on the {@code what} field of our parameter
         * {@code Message message}:
         * <ul>
         * <li>
         * MESSAGE_SPEAK - we initialize {@code String utterance} with the {@code obj} field of
         * {@code message} (cast to {@code String}), call the {@code speak} method of our field
         * {@code TextToSpeech mTts} to speak {@code utterance} using the queuing strategy
         * QUEUING_MODE_INTERRUPT, then return
         * </li>
         * <li>
         * MESSAGE_STOP_SPEAK - we call the {@code stop} method of our field {@code TextToSpeech mTts}
         * to interrupt the current utterance and discard all utterances in the queue, then return.
         * </li>
         * <li>
         * MESSAGE_START_TTS - we initialize our field {@code TextToSpeech mTts} with a new instance
         * using an anonymous class for the {@code TextToSpeech.OnInitListener} which just registers
         * us as a broadcast receiver, and we then return.
         * </li>
         * <li>
         * MESSAGE_SHUTDOWN_TTS - we call the {@code shutdown} method of {@code mTts} and return.
         * </li>
         * <li>
         * MESSAGE_PLAY_EARCON - we initialize {@code int resourceId} with the {@code arg1} field of
         * our argument {@code message}, call our method {@code playEarcon} with it to play the
         * earcon with that id (an earcon is a brief, distinctive sound used to represent a specific
         * event or convey other information), then we return.
         * </li>
         * <li>
         * MESSAGE_STOP_PLAY_EARCON - we call the {@code stop} method of {@code mTts} and return.
         * </li>
         * <li>
         * MESSAGE_VIBRATE - we initialize {@code int key} with the {@code arg1} field of our argument
         * {@code message}, initialize {@code long[] pattern} with the array stored at position {@code key}
         * in {@code SparseArray<long[]> sVibrationPatterns}, and if that is not null we call the
         * {@code vibrate} method of {@code Vibrator mVibrator} with that pattern. In either case we
         * return to our caller.
         * </li>
         * <li>
         * MESSAGE_STOP_VIBRATE - we call the {@code cancel} method of {@code Vibrator mVibrator} and
         * return.
         * </li>
         * </ul>
         *
         * @param message A {@code Message} object
         */
        @SuppressWarnings("UnnecessaryReturnStatement")
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case MESSAGE_SPEAK:
                    String utterance = (String) message.obj;
                    mTts.speak(utterance, QUEUING_MODE_INTERRUPT, null);
                    return;
                case MESSAGE_STOP_SPEAK:
                    mTts.stop();
                    return;
                case MESSAGE_START_TTS:
                    mTts = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
                        public void onInit(int status) {
                            // Register here since to add earcons the TTS must be initialized and
                            // the receiver is called immediately with the current ringer mode.
                            registerBroadCastReceiver();
                        }
                    });
                    return;
                case MESSAGE_SHUTDOWN_TTS:
                    mTts.shutdown();
                    return;
                case MESSAGE_PLAY_EARCON:
                    int resourceId = message.arg1;
                    playEarcon(resourceId);
                    return;
                case MESSAGE_STOP_PLAY_EARCON:
                    mTts.stop();
                    return;
                case MESSAGE_VIBRATE:
                    int key = message.arg1;
                    long[] pattern = sVibrationPatterns.get(key);
                    if (pattern != null) {
                        mVibrator.vibrate(pattern, -1);
                    }
                    return;
                case MESSAGE_STOP_VIBRATE:
                    mVibrator.cancel();
                    return;
            }
        }
    };

    /**
     * {@code BroadcastReceiver} for receiving updates for the actions ACTION_SCREEN_ON, ACTION_SCREEN_OFF
     * and AudioManager.RINGER_MODE_CHANGED_ACTION.
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /**
         * This method is called when the BroadcastReceiver is receiving an Intent broadcast. We
         * initialize {@code String action} with the action of our argument {@code Intent intent}.
         * If {@code action} is equal to AudioManager.RINGER_MODE_CHANGED_ACTION we initialize
         * {@code int ringerMode} with the extra in {@code intent} stored under the key
         * AudioManager.EXTRA_RINGER_MODE (defaulting to RINGER_MODE_NORMAL) and call our method
         * {@code configureForRingerMode} with {@code ringerMode} to configure our feedback to
         * the new ringer mode. Else if {@code action} equals ACTION_SCREEN_ON, we call our method
         * {@code provideScreenStateChangeFeedback} to provide feedback to announce the screen state
         * change to INDEX_SCREEN_ON. Else if {@code action} equals ACTION_SCREEN_OFF, we call our method
         * {@code provideScreenStateChangeFeedback} to provide feedback to announce the screen state
         * change to INDEX_SCREEN_OFF. If {@code action} is not any of the above we just log a message
         * stating that we do not handle {@code action}.
         *
         * @param context The Context in which the receiver is running.
         * @param intent The Intent being received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(action)) {
                int ringerMode = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE,
                        AudioManager.RINGER_MODE_NORMAL);
                configureForRingerMode(ringerMode);
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                provideScreenStateChangeFeedback(INDEX_SCREEN_ON);
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                provideScreenStateChangeFeedback(INDEX_SCREEN_OFF);
            } else {
                Log.w(LOG_TAG, "Registered for but not handling action " + action);
            }
        }

        /**
         * Provides feedback to announce the screen state change. Such a change is turning the screen
         * on or off. We switch on our field {@code int mProvidedFeedbackType}:
         * <ul>
         *     <li>
         *         FEEDBACK_SPOKEN - we initialize {@code String utterance} with the string returned
         *         by our method {@code generateScreenOnOrOffUtternace} for our argument {@code feedbackIndex},
         *         use {@code Handler mHandler} to load a {@code Message} with the {@code what} field of
         *         MESSAGE_SPEAK, and the {@code obj} field of {@code utterance} then send this {@code Message}
         *         to {@code Handler mHandler}, and return to our caller.
         *     </li>
         *     <li>
         *         FEEDBACK_AUDIBLE - we use {@code Handler mHandler} to load a {@code Message} with
         *         the {@code what} field of MESSAGE_PLAY_EARCON, the {@code arg1} field of {@code feedbackIndex},
         *         and the {@code arg2} field of 0, then send this {@code Message} to {@code Handler mHandler},
         *         and return to our caller.
         *     </li>
         *     <li>
         *         FEEDBACK_HAPTIC - we use {@code Handler mHandler} to load a {@code Message} with
         *         the {@code what} field of MESSAGE_VIBRATE, the {@code arg1} field of {@code feedbackIndex},
         *         and the {@code arg2} field of 0, then send this {@code Message} to {@code Handler mHandler},
         *         and return to our caller.
         *     </li>
         *     <li>
         *         default - we throw an IllegalStateException.
         *     </li>
         * </ul>
         *
         * @param feedbackIndex The index of the feedback in the statically
         *            mapped feedback resources.
         */
        private void provideScreenStateChangeFeedback(int feedbackIndex) {
            // We take a specific action depending on the feedback we currently provide.
            switch (mProvidedFeedbackType) {
                case AccessibilityServiceInfo.FEEDBACK_SPOKEN:
                    String utterance = generateScreenOnOrOffUtternace(feedbackIndex);
                    mHandler.obtainMessage(MESSAGE_SPEAK, utterance).sendToTarget();
                    return;
                case AccessibilityServiceInfo.FEEDBACK_AUDIBLE:
                    mHandler.obtainMessage(MESSAGE_PLAY_EARCON, feedbackIndex, 0).sendToTarget();
                    return;
                case AccessibilityServiceInfo.FEEDBACK_HAPTIC:
                    mHandler.obtainMessage(MESSAGE_VIBRATE, feedbackIndex, 0).sendToTarget();
                    return;
                default:
                    throw new IllegalStateException("Unexpected feedback type "
                            + mProvidedFeedbackType);
            }
        }
    };

    /**
     * This method is a part of the {@code AccessibilityService} lifecycle and is called after the
     * system has successfully bound to the service. First we check if we have already initialized
     * our Infrastructure (our flag {@code isInfrastructureInitialized} is true) and if so we return
     * having done nothing. We initialize our field {@code Context mContext} to "this", then send
     * the empty message MESSAGE_START_TTS to {@code Handler mHandler} to start the Text to speech
     * service running. We initialize {@code Vibrator mVibrator} with an instance of the system level
     * service VIBRATOR_SERVICE, and {@code AudioManager mAudioManager} with an instance of the
     * service AUDIO_SERVICE. We use {@code mAudioManager} to fetch the current ringer mode to
     * {@code int ringerMode} and call our method {@code configureForRingerMode} with it to configure
     * the types of feedback which are appropriate for the ringer mode. Finally we set our flag
     * {@code boolean isInfrastructureInitialized} to true.
     */
    @Override
    public void onServiceConnected() {
        if (isInfrastructureInitialized) {
            return;
        }

        mContext = this;

        // Send a message to start the TTS.
        mHandler.sendEmptyMessage(MESSAGE_START_TTS);

        // Get the vibrator service.
        mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);

        // Get the AudioManager and configure according the current ring mode.
        mAudioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        // In Froyo the broadcast receiver for the ringer mode is called back with the
        // current state upon registering but in Eclair this is not done so we poll here.
        @SuppressWarnings("ConstantConditions")
        int ringerMode = mAudioManager.getRingerMode();
        configureForRingerMode(ringerMode);

        // We are in an initialized state now.
        isInfrastructureInitialized = true;
    }

    /**
     * Called when all clients have disconnected from a particular interface published by the service.
     * If our flag {@code boolean isInfrastructureInitialized} is true we send the empty message
     * MESSAGE_SHUTDOWN_TTS to our {@code Handler mHandler} to shut down the text to speech service,
     * and if {@code BroadcastReceiver mBroadcastReceiver} is not null we unregister it. We then set
     * {@code isInfrastructureInitialized} to false. In either case we return false to the caller.
     *
     * @param intent The Intent that was used to bind to this service
     * @return Return true if you would like to have the service's
     */
    @Override
    public boolean onUnbind(Intent intent) {
        if (isInfrastructureInitialized) {
            // Stop the TTS service.
            mHandler.sendEmptyMessage(MESSAGE_SHUTDOWN_TTS);

            // Unregister the intent broadcast receiver.
            if (mBroadcastReceiver != null) {
                unregisterReceiver(mBroadcastReceiver);
            }

            // We are not in an initialized state anymore.
            isInfrastructureInitialized = false;
        }
        return false;
    }

    /**
     * Registers the phone state observing broadcast receiver. We initialize {@code IntentFilter filter}
     * with a new instance, add the actions RINGER_MODE_CHANGED_ACTION, ACTION_SCREEN_ON, and
     * ACTION_SCREEN_OFF to it then register {@code BroadcastReceiver mBroadcastReceiver} to receive
     * broadcast intents matching {@code filter}.
     */
    private void registerBroadCastReceiver() {
        // Create a filter with the broadcast intents we are interested in.
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        // Register for broadcasts of interest.
        registerReceiver(mBroadcastReceiver, filter, null, null);
    }

    /**
     * Generates an utterance for announcing screen on and screen off. If our argument {@code feedbackIndex}
     * is INDEX_SCREEN_ON We initialize our variable {@code int resourceId} to the resource id
     * R.string.template_screen_on ("Screen on. Volume %1$s percent."), otherwise we initialize it
     * to the resource id R.string.template_screen_off ("Screen off. Volume %1$s percent."). We
     * fetch the string that {@code resourceId} references to {@code String template}. We set
     * {@code int currentRingerVolume} to the volume of the stream STREAM_RING that {@code mAudioManager}
     * returns when queried, and {@code int maxRingerVolume} to the maximum value for that stream.
     * We calculate {@code int volumePercent} to be 100 divided by {@code maxRingerVolume} times
     * {@code currentRingerVolume} (the current volume 0 to 100 percent). We do some math tricks
     * to round {@code volumePercent} to the nearest 5 percent then we return the string formed by
     * formatting {@code volumePercent} using the format in {@code template}.
     *
     * @param feedbackIndex The feedback index for looking up feedback value.
     * @return The utterance.
     */
    private String generateScreenOnOrOffUtternace(int feedbackIndex) {
        // Get the announce template.
        int resourceId = (feedbackIndex == INDEX_SCREEN_ON) ? R.string.template_screen_on
                : R.string.template_screen_off;
        String template = mContext.getString(resourceId);

        // Format the template with the ringer percentage.
        int currentRingerVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        int maxRingerVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        int volumePercent = (100 / maxRingerVolume) * currentRingerVolume;

        // Let us round to five so it sounds better.
        int adjustment = volumePercent % 10;
        if (adjustment < 5) {
            volumePercent -= adjustment;
        } else if (adjustment > 5) {
            volumePercent += (10 - adjustment);
        }

        return String.format(template, volumePercent);
    }

    /**
     * Configures the service according to a ringer mode. Possible
     * configurations:
     * <p>
     * 1. {@code AudioManager#RINGER_MODE_SILENT}<br/>
     * Goal:     Provide only custom haptic feedback.<br/>
     * Approach: Take over the haptic feedback by configuring this service to provide
     * such and do so. This way the system will not call the default haptic
     * feedback service KickBack.<br/>
     * Take over the audible and spoken feedback by configuring this
     * service to provide such feedback but not doing so. This way the system
     * will not call the default spoken feedback service TalkBack and the
     * default audible feedback service SoundBack.
     * </p>
     * <p>
     * 2. {@code AudioManager#RINGER_MODE_VIBRATE}<br/>
     * Goal:     Provide custom audible and default haptic feedback.<br/>
     * Approach: Take over the audible feedback and provide custom one.<br/>
     * Take over the spoken feedback but do not provide such.<br/>
     * Let some other service provide haptic feedback (KickBack).
     * </p>
     * <p>
     * 3. {@code AudioManager#RINGER_MODE_NORMAL}
     * Goal:     Provide custom spoken, default audible and default haptic feedback.<br/>
     * Approach: Take over the spoken feedback and provide custom one.<br/>
     * Let some other services provide audible feedback (SoundBack) and haptic
     * feedback (KickBack).
     * </p>
     *
     * @param ringerMode The device ringer mode.
     */
    private void configureForRingerMode(int ringerMode) {
        if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
            // When the ringer is silent we want to provide only haptic feedback.
            mProvidedFeedbackType = AccessibilityServiceInfo.FEEDBACK_HAPTIC;

            // Take over the spoken and sound feedback so no such feedback is provided.
            setServiceInfo(AccessibilityServiceInfo.FEEDBACK_HAPTIC
                    | AccessibilityServiceInfo.FEEDBACK_SPOKEN
                    | AccessibilityServiceInfo.FEEDBACK_AUDIBLE);

            // Use only an earcon to announce ringer state change.
            mHandler.obtainMessage(MESSAGE_PLAY_EARCON, INDEX_RINGER_SILENT, 0).sendToTarget();
        } else if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            // When the ringer is vibrating we want to provide only audible feedback.
            mProvidedFeedbackType = AccessibilityServiceInfo.FEEDBACK_AUDIBLE;

            // Take over the spoken feedback so no spoken feedback is provided.
            setServiceInfo(AccessibilityServiceInfo.FEEDBACK_AUDIBLE
                    | AccessibilityServiceInfo.FEEDBACK_SPOKEN);

            // Use only an earcon to announce ringer state change.
            mHandler.obtainMessage(MESSAGE_PLAY_EARCON, INDEX_RINGER_VIBRATE, 0).sendToTarget();
        } else if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            // When the ringer is ringing we want to provide spoken feedback
            // overriding the default spoken feedback.
            mProvidedFeedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
            setServiceInfo(AccessibilityServiceInfo.FEEDBACK_SPOKEN);

            // Use only an earcon to announce ringer state change.
            mHandler.obtainMessage(MESSAGE_PLAY_EARCON, INDEX_RINGER_NORMAL, 0).sendToTarget();
        }
    }

    /**
     * Sets the {@code AccessibilityServiceInfo} which informs the system how to handle this
     * {@code AccessibilityService}. We initialize {@code AccessibilityServiceInfo info} with a new
     * instance, set its {@code eventTypes} field (the event types an AccessibilityService we are
     * interested in) to TYPES_ALL_MASK (Mask for all types of AccessibilityEvent), and set its
     * {@code feedbackType} field (the type of feedback we want to provide) to our parameter
     * {@code int feedbackType}, the {@code notificationTimeout} field (the timeout after the most
     * recent event of a given type before an AccessibilityService is notified) to our constant
     * EVENT_NOTIFICATION_TIMEOUT_MILLIS (80ms), and its {@code packageNames} field (the package names
     * we are interested in) to our array {@code String[] PACKAGE_NAMES} (which contains the package
     * names of the standard Android clock packages). Finally we call the {@code setServiceInfo} method
     * with {@code info} as its argument to set the AccessibilityServiceInfo that describes this service.
     *
     * @param feedbackType The type of feedback this service will provide. Note: The feedbackType
     *                     parameter is an bitwise or of all feedback types this service would like
     *                     to provide.
     */
    private void setServiceInfo(int feedbackType) {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        // We are interested in all types of accessibility events.
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        // We want to provide specific type of feedback.
        info.feedbackType = feedbackType;
        // We want to receive events in a certain interval.
        info.notificationTimeout = EVENT_NOTIFICATION_TIMEOUT_MILLIS;
        // We want to receive accessibility events only from certain packages.
        info.packageNames = PACKAGE_NAMES;
        setServiceInfo(info);
    }

    /**
     * Callback for {@code AccessibilityEvent}s. First we log the string value of our argument
     * {@code AccessibilityEvent event}. If our field {@code mProvidedFeedbackType} is equal to
     * FEEDBACK_SPOKEN, we obtain and send a {@code Message} to {@code Handler mHandler} with a
     * {@code what} field of MESSAGE_SPEAK, and the {@code String} generated by our method
     * {@code formatUtterance} for {@code event}. Else if field {@code mProvidedFeedbackType} is equal
     * to FEEDBACK_AUDIBLE, we obtain and send a {@code Message} to {@code Handler mHandler} with a
     * {@code what} field of MESSAGE_PLAY_EARCON, the event type of {@code event} as the {@code arg1}
     * field and 0 for the {@code arg2} field. Else if field {@code mProvidedFeedbackType} is equal
     * to FEEDBACK_HAPTIC, we obtain and send a {@code Message} to {@code Handler mHandler} with a
     * {@code what} field of MESSAGE_VIBRATE, the event type of {@code event} as the {@code arg1}
     * field and 0 for the {@code arg2} field. If the {@code mProvidedFeedbackType} is none of the
     * above we throw an IllegalStateException: "Unexpected feedback type ".
     *
     * @param event An event.
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(LOG_TAG, mProvidedFeedbackType + " " + event.toString());

        // Here we act according to the feedback type we are currently providing.
        if (mProvidedFeedbackType == AccessibilityServiceInfo.FEEDBACK_SPOKEN) {
            mHandler.obtainMessage(MESSAGE_SPEAK, formatUtterance(event)).sendToTarget();
        } else if (mProvidedFeedbackType == AccessibilityServiceInfo.FEEDBACK_AUDIBLE) {
            mHandler.obtainMessage(MESSAGE_PLAY_EARCON, event.getEventType(), 0).sendToTarget();
        } else if (mProvidedFeedbackType == AccessibilityServiceInfo.FEEDBACK_HAPTIC) {
            mHandler.obtainMessage(MESSAGE_VIBRATE, event.getEventType(), 0).sendToTarget();
        } else {
            throw new IllegalStateException("Unexpected feedback type " + mProvidedFeedbackType);
        }
    }

    /**
     * Callback for interrupting the accessibility feedback. If our field {@code mProvidedFeedbackType}
     * is equal to FEEDBACK_SPOKEN, we obtain and send a {@code Message} to {@code Handler mHandler}
     * with a {@code what} field of MESSAGE_STOP_SPEAK. Else if our field {@code mProvidedFeedbackType}
     * is equal to FEEDBACK_AUDIBLE, we obtain and send a {@code Message} to {@code Handler mHandler}
     * with a {@code what} field of MESSAGE_STOP_PLAY_EARCON. Else if our field {@code mProvidedFeedbackType}
     * is equal to FEEDBACK_HAPTIC, we obtain and send a {@code Message} to {@code Handler mHandler}
     * with a {@code what} field of MESSAGE_STOP_VIBRATE. If the {@code mProvidedFeedbackType} is none
     * of the above we throw an IllegalStateException: "Unexpected feedback type ".
     */
    @Override
    public void onInterrupt() {
        // Here we act according to the feedback type we are currently providing.
        if (mProvidedFeedbackType == AccessibilityServiceInfo.FEEDBACK_SPOKEN) {
            mHandler.obtainMessage(MESSAGE_STOP_SPEAK).sendToTarget();
        } else if (mProvidedFeedbackType == AccessibilityServiceInfo.FEEDBACK_AUDIBLE) {
            mHandler.obtainMessage(MESSAGE_STOP_PLAY_EARCON).sendToTarget();
        } else if (mProvidedFeedbackType == AccessibilityServiceInfo.FEEDBACK_HAPTIC) {
            mHandler.obtainMessage(MESSAGE_STOP_VIBRATE).sendToTarget();
        } else {
            throw new IllegalStateException("Unexpected feedback type " + mProvidedFeedbackType);
        }
    }

    /**
     * Formats an utterance from an {@code AccessibilityEvent}. We initialize {@code StringBuilder utterance}
     * by copying the reference from our field {@code StringBuilder mUtterance}, and clear it by setting
     * its length to 0. We initialize {@code List<CharSequence> eventText} by retrieving the text
     * of the {@code AccessibilityEvent event}. If {@code eventText} we loop through all the
     * {@code CharSequence subText} in {@code eventText} removing the character '0' if it is the first
     * character, appending {@code subText} to {@code utterance} followed by a SPACE character. When
     * we are done appending all the {@code CharSequence} into {@code utterance} we return the string
     * value of {@code utterance} to the caller.
     *
     * @param event The event from which to format an utterance.
     * @return The formatted utterance.
     */
    private String formatUtterance(AccessibilityEvent event) {
        StringBuilder utterance = mUtterance;

        // Clear the utterance before appending the formatted text.
        utterance.setLength(0);

        List<CharSequence> eventText = event.getText();

        // We try to get the event text if such.
        if (!eventText.isEmpty()) {
            for (CharSequence subText : eventText) {
                // Make 01 pronounced as 1
                if (subText.charAt(0) == '0') {
                    subText = subText.subSequence(1, subText.length());
                }
                utterance.append(subText);
                utterance.append(SPACE);
            }

            return utterance.toString();
        }

        // There is no event text but we try to get the content description which is
        // an optional attribute for describing a view (typically used with ImageView).
        CharSequence contentDescription = event.getContentDescription();
        if (contentDescription != null) {
            utterance.append(contentDescription);
            return utterance.toString();
        }

        return utterance.toString();
    }

    /**
     * Plays an earcon given its id. We set {@code String earconName} to the string in our field
     * {@code SparseArray<String> mEarconNames} at index {@code earconId}. If {@code earconName} is
     * null we do not know the sound id, hence we need to load the sound. We initialize
     * {@code Integer resourceId} with the resource id at index {@code earconId} in our field
     * {@code SparseArray<Integer> sSoundsResourceIds}. If {@code resourceId} is not equal to null,
     * we set {@code earconName} to the string formed by surrounding the string value of {@code earconId}
     * with '[' and ']' characters, call the {@code addEarcon} method of {@code TextToSpeech mTts} to
     * add a mapping between the string of text {@code earconName}, our package name, and the sound
     * resource {@code resourceId}. We then store {@code earconName} under the key {@code earconId}
     * in {@code SparseArray<String> mEarconNames}. Finally we call the {@code playEarcon} method of
     * {@code mTts} to play the Earcon {@code earconName} using QUEUING_MODE_INTERRUPT as the queueing
     * mode.
     *
     * @param earconId The id of the earcon to be played.
     */
    private void playEarcon(int earconId) {
        String earconName = mEarconNames.get(earconId);
        if (earconName == null) {
            // We do not know the sound id, hence we need to load the sound.
            Integer resourceId = sSoundsResourceIds.get(earconId);
            if (resourceId != null) {
                earconName = "[" + earconId + "]";
                mTts.addEarcon(earconName, getPackageName(), resourceId);
                mEarconNames.put(earconId, earconName);
            }
        }

        mTts.playEarcon(earconName, QUEUING_MODE_INTERRUPT, null);
    }
}
