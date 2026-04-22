package exceptions;

/**
 * Duplicate Provider Exception
 * Thrown when registering a provider with an existing username
 */
public class DuplicateProviderException extends RuntimeException {
    public DuplicateProviderException(String username) {
        super("Provider with username '" + username + "' already exists.");
    }
}
