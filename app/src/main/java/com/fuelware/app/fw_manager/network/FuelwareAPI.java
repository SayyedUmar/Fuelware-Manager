package com.fuelware.app.fw_manager.network;

import com.fuelware.app.fw_manager.models.CounterBillPojo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by Zest Developer on 3/27/2018.
 */

public interface FuelwareAPI {
    @FormUrlEncoded
    @POST("common/login")
    Call<ResponseBody> post_login(@Field("username") String email,
                                  @Field("password") String password,
                                  @Field("device_type") String device_type,
                                  @Field("user_type") String user_type);


    @GET("common/user")
    Call<ResponseBody> getUserDetails(@Header("Authorization") String value);

    @GET("common/outlet-products")
    Call<ResponseBody> getOutletProducts(@Header("Authorization") String value);

    @GET("outlet/manager/cashier")
    Call<ResponseBody> getCashiers(@Header("Authorization") String value);

    @GET("outlet/cashier/indent")
    Call<ResponseBody> getMindentList(@Header("Authorization") String value);

    @POST("outlet/common/save-counter-bill")
    Call<ResponseBody> createConuterBill(@Header("Authorization") String value,@Header("Content-Type") String type,
                                         @Body CounterBillPojo item);


}
