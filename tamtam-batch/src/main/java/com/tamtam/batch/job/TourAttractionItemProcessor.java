package com.tamtam.batch.job;

import com.tamtam.batch.job.dto.TourAttractionUpsertCommand;
import com.tamtam.core.infrastructure.tour.dto.AreaBasedListResponse.AreaBasedListItem;
import java.math.BigDecimal;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class TourAttractionItemProcessor implements ItemProcessor<AreaBasedListItem, TourAttractionUpsertCommand> {

    @Override
    public TourAttractionUpsertCommand process(AreaBasedListItem item) {
        return new TourAttractionUpsertCommand(
            required(item.contentid(), "contentid"),
            required(item.contenttypeid(), "contenttypeid"),
            required(item.title(), "title"),
            blankToNull(item.addr1()),
            blankToNull(item.addr2()),
            blankToNull(item.zipcode()),
            blankToNull(item.tel()),
            blankToNull(item.firstimage()),
            blankToNull(item.firstimage2()),
            toBigDecimal(item.mapx()),
            toBigDecimal(item.mapy()),
            blankToNull(item.mlevel()),
            blankToNull(item.areacode()),
            blankToNull(item.sigungucode()),
            blankToNull(item.lDongRegnCd()),
            blankToNull(item.lDongRegnNm()),
            blankToNull(item.lDongSignguCd()),
            blankToNull(item.lDongSignguNm()),
            blankToNull(item.cat1()),
            blankToNull(item.cat2()),
            blankToNull(item.cat3()),
            blankToNull(item.createdtime()),
            blankToNull(item.modifiedtime())
        );
    }

    private BigDecimal toBigDecimal(String value) {
        String trimmed = blankToNull(value);
        return trimmed == null ? null : new BigDecimal(trimmed);
    }

    private String required(String value, String fieldName) {
        String trimmed = blankToNull(value);
        if (trimmed == null) {
            throw new IllegalArgumentException("Tour API item has no " + fieldName);
        }
        return trimmed;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
