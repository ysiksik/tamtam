package com.tamtam.batch.policy;

import com.tamtam.batch.exception.BatchApiException;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.BackOffContext;
import org.springframework.retry.backoff.BackOffInterruptedException;
import org.springframework.retry.backoff.BackOffPolicy;

public class BatchApiStatusAwareBackOffPolicy implements BackOffPolicy {

    private final long tooManyRequestsBackOffMillis;
    private final long serverErrorBackOffMillis;

    public BatchApiStatusAwareBackOffPolicy(long tooManyRequestsBackOffMillis, long serverErrorBackOffMillis) {
        this.tooManyRequestsBackOffMillis = tooManyRequestsBackOffMillis;
        this.serverErrorBackOffMillis = serverErrorBackOffMillis;
    }

    @Override
    public BackOffContext start(RetryContext context) {
        return new BatchApiBackOffContext(context);
    }

    @Override
    public void backOff(BackOffContext backOffContext) throws BackOffInterruptedException {
        BatchApiBackOffContext context = (BatchApiBackOffContext) backOffContext;
        long delayMillis = resolveDelay(context.retryContext.getLastThrowable());
        if (delayMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BackOffInterruptedException("Interrupted while backing off for batch API retry", e);
        }
    }

    private long resolveDelay(Throwable throwable) {
        if (throwable instanceof BatchApiException batchApiException && batchApiException.getStatusCode() == 429) {
            return tooManyRequestsBackOffMillis;
        }
        return serverErrorBackOffMillis;
    }

    private record BatchApiBackOffContext(RetryContext retryContext) implements BackOffContext {
    }
}
