package com.fuelware.app.fw_manager.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.widget.Button;

import com.fuelware.app.fw_manager.BuildConfig;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.models.PlanModel;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.utils.MyUtils;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TechSupportActivity extends SuperActivity {

    @BindView(R.id.btnUpload)
    Button btnUpload;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tech_support);
        ButterKnife.bind(this);

        setupBackNavigation(null);
        initialise();
        setEventListeners();
    }

    private void initialise() {

    }

    private void setEventListeners() {
        btnUpload.setOnClickListener(v -> {
            showImageOpenDialog();
            /*AndPermission.with(v.getContext())
                    .runtime()
                    .permission(Permission.Group.STORAGE)
                    .onGranted(permissions -> {

                    })
                    .onDenied(permissions -> {
                        MLog.showToast(v.getContext(), "Read/Write External Storage permission denied!");
                    })
                    .start();*/
        });
    }

    private void showImageOpenDialog() {
        DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(new ViewHolder(R.layout.upload_image_layout))
                .setCancelable(true)
                .setContentHeight(100)
                .setOnItemClickListener((dialog1, item, view, position) -> {
                    dialog1.dismiss();
                }).create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}