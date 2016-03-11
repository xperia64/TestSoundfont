package com.example.testsoundfont.views.adapters;

import android.content.Context;
import android.view.ViewGroup;

import com.example.testsoundfont.App;
import com.example.testsoundfont.enums.AppKeys;
import com.example.testsoundfont.enums.PrefsKeys;
import com.example.testsoundfont.models.SoundFont;
import com.example.testsoundfont.util.Serializer;
import com.example.testsoundfont.views.adapters.base.RecyclerViewBaseAdapter;
import com.example.testsoundfont.views.cells.SoundFontCell;
import com.example.testsoundfont.views.cells.SoundFontCell_;
import com.example.testsoundfont.views.cells.base.CellWrapper;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016-03-09.
 */
public class SoundFontListAdapter extends RecyclerViewBaseAdapter<SoundFont, SoundFontCell> {

    public SoundFontListAdapter(Context context) {
        super(context);

        try {
            this.addAll((ArrayList<SoundFont>) Serializer.deserialize(Prefs.getString(PrefsKeys.SOUND_FONT.name(), Serializer.serialize(new ArrayList<>()))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ///this.addAll((List<SoundFont>) App.getInstance().getObjectList(AppKeys.ALL_SOUNDFONT));
    }

    @Override
    protected SoundFontCell onCreateItemView(ViewGroup parent, int viewType) {
        return SoundFontCell_.build(getContext());
    }

    @Override
    public void onBindViewHolder(CellWrapper<SoundFontCell> baseCell, int position) {
        super.onBindViewHolder(baseCell, position);
        SoundFontCell cell = baseCell.getView();
        cell.bind(getItem(position));
    }
}
