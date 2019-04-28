package com.fuelware.app.fw_manager.models;

import org.json.JSONObject;

public class User {

    private long id;
    private String first_name;
    private String last_name;
    private String email;
    private long mobile;
    private String formatted_role;
    private String notification_token;
    private String outlet_token;
    private JSONObject outlet;
}
