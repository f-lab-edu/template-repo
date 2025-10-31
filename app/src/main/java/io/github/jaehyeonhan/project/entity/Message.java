package io.github.jaehyeonhan.project.entity;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class Message {

    private final String id;

    private final String chatId;
    private final String userId;

    private final String message;

    private final LocalDateTime createdAt;

    public Message(String id, String chatId, String userId, String message) {
        this.id = id;
        this.chatId = chatId;
        this.userId = userId;
        this.message = message;

        this.createdAt = LocalDateTime.now();
    }
}
