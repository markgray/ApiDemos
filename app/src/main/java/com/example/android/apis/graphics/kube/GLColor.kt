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
package com.example.android.apis.graphics.kube

/**
 * Class containing the four components of a color
 */
class GLColor {
    /**
     * red component of color
     */
    @JvmField
    val red: Int
    /**
     * green component of color
     */
    @JvmField
    val green: Int
    /**
     * blue component of color
     */
    @JvmField
    val blue: Int
    /**
     * alpha component of color
     */
    @JvmField
    val alpha: Int

    /**
     * Constructor for an instance of `GLColor` with all four components passed as parameters.
     *
     * @param red   red component of color
     * @param green green component of color
     * @param blue  blue component of color
     * @param alpha alpha component of color
     */
    @Suppress("unused")
    constructor(red: Int, green: Int, blue: Int, alpha: Int) {
        this.red = red
        this.green = green
        this.blue = blue
        this.alpha = alpha
    }

    /**
     * Constructor for an instance of `GLColor` with three components specified, and alpha
     * defaulting to 0x10000
     *
     * @param red   red component of color
     * @param green green component of color
     * @param blue  blue component of color
     */
    constructor(red: Int, green: Int, blue: Int) {
        this.red = red
        this.green = green
        this.blue = blue
        alpha = 0x10000
    }

    /**
     * Compares this instance with the specified object and indicates if they are equal. First we make
     * sure our argument is an instance of `GLColor`, and if so we cast `other` to
     * `GLColor color`, and return true if all components are the same as the components of this
     * instance, false if not. We also return false if `other` is not an instance of `GLColor`.
     *
     * @param other `GLColor` object to compare against
     * @return true if all components of both `GLColor` objects are equal, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (other is GLColor) {
            return red == other.red && green == other.green && blue == other.blue && alpha == other.alpha
        }
        return false
    }

    override fun hashCode(): Int {
        var result = red
        result = 31 * result + green
        result = 31 * result + blue
        result = 31 * result + alpha
        return result
    }
}