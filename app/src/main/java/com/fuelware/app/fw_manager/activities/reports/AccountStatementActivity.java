package com.fuelware.app.fw_manager.activities.reports;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.fuelware.app.fw_manager.BuildConfig;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.adapters.AccounStatementAdapter;
import com.fuelware.app.fw_manager.adapters.GeneriBaseAdapter;
import com.fuelware.app.fw_manager.adapters.SpinnerAdapter;
import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.appconst.Const;
import com.fuelware.app.fw_manager.models.AccountModel;
import com.fuelware.app.fw_manager.models.CreditCustomer;
import com.fuelware.app.fw_manager.models.Pagination;
import com.fuelware.app.fw_manager.models.TransactionTypeEnum;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.services.MyService;
import com.fuelware.app.fw_manager.services.MyServiceListerner;
import com.fuelware.app.fw_manager.utils.MyPreferences;
import com.fuelware.app.fw_manager.utils.MyUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ListHolder;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;
import com.wangjie.rapidfloatingactionbutton.util.RFABShape;
import com.wangjie.rapidfloatingactionbutton.util.RFABTextUtil;
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
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.slybeaver.slycalendarview.SlyCalendarDialog;

public class AccountStatementActivity extends SuperActivity implements SlyCalendarDialog.Callback,
        RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener,
        SearchView.OnQueryTextListener {


    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.tvNoRecordsFound)
    TextView tvNoRecordsFound;
    @BindView(R.id.tvCreditCustomer)
    TextView tvCreditCustomer;
    @BindView(R.id.activity_main_rfal)
    RapidFloatingActionLayout rfaLayout;
    @BindView(R.id.activity_main_rfab)
    RapidFloatingActionButton rfaBtn;
    @BindView(R.id.refresh_layout)
    SwipyRefreshLayout refresh_layout;
    @BindView(R.id.tvClosingBalance)
    TextView tvClosingBalance;

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
    private String file_type = "pdf";

    private Pagination pagination = new Pagination();
    private int per_page_count = 40;
    private boolean sort_order_desc = true;
    private String searchText = "";

    private RapidFloatingActionHelper rfabHelper;
    private List<RFACLabelItem> items = new ArrayList<>();
    private Menu menu;
    private String debit_amount;
    private RFACLabelItem<Integer> reportTypeItem;
    private RapidFloatingActionContentLabelList rfaContent;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account_statement);
        ButterKnife.bind(this);

        setupBackNavigation(null);
