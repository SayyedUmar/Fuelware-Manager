package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fuelware.app.fw_manager.Const.AppConst;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.adapters.SpinnerAdapterNew;
import com.fuelware.app.fw_manager.models.PlanModel;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.utils.MyPreferences;
import com.fuelware.app.fw_manager.utils.MyUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.GridHolder;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlansActivity extends SuperActivity {


    @BindView(R.id.tvViewPlans)
    TextView tvViewPlans;
    private AlertDialog progress;
    private String authkey;
    private Gson gson;
    private List<PlanModel> planList = new ArrayList<>();
    private SpinnerAdapterNew plansAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myplans);
        ButterKnife.bind(this);

        setupBackNavigation(null);
        setTitle("My Plans");

        initialize();
        setEventListener();
        fetchAllPlans();
    }

    private void initialize() {
        progress = new SpotsDialog(this, R.style.Custom);
        authkey = MyPreferences.getStringValue(getApplicationContext(), "authkey");
        gson = new Gson();
    }

    private void fetchAllPlans() {
        if (!MyUtils.hasInternetConnection(this)) {
            MLog.showToast(this, AppConst.NO_INTERNET_MSG);
            return;
        }

        progress.show();

        APIClient.getApiService().getPlans(authkey).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONObject data = jsonObject.getJSONObject("data");
                        Type token = new TypeToken<List<PlanModel>>(){}.getType();
                        List<PlanModel> tempList = gson.fromJson(data.getJSONArray("plans").toString(), token);
                        planList.clear();
                        planList.addAll(tempList);
                        plansAdapter.notifyDataSetChanged();
                    } else {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        if (jsonObject.has("message"))
                            MLog.showLongToast(getApplicationContext(), jsonObject.getString("message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                progress.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progress.dismiss();
                MLog.showLongToast(getApplicationContext(), t.getMessage());
            }
        });
    }

    private void setEventListener() {
        plansAdapter = new SpinnerAdapterNew<PlanModel>(planList) {
            @Override
            public View onCreateView(int pos, View view, ViewGroup parent) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_plans_grid, parent, false);
                return v;
            }

            @Override
            public void onBindView(int pos, PlanModel model) {

            }
        };

        tvViewPlans.setOnClickListener(v -> {
            hideSoftKeyboard();
            DialogPlus dialog = DialogPlus.newDialog(this)
                    .setContentHolder(new GridHolder(2))
                    .setFooter(R.layout.dialog_footer, true)
                    .setCancelable(true)
                    .setGravity(Gravity.CENTER)
                    .setAdapter(plansAdapter)
                    .setExpanded(true)  // This will enable the expand feature, (similar to android L share dialog)
                    .setContentHeight(MyUtils.dpToPx(200))
                    .setOnCancelListener(d -> d.dismiss())
                    .setOnItemClickListener((dialog1, item, view, position) -> {
                        dialog1.dismiss();

                    })


                    .create();
            dialog.show();
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
