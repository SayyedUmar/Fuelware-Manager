package com.fuelware.app.fw_manager.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.models.PlanHistory;
import com.fuelware.app.fw_manager.utils.MyUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlanHistoryAdapter extends RecyclerView.Adapter<PlanHistoryAdapter.MyViewHolder>  {
    private List<PlanHistory> records = new ArrayList<>();
    private List<PlanHistory> origialRecords ;
    private Context mContext;

    public PlanHistoryAdapter(List<PlanHistory> records, Context mContext) {
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
    public PlanHistoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_plan_history, parent, false);

        return new PlanHistoryAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanHistoryAdapter.MyViewHolder holder, int position) {

        position = holder.getAdapterPosition();
        PlanHistory model = records.get(position);
        holder.tvStartDate.setText("Start: " + MyUtils.dateToString(AppConst.SERVER_DATE_FORMAT, AppConst.APP_DATE_FORMAT, model.getStart_date()));
        holder.tvEndDate.setText("End: " + MyUtils.dateToString(AppConst.SERVER_DATE_FORMAT, AppConst.APP_DATE_FORMAT, model.getEnd_date()));
        holder.tvInvoiceNumber.setText(model.getInvoice_num());
        holder.tvPrice.setText(MyUtils.formatCurrency(model.getFinal_price()));
        holder.tvPlanType.setText(model.getPlan());
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvStartDate)
        TextView tvStartDate;
        @BindView(R.id.tvEndDate)
        TextView tvEndDate;
        @BindView(R.id.tvInvoiceNumber)
        TextView tvInvoiceNumber;
        @BindView(R.id.tvPrice)
        TextView tvPrice;
        @BindView(R.id.tvStatus)
        TextView tvStatus;
        @BindView(R.id.tvPlanType)
        TextView tvPlanType;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
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