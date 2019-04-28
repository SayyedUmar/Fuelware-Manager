package com.fuelware.app.fw_manager.models;

import java.util.List;

public class Cashier {

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

class M_Indent {
    long cashier_batch_id;
    long total;
}