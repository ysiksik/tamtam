package com.tamtam.api.error.code;

import org.springframework.http.HttpStatus;

public interface ResponseEnumType{

    String getCode();

    HttpStatus getStatus();

    String getMessage();

}
