package com.tamtam.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tour-api")
public record TourApiProperties(
    String baseUrl,
    String serviceKey,
    String mobileOs,
    String mobileApp,
    String type,
    String arrange,
    String contentTypeId,
    String lDongRegnCd,
    int numOfRows,
    String detailImageYn
) {
}