//        setTitle("Business Name");

        initialise();
        setupRecyclerView();
        setEventListeners();
        setupFloatButton();
        fetchTransactions();
        fetchCreditCustomers();

        tvClosingBalance.setVisibility(View.GONE);
    }

    private void setupFloatButton() {
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Download PDF")
                .setResId(R.drawable.ic_download_arrow)
                .setIconNormalColor(0xffd84315)
                .setIconPressedColor(0xffbf360c)
                .setLabelColor(Color.WHITE)
                .setLabelSizeSp(12)
                .setLabelBackgroundDrawable(RFABShape.generateCornerShapeDrawable(0xaa000000, RFABTextUtil.dip2px(this, 4)))
                .setWrapper(0)
        );

        reportTypeItem = new RFACLabelItem<Integer>()
                .setLabel("Report Type")
                .setResId(R.drawable.ic_calendar)
                .setIconNormalColor(0xff4e342e)
                .setIconPressedColor(0xff3e2723)
                .setLabelColor(Color.WHITE)
                .setLabelSizeSp(12)
                .setLabelBackgroundDrawable(RFABShape.generateCornerShapeDrawable(0xaa000000, RFABTextUtil.dip2px(this, 4)))
                .setWrapper(1);

        //items.add(reportTypeItem);

        items.add(new RFACLabelItem<Integer>()
                .setLabel("Apply Date Filter")
                .setResId(R.drawable.ic_calendar)
                .setIconNormalColor(0xff4e342e)
                .setIconPressedColor(0xff3e2723)
                .setLabelColor(Color.WHITE)
                .setLabelSizeSp(12)
                .setLabelBackgroundDrawable(RFABShape.generateCornerShapeDrawable(0xaa000000, RFABTextUtil.dip2px(this, 4)))
                .setWrapper(1)
        );

        rfaContent = new RapidFloatingActionContentLabelList(this);
        rfaContent.setOnRapidFloatingActionContentLabelListListener(this);

        rfaContent.setItems(items)
                .setIconShadowRadius(RFABTextUtil.dip2px(this, 5))
                .setIconShadowColor(0xff888888)
                .setIconShadowDy(RFABTextUtil.dip2px(this, 5))
        ;
        rfabHelper = new RapidFloatingActionHelper(
                this,
                rfaLayout,
                rfaBtn,
                rfaContent
        ).build();
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
                if (items.indexOf(reportTypeItem) != -1) {
                    items.remove(reportTypeItem);
                    rfabHelper.build();
                }
                break;
            case Credit:
                credit = true;
                debit = false;
                if (items.indexOf(reportTypeItem) != -1) {
                    items.remove(reportTypeItem);
                    rfabHelper.build();
                }
                break;
            case Debit:
                debit = true;
                credit = false;
                if (!firstDateString.isEmpty() && !secondDateString.isEmpty() && selectedCustomer != null) {
                    if (items.indexOf(reportTypeItem) == -1) {
                        items.add(reportTypeItem);
                        rfabHelper.build();
                    }
                }
                break;
        }

        String sortOder = "desc";
        if (sort_order_desc) {
            sortOder = "desc";
        } else {
            sortOder = "asc";
        }

        String customerID = selectedCustomer != null ? selectedCustomer.getId() : "";
        long pageNumber = pagination != null ? pagination.currentPage+1 : 0;

        Call<ResponseBody> responce_outlets = APIClient.getApiService().fetchTransactions(
                authkey, customerID,
                firstDateString, secondDateString,
                credit, debit, sortOder, pageNumber, per_page_count, searchText);
        responce_outlets.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject res = new JSONObject(response.body().string());
                        JSONObject page = res.getJSONObject("meta").getJSONObject("pagination");
                        pagination = gson.fromJson(page.toString(), Pagination.class);
                        JSONObject dataObj = res.getJSONObject("data");
                        debit_amount = dataObj.getString("debit_amount");
                        JSONArray jsonArray = dataObj.getJSONArray("credit_customer");
                        Type type = new TypeToken<List<AccountModel>>() {}.getType();
                        if (pageNumber == 1)
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
                refresh_layout.setRefreshing(false);
                progress.hide();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                MLog.showToast(getApplicationContext(), AppConst.ERROR_MSG);
                refresh_layout.setRefreshing(false);
                progress.hide();
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

