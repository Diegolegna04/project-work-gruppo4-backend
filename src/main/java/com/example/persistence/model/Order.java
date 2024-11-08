package com.example.persistence.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import java.util.Date;
import java.util.List;

@MongoEntity(collection = "order")
public class Order extends PanacheMongoEntity {
    @BsonProperty("_id")
    public ObjectId id;
    @BsonProperty("products")
    public List<ProductItem> products;
    @BsonProperty("email")
    public String email;
    @BsonProperty("phone")
    public String phone;
    @BsonProperty("status")
    public String status;
    @BsonProperty("price")
    public Double price;
    @BsonProperty("order_date")
    public Date orderDate;
    @BsonProperty("pickup_date")
    public Date pickupDate;

    public static class ProductItem {
        @BsonProperty("id_product")
        public Integer idProduct;

        @BsonProperty("quantity")
        public Integer quantity;
    }


    public Order() {

    }
    public Order(ObjectId id, List<ProductItem> products, String email, String phone, String status, Double price, Date orderDate, Date pickupDate) {
        this.id = id;
        this.products = products;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.price = price;
        this.orderDate = orderDate;
        this.pickupDate = pickupDate;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public List<ProductItem> getProducts() {
        return products;
    }

    public void setProducts(List<ProductItem> products) {
        this.products = products;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(Date pickupDate) {
        this.pickupDate = pickupDate;
    }
}