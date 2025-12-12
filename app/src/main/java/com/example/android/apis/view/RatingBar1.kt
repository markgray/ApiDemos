/*
 * Copyright (C) 2007 The Android Open Source Project
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
@file:Suppress("ReplaceNotNullAssertionWithElvisReturn")

package com.example.android.apis.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.RatingBar
import android.widget.RatingBar.OnRatingBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates how to use a rating bar. The top two [RatingBar]'s have their
 * [RatingBar.OnRatingBarChangeListener] set to this and a [onRatingChanged] method
 * updates a [TextView] and two [RatingBar] indicators based on the values passed to it.
 */
@SuppressLint("SetTextI18n")
class RatingBar1 : AppCompatActivity(), OnRatingBarChangeListener {
    /**
     * [RatingBar] with the ID R.id.small_ratingbar, it is used to display the value that is
     * given to either of the top two [RatingBar] using ?android:attr/ratingBarStyleSmall
     */
    private var mSmallRatingBar: RatingBar? = null

    /**
     * [RatingBar] with the ID R.id.indicator_ratingbar, it is used to display the value that
     * is given to either of the top two [RatingBar] using ?android:attr/ratingBarStyleIndicator
     */
    private var mIndicatorRatingBar: RatingBar? = null

    /**
     * [TextView] we use to display the textual version of the value that is given to either
     * of the top two [RatingBar]
     */
    private var mRatingText: TextView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.ratingbar_1. We
     * initialize our [TextView] field [mRatingText] by finding the view with ID R.id.rating,
     * [RatingBar] field [mIndicatorRatingBar] by finding the view with ID R.id.indicator_ratingbar,
     * and [RatingBar] field [mSmallRatingBar] by finding the view with ID R.id.small_ratingbar.
     * We find the view with ID R.id.ratingbar1 and set its `OnRatingBarChangeListener` to this
     * and do the same thing with the view with ID R.id.ratingbar2.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ratingbar_1)
        mRatingText = findViewById(R.id.rating)

        // We copy the most recently changed rating on to these indicator-only
        // rating bars
        mIndicatorRatingBar = findViewById(R.id.indicator_ratingbar)
        mSmallRatingBar = findViewById(R.id.small_ratingbar)

        // The different rating bars in the layout. Assign the listener to us.
        (findViewById<View>(R.id.ratingbar1) as RatingBar).onRatingBarChangeListener = this
        (findViewById<View>(R.id.ratingbar2) as RatingBar).onRatingBarChangeListener = this
    }

    /**
     * Notification that the rating has changed. We initialize our [Int] variable `val numStars` by
     * fetching the number of stars that our [RatingBar] parameter [ratingBar] displays. Then we
     * set the text of our [TextView] field [mRatingText] to the string created by concatenating
     * the string with resource ID R.string.ratingbar_rating ("Rating:") with a space then the
     * string value of [rating] followed by a "/" followed by the string value of `numStars`.
     * If the number of stars that our [RatingBar] field [mIndicatorRatingBar] displays is not
     * equal to `numStars` we set the number of stars of both [mIndicatorRatingBar] and
     * [mSmallRatingBar] to `numStars`. If the current rating (number of stars filled) of
     * [mIndicatorRatingBar] is not equal to our parameter [rating] we set the rating  of
     * both [mIndicatorRatingBar] and [mSmallRatingBar]  to [rating]. We initialize
     * our [Float] variable `val ratingBarStepSize` by getting the step size of [ratingBar] and
     * if the step size of [mIndicatorRatingBar] is not equal to `ratingBarStepSize` we
     * set the step size of both [mIndicatorRatingBar] and [mSmallRatingBar] to `ratingBarStepSize`.
     *
     * @param ratingBar The [RatingBar] whose rating has changed.
     * @param rating    The current rating. This will be in the range 0..numStars.
     * @param fromTouch True if the rating change was initiated by a user's touch gesture or arrow
     *                  key/horizontal trackball movement.
     */
    override fun onRatingChanged(ratingBar: RatingBar, rating: Float, fromTouch: Boolean) {
        val numStars: Int = ratingBar.numStars
        mRatingText!!.text = getString(R.string.ratingbar_rating) + " " + rating + "/" + numStars

        // Since this rating bar is updated to reflect any of the other rating
        // bars, we should update it to the current values.
        if (mIndicatorRatingBar!!.numStars != numStars) {
            mIndicatorRatingBar!!.numStars = numStars
            mSmallRatingBar!!.numStars = numStars
        }
        if (mIndicatorRatingBar!!.rating != rating) {
            mIndicatorRatingBar!!.rating = rating
            mSmallRatingBar!!.rating = rating
        }
        val ratingBarStepSize: Float = ratingBar.stepSize
        if (mIndicatorRatingBar!!.stepSize != ratingBarStepSize) {
            mIndicatorRatingBar!!.stepSize = ratingBarStepSize
            mSmallRatingBar!!.stepSize = ratingBarStepSize
        }
    }
}