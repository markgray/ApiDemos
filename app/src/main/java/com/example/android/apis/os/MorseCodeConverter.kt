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
package com.example.android.apis.os

/**
 * Class that implements the text to morse code conversion
 */
internal object MorseCodeConverter {
    /**
     * Base duration of vibration in milliseconds, all other durations are multiples of this
     */
    private const val SPEED_BASE: Long = 100

    /**
     * Duration in milliseconds of a "Dot"
     */
    private const val DOT = SPEED_BASE

    /**
     * Duration in milliseconds of a "Dash"
     */
    private const val DASH = SPEED_BASE * 3

    /**
     * Duration in milliseconds of a pause between dots and dashes
     */
    private const val GAP = SPEED_BASE

    /**
     * Duration in milliseconds of a pause between letters
     */
    private const val LETTER_GAP = SPEED_BASE * 3

    /**
     * Duration in milliseconds of a pause between words
     */
    private const val WORD_GAP = SPEED_BASE * 7

    /**
     * The characters from 'A' to 'Z'
     */
    private val LETTERS = arrayOf(
            longArrayOf(DOT, GAP, DASH), longArrayOf(DASH, GAP, DOT, GAP, DOT, GAP, DOT),
            longArrayOf(DASH, GAP, DOT, GAP, DASH, GAP, DOT), longArrayOf(DASH, GAP, DOT, GAP, DOT),
            longArrayOf(DOT), longArrayOf(DOT, GAP, DOT, GAP, DASH, GAP, DOT),
            longArrayOf(DASH, GAP, DASH, GAP, DOT), longArrayOf(DOT, GAP, DOT, GAP, DOT, GAP, DOT),
            longArrayOf(DOT, GAP, DOT), longArrayOf(DOT, GAP, DASH, GAP, DASH, GAP, DASH),
            longArrayOf(DASH, GAP, DOT, GAP, DASH), longArrayOf(DOT, GAP, DASH, GAP, DOT, GAP, DOT),
            longArrayOf(DASH, GAP, DASH), longArrayOf(DASH, GAP, DOT),
            longArrayOf(DASH, GAP, DASH, GAP, DASH), longArrayOf(DOT, GAP, DASH, GAP, DASH, GAP, DOT),
            longArrayOf(DASH, GAP, DASH, GAP, DOT, GAP, DASH), longArrayOf(DOT, GAP, DASH, GAP, DOT),
            longArrayOf(DOT, GAP, DOT, GAP, DOT), longArrayOf(DASH),
            longArrayOf(DOT, GAP, DOT, GAP, DASH), longArrayOf(DOT, GAP, DOT, GAP, DOT, GAP, DASH),
            longArrayOf(DOT, GAP, DASH, GAP, DASH), longArrayOf(DASH, GAP, DOT, GAP, DOT, GAP, DASH),
            longArrayOf(DASH, GAP, DOT, GAP, DASH, GAP, DASH),
            longArrayOf(DASH, GAP, DASH, GAP, DOT, GAP, DOT)
    )

    /**
     * The characters from '0' to '9'
     */
    private val NUMBERS = arrayOf(
            longArrayOf(DASH, GAP, DASH, GAP, DASH, GAP, DASH, GAP, DASH),
            longArrayOf(DOT, GAP, DASH, GAP, DASH, GAP, DASH, GAP, DASH),
            longArrayOf(DOT, GAP, DOT, GAP, DASH, GAP, DASH, GAP, DASH),
            longArrayOf(DOT, GAP, DOT, GAP, DOT, GAP, DASH, GAP, DASH),
            longArrayOf(DOT, GAP, DOT, GAP, DOT, GAP, DOT, GAP, DASH),
            longArrayOf(DOT, GAP, DOT, GAP, DOT, GAP, DOT, GAP, DOT),
            longArrayOf(DASH, GAP, DOT, GAP, DOT, GAP, DOT, GAP, DOT),
            longArrayOf(DASH, GAP, DASH, GAP, DOT, GAP, DOT, GAP, DOT),
            longArrayOf(DASH, GAP, DASH, GAP, DASH, GAP, DOT, GAP, DOT),
            longArrayOf(DASH, GAP, DASH, GAP, DASH, GAP, DASH, GAP, DOT)
    )

    /**
     * Duration in milliseconds of a pause when the character is not one we support
     */
    private val ERROR_GAP = longArrayOf(GAP)

