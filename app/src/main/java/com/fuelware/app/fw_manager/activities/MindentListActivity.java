package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
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

import com.fuelware.app.fw_manager.Const.AppConst;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.adapters.MindentListAdapter;
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

public class MindentListActivity extends SuperActivity implements SearchView.OnQueryTextListener {

    AlertDialog progressDialog;
    private RecyclerView recyclerView;
    private List<IndentModel> list = new ArrayList<>();
    private MindentListAdapter adapter;
    private String authkey;
    private TextView tvNoRecordsFound;
    private Gson gson;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mindent_list);
        setTitle("M-Indent");
        setupBackNavigation(null);
        gson = new Gson();
        progressDialog = new SpotsDialog(MindentListActivity.this, R.style.Custom);
        authkey = MyPreferences.getStringValue(getApplicationContext(), "authkey");

        tvNoRecordsFound = findViewById(R.id.tvNoRecordsFound);
        recyclerView = findViewById(R.id.recycler_view_mindent);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        adapter = new MindentListAdapter(list, MindentListActivity.this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.activity_mindent);
        //fab.setOnClickListener(view -> startActivity(new Intent(MindentListActivity.this, AddMIndentActivity.class)));
        fetchData();
        setObserver();
    }

    private void setObserver() {
        compositeDisposable.add(RxBus.getInstance().toObservable().subscribe(new Consumer<Map<Object, Object>>() {
            @Override
            public void accept(Map<Object, Object> map) throws Exception {
                try {
                    if (map.containsKey(RxBus.ADD_ACTION)) {
                        Object object = map.get(RxBus.ADD_ACTION);
                        if (object instanceof IndentModel){
                            list.add(0, (IndentModel) object);
                        }
                    } else if (map.containsKey(RxBus.EDIT_ACTION)) {
                        Object object = map.get(RxBus.EDIT_ACTION);
                        if (object instanceof IndentModel) {
                            int positon = (int) map.get(RxBus.POSITION);
                            if (list.size() > positon)
                                list.set(positon, (IndentModel) object);
                        }
                    } else if (map.containsKey(RxBus.DELETE_ACTION)) {
                        int positon = (int) map.get(RxBus.DELETE_ACTION);
                        try {
                            list.remove(positon);
                        }catch (Exception e) {e.printStackTrace();}
                    }
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

        /*MyService.CallAPI2(APIClient.getApiService().getMindentList(authkey), new MyServiceListerner<ResponseClass<List<IndentModel>>>() {
            @Override
            public void onNext(ResponseClass<List<IndentModel>> response) {
                if (response.success) {
                    list.clear();
                    list.addAll(response.getData());
                }
            }

            @Override
            public void onComplete() {
                adapter.notifyDataSetChanged();
                progressDialog.dismiss();
                if (list.size() > 0) {
                    tvNoRecordsFound.setVisibility(View.GONE);
                } else {
                    tvNoRecordsFound.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Throwable e) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }
        });*/

        APIClient.getApiService().getMindentList(authkey).enqueue(new Callback<ResponseBody>() {
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

        SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("Enter vehicle no/indent no..");
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
}
