package com.tamtam.batch.job;

import com.tamtam.batch.job.dto.TourAttractionUpsertCommand;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TourAttractionJdbcItemWriter implements ItemWriter<TourAttractionUpsertCommand> {

    private static final String UPSERT_SQL = """
        MERGE INTO tour_attractions (
            content_id,
            content_type_id,
            title,
            addr1,
            addr2,
            zipcode,
            tel,
            first_image,
            first_image2,
            map_x,
            map_y,
            mlevel,
            area_code,
            sigungu_code,
            ldong_regn_cd,
            ldong_regn_nm,
            ldong_signgu_cd,
            ldong_signgu_nm,
            cat1,
            cat2,
            cat3,
            created_time,
            modified_time,
            updated_at
        ) KEY (content_id) VALUES (
            ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP
        )
        """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void write(Chunk<? extends TourAttractionUpsertCommand> chunk) {
        if (chunk.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(UPSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                TourAttractionUpsertCommand command = chunk.getItems().get(i);
                ps.setString(1, command.contentId());
                ps.setString(2, command.contentTypeId());
                ps.setString(3, command.title());
                ps.setString(4, command.addr1());
                ps.setString(5, command.addr2());
                ps.setString(6, command.zipcode());
                ps.setString(7, command.tel());
                ps.setString(8, command.firstImage());
                ps.setString(9, command.firstImage2());
                ps.setBigDecimal(10, command.mapX());
                ps.setBigDecimal(11, command.mapY());
                ps.setString(12, command.mlevel());
                ps.setString(13, command.areaCode());
                ps.setString(14, command.sigunguCode());
                ps.setString(15, command.lDongRegnCd());
                ps.setString(16, command.lDongRegnNm());
                ps.setString(17, command.lDongSignguCd());
                ps.setString(18, command.lDongSignguNm());
                ps.setString(19, command.cat1());
                ps.setString(20, command.cat2());
                ps.setString(21, command.cat3());
                ps.setString(22, command.createdTime());
                ps.setString(23, command.modifiedTime());
            }

            @Override
            public int getBatchSize() {
                return chunk.size();
            }
        });

        log.info("Upserted tour attractions. chunkSize={}", chunk.size());
    }
}
