package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.adapters.AccounStatementAdapter;
import com.fuelware.app.fw_manager.adapters.SpinnerAdapter;
import com.fuelware.app.fw_manager.models.AccountModel;
import com.fuelware.app.fw_manager.models.CreditCustomer;
import com.fuelware.app.fw_manager.models.TransactionTypeEnum;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.services.MyService;
import com.fuelware.app.fw_manager.services.MyServiceListerner;
import com.fuelware.app.fw_manager.utils.MyPreferences;
import com.fuelware.app.fw_manager.utils.MyUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.dialogplus.DialogPlus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.slybeaver.slycalendarview.SlyCalendarDialog;

public class AccountStatementActivity extends SuperActivity implements SlyCalendarDialog.Callback {


    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.tvNoRecordsFound)
    TextView tvNoRecordsFound;
    @BindView(R.id.tvCreditCustomer)
    TextView tvCreditCustomer;

    private AlertDialog progress;
    private Gson gson;
    private String authkey;
    private AccounStatementAdapter adapter;
    private SlyCalendarDialog dateDiaog;

    private String firstDateString = "";
    private String secondDateString = "";
    private TransactionTypeEnum typeEnum = TransactionTypeEnum.All;
    private Calendar firstDate, secondDate;
    private List<AccountModel> records = new ArrayList<>();
    private List<CreditCustomer> creditCustomerList = new ArrayList<>();
    private CreditCustomer selectedCustomer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account_statement);
        ButterKnife.bind(this);

        setupBackNavigation(null);

        initialise();
        setupRecyclerView();
        setEventListeners();
        fetchTransactions();
        fetchCreditCustomers();
    }

    private void fetchCreditCustomers() {
        if (!MyUtils.hasInternetConnection(getApplicationContext())) {
            return;
        }

        progress.show();

        MyService.CallAPI(APIClient.getApiService().getCreditCustomerList(authkey), new MyServiceListerner<Response<ResponseBody>>() {
            @Override
            public void onNext(Response<ResponseBody> response) {

                try {
                    JSONObject jsonObject;
                    if (response.isSuccessful()) {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        Type type = new TypeToken<List<CreditCustomer>>() {}.getType();
                        creditCustomerList.clear();
                        creditCustomerList.addAll(gson.fromJson(jsonArray.toString(), type));
                    } else {
                        jsonObject = new JSONObject(response.errorBody().string());
                        if (jsonObject.has("message"))
                            MLog.showLongToast(getApplicationContext(), jsonObject.getString("message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable e) {
                progress.hide();
                MLog.showLongToast(getApplicationContext(), e.getMessage());
            }

            @Override
            public void onComplete() {
                progress.hide();
            }

            @Override
            public void onSubscribe(Disposable d) {
                //compositeDisposable.add(d);
            }
        });

    }

    private void fetchTransactions() {
        if (!MyUtils.hasInternetConnection(this)) {
            MLog.showToast(this, AppConst.NO_INTERNET_MSG);
            return;
        }


        boolean credit = true;
        boolean debit = true;
        switch (typeEnum) {
            case All:
                break;
            case Credit:
                credit = true;
                debit = false;
                break;
            case Debit:
                debit = true;
                credit = false;
                break;
        }

        String customerID = selectedCustomer != null ? selectedCustomer.getId() : "";
        Call<ResponseBody> responce_outlets = APIClient.getApiService().getAccountStatementNew(authkey, firstDateString, secondDateString,
                credit, debit, customerID);
        responce_outlets.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject res = new JSONObject(response.body().string());
                        JSONObject dataObj = res.getJSONObject("data");
                        JSONArray jsonArray = dataObj.getJSONArray("credit_customer");
                        Type type = new TypeToken<List<AccountModel>>() {}.getType();
                        records.clear();
                        records.addAll(gson.fromJson(jsonArray.toString(), type));
                        adapter.refresh();
                        showTitle();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject errorObj = new JSONObject(response.errorBody().string());
                        if (errorObj.has("success") && errorObj.has("message") && !errorObj.getBoolean("success"))
                            MLog.showToast(getApplicationContext(), errorObj.getString("message"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                progress.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progress.dismiss();
                MLog.showToast(getApplicationContext(), AppConst.ERROR_MSG);
            }
        });
    }

    private void showTitle() {
        if (records.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            tvNoRecordsFound.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvNoRecordsFound.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        adapter = new AccounStatementAdapter(records, this);
        recyclerView.setAdapter(adapter);
    }

    private void initialise() {
        progress = new SpotsDialog(this, R.style.Custom);
        authkey = MyPreferences.getStringValue(getApplicationContext(), "authkey");
        gson = new Gson();
    }

    private void setEventListeners () {

        SpinnerAdapter adapter = new SpinnerAdapter<CreditCustomer>(creditCustomerList) {
            @Override
            public void getView(int pos, SpinnerAdapter.MyHolder holder) {
                holder.textView.setText(creditCustomerList.get(pos).getId()+" - "+creditCustomerList.get(pos).getBusiness());
            }
        };

        tvCreditCustomer.setOnClickListener(v -> {
            DialogPlus dialog = DialogPlus.newDialog(this)
                    .setAdapter(adapter)
                    .setContentHeight(MyUtils.dpToPx(200))
                    .setOnItemClickListener((dialog1, item, view, position) -> {
                        dialog1.dismiss();
                        selectedCustomer = (CreditCustomer) item;
                        tvCreditCustomer.setText(selectedCustomer.getId()+" - "+selectedCustomer.getBusiness());
                        fetchTransactions();
                    })
                    .setCancelable(true)
                    .setExpanded(true)  // This will enable the expand feature, (similar to android L share dialog)
                    .setFooter(R.layout.dialog_footer)
                    .create();
            dialog.show();
            dialog.findViewById(R.id.btnCancel).setOnClickListener(v1 -> dialog.dismiss());
        });
    }

    private void getAccountStatementPDFUrl(boolean withHeader) {
        if (!MyUtils.hasInternetConnection(this)) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        progress.show();

        boolean credit = true;
        boolean debit = true;
        switch (typeEnum) {
            case All:
                break;
            case Credit:
                credit = true;
                debit = false;
                break;
            case Debit:
                debit = true;
                credit = false;
                break;
        }

        String customerID = selectedCustomer != null ? selectedCustomer.getId() : "";
        Call<ResponseBody> responce_outlets = APIClient.getApiService().generateAccountStatementPDF(authkey, firstDateString, secondDateString,
                credit, debit, withHeader, customerID, false);
        responce_outlets.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    try {
                        JSONObject res = new JSONObject(response.body().string());
                        JSONObject dataObj = res.getJSONObject("data");
                        String pdfURL = dataObj.getString("content");
                        downloadPDF(pdfURL);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject errorObj = new JSONObject(response.errorBody().string());
                        if (errorObj.has("success") && errorObj.has("message") && !errorObj.getBoolean("success"))
                            MLog.showToast(getApplicationContext(), errorObj.getString("message"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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

    private void downloadPDF(String pdfURL) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfURL.trim()));
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "account_transaction.pdf");
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home :
                onBackPressed();
                break;

            case R.id.menu_download_pdf:
                showFileHeaderPopup();
                break;

            case R.id.action_filter:
                break;

            case R.id.action_calendar:
                openDateFilterDialog();
                break;

            case R.id.action_all:
                item.setChecked(true);
                filterTransaction(TransactionTypeEnum.All);
                break;

            case R.id.action_credit:
                item.setChecked(true);
                filterTransaction(TransactionTypeEnum.Credit);
                break;

            case R.id.action_debit:
                item.setChecked(true);
                filterTransaction(TransactionTypeEnum.Debit);
                break;

            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void openDateFilterDialog () {
        if (dateDiaog == null ) {
            dateDiaog = new SlyCalendarDialog().setSingle(false).setCallback(this);
        }
        if (firstDate != null && secondDate != null) {
            dateDiaog.setStartDate(firstDate.getTime());
            dateDiaog.setEndDate(secondDate.getTime());
        }
        dateDiaog.show(getSupportFragmentManager(), "TAG_SLYCALENDAR");
    }

    private void filterTransaction(TransactionTypeEnum status) {
        typeEnum = status;
        if (!firstDateString.isEmpty() || !secondDateString.isEmpty()) {
            fetchTransactions();
        } else {
            adapter.filter(status);
        }
    }

    private void showFileHeaderPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want PDF with header?")
                .setNegativeButton("No", (dialogInterface, i) -> {
                    getAccountStatementPDFUrl(false);
                })
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    getAccountStatementPDFUrl(true);
                });


        builder.setCancelable(true);
        builder.show();
    }

    @Override
    public void onCancelled() {
    }

    @Override
    public void onDataSelected(Calendar firstDate, Calendar secondDate, int hours, int minutes) {
        try {
            this.firstDateString = MyUtils.dateToString(firstDate.getTime(), AppConst.SERVER_DATE_FORMAT);
            this.secondDateString = MyUtils.dateToString(secondDate.getTime(), AppConst.SERVER_DATE_FORMAT);
            this.firstDate = firstDate;
            this.secondDate = secondDate;
            fetchTransactions();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
