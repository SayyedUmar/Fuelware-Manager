package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fuelware.app.fw_manager.BuildConfig;
import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.FuelwareAPI;
import com.fuelware.app.fw_manager.utils.ConnectionDetector;
import com.fuelware.app.fw_manager.utils.MyPreferences;

import org.json.JSONObject;

import dmax.dialog.SpotsDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    AlertDialog progressDialog;
    EditText etUsername, etPassword;
    Button signinbutton,registerButton;
    TextView forgotpassword,themetext;
    ConnectionDetector connectionDetector;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private ImageView ivHideShowPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById();

        initialiseViews();

        setActionListeners();

        if (BuildConfig.DEBUG) {
            etUsername.setText("9920010395");
            etPassword.setText("test@123");
        }
    }

    private void initialiseViews() {
        connectionDetector = new ConnectionDetector(this);
        sharedPreferences = getSharedPreferences("userdetails",0);
        editor = sharedPreferences.edit();
        //changing statusbar color
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.statusbarlogin));
        }

        //custom font adding starts here
//        Typeface typefacecursive = Typeface.createFromAsset(getApplicationContext().getAssets(),
//                "GreatVibes-Regular.ttf");
//        Typeface typefacemedium = Typeface.createFromAsset(getApplicationContext().getAssets(),
//                "Quicksand-Medium.ttf");
//        Typeface typefacebold= Typeface.createFromAsset(getApplicationContext().getAssets(),
//                "Quicksand-Bold.ttf");
//        themetext.setTypeface(typefacecursive);
//        userid.setTypeface(typefacemedium);
//        password.setTypeface(typefacemedium);
//        forgotpassword.setTypeface(typefacemedium);
//        signinbutton.setTypeface(typefacebold);

        progressDialog = new SpotsDialog(LoginActivity.this, R.style.Custom);


        String text = "Forgot Password? <font color=\"#33691E\">ClickHere</font>";
        forgotpassword.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);

    }

    private void findViewById() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        forgotpassword = findViewById(R.id.forgotpassword);
        signinbutton = findViewById(R.id.signin_button);
        registerButton = findViewById(R.id.register_button);
        themetext = findViewById(R.id.theme_text);
        ivHideShowPwd = findViewById(R.id.ivHideShowPwd);
        ivHideShowPwd.setTag(true);
    }

    private void setActionListeners() {
        signinbutton.setOnClickListener( v -> performLogin() );
        forgotpassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

        etPassword.setOnEditorActionListener((v, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                performLogin();
                return true;
            }
            return false;
        });

        ivHideShowPwd.setOnTouchListener((v, event) -> {
            switch ( event.getAction() ) {

                case MotionEvent.ACTION_UP:
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    break;

                case MotionEvent.ACTION_DOWN:
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    break;
            }
            return true;
        });
    }

    private void performLogin(){
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        String user_id = etUsername.getText().toString().trim().toLowerCase();
        String pass = etPassword.getText().toString().trim();
        if (user_id.equals("")) {
            etUsername.setError("Please Enter User ID.");
            etUsername.requestFocus();
        } else if (pass.equals("")) {
            etPassword.setError("Please Enter Password");
            etPassword.requestFocus();
        } else {
            progressDialog.show();
            CheckInternetConnection();
        }
    }
    public void CheckInternetConnection() {
        ConnectivityManager cn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nf = cn.getActiveNetworkInfo();
        if (nf != null && nf.isConnected() == true) {
            sendLoginRequest();
        } else {
            Toast.makeText(this,"no internet connection", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }

    }
    public void sendLoginRequest(){
        //8007239708
        Retrofit adapter = APIClient.getRetroClient(AppConst.ROOT_URL);

        FuelwareAPI fuelwareAPI = adapter.create(FuelwareAPI.class);
        Call<ResponseBody> responce_login;
        responce_login = fuelwareAPI.post_login(etUsername.getText().toString().trim(),
                etPassword.getText().toString().trim(), AppConst.DEVICE_TYPE, AppConst.APP_TYPE);
        responce_login.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();

                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String access_token=jsonObject.getString("access_token");
                        String token_type=jsonObject.getString("token_type");
                        String data=token_type+" "+access_token;
                        editor.putString("authkey",data);
                        MyPreferences.setBoolValue(LoginActivity.this, AppConst.LOGIN_STATUS, true);
                        editor.commit();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        JSONObject errorObj = new JSONObject(response.errorBody().string());
                        if (errorObj.has("success") && errorObj.has("message") && !errorObj.getBoolean("success"))
                            Toast.makeText(LoginActivity.this, errorObj.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), AppConst.ERROR_MSG, Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progressDialog != null) {
            progressDialog.dismiss();
        }

    }

}
