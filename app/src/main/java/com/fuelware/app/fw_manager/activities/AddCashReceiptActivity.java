package com.fuelware.app.fw_manager.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddCashReceiptActivity extends SuperActivity {


    @BindView(R.id.btnAdd)
    Button btnAdd;
    @BindView(R.id.tvAutoBusiness)
    AutoCompleteTextView tvAutoBusiness;
    @BindView(R.id.etCustomerName)
    EditText etCustomerName;
    @BindView(R.id.etCustomerMobile)
    EditText etCustomerMobile;
    @BindView(R.id.etCustomerEmail)
    EditText etCustomerEmail;
    @BindView(R.id.etRemark)
    EditText etRemark;
    @BindView(R.id.etAmount)
    EditText etAmount;
    @BindView(R.id.etReceiptNumber)
    EditText etReceiptNumber;
    @BindView(R.id.chkReceiptManual)
    CheckBox chkReceiptManual;
    @BindView(R.id.linlayReceiptNumber)
    LinearLayout linlayReceiptNumber;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_cash_receipts);
        ButterKnife.bind(this);

        setupBackNavigation(null);
        setTitle("Cash Receipts Activity");

        initialize();
        setEventListeners();

        getCreditCustomers();
    }

    private void getCreditCustomers() {

    }

    private void initialize() {

    }

    private void setEventListeners() {
        chkReceiptManual.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                linlayReceiptNumber.setVisibility(View.VISIBLE);
            } else {
                linlayReceiptNumber.setVisibility(View.GONE);
            }
        });

        btnAdd.setOnClickListener(v -> {
            if (isValid()) {
                addReceipt();
            }
        });
    }

    private void addReceipt() {

    }

    private boolean isValid() {
        return false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
