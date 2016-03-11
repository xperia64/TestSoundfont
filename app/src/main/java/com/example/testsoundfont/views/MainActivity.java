package com.example.testsoundfont.views;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.testsoundfont.App;
import com.example.testsoundfont.R;
import com.example.testsoundfont.enums.AppKeys;
import com.example.testsoundfont.models.Song;
import com.example.testsoundfont.util.Logger;
import com.example.testsoundfont.views.adapters.SongListAdapter;
import com.example.testsoundfont.views.adapters.base.RecyclerViewBaseAdapter;
import com.example.testsoundfont.views.cells.SongCell;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity implements RecyclerViewBaseAdapter.adapterListener<Song,SongCell> {

    private String TAG = MainActivity.class.getSimpleName();

    @ViewById(R.id.rv_vertical_linear)
    RecyclerView rv_songs;

    @Override
    public void afterViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SongListAdapter adapter = new SongListAdapter(this);
        adapter.setAdapterListener(this);

        rv_songs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_songs.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(SongCell view, Song item, int position) {
        Logger.i(TAG, "clicked. song name : " + item.getSongName() + " position : " + position);

        App.getInstance().setValue(AppKeys.SELECTED_SONG, item);
        startActivity(SoundFontActivity_.class);
    }
}
