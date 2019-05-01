package com.fuelware.app.fw_manager.models;

import java.io.Serializable;

public class IndentModel implements Serializable {
    private int id;
    private String business;
    private String customer_id;
    private String formatted_user_id;
    private String customer_name;
    private String indent_number;
    private String vehicle_number;
    private String product;
    private String status;
    private String approve_status;


    private String mobile;
    private String contact;
    private String driver;
    private String driver_mobile;
    private String fill_type;
    private String date_of_indent;
    private String fill_date;
    private String price;
    private String litre;
    private String amount;
    private String invoice_id;
    private String call_type;
    private long meter_reading;

    private String user_id,product_id;
    private boolean has_blacklisted;
    public boolean is_manual_invoice;

    private boolean verify_credit_limit;

    public ProductPriceModel product_detail;

    public boolean isVerify_credit_limit() {
        return verify_credit_limit;
    }

    public void setVerify_credit_limit(boolean verify_credit_limit) {
        this.verify_credit_limit = verify_credit_limit;
    }

    public String getFormatted_user_id() {
        return formatted_user_id;
    }

    public void setFormatted_user_id(String formatted_user_id) {
        this.formatted_user_id = formatted_user_id;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getIndent_number() {
        return indent_number;
    }

    public void setIndent_number(String indent_number) {
        this.indent_number = indent_number;
    }

    public String getVehicle_number() {
        return vehicle_number;
    }

    public void setVehicle_number(String vehicle_number) {
        this.vehicle_number = vehicle_number;
    }


    //-   -- - - - - - - - - - -- - - - - - - - - -  --


    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getDriver() {
        if (driver == null) return "";
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getDriver_mobile() {
        if (driver_mobile == null) return "";
        return driver_mobile;
    }

    public void setDriver_mobile(String driver_mobile) {
        this.driver_mobile = driver_mobile;
    }

    public String getFill_type() {
        return fill_type;
    }

    public void setFill_type(String fill_type) {
        this.fill_type = fill_type;
    }

    public String getFill_date() {
        return fill_date;
    }

    public void setFill_date(String fill_date) {
        this.fill_date = fill_date;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLitre() {
        return litre;
    }

    public void setLitre(String litre) {
        this.litre = litre;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getInvoice_id() {
        return invoice_id;
    }

    public void setInvoice_id(String invoice_id) {
        this.invoice_id = invoice_id;
    }

    public long getMeter_reading() {
        return meter_reading;
    }

    public void setMeter_reading(long meter_reading) {
        this.meter_reading = meter_reading;
    }

    public String getContact() {
        if (contact == null)
            return "NA";
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getDate_of_indent() {
        return date_of_indent;
    }

    public void setDate_of_indent(String date_of_indent) {
        this.date_of_indent = date_of_indent;
    }

    public String getCall_type() {
        return call_type;
    }

    public void setCall_type(String call_type) {
        this.call_type = call_type;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getApprove_status() {
        if (approve_status == null)
            approve_status = "";
        return approve_status;
    }

    public void setApprove_status(String approve_status) {
        this.approve_status = approve_status;
    }

    public boolean isHas_blacklisted() {
        return has_blacklisted;
    }

    public void setHas_blacklisted(boolean has_blacklisted) {
        this.has_blacklisted = has_blacklisted;
    }
}
