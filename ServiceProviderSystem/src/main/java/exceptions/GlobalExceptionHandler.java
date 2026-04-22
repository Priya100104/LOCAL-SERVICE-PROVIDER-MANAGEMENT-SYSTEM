package exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 *  CO5 - Global Exception Handler
 *  @ControllerAdvice catches exceptions from ALL controllers
 *  Returns JSON for REST requests, HTML page for web requests
 * ============================================================
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // ===== REST: ProviderNotFoundException → 404 =====
    @ExceptionHandler(ProviderNotFoundException.class)
    public Object handleProviderNotFound(ProviderNotFoundException ex,
                                          jakarta.servlet.http.HttpServletRequest request,
                                          Model model) {
        // If request wants JSON (REST API call)
        if (isApiRequest(request)) {
            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", LocalDateTime.now().toString());
            error.put("status",    404);
            error.put("error",     "Not Found");
            error.put("message",   ex.getMessage());
            error.put("path",      request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        // If request wants HTML (web browser call)
        model.addAttribute("errorCode",    "404");
        model.addAttribute("errorTitle",   "Provider Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("backLink",     "/web/providers");
        return "error";
    }

    // ===== REST: InvalidRatingException → 400 =====
    @ExceptionHandler(InvalidRatingException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRating(InvalidRatingException ex,
                                                                     jakarta.servlet.http.HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status",    400);
        error.put("error",     "Bad Request");
        error.put("message",   ex.getMessage());
        error.put("path",      request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ===== REST: DuplicateProviderException → 409 =====
    @ExceptionHandler(DuplicateProviderException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateProviderException ex,
                                                                 jakarta.servlet.http.HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status",    409);
        error.put("error",     "Conflict");
        error.put("message",   ex.getMessage());
        error.put("path",      request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // ===== Catch-all: Any other exception → 500 =====
    @ExceptionHandler(Exception.class)
    public Object handleGeneral(Exception ex,
                                  jakarta.servlet.http.HttpServletRequest request,
                                  Model model) {
        if (isApiRequest(request)) {
            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", LocalDateTime.now().toString());
            error.put("status",    500);
            error.put("error",     "Internal Server Error");
            error.put("message",   ex.getMessage());
            error.put("path",      request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
        model.addAttribute("errorCode",    "500");
        model.addAttribute("errorTitle",   "Something went wrong");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("backLink",     "/web/providers");
        return "error";
    }

    // Helper: detect if request came from REST client or browser
    private boolean isApiRequest(jakarta.servlet.http.HttpServletRequest request) {
        String uri    = request.getRequestURI();
        String accept = request.getHeader("Accept");
        return uri.startsWith("/api/") ||
               (accept != null && accept.contains("application/json"));
    }
}
