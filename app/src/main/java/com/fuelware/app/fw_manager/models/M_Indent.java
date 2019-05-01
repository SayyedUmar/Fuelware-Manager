package com.fuelware.app.fw_manager.models;

import android.os.Parcel;

import java.io.Serializable;

public class M_Indent implements Serializable {

    private long total;
    private long cashier_batch_id;

    public M_Indent(Parcel in){
        this.total = in.readLong();
        this.cashier_batch_id = in.readLong();
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getCashier_batch_id() {
        return cashier_batch_id;
    }

    public void setCashier_batch_id(long cashier_batch_id) {
        this.cashier_batch_id = cashier_batch_id;
    }
}
