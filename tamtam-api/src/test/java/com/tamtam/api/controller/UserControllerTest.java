package com.tamtam.api.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tamtam.api.controller.dto.LoginRequest;
import com.tamtam.api.controller.dto.RefreshTokenRequest;
import com.tamtam.api.controller.dto.UserRegisterRequest;
import com.tamtam.api.global.config.SpringSecurityConfig;
import com.tamtam.api.service.UserService;
import com.tamtam.api.service.dto.TokenResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(
    controllers = UserController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SpringSecurityConfig.class)
)
@DisplayName("UserController 테스트")
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private UserService userService;

    @Test
    void signup_authenticated_shouldSucceed() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("user@example.com", "Password123");

        mockMvc.perform(post("/signup")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk());

        Mockito.verify(userService).signup(any());
    }

    @Test
    void signup_shouldFail_whenEmailInvalid() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("invalid-email", "Password123");

        mockMvc.perform(post("/signup")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void signup_shouldFail_whenPasswordBlank() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("user@example.com", "");

        mockMvc.perform(post("/signup")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void signup_shouldFail_whenEmailDuplicate() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("user@example.com", "Password123");

        Mockito.doThrow(new IllegalArgumentException("이미 사용 중인 이메일입니다."))
               .when(userService).signup(any());

        mockMvc.perform(post("/signup")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void login_authenticated_shouldSucceed() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "Password123");
        TokenResult tokenResult = new TokenResult("access-token", "refresh-token");

        given(userService.login(anyString(), anyString())).willReturn(tokenResult);

        mockMvc.perform(post("/login")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
               .andExpect(jsonPath("$.accessToken").value("access-token"))
               .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void login_shouldFail_whenCredentialsInvalid() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "wrongPassword");

        given(userService.login(anyString(), anyString()))
            .willThrow(new IllegalArgumentException("Invalid credentials"));

        mockMvc.perform(post("/login")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void refreshToken_authenticated_shouldSucceed() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        TokenResult tokenResult = new TokenResult("new-access-token", "refresh-token");

        given(userService.reissueAccessToken(anyString())).willReturn(tokenResult);

        mockMvc.perform(post("/refresh-token")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer new-access-token"))
               .andExpect(jsonPath("$.accessToken").value("new-access-token"))
               .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void refreshToken_shouldFail_whenTokenInvalid() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");

        given(userService.reissueAccessToken(anyString()))
            .willThrow(new IllegalArgumentException("Invalid refresh token"));

        mockMvc.perform(post("/refresh-token")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest());
    }
}
