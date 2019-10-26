package com.fuelware.app.fw_manager.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.fuelware.app.fw_manager.BuildConfig;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.activities.indents.BIndentListActivity;
import com.fuelware.app.fw_manager.activities.morning_params.MorningParamsActivity;
import com.fuelware.app.fw_manager.activities.plans.PlansActivity;
import com.fuelware.app.fw_manager.activities.receipts.ReceiptsActivity;
import com.fuelware.app.fw_manager.activities.reports.ReportsActivity;
import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.appconst.Const;
import com.fuelware.app.fw_manager.models.Cashier;
import com.fuelware.app.fw_manager.models.ProductPriceModel;
import com.fuelware.app.fw_manager.models.User;
import com.fuelware.app.fw_manager.models.VersionCheckModel;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.FuelwareAPI;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.receiver.AlarmReceiver;
import com.fuelware.app.fw_manager.utils.MyPreferences;
import com.fuelware.app.fw_manager.utils.MyUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends SuperActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView tvShiftID, tvLoginTime;
    private TextView tvFuelPrice, tvActiveBatches;
    private Button btnShiftClose;
    private String authkey;
    private AlertDialog progressDialog;
    private Gson gson;
    private User user;
    private List<ProductPriceModel> prodPriceList = new ArrayList<>();
    private List<Cashier> cashierList = new ArrayList<>();
    private ImageView imgReceipts, imgCounterBilling;

    TextView tvUsername, tvAccountVersion, tvAppVersion;


    @BindView(R.id.imgMorningParams)
    ImageView imgMorningParams;
    @BindView(R.id.imgReports)
    ImageView imgReports;
    @BindView(R.id.linlayCounterBilling)
    LinearLayout linlayCounterBilling;
    @BindView(R.id.linlayReceipts)
    LinearLayout linlayReceipts;

    @BindView(R.id.imgBindent)
    ImageView imgBindent;
    @BindView(R.id.tvBindentCount)
    TextView tvBindentCount;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setupBackNavigation(toolbar);

        findViewById();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
         navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        tvUsername = navigationView.getHeaderView(0).findViewById(R.id.tvUsername);
        tvAccountVersion = navigationView.getHeaderView(0).findViewById(R.id.tvAccountVersion);
        tvAppVersion = navigationView.getHeaderView(0).findViewById(R.id.tvAppVersion);

        init();

        displayVersionInfo();
        setEventListeners();
        fetchProducts();
//        fetchCashiers();
        fetchMorningPrice();

        if(BuildConfig.DEBUG) {
            navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
        }
        navigationView.getMenu().findItem(R.id.nav_tech_support).setVisible(false);
        if (MyPreferences.getBoolValue(this, AppConst.SMS_READ_PERMISSION_ALLOWED)) {
            navigationView.getMenu().findItem(R.id.nav_sms_read).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_dollar));
        } else {
            navigationView.getMenu().findItem(R.id.nav_sms_read).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_dollar_off));
        }

