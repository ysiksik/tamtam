package com.tamtam.core.infrastructure.tour;

import com.tamtam.core.infrastructure.tour.dto.AreaBasedListResponse;
import com.tamtam.core.infrastructure.tour.dto.DetailCommonResponse;
import com.tamtam.core.infrastructure.tour.dto.DetailImageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "tourApiClient", url = "${tour-api.base-url}")
public interface TourApiClient {

    @GetMapping("/areaBasedList2")
    AreaBasedListResponse getAreaBasedList(
        @RequestParam("serviceKey") String serviceKey,
        @RequestParam("MobileOS") String mobileOs,
        @RequestParam("MobileApp") String mobileApp,
        @RequestParam("_type") String type,
        @RequestParam("arrange") String arrange,
        @RequestParam("contentTypeId") String contentTypeId,
        @RequestParam("lDongRegnCd") String lDongRegnCd,
        @RequestParam("numOfRows") int numOfRows,
        @RequestParam("pageNo") int pageNo
    );

    @GetMapping("/detailCommon2")
    DetailCommonResponse getDetailCommon(
        @RequestParam("serviceKey") String serviceKey,
        @RequestParam("MobileOS") String mobileOs,
        @RequestParam("MobileApp") String mobileApp,
        @RequestParam("_type") String type,
        @RequestParam("contentId") String contentId
    );

    @GetMapping("/detailImage2")
    DetailImageResponse getDetailImage(
        @RequestParam("serviceKey") String serviceKey,
        @RequestParam("MobileOS") String mobileOs,
        @RequestParam("MobileApp") String mobileApp,
        @RequestParam("_type") String type,
        @RequestParam("contentId") String contentId,
        @RequestParam("imageYN") String imageYn
    );
}
