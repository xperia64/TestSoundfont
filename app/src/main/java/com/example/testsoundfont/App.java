package com.example.testsoundfont;

import android.app.Application;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.net.Uri;

import com.example.testsoundfont.enums.AppKeys;
import com.example.testsoundfont.enums.PrefsKeys;
import com.example.testsoundfont.models.Song;
import com.example.testsoundfont.models.SoundFont;
import com.example.testsoundfont.util.Directory;
import com.example.testsoundfont.util.Logger;
import com.example.testsoundfont.util.Serializer;
import com.pixplicity.easyprefs.library.Prefs;

import org.androidannotations.annotations.EApplication;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Administrator on 2016-03-08.
 */
@EApplication
public class App extends Application {

    private static App application;

    // Object들을 저장해두는 Map
    private HashMap<AppKeys, Object> mAppStorage = new HashMap<>();

    // List<Object>들을 저장해두는 Map
    private HashMap<String, List<? extends Object>> mAppListStorage = new LinkedHashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        JNIHandler.getInstance(this);

        loadLibraries();
        loadSoundFontsFromSdcard();
        loadMidiFilesFromAssets();
    }

    private void loadLibraries() {
        try {
            System.loadLibrary("timidityhelper");
            if(JNIHandler.loadLib(Directory.getLibDir(this)+"libtimidityplusplus.so") < 0) {
                throw new RuntimeException("Cannot load timidityplusplus");
            }

            Logger.i("success load timidityhelper with timidityplusplus");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSoundFontsFromSdcard() {
        File soundFonts = new File(JNIHandler.getInstance(this).getRootStorage().getAbsolutePath() + "/soundfonts/");
        ArrayList<SoundFont> sfs = null;
        try {
            sfs = (ArrayList<SoundFont>) Serializer.deserialize(Prefs.getString(PrefsKeys.SOUND_FONT.name(), Serializer.serialize(new ArrayList<SoundFont>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(sfs == null) {
            sfs = new ArrayList<>();
        }
        for(File file : soundFonts.listFiles()) {
            if(!file.getName().endsWith(".sf2")) {
                continue;
            }
            boolean alreadyExist = false;
            for(SoundFont sf : sfs) {
                if(file.getName().equals(sf.getSfName())) {
                    alreadyExist = true;
                }
            }
            if(alreadyExist) {
                continue;
            }

            sfs.add(new SoundFont(file.getName(), file.getParent() + "/", false));
            //Logger.i("Sdcard: ", uri.getPath().substring(0, uri.getPath().lastIndexOf('/' + 1)));
            //Logger.i("Sdcard: ", uri.getLastPathSegment());
        }

        Prefs.putString(PrefsKeys.SOUND_FONT.name(), Serializer.serialize(sfs));
        //this.setValue(AppKeys.ALL_SOUNDFONT, sfs);
    }

    private void loadSoundFontsFromAssets() {
        AssetManager am = getAssets();
        String path = "sf";

        try {
            String list[] = am.list(path);
            if (list != null) {
                List<SoundFont> sfs = (List<SoundFont>) App.getInstance().getObjectList(AppKeys.ALL_SOUNDFONT);
                if(sfs == null) {
                    sfs = new ArrayList<>();
                }
                for (int i = 0; i < list.length; ++i) {
                    if(!list[i].endsWith(".sf2")) {
                        continue;
                    }

                    File file = new File(list[i]);
                    sfs.add(new SoundFont(file.getName(), file.getParent() + "/", false));
                    Logger.i("Assets: ", path + "/" + list[i]);
                }

                this.setValue(AppKeys.ALL_SOUNDFONT, sfs);
            }
        } catch (IOException e) {
            Logger.i("List error:", "can't list" + path);
        }
    }

    private void loadMidiFilesFromAssets() {
        AssetManager am = getAssets();
        String path = "midi";

        try {
            String list[] = am.list(path);
            if (list != null) {
                List<Song> songs = new ArrayList<>();
                for (int i = 0; i < list.length; ++i) {
                    if(!list[i].endsWith(".mid")) {
                        continue;
                    }

                    Uri uri = Uri.parse("file:///android_asset/" + path + "/" + list[i]);
                    songs.add(new Song(uri.getLastPathSegment().substring(0, uri.getLastPathSegment().lastIndexOf(".")), uri.getPath(), uri));
                    Logger.i("Assets: ", path + "/" + list[i]);
                }

                this.setValue(AppKeys.ALL_SONG, songs);
            }
        } catch (IOException e) {
            Logger.i("List error:", "can't list" + path);
        }
    }

    public static App getInstance() {
        return application;
    }

    public Object getValue(String key) {
        return mAppStorage.get(key);
    }

    public Object getValue(AppKeys key, boolean deleteFlag) {
        Object value = mAppStorage.get(key);
        if (deleteFlag) {
            if (value != null)
                mAppStorage.remove(key);
        }
        return value;
    }

    public List<? extends Object> getObjectList(String key, boolean delFlag) {
        List<? extends Object> value = mAppListStorage.get(key);
        if (delFlag) {
            if (value != null)
                mAppListStorage.remove(key);
        }
        return value;
    }

    public List<? extends Object> getObjectList(AppKeys key) {
        return mAppListStorage.get(key.name());
    }

    public void setValue(AppKeys key, List<? extends Object> value) {
        mAppListStorage.put(key.name(), value);
    }

    public void setValue(AppKeys key, Object value) {
        mAppStorage.put(key, value);
    }
}
