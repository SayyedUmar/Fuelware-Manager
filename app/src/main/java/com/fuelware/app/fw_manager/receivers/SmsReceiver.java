package com.fuelware.app.fw_manager.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.network.APIClient;
import com.fuelware.app.fw_manager.network.MLog;
import com.fuelware.app.fw_manager.utils.MyPreferences;
import com.fuelware.app.fw_manager.utils.MyUtils;

import org.json.JSONObject;

import java.util.Calendar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = SmsReceiver.class.getSimpleName();
    public static final String pdu_type = "pdus";

    final SmsManager sms = SmsManager.getDefault();


    @Override
    public void onReceive(Context context, Intent intent) {
//        Calendar time12 = get12AMTime();
        Calendar time6 = get6AMTime();
        Calendar calendar = Calendar.getInstance();
//        boolean isUpdated = MyPreferences.getBoolValue(context, AppConst.IS_MORNING_PARAMETERS_UPDATED);
        if (MyPreferences.getStringValue(context, "authkey").isEmpty() || !calendar.before(time6)) {
            MLog.e(TAG, "Message receive but cant call api");
            return;
        }

        Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();
                    long time = currentMessage.getTimestampMillis();
//                    MLog.showLongToast(context, message);
//                    Log.i("SmsReceiver", "senderNum: " + phoneNumber + "; message: " + message);
//                    Intent myIntent = new Intent("otp");
//                    myIntent.putExtra("message",message);
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(myIntent);
                    updateMorningPrice(context, phoneNumber, message, time);
                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);
        }
    }

    private Calendar get12AMTime() {
        Calendar cal = Calendar.getInstance();
        // set calendar to TODAY 21:00:00.000
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    private Calendar get6AMTime() {
        Calendar cal = Calendar.getInstance();
        // set calendar to TODAY 21:00:00.000
        cal.set(Calendar.HOUR_OF_DAY, 6);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }



    private void updateMorningPrice(Context context, String mobile, String msg, long time) {
        MLog.e("updateMorningPrice", msg);
        if (!MyUtils.hasInternetConnection(context)) {
            MLog.showToast(context, AppConst.NO_INTERNET_MSG);
            return;
        }
        SMSModel model = new SMSModel(mobile, msg, time);
        String authkey = MyPreferences.getStringValue(context, "authkey");
        Call<ResponseBody> responce = APIClient.getApiService().morningPriceUpdateReadingMsg(authkey, model);
        responce.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject res = new JSONObject(response.body().string());
                        MLog.showLongToast(context, res.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }
    public static class SMSModel {
        public String sender;
        public String message;
        public long time;

        public SMSModel(String sender, String message, long time) {
            this.sender = sender;
            this.message = message;
            this.time = time;
        }
    }
}
