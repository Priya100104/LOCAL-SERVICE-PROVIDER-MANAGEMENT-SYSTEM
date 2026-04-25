package exceptions;

/**
 * Provider Not Found Exception
 * Thrown when a provider ID doesn't exist in the database
 */
public class ProviderNotFoundException extends RuntimeException {

    private final Long providerId;

    public ProviderNotFoundException(Long providerId) {
        super("Provider not found with ID: " + providerId);
        this.providerId = providerId;
    }

    public ProviderNotFoundException(String message) {
        super(message);
        this.providerId = null;
    }

    public Long getProviderId() { return providerId; }
}
