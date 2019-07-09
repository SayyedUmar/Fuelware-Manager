package com.fuelware.app.fw_manager.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.adapters.AccounStatementAdapter;
import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.models.AccountModel;
import com.fuelware.app.fw_manager.network.APIClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchTransactionsActivity extends SuperActivity {

    private RecyclerView recyclerView;
    private TextView transactions_details, debit_details, credit_details, balance_details;
    private List<AccountModel> records;
    AccounStatementAdapter adapter;
    EditText search_transactions_input;
    ImageView searchsubit_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_transactions);
        // Hide the keyboard.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Toolbar toolbar = findViewById(R.id.toolbar_searchalert);
        setupBackNavigation(toolbar);

        // backbutton = (ImageView) findViewById(R.id.back_button_searchalert);
        searchsubit_button = findViewById(R.id.conform_notifsearch_icon);
        search_transactions_input = findViewById(R.id.search_alert_et_toolbar);
        recyclerView = findViewById(R.id.recycler_view_transactions);

        transactions_details = findViewById(R.id.trasactions_details);
        debit_details = findViewById(R.id.debit_details);
        credit_details = findViewById(R.id.credit_details);
        balance_details = findViewById(R.id.balance_details);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        prepareTransactionData(data -> {
            records = data;
            adapter = new AccounStatementAdapter(records, SearchTransactionsActivity.this);
            recyclerView.setAdapter(adapter);
        });

        search_transactions_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.getFilter().filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

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

    public interface ResultCallback{
        void onResult(List<AccountModel> data);
    }

    private void prepareTransactionData(final ResultCallback callback) {

        SharedPreferences prfs = getSharedPreferences("userdetails", Context.MODE_PRIVATE);
        String outletid = prfs.getString("outletid", "");
        String authkey = prfs.getString("authkey", "");

        Call<ResponseBody> responce_outlets = APIClient.getApiService().fetchTransactions(authkey, "", "",
                true, true, "");
        responce_outlets.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    try {
                        Gson gson = new Gson();
                        JSONObject res = new JSONObject(response.body().string());
                        JSONObject dataObj = res.getJSONObject("data");
                        JSONArray jsonArray = dataObj.getJSONArray("credit_customer");
                        Type token = new TypeToken<List<AccountModel>>(){}.getType();
                        List<AccountModel> tempList = gson.fromJson(jsonArray.toString(), token);
                        callback.onResult(tempList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject errorObj = new JSONObject(response.errorBody().string());
                        if (errorObj.has("success") && errorObj.has("message") && !errorObj.getBoolean("success"))
                            Toast.makeText(SearchTransactionsActivity.this, errorObj.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), AppConst.ERROR_MSG, Toast.LENGTH_LONG).show();
            }
        });
    }

}
