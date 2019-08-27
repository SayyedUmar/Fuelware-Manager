package com.fuelware.app.fw_manager.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.plans.PlansActivity;
import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.models.PurchasedPlan;
import com.fuelware.app.fw_manager.utils.MyUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyPlansAdapter extends RecyclerView.Adapter<MyPlansAdapter.MyViewHolder>  {
    private List<PurchasedPlan> records = new ArrayList<>();
    private List<PurchasedPlan> origialRecords ;
    private Context mContext;

    public MyPlansAdapter(List<PurchasedPlan> records, Context mContext) {
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
    public MyPlansAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_my_plans, parent, false);

        return new MyPlansAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPlansAdapter.MyViewHolder holder, int position) {

        position = holder.getAdapterPosition();
        PurchasedPlan model = records.get(position);
        holder.tvStartDate.setText("Start: " + MyUtils.dateToString(AppConst.SERVER_DATE_FORMAT, AppConst.APP_DATE_FORMAT, model.start_date));
        holder.tvEndDate.setText("End: " + MyUtils.dateToString(AppConst.SERVER_DATE_FORMAT, AppConst.APP_DATE_FORMAT, model.end_date));
        holder.tvPrice.setText(MyUtils.formatCurrency(model.price));

        String text2 = "Duration: <font color=#64bc88><big>" +model.subscriptionDetail.plan.duration+" Days</big></font>";
        holder.tvDuration.setText(Html.fromHtml(text2), TextView.BufferType.SPANNABLE);

        int finalPosition = position;
        View.OnClickListener clickListener = v -> {
            ((PlansActivity)mContext).activatePlan(finalPosition, model);
        };
        if (model.is_plan_activated == 1) {
            holder.tvActivate.setText("Activated");
            holder.tvActivate.setBackgroundResource(R.drawable.rec_round_green_button_30);
            holder.tvActivate.setOnClickListener(null);
        } else {
            holder.tvActivate.setText("Active");
            holder.tvActivate.setBackgroundResource(R.drawable.rec_round_blue_signin_button);
            holder.tvActivate.setOnClickListener(clickListener);
        }
        if (model.subscriptionDetail.plan.has_sms == 1) {
            holder.tvSms.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_check_circle, 0);
        } else {
            holder.tvSms.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_error, 0);
        }
        String text1 = "Total Indents: <font color=#64bc88><big>" +model.subscriptionDetail.plan.indents+"</big></font>";
        holder.tvIndentTotal.setText(Html.fromHtml(text1), TextView.BufferType.SPANNABLE);

        String text = "Remaining Indents: <font color=#e47164><big>" +model.remaining_indents+"</big></font>";
        holder.tvIndentPending.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);

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
        @BindView(R.id.tvPrice)
        TextView tvPrice;
        @BindView(R.id.tvSms)
        TextView tvSms;
        @BindView(R.id.tvActivate)
        TextView tvActivate;

        @BindView(R.id.tvDuration)
        TextView tvDuration;
        @BindView(R.id.tvIndentTotal)
        TextView tvIndentTotal;
        @BindView(R.id.tvIndentPending)
        TextView tvIndentPending;

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