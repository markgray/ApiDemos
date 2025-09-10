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
package com.example.android.apis.media

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.media.audiofx.Visualizer
import android.media.audiofx.Visualizer.OnDataCaptureListener
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import com.example.android.apis.graphics.Utilities.id2p

/**
 * Nifty equalizer with simplified audio waveform display using onWaveFormDataCapture callback of
 * the [Visualizer.OnDataCaptureListener] interface.
 * RequiresApi(Build.VERSION_CODES.GINGERBREAD)
 */
class AudioFxDemo : AppCompatActivity() {
    /**
     * [MediaPlayer] that plays test_cbr.mp3
     */
    private var mMediaPlayer: MediaPlayer? = null

    /**
     * [Visualizer] which gathers waveform data from our [mMediaPlayer] and delivers it to
     * our [OnDataCaptureListener] which passes the bytes of captured waveform to the
     * [VisualizerView.updateVisualizer] method of our [VisualizerView] field [mVisualizerView]
     * for display.
     */
    private var mVisualizer: Visualizer? = null

    /**
     * [Equalizer] which allows the user to adjust the balance between the frequency components
     * of the media being played by [MediaPlayer] field [mMediaPlayer].
     */
    private var mEqualizer: Equalizer? = null

    /**
     * [LinearLayout] we create programmatically and use as our content view.
     */
    private var mLinearLayout: LinearLayout? = null

    /**
     * [VisualizerView] which draws our waveform.
     */
    private var mVisualizerView: VisualizerView? = null

    /**
     * [TextView] used to display the status of our app, (always "Playing audio..." as far as
     * I can see, even when the mp3 being played is long finished).
     */
    private var mStatusTextView: TextView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we call the method `setVolumeControlStream` to request that the volume of
     * the audio stream for music playback ([AudioManager.STREAM_MUSIC]) should be controlled by
     * the hardware volume controls.
     *
     * We initialize our [TextView] field [mStatusTextView] with a new instance of [TextView], and
     * our [LinearLayout] field [mLinearLayout] with a new instance of [LinearLayout] whose
     * orientation we set to VERTICAL. We add the view [mStatusTextView] to [mLinearLayout] and
     * then set [mLinearLayout] to be our content view.
     *
     * We initialize [MediaPlayer] field [mMediaPlayer] with a [MediaPlayer] created to play the
     * mp3 R.raw.test_cbr, then call our method [setupVisualizerFxAndUI] to set up our visualizer,
     * and our method [setupEqualizerFxAndUI] to set up our equalizer. We then enable our
     * visualization engine in [Visualizer] field [mVisualizer] (which was set up by
     * [setupVisualizerFxAndUI]).
     *
     * We set the [MediaPlayer.OnCompletionListener] of [MediaPlayer] field [mMediaPlayer] to an
     * lambda which simply disables our visualization engine [mVisualizer].
     *
     * Finally we start the playback of [mMediaPlayer], and set the text of [TextView] field
     * [mStatusTextView] to the string "Playing audio...".
     *
     * @param icicle we do not override [onSaveInstanceState] so do not use.
     */
    @SuppressLint("SetTextI18n")
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        volumeControlStream = AudioManager.STREAM_MUSIC
        mStatusTextView = TextView(this)
        mLinearLayout = LinearLayout(this)
        mLinearLayout!!.orientation = LinearLayout.VERTICAL
        mLinearLayout!!.setPadding(0, id2p(160), 0, id2p(60))
        mLinearLayout!!.addView(mStatusTextView)
        setContentView(mLinearLayout)

        // Create the MediaPlayer
        mMediaPlayer = MediaPlayer.create(this, R.raw.test_cbr)
        Log.d(TAG, "MediaPlayer audio session ID: " + mMediaPlayer!!.audioSessionId)
        setupVisualizerFxAndUI()
        setupEqualizerFxAndUI()

        // Make sure the visualizer is enabled only when you actually want to receive data, and
        // when it makes sense to receive data.
        mVisualizer!!.enabled = true

