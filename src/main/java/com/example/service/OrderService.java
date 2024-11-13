package com.example.service;

import com.example.persistence.ProductRepository;
import com.example.persistence.model.Cart;
import com.example.persistence.model.Order;
import com.example.persistence.model.Product;
import com.example.persistence.model.Utente;
import com.example.rest.model.OrderDateRequest;
import com.example.service.exception.ProductNotAvailable;
import com.example.service.exception.QuantityNotAvailable;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static io.quarkus.arc.impl.UncaughtExceptions.LOGGER;


@ApplicationScoped
public class OrderService implements PanacheMongoRepository<Order> {

    private final ProductRepository productRepository;
    private final CartService cartService;

    public OrderService(ProductRepository productRepository, CartService cartService) throws QuantityNotAvailable {
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    @Transactional
    public Response makeAOrder(Utente user, OrderDateRequest pickupDateTime) {
        Cart userCart = cartService.find("idUser", user.getId()).firstResult();
        if (userCart.products == null || userCart.products.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("L'ordine non contiene alcun prodotto.")
                    .build();
        }

        Order newOrder = new Order();
        newOrder.products = userCart.products;
        if (isValidEmail(user.getEmail())) {
            newOrder.email = user.getEmail();
        } else {
            newOrder.phone = user.getTelefono();
        }
        newOrder.status = "Pending";
        newOrder.price = userCart.price;
        newOrder.orderDate = new Date();
        newOrder.pickupDateTime = pickupDateTime;
        try {
            persist(newOrder);
            modifyProductQuantity(userCart.products);
            cartService.clearCart(userCart.id);
            return Response.status(Response.Status.CREATED)
                    .entity("Ordine creato con successo")
                    .build();
        }catch (QuantityNotAvailable e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Quantità non disponibile per uno o più prodotti.")
                    .build();
        }
        catch (Exception e) {
            LOGGER.error("Errore durante la creazione dell'ordine: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore durante la creazione dell'ordine.")
                    .build();
        }
    }

    public List<Order> getAllOrders(){
        return listAll();
    }

    public List<Order> getAllUserOrders(String contact) {
        if (isValidEmail(contact)) {
            return find("email", contact).list();
        } else {
            return find("phone", contact).list();
        }

    }




    private void modifyProductQuantity(List<Order.ProductItem> products) throws QuantityNotAvailable, ProductNotAvailable {
        for (Order.ProductItem item : products){
            Product foundProduct = productRepository.findById(Long.valueOf(item.idProduct));

            if (foundProduct != null) {
                // Calculate and set the new product quantity
                foundProduct.setQuantity(calculateProductNewQuantity(foundProduct.getQuantity(), item.quantity));
                // Persist the modified product
                productRepository.persist(foundProduct);
            } else {
                throw new ProductNotAvailable();
            }
        }
    }
    private Integer calculateProductNewQuantity(Integer oldQuantity, Integer productQuantity) throws QuantityNotAvailable {
        if (oldQuantity < productQuantity){
            throw new QuantityNotAvailable();
        }
        return oldQuantity - productQuantity;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email != null && email.matches(emailRegex);
    }

    // TODO: remove method
    private BigDecimal calculateOrderPrice(List<Order.ProductItem> products) {
        BigDecimal total = BigDecimal.ZERO;
        for (Order.ProductItem orderProduct : products) {
            int quantity = orderProduct.quantity;
            Product product = productRepository.findById(Long.valueOf(orderProduct.idProduct));
            if (product == null) {
                throw new IllegalArgumentException("Prodotto con ID " + orderProduct.idProduct + " non trovato");
            }
            BigDecimal productPrice = product.getPrice();
            BigDecimal quantityBD = BigDecimal.valueOf(quantity);
            BigDecimal productTotal = productPrice.multiply(quantityBD); // Usa multiply per la moltiplicazione

            // Aggiungi al totale
            total = total.add(productTotal);
        }
        return total;
    }
}
