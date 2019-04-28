package com.fuelware.app.fw_manager.models;

public class CounterBillPojo {
    private String vehicle_number,email,mobile,v_meter;
    private Integer product_id;
    private Double amount,quantity;

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setV_meter(String v_meter) {
        this.v_meter = v_meter;
    }

    public void setVehicle_number(String vehicle_number) {
        this.vehicle_number = vehicle_number;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }


    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }
}
