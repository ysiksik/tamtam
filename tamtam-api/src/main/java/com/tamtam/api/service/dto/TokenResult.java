package com.tamtam.api.service.dto;

public record TokenResult(
    String accessToken,
    String refreshToken
) {
}
