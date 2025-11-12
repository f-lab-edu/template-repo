package io.github.jaehyeonhan.project.entity;

import static io.github.jaehyeonhan.project.util.ValidationUtils.requireNonNull;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participation {

    @Id
    private String id;

    private String userId;
    private String chatId;

    private LocalDateTime createdAt;

    public Participation(String id, String userId, String chatId) {
        this.id = requireNonNull(id, "id");
        this.userId = requireNonNull(userId, "user id");
        this.chatId = requireNonNull(chatId, "chat id");
        createdAt = LocalDateTime.now();
    }
}
