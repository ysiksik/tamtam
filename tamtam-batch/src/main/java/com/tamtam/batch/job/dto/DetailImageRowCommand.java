package com.tamtam.batch.job.dto;

public record DetailImageRowCommand(
    String contentId,
    String imageName,
    String originImageUrl,
    String smallImageUrl,
    String serialNum
) {
}
