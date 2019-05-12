package com.fuelware.app.fw_manager.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ReceiptModel implements Parcelable {

    private String id;

    private String customer_id;
    private String name;
    private String mobile;
    private String business;
    private String email;

    private String paid_by;
    private String amount;
    private String receipt_number;
    private String remark;
    private String created_date;
    private boolean auto_receipt;
    private String token;

    // by cheque receipt model
    private String bank;
    private String branch;
    private String cheque_date;
    private String cheque_number;

    // for E-wallet
    private String transaction_number;
    private String ewallet;

    // for Online Payment
    //private String transaction_number;
    private String transaction_date;

    public String getTransaction_date() {
        return transaction_date;
    }

    public void setTransaction_date(String transaction_date) {
        this.transaction_date = transaction_date;
    }

    public String getTransaction_number() {
        return transaction_number;
    }

    public void setTransaction_number(String transaction_number) {
        this.transaction_number = transaction_number;
    }

    public String getEwallet() {
        return ewallet;
    }

    public void setEwallet(String ewallet) {
        this.ewallet = ewallet;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCheque_date() {
        return cheque_date;
    }

    public void setCheque_date(String cheque_date) {
        this.cheque_date = cheque_date;
    }

    public String getCheque_number() {
        return cheque_number;
    }

    public void setCheque_number(String cheque_number) {
        this.cheque_number = cheque_number;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAuto_receipt() {
        return auto_receipt;
    }

    public void setAuto_receipt(boolean auto_receipt) {
        this.auto_receipt = auto_receipt;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
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



    public ReceiptModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.customer_id);
        dest.writeString(this.name);
        dest.writeString(this.mobile);
        dest.writeString(this.business);
        dest.writeString(this.email);
        dest.writeString(this.paid_by);
        dest.writeString(this.amount);
        dest.writeString(this.receipt_number);
        dest.writeString(this.remark);
        dest.writeString(this.created_date);
        dest.writeByte(this.auto_receipt ? (byte) 1 : (byte) 0);
        dest.writeString(this.token);
        dest.writeString(this.bank);
        dest.writeString(this.branch);
        dest.writeString(this.cheque_date);
        dest.writeString(this.cheque_number);
        dest.writeString(this.transaction_number);
        dest.writeString(this.ewallet);
        dest.writeString(this.transaction_date);
    }

    protected ReceiptModel(Parcel in) {
        this.id = in.readString();
        this.customer_id = in.readString();
        this.name = in.readString();
        this.mobile = in.readString();
        this.business = in.readString();
        this.email = in.readString();
        this.paid_by = in.readString();
        this.amount = in.readString();
        this.receipt_number = in.readString();
        this.remark = in.readString();
        this.created_date = in.readString();
        this.auto_receipt = in.readByte() != 0;
        this.token = in.readString();
        this.bank = in.readString();
        this.branch = in.readString();
        this.cheque_date = in.readString();
        this.cheque_number = in.readString();
        this.transaction_number = in.readString();
        this.ewallet = in.readString();
        this.transaction_date = in.readString();
    }

    public static final Creator<ReceiptModel> CREATOR = new Creator<ReceiptModel>() {
        @Override
        public ReceiptModel createFromParcel(Parcel source) {
            return new ReceiptModel(source);
        }

        @Override
        public ReceiptModel[] newArray(int size) {
            return new ReceiptModel[size];
        }
    };
}
