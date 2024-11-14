package com.example.rest;

import com.example.persistence.AuthRepository;
import com.example.persistence.model.Cart;
import com.example.persistence.model.Order;
import com.example.persistence.model.Utente;
import com.example.service.CartService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/cart")
public class CartResource {

    private final CartService service;
    private final AuthRepository authRepository;

    public CartResource(CartService service, AuthRepository authRepository) {
        this.service = service;
        this.authRepository = authRepository;
    }


    // SHOW CART METHOD
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Cart showCart(@CookieParam("SESSION_COOKIE") String sessionCookie) {
        Utente utente = authRepository.getUtenteBySessionCookie(sessionCookie);
        return service.find("idUser", utente.getId()).firstResult();
    }

    // JSON FOR SIMULATING THE POST
//    {
//        "idProduct": 15,
//        "quantity": 3
//    }

    // ADD A PRODUCT TO CART METHOD
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addProductToCart(@CookieParam("SESSION_COOKIE") String sessionCookie, Order.ProductItem product) {
        // Get the user by session cookie (used to find his cart by the userId)
        Utente utente = authRepository.getUtenteBySessionCookie(sessionCookie);
        return service.addProductToCart(utente.getId(), product);
    }

    // DELETE A PRODUCT FROM CART METHOD
    @DELETE
    @Path("/{idProduct}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProductFromCart(@CookieParam("SESSION_COOKIE") String sessionCookie, @PathParam("idProduct") Integer idProduct) {
        Utente utente = authRepository.getUtenteBySessionCookie(sessionCookie);
        return service.deleteProductFromCart(utente.getId(), idProduct);
    }

}
