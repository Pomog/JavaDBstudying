package yp.peopledb.repository.exeption;

public class UnableToSaveException extends RuntimeException{
    public UnableToSaveException(String message) {
        super(message);
    }
}
