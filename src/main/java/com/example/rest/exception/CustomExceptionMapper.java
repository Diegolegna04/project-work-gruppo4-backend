package com.example.rest.exception;

import com.example.service.exception.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.ExceptionMapper;

@Provider
public class CustomExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof EmailNotAvailable){
            return Response.status(Response.Status.CONFLICT)
                    .entity("Questa email è già collegata ad un altro account")
                    .type("text/plain")
                    .build();
        } else if (exception instanceof TelephoneNotAvailable) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Questo numero di telefono è già collegato ad un altro account")
                    .type("text/plain")
                    .build();
        }else if (exception instanceof EmailNotVerified) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Prima di poter accedere verifica la tua email nella casella di posta")
                    .type("text/plain")
                    .build();
        } else if (exception instanceof WrongUsernameOrPasswordException) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Username o password errati")
                    .type("text/plain")
                    .build();
        } else if (exception instanceof UserNotRegisteredException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Utente con email inserita inesistente")
                    .type("text/plain")
                    .build();
        } else if (exception instanceof ContactNotInserted) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Inserire almeno un contatto (email o telefono)")
                    .type("text/plain")
                    .build();
        } else if (exception instanceof PasswordCannotBeEmpty) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Inserire una password")
                    .type("text/plain")
                    .build();
        } else if (exception instanceof LoginNotPossible) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Per poter effettuare nuovamente il login eseguire prima il logout")
                    .type("text/plain")
                    .build();
        } else if (exception instanceof ProductNotAvailable) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Il prodotto non è disponibile")
                    .type("text/plain")
                    .build();
        } else if (exception instanceof QuantityNotAvailable) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Quantità non disponibile")
                    .type("text/plain")
                    .build();
        }

        // Default Response
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Errore imprevisto: " + exception.getMessage())
                .type("text/plain")
                .build();
    }
}
