package com.gringosexy.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFoundException(ResourceNotFoundException ex, Model model) {
        log.warn("Resource not found: {}", ex.getMessage());
        model.addAttribute("errorCode", 404);
        model.addAttribute("errorTitle", "Recurso No Encontrado");
        model.addAttribute("errorMessage", ex.getMessage());
        return "errors/404";
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFoundException(Exception ex, Model model) {
        log.debug("Static resource or route not found: {}", ex.getMessage());
        model.addAttribute("errorCode", 404);
        model.addAttribute("errorTitle", "Página No Encontrada");
        model.addAttribute("errorMessage", "El recurso o página a la que intentas acceder no existe.");
        return "errors/404";
    }

    @ExceptionHandler({UnauthorizedException.class, AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDeniedException(Exception ex, Model model) {
        log.warn("Access denied: {}", ex.getMessage());
        model.addAttribute("errorCode", 403);
        model.addAttribute("errorTitle", "Acceso Restringido");
        model.addAttribute("errorMessage", "No tienes permisos suficientes o tu dispositivo no está habilitado para ver este recurso.");
        return "errors/403";
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(ValidationException ex, Model model) {
        log.warn("Validation error: {}", ex.getMessage());
        model.addAttribute("errorCode", 400);
        model.addAttribute("errorTitle", "Solicitud Inválida");
        model.addAttribute("errorMessage", ex.getMessage());
        return "errors/500";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception ex, Model model) {
        // Suppress benign client disconnects (e.g. user closes tab or cancels media stream download)
        String exName = ex.getClass().getName();
        String message = ex.getMessage() != null ? ex.getMessage() : "";
        if (exName.contains("ClientAbortException") || message.contains("Broken pipe") || message.contains("connection was aborted")) {
            log.debug("Client closed media connection early (Broken pipe / ClientAbortException)");
            return null;
        }

        log.error("Internal server error: ", ex);
        model.addAttribute("errorCode", 500);
        model.addAttribute("errorTitle", "Error Interno del Servidor");
        model.addAttribute("errorMessage", "Ha ocurrido un error inesperado al procesar tu solicitud. Nuestro equipo ha sido notificado.");
        return "errors/500";
    }
}
