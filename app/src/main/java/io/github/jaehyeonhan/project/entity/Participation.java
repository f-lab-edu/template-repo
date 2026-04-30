package io.github.jaehyeonhan.project.entity;

import static io.github.jaehyeonhan.project.util.ValidationUtils.requireNonNull;

import io.github.jaehyeonhan.project.exception.UnauthorizedPromotionException;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participation {

    @Id
    private String id;

    private String userId;
    private String chatId;

    @Setter(AccessLevel.PRIVATE)
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

    public boolean canBlock(Participation target) {
        return this.role.canBlock(target.getRole());
    }

    public boolean canUnblock(Participation target) {
        return this.role.canUnblock(target.getRole()); // block과 같은 권한
    }

    public void promoteToManager(Participation target) {
        if (!this.canPromoteToManager()) {
            throw new UnauthorizedPromotionException("매니저 승격 권한이 없습니다");
        }

        target.setRole(ParticipationRole.MANAGER);
    }

    private boolean canPromoteToManager() {
        return this.role.equals(ParticipationRole.CREATOR);
    }

    static Participation restoreFromCache(String id, String userId, String chatId,
        ParticipationRole role, LocalDateTime createdAt) {
        Participation p = new Participation();
        p.id = id;
        p.userId = userId;
        p.chatId = chatId;
        p.role = role;
        p.createdAt = createdAt;
        return p;
    }
}
