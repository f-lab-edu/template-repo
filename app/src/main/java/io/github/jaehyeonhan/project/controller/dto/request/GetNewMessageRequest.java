package io.github.jaehyeonhan.project.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class GetNewMessageRequest {
    private final String userId;

    @Schema(example = "2025-11-22T02:53:40.578")
    private final LocalDateTime lastRead;
}
