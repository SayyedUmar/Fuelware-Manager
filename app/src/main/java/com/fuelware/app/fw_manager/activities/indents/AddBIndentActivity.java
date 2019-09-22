package com.fuelware.app.fw_manager.activities.indents;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.adapters.CustomAutoCompleteAdapter;
import com.fuelware.app.fw_manager.adapters.HintSpinnerAdapter;
import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.appconst.Const;
import com.fuelware.app.fw_manager.models.CreditCustomer;
import com.fuelware.app.fw_manager.models.IndentModel;
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
import com.shashank.sony.fancytoastlib.FancyToast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddBIndentActivity extends SuperActivity {

    private AutoCompleteTextView autoCCBusinessName;
    private CustomAutoCompleteAdapter<CreditCustomer> businessNamesAdapter;
    EditText etIndentDate, etCustomerName, etCustomerID;
    Spinner spnProducts, spnFillType;
    EditText etCustomerMobile, etIndentNo, etVehicleNo, etVehicleKilometers;
    EditText etProductRate, etInvoiceNo, etLiters, etAmount, etDriverName, etDriverMobile;
    Button btnAdd;
    JSONArray productsArray = new JSONArray();
    JSONObject selectedProduct = new JSONObject();
    String authkey;
    ArrayList<String> outlet_products;
    ArrayAdapter<CharSequence> adapter_filltype;
    AlertDialog progressDialog;
    Calendar calender;
    private boolean isSpinnerInitialProduct = true;
    private List<CreditCustomer> creditCustomerList = new ArrayList<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Gson gson = new Gson();
    private CreditCustomer selectedCustomer;
    private TextWatcher amountTextWatcher;
    private TextWatcher litreTextWatcher;
    private LinearLayout linlayVehicleKms, linlayLitre, linlayAmount;
    private TextView etFuelingDate;
    private Date fuelingDate;

    //    String[] outlet_products = new String[10];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bindent);

        setTitle("Add B-Indent");
        setupBackNavigation(null);
        authkey = MyPreferences.getStringValue(this, "authkey");

        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.show();


        findViewById();


        etIndentNo.setFilters(new InputFilter[] {new InputFilter.AllCaps(), new InputFilter.LengthFilter(12)});
        etVehicleNo.setFilters(new InputFilter[] {new InputFilter.AllCaps(), new InputFilter.LengthFilter(16)});
        etVehicleKilometers.setFilters(new InputFilter[] {new MyUtils.InputFilterMinMax(1, 999999)});

        adapter_filltype = ArrayAdapter.createFromResource(this,
                R.array.Select_fillingtype, R.layout.dropdown_spinnerview);
        adapter_filltype.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnFillType.setAdapter(new HintSpinnerAdapter(
                adapter_filltype, R.layout.filltype_spinneritems, this));
//        spnFillType.setSelection(3);

        outlet_products = new ArrayList<>();

        setAutoBusinessNameAdapter();
        setEventListeners();

        fetchCreditCustomers();
        fetchProducts();

        linlayVehicleKms.setVisibility(View.GONE);
        linlayAmount.setVisibility(View.GONE);
        linlayLitre.setVisibility(View.GONE);

        setFuelingDateAsToday();
//        if (!MyPreferences.getBoolValue(this, AppConst.IS_MORNING_PARAMETERS_UPDATED)) {
//            showMorningParamsPopup();
//        }
    }

    private void showMorningParamsPopup() {

        new AlertDialog.Builder(this)
                .setTitle("Morning Parameters not updated")
                .setMessage("Please update Morning Parametes to continue use of app.")
                .setPositiveButton("OK", null)
                .setCancelable(false)
                .show();
    }

    private void findViewById() {
        linlayVehicleKms = findViewById(R.id.linlayVehicleKms);
        linlayLitre = findViewById(R.id.linlayLitre);
        linlayAmount = findViewById(R.id.linlayAmount);


        autoCCBusinessName = findViewById(R.id.autoCCBusinessName);
        etCustomerID = findViewById(R.id.etCustomerID);
        etCustomerName = findViewById(R.id.etCustomerName);
        etCustomerMobile = findViewById(R.id.etCustomerMobile);

        etIndentDate = findViewById(R.id.etIndentDate);
        spnProducts = findViewById(R.id.spnProducts);
        spnFillType = findViewById(R.id.spnFillType);
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
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setText("Submit");
        spnProducts = findViewById(R.id.spnProducts);
        etVehicleKilometers = findViewById(R.id.etVehicleKilometers);
        etFuelingDate = findViewById(R.id.etFuelingDate);
    }

    private void setFuelingDateAsToday() {
        Calendar calender = Calendar.getInstance();
        int year = calender.get(Calendar.YEAR);
        int month = calender.get(Calendar.MONTH);
        int day = calender.get(Calendar.DAY_OF_MONTH);
        ondate.onDateSet(null, year, month, day);
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
        autoCCBusinessName.setThreshold(1);
        autoCCBusinessName.setAdapter(businessNamesAdapter);

        autoCCBusinessName.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus && null != autoCCBusinessName)
                autoCCBusinessName.showDropDown();
        });

        autoCCBusinessName.setOnTouchListener((view, motionEvent) -> {
            autoCCBusinessName.showDropDown();
            return false;
        });

        autoCCBusinessName.setOnItemClickListener((adapterView, view, position, l) -> {
            autoCCBusinessName.setError(null);
            selectedCustomer = (CreditCustomer) adapterView.getItemAtPosition(position);
            autoCCBusinessName.setText(selectedCustomer.getBusiness());
            etCustomerID.setText(selectedCustomer.getId());
            String customerName = selectedCustomer.getFirst_name() + " " + selectedCustomer.getLast_name();
            etCustomerName.setText(customerName);
            etCustomerMobile.setText(selectedCustomer.getMobile());
            if (selectedCustomer.isHas_blacklisted()) {
                customerIsBlockedDialog(selectedCustomer.getBusiness() + " is a blacklisted user.");
            }

        });

        autoCCBusinessName.addTextChangedListener(new TextWatcher() {
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

    private void customerIsBlockedDialog(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(title)
                .setPositiveButton("OK", null)
                .setCancelable(true)
                .show();
    }

    private void setEventListeners() {

        btnAdd.setOnClickListener(view -> isValid());
//        etIndentDate.setOnClickListener(view -> showDatePicker());
        etFuelingDate.setOnClickListener(view -> showDatePicker());

        spnProducts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(isSpinnerInitialProduct) {
                    isSpinnerInitialProduct = false;
                } else {
                    try {
                        selectedProduct = productsArray.getJSONObject(i-1);
                        etProductRate.setText(productsArray.getJSONObject(i-1).get("price").toString());
                        etAmount.removeTextChangedListener(amountTextWatcher);
                        etLiters.removeTextChangedListener(litreTextWatcher);
                        etAmount.setText("");
                        etLiters.setText("");
                        etAmount.addTextChangedListener(amountTextWatcher);
                        etLiters.addTextChangedListener(litreTextWatcher);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        spnFillType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getSelectedItem() != null) {
                    String fillType = adapterView.getSelectedItem().toString();
                    if (fillType.equalsIgnoreCase(Const.AMOUNT)) {
//                        etLiters.setFocusable(false);
//                        etAmount.setFocusable(true);
                        etAmount.setEnabled(true);
                        etAmount.setBackground(ContextCompat.getDrawable(AddBIndentActivity.this, R.drawable.general_et_background));
                        etLiters.setEnabled(false);
                        etLiters.setBackground(ContextCompat.getDrawable(AddBIndentActivity.this, R.drawable.bg_rect_border_disabled));
                        etAmount.setFilters(new InputFilter[] {new MyUtils.InputFilterMinMax(Const.AMOUNT_MIN, Const.AMOUNT_MAX)});
                        etLiters.setFilters(new InputFilter[]{});


                        linlayAmount.setVisibility(View.VISIBLE);
                        linlayLitre.setVisibility(View.GONE);

                    } else if (fillType.equalsIgnoreCase(Const.LITRE) ||
                            fillType.equalsIgnoreCase("litre/kg") ||
                            fillType.equalsIgnoreCase(Const.FULL_TANK_V)) {

                        etLiters.setEnabled(true);
                        etLiters.setBackground(ContextCompat.getDrawable(AddBIndentActivity.this, R.drawable.general_et_background));
                        etAmount.setEnabled(false);
                        etAmount.setBackground(ContextCompat.getDrawable(AddBIndentActivity.this, R.drawable.bg_rect_border_disabled));
                        etLiters.setFilters(new InputFilter[] {new MyUtils.InputFilterMinMaxForLitre(Const.LITRE_MIN, Const.LITRE_MAX)});
                        etAmount.setFilters(new InputFilter[]{});

                        linlayLitre.setVisibility(View.VISIBLE);
                        linlayAmount.setVisibility(View.GONE);
                    }

                    etAmount.removeTextChangedListener(amountTextWatcher);
                    etLiters.removeTextChangedListener(litreTextWatcher);
                    etAmount.setText("");
                    etLiters.setText("");
                    etAmount.addTextChangedListener(amountTextWatcher);
                    etLiters.addTextChangedListener(litreTextWatcher);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        amountTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 0 && selectedProduct != null && spnFillType != null) {
                    if ( selectedProduct.has("price") && spnFillType.getSelectedItem().toString().equalsIgnoreCase(Const.AMOUNT)) {
                        try {
                            double newAmount = MyUtils.parseDouble(editable.toString().trim());
                            double productPrice = MyUtils.parseDouble(selectedProduct.getString("price"));
                            double newLitres = newAmount / productPrice ;
                            etLiters.setText(String.format("%.2f", newLitres));
                            etLiters.setError(null);
                            etAmount.setError(null);
//                            etAmount.removeTextChangedListener(amountTextWatcher);
//                            etAmount.setText(String.format("%.2f", newAmount));
//                            etAmount.setSelection(etAmount.getText().length());
//                            etAmount.addTextChangedListener(amountTextWatcher);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    etLiters.removeTextChangedListener(litreTextWatcher);
                    etLiters.setText("");
                    etLiters.addTextChangedListener(litreTextWatcher);
                }
            }
        };

        litreTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 0) {
                    String filltype = spnFillType.getSelectedItem().toString();
                    if ( selectedProduct != null &&
                            (filltype.equalsIgnoreCase(Const.LITRE) ||
                                    filltype.equalsIgnoreCase("litre/kg") ||
                                    filltype.equalsIgnoreCase(Const.FULL_TANK_V))) {
                        try {
                            double newLitres = MyUtils.parseDouble(editable.toString().trim());
                            double productPrice = Double.parseDouble(selectedProduct.getString("price"));
                            double newAmount = newLitres * productPrice ;
                            etAmount.setText(String.format("%.2f", newAmount));
                            etLiters.setError(null);
                            etAmount.setError(null);
//                            etLiters.removeTextChangedListener(litreTextWatcher);
//                            etLiters.setText(String.format("%.2f", newLitres));
//                            etLiters.setSelection(etLiters.getText().length());
//                            etLiters.addTextChangedListener(litreTextWatcher);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                } else {
                    etAmount.removeTextChangedListener(amountTextWatcher);
                    etAmount.setText("");
                    etAmount.addTextChangedListener(amountTextWatcher);
                }
            }
        };

        etAmount.addTextChangedListener(amountTextWatcher);
        etLiters.addTextChangedListener(litreTextWatcher);

    }

    /*public boolean isValidData () {
        String indentDate = etIndentDate.getText().toString().trim();
        String indentNumber = etIndentNo.getText().toString().trim();
        String vehicleNumber = etVehicleNo.getText().toString().trim();
        String kilometers = etVehicleKilometers.getText().toString().trim();


        if (selectedCustomer == null) {
            autoCCBusinessName.setError("Select Credit Customer's Business");
            autoCCBusinessName.requestFocus();
            return false;
        } else if (indentDate.isEmpty()) {
            etIndentDate.setError("Select Date Of Indent");
            etIndentDate.requestFocus();
            return false;
        } else if (indentNumber.isEmpty()) {
            etIndentNo.setError("Enter Indent Number");
            etIndentNo.requestFocus();
            return false;
        } else if (vehicleNumber.isEmpty()) {
            etVehicleNo.setError("Enter Vehicle Number");
            etVehicleNo.requestFocus();
            return false;
        } else if (kilometers.isEmpty()) {
            etVehicleKilometers.setError("Enter Vehicle Kms");
            etVehicleKilometers.requestFocus();
            return false;
        } else if (!kilometers.isEmpty()) {
            try {
                int meter_reading = Integer.parseInt(kilometers);
                if (meter_reading < 0) {
                    etVehicleKilometers.setError("Vehicle Kms should not be less than zero");
                    etVehicleKilometers.requestFocus();
                    return false;
                }
            } catch (Exception e) { e.printStackTrace(); }
        } else if (spnProducts.getSelectedItem() == null) {
            Toast.makeText(getApplicationContext(),"Select Product", Toast.LENGTH_LONG).show();
            ((TextView) spnProducts.getSelectedView()).setError("Select Product");
            return false;
        } else if (spnFillType.getSelectedItem() == null) {
            Toast.makeText(getApplicationContext(),"Select Fill Type",Toast.LENGTH_LONG).show();
            ((TextView) spnFillType.getSelectedView()).setError("Select Fill Type");
            return false;
        }



        return true;
    }*/

    long product_id,meter_reading;
    Double litre = 0.0 ,amount = 0.0;
    String user_id, indent_number,invoice_id,fill_date,vehicle_number;
    String fill_type = "";

    private void isValid() {

        if (!MyUtils.hasInternetConnection(getApplicationContext())) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        try {

            if (selectedCustomer == null) {
                autoCCBusinessName.setError("Select Credit Customer's Business");
                autoCCBusinessName.requestFocus();
                return;
            } else {
                String businessName = autoCCBusinessName.getText().toString().trim();
                if (selectedCustomer.getBusiness().equalsIgnoreCase(businessName)) {
                    user_id = selectedCustomer.getId();
                } else {
                    autoCCBusinessName.setError("Select valid Credit Customer's Business");
                    autoCCBusinessName.requestFocus();
                    return;
                }
            }

            fill_date = etIndentDate.getText().toString().trim();
//            if (fill_date.isEmpty()) {
//                etIndentDate.setError("Select Date Of Indent");
//                etIndentDate.requestFocus();
//                return;
//            }

            indent_number = etIndentNo.getText().toString().trim();
            if (indent_number.isEmpty()) {
                etIndentNo.setError("Enter Indent Number");
                etIndentNo.requestFocus();
                return;
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

            String fuelingDate = etFuelingDate.getText().toString().trim();
            boolean isFuelingDateToday = false;

            if (fuelingDate.isEmpty()) {
                etFuelingDate.setError("Select Fueling Date");
                etFuelingDate.requestFocus();
                return;
            } else if (isTodaysFuelingDate(this.fuelingDate)) {
                isFuelingDateToday = true;
                if (!MyPreferences.getBoolValue(getApplicationContext(), AppConst.IS_MORNING_PARAMETERS_UPDATED)) {
                    showMorningParamsPopup();
                    return;
                }
            }

            if (spnProducts.getSelectedItem() == null) {
                Toast.makeText(getApplicationContext(),"Select Product",Toast.LENGTH_LONG).show();
                ((TextView) spnProducts.getSelectedView()).setError("Select Product");
                return;

            } else {
                product_id = selectedProduct.getInt("id");
            }

            if (spnFillType.getSelectedItem() == null) {
                Toast.makeText(getApplicationContext(),"Select Fill Type",Toast.LENGTH_LONG).show();
                ((TextView) spnFillType.getSelectedView()).setError("Select Fill Type");
                return;
            } else {
                if (spnFillType.getSelectedItem().toString().equalsIgnoreCase(Const.AMOUNT)) {
                    fill_type = AppConst.amount;
                    meter_reading = 1;
                } else if (spnFillType.getSelectedItem().toString().equalsIgnoreCase(Const.FULL_TANK_V)) {
                    fill_type = AppConst.full_tank;
//                    meter_reading =  MyUtils.parseLong(etVehicleKilometers.getText().toString());
//                    if (meter_reading <= 0) {
//                        etVehicleKilometers.setError("Enter [Km or Hr]");
//                        etVehicleKilometers.requestFocus();
//                        return;
//                    }
                } else { //litre
                    meter_reading = 1;
                    fill_type = AppConst.litre;
                }
            }

            String litreString = etLiters.getText().toString().trim();
            litre = MyUtils.parseDouble(litreString);

            String amountString = etAmount.getText().toString().trim();
            amount = MyUtils.parseDouble(amountString);

            if (etLiters.isEnabled() && (litreString.isEmpty() || litre <= 0)) {
                etLiters.setError("Enter Liter/Kg *");
                etLiters.requestFocus();
                return;
            } /*else if (isFuelingDateToday && etLiters.isEnabled() && (amount < Const.AMOUNT_MIN || amount > Const.AMOUNT_MAX)) {
                etLiters.setError("Amount must be between 1 and 5,00,000");
                etLiters.requestFocus();
                return;
            }*/

            if (etAmount.isEnabled() && (amountString.isEmpty() || amount < 1)) {
                etAmount.setError("Enter Amount *");
                etAmount.requestFocus();
                return;
            } /*else if (isFuelingDateToday && etAmount.isEnabled() && (litre < Const.LITRE_MIN || litre > Const.LITRE_MAX)) {
                etAmount.setError("Litre/Kg must be between 0.01 and 5000");
                etAmount.requestFocus();
                return;
            }*/

            invoice_id = etInvoiceNo.getText().toString().trim();
            /*if (invoice_id.isEmpty()) {
                etInvoiceNo.setError("Enter Invoice Number");
                etInvoiceNo.requestFocus();
                return;
            }*/

            addIndent();

        } catch (Exception e){
            e.printStackTrace();
            MLog.showLongToast(getApplicationContext(), e.getMessage());
        }

    }

    private void addIndent() {
        btnAdd.setEnabled(false);

        String driver = etDriverName.getText().toString();
        String driver_mobile = etDriverMobile.getText().toString();

        progressDialog.show();

        IndentModel model = new IndentModel();
        model.setAmount(String.format("%.2f", amount));
        model.setLitre(String.format("%.2f", litre));
        model.setDriver(driver);
        model.setDriver_mobile(driver_mobile);
        model.setFill_type(fill_type);
        model.setFill_date(MyUtils.dateToString(AppConst.APP_DATE_FORMAT, AppConst.SERVER_DATE_FORMAT, fill_date));
        model.setIndent_number(indent_number);
        model.setInvoice_id(invoice_id);
        model.setMeter_reading(meter_reading);
        model.setProduct_id(product_id+"");
        model.setVehicle_number(vehicle_number);
        model.setUser_id(user_id+"");
        model.setVerify_credit_limit(selectedCustomer.isVerify_credit_limit());

        Call<ResponseBody> call_createMindent;
        call_createMindent = APIClient.getApiService().createBindent(authkey, AppConst.content_type, model);
        call_createMindent.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {

                    JSONObject jsonObject;
                    if (response.isSuccessful()) {
                        jsonObject = new JSONObject(response.body().string());
                        JSONObject dataObject = jsonObject.getJSONObject("data");
                        boolean shouldShowMessage = dataObject.has("show_message") ? dataObject.getBoolean("show_message"):false;
                        if (shouldShowMessage) {
                            String msg = jsonObject.getString("message");
                            // do you still want to aknowledge indent?
                            showWarningDialog(msg);
                        } else {
                            String msg = "B-Indent added successfully.";
                            MLog.showToast(getApplicationContext(), msg);
                            IndentModel item = new Gson().fromJson(dataObject.toString(), IndentModel.class);
                            Map map = new HashMap<Object, Object>();
                            map.put(RxBus.ADD_ACTION, item);
                            RxBus.getInstance().publish(map);
                            finish();
                        }
                    } else {
                        jsonObject = new JSONObject(response.errorBody().string());
                        if (jsonObject.has("message"))
                            MLog.showFancyToast(getApplicationContext(), jsonObject.getString("message"), FancyToast.ERROR);
                        if (jsonObject.has("errors")) {
                            JSONObject errorObject = jsonObject.getJSONObject("errors");
                            if (errorObject.has("user_id")) {
                                String error = errorObject.getJSONArray("user_id").get(0).toString();
                                autoCCBusinessName.setError(error);
                                autoCCBusinessName.requestFocus();
                            } else if (errorObject.has("fill_date")) {
                                String error = errorObject.getJSONArray("fill_date").get(0).toString();
                                etIndentDate.setError(error);
                                etIndentDate.requestFocus();
                            } else if (errorObject.has("indent_number")) {
                                String error = errorObject.getJSONArray("indent_number").get(0).toString();
                                etIndentNo.setError(error);
                                etIndentNo.requestFocus();
                            } else if (errorObject.has("vehicle_number")) {
                                String error = errorObject.getJSONArray("vehicle_number").get(0).toString();
                                etVehicleNo.setError(error);
                                etVehicleNo.requestFocus();
                            } else if (errorObject.has("meter_reading")) {
                                String error = errorObject.getJSONArray("meter_reading").get(0).toString();
                                etVehicleKilometers.setError(error);
                                etVehicleKilometers.requestFocus();
                            } else if (errorObject.has("product_id")) {
                                String error = errorObject.getJSONArray("product_id").get(0).toString();
                                MLog.showToast(getApplicationContext(), error);
//                                    spnProducts.setError(error);
                            } else if (errorObject.has("fill_type")) {
                                String error = errorObject.getJSONArray("fill_type").get(0).toString();
                                MLog.showToast(getApplicationContext(), error);
//                                    spnFillType.setError(error);
                            } else if (errorObject.has("litre")) {
                                String error = errorObject.getJSONArray("litre").get(0).toString();
                                etLiters.setError(error);
                                etLiters.requestFocus();
                            } else if (errorObject.has("amount")) {
                                String error = errorObject.getJSONArray("amount").get(0).toString();
                                etAmount.setError(error);
                                etAmount.requestFocus();
                            } else if (errorObject.has("invoice_id")) {
                                String error = errorObject.getJSONArray("invoice_id").get(0).toString();
                                etInvoiceNo.setError(error);
                                etIndentNo.requestFocus();
                            } else if (errorObject.has("driver")) {
                                String error = errorObject.getJSONArray("driver").get(0).toString();
                                etDriverName.setError(error);
                                etDriverName.requestFocus();
                            } else if (errorObject.has("driver_mobile")) {
                                String error = errorObject.getJSONArray("driver_mobile").get(0).toString();
                                etDriverMobile.setError(error);
                                etDriverMobile.requestFocus();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                btnAdd.setEnabled(true);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnAdd.setEnabled(true);
                progressDialog.dismiss();
                MLog.showLongToast(getApplicationContext(), t.getMessage());
            }
        });

    }

    private void showWarningDialog(String msg) {

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
            selectedCustomer.setVerify_credit_limit(false);
            addIndent();
        });
        btnNo.setOnClickListener(view -> dialog.dismiss());
    }

    private void fetchCreditCustomers() {

        if (!MyUtils.hasInternetConnection(getApplicationContext())) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        progressDialog.show();

        MyService.CallAPI(APIClient.getApiService().creditCustomerListApi(authkey), new MyServiceListerner<Response<ResponseBody>>() {
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
                progressDialog.hide();
                MLog.showLongToast(getApplicationContext(), e.getMessage());
            }

            @Override
            public void onComplete() {
                progressDialog.hide();
                businessNamesAdapter.refresh();
            }

            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }
        });

    }

    private void fetchProducts() {

        if (!MyUtils.hasInternetConnection(getApplicationContext())) {
            return;
        }

        progressDialog.show();

        Call<ResponseBody> call_getProducts;
        call_getProducts = APIClient.getApiService().getOutletProducts(authkey);
        call_getProducts.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONArray dataArray = new JSONObject(response.body().string()).getJSONArray("data");
                        productsArray = dataArray;

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject eachProduct = dataArray.getJSONObject(i);
                            outlet_products.add(eachProduct.getString("product"));
                        }

                        ArrayAdapter<String> adapter_products = new ArrayAdapter<>(AddBIndentActivity.this,
                                R.layout.dropdown_spinnerview, outlet_products);
                        adapter_products.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spnProducts.setAdapter(new HintSpinnerAdapter(
                                adapter_products, R.layout.product_spinneritems, AddBIndentActivity.this));
                        // spnProducts.setSelection(1);
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

    private void showDatePicker() {
        calender = Calendar.getInstance();
        Integer year = calender.get(Calendar.YEAR);
        Integer month = calender.get(Calendar.MONTH);
        Integer day = calender.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, fuelingDateListener, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);

        Calendar oldDate = Calendar.getInstance();
        oldDate.set(Calendar.DAY_OF_YEAR, calender.get(Calendar.DAY_OF_YEAR) - 3);
        datePickerDialog.getDatePicker().setMinDate(oldDate.getTimeInMillis());
        datePickerDialog.show();
    }

    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            String year1 = String.valueOf(year);
            String month1 = String.valueOf(monthOfYear + 1);
            String day1 = String.valueOf(dayOfMonth);

            if (monthOfYear + 1 < 10) {
                month1 = "0" + month1;
            }
            if (dayOfMonth < 10){
                day1  = "0" + day1 ;
            }

            etIndentDate.setError(null);
            String dateString = year1 + "-" + month1 + "-" + day1;
            etIndentDate.setText(MyUtils.dateToString(AppConst.SERVER_DATE_FORMAT,  AppConst.APP_DATE_FORMAT, dateString));
            etFuelingDate.setText(MyUtils.dateToString(AppConst.SERVER_DATE_FORMAT, AppConst.APP_DATE_FORMAT, dateString));
