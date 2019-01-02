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

package com.example.android.apis.graphics;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

/**
 * Only used by {@code AnimateDrawable}, which extends us. We implement all the methods required by
 * the {@code Drawable} abstract class, allowing a subclass to only override the methods it needs
 * to customize.
 */
@SuppressWarnings("WeakerAccess")
public class ProxyDrawable extends Drawable {

    /**
     * "Real" Drawable that we are proxy for
     */
    private Drawable mProxy;
    /**
     * Flag indicating whether our {@code mutate} override has been called and propagated to the
     * {@code Drawable mProxy} we proxy for (it and "this" are now mutable)
     */
    private boolean mMutated;

    /**
     * Saves the parameter {@code Drawable target} in our field {@code Drawable mProxy}, so it can
     * be retrieved using our method {@code getProxy} in the {@code draw} override of our subclass
     *
     * @param target {@code Drawable} we are to proxy for, thereby allowing a subclass of us to override
     *               any methods it needs to
     */
    public ProxyDrawable(Drawable target) {
        mProxy = target;
    }

    /**
     * This is called by the {@code draw} override of our subclass in order to retrieve the
     * {@code Drawable} that we were constructed with (or have hadd set using {@code setProxy}).
     *
     * @return the {@code Drawable} that we are the proxy for
     */
    public Drawable getProxy() {
        return mProxy;
    }

    /**
     * Sets the field {@code Drawable mProxy} to the parameter {@code Drawable proxy} (Unused)
     *
     * @param proxy {@code Drawable} we are to be the proxy for
     */
    @SuppressWarnings("unused")
    public void setProxy(Drawable proxy) {
        if (proxy != this) {
            mProxy = proxy;
        }
    }

    /**
     * Draw in its bounds (set via setBounds) respecting optional effects such as alpha (set via
     * setAlpha) and color filter (set via setColorFilter). If our field {@code Drawable mProxy} is
     * not null, we pass the call to it. This is overridden by out subclass {@code AnimateDrawable}
     *
     * @param canvas The canvas to draw into
     */
    @Override
    public void draw(@NonNull Canvas canvas) {
        if (mProxy != null) {
            mProxy.draw(canvas);
        }
    }

    /**
     * Return the intrinsic width of the underlying drawable object. Returns -1 if it has no intrinsic
     * width, such as with a solid color. If our field {@code Drawable mProxy} is not null, we pass
     * the call through to it, otherwise we return -1.
     *
     * @return intrinsic width of our field {@code Drawable mProxy}
     */
    @Override
    public int getIntrinsicWidth() {
        return mProxy != null ? mProxy.getIntrinsicWidth() : -1;
    }

    /**
     * Return the intrinsic height of the underlying drawable object. Returns -1 if it has no intrinsic
     * height, such as with a solid color. If our field {@code Drawable mProxy} is not null, we pass
     * the call through to it, otherwise we return -1.
     *
     * @return intrinsic height of our field {@code Drawable mProxy}
     */
    @Override
    public int getIntrinsicHeight() {
        return mProxy != null ? mProxy.getIntrinsicHeight() : -1;
    }

    /**
     * Return the opacity/transparency of this Drawable.  The returned value is
     * one of the abstract format constants in
     * {@link android.graphics.PixelFormat}:
     * {@link android.graphics.PixelFormat#UNKNOWN},
     * {@link android.graphics.PixelFormat#TRANSLUCENT},
     * {@link android.graphics.PixelFormat#TRANSPARENT}, or
     * {@link android.graphics.PixelFormat#OPAQUE}.
     * <p>
     * An OPAQUE drawable is one that draws all content within its bounds, completely
     * covering anything behind the drawable. A TRANSPARENT drawable is one that draws nothing
     * within its bounds, allowing everything behind it to show through. A TRANSLUCENT drawable
     * is a drawable in any other state, where the drawable will draw some, but not all,
     * of the content within its bounds and at least some content behind the drawable will
     * be visible. If the visibility of the drawable's contents cannot be determined, the
     * safest/best return value is TRANSLUCENT.
     * <p>
     * Generally a Drawable should be as conservative as possible with the
     * value it returns.  For example, if it contains multiple child drawables
     * and only shows one of them at a time, if only one of the children is
     * TRANSLUCENT and the others are OPAQUE then TRANSLUCENT should be
     * returned.  You can use the method {@link #resolveOpacity} to perform a
     * standard reduction of two opacity's to the appropriate single output.
     * <p>
     * If our field {@code Drawable mProxy} is not null, we pass the call through to its
     * {@code getOpacity} method, if null we return TRANSPARENT.
     *
     * @return int The opacity class of the Drawable.
     */
    @Override
    public int getOpacity() {
        return mProxy != null ? mProxy.getOpacity() : PixelFormat.TRANSPARENT;
    }