//        GeneriBaseAdapter adapter2 = new GeneriBaseAdapter<CreditCustomer>(creditCustomerList, R.layout.row_credit_customer, new GeneriBaseAdapter.DailogListener() {
//            @Override
//            public Object getHolder(View v, int pos) {
//                return null;
//            }
//
//            @Override
//            public void setView(View v, int pos) {
//
//            }
//        });

        tvCreditCustomer.setOnClickListener(v -> {
            DialogPlus dialog = DialogPlus.newDialog(this)
                    .setAdapter(adapter)
                    .setContentHeight(MyUtils.dpToPx(250))
                    .setOnItemClickListener((dialog1, item, view, position) -> {
                        dialog1.dismiss();
                        selectedCustomer = (CreditCustomer) item;
                        tvCreditCustomer.setText(selectedCustomer.getId()+" - "+selectedCustomer.getBusiness());
                        pagination = new Pagination();
                        fetchTransactions();
                    })
                    .setMargin(0, MyUtils.dpToPx(60), 0, 0)
                    .setCancelable(true)
                    .setExpanded(true)  // This will enable the expand feature, (similar to android L share dialog)
                    .setFooter(R.layout.dialog_footer)
                    .create();
            dialog.show();
            dialog.findViewById(R.id.btnCancel).setOnClickListener(v1 -> dialog.dismiss());
        });

        refresh_layout.setOnRefreshListener(direction -> {
            Log.d("MainActivity", "Refresh triggered at "
                    + (direction == SwipyRefreshLayoutDirection.TOP ? "top" : "bottom"));

            if (direction == SwipyRefreshLayoutDirection.BOTTOM) {
                if (pagination != null && pagination.currentPage < pagination.totalPages) {
                    fetchTransactions();
                } else {
                    refresh_layout.setRefreshing(false);
                    MLog.showToast(this, "Your data is already updated.");
                }
            }
        });
    }

    private void getAccountStatementPDFUrl(boolean withHeader, boolean sort_order_desc) {
        if (!MyUtils.hasInternetConnection(this)) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
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

        String sortOder = "desc";
        if (sort_order_desc) {
            sortOder = "desc";
        } else {
            sortOder = "asc";
        }

        String customerID = selectedCustomer != null ? selectedCustomer.getId() : "";

        Call<ResponseBody> responce_outlets;
        if (file_type.equals("pdf")) {
            responce_outlets = APIClient.getApiService().generateAccountStatementPDF(authkey, customerID,
                    firstDateString, secondDateString, credit, debit, withHeader, sortOder, searchText);
        } else {
            String notification_token = MyPreferences.getStringValue(this, Const.NOTIFICATION_TOKEN);
            String url = getCSVurl(authkey, customerID, firstDateString, secondDateString, credit, debit, withHeader, sortOder, searchText, notification_token);
            downloadCSV(url);
            return;
        }

        progress.show();

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
            MLog.showToast(getApplicationContext(), "Account Statement downloaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadCSV(String url) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url.trim()));
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "account_transaction.csv");
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(request);
            MLog.showToast(getApplicationContext(), "Account Statement downloaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_menu, menu);
        this.menu = menu;
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("Enter Indent No, Veh/Mc No");
            searchView.setOnQueryTextListener(this);
            searchView.setIconified(false);

            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem menuItem) {
                    showAllMenu(false);
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                    showAllMenu(true);
                    searchText = "";

                    return true;
                }
            });

        }

        return super.onCreateOptionsMenu(menu);
    }

    private void showAllMenu (boolean toshow) {
        if (toshow) {
            menu.findItem(R.id.menu_download_pdf).setVisible(true);
            menu.findItem(R.id.action_filter).setVisible(true);
            menu.findItem(R.id.action_calendar).setVisible(true);
        } else {
            menu.findItem(R.id.menu_download_pdf).setVisible(false);
            menu.findItem(R.id.action_filter).setVisible(false);
            menu.findItem(R.id.action_calendar).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home :
                onBackPressed();
                break;

            case R.id.action_search:
                //startActivity(new Intent(this, SearchTransactionsActivity.class));
                break;

            case R.id.menu_download_pdf:
                if (selectedCustomer == null) {
                    MLog.showLongToast(getApplicationContext(), "Select Business Name first");
                } else {
                    showDownloadPDFDialog();
                }
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
        pagination = new Pagination();
//        if (!firstDateString.isEmpty() || !secondDateString.isEmpty()) {
            fetchTransactions();
//        } else {
//            adapter.filter(status);
//        }
    }



    private void showReportTypeDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_report_type);
