package com.tamtam.core.infrastructure.tour.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DetailCommonResponse(
    String responseTime,
    String resultCode,
    String resultMsg,
    TourApiEnvelope response
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TourApiEnvelope(
        Header header,
        Body body
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(
        String resultCode,
        String resultMsg
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Body(
        Items items,
        int numOfRows,
        int pageNo,
        int totalCount
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Items(
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<DetailCommonItem> item
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DetailCommonItem(
        String contentid,
        String contenttypeid,
        String homepage,
        String overview,
        String tel,
        String title,
        String addr1,
        String addr2,
        String firstimage,
        String firstimage2,
        String mapx,
        String mapy,
        String modifiedtime
    ) {
    }
}
