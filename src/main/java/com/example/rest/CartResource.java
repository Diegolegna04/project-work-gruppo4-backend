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

    // TODO: non devi poter inserire una quantità negativa così da abbassare il prezzo
    // TODO: se quantity di un prodotto = 0 elimina il prodotto dalla lista products
    // TODO: come è possibile che da front posso eliminare un prodotto e modificare quantità dal carrello ma non ho il metodo in back??????

    // JSON FOR SIMULATING THE POST
//    {
//        "idProduct": 15,
//        "quantity": 3
//    }


    //Show cart method
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Cart showCart(@CookieParam("SESSION_COOKIE") String sessionCookie) {
        Utente utente = authRepository.getUtenteBySessionCookie(sessionCookie);
        return service.find("idUser", utente.getId()).firstResult();
    }

    // Add a product to cart method
    // TODO: non devi poter inserire una quantità negativa così da abbassare il prezzo
    // TODO: se quantity di un prodotto = 0 elimina il prodotto dalla lista products
    // TODO: come è possibile che da front posso eliminare un prodotto e modificare quantità dal carrello ma non ho il metodo in back??????

    // JSON FOR SIMULATING THE POST
//    {
//        "idProduct": 15,
//        "quantity": 3
//    }

    // Add a product to cart method
    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addProductToCart(@CookieParam("SESSION_COOKIE") String sessionCookie, Order.ProductItem product) {
        // Get the user by session cookie (used to find his cart by the userId)
        Utente utente = authRepository.getUtenteBySessionCookie(sessionCookie);
        return service.addProductToCart(utente.getId(), product);
    }

    // Delete a single product from the cart
    @DELETE
    @Path("/delete/{idProduct}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProductFromCart(@CookieParam("SESSION_COOKIE") String sessionCookie, @PathParam("idProduct") Integer idProduct) {
        Utente utente = authRepository.getUtenteBySessionCookie(sessionCookie);
        return service.deleteProductFromCart(utente.getId(), idProduct);
    }

}
