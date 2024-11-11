package com.example.service;

import com.example.persistence.ProductRepository;
import com.example.persistence.model.Order;
import com.example.persistence.model.Product;
import com.example.rest.model.OrderRequest;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static io.quarkus.arc.impl.UncaughtExceptions.LOGGER;


@ApplicationScoped
public class OrderService implements PanacheMongoRepository<Order> {

    private final ProductRepository productRepository;

    public OrderService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Response makeAOrder(String contact, OrderRequest order) {
        if (order.products == null || order.products.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("L'ordine non contiene alcun prodotto.")
                    .build();
        }
        Order newOrder = new Order();
        newOrder.products = order.products;
        if (isValidEmail(contact)) {
            newOrder.email = contact;
        } else {
            newOrder.phone = contact;
        }
        newOrder.status = "Pending";
        newOrder.price = calculateOrderPrice(order.products);
        newOrder.orderDate = new Date();
        newOrder.pickupDate = order.pickupDate;
        try {
            persist(newOrder);
            return Response.status(Response.Status.CREATED)
                    .entity("Ordine creato con successo")
                    .build();
        } catch (Exception e) {
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




    private boolean isValidEmail(String email) {
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email != null && email.matches(emailRegex);
    }

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
