package com.example.persistence.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@MongoEntity(collection = "ingredientList")
public class IngredientList extends PanacheMongoEntity {
    @BsonProperty("ingredients")
    public List<String> ingredients = new ArrayList<>();


    public IngredientList(){

    }
    public IngredientList(List<String> ingredients) {
        this.ingredients = ingredients;
    }


    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }
}
