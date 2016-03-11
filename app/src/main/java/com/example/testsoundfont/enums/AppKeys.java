package com.example.testsoundfont.enums;

/**
 * Created by Administrator on 2016-03-08.
 */
public enum AppKeys {
    ALL_SONG("all_song"),
    ALL_SOUNDFONT("all_soundfont"),
    SELECTED_SONG("selected_song"),
    SELECTED_SOUNDFONT("selected_soundfont");

    private String key;

    AppKeys(String msg) {
        key = msg;
    }
}
