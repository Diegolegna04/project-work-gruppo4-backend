package com.example.rest.model;

public class UtenteLoginRequest {
    private String email;
    private String telefono;
    private String password;

    public UtenteLoginRequest(String email, String telefono, String password){
        this.email = email;
        this.telefono = telefono;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
