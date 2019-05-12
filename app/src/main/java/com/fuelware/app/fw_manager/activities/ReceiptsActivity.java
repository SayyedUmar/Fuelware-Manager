package com.fuelware.app.fw_manager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.fuelware.app.fw_manager.Const.Const;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReceiptsActivity extends SuperActivity {


    @BindView(R.id.linlayCashReceipt)
    LinearLayout linlayCashReceipt;
    @BindView(R.id.linlayChequeReceipt)
    LinearLayout linlayChequeReceipt;
    @BindView(R.id.linlayEwallet)
    LinearLayout linlayEwallet;
    @BindView(R.id.linlayOnlinePayment)
    LinearLayout linlayOnlinePayment;
    @BindView(R.id.linlayDebit)
    LinearLayout linlayDebit;
    @BindView(R.id.linlayCredit)
    LinearLayout linlayCredit;
    @BindView(R.id.linlayloyalty)
    LinearLayout linlayloyalty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_receipts);
        ButterKnife.bind(this);

        setupBackNavigation(null);
        setTitle("Receipts");

        initialise();
        setEventListeners();


    }

    private void initialise() {

    }


    private void setEventListeners() {
        linlayCashReceipt.setOnClickListener(v -> {
            startActivity(new Intent(this, ReceiptListActivity.class)
                    .putExtra(Const.RECEIPT_TYPE,  Const.RECEIPT_CASH));
        });
        linlayChequeReceipt.setOnClickListener(v -> {
            startActivity(new Intent(this, ChequeReceiptListActivity.class)
                    .putExtra(Const.RECEIPT_TYPE,  Const.RECEIPT_CHEQUE));
        });
        linlayEwallet.setOnClickListener(v -> {
            startActivity(new Intent(this, EwalletListActivity.class));
        });
        linlayOnlinePayment.setOnClickListener(v -> {
            startActivity(new Intent(this, OnlinePaymentListActivity.class));
        });
        linlayCredit.setOnClickListener(v -> {
            startActivity(new Intent(this, ReceiptListActivity.class)
                    .putExtra(Const.RECEIPT_TYPE,  Const.RECEIPT_CREDIT));
        });
        linlayDebit.setOnClickListener(v -> {
            startActivity(new Intent(this, ReceiptListActivity.class)
                    .putExtra(Const.RECEIPT_TYPE,  Const.RECEIPT_DEBIT));
        });
        linlayloyalty.setOnClickListener(v -> {
            startActivity(new Intent(this, ReceiptListActivity.class)
                    .putExtra(Const.RECEIPT_TYPE,  Const.RECEIPT_LOYALTY));
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
