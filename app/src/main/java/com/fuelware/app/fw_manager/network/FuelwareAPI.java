package com.fuelware.app.fw_manager.network;

import com.fuelware.app.fw_manager.activities.ChangePasswordActivity;
import com.fuelware.app.fw_manager.activities.EditMIndentActivity;
import com.fuelware.app.fw_manager.activities.MorningParamsActivity;
import com.fuelware.app.fw_manager.activities.PlansActivity;
import com.fuelware.app.fw_manager.models.ReceiptModel;
import com.fuelware.app.fw_manager.models.CounterBillPojo;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
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

    @GET("outlet/manager/morning-detail?type=product")
    Call<ResponseBody> getManagerOutletProducts(@Header("Authorization") String value);

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
    Call<ResponseBody> getReceiptsList(@Header("Authorization") String value,
                                       @Query("payment-mode") String paymentMode);



    @DELETE("outlet/manager/receipt/{receipt_id}")
    Call<ResponseBody> deleteCashReceipt(@Header("Authorization") String value,
                                         @Path("receipt_id") String receiptID);
    //{"success":true,"message":"Receipt deleted successfully","data":null}


//    @GET("print/manager-receipt/{receipt_token}")
//    Call<ResponseBody> downloadCashReceipt(@Header("Authorization") String value,
//                                           @Path("receipt_token") String receiptToken);



    @GET("outlet/manager/search")
    Observable<Response<ResponseBody>> getCreditCustomerList(@Header("Authorization") String value);


    @POST("outlet/manager/receipt?")
    Call<ResponseBody> addCashReceipt(@Header("Authorization")String authkey,
                                      @Body ReceiptModel model,
                                      @Query("payment-mode") String paymentMode);


    @GET("outlet/e-wallet")
    Observable<Response<ResponseBody>> getAllEwallets(@Header("Authorization")String authkey);

    @PUT("outlet/manager/receipt/{receiptID}?")
    Call<ResponseBody> updateCashReceipt(@Header("Authorization")String authkey,
                                         @Path("receiptID") String receiptID,
                                         @Body ReceiptModel model,
                                         @Query("payment-mode") String mode);

    @POST("outlet/manager/price")
    Call<ResponseBody> updateMorningParams(@Header("Authorization")String authkey,
                                           @Body MorningParamsActivity.Products products);
    //res ; {"success":true,"message":"","data":[{"id":4233,"product_id":14,"price":"40"},{"id":4234,"product_id":15,"price":"20"}]}


    @GET("outlet/manager/report/credit-customer")
    Call<ResponseBody> getAccountStatementNew(@Header("Authorization") String value,
                                              @Query("date_from") String date_from,
                                              @Query("date_to") String date_to,
                                              @Query("credit") boolean credit,
                                              @Query("debit") boolean debit,
                                              @Query("customer_id") String customer_id

    );

    @GET("outlet/manager/report/credit-customer")
    Call<ResponseBody> getAccountStatementNew1(@Header("Authorization") String value,
                                              @Query("date_from") String date_from,
                                              @Query("date_to") String date_to,
                                              @Query("credit") boolean credit,
                                              @Query("debit") boolean debit,
                                              @Query("customer_id") String customer_id,
                                              @Query("file-header") boolean header,
                                              @Query("paginate") boolean paginate

    );

    @GET("outlet/manager/report/credit-customer?file=pdf")
    Call<ResponseBody> generateAccountStatementPDF(@Header("Authorization") String value,
                                                   @Query("date_from") String date_from,
                                                   @Query("date_to") String date_to,
                                                   @Query("credit") boolean credit,
                                                   @Query("debit") boolean debit,
                                                   @Query("file-header") boolean header,
                                                   @Query("customer_id") String customer_id,
                                                   @Query("paginate") boolean paginate
    );

    @POST("common/update-password")
    Observable<Response<ResponseBody>> changePassword(@Header("Authorization") String value,
                                                      @Body ChangePasswordActivity.ChangePasswordModel item);

    @GET("outlet/common/payment-plans")
    Call<ResponseBody> getPlans(@Header("Authorization") String value,
                                @Query("duration") String duration,
                                @Query("has_sms") boolean hasSms

    );

    @GET("outlet/common/payment-plans")
    Call<ResponseBody> getPlans(@Header("Authorization") String value,
                                @Query("duration") String duration

    );

    @GET("outlet/common/payment-plans")
    Call<ResponseBody> getPlans(@Header("Authorization") String value);

    @POST("outlet/common/apply-coupon")
    Call<ResponseBody> applyCouponCode(@Header("Authorization") String value,
                                       @Body PlansActivity.ApplyCouponParam param);

    @GET("outlet/common/payment-history")
    Call<ResponseBody> getPlanHistory(@Header("Authorization") String value);
}
