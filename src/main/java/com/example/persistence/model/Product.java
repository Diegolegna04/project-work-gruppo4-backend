package com.example.persistence.model;

import jakarta.persistence.*;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "price", nullable = false)
    private BigDecimal price;
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    @Column(name = "ingredient_list_id", length = 50)
    private String ingredientListId;
    @Column(name = "category", length = 100, nullable = false)
    private String category;
    @Column(name = "image", length = 200)
    private String image;
    @Column(name = "show_to_user", nullable = false)
    private Boolean showToUser = true;


    public Product() {

    }
    public Product(Integer id, String name, String description, BigDecimal price, Integer quantity, String ingredientListId, String category, String image, Boolean showToUser) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.ingredientListId = ingredientListId;
        this.category = category;
        this.image = image;
        this.showToUser = showToUser;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getIngredientListId() {
        return ingredientListId;
    }

    public void setIngredientListId(String ingredientListId) {
        this.ingredientListId = ingredientListId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean getShowToUser() {
        return showToUser;
    }

    public void setShowToUser(Boolean showToUser) {
        this.showToUser = showToUser;
    }
}