    /**
     * Return the pattern data for a given character. We first check if the [Char] parameter [c]
     * is between 'A' and 'Z' and if so we return the array in [LETTERS] that is indexed by the
     * value `c - 'A'`, then we check if the parameter [c] is between 'a' and 'z' and if so we
     * return the array in [LETTERS] that is indexed by the value `c - 'a'`, then we check if the
     * parameter [c] is between '0' and '9' and if so we return the array in [NUMBERS] that is
     * indexed by the value `c - '0'`. If it is not one of the above we return the array [ERROR_GAP].
     *
     * @param c Character that is being converted to Morse code
     * @return an array of [Long] that contains the Morse code for the parameter [c]
     */
    fun pattern(c: Char): LongArray {
        if (c in 'A'..'Z') {
            return LETTERS[c - 'A']
        }
        return when (c) {
            in 'a'..'z' -> {
                LETTERS[c - 'a']
            }
            in '0'..'9' -> {
                NUMBERS[c - '0']
            }
            else -> {
                ERROR_GAP
            }
        }
    }

    /**
     * Return the pattern data for a given string. We declare our [Boolean] flag
     * `var lastWasWhitespace`, and store the length of our [String] parameter
     * [str] in `val strlen`.
     *
     * Next we proceed to calculate how big our return array of [LongArray] needs to be, by first
     * initializing `var len` to 1 (the length needed), and setting `lastWasWhitespace` to true.
     * Then we loop through all the [Char] `val c` in [str], checking whether `c` is a white space
     * character, and if so we check whether the previous character was also a white space character
     * skipping it if so, otherwise incrementing `len` and setting `lastWasWhitespace` to true. If
     * `c` was not a white space character and the previous character was also not a white space
     * character we increment `len`, then for all non white space characters we then set
     * `lastWasWhitespace` to false and add the length of the Morse code pattern returned for `c`
     * by our method [pattern] to `len`.
     *
     * Now that we know how long our array needs to be we allocate `len+1` longs for our [LongArray]
     * variable `val result`, set `result[0]` to 0 (the initial pause of the vibration pattern),
     * set `var pos` to 1 (next storage location in `result[]`), and set `lastWasWhitespace`
     * to true. Then once again looping through all the [Char] `val c` in [str], we first check
     * if `c` is a white space character, and if so we check whether the previous character was
     * also a white space character skipping it if so, otherwise setting the `pos` entry in `result`
     * to [WORD_GAP] incrementing `pos` and setting `lastWasWhitespace` to true. If `c` was not a
     * white space character and the previous character was a white space character we set the `pos`
     * entry in `result` to the array [LETTER_GAP] and increment `pos` before setting
     * `lastWasWhitespace` to true, and [LongArray] `val letter` to the array of Morse code that our
     * method [pattern] returns for `c`. We then copy the contents of the array `letter` to our array
     * `result` starting at location `pos` and add the length of `letter` to `pos`.
     *
     * When done with all the characters in [str] we return `result` to the caller.
     *
     * @param str a [String] which needs to be converted to Morse code
     * @return a [LongArray] that contains the Morse code for the parameter [str]
     */
    fun pattern(str: String): LongArray {
        /*
         * Flag to indicate that the previous character was a "white space" character
         */
        var lastWasWhitespace: Boolean
        /*
         * Number of characters in our parameter {@code str}
         */
        val strlen = str.length

        // Calculate how long our array needs to be.
        var len = 1
        lastWasWhitespace = true
        for (i in 0 until strlen) {
            val c = str[i]
            if (Character.isWhitespace(c)) {
                if (!lastWasWhitespace) {
                    len++
                    lastWasWhitespace = true
                }
            } else {
                if (!lastWasWhitespace) {
                    len++
                }
                lastWasWhitespace = false
                len += pattern(c).size
            }
        }

        // Generate the pattern array.  Note that we put an extra element of 0
        // in at the beginning, because the pattern always starts with the pause,
        // not with the vibration.
        val result = LongArray(len + 1)
        result[0] = 0
        var pos = 1
        lastWasWhitespace = true
        for (i in 0 until strlen) {
            val c = str[i]
            if (Character.isWhitespace(c)) {
                if (!lastWasWhitespace) {
                    result[pos] = WORD_GAP
                    pos++
                    lastWasWhitespace = true
                }
            } else {
                if (!lastWasWhitespace) {
                    result[pos] = LETTER_GAP
                    pos++
                }
                lastWasWhitespace = false
                val letter = pattern(c)
                System.arraycopy(letter, 0, result, pos, letter.size)
                pos += letter.size
            }
        }
        return result
    }
}