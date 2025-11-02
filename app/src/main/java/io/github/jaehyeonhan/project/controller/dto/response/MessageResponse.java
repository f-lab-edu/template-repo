package io.github.jaehyeonhan.project.controller.dto.response;

import io.github.jaehyeonhan.project.service.dto.MessageDto;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MessageResponse {
    private final String id;
    private final String content;
    private final LocalDateTime createdAt;

    public static MessageResponse from(MessageDto dto) {
        return new MessageResponse(dto.getId(), dto.getContent(), dto.getCreatedAt());
    }
}
