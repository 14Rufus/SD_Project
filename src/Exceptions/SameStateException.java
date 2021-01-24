package Exceptions;

/**
 * Exception lançada quando o estado de infeção introduzida é igual à anterior
 */
public class SameStateException extends Exception {
    public SameStateException(String message) {
        super(message);
    }
}
