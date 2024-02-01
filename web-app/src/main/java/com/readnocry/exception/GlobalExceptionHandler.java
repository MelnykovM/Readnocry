package com.readnocry.exception;

import lombok.extern.log4j.Log4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
@Log4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(Exception e) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("errorPage");
        modelAndView.addObject("message", "Something went wrong.");
        log.error("Unexpected error: " + e.getMessage(), e);
        return modelAndView;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ModelAndView handleUserNotFoundException(Exception e) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("errorPage");
        modelAndView.addObject("message", "UserNotFoundException.");
        log.error("User not found: " + e.getMessage(), e);
        return modelAndView;
    }

    @ExceptionHandler(AuthorizedUserNotFoundException.class)
    public ModelAndView handleAuthorizedUserNotFoundException(Exception e) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("errorPage");
        modelAndView.addObject("message", "Something went wrong. Please login again.");
        log.error("Authorized user not found: " + e.getMessage(), e);
        return modelAndView;
    }
}
