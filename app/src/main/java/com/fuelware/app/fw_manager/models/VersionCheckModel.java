package com.fuelware.app.fw_manager.models;

import com.fuelware.app.fw_manager.appconst.AppConst;

public class VersionCheckModel {

    private String app_type = AppConst.APP_TYPE;
    private int app_version_code = AppConst.APP_SERVER_VERSION;
    private int os_version = android.os.Build.VERSION.SDK_INT;

}
