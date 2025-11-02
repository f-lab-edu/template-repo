package io.github.jaehyeonhan.project.controller.dto.request;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class GetNewMessageRequest {
    private final String userId;
    private final LocalDateTime lastRead;
}
