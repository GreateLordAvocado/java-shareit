package ru.practicum.shareit.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, HttpServletRequest req) {
        return body(status, req, status.getReasonPhrase());
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status,
                                                     HttpServletRequest req,
                                                     String message) {
        Map<String, Object> m = new HashMap<>();
        m.put("timestamp", OffsetDateTime.now().toString());
        m.put("status", status.value());
        m.put("error", message != null ? message : status.getReasonPhrase());
        m.put("path", req.getRequestURI());
        return ResponseEntity.status(status).body(m);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(ValidationException ex, HttpServletRequest req) {
        log.warn("Validation error: {}", ex.getMessage());
        return body(HttpStatus.BAD_REQUEST, req, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex, HttpServletRequest req) {
        log.warn("Conflict: {}", ex.getMessage());
        return body(HttpStatus.CONFLICT, req, ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        log.warn("Not found: {}", ex.getMessage());
        return body(HttpStatus.NOT_FOUND, req, ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex, HttpServletRequest req) {
        log.warn("Forbidden: {}", ex.getMessage());
        return body(HttpStatus.FORBIDDEN, req, ex.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<Map<String, Object>> handleBadRequestExceptions(Exception ex, HttpServletRequest req) {
        String message;

        if (ex instanceof MethodArgumentNotValidException manv) {
            message = manv.getBindingResult().getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            if (message.isBlank()) message = "Некорректный запрос";
        } else if (ex instanceof ConstraintViolationException cve) {
            message = cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            if (message.isBlank()) message = "Некорректный запрос";
        } else if (ex instanceof MissingRequestHeaderException mrh) {
            message = "Отсутствует обязательный заголовок: " + mrh.getHeaderName();
        } else if (ex instanceof MissingServletRequestParameterException msp) {
            message = "Отсутствует обязательный параметр: " + msp.getParameterName();
        } else if (ex instanceof MethodArgumentTypeMismatchException mat) {
            String required = mat.getRequiredType() != null ? mat.getRequiredType().getSimpleName() : "неизвестно";
            message = "Неверный формат параметра '" + mat.getName() + "'. Ожидается: " + required;
        } else if (ex instanceof HttpMessageNotReadableException) {
            message = "Некорректное тело запроса";
        } else {
            message = "Некорректный запрос";
        }

        log.warn("Bad request ({}): {}", ex.getClass().getSimpleName(), message);
        return body(HttpStatus.BAD_REQUEST, req, message);
    }

    @ExceptionHandler(jakarta.validation.ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleFrameworkValidation(jakarta.validation.ValidationException ex,
                                                                         HttpServletRequest req) {
        log.warn("Bean Validation error: {}", ex.getMessage());
        return body(HttpStatus.BAD_REQUEST, req, ex.getMessage());
    }

    @ExceptionHandler({ IllegalArgumentException.class, NoSuchElementException.class })
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(RuntimeException ex, HttpServletRequest req) {
        log.warn("Bad request ({}): {}", ex.getClass().getSimpleName(), ex.getMessage());
        String msg = (ex.getMessage() == null || ex.getMessage().isBlank())
                ? "Некорректный запрос" : ex.getMessage();
        return body(HttpStatus.BAD_REQUEST, req, msg);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                   HttpServletRequest req) {
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        log.warn("Data integrity violation: {}", msg);
        return body(HttpStatus.CONFLICT, req, "Нарушение целостности данных");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex,
                                                                    HttpServletRequest req) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        String msg = ex.getReason();
        if (status.is4xxClientError()) {
            log.warn("ResponseStatus {}: {}", status.value(), msg);
        } else {
            log.error("ResponseStatus {}: {}", status.value(), msg, ex);
        }
        return body(status, req, msg);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Map<String, Object>> handleOther(Throwable ex, HttpServletRequest req) {
        log.error("Unexpected error", ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, req, "Внутренняя ошибка сервера");
    }
}
