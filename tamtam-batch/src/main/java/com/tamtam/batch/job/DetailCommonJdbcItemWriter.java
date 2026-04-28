package com.tamtam.batch.job;

import com.tamtam.batch.job.dto.DetailCommonUpdateCommand;
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
public class DetailCommonJdbcItemWriter implements ItemWriter<DetailCommonUpdateCommand> {

    private static final String UPDATE_SQL = """
        UPDATE tour_attractions
           SET homepage = ?,
               overview = ?,
               detail_common_source_modified_time = ?,
               detail_common_synced_at = CURRENT_TIMESTAMP,
               updated_at = CURRENT_TIMESTAMP
         WHERE content_id = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void write(Chunk<? extends DetailCommonUpdateCommand> chunk) {
        if (chunk.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(UPDATE_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DetailCommonUpdateCommand command = chunk.getItems().get(i);
                ps.setString(1, command.homepage());
                ps.setString(2, command.overview());
                ps.setString(3, command.sourceModifiedTime());
                ps.setString(4, command.contentId());
            }

            @Override
            public int getBatchSize() {
                return chunk.size();
            }
        });

        log.info("Updated detailCommon2 fields. chunkSize={}", chunk.size());
    }
}
