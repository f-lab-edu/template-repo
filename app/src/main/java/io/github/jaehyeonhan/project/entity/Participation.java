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

    private ParticipationRole role;

    private LocalDateTime createdAt;

    public static Participation joinAsUser(String id, String userId, String chatId) {
        return new Participation(id, userId, chatId, ParticipationRole.USER);
    }

    public static Participation joinAsCreator(String id, String userId, String chatId) {
        return new Participation(id, userId, chatId, ParticipationRole.CREATOR);
    }

    private Participation(String id, String userId, String chatId, ParticipationRole role) {
        this.id = requireNonNull(id, "id");
        this.userId = requireNonNull(userId, "user id");
        this.chatId = requireNonNull(chatId, "chat id");
        this.role = requireNonNull(role, "role");
        createdAt = LocalDateTime.now();
    }
}
