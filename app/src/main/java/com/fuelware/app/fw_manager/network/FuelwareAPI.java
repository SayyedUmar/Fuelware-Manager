package com.fuelware.app.fw_manager.network;

import com.fuelware.app.fw_manager.activities.EditMIndentActivity;
import com.fuelware.app.fw_manager.models.CounterBillPojo;
import com.fuelware.app.fw_manager.models.IndentModel;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @GET("outlet/manager/cashier/{cashierID}/m-indent")
    Call<ResponseBody> getMindentList(@Header("Authorization") String value,
                                      @Path("cashierID") String cashierID);

    @POST("outlet/common/save-counter-bill")
    Call<ResponseBody> createConuterBill(@Header("Authorization") String value, @Header("Content-Type") String type,
                                         @Body CounterBillPojo item);

    @GET("outlet/cashier/batch?")
    Call<ResponseBody> getCashierDetail(@Header("Authorization") String value,
                                        @Query("cashier_id") String cashierID);

    @GET("outlet/manager/cashier/{cashierID}/m-indent")
    Call<ResponseBody> getMIndents(@Header("Authorization") String value,
                                   @Path("cashierID") String cashierID);

    @GET("outlet/manager/cashier/{cashierID}")
    Call<ResponseBody> getMIndentCount(@Header("Authorization") String value,
                                   @Path("cashierID") String cashierID);

    @GET("outlet/manager/cashier/indent/{id}")
    Call<ResponseBody> getMindentDetails(@Header("Authorization") String value,@Path("id") String id);

    @POST("outlet/manager/cashier/{cashierID}/m-indent/{indentID}")
    Call<ResponseBody> approveMIndent(@Header("Authorization") String value,
                                      @Path("cashierID") String cashierID,
                                      @Path("indentID") String indentID
                                      );

    @DELETE("outlet/manager/cashier/{cashierID}/m-indent/{indentID}?")
    Call<ResponseBody> deleteMIndent(@Header("Authorization") String value,
                                     @Path("cashierID") String cashierID,
                                     @Path("indentID") String indentID,
                                     @Query("otp") String otp
    );

    @PUT("outlet/manager/cashier/{cashierID}/m-indent/{indentID}")
    Call<ResponseBody> updateMIndent(@Header("Authorization") String value,
                                     @Path("cashierID") String cashierID,
                                     @Path("indentID") String indentID,
                                     @Body EditMIndentActivity.IndentNew model
                                     );


    @POST("common/otp?")
    Call<ResponseBody> requestOTP(@Header("Authorization") String value,
                                  @Query("user_id") String cashierID
    );


    @GET("outlet/manager/receipt?payment-mode=cash")
    Call<ResponseBody> getCashReceiptsList(@Header("Authorization") String value);


    @DELETE("outlet/manager/receipt/{receipt_id}")
    Call<ResponseBody> deleteCashReceipt(@Header("Authorization") String value,
                                         @Path("receipt_id") String receiptID);
    //{"success":true,"message":"Receipt deleted successfully","data":null}


//    @GET("print/manager-receipt/{receipt_token}")
//    Call<ResponseBody> downloadCashReceipt(@Header("Authorization") String value,
//                                           @Path("receipt_token") String receiptToken);



    @GET("outlet/manager/search")
    Call<ResponseBody> getCreditCustomerList(@Header("Authorization") String value);

}
