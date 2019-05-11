package com.fuelware.app.fw_manager.models;

public class CashReceiptModel {

    private String id;

    private String customer_id;
    private String cName;
    private String cMobile;
    private String cBusiness;
    private String cEmail;

    private String paid_by;
    private String amount;
    private String receipt_number;
    private String remark;
    private String created_date;
    private boolean auto_receipt;
    private String token;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

    public String getcEmail() {
        return cEmail;
    }

    public void setcEmail(String cEmail) {
        this.cEmail = cEmail;
    }

    public boolean isAuto_receipt() {
        return auto_receipt;
    }

    public void setAuto_receipt(boolean auto_receipt) {
        this.auto_receipt = auto_receipt;
    }

    public String getcMobile() {
        return cMobile;
    }

    public void setcMobile(String cMobile) {
        this.cMobile = cMobile;
    }

    public String getcBusiness() {
        return cBusiness;
    }

    public void setcBusiness(String cBusiness) {
        this.cBusiness = cBusiness;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getPaid_by() {
        return paid_by;
    }

    public void setPaid_by(String paid_by) {
        this.paid_by = paid_by;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getReceipt_number() {
        return receipt_number;
    }

    public void setReceipt_number(String receipt_number) {
        this.receipt_number = receipt_number;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }
}