//        final TextView tvTransCount = dialog.findViewById(R.id.tvTransCount);
//        final RadioButton radioDsc = dialog.findViewById(R.id.radioDsc);
//        final RadioButton radioPdf = dialog.findViewById(R.id.radioPdf);


        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();

        final LinearLayout linlayReportFormat = dialog.findViewById(R.id.linlayReportFormat);
        final TableLayout tableContainer = dialog.findViewById(R.id.tableContainer);


        final RadioButton radioDebit = dialog.findViewById(R.id.radioDebit);
        final RadioButton radioBillInvoice = dialog.findViewById(R.id.radioBillInvoice);
        final RadioButton radioCsv = dialog.findViewById(R.id.radioCsv);
        final RadioButton radioPdf = dialog.findViewById(R.id.radioPdf);
        final RadioButton radioHeader = dialog.findViewById(R.id.radioHeader);
        final RadioButton radioWithoutHeader = dialog.findViewById(R.id.radioWithoutHeader);
        final RadioButton radioAsc = dialog.findViewById(R.id.radioAsc);
        final RadioButton radioDsc = dialog.findViewById(R.id.radioDsc);

        final EditText etBillNumber = dialog.findViewById(R.id.etBillNumber);
        final EditText etNetPayable = dialog.findViewById(R.id.etNetPayable);
        final EditText etIndentCharge = dialog.findViewById(R.id.etIndentCharge);
        final TextView tvIndentConsumed = dialog.findViewById(R.id.tvIndentConsumed);
        final EditText etLateCharge = dialog.findViewById(R.id.etLateCharge);
        final EditText etSurplusCharge = dialog.findViewById(R.id.etSurplusCharge);
        final EditText etSurplusRemark = dialog.findViewById(R.id.etSurplusRemark);

        final TextView tvDownload = dialog.findViewById(R.id.tvDownload);
        final TextView tvCancel = dialog.findViewById(R.id.tvCancel);

        etNetPayable.setText(debit_amount);
        tvIndentConsumed.setText("Consumed ("+pagination.total+")");

        etIndentCharge.setFilters(new InputFilter[]{ new MyUtils.InputFilterMinMax(0, 1000000)});
        etLateCharge.setFilters(new InputFilter[]{ new MyUtils.InputFilterMinMax(0, 1000000)});
        etSurplusCharge.setFilters(new InputFilter[]{ new MyUtils.InputFilterMinMax(0, 1000000)});

        tvCancel.setOnClickListener(v -> dialog.dismiss());

        tvDownload.setOnClickListener(v -> {

            AndPermission.with(v.getContext())
                    .runtime()
                    .permission(Permission.Group.STORAGE)
                    .onGranted(permissions -> {
                        if (radioDebit.isChecked()) {
                            typeEnum = TransactionTypeEnum.Debit;
                            fetchResult(radioDsc.isChecked(), radioPdf.isChecked(), radioHeader.isChecked());
                            dialog.dismiss();
                        } else {
                            String billNumber = etBillNumber.getText().toString().trim();
                            String indentCharges = etIndentCharge.getText().toString().trim();
                            String lateCharges = etLateCharge.getText().toString().trim();
                            String surplusCharges = etSurplusCharge.getText().toString().trim();
                            String surplusRemarks = etSurplusRemark.getText().toString().trim();
                            DebitReportData data = new DebitReportData(billNumber, indentCharges, lateCharges, surplusCharges, surplusRemarks);
                            fetchTransactionsPosts(data, dialog, radioHeader.isChecked());
                        }
                    })
                    .onDenied(permissions -> {
                        MLog.showToast(v.getContext(), "Read/Write External Storage permission denied!");
                    })
                    .start();

        });

        if (radioDebit.isChecked()) {
            tableContainer.setVisibility(View.GONE);
            linlayReportFormat.setVisibility(View.VISIBLE);
        } else {
            linlayReportFormat.setVisibility(View.GONE);
            tableContainer.setVisibility(View.VISIBLE);
        }

        radioDebit.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked) {
                tableContainer.setVisibility(View.GONE);
                linlayReportFormat.setVisibility(View.VISIBLE);
            } else {
                linlayReportFormat.setVisibility(View.GONE);
                tableContainer.setVisibility(View.VISIBLE);
            }
        });

    }

    private void showDownloadPDFDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_download_pdf);
        final TextView tvYes = dialog.findViewById(R.id.tvYes);
        final TextView tvNo = dialog.findViewById(R.id.tvNo);
        final TextView tvTransCount = dialog.findViewById(R.id.tvTransCount);
        final RadioButton radioDsc = dialog.findViewById(R.id.radioDsc);
        final RadioButton radioPdf = dialog.findViewById(R.id.radioPdf);
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();

        tvTransCount.setText("Result: "+pagination.total+" transactions");

        tvNo.setOnClickListener(view -> {
            fetchResult(radioDsc.isChecked(), radioPdf.isChecked(), false);
            dialog.dismiss();
        });
        tvYes.setOnClickListener(view -> {
            fetchResult(radioDsc.isChecked(), radioPdf.isChecked(), true);
            dialog.dismiss();
        });
    }

    private void fetchResult(boolean sortOrder, boolean pdfFile,  boolean withHeader) {
        if (sort_order_desc != sortOrder) {
            sort_order_desc = sortOrder;
            pagination = new Pagination();
        }
        if (pdfFile) {
            file_type = "pdf";
        } else {
            file_type = "csv";
        }

        getAccountStatementPDFUrl(withHeader, sortOrder);
        fetchTransactions();
    }

    private String getCSVurl(String authkey, String customerID, String date_from, String date_to, boolean credit, boolean debit, boolean withHeader, String sortOder, String searchText,String notification_token) {
        String url = BuildConfig.BASE_URL_2+"csv/?"+"file=csv&date_from="+date_from+"&customer_id="+customerID+"&date_to="+date_to+"&credit="+credit
                +"&debit="+debit+"&file-header="+withHeader+"&sort_order="+sortOder+"&search="+searchText+"&token="+notification_token;
        return url;
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
            pagination = new Pagination();
            fetchTransactions();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void onRFACItemLabelClick(int position, RFACLabelItem item) {
        selectFilterOptions(item);
        rfabHelper.toggleContent();
    }

    @Override
    public void onRFACItemIconClick(int position, RFACLabelItem item) {
        selectFilterOptions(item);
        rfabHelper.toggleContent();
    }

    private void selectFilterOptions(RFACLabelItem item) {
        if (item.getLabel().equalsIgnoreCase("apply date filter")) { // date filter
            openDateFilterDialog();
        } else if (item.getLabel().equalsIgnoreCase("report type")) { // report type
            showReportTypeDialog();
        } else if (item.getLabel().equalsIgnoreCase("download pdf")) { // pdf download
            if (selectedCustomer == null) {
                MLog.showLongToast(getApplicationContext(), "Select Business Name first");
            } else {
                showDownloadPDFDialog();
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchText = newText;
        adapter.getFilter().filter(newText);
        return true;
    }



    private void fetchTransactionsPosts(DebitReportData item, Dialog dialog, boolean withHeader) {
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

        String sortOder = "desc";
        if (sort_order_desc) {
            sortOder = "desc";
        } else {
            sortOder = "asc";
        }

        String customerID = selectedCustomer != null ? selectedCustomer.getId() : "";
        long pageNumber = pagination != null ? pagination.currentPage+1 : 0;

        Call<ResponseBody> responce_outlets = APIClient.getApiService().fetchTransactionsPosts(
                authkey, customerID,
                firstDateString, secondDateString,
                credit, debit, sortOder, pageNumber, per_page_count, "", "pdf", withHeader, item);
        responce_outlets.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject res = new JSONObject(response.body().string());
                        MLog.showToast(getApplicationContext(), "response received");
                        dialog.dismiss();
                        downloadPDF(res.getJSONObject("data").getString("content"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject errorObj = new JSONObject(response.errorBody().string());

                        if (errorObj.has("errors")) {
                            JSONObject errors = errorObj.getJSONObject("errors");
                            if (errors.has("invoice_number")) {
                                MLog.showToast(getApplicationContext(), errors.getJSONArray("invoice_number").get(0).toString());
                            } else if (errors.has("indent_charge")) {
                                MLog.showToast(getApplicationContext(), errors.getJSONArray("indent_charge").get(0).toString());
                            } else if (errors.has("late_pay_charge")) {

                            } else if (errors.has("surplus_charge")) {

                            } else if (errors.has("surplus_remark")) {

                            }
                        } else if (errorObj.has("success") && errorObj.has("message") && !errorObj.getBoolean("success"))
                            MLog.showToast(getApplicationContext(), errorObj.getString("message"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                refresh_layout.setRefreshing(false);
                progress.hide();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                MLog.showToast(getApplicationContext(), AppConst.ERROR_MSG);
                refresh_layout.setRefreshing(false);
                progress.hide();
            }
        });
    }


    public static class DebitReportData {
        public String invoice_number;
        public String indent_charge;
        public String late_pay_charge;
        public String surplus_charge;
        public String surplus_remark;

        public DebitReportData(String invoice_number, String indent_charge, String late_pay_charge, String surplus_charge, String surplus_remark) {
            this.invoice_number = invoice_number;
            this.indent_charge = indent_charge;
            this.late_pay_charge = late_pay_charge;
            this.surplus_charge = surplus_charge;
            this.surplus_remark = surplus_remark;
        }
    }
}
