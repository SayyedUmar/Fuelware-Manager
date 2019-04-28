package com.fuelware.app.fw_manager.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.MiDetailsActivity;
import com.fuelware.app.fw_manager.models.IndentModel;
import com.fuelware.app.fw_manager.network.RxBus;
import com.fuelware.app.fw_manager.utils.MyUtils;

import java.util.ArrayList;
import java.util.List;

public class MindentListAdapter extends RecyclerView.Adapter<MindentListAdapter.MyViewHolder> {
    private List<IndentModel> records = new ArrayList<>();
    private List<IndentModel> origialRecords ;
    private Context mContext;

    public MindentListAdapter(List<IndentModel> records, Context mContext) {
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
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_mindent, parent, false);

        return new MindentListAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MindentListAdapter.MyViewHolder holder, int position) {

        position = holder.getAdapterPosition();
        holder.tvSerailNumber.setText((position+1)+"");

        final IndentModel indentModel = records.get(position);
        holder.tvBusinessName.setText(indentModel.getBusiness());
        holder.tvCustomerName.setText(indentModel.getCustomer_name());
        holder.tvIndentNumber.setText(indentModel.getIndent_number());
        holder.tvVehicleNumber.setText(indentModel.getVehicle_number());
        String text = indentModel.getProduct() + " |" + "<font color=#660E7A>"+MyUtils.formatCurrency(indentModel.getAmount())+"</font>";
        holder.tvProduct.setText(Html.fromHtml(text));
        holder.tvStatus.setText(MyUtils.toTitleCase(indentModel.getApprove_status()));

        if (indentModel.isHas_blacklisted()) {
            holder.rellayBlackList.setVisibility(View.VISIBLE);
        } else {
            holder.rellayBlackList.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), MiDetailsActivity.class);
            intent.putExtra("indent_id",""+indentModel.getId());
            intent.putExtra("indent_model", indentModel);
            intent.putExtra(RxBus.POSITION, holder.getAdapterPosition());
            view.getContext().startActivity(intent);
        });

        if (indentModel.getApprove_status().equalsIgnoreCase("pending")) {
            holder.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.error_color));
        } else {
            holder.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.green_light_color1));
        }
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvBusinessName, tvCustomerName, tvIndentNumber, tvVehicleNumber;
        TextView tvProduct, tvStatus, tvSerailNumber, tvBlacklistMsg;
        RelativeLayout rellayBlackList;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.tvBusinessName = itemView.findViewById(R.id.tvBusinessName);
            this.tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            this.tvIndentNumber = itemView.findViewById(R.id.tvIndentNumber);
            this.tvVehicleNumber = itemView.findViewById(R.id.tvVehicleNumber);
            this.tvProduct = itemView.findViewById(R.id.tvProduct);
            this.tvStatus = itemView.findViewById(R.id.tvStatus);
            this.tvSerailNumber = itemView.findViewById(R.id.tvSerailNumber);
            this.tvBlacklistMsg = itemView.findViewById(R.id.tvBlacklistMsg);
            this.rellayBlackList = itemView.findViewById(R.id.rellayBlackList);

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
