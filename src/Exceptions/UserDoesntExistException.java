package Exceptions;

public class UserDoesntExistException extends Exception {
    public UserDoesntExistException(String message) {
        super(message);
    }
}
