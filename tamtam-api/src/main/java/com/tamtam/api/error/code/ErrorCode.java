package com.tamtam.api.error.code;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements ResponseEnumType {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD REQUEST"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION ERRORS"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE NOT FOUND"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE EMAIL"),
    NO_CHANGE(HttpStatus.CONFLICT, "DUPLICATE EMAIL"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL ERRORS"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED ACCESS"),;

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }

}
