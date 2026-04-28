package com.tamtam.batch.job;

import com.tamtam.batch.config.TourApiProperties;
import com.tamtam.core.infrastructure.tour.TourApiClient;
import com.tamtam.core.infrastructure.tour.dto.AreaBasedListResponse;
import com.tamtam.core.infrastructure.tour.dto.AreaBasedListResponse.AreaBasedListItem;
import feign.FeignException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Slf4j
@StepScope
@Component
@RequiredArgsConstructor
public class AreaBasedListItemReader implements ItemReader<AreaBasedListItem> {

    private static final String SUCCESS_CODE = "0000";

    private final TourApiClient tourApiClient;
    private final TourApiProperties properties;

    private int nextPageNo = 1;
    private int totalCount = -1;
    private Iterator<AreaBasedListItem> currentItems = Collections.emptyIterator();

    @Override
    public AreaBasedListItem read() {
        validateProperties();

        while (!currentItems.hasNext()) {
            if (isFinished()) {
                return null;
            }
            currentItems = fetchPage(nextPageNo).iterator();
            nextPageNo++;
        }

        return currentItems.next();
    }

    private boolean isFinished() {
        return totalCount >= 0 && (nextPageNo - 1) * properties.numOfRows() >= totalCount;
    }

    private List<AreaBasedListItem> fetchPage(int pageNo) {
        AreaBasedListResponse response;
        try {
            response = tourApiClient.getAreaBasedList(
                properties.serviceKey(),
                properties.mobileOs(),
                properties.mobileApp(),
                properties.type(),
                properties.arrange(),
                properties.contentTypeId(),
                properties.lDongRegnCd(),
                properties.numOfRows(),
                pageNo
            );
        } catch (FeignException e) {
            throw new IllegalStateException(
                "Tour API request failed. status=%d, pageNo=%d. Check TOUR_API_SERVICE_KEY authorization."
                    .formatted(e.status(), pageNo)
            );
        }

        validate(response);
        AreaBasedListResponse.Body body = response.response().body();
        totalCount = body.totalCount();

        List<AreaBasedListItem> items = items(body);
        log.info(
            "Fetched Tour API areaBasedList2 page. pageNo={}, numOfRows={}, pageItemCount={}, totalCount={}",
            pageNo,
            properties.numOfRows(),
            items.size(),
            totalCount
        );
        return items;
    }

    private void validateProperties() {
        if (properties.serviceKey() == null || properties.serviceKey().isBlank()) {
            throw new IllegalStateException("tour-api.service-key is required. Set TOUR_API_SERVICE_KEY.");
        }
    }

    private void validate(AreaBasedListResponse response) {
        if (response == null || response.response() == null || response.response().header() == null) {
            throw new IllegalStateException("Tour API response is empty");
        }

        AreaBasedListResponse.Header header = response.response().header();
        if (!SUCCESS_CODE.equals(header.resultCode())) {
            throw new IllegalStateException(
                "Tour API request failed. resultCode=%s, resultMsg=%s".formatted(header.resultCode(), header.resultMsg())
            );
        }

        if (response.response().body() == null) {
            throw new IllegalStateException("Tour API response body is empty");
        }
    }

    private List<AreaBasedListItem> items(AreaBasedListResponse.Body body) {
        if (body.items() == null || body.items().item() == null) {
            return Collections.emptyList();
        }
        return body.items().item();
    }
}
