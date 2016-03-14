package com.example.testsoundfont;

import android.media.AudioTrack;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-03-11.
 */
public class TimiditySettings {

    public static int mono = 2;
    public static boolean isSixteenBits = true;
    public static int rate = Integer.parseInt(Integer.toString(AudioTrack.getNativeOutputSampleRate(AudioTrack.MODE_STREAM)));
    public static int buffer = 192000;
    public static int maxChannel = 32;
    public static int keyOffset = 0;

    // tempo
    public static int tt = 0;
    public static int ttr = 0;

    public static int maxTime = 0;
    public static int currentTime = 0;

    public static int maxVoice = 0;
    public static int voice = 0;
    public static int overwriteLyricAt = 0;
    public static String currentLyric = "";

    public static ArrayList<Boolean> drums = new ArrayList<Boolean>();
    public static ArrayList<Integer> volumes = new ArrayList<Integer>();
    public static ArrayList<Integer> programs = new ArrayList<Integer>();
}
