package com.example.service;

import com.example.persistence.model.Ruolo;
import com.example.persistence.model.Sessione;
import com.example.persistence.model.Utente;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SessionService implements PanacheRepository<Sessione> {

    private final AuthService authService;

    public SessionService (AuthService authService){
        this.authService = authService;
    }


    public Ruolo getUserRoleBySessionCookie(String sessionCookie){
        Sessione sessioneUtente = find("sessionCookie", sessionCookie).firstResult();
        if(sessioneUtente == null){
            return null;
        }
        Utente utente = authService.find("id", sessioneUtente.getIdUtente()).firstResult();
        return utente.getRuolo();
    }

    public String getUserContactBySessionCookie(String sessionCookie){
        Sessione sessioneUtente = find("sessionCookie", sessionCookie).firstResult();
        if(sessioneUtente == null){
            return null;
        }
        Utente utente = authService.find("id", sessioneUtente.getIdUtente()).firstResult();
        if(utente == null){
            return null;
        }

        if(utente.getEmail() != null){
            return utente.getEmail();
        }else {
            return utente.getTelefono();
        }

    }
}
