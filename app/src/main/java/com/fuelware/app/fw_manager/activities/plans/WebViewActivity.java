package com.fuelware.app.fw_manager.activities.plans;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fuelware.app.fw_manager.BuildConfig;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.MainActivity;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.appconst.Const;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.utils.MyPreferences;

import java.util.regex.Pattern;

import butterknife.BindView;

public class WebViewActivity extends SuperActivity {

    @BindView(R.id.webview)
    WebView webView;
    final String staging_success_url = "http://staging.fuelware.in/payment-status/[0-9]{20}/success";
    final String prod_success_url = "http://app.fuelware.in/payment-status/[0-9]{20}/success";
    private Pattern pattern;
    private String frontend_url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);
        setupBackNavigation(null);
        setTitle("Purchase Plan");
        //pattern = Pattern.compile(staging_success_url);
        frontend_url = getIntent().getStringExtra("frontend_url");
        webView = findViewById(R.id.webview);
        webView.loadUrl(getIntent().getStringExtra("URL"));
        webView.getSettings().setJavaScriptEnabled(true);
        setEventListener();
    }

    private void setEventListener() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (url.equals(frontend_url)) {
                    MyPreferences.setBoolValue(getApplicationContext(), Const.PLAN_EXPIRED, false);
                    MLog.showLongToast(getApplicationContext(), "Your payment is received successfully.");
                    openHome();
                }
            }

        });
    }
    private void openHome() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
