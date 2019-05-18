package com.fuelware.app.fw_manager.models;

import java.io.Serializable;

public class ProductPriceModel implements Serializable {

    private String id;
    private String product;
    private String product_id;
    private double price;



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
