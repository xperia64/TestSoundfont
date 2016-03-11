package com.example.testsoundfont.views.adapters.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * description
 * ListView에서 사용되는 기본 BaseAdapter를 한번 더 Wrapping 해서 중복메소드를 방지하기위한 기본 클래스
 * @author JintaePang
 */
public abstract class ListBaseAdapter<D> extends BaseAdapter {
    private List<D> mItemList = new ArrayList<>();
    private Context mContext;

    public ListBaseAdapter(Context context) {
        this.mContext = context;
    }

    public ListBaseAdapter(Context context, List<D> itemList) {
        this.mContext = context;
        this.mItemList = itemList;
    }

    @Override
    public int getCount() {
        return mItemList != null ? mItemList.size() : 0;
    }

    @Override
    public D getItem(int position) {
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = createCell(position, parent);
        }
        bindCell(convertView, position);

        return convertView;
    }

    protected abstract View createCell(int position, ViewGroup parent);

    protected abstract void bindCell(View convertView, int position);

    public Context getContext() {
        return this.mContext;
    }

    public void addAll(List<D> itemList) {
        this.mItemList.addAll(itemList);
        notifyDataSetChanged();
    }

    public void add(D item) {
        this.mItemList.add(item);
        notifyDataSetChanged();
    }

    public void remove(D item) {
        this.mItemList.remove(item);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        this.mItemList.remove(position);
        notifyDataSetChanged();
    }

    public void removeAll() {
        this.mItemList.clear();
        notifyDataSetChanged();
    }

    public void refresh(Collection<D> collection) {
        this.mItemList.clear();
        this.mItemList.addAll(collection);
        notifyDataSetChanged();
    }

    public boolean isEmptyList() {
        return this.mItemList == null || this.mItemList.isEmpty();
    }

    public List<D> getItemList() {
        return this.mItemList;
    }
}
