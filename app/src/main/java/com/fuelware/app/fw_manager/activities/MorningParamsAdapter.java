package com.fuelware.app.fw_manager.activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.models.ProductPriceModel;
import com.fuelware.app.fw_manager.utils.MyUtils;

import java.util.ArrayList;
import java.util.List;

public class MorningParamsAdapter extends RecyclerView.Adapter<MorningParamsAdapter.MyViewHolder>  {
    private List<ProductPriceModel> records = new ArrayList<>();
    private List<ProductPriceModel> origialRecords ;
    private Context mContext;

    public MorningParamsAdapter(List<ProductPriceModel> records, Context mContext) {
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
    public MorningParamsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_morning_param, parent, false);

        return new MorningParamsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MorningParamsAdapter.MyViewHolder holder, int position) {

        position = holder.getAdapterPosition();

        final ProductPriceModel model = records.get(position);
        holder.tvProduct.setText(model.getProduct());
        String price = holder.etConfirmRate.getText().toString().trim();
        model.setPrice(MyUtils.parseDouble(price));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvProduct;
        EditText etRate, etConfirmRate;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.tvProduct = itemView.findViewById(R.id.tvProduct);
            this.etRate = itemView.findViewById(R.id.etRate);
            this.etConfirmRate = itemView.findViewById(R.id.etConfirmRate);
            etRate.setFilters(new InputFilter[] {new MyUtils.InputFilterMinMax(1, 50000)});
            etConfirmRate.setFilters(new InputFilter[] {new MyUtils.InputFilterMinMax(1, 50000)});

            etConfirmRate.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void afterTextChanged(Editable editable) {
                    String cRate = editable.toString().trim();
                    String rate = etRate.getText().toString().trim();
                    if (rate.isEmpty()) {
                        etRate.setError("Enter Rate First.");
                        etRate.requestFocus();
                    } else if (!cRate.equals(rate)) {
                        etConfirmRate.setError("Rate and Confirm rate must be same.");
                        etConfirmRate.requestFocus();
                    }  else {
                        etConfirmRate.setError(null);
                        etConfirmRate.requestFocus();
                    }
                }
            });
        }

    }

    public void filter (String newText) {
        newText = newText.trim().toLowerCase();
        records.clear();
        if (newText.isEmpty()) {
            records.addAll(origialRecords);
        } else {
            /*for (ReceiptModel item : origialRecords) {
                if (item.getVehicle_number().toLowerCase().startsWith(newText)) {
                    records.add(item);
                } else if (item.getIndent_number().toLowerCase().startsWith(newText)) {
                    records.add(item);
                }
            }*/
        }
        notifyDataSetChanged();
    }
}