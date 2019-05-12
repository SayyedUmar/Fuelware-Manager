package com.fuelware.app.fw_manager.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.TextView;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.models.ReceiptModel;
import com.fuelware.app.fw_manager.utils.MyUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EwalletDetailActivity  extends SuperActivity {




    @BindView(R.id.tvReceiptNo)
    TextView tvReceiptNo;
    @BindView(R.id.tvBusinessName)
    TextView tvBusinessName;
    @BindView(R.id.tvCustomerName)
    TextView tvCustomerName;
    @BindView(R.id.tvMobile)
    TextView tvMobile;
    @BindView(R.id.tvEmail)
    TextView tvEmail;
    @BindView(R.id.tvRemark)
    TextView tvRemark;
    @BindView(R.id.tvAmount)
    TextView tvAmount;


    @BindView(R.id.tvTransID)
    TextView tvTransID;
    @BindView(R.id.tvEwalletName)
    TextView tvEwalletName;


    private ReceiptModel model;
    private boolean isChequeReceipt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ewallet_details);
        ButterKnife.bind(this);

        setupBackNavigation(null);

        setTitle("E-Wallet Details");

        model = getIntent().getParcelableExtra("CASH_RECEIPT_MODEL");


        showData();
    }

    private void showData() {
        tvReceiptNo.setText(model.getReceipt_number());
        tvBusinessName.setText(model.getBusiness());
        tvCustomerName.setText(model.getName());
        tvMobile.setText(model.getMobile());
        tvEmail.setText(model.getEmail());
        tvRemark.setText(model.getRemark());
        tvAmount.setText(MyUtils.formatCurrency(model.getAmount()));

        tvEwalletName.setText(model.getEwallet());
        tvTransID.setText(model.getTransaction_number());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}