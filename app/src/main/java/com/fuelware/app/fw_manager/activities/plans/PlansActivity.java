package com.fuelware.app.fw_manager.activities.plans;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.adapters.GeneriBaseAdapter;
import com.fuelware.app.fw_manager.adapters.MyPlansAdapter;
import com.fuelware.app.fw_manager.adapters.PlanHistoryAdapter;
import com.fuelware.app.fw_manager.adapters.SpinnerAdapterNew;
import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.appconst.Const;
import com.fuelware.app.fw_manager.models.Coupon;
import com.fuelware.app.fw_manager.models.PlanHistory;
import com.fuelware.app.fw_manager.models.PlanModel;
import com.fuelware.app.fw_manager.models.PurchasedPlan;
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
import info.hoang8f.android.segmented.SegmentedGroup;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlansActivity extends SuperActivity {


    @BindView(R.id.tvBasePrice)
    TextView tvBasePrice;
    @BindView(R.id.tvVersion)
    TextView tvVersion;
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

    @BindView(R.id.radioPlans)
    RadioButton radioPlans;
    @BindView(R.id.radioMyPlans)
    RadioButton radioMyPlans;
    @BindView(R.id.radioHistory)
    RadioButton radioHistory;

    @BindView(R.id.segment_group_plantype)
    SegmentedGroup segment_group_plantype;
    @BindView(R.id.radioCCM)
    RadioButton radioCCM;

    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    @BindView(R.id.btnViewPlans)
    Button btnViewPlans;
    @BindView(R.id.btnAddCoupon)
    Button btnAddCoupon;
    @BindView(R.id.btnClearCoupon)
    ImageButton btnClearCoupon;

    @BindView(R.id.tvPlanType)
    TextView tvPlanType;
    @BindView(R.id.tvRegisterationFee)
    TextView tvRegisterationFee;
    @BindView(R.id.linlayRegistrationFees)
    LinearLayout linlayRegistrationFees;

    private AlertDialog progress;
    private String authkey;
    private Gson gson;
    private List<PlanModel> planList = new ArrayList<>();
    private List<Coupon> coupons = new ArrayList<>();
    private GridAdapter plansAdapter;
    private SpinnerAdapterNew couponAdapter;
    boolean isSmsSubscribed;
    private PlanModel selectedPlan;
    private String selectedCouponCode = "";
    private PlanHistoryAdapter historyAdapter;
    private MyPlansAdapter purchasedPlansAdapter;
    private List<PlanHistory> historyList = new ArrayList<>();
    private List<PlanType> planTypes = new ArrayList<>();
    private List<PurchasedPlan> purchasedPlans = new ArrayList<>();
    private List<PurchasedPlan> ecmPlans = new ArrayList<>();
    private List<PurchasedPlan> ccmPlans = new ArrayList<>();
    private String selectedDuration = "Annually";
    private List<String> planTyesList = new ArrayList<>();
    private static boolean isPlanType_CCM = true;


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
        fetchAllPlansAndCoupons(selectedDuration);
        //fetchPlanTerms();
        linlayContainer.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        btnClearCoupon.setVisibility(View.GONE);
        linlayRegistrationFees.setVisibility(View.GONE);

        //if ()
        String subscription_type = MyPreferences.getStringValue(getApplicationContext(), Const.SUBSCRIPTION_TYPE);
        if (subscription_type.equals("free")) {
            tvVersion.setText("Credit Customer Management");
        } else {
            tvVersion.setText("Credit Customer Management - Preminum");
        }
    }

    private void setupRecycler() {
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        historyAdapter = new PlanHistoryAdapter(historyList, this);
        purchasedPlansAdapter = new MyPlansAdapter(purchasedPlans, this);
    }

    private void initialize() {
        progress = new SpotsDialog(this, R.style.Custom);
        authkey = MyPreferences.getStringValue(getApplicationContext(), "authkey");
        gson = new Gson();

        planTyesList.add("CCM");
        planTyesList.add("E-CCM");
    }

    private void fetchAllPlansAndCoupons(String duration) {
        if (!MyUtils.hasInternetConnection(this)) {
            MLog.showToast(this, AppConst.NO_INTERNET_MSG);
            return;
        }

        progress.show();
        selectedDuration = duration;
        duration = duration.toLowerCase();
        duration = duration.replace(" ", "_");

        if (duration.equalsIgnoreCase("all"))
            duration = "";

        Call<ResponseBody> call;

        String planType = tvPlanType.getText().toString().trim().toLowerCase();
        call = APIClient.getApiService().getPlans(authkey, duration, planType, isSmsSubscribed);

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


        tvPlanType.setOnClickListener(v -> {
            showPlanTypeDialog();
        });

        btnClearCoupon.setOnClickListener(v -> {
            selectedCouponCode = "";
            btnAddCoupon.setText("apply coupon");
            btnClearCoupon.setVisibility(View.GONE);
            showPlanData(selectedPlan, "");
        });

        radioHistory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                linlayContainer.setVisibility(View.GONE);
                segment_group_plantype.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                fetchAllHistory();
                recyclerView.setAdapter(historyAdapter);
            }
        });

        radioMyPlans.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                linlayContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                segment_group_plantype.setVisibility(View.VISIBLE);
                recyclerView.setAdapter(purchasedPlansAdapter);
                fetchPurchasedPlans();
            }
        });

        radioPlans.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                linlayContainer.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                segment_group_plantype.setVisibility(View.GONE);
            }
        });

        radioCCM.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                purchasedPlans.clear();
                purchasedPlans.addAll(ccmPlans);
                purchasedPlansAdapter.refresh();
            } else {
                purchasedPlans.clear();
                purchasedPlans.addAll(ecmPlans);
                purchasedPlansAdapter.refresh();
            }
        });

        plansAdapter = new GridAdapter(planList, this);
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
//                holder.tvApply.setOnClickListener(v3 -> {
//                    selectedCouponCode = model.getCode();
//                    applyCoupon(selectedCouponCode);
//
//                });
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
                    .setExpanded(false, MyUtils.dpToPx(400))  // This will enable the expand feature, (similar to android L share dialog)
                    .setOnItemClickListener((dialog1, item, view, position) -> {
                        dialog1.dismiss();
                        selectedPlan = (PlanModel) item;
                        plansAdapter.selectedPlan = selectedPlan;
                        btnAddCoupon.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                        showPlanData(selectedPlan, "");
                        if (!selectedCouponCode.trim().isEmpty()) {
                            applyCoupon(selectedCouponCode);
                        }
                    }).create();
            String planType = tvPlanType.getText().toString().trim().toLowerCase();
            if (planType.equalsIgnoreCase("e-ccm")) {
                holder.addHeader(getPlanHeaderView());
            }
            dialog.findViewById(R.id.btnCancel).setOnClickListener(v1 -> {
                dialog.dismiss();
                isSmsSubscribed = false;
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
                    .setExpanded(false, MyUtils.dpToPx(400))
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

        btnPay.setOnClickListener(v -> {
            if (selectedPlan == null) {
                MLog.showLongToast(getApplicationContext(), "Kinldy select a plan first.");
            } else {
                purchasePlan();
            }
        });

    }

    private void showPlanTypeDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_list_custom);
        ListView listView = dialog.findViewById(R.id.listView);
        TextView tvHeading = dialog.findViewById(R.id.tvHeading);
        tvHeading.setText("Select Plan Type");
        listView.setAdapter(new GeneriBaseAdapter<String>(planTyesList, R.layout.custom_spinner_adapter, new GeneriBaseAdapter.DailogListener() {
            @Override
            public TextView getHolder(View v, int pos) {
                TextView textView = v.findViewById(R.id.itemName);
                return textView;
            }
            @Override
            public void setView(View v, int pos) {
                ((TextView)v.getTag()).setText(planTyesList.get(pos));
            }
        }));
        listView.setOnItemClickListener((parent, view, pos, id) -> {
            tvPlanType.setText(planTyesList.get(pos));
            dialog.dismiss();
            String planType = tvPlanType.getText().toString().trim().toLowerCase();
            if (planType.equalsIgnoreCase("ccm")) {
                selectedDuration = "Annually";
                isPlanType_CCM = true;
            } else {
                isPlanType_CCM = false;
                selectedDuration = "Half Yearly";
            }
            fetchAllPlansAndCoupons(selectedDuration);
        });
        dialog.show();
    }

    private void purchasePlan() {
        ApplyCouponParam param = new ApplyCouponParam();
        param.coupon_code = selectedCouponCode;
        param.plan_id = selectedPlan.getId();

        if (!MyUtils.hasInternetConnection(this)) {
            MLog.showToast(this, AppConst.NO_INTERNET_MSG);
            return;
        }

        progress.show();

        APIClient.getApiService().purchasePlan(authkey, param).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONObject data = jsonObject.getJSONObject("data");
                        String redirect_url = data.getString("redirect_url");
                        String frontend_url = data.getString("frontend_url");
                        if (!redirect_url.isEmpty()) {
                            startActivity(new Intent(getBaseContext(), WebViewActivity.class)
                                    .putExtra("URL", redirect_url)
                                    .putExtra("frontend_url", frontend_url));
                        }
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

    private void showPlanData(PlanModel model, String discount) {
        btnViewPlans.setText("Change Plan");
        tvBasePrice.setText(model.getPrice());
        tvSubtotal.setText(model.getPrice());
        tvDiscount.setText(discount); // no discount
        tvGst.setText(model.getGst());
        tvTotal.setText(model.getTotal());
        btnPay.setText("PAY "+MyUtils.formatCurrency(model.getFinal_price()));
        btnPay.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
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
                        btnAddCoupon.setText("Change Coupon");
                        btnClearCoupon.setVisibility(View.VISIBLE);
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONObject data = jsonObject.getJSONObject("data");
                        String base_price = data.getString("base_price");
                        double discount = data.getDouble("discount");
                        String price = data.getString("price");
                        String gst = data.getString("gst");
                        String total_price = data.getString("total_price");
                        String final_price = data.getString("final_price");
                        if (selectedPlan.new_user) {
                            linlayRegistrationFees.setVisibility(View.VISIBLE);
                            tvRegisterationFee.setText(MyUtils.formatCurrency(selectedPlan.registration_fees));
                        }

                        tvBasePrice.setText(MyUtils.formatCurrency(base_price));
                        tvDiscount.setText(MyUtils.formatCurrency(-discount));
                        tvSubtotal.setText(MyUtils.formatCurrency(price));
                        tvGst.setText(MyUtils.formatCurrency(gst));
                        tvTotal.setText(MyUtils.formatCurrency(total_price));
                        btnPay.setText("PAY "+MyUtils.formatCurrency(final_price));
                        btnPay.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary));
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
        tvAutoDuration.setText(selectedDuration);
        chkSms.setChecked(isSmsSubscribed);
        chkSms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isSmsSubscribed = isChecked;
            fetchAllPlansAndCoupons(selectedDuration);
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
            fetchAllPlansAndCoupons(tvAutoDuration.getHint().toString());
        });
