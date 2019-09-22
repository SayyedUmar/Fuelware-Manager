package com.fuelware.app.fw_manager.activities.reports;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.models.AccountModel;
import com.fuelware.app.fw_manager.utils.MyUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AccountDetailActivity extends SuperActivity {


    private TextView tvIndentNumberLabel, tvIndentNumber, tvInvoiceNumber, tvIndentDateLabel, tvIndentDate,
            tvVehicleNo, tvVehicleKms, tvProduct, tvRate, tvLitres, tvAmount, tvBalance,
            tvAmountLabel, tvRemark, tvBatchID;
    private LinearLayout linlay1, linlay2, linlayInvoiceNo, linlayBatchID, linlayVehicleKms;
    private AccountModel model;

    @BindView(R.id.tvBusinessName)
    TextView tvBusinessName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Account Details");
        setContentView(R.layout.activity_account_detail);
        ButterKnife.bind(this);
        setupBackNavigation(null);
        findViewById();
        model = getIntent().getParcelableExtra("account_model");
        setValues();
    }

    private void setValues() {

        tvIndentDate.setText(model.getFormattedFillDate());
        tvBusinessName.setText(model.getBusiness());
        if (model.getType().equalsIgnoreCase("credit")){
            tvIndentNumberLabel.setText("Receipt Number :");
            tvIndentNumber.setText(model.getReceipt_number());
            tvIndentDateLabel.setText("Receipt Date : ");
            tvAmountLabel.setText("Credit Amount");
            linlay1.setVisibility(View.VISIBLE);
            linlay2.setVisibility(View.GONE);
            linlayInvoiceNo.setVisibility(View.GONE);
            linlayBatchID.setVisibility(View.GONE);
        } else {
            String type = "(M) ";
            if (model.indent_type.equalsIgnoreCase("e-indent")) {
                type = "(E) ";
            } else if (model.indent_type.equalsIgnoreCase("b-indent")) {
                type = "(B) ";
            }
            tvIndentNumber.setText(type+model.getIndentNum());

            linlayInvoiceNo.setVisibility(View.VISIBLE);
            linlay1.setVisibility(View.GONE);
            linlay2.setVisibility(View.VISIBLE);
            tvIndentNumberLabel.setText("Indent Number :");
            tvIndentDateLabel.setText("Indent Date : ");
            tvAmountLabel.setText("Debit Amount");
            linlayBatchID.setVisibility(View.VISIBLE);
            tvBatchID.setText(model.batch_number);
        }

        tvInvoiceNumber.setText(model.invoice_number);

        tvVehicleNo.setText(model.getVehicleNum());
        tvVehicleKms.setText(model.vehicle_km);
        if (MyUtils.parseLong(model.vehicle_km) == 0) {
            linlayVehicleKms.setVisibility(View.GONE);
        }
        tvProduct.setText(model.product);
        tvRate.setText(MyUtils.formatCurrency(model.rate));
        tvLitres.setText(model.litre);

        tvAmount.setText(MyUtils.formatCurrency(model.getAmount()));
        tvBalance.setText(MyUtils.formatCurrency(model.getBalance()));

        tvRemark.setText(model.remark);
    }

    private void findViewById() {
        tvIndentNumberLabel = findViewById(R.id.tvIndentNumberLabel);
        tvIndentNumber = findViewById(R.id.tvIndentNumber);
        tvIndentDateLabel = findViewById(R.id.tvIndentDateLabel);
        tvIndentDate = findViewById(R.id.tvIndentDate);

        tvVehicleNo = findViewById(R.id.tvVehicleNo);
        tvVehicleKms = findViewById(R.id.tvVehicleKms);
        tvProduct = findViewById(R.id.tvProduct);
        tvRate = findViewById(R.id.tvRate);
        tvLitres = findViewById(R.id.tvLitres);
        tvAmountLabel = findViewById(R.id.tvAmountLabel);
        tvAmount = findViewById(R.id.tvAmount);
        tvBalance = findViewById(R.id.tvBalance);

        tvRemark = findViewById(R.id.tvRemark);
        linlay1 = findViewById(R.id.linlay1);
        linlay2 = findViewById(R.id.linlay2);
        tvInvoiceNumber = findViewById(R.id.tvInvoiceNumber);
        linlayInvoiceNo = findViewById(R.id.linlayInvoiceNo);
        linlayBatchID = findViewById(R.id.linlayBatchID);
        tvBatchID = findViewById(R.id.tvBatchID);
        linlayVehicleKms = findViewById(R.id.linlayVehicleKms);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
