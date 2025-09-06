package com.example.android.apis.graphics

import android.content.res.Resources

/**
 * Contains important graphics utility methods
 */
object Utilities {
    /**
     * Converts density-independent [Float] pixels (dp) to physical [Float] pixels (px).
     *
     * @param dpi The value in dp to convert.
     * @return The equivalent value in px.
     */
    fun d2p(dpi: Float): Float {
        val scale = Resources.getSystem().displayMetrics.density
        return dpi * scale
    }

    /**
     * Converts density-independent [Int] pixels (dp) to physical [Int] pixels (px), rounding to
     * the nearest integer.
     *
     * @param dpi The value in dp to convert.
     * @return The equivalent value in px, rounded to the nearest integer.
     */
    fun id2p(dpi: Int): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpi * scale).toInt()
    }
}