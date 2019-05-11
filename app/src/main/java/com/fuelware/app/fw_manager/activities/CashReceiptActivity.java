package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.fuelware.app.fw_manager.Const.AppConst;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.adapters.CashReceiptAdapter;
import com.fuelware.app.fw_manager.models.CashReceiptModel;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.utils.MyPreferences;
import com.fuelware.app.fw_manager.utils.MyUtils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CashReceiptActivity extends SuperActivity {


    private Gson gson;
    private CashReceiptAdapter adapter;
    private String authkey;

    @BindView(R.id.tvNoRecordsFound)
    TextView tvNoRecordsFound;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.btnAdd)
    FloatingActionButton btnAdd;

    private List<CashReceiptModel> list = new ArrayList<>();
    private AlertDialog progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cash_receipts);
        ButterKnife.bind(this);

        setupBackNavigation(null);
        setTitle("Cash Receipts Activity");

        initialize();
        setupRecycler();
        setEventListeners();
        getCashReceipts();

    }

    private void setupRecycler() {
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        adapter = new CashReceiptAdapter(list, this);
        recyclerView.setAdapter(adapter);
    }

    private void initialize() {
        gson = new Gson();
        progress = new SpotsDialog(this, R.style.Custom);
        authkey = MyPreferences.getStringValue(getApplicationContext(), "authkey");
    }

    private void setEventListeners() {
        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AddCashReceiptActivity.class)));
    }

    private void getCashReceipts() {
        if (!MyUtils.hasInternetConnection(getApplicationContext())) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        progress.show();

        APIClient.getApiService().getCashReceiptsList(authkey).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject result = new JSONObject(response.body().string());
                        JSONArray jsonArray = result.getJSONArray("data");
                        list.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            JSONObject customer = object.getJSONObject("customer");
                            CashReceiptModel model = gson.fromJson(object.toString(), CashReceiptModel.class);
                            model.setCustomer_id(customer.getString("customer_id"));
                            model.setcName(customer.getString("name"));
                            model.setcMobile(customer.getString("mobile"));
                            model.setcEmail(customer.getString("email"));
                            model.setcBusiness(customer.getString("business"));
                            list.add(model);
                        }
                        adapter.refresh();
                        showNoRecordsFound();
                    } else {
                        try {
                            JSONObject errorObj = new JSONObject(response.errorBody().string());
                            if (errorObj.has("success") && errorObj.has("message") && !errorObj.getBoolean("success"))
                                MLog.showToast(getApplicationContext(),  errorObj.getString("message"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progress.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progress.dismiss();
                MLog.showToast(getApplicationContext(), t.getMessage());
            }
        });
    }

    private void showNoRecordsFound() {
        if (list.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            tvNoRecordsFound.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvNoRecordsFound.setVisibility(View.GONE);
        }
    }

    public void deleteReceipt(CashReceiptModel model, int index, Dialog dialog) {
        if (!MyUtils.hasInternetConnection(getApplicationContext())) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        progress.show();

        APIClient.getApiService().deleteCashReceipt(authkey, model.getReceipt_number()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        MLog.showToast(CashReceiptActivity.this, jsonObject.getString("message"));
                        list.remove(index);
                        adapter.refresh();
                        dialog.dismiss();
                    } else {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        MLog.showToast(getApplicationContext(),jObjError.getString("message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progress.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progress.dismiss();
                MLog.showToast(getApplicationContext(), t.getMessage());
            }
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
