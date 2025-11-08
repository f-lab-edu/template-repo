package io.github.jaehyeonhan.project.entity;

import static io.github.jaehyeonhan.project.util.ValidationUtils.requireNonNull;

import io.github.jaehyeonhan.project.exception.InvalidMessageContentException;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    @Id
    private String id;

    private String chatId;
    private String userId;

    private String content;

    private LocalDateTime createdAt;

    public Message(String id, String chatId, String userId, String content) {
        this.id = requireNonNull(id, "id");
        this.chatId = requireNonNull(chatId, "chat id");
        this.userId = requireNonNull(userId, "user id");
        this.content = requireNonNull(content, "content");
        this.createdAt = LocalDateTime.now();

        validateContent(content);
    }

    private void validateContent(String content) {
        if (content.length() > 300) {
            throw new InvalidMessageContentException("Message cannot be longer than 300 characters");
        }
    }
}
