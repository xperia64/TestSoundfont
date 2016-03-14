package com.example.testsoundfont;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.widget.Toast;

import com.example.testsoundfont.enums.PrefsKeys;
import com.example.testsoundfont.models.SoundFont;
import com.example.testsoundfont.util.Logger;
import com.example.testsoundfont.util.Serializer;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Created by Administrator on 2016-03-08.
 */
public class JNIHandler {

    public static final String autoSoundfontHeader = "#<--------Config Generated By Timidity AE (DO NOT MODIFY)-------->";
    public static String[] sampls = { "Cubic Spline", "Lagrange", "Gaussian", "Newton", "Linear", "None" };

    private Context c;

    @Getter
    private AudioTrack track;

    @Getter
    private File rootStorage = null;

    public static void init(Context c) {
        instance.c = c;
        instance.rootStorage = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/vpang_test/");
        if (!instance.rootStorage.exists()) {
            instance.rootStorage.mkdir();
        }

        File sfDir = new File(instance.rootStorage.getAbsolutePath() + "/soundfonts/");
        if (!sfDir.exists()) {
            sfDir.mkdir();
        }

        for(int i=0; i<TimiditySettings.maxChannel; i++) {
            TimiditySettings.drums.add(i == 9);
            TimiditySettings.volumes.add(75); // Assuming not XG
            TimiditySettings.programs.add(0);
        }

        instance.track = new AudioTrack(AudioManager.STREAM_MUSIC, TimiditySettings.rate,
                (TimiditySettings.mono == 2) ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO,
                (TimiditySettings.isSixteenBits) ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT, TimiditySettings.buffer,
                AudioTrack.MODE_STREAM);

        instance.loadTimidityConfig();
        instance.prepareTimidity(instance.rootStorage.getAbsolutePath(), instance.rootStorage.getAbsolutePath() + "/" + "timidity.cfg", 0, 0, 1, 1, 0, 1);
    }

