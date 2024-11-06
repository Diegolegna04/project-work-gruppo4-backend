package com.example.rest;

import com.example.persistence.AuthRepository;
import com.example.persistence.model.Utente;
import com.example.rest.model.UtenteLoginRequest;
import com.example.rest.model.UtenteRegisterRequest;
import com.example.service.exception.EmailNotAvailable;
import com.example.service.exception.TelephoneNotAvailable;
import com.example.service.exception.WrongUsernameOrPasswordException;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/auth")
public class AuthResource {

    private final AuthRepository repository;
    private final Mailer mailer;

    @Inject
    public AuthResource(AuthRepository authRepository, Mailer mailer) {
        this.repository = authRepository;
        this.mailer = mailer;
    }


    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(UtenteRegisterRequest u) throws EmailNotAvailable, TelephoneNotAvailable {
        return repository.registerUser(u);
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(UtenteLoginRequest u) throws WrongUsernameOrPasswordException {
        return repository.loginUser(u);
    }

    @GET
    @Path("/email")
    public Response sendTestEmail(@CookieParam("SESSION_COOKIE") String sessionCookie) {
        Utente utente = repository.getUtenteBySessionCookie(sessionCookie);

        String token = repository.UUIDGenerator();
        repository.saveToken(utente.getId(), token);

        String verificationLink = "http://localhost:8080/auth/verify?token=" + token;
        String message = "Clicca sul <a href=\"" + verificationLink + "\">link</a> di verifica per autenticare la tua email";
        mailer.send(Mail.withHtml(utente.getEmail(), "Verifica la tua email", message));

        return Response.ok("Email di verifica inviata a " + utente.getEmail()).build();
    }

    @GET
    @Path("/verify")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public Response verifyEmail(@QueryParam("token") String token) {
        // Cerca il token nel database
        Utente utente = repository.find("verificationToken", token).firstResult();

        if (utente != null) {
            utente.setRuolo("User");

            return Response.ok("La tua email è stata verificata con successo!").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Utente non trovato").build();
        }
    }


    // TODO: modificare il ruolo in User una volta verificata l'email
}
