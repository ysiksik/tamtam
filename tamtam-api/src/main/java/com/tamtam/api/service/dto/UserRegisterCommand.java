package com.tamtam.api.service.dto;


import com.tamtam.api.controller.dto.UserRegisterRequest;
import com.tamtam.core.domain.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;


@Getter
@Builder(access = AccessLevel.PRIVATE)
public class UserRegisterCommand {

    @Email(message = "잘못된 이메일 형식")
    @NotEmpty(message = "사용자의 아이디는 필수입니다.")
    private String email;

    @NotEmpty(message = "사용자의 비밀번호는 필수입니다.")
    private String password;

    public static UserRegisterCommand from(UserRegisterRequest request) {
        return UserRegisterCommand.builder()
                                    .email(request.email())
                                    .password(request.password())
                                    .build();
    }
    public User toEntity(PasswordEncoder passwordEncoder) {
        return User.builder()
                   .email(email)
                   .password(passwordEncoder.encode(password)) // ✅ 안전하게 인코딩
                   .build();
    }
}
