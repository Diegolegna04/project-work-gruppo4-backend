package com.example.rest;

import com.example.persistence.model.Utente;
import com.example.rest.model.UtenteResponse;
import com.example.service.exception.UserNotRegisteredException;
import com.example.rest.model.UtenteLoginRequest;
import com.example.rest.model.UtenteRegisterRequest;
import com.example.service.AuthService;
import com.example.service.exception.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.util.Map;


@Path("/auth")
public class AuthResource {

    private final AuthService service;

    @Inject
    public AuthResource(AuthService service) {
        this.service = service;
    }

    // JSON FOR SIMULATING THE POST
//    {
//        "nome": "Fabio",
//        "cognome": "Giannico",
//        "email": "fabio.giannico@gmail.com",
//        "password": "FabioGiannico",
//        "telefono": ""
//    }
    // REGISTER METHOD
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(UtenteRegisterRequest u) throws EmailNotAvailable, TelephoneNotAvailable, ContactNotInserted, PasswordCannotBeEmpty {
        return service.registerUser(u);
    }

    // EMAIL VERIFYING SENDING METHOD
    @GET
    @Path("/verify")
    @Produces(MediaType.TEXT_PLAIN)
    public Response verifyEmail(@QueryParam("token") String token) {
        return service.verifyEmail(token);
    }

    // JSON FOR SIMULATING THE POST
//    {
//        "email": "fabio.giannico@gmail.com",
//        "password": "FabioGiannico"
//    }
    // LOGIN METHOD
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(UtenteLoginRequest u) throws WrongUsernameOrPasswordException, EmailNotVerified, UserNotRegisteredException, ContactNotInserted, PasswordCannotBeEmpty, LoginNotPossible {
        return service.loginUser(u);
    }

    // LOGOUT METHOD
    @DELETE
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@CookieParam("SESSION_COOKIE") String sessionCookie) {
        if (service.logout(sessionCookie)) {
            NewCookie session = new NewCookie("SESSION_COOKIE", "", "/", null, null, 0, false);
            return Response.ok("Logout effettuato con successo").cookie(session).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Sessione non trovata").build();
        }
    }

    //GET YOUR ROLE
    @GET
    @Path("/role")
    @Produces(MediaType.TEXT_PLAIN)
    public String getRole(@CookieParam("SESSION_COOKIE") String sessionCookie) {
        return service.getRole(sessionCookie);
    }

    // GET YOUR PROFILE (used for customizing the dashboard with user info)
    @GET
    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    public UtenteResponse getUtente(@CookieParam("SESSION_COOKIE") String sessionCookie) {
        return service.getUtenteBySessionCookie(sessionCookie);
    }

    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUtente(@CookieParam("SESSION_COOKIE") String sessionCookie, Map<String, String> campiAggiornati) {
        return service.getResponse(sessionCookie, campiAggiornati);
    }


}
