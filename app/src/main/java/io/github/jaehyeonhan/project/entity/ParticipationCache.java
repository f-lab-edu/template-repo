package io.github.jaehyeonhan.project.entity;

import java.time.LocalDateTime;

public record ParticipationCache(String id, ParticipationRole role, LocalDateTime createdAt) {

    public Participation toParticipation(String userId, String chatId) {
        return Participation.restoreFromCache(id, userId, chatId, role, createdAt);
    }
}
