package com.example.service;

import com.example.persistence.AuthRepository;
import com.example.persistence.model.Ruolo;
import com.example.persistence.model.Sessione;
import com.example.persistence.model.Utente;
import com.example.service.exception.UserNotRegisteredException;
import com.example.rest.model.UtenteLoginRequest;
import com.example.rest.model.UtenteRegisterRequest;
import com.example.service.exception.*;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class AuthService implements PanacheRepository<Utente> {

    private final AuthRepository repository;
    private final Mailer mailer;
    private final SessionService sessionService;
    private final CartService cartService;

    @Inject
    public AuthService(AuthRepository repository, Mailer mailer, SessionService sessionService, CartService cartService) {
        this.repository = repository;
        this.mailer = mailer;
        this.sessionService = sessionService;
        this.cartService = cartService;
    }


    @Transactional
    public Response registerUser(UtenteRegisterRequest u) throws EmailNotAvailable, TelephoneNotAvailable, ContactNotInserted, PasswordCannotBeEmpty {
        // Check the user inserted at least one contact method
        if ((u.getEmail() == null || u.getEmail().isEmpty()) && (u.getTelefono() == null || u.getTelefono().isEmpty())) {
            throw new ContactNotInserted();
        }

        // If the user inserted email as contact method
        if (u.getEmail() != null && !u.getEmail().isEmpty()){
            // If the inserted email is already in use throw EmailNotAvailable
            if (repository.utenteWithMailAlreadyExists(u.getEmail()).isPresent()) {
                throw new EmailNotAvailable();
            }
        } else {
            // If the inserted phone is already in use throw TelephoneNotAvailable
            if (repository.utenteWithTelephoneAlreadyExists(u.getTelefono()).isPresent()) {
                throw new TelephoneNotAvailable();
            }
        }

        // Check the password is not empty
        if (u.getPassword() == null || u.getPassword().isEmpty()) {
            throw new PasswordCannotBeEmpty();
        }

        // Create a new User
        Utente newUtente = new Utente();
        newUtente.setNome(u.getNome());
        newUtente.setCognome(u.getCognome());
        newUtente.setEmail(u.getEmail());
        newUtente.setPassword(repository.hashPassword(u.getPassword()));
        newUtente.setTelefono(u.getTelefono());
        newUtente.setRuolo(Ruolo.Non_verificato);
        // Persist it
        persist(newUtente);
        // Send the verification link to the user email
        sendVerificationEmail(newUtente.getId(), newUtente.getEmail());
        return Response.ok("Registrazione avvenuta con successo.\nControlla la tua casella di posta e verifica la tua email!").build();
    }

    @Transactional
    public void sendVerificationEmail(Integer id, String email) {
        // Generate a unique token for the verifying method
        String token = UUIDGenerator();
        // Persist it
        repository.saveToken(id, token);

        // Build the verification link
        String verificationLink = "http://localhost:8080/auth/verify?token=" + token;
        String message = "Clicca sul <a href=\"" + verificationLink + "\">link</a> di verifica per autenticare la tua email";
        // Send verification
        mailer.send(Mail.withHtml(email, "Verifica la tua email", message));
    }

    @Transactional
    public Response verifyEmail(String token) {
        // Find the user that have the verification token sent to email
        Utente utente = find("verificationToken", token).firstResult();

        // If the user exists change role from "Non_verificato" to "User"
        if (utente != null) {
            // Set user role to "User" (verified)
            utente.setRuolo(Ruolo.User);
            // Delete the verification token
            utente.setVerificationToken(null);
            // Create a new cart for the user
            cartService.createNewEmptyCart(utente.getId());
            return Response.ok("La tua email è stata verificata con successo!").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Utente non trovato").build();
        }
    }

    @Transactional
    public Response loginUser(UtenteLoginRequest u) throws WrongUsernameOrPasswordException, EmailNotVerified, UserNotRegisteredException, ContactNotInserted, PasswordCannotBeEmpty, LoginNotPossible {
        Utente utente = new Utente();
        // Find the user by the contact method
        if ((u.getEmail() != null && !u.getEmail().isEmpty()) || (u.getTelefono() != null && !u.getTelefono().isEmpty())) {
            if (u.getEmail() != null && !u.getEmail().isEmpty()) {
                utente = find("email", u.getEmail()).firstResult();
            } else {
                utente = find("telefono", u.getTelefono()).firstResult();
            }
        } else {
            throw new ContactNotInserted();
        }

        if (u.getPassword() == null || u.getPassword().isEmpty()) {
            throw new PasswordCannotBeEmpty();
        }


        if (utente == null) {
            throw new UserNotRegisteredException(); // If user doesn't exist it's not registered
        }
        if (Objects.equals(utente.getRuolo().toString(), "Non_verificato")) {
            throw new EmailNotVerified(); // If user's role is "Non_verificato" he can't have access to the ecommerce
        }
        // If credentials are wrong checkCredentials throws a WrongUsernameOrPasswordException
        Integer idUtente = checkCredentials(u.getEmail(), u.getTelefono(), u.getPassword());

        if (sessionService.userSessionAlreadyExists(idUtente)){
            throw new LoginNotPossible();
        }
        // Create a new session
        Sessione newSessione = new Sessione();
        newSessione.setSessionCookie(UUIDGenerator());
        newSessione.setDate(LocalDate.now());
        newSessione.setTime(LocalTime.now());
        newSessione.setIdUtente(idUtente);
        // Persist it
        sessionService.persist(newSessione);
        // Return Response and build the cookie
        return Response.ok("Sessione creata correttamente").
                cookie(new NewCookie.Builder("SESSION_COOKIE")
                        .value(newSessione.getSessionCookie())
                        .path("/")
                        .build())
                .build();
    }

    @Transactional
    public boolean logout(String sessionCookie) {
        // Find the user by the session cookie value
        Utente utente = repository.getUtenteBySessionCookie(sessionCookie);
        // If it exists delete his session and return true
        if (utente != null) {
            try {
                Sessione sessione = sessionService.findSessioneByCookie(sessionCookie);
                sessionService.delete(sessione);
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }



    private String UUIDGenerator() {
        return UUID.randomUUID().toString();
    }

    // Check if the credentials used for the login are correct
    public Integer checkCredentials(String email, String telefono, String password) throws WrongUsernameOrPasswordException {
        String hashedPassword = repository.hashPassword(password);
        Utente utente = new Utente();

        // Find teh user by email or password
        if (email != null && !email.isEmpty()) {
            utente = find("email", email).firstResult();
        } else if (telefono != null && !telefono.isEmpty()) {
            utente = find("telefono", telefono).firstResult();
        }

        // If the user doesn't exist => WrongUsernameOrPasswordException
        if (utente == null) {
            throw new WrongUsernameOrPasswordException();
        }

        // If the credentials are wrong => WrongUsernameOrPasswordException
        if (!hashedPassword.equals(utente.getPassword())) {
            throw new WrongUsernameOrPasswordException();
        } else {
            // The id is used as user referral in the session
            return utente.getId();
        }
    }

    // Get user by the session cookie value
    public Utente getUtenteBySessionCookie(String sessionCookie){
        return repository.getUtenteBySessionCookie(sessionCookie);
    }
}
