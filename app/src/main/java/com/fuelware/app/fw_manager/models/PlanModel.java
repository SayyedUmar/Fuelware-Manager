package com.fuelware.app.fw_manager.models;

public class PlanModel {

    private String id;
    private String plan_type;
    private String formatted_plan_type;
    private String plan_name;
    private long indents;
    private long duration;
    private String duration_description;
    private String type;
    private String price; // base price
    private String gst;
    private String total; // price after gst
    private String final_price; // price after removing decimal
    public boolean new_user;
    public String registration_fees;

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlan_type() {
        return plan_type;
    }

    public void setPlan_type(String plan_type) {
        this.plan_type = plan_type;
    }

    public String getFormatted_plan_type() {
        return formatted_plan_type;
    }

    public void setFormatted_plan_type(String formatted_plan_type) {
        this.formatted_plan_type = formatted_plan_type;
    }

    public String getPlan_name() {
        return plan_name;
    }

    public void setPlan_name(String plan_name) {
        this.plan_name = plan_name;
    }

    public long getIndents() {
        return indents;
    }

    public void setIndents(long indents) {
        this.indents = indents;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getDuration_description() {
        return duration_description;
    }

    public void setDuration_description(String duration_description) {
        this.duration_description = duration_description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getGst() {
        return gst;
    }

    public void setGst(String gst) {
        this.gst = gst;
    }

    public String getFinal_price() {
        return final_price;
    }

    public void setFinal_price(String final_price) {
        this.final_price = final_price;
    }
}
