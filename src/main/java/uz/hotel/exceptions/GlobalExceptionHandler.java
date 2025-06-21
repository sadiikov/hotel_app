package uz.hotel.exceptions;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OperationCannotBeDoneException.class)
    public String handleOperationCannotBeDone(OperationCannotBeDoneException ex, Model model) {
        model.addAttribute("statusCode", 400);
        model.addAttribute("message", ex.getMessage());
        return "error";
    }
    @ExceptionHandler(DuplicateKeyException.class)
    public String handleDuplicateKeyException(DuplicateKeyException ex, Model model) {
        model.addAttribute("message", "This email is already registered.");
        model.addAttribute("statusCode", 409); // Conflict
        return "error";
    }
    @ExceptionHandler(DataNotFoundException.class)
    public String handleDataNotFound(DataNotFoundException ex, Model model) {
        model.addAttribute("statusCode", 404);
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("statusCode", 500);
        model.addAttribute("message", "Internal server error");
        return "error";
    }
}
