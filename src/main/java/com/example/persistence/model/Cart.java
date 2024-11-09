package com.example.persistence.model;

import com.example.persistence.model.Order;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@MongoEntity(collection = "cart")
public class Cart extends PanacheMongoEntity {
    @BsonProperty("_id")
    public ObjectId id;
    @BsonProperty("id_user")
    public Integer idUser;
    @BsonProperty("products")
    public List<Order.ProductItem> products = new ArrayList<>();
    @BsonProperty("price")
    public float price;

    public Cart() {

    }
    public Cart(ObjectId id, Integer idUser, List<Order.ProductItem> products, float price) {
        this.id = id;
        this.idUser = idUser;
        this.products = products;
        this.price = price;
    }


    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public List<Order.ProductItem> getProducts() {
        return products;
    }

    public void setProducts(List<Order.ProductItem> products) {
        this.products = products;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}