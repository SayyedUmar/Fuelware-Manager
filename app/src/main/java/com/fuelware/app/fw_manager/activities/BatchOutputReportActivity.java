package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

public class BatchOutputReportActivity extends SuperActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refresh_layout)
    RecyclerView refreshLayout;

    BatchReportAdapter adapter;
    private List<BatchReport> list = new ArrayList<>();
    private AlertDialog progress;
    private String authkey;
    private Gson gson;


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
        setEventListeners();
        setupRecycler();
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

    }

    private void fetchAllBatchReports() {
        if (!MyUtils.hasInternetConnection(getApplicationContext())) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        progress.show();

        APIClient.getApiService().getBatchReport(authkey).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject result = new JSONObject(response.body().string());
                        JSONArray array = result.getJSONArray("data");
                        Type token2 = new TypeToken<List<BatchReport>>(){}.getType();
                        List<BatchReport> tempList = gson.fromJson(array.toString(), token2);
                        list.clear();
                        list.addAll(tempList);
                        adapter.refresh();
                    } else {
                        JSONObject errorObj = new JSONObject(response.errorBody().string());
                        if (errorObj.has("success") && errorObj.has("message") && !errorObj.getBoolean("success"))
                            MLog.showToast(getApplicationContext(),  errorObj.getString("message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progress.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progress.dismiss();
                MLog.showToast(getApplicationContext(), t.getMessage());
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

    /*public class AutoLoadEventDetector extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView view, int dx, int dy) {
            RecyclerView.LayoutManager manager = view.getLayoutManager();
            if (manager.getChildCount() > 0) {
                int count = manager.getItemCount();
                int last = ((RecyclerView.LayoutParams) manager
                        .getChildAt(manager.getChildCount() - 1).getLayoutParams()).getViewAdapterPosition();

                if (last == count - 1 && !mIsLoading && mInteractionListener != null) {
                    requestMore();
                }
            }
        }
    }*/

    /*public abstract class InteractionListener {
        public void requestRefresh() {
            requestComplete();

            if (mOriginAdapter.isEmpty()) {
                //mTipsHelper.showEmpty();
            } else if (hasMore()) {
                //mTipsHelper.showHasMore();
            } else {
                //mTipsHelper.hideHasMore();
            }
        }

        public void requestMore() {
            requestComplete();
        }

        public void requestFailure() {
            requestComplete();
            //mTipsHelper.showError(isFirstPage(), new Exception("net error"));
        }

        protected void requestComplete() {
            mIsLoading = false;

            if (refreshLayout != null) {
                refreshLayout.setRefreshing(false);
            }
        }

        protected boolean hasMore() {
            return mOriginAdapter.getItem(mOriginAdapter.getItemCount() - 1).hasMore();
        }
    }*/

}
