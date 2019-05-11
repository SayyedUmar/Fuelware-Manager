package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fuelware.app.fw_manager.Const.AppConst;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.models.IndentModel;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.utils.MyUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceiptsActivity extends SuperActivity {


    @BindView(R.id.linlayCashReceipt)
    LinearLayout linlayCashReceipt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_receipts);
        ButterKnife.bind(this);

        setupBackNavigation(null);
        setTitle("Receipts Activity");

        initialise();
        setEventListeners();


    }

    private void initialise() {

    }


    private void setEventListeners() {
        linlayCashReceipt.setOnClickListener(v -> {
            startActivity(new Intent(this, CashReceiptActivity.class));
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
