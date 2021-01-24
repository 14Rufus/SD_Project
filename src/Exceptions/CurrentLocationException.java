package Exceptions;

/**
 * Exception que lançada quando a localização é inválida
 */
public class CurrentLocationException extends Exception {
    public CurrentLocationException(String message) {
        super(message);
    }
}
