package com.fuelware.app.fw_manager.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.EindentListActivity;
import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.models.IndentModel;
import com.fuelware.app.fw_manager.utils.MyUtils;

import java.util.ArrayList;
import java.util.List;

public class EindentListAdapter extends RecyclerView.Adapter<EindentListAdapter.MyViewHolder> {
    private List<IndentModel> records = new ArrayList<>();
    private List<IndentModel> origialRecords ;
    private Context mContext;


    public EindentListAdapter(List<IndentModel> list, EindentListActivity mContext) {
        this.origialRecords = list;
        this.mContext = mContext;
        this.records.addAll(list);
    }

    public void refresh () {
        records.clear();
        records.addAll(origialRecords);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EindentListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_eindent, parent, false);

        return new EindentListAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        position = holder.getAdapterPosition();

        holder.tvSerailNumber.setText((position+1)+"");

        final IndentModel indentModel = records.get(position);
        holder.tvBusinessName.setText(indentModel.getBusiness());
        holder.tvIndentDate.setText(MyUtils.dateToString(AppConst.SERVER_DATE_FORMAT, AppConst.APP_DATE_FORMAT, indentModel.getDate_of_indent()));
        holder.tvIndentNumber.setText(indentModel.getIndent_number());
        holder.tvVehicleNumber.setText(indentModel.getVehicle_number());
        holder.tvProduct.setText(indentModel.getProduct());

        String status = indentModel.getStatus().equalsIgnoreCase("Filled") ? "Acknowledged" : indentModel.getStatus();
        holder.tvStatus.setText(MyUtils.toTitleCase(status));


        if (indentModel.getStatus().equalsIgnoreCase("pending")) {
            holder.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.error_color));
            holder.tvStatus.setVisibility(View.GONE);
            holder.tvLabelStatus.setVisibility(View.GONE);

            if (indentModel.getFill_type().equalsIgnoreCase("full_tank")) {
                String text = " | " + "<font color=#f5695e>Full Tank</font>";
                holder.tvQuantity.setText(Html.fromHtml(text));
            } else if(indentModel.getFill_type().equalsIgnoreCase("litre")) {
                String text = " | " + "<font color=#f5695e>"+MyUtils.parseDouble(indentModel.getLitre())+" L</font>";
                holder.tvQuantity.setText(Html.fromHtml(text));
            } else if (indentModel.getFill_type().equalsIgnoreCase("amount")) {
                String text = " | " + "<font color=#f5695e>"+MyUtils.formatCurrency(indentModel.getAmount())+"</font>";
                holder.tvQuantity.setText(Html.fromHtml(text));
            }
        } else {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvLabelStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.green_light_color1));

            String text = " | " + "<font color=#f5695e>"+MyUtils.formatCurrency(indentModel.getAmount())+"</font>";
            holder.tvQuantity.setText(Html.fromHtml(text));
        }

    }




    @Override
    public int getItemCount() {
        return records.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvBusinessName, tvIndentDate, tvIndentNumber, tvVehicleNumber;
        TextView tvProduct, tvStatus, tvSerailNumber, tvBlacklistMsg,
                tvLabelStatus, tvQuantity;
        MyViewHolder(View v) {
            super(v);
            this.tvBusinessName = v.findViewById(R.id.tvBusinessName);
            this.tvIndentDate = v.findViewById(R.id.tvIndentDate);
            this.tvIndentNumber = v.findViewById(R.id.tvIndentNumber);
            this.tvVehicleNumber = v.findViewById(R.id.tvVehicleNumber);
            this.tvProduct = v.findViewById(R.id.tvProduct);
            this.tvStatus = v.findViewById(R.id.tvStatus);
            this.tvSerailNumber = v.findViewById(R.id.tvSerailNumber);
            this.tvBlacklistMsg = v.findViewById(R.id.tvBlacklistMsg);
            this.tvLabelStatus = v.findViewById(R.id.labelStatus);
            this.tvQuantity = v.findViewById(R.id.tvQuantity);
        }
    }

    public void filter(String newText) {
        newText = newText.trim().toLowerCase();
        records.clear();
        if (newText.isEmpty()) {
            records.addAll(origialRecords);
        } else {
            for (IndentModel item : origialRecords) {
                if (item.getVehicle_number().toLowerCase().startsWith(newText)) {
                    records.add(item);
                } else if (item.getIndent_number().toLowerCase().startsWith(newText)) {
                    records.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }
}
