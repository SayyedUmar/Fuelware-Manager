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

import com.fuelware.app.fw_manager.Const.AppConst;
import com.fuelware.app.fw_manager.activities.CounterBillActivity;
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

import static com.fuelware.app.fw_manager.R.color;
import static com.fuelware.app.fw_manager.R.drawable;
import static com.fuelware.app.fw_manager.R.id;
import static com.fuelware.app.fw_manager.R.layout;
import static com.fuelware.app.fw_manager.R.style;


public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.MyViewHolder> {
    private ArrayList<ProductsPojo> dataSet;
    Context mContext;
    private Dialog progressDialog;

    public ProductListAdapter(ArrayList<ProductsPojo> data, CounterBillActivity counterBillActivity) {
        this.dataSet = data;
        this.mContext = counterBillActivity;
        progressDialog = new SpotsDialog(mContext, style.Custom);
    }

    @NonNull
    @Override
    public ProductListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout.row_products, parent, false);

        return new ProductListAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        final ProductsPojo productsPojo = dataSet.get(position);
        holder.price.setText(productsPojo.getPrice()+" Rs");
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
            product = itemView.findViewById(id.product_name);
            price = itemView.findViewById(id.product_price);
            linlayContainer = itemView.findViewById(id.linlayContainer);
        }
    }

    private void setOnClickListener(ProductsPojo item) {
        String product_name = item.getProduct();
        String product_price = item.getPrice();

        Dialog dialog = new Dialog(mContext);
        dialog.setContentView(layout.dialog_counter_bill);
        TextView btnCancel = dialog.findViewById(id.btnCancel);
        TextView btnSave = dialog.findViewById(id.btnSave);
        TextView tvQuantity = dialog.findViewById(id.tvQuantity);
        LinearLayout linlayQuantity =   dialog.findViewById(id.linlayQuantity);
        LinearLayout linlayAmount =   dialog.findViewById(id.linlayAmount);
        TextView tvAmount = dialog.findViewById(id.tvAmount);
        TextView tvHeading = dialog.findViewById(id.tvHeading);
//                final TextView cb_price_text = dialog.findViewById(R.id.cb_price_text);
        EditText etQuantity = dialog.findViewById(id.etQuantity);
        EditText etAmount = dialog.findViewById(id.etAmount);
        EditText etVehicleNumber = dialog.findViewById(id.etVehicleNumber);
        EditText etVehicle_kms = dialog.findViewById(id.etVehicle_kms);
        EditText etMobile = dialog.findViewById(id.etMobile);
        EditText etEmail = dialog.findViewById(id.etEmail);
        TextView tvDateTime = dialog.findViewById(id.tvDateTime);

        Calendar cal = Calendar.getInstance();
        tvDateTime.setText(MyUtils.dateToString(cal.getTime(), AppConst.APP_DATE_TIME_FORMAT));

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        tvHeading.setText(product_name + " - Rs " + product_price);
        //cb_price_text.setText(product_price+" Rs");

        dialog.show();

        etQuantity.setEnabled(false);
        etAmount.setEnabled(false);

        etVehicle_kms.setFilters(new InputFilter[] {new MyUtils.InputFilterMinMax(1, 999999)});

        final String[] selectedType = new String[] {""};

        tvQuantity.setOnClickListener(view -> {
            setBackground(tvQuantity, color.red_dark, color.white);
            setBackground(tvAmount, drawable.rec_border_red_counter_bill, color.red_dark);
            etQuantity.setEnabled(true);
            etQuantity.setBackground(ContextCompat.getDrawable(mContext, drawable.general_et_background));
            etQuantity.requestFocus();
            etAmount.setEnabled(false);
            etAmount.setBackground(ContextCompat.getDrawable(mContext, drawable.bg_rect_border_disabled));
            selectedType[0] = "quantity";

            etQuantity.setFilters(new InputFilter[] {new MyUtils.InputFilterMinMaxForLitre(0.01, 5000)});
            etAmount.setFilters(new InputFilter[] {});
        });

        tvAmount.setOnClickListener(view -> {
            setBackground(tvAmount, color.red_dark, color.white);
            setBackground(tvQuantity, drawable.rec_border_red_counter_bill, color.red_dark);
            etAmount.setEnabled(true);
            etAmount.setBackground(ContextCompat.getDrawable(mContext, drawable.general_et_background));
            etQuantity.setEnabled(false);
            etQuantity.setBackground(ContextCompat.getDrawable(mContext, drawable.bg_rect_border_disabled));
            etAmount.requestFocus();

            selectedType[0] = "amount";

            etAmount.setFilters(new InputFilter[] {new MyUtils.InputFilterMinMax(1, 500000)});
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

            etAmount.setError(null);
            etQuantity.setError(null);

            if (selectedType[0].isEmpty()) {
                MLog.showToast(mContext, "Please enter either Quantity or Amount.");
                return;
            } else if (selectedType[0].equalsIgnoreCase("amount")) {
                if (amount.isEmpty()) {
                    etAmount.setError("Enter valid Amount");
                    etAmount.requestFocus();
                    return;
                } else if (MyUtils.parseDouble(amount) <= 0) {
                    etAmount.setError("Amount must be equal or higher than 1.");
                    etAmount.requestFocus();
                    return;
                }
            } else if (selectedType[0].equalsIgnoreCase("quantity")) {
                if (quantity.isEmpty()) {
                    etQuantity.setError("Enter valid Quantity");
                    etQuantity.requestFocus();
                    return;
                } else if (!(MyUtils.parseDouble(quantity) > 0.01)) {
                    etQuantity.setError("Quantity must be equal or higher than 0.01.");
                    etQuantity.requestFocus();
                    return;
                }

            } else if (!vehicleNo.isEmpty() && vehicleNo.length() < 5) {
                etVehicleNumber.setError("Vehicle Number must contain at least 5 chars.");
                etVehicleNumber.requestFocus();
                return;
            } else if (etMobile.getText().toString().length() != 10) {
                etMobile.setError("Enter valid Mobile Number");
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
                                    String error = errorObject.getJSONArray("vehicle_number").get(0).toString();
                                    etVehicleNumber.setError(error);
                                }
                                if (errorObject.has("mobile")) {
                                    String error = errorObject.getJSONArray("mobile").get(0).toString();
                                    etMobile.setError(error);
                                }
                                if (errorObject.has("email")) {
                                    String error = errorObject.getJSONArray("email").get(0).toString();
                                    etEmail.setError(error);
                                }
                                if (errorObject.has("v_meter")) {
                                    String error = errorObject.getJSONArray("v_meter").get(0).toString();
                                    etVehicle_kms.setError(error);
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
