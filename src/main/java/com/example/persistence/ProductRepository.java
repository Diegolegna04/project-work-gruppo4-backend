package com.example.persistence;

import com.example.persistence.model.Product;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;
import java.math.BigDecimal;
import java.util.List;


@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

    // Get all the products
    public List<Product> getAllProducts(){
        return find("from Product").list();
    }

    // Get all the products that the user can see
    public List<Product> getAllProductsForUsers(){
        return find("from Product where showToUser = true").list();
    }

    public boolean productExists(String name, BigDecimal price, String ingredientListId, String category) {
        return find("name = ?1 and price = ?2 and ingredientListId = ?3 and category = ?4",
                name, price, ingredientListId, category).firstResultOptional().isPresent();
    }
}
