package com.fuelware.app.fw_manager.models;

import java.io.Serializable;
import java.util.List;
public class Cashier implements Serializable {

    private long id;
    private long cashier_id;
    private String first_name;
    private String last_name;
    private String batch_number;
    private List<M_Indent> manual_indent;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCashier_id() {
        return cashier_id;
    }

    public void setCashier_id(long cashier_id) {
        this.cashier_id = cashier_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getBatch_number() {
        return batch_number;
    }

    public void setBatch_number(String batch_number) {
        this.batch_number = batch_number;
    }

    public List<M_Indent> getManual_indent() {
        return manual_indent;
    }

    public void setManual_indent(List<M_Indent> manual_indent) {
        this.manual_indent = manual_indent;
    }

}


/*

public class Cashier implements Parcelable {

    private long id;
    private long cashier_id;
    private String first_name;
    private String last_name;
    private String batch_number;
    private List<M_Indent> manual_indent;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCashier_id() {
        return cashier_id;
    }

    public void setCashier_id(long cashier_id) {
        this.cashier_id = cashier_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getBatch_number() {
        return batch_number;
    }

    public void setBatch_number(String batch_number) {
        this.batch_number = batch_number;
    }

    public List<M_Indent> getManual_indent() {
        return manual_indent;
    }

    public void setManual_indent(List<M_Indent> manual_indent) {
        this.manual_indent = manual_indent;
    }


    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Cashier createFromParcel(Parcel in) {
            return new Cashier(in);
        }

        public Cashier[] newArray(int size) {
            return new Cashier[size];
        }
    };

    public Cashier(Parcel in){
        this.id = in.readLong();
        this.cashier_id = in.readLong();
        this.first_name =  in.readString();
        this.last_name =  in.readString();
        this.batch_number =  in.readString();
        manual_indent = new ArrayList<>();
        in.readTypedList(manual_indent, M_Indent.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.cashier_id);
        dest.writeString(this.first_name);
        dest.writeString(this.last_name);
        dest.writeString(this.batch_number);
        dest.writeList(this.manual_indent);
    }
}

class M_Indent implements Parcelable {
    long cashier_batch_id;
    long total;

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Cashier createFromParcel(Parcel in) {
            return new Cashier(in);
        }

        public Cashier[] newArray(int size) {
            return new Cashier[size];
        }
    };

    public M_Indent(Parcel in){
        this.total = in.readLong();
        this.cashier_batch_id = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.total);
        dest.writeLong(this.cashier_batch_id);
    }
}
*/
