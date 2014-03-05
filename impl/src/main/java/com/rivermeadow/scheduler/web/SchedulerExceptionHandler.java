package com.rivermeadow.scheduler.web;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.rivermeadow.api.exception.MessageArgumentException;
import com.rivermeadow.api.exception.ResponseStatusException;
import com.rivermeadow.api.model.ErrorMessageDTO;
import com.rivermeadow.api.model.FieldErrorDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerExceptionHandler.class);
    private final MessageSource messageSource;

    @Autowired
    public SchedulerExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public @ResponseBody ResponseEntity handleValidationFailure(MethodArgumentNotValidException e) {
        LOGGER.error("Validation failed", e);
        List<FieldErrorDTO> errors = Lists.newArrayList();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            String errorMessage = resolveLocalizedErrorMessage(fieldError);
            errors.add(FieldErrorDTO.builder()
                    .withField(fieldError.getField())
                    .withMessage(errorMessage)
                    .build());
        }
        return new ResponseEntity<>(ImmutableMap.of("errors", errors), HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(MessageArgumentException.class)
    public @ResponseBody ResponseEntity handleMessageArgumentException(MessageArgumentException e) {
        String errorMessage = resolveLocalizedErrorMessage(e.getMessage(), e.getArgs());
        LOGGER.error(errorMessage, e);
        return new ResponseEntity<>(new ErrorMessageDTO(errorMessage), e.getHttpStatus());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public @ResponseBody ResponseEntity<ErrorMessageDTO> handleResponseStatusException(
            ResponseStatusException e) {
        String errorMessage = resolveLocalizedErrorMessage(e.getMessage(), null);
        LOGGER.error(errorMessage, e);
        return new ResponseEntity<>(new ErrorMessageDTO(errorMessage), e.getHttpStatus());
    }

    @ExceptionHandler(Throwable.class)
    public @ResponseBody ResponseEntity<ErrorMessageDTO> handleThrowable(Throwable e) {
        String errorMessage = resolveLocalizedErrorMessage(e.getMessage(), null);
        LOGGER.error(errorMessage, e);
        return new ResponseEntity<>(new ErrorMessageDTO(errorMessage),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String resolveLocalizedErrorMessage(FieldError fieldError) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(fieldError, currentLocale);
    }

    private String resolveLocalizedErrorMessage(String code, Object[] args) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, currentLocale);
    }
}