    public void loadTimidityConfig() {
        List<SoundFont> sfs = null;
        try {
            sfs = (ArrayList<SoundFont>) Serializer.deserialize(Prefs.getString(PrefsKeys.SOUND_FONT.name(), Serializer.serialize(new ArrayList<SoundFont>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (sfs == null || sfs.size() == 0) {
            Toast.makeText(c, "Soundfonts null", Toast.LENGTH_LONG).show();
            return;
        }

        writeCfg(c, rootStorage.getAbsolutePath() + "/timidity.cfg", sfs);
    }

    public void writeCfg(Context c, String path, List<SoundFont> sfs) {
        if(path == null) {
            Toast.makeText(c, "Configuration path null", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            FileWriter fw = new FileWriter(path, false);
            fw.write(autoSoundfontHeader);
            fw.write("\n");

            for(SoundFont sf : sfs) {
                fw.write("soundfont \"" + sf.getFilePath() + sf.getSfName() + "\"" + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JNIHandler instance = null;
    public static JNIHandler getInstance(Context context) {
        if(instance == null) {
            instance = new JNIHandler();
            instance.init(context);
        }
        return instance;
    }

    public static void buffitWrite(byte[] data, int length) {
        Logger.i("welcome buffitWrite");
        if (TimiditySettings.mono == 0) {
            byte[] mono = new byte[length / 2];
            if (TimiditySettings.isSixteenBits) {
                for (int i = 0; i < mono.length / 2; ++i) {
                    int HI = 1;
                    int LO = 0;
                    int left = (data[i * 4 + HI] << 8) | (data[i * 4 + LO] & 0xff);
                    int right = (data[i * 4 + 2 + HI] << 8) | (data[i * 4 + 2 + LO] & 0xff);
                    int avg = (left + right) / 2;
                    mono[i * 2 + HI] = (byte) ((avg >> 8) & 0xff);
                    mono[i * 2 + LO] = (byte) (avg & 0xff);
                }
            } else {
                for (int i = 0; i < mono.length; ++i) {
                    int left = (data[i * 2]) & 0xff;
                    int right = (data[i * 2 + 1]) & 0xff;
                    int avg = (left + right) / 2;
                    mono[i] = (byte) (avg);
                }
            }
            try {
                instance.track.write(mono, 0, mono.length);
                Logger.i("track write1");
            } catch (IllegalStateException e) {
            }
        } else {
            try {
                instance.track.write(data, 0, length);
                Logger.i("track write2");
            } catch (IllegalStateException e) {
            }
        }
    }

    public static void flushIt() {
        instance.track.flush();
    }

    public static void finishIt() {
        /*ultSafetyCheck = 1;
        Globals.isPlaying = 1;*/
    }

    public static int bufferSize() {
        return instance.track.getPlaybackHeadPosition() * instance.track.getChannelCount()
                * (2 - (instance.track.getAudioFormat() & 1));
    }

    public static void initMaxtime(int maxTime) {
        TimiditySettings.maxTime = maxTime;
    }

    public static void updateDrumInfo(int ch, int isDrum) {
        if (ch < TimiditySettings.maxChannel)
            TimiditySettings.drums.set(ch, (isDrum != 0));
    }

    public static void updateTempo(int t, int tr) {
        TimiditySettings.tt = t;
        TimiditySettings.ttr = tr;
        // TODO something
        // int x = (int) (500000 / (double) t * 120 * (double) tr / 100 + 0.5);
        // System.out.println("T: "+t+ " TR: "+tr+" X: "+x);
    }

    public static void updateLyrics(byte[] b) {
        final StringBuilder stb = new StringBuilder(TimiditySettings.currentLyric);
        final StringBuilder tmpBuild = new StringBuilder();
        boolean isNormalLyric = b[0] == 'L';
        boolean isNewline = b[0] == 'N';
        boolean isComment = b[0] == 'Q';

        for (int i = 2; i < b.length; i++) {
            if (b[i] == 0)
                break;
            tmpBuild.append((char) b[i]);
        }
        if (isComment) // commentsAlways get newlines
        {
            stb.append(tmpBuild);
            stb.append('\n');
            TimiditySettings.overwriteLyricAt = stb.length();
        } else if (isNewline || isNormalLyric) {
            if (isNewline) {
                stb.append('\n');
                TimiditySettings.overwriteLyricAt = stb.length();
            }
            stb.replace(TimiditySettings.overwriteLyricAt, stb.length(), tmpBuild.toString());
        } else { // A marker or something
            stb.append(tmpBuild);
            stb.append("\n");
            TimiditySettings.overwriteLyricAt = stb.length();
        }
        TimiditySettings.currentLyric = stb.toString();
    }

    public static void updateVolInfo(int ch, int vol) {
        if (ch < TimiditySettings.maxChannel)
            TimiditySettings.volumes.set(ch, vol);
    }

    public static void updateTime(int currentTime, int voice) {
        TimiditySettings.currentTime = currentTime;
        TimiditySettings.voice = voice;
    }

    public static void updateProgramInfo(int ch, int prog) {
        if (ch < TimiditySettings.maxChannel)
            TimiditySettings.programs.set(ch, prog);
    }

    public static void updateKey(int k) {
        TimiditySettings.keyOffset = k;
    }



    public static void controlMe(int y) {
		/*
		 * String[] control = { "PM_REQ_MIDI",
		 *
		 * "PM_REQ_INST_NAME",
		 *
		 * "PM_REQ_DISCARD",
		 *
		 * "PM_REQ_FLUSH",
		 *
		 * "PM_REQ_GETQSIZ",
		 *
		 * "PM_REQ_SETQSIZ",
		 *
		 * "PM_REQ_GETFRAGSIZ",
		 *
		 * "PM_REQ_RATE",
		 *
		 * "PM_REQ_GETSAMPLES",
		 *
		 * "PM_REQ_PLAY_START",
		 *
		 * "PM_REQ_PLAY_END",
		 *
		 * "PM_REQ_GETFILLABLE",
		 *
		 * "PM_REQ_GETFILLED",
		 *
		 * "PM_REQ_OUTPUT_FINISH",
		 *
		 * "PM_REQ_DIVISIONS" };
		 */
        if (y == 10) {
            // Globals.isPlaying = 1; // Wait until all is unloaded.
            if (instance.track != null) {
                instance.track.stop();
            }
            instance.track.release();
        }
    }

    public static int getRate() {
        return instance.track.getSampleRate();
    }

    public static void updateMaxChannels(int channels) {
        TimiditySettings.maxChannel = channels;
    }

    public static void updateMaxVoice(int vvv) {
        TimiditySettings.maxVoice = vvv;
    }

    public static int play(Context context, String songTitle) {
        if(!new File(songTitle).exists()) {
            return -9;
        }

        Logger.i("song");
        instance.track.play();
        loadSongTimidity(songTitle);
        Logger.i("welcome play");

        return 0;
    }

    private static byte[] intToByteArray(int i) {
        byte[] b = new byte[4];
        b[0] = (byte) (i & 0xFF);
        b[1] = (byte) ((i >> 8) & 0xFF);
        b[2] = (byte) ((i >> 16) & 0xFF);
        b[3] = (byte) ((i >> 24) & 0xFF);
        return b;
    }

    public static byte[] shortToByteArray(short data) {
        return new byte[] { (byte) (data & 0xff), (byte) ((data >>> 8) & 0xff) };
    }

    private static void finishOutput() {

    }

    public static native void controlTimidity(int jcmd, int jcmdArg); //Timidity옵션을 컨트롤하는 소스. 안쓰일듯
    public static native void setChannelTimidity(int jchan, int jprog);
    public static native int setResampleTimidity(int jcustResamp);
    public static native int loadLib(String libPath);
    public static native boolean timidityReady();

    private static native int loadSongTimidity(String filename);
    private static native int prepareTimidity(String config, String config2, int jmono, int jcustResamp, int jsixteen,
                                              int jPresSil, int jreloading, int jfreeInsts);

    public static native int unloadLib();
    public static native void setChannelVolumeTimidity(int jchan, int jvol);
    public static native int decompressSFArk(String from, String to);
}
