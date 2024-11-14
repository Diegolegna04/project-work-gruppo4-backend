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


    // GET ALL VISIBLE PRODUCTS METHOD
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Product> getProducts(@CookieParam("SESSION_COOKIE") String sessionCookie) {
        if (sessionService.getUserRoleBySessionCookie(sessionCookie).equals(Ruolo.Admin)) {
            return repository.getAllProducts();
        } else if (sessionService.getUserRoleBySessionCookie(sessionCookie).equals(Ruolo.User)) {
            return repository.getAllProductsForUsers();
        }
        return List.of();
    }


    // ##### ADMIN METHODS #####

    // JSON FOR SIMULATING THE POST
//    {
//            "name":"Millefoglie",
//            "description":"Una torta multistrati con crema alla vaniglia e scaglie di cioccolato",
//            "price":29.99,
//            "ingredientList": ["Uovo", "Zucchero", "Panna", "Latte", "Farina"],
//            "quantity":11,
//            "category":"Torta",
//            "image":"/path_to_image",
//            "show_to_user":true
//    }

    // ADD A PRODUCT METHOD
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addProduct(@CookieParam("SESSION_COOKIE") String sessionCookie, ProductRequest product) {
        // Check if the user is logged in
        if (sessionService.getUserRoleBySessionCookie(sessionCookie) == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Non è stata trovata nessuna sessione per questo utente")
                    .build();
        }
        // Check if the user is Admin
        if (sessionService.getUserRoleBySessionCookie(sessionCookie).equals(Ruolo.Admin)) {
            // First add the ingredient list (and return its id)
            ObjectId ingredientListId = service.addIngredientList(product.getIngredientList());
            // Then add the product
            return service.addProduct(product, ingredientListId);
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity("Devi essere un admin per poter inserire un prodotto nel database").build();
    }

    // MODIFY A PRODUCT METHOD
    @PUT
    @Path("/{productId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifyProduct(@CookieParam("SESSION_COOKIE") String sessionCookie, @PathParam("productId") Integer productId, ProductRequest productRequest){
        // Check if the user is logged in
        if (sessionService.getUserRoleBySessionCookie(sessionCookie) == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Non è stata trovata nessuna sessione per questo utente").build();
        }
        // Check if the user is Admin
        if (sessionService.getUserRoleBySessionCookie(sessionCookie).equals(Ruolo.Admin)) {
            return service.modifyProduct(productId, productRequest);
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity("Devi essere un admin per poter inserire un prodotto nel database").build();
    }

    // DELETE A PRODUCT METHOD
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteProduct(@CookieParam("SESSION_COOKIE") String sessionCookie,Product product){
        // Check if the user is logged in
        if (sessionService.getUserRoleBySessionCookie(sessionCookie) == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Non è stata trovata nessuna sessione per questo utente").build();
        }
        // Check if the user is Admin
        if (sessionService.getUserRoleBySessionCookie(sessionCookie).equals(Ruolo.Admin)) {
            return service.removeProduct(product);
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity("Devi essere un admin per poter inserire un prodotto nel database").build();
    }
}
