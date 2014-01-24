package com.rivermeadow.scheduler.web;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

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
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public @ResponseBody ResponseEntity handleException(MethodArgumentNotValidException e) {
        List<Map<String, String>> errors = Lists.newArrayList();
        for(FieldError objectError: e.getBindingResult().getFieldErrors()) {
            //TODO(jinloes) use message resolver
            errors.add(ImmutableMap.of(objectError.getField(), objectError.getDefaultMessage()));
        }
        return new ResponseEntity<>(ImmutableMap.of("errors", errors), HttpStatus.NOT_ACCEPTABLE);
    }
}
