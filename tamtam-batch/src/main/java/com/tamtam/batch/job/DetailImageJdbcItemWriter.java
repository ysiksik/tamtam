package com.tamtam.batch.job;

import com.tamtam.batch.job.dto.DetailImageRowCommand;
import com.tamtam.batch.job.dto.DetailImageUpdateCommand;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetailImageJdbcItemWriter implements ItemWriter<DetailImageUpdateCommand> {

    private static final String DELETE_IMAGE_SQL = "DELETE FROM tour_attraction_images WHERE content_id = ?";
    private static final String INSERT_IMAGE_SQL = """
        INSERT INTO tour_attraction_images (
            content_id,
            image_name,
            origin_image_url,
            small_image_url,
            serial_num,
            created_at,
            updated_at
        ) VALUES (
            ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        )
        """;
    private static final String UPDATE_ATTRACTION_SYNC_SQL = """
        UPDATE tour_attractions
           SET detail_image_source_modified_time = ?,
               detail_image_synced_at = CURRENT_TIMESTAMP,
               updated_at = CURRENT_TIMESTAMP
         WHERE content_id = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void write(Chunk<? extends DetailImageUpdateCommand> chunk) {
        if (chunk.isEmpty()) {
            return;
        }

        for (DetailImageUpdateCommand command : chunk) {
            jdbcTemplate.update(DELETE_IMAGE_SQL, command.contentId());

            List<DetailImageRowCommand> deduplicatedImages = deduplicateByOriginImageUrl(command.images());
            if (!deduplicatedImages.isEmpty()) {
                jdbcTemplate.batchUpdate(INSERT_IMAGE_SQL, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                        DetailImageRowCommand image = deduplicatedImages.get(i);
                        ps.setString(1, image.contentId());
                        ps.setString(2, image.imageName());
                        ps.setString(3, image.originImageUrl());
                        ps.setString(4, image.smallImageUrl());
                        ps.setString(5, image.serialNum());
                    }

                    @Override
                    public int getBatchSize() {
                        return deduplicatedImages.size();
                    }
                });
            }

            jdbcTemplate.update(UPDATE_ATTRACTION_SYNC_SQL, command.sourceModifiedTime(), command.contentId());
        }

        log.info("Updated detailImage2 fields. chunkSize={}", chunk.size());
    }

    private List<DetailImageRowCommand> deduplicateByOriginImageUrl(List<DetailImageRowCommand> images) {
        Set<String> seen = new LinkedHashSet<>();
        return images.stream()
            .filter(image -> seen.add(image.originImageUrl()))
            .toList();
    }
}
