package com.fuelware.app.fw_manager.models;

import com.google.gson.annotations.SerializedName;

public class PurchasedPlan {

    public String id;
    public String price;
    public String start_date;
    public String end_date;
    public int is_plan_activated;
    public long remaining_indents;

    public String subscription_detail;

    public transient SubscriptionDetail subscriptionDetail;

    public static class SubscriptionDetail {

        @SerializedName("plan")
        public Plan plan;


    }

    public static class Plan {
        public String plan_name;
        public String plan_type;
        public int has_sms;
        public int duration;
        public int indents;
        public int used_indents;
        public String price;
    }
}


