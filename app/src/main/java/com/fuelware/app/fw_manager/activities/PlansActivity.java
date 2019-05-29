package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.adapters.PlanHistoryAdapter;
import com.fuelware.app.fw_manager.adapters.SpinnerAdapterNew;
import com.fuelware.app.fw_manager.models.Coupon;
import com.fuelware.app.fw_manager.models.PlanHistory;
import com.fuelware.app.fw_manager.models.PlanModel;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.utils.MyPreferences;
import com.fuelware.app.fw_manager.utils.MyUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.GridHolder;

import org.json.JSONArray;
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


    @BindView(R.id.tvBasePrice)
    TextView tvBasePrice;
    @BindView(R.id.tvGst)
    TextView tvGst;
    @BindView(R.id.tvDiscount)
    TextView tvDiscount;
    @BindView(R.id.tvSubtotal)
    TextView tvSubtotal;
    @BindView(R.id.tvTotal)
    TextView tvTotal;

    @BindView(R.id.btnPay)
    Button btnPay;
    @BindView(R.id.linlayContainer)
    LinearLayout linlayContainer;
    @BindView(R.id.radioMyPlans)
    RadioButton radioMyPlans;
    @BindView(R.id.radioHistory)
    RadioButton radioHistory;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    @BindView(R.id.btnViewPlans)
    Button btnViewPlans;
    @BindView(R.id.btnAddCoupon)
    Button btnAddCoupon;
    private AlertDialog progress;
    private String authkey;
    private Gson gson;
    private List<PlanModel> planList = new ArrayList<>();
    private List<Coupon> coupons = new ArrayList<>();
    private GridAdapter plansAdapter;
    private SpinnerAdapterNew couponAdapter;
    Boolean isSmsSubscribed;
    private PlanModel selectedPlan;
    private String selectedCouponCode = "";
    private PlanHistoryAdapter historyAdapter;
    private List<PlanHistory> historyList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myplans);
        ButterKnife.bind(this);

        setupBackNavigation(null);
        setTitle("My Plans");

        initialize();
        setEventListener();
        setupRecycler();
        fetchAllPlans("");
        linlayContainer.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void setupRecycler() {
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        historyAdapter = new PlanHistoryAdapter(historyList, this);
        recyclerView.setAdapter(historyAdapter);
    }

    private void initialize() {
        progress = new SpotsDialog(this, R.style.Custom);
        authkey = MyPreferences.getStringValue(getApplicationContext(), "authkey");
        gson = new Gson();
    }

    private void fetchAllPlans(String duration) {
        if (!MyUtils.hasInternetConnection(this)) {
            MLog.showToast(this, AppConst.NO_INTERNET_MSG);
            return;
        }

        progress.show();
        duration = duration.toLowerCase();
        duration = duration.replace(" ", "_");

        if (duration.equalsIgnoreCase("all"))
            duration = "";

        Call<ResponseBody> call;

        if (isSmsSubscribed != null) {
            call = APIClient.getApiService().getPlans(authkey, duration,isSmsSubscribed);
        } else {
            call = APIClient.getApiService().getPlans(authkey, duration);
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONObject data = jsonObject.getJSONObject("data");
//                        responseModel = gson.fromJson(data.toString(), PlanResponseModel.class);
                        Type token = new TypeToken<List<PlanModel>>(){}.getType();
                        List<PlanModel> tempList = gson.fromJson(data.getJSONArray("plans").toString(), token);
                        Type token2 = new TypeToken<List<Coupon>>(){}.getType();
                        List<Coupon> tempList2 = gson.fromJson(data.getJSONArray("coupons").toString(), token2);
                        planList.clear();
                        planList.addAll(tempList);
                        coupons.clear();
                        coupons.addAll(tempList2);
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

        radioHistory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            linlayContainer.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            fetchAllHistory();
        });

        radioMyPlans.setOnCheckedChangeListener((buttonView, isChecked) -> {
            linlayContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        });

        plansAdapter = new GridAdapter(planList);
        couponAdapter = new SpinnerAdapterNew<Coupon>(coupons) {
            @Override
            public View onCreateView(int pos, View view, ViewGroup parent) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_coupon_grid, parent, false);
                CouponHolder holder = new CouponHolder(v);
                v.setTag(holder);
                return v;
            }

            @Override
            public void onBindView(int pos, Coupon coupon, View v) {
                Coupon model = coupons.get(pos);
                CouponHolder holder = (CouponHolder) v.getTag();
                holder.tvCouponName.setText(model.getCode());
                double percent = model.getPercent();
                holder.tvText3.setText(MyUtils.parseToString(percent)+"% Off");
                holder.tvApply.setOnClickListener(v3 -> {
                    selectedCouponCode = model.getCode();
                    applyCoupon(selectedCouponCode);
                });
            }
        };

        GridHolder holder = new GridHolder(2);
        btnViewPlans.setOnClickListener(v -> {
            hideSoftKeyboard();
            DialogPlus dialog = DialogPlus.newDialog(this)
                    .setContentHolder(holder)
                    .setFooter(R.layout.dialog_footer)
                    .setCancelable(true)
                    .setAdapter(plansAdapter)
                    .setExpanded(true)  // This will enable the expand feature, (similar to android L share dialog)
                    .setContentHeight(MyUtils.dpToPx(200))
                    .setOnItemClickListener((dialog1, item, view, position) -> {
                        dialog1.dismiss();
                        selectedPlan = (PlanModel) item;
                        btnAddCoupon.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                        if (!selectedCouponCode.trim().isEmpty()) {
                            applyCoupon(selectedCouponCode);
                        }
                    }).create();
            holder.addHeader(getPlanHeaderView());
            dialog.findViewById(R.id.btnCancel).setOnClickListener(v1 -> {
                dialog.dismiss();
                isSmsSubscribed = null;
            });
            dialog.show();

        });

        btnAddCoupon.setOnClickListener(v -> {
            if (selectedPlan == null) {
                MLog.showToast(getApplicationContext(), "Please select a plan first.");
                return;
            }

            hideSoftKeyboard();
            DialogPlus dialog = DialogPlus.newDialog(this)
                    .setContentHolder(holder)
                    .setFooter(R.layout.dialog_footer)
                    .setCancelable(true)
                    .setAdapter(couponAdapter)
                    .setExpanded(true)  // This will enable the expand feature, (similar to android L share dialog)
                    .setContentHeight(MyUtils.dpToPx(200))
                    .setOnItemClickListener((dialog1, item, view, position) -> {
                        Coupon selectedCoupon = (Coupon) item;
                        selectedCouponCode = selectedCoupon.getCode();
                        applyCoupon(selectedCouponCode);
                        dialog1.dismiss();
                    }).create();
            holder.addHeader(getCouponHeaderView(dialog));
            dialog.findViewById(R.id.btnCancel).setOnClickListener(v1 -> dialog.dismiss());
            dialog.show();
        });

    }

    private void fetchAllHistory() {
        if (!MyUtils.hasInternetConnection(this)) {
            MLog.showToast(this, AppConst.NO_INTERNET_MSG);
            return;
        }

        progress.show();

        APIClient.getApiService().getPlanHistory(authkey).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray data = jsonObject.getJSONArray("data");

                        Type token = new TypeToken<List<PlanHistory>>(){}.getType();
                        List<PlanHistory> tempList = gson.fromJson(data.toString(), token);
                        historyList.clear();
                        historyList.addAll(tempList);
                        historyAdapter.refresh();
                    } else {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
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

    private void applyCoupon(String couponCode) {
        ApplyCouponParam param = new ApplyCouponParam();
        param.coupon_code = couponCode;
        param.plan_id = selectedPlan.getId();

        if (!MyUtils.hasInternetConnection(this)) {
            MLog.showToast(this, AppConst.NO_INTERNET_MSG);
            return;
        }

        progress.show();

        APIClient.getApiService().applyCouponCode(authkey, param).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        MLog.showToast(getApplicationContext(), "Coupon has been applied successfully.");
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONObject data = jsonObject.getJSONObject("data");
                        String base_price = data.getString("base_price");
                        double discount = data.getDouble("discount");
                        String price = data.getString("price");
                        String gst = data.getString("gst");
                        String total_price = data.getString("total_price");
                        String final_price = data.getString("final_price");

                        tvBasePrice.setText(MyUtils.formatCurrency(base_price));
                        tvDiscount.setText(MyUtils.formatCurrency(-discount));
                        tvSubtotal.setText(MyUtils.formatCurrency(price));
                        tvGst.setText(MyUtils.formatCurrency(gst));
                        tvTotal.setText(MyUtils.formatCurrency(total_price));
                        btnPay.setText("PAY "+MyUtils.formatCurrency(final_price));
                    } else {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());

                        if (jsonObject.has("errors")) {
                            JSONObject errorObject = jsonObject.getJSONObject("errors");
                            if (errorObject.has("coupon_code")) {
                                String error = errorObject.getJSONArray("coupon_code").get(0).toString();
                                MLog.showToast(getApplicationContext(), error);
                            }
                        } else  if (jsonObject.has("message")) {
                            MLog.showLongToast(getApplicationContext(), jsonObject.getString("message"));
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
                MLog.showLongToast(getApplicationContext(), t.getMessage());
            }
        });


    }

    private View getCouponHeaderView(DialogPlus dialog) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_coupon_grid_header, null, false);
        EditText editText = v.findViewById(R.id.etCouponCode);
        TextView tvApply = v.findViewById(R.id.tvApply);

        tvApply.setOnClickListener(v2 -> {
            dialog.dismiss();
            selectedCouponCode = editText.getText().toString().trim();
            applyCoupon(selectedCouponCode);
        });

        return v;
    }

    private View getPlanHeaderView() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_plan_grid_header, null, false);
        AutoCompleteTextView tvAutoDuration = v.findViewById(R.id.tvAutoDuration);
        CheckBox chkSms = v.findViewById(R.id.chkSms);
        chkSms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isSmsSubscribed = isChecked;
            fetchAllPlans(tvAutoDuration.getHint().toString());
        });

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, getResources()
                .getStringArray(R.array.array_duration));
        tvAutoDuration.setAdapter(arrayAdapter);
        tvAutoDuration.setOnClickListener(v1 -> {
            tvAutoDuration.showDropDown();
        });
        tvAutoDuration.setOnItemClickListener((parent, view, position, id) -> {
            tvAutoDuration.setHint(parent.getItemAtPosition(position).toString());
            fetchAllPlans(tvAutoDuration.getHint().toString());
        });
        return v;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    static class GridAdapter extends SpinnerAdapterNew {

        public GridAdapter(List list) {
            super(list);
        }

        @Override
        public View onCreateView(int pos, View view, ViewGroup parent) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_plans_grid, parent, false);
            PlanHolder holder = new PlanHolder(v);
            v.setTag(holder);
            return v;
        }

        @Override
        public void onBindView(int pos, Object o, View v) {
            PlanModel model = (PlanModel) o;
            PlanHolder holder = (PlanHolder) v.getTag();
            holder.tvPlanName.setText(model.getFormatted_plan_type()+"("+model.getPlan_name()+")");
            holder.tvText2.setText(model.getIndents()+" Indents in "+MyUtils.formatCurrency(model.getPrice()));
            holder.tvText3.setText(model.getDuration_description());
            holder.tvAmount.setText(MyUtils.formatCurrency(model.getFinal_price()));
        }

    }

    static class PlanHolder {
        @BindView(R.id.tvPlanName)
        TextView tvPlanName;
        @BindView(R.id.tvText2)
        TextView tvText2;
        @BindView(R.id.tvText3)
        TextView tvText3;
        @BindView(R.id.tvAmount)
        TextView tvAmount;

        PlanHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }

    static class CouponHolder {
        @BindView(R.id.tvCouponName)
        TextView tvCouponName;
        @BindView(R.id.tvText3)
        TextView tvText3;
        @BindView(R.id.tvApply)
        TextView tvApply;

        CouponHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }

    public static class ApplyCouponParam {
        String coupon_code;
        String plan_id;
    }
}
