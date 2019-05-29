package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.utils.MyPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import dmax.dialog.SpotsDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends SuperActivity {


    private TextView tvID, tvUsername, tvUserRole, tvMobileNumber, tvEmail;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("Profile");
        setupBackNavigation(null);
        progressDialog = new SpotsDialog(ProfileActivity.this, R.style.Custom);
        findViewById();

        String profileResponse = MyPreferences.getStringValue(this, AppConst.USER_PROFILE_DATA);
        if (profileResponse.isEmpty()) {
            getuserProfile();
        } else {
            showData();
        }
    }

    private void findViewById() {
        tvID = findViewById(R.id.tvID);
        tvUsername = findViewById(R.id.tvUsername);
        tvUserRole = findViewById(R.id.tvUserRole);
        tvMobileNumber = findViewById(R.id.tvMobileNumber);
        tvEmail = findViewById(R.id.tvEmail);
    }

    public void getuserProfile() {
        progressDialog.show();

        String authkey = MyPreferences.getStringValue(getApplicationContext(), "authkey");
        Call<ResponseBody> responce = APIClient.getApiService().getUserDetails(authkey);
        responce.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    String result;
                    try {
                        result = response.body().string();
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject dataObject = jsonObject.getJSONObject("data");
                        MyPreferences.setStringValue(ProfileActivity.this, AppConst.USER_PROFILE_DATA, dataObject.toString());
                        showData();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(getApplicationContext(),jObjError.getString("message"),Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                MLog.showLongToast(getApplicationContext(), t.getMessage());
            }
        });
    }

    private void showData() {
        String profileResponse = MyPreferences.getStringValue(this, AppConst.USER_PROFILE_DATA);

        try {
            JSONObject data = new JSONObject(profileResponse);
            String userName = data.getString("first_name") +" "+ data.getString("last_name");
            String userRole = data.getString("formatted_role");
            String mobile = data.getString("mobile");
            String email = data.getString("email");

            try {
                JSONArray outletArray = data.getJSONObject("outlet").getJSONArray("data");
                if (outletArray.length() > 0) {
                    tvID.setText(outletArray.getJSONObject(0).getString("formatted_id"));
                }
            } catch (Exception e) { e.printStackTrace(); }

            tvUsername.setText(userName);
            tvUserRole.setText(userRole);
            tvMobileNumber.setText(mobile);
            tvEmail.setText(email);
        } catch (Exception e) { e.printStackTrace(); }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
