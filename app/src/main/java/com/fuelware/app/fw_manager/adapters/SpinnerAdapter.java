package com.fuelware.app.fw_manager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.fuelware.app.fw_manager.R;
import com.orhanobut.dialogplus.Holder;

import java.util.List;

public abstract class SpinnerAdapter<T> extends BaseAdapter {

    List<T> list;


    public SpinnerAdapter (List<T> list) {
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

        MyHolder holder;
        if (convertView == null) { //custom_spinner_adapter
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_spinner_adapter, parent, false);
            holder = new MyHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (MyHolder) convertView.getTag();
        }
        getView(position, holder);

        return convertView;
    }

    public abstract void getView(int pos, MyHolder holder);

    public static class MyHolder {
        public TextView textView;
        MyHolder(View v) {
            textView = v.findViewById(R.id.itemName);
        }
    }
}
