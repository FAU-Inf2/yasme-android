package de.fau.cs.mad.yasme.android.controller;

import de.fau.cs.mad.yasme.android.BuildConfig;


/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 13.08.2014.
 */
public class Log {
    static final boolean LOG_I = BuildConfig.DEBUG;
    static final boolean LOG_E = BuildConfig.DEBUG;
    static final boolean LOG_D = BuildConfig.DEBUG;
    static final boolean LOG_V = BuildConfig.DEBUG;
    static final boolean LOG_W = BuildConfig.DEBUG;

    public static void i(String tag, String string) {
        if (LOG_I) android.util.Log.i(tag, string);
    }
    public static void e(String tag, String string) {
        if (LOG_E) android.util.Log.e(tag, string);
    }
    public static void d(String tag, String string) {
        if (LOG_D) android.util.Log.d(tag, string);
    }
    public static void v(String tag, String string) {
        if (LOG_V) android.util.Log.v(tag, string);
    }
    public static void w(String tag, String string) {
        if (LOG_W) android.util.Log.w(tag, string);
    }
}
