package com.fuelware.app.fw_manager.adapters;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.CounterBillActivity;
import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.appconst.Const;
import com.fuelware.app.fw_manager.models.CounterBillPojo;
import com.fuelware.app.fw_manager.models.ProductsPojo;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.utils.MyPreferences;
import com.fuelware.app.fw_manager.utils.MyUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import dmax.dialog.SpotsDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.MyViewHolder> {
    private ArrayList<ProductsPojo> dataSet;
    Context mContext;
    private Dialog progressDialog;

    public ProductListAdapter(ArrayList<ProductsPojo> data, CounterBillActivity counterBillActivity) {
        this.dataSet = data;
        this.mContext = counterBillActivity;
        progressDialog = new SpotsDialog(mContext, R.style.Custom);
    }

    @NonNull
    @Override
    public ProductListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_products, parent, false);

        return new ProductListAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        final ProductsPojo productsPojo = dataSet.get(position);
        holder.price.setText(MyUtils.formatCurrency(productsPojo.getPrice()));
        holder.product.setText(productsPojo.getProduct());

        holder.itemView.setOnClickListener(view -> {
            double price = 0;
            try {
                price = Double.parseDouble(productsPojo.getPrice());
            } catch (Exception e) {e.printStackTrace();}
            if (price <= 0) {
                //((CounterBillActivity) mContext).showMorningParamsUdpateDialog(null);
            } else {
                setOnClickListener(productsPojo);
            }
        });
    }

    private void setBackground(TextView tv, int backResID, int colorID) {
        tv.setBackground(ContextCompat.getDrawable(mContext, backResID));
        tv.setTextColor(ContextCompat.getColor(mContext, colorID));
    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView product,price;
        LinearLayout linlayContainer;
        MyViewHolder(View itemView) {
            super(itemView);
            product = itemView.findViewById(R.id.product_name);
            price = itemView.findViewById(R.id.product_price);
            linlayContainer = itemView.findViewById(R.id.linlayContainer);
        }
    }

    private void setOnClickListener(ProductsPojo item) {
        String product_name = item.getProduct();
        String product_price = item.getPrice();

        Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_counter_bill);
        TextView btnCancel = dialog.findViewById(R.id.btnCancel);
        TextView btnSave = dialog.findViewById(R.id.btnSave);
        TextView tvQuantity = dialog.findViewById(R.id.tvQuantity);
        LinearLayout linlayQuantity =   dialog.findViewById(R.id.linlayQuantity);
        LinearLayout linlayAmount =   dialog.findViewById(R.id.linlayAmount);
        TextView tvAmount = dialog.findViewById(R.id.tvAmount);
        TextView tvHeading = dialog.findViewById(R.id.tvHeading);
