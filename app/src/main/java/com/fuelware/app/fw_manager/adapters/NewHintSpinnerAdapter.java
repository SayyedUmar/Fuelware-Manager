package com.fuelware.app.fw_manager.adapters;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * @author Chintan.rathod
 * An Adapter which will show inital text like "Select Country" or something else intead of first element of spinner.
 */
public class NewHintSpinnerAdapter implements SpinnerAdapter, ListAdapter {

    protected static final int PIVOT = 1;

    protected SpinnerAdapter adapter;

    protected Context context;

    protected int hintLayout;

    protected LayoutInflater layoutInflater;

    private String hintText = "";

    /**
     * Use this constructor to have NO 'Select One...' item, instead use
     * the standard prompt or nothing at all.
     * @param spinnerAdapter wrapped Adapter.
     * @param hintLayout layout for nothing selected, perhaps
     * you want text grayed out like a prompt...
     * @param context
     */
    public NewHintSpinnerAdapter(
            SpinnerAdapter spinnerAdapter,
            int hintLayout, Context context) {

        this.adapter = spinnerAdapter;
        this.context = context;
        this.hintLayout = hintLayout;
        layoutInflater = LayoutInflater.from(context);
    }

    public NewHintSpinnerAdapter(
            SpinnerAdapter spinnerAdapter,
            int hintLayout, String hintText, Context context) {

        this(spinnerAdapter, hintLayout, context);
        this.hintText = hintText;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        TextView v = (TextView) adapter.getView(position - PIVOT, null, parent);
        v.setTextSize(14);
        if (position == 0) {
            v.setText(hintText);
        }
        return v; // Could re-use
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        TextView v = (TextView) adapter.getDropDownView(position - PIVOT, null, parent);
        if (position == 0) {
            v.setText(hintText);
        }
        return v;
    }


    @Override
    public int getCount() {
        int count = adapter.getCount();
        return count == 0 ? 0 : count + PIVOT;
    }

    @Override
    public Object getItem(int position) {
        return position == 0 ? null : adapter.getItem(position - PIVOT);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position >= PIVOT ? adapter.getItemId(position - PIVOT) : position - PIVOT;
    }

    @Override
    public boolean hasStableIds() {
        return adapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return adapter.isEmpty();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        adapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        adapter.unregisterDataSetObserver(observer);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0; // Don't allow the 'hint' item to be picked.
    }


}