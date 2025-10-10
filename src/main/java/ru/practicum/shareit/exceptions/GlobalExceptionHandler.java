package ru.practicum.shareit.exceptions;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 400: бизнес-валидация
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(ValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return new ErrorResponse(ex.getMessage());
    }

    // 409: конфликт
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(ConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return new ErrorResponse(ex.getMessage());
    }

    // 404: не найдено
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return new ErrorResponse(ex.getMessage());
    }

    // 400: сгруппированные "плохие запросы" и ошибки валидации фреймворка
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestExceptions(Exception ex) {
        String msg;

        if (ex instanceof HttpMessageNotReadableException) {
            msg = "Некорректное тело запроса";
        } else if (ex instanceof MethodArgumentNotValidException manv) {
            msg = manv.getBindingResult().getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            msg = "Ошибка валидации: " + msg;
        } else if (ex instanceof ConstraintViolationException cve) {
            msg = cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            msg = "Ошибка валидации: " + msg;
        } else if (ex instanceof MissingRequestHeaderException mrh) {
            msg = "Отсутствует обязательный заголовок: " + mrh.getHeaderName();
        } else if (ex instanceof MissingServletRequestParameterException msp) {
            msg = "Отсутствует обязательный параметр: " + msp.getParameterName();
        } else if (ex instanceof MethodArgumentTypeMismatchException mat) {
            String name = mat.getName();
            String required = mat.getRequiredType() != null
                    ? mat.getRequiredType().getSimpleName()
                    : "неизвестно";
            msg = "Неверный формат параметра '" + name + "'. Ожидается: " + required;
        } else {
            msg = "Некорректный запрос";
        }

        log.warn("Bad request ({}): {}", ex.getClass().getSimpleName(), msg);
        return new ErrorResponse(msg);
    }

    // Прямые ResponseStatusException — сохраняем код статуса
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        HttpStatusCode code = ex.getStatusCode();
        String msg = ex.getReason() != null ? ex.getReason() : "Ошибка запроса";
        if (code.is4xxClientError()) {
            log.warn("ResponseStatusException {}: {}", code.value(), msg);
        } else {
            log.error("ResponseStatusException {}: {}", code.value(), msg, ex);
        }
        return ResponseEntity.status(code).body(new ErrorResponse(msg));
    }

    // 500: всё остальное
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOther(Throwable ex) {
        log.error("Unexpected error", ex);
        return new ErrorResponse("Внутренняя ошибка сервера");
    }
}
