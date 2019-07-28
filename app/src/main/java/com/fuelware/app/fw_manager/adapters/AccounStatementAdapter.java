package com.fuelware.app.fw_manager.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.AccountDetailActivity;
import com.fuelware.app.fw_manager.models.AccountModel;
import com.fuelware.app.fw_manager.models.TransactionTypeEnum;
import com.fuelware.app.fw_manager.utils.TouchImageView;

import java.util.ArrayList;
import java.util.List;

public class AccounStatementAdapter extends RecyclerView.Adapter<AccounStatementAdapter.MyViewHolder> implements Filterable {
    private List<AccountModel> records;
    private CustomFilter mFilter;
    private List<AccountModel> filteredList;
    private Context mContext;

    public void refresh() {
        filteredList.clear();
        filteredList.addAll(records);
        notifyDataSetChanged();
    }

//    public void filter(TransactionTypeEnum type) {
//        filteredList.clear();
//        if (type == TransactionTypeEnum.All) {
//            filteredList.addAll(records);
//        } else {
//
//            for (AccountModel item : records) {
//                if (item.getType().equalsIgnoreCase(type.toString())) {
//                    filteredList.add(item);
//                }
//            }
//        }
//
//        notifyDataSetChanged();
//    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView vehicleno_tx,indentno_tx,date_tx,debit_tx,credit_tx,balance_tx, tvSerailNumber;
        TextView tvVehicleNumberLabel, tvIndentNumberLabel, tvFuelingDateLabel;
        ImageView imgAttach;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.vehicleno_tx = itemView.findViewById(R.id.txn_vehicleno);
            this.indentno_tx = itemView.findViewById(R.id.txn_indentno);
            this.date_tx = itemView.findViewById(R.id.txn_date);
            this.debit_tx = itemView.findViewById(R.id.txn_debit);
            this.credit_tx = itemView.findViewById(R.id.txn_credit);
            this.balance_tx = itemView.findViewById(R.id.txn_balance);
            this.tvSerailNumber = itemView.findViewById(R.id.tvSerailNumber);

            this.tvIndentNumberLabel = itemView.findViewById(R.id.tvIndentNumberLabel);
            this.tvFuelingDateLabel = itemView.findViewById(R.id.tvFuelingDateLabel);
            this.tvVehicleNumberLabel = itemView.findViewById(R.id.tvVehicleNumberLabel);
            this.imgAttach = itemView.findViewById(R.id.imgAttach);
        }
    }

    public AccounStatementAdapter(List<AccountModel> data, Context mContext) {
        this.records = data;
        filteredList = new ArrayList<>();
        filteredList.addAll(data);
        mFilter = new CustomFilter(this);
        this.mContext = mContext;
    }

    @Override
    public AccounStatementAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //LAYOUT INFLATOR TO INFLATE CARDS LAYOUT
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_transaction_list, parent, false);

        return new AccounStatementAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AccounStatementAdapter.MyViewHolder holder, int listPosition) {
        listPosition = holder.getAdapterPosition();
        AccountModel model = filteredList.get(listPosition);

        holder.vehicleno_tx.setText(model.getVehicleNum().toUpperCase());
        holder.date_tx.setText(model.getFormattedFillDate());
        int i=listPosition+1;
        holder.tvSerailNumber.setText(""+i);

        if (model.getType().equalsIgnoreCase("credit")){
            holder.credit_tx.setText(model.getAmount());
            holder.debit_tx.setText("0.0");

            holder.tvIndentNumberLabel.setText("Receipt Number");
            holder.indentno_tx.setText(model.getReceipt_number());
            holder.tvFuelingDateLabel.setText("Receipt Date");
        } else {
            holder.credit_tx.setText("0.0");
            holder.debit_tx.setText(model.getAmount());

            holder.tvIndentNumberLabel.setText("Indent Number");
            holder.indentno_tx.setText(model.getIndentNum());
            holder.tvFuelingDateLabel.setText("Indent Date");
        }
        holder.date_tx.setText(model.getFormattedFillDate());

        String balance = model.getBalance();
        /*  String final_balance=balance.substring(0,1);*/
        //--------------math.max returns the max value of two arguments -------------//
        if (balance.length() > 2 && balance.substring(0,2).equalsIgnoreCase("Dr")){
            holder.balance_tx.setText(model.getBalance());
            holder.balance_tx.setTextColor(Color.parseColor("#f5695e"));
        } else {
//            holder.tvIndentNumberLabel.setText("");
//            holder.tvVehicleNumberLabel.setText("");
//            holder.tvFuelingDateLabel.setText("");

            holder.balance_tx.setText(model.getBalance());
            holder.balance_tx.setTextColor(Color.parseColor("#4CAF50"));
        }
        holder.itemView.setOnClickListener(v -> {
            //Intent intent = new Intent(v.getContext(), TransactionDetailActivity.class);
            Intent intent = new Intent(v.getContext(), AccountDetailActivity.class);
            intent.putExtra("account_model", model);
            v.getContext().startActivity(intent);
        });


        if (model.snap_bill_url != null && !model.snap_bill_url.isEmpty()) {
            holder.imgAttach.setVisibility(View.VISIBLE);
        } else {
            holder.imgAttach.setVisibility(View.GONE);
        }

        int finalListPosition = listPosition;
        holder.imgAttach.setOnClickListener(v -> {
            showFullPreviewPopup(v, model, finalListPosition);
        });

    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public class CustomFilter extends Filter {
        private RecyclerView.Adapter mAdapter;
        private CustomFilter(RecyclerView.Adapter mAdapter) {
            super();
            this.mAdapter = mAdapter;
        }
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();
            if (constraint.length() == 0) {
                filteredList.addAll(records);
            } else {
                final String filterPattern = constraint.toString().toLowerCase().trim();
                for (final AccountModel mWords : records) {
                    if (mWords.getIndentNum().toLowerCase().startsWith(filterPattern)) {
                        filteredList.add(mWords);
                    } else if (mWords.getVehicleNum().toLowerCase().startsWith(filterPattern)) {
                        filteredList.add(mWords);
                    } else if (mWords.getReceipt_number().toLowerCase().startsWith(filterPattern)) {
                        filteredList.add(mWords);
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            this.mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private void showFullPreviewPopup(View v, AccountModel model, int finalListPosition) {
        Dialog dialog = new Dialog(v.getContext());
        dialog.setContentView(R.layout.dialog_full_preview);
        dialog.setCancelable(true);
        dialog.show();
        TouchImageView imageView = dialog.findViewById(R.id.imageView);
        TextView tvClose = dialog.findViewById(R.id.tvClose);
        tvClose.setOnClickListener(v1 -> dialog.dismiss());
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        imageView.setImageResource(R.drawable.ic_placeholder);
        imageView.setMaxZoom(10);
        Glide.with(v.getContext()).load(model.snap_bill_url)
                .thumbnail(0.5f)
                .placeholder(R.drawable.ic_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView );
    }


}
