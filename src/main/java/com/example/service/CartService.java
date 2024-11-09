package com.example.service;

import com.example.persistence.CartRepository;
import com.example.persistence.model.Cart;
import com.example.persistence.model.Order;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class CartService implements PanacheMongoRepository<Cart> {

    private final CartRepository repository;

    public CartService(CartRepository repository) {
        this.repository = repository;
    }


    // Create an empty cart when the user is verified and can now login
    public void createNewEmptyCart(Integer idUtente) {
        Cart cart = new Cart();
        cart.idUser = idUtente;
        cart.price = 0;
        persist(cart);
    }

    // Add a productItem (product_id and quantity) to the cart
    public Response addProductToCart(Integer idUtente, Order.ProductItem product){
        return repository.addProductToCart(idUtente, product);
    }
}
