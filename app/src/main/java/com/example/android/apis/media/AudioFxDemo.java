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

package com.example.android.apis.media;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

/**
 * Nifty equalizer with simplified audio waveform display using onWaveFormDataCapture callback of
 * the Visualizer.OnDataCaptureListener interface.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class AudioFxDemo extends AppCompatActivity {
    /**
     * TAG for logging
     */
    private static final String TAG = "AudioFxDemo";

    /**
     * Visualizer height in dip, scaled to pixels using logical screen density before use.
     */
    private static final float VISUALIZER_HEIGHT_DIP = 50f;

    /**
     * {@code MediaPlayer} that plays test_cbr.mp3
     */
    private MediaPlayer mMediaPlayer;
    /**
     * {@code Visualizer} which gathers waveform data from our {@code mMediaPlayer} and delivers it to
     * our {@code OnDataCaptureListener} which passes the bytes of captured waveform to the method
     * {@code mVisualizerView.updateVisualizer} for display.
     */
    private Visualizer mVisualizer;

    /**
     * {@code Equalizer} which allows the user to adjust the balance between the frequency components
     * of the media being played by {@code MediaPlayer mMediaPlayer}.
     */
    private Equalizer mEqualizer;

    /**
     * {@code LinearLayout} we create programmatically and use as our content view.
     */
    private LinearLayout mLinearLayout;
    /**
     * {@code VisualizerView} which draws our waveform.
     */
    private VisualizerView mVisualizerView;
    /**
     * {@code TextView} used to display the status of our app, (always "Playing audio..." as far as
     * I can see, even when the mp3 being played is long finished).
     */
    @SuppressWarnings("FieldCanBeLocal")
    private TextView mStatusTextView;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we call the method {@code setVolumeControlStream} to request that the
     * volume of the audio stream for music playback (STREAM_MUSIC) should be changed by the hardware
     * volume controls.
     * <p>
     * We initialize our field {@code TextView mStatusTextView} with a new instance of {@code TextView},
     * and our field {@code LinearLayout mLinearLayout} with a new instance of {@code LinearLayout}
     * whose orientation we set to VERTICAL. We add the view {@code mStatusTextView} to {@code mLinearLayout}
     * and then set {@code mLinearLayout} to be our content view.
     * <p>
     * We initialize {@code MediaPlayer mMediaPlayer} with a {@code MediaPlayer} to play the mp3
     * R.raw.test_cbr, then call our method {@code setupVisualizerFxAndUI} to set up our visualizer,
     * and our method {@code setupEqualizerFxAndUI} to set up our equalizer. We then enable our
     * visualization engine {@code mVisualizer} (which was set up by {@code setupVisualizerFxAndUI}).
     * <p>
     * We set the {@code OnCompletionListener} of {@code MediaPlayer mMediaPlayer} to an anonymous
     * class which simply disables our visualization engine {@code mVisualizer}.
     * <p>
     * Finally we start the playback of {@code MediaPlayer mMediaPlayer}, and set the text of
     * {@code TextView mStatusTextView} to the string "Playing audio...".
     *
     * @param icicle we do not override {@code onSaveInstanceState} so do not use.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mStatusTextView = new TextView(this);

        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.addView(mStatusTextView);

        setContentView(mLinearLayout);

        // Create the MediaPlayer
        mMediaPlayer = MediaPlayer.create(this, R.raw.test_cbr);
        Log.d(TAG, "MediaPlayer audio session ID: " + mMediaPlayer.getAudioSessionId());

        setupVisualizerFxAndUI();
        setupEqualizerFxAndUI();

        // Make sure the visualizer is enabled only when you actually want to receive data, and
        // when it makes sense to receive data.
        mVisualizer.setEnabled(true);

        // When the stream ends, we don't need to collect any more data. We don't do this in
        // setupVisualizerFxAndUI because we likely want to have more, non-Visualizer related code
        // in this callback.
        mMediaPlayer.setOnCompletionListener(mediaPlayer -> mVisualizer.setEnabled(false));

        mMediaPlayer.start();
        mStatusTextView.setText("Playing audio...");
    }

    /**
     * Creates and sets up the equalizer. First we create a new instance for {@code Equalizer mEqualizer}
     * using the audio session ID of {@code MediaPlayer mMediaPlayer} with a priority for our control
     * of it of 0 (the normal priority). We then enable {@code Equalizer mEqualizer} to make the effect
     * to be actually applied to the audio content being played in the corresponding audio session.
     * <p>
     * We create {@code TextView eqTextView}, set its text to "Equalizer:" and add it to the main UI
     * {@code LinearLayout mLinearLayout}.
     * <p>
     * We set {@code short bands} to the number of frequency bands supported by the Equalizer engine
     * {@code Equalizer mEqualizer}, set {@code short minEQLevel} to minimum equalization value, and
     * {@code short maxEQLevel} to the maximum equalization value of {@code mEqualizer}.
     * <p>
     * We loop through the {@code bands} bands of {@code Equalizer mEqualizer}, set {@code short band}
     * to the band index being processed, create a {@code TextView freqTextView}, set its layout parameters
     * to MATCH_PARENT and WRAP_CONTENT, set its gravity to CENTER_HORIZONTAL, and set its text to
     * the center frequency of the given {@code band} divided by 1000 and with the string " Hz" appended
     * to it. We then add the view {@code freqTextView} to {@code LinearLayout mLinearLayout}.
     * <p>
     * We create a {@code LinearLayout row}, and set its orientation to HORIZONTAL. We create a
     * {@code TextView minDbTextView}, set its layout parameters to WRAP_CONTENT (both horizontal and
     * vertical), and set its text to {@code minEQLevel} divided by 100 with the string " dB" appended
     * to it. We create a {@code TextView maxDbTextView}, set its layout parameters to WRAP_CONTENT
     * (both horizontal and vertical), and set its text to {@code maxEQLevel} divided by 100 with the
     * string " dB" appended to it. We create {@code LinearLayout.LayoutParams layoutParams} with the
     * layout parameters MATCH_PARENT and WRAP_CONTENT, and set its {@code weight} to 1. We create a
     * {@code SeekBar bar}, set its layout parameters to {@code layoutParams}, set its maximum setting
     * to {@code maxEQLevel-minEQLevel}, and set its current progress to the gain currently set for
     * the equalizer band {@code band} of {@code mEqualizer}. Then we set the {@code OnSeekBarChangeListener}
     * of {@code bar} to an anonymous class whose {@code onProgressChanged} override sets the equalizer
     * gain value of band {@code band} to the {@code progress} added to {@code minEQLevel}. Now we
     * add the views {@code minDbTextView}, {@code bar} and {@code maxDbTextView} to {@code LinearLayout row},
     * and add {@code row} to {@code LinearLayout mLinearLayout} (the content view of our UI that we are
     * filling.
     * <p>
     * Then we loop back to process the next {@code band} of {@code bands}.
     */
    @SuppressLint("SetTextI18n")
    private void setupEqualizerFxAndUI() {
        // Create the Equalizer object (an AudioEffect subclass) and attach it to our media player,
        // with a default priority (0).
        mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());
        mEqualizer.setEnabled(true);

        TextView eqTextView = new TextView(this);
        eqTextView.setText("Equalizer:");
        mLinearLayout.addView(eqTextView);

        short bands = mEqualizer.getNumberOfBands();

        final short minEQLevel = mEqualizer.getBandLevelRange()[0];
        final short maxEQLevel = mEqualizer.getBandLevelRange()[1];

        for (short i = 0; i < bands; i++) {
            final short band = i;

            TextView freqTextView = new TextView(this);
            freqTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            freqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            freqTextView.setText((mEqualizer.getCenterFreq(band) / 1000) + " Hz");
            mLinearLayout.addView(freqTextView);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView minDbTextView = new TextView(this);
            minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            minDbTextView.setText((minEQLevel / 100) + " dB");

            TextView maxDbTextView = new TextView(this);
            maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            maxDbTextView.setText((maxEQLevel / 100) + " dB");

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            SeekBar bar = new SeekBar(this);
            bar.setLayoutParams(layoutParams);
            bar.setMax(maxEQLevel - minEQLevel);
            bar.setProgress(mEqualizer.getBandLevel(band));

            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mEqualizer.setBandLevel(band, (short) (progress + minEQLevel));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            row.addView(minDbTextView);
            row.addView(bar);
            row.addView(maxDbTextView);

            mLinearLayout.addView(row);
        }
    }

    /**
     * Creates and set up our {@code VisualizerView mVisualizerView}. First we initialize our field
     * {@code VisualizerView mVisualizerView} with a new instance. Then we set its layout parameters
     * to MATCH_PARENT and VISUALIZER_HEIGHT_DIP scaled to pixels by multiplying by the logical screen
     * density. We then add {@code mVisualizerView} to {@code VisualizerView mVisualizerView}. We
     * initialize our field {@code Visualizer mVisualizer} with a new instance constructed to use the
     * system wide unique audio session identifier of {@code MediaPlayer mMediaPlayer} to attach to it.
     * We set the capture size, i.e. the number of bytes returned by getWaveForm(byte[]) and getFft(byte[])
     * methods to the maximum capture size range of {@code Visualizer}. We set the {@code OnDataCaptureListener}
     * of {@code mVisualizer} to an anonymous class whose {@code onWaveFormDataCapture} override calls
     * the {@code updateVisualizer} method of {@code VisualizerView mVisualizerView} with the {@code bytes}
     * sampled. The rate of the {@code OnDataCaptureListener} is half of the maximum capture rate for
     * the callback capture method, and a waveform capture is requested and not a frequency capture.
     */
    private void setupVisualizerFxAndUI() {
        // Create a VisualizerView (defined below), which will render the simplified audio
        // wave form to a Canvas.
        mVisualizerView = new VisualizerView(this);
        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (VISUALIZER_HEIGHT_DIP * getResources().getDisplayMetrics().density)));
        mLinearLayout.addView(mVisualizerView);

        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                mVisualizerView.updateVisualizer(bytes);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false);
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call our super's implementation of {@code onPause}, then
     * if the method {@code isFinishing} returns true and is {@code MediaPlayer mMediaPlayer} is not
     * null, we release the native resources used by {@code Visualizer mVisualizer}, release the
     * native AudioEffect resources of {@code Equalizer mEqualizer}, release resources associated
     * with {@code MediaPlayer mMediaPlayer} and set {@code mMediaPlayer} to null.
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (isFinishing() && mMediaPlayer != null) {
            mVisualizer.release();
            mEqualizer.release();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}

