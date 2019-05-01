package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.fuelware.app.fw_manager.Const.AppConst;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.models.IndentModel;
import com.fuelware.app.fw_manager.utils.MyPreferences;
import com.fuelware.app.fw_manager.utils.MyUtils;
import com.google.gson.Gson;

import dmax.dialog.SpotsDialog;

public class MiDetailsActivity extends SuperActivity {

    AlertDialog progressDialog;
    String mindent_id;
    String authkey;
    TextView tvBusinessName, tvCustomerID, tvCustomerName, tvMobileNumber, tvIndentDate, tvIndentNumber;
    TextView tvVehicleNo, tvDriverName, tvDriverMobile, tvFillType, tvProduct;
    TextView tvPrice, tvLitres, tvAmount, tvVehicleKms, tvInvoiceNo;
    Button btnClose;

    String ei_fill_type,ei_liters,ei_amount,ei_rate;
    private IndentModel indentModel;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_details);
        setupBackNavigation(null);
        setTitle("M-Indent Details");

        authkey = MyPreferences.getStringValue(this, "authkey");
        indentModel = (IndentModel) getIntent().getSerializableExtra("indent_model");

        progressDialog = new SpotsDialog(MiDetailsActivity.this, R.style.Custom);
        mindent_id = getIntent().getStringExtra("indent_id");
        findViewById();
        setEventListeners();
        //getMindentDetails();
//        setObserver();
        showDataOnUI();
    }

    private void findViewById() {
        tvBusinessName = findViewById(R.id.etBusinessName);
        tvCustomerID = findViewById(R.id.tvCustomerID);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvMobileNumber = findViewById(R.id.tvMobileNumber);
        tvIndentDate = findViewById(R.id.etIndentDate);
        tvIndentNumber = findViewById(R.id.tvIndentNumber);
        tvVehicleNo = findViewById(R.id.tvVehicleNo);
        tvDriverName = findViewById(R.id.tvDriverName);
        tvDriverMobile = findViewById(R.id.tvDriverMobile);
        tvFillType = findViewById(R.id.tvFillType);
        tvProduct = findViewById(R.id.tvProduct);
        tvPrice = findViewById(R.id.tvPrice);
        tvLitres = findViewById(R.id.tvLitres);
        tvAmount = findViewById(R.id.tvAmount);
        tvVehicleKms = findViewById(R.id.tvVehicleKms);
        tvInvoiceNo = findViewById(R.id.etInvoiceNo);
        btnClose = findViewById(R.id.btnClose);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //getMindentDetails();
    }

    private void setEventListeners() {

        btnClose.setOnClickListener(view -> {
            onBackPressed();
        });
    }

    /*private void getMindentDetails() {

        if (!MyUtils.hasInternetConnection(getApplicationContext())) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        progressDialog.show();
        Call<ResponseBody> call_eindentList = APIClient.getApiService().getMindentDetails(authkey,mindent_id);
        call_eindentList.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    JSONObject jsonObject;

                    if (response.isSuccessful()) {
                        jsonObject = new JSONObject(response.body().string());
                        JSONObject dataObject = jsonObject.getJSONObject("data");
                        indentModel = gson.fromJson(jsonObject.getString("data"), IndentModel.class);
                        showDataOnUI();
                    } else {
                        jsonObject = new JSONObject(response.errorBody().string());
                        if (jsonObject.has("message"))
                            MLog.showLongToast(getApplicationContext(), jsonObject.getString("message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                MLog.showToast(getApplicationContext(), t.getMessage());
            }
        });
    }*/

    private void showDataOnUI() {
        tvIndentNumber.setText(indentModel.getIndent_number());
        tvIndentDate.setText(MyUtils.dateToString(AppConst.SERVER_DATE_FORMAT, AppConst.APP_DATE_FORMAT, indentModel.getFill_date()));
        tvBusinessName.setText(MyUtils.toTitleCase(indentModel.getBusiness()));
        tvCustomerID.setText(indentModel.getFormatted_user_id().toUpperCase());
        tvCustomerName.setText(MyUtils.toTitleCase(indentModel.getCustomer_name()));
        tvMobileNumber.setText(indentModel.getMobile());
        tvVehicleNo.setText(indentModel.getVehicle_number());

        if (indentModel.getDriver() == null || indentModel.getDriver().isEmpty()) {
            tvDriverName.setText("-");
        } else {
            tvDriverName.setText(MyUtils.toTitleCase(indentModel.getDriver()));
        }
        if (indentModel.getDriver_mobile() == null || indentModel.getDriver_mobile().isEmpty()) {
            tvDriverMobile.setText("-");
        } else {
            tvDriverMobile.setText(indentModel.getDriver_mobile());
        }
        tvFillType.setText(MyUtils.toTitleCase(indentModel.getFill_type()));
        ei_fill_type = indentModel.getFill_type();
        tvProduct.setText(indentModel.getProduct());
        try {
            tvPrice.setText(MyUtils.formatCurrency(indentModel.getPrice()));
            double prod_liter = MyUtils.parseDouble(indentModel.getLitre());
            tvLitres.setText(String.format("%.2f", prod_liter) + " L");
            tvAmount.setText(MyUtils.formatCurrency(indentModel.getAmount()));
        } catch (Exception e) { e.printStackTrace(); }
        ei_rate = indentModel.getPrice();
        ei_liters = indentModel.getLitre();
        ei_amount = indentModel.getAmount();
        tvVehicleKms.setText(indentModel.getMeter_reading()+"");
        tvInvoiceNo.setText(indentModel.getInvoice_id());
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
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }
}
