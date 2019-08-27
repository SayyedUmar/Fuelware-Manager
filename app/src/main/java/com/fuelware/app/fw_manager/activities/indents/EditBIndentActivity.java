package com.fuelware.app.fw_manager.activities.indents;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.adapters.BIndentListAdapter;
import com.fuelware.app.fw_manager.adapters.HintSpinnerAdapter;
import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.appconst.Const;
import com.fuelware.app.fw_manager.models.IndentModel;
import com.fuelware.app.fw_manager.models.ProductPriceModel;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.network.RxBus;
import com.fuelware.app.fw_manager.utils.DatePickerFragment;
import com.fuelware.app.fw_manager.utils.MyPreferences;
import com.fuelware.app.fw_manager.utils.MyUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditBIndentActivity extends SuperActivity {

    //POST http://fuelwarestaging-env-1.yvcgzs9hzb.ap-south-1.elasticbeanstalk.com/api/v1/common/otp?user_id=2297 // otp
    //PUT http://fuelwarestaging-env-1.yvcgzs9hzb.ap-south-1.elasticbeanstalk.com/api/v1/outlet/manager/cashier/2297/m-indent/1850 // [post
    //GET http://fuelwarestaging-env-1.yvcgzs9hzb.ap-south-1.elasticbeanstalk.com/api/v1/outlet/manager/cashier/2297/m-indent // list

    EditText etIndentDate;
    Spinner spnProducts, spnFillType;
    EditText etBusinessName, etCustomerID, etCustomerName, etCustomerMobile, etIndentNo, etVehicleNo;
    EditText etProductRate, etInvoiceNo, etLiters, etAmount, etDriverName, etDriverMobile;
    Button btnUpdate;
    String authkey;
    ProductPriceModel selectedProduct;
    ArrayList<ProductPriceModel> productList =  new ArrayList<>();;

    ArrayAdapter<CharSequence> adapter_filltype;
    AlertDialog progressDialog;
    Calendar calender;
    private boolean shallIgnoreProductSelection = true;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Gson gson = new Gson();
    private ArrayAdapter producAdapter;
    private IndentModel indent;

    private LinearLayout linlayLitre, linlayAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bindent);

        setTitle("Update B-Indent");
        setupBackNavigation(null);
        findViewById();

        etIndentNo.setFilters(new InputFilter[] {new InputFilter.AllCaps(), new InputFilter.LengthFilter(12)});
        etVehicleNo.setFilters(new InputFilter[] {new InputFilter.AllCaps(), new InputFilter.LengthFilter(16)});

        initData();
        setEventListeners();
        fetchProducts();
    }


    private void setEventListeners() {
        btnUpdate.setOnClickListener(view -> validate());
        etIndentDate.setOnClickListener(view -> showDatePicker());

        spnProducts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(shallIgnoreProductSelection) {
                    shallIgnoreProductSelection = false;
                } else  {
                    try {
                        selectedProduct = productList.get(i-1);
                        etProductRate.setText(MyUtils.formatCurrency(selectedProduct.getPrice()));
                        etAmount.setText("0");
                        etLiters.setText("0");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        spnFillType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getSelectedItem() != null) {
                    String filltype = adapterView.getSelectedItem().toString();
                    if (filltype.equalsIgnoreCase(Const.AMOUNT)) {
                        etAmount.setEnabled(true);
                        etAmount.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.general_et_background));
                        etLiters.setEnabled(false);
                        etLiters.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_rect_border_disabled));
                        etAmount.setFilters(new InputFilter[] {new MyUtils.InputFilterMinMax(Const.AMOUNT_MIN, Const.AMOUNT_MAX)});
                        etLiters.setFilters(new InputFilter[] {});

                        linlayAmount.setVisibility(View.VISIBLE);
                        linlayLitre.setVisibility(View.GONE);
                    } else if (filltype.equalsIgnoreCase(Const.LITRE) ||
                            filltype.equalsIgnoreCase("litre/kg") ||
                            filltype.equalsIgnoreCase(Const.FULL_TANK_V)) {

                        etLiters.setEnabled(true);
                        etLiters.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.general_et_background));
                        etAmount.setEnabled(false);
                        etAmount.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_rect_border_disabled));
                        etLiters.setFilters(new InputFilter[] {new MyUtils.InputFilterMinMaxForLitre(Const.LITRE_MIN, Const.LITRE_MAX)});
                        etAmount.setFilters(new InputFilter[] {});

                        linlayAmount.setVisibility(View.GONE);
                        linlayLitre.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    String filltype = spnFillType.getSelectedItem().toString();
                    if ( selectedProduct != null && filltype.equalsIgnoreCase(Const.AMOUNT)) {
                        try {
                            Double newAmount = MyUtils.parseDouble(charSequence.toString().trim());
                            Double newLitres = newAmount / selectedProduct.getPrice() ;
                            etLiters.setText(String.format("%.2f",newLitres));
                            etAmount.setError(null);
                            etLiters.setError(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    etLiters.setText("0");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        etLiters.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    String filltype = spnFillType.getSelectedItem().toString();
                    if (selectedProduct != null && (filltype.equalsIgnoreCase(Const.LITRE) ||
                            filltype.equalsIgnoreCase("litre/kg")
                            || spnFillType.getSelectedItem().toString().equalsIgnoreCase(Const.FULL_TANK_V))) {
                        try {
                            Double newLitres = MyUtils.parseDouble(charSequence.toString().trim());
                            Double newAmount = newLitres * selectedProduct.getPrice() ;
                            etAmount.setText(String.format("%.2f", newAmount));
                            etAmount.setError(null);
                            etLiters.setError(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    etAmount.setText("0");
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    private void initData() {
        indent = (IndentModel) getIntent().getSerializableExtra("INDENT_MODEL");
        authkey = MyPreferences.getStringValue(this, "authkey");
        progressDialog = new SpotsDialog(EditBIndentActivity.this, R.style.Custom);
        adapter_filltype = ArrayAdapter.createFromResource(this, R.array.Select_fillingtype, R.layout.dropdown_spinnerview);
        adapter_filltype.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnFillType.setAdapter(new HintSpinnerAdapter(adapter_filltype, R.layout.filltype_spinneritems, this));
    }

    private void findViewById() {
        etBusinessName = findViewById(R.id.etBusinessName);
        etIndentDate = findViewById(R.id.etIndentDate);
        spnProducts = findViewById(R.id.spnProducts);
        spnFillType = findViewById(R.id.spnFillType);
        etCustomerName = findViewById(R.id.etCustomerName);
        etCustomerID = findViewById(R.id.etCustomerID);
        etCustomerMobile = findViewById(R.id.etCustomerMobile);
        etIndentNo = findViewById(R.id.etIndentNo);
        etVehicleNo = findViewById(R.id.etVehicleNo);
        etProductRate = findViewById(R.id.etProductRate);
        etProductRate.setEnabled(false);
        etInvoiceNo = findViewById(R.id.etInvoiceNo);
        etLiters = findViewById(R.id.etLiters);
        etLiters.setEnabled(false);
        etAmount = findViewById(R.id.etAmount);
        etAmount.setEnabled(false);
        etDriverName = findViewById(R.id.etDriverName);
        etDriverMobile = findViewById(R.id.etDriverMobile);
        btnUpdate = findViewById(R.id.btnAdd);
        btnUpdate.setText("Update");

        linlayLitre = findViewById(R.id.linlayLitre);
        linlayAmount = findViewById(R.id.linlayAmount);
    }

    long meter_reading;
    double litre = 0, amount = 0;
    String user_id, indent_number,invoice_id,fill_date,vehicle_number;
    String fill_type = "", product_id;

    private void validate () {
        if (!MyUtils.hasInternetConnection(getApplicationContext())) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        try {


            fill_date = etIndentDate.getText().toString().trim();

            if (fill_date.isEmpty()) {
                etIndentDate.setError("Select Date Of Indent");
                etIndentDate.requestFocus();
                return;
            }

            if (etIndentNo.getText().toString().trim().isEmpty()) {
                etIndentNo.setError("Enter Indent Number");
                etIndentNo.requestFocus();
                return;
            } else {
                indent_number = etIndentNo.getText().toString();
            }


            vehicle_number = etVehicleNo.getText().toString().trim();
            if (vehicle_number.isEmpty()) {
                etVehicleNo.setError("Enter Vehicle/Machine Number");
                etVehicleNo.requestFocus();
                return;
            } else if (vehicle_number.length() < 3) {
                etVehicleNo.setError("Vehicle/Machine Number must contain at least 3 chars.");
                etVehicleNo.requestFocus();
                return;
            }

            if (spnProducts.getSelectedItem() == null) {
                Toast.makeText(getApplicationContext(),"Select Product", Toast.LENGTH_LONG).show();
                ((TextView) spnProducts.getSelectedView()).setError("Select Product");
                return;

            } else {
                product_id = selectedProduct.getId()+"";
            }

            if (spnFillType.getSelectedItem() == null) {
                Toast.makeText(getApplicationContext(),"Select Fill Type", Toast.LENGTH_LONG).show();
                ((TextView) spnFillType.getSelectedView()).setError("Select Fill Type");
                return;
            } else {
                if (spnFillType.getSelectedItem().toString().equals("Amount")) {
                    fill_type = AppConst.amount;
                }
                if (spnFillType.getSelectedItem().toString().equals("Litres")) {
                    fill_type = AppConst.litre;
                }
                if (spnFillType.getSelectedItem().toString().equals("Full Tank")) {
                    fill_type = AppConst.full_tank;
                }

                if (spnFillType.getSelectedItem().toString().equalsIgnoreCase(Const.AMOUNT)) {
                    fill_type = Const.AMOUNT;
                } else  if (spnFillType.getSelectedItem().toString().equalsIgnoreCase(Const.FULL_TANK_V)) {
                    fill_type = Const.FULL_TANK_K;
                } else {
//                if (spnFillType.getSelectedItem().toString().equals("Litres")) {
                    fill_type = Const.LITRE;
                }
            }

            if (etInvoiceNo.getText().toString().trim().isEmpty()) {
                etInvoiceNo.setError("Enter Invoice Number");
                etInvoiceNo.requestFocus();
                return;
            } else {
                invoice_id = etInvoiceNo.getText().toString();
            }

            String litreString = etLiters.getText().toString().trim();
            litre = MyUtils.parseDouble(litreString);
            if (etLiters.isEnabled() && (litreString.isEmpty() || litre <= 0)) {
                etLiters.setError("Enter Liter/Kg");
                etLiters.requestFocus();
                return;
            }

            String amountString = etAmount.getText().toString().trim();
            amount = MyUtils.parseDouble(amountString);
            if (etAmount.isEnabled() && (amountString.isEmpty() || amount <= 0)) {
                etAmount.setError("Enter Amount");
                etAmount.requestFocus();
                return;
            }

//            requestOTP(null);
            updateMIndent("");

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    /*private void requestOTP(Dialog dialog) {
        if (!MyUtils.hasInternetConnection(this)) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }
        progressDialog.show();

        Call<ResponseBody> callback = APIClient.getApiService().requestOTP(authkey, cashier.getCashier_id()+"");
        callback.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        MLog.showToast(EditBIndentActivity.this, jsonObject.getString("message"));
                        String name = jsonObject.getJSONObject("data").getString("name");
                        String mobile = jsonObject.getJSONObject("data").getString("mobile");
                        if (dialog == null)
                            showConfirmOTPDialog(name, mobile);
                        //{"success":true,"message":"OTP successfully send","data":{"name":"Rohit Gaydhane","mobile":"9518950847"}}
                    } else {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
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
                MLog.showLongToast(getApplicationContext(), t.getMessage());
            }
        });
    }

    private void showConfirmOTPDialog(String name, String mobile) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_verify_action_otp);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        EditText etOTP = dialog.findViewById(R.id.etOTP);
        TextView tvReSend = dialog.findViewById(R.id.tvReSend);
        tvTitle.setText("OTP has been sent to "+name+" "+mobile);
        TextView btnNo = dialog.findViewById(R.id.btnNo);
        TextView btnYes = dialog.findViewById(R.id.btnYes);
        dialog.setCancelable(true);
        btnNo.setOnClickListener(w -> dialog.dismiss());
        btnYes.setOnClickListener(w -> {
            dialog.dismiss();
            updateMIndent(etOTP.getText().toString().trim());
        });
        tvReSend.setOnClickListener(v -> {
            requestOTP(dialog);
        });
        dialog.show();
    }*/

    private void updateMIndent(String otp) {
        btnUpdate.setEnabled(false);

        String driver = etDriverName.getText().toString();
        String driver_mobile = etDriverMobile.getText().toString();

        progressDialog.show();

        IndentNew model = new IndentNew();
        model.otp = otp;

        model.setUser_id(indent.getUser_id());
        model.setAmount(MyUtils.parseToString(amount));
        model.setLitre(MyUtils.parseToString(litre));
        model.setDriver(driver);
        model.setDriver_mobile(driver_mobile);
        model.setFill_type(fill_type);
        model.setFill_date(MyUtils.dateToString(AppConst.APP_DATE_FORMAT, AppConst.SERVER_DATE_FORMAT, fill_date));
        model.setIndent_number(indent_number);
        model.setInvoice_id(invoice_id);
        model.setMeter_reading(meter_reading);
        model.setProduct_id(product_id+"");
        model.setVehicle_number(vehicle_number);
        model.setVerify_credit_limit(indent.isVerify_credit_limit());

        Call<ResponseBody> call_createMindent = APIClient.getApiService().updateBIndent(authkey,indent.getId()+"", model);

        call_createMindent.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        if (jsonObject.getBoolean("success")) {

                            JSONObject dataObject = jsonObject.getJSONObject("data");
                            boolean shouldShowMessage = dataObject.has("show_message") ? dataObject.getBoolean("show_message"):false;
                            if (shouldShowMessage) {
                                String msg = jsonObject.getString("message");
                                // do you still want to aknowledge indent?
                                showWarningDialog(msg, model, otp);
                            } else {
                                String msg = "M Indent updated successfully.";
                                MLog.showToast(getApplicationContext(), msg);
                                indent = new Gson().fromJson(dataObject.toString(), IndentModel.class);
                                Map map = new HashMap<Object, Object>();
                                map.put(RxBus.EDIT_ACTION, indent);
                                map.put(RxBus.POSITION, getIntent().getIntExtra(RxBus.POSITION, 0));
                                RxBus.getInstance().publish(map);
                                finish();
                            }
                        }
                    } else {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        if (jsonObject.has("errors")) {
                            JSONObject errorObject = jsonObject.getJSONObject("errors");
                            if(errorObject.has("user_id")) {
                                String error = errorObject.getJSONArray("user_id").get(0).toString();
                                etBusinessName.setError(error);
                            }else if(errorObject.has("indent_number")) {
                                String error = errorObject.getJSONArray("indent_number").get(0).toString();
                                etIndentNo.setError(error);
                            } else if(errorObject.has("invoice_id")) {
                                String error = errorObject.getJSONArray("invoice_id").get(0).toString();
                                etInvoiceNo.setError(error);
                            } else if(errorObject.has("product_id")) {
                                String error = errorObject.getJSONArray("product_id").get(0).toString();
                                MLog.showLongToast(getApplicationContext(), error);
//                                    spnProducts.setError(error);
                            } else if(errorObject.has("fill_date")) {
                                String error = errorObject.getJSONArray("fill_date").get(0).toString();
                                etIndentDate.setError(error);
                            } else if(errorObject.has("litre")) {
                                String error = errorObject.getJSONArray("litre").get(0).toString();
                                etLiters.setError(error);
                            } else if(errorObject.has("vehicle_number")) {
                                String error = errorObject.getJSONArray("vehicle_number").get(0).toString();
                                etVehicleNo.setError(error);
                            } else if(errorObject.has("driver")) {
                                String error = errorObject.getJSONArray("driver").get(0).toString();
                                etDriverName.setError(error);
                            } else if(errorObject.has("amount")) {
                                String error = errorObject.getJSONArray("amount").get(0).toString();
                                etAmount.setError(error);
                            } else if(errorObject.has("fill_type")) {
                                String error = errorObject.getJSONArray("fill_type").get(0).toString();
                                MLog.showLongToast(getApplicationContext(), error);
//                                    spnFillType.setError(error);
                            } else if(errorObject.has("meter_reading")) {
                                String error = errorObject.getJSONArray("meter_reading").get(0).toString();
                                MLog.showLongToast(getApplicationContext(), error);
                            } else if(errorObject.has("driver_mobile")) {
                                String error = errorObject.getJSONArray("driver_mobile").get(0).toString();
                                etDriverMobile.setError(error);
                            }
                        } else if (jsonObject.has("message")) {
                            MLog.showLongToast(getApplicationContext(), jsonObject.getString("message"));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                btnUpdate.setEnabled(true);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnUpdate.setEnabled(true);
                progressDialog.dismiss();
                MLog.showToast(getApplicationContext(), t.getMessage());
            }
        });
    }

    private void showWarningDialog(String msg, IndentModel model, String otp) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_credit_limit_warning);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        TextView btnYes = dialog.findViewById(R.id.btnYes);
        TextView btnNo = dialog.findViewById(R.id.btnNo);

        tvTitle.setText(msg);
        tvMessage.setText("Do you still want to acknowledge indent ?");

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        dialog.show();

        btnYes.setOnClickListener(view -> {
            dialog.dismiss();
            indent.setVerify_credit_limit(false);
            updateMIndent(otp);
        });
        btnNo.setOnClickListener(view -> dialog.dismiss());
    }


    private void fetchProducts() {
        if (!MyUtils.hasInternetConnection(getApplicationContext())) {
            return;
        }

        Call<ResponseBody> call_getProducts;
        call_getProducts = APIClient.getApiService().getOutletProducts(authkey);
        call_getProducts.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if (response.isSuccessful()) {
                        JSONArray dataArray = jsonObject.getJSONArray("data");

                        Type token = new TypeToken<List<ProductPriceModel>>(){}.getType();
                        productList = gson.fromJson(dataArray.toString(), token);

                        List<String> outlet_products = new ArrayList<>();
                        for (ProductPriceModel p : productList) {
                            outlet_products.add(p.getProduct());
                        }

                        producAdapter = new ArrayAdapter<>(EditBIndentActivity.this,
                                R.layout.dropdown_spinnerview, outlet_products);

                        producAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spnProducts.setAdapter(new HintSpinnerAdapter(producAdapter, R.layout.product_spinneritems, EditBIndentActivity.this));

                        showDataOnUI();
                    } else {
                        jsonObject = new JSONObject(response.errorBody().string());
                        if (jsonObject.has("message"))
                            MLog.showLongToast(getApplicationContext(), jsonObject.getString("message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                MLog.showToast(getApplicationContext(), t.getMessage());
            }
        });


    }

    private void showDatePicker() {
        DatePickerFragment date = new DatePickerFragment();
        calender = Calendar.getInstance();
        Integer year = calender.get(Calendar.YEAR);
        Integer month = calender.get(Calendar.MONTH);
        Integer day = calender.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, ondate, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);

        Calendar oldDate = Calendar.getInstance();
        oldDate.set(Calendar.DAY_OF_YEAR, calender.get(Calendar.DAY_OF_YEAR) - 6);
        datePickerDialog.getDatePicker().setMinDate(oldDate.getTimeInMillis());
        datePickerDialog.show();
        date.setCallBack(ondate);
    }

    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            String year1 = String.valueOf(year);
            String month1 = String.valueOf(monthOfYear + 1);
            String day1 = String.valueOf(dayOfMonth);

            if (monthOfYear + 1 < 10){
                month1 = "0" + month1;
            }
            if (dayOfMonth < 10){
                day1  = "0" + day1 ;
            }

            etIndentDate.setError(null);
            String dateString = year1 + "-" + month1 + "-" + day1;
            etIndentDate.setText(MyUtils.dateToString(AppConst.SERVER_DATE_FORMAT, AppConst.APP_DATE_FORMAT, dateString));
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem refreshItem = menu.findItem(R.id.action_refresh);
        refreshItem.setVisible(true);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_refresh) {
            resetAllFormFields();
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetAllFormFields() {
        showDataOnUI();
    }

    private void showDataOnUI() {
        etBusinessName.setText(indent.getBusiness());
        etCustomerMobile.setText(indent.getMobile());
        etCustomerID.setText(indent.getFormatted_user_id());
        String indentDate = indent.getFill_date();
        etIndentDate.setText(MyUtils.dateToString(AppConst.SERVER_DATE_FORMAT, AppConst.APP_DATE_FORMAT, indentDate));
        etIndentNo.setText(indent.getIndent_number());
        etVehicleNo.setText(indent.getVehicle_number());

        etDriverName.setText(indent.getDriver());
        if (indent.getDriver().equalsIgnoreCase("null")) {
            etDriverName.setText("");
        }

        if (indent.getDriver_mobile().equalsIgnoreCase("null")) {
            etDriverMobile.setText("");
        } else {
            etDriverMobile.setText(indent.getDriver_mobile());
        }

        String product = indent.getProduct();
        if (!product.isEmpty()) {
            int pos = 1;
            producAdapter.notifyDataSetChanged();
            for (ProductPriceModel prod : productList) {
                if (prod.getProduct().equalsIgnoreCase(product)) {
                    shallIgnoreProductSelection = true;
                    spnProducts.setSelection(pos, false);
                    break;
                }
                pos++;
            }
        }
        if (indent.getFill_type().equalsIgnoreCase(AppConst.amount)) {
            spnFillType.setSelection(1);
        } else if (indent.getFill_type().equalsIgnoreCase(AppConst.litre)) {
            spnFillType.setSelection(2);
        } else if (indent.getFill_type().equalsIgnoreCase(AppConst.full_tank)) {
            spnFillType.setSelection(3);
        }

        selectedProduct = null;
        etProductRate.setText(indent.getPrice());
        etLiters.setText(String.format("%.2f", MyUtils.parseDouble(indent.getLitre())));
        etAmount.setText(String.format("%.2f",  MyUtils.parseDouble(indent.getAmount())));
        etInvoiceNo.setText(indent.getInvoice_id());

        selectedProduct = indent.product_detail;
        etCustomerName.setText(indent.getCustomer_name());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public static class IndentNew extends IndentModel {
        public String otp;
    }

    public static class BIndentListActivity extends SuperActivity implements SearchView.OnQueryTextListener {

        AlertDialog progressDialog;
        private RecyclerView recyclerView;
        private List<IndentModel> list = new ArrayList<>();
        private BIndentListAdapter adapter;
        private String authkey;
        private TextView tvNoRecordsFound;
        private Gson gson;
        private CompositeDisposable compositeDisposable = new CompositeDisposable();
        private FloatingActionButton fab;

        @SuppressLint("RestrictedApi")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_mindent_list);
            setTitle("B-Indent");
            setupBackNavigation(null);

            gson = new Gson();
            progressDialog = new SpotsDialog(BIndentListActivity.this, R.style.Custom);
            authkey = MyPreferences.getStringValue(getApplicationContext(), "authkey");

            fab = findViewById(R.id.fab);
            tvNoRecordsFound = findViewById(R.id.tvNoRecordsFound);
            recyclerView = findViewById(R.id.recycler_view_mindent);

            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());


            adapter = new BIndentListAdapter(list, BIndentListActivity.this);
            recyclerView.setAdapter(adapter);


            fetchData();
            setObserver();

            fab.setOnClickListener(view -> startActivity(new Intent(BIndentListActivity.this, AddBIndentActivity.class)));
            if (getIntent().getBooleanExtra(Const.B_INDENT, false)) {
                fab.setVisibility(View.VISIBLE);
            }
        }

        private void setObserver() {
            compositeDisposable.add(RxBus.getInstance().toObservable().subscribe(new Consumer<Map<Object, Object>>() {
                @Override
                public void accept(Map<Object, Object> map) throws Exception {
                    try {
                        if (map.containsKey(RxBus.ADD_ACTION)) {
                            Object object = map.get(RxBus.ADD_ACTION);
                            if (object instanceof IndentModel) {
                                list.add(0, (IndentModel) object);
                            }
                        } else if (map.containsKey(RxBus.EDIT_ACTION)) {
                            Object object = map.get(RxBus.EDIT_ACTION);
                            if (object instanceof IndentModel) {
                                int positon = (int) map.get(RxBus.POSITION);
                                if (list.size() > positon)
                                    list.set(positon, (IndentModel) object);
                            }
                        } /*else if (map.containsKey(RxBus.DELETE_ACTION)) {
                            int positon = (int) map.get(RxBus.DELETE_ACTION);
                            try {
                                list.remove(positon);
                            }catch (Exception e) {e.printStackTrace();}
                        }*/
                        adapter.refresh();
                        showNoRecordsFound();
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }));
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


        private void fetchData() {

            if (!MyUtils.hasInternetConnection(getApplicationContext())) {
                MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
                return;
            }

            progressDialog.show();

            APIClient.getApiService().getBindentList(authkey).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful()) {
                            JSONObject result = new JSONObject(response.body().string());
                            JSONArray jsonArray = result.getJSONArray("data");
                            Type token = new TypeToken<List<IndentModel>>(){}.getType();
                            List<IndentModel> tempList = gson.fromJson(jsonArray.toString(), token);
                            list.clear();
                            list.addAll(tempList);
                            adapter.refresh();
                            showNoRecordsFound();
                        } else {
                            try {
                                JSONObject errorObj = new JSONObject(response.errorBody().string());
                                if (errorObj.has("success") && errorObj.has("message") && !errorObj.getBoolean("success"))
                                    Toast.makeText(getApplicationContext(), errorObj.getString("message"), Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });


        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_search, menu);
            MenuItem searchItem = menu.findItem(R.id.action_search);
            searchItem.setVisible(false);

            SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);

            if (searchItem != null) {
                SearchView searchView = (SearchView) searchItem.getActionView();
                searchView.setQueryHint("Enter Cashier Name");
                searchView.setOnQueryTextListener(this);
                searchView.setIconified(false);
                searchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
                ((EditText)searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setTextColor(Color.WHITE);
                ((EditText)searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setHintTextColor(Color.WHITE);
            }

            return super.onCreateOptionsMenu(menu);
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
            compositeDisposable.clear();
            if(progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            adapter.filter(newText);
            return true;
        }


        public void deleteMIndent(IndentModel indentModel, int index, String otp) {
            if (!MyUtils.hasInternetConnection(this)) {
                MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
                return;
            }

            progressDialog.show();

            Call<ResponseBody> responce = APIClient.getApiService().deleteBIndent(authkey, indentModel.getId()+"");
            responce.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    progressDialog.dismiss();
                    try {
                        if (response.isSuccessful()) {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            MLog.showToast(BIndentListActivity.this, jsonObject.getString("message"));
                            list.remove(index);
                            adapter.refresh();
                        } else {
                            JSONObject jObjError = new JSONObject(response.errorBody().string());
                            MLog.showToast(getApplicationContext(),jObjError.getString("message"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressDialog.dismiss();
                    MLog.showLongToast(getApplicationContext(), t.getMessage());
                }
            });
        }
    }
}

