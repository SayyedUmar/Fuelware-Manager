package com.fuelware.app.fw_manager.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public abstract class SpinnerAdapterNew<T> extends BaseAdapter {

    List<T> list;


    public SpinnerAdapterNew(List<T> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) { //custom_spinner_adapter
            convertView = onCreateView(position, convertView, parent);
        }
        onBindView(position, list.get(position), convertView);

        return convertView;
    }

    public abstract View onCreateView(int pos, View view, ViewGroup parent);
    public abstract void onBindView(int pos, T t, View v);

}
