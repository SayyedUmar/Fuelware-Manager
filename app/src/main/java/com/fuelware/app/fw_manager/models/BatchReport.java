package com.fuelware.app.fw_manager.models;

import com.google.gson.annotations.SerializedName;

public class BatchReport {

    public String id;
    public String start_time;
    public String end_time;
    public String batch_number;
    public String actual_batch_total;
    public String earn_total;
    public String deducted_total;
    public String excess_less;

    @SerializedName("user")
    public User user;

    @SerializedName("produce")
    public Produce produce;

    public static class Produce {
        @SerializedName("income")
        public Income income;
    }
    public static class Income {
        @SerializedName("mode")
        public Mode mode;
    }
    public static class Mode {
        public String e_indent;
        public String manual_indent;
        /*String cash;
        String credit_card;
        String debit_card;
        String loyalty_card;*/
    }

    public static class User {
        @SerializedName("data")
        public Data data;
    }

    public static class Data {
        public String first_name;
        public String last_name;
    }
}


