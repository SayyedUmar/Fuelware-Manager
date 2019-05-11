package com.fuelware.app.fw_manager.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.EditMIndentActivity;
import com.fuelware.app.fw_manager.activities.MiDetailsActivity;
import com.fuelware.app.fw_manager.activities.MindentListActivity;
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

        if (indentModel.getApprove_status().equalsIgnoreCase("approved")) {
            holder.tvApprove.setVisibility(View.GONE);
            holder.linlayEditDelete.setVisibility(View.VISIBLE);
        } else {
            holder.tvApprove.setVisibility(View.VISIBLE);
            holder.linlayEditDelete.setVisibility(View.GONE);
        }

        holder.tvApprove.setOnClickListener(v -> {
            Dialog dialog = new Dialog(mContext);
            dialog.setContentView(R.layout.dialog_cofirm_action);
            TextView btnNo = dialog.findViewById(R.id.btnNo);
            TextView btnYes = dialog.findViewById(R.id.btnYes);
            dialog.setCancelable(true);
            btnNo.setOnClickListener(w -> dialog.dismiss());
            btnYes.setOnClickListener(w -> {
                ((MindentListActivity)mContext).approveMIndent(dialog, indentModel, holder.getAdapterPosition());
            });
            dialog.show();
        });

        holder.imgDelete.setOnClickListener(view -> {
            Dialog dialog = new Dialog(mContext);
            dialog.setContentView(R.layout.dialog_cofirm_action);
            TextView tvTitle = dialog.findViewById(R.id.tvTitle);
            TextView tvMessage = dialog.findViewById(R.id.tvMessage);
            tvTitle.setText("Confirm Delete");
            tvMessage.setText("Are you surely want to delete ?");
            TextView btnNo = dialog.findViewById(R.id.btnNo);
            TextView btnYes = dialog.findViewById(R.id.btnYes);
            dialog.setCancelable(true);
            btnNo.setOnClickListener(w -> dialog.dismiss());
            btnYes.setOnClickListener(w -> {
                dialog.dismiss();
                ((MindentListActivity)mContext).requestOTP(indentModel, holder.getAdapterPosition());
            });
            dialog.show();
        });

        holder.imgEdit.setOnClickListener(view -> {
            view.getContext().startActivity(new Intent(view.getContext(), EditMIndentActivity.class)
                    .putExtra("INDENT_MODEL",indentModel)
                    .putExtra(RxBus.POSITION, holder.getAdapterPosition()));
        });

    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvBusinessName, tvCustomerName, tvIndentNumber, tvVehicleNumber;
        TextView tvProduct, tvApprove, tvSerailNumber, tvBlacklistMsg;
        RelativeLayout rellayBlackList;
        ImageView imgEdit, imgDelete;
        LinearLayout linlayEditDelete;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.tvBusinessName = itemView.findViewById(R.id.etBusinessName);
            this.tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            this.tvIndentNumber = itemView.findViewById(R.id.tvReceiptNo);
            this.tvVehicleNumber = itemView.findViewById(R.id.tvVehicleNumber);
            this.tvProduct = itemView.findViewById(R.id.tvAmount);
            this.tvApprove = itemView.findViewById(R.id.tvApprove);
            this.tvSerailNumber = itemView.findViewById(R.id.tvSerailNumber);
            this.tvBlacklistMsg = itemView.findViewById(R.id.tvBlacklistMsg);
            this.rellayBlackList = itemView.findViewById(R.id.rellayBlackList);
            this.imgEdit = itemView.findViewById(R.id.imgEdit);
            this.imgDelete = itemView.findViewById(R.id.imgClose);
            this.linlayEditDelete = itemView.findViewById(R.id.linlayEditDelete);

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
