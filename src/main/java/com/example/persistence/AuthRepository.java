package com.example.persistence;

import com.example.persistence.model.Ruolo;
import com.example.persistence.model.Sessione;
import com.example.persistence.model.Utente;
import com.example.rest.exception.UserNotRegisteredException;
import com.example.rest.model.UtenteLoginRequest;
import com.example.rest.model.UtenteRegisterRequest;
import com.example.service.AuthService;
import com.example.service.HashCalculator;
import com.example.service.SessionService;
import com.example.service.exception.EmailNotAvailable;
import com.example.service.exception.EmailNotVerified;
import com.example.service.exception.TelephoneNotAvailable;
import com.example.service.exception.WrongUsernameOrPasswordException;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AuthRepository implements PanacheRepository<Utente> {

    @Inject
    EntityManager entityManager;

    private final HashCalculator hashCalculator;

    @Inject
    public AuthRepository(HashCalculator hashCalculator) {
        this.hashCalculator = hashCalculator;
    }




    // TODO: sostituire EntityManager con sessionService

    // Find User by the session cookie value
    public Utente getUtenteBySessionCookie(String sessionCookie) {
        // Find the session from the value of the SESSION_COOKIE
        Sessione sessione = entityManager.createQuery(
                        "SELECT s FROM Sessione s WHERE s.sessionCookie = :sessionCookie", Sessione.class)
                .setParameter("sessionCookie", sessionCookie)
                .getSingleResult();
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
