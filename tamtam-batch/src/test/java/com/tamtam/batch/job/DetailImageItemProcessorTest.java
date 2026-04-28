package com.tamtam.batch.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.tamtam.batch.config.DetailImageBatchProperties;
import com.tamtam.batch.config.TourApiProperties;
import com.tamtam.batch.job.dto.DetailImageTarget;
import com.tamtam.batch.job.dto.DetailImageUpdateCommand;
import com.tamtam.batch.job.detailimage.exception.DetailImageApiException;
import com.tamtam.core.infrastructure.tour.TourApiClient;
import com.tamtam.core.infrastructure.tour.dto.DetailImageResponse;
import com.tamtam.core.infrastructure.tour.dto.DetailImageResponse.Body;
import com.tamtam.core.infrastructure.tour.dto.DetailImageResponse.DetailImageItem;
import com.tamtam.core.infrastructure.tour.dto.DetailImageResponse.Header;
import com.tamtam.core.infrastructure.tour.dto.DetailImageResponse.Items;
import com.tamtam.core.infrastructure.tour.dto.DetailImageResponse.TourApiEnvelope;
import feign.FeignException;
import feign.Request;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DetailImageItemProcessorTest {

    @Mock
    private TourApiClient tourApiClient;

    @Test
    void process_shouldThrowRetryableException_whenStatusIs429() {
        DetailImageBatchProperties batchProperties = new DetailImageBatchProperties();
        batchProperties.setApiCallDelayMillis(0);
        DetailImageItemProcessor processor = new DetailImageItemProcessor(tourApiClient, properties(), batchProperties);

        FeignException tooManyRequests = feignException(429, "quota exceeded");
        when(tourApiClient.getDetailImage("service-key", "ETC", "tamtam", "json", "126990", "Y"))
            .thenThrow(tooManyRequests);

        assertThatThrownBy(() -> processor.process(new DetailImageTarget("126990", "20260423170000")))
            .isInstanceOf(DetailImageApiException.class)
            .satisfies(throwable -> assertThat(((DetailImageApiException) throwable).isRetryable()).isTrue())
            .hasMessageContaining("status=429");
    }

    @Test
    void process_shouldThrowSkippableException_whenStatusIs404() {
        DetailImageBatchProperties batchProperties = new DetailImageBatchProperties();
        batchProperties.setApiCallDelayMillis(0);
        DetailImageItemProcessor processor = new DetailImageItemProcessor(tourApiClient, properties(), batchProperties);

        FeignException notFound = feignException(404, "not found");
        when(tourApiClient.getDetailImage("service-key", "ETC", "tamtam", "json", "126990", "Y"))
            .thenThrow(notFound);

        assertThatThrownBy(() -> processor.process(new DetailImageTarget("126990", "20260423170000")))
            .isInstanceOf(DetailImageApiException.class)
            .satisfies(throwable -> assertThat(((DetailImageApiException) throwable).isRetryable()).isFalse())
            .hasMessageContaining("status=404");
    }

    private TourApiProperties properties() {
        return new TourApiProperties(
            "https://apis.data.go.kr/B551011/KorService2",
            "service-key",
            "ETC",
            "tamtam",
            "json",
            "C",
            "12",
            "44",
            100,
            "Y"
        );
    }

    private FeignException feignException(int status, String body) {
        Request request = Request.create(
            Request.HttpMethod.GET,
            "https://apis.data.go.kr/B551011/KorService2/detailImage2",
            Collections.emptyMap(),
            null,
            StandardCharsets.UTF_8,
            null
        );
        Response response = Response.builder()
            .status(status)
            .reason("test")
            .request(request)
            .body(body, StandardCharsets.UTF_8)
            .build();
        return FeignException.errorStatus("TourApiClient#getDetailImage", response);
    }

    @Test
    void process_shouldDeduplicateSameOriginImageUrl() {
        TourApiProperties properties = properties();
        DetailImageBatchProperties batchProperties = new DetailImageBatchProperties();
        batchProperties.setApiCallDelayMillis(0);

        DetailImageItemProcessor processor = new DetailImageItemProcessor(tourApiClient, properties, batchProperties);

        DetailImageResponse response = new DetailImageResponse(
            null,
            null,
            null,
            new TourApiEnvelope(
                new Header("0000", "OK"),
                new Body(
                    new Items(
                        List.of(
                            new DetailImageItem("125448", "first", "https://example.com/a.jpg", "1", "https://example.com/a_s.jpg"),
                            new DetailImageItem("125448", "duplicate", "https://example.com/a.jpg", "2", "https://example.com/a2_s.jpg"),
                            new DetailImageItem("125448", "blank", "   ", "3", "https://example.com/blank_s.jpg")
                        )
                    ),
                    3,
                    1,
                    3
                )
            )
        );

        when(tourApiClient.getDetailImage("service-key", "ETC", "tamtam", "json", "125448", "Y"))
            .thenReturn(response);

        DetailImageUpdateCommand result = processor.process(new DetailImageTarget("125448", "20260423170000"));

        assertThat(result).isNotNull();
        assertThat(result.images()).isNotNull();
        assertThat(result.images()).hasSize(1);
        assertThat(result.images().getFirst().originImageUrl()).isEqualTo("https://example.com/a.jpg");
        assertThat(result.images().getFirst().imageName()).isEqualTo("first");
    }
}
