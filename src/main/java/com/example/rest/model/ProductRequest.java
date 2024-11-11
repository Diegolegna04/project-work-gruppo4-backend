package com.example.rest.model;

import java.math.BigDecimal;
import java.util.List;

public class ProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private List<String> ingredientList;
    private String category;
    private String image;
    private Boolean showToUser = true;


    public ProductRequest() {

    }
    public ProductRequest(String name, String description, BigDecimal price, Integer quantity, List<String> ingredientList, String category, String image, Boolean showToUser) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.ingredientList = ingredientList;
        this.category = category;
        this.image = image;
        this.showToUser = showToUser;
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

    public List<String> getIngredientList() {
        return ingredientList;
    }

    public void setIngredientList(List<String> ingredientList) {
        this.ingredientList = ingredientList;
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
