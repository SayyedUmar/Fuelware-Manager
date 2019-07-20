package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.models.Cashier;
import com.fuelware.app.fw_manager.models.ProductPriceModel;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.utils.MyPreferences;
import com.fuelware.app.fw_manager.utils.MyUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

public class CashierDashboardActivity extends SuperActivity {


    private Cashier cashier;
    private String authkey;
    private AlertDialog progressDialog;
    @BindView(R.id.tvBatchID)
    TextView tvBatchID;
    @BindView(R.id.tvLoginTime)
    TextView tvLoginTime;
    @BindView(R.id.tvMindentCount)
    TextView tvMindentCount;
    @BindView(R.id.tvFuelPrice)
    TextView tvFuelPrice;
    private List<ProductPriceModel> prodPriceList = new ArrayList<>();
    private Gson gson;

    @BindView(R.id.mindent_img)
    ImageView mIndentImage;
    @BindView(R.id.imgEindent)
    ImageView eIndentImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cashier_dashboard);
        ButterKnife.bind(this);
        setupBackNavigation(null);
        cashier = (Cashier) getIntent().getSerializableExtra("CASHIER");
        setTitle(cashier.getFirst_name()+" "+cashier.getLast_name());

        initialize();
        findViewById();
        showProducts();
        setEventListeners();
        getCashierDetail(cashier.getCashier_id());
    }


    private void findViewById() {
//        tvBatchID = findViewById(R.id.tvBatchID);
//        tvLoginTime = findViewById(R.id.tvLoginTime);
//        tvMindentCount = findViewById(R.id.tvMindentCount);
//        tvFuelPrice = findViewById(R.id.tvFuelPrice);
//        tvMindentCount = findViewById(R.id.tvMindentCount);
    }

    private void setEventListeners() {
        tvFuelPrice.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this, tvFuelPrice);
            for (ProductPriceModel m: prodPriceList) {
                String text = m.getProduct() + "  >  <font color=#008577>" + MyUtils.formatCurrency(m.getPrice())+"</font>";
                popupMenu.getMenu().add(Html.fromHtml(text));
            }
            popupMenu.show();
        });

        mIndentImage.setOnClickListener(v -> {
            startActivity(new Intent(this, MindentListActivity.class));
        });

        eIndentImage.setOnClickListener(v -> {
            startActivity(new Intent(this, EindentListActivity.class));
        });
    }

    private void initialize() {
        authkey = MyPreferences.getStringValue(this, "authkey");
        gson = new Gson();
        progressDialog = new SpotsDialog(this, R.style.Custom);
    }

    private void showProducts() {
        try {
            String products = MyPreferences.getStringValue(this, AppConst.PRODUCTS_LIST);
            Type token = new TypeToken<List<ProductPriceModel>>(){}.getType();
            prodPriceList.addAll(gson.fromJson(products, token));
        } catch (Exception e) { e.printStackTrace(); }

    }

    private void getCashierDetail(long cashierID) {
        Call<ResponseBody> req = APIClient.getApiService().getCashierDetail(authkey, cashierID+"");
        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONObject data = jsonObject.getJSONObject("data");
                        JSONObject batch = data.getJSONObject("batch");
                        tvBatchID.setText(batch.getString("batch_number"));
                        String start_time = batch.getString("start_time");
                        tvLoginTime.setText(MyUtils.dateToString(AppConst.SERVER_DATE_TIME_FORMAT, AppConst.APP_DATE_TIME_FORMAT, start_time));

                        if (data.has("pending")) {
                            tvMindentCount.setText(data.getJSONObject("pending").getString("manual_indent"));
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
