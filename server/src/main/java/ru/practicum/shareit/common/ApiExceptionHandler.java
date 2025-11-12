package ru.practicum.shareit.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;

import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    public Map<String, Object> handleValidation(ValidationException ex, HttpServletRequest req) {
        log.debug("400 {} {}", req.getRequestURI(), ex.getMessage());
        return Map.of("error", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException.class)
    public Map<String, Object> handleForbidden(ForbiddenException ex, HttpServletRequest req) {
        log.debug("403 {} {}", req.getRequestURI(), ex.getMessage());
        return Map.of("error", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ NotFoundException.class, NoSuchElementException.class })
    public Map<String, Object> handleNotFound(Exception ex, HttpServletRequest req) {
        log.debug("404 {} {}", req.getRequestURI(), ex.getMessage());
        return Map.of("error", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({ ConflictException.class, DataIntegrityViolationException.class })
    public Map<String, Object> handleConflict(Exception ex, HttpServletRequest req) {
        log.debug("409 {} {}", req.getRequestURI(), ex.getMessage());
        String message = (ex instanceof DataIntegrityViolationException)
                ? "Conflict"
                : ex.getMessage();
        return Map.of("error", message);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Throwable.class)
    public Map<String, Object> handleAny(Throwable ex, HttpServletRequest req) {
        log.error("500 {} {}", req.getRequestURI(), ex.toString(), ex);
        return Map.of("error", "Internal Server Error");
    }
}
