package com.example.testsoundfont.views.adapters.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.example.testsoundfont.views.cells.base.CellWrapper;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * description
 * RecyclerView를 사용할때 Adapter에 쓰이게되는 공통 메소드 및 구조를 잡아둔 Class
 * CellWrapper와 관계성이 뚜렷함.
 *
 * @author JintaePang
 */
public abstract class RecyclerViewBaseAdapter<D, C extends View> extends RecyclerView.Adapter<CellWrapper<C>> {
    private ArrayList<D> mItemList = new ArrayList<>();
    private Context mContext;

    @Setter
    @Getter
    private adapterListener<D, C> adapterListener;

    public RecyclerViewBaseAdapter(Context context) {
        if (context == null)
            throw new IllegalStateException("context == null");
        this.mContext = context;
    }

    public RecyclerViewBaseAdapter(Context context, ArrayList<D> itemList) {
        this.mContext = context;
        this.mItemList = itemList;
    }

    @Override
    public CellWrapper<C> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CellWrapper<>(onCreateItemView(parent, viewType));
    }

    protected abstract C onCreateItemView(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(CellWrapper<C> baseCell, int position) {
        baseCell.getView().setTag(position);
        baseCell.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterListener.onItemClick((C)v, getItem((Integer) v.getTag()), (Integer) v.getTag());
            }
        });

    }

    @Override
    public int getItemCount() {
        return mItemList != null ? mItemList.size() : 0;
    }

    protected D getItem(int position) {
        return this.mItemList.get(position);
    }

    public Context getContext() {
        return this.mContext;
    }

    public void add(D item) {
        if (item == null) return;
        this.mItemList.add(item);
        notifyDataSetChanged();
    }

    public void addAll(List<D> itemList) {
        if (itemList == null) return;
        this.mItemList.addAll(itemList);
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

    public void refresh(List<D> itemList) {
        this.mItemList.clear();
        if (itemList != null) {
            this.mItemList.addAll(itemList);
        }
        notifyDataSetChanged();
    }

    public boolean isEmptyList() {
        return mItemList == null || mItemList.isEmpty();
    }

    public ArrayList<D> getItemList() {
        return this.mItemList;
    }

    public interface adapterListener<D, C> {
        void onItemClick(C view, D item, int position);
    }
}
