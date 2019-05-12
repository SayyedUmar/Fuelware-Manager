package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.fuelware.app.fw_manager.Const.AppConst;
import com.fuelware.app.fw_manager.Const.Const;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.adapters.CustomAutoCompleteAdapter;
import com.fuelware.app.fw_manager.models.CreditCustomer;
import com.fuelware.app.fw_manager.models.ReceiptModel;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.network.RxBus;
import com.fuelware.app.fw_manager.services.MyService;
import com.fuelware.app.fw_manager.services.MyServiceListerner;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditReceiptActivity  extends SuperActivity {


    @BindView(R.id.btnAdd)
    Button btnAdd;
    @BindView(R.id.etBusiness)
    EditText etBusiness;
    @BindView(R.id.etCustomerName)
    EditText etCustomerName;
    @BindView(R.id.etCustomerMobile)
    EditText etCustomerMobile;
    @BindView(R.id.etCustomerEmail)
    EditText etCustomerEmail;
    @BindView(R.id.etRemarks)
    EditText etRemarks;
    @BindView(R.id.etAmount)
    EditText etAmount;
    @BindView(R.id.etReceiptNumber)
    EditText etReceiptNumber;
    @BindView(R.id.chkReceiptManual)
    CheckBox chkReceiptManual;
    @BindView(R.id.linlayReceiptNumber)
    LinearLayout linlayReceiptNumber;



    private AlertDialog progress;
    private Gson gson;
    private String authkey;
    private String amount = "", receiptNumber = "", remarks = "", businessName = "", bankName= "", branch = ""
            , chequeDate = "", chequeNo = "";
    private String receiptType;
    private ReceiptModel model;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_receipts);
        ButterKnife.bind(this);

        setupBackNavigation(null);

        model = getIntent().getParcelableExtra("CASH_RECEIPT_MODEL");
        receiptType = getIntent().getStringExtra(Const.RECEIPT_TYPE);
        if (receiptType.equals(Const.RECEIPT_CREDIT)) {
            setTitle("Update Credit Card Receipts");
        } else if (receiptType.equals(Const.RECEIPT_DEBIT)) {
            setTitle("Update Debit Card Receipts");
        } else if (receiptType.equals(Const.RECEIPT_LOYALTY)) {
            setTitle("Update Loyalty Receipts");
        }

        initialize();
        setEventListeners();
        showData();
    }

    private void showData() {
        etBusiness.setText(model.getBusiness());
        etCustomerName.setText(model.getName());
        etCustomerMobile.setText(model.getMobile());
        etCustomerEmail.setText(model.getEmail());

        etRemarks.setText(model.getRemark());
        etAmount.setText(MyUtils.parseDouble(model.getAmount())+"");
    }


    private void initialize() {
        gson = new Gson();
        progress = new SpotsDialog(this, R.style.Custom);
        authkey = MyPreferences.getStringValue(getApplicationContext(), "authkey");
        etAmount.setFilters(new InputFilter[] {new MyUtils.InputFilterMinMax(1, 10000000)});
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
        btnAdd.setEnabled(false);

        model.setAmount(MyUtils.parseDouble(amount)+"");
        model.setRemark(remarks);

        String mode = "cash";
        if (receiptType.equals(Const.RECEIPT_CREDIT)) {
            mode = "credit-card";
        } else if (receiptType.equals(Const.RECEIPT_DEBIT)) {
            mode = "debit-card";
        } else if (receiptType.equals(Const.RECEIPT_LOYALTY)) {
            mode = "loyalty-card";
        }


        progress.show();


        APIClient.getApiService().updateCashReceipt(authkey, model.getId(), model, mode).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject result = new JSONObject(response.body().string());
                        JSONObject data = result.getJSONObject("data");
//                        JSONObject customer = data.getJSONObject("customer");
                        ReceiptModel item = gson.fromJson(data.toString(), ReceiptModel.class);
//                        item.setCustomer_id(customer.getString("customer_id"));
//                        item.setName(customer.getString("name"));
//                        item.setMobile(customer.getString("mobile"));
//                        item.setEmail(customer.getString("email"));
//                        item.setBusiness(customer.getString("business"));
                        Map map = new HashMap<Object, Object>();
                        map.put(RxBus.EDIT_ACTION, item);
                        map.put(RxBus.POSITION, getIntent().getIntExtra(RxBus.POSITION, 0));
                        RxBus.getInstance().publish(map);
                        finish();

                    } else {
                        try {
                            JSONObject errorObj = new JSONObject(response.errorBody().string());
                            if (errorObj.has("success") && errorObj.has("message") && !errorObj.getBoolean("success"))
                                MLog.showToast(getApplicationContext(),  errorObj.getString("message"));

                            if (errorObj.has("errors")) {
                                JSONObject errorObject = errorObj.getJSONObject("errors");
                                if (errorObject.has("remark")) {
                                    String error = errorObject.getJSONArray("remark").get(0).toString();
                                    etRemarks.setError(error);
                                    etRemarks.requestFocus();
                                } else if (errorObject.has("amount")) {
                                    String error = errorObject.getJSONArray("amount").get(0).toString();
                                    etAmount.setError(error);
                                    etAmount.requestFocus();
                                }
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

    private boolean isValid() {

        amount = etAmount.getText().toString().trim();
        receiptNumber = etReceiptNumber.getText().toString().trim();
        remarks = etRemarks.getText().toString().trim();
        businessName = etBusiness.getText().toString().trim();

        if (remarks.isEmpty()) {
            etRemarks.setError("Enter valid remarks.");
            etRemarks.requestFocus();
            return false;
        } else if (amount.isEmpty()) {
            etAmount.setError("Enter valid amount.");
            etAmount.requestFocus();
            return false;
        } /*else if (chkReceiptManual.isChecked() && receiptNumber.isEmpty()) {
            etReceiptNumber.setError("Enter valid receipt number.");
            etReceiptNumber.requestFocus();
            return false;
        } else if (chkReceiptManual.isChecked() && receiptNumber.length() < 2) {
            etReceiptNumber.setError("Receipt number must be at least 2 character.");
            etReceiptNumber.requestFocus();
            return false;
        }*/

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
