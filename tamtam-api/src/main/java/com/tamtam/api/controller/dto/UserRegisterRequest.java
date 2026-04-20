package com.tamtam.api.controller.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;


public record UserRegisterRequest(
    @Email(message = "잘못된 이메일 형식")
    @NotEmpty(message = "사용자의 아이디는 필수입니다.")
    String email,
    @NotEmpty(message = "사용자의 비밀번호는 필수입니다.")
    String password
) {


}
