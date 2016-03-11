package com.example.testsoundfont.views;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.testsoundfont.App;
import com.example.testsoundfont.R;
import com.example.testsoundfont.enums.AppKeys;
import com.example.testsoundfont.enums.PrefsKeys;
import com.example.testsoundfont.models.Song;
import com.example.testsoundfont.models.SoundFont;
import com.example.testsoundfont.util.Serializer;
import com.example.testsoundfont.views.adapters.SoundFontListAdapter;
import com.example.testsoundfont.views.adapters.base.RecyclerViewBaseAdapter;
import com.example.testsoundfont.views.cells.SoundFontCell;
import com.pixplicity.easyprefs.library.Prefs;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.Serializable;

/**
 * Created by Administrator on 2016-03-09.
 */
@EActivity(R.layout.activity_sf)
public class SoundFontActivity extends BaseActivity implements RecyclerViewBaseAdapter.adapterListener<SoundFont,SoundFontCell> {

    @ViewById(R.id.rv_vertical_linear)
    RecyclerView rv_soundfonts;

    SoundFontListAdapter adapter;

    Song selectedSong;

    @Override
    public void afterViews() {
        adapter = new SoundFontListAdapter(this);
        adapter.setAdapterListener(this);

        rv_soundfonts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_soundfonts.setAdapter(adapter);

        selectedSong = (Song) App.getInstance().getValue(AppKeys.SELECTED_SONG, true);
    }

    @Override
    public void onItemClick(SoundFontCell view, SoundFont item, int position) {
        App.getInstance().setValue(AppKeys.SELECTED_SONG, selectedSong);
        App.getInstance().setValue(AppKeys.SELECTED_SOUNDFONT, item);
        startActivity(PlayerActivity_.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Prefs.putString(PrefsKeys.SOUND_FONT.name(), Serializer.serialize(adapter.getItemList()));
    }
}
