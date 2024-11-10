package com.example.persistence;

import com.example.persistence.model.Sessione;
import com.example.persistence.model.Utente;
import com.example.service.HashCalculator;
import com.example.service.SessionService;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class AuthRepository implements PanacheRepository<Utente> {

    private final HashCalculator hashCalculator;
    private final SessionService sessionService;

    @Inject
    public AuthRepository(HashCalculator hashCalculator, SessionService sessionService) {
        this.hashCalculator = hashCalculator;
        this.sessionService = sessionService;
    }


    // Find User by the session cookie value
    public Utente getUtenteBySessionCookie(String sessionCookie) {
        // Find the session from the value of the SESSION_COOKIE
        Sessione sessione = sessionService.findSessioneByCookie(sessionCookie);
        // Find the user by idUtente value in sessione
        return find("id", sessione.getIdUtente()).firstResult();
    }

    //Save the token for the verification method
    public void saveToken(Integer idUtente, String token) {
        // Find the user where to save the verification token
        Utente utente = findById(Long.valueOf(idUtente));
        // If the user exists => save the verification token
        if (utente != null) {
            utente.setVerificationToken(token);
        } else {
            throw new IllegalArgumentException("Utente non trovato con ID: " + idUtente);
        }
    }

    // Used in registration => User with this email already exists?
    public Optional<Utente> utenteWithMailAlreadyExists(String email) {
        return find("email", email).singleResultOptional();
    }

    // Used in registration => User with this phone number already exists?
    public Optional<Utente> utenteWithTelephoneAlreadyExists(String telefono) {
        return find("telefono", telefono).singleResultOptional();
    }

    // Hash the password
    public String hashPassword(String password) {
        return hashCalculator.hashPassword(password);
    }
}
