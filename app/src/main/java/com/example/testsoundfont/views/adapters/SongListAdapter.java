package com.example.testsoundfont.views.adapters;

import android.content.Context;
import android.view.ViewGroup;

import com.example.testsoundfont.App;
import com.example.testsoundfont.enums.AppKeys;
import com.example.testsoundfont.enums.PrefsKeys;
import com.example.testsoundfont.models.Song;
import com.example.testsoundfont.util.Serializer;
import com.example.testsoundfont.views.adapters.base.RecyclerViewBaseAdapter;
import com.example.testsoundfont.views.cells.SongCell;
import com.example.testsoundfont.views.cells.SongCell_;
import com.example.testsoundfont.views.cells.base.CellWrapper;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016-03-08.
 */
public class SongListAdapter extends RecyclerViewBaseAdapter<Song, SongCell> {

    public SongListAdapter(Context context) {
        super(context);

        try {
            this.addAll((ArrayList<Song>) Serializer.deserialize(Prefs.getString(PrefsKeys.SONG.name(), Serializer.serialize(new ArrayList<Song>()))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //this.addAll((List<Song>) App.getInstance().);
    }

    @Override
    protected SongCell onCreateItemView(ViewGroup parent, int viewType) {
        return SongCell_.build(getContext());
    }

    @Override
    public void onBindViewHolder(CellWrapper<SongCell> baseCell, int position) {
        super.onBindViewHolder(baseCell, position);
        SongCell cell = baseCell.getView();
        cell.bind(getItem(position));
    }
}
