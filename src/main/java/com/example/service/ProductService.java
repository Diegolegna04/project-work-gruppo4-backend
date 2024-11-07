package com.example.service;

import com.example.persistence.model.Product;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import java.util.List;


@ApplicationScoped
public class ProductService implements PanacheRepository<Product> {

//    {
//            "name": "Millefoglie",
//            "description": "Una torta multistrati con crema alla vaniglia",
//            "price": 29.99,
//            "quantity": 11,
//            "category": "Torta",
//            "image": "/path_to_image",
//            "showToUser": true
//    }
    @Transactional
    public Response addProduct(Product product) {
        Product newProduct = new Product();
        newProduct.setName(product.getName());
        newProduct.setDescription(product.getDescription());
        newProduct.setPrice(product.getPrice());
        newProduct.setQuantity(product.getQuantity());
        newProduct.setCategory(product.getCategory());
        newProduct.setImage(product.getImage());
        newProduct.setShowToUser(product.getShowToUser());
        try {
            persist(newProduct);
            System.out.println("Prodotto aggiunto");
            return Response.ok("Il prodotto è stato aggiunto correttamente").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }

}
