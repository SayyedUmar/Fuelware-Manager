package com.fuelware.app.fw_manager.activities.indents;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.adapters.BIndentListAdapter;
import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.appconst.Const;
import com.fuelware.app.fw_manager.models.IndentModel;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.network.RxBus;
import com.fuelware.app.fw_manager.utils.MyPreferences;
import com.fuelware.app.fw_manager.utils.MyUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BIndentListActivity extends SuperActivity implements SearchView.OnQueryTextListener {

    AlertDialog progressDialog;
    private RecyclerView recyclerView;
    private List<IndentModel> list = new ArrayList<>();
    private BIndentListAdapter adapter;
    private String authkey;
    private TextView tvNoRecordsFound;
    private Gson gson;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private FloatingActionButton fab;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mindent_list);
        setTitle("B-Indent");
        setupBackNavigation(null);

        gson = new Gson();
        progressDialog = new SpotsDialog(BIndentListActivity.this, R.style.Custom);
        authkey = MyPreferences.getStringValue(getApplicationContext(), "authkey");

        fab = findViewById(R.id.fab);
        tvNoRecordsFound = findViewById(R.id.tvNoRecordsFound);
        recyclerView = findViewById(R.id.recycler_view_mindent);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        adapter = new BIndentListAdapter(list, BIndentListActivity.this);
        recyclerView.setAdapter(adapter);


        fetchData();
        setObserver();

        fab.setOnClickListener(view -> startActivity(new Intent(BIndentListActivity.this, AddBIndentActivity.class)));
        if (getIntent().getBooleanExtra(Const.B_INDENT, false)) {
            fab.setVisibility(View.VISIBLE);
        }
    }

    private void setObserver() {
        compositeDisposable.add(RxBus.getInstance().toObservable().subscribe(new Consumer<Map<Object, Object>>() {
            @Override
            public void accept(Map<Object, Object> map) throws Exception {
                try {
                    if (map.containsKey(RxBus.ADD_ACTION)) {
                        Object object = map.get(RxBus.ADD_ACTION);
                        if (object instanceof IndentModel) {
                            list.add(0, (IndentModel) object);
                        }
                    } else if (map.containsKey(RxBus.EDIT_ACTION)) {
                        Object object = map.get(RxBus.EDIT_ACTION);
                        if (object instanceof IndentModel) {
                            int positon = (int) map.get(RxBus.POSITION);
                            if (list.size() > positon)
                                list.set(positon, (IndentModel) object);
                        }
                    } /*else if (map.containsKey(RxBus.DELETE_ACTION)) {
                            int positon = (int) map.get(RxBus.DELETE_ACTION);
                            try {
                                list.remove(positon);
                            }catch (Exception e) {e.printStackTrace();}
                        }*/
                    adapter.refresh();
                    showNoRecordsFound();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }));
    }


    private void showNoRecordsFound() {
        if (list.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            tvNoRecordsFound.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvNoRecordsFound.setVisibility(View.GONE);
        }
    }


    private void fetchData() {

        if (!MyUtils.hasInternetConnection(getApplicationContext())) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        progressDialog.show();

        APIClient.getApiService().getBindentList(authkey).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject result = new JSONObject(response.body().string());
                        JSONArray jsonArray = result.getJSONArray("data");
                        Type token = new TypeToken<List<IndentModel>>(){}.getType();
                        List<IndentModel> tempList = gson.fromJson(jsonArray.toString(), token);
                        list.clear();
                        list.addAll(tempList);
                        adapter.refresh();
                        showNoRecordsFound();
                    } else {
                        try {
                            JSONObject errorObj = new JSONObject(response.errorBody().string());
                            if (errorObj.has("success") && errorObj.has("message") && !errorObj.getBoolean("success"))
                                Toast.makeText(getApplicationContext(), errorObj.getString("message"), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
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
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);

        SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("Enter Cashier Name");
            searchView.setOnQueryTextListener(this);
            searchView.setIconified(false);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
            ((EditText)searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setTextColor(Color.WHITE);
            ((EditText)searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setHintTextColor(Color.WHITE);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filter(newText);
        return true;
    }


    public void deleteMIndent(IndentModel indentModel, int index, String otp) {
        if (!MyUtils.hasInternetConnection(this)) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        progressDialog.show();

        Call<ResponseBody> responce = APIClient.getApiService().deleteBIndent(authkey, indentModel.getId()+"");
        responce.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        MLog.showToast(BIndentListActivity.this, jsonObject.getString("message"));
                        list.remove(index);
                        adapter.refresh();
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
}