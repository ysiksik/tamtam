package com.tamtam.batch.job;

import com.tamtam.batch.config.DetailImageBatchProperties;
import com.tamtam.batch.config.TourApiProperties;
import com.tamtam.batch.exception.BatchApiException;
import com.tamtam.batch.job.dto.DetailImageRowCommand;
import com.tamtam.batch.job.dto.DetailImageTarget;
import com.tamtam.batch.job.dto.DetailImageUpdateCommand;
import com.tamtam.core.infrastructure.tour.TourApiClient;
import com.tamtam.core.infrastructure.tour.dto.DetailImageResponse;
import com.tamtam.core.infrastructure.tour.dto.DetailImageResponse.DetailImageItem;
import feign.FeignException;
import feign.codec.DecodeException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetailImageItemProcessor implements ItemProcessor<DetailImageTarget, DetailImageUpdateCommand> {

    private static final String SUCCESS_CODE = "0000";

    private final TourApiClient tourApiClient;
    private final TourApiProperties properties;
    private final DetailImageBatchProperties batchProperties;

    @Override
    public DetailImageUpdateCommand process(DetailImageTarget item) {
        validateProperties();
        applyApiCallDelay();

        DetailImageResponse response;
        try {
            response = tourApiClient.getDetailImage(
                properties.serviceKey(),
                properties.mobileOs(),
                properties.mobileApp(),
                properties.type(),
                item.contentId(),
                properties.detailImageYn()
            );
        } catch (DecodeException e) {
            throw BatchApiException.skippable(
                "Tour API detailImage2 decode failed. contentId=%s, message=%s"
                    .formatted(item.contentId(), e.getMessage()),
                -1,
                item.contentId(),
                e
            );
        } catch (FeignException e) {
            int status = e.status();
            if (status == 429 || status >= 500) {
                throw BatchApiException.retryable(
                    "Tour API detailImage2 request retryable. status=%d, contentId=%s, message=%s"
                        .formatted(status, item.contentId(), e.getMessage()),
                    status,
                    item.contentId(),
                    e
                );
            }
            if (status >= 0) {
                throw BatchApiException.skippable(
                    "Tour API detailImage2 request skippable. status=%d, contentId=%s, message=%s"
                        .formatted(status, item.contentId(), e.getMessage()),
                    status,
                    item.contentId(),
                    e
                );
            }
            throw new IllegalStateException(
                "Tour API detailImage2 request failed. status=%d, contentId=%s, message=%s"
                    .formatted(status, item.contentId(), e.getMessage()),
                e
            );
        }

        validate(item, response);
        List<DetailImageRowCommand> imageCommands = deduplicateByOriginImageUrl(
            images(response).stream()
                .map(image -> new DetailImageRowCommand(
                    item.contentId(),
                    blankToNull(image.imgname()),
                    blankToNull(image.originimgurl()),
                    blankToNull(image.smallimageurl()),
                    blankToNull(image.serialnum())
                ))
                .filter(image -> image.originImageUrl() != null)
                .toList()
        );

        return new DetailImageUpdateCommand(item.contentId(), item.modifiedTime(), imageCommands);
    }

    private void validateProperties() {
        if (properties.serviceKey() == null || properties.serviceKey().isBlank()) {
            throw new IllegalStateException("tour-api.service-key is required. Set TOUR_API_SERVICE_KEY.");
        }
    }

    private void validate(DetailImageTarget item, DetailImageResponse response) {
        if (response == null) {
            throw new IllegalStateException("Tour API detailImage2 response is null. contentId=%s".formatted(item.contentId()));
        }

        if (response.response() == null || response.response().header() == null) {
            if (response.resultCode() != null || response.resultMsg() != null) {
                throw new IllegalStateException(
                    "Tour API detailImage2 request failed. resultCode=%s, resultMsg=%s, contentId=%s"
                        .formatted(response.resultCode(), response.resultMsg(), item.contentId())
                );
            }
            throw new IllegalStateException(
                "Tour API detailImage2 response is empty. contentId=%s, response=%s"
                    .formatted(item.contentId(), response)
            );
        }

        DetailImageResponse.Header header = response.response().header();
        if (!SUCCESS_CODE.equals(header.resultCode())) {
            throw new IllegalStateException(
                "Tour API detailImage2 request failed. resultCode=%s, resultMsg=%s, contentId=%s"
                    .formatted(header.resultCode(), header.resultMsg(), item.contentId())
            );
        }

        if (response.response().body() == null) {
            throw new IllegalStateException("Tour API detailImage2 response body is empty");
        }
    }

    private List<DetailImageItem> images(DetailImageResponse response) {
        if (response.response().body().items() == null || response.response().body().items().item() == null) {
            return Collections.emptyList();
        }
        return response.response().body().items().item();
    }

    private void applyApiCallDelay() {
        long delayMillis = batchProperties.getApiCallDelayMillis();
        if (delayMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting before Tour API detailImage2 call", e);
        }
    }

    private List<DetailImageRowCommand> deduplicateByOriginImageUrl(List<DetailImageRowCommand> images) {
        Set<String> seen = new LinkedHashSet<>();
        return images.stream()
            .filter(image -> seen.add(image.originImageUrl()))
            .toList();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
