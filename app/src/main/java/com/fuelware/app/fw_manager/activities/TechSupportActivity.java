package com.fuelware.app.fw_manager.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fuelware.app.fw_manager.R;
import com.fuelware.app.fw_manager.activities.base.SuperActivity;
import com.fuelware.app.fw_manager.adapters.GenericRecyclerAdapter;
import com.fuelware.app.fw_manager.adapters.SpinnerAdapter;
import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.appconst.Const;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.utils.MyPreferences;
import com.fuelware.app.fw_manager.utils.MyUtils;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ListHolder;
import com.orhanobut.dialogplus.ViewHolder;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TechSupportActivity extends SuperActivity {

    @BindView(R.id.btnSubmit)
    Button btnSubmit;
    @BindView(R.id.btnUpload)
    Button btnUpload;
    @BindView(R.id.tvIssue)
    TextView tvIssue;
    @BindView(R.id.tvModule)
    TextView tvModule;
    @BindView(R.id.etSubModuel)
    EditText etSubModuel;
    @BindView(R.id.etDescription)
    EditText etDescription;
    private Executor executor;
    String authkey;
    private List<String> issueList = new ArrayList<>();
    private List<String> modules = new ArrayList<>();
    private MultipartBody.Part fileToUpload;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tech_support);
        ButterKnife.bind(this);

        setupBackNavigation(null);
        initialise();
        setEventListeners();
    }

    private void initialise() {
        progressDialog = new SpotsDialog(this, R.style.Custom);
        authkey = MyPreferences.getStringValue(getApplicationContext(), Const.AUTHKEY);
        executor = Executors.newSingleThreadExecutor();
        issueList.add("Billing Issue");
        issueList.add("Technical Issue");
        issueList.add("Service Issue");
        issueList.add("Enhancement & Feeback");

        modules.add("Dealer");
        modules.add("Manager");
        modules.add("Cashier");
        modules.add("Credit Customer");
    }

    private void setEventListeners() {
        btnUpload.setOnClickListener(v -> {
            //showImageOpenDialog();
            AndPermission.with(this)
                    .runtime()
                    .permission(Permission.CAMERA, Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE)
                    .onGranted(permissions -> {
                        showImageSelectionPopup();
                    })
                    .onDenied(permissions -> {
                        MLog.showToast(getApplicationContext(), "Camera/Read/Write External storage access permission denied.");
                    })
                    .start();
        });

        SpinnerAdapter issueAdapter = new SpinnerAdapter<String>(issueList) {
            @Override
            public void getView(int pos, MyHolder holder) {
                holder.textView.setText(issueList.get(pos));
            }
        };

        SpinnerAdapter modulesAdapter = new SpinnerAdapter<String>(modules) {
            @Override
            public void getView(int pos, MyHolder holder) {
                holder.textView.setText(modules.get(pos));
            }
        };

        tvIssue.setOnClickListener(v -> {
            DialogPlus dialog = DialogPlus.newDialog(this)
                    .setContentHolder(new ListHolder())
                    .setAdapter(issueAdapter)
                    .setCancelable(true)
                    .setGravity(Gravity.CENTER)
                    .setOnItemClickListener((dialog1, item, view, position) -> {
                        dialog1.dismiss();
                        tvIssue.setText(item.toString());
                    })
                    .create();
            dialog.show();
        });

        tvModule.setOnClickListener(v -> {
            DialogPlus dialog = DialogPlus.newDialog(this)
                    .setContentHolder(new ListHolder())
                    .setAdapter(modulesAdapter)
                    .setCancelable(true)
                    .setGravity(Gravity.CENTER)
                    .setOnItemClickListener((dialog1, item, view, position) -> {
                        dialog1.dismiss();
                        tvModule.setText(item.toString());
                    })
                    .create();
            dialog.show();
        });

        btnSubmit.setOnClickListener(v -> uploadImage2());
    }

    private void showImageSelectionPopup() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Attach Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    openCamera();
                    dialog.dismiss();
                } else if (items[item].equals("Choose from Library")) {
                    openGallery();
                    dialog.dismiss();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, Const.REQUEST_IMAGE_CAPTURE);
    }

    private void openGallery() {
        Intent openGalleryIntent = new Intent(Intent.ACTION_PICK);
        openGalleryIntent.setType("image/*");
        startActivityForResult(openGalleryIntent, Const.REQUEST_GALLERY_CAPTURE);
    }

    private void showImageOpenDialog() {
        DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(new ViewHolder(R.layout.upload_image_layout))
                .setCancelable(true)
                .setContentHeight(250)
                .create();

        ImageView imgGallery = (ImageView) dialog.findViewById(R.id.imgGallery);
        ImageView imgCamera = (ImageView) dialog.findViewById(R.id.imgCamera);
        dialog.show();
        imgCamera.setOnClickListener(v -> {
            dialog.dismiss();
            AndPermission.with(v.getContext())
                    .runtime()
                    .permission(Permission.Group.STORAGE)
                    .onGranted(permissions -> {

                    })
                    .onDenied(permissions -> {
                        MLog.showToast(v.getContext(), "Read/Write External Storage permission denied!");
                    })
                    .start();
        });
        imgGallery.setOnClickListener(v -> {
            dialog.dismiss();

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Const.REQUEST_GALLERY_CAPTURE && resultCode == RESULT_OK) {
            uploadImage(new File(MyUtils.getRealPathFromURIPath(this, data.getData())));
        } else if (requestCode == Const.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        executor.execute(() -> {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            File destination = new File(getCacheDir().getPath() + "/","_Profile" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fo;
            try {
                destination.createNewFile();
                fo = new FileOutputStream(destination);
                fo.write(bytes.toByteArray());
                fo.close();
                uploadImage(destination);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void uploadImage(File file) {
        RequestBody fbody = RequestBody.create(MediaType.parse("image/*"), file);
        fileToUpload = MultipartBody.Part.createFormData("img", file.getName(), fbody);
        //uploadImage2();
    }
    private void uploadImage2() {

        if (!MyUtils.hasInternetConnection(this)) {
            MLog.showToast(getApplicationContext(), AppConst.NO_INTERNET_MSG);
            return;
        }

        progressDialog.show();

        String profileResponse = MyPreferences.getStringValue(this, AppConst.USER_PROFILE_DATA);
        String userName = "", mobile = "", formattedID = "";
        try {
            JSONObject data = new JSONObject(profileResponse);
            userName = data.getString("first_name") +" "+ data.getString("last_name");
            mobile = data.getString("mobile");
            try {
                JSONArray outletArray = data.getJSONObject("outlet").getJSONArray("data");
                if (outletArray.length() > 0) {
                    formattedID = outletArray.getJSONObject(0).getString("formatted_id");
                }
            } catch (Exception e) { e.printStackTrace(); }
        } catch (Exception e) { e.printStackTrace(); }

        Call<ResponseBody> call = APIClient.getApiService("http://complaints-fw.gobigdream.com/")
                .uploadImage(authkey,
                        fileToUpload,
                        RequestBody.create(MediaType.parse("multipart/form-data"), formattedID),
                        RequestBody.create(MediaType.parse("multipart/form-data"), userName),
                        RequestBody.create(MediaType.parse("multipart/form-data"), mobile),
                        RequestBody.create(MediaType.parse("multipart/form-data"), "fuelwaremail1@gmail.com"),
                        RequestBody.create(MediaType.parse("multipart/form-data"), tvIssue.getText().toString().trim()),
                        RequestBody.create(MediaType.parse("multipart/form-data"), tvModule.getText().toString().trim()),
                        RequestBody.create(MediaType.parse("multipart/form-data"), etSubModuel.getText().toString().trim()),
                        RequestBody.create(MediaType.parse("multipart/form-data"), etDescription.getText().toString().trim())
                );
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    progressDialog.dismiss();
                    if(response.isSuccessful()) {
                        MLog.showLongToast(getApplicationContext(), "Uploaded successfully.");
                        JSONObject res = new JSONObject(response.body().string());
                        finish();
                    } else {
                        JSONObject err = new JSONObject(response.errorBody().string());
                        if(err.has("message"))
                            MLog.showToast(getApplicationContext(), err.getString("message"));
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                MLog.showToast(getApplicationContext(), t.getMessage());
            }
        });
    }
}