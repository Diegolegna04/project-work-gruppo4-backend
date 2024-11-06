package com.example.persistence.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "sessione")
public class Sessione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "session_cookie", length = 50, nullable = false)
    private String sessionCookie;
    @Column(name = "data", nullable = false)
    private LocalDate date;
    @Column(name = "ora", nullable = false)
    private LocalTime time;
    @Column(name = "id_utente", nullable = false)
    private Integer idUtente;

    public Sessione(){

    }
    public Sessione(Integer id, String sessionCookie, LocalDate date, LocalTime time, Integer idUtente){
        this.id = id;
        this.sessionCookie = sessionCookie;
        this.date = date;
        this.time = time;
        this.idUtente = idUtente;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSessionCookie() {
        return sessionCookie;
    }

    public void setSessionCookie(String sessionCookie) {
        this.sessionCookie = sessionCookie;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public Integer getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(Integer idUtente) {
        this.idUtente = idUtente;
    }
}
