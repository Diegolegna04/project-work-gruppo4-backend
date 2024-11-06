package com.example.persistence.model;

import jakarta.persistence.*;

@Entity
@Table(name = "utente")
public class Utente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "nome", length = 100, nullable = false)
    private String nome;
    @Column(name = "cognome", length = 100, nullable = false)
    private String cognome;
    @Column(name = "email", length = 200)
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "telefono", length = 10)
    private String telefono;
    @Enumerated(EnumType.STRING)
    @Column(name = "ruolo")
    private Ruolo ruolo;
    @Column(name = "token_verifica")
    private String verificationToken;

    public Utente() {

    }
    public Utente(Integer id, String nome, String cognome, String email, String password, String telefono, Ruolo ruolo, String verificationToken) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
        this.ruolo = ruolo;
        this.verificationToken = verificationToken;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Ruolo getRuolo() {
        return ruolo;
    }

    public void setRuolo(Ruolo ruolo) {
        this.ruolo = ruolo;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }
}