package com.tamtam.api.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;


public record LoginRequest(
    @NotEmpty
    @Email(message = "잘못된 이메일 형식")
    String email,
    @NotEmpty
    String password
) {

}
