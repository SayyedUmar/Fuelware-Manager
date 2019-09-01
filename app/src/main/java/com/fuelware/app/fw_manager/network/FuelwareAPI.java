package com.fuelware.app.fw_manager.network;

import com.fuelware.app.fw_manager.activities.reports.AccountStatementActivity;
import com.fuelware.app.fw_manager.activities.ChangePasswordActivity;
import com.fuelware.app.fw_manager.activities.indents.EditBIndentActivity;
import com.fuelware.app.fw_manager.activities.indents.EditMIndentActivity;
import com.fuelware.app.fw_manager.activities.morning_params.MorningParamsActivity;
import com.fuelware.app.fw_manager.activities.plans.PlansActivity;
import com.fuelware.app.fw_manager.models.CounterBillPojo;
import com.fuelware.app.fw_manager.models.ForgotPasswordModel;
import com.fuelware.app.fw_manager.models.IndentModel;
import com.fuelware.app.fw_manager.models.ReceiptModel;
import com.fuelware.app.fw_manager.models.VersionCheckModel;
import com.fuelware.app.fw_manager.receivers.SmsReceiver;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
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

    @GET("outlet/manager/back-dated-indents")
    Call<ResponseBody> getBindentList(@Header("Authorization") String value);

    @GET("outlet/manager/cashier/{cashierID}/e-indent")
    Call<ResponseBody> getEindentList(@Header("Authorization") String value,
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


    @DELETE("outlet/manager/back-dated-indents/{indentID}?")
    Call<ResponseBody> deleteBIndent(@Header("Authorization") String value,
                                     @Path("indentID") String indentID
    );


    @PUT("outlet/manager/cashier/{cashierID}/m-indent/{indentID}")
    Call<ResponseBody> updateMIndent(@Header("Authorization") String value,
                                     @Path("cashierID") String cashierID,
                                     @Path("indentID") String indentID,
                                     @Body EditMIndentActivity.IndentNew model
                                     );

    @PUT("outlet/manager/back-dated-indents/{indentID}")
    Call<ResponseBody> updateBIndent(@Header("Authorization") String value,
                                     @Path("indentID") String indentID,
                                     @Body EditBIndentActivity.IndentNew model
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


    @GET("outlet/manager/report/credit-customer?paginate=true")
    Call<ResponseBody> fetchTransactions(@Header("Authorization") String value,
                                              @Query("customer_id") String customer_id,
                                              @Query("date_from") String date_from,
                                              @Query("date_to") String date_to,
                                              @Query("credit") boolean credit,
                                              @Query("debit") boolean debit,
                                              @Query("sort_order") String sortOder,
                                              @Query("page") long page,
                                              @Query("per_page") int count_per_page,
                                              @Query("search") String searchText
    );


    @POST("outlet/manager/report/credit-customer?paginate=true")
    Call<ResponseBody> fetchTransactionsPosts(@Header("Authorization") String value,
                                              @Query("customer_id") String customer_id,
                                              @Query("date_from") String date_from,
                                              @Query("date_to") String date_to,
                                              @Query("credit") boolean credit,
                                              @Query("debit") boolean debit,
                                              @Query("sort_order") String sortOder,
                                              @Query("page") long page,
                                              @Query("per_page") int count_per_page,
                                              @Query("search") String searchText,
                                              @Query("file") String fileType,
                                              @Query("file-header") boolean header,
                                              @Body AccountStatementActivity.DebitReportData param
    );



    @GET("outlet/manager/report/credit-customer")
    Call<ResponseBody> fetchTransactions(@Header("Authorization") String value,
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
                                                   @Query("customer_id") String customer_id,
                                                   @Query("date_from") String date_from,
                                                   @Query("date_to") String date_to,
                                                   @Query("credit") boolean credit,
                                                   @Query("debit") boolean debit,
                                                   @Query("file-header") boolean header,
                                                   @Query("sort_order") String sortOder,
                                                   @Query("search") String searchText
    );
    /*Call<ResponseBody> generateAccountStatementPDF(@Header("Authorization") String value,
                                                   @Query("date_from") String date_from,
                                                   @Query("date_to") String date_to,
                                                   @Query("credit") boolean credit,
                                                   @Query("debit") boolean debit,
                                                   @Query("file-header") boolean header,
                                                   @Query("customer_id") String customer_id,
                                                   @Query("paginate") boolean paginate
    );*/

    @POST("common/update-password")
    Observable<Response<ResponseBody>> changePassword(@Header("Authorization") String value,
                                                      @Body ChangePasswordActivity.ChangePasswordModel item);

    @GET("outlet/common/payment-plans")
    Call<ResponseBody> getPlans(@Header("Authorization") String value,
                                @Query("duration") String duration,
                                @Query("category") String category,
                                @Query("has_sms") boolean hasSms

    );

    @GET("outlet/common/payment-plans")
    Call<ResponseBody> getPlans(@Header("Authorization") String value,
                                @Query("duration") String duration

    );

    @GET("outlet/common/active-plan")
    Call<ResponseBody> getPurchasedPlans(@Header("Authorization") String value);

    @POST("outlet/common/apply-coupon")
    Call<ResponseBody> applyCouponCode(@Header("Authorization") String value,
                                       @Body PlansActivity.ApplyCouponParam param);

    @GET("outlet/common/payment-history")
    Call<ResponseBody> getPlanHistory(@Header("Authorization") String value);

    @POST("outlet/common/verify-payment-detail")
    Call<ResponseBody> purchasePlan(@Header("Authorization") String value,
                                    @Body PlansActivity.ApplyCouponParam param
    );

    @POST("outlet/manager/close-shift")
    Call<ResponseBody> closeShift(@Header("Authorization") String value);

    @GET("outlet/common/payment-plan-types")
    Call<ResponseBody> fetchPlanTypes(@Header("Authorization") String value);

    @PUT("outlet/common/payment-plans/{plan_id}")
    Call<ResponseBody> activatePlan(@Header("Authorization") String value,
                                @Path("plan_id") String planId
    );

    @Multipart
    @POST("mailtest.php")
    Call<ResponseBody> uploadImage(@Header("Authorization") String value,
                                   @Part MultipartBody.Part file,
                                   @Part("fw_id") RequestBody fw_id,
                                   @Part("name") RequestBody name,
                                   @Part("mobile") RequestBody mobile,
                                   @Part("mail_to") RequestBody email,
                                   @Part("issue") RequestBody issue,
                                   @Part("module") RequestBody module,
                                   @Part("sub_module") RequestBody sub_module,
                                   @Part("description") RequestBody description
    );


    @GET("outlet/batch-report")
    Call<ResponseBody> getBatchReport(@Header("Authorization") String authkey,
                                      @Query("page") long page
                                      );

    @POST("android-version")
    Call<ResponseBody> checkForNewVersion(@Header("Authorization") String value,
                                          @Body VersionCheckModel item);

    @POST("common/generate-otp")
    Call<ResponseBody> resetPassword(@Header("Content-Type") String type,
                                     @Body ForgotPasswordModel item);

    @POST("common/verify-otp")
    Call<ResponseBody> verifyOtp(@Header("Content-Type") String type,
                                 @Body ForgotPasswordModel item);

    @POST("common/reset-password")
    Call<ResponseBody> setNewPassword(@Header("Content-Type") String type,
                                      @Body ForgotPasswordModel item);


    @POST("outlet/cashier/indent?type=android")
    Call<ResponseBody> createMindent(@Header("Authorization") String value,
                                     @Header("Content-Type") String type,
                                     @Body IndentModel createMindentPojo
    );

    @POST("common/add-price")
    Call<ResponseBody> morningPriceUpdateReadingMsg(@Header("Authorization") String value,
                                                    @Body SmsReceiver.SMSModel model
    );



    @POST("outlet/manager/back-dated-indents")
    Call<ResponseBody> createBindent(@Header("Authorization") String value,
                                     @Header("Content-Type") String type,
                                     @Body IndentModel createMindentPojo
    );


    @GET("outlet/manager/search?indent_type=back_dated_indent")
    Observable<Response<ResponseBody>> creditCustomerListApi(@Header("Authorization") String value);

}
