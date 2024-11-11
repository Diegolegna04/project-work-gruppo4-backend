package com.example.service;

import com.example.persistence.model.IngredientList;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class IngredientListService implements PanacheMongoRepository<IngredientList> {

    @Transactional
    public ObjectId createIngredientList(List<String> ingredientList) {
        // Sort the ingredient list
        List<String> sortedIngredientList = new ArrayList<>(ingredientList);
        Collections.sort(sortedIngredientList);

        // If there is the same list in the db return its _id
        ObjectId existingListId = findIngredientList(sortedIngredientList);
        if (existingListId != null) {
            return existingListId;

        }
        IngredientList newIngredientList = new IngredientList();
        newIngredientList.setIngredients(sortedIngredientList);
        persist(newIngredientList);
        return newIngredientList.getId();
    }

    @Transactional
    public ObjectId findIngredientList(List<String> ingredientList){
        IngredientList foundIngredientList = find("ingredients", ingredientList).firstResult();
        if (foundIngredientList != null){
            return foundIngredientList.getId();
        }else {
            return null;
        }
    }
}
