package com.example.rest;

import com.example.persistence.AuthRepository;
import com.example.persistence.model.Order;
import com.example.persistence.model.Ruolo;
import com.example.persistence.model.Sessione;
import com.example.persistence.model.Utente;
import com.example.rest.model.OrderDateRequest;
import com.example.service.OrderService;
import com.example.service.SessionService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


@Path("/order")
public class OrderResource {

    private final OrderService service;
    private final SessionService sessionService;
    private final AuthRepository authRepository;

    public OrderResource(OrderService service, SessionService sessionService, AuthRepository authRepository) {
        this.service = service;
        this.sessionService = sessionService;
        this.authRepository = authRepository;
    }


//    {
//        "pickupDateTime": "2024-11-10T13:00:00"
//    }
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response makeAOrder(@CookieParam("SESSION_COOKIE") String sessionCookie, OrderDateRequest pickupDateTime) {
        Sessione userSession = sessionService.findSessioneByCookie(sessionCookie);
        Utente utente = authRepository.findById(Long.valueOf(userSession.getIdUtente()));
        return service.makeAOrder(utente, pickupDateTime);
    }

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUserOrders(@CookieParam("SESSION_COOKIE") String sessionCookie) {
        Utente utente = authRepository.getUtenteBySessionCookie(sessionCookie);
        if (utente.getRuolo().equals(Ruolo.User)) {
            String contact;
            if (utente.getEmail() != null){
                contact = utente.getTelefono();
            }else {
                contact = utente.getEmail();
            }
            List<Order> orders = service.getAllUserOrders(contact);
            return Response.ok(orders).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }



    // ADMIN METHODS

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllOrders(@CookieParam("SESSION_COOKIE") String sessionCookie) {
        if (sessionService.getUserRoleBySessionCookie(sessionCookie).equals(Ruolo.Admin)) {
            List<Order> orders = service.getAllOrders();
            return Response.ok(orders).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}
