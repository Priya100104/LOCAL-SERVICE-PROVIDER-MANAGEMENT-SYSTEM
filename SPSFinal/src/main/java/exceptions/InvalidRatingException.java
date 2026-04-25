package exceptions;

/**
 * Invalid Rating Exception
 * Thrown when review rating is outside 1-5 range
 */
public class InvalidRatingException extends RuntimeException {
    public InvalidRatingException(int rating) {
        super("Invalid rating: " + rating + ". Rating must be between 1 and 5.");
    }
}
