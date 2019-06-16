package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.adapters.BatchReportAdapter;
import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.models.BatchReport;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.utils.MyPreferences;
import com.fuelware.app.fw_manager.utils.MyUtils;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

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

public class BatchOutputReportActivity extends SuperActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refresh_layout)
    SwipyRefreshLayout refreshLayout;

    BatchReportAdapter adapter;
    private List<BatchReport> list = new ArrayList<>();
    private AlertDialog progress;
    private String authkey;
    private Gson gson;
    private Pagination page = new Pagination();


//    private AutoLoadEventDetector mAutoLoadEventDetector;
//    private boolean mIsLoading;
//    private InteractionListener mInteractionListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_eindent_batch_report);
        ButterKnife.bind(this);

        setupBackNavigation(null);
        setTitle("Batch Report");

        initialise();
        setupRecycler();
        setEventListeners();
        fetchAllBatchReports();
    }

    private void setupRecycler() {
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new BatchReportAdapter(list, this);
        recyclerView.setAdapter(adapter);
        //recyclerView.addOnScrollListener(mAutoLoadEventDetector);
    }

    private void initialise() {
        gson = new Gson();
        progress = new SpotsDialog(this, R.style.Custom);
        authkey = MyPreferences.getStringValue(getApplicationContext(), "authkey");
    }

    private void setEventListeners() {
        refreshLayout.setOnRefreshListener(direction -> {
            Log.d("MainActivity", "Refresh triggered at "
                    + (direction == SwipyRefreshLayoutDirection.TOP ? "top" : "bottom"));

            if (direction == SwipyRefreshLayoutDirection.BOTTOM) {
                if (page != null && page.currentPage < page.totalPages) {
                    fetchAllBatchReports();
                } else {
                    refreshLayout.setRefreshing(false);
                    MLog.showToast(getApplicationContext(), "No new data to display.");
                }
            }
        });
    }

    private void fetchAllBatchReports() {
        if (!MyUtils.hasInternetConnection(getApplicationContext())) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        progress.show();
        long pageNumber = page != null ? page.currentPage+1 : 0;
        APIClient.getApiService().getBatchReport(authkey, pageNumber).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject result = new JSONObject(response.body().string());
                        JSONArray array = result.getJSONArray("data");
                        JSONObject pagination = result.getJSONObject("meta").getJSONObject("pagination");
                        page = gson.fromJson(pagination.toString(), Pagination.class);
                        Type token2 = new TypeToken<List<BatchReport>>(){}.getType();
                        List<BatchReport> tempList = gson.fromJson(array.toString(), token2);
                        list.addAll(tempList);
                        adapter.refresh();
                    } else {
                        JSONObject errorObj = new JSONObject(response.errorBody().string());
                        if (errorObj.has("success") && errorObj.has("message") && !errorObj.getBoolean("success"))
                            MLog.showToast(getApplicationContext(),  errorObj.getString("message"));
                    }
                    refreshLayout.setRefreshing(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progress.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progress.dismiss();
                MLog.showToast(getApplicationContext(), t.getMessage());
                refreshLayout.setRefreshing(false);
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

    static class Pagination {
        @SerializedName("count")
        long offset = 30;
        @SerializedName("current_page")
        long currentPage = 0;
        @SerializedName("total_pages")
        long totalPages = 0;
    }

}
