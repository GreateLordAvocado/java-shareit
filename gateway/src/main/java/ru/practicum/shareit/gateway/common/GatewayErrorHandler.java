package ru.practicum.shareit.gateway.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GatewayErrorHandler {

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<Object> handleDownstream(HttpStatusCodeException ex) {
        var status = ex.getStatusCode(); // в Spring 6 это HttpStatusCode
        String body = ex.getResponseBodyAsString();
        if (body == null || body.isBlank()) {
            body = "{\"error\":\"Downstream error\"}";
        }
        log.debug("Proxy error from server: {} {}", status, body);
        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(ResourceAccessException.class)
    public ErrorResponse handleServerUnavailable(ResourceAccessException ex) {
        log.warn("503 Downstream server unavailable: {}", ex.getMessage());
        return new ErrorResponse("Сервер недоступен");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ErrorResponse handleMissingHeader(MissingRequestHeaderException ex) {
        String header = ex.getHeaderName();
        String msg = "Отсутствует обязательный заголовок: " + (header == null ? "unknown" : header);
        log.debug("400 MissingRequestHeader: {}", msg);
        return new ErrorResponse(msg);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class
    })
    public ErrorResponse handleBeanValidation(Exception ex) {
        String message;

        if (ex instanceof MethodArgumentNotValidException manve) {
            message = manve.getBindingResult().getFieldErrors().stream()
                    .map(this::formatFieldError)
                    .collect(Collectors.joining("; "));
        } else if (ex instanceof BindException be) {
            message = be.getBindingResult().getFieldErrors().stream()
                    .map(this::formatFieldError)
                    .collect(Collectors.joining("; "));
        } else {
            message = "Некорректные входные данные";
        }

        if (message.isBlank()) {
            message = "Некорректные входные данные";
        }
        log.debug("400 BeanValidation: {}", message);
        return new ErrorResponse(message);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(this::formatConstraintViolation)
                .collect(Collectors.joining("; "));
        if (message.isBlank()) message = "Нарушение ограничений параметров запроса";
        log.debug("400 ConstraintViolation: {}", message);
        return new ErrorResponse(message);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ErrorResponse handleMissingParam(MissingServletRequestParameterException ex) {
        String message = "Отсутствует обязательный параметр: " + ex.getParameterName();
        log.debug("400 MissingParam: {}", message);
        return new ErrorResponse(message);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String required = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String message = "Некорректный тип параметра '" + name + "', ожидается " + required;
        log.debug("400 TypeMismatch: {}", message);
        return new ErrorResponse(message);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleNotReadable(HttpMessageNotReadableException ex) {
        String root = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        String message = "Некорректное тело запроса (JSON): " + (root == null ? "" : root);
        log.debug("400 NotReadable: {}", message);
        return new ErrorResponse(message);
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ErrorResponse handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        String message = "Метод не поддерживается для этого ресурса";
        log.debug("405 MethodNotAllowed: {}", ex.getMessage());
        return new ErrorResponse(message);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleAny(Exception ex) {
        log.error("500 Internal error", ex);
        return new ErrorResponse("Внутренняя ошибка сервера");
    }

    private String formatFieldError(FieldError fe) {
        String field = fe.getField();
        String msg = Objects.toString(fe.getDefaultMessage(), "ошибка");
        return field + ": " + msg;
    }

    private String formatConstraintViolation(ConstraintViolation<?> v) {
        String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "<param>";
        String msg = Objects.toString(v.getMessage(), "ошибка");
        return path + ": " + msg;
    }

    @Value
    public static class ErrorResponse {
        String error;
    }
}
