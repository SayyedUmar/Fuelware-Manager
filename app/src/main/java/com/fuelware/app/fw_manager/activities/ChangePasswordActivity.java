package com.fuelware.app.fw_manager.activities;


import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.services.MyService;
import com.fuelware.app.fw_manager.services.MyServiceListerner;
import com.fuelware.app.fw_manager.utils.MyPreferences;
import com.fuelware.app.fw_manager.utils.MyUtils;

import org.json.JSONObject;

import dmax.dialog.SpotsDialog;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class ChangePasswordActivity extends SuperActivity {


    private EditText etOldPassword, etNewPasssword, etNewPasssword1;
    private Button btnSubmit;
    private String oldPass;
    private String newPass;
    private Dialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepassword);
        setTitle("Change Password");
        progressDialog = new SpotsDialog(this);
        setupBackNavigation(null);
        findViewById();
        btnSubmit.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_dark));
        btnSubmit.setEnabled(false);
        etNewPasssword1.setEnabled(false);
        etNewPasssword1.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rect_border_disabled));
        setOnclickListener();
    }

    private void findViewById() {
        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPasssword = findViewById(R.id.etNewPasssword);
        etNewPasssword1 = findViewById(R.id.etNewPasssword1);
        btnSubmit = findViewById(R.id.btnSubmit);
    }


    private void setOnclickListener() {
        btnSubmit.setOnClickListener(view -> {
            if (isValidData()) {
                changePassword();
            }
        });

        etNewPasssword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                String string = editable.toString().trim();
                if (!MyUtils.isValidPassword(string) || string.length() < 6) {
                    etNewPasssword.setError("Password must be 6 to 20 char long and must contain at least 1 alphabet and 1 number.");
                    etNewPasssword1.setEnabled(false);
                    etNewPasssword1.setBackground(ContextCompat.getDrawable(ChangePasswordActivity.this, R.drawable.bg_rect_border_disabled));
                } else {
                    etNewPasssword1.setEnabled(true);
                    etNewPasssword1.setBackground(ContextCompat.getDrawable(ChangePasswordActivity.this, R.drawable.general_et_background));
                    etNewPasssword.setError(null);
                }
            }
        });

        etNewPasssword1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                String string = editable.toString().trim();
                String newPass = etNewPasssword.getText().toString().trim();
                if (newPass.equalsIgnoreCase(string)) {
                    etNewPasssword1.setError(null);
                    btnSubmit.setEnabled(true);
                    btnSubmit.setBackgroundColor(ContextCompat.getColor(ChangePasswordActivity.this, R.color.colorPrimary));
                } else {
                    btnSubmit.setBackgroundColor(ContextCompat.getColor(ChangePasswordActivity.this, R.color.gray_dark));
                    btnSubmit.setEnabled(false);
                    etNewPasssword1.setError("Confirm password must be same as new password.");
                }
            }
        });
    }

    private void changePassword() {
        if (!MyUtils.hasInternetConnection(this)) {
            Toast.makeText(getApplicationContext(), AppConst.NO_INTERNET_MSG, Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();

        ChangePasswordModel model = new ChangePasswordModel(oldPass, newPass);


        MyService.CallAPI(APIClient.getApiService().changePassword(MyPreferences.getStringValue(this, "authkey"), model),
                new MyServiceListerner<Response<ResponseBody>>() {
                    @Override
                    public void onNext(Response<ResponseBody> response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject result;
                            if (response.isSuccessful()) {
                                result = new JSONObject(response.body().string());
                                String msg = result.getString("message");
                                Toast.makeText(ChangePasswordActivity.this, msg, Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                result = new JSONObject(response.errorBody().string());
                                if (result.has("errors")) {
                                    JSONObject errorObject = result.getJSONObject("errors");

                                    if (errorObject.has("old_password")) {
                                        String error = errorObject.getJSONArray("old_password").get(0).toString();
                                        etOldPassword.setError(error);
                                    }
                                    if (errorObject.has("password")) {
                                        String error = errorObject.getJSONArray("password").get(0).toString();
                                        etNewPasssword.setError(error);
                                    }
                                }
                                Toast.makeText(getApplicationContext(), result.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressDialog.dismiss();
                        handlerNetworkError(e);
                    }

                    @Override
                    public void onComplete() {
                        progressDialog.dismiss();
                    }

                });
    }


    private boolean isValidData() {
        oldPass = etOldPassword.getText().toString().trim();
        newPass = etNewPasssword.getText().toString().trim();
        String newPass1 = etNewPasssword1.getText().toString().trim();

        etOldPassword.setError(null);
        etNewPasssword.setError(null);
        etNewPasssword1.setError(null);

        if (oldPass.isEmpty()) {
            etOldPassword.setError("Enter old password!");
            etOldPassword.requestFocus();
            return false;
        } else if (newPass.isEmpty()) {
            etNewPasssword.setError("Enter new password!");
            etNewPasssword.requestFocus();
            return false;
        } else if (newPass1.isEmpty()) {
            etNewPasssword1.setError("Reenter new password!");
            etNewPasssword1.requestFocus();
            return false;
        } else if (!newPass.equals(newPass1)) {
            etNewPasssword1.setError("Confirm password doesn't match with new password!");
            etNewPasssword1.requestFocus();
            return false;
        } else if (etNewPasssword.getError() != null ) {
            etNewPasssword.requestFocus();
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

    public static class ChangePasswordModel {

        ChangePasswordModel(String old_password, String password) {
            this.old_password = old_password;
            this.password = password;
        }

        String old_password;
        String password;
    }

}
