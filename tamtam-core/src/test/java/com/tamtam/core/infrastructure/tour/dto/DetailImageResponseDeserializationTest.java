package com.tamtam.core.infrastructure.tour.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class DetailImageResponseDeserializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldTreatBlankItemsAsNull() throws Exception {
        String json = """
            {
              "response": {
                "header": {"resultCode": "0000", "resultMsg": "OK"},
                "body": {"items": "", "numOfRows": 0, "pageNo": 1, "totalCount": 0}
              }
            }
            """;

        DetailImageResponse response = objectMapper.readValue(json, DetailImageResponse.class);

        assertThat(response.response()).isNotNull();
        assertThat(response.response().body()).isNotNull();
        assertThat(response.response().body().items()).isNull();
    }

    @Test
    void shouldDeserializeItemsObject() throws Exception {
        String json = """
            {
              "response": {
                "header": {"resultCode": "0000", "resultMsg": "OK"},
                "body": {
                  "items": {
                    "item": [
                      {
                        "contentid": "3371784",
                        "imgname": "main",
                        "originimgurl": "https://example.com/original.jpg",
                        "smallimageurl": "https://example.com/small.jpg",
                        "serialnum": "1"
                      }
                    ]
                  },
                  "numOfRows": 10,
                  "pageNo": 1,
                  "totalCount": 1
                }
              }
            }
            """;

        DetailImageResponse response = objectMapper.readValue(json, DetailImageResponse.class);

        assertThat(response.response().body().items()).isNotNull();
        assertThat(response.response().body().items().item()).hasSize(1);
        assertThat(response.response().body().items().item().getFirst().originimgurl())
            .isEqualTo("https://example.com/original.jpg");
    }

    @Test
    void shouldAcceptSingleItemObject() throws Exception {
        String json = """
            {
              "response": {
                "header": {"resultCode": "0000", "resultMsg": "OK"},
                "body": {
                  "items": {
                    "item": {
                      "contentid": "3371784",
                      "imgname": "main",
                      "originimgurl": "https://example.com/original.jpg",
                      "smallimageurl": "https://example.com/small.jpg",
                      "serialnum": "1"
                    }
                  },
                  "numOfRows": 10,
                  "pageNo": 1,
                  "totalCount": 1
                }
              }
            }
            """;

        DetailImageResponse response = objectMapper.readValue(json, DetailImageResponse.class);

        assertThat(response.response().body().items()).isNotNull();
        assertThat(response.response().body().items().item()).hasSize(1);
        assertThat(response.response().body().items().item().getFirst().contentid()).isEqualTo("3371784");
    }
}
