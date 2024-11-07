package com.example.service;

import com.example.persistence.AuthRepository;
import com.example.persistence.model.Ruolo;
import com.example.persistence.model.Sessione;
import com.example.persistence.model.Utente;
import com.example.rest.model.UtenteLoginRequest;
import com.example.rest.model.UtenteRegisterRequest;
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
import java.util.UUID;

@ApplicationScoped
public class AuthService implements PanacheRepository<Utente> {

    @Inject
    EntityManager entityManager;

    private final HashCalculator hashCalculator;
    private final AuthRepository repository;
    private final Mailer mailer;

    @Inject
    public AuthService(HashCalculator hashCalculator, AuthRepository repository, Mailer mailer){
        this.hashCalculator = hashCalculator;
        this.repository = repository;
        this.mailer = mailer;
    }

    // TODO: nella classe Utente se estendo con PanacheEntity posso non inserire l'id (sia come private Integer che come generativeStrategy)

    @Transactional
    public Response registerUser(UtenteRegisterRequest u) throws EmailNotAvailable, TelephoneNotAvailable {
        // Check if the email or phone number the user want to register with already exists in DB
        if (repository.utenteWithMailAlreadyExists(u.getEmail()).isPresent()) {
            throw new EmailNotAvailable();
        } else if (repository.utenteWithTelephoneAlreadyExists(u.getTelefono()).isPresent()) {
            throw new TelephoneNotAvailable();
        }

        // Create a new User
        Utente newUtente = new Utente();
        newUtente.setNome(u.getNome());
        newUtente.setCognome(u.getCognome());
        newUtente.setEmail(u.getEmail());
        newUtente.setPassword(hashPassword(u.getPassword()));
        newUtente.setTelefono(u.getTelefono());
        newUtente.setRuolo(Ruolo.Non_verificato);
        // Persist it
        persist(newUtente);

        sendVerificationEmail(newUtente.getId(), newUtente.getEmail());
        return Response.ok("Registrazione avvenuta con successo.\nControlla la tua casella di posta e verifica la tua email!").build();
    }

    @Transactional
    public Response loginUser(UtenteLoginRequest u) throws WrongUsernameOrPasswordException, EmailNotVerified {
        Utente utente = find("email", u.getEmail()).firstResult();
        if (Objects.equals(utente.getRuolo().toString(), "Non_verificato")) {
            throw new EmailNotVerified();
        }
        // If credentials are wrong checkCredentials throws a WrongUsernameOrPasswordException
        int idUtente = checkCredentials(u.getEmail(), u.getTelefono(), u.getPassword());
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
        repository.saveToken(id, token);

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
            utente.setVerificationToken(null);

//            persist(utente);
            return Response.ok("La tua email è stata verificata con successo!").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Utente non trovato").build();
        }
    }





    private String UUIDGenerator() {
        return UUID.randomUUID().toString();
    }

    // Hash the password
    public String hashPassword(String password) {
        return hashCalculator.hashPassword(password);
    }

    // Check if the credentials used for the login are correct
    public int checkCredentials(String email, String telefono, String password) throws WrongUsernameOrPasswordException {
        String hashedPassword = hashPassword(password);
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
        if (!hashedPassword.equals(utente.getPassword())){
            throw new WrongUsernameOrPasswordException();
        }else {
            // The id is used as user referral in the session
            return utente.getId();
        }
    }

}
