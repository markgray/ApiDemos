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

package com.example.android.apis.os;

/**
 * Class that implements the text to morse code conversion
 */
@SuppressWarnings("WeakerAccess")
class MorseCodeConverter {
    /**
     * Base duration of vibration in milliseconds, all other durations are multiples of this
     */
    private static final long SPEED_BASE = 100;
    /**
     * Duration in milliseconds of a "Dot"
     */
    static final long DOT = SPEED_BASE;
    /**
     * Duration in milliseconds of a "Dash"
     */
    static final long DASH = SPEED_BASE * 3;
    /**
     * Duration in milliseconds of a pause between dots and dashes
     */
    static final long GAP = SPEED_BASE;
    /**
     * Duration in milliseconds of a pause between letters
     */
    static final long LETTER_GAP = SPEED_BASE * 3;
    /**
     * Duration in milliseconds of a pause between words
     */
    static final long WORD_GAP = SPEED_BASE * 7;

    /**
     * The characters from 'A' to 'Z'
     */
    private static final long[][] LETTERS = new long[][]{
        /* A */ new long[]{DOT, GAP, DASH},
        /* B */ new long[]{DASH, GAP, DOT, GAP, DOT, GAP, DOT},
        /* C */ new long[]{DASH, GAP, DOT, GAP, DASH, GAP, DOT},
        /* D */ new long[]{DASH, GAP, DOT, GAP, DOT},
        /* E */ new long[]{DOT},
        /* F */ new long[]{DOT, GAP, DOT, GAP, DASH, GAP, DOT},
        /* G */ new long[]{DASH, GAP, DASH, GAP, DOT},
        /* H */ new long[]{DOT, GAP, DOT, GAP, DOT, GAP, DOT},
        /* I */ new long[]{DOT, GAP, DOT},
        /* J */ new long[]{DOT, GAP, DASH, GAP, DASH, GAP, DASH},
        /* K */ new long[]{DASH, GAP, DOT, GAP, DASH},
        /* L */ new long[]{DOT, GAP, DASH, GAP, DOT, GAP, DOT},
        /* M */ new long[]{DASH, GAP, DASH},
        /* N */ new long[]{DASH, GAP, DOT},
        /* O */ new long[]{DASH, GAP, DASH, GAP, DASH},
        /* P */ new long[]{DOT, GAP, DASH, GAP, DASH, GAP, DOT},
        /* Q */ new long[]{DASH, GAP, DASH, GAP, DOT, GAP, DASH},
        /* R */ new long[]{DOT, GAP, DASH, GAP, DOT},
        /* S */ new long[]{DOT, GAP, DOT, GAP, DOT},
        /* T */ new long[]{DASH},
        /* U */ new long[]{DOT, GAP, DOT, GAP, DASH},
        /* V */ new long[]{DOT, GAP, DOT, GAP, DOT, GAP, DASH},
        /* W */ new long[]{DOT, GAP, DASH, GAP, DASH},
        /* X */ new long[]{DASH, GAP, DOT, GAP, DOT, GAP, DASH},
        /* Y */ new long[]{DASH, GAP, DOT, GAP, DASH, GAP, DASH},
        /* Z */ new long[]{DASH, GAP, DASH, GAP, DOT, GAP, DOT},
    };

    /**
     * The characters from '0' to '9'
     */
    private static final long[][] NUMBERS = new long[][]{
        /* 0 */ new long[]{DASH, GAP, DASH, GAP, DASH, GAP, DASH, GAP, DASH},
        /* 1 */ new long[]{DOT, GAP, DASH, GAP, DASH, GAP, DASH, GAP, DASH},
        /* 2 */ new long[]{DOT, GAP, DOT, GAP, DASH, GAP, DASH, GAP, DASH},
        /* 3 */ new long[]{DOT, GAP, DOT, GAP, DOT, GAP, DASH, GAP, DASH},
        /* 4 */ new long[]{DOT, GAP, DOT, GAP, DOT, GAP, DOT, GAP, DASH},
        /* 5 */ new long[]{DOT, GAP, DOT, GAP, DOT, GAP, DOT, GAP, DOT},
        /* 6 */ new long[]{DASH, GAP, DOT, GAP, DOT, GAP, DOT, GAP, DOT},
        /* 7 */ new long[]{DASH, GAP, DASH, GAP, DOT, GAP, DOT, GAP, DOT},
        /* 8 */ new long[]{DASH, GAP, DASH, GAP, DASH, GAP, DOT, GAP, DOT},
        /* 9 */ new long[]{DASH, GAP, DASH, GAP, DASH, GAP, DASH, GAP, DOT},
    };

    /**
     * Duration in milliseconds of a pause when the character is not one we support
     */
    private static final long[] ERROR_GAP = new long[]{GAP};

