package com.fuelware.app.fw_manager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.LinearLayout;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportsActivity extends SuperActivity {


    @BindView(R.id.linlayAccount)
    LinearLayout linlayAccount;
    @BindView(R.id.linlayBatchOutput)
    LinearLayout linlayBatchOutput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reports);
        ButterKnife.bind(this);

        setupBackNavigation(null);
        setTitle("My Reports");

        initialise();
        setEventListeners();


    }

    private void initialise() {
        linlayAccount.setOnClickListener(v -> startActivity(new Intent(this, AccountStatementActivity.class)));
    }

    private void setEventListeners() {

    }
}
