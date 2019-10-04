package com.fuelware.app.fw_manager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class GeneriBaseAdapter<T> extends BaseAdapter {

    private LayoutInflater inflater;
    private List<T> list;
    private int layoutId;
    private DailogListener listener;

    public GeneriBaseAdapter (List<T> list, int layoutId, DailogListener listener) {
        this.layoutId = layoutId;
        this.list = list;
        this.listener = listener;
    }

    private LayoutInflater getInflator (ViewGroup parent) {
        if(inflater == null){
            Context context = parent.getContext();
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        return inflater;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = getInflator(parent).inflate(layoutId, parent, false);
            convertView.setTag(listener.getHolder(convertView, position));
        }
        listener.setView(convertView, position);
//        convertView.setOnClickListener(v -> listener.setView(v, position));
        return convertView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public T getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public interface DailogListener<T> {
        T getHolder(View v, int pos);
        void setView(View v, int pos);
//        void onClick(View v, int pos);
    }
}