//        if (!BuildConfig.DEBUG) {
//            linlayReceipts.setVisibility(View.GONE);
//            linlayCounterBilling.setVisibility(View.GONE);
//        }
//        tvActiveBatches.setVisibility(View.GONE);
    }


    private void displayVersionInfo() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            tvAppVersion.setText("Version   v" + version + " - " + pInfo.versionCode);
        } catch (Exception e) { e.printStackTrace(); }
    }


    @Override
    protected void onResume() {
        super.onResume();
        getuserProfile();
        fetchCashiers();
        checkForNewVersion();
        if (MyPreferences.getBoolValue(getApplicationContext(), Const.PLAN_EXPIRED)) {
            showPlanExpirePopup();
        }
    }

    private void fetchMorningPrice() {
        if (!MyUtils.hasInternetConnection(this)) {
            MLog.showToast(this, AppConst.NO_INTERNET_MSG);
            return;
        }

        Call<ResponseBody> call_getProducts;
        call_getProducts = APIClient.getApiService().getManagerOutletProducts(authkey);
        call_getProducts.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray dataArray = jsonObject.getJSONArray("data");
                        Type token = new TypeToken<List<ProductPriceModel>>(){}.getType();
                        List<ProductPriceModel> tempList = gson.fromJson(dataArray.toString(), token);
                        if (tempList.get(0).getPrice() == 0) {
                            MyPreferences.setBoolValue(getApplicationContext(),AppConst.IS_MORNING_PARAMETERS_UPDATED,false);
                            showMorningPriceUpdatePopup();
                            setMorningParamAlarm();
                        } else {
                            MyPreferences.setBoolValue(getApplicationContext(),AppConst.IS_MORNING_PARAMETERS_UPDATED,true);
                        }
                    } else {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        if (jsonObject.has("message"))
                            MLog.showLongToast(getApplicationContext(), jsonObject.getString("message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                MLog.showLongToast(getApplicationContext(), t.getMessage());
            }
        });
    }

    private void setMorningParamAlarm() {
        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getBaseContext(), Const.ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, 0);
        calendar.add(Calendar.SECOND, 0);
        calendar.add(Calendar.HOUR_OF_DAY, 6);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                calendar.getTimeInMillis(), //+ AlarmManager.INTERVAL_HALF_HOUR,
                Const.ALARM_HOUR*24, pendingIntent);

    }

    private void showMorningPriceUpdatePopup() {

    }

    private void setEventListeners() {

        imgBindent.setOnClickListener(v -> {
            startActivity(new Intent(this, BIndentListActivity.class).putExtra(Const.B_INDENT, true));
        });

        imgMorningParams.setOnClickListener(v -> {
            startActivity(new Intent(this, MorningParamsActivity.class));
        });

        imgReports.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportsActivity.class));
        });

        tvFuelPrice.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this, tvFuelPrice);
            for (ProductPriceModel m: prodPriceList) {
                String text = m.getProduct() + "  >  <font color=#008577>" + MyUtils.formatCurrency(m.getPrice())+"</font>";
                popupMenu.getMenu().add(Html.fromHtml(text));
            }
            popupMenu.show();
        });

        tvActiveBatches.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this, tvActiveBatches);
            int i = 0;
            for (Cashier m: cashierList) {
                long count = 0;
                if (m.getManual_indent() != null && m.getManual_indent().size() > 0) {
                    count = m.getManual_indent().get(0).getTotal();
                }
                String text = "Batch "+(i+1)+ "  >  <font color=#008577>"+m.getBatch_number()+"</font> -<font color=#D81B60> "+count+"</font>";
                popupMenu.getMenu().add(Menu.NONE, i++, Menu.NONE, Html.fromHtml(text));
            }
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(item -> {
                MyPreferences.setStringValue(getApplicationContext(), AppConst.CASHIER_DETAILS, gson.toJson(cashierList.get(item.getItemId())));
                startActivity(new Intent(MainActivity.this, CashierDashboardActivity.class)
                        .putExtra("CASHIER", cashierList.get(item.getItemId())));
                return true;
            });
        });

        imgCounterBilling.setOnClickListener(v -> {
            if (MyPreferences.getBoolValue(this, AppConst.LOGIN_STATUS)) {
                if(tvAccountVersion.getText().toString().equalsIgnoreCase("ccm")) {
                    MLog.showFancyToast(this, "To access this feature, Buy Premium Version", FancyToast.WARNING);
                } else {
                    startActivity(new Intent(MainActivity.this, CounterBillActivity.class));
                }
            } else {
                MLog.showToast(getApplicationContext(), "Please login to the batch first.");
            }
        });

        imgReceipts.setOnClickListener(v -> {
            if (MyPreferences.getBoolValue(this, AppConst.LOGIN_STATUS)) {
                startActivity(new Intent(MainActivity.this, ReceiptsActivity.class));
            } else {
                MLog.showToast(getApplicationContext(), "Please login to the batch first.");
            }
        });

        btnShiftClose.setOnClickListener(v -> callShiftCloseAPI());

    }

    private void callShiftCloseAPI() {
        if (!MyUtils.hasInternetConnection(MainActivity.this)) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }
        progressDialog.show();
        APIClient.getApiService().closeShift(authkey).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        logoutUser();
                    } else {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        MLog.showToast(getApplicationContext(),jObjError.getString("message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                MLog.showLongToast(getApplicationContext(), t.getMessage());
            }
        });
    }


    private void logoutUser() {
        MyPreferences.setStringValue(getApplicationContext(), Const.AUTHKEY, "");
        startActivity(new Intent(MainActivity.this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        );
        finish();
    }


    private void init() {
        authkey = MyPreferences.getStringValue(this, Const.AUTHKEY);
        //String userString = MyPreferences.getStringValue(this, AppConst.USER_PROFILE_DATA);
        gson = new Gson();
        /*if (userString.length() > 50) {
            user = gson.fromJson(userString, User.class);
        }*/
        progressDialog = new SpotsDialog(MainActivity.this, R.style.Custom);

    }

    private void getuserProfile() {
        if (!MyUtils.hasInternetConnection(MainActivity.this)) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        progressDialog.show();

//        MLog.e("authkey", authkey);

        Call<ResponseBody> responce = APIClient.getApiService().getUserDetails(authkey);
        responce.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONObject data = jsonObject.getJSONObject("data");
                        MyPreferences.setStringValue(MainActivity.this, AppConst.USER_PROFILE_DATA, data.toString());
                        user = gson.fromJson(data.toString(), User.class);
                        boolean payment_status = data.getBoolean("payment_status");
                        String notification_token = data.getString("notification_token");
                        try {
                            String outletName = data.getJSONObject("outlet").getJSONArray("data").getJSONObject(0).getString("name");
                            setTitle(outletName.toUpperCase());
                            tvBindentCount.setText(data.getString("bindent_count"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        MyPreferences.setStringValue(MainActivity.this, Const.NOTIFICATION_TOKEN, notification_token);

                        try {
                            tvUsername.setText(data.getString("first_name")+" - "+data.getString("formatted_role"));
                            JSONArray array = data.getJSONObject("outlet").getJSONArray("data");
                            if (array.length() > 0) {
                                String subscription_type = array.getJSONObject(0).getString("subscription_type");
                                MyPreferences.setStringValue(getApplicationContext(), Const.SUBSCRIPTION_TYPE, subscription_type);
                                if (subscription_type.equals("free")) {
//                                    linlayCounterBilling.setVisibility(View.GONE);
                                    ImageViewCompat.setImageTintList(imgCounterBilling, ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.darker_gray)));
                                    tvAccountVersion.setText("CCM");
                                } else if (subscription_type.toLowerCase().contains("premium")) {
                                    tvAccountVersion.setText("CCM Premium");
                                }
                            }

                        } catch (Exception e) {e.printStackTrace();}

                        if (!payment_status) {
                            MyPreferences.setBoolValue(getApplicationContext(), Const.PLAN_EXPIRED, true);
                            showPlanExpirePopup();
                        } else {
                            MyPreferences.setBoolValue(getApplicationContext(), Const.PLAN_EXPIRED, false);
                        }
                        if (data.has("last_shift")) {
                            JSONObject last_shift = data.getJSONObject("last_shift");
                            tvShiftID.setText(last_shift.getString("shift_number"));
                            String start_time = last_shift.getString("login_time");
                            tvLoginTime.setText(MyUtils.dateToString(AppConst.SERVER_DATE_TIME_FORMAT, AppConst.APP_DATE_TIME_FORMAT, start_time));
                        }

                    } else {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(getApplicationContext(),jObjError.getString("message"),Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                MLog.showLongToast(getApplicationContext(), t.getMessage());
            }
        });
    }

    private void showPlanExpirePopup() {

        DialogInterface.OnClickListener listener = (d, w) -> {
            if (w == -1) { // purchase
                startActivity(new Intent(this, PlansActivity.class));
            } else { // logout
                logoutUser();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Plan Expired")
                .setMessage("You don't have a plan, Please purchase new plan to continue.")
                .setPositiveButton("Purchase Plan", listener)
                .setNegativeButton("Logout", listener)
                .setCancelable(false);

        builder.show();
    }

    private void findViewById() {
        tvShiftID = findViewById(R.id.tvShiftID);
        tvLoginTime = findViewById(R.id.tvLoginTime);
        tvFuelPrice = findViewById(R.id.tvFuelPrice);
        tvActiveBatches = findViewById(R.id.tvActiveBatches);
        btnShiftClose = findViewById(R.id.btnShiftClose);
        imgCounterBilling = findViewById(R.id.imgCounterBilling);
        imgReceipts = findViewById(R.id.imgReceipts);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        } else if (id == R.id.nav_slideshow) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_tools) {
            startActivity(new Intent(this, PlansActivity.class));
        } else if (id == R.id.nav_tech_support) {
            startActivity(new Intent(this, TechSupportActivity.class));
        } else if (id == R.id.nav_logout) {
            showLogoutDialog();
        } else if (id == R.id.nav_Shift_close) {
            callShiftCloseAPI();
        } else if (id == R.id.nav_knowledgeCenter) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Const.KNOWLEDGE_CENTER_URL_PROD)));
        } else if (id == R.id.nav_sms_read) {
            AndPermission.with(this)
                    .runtime()
                    .permission(Permission.READ_SMS, Permission.RECEIVE_SMS)
                    .onGranted(permissions -> {
                        MyPreferences.setBoolValue(this, AppConst.SMS_READ_PERMISSION_ALLOWED, true);
                        navigationView.getMenu().findItem(R.id.nav_sms_read).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_dollar));
                    })
                    .onDenied(permissions -> {
                        MLog.showToast(getApplicationContext(), "Please provide this permission to enable Morning Price Auto Update.");
                    })
                    .start();

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_logout_confirmation);
        dialog.show();
        dialog.setCancelable(true);
        TextView tvYes = dialog.findViewById(R.id.btnYes);
        TextView tvNo = dialog.findViewById(R.id.btnNo);
        tvNo.setOnClickListener(v -> dialog.dismiss());
        tvYes.setOnClickListener(v -> {
            dialog.dismiss();
            logoutUser();
        });
    }



    private void fetchProducts() {

        if (!MyUtils.hasInternetConnection(MainActivity.this)) {
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
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray dataArray = jsonObject.getJSONArray("data");
                        Type token = new TypeToken<List<ProductPriceModel>>(){}.getType();
                        List<ProductPriceModel> tempList = gson.fromJson(dataArray.toString(), token);
                        prodPriceList.clear();
                        prodPriceList.addAll(tempList);
                        MyPreferences.setStringValue(MainActivity.this, AppConst.PRODUCTS_LIST, dataArray.toString());
                        //recyclerView.getAdapter().notifyDataSetChanged();
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

    private void fetchCashiers() {
        if (!MyUtils.hasInternetConnection(MainActivity.this)) {
            return;
        }

        progressDialog.show();

//        MLog.e("authkey", authkey);

        Call<ResponseBody> call_getProducts;
        call_getProducts = APIClient.getApiService().getCashiers(authkey);
        call_getProducts.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray dataArray = jsonObject.getJSONArray("data");
                        Type token = new TypeToken<List<Cashier>>(){}.getType();
                        List<Cashier> tempList = gson.fromJson(dataArray.toString(), token);
                        cashierList.clear();
                        cashierList.addAll(tempList);
                        if (dataArray.length() == 0) {
                            tvActiveBatches.setVisibility(View.GONE);
                        } else{
                            tvActiveBatches.setVisibility(View.VISIBLE);
                        }
                        //recyclerView.getAdapter().notifyDataSetChanged();
                    } else {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        if (jsonObject.has("message")) {
                            String msg = jsonObject.getString("message");
                            MLog.showLongToast(getApplicationContext(), msg);
                            if (msg.toLowerCase().contains("unauthenticated") || msg.toLowerCase().contains("manager shift closed")) {
                                logoutUser();
                            }
                        }
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

    private void checkForNewVersion() {
        if (!MyUtils.hasInternetConnection(MainActivity.this)) {
            return;
        }

        Retrofit adapter = new Retrofit.Builder()
                .baseUrl(AppConst.ROOT_URL+"common/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        FuelwareAPI fuelwareAPI = adapter.create(FuelwareAPI.class);
        Call<ResponseBody> callable;

        callable = fuelwareAPI.checkForNewVersion (authkey, new VersionCheckModel());
        callable.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject res = new JSONObject(response.body().string());
                        boolean success = res.getBoolean("success");

                        if (success) {
                            JSONObject dataObject = res.getJSONObject("data");
                            boolean is_new_version_available = dataObject.getBoolean("is_new_version_available");
                            if (is_new_version_available) {
                                JSONObject versionObject = dataObject.getJSONObject("version");
                                String version_name = versionObject.getString("version_name");
                                String store_url = versionObject.getString("store_url");
                                boolean isForceUpdateRequired = versionObject.getBoolean("force_update_required");

                                showUpdatePopup(isForceUpdateRequired, version_name, store_url);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        JSONObject errorObj = new JSONObject(response.errorBody().string());
                        if (errorObj.has("success") && errorObj.has("message") && !errorObj.getBoolean("success"))
                            Toast.makeText(MainActivity.this, errorObj.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("MainActivity", "onFailure");
            }
        });
    }

    private void showUpdatePopup(final boolean isForceUpdateRequired, String version_name, String store_url) {

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    final String appPackageName = getPackageName();
                    try {
//                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(store_url)));
                    } catch (Exception anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(store_url)));
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("A new version "+ version_name + " of Cashier app is availble on Google play. Kindly update.")
                .setPositiveButton("Update", dialogClickListener);
        if (!isForceUpdateRequired) {
            builder.setNegativeButton("Cancel", dialogClickListener);
        }
        builder.setCancelable(!isForceUpdateRequired);
        builder.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (item.getItemId() == R.id.action_refresh) {
            fetchProducts();
            fetchCashiers();
        }
        return super.onOptionsItemSelected(item);
    }
}
