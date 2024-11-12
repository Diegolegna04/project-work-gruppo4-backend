package com.example.rest;

import com.example.persistence.ProductRepository;
import com.example.persistence.model.Product;
import com.example.persistence.model.Ruolo;
import com.example.rest.model.ProductRequest;
import com.example.service.ProductService;
import com.example.service.SessionService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;

import java.util.List;

@Path("/products")
public class ProductResource {

    private final SessionService sessionService;
    private final ProductService service;
    private final ProductRepository repository;

    public ProductResource(SessionService sessionService, ProductService service, ProductRepository repository) {
        this.sessionService = sessionService;
        this.service = service;
        this.repository = repository;
    }



    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Product> getProducts(@CookieParam("SESSION_COOKIE") String sessionCookie) {
        // Get a list of all the products in storage (only for admins)
        if (sessionService.getUserRoleBySessionCookie(sessionCookie).equals(Ruolo.Admin)) {
            return repository.getAllProducts();
        }
        // Get a list of all the products visible (for users)
        else if (sessionService.getUserRoleBySessionCookie(sessionCookie).equals(Ruolo.User)) {
            return repository.getAllProductsForUsers();
        }
        return List.of();
    }


    // ADMIN METHODS
    // JSON FOR SIMULATING THE POST
//    {
//            "name":"Millefoglie",
//            "description":"Una torta multistrati con crema alla vaniglia e scaglie di cioccolato",
//            "price":29.99,
//            "quantity":11,
//            "category":"Torta",
//            "image":"/path_to_image",
//            "show_to_user":true
//    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addProduct(@CookieParam("SESSION_COOKIE") String sessionCookie, ProductRequest product) {
        if (sessionService.getUserRoleBySessionCookie(sessionCookie) == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Non è stata trovata nessuna sessione per questo utente").build();
        }
        if (sessionService.getUserRoleBySessionCookie(sessionCookie).equals(Ruolo.Admin)) {
            ObjectId ingredientListId = service.addIngredientList(product.getIngredientList());
            return service.addProduct(product, ingredientListId);
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity("Devi essere un admin per poter inserire un prodotto nel database").build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteProduct(@CookieParam("SESSION_COOKIE") String sessionCookie,Product product){
        if (sessionService.getUserRoleBySessionCookie(sessionCookie) == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Non è stata trovata nessuna sessione per questo utente").build();
        }
        if (sessionService.getUserRoleBySessionCookie(sessionCookie).equals(Ruolo.Admin)) {
            return service.removeProduct(product);
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity("Devi essere un admin per poter inserire un prodotto nel database").build();
    }


}