/**
 * A simple class that draws waveform data received from a
 * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture }
 */
class VisualizerView extends View {
    /**
     * Array containing latest sample bytes passed to our method {@code updateVisualizer} by the
     * {@code onWaveFormDataCapture} override of the {@code OnDataCaptureListener} of
     * {@code Visualizer mVisualizer}
     */
    private byte[] mBytes;
    /**
     * The coordinates of points used to draw lines to display the {@code mBytes} of the sample. Each
     * line requires four coordinates (two points) so its size is four times the number of sample
     * values.
     */
    private float[] mPoints;
    /**
     * {@code Rect} having same size as the view we are drawing to (ie. {@code getWidth()} by
     * {@code getHeight()}.
     */
    private Rect mRect = new Rect();

    /**
     * {@code Paint} used to draw the lines of our graph of the media sample.
     */
    private Paint mForePaint = new Paint();

    /**
     * Our constructor. First we call our super's constructor, then we call our {@code init} method
     * to initialize our instance.
     *
     * @param context {@code Context} to use to access resources, "this" in the
     *                {@code setupVisualizerFxAndUI} method of {@code AudioFxDemo}.
     */
    public VisualizerView(Context context) {
        super(context);
        init();
    }

    /**
     * Initialize our instance, called from our constructor. First we set our field
     * {@code byte[] mBytes} to null, then we set the stroke width of {@code Paint mForePaint}
     * to 1, set its antialias flag, and set its color to a shade of blue.
     */
    private void init() {
        mBytes = null;

        mForePaint.setStrokeWidth(1f);
        mForePaint.setAntiAlias(true);
        mForePaint.setColor(Color.rgb(0, 128, 255));
    }

