package com.tamtam.batch.job.dto;

import java.math.BigDecimal;

public record TourAttractionUpsertCommand(
    String contentId,
    String contentTypeId,
    String title,
    String addr1,
    String addr2,
    String zipcode,
    String tel,
    String firstImage,
    String firstImage2,
    BigDecimal mapX,
    BigDecimal mapY,
    String mlevel,
    String areaCode,
    String sigunguCode,
    String lDongRegnCd,
    String lDongRegnNm,
    String lDongSignguCd,
    String lDongSignguNm,
    String cat1,
    String cat2,
    String cat3,
    String createdTime,
    String modifiedTime
) {
}
