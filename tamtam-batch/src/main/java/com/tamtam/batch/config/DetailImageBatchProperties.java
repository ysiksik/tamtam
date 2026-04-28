package com.tamtam.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "batch.detail-image")
public class DetailImageBatchProperties {

    private int retryLimit = 5;
    private int skipLimit = 200;
    private long retry429BackoffMillis = 3000;
    private long retry5xxBackoffMillis = 1000;
    private long apiCallDelayMillis = 0;

    public int getRetryLimit() {
        return retryLimit;
    }

    public void setRetryLimit(int retryLimit) {
        this.retryLimit = retryLimit;
    }

    public int getSkipLimit() {
        return skipLimit;
    }

    public void setSkipLimit(int skipLimit) {
        this.skipLimit = skipLimit;
    }

    public long getRetry429BackoffMillis() {
        return retry429BackoffMillis;
    }

    public void setRetry429BackoffMillis(long retry429BackoffMillis) {
        this.retry429BackoffMillis = retry429BackoffMillis;
    }

    public long getRetry5xxBackoffMillis() {
        return retry5xxBackoffMillis;
    }

    public void setRetry5xxBackoffMillis(long retry5xxBackoffMillis) {
        this.retry5xxBackoffMillis = retry5xxBackoffMillis;
    }

    public long getApiCallDelayMillis() {
        return apiCallDelayMillis;
    }

    public void setApiCallDelayMillis(long apiCallDelayMillis) {
        this.apiCallDelayMillis = apiCallDelayMillis;
    }
}
