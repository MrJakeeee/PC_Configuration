package kg.kstu.pcconfiguration.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({
            IllegalArgumentException.class,
            MissingServletRequestParameterException.class
    })
    public String handleUserError(Exception ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error";
    }
}
