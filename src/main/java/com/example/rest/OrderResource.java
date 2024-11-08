package com.example.rest;

import com.example.persistence.model.Order;
import com.example.persistence.model.Ruolo;
import com.example.rest.model.OrderRequest;
import com.example.service.OrderService;
import com.example.service.SessionService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;


@Path("/order")
public class OrderResource {

    private final OrderService orderService;
    private final SessionService sessionService;

    public OrderResource(OrderService orderService, SessionService sessionService) {
        this.orderService = orderService;
        this.sessionService = sessionService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response makeAOrder(@CookieParam("SESSION_COOKIE") String sessionCookie, OrderRequest order) {
        String contact = sessionService.getUserContactBySessionCookie(sessionCookie);
        return orderService.makeAOrder(contact, order);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrders(@CookieParam("SESSION_COOKIE") String sessionCookie) {
        if (sessionService.getUserRoleBySessionCookie(sessionCookie).equals(Ruolo.Admin)) {
            List<Order> orders = orderService.getAllOrders();
            return Response.ok(orders).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}
