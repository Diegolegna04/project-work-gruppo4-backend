package com.example.service;

import com.example.persistence.model.Utente;
import com.example.service.exception.WrongUsernameOrPasswordException;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import javax.sql.DataSource;

@ApplicationScoped
public class AuthService implements PanacheRepository<Utente> {

    private final HashCalculator hashCalculator;

    public AuthService(HashCalculator hashCalculator){
        this.hashCalculator = hashCalculator;
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
