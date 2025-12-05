package org.com.smartpayments.subscription.application.config.exception;

import org.com.smartpayments.subscription.core.common.base.BadRequestException;
import org.com.smartpayments.subscription.core.common.base.ConflictException;
import org.com.smartpayments.subscription.core.common.base.ForbiddenException;
import org.com.smartpayments.subscription.core.common.base.NotFoundException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.util.ObjectUtils.isEmpty;


@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        HashMap<String, List<String>> errorsMap = new LinkedHashMap<>();

        ex.getAllErrors().forEach(objectError -> {
            String fieldName = ((DefaultMessageSourceResolvable) objectError.getArguments()[0]).getDefaultMessage();

            if (errorsMap.containsKey(fieldName)) {
                List<String> currErrorsForField = new ArrayList<>(errorsMap.get(fieldName));
                currErrorsForField.add(objectError.getDefaultMessage());
                errorsMap.put(fieldName, currErrorsForField);
            } else if (!isEmpty(objectError.getDefaultMessage())) {
                errorsMap.put(fieldName, List.of(objectError.getDefaultMessage()));
            }
        });

        return new ResponseEntity<>(mapErrors(ex, errorsMap, HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException ex) {
        return new ResponseEntity<>(mapErrors(ex, HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflictException(ConflictException ex) {
        return new ResponseEntity<>(mapErrors(ex, HttpStatus.CONFLICT.value()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(NotFoundException ex) {
        return new ResponseEntity<>(mapErrors(ex, HttpStatus.NOT_FOUND.value()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbiddenException(ForbiddenException ex) {
        return new ResponseEntity<>(mapErrors(ex, HttpStatus.FORBIDDEN.value()), HttpStatus.FORBIDDEN);
    }

    private Map<String, Object> mapErrors(Exception ex, Map<String, List<String>> errors, int statusValue) {
        return new LinkedHashMap<>() {{
            put("date", new Date());
            put("status", statusValue);
            put("message", errors);
            put("exception", ex.getClass().getSimpleName());
        }};
    }

    private Map<String, Object> mapErrors(Exception ex, int statusValue) {
        return new LinkedHashMap<>() {{
            put("date", new Date());
            put("status", statusValue);
            put("message", ex.getMessage());
            put("exception", ex.getClass().getSimpleName());
        }};
    }
}
