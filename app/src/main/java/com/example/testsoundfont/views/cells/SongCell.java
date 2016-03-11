package com.example.testsoundfont.views.cells;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.testsoundfont.R;
import com.example.testsoundfont.models.Song;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Administrator on 2016-03-08.
 */
@EViewGroup(R.layout.cell_song)
public class SongCell extends LinearLayout {

    @ViewById(R.id.tv_title)
    TextView tv_songTitle;

    public SongCell(Context context) {
        this(context, null);
    }

    public SongCell(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SongCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @AfterViews
    public void afterViews() {
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void bind(Song song) {
        tv_songTitle.setText(song.getSongName());
    }
}
