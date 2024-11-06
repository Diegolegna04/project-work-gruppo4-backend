package com.example.rest.exception;

import com.example.service.exception.EmailNotAvailable;
import com.example.service.exception.EmailNotVerified;
import com.example.service.exception.TelephoneNotAvailable;
import com.example.service.exception.WrongUsernameOrPasswordException;
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
        }

        // Default Response
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Si è verificato un errore imprevisto.")
                .type("text/plain")
                .build();
    }
}
