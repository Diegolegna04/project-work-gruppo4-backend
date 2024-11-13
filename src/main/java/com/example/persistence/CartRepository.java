package com.example.persistence;

import com.example.persistence.model.Cart;
import com.example.persistence.model.Order;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;

@ApplicationScoped
public class CartRepository implements PanacheMongoRepository<Cart> {

    private final ProductRepository productRepository;

    public CartRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    public Response addProductToCart(Integer idUtente, Order.ProductItem product){
        Cart cart = find("idUser", idUtente).firstResult();
        if (cart == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Carrello non trovato per l'utente con ID " + idUtente)
                    .build();
        }

        // Does the product the user want to add already exist in the cart?
        // If yes just update the quantity
        boolean productExistsInCart = false;
        for (Order.ProductItem existingProduct : cart.products) {
            if (existingProduct.idProduct.equals(product.idProduct)) {
                existingProduct.quantity += product.quantity;
                productExistsInCart = true;
                break;
            }
        }

        // If it doesn't exist create a new one
        if (!productExistsInCart) {
            Order.ProductItem newProduct = new Order.ProductItem();
            newProduct.idProduct = product.idProduct;
            newProduct.quantity = product.quantity;
            cart.products.add(newProduct);
        }
        cart.price = calculateNewPrice(cart.price, product.idProduct, product.quantity);

        update(cart);
        return Response.ok().entity("Prodotto aggiunto al carrello").build();
    }

    private BigDecimal calculateNewPrice(BigDecimal oldPrice, Integer productId, Integer quantity) {
        BigDecimal productPrice = productRepository.findById(Long.valueOf(productId)).getPrice();
        // oldPrice + (productPrice * quantity).
        return oldPrice.add(productPrice.multiply(BigDecimal.valueOf(quantity)));
    }
}
