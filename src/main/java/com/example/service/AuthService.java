package com.example.service;

import com.example.persistence.model.Utente;
import com.example.service.exception.WrongUsernameOrPasswordException;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import javax.sql.DataSource;

@ApplicationScoped
public class AuthService implements PanacheRepository<Utente> {

    private final HashCalculator hashCalculator;
    private final DataSource source;

    public AuthService(HashCalculator hashCalculator, DataSource source){
        this.hashCalculator = hashCalculator;
        this.source = source;
    }


    // Esegue l'hash della password
    public String hashPassword(String password) {
        return hashCalculator.hashPassword(password);
    }

    public int checkCredentials(String email, String telefono, String password) throws WrongUsernameOrPasswordException {
        String hashedPassword = hashPassword(password);
        Utente utente = new Utente();

        if (email != null && !email.isEmpty()) {
            utente = find("email", email).firstResult();
        } else if (telefono != null && !telefono.isEmpty()) {
            utente = find("telefono", telefono).firstResult();
        }

        if (utente == null) {
            throw new WrongUsernameOrPasswordException();
        }

        if (!hashedPassword.equals(utente.getPassword())){
            throw new WrongUsernameOrPasswordException();
        }else {
            return utente.getId();
        }
    }

}
