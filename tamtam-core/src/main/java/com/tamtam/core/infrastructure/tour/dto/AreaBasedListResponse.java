package com.tamtam.core.infrastructure.tour.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AreaBasedListResponse(
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
        List<AreaBasedListItem> item
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AreaBasedListItem(
        String addr1,
        String addr2,
        String areacode,
        String sigungucode,
        String cat1,
        String cat2,
        String cat3,
        String contentid,
        String contenttypeid,
        String createdtime,
        String firstimage,
        String firstimage2,
        String mapx,
        String mapy,
        String mlevel,
        String modifiedtime,
        String tel,
        String title,
        String zipcode,
        @JsonProperty("lDongRegnCd") String lDongRegnCd,
        @JsonProperty("lDongRegnNm") String lDongRegnNm,
        @JsonProperty("lDongSignguCd") String lDongSignguCd,
        @JsonProperty("lDongSignguNm") String lDongSignguNm
    ) {
    }
}
