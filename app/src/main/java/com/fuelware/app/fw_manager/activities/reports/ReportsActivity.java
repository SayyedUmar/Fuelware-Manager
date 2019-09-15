package com.fuelware.app.fw_manager.activities.reports;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.fuelware.app.fw_manager.BuildConfig;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportsActivity extends SuperActivity {


    @BindView(R.id.linlayAccount)
    LinearLayout linlayAccount;
    @BindView(R.id.linlayCashierOutput)
    LinearLayout linlayCashierOutput;
    @BindView(R.id.linlayManagerOuput)
    LinearLayout linlayManagerOuput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reports);
        ButterKnife.bind(this);

        setupBackNavigation(null);
        setTitle("My Reports");

        initialise();
        setEventListeners();

        //hide the account statement
        if (!BuildConfig.DEBUG)
            linlayAccount.setVisibility(View.GONE);
    }

    private void initialise() {

    }

    private void setEventListeners() {
        linlayAccount.setOnClickListener(v -> startActivity(new Intent(this, AccountStatementActivity.class)));
        linlayCashierOutput.setOnClickListener(v -> startActivity(new Intent(this, CashierReportActivity.class)));
        linlayManagerOuput.setOnClickListener(v -> startActivity(new Intent(this, ManagerReportActivity.class)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