    /**
     * Set to true to have the drawable filter its bitmaps with bilinear
     * sampling when they are scaled or rotated.
     * <p>
     * This can improve appearance when bitmaps are rotated. If the drawable
     * does not use bitmaps, this call is ignored.
     * <p>
     * If our field {@code Drawable mProxy} is not null we pass the call through to it.
     *
     * @param filter true to have the drawable filter its bitmaps with bilinear sampling when they
     *               are scaled or rotated.
     */

    @Override
    public void setFilterBitmap(boolean filter) {
        if (mProxy != null) {
            mProxy.setFilterBitmap(filter);
        }
    }

    /**
     * Set to true to have the drawable dither its colors when drawn to a device with fewer than
     * 8-bits per color component. Dithering affects how colors that are higher precision than the
     * device are down-sampled. No dithering is generally faster, but higher precision colors are
     * just truncated down (e.g. 8888 -> 565). Dithering tries to distribute the error inherent in
     * this process to reduce the visual artifacts.
     * <p>
     * If our field {@code Drawable mProxy} is not null, we pass the call through to it.
     *
     * @param dither true to set the dithering bit in flags, false to clear it
     * @see android.graphics.Paint#setDither(boolean)
     * @deprecated This property is ignored.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void setDither(boolean dither) {
        if (mProxy != null) {
            mProxy.setDither(dither);
        }
    }

    /**
     * Specify an optional color filter for the drawable.
     * <p>
     * If a Drawable has a ColorFilter, each output pixel of the Drawable's
     * drawing contents will be modified by the color filter before it is
     * blended onto the render target of a Canvas.
     * <p>
     * Pass {@code null} to remove any existing color filter.
     * Note: Setting a non-{@code null} color
     * filter disables {@link android.graphics.drawable.Drawable#setTintList(ColorStateList) tint}.
     * <p>
     * If our field {@code Drawable mProxy} is not null we pass the call through to it.
     *
     * @param colorFilter The color filter to apply, or {@code null} to remove the
     *                    existing color filter
     */
    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        if (mProxy != null) {
            mProxy.setColorFilter(colorFilter);
        }
    }

    /**
     * Specify an alpha value for the drawable. 0 means fully transparent, and 255 means fully
     * opaque. If our field {@code Drawable mProxy} is not null we pass the call through to it.
     *
     * @param alpha alpha value for our drawable.
     */
    @Override
    public void setAlpha(int alpha) {
        if (mProxy != null) {
            mProxy.setAlpha(alpha);
        }
    }

    /**
     * Make this drawable mutable. This operation cannot be reversed. A mutable
     * drawable is guaranteed to not share its state with any other drawable.
     * This is especially useful when you need to modify properties of drawables
     * loaded from resources. By default, all drawables instances loaded from
     * the same resource share a common state; if you modify the state of one
     * instance, all the other instances will receive the same modification.
     * <p>
     * Calling this method on a mutable Drawable will have no effect.
     * <p>
     * If our field {@code Drawable mProxy} is not null, and our field {@code boolean mMutated} is
     * false (meaning we have not been called already), we check to see if our super's implementation
     * of {@code mutate} returns a reference to "this" (which it should!) and if we pass these tests
     * we pass the call through to {@code mProxy} and set the {@code mMutated} flag to true.
     *
     * @return This drawable.
     * @see ConstantState
     * @see #getConstantState()
     */
    @NonNull
    @Override
    public Drawable mutate() {
        if (mProxy != null && !mMutated && super.mutate() == this) {
            mProxy.mutate();
            mMutated = true;
        }
        return this;
    }
}

