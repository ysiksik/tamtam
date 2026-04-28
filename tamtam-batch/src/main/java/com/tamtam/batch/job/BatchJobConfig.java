package com.tamtam.batch.job;

import com.tamtam.batch.config.DetailImageBatchProperties;
import com.tamtam.batch.config.TourApiProperties;
import com.tamtam.batch.exception.BatchApiException;
import com.tamtam.batch.job.dto.DetailCommonTarget;
import com.tamtam.batch.job.dto.DetailCommonUpdateCommand;
import com.tamtam.batch.job.dto.DetailImageTarget;
import com.tamtam.batch.job.dto.DetailImageUpdateCommand;
import com.tamtam.batch.job.dto.TourAttractionUpsertCommand;
import com.tamtam.batch.policy.BatchApiRetryPolicy;
import com.tamtam.batch.policy.BatchApiStatusAwareBackOffPolicy;
import com.tamtam.core.infrastructure.tour.dto.AreaBasedListResponse.AreaBasedListItem;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.RetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@EnableConfigurationProperties(TourApiProperties.class)
public class BatchJobConfig {

    private static final int CHUNK_SIZE = 100;

    @Bean
    public Job tourAreaBasedListJob(
        JobRepository jobRepository,
        Step tourAreaBasedListStep,
        Step detailCommonStep,
        Step detailImageStep
    ) {
        return new JobBuilder("tourAreaBasedListJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(tourAreaBasedListStep)
            .next(detailCommonStep)
            .next(detailImageStep)
            .build();
    }

    @Bean
    public Step tourAreaBasedListStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        AreaBasedListItemReader reader,
        TourAttractionItemProcessor processor,
        TourAttractionJdbcItemWriter writer
    ) {
        return new StepBuilder("tourAreaBasedListStep", jobRepository)
            .<AreaBasedListItem, TourAttractionUpsertCommand>chunk(CHUNK_SIZE, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }

    @Bean
    public Step detailCommonStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        JdbcCursorItemReader<DetailCommonTarget> detailCommonItemReader,
        DetailCommonItemProcessor detailCommonItemProcessor,
        DetailCommonJdbcItemWriter detailCommonJdbcItemWriter
    ) {
        return new StepBuilder("detailCommonStep", jobRepository)
            .<DetailCommonTarget, DetailCommonUpdateCommand>chunk(CHUNK_SIZE, transactionManager)
            .reader(detailCommonItemReader)
            .processor(detailCommonItemProcessor)
            .writer(detailCommonJdbcItemWriter)
            .build();
    }

    @Bean
    public Step detailImageStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        JdbcCursorItemReader<DetailImageTarget> detailImageItemReader,
        DetailImageItemProcessor detailImageItemProcessor,
        DetailImageJdbcItemWriter detailImageJdbcItemWriter,
        DetailImageBatchProperties detailImageBatchProperties
    ) {
        return new StepBuilder("detailImageStep", jobRepository)
            .<DetailImageTarget, DetailImageUpdateCommand>chunk(CHUNK_SIZE, transactionManager)
            .reader(detailImageItemReader)
            .processor(detailImageItemProcessor)
            .writer(detailImageJdbcItemWriter)
            .faultTolerant()
            .retryPolicy(detailImageRetryPolicy(detailImageBatchProperties))
            .backOffPolicy(detailImageBackOffPolicy(detailImageBatchProperties))
            .skipPolicy(detailImageSkipPolicy(detailImageBatchProperties))
            .listener(detailImageSkipListener())
            .build();
    }

    private RetryPolicy detailImageRetryPolicy(DetailImageBatchProperties properties) {
        return new BatchApiRetryPolicy(properties.getRetryLimit());
    }

    private BackOffPolicy detailImageBackOffPolicy(DetailImageBatchProperties properties) {
        return new BatchApiStatusAwareBackOffPolicy(
            properties.getRetry429BackoffMillis(),
            properties.getRetry5xxBackoffMillis()
        );
    }

    private SkipPolicy detailImageSkipPolicy(DetailImageBatchProperties properties) {
        return (throwable, skipCount) -> throwable instanceof BatchApiException batchApiException
            && batchApiException.isSkippable()
            && skipCount < properties.getSkipLimit();
    }

    private SkipListener<DetailImageTarget, DetailImageUpdateCommand> detailImageSkipListener() {
        return new SkipListener<>() {
            @Override
            public void onSkipInProcess(@NonNull DetailImageTarget item, @NonNull Throwable t) {
                if (t instanceof BatchApiException batchApiException) {
                    log.warn(
                        "Skipped detailImage2 item. contentId={}, status={}, reason={}",
                        batchApiException.getContentId(),
                        batchApiException.getStatusCode(),
                        batchApiException.getMessage()
                    );
                }
            }
        };
    }

    @Bean
    public JdbcCursorItemReader<DetailCommonTarget> detailCommonItemReader(DataSource dataSource) {
        JdbcCursorItemReader<DetailCommonTarget> reader = new JdbcCursorItemReader<>();
        reader.setName("detailCommonItemReader");
        reader.setDataSource(dataSource);
        reader.setSql("""
            SELECT content_id, content_type_id, modified_time
              FROM tour_attractions
             WHERE detail_common_synced_at IS NULL
                OR COALESCE(detail_common_source_modified_time, '') <> COALESCE(modified_time, '')
             ORDER BY id
            """);
        reader.setRowMapper((rs, rowNum) -> new DetailCommonTarget(
            rs.getString("content_id"),
            rs.getString("content_type_id"),
            rs.getString("modified_time")
        ));
        return reader;
    }

    @Bean
    public JdbcCursorItemReader<DetailImageTarget> detailImageItemReader(DataSource dataSource) {
        JdbcCursorItemReader<DetailImageTarget> reader = new JdbcCursorItemReader<>();
        reader.setName("detailImageItemReader");
        reader.setDataSource(dataSource);
        reader.setSql("""
            SELECT content_id, modified_time
              FROM tour_attractions
             WHERE detail_image_synced_at IS NULL
                OR COALESCE(detail_image_source_modified_time, '') <> COALESCE(modified_time, '')
             ORDER BY id
            """);
        reader.setRowMapper((rs, rowNum) -> new DetailImageTarget(
            rs.getString("content_id"),
            rs.getString("modified_time")
        ));
        return reader;
    }

    @Bean
    public Job noopJob(JobRepository jobRepository, Step noopStep) {
        return new JobBuilder("noopJob", jobRepository)
            .start(noopStep)
            .build();
    }

    @Bean
    public Step noopStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("noopStep", jobRepository)
            .tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED, transactionManager)
            .build();
    }
}
