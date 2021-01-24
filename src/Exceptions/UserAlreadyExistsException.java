package Exceptions;

/**
 * Exceção lançada quando o utilizador a ser registado já existe
 */
public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
