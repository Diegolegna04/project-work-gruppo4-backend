package com.example.rest;

import com.example.persistence.AuthRepository;
import com.example.rest.model.UtenteLoginRequest;
import com.example.rest.model.UtenteRegisterRequest;
import com.example.service.exception.EmailNotAvailable;
import com.example.service.exception.TelephoneNotAvailable;
import com.example.service.exception.WrongUsernameOrPasswordException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
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

    // LOGIN METHOD
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(UtenteLoginRequest u) throws WrongUsernameOrPasswordException {
        return repository.loginUser(u);
    }

    // EMAIL SENDING METHOD
    @GET
    @Path("/email")
    public Response sendTestEmail(@CookieParam("SESSION_COOKIE") String sessionCookie) {
        return repository.sendTestEmail(sessionCookie);
    }

    // EMAIL VERIFYING SENDING METHOD
    @GET
    @Path("/verify")
    @Produces(MediaType.TEXT_PLAIN)
    public Response verifyEmail(@QueryParam("token") String token) {
        return repository.verifyEmail(token);
    }


    // TODO: modificare il ruolo in User una volta verificata l'email
}
