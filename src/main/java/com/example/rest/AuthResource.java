package com.example.rest;

import com.example.persistence.AuthRepository;
import com.example.persistence.model.Utente;
import com.example.rest.exception.UserNotRegisteredException;
import com.example.rest.model.UtenteLoginRequest;
import com.example.rest.model.UtenteRegisterRequest;
import com.example.service.exception.EmailNotAvailable;
import com.example.service.exception.EmailNotVerified;
import com.example.service.exception.TelephoneNotAvailable;
import com.example.service.exception.WrongUsernameOrPasswordException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;


@Path("/auth")
public class AuthResource {

    private final AuthRepository repository;

    @Inject
    public AuthResource(AuthRepository authRepository) {
        this.repository = authRepository;
    }


    // REGISTER METHOD
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(UtenteRegisterRequest u) throws EmailNotAvailable, TelephoneNotAvailable {
        return repository.registerUser(u);
    }

    // EMAIL VERIFYING SENDING METHOD
    @GET
    @Path("/verify")
    @Produces(MediaType.TEXT_PLAIN)
    public Response verifyEmail(@QueryParam("token") String token) {
        return repository.verifyEmail(token);
    }

    // LOGIN METHOD
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(UtenteLoginRequest u) throws WrongUsernameOrPasswordException, EmailNotVerified, UserNotRegisteredException {
        return repository.loginUser(u);
    }

    // LOGOUT METHOD
    @DELETE
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@CookieParam("SESSION_COOKIE") String sessionCookie) {
        if (repository.logout(sessionCookie)) {
            NewCookie session = new NewCookie("SESSION_COOKIE", "", "/", null, null, 0, false);
            return Response.ok("Logout effettuato con successo").cookie(session).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Sessione non trovata").build();
        }
    }

    @GET
    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    public Utente getRuolo(@CookieParam("SESSION_COOKIE") String sessionCookie) {
        return repository.getRuolo(sessionCookie);
    }
}
