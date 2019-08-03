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

import com.fuelware.app.fw_manager.appconst.AppConst;
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

public class AddOnlinePaymentActivity extends SuperActivity {


    @BindView(R.id.btnAdd)
    Button btnAdd;
    @BindView(R.id.tvAutoBusiness)
    AutoCompleteTextView tvAutoBusiness;
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


    // for cheque receipts
    @BindView(R.id.etTransNumber)
    EditText etTransNumber;
    @BindView(R.id.etTransDate)
    EditText etTransDate;


    private AlertDialog progress;
    private Gson gson;
    private String authkey;
    private List<CreditCustomer> creditCustomerList = new ArrayList<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private CustomAutoCompleteAdapter<CreditCustomer> businessNamesAdapter;
    private CreditCustomer selectedCustomer;
    private String amount = "", receiptNumber = "", remarks = "", businessName = "", transNumber = "", transDate = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_onlinepayment);
        ButterKnife.bind(this);

        setupBackNavigation(null);

        initialize();
        setAutoBusinessNameAdapter();
        setEventListeners();

        setTitle("Add Online Payment");
        setChequeDateAsToday();
        fetchCreditCustomers();

    }

    private void setChequeDateAsToday() {
        Calendar calender = Calendar.getInstance();
        int year = calender.get(java.util.Calendar.YEAR);
        int month = calender.get(java.util.Calendar.MONTH);
        int day = calender.get(java.util.Calendar.DAY_OF_MONTH);
        dateListener.onDateSet(null, year, month, day);
    }


    private void setAutoBusinessNameAdapter() {
        businessNamesAdapter = new CustomAutoCompleteAdapter<CreditCustomer>(this, creditCustomerList, new CustomAutoCompleteAdapter.SearchAdapterListener<CreditCustomer>() {

            @Override
            public void getView(int position, CustomAutoCompleteAdapter.MCustomHolder holder, CreditCustomer item) {
                holder.itemName.setText(item.getBusiness());
            }

            @Override
            public List onPerformFiltering(String searchText) {
                List<CreditCustomer> list = new ArrayList<>();
                for (CreditCustomer v : creditCustomerList) {
                    if (v.getBusiness().toLowerCase().startsWith(searchText))
                        list.add(v);
                }
                return list;
            }
        });
        tvAutoBusiness.setThreshold(1);
        tvAutoBusiness.setAdapter(businessNamesAdapter);

        tvAutoBusiness.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus && null != tvAutoBusiness)
                tvAutoBusiness.showDropDown();
        });

        tvAutoBusiness.setOnTouchListener((view, motionEvent) -> {
            tvAutoBusiness.showDropDown();
            return false;
        });

        tvAutoBusiness.setOnItemClickListener((adapterView, view, position, l) -> {
            tvAutoBusiness.setError(null);
            selectedCustomer = (CreditCustomer) adapterView.getItemAtPosition(position);
            tvAutoBusiness.setText(selectedCustomer.getBusiness());
            String customerName = selectedCustomer.getFirst_name() + " " + selectedCustomer.getLast_name();
            etCustomerName.setText(customerName);
            etCustomerMobile.setText(selectedCustomer.getMobile());
            etCustomerEmail.setText(selectedCustomer.getEmail());

        });

        tvAutoBusiness.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    businessNamesAdapter.getFilter().filter(s);
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });
    }

    private void fetchCreditCustomers() {

        if (!MyUtils.hasInternetConnection(getApplicationContext())) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        progress.show();

        MyService.CallAPI(APIClient.getApiService().getCreditCustomerList(authkey), new MyServiceListerner<Response<ResponseBody>>() {
            @Override
            public void onNext(Response<ResponseBody> response) {

                try {
                    JSONObject jsonObject;
                    if (response.isSuccessful()) {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        Type type = new TypeToken<List<CreditCustomer>>() {}.getType();
                        creditCustomerList.clear();
                        creditCustomerList.addAll(gson.fromJson(jsonArray.toString(), type));
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
            public void onError(Throwable e) {
                progress.hide();
                MLog.showLongToast(getApplicationContext(), e.getMessage());
            }

            @Override
            public void onComplete() {
                progress.hide();
                businessNamesAdapter.refresh();
            }

            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }
        });

    }

    private void initialize() {
        gson = new Gson();
        progress = new SpotsDialog(this, R.style.Custom);
        authkey = MyPreferences.getStringValue(getApplicationContext(), "authkey");
        etAmount.setFilters(new InputFilter[] {new MyUtils.InputFilterMinMax(1, 10000000)});
    }

    private void setEventListeners() {

        etTransDate.setOnClickListener(v -> showDatePicker());

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

    private void showDatePicker() {
        DatePickerFragment date = new DatePickerFragment();
        Calendar calender = Calendar.getInstance();
        Integer year = calender.get(Calendar.YEAR);
        Integer month = calender.get(Calendar.MONTH);
        Integer day = calender.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, dateListener, year, month, day);

        Calendar today = Calendar.getInstance();
        datePickerDialog.getDatePicker().setMaxDate(today.getTimeInMillis());
        datePickerDialog.show();
        date.setCallBack(dateListener);
    }

    private void addReceipt() {
        btnAdd.setEnabled(false);

        ReceiptModel model = new ReceiptModel();
        model.setPaid_by(selectedCustomer.formatted_role.toLowerCase());
        model.setBusiness(selectedCustomer.getBusiness());
        model.setCustomer_id(selectedCustomer.getId());
        model.setAmount(MyUtils.parseDouble(amount)+"");
        model.setRemark(remarks);
        model.setAuto_receipt(!chkReceiptManual.isChecked());
        model.setReceipt_number(receiptNumber);

        model.setTransaction_number(transNumber);
        model.setTransaction_date(MyUtils.dateToString(AppConst.APP_DATE_FORMAT, AppConst.SERVER_DATE_FORMAT, transDate));

        progress.show();

        APIClient.getApiService().addCashReceipt(authkey, model, "online-payment").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnAdd.setEnabled(true);
                try {
                    if (response.isSuccessful()) {
                        JSONObject result = new JSONObject(response.body().string());
                        JSONObject data = result.getJSONObject("data");
                        JSONObject customer = data.getJSONObject("customer");
                        ReceiptModel item = gson.fromJson(data.toString(), ReceiptModel.class);
//                        item.setCustomer_id(customer.getString("customer_id"));
//                        item.setName(customer.getString("name"));
//                        item.setMobile(customer.getString("mobile"));
//                        item.setEmail(customer.getString("email"));
//                        item.setBusiness(customer.getString("business"));
                        Map map = new HashMap<Object, Object>();
                        map.put(RxBus.ADD_ACTION, item);
                        RxBus.getInstance().publish(map);
                        finish();

                    } else {
                        try {
                            JSONObject errorObj = new JSONObject(response.errorBody().string());
                            if (errorObj.has("success") && errorObj.has("message") && !errorObj.getBoolean("success"))
                                MLog.showToast(getApplicationContext(),  errorObj.getString("message"));

                            if (errorObj.has("errors")) {
                                JSONObject errorObject = errorObj.getJSONObject("errors");
                                if (errorObject.has("customer_id")) {
                                    String error = errorObject.getJSONArray("customer_id").get(0).toString();
                                    tvAutoBusiness.setError(error);
                                    tvAutoBusiness.requestFocus();
                                } else if (errorObject.has("remark")) {
                                    String error = errorObject.getJSONArray("remark").get(0).toString();
                                    etRemarks.setError(error);
                                    etRemarks.requestFocus();
                                } else if (errorObject.has("amount")) {
                                    String error = errorObject.getJSONArray("amount").get(0).toString();
                                    etAmount.setError(error);
                                    etAmount.requestFocus();
                                } else if (errorObject.has("receipt_number")) {
                                    String error = errorObject.getJSONArray("receipt_number").get(0).toString();
                                    etReceiptNumber.setError(error);
                                    etReceiptNumber.requestFocus();
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
        businessName = tvAutoBusiness.getText().toString().trim();

        transNumber = etTransNumber.getText().toString().trim();
        transDate = etTransDate.getText().toString().trim();

        if (selectedCustomer == null) {
            tvAutoBusiness.setError("Select business name.");
            tvAutoBusiness.requestFocus();
            return false;
        } else if (!businessName.equalsIgnoreCase(selectedCustomer.getBusiness())) {
            tvAutoBusiness.setError("Invalid business name.");
            tvAutoBusiness.requestFocus();
            return false;
        } else if (transDate.isEmpty()) {
            etTransDate.setError("Please select Transaction Date.");
            MLog.showToast(getApplicationContext(), "Please select Transaction Date.");
            etTransDate.requestFocus();
            return false;

        } else if (transNumber.isEmpty()) {
            etTransNumber.setError("Please enter Transaction Number.");
            etTransNumber.requestFocus();
            return false;
        } else if (transNumber.length() < 5) {
            etTransNumber.setError("Transaction Number must be atleast 5 character.");
            etTransNumber.requestFocus();
            return false;
        } else if (remarks.isEmpty()) {
            etRemarks.setError("Enter valid remarks.");
            etRemarks.requestFocus();
            return false;
        } else if (amount.isEmpty()) {
            etAmount.setError("Enter valid amount.");
            etAmount.requestFocus();
            return false;
        } else if (chkReceiptManual.isChecked() && receiptNumber.isEmpty()) {
            etReceiptNumber.setError("Enter valid receipt number.");
            etReceiptNumber.requestFocus();
            return false;
        } else if (chkReceiptManual.isChecked() && receiptNumber.length() < 2) {
            etReceiptNumber.setError("Receipt number must be at least 2 character.");
            etReceiptNumber.requestFocus();
            return false;
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {

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

            etTransDate.setError(null);
            String dateString = year1 + "-" + month1 + "-" + day1;
            etTransDate.setText(MyUtils.dateToString(AppConst.SERVER_DATE_FORMAT, AppConst.APP_DATE_FORMAT, dateString));
        }
    };
}

