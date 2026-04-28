package com.tamtam.batch.job.dto;

import java.util.List;

public record DetailImageUpdateCommand(
    String contentId,
    String sourceModifiedTime,
    List<DetailImageRowCommand> images
) {
    public DetailImageUpdateCommand {
        images = images == null ? List.of() : List.copyOf(images);
    }
}
