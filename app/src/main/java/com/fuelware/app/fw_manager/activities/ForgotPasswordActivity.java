package com.fuelware.app.fw_manager.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.models.ForgotPasswordModel;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    @BindView(R.id.btnRequestOTP)
    Button btnRequestOTP;
    @BindView(R.id.etMobileNumber)
    EditText etMobileNumber;
    @BindView(R.id.tvBack)
    TextView tvBack;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(this);
        setEventListeners();
    }

    private void setEventListeners() {
        tvBack.setOnClickListener(view -> finish());
        btnRequestOTP.setOnClickListener(view -> {
            String mobileNumber = etMobileNumber.getText().toString().trim();
            if (mobileNumber.length()==10) {

                progressDialog.setMessage("Sending otp ...");
                progressDialog.setCancelable(false);
                // progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                progressDialog.show();

                ForgotPasswordModel resetPasswordPojo = new ForgotPasswordModel();
                resetPasswordPojo.setMobile(mobileNumber);

                Call<ResponseBody> resetPassword = APIClient.getApiService().resetPassword(AppConst.content_type,resetPasswordPojo);
                resetPassword.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        try {
                            JSONObject jsonObject;
                            if (response.isSuccessful()) {
                                jsonObject = new JSONObject(response.body().string());
                                Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
                                intent.putExtra("number_requested_otp", mobileNumber);
                                startActivity(intent);
                            } else {
                                jsonObject = new JSONObject(response.errorBody().string());
                                MLog.showToast(getApplicationContext(), jsonObject.getString("message"));
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
            } else {
                MLog.showToast(getApplicationContext(), "Enter valid 10 digits number");
            }
        });

    }

}
