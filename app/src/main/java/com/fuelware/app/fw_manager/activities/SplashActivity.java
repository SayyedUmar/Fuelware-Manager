package com.fuelware.app.fw_manager.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.utils.MyPreferences;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.white));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        netValidator();
        /*if (isReadStorageAllowed()) {
            netValidator();
        } else {   //If the app has not the permission then asking for the permission
            requestStoragePermission();
        }*/
    }

    private boolean isReadStorageAllowed() {
        // int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int resultread = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int resultwrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (resultread == PackageManager.PERMISSION_GRANTED && resultwrite == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            //If permission is not granted returning false
            return false;
        }
    }

    private void requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {

            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            //  Toast.makeText(SplashActivity.this, "For Marsmallow,we do need this permission", Toast.LENGTH_SHORT).show();
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request

        Log.d("requestcodefor","resultsare "+requestCode);

        if(requestCode == 1) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                netValidator();
                //Displaying a toast
                // Toast.makeText(this,"Permission granted now you can read the storage",Toast.LENGTH_LONG).show();
            } else {
                // finish();
                //Displaying another toast if permission is not granted
                // Toast.makeText(this,"You have just denied the permission",Toast.LENGTH_LONG).show();

                final AlertDialog.Builder alertDialog;
                DialogInterface.OnClickListener diloagclicklistener=new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                requestStoragePermission();
                                dialogInterface.dismiss();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicke
                                dialogInterface.dismiss();
                                finish();
                                break;
                        }
                    }
                };
                alertDialog = new AlertDialog.Builder(SplashActivity.this);
                alertDialog.setMessage("In order to use this App, you need to allow all Permission").setPositiveButton("Ok",diloagclicklistener).
                        setNegativeButton("", diloagclicklistener).show();
            }
        }
    }

    public void netValidator() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run()
            {
                forNext();
            }
        }, 3000);

    }

    private void forNext() {
        if (!MyPreferences.getStringValue(getApplicationContext(), "authkey").isEmpty()) {
            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        } else {
            Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(mainIntent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
