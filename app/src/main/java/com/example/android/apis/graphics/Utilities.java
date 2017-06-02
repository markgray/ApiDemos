package com.example.android.apis.graphics;

import android.content.res.Resources;

/**
 * Contains important graphics utility methods
 */
@SuppressWarnings("WeakerAccess")
public class Utilities {
    public static float d2p(float dpi) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return  dpi * scale;
    }
    public static int id2p(int dpi) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpi * scale);
    }
}