//            isTodaysFuelingDate(etFuelingDate.getText().toString());
        }
    };



    DatePickerDialog.OnDateSetListener fuelingDateListener = new DatePickerDialog.OnDateSetListener() {



        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            String year1 = String.valueOf(year);
            String month1 = String.valueOf(monthOfYear + 1);
            String day1 = String.valueOf(dayOfMonth);

            if (monthOfYear + 1 < 10) {
                month1 = "0" + month1;
            }
            if (dayOfMonth < 10){
                day1  = "0" + day1 ;
            }

            etFuelingDate.setError(null);
            String dateString = year1 + "-" + month1 + "-" + day1;
            etFuelingDate.setText(MyUtils.dateToString(AppConst.SERVER_DATE_FORMAT, AppConst.APP_DATE_FORMAT, dateString));
            etIndentDate.setText(MyUtils.dateToString(AppConst.SERVER_DATE_FORMAT, AppConst.APP_DATE_FORMAT, dateString));
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, monthOfYear, dayOfMonth);
            fuelingDate = selectedDate.getTime();
            if (isTodaysFuelingDate(fuelingDate) &&
                    !MyPreferences.getBoolValue(getApplicationContext(), AppConst.IS_MORNING_PARAMETERS_UPDATED)) {
                showMorningParamsPopup();
            }
        }
    };

    private boolean isTodaysFuelingDate(Date selectedDate) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        if (selectedDate.after(today.getTime())) {
            return true;
        }
        return false;
    }



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
        autoCCBusinessName.setText("");
        selectedCustomer = null;
        etCustomerID.setText("");
        etCustomerName.setText("");
        etCustomerMobile.setText("");
        //etIndentDate.setText("");
        etIndentNo.setText("");
        etVehicleNo.setText("");
        etVehicleKilometers.setText("");
        spnProducts.setSelection(0, false);
        selectedProduct = null;
        spnFillType.setSelection(0, false);
        etProductRate.setText("");
        etLiters.setText("");
        etAmount.setText("");
        etInvoiceNo.setText("");
        etDriverName.setText("");
        etDriverMobile.setText("");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
