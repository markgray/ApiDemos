/*
 * Copyright (C) 2008 The Android Open Source Project
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
package com.example.android.apis.graphics

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

/**
 * Only used by [AnimateDrawable], which extends us. We implement all the methods required by
 * the [Drawable] abstract class, allowing a subclass to only override the methods it needs
 * to customize.
 */
open class ProxyDrawable
/**
 * Saves the [Drawable] parameter [mProxy], so it can be used in the `draw` override of our subclass
 */
(
    /**
     * "Real" [Drawable] that we are proxy for
     */
    var mProxy: Drawable?
) : Drawable() {

    /**
     * Flag indicating whether our `mutate` override has been called and propagated to the
     * [Drawable] field [mProxy] we proxy for (it and *this* are now mutable)
     */
    private var mMutated = false

    /**
     * Draw in its bounds (set via setBounds) respecting optional effects such as alpha (set via
     * setAlpha) and color filter (set via setColorFilter). If our [Drawable] field [mProxy] is
     * not *null*, we pass the call to it. This is overridden by our subclass [AnimateDrawable]
     *
     * @param canvas The [Canvas] to draw into
     */
    override fun draw(canvas: Canvas) {
        if (mProxy != null) {
            mProxy!!.draw(canvas)
        }
    }

    /**
     * Return the intrinsic width of the underlying drawable object. Returns -1 if it has no intrinsic
     * width, such as with a solid color. If our [Drawable] field [mProxy] is not *null*, we pass
     * the call through to it, otherwise we return -1.
     *
     * @return intrinsic width of our [Drawable] field [mProxy]
     */
    override fun getIntrinsicWidth(): Int {
        return if (mProxy != null) mProxy!!.intrinsicWidth else -1
    }

    /**
     * Return the intrinsic height of the underlying drawable object. Returns -1 if it has no intrinsic
     * height, such as with a solid color. If our [Drawable] field [mProxy] is not *null*, we pass
     * the call through to it, otherwise we return -1.
     *
     * @return intrinsic height of our [Drawable] field [mProxy]
     */
    override fun getIntrinsicHeight(): Int {
        return if (mProxy != null) mProxy!!.intrinsicHeight else -1
    }

    /**
     * Return the opacity/transparency of this Drawable.  The returned value is
     * one of the abstract format constants in
     * [android.graphics.PixelFormat]:
     * [android.graphics.PixelFormat.UNKNOWN],
     * [android.graphics.PixelFormat.TRANSLUCENT],
     * [android.graphics.PixelFormat.TRANSPARENT], or
     * [android.graphics.PixelFormat.OPAQUE].
     *
     * An OPAQUE drawable is one that draws all content within its bounds, completely
     * covering anything behind the drawable. A TRANSPARENT drawable is one that draws nothing
     * within its bounds, allowing everything behind it to show through. A TRANSLUCENT drawable
     * is a drawable in any other state, where the drawable will draw some, but not all,
     * of the content within its bounds and at least some content behind the drawable will
     * be visible. If the visibility of the drawable's contents cannot be determined, the
     * safest/best return value is TRANSLUCENT.
     *
     * Generally a Drawable should be as conservative as possible with the
     * value it returns. For example, if it contains multiple child drawables
     * and only shows one of them at a time, if only one of the children is
     * TRANSLUCENT and the others are OPAQUE then TRANSLUCENT should be
     * returned. You can use the method `resolveOpacity` to perform a
     * standard reduction of two opacity's to the appropriate single output.
     *
     * If our [Drawable] field [mProxy] is not *null*, we pass the call through to its
     * `getOpacity` method, if null we return TRANSPARENT.
     *
     * @return int The opacity class of the Drawable.
     */
    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        @Suppress("DEPRECATION")
        return if (mProxy != null) mProxy!!.opacity else PixelFormat.TRANSPARENT
    }

    /**
     * Set to *true* to have the [Drawable] filter its bitmaps with bilinear sampling when they are
     * scaled or rotated. This can improve appearance when bitmaps are rotated. If the [Drawable]
     * does not use bitmaps, this call is ignored.
     *
     * If our [Drawable] field [mProxy] is not *null* we pass the call through to it.
     *
     * @param filter *true* to have the [Drawable] filter its bitmaps with bilinear sampling when
     * they are scaled or rotated.
     */
    override fun setFilterBitmap(filter: Boolean) {
        if (mProxy != null) {
            mProxy!!.isFilterBitmap = filter
        }
    }

    /**
     * Set to true to have the [Drawable] dither its colors when drawn to a device with fewer than
     * 8-bits per color component. Dithering affects how colors that are higher precision than the
     * device are down-sampled. No dithering is generally faster, but higher precision colors are
     * just truncated down (e.g. 8888 -> 565). Dithering tries to distribute the error inherent in
     * this process to reduce the visual artifacts.
     *
     * If our [Drawable] field [mProxy] is not *null*, we pass the call through to it.
     *
     * @param dither *true* to set the dithering bit in flags, *false* to clear it
     * @see android.graphics.Paint.setDither
     */
    @Deprecated("Deprecated in Java")
    override fun setDither(dither: Boolean) {
        if (mProxy != null) {
            @Suppress("DEPRECATION")
            mProxy!!.setDither(dither)
        }
    }

    /**
     * Specify an optional color filter for the [Drawable]. If a [Drawable] has a [ColorFilter],
     * each output pixel of the [Drawable]'s drawing contents will be modified by the color filter
     * before it is blended onto the render target of a Canvas. Pass *null* to remove any existing
     * color filter. **Note:** Setting a non-`null` color filter disables
     * [tint][android.graphics.drawable.Drawable.setTintList].
     *
     * If our [Drawable] field [mProxy] is not *null* we pass the call through to it.
     *
     * @param colorFilter The color filter to apply, or *null* to remove the
     * existing color filter
     */
    override fun setColorFilter(colorFilter: ColorFilter?) {
        if (mProxy != null) {
            mProxy!!.colorFilter = colorFilter
        }
    }

    /**
     * Specify an alpha value for the [Drawable]. 0 means fully transparent, and 255 means fully
     * opaque. If our [Drawable] field [mProxy] is not *null* we pass the call through to it.
     *
     * @param alpha alpha value for our drawable.
     */
    override fun setAlpha(alpha: Int) {
        if (mProxy != null) {
            mProxy!!.alpha = alpha
        }
    }

    /**
     * Make this [Drawable] mutable. This operation cannot be reversed. A mutable [Drawable] is
     * guaranteed to not share its state with any other drawable. This is especially useful when
     * you need to modify properties of drawables loaded from resources. By default, all drawables
     * instances loaded from the same resource share a common state; if you modify the state of one
     * instance, all the other instances will receive the same modification. Calling this method on
     * a mutable Drawable will have no effect.
     *
     * If our [Drawable] field [mProxy] is not *null*, and our [Boolean] field [mMutated] is *false*
     * (meaning we have not been called already), we check to see if our super's implementation of
     * `mutate` returns a reference to *this* (which it should!) and if we pass these tests we pass
     * the call through to [mProxy] and set the [mMutated] flag to *true*.
     *
     * @return This [Drawable].
     */
    override fun mutate(): Drawable {
        if (mProxy != null && !mMutated && super.mutate() === this) {
            mProxy!!.mutate()
            mMutated = true
        }
        return this
    }

}