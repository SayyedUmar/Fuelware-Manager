package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.models.ProductPriceModel;
import com.fuelware.app.fw_manager.network.APIClient;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public  class MorningParamsActivity extends SuperActivity {


    /*@BindView(R.id.etProduct)
    EditText etProduct;
    @BindView(R.id.etRate)
    EditText etRate;
    @BindView(R.id.etConfirmRate)
    EditText etConfirmRate;*/
    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.tvNoRecordsFound)
    TextView tvNoRecordsFound;
    @BindView(R.id.btnSubmit)
    Button btnSubmit;



    private List<ProductPriceModel> list = new ArrayList<>();
    private AlertDialog progress;
    private Gson gson;
    private String authkey;
    private MorningParamsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_morning_params);
        ButterKnife.bind(this);

        setupBackNavigation(null);
        setTitle("Fuel Price");

        initialise();
        setupRecycler();
        setEventListeners();
        fetchProducts();
    }

    private void setupRecycler() {
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new MorningParamsAdapter(list, this);
        recyclerView.setAdapter(adapter);
    }

    private void initialise() {
        progress = new SpotsDialog(this, R.style.Custom);
        authkey = MyPreferences.getStringValue(getApplicationContext(), "authkey");
        gson = new Gson();
    }

    private void fetchProducts() {
        if (!MyUtils.hasInternetConnection(this)) {
            MLog.showToast(this, AppConst.NO_INTERNET_MSG);
            return;
        }

        progress.show();

        Call<ResponseBody> call_getProducts;
        call_getProducts = APIClient.getApiService().getManagerOutletProducts(authkey);
        call_getProducts.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray dataArray = jsonObject.getJSONArray("data");
                        Type token = new TypeToken<List<ProductPriceModel>>(){}.getType();
                        List<ProductPriceModel> tempList = gson.fromJson(dataArray.toString(), token);
                        list.clear();
                        list.addAll(tempList);
                        MyPreferences.setStringValue(MorningParamsActivity.this, AppConst.PRODUCTS_LIST, dataArray.toString());
                        boolean requiresUpdate = list.get(0).getPrice() < 1;
                        adapter.setEditable(requiresUpdate);
                        btnSubmit.setVisibility(requiresUpdate ? View.VISIBLE : View.GONE);
                        adapter.refresh();
                        if (requiresUpdate) {
                            MyPreferences.setBoolValue(getApplicationContext(),AppConst.IS_MORNING_PARAMETERS_UPDATED,false);
                        } else {
                            MyPreferences.setBoolValue(getApplicationContext(),AppConst.IS_MORNING_PARAMETERS_UPDATED,true);
                        }
                        String msg = jsonObject.getString("message");
                        if (requiresUpdate && !msg.isEmpty()) {
                            showReloginPopup();
                        }
                    } else {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        if (jsonObject.has("message"))
                            MLog.showLongToast(getApplicationContext(), jsonObject.getString("message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                progress.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progress.dismiss();
                MLog.showLongToast(getApplicationContext(), t.getMessage());
            }
        });
    }

    private void showReloginPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Alert")
                .setMessage("Close your shift and login again to update Morning Parameters.")
                .setPositiveButton("Ok", (d, w) -> {
                    finish();
                })
                .setCancelable(false);

        builder.show();
    }

    private void setEventListeners() {

       /* SpinnerAdapter adapter = new SpinnerAdapter<ProductPriceModel>(list) {
            @Override
            public void getView(int pos, SpinnerAdapter.MyHolder holder) {
                holder.textView.setText(list.get(pos).getProduct());
            }
        };

        etProduct.setOnClickListener(v -> {
            hideSoftKeyboard();
            DialogPlus dialog = DialogPlus.newDialog(this)
                    .setAdapter((BaseAdapter) adapter)
                    .setOnItemClickListener((dialog1, item, view, position) -> {
                        dialog1.dismiss();
                        etProduct.setText(((ProductPriceModel)item).getProduct());
                    })
                    .setCancelable(true)
                    .setExpanded(false)  // This will enable the expand feature, (similar to android L share dialog)
                    .setFooter(R.layout.dialog_footer)
                    .create();
            dialog.show();
        });*/

        btnSubmit.setOnClickListener(v -> {
            validateData();
        });
    }

    private void validateData() {
        Products p = new Products();
        int count = 0;
        boolean samePriceCheck = false;
        List<ProductPriceModel> wrongList = new ArrayList<>();
        for (ProductPriceModel m : list) {
            if (m.getPrice() == 0) {
                MLog.showToast(getApplicationContext(), "All fields are mandatory..");
                break;
            } else if (m.last_price == m.getPrice() || m.getPrice() > m.last_price+.30 || m.getPrice() < m.last_price-.30) {
                wrongList.add(m);
                samePriceCheck = true;
            }
            p.products.add(m);
            count++;
            if (count == list.size()) {
                if (samePriceCheck) {
                    showSamePricePopup(wrongList, p);
                } else {
                   // MLog.showToast(getApplicationContext(), "Morning price updated.");
                    updateMorningParam(p);
                }
            }
        }
    }

    private void showSamePricePopup(List<ProductPriceModel> wrongList, Products p) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String msg = "Below product Rate entered is either same or beyond the range of yesterday's rate.\nKindly Confirm.\n\nYou have entered:";
        int count = 1;
        for (ProductPriceModel m : wrongList) {
            msg += "\n    " +count++ + ". INR "+m.getPrice()+" for "+m.getProduct();
        }
        builder.setMessage(msg)
                .setPositiveButton("Confirm", (d, w) -> {
//                    MLog.showToast(getApplicationContext(), "Confirm clicked");
                    updateMorningParam(p);
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show();
    }

    private void updateMorningParam(Products p) {
        if (!MyUtils.hasInternetConnection(getApplicationContext())) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        progress.show();

        APIClient.getApiService().updateMorningParams(authkey, p).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        MLog.showToast(getApplicationContext(), "Morning Price updated successfully.");
                        MyPreferences.setBoolValue(getApplicationContext(),AppConst.IS_MORNING_PARAMETERS_UPDATED,true);
                        MorningParamsActivity.this.finish();
                    } else {
                        try {
                            JSONObject errorObj = new JSONObject(response.errorBody().string());
                            if (errorObj.has("success") && errorObj.has("message") && !errorObj.getBoolean("success")) {
                                new AlertDialog.Builder(MorningParamsActivity.this)
                                        .setTitle("An Error Occurred")
                                        .setMessage(errorObj.getString("message"))
                                        .setNegativeButton("Ok", null)
                                        .setCancelable(false)
                                        .show();
                                //MLog.showToast(getApplicationContext(),  errorObj.getString("message"));
                            }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    public static class Products {
        public List<ProductPriceModel> products = new ArrayList<>();
    }


//    confirm_price	30
//    errorPrice	false
//    outlet_id	7
//    price	30
//    product	Petrol
//    product_id	14
//    set_price	30
}
