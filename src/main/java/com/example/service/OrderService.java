package com.example.service;

import com.example.persistence.ProductRepository;
import com.example.persistence.model.*;
import com.example.rest.model.OrderRequest;
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
import java.sql.Time;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Response makeAnOrder(Utente user, OrderRequest orderRequest) {
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
        newOrder.pickupDateTime = handleOrderDateRequest(orderRequest);
        newOrder.notes = orderRequest.notes;
        try {
            persist(newOrder);
            modifyProductQuantity(userCart.products);
            cartService.clearCart(userCart.id);

            // Send an "order completed" email
            String emailOrderMessage = buildEmailOrderMessage(userCart, orderRequest);
            mailer.send(Mail.withHtml(user.getEmail(), "Ordine effettuato", "Il tuo ordine è andato a buon fine!" + emailOrderMessage));
            mailer.send(Mail.withHtml("fabiogiannico3@gmail.com", "Nuovo ordine", "È stato effettuato un nuovo ordine!" + emailOrderMessage));

            return Response.status(Response.Status.CREATED)
                    .entity("Ordine creato con successo")
                    .build();
        } catch (QuantityNotAvailable e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Quantità non disponibile per uno o più prodotti.")
                    .build();
        } catch (Exception e) {
            LOGGER.error("Errore durante la creazione dell'ordine: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore durante la creazione dell'ordine.")
                    .build();
        }
    }

    @Transactional
    public Response acceptAnOrder(AcceptOrder acceptOrder) {
        Order foundOrder = findById(acceptOrder.orderId);

        if (foundOrder == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String responseEntity;
        String orderStatus;
        if (acceptOrder.accepted) {
            orderStatus = "Accepted";
            responseEntity = "Order accepted";
        } else {
            orderStatus = "Rejected";
            responseEntity = "Order rejected";
        }

        foundOrder.status = orderStatus;
        update(foundOrder);
        return Response.ok().entity(responseEntity).build();
    }

    public List<Order> getAllOrders() {
        return listAll();
    }

    public List<Order> getAllUserOrders(String contact) {
        if (isValidEmail(contact)) {
            return find("email", contact).list();
        } else {
            return find("phone", contact).list();
        }

    }


    private OrderRequest handleOrderDateRequest(OrderRequest orderRequest) throws IllegalArgumentException {
        LocalDateTime requestedDateTime = orderRequest.getPickupDateTime();

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
        if (orderWithSameTime != null) {
            throw new IllegalArgumentException("Questo orario di ritiro è già occupato da un altro utente");
        }

        // Return the adjusted OrderDateRequest
        OrderRequest adjustedRequest = new OrderRequest();
        adjustedRequest.setPickupDateTime(requestedDateTime);
        return adjustedRequest;
    }

    private void modifyProductQuantity(List<Order.ProductItem> products) throws QuantityNotAvailable, ProductNotAvailable {
        for (Order.ProductItem item : products) {
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
        if (oldQuantity < productQuantity) {
            throw new QuantityNotAvailable();
        }
        return oldQuantity - productQuantity;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email != null && email.matches(emailRegex);
    }

    private String buildEmailOrderMessage(Cart cart, OrderRequest orderRequest) {
        StringBuilder productDetails = new StringBuilder();
        for (Order.ProductItem item : cart.products) {
            Product product = productRepository.findById(Long.valueOf(item.idProduct));
            if (product != null) {
                productDetails.append(product.getName())
                        .append(" (quantità: ")
                        .append(item.quantity)
                        .append(")");
            }
        }
        String pickupDateTimeFormatted = orderRequest.pickupDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        return "<p>Data di ritiro: " + pickupDateTimeFormatted + "</p>"
                + "<p>Note: " + (orderRequest.notes != null ? orderRequest.notes : "Nessuna") + "</p>"
                + "<p>Prodotti:</p>"
                + "<ul>" + productDetails + "</ul>"
                + "<p>Prezzo totale: " + cart.price + "€</p>";
    }

    public Response getNotAvailablePickupTimes(Date date) {

        // Set of unavailable dates
        Set<LocalDate> unavailableDates = Set.of(
                LocalDate.of(2024, 12, 24), // Example of an unavailable date
                LocalDate.of(2024, 12, 25),
                LocalDate.of(2024, 12, 26)
        );

        // Convert Date to LocalDate
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // Check if the date is a Monday
        if (localDate.getDayOfWeek() == DayOfWeek.MONDAY) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Deliveries are not available on Mondays.")
                    .build();
        }

        // Check if the date is in the list of unavailable dates
        if (unavailableDates.contains(localDate)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("The selected date is not available.")
                    .build();
        }

        // Convert LocalDate to Date for the start and end of the day
        Date startOfDay = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfDay = Date.from(localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Find all orders with a pickup_date_time within the same date
        List<Order> orders = find("{'pickupDateTime': { $gte: ?1, $lte: ?2 }}", startOfDay, endOfDay).list();

        // Extract only the occupied times
        List<Time> occupiedTimes = orders.stream()
                .map(order -> {
                    // Access the pickupDateTime field inside OrderRequest
                    LocalDateTime pickupTime = order.pickupDateTime.pickupDateTime;  // pickupDateTime is of type LocalDateTime

                    // Convert LocalDateTime to LocalTime and then to Time
                    LocalTime localTime = pickupTime.toLocalTime();  // Extract only the time
                    return Time.valueOf(localTime);  // Convert LocalTime to Time
                })
                .collect(Collectors.toList());

        // Return the occupied times as a response
        return Response.ok(occupiedTimes).build();
    }


}