//        tvAutoDuration.setText(selectedDuration);
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

        private Context context;
        public PlanModel selectedPlan;

        public GridAdapter(List list, Context context) {
            super(list);
            this.context = context;
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
            if (isPlanType_CCM) {
                holder.tvText2.setText(MyUtils.formatCurrency(model.getPrice()));
            } else {
                holder.tvText2.setText(model.getIndents()+" Indents in "+MyUtils.formatCurrency(model.getPrice()));
            }
            holder.tvText3.setText(model.getDuration_description());
            holder.tvAmount.setText(MyUtils.formatCurrency(model.getFinal_price()));
            if (selectedPlan != null && selectedPlan.getId().equals(model.getId())) {
                holder.rellayContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.red_lightest));
            } else {
                holder.rellayContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.green_lightest));
            }
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
        @BindView(R.id.rellayContainer)
        RelativeLayout rellayContainer;

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

    static class PlanType {
        String name;
        String value;
    }

    /*private void fetchPlanTerms() {
        if (!MyUtils.hasInternetConnection(this)) {
            return;
        }

        APIClient.getApiService().fetchPlanTypes(authkey).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        Type token = new TypeToken<List<PlanType>>(){}.getType();
                        List<PlanType> tempList = gson.fromJson(jsonObject.getJSONArray("data").toString(), token);
                        planTypes.clear();
                        planTypes.addAll(tempList);

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
    }*/

    private void fetchPurchasedPlans() {
        if (!MyUtils.hasInternetConnection(this)) {
            return;
        }

        progress.show();

        APIClient.getApiService().getPurchasedPlans(authkey).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject res = new JSONObject(response.body().string());
                        JSONObject data = res.getJSONObject("data");
                        JSONArray ccm = data.getJSONArray("ccm");
                        JSONArray ecm = data.getJSONArray("e_ccm");

                        ccmPlans.clear();
                        for (int i = 0; i < ccm.length(); i++) {
                            PurchasedPlan m = gson.fromJson(ccm.getJSONObject(i).toString(), PurchasedPlan.class);
                            m.subscriptionDetail = gson.fromJson(m.subscription_detail, PurchasedPlan.SubscriptionDetail.class);
                            ccmPlans.add(m);
                        }
                        ecmPlans.clear();
                        for (int i = 0; i < ecm.length(); i++) {
                            PurchasedPlan m = gson.fromJson(ecm.getJSONObject(i).toString(), PurchasedPlan.class);
                            m.subscriptionDetail = gson.fromJson(m.subscription_detail, PurchasedPlan.SubscriptionDetail.class);
                            ecmPlans.add(m);
                        }
                        purchasedPlans.clear();
                        purchasedPlans.addAll(ccmPlans);
                        purchasedPlansAdapter.refresh();
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

    public void activatePlan(int finalPosition, PurchasedPlan model) {
        if (!MyUtils.hasInternetConnection(this)) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        progress.show();

        APIClient.getApiService().activatePlan(authkey, model.id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        MLog.showLongToast(getApplicationContext(), "Plan activated successfully.");
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray array = jsonObject.getJSONArray("data");
                        purchasedPlans.clear();
                        for (int i = 0; i < array.length(); i++) {
                            PurchasedPlan m = gson.fromJson(array.getJSONObject(i).toString(), PurchasedPlan.class);
                            m.subscriptionDetail = gson.fromJson(m.subscription_detail, PurchasedPlan.SubscriptionDetail.class);
                            purchasedPlans.add(m);
                        }
                        purchasedPlansAdapter.refresh();
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
}
