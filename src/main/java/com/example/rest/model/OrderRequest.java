package com.example.rest.model;

import com.example.persistence.model.Order;

import java.util.Date;
import java.util.List;

public class OrderRequest {
    public List<Order.ProductItem> products;
    public Date pickupDate;

    public OrderRequest(){

    }
    public OrderRequest(List<Order.ProductItem> products, Date pickupDate){
        this.products = products;
        this.pickupDate = pickupDate;
    }


    public List<Order.ProductItem> getProducts() {
        return products;
    }

    public void setProducts(List<Order.ProductItem> products) {
        this.products = products;
    }

    public Date getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(Date pickupDate) {
        this.pickupDate = pickupDate;
    }
}
