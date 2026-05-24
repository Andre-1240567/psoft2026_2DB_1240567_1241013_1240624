package pt.isep.psoft.alsafe.shared.exceptions;

/**
 * Thrown when a requested resource cannot be found.
 * Mapped to HTTP 404 Not Found by GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}