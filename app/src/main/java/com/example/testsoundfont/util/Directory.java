package com.example.testsoundfont.util;

import android.content.Context;
import android.os.Build;

import java.io.File;

/**
 * Created by Administrator on 2016-03-09.
 */
public class Directory {

    public static String getLibDir(Context c) {
        String s = c.getApplicationInfo().nativeLibraryDir;
        if (!s.endsWith(File.separator)) {
            s += "/";
        }
        return s;
    }
}
