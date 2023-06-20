package yp.peopledb.repository.exeption;

import java.sql.SQLException;

public class DataException extends RuntimeException{
    public DataException(String msg, Throwable e) {
        super(msg, e);
    }
}
