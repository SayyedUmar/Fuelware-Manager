package com.fuelware.app.fw_manager.models;

public class CreditCustomer {

    private String business;
    private String id;
    private String first_name;
    private String last_name;
    private String mobile;
    private boolean has_blacklisted;
    private boolean verify_credit_limit;

    public boolean isVerify_credit_limit() {
        return verify_credit_limit;
    }

    public void setVerify_credit_limit(boolean verify_credit_limit) {
        this.verify_credit_limit = verify_credit_limit;
    }

    public CreditCustomer() {}

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirst_name() {
        if (first_name == null)
            return "";
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        if (last_name == null)
            return "";
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public boolean isHas_blacklisted() {
        return has_blacklisted;
    }

    public void setHas_blacklisted(boolean has_blacklisted) {
        this.has_blacklisted = has_blacklisted;
    }
}
