package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.adapters.ProductListAdapter;
import com.fuelware.app.fw_manager.appconst.Const;
import com.fuelware.app.fw_manager.models.ProductsPojo;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.FuelwareAPI;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.utils.MyPreferences;
import com.fuelware.app.fw_manager.utils.MyUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CounterBillActivity extends SuperActivity {


    private AlertDialog progressDialog;
    private RecyclerView recyclerView;
    private ArrayList<ProductsPojo> list = new ArrayList<>();
    private ProductListAdapter adapter;
    private String authkey;
    private FuelwareAPI fuelwareInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBackNavigation(null);
        setTitle("Counter Billing");
        setContentView(R.layout.activity_counter_bill);

        progressDialog = new SpotsDialog(CounterBillActivity.this, R.style.Custom);
        authkey = MyPreferences.getStringValue(getApplicationContext(), Const.AUTHKEY);

        recyclerView = findViewById(R.id.recycler_view_products);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(),2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new ProductListAdapter(list,CounterBillActivity.this);
        recyclerView.setAdapter(adapter);

        fuelwareInterface = APIClient.getApiService();
        prepareListData();
        if (!MyPreferences.getBoolValue(getApplicationContext(),AppConst.IS_MORNING_PARAMETERS_UPDATED)) {
            showMorningParamsPopup();
        }
    }

    private void showMorningParamsPopup() {
        new AlertDialog.Builder(this)
                .setTitle("Morning Parameters not updated")
                .setMessage("Please update Morning Parametes to continue use of app.")
                .setPositiveButton("OK", (d, w) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void prepareListData() {
        if (!MyUtils.hasInternetConnection(getApplicationContext())) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        progressDialog.show();

        Call<ResponseBody> call_eindentList = fuelwareInterface.getOutletProducts(authkey);
        call_eindentList.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    JSONObject jsonObject;
                    if (response.isSuccessful()) {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray dataArray = jsonObject.getJSONArray("data");
                        list.clear();
                        Type token = new TypeToken<List<ProductsPojo>>(){}.getType();
                        list.addAll(new Gson().fromJson(dataArray.toString(), token));
                    } else {
                        jsonObject = new JSONObject(response.errorBody().string());
                        String msg = jsonObject.getString("message");
                        MLog.showToast(getApplicationContext(), msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }


}