    /**
     * Return the pattern data for a given character. We first check if the parameter {@code char c}
     * is between 'A' and 'Z' and if so we return the array in {@code LETTERS} that is indexed by the
     * value {@code c - 'A'}, then we check if the parameter {@code char c} is between 'a' and 'z'
     * and if so we return the array in {@code LETTERS} that is indexed by the value {@code c - 'a'},
     * then we check if the parameter {@code char c} is between '0' and '9' and if so we return the
     * array in {@code NUMBERS} that is indexed by the value {@code c - '0'}. If it is not one of the
     * above we return the array {@code ERROR_GAP}.
     *
     * @param c Character that is being converted to Morse code
     * @return an array of {@code long} that contains the Morse code for the parameter {@code c}
     */
    static long[] pattern(char c) {
        if (c >= 'A' && c <= 'Z') {
            return LETTERS[c - 'A'];
        }
        if (c >= 'a' && c <= 'z') {
            return LETTERS[c - 'a'];
        } else if (c >= '0' && c <= '9') {
            return NUMBERS[c - '0'];
        } else {
            return ERROR_GAP;
        }
    }

    /**
     * Return the pattern data for a given string. We declare our flag {@code boolean lastWasWhitespace},
     * and store the length of our parameter {@code str} in {@code strlen}.
     * <p>
     * Next we proceed to calculate how big our return array of {@code long[]} needs to be, by first
     * initializing {@code len} to 1 (the length needed), and setting {@code lastWasWhitespace} to
     * true. Then we loop through all the {@code char c} in {@code str}, checking whether {@code c}
     * is a white space character, and if so we check whether the previous character was also a
     * white space character skipping it if so, otherwise incrementing {@code len} and setting
     * {@code lastWasWhitespace} to true. If {@code c} was not a white space character and the
     * previous character was also not a white space character we increment {@code len}, then for all
     * non white space characters we then set {@code lastWasWhitespace} to false and add the length
     * of the Morse code pattern returned for {@code c} by our method {@code pattern(char)} to {@code len}.
     * <p>
     * Now that we know how long our array needs to be we allocate {@code len+1} longs for our variable
     * {@code long[] result}, set {@code result[0]} to 0 (the initial pause of the vibration pattern),
     * set {@code int pos} to 1 (next storage location in {@code result[]}), and set {@code lastWasWhitespace}
     * to true. Then once again looping through all the {@code char c} in {@code str}, we first check
     * if {@code c} is a white space character, and if so we check whether the previous character was
     * also a white space character skipping it if so, otherwise setting {@code result[pos]} to
     * {@code WORD_GAP} incrementing {@code pos} and setting {@code lastWasWhitespace} to true. If
     * {@code c} was not a white space character and the previous character was a white space character
     * we set {@code result[pos]} to the array {@code LETTER_GAP} and increment {@code pos} before setting
     * {@code lastWasWhitespace} to true, and {@code long[] letter} to the array of Morse code that our
     * method {@code pattern(char)} returns for {@code c}. We then copy the contents of the array
     * {@code letter} to our array {@code result} starting at location {@code pos} and add the length
     * of {@code letter} to {@code pos}.
     * <p>
     * When done with all the characters in {@code str} we return {@code result} to the caller.
     *
     * @param str a string which needs to be converted to Morse code
     * @return an array of {@code long} that contains the Morse code for the parameter {@code str}
     */
    static long[] pattern(String str) {
        /*
         * Flag to indicate that the previous character was a "white space" character
         */
        boolean lastWasWhitespace;
        /*
         * Number of characters in our parameter {@code str}
         */
        int strlen = str.length();

        // Calculate how long our array needs to be.
        int len = 1;
        lastWasWhitespace = true;
        for (int i = 0; i < strlen; i++) {
            char c = str.charAt(i);
            if (Character.isWhitespace(c)) {
                if (!lastWasWhitespace) {
                    len++;
                    lastWasWhitespace = true;
                }
            } else {
                if (!lastWasWhitespace) {
                    len++;
                }
                lastWasWhitespace = false;
                len += pattern(c).length;
            }
        }

        // Generate the pattern array.  Note that we put an extra element of 0
        // in at the beginning, because the pattern always starts with the pause,
        // not with the vibration.
        long[] result = new long[len + 1];
        result[0] = 0;
        int pos = 1;
        lastWasWhitespace = true;
        for (int i = 0; i < strlen; i++) {
            char c = str.charAt(i);
            if (Character.isWhitespace(c)) {
                if (!lastWasWhitespace) {
                    result[pos] = WORD_GAP;
                    pos++;
                    lastWasWhitespace = true;
                }
            } else {
                if (!lastWasWhitespace) {
                    result[pos] = LETTER_GAP;
                    pos++;
                }
                lastWasWhitespace = false;
                long[] letter = pattern(c);
                System.arraycopy(letter, 0, result, pos, letter.length);
                pos += letter.length;
            }
        }
        return result;
    }
}