//                final TextView cb_price_text = dialog.findViewById(R.id.cb_price_text);
        EditText etQuantity = dialog.findViewById(R.id.etQuantity);
        EditText etAmount = dialog.findViewById(R.id.etAmount);
        EditText etVehicleNumber = dialog.findViewById(R.id.etVehicleNumber);
        EditText etVehicle_kms = dialog.findViewById(R.id.etVehicle_kms);
        EditText etMobile = dialog.findViewById(R.id.etMobile);
        EditText etEmail = dialog.findViewById(R.id.etEmail);
        TextView tvDateTime = dialog.findViewById(R.id.tvDateTime);

        Calendar cal = Calendar.getInstance();
        tvDateTime.setText(MyUtils.dateToString(cal.getTime(), AppConst.APP_DATE_TIME_FORMAT));

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        tvHeading.setText(product_name + " - " + MyUtils.formatCurrency(product_price));
        //cb_price_text.setText(product_price+" Rs");

        dialog.show();

        etQuantity.setEnabled(false);
        etAmount.setEnabled(false);

        etVehicle_kms.setFilters(new InputFilter[] {new MyUtils.InputFilterMinMax(1, 999999)});

        final String[] selectedType = new String[] {""};

        tvQuantity.setOnClickListener(view -> {
            setBackground(tvQuantity, R.color.red_dark, R.color.white);
            setBackground(tvAmount, R.drawable.rec_border_red_counter_bill, R.color.red_dark);
            etQuantity.setEnabled(true);
            etQuantity.setBackground(ContextCompat.getDrawable(mContext, R.drawable.general_et_background));
            etQuantity.requestFocus();
            etAmount.setEnabled(false);
            etAmount.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_rect_border_disabled));
            selectedType[0] = "quantity";

            etQuantity.setFilters(new InputFilter[] {new MyUtils.InputFilterMinMaxForLitre(Const.LITRE_MIN, Const.LITRE_MAX)});
            etAmount.setFilters(new InputFilter[] {});
        });

        tvAmount.setOnClickListener(view -> {
            setBackground(tvAmount, R.color.red_dark, R.color.white);
            setBackground(tvQuantity, R.drawable.rec_border_red_counter_bill, R.color.red_dark);
            etAmount.setEnabled(true);
            etAmount.setBackground(ContextCompat.getDrawable(mContext, R.drawable.general_et_background));
            etQuantity.setEnabled(false);
            etQuantity.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_rect_border_disabled));
            etAmount.requestFocus();

            selectedType[0] = "amount";

            etAmount.setFilters(new InputFilter[] {new MyUtils.InputFilterMinMax(Const.AMOUNT_MIN, Const.AMOUNT_MAX)});
            etQuantity.setFilters(new InputFilter[] {});
        });


        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (selectedType[0].equalsIgnoreCase("amount")) {

                    if (charSequence.toString().length() > 0){
                        Double newAmount = MyUtils.parseDouble(charSequence.toString().trim());
                        Double productPrice = MyUtils.parseDouble(product_price);
                        Double newLitres = newAmount / productPrice ;
                        etQuantity.setText(String.format("%.2f", newLitres));
                    } else {
                        etQuantity.setText("0");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        etQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (selectedType[0].equalsIgnoreCase("quantity")) {
                    if (charSequence.toString().length() > 0){
                        Double newLitres = MyUtils.parseDouble(charSequence.toString().trim());
                        Double productPrice = MyUtils.parseDouble(product_price);
                        Double newAmount = newLitres * productPrice ;
                        etAmount.setText(String.format("%.2f", newAmount));
                    } else {
                        etAmount.setText("0");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        btnCancel.setOnClickListener(view1 ->  dialog.dismiss());

        btnSave.setOnClickListener(view -> {

            if (!MyUtils.hasInternetConnection(mContext.getApplicationContext())) {
                MLog.showToast(mContext.getApplicationContext(), AppConst.NO_INTERNET_MSG);
                return;
            }

            String amount = etAmount.getText().toString().trim();
            String quantity = etQuantity.getText().toString().trim();
            String vehicleNo = etVehicleNumber.getText().toString().trim();
            double dQunatity = MyUtils.parseDouble(quantity);
            double dAmount = MyUtils.parseDouble(amount);

            etAmount.setError(null);
            etQuantity.setError(null);

            if (selectedType[0].isEmpty()) {
                MLog.showToast(mContext, "Enter either Quantity or Amount");
                return;
            } else if (selectedType[0].equalsIgnoreCase(AppConst.amount)
                    && amount.isEmpty()) {
                etAmount.setError("Enter Amount");
                etAmount.requestFocus();
                return;
            } else if (selectedType[0].equalsIgnoreCase(AppConst.amount) && (dAmount < Const.AMOUNT_MIN || dAmount > Const.AMOUNT_MAX)) {
                etAmount.setError("Amount must be between 1 and 5,00,0000");
                etAmount.requestFocus();
                return;
            } else if (selectedType[0].equalsIgnoreCase(AppConst.amount)
                    && (dQunatity > Const.LITRE_MAX || dQunatity < Const.LITRE_MIN) ) {
                etAmount.setError("Quantity must be between 0.01 and 5000");
                MLog.showToast(mContext, "Quantity must be between 0.01 and 5000");
                etAmount.requestFocus();
                return;
            } else if (selectedType[0].equalsIgnoreCase("quantity")
                    && quantity.isEmpty()) {
                etQuantity.setError("Enter Quantity.");
                etQuantity.requestFocus();
                return;
            } else if (selectedType[0].equalsIgnoreCase("quantity") && (dQunatity < Const.LITRE_MIN || dQunatity > Const.LITRE_MAX)) {
                etQuantity.setError("Quantity must be between 0.01 and 5000");
                etQuantity.requestFocus();
                return;
            } else if (selectedType[0].equalsIgnoreCase("quantity") &&
                    (dAmount < Const.AMOUNT_MIN || dAmount > Const.AMOUNT_MAX)) {
                MLog.showToast(mContext, "Amount must be between 1 and 5,00,0000");
                etQuantity.setError("Amount must be between 1 and 5,00,0000");
                etQuantity.requestFocus();
                return;
            } else if (!vehicleNo.isEmpty() && vehicleNo.length() < 3) {
                etVehicleNumber.setError("Vehicle/Machine Number must contain at least 3 chars");
                etVehicleNumber.requestFocus();
                return;
            } else if (etMobile.getText().toString().length() != 10) {
                etMobile.setError("Enter valid customer mobile number");
                etMobile.requestFocus();
                return;
            }

            btnSave.setEnabled(false);

            progressDialog.show();
            String authkey = MyPreferences.getStringValue(view.getContext(),"authkey");

            CounterBillPojo counterBillPojo = new CounterBillPojo();
            counterBillPojo.setAmount(MyUtils.parseDouble(amount));
            counterBillPojo.setQuantity(MyUtils.parseDouble(quantity));
            counterBillPojo.setEmail(etEmail.getText().toString());
            counterBillPojo.setMobile(etMobile.getText().toString());
            counterBillPojo.setProduct_id(item.getId());
            counterBillPojo.setVehicle_number(etVehicleNumber.getText().toString());
            counterBillPojo.setV_meter(etVehicle_kms.getText().toString());

            Call<ResponseBody> call_createConuterBill = APIClient.getApiService().createConuterBill(authkey, AppConst.content_type, counterBillPojo);
            call_createConuterBill.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    try {
                        JSONObject jsonObject;
                        if (response.isSuccessful()) {
                            jsonObject = new JSONObject(response.body().string());
                            if (jsonObject.getBoolean("success")) {
                                String msg = "Counter Bill Send to "+etMobile.getText().toString();
                                MLog.showToast(mContext, msg);
                                dialog.dismiss();
                            }
                        } else {
                            jsonObject = new JSONObject(response.errorBody().string());
                            if (jsonObject.has("message"))
                                MLog.showLongToast(view.getContext(), jsonObject.getString("message"));

                            if (jsonObject.has("errors")) {
                                JSONObject errorObject = jsonObject.getJSONObject("errors");

                                if (errorObject.has("vehicle_number")) {
                                    String error = errorObject.getJSONArray("vehicle_number").getString(0);
                                    etVehicleNumber.setError(error);
                                } else if (errorObject.has("mobile")) {
                                    String error = errorObject.getJSONArray("mobile").getString(0);
                                    etMobile.setError(error);
                                } else if (errorObject.has("email")) {
                                    String error = errorObject.getJSONArray("email").getString(0);
                                    etEmail.setError(error);
                                } else if (errorObject.has("v_meter")) {
                                    String error = errorObject.getJSONArray("v_meter").getString(0);
                                    etVehicle_kms.setError(error);
                                } else if (errorObject.has("amount")) {
                                    String error = errorObject.getJSONArray("amount").getString(0);
                                    etAmount.setError(error);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();
                    btnSave.setEnabled(true);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    btnSave.setEnabled(true);
                    progressDialog.dismiss();
                    MLog.showToast(mContext, t.getMessage());
                }
            });
        });
    }
}

