package com.fuelware.app.fw_manager.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.fuelware.app.fw_manager.R;

import java.util.ArrayList;
import java.util.List;

public class CustomAutoCompleteAdapter<T> extends BaseAdapter implements Filterable {

    Context context;

    private final Object mLock = new Object();
    private List<T> records;
    private List<T> originalRecords;
    private ArrayFilter mFilter;
    private OnItemSelectClickListener mSelectListener;
    private OnItemDeleteClickListener mDeleteListener;
    private SearchAdapterListener searchAdapterListener;

    private LayoutInflater mInflater;

    public interface OnItemSelectClickListener {
        public void onItemSelectClicked();
    }

    public interface OnItemDeleteClickListener {
        public void onItemDeleteClicked();
    }

    public interface SearchAdapterListener<T> {
        void getView(int position, MCustomHolder holder, T item);
        public List<T> onPerformFiltering(String searchText);
    }


    /**
     * Set listener for clicks on the footer item
     */
    /*public void setOnItemSelectClickListener(OnItemSelectClickListener listener) {
        mSelectListener = listener;
    }*/

    /*public void setOnItemDeleteClickListener(OnItemDeleteClickListener listener) {
        mDeleteListener = listener;
    }*/

    public CustomAutoCompleteAdapter(Context context, List<T> list, SearchAdapterListener type) {
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        originalRecords = list;
        records = new ArrayList<>(list);
        this.context = context;
        searchAdapterListener = type;
    }

    public void refresh () {
        records.clear();
        records.addAll(originalRecords);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public T getItem(int position) {
        return records.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(final int position, View convertView, ViewGroup parent){

        MCustomHolder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView =  inflater.inflate(R.layout.custom_adapter_row, parent, false);
            holder = new MCustomHolder(convertView);
            convertView.setTag(holder);
            holder.itemDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    records.remove(getItem(position));
                    notifyDataSetChanged();
                    Toast.makeText(context, "Removed successfully", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
           holder = (MCustomHolder) convertView.getTag();
        }
        searchAdapterListener.getView(position, holder, records.get(position));

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    //filter items which does not contain typed text
    private class ArrayFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {

            FilterResults results = new FilterResults();
            List<T> newValues = null;

            if (prefix == null || prefix.length() == 0) {
                synchronized (mLock) {
                    newValues = new ArrayList<>(originalRecords);
                }
            } else {
                String prefixString = prefix.toString().toLowerCase();
                newValues = searchAdapterListener.onPerformFiltering(prefixString);
            }

            results.values = newValues;
            results.count = newValues.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            records.clear();
            records.addAll((List<T>) results.values);
            notifyDataSetChanged();
        }


    }


    public static class MCustomHolder {
        public TextView itemName, itemDel;
        public MCustomHolder(View view) {
            itemName = view.findViewById(R.id.itemName);
            itemDel = view.findViewById(R.id.itemDel);
        }
    }

}
