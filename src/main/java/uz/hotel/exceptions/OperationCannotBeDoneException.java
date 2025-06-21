package uz.hotel.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OperationCannotBeDoneException extends RuntimeException {
    public OperationCannotBeDoneException() {
        super("Operation cannot be done");
    }

    public OperationCannotBeDoneException(String message) {
        super(message);
    }
}

