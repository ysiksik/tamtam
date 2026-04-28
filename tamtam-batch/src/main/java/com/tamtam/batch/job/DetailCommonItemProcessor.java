package com.tamtam.batch.job;

import com.tamtam.batch.config.TourApiProperties;
import com.tamtam.batch.job.dto.DetailCommonTarget;
import com.tamtam.batch.job.dto.DetailCommonUpdateCommand;
import com.tamtam.core.infrastructure.tour.TourApiClient;
import com.tamtam.core.infrastructure.tour.dto.DetailCommonResponse;
import feign.FeignException;
import feign.codec.DecodeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetailCommonItemProcessor implements ItemProcessor<DetailCommonTarget, DetailCommonUpdateCommand> {

    private static final String SUCCESS_CODE = "0000";

    private final TourApiClient tourApiClient;
    private final TourApiProperties properties;

    @Override
    public DetailCommonUpdateCommand process(DetailCommonTarget item) {
        validateProperties();

        DetailCommonResponse response;
        try {
            response = tourApiClient.getDetailCommon(
                properties.serviceKey(),
                properties.mobileOs(),
                properties.mobileApp(),
                properties.type(),
                item.contentId()
            );
        } catch (DecodeException e) {
            throw new IllegalStateException(
                "Tour API detailCommon2 decode failed. contentId=%s, message=%s"
                    .formatted(item.contentId(), e.getMessage())
            );
        } catch (FeignException e) {
            throw new IllegalStateException(
                "Tour API detailCommon2 request failed. status=%d, contentId=%s, message=%s"
                    .formatted(e.status(), item.contentId(), e.getMessage())
            );
        }

        validate(item, response);
        DetailCommonResponse.DetailCommonItem detailItem = detailItem(response);
        if (detailItem == null) {
            log.warn("Tour API detailCommon2 returned no item. contentId={}", item.contentId());
            return new DetailCommonUpdateCommand(item.contentId(), null, null, item.modifiedTime());
        }

        return new DetailCommonUpdateCommand(
            item.contentId(),
            blankToNull(detailItem.homepage()),
            blankToNull(detailItem.overview()),
            item.modifiedTime()
        );
    }

    private void validateProperties() {
        if (properties.serviceKey() == null || properties.serviceKey().isBlank()) {
            throw new IllegalStateException("tour-api.service-key is required. Set TOUR_API_SERVICE_KEY.");
        }
    }

    private void validate(DetailCommonTarget item, DetailCommonResponse response) {
        if (response == null) {
            throw new IllegalStateException(
                "Tour API detailCommon2 response is null. contentId=%s, contentTypeId=%s"
                    .formatted(item.contentId(), item.contentTypeId())
            );
        }

        if (response.response() == null || response.response().header() == null) {
            if (response.resultCode() != null || response.resultMsg() != null) {
                throw new IllegalStateException(
                    "Tour API detailCommon2 request failed. resultCode=%s, resultMsg=%s, contentId=%s"
                        .formatted(response.resultCode(), response.resultMsg(), item.contentId())
                );
            }
            throw new IllegalStateException(
                "Tour API detailCommon2 response is empty. contentId=%s, contentTypeId=%s, response=%s"
                    .formatted(item.contentId(), item.contentTypeId(), response)
            );
        }

        DetailCommonResponse.Header header = response.response().header();
        if (!SUCCESS_CODE.equals(header.resultCode())) {
            throw new IllegalStateException(
                "Tour API detailCommon2 request failed. resultCode=%s, resultMsg=%s, contentId=%s"
                    .formatted(header.resultCode(), header.resultMsg(), item.contentId())
            );
        }

        if (response.response().body() == null) {
            throw new IllegalStateException("Tour API detailCommon2 response body is empty");
        }
    }

    private DetailCommonResponse.DetailCommonItem detailItem(DetailCommonResponse response) {
        if (response.response().body().items() == null || response.response().body().items().item() == null) {
            return null;
        }
        if (response.response().body().items().item().isEmpty()) {
            return null;
        }
        return response.response().body().items().item().getFirst();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
