package com.rivermeadow.scheduler.web;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.rivermeadow.api.validation.UriValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Global exception handler used by all controllers.
 */
@ControllerAdvice
public class SchedulerExceptionHandler {
    private final MessageSource messageSource;

    @Autowired
    public SchedulerExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public @ResponseBody ResponseEntity handleValidationFailure(MethodArgumentNotValidException e) {
        List<Map<String, String>> errors = Lists.newArrayList();
        for(FieldError fieldError: e.getBindingResult().getFieldErrors()) {
            String errorMessage = resolveLocalizedErrorMessage(fieldError);
            errors.add(ImmutableMap.of("field", fieldError.getField(), "message", errorMessage));
        }
        return new ResponseEntity<>(ImmutableMap.of("errors", errors), HttpStatus.NOT_ACCEPTABLE);
    }

    private String resolveLocalizedErrorMessage(FieldError fieldError) {
        Locale currentLocale =  LocaleContextHolder.getLocale();
        return messageSource.getMessage(fieldError, currentLocale);
    }

    @ExceptionHandler(DataRetrievalFailureException.class)
    public @ResponseBody ResponseEntity handleDataRetrievalFailure(DataRetrievalFailureException e) {
        return new ResponseEntity<>("The requested resource could not be found.", HttpStatus.NOT_FOUND);
    }
}
