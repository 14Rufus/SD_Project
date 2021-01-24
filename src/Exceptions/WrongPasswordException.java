package Exceptions;

/**
 * Exceção lançada quando a password do Utilizador não é válida
 */
public class WrongPasswordException extends Exception {
    public WrongPasswordException(String message) {
        super(message);
    }
}