    /**
     * Saves the data array passed to the {@code onWaveFormDataCapture} method of the
     * {@code OnDataCaptureListener} of {@code Visualizer mVisualizer} in our field
     * {@code byte[] mBytes}.
     *
     * @param bytes Array of bytes containing the waveform representation of our audio sample.
     */
    public void updateVisualizer(byte[] bytes) {
        mBytes = bytes;
        invalidate();
    }

    /**
     * We implement this to do our drawing. First we call our super's implementation of {@code onDraw},
     * and if our field {@code byte[] mBytes} is null we return having done nothing. Then if our field
     * {@code float[] mPoints} is null, or smaller than four times the length of {@code mBytes} we
     * allocate a {@code float[]} array that is four times the length of {@code mBytes} and set
     * {@code mPoints} to it. We set the size of {@code Rect mRect} to the same size as our view in
     * order to use it to scale the audio waveform sample data to fit in our view. We loop through
     * all the bytes in {@code byte[] mBytes} calculating the (x,y) coordinates to plot the values in
     * the space allocated for our view, which we store in {@code float[] mPoints}. Each line requires
     * four coordinates and each data point has a line which connects to the one before it and a line
     * which has connects it to the one after it.
     *
     * When done filling {@code float[] mPoints} we call {@code canvas.drawLines} to draw the lines
     * using the {@code Paint mForePaint} as the {@code Paint}.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBytes == null) {
            return;
        }

        if (mPoints == null || mPoints.length < mBytes.length * 4) {
            mPoints = new float[mBytes.length * 4];
        }

        mRect.set(0, 0, getWidth(), getHeight());

        for (int i = 0; i < mBytes.length - 1; i++) {
            mPoints[i * 4] = (mRect.width() * (float)i / (mBytes.length - 1));
            mPoints[i * 4 + 1] = mRect.height() / 2f
                    + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2f) / 128f;
            mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length - 1f);
            mPoints[i * 4 + 3] = mRect.height() / 2f
                    + ((byte) (mBytes[i + 1] + 128f)) * (mRect.height() / 2f) / 128;
        }

        canvas.drawLines(mPoints, mForePaint);
    }
}
