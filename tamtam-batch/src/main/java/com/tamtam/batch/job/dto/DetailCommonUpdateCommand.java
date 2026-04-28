package com.tamtam.batch.job.dto;

public record DetailCommonUpdateCommand(
    String contentId,
    String homepage,
    String overview,
    String sourceModifiedTime
) {
}
