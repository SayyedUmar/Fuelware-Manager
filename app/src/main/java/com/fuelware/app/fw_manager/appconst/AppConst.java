package com.fuelware.app.fw_manager.appconst;

import com.fuelware.app.fw_manager.BuildConfig;

/**
 * Created by Zest Developer on 7/28/2017.
 */

public class AppConst {

    public static final int APP_SERVER_VERSION = BuildConfig.VERSION_CODE;

    public static final String ROOT_URL = BuildConfig.BASE_URL;

    public static final String ERROR_MSG = "Something went wrong! Try again later.";
    public static final String NO_INTERNET_MSG = "Please check your internet connection!";

    public static final String DEVICE_TYPE = "android";
    public static final String APP_TYPE = "manager";

    public static final String FULL_VERSION_MSG = "This is feature is available only in Full version";

    public static final String PRFS_PRODUCT_TYPES = "PRFS_PRODUCT_TYPES";

    public static final String INTENT_EXTRA = "INTENT_EXTRA";
    public static final String LOGIN_STATUS = "LOGIN_STATUS";
    public static final float MAX_LITRE = 5000;
    public static final float MAX_AMOUNT = 500000;
    public static final String USER_ACCOUNT_BLACKLIST = "USER_ACCOUNT_BLACKLIST";
    public static final String KNOWLEDGE_CENTER_URL = "http://fuelware.in/knowledge-center.html";
    public static final String USER_PROFILE_DATA = "USER_PROFILE_DATA";
    public static final String MORNING_PARAMETERS_STATUS = "MORNING_PARAMETERS_STATUS";
    public static final String content_type = "application/json";


    public static final String SERVER_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SERVER_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String APP_DATE_FORMAT = "dd MMM, yy";
    public static final String APP_DATE_TIME_FORMAT = "dd MMM, yy hh:mm a";
    public static final String PRODUCTS_LIST = "PRODUCTS_LIST";
    public static final String CASHIER_DETAILS = "CASHIER_DETAILS";

    public static final String full_tank = "full_tank";
    public static final String litre = "litre";
    public static final String amount = "amount";
}
