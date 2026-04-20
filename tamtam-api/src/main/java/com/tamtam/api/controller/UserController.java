package com.tamtam.api.controller;

import com.tamtam.api.controller.dto.LoginRequest;
import com.tamtam.api.controller.dto.RefreshTokenRequest;
import com.tamtam.api.controller.dto.TokenResponse;
import com.tamtam.api.controller.dto.UserRegisterRequest;
import com.tamtam.api.service.UserService;
import com.tamtam.api.service.dto.TokenResult;
import com.tamtam.api.service.dto.UserRegisterCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
        @Valid @RequestBody UserRegisterRequest request
    ) {
        userService.signup(UserRegisterCommand.from(request));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return createAuthResponseEntity(userService.login(request.email(), request.password()));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshAccessToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        return createAuthResponseEntity(userService.reissueAccessToken(refreshTokenRequest.refreshToken()));
    }

    private ResponseEntity<TokenResponse> createAuthResponseEntity(TokenResult token) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken());
        return ResponseEntity.ok().headers(responseHeaders).body(TokenResponse.from(token));
    }
}
