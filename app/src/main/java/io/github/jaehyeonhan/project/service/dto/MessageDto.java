package io.github.jaehyeonhan.project.service.dto;

import io.github.jaehyeonhan.project.entity.Message;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MessageDto {

    private final String id;

    private final String chatId;
    private final String userId;

    private final String content;

    private final LocalDateTime createdAt;

    public static MessageDto from(Message message) {
        return new MessageDto(message.getId(), message.getChatId(), message.getUserId(),
            message.getContent(), message.getCreatedAt());
    }
}
