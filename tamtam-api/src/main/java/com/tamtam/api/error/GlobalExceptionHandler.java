package com.tamtam.api.error;

import com.tamtam.api.error.code.ErrorCode;
import com.tamtam.api.error.code.ResponseEnumType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected @NonNull ResponseEntity<Object> handleMethodArgumentNotValid(
        @NonNull MethodArgumentNotValidException ex,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatusCode status,
        @NonNull WebRequest request
    ) {
        ApiValidErrorResponse response = ApiValidErrorResponse.of(ex.getBindingResult(), ErrorCode.VALIDATION_ERROR);
        return buildErrorResponseEntity(ex, response, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(BindException.class)
    protected ResponseEntity<Object> handleBindException(
        BindException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        logError(request, ex);
        ApiValidErrorResponse response = ApiValidErrorResponse.of(ex.getBindingResult(), ErrorCode.VALIDATION_ERROR);
        return buildErrorResponseEntity(ex, response, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleValidationException(ConstraintViolationException e, WebRequest request) {
        logError(request, e);
        BindingResult bindingResult = createBindingResultFromConstraintViolations(e);
        ApiValidErrorResponse response = ApiValidErrorResponse.of(bindingResult, ErrorCode.VALIDATION_ERROR);
        return buildErrorResponseEntity(e, response, HttpStatus.BAD_REQUEST, request);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e, WebRequest request) {
        return buildSimpleErrorResponse(e, ErrorCode.BAD_REQUEST, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e, WebRequest request) {
        return buildSimpleErrorResponse(e, ErrorCode.BAD_REQUEST, request);
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException e, WebRequest request) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다.");
        return buildErrorResponseEntity(e, response, HttpStatus.UNAUTHORIZED, request);
    }


    @Override
    protected @NonNull ResponseEntity<Object> handleHttpRequestMethodNotSupported(
        @NonNull HttpRequestMethodNotSupportedException e,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatusCode status,
        @NonNull WebRequest request
    ) {
        return buildSimpleErrorResponse(e, ErrorCode.BAD_REQUEST, request);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFound(UsernameNotFoundException e, WebRequest request) {
        return buildSimpleErrorResponse(e, ErrorCode.UNAUTHORIZED, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception e, WebRequest request) {
        return buildSimpleErrorResponse(e, ErrorCode.INTERNAL_ERROR, request);
    }


    // Helper Methods
    private void logError(WebRequest request, Exception e) {
        log.error("Context: {}, Exception: {}", request.getDescription(false), e.getMessage(), e);
    }

    private BindingResult createBindingResultFromConstraintViolations(ConstraintViolationException e) {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "objectName");
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            FieldError fieldError = new FieldError(
                violation.getLeafBean().getClass().getSimpleName(),
                violation.getPropertyPath().toString(),
                violation.getInvalidValue(),
                false,
                new String[]{violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName()},
                null,
                violation.getMessage()
            );
            bindingResult.addError(fieldError);
        }
        return bindingResult;
    }

    private ResponseEntity<Object> buildErrorResponseEntity(
        Exception e,
        ErrorResponse response,
        HttpStatus status,
        WebRequest request
    ) {
        log.error("Error: {}, Status: {}", response, status, e);
        return super.handleExceptionInternal(e, response, HttpHeaders.EMPTY, status, request);
    }

    private ResponseEntity<Object> buildSimpleErrorResponse(Exception e, ResponseEnumType errorCode, WebRequest request) {
        ErrorResponse response = ErrorResponse.of(errorCode, errorCode.getMessage());
        return buildErrorResponseEntity(e, response, errorCode.getStatus(), request);
    }
}

