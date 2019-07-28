package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.models.ForgotPasswordModel;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.utils.MyUtils;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpVerificationActivity extends SuperActivity {

    @BindView(R.id.etOTP)
    EditText etOTP;
    @BindView(R.id.btnVerify)
    Button btnVerify;
    @BindView(R.id.btnResend)
    Button btnResend;
    @BindView(R.id.tvMessage)
    TextView tvMessage;

    private AlertDialog progressDialog;
    private String mobileNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verfication);
        ButterKnife.bind(this);
        setupBackNavigation(null);
        setTitle("Verify OTP");

        init();
        setEventListeners();
    }

    private void init() {
        mobileNumber = getIntent().getStringExtra("number_requested_otp");
        progressDialog = new SpotsDialog(this, R.style.Custom);
        tvMessage.setText("OTP has sent to your primary Mobile "+mobileNumber+", Enter OTP to continue");
    }

    private void setEventListeners() {
        btnResend.setOnClickListener(view -> resendOTP());
        btnVerify.setOnClickListener(view -> verifyOTP());
    }

    private void verifyOTP() {
        String otpString = etOTP.getText().toString().trim();
        if (otpString.length() == 6 ) {
            progressDialog.show();
            ForgotPasswordModel resetPasswordPojo = new ForgotPasswordModel();
            resetPasswordPojo.setMobile(mobileNumber);
            resetPasswordPojo.setOtp(MyUtils.parseLong(otpString));

            Call<ResponseBody> request_otp = APIClient.getApiService().verifyOtp(AppConst.content_type, resetPasswordPojo);
            request_otp.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject;
                        if (response.isSuccessful()) {
                            jsonObject = new JSONObject(response.body().string());
                            JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                            String token = jsonObject1.getString("token");
                            showDialog(token);
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
                    progressDialog.dismiss();
                    MLog.showToast(getApplicationContext(), t.getMessage());
                }
            });
        } else {
            Toast.makeText(getApplicationContext(),"Enter otp sent to you properly",Toast.LENGTH_LONG).show();
        }
    }

    private void resendOTP() {
        progressDialog.show();

        ForgotPasswordModel resetPasswordPojo = new ForgotPasswordModel();
        resetPasswordPojo.setMobile(mobileNumber);

        Call<ResponseBody> resetPassword = APIClient.getApiService().resetPassword(AppConst.content_type,resetPasswordPojo);
        resetPassword.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                try {
                    JSONObject jsonObject;
                    if (response.isSuccessful()) {

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
                progressDialog.dismiss();
                MLog.showToast(getApplicationContext(), t.getMessage());
            }
        });
    }

    public void showDialog(final String accesstoken){

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_of_resetpass);
        final EditText etPass = dialog.findViewById(R.id.reset_dialog1);
        final EditText etPass2 = dialog.findViewById(R.id.reset_dialog2);
        final TextView dimoney_cancel = dialog.findViewById(R.id.button_profile_cancel);
        final TextView dimoney_save = dialog.findViewById(R.id.button_profile_save);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        dialog.show();

        etPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                String string = editable.toString().trim();
                if (!MyUtils.isValidPassword(string) || string.length() < 6) {
                    etPass.setError("Password must be 6 to 20 char long and must contain at least 1 alphabet and 1 number.");
                } else {
                    etPass.setError(null);
                }
            }
        });

        dimoney_cancel.setOnClickListener(view -> dialog.dismiss());
        dimoney_save.setOnClickListener(view -> {

            if (!MyUtils.hasInternetConnection(this)) {
                MLog.showToast(this, AppConst.NO_INTERNET_MSG);
                return;
            }
            String password = etPass.getText().toString().trim();
            String reenterpassword = etPass2.getText().toString().trim();


            if (password.isEmpty()) {
                etPass.setError("Enter password.");
                etPass.requestFocus();
                return;
            } else if (password.length() < 6) {
                etPass.setError("Password must contain at least 8 char.");
                etPass.requestFocus();
                return;
            } else if (reenterpassword.isEmpty()) {
                etPass2.setError("Enter confirm password.");
                etPass2.requestFocus();
                return;
            } else if (!password.equals(reenterpassword)) {
                etPass2.setError("Confirm password must be same as new password.");
                etPass2.requestFocus();
                return;
            } else if (etPass.getError() != null) {
                etPass.requestFocus();
                return;
            }

            if (password.equals(reenterpassword)) {
                ForgotPasswordModel resetPasswordPojo = new ForgotPasswordModel();
                resetPasswordPojo.setToken(accesstoken);
                resetPasswordPojo.setPassword(password);

                progressDialog.show();

                Call<ResponseBody> request_otp = APIClient.getApiService().setNewPassword(AppConst.content_type, resetPasswordPojo);
                request_otp.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject res;
                            if (response.isSuccessful()) {
                                MLog.showToast(getApplicationContext(), "your password successfully updated");
                                res = new JSONObject(response.body().string());
                                Intent intent = new Intent(OtpVerificationActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                res = new JSONObject(response.errorBody().string());
                                if (res.has("errors")) {
                                    JSONObject errorObject = res.getJSONObject("errors");
                                    if(errorObject.has("Password")) {
                                        String error = errorObject.getJSONArray("etPassword").get(0).toString();
                                        etPass.setError(error);
                                        etPass.requestFocus();
                                    }
                                }

                                if (res.has("message"))
                                    MLog.showLongToast(getApplicationContext(), res.getString("message"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        progressDialog.dismiss();
                        MLog.showToast(getApplicationContext(), t.getMessage());
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(),"Password does not match",Toast.LENGTH_LONG).show();
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
