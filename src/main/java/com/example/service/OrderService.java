package com.example.service;

import com.example.persistence.ProductRepository;
import com.example.persistence.model.Cart;
import com.example.persistence.model.Order;
import com.example.persistence.model.Product;
import com.example.persistence.model.Utente;
import com.example.rest.model.OrderDateRequest;
import com.example.service.exception.ProductNotAvailable;
import com.example.service.exception.QuantityNotAvailable;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static io.quarkus.arc.impl.UncaughtExceptions.LOGGER;


@ApplicationScoped
public class OrderService implements PanacheMongoRepository<Order> {

    private final ProductRepository productRepository;
    private final CartService cartService;
    private final Mailer mailer;

    @Inject
    public OrderService(ProductRepository productRepository, CartService cartService, Mailer mailer) throws QuantityNotAvailable, IllegalArgumentException {
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.mailer = mailer;
    }

    @Transactional
    public Response makeAnOrder(Utente user, OrderDateRequest pickupDateTime) {
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
        newOrder.pickupDateTime = handleOrderDateRequest(pickupDateTime) ;
        try {
            persist(newOrder);
            modifyProductQuantity(userCart.products);
            cartService.clearCart(userCart.id);

            // Send an "order completed successfully" message
            mailer.send(Mail.withHtml(user.getEmail(), "Ordine effettuato", "Il tuo ordine è andato a buon fine!"));
            mailer.send(Mail.withHtml("fabiogiannico3@gmail.com", "Ordine effettuato", "È stato effettuato un nuovo ordine!"));

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




    private OrderDateRequest handleOrderDateRequest(OrderDateRequest orderDateRequest) throws IllegalArgumentException{
        LocalDateTime requestedDateTime = orderDateRequest.getPickupDateTime();

        // Check if the pickup day is Monday
        if (requestedDateTime.getDayOfWeek() == DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("La pasticceria è chiusa il lunedì.");
        }

        // Round time to the closest 10-minute interval
        int minutes = requestedDateTime.getMinute();
        int roundedMinutes = (minutes / 10) * 10;
        if (minutes % 10 >= 5) {
            roundedMinutes += 10;
        }
        requestedDateTime = requestedDateTime.withMinute(roundedMinutes).withSecond(0).withNano(0);

        // Check if the rounded pickup date and time is in the past
        if (requestedDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La data di ritiro non può essere nel passato.");
        }

        // Define opening and closing times
        LocalTime openingTime;
        LocalTime closingTime;

        switch (requestedDateTime.getDayOfWeek()) {
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
            case FRIDAY:
                openingTime = LocalTime.of(8, 30);
                closingTime = LocalTime.of(19, 0);
                break;
            case SATURDAY:
                openingTime = LocalTime.of(9, 0);
                closingTime = LocalTime.of(19, 0);
                break;
            case SUNDAY:
                if (requestedDateTime.toLocalTime().isBefore(LocalTime.of(13, 0))) {
                    openingTime = LocalTime.of(9, 0);
                    closingTime = LocalTime.of(13, 0);
                } else if (requestedDateTime.toLocalTime().isAfter(LocalTime.of(15, 0))) {
                    openingTime = LocalTime.of(15, 0);
                    closingTime = LocalTime.of(19, 0);
                } else {
                    throw new IllegalArgumentException("Orario non disponibile.");
                }
                break;
            default:
                throw new IllegalArgumentException("Giorno non valido per la prenotazione.");
        }

        // Check if the time is within opening hours
        if (requestedDateTime.toLocalTime().isBefore(openingTime) || requestedDateTime.toLocalTime().isAfter(closingTime)) {
            throw new IllegalArgumentException("L'orario richiesto è fuori dagli orari di apertura.");
        }

        Order orderWithSameTime = find("pickup_date_time.pickupDateTime", requestedDateTime).firstResult();
        if (orderWithSameTime != null){
            throw new IllegalArgumentException("Questo orario di ritiro è già occupato da un altro utente");
        }

        // Return the adjusted OrderDateRequest
        OrderDateRequest adjustedRequest = new OrderDateRequest();
        adjustedRequest.setPickupDateTime(requestedDateTime);
        return adjustedRequest;
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
