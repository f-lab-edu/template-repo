package io.github.jaehyeonhan.project.entity;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class Participation {

    private final String id;

    private final String userId;
    private final String chatId;

    private final LocalDateTime createdAt;

    public Participation(String id, String userId, String chatId) {
        this.id = id;
        this.userId = userId;
        this.chatId = chatId;
        createdAt = LocalDateTime.now();
    }
}
