package com.tamtam.batch.exception;

public class BatchApiException extends RuntimeException {

    public enum Handling {
        RETRYABLE,
        SKIPPABLE,
        FATAL
    }

    private final int statusCode;
    private final String contentId;
    private final Handling handling;

    private BatchApiException(String message, int statusCode, String contentId, Handling handling, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.contentId = contentId;
        this.handling = handling;
    }

    public static BatchApiException retryable(String message, int statusCode, String contentId, Throwable cause) {
        return new BatchApiException(message, statusCode, contentId, Handling.RETRYABLE, cause);
    }

    public static BatchApiException skippable(String message, int statusCode, String contentId, Throwable cause) {
        return new BatchApiException(message, statusCode, contentId, Handling.SKIPPABLE, cause);
    }

    public static BatchApiException fatal(String message, int statusCode, String contentId, Throwable cause) {
        return new BatchApiException(message, statusCode, contentId, Handling.FATAL, cause);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getContentId() {
        return contentId;
    }

    public Handling getHandling() {
        return handling;
    }

    public boolean isRetryable() {
        return handling == Handling.RETRYABLE;
    }

    public boolean isSkippable() {
        return handling == Handling.SKIPPABLE || handling == Handling.RETRYABLE;
    }
}