        // When the stream ends, we don't need to collect any more data. We don't do this in
        // setupVisualizerFxAndUI because we likely want to have more, non-Visualizer related code
        // in this callback.
        mMediaPlayer!!.setOnCompletionListener { mVisualizer!!.enabled = false }
        mMediaPlayer!!.start()
        mStatusTextView!!.text = "Playing audio..."
    }

    /**
     * Creates and sets up the equalizer. First we create a new instance for [Equalizer] field
     * [mEqualizer] using the audio session ID of [MediaPlayer] field [mMediaPlayer] with a priority
     * for our control of it of 0 (the normal priority). We then enable [mEqualizer] to make the
     * [Equalizer] apply its settings to the audio content being played in the audio session.
     *
     * We create [TextView] `val eqTextView`, set its text to "Equalizer:" and add it to the main UI
     * in [LinearLayout] field [mLinearLayout].
     *
     * We set [Short] `val bands` to the number of frequency bands supported by the Equalizer engine
     * in [Equalizer] field [mEqualizer], set [Short] `val minEQLevel` to minimum equalization value,
     * and [Short] `val maxEQLevel` to the maximum equalization value of [mEqualizer].
     *
     * We loop through the `bands` bands of [mEqualizer], set [Short] `val band` to the band index
     * being processed, create a [TextView] `val freqTextView`, set its layout parameters to
     * MATCH_PARENT and WRAP_CONTENT, set its gravity to CENTER_HORIZONTAL, and set its text to
     * the center frequency of the given `band` divided by 1000 and with the string " Hz" appended
     * to it. We then add the view `freqTextView` to [LinearLayout] field [mLinearLayout].
     *
     * We create a [LinearLayout] `val row`, and set its orientation to HORIZONTAL. We create a
     * [TextView] `val minDbTextView`, set its layout parameters to WRAP_CONTENT (both horizontal
     * and vertical), and set its text to `minEQLevel` divided by 100 with the string " dB" appended
     * to it. We create a [TextView] `val maxDbTextView`, set its layout parameters to WRAP_CONTENT
     * (both horizontal and vertical), and set its text to `maxEQLevel` divided by 100 with the
     * string " dB" appended to it. We create [LinearLayout.LayoutParams] `val layoutParams` with
     * the layout parameters MATCH_PARENT and WRAP_CONTENT, and set its `weight` to 1. We create a
     * [SeekBar] `val bar`, set its layout parameters to `layoutParams`, set its maximum setting
     * to `maxEQLevel` minus `minEQLevel`, and set its current progress to the gain currently set
     * for the equalizer band `band` of `mEqualizer`. Then we set the `OnSeekBarChangeListener`
     * of `bar` to an anonymous class whose `onProgressChanged` override sets the equalizer gain
     * value of band `band` to the `progress` added to `minEQLevel`. Now we add the views
     * `minDbTextView`, `bar` and `maxDbTextView` to [LinearLayout] `row`, and add `row` to
     * [LinearLayout]  field [mLinearLayout] (the content view of our UI that we are filling.
     *
     * Then we loop back to process the next `band` of `bands`.
     */
    @SuppressLint("SetTextI18n")
    private fun setupEqualizerFxAndUI() {
        // Create the Equalizer object (an AudioEffect subclass) and attach it to our media player,
        // with a default priority (0).
        mEqualizer = Equalizer(0, mMediaPlayer!!.audioSessionId)
        mEqualizer!!.enabled = true
        val eqTextView = TextView(this)
        eqTextView.text = "Equalizer:"
        mLinearLayout!!.addView(eqTextView)
        val bands = mEqualizer!!.numberOfBands
        val minEQLevel = mEqualizer!!.bandLevelRange[0]
        val maxEQLevel = mEqualizer!!.bandLevelRange[1]
        for (i in 0 until bands) {
            val band = i.toShort()
            val freqTextView = TextView(this)
            freqTextView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            freqTextView.gravity = Gravity.CENTER_HORIZONTAL
            freqTextView.text = (mEqualizer!!.getCenterFreq(band) / 1000).toString() + " Hz"
            mLinearLayout!!.addView(freqTextView)
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            val minDbTextView = TextView(this)
            minDbTextView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            minDbTextView.text = (minEQLevel / 100).toString() + " dB"
            val maxDbTextView = TextView(this)
            maxDbTextView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            maxDbTextView.text = (maxEQLevel / 100).toString() + " dB"
            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.weight = 1f
            val bar = SeekBar(this)
            bar.layoutParams = layoutParams
            bar.max = maxEQLevel - minEQLevel
            bar.progress = mEqualizer!!.getBandLevel(band).toInt()
            bar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    mEqualizer!!.setBandLevel(band, (progress + minEQLevel).toShort())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
            row.addView(minDbTextView)
            row.addView(bar)
            row.addView(maxDbTextView)
            mLinearLayout!!.addView(row)
        }
    }

    /**
     * Creates and set up our [VisualizerView] field [mVisualizerView]. First we initialize our
     * [VisualizerView] field [mVisualizerView] with a new instance. Then we set its layout parameters
     * to MATCH_PARENT and VISUALIZER_HEIGHT_DIP scaled to pixels by multiplying by the logical screen
     * density. We then add [mVisualizerView] to [mVisualizerView]. We initialize our [Visualizer]
     * field [mVisualizer] with a new instance constructed to use the system wide unique audio session
     * identifier of [MediaPlayer] field [mMediaPlayer] as the audio session to attach to. We set the
     * capture size, i.e. the number of bytes returned by getWaveForm(byte[]) and getFft(byte[])
     * methods to the maximum capture size range of [Visualizer]. We set the [OnDataCaptureListener]
     * of [mVisualizer] to an anonymous class whose `onWaveFormDataCapture` override calls the
     * `updateVisualizer` method of [VisualizerView] field [mVisualizerView] with the `bytes`
     * sampled. The rate of the [OnDataCaptureListener] is half of the maximum capture rate for
     * the callback capture method, and a waveform capture is requested and not a frequency capture.
     */
    private fun setupVisualizerFxAndUI() {
        // Create a VisualizerView (defined below), which will render the simplified audio
        // wave form to a Canvas.
        mVisualizerView = VisualizerView(this)
        mVisualizerView!!.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            (VISUALIZER_HEIGHT_DIP * resources.displayMetrics.density).toInt()
        )
        mLinearLayout!!.addView(mVisualizerView)

        // Create the Visualizer object and attach it to our media player.
        mVisualizer = Visualizer(mMediaPlayer!!.audioSessionId)
        mVisualizer!!.captureSize = Visualizer.getCaptureSizeRange()[1]
        mVisualizer!!.setDataCaptureListener(object : OnDataCaptureListener {
            override fun onWaveFormDataCapture(
                visualizer: Visualizer,
                bytes: ByteArray,
                samplingRate: Int
            ) {
                mVisualizerView!!.updateVisualizer(bytes)
            }

            override fun onFftDataCapture(
                visualizer: Visualizer,
                bytes: ByteArray,
                samplingRate: Int
            ) {
            }

        }, Visualizer.getMaxCaptureRate() / 2, true, false)
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call our super's implementation of `onPause`, then
     * if the method [isFinishing] returns true and if [MediaPlayer] field [mMediaPlayer] is not
     * null, we release the native resources used by [Visualizer] field [mVisualizer], release the
     * native AudioEffect resources of [Equalizer] field [mEqualizer], release resources associated
     * with [MediaPlayer] field [mMediaPlayer] and set [mMediaPlayer] to null.
     */
    override fun onPause() {
        super.onPause()
        if (isFinishing && mMediaPlayer != null) {
            mVisualizer!!.release()
            mEqualizer!!.release()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    companion object {
        /**
         * TAG for logging
         */
        private const val TAG = "AudioFxDemo"

        /**
         * Visualizer height in dip, scaled to pixels using logical screen density before use.
         */
        private const val VISUALIZER_HEIGHT_DIP = 50f
    }
}

/**
 * A simple class that draws waveform data received from a
 * [Visualizer.OnDataCaptureListener.onWaveFormDataCapture]
 */
internal class VisualizerView(context: Context?) : View(context) {
    /**
     * Array containing latest sample bytes passed to our method [updateVisualizer] by the
     * `onWaveFormDataCapture` override of the [OnDataCaptureListener] of [Visualizer] field
     * `mVisualizer` of [AudioFxDemo].
     */
    private var mBytes: ByteArray? = null

    /**
     * The coordinates of points used to draw lines to display the [mBytes] of the sample. Each
     * line requires four coordinates (two points) so its size is four times the number of sample
     * values.
     */
    private var mPoints: FloatArray? = null

    /**
     * [Rect] having same size as the view we are drawing to (ie. `getWidth()` by `getHeight()`.
     */
    private val mRect = Rect()

    /**
     * [Paint] used to draw the lines of our graph of the media sample.
     */
    private val mForePaint = Paint()

    /**
     * Initialize our instance, called from our constructor. First we set our [ByteArray] field
     * [mBytes] to null, then we set the stroke width of [Paint] field [mForePaint] to 1, set its
     * antialias flag, and set its color to a shade of blue.
     */
    private fun init() {
        mBytes = null
        mForePaint.strokeWidth = 1f
        mForePaint.isAntiAlias = true
        mForePaint.color = Color.rgb(0, 128, 255)
    }

    /**
     * Saves the data array passed to the `onWaveFormDataCapture` method of the [OnDataCaptureListener]
     * of the [Visualizer] field `mVisualizer` of [AudioFxDemo] in our [ByteArray] field [mBytes],
     * then calls [invalidate] so that our [onDraw] override will be called.
     *
     * @param bytes Array of bytes containing the waveform representation of our audio sample.
     */
    fun updateVisualizer(bytes: ByteArray?) {
        mBytes = bytes
        invalidate()
    }

    /**
     * We implement this to do our drawing. First we call our super's implementation of `onDraw`,
     * and if our [ByteArray] field [mBytes] is null we return having done nothing. Then if our
     * [FloatArray] field [mPoints] is null, or smaller than four times the length of [mBytes] we
     * allocate a [FloatArray] that is four times the length of [mBytes] and set [mPoints] to it.
     * We set the size of [Rect] field [mRect] to the same size as our view in order to use it to
     * scale the audio waveform sample data to fit in our view. We loop through all the bytes in
     * [ByteArray] field [mBytes] calculating the (x,y) coordinates to plot the values in the space
     * allocated for our view, which we store in [FloatArray] field [mPoints]. Each line requires
     * four coordinates and each data point has a line which connects to the one before it and a
     * line which connects it to the one after it.
     *
     * When done filling [mPoints] we call the [Canvas.drawLines] method of our [Canvas] parameter
     * [canvas] to draw the lines using [Paint] field [mForePaint] as the [Paint].
     *
     * @param canvas the [Canvas] on which the background will be drawn
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mBytes == null) {
            return
        }
        if (mPoints == null || mPoints!!.size < mBytes!!.size * 4) {
            mPoints = FloatArray(mBytes!!.size * 4)
        }
        mRect[0, 0, width] = height
        for (i in 0 until mBytes!!.size - 1) {
            mPoints!![i * 4] = mRect.width() * i.toFloat() / (mBytes!!.size - 1)
            mPoints!![i * 4 + 1] = (mRect.height() / 2f
                + (mBytes!![i] + 128).toByte() * (mRect.height() / 2f) / 128f)
            mPoints!![i * 4 + 2] = mRect.width() * (i + 1) / (mBytes!!.size - 1f)
            mPoints!![i * 4 + 3] = (mRect.height() / 2f
                + (mBytes!![i + 1] + 128f).toInt().toByte() * (mRect.height() / 2f) / 128)
        }
        canvas.drawLines(mPoints!!, mForePaint)
    }

    /**
     * The init block of our constructor. We just call our `init` method to initialize our instance.
     */
    init {
        init()
    }
}