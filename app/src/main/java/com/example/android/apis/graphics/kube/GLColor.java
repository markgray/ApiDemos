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

package com.example.android.apis.graphics.kube;

/**
 * Class containing the four components of a color
 */
@SuppressWarnings("WeakerAccess")
public class GLColor {

    /**
     * red component of color
     */
    public final int red;
    /**
     * green component of color
     */
    public final int green;
    /**
     * blue component of color
     */
    public final int blue;
    /**
     * alpha component of color
     */
    public final int alpha;

    /**
     * Constructor for an instance of {@code GLColor} with all four components passed as parameters.
     *
     * @param red   red component of color
     * @param green green component of color
     * @param blue  blue component of color
     * @param alpha alpha component of color
     */
    @SuppressWarnings("unused")
    public GLColor(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    /**
     * Constructor for an instance of {@code GLColor} with three components specified, and alpha
     * defaulting to 0x10000
     *
     * @param red   red component of color
     * @param green green component of color
     * @param blue  blue component of color
     */
    public GLColor(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = 0x10000;
    }

    /**
     * Compares this instance with the specified object and indicates if they are equal. First we make
     * sure our argument is an instance of {@code GLColor}, and if so we cast {@code other} to
     * {@code GLColor color}, and return true if all components are the same as the components of this
     * instance, false if not. We also return false if {@code other} is not an instance of {@code GLColor}.
     *
     * @param other {@code GLColor} object to compare against
     * @return true if all components of both {@code GLColor} objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof GLColor) {
            GLColor color = (GLColor) other;
            return (red == color.red &&
                    green == color.green &&
                    blue == color.blue &&
                    alpha == color.alpha);
        }
        return false;
    }
}
