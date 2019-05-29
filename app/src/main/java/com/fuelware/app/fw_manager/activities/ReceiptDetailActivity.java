package com.fuelware.app.fw_manager.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.appconst.Const;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.models.ReceiptModel;
import com.fuelware.app.fw_manager.utils.MyUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReceiptDetailActivity extends SuperActivity {


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


    @BindView(R.id.trBankName)
    TableRow trBankName;
    @BindView(R.id.trBranch)
    TableRow trBranch;
    @BindView(R.id.trChequeDate)
    TableRow trChequeDate;
    @BindView(R.id.trChequeNumber)
    TableRow trChequeNumber;

    @BindView(R.id.tvBankName)
    TextView tvBankName;
    @BindView(R.id.tvBranch)
    TextView tvBranch;
    @BindView(R.id.tvChequeDate)
    TextView tvChequeDate;
    @BindView(R.id.chequeNumber)
    TextView chequeNumber;


    private ReceiptModel model;
    private String receiptType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_receipts_details);
        ButterKnife.bind(this);

        setupBackNavigation(null);

        receiptType = getIntent().getStringExtra(Const.RECEIPT_TYPE);

        if (receiptType.equals(Const.RECEIPT_CHEQUE)) {
            setTitle("Cheque Receipt Details");
        } else if (receiptType.equals(Const.RECEIPT_CASH)) {
            setTitle("Cash Receipt Details");
        } else if (receiptType.equals(Const.RECEIPT_CREDIT)) {
            setTitle("Credit Receipt Details");
        } else if (receiptType.equals(Const.RECEIPT_DEBIT)) {
            setTitle("Debit Receipt Details");
        } else if (receiptType.equals(Const.RECEIPT_LOYALTY)) {
            setTitle("Loyalty Receipt Details");
        }


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

        tvBankName.setText(model.getBank());
        tvBranch.setText(model.getBranch());
        tvChequeDate.setText(MyUtils.dateToString(AppConst.SERVER_DATE_FORMAT, AppConst.APP_DATE_FORMAT, model.getCheque_date()));
        chequeNumber.setText(model.getCheque_number());

        if (receiptType.equals(Const.RECEIPT_CHEQUE)) {
            trBankName.setVisibility(View.VISIBLE);
            trBranch.setVisibility(View.VISIBLE);
            trChequeDate.setVisibility(View.VISIBLE);
            trChequeNumber.setVisibility(View.VISIBLE);
        } else {
            trBankName.setVisibility(View.GONE);
            trBranch.setVisibility(View.GONE);
            trChequeDate.setVisibility(View.GONE);
            trChequeNumber.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
