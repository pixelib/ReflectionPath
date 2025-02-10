package dev.pixelib.reflectionpath.errors;

/**
 * Exception class for reflection-related errors.
 */
public class ReflectionException extends RuntimeException {
    /**
     * Constructs a new ReflectionException with the specified message.
     *
     * @param message The error message
     */
    public ReflectionException(String message) {
        super(message);
    }

    /**
     * Constructs a new ReflectionException with the specified message and cause.
     *
     * @param message The error message
     * @param cause The cause of the exception
     */
    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
