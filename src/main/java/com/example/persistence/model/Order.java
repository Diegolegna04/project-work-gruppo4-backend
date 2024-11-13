package com.example.persistence.model;

import com.example.rest.model.OrderRequest;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
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
    public BigDecimal price;
    @BsonProperty("order_date")
    public Date orderDate;
    @BsonProperty("pickup_date_time")
    public OrderRequest pickupDateTime;
    @BsonProperty("notes")
    public String notes;

    public static class ProductItem {
        @BsonProperty("id_product")
        public Integer idProduct;

        @BsonProperty("quantity")
        public Integer quantity;
    }


    public Order() {

    }
    public Order(ObjectId id, List<ProductItem> products, String email, String phone, String status, BigDecimal price, Date orderDate, OrderRequest pickupDateTime, String notes) {
        this.id = id;
        this.products = products;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.price = price;
        this.orderDate = orderDate;
        this.pickupDateTime = pickupDateTime;
        this.notes = notes;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public OrderRequest getPickupDateTime() {
        return pickupDateTime;
    }

    public void setPickupDateTime(OrderRequest pickupDateTime) {
        this.pickupDateTime = pickupDateTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
