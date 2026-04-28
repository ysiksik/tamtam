package com.tamtam.batch.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.tamtam.batch.job.dto.DetailImageRowCommand;
import com.tamtam.batch.job.dto.DetailImageUpdateCommand;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class DetailImageJdbcItemWriterTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void write_shouldDeduplicateDuplicateOriginImageUrlsBeforeInsert() {
        DetailImageJdbcItemWriter writer = new DetailImageJdbcItemWriter(jdbcTemplate);

        DetailImageUpdateCommand command = new DetailImageUpdateCommand(
            "125448",
            "20260423170000",
            List.of(
                new DetailImageRowCommand("125448", "first", "https://example.com/a.jpg", "https://example.com/a_s.jpg", "1"),
                new DetailImageRowCommand("125448", "duplicate", "https://example.com/a.jpg", "https://example.com/a2_s.jpg", "2")
            )
        );

        writer.write(new Chunk<>(List.of(command)));

        ArgumentCaptor<BatchPreparedStatementSetter> setterCaptor = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);
        verify(jdbcTemplate).batchUpdate(anyString(), setterCaptor.capture());
        assertThat(setterCaptor.getValue().getBatchSize()).isEqualTo(1);

        verify(jdbcTemplate).update(anyString(), eq("125448"));
        verify(jdbcTemplate).update(anyString(), eq("20260423170000"), eq("125448"));
    }
}
