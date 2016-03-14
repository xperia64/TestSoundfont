package com.example.testsoundfont.views;

import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;

import com.example.testsoundfont.App;
import com.example.testsoundfont.JNIHandler;
import com.example.testsoundfont.R;
import com.example.testsoundfont.TimiditySettings;
import com.example.testsoundfont.enums.AppKeys;
import com.example.testsoundfont.models.Song;
import com.example.testsoundfont.models.SoundFont;

import org.androidannotations.annotations.EActivity;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Administrator on 2016-03-09.
 */
@EActivity(R.layout.activity_player)
public class PlayerActivity extends BaseActivity {
    AudioTrack track;

    int rate = 48000;
    int buffer = 192000;

    Song selectedSong;
    SoundFont selectedSf;

    @Override
    public void afterViews() {
        selectedSong = (Song) App.getInstance().getValue(AppKeys.SELECTED_SONG, true);
        selectedSf = (SoundFont) App.getInstance().getValue(AppKeys.ALL_SONG.SELECTED_SOUNDFONT, true);

        JNIHandler.timidityReady();

        /*track = new AudioTrack(AudioManager.STREAM_MUSIC, rate,
                (TimiditySettings.mono == 2) ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO,
                (TimiditySettings.isSixteenBits) ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT, buffer,
                AudioTrack.MODE_STREAM);*/
        JNIHandler.play(this, selectedSong.getFilePath() + "/" + selectedSong.getSongName());
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
