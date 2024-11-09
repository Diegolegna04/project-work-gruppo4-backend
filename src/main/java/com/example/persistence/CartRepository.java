package com.example.persistence;

import com.example.persistence.model.Cart;
import com.example.persistence.model.Order;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import java.util.List;

@ApplicationScoped
public class CartRepository implements PanacheMongoRepository<Cart> {

    public Response addProductToCart(Integer idUtente, Order.ProductItem product){
        Cart cart = find("idUser", idUtente).firstResult();
        if (cart == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Carrello non trovato per l'utente con ID " + idUtente)
                    .build();
        }

        // Does the product the user want to add already exist in the cart?
        boolean productExistsInCart = false;
        for (Order.ProductItem existingProduct : cart.products) {
            if (existingProduct.idProduct.equals(product.idProduct)) {
                existingProduct.quantity += product.quantity;
                productExistsInCart = true;
                break;
            }
        }

        // If it doesn't exist
        if (!productExistsInCart) {
            Order.ProductItem newProduct = new Order.ProductItem();
            newProduct.idProduct = product.idProduct;
            newProduct.quantity = product.quantity;
            cart.products.add(newProduct);
        }

        update(cart);
        return Response.ok().entity("Prodotto aggiunto al carrello").build();
    }
}
