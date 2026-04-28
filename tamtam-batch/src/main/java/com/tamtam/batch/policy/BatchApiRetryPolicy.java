package com.tamtam.batch.policy;

import com.tamtam.batch.exception.BatchApiException;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.context.RetryContextSupport;

public class BatchApiRetryPolicy implements RetryPolicy {

    private final int maxAttempts;

    public BatchApiRetryPolicy(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public boolean canRetry(RetryContext context) {
        Throwable throwable = context.getLastThrowable();
        if (throwable == null) {
            return true;
        }
        if (throwable instanceof BatchApiException batchApiException) {
            return batchApiException.isRetryable() && context.getRetryCount() < maxAttempts;
        }
        return false;
    }

    @Override
    public RetryContext open(RetryContext parent) {
        return new RetryContextSupport(parent);
    }

    @Override
    public void close(RetryContext context) {
        // no-op
    }

    @Override
    public void registerThrowable(RetryContext context, Throwable throwable) {
        ((RetryContextSupport) context).registerThrowable(throwable);
    }
}
