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
        // Check if the product quantity is zero
        if (product.quantity == 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("La quantità del prodotto non può essere zero.")
                    .build();
        }

        Cart cart = find("idUser", idUtente).firstResult();
        if (cart == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Carrello non trovato per l'utente con ID " + idUtente)
                    .build();
        }

        // Check if the product already exists in the cart to update the quantity
        boolean productExistsInCart = false;
        for (Order.ProductItem existingProduct : cart.products) {
            if (existingProduct.idProduct.equals(product.idProduct)) {
                // Update the quantity, ensuring it does not become negative
                int newQuantity = existingProduct.quantity + product.quantity;
                if (newQuantity < 0) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("La quantità finale del prodotto nel carrello non può essere negativa.")
                            .build();
                }
                existingProduct.quantity = newQuantity;
                productExistsInCart = true;

                if (newQuantity == 0){
                    cart.products.remove(existingProduct);
                }
                break;
            }
        }


        // If it doesn't exist, add the new product item, but only if the quantity is positive
        if (!productExistsInCart) {
            if (product.quantity < 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Non puoi aggiungere una quantità negativa di un nuovo prodotto.")
                        .build();
            }
            Order.ProductItem newProduct = new Order.ProductItem();
            newProduct.idProduct = product.idProduct;
            newProduct.quantity = product.quantity;
            cart.products.add(newProduct);
        }

        // Calculate the new price
        cart.price = calculateNewPrice(cart.price, product.idProduct, product.quantity);

        // Update the cart and return success response
        update(cart);
        return Response.ok().entity("Prodotto aggiornato nel carrello").build();
    }

    public Response deleteProductFromCart(Integer id, Integer idProduct) {
        Cart cart = find("idUser", id).firstResult();
        if (cart == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Carrello non trovato per l'utente con ID " + id)
                    .build();
        }

        boolean productExistsInCart = false;
        for (Order.ProductItem existingProduct : cart.products) {
            if (existingProduct.idProduct.equals(idProduct)) {
                cart.products.remove(existingProduct);
                cart.price = calculateNewPrice(cart.price, idProduct, -existingProduct.quantity);
                productExistsInCart = true;
                break;
            }
        }

        if (!productExistsInCart) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Prodotto non trovato nel carrello")
                    .build();
        }

        // Update the cart and return success response
        update(cart);
        return Response.ok().entity("Prodotto rimosso dal carrello").build();
    }

    private BigDecimal calculateNewPrice(BigDecimal oldPrice, Integer productId, Integer quantity) {
        BigDecimal productPrice = productRepository.findById(Long.valueOf(productId)).getPrice();
        // oldPrice + (productPrice * quantity).
        return oldPrice.add(productPrice.multiply(BigDecimal.valueOf(quantity)));
    }
}
