package com.example.testsoundfont.views.cells;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.testsoundfont.R;
import com.example.testsoundfont.models.SoundFont;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Administrator on 2016-03-09.
 */
@EViewGroup(R.layout.cell_sf)
public class SoundFontCell extends RelativeLayout {

    @ViewById(R.id.tv_title)
    TextView tv_sfName;

    @ViewById(R.id.cb_uses)
    CheckBox cb_uses;

    public SoundFontCell(Context context) {
        this(context, null);
    }

    public SoundFontCell(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SoundFontCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @AfterViews
    public void afterViews() {
        this.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void bind(final SoundFont sf) {
        tv_sfName.setText(sf.getSfName());
        cb_uses.setChecked(sf.isUsing());
        cb_uses.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sf.setUsing(isChecked);
            }
        });
    }
}
