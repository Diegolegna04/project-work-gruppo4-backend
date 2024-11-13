package com.example.rest;

import com.example.persistence.AuthRepository;
import com.example.persistence.model.*;
import com.example.rest.model.OrderDateRequest;
import com.example.service.OrderService;
import com.example.service.SessionService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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


    // JSON FOR SIMULATING THE POST
//    {
//        "pickupDateTime": "2025-03-04T13:18:00"
//    }

    // MAKE AN ORDER METHOD
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response makeAnOrder(@CookieParam("SESSION_COOKIE") String sessionCookie, OrderDateRequest pickupDateTime) {
        Sessione userSession = sessionService.findSessioneByCookie(sessionCookie);
        Utente utente = authRepository.findById(Long.valueOf(userSession.getIdUtente()));
        return service.makeAnOrder(utente, pickupDateTime);
    }


    // TODO: sposta in service
    // GET ALL MY ORDERS (AS I'M LOGGED IN AS A USER) METHOD
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



    // ##### ADMIN METHODS #####


    // GET ALL THE ORDERS METHOD
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllOrders(@CookieParam("SESSION_COOKIE") String sessionCookie) {
        // Check if the user is an Admin
        if (sessionService.getUserRoleBySessionCookie(sessionCookie).equals(Ruolo.Admin)) {
            List<Order> orders = service.getAllOrders();
            return Response.ok(orders)
                    .build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    // ACCEPT / REJECT AN ORDER METHOD (true = accepted)
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response acceptAnOrder(@CookieParam("SESSION_COOKIE") String sessionCookie, AcceptOrder acceptOrder) {
        // Check if the user is an Admin
        if (sessionService.getUserRoleBySessionCookie(sessionCookie).equals(Ruolo.Admin)) {
            return service.acceptAnOrder(acceptOrder);
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}
