package com.tamtam.api.controller.dto;

import com.tamtam.api.service.dto.TokenResult;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record TokenResponse(
    String accessToken,
    String refreshToken
) {

    public static TokenResponse from(TokenResult result) {
        return TokenResponse.builder()
                            .accessToken(result.accessToken())
                            .refreshToken(result.refreshToken())
                            .build();
    }
}
