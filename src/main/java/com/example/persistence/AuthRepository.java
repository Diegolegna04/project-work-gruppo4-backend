package com.example.persistence;

import com.example.persistence.model.Sessione;
import com.example.persistence.model.Utente;
import com.example.rest.model.UtenteLoginRequest;
import com.example.rest.model.UtenteRegisterRequest;
import com.example.service.AuthService;
import com.example.service.exception.EmailNotAvailable;
import com.example.service.exception.TelephoneNotAvailable;
import com.example.service.exception.WrongUsernameOrPasswordException;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AuthRepository implements PanacheRepository<Utente> {

    @Inject
    EntityManager entityManager;

    private final AuthService authService;

    public AuthRepository(AuthService authService) {
        this.authService = authService;
    }


    // TODO: serve un costruttore vuoto in UtenteRegisterRequest e UtenteLoginRequest   ?????
    // TODO: serve qui sopra @Transactional     ?????
    // TODO: nella classe Utente se estendo con PanacheEntity posso non inserire l'id (sia come private Integer che come generativeStrategy)


    @Transactional
    public Response registerUser(UtenteRegisterRequest u) throws EmailNotAvailable, TelephoneNotAvailable {
        // Controlla se la mail o telefono che si vuole utilizzare per la registrazione sia già presente nel DB
        if (utenteWithMailAlreadyExists(u.getEmail()).isPresent()) {
            throw new EmailNotAvailable();
        } else if (utenteWithTelephoneAlreadyExists(u.getTelefono()).isPresent()) {
            throw new TelephoneNotAvailable();
        }

        // Crea un nuovo utente da inserire nel DB
        Utente newUtente = new Utente();
        newUtente.setNome(u.getNome());
        newUtente.setCognome(u.getCognome());
        newUtente.setEmail(u.getEmail());
        newUtente.setPassword(authService.hashPassword(u.getPassword()));
        newUtente.setTelefono(u.getTelefono());
        newUtente.setRuolo("non verificato");

        persist(newUtente);
        return Response.ok("Registrazione avvenuta con successo").build();
    }

    @Transactional
    public Response loginUser(UtenteLoginRequest u) throws WrongUsernameOrPasswordException {
        // Se le credenziali sono errate checkCredentials lancia una WrongUsernameOrPasswordException
        int idUtente = authService.checkCredentials(u.getEmail(), u.getTelefono(), u.getPassword());
        Sessione newSessione = new Sessione();
        newSessione.setSessionCookie(UUIDGenerator());
        newSessione.setDate(LocalDate.now());
        newSessione.setTime(LocalTime.now());
        newSessione.setIdUtente(idUtente);

        entityManager.persist(newSessione);
        return Response.ok("Sessione creata correttamente").
                cookie(new NewCookie.Builder("SESSION_COOKIE")
                        .value(newSessione.getSessionCookie())
                        .path("/")
                        .build())
                .build();
    }

    @Transactional
    public Utente getUtenteBySessionCookie(String sessionCookie) {
        Sessione sessione = entityManager.createQuery(
                        "SELECT s FROM Sessione s WHERE s.sessionCookie = :sessionCookie", Sessione.class)
                .setParameter("sessionCookie", sessionCookie)
                .getSingleResult();

        // Usa l'idUtente della sessione per trovare l'utente corrispondente
        return find("id", sessione.getIdUtente()).firstResult();
    }

    public String UUIDGenerator() {
        // Genera un id univoco per il valore della sessione
        return UUID.randomUUID().toString();
    }

    @Transactional
    public void saveToken(Integer idUtente, String token) {
        Utente utente = findById(Long.valueOf(idUtente));

        if (utente != null) {
            utente.setVerificationToken(token);
        } else {
            throw new IllegalArgumentException("Utente non trovato con ID: " + idUtente);
        }
    }


    public Optional<Utente> utenteWithMailAlreadyExists(String email) {
        return find("email", email).singleResultOptional();
    }

    public Optional<Utente> utenteWithTelephoneAlreadyExists(String telefono) {
        return find("telefono", telefono).singleResultOptional();
    }

}
