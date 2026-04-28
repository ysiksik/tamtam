package com.tamtam.core.infrastructure.tour.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DetailImageResponse(
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
        @JsonDeserialize(using = EmptyStringAsNullItemsDeserializer.class)
        Items items,
        int numOfRows,
        int pageNo,
        int totalCount
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Items(
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<DetailImageItem> item
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DetailImageItem(
        String contentid,
        String imgname,
        String originimgurl,
        String serialnum,
        String smallimageurl
    ) {
    }

    static class EmptyStringAsNullItemsDeserializer extends JsonDeserializer<Items> {

        @Override
        public Items deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            if (parser.currentToken() == JsonToken.VALUE_STRING) {
                String text = parser.getValueAsString();
                if (text == null || text.isBlank()) {
                    return null;
                }
            }

            JsonNode node = parser.readValueAsTree();
            if (node == null || node.isNull()) {
                return null;
            }
            if (node.isTextual() && node.asText().isBlank()) {
                return null;
            }
            return parser.getCodec().treeToValue(node, Items.class);
        }
    }
}
