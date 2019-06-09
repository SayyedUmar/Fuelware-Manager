package com.fuelware.app.fw_manager.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fuelware.app.fw_manager.appconst.AppConst;
import com.fuelware.app.fw_manager.utils.MyUtils;
import com.google.gson.annotations.SerializedName;

public class AccountModel implements Parcelable {

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("vehicle_number")
    private String vehicleNum;

    @SerializedName("indent_number")
    private String indentNum;

    @SerializedName("show_date")
    private String fillingDate;

    private String type;
    private String amount;

    @SerializedName("formatted_balance")
    private String balance;

    private String receipt_number;

    public String vehicle_km;
    public String product;
    public String rate;
    public String litre;
    public String remark;
    public String invoice_number;
    public String business;
    public String customer_id;
    public String batch_number;
    public String snap_bill_url;
    public String indent_type;

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getVehicleNum() {
        return vehicleNum;
    }

    public void setVehicleNum(String vehicleNum) {
        this.vehicleNum = vehicleNum;
    }

    public String getIndentNum() {
        return indentNum;
    }

    public void setIndentNum(String indentNum) {
        this.indentNum = indentNum;
    }

    public String getFillingDate() {
        return fillingDate;
    }

    public void setFillingDate(String fillingDate) {
        this.fillingDate = fillingDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getReceipt_number() {
        return receipt_number;
    }

    public void setReceipt_number(String receipt_number) {
        this.receipt_number = receipt_number;
    }

    public String getVehicle_km() {
        return vehicle_km;
    }

    public void setVehicle_km(String vehicle_km) {
        this.vehicle_km = vehicle_km;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getLitre() {
        return litre;
    }

    public void setLitre(String litre) {
        this.litre = litre;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getInvoice_number() {
        return invoice_number;
    }

    public void setInvoice_number(String invoice_number) {
        this.invoice_number = invoice_number;
    }

    public String getFormattedFillDate () {
        return MyUtils.dateToString(AppConst.SERVER_DATE_FORMAT, AppConst.APP_DATE_FORMAT, fillingDate);
    }

    public AccountModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.updatedAt);
        dest.writeString(this.vehicleNum);
        dest.writeString(this.indentNum);
        dest.writeString(this.fillingDate);
        dest.writeString(this.type);
        dest.writeString(this.amount);
        dest.writeString(this.balance);
        dest.writeString(this.receipt_number);
        dest.writeString(this.vehicle_km);
        dest.writeString(this.product);
        dest.writeString(this.rate);
        dest.writeString(this.litre);
        dest.writeString(this.remark);
        dest.writeString(this.invoice_number);
        dest.writeString(this.business);
        dest.writeString(this.customer_id);
        dest.writeString(this.batch_number);
        dest.writeString(this.snap_bill_url);
        dest.writeString(this.indent_type);
    }

    protected AccountModel(Parcel in) {
        this.updatedAt = in.readString();
        this.vehicleNum = in.readString();
        this.indentNum = in.readString();
        this.fillingDate = in.readString();
        this.type = in.readString();
        this.amount = in.readString();
        this.balance = in.readString();
        this.receipt_number = in.readString();
        this.vehicle_km = in.readString();
        this.product = in.readString();
        this.rate = in.readString();
        this.litre = in.readString();
        this.remark = in.readString();
        this.invoice_number = in.readString();
        this.business = in.readString();
        this.customer_id = in.readString();
        this.batch_number = in.readString();
        this.snap_bill_url = in.readString();
        this.indent_type = in.readString();
    }

    public static final Creator<AccountModel> CREATOR = new Creator<AccountModel>() {
        @Override
        public AccountModel createFromParcel(Parcel source) {
            return new AccountModel(source);
        }

        @Override
        public AccountModel[] newArray(int size) {
            return new AccountModel[size];
        }
    };
}