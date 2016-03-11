package com.example.testsoundfont.util;

import android.util.Log;

/**
 * Created by Administrator on 2016-03-08.
 */
public class Logger {

    private final static String TAG = Logger.class.getSimpleName();

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void i(String msg) {
        i(TAG, msg);
    }
}
