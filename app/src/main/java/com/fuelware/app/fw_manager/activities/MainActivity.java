package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.fuelware.app.fw_manager.Const.AppConst;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.models.Cashier;
import com.fuelware.app.fw_manager.models.ProductPriceModel;
import com.fuelware.app.fw_manager.models.User;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.utils.MyPreferences;
import com.fuelware.app.fw_manager.utils.MyUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

public class MainActivity extends SuperActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView tvShiftID, tvLoginTime;
    private TextView tvFuelPrice, tvActiveBatches;
    private Button btnLogin;
    private String authkey;
    private AlertDialog progressDialog;
    private Gson gson;
    private User user;
    private List<ProductPriceModel> prodPriceList = new ArrayList<>();
    private List<Cashier> cashierList = new ArrayList<>();
    private ImageView imgReceipts, imgCounterBilling;

    @BindView(R.id.imgMorningParams)
    ImageView imgMorningParams;
    @BindView(R.id.imgReports)
    ImageView imgReports;

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
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        init();

        setEventListeners();
        getuserProfile();
        fetchProducts();
        fetchCashiers();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setEventListeners() {

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
                popupMenu.getMenu().add(Menu.NONE, Menu.NONE, i,Html.fromHtml(text));
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
//                if (MyPreferences.getBoolValue(MainActivity.this, AppConst.MORNING_PARAMETERS_STATUS)) {
                    startActivity(new Intent(MainActivity.this, CounterBillActivity.class));
//                } else {
                    //showMorningParamsUdpateDialog(null);
//                }
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

        btnLogin.setOnClickListener(v -> {
            MyPreferences.setStringValue(getApplicationContext(), "authkey", "");
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void init() {
        String userString = MyPreferences.getStringValue(this, AppConst.USER_PROFILE_DATA);
        gson = new Gson();
        /*if (userString.length() > 50) {
            user = gson.fromJson(userString, User.class);
        }*/
        progressDialog = new SpotsDialog(MainActivity.this, R.style.Custom);
        authkey = MyPreferences.getStringValue(getApplicationContext(), "authkey");
    }

    private void getuserProfile() {
        if (!MyUtils.hasInternetConnection(MainActivity.this)) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        progressDialog.show();

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

    private void findViewById() {
        tvShiftID = findViewById(R.id.tvShiftID);
        tvLoginTime = findViewById(R.id.tvLoginTime);
        tvFuelPrice = findViewById(R.id.tvFuelPrice);
        tvActiveBatches = findViewById(R.id.tvActiveBatches);
        btnLogin = findViewById(R.id.btnLogin);
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

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
