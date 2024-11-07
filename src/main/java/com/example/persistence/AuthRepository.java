package com.example.persistence;

import com.example.persistence.model.Ruolo;
import com.example.persistence.model.Sessione;
import com.example.persistence.model.Utente;
import com.example.rest.exception.UserNotRegisteredException;
import com.example.rest.model.UtenteLoginRequest;
import com.example.rest.model.UtenteRegisterRequest;
import com.example.service.AuthService;
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

    private final AuthService authService;
    private final Mailer mailer;

    @Inject
    public AuthRepository(AuthService authService, Mailer mailer) {
        this.authService = authService;
        this.mailer = mailer;
    }


    // TODO: serve un costruttore vuoto in UtenteRegisterRequest e UtenteLoginRequest   ?????
    // TODO: nella classe Utente se estendo con PanacheEntity posso non inserire l'id (sia come private Integer che come generativeStrategy)


    @Transactional
    public Response registerUser(UtenteRegisterRequest u) throws EmailNotAvailable, TelephoneNotAvailable {
        // Check if the email or phone number the user want to register with already exists in DB
        if (utenteWithMailAlreadyExists(u.getEmail()).isPresent()) {
            throw new EmailNotAvailable();
        } else if (utenteWithTelephoneAlreadyExists(u.getTelefono()).isPresent()) {
            throw new TelephoneNotAvailable();
        }

        // Check if the email is valid (allow numbers before @, but not after, and must end with .com or .it)
        String emailRegex = "^[A-Za-z0-9][A-Za-z0-9._%+-]*@[A-Za-z0-9.-]+\\.(com|it)$";
        if (u.getEmail() == null || !u.getEmail().matches(emailRegex)) {
            throw new IllegalArgumentException("L'indirizzo email inserito non è valido. Assicurati che non ci siano numeri dopo la @ e che termini con .com o .it");
        }

        // Create a new User
        Utente newUtente = new Utente();
        newUtente.setNome(u.getNome());
        newUtente.setCognome(u.getCognome());
        newUtente.setEmail(u.getEmail());
        newUtente.setPassword(authService.hashPassword(u.getPassword()));
        newUtente.setTelefono(u.getTelefono());
        newUtente.setRuolo(Ruolo.Non_verificato);
        // Persist it
        persist(newUtente);

        sendVerificationEmail(newUtente.getId(), newUtente.getEmail());
        return Response.ok("Registrazione avvenuta con successo.\nControlla la tua casella di posta e verifica la tua email!").build();
    }

    @Transactional
    public Response loginUser(UtenteLoginRequest u) throws WrongUsernameOrPasswordException, EmailNotVerified, UserNotRegisteredException {
        Utente utente = find("email", u.getEmail()).firstResult();
        if (utente == null) {
            throw new UserNotRegisteredException();
        }
        if (Objects.equals(utente.getRuolo().toString(), "Non_verificato")) {
            throw new EmailNotVerified();
        }
        // If credentials are wrong checkCredentials throws a WrongUsernameOrPasswordException
        int idUtente = authService.checkCredentials(u.getEmail(), u.getTelefono(), u.getPassword());
        // Create a new session
        Sessione newSessione = new Sessione();
        newSessione.setSessionCookie(UUIDGenerator());
        newSessione.setDate(LocalDate.now());
        newSessione.setTime(LocalTime.now());
        newSessione.setIdUtente(idUtente);
        // Persist it
        entityManager.persist(newSessione);
        // Return Response and build the cookie
        return Response.ok("Sessione creata correttamente").
                cookie(new NewCookie.Builder("SESSION_COOKIE")
                        .value(newSessione.getSessionCookie())
                        .path("/")
                        .build())
                .build();
    }

    @Transactional
    public void sendVerificationEmail(Integer id, String email) {
        // Generate a unique token for the verifying method
        String token = UUIDGenerator();
        // Persist it
        saveToken(id, token);

        // Build the verification link
        String verificationLink = "http://localhost:8080/auth/verify?token=" + token;
        String message = "Clicca sul <a href=\"" + verificationLink + "\">link</a> di verifica per autenticare la tua email";
        // Send verification
        mailer.send(Mail.withHtml(email, "Verifica la tua email", message));
    }

    @Transactional
    public Response verifyEmail(String token) {
        // Trova l'utente con il token inviato nell'email
        Utente utente = find("verificationToken", token).firstResult();

        // Se l'utente esiste, aggiorna il ruolo a "User"
        if (utente != null) {
            utente.setRuolo(Ruolo.User);

            utente.setVerificationToken(null); // Rimuovi il token se non serve più

            // Forza la persistenza delle modifiche
            persist(utente);
            // oppure: flush();

            return Response.ok("La tua email è stata verificata con successo!").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Utente non trovato").build();
        }
    }

//    @Transactional
//    public Response verifyEmail(String token) {
//        // Find the user with the token sent in the email
//        Utente utente = find("verificationToken", token).firstResult();
//        // If the user exists => set his role to User (its now verified)
//        if (utente != null) {
//            utente.setRuolo("User");
//            return Response.ok("La tua email è stata verificata con successo!").build();
//        } else {
//            return Response.status(Response.Status.NOT_FOUND).entity("Utente non trovato").build();
//        }
//    }

    private Utente getUtenteBySessionCookie(String sessionCookie) {
        // Find the session from the value of the SESSION_COOKIE
        Sessione sessione = entityManager.createQuery(
                "SELECT s FROM Sessione s WHERE s.sessionCookie = :sessionCookie", Sessione.class)
                .setParameter("sessionCookie", sessionCookie)
                .getSingleResult();
        // Find the user by idUtente value in sessione
       return find("id", sessione.getIdUtente()).firstResult();
    }

    private String UUIDGenerator() {
        return UUID.randomUUID().toString();
    }

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

    // Logout
    @Transactional
    public boolean logout(String sessionCookie) {
        Utente utente = getUtenteBySessionCookie(sessionCookie);

        if (utente != null) {
            try {
                Sessione sessione = entityManager.createQuery(
                                "SELECT s FROM Sessione s WHERE s.sessionCookie = :sessionCookie", Sessione.class)
                        .setParameter("sessionCookie", sessionCookie)
                        .getSingleResult();

                entityManager.remove(sessione);
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
