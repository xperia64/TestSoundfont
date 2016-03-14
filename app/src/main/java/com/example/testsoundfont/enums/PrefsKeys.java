package com.example.testsoundfont.enums;

/**
 * Created by Administrator on 2016-03-11.
 */
public enum  PrefsKeys {

    SOUND_FONT("sound_font"),
    SONG("song");

    private String key;
    PrefsKeys(String soundFont) {
        key = soundFont;
    }
}
