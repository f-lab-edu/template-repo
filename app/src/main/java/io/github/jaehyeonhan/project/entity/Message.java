package io.github.jaehyeonhan.project.entity;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class Message {

    private final String id;

    private final String chatId;
    private final String userId;

    private final String content;

    private final LocalDateTime createdAt;

    public Message(String id, String chatId, String userId, String content) {
        this.id = id;
        this.chatId = chatId;
        this.userId = userId;
        this.content = content;

        this.createdAt = LocalDateTime.now();
    }
}
