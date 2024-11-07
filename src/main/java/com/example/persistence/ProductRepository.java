package com.example.persistence;

import com.example.persistence.model.Product;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

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
}
