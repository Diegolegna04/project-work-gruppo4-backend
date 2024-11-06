package com.example.rest;

import com.example.rest.model.UtenteLoginRequest;
import com.example.rest.model.UtenteRegisterRequest;
import com.example.service.AuthService;
import com.example.service.exception.EmailNotAvailable;
import com.example.service.exception.EmailNotVerified;
import com.example.service.exception.TelephoneNotAvailable;
import com.example.service.exception.WrongUsernameOrPasswordException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/auth")
public class AuthResource {

    private final AuthService service;

    @Inject
    public AuthResource(AuthService service) {
        this.service = service;
    }


    // REGISTER METHOD
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(UtenteRegisterRequest u) throws EmailNotAvailable, TelephoneNotAvailable {
        return service.registerUser(u);
    }

    // EMAIL VERIFYING SENDING METHOD
    @GET
    @Path("/verify")
    @Produces(MediaType.TEXT_PLAIN)
    public Response verifyEmail(@QueryParam("token") String token) {
        return service.verifyEmail(token);
    }

    // LOGIN METHOD
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(UtenteLoginRequest u) throws WrongUsernameOrPasswordException, EmailNotVerified {
        return service.loginUser(u);
    }
}
