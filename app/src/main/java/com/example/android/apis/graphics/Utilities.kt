package com.example.android.apis.graphics

import android.content.res.Resources

/**
 * Contains important graphics utility methods which are unused :)
 */
@Suppress("unused")
object Utilities {
    fun d2p(dpi: Float): Float {
        val scale = Resources.getSystem().displayMetrics.density
        return dpi * scale
    }

    fun id2p(dpi: Int): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpi * scale).toInt()
    }
}