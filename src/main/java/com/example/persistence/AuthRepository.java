package com.example.persistence;

import com.example.persistence.model.Utente;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class AuthRepository implements PanacheRepository<Utente> {

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

}
