package com.example.testsoundfont.views.cells.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * description
 * RecyclerView에 쓰이게될 Cell <extends View)들을 Wrapping해주는 Class
 * RecyclerView Cell -> ListView Cell
 * ListView Cell -> RecyclerViewCell 가능하게 해줌.
 * @author JintaePang
 */
public class CellWrapper<C extends View> extends RecyclerView.ViewHolder {
    private C mRootView;

    public CellWrapper(C itemView) {
        super(itemView);
        mRootView = itemView;
    }

    public C getView() {
        return mRootView;
    }
}