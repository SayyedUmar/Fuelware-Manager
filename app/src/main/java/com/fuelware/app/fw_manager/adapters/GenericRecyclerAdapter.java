package com.fuelware.app.fw_manager.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class GenericRecyclerAdapter<T, P extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<ViewHolder>  {
    private List<T> records = new ArrayList<>();
    private List<T> origialRecords ;
    private Context mContext;

    public GenericRecyclerAdapter(List<T> records, Context mContext) {
        this.origialRecords = records;
        this.mContext = mContext;
        this.records.addAll(records);
    }

    public void refresh () {
        records.clear();
        records.addAll(origialRecords);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    abstract public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    @Override
    abstract public void onBindViewHolder(@NonNull ViewHolder viewHolder, int pos) ;

    @Override
    public int getItemCount() {
        return records.size();
    }

    public class MyViewHolder extends ViewHolder {
        public MyViewHolder(View itemView) {
            super(itemView);
        }
    }

    public void filter (String newText) {
        newText = newText.trim().toLowerCase();
        records.clear();
        if (newText.isEmpty()) {
            records.addAll(origialRecords);
        } else {
            onfilter(newText, origialRecords);
        }
        notifyDataSetChanged();
    }

    abstract void onfilter (String searchText, List<T> records);
}
