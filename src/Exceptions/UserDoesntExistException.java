package Exceptions;

/**
 * Exceção lançada quando há a tentativa de fazer login com um Utilizador inexistente
 */
public class UserDoesntExistException extends Exception {
    public UserDoesntExistException(String message) {
        super(message);
    }
